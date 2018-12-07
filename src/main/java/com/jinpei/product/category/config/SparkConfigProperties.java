package com.jinpei.product.category.config;

import lombok.Data;

import java.io.Serializable;
import java.util.Properties;

/**
 * Spark 配置参数
 * Created by liuzhaoming on 2017/1/12.
 */
@Data
public class SparkConfigProperties implements Serializable {
    /**
     * spring master url
     */
    private String masterUrl;

    /**
     * Spark应用名称
     */
    private String appName;

    /**
     * job jar
     */
    private String dependenceJar;

    /**
     * spark配置属性
     */
    private Properties properties;
}
