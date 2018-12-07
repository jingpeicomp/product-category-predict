package com.jinpei.product.category.ml;

import com.jinpei.product.category.config.AppConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.NaiveBayes;
import org.apache.spark.ml.classification.NaiveBayesModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 模型训练
 * Created by liuzhaoming on 2017/8/19.
 */
@Component
@Slf4j
public class Trainer implements Serializable {

    @Autowired
    private JavaSparkContext sparkContext;

    @Autowired
    private SQLContext sqlContext;

    @Autowired
    private AppConfigProperties configProperties;

    private Tokenizer tokenizer;

    private HashingTF hashingTF;

    private IDFModel idfModel;

    private Word2VecModel wvModel;

    /**
     * 通过贝叶斯分类算法训练商品类目预测模型, 特征值提取算法为tf-idf
     *
     * @throws IOException IOException
     */
    public void trainWithTfIdf() throws IOException {
        log.info("Start training model .......");

        JavaRDD<Row> textRowRDD = sparkContext.textFile(configProperties.getTrainData())
                .repartition(140)
                .map(rowString -> {
                    String[] tempStrArray = rowString.split("||");
                    if (tempStrArray.length != 2) {
                        log.info("Invalid line {}", rowString);
                        return null;
                    }

                    double label = NumberUtils.toDouble(StringUtils.strip(tempStrArray[0]));
                    String originText = StringUtils.strip(tempStrArray[1]);
                    String segmentText = StringUtils.strip(tempStrArray[2]);
                    return new Object[]{label, originText, segmentText};
                })
                .filter(Objects::nonNull)
                .map(objects -> RowFactory.create(objects[0], objects[1], objects[2]));

        Metadata labelMeta = Metadata.fromJson("{\"ml_attr\":{\"num_vals\":962,\"type\":\"nominal\"}}");
        StructType schema = new StructType(new StructField[]{
                new StructField("label", DataTypes.DoubleType, false, labelMeta),
                new StructField("origin", DataTypes.StringType, false, Metadata.empty()),
                new StructField("text", DataTypes.StringType, false, Metadata.empty())
        });
        Dataset<Row> sentenceData = sqlContext.createDataFrame(textRowRDD, schema);
        //划分训练数据和测试数据
        Dataset<Row>[] splits = sentenceData.randomSplit(new double[]{0.9, 0.1});
        Dataset<Row> trainData = processFeatureByTfIdf(sentenceData);
        NaiveBayesModel model = trainNaiveBayesModel(trainData);

        evaluateBasicByTfIdf(splits[1], model);
        trainData.unpersist();
        log.info("Finish training model .......");
    }

    /**
     * 通过贝叶斯分类算法训练商品类目预测模型, 特征值提取算法为tf-idf
     *
     * @throws IOException IOException
     */
    public void trainWithWord2Vec() throws IOException {
        log.info("Start training model .......");

        JavaRDD<Row> textRowRDD = sparkContext.textFile(configProperties.getTrainData())
                .repartition(140)
                .map(rowString -> {
                    String[] tempStrArray = rowString.split(" ,, ");
                    if (tempStrArray.length != 3) {
                        log.info("Invalid line " + rowString);
                        return null;
                    }

                    double label = NumberUtils.toDouble(StringUtils.strip(tempStrArray[0]));
                    String originText = StringUtils.strip(tempStrArray[1]);
                    String[] segmentText = tempStrArray[2].split(" ");
                    return new Object[]{label, originText, segmentText};
                })
                .filter(Objects::nonNull)
                .map(objects -> RowFactory.create(objects[0], objects[1], objects[2]));
        Metadata labelMeta = Metadata.fromJson("{\"ml_attr\":{\"num_vals\":962,\"type\":\"nominal\"}}");
        StructType schema = new StructType(new StructField[]{
                new StructField("label", DataTypes.DoubleType, false, labelMeta),
                new StructField("origin", DataTypes.StringType, false, Metadata.empty()),
                new StructField("text", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
        });

        Dataset<Row> sentenceData = sqlContext.createDataFrame(textRowRDD, schema);
        //划分训练数据和测试数据
        Dataset<Row>[] splits = sentenceData.randomSplit(new double[]{0.1, 0.9});
        Dataset<Row> trainData = processFeatureByWord2Vec(splits[0]);
        NaiveBayesModel model = trainNaiveBayesModel(trainData);

        evaluateBasicByWord2Vec(splits[1], model);
        trainData.unpersist();
        log.info("Finish train model .......");
    }

    /**
     * 使用TF-IDF算法对特征值提取、清洗、转换
     *
     * @param sentenceData 原始数据
     * @return 特征值
     * @throws IOException IOException
     */
    private Dataset<Row> processFeatureByTfIdf(Dataset<Row> sentenceData) throws IOException {
        long startTime = System.currentTimeMillis();
        tokenizer = new Tokenizer().setInputCol("text").setOutputCol("words");
        Dataset<Row> wordsData = tokenizer.transform(sentenceData);

        int numFeatures = configProperties.getNumFeatures();
        hashingTF = new HashingTF()
                .setInputCol("words")
                .setOutputCol("rawFeatures")
                .setNumFeatures(numFeatures);
        Dataset<Row> featurizedData = hashingTF.transform(wordsData);

        IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol("features");
        idfModel = idf.fit(featurizedData);
        idfModel.save(configProperties.getIdfModelFile());
        Dataset<Row> rescaledData = idfModel.transform(featurizedData);
        long count = rescaledData.cache().count();
        log.info("Extract feature by tf-idf spends {} ms and train data count is {}",
                (System.currentTimeMillis() - startTime), count);
        return rescaledData;
    }

    /**
     * 通过word2Vector算法对特征值进行提取、清洗、转换
     *
     * @param sentenceData 原始数据
     * @return 特征值
     * @throws IOException IOException
     */
    private Dataset<Row> processFeatureByWord2Vec(Dataset<Row> sentenceData) throws IOException {
        long startTime = System.currentTimeMillis();
        Word2Vec word2Vec = new Word2Vec()
                .setInputCol("text")
                .setOutputCol("features")
                .setVectorSize(configProperties.getNumFeatures())
                .setMinCount(10);

        wvModel = word2Vec.fit(sentenceData);
        wvModel.save(configProperties.getIdfModelFile());
        Dataset<Row> rescaledData = wvModel.transform(sentenceData);
        long count = rescaledData.cache().count();
        log.info("Extract feature by word2Vector spends {} ms and train data count is {}",
                (System.currentTimeMillis() - startTime), count);
        return rescaledData;
    }

    /**
     * 训练bayes模型
     *
     * @param trainData 训练数据
     * @return bayes分类模型
     * @throws IOException IOException
     */
    private NaiveBayesModel trainNaiveBayesModel(Dataset<Row> trainData) throws IOException {
        long startTime = System.currentTimeMillis();
        NaiveBayes nb = new NaiveBayes();
        NaiveBayesModel model = nb.fit(trainData);
        model.save(configProperties.getBayesModelFile());
//        model.transform(trainData.limit(1)).count();
        log.info("Bayes train spends {}", (System.currentTimeMillis() - startTime));
        return model;
    }

    /**
     * 对模型进行评估
     *
     * @param testData 测试数据
     * @param model    分类模型
     * @return 精度
     */
    private double evaluate(Dataset<Row> testData, NaiveBayesModel model) {
        long startTime = System.currentTimeMillis();
        Dataset<Row> resultData = model.transform(testData);
        JavaRDD<Row> predictionRdd = resultData.select("label", "prediction").toJavaRDD().cache();
        long validCount = predictionRdd.filter(row -> Double.compare(row.getDouble(0), row.getDouble(1)) == 0).count();
        long totalCount = predictionRdd.count();
        double accuracy = Double.valueOf(String.valueOf(validCount)) / totalCount;
        predictionRdd.unpersist(false);
        log.info("Bayes evaluate spends {}", (System.currentTimeMillis() - startTime));
        log.info("Valid count is {} , total count is {}, accuracy is {}",
                validCount, totalCount, accuracy);

        return accuracy;
    }

    /**
     * 对模型进行评估, 测试数据为原始文本数据，使用tf-idf对提取特征数据
     *
     * @param testData 测试数据， 原始文本数据
     * @param model    分类模型
     * @return 精度
     */
    private double evaluateBasicByTfIdf(Dataset<Row> testData, NaiveBayesModel model) {
        long startTime = System.currentTimeMillis();
        Dataset<Row> wordsData = tokenizer.transform(testData);
        Dataset<Row> featurizedData = hashingTF.transform(wordsData);
        Dataset<Row> rescaledData = idfModel.transform(featurizedData);

        Dataset<Row>[] splits = rescaledData.randomSplit(new double[]{0.9, 0.1});
        Dataset<Row> predictions = model.transform(splits[0]);
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setLabelCol("label")
                .setPredictionCol("prediction")
                .setMetricName("accuracy");
        double accuracy = evaluator.evaluate(predictions);
        log.info("Test set accuracy = {}   , and spends {}", accuracy, (System.currentTimeMillis() - startTime));

        return accuracy;
    }

    /**
     * 对模型进行评估, 测试数据为原始文本数据，使用word2vec对提取特征数据
     *
     * @param testData 测试数据， 原始文本数据
     * @param model    分类模型
     * @return 精度
     */
    private double evaluateBasicByWord2Vec(Dataset<Row> testData, NaiveBayesModel model) {
        long startTime = System.currentTimeMillis();
        Dataset<Row> wordsData = tokenizer.transform(testData);
        Dataset<Row> rescaledData = wvModel.transform(wordsData);

        Dataset<Row>[] splits = rescaledData.randomSplit(new double[]{0.1, 0.9});
        Dataset<Row> predictions = model.transform(splits[0]);
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setLabelCol("label")
                .setPredictionCol("prediction")
                .setMetricName("accuracy");
        double accuracy = evaluator.evaluate(predictions);
        log.info("Test set accuracy = {}   , and spends {}", accuracy, (System.currentTimeMillis() - startTime));

        return accuracy;
    }
}
