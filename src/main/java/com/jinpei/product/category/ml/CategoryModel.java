package com.jinpei.product.category.ml;

import com.jinpei.product.category.common.CategoryVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 商品类目预测模型
 * Created by liuzhaoming on 2017/8/19.
 */
@Component
public class CategoryModel implements Serializable {

    @Autowired
    private FeatureExtractor featureExtractor;

    @Autowired
    private BayesClassification classification;

    /**
     * 预测商品类目
     *
     * @param productNames 商品名称列表
     * @return 商品类目
     */
    public List<CategoryVo> predict(List<String> productNames) {
        if (CollectionUtils.isEmpty(productNames)) {
            return Collections.emptyList();
        }

        Dataset<Row> features = featureExtractor.extract(productNames);
        Dataset<Row> predictData = classification.classify(features).cache();
        List<Double> categoryIdList = predictData.select("prediction")
                .toJavaRDD()
                .map(row -> row.getDouble(0))
                .collect();

        List<CategoryVo> voList = new ArrayList<>();
        for (int i = 0, length = productNames.size(); i < length; i++) {
            String productName = productNames.get(i);
            double categoryId = categoryIdList.get(i);
            voList.add(new CategoryVo(productName, categoryId));
        }

        return voList;
    }
}
