package com.jinpei.product.category.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * APP配置参数
 * Created by liuzhaoming on 2017/8/17.
 */
@Data
public class AppConfigProperties implements Serializable {

    /**
     * idf model文件
     */
    private String idfModelFile;

    /**
     * bayes model file
     */
    private String bayesModelFile;

    /**
     * 训练数据集
     */
    private String trainData;

    /**
     * 特征向量维度
     */
    private int numFeatures = 10000;
}