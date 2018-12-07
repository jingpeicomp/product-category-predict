package com.jinpei.product.category.ml;

import com.jinpei.product.category.config.AppConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 特征值抽取
 * Created by liuzhaoming on 2017/8/16.
 */
@Component
@Slf4j
public class FeatureExtractor {

    @Autowired
    private AppConfigProperties configProperties;

    @Autowired
    private JavaSparkContext sparkContext;

    @Autowired
    private SQLContext sqlContext;

    @Autowired
    private NlpTokenizer nlpTokenizer;

    private Tokenizer tokenizer;

    private HashingTF hashingTF;

    private IDFModel idfModel;

    public FeatureExtractor() {
        tokenizer = new Tokenizer()
                .setInputCol("text")
                .setOutputCol("words");
        hashingTF = new HashingTF()
                .setInputCol("words")
                .setOutputCol("rawFeatures")
                .setNumFeatures(configProperties.getNumFeatures());
        loadModel();
    }

    /**
     * 从本地加载训练好的tf-idf模型
     */
    public synchronized void loadModel() {
        if (StringUtils.isNotBlank(configProperties.getIdfModelFile())) {
            try {
                idfModel = IDFModel.load(configProperties.getIdfModelFile());
            } catch (Exception e) {
                log.error("Cannot load tf-idf model from {}", configProperties.getIdfModelFile(), e);
            }
        }
    }

    /**
     * 生成特征向量
     *
     * @param names 商品名称列表
     * @return 特征向量
     */
    public Dataset<Row> extract(List<String> names) {
        List<String> terms = nlpTokenizer.segment(names);
        List<String[]> segmentNames = new ArrayList<>();
        for (int i = 0, length = names.size(); i < length; i++) {
            segmentNames.add(new String[]{StringUtils.strip(names.get(i)), StringUtils.strip(terms.get(i))});
        }

        JavaRDD<Row> textRowRDD = sparkContext.parallelize(segmentNames)
                .map(RowFactory::create);
        StructType schema = new StructType(new StructField[]{
                new StructField("origin", DataTypes.StringType, false, Metadata.empty()),
                new StructField("text", DataTypes.StringType, false, Metadata.empty())
        });

        Dataset<Row> sentenceDataset = sqlContext.createDataFrame(textRowRDD, schema);
        Dataset<Row> wordsDataset = tokenizer.transform(sentenceDataset);
        Dataset<Row> featurizedDataset = hashingTF.transform(wordsDataset);
        return idfModel.transform(featurizedDataset);
    }
}
