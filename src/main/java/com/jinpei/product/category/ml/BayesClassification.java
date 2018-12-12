package com.jinpei.product.category.ml;

import com.jinpei.product.category.common.CategoryUtils;
import com.jinpei.product.category.config.AppConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.ml.classification.NaiveBayesModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;

/**
 * 贝叶斯分类器
 * Created by liuzhaoming on 2017/8/19.
 */
@Component
@Slf4j
public class BayesClassification implements Serializable {

    @Autowired
    private AppConfigProperties appConfigProperties;

    private NaiveBayesModel model;

    @PostConstruct
    public void init() {
        loadModel();
    }

    /**
     * 从本地加载训练好的贝叶斯模型
     */
    public synchronized void loadModel() {
        if (StringUtils.isNotBlank(appConfigProperties.getBayesModelFile())
                && CategoryUtils.isFileExist(appConfigProperties.getBayesModelFile())) {
            try {
                model = NaiveBayesModel.load(appConfigProperties.getBayesModelFile());
                log.info("Successfully loading bayes model from {}", appConfigProperties.getBayesModelFile());
            } catch (Exception e) {
                log.error("Cannot load bayes model from {}", appConfigProperties.getBayesModelFile(), e);
            }
        }
    }

    /**
     * 对模型进行评估
     *
     * @param testData 评估数据
     * @return 预测精度
     */
    public double evaluate(Dataset<Row> testData) {
        Dataset<Row> result = model.transform(testData);
        Dataset<Row> predictionAndLabels = result.select("prediction", "label");
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator().setMetricName("accuracy");
        return evaluator.evaluate(predictionAndLabels);
    }

    /**
     * 对数据进行分类
     *
     * @param features 待分类数据
     * @return 数据预测结果
     */
    public Dataset<Row> classify(Dataset<Row> features) {
        return model.transform(features);
    }

    /**
     * 预测向量分类
     *
     * @param feature 向量
     * @return 分类
     */
    public double predict(Vector feature) {
        return model.predict(feature);
    }
}
