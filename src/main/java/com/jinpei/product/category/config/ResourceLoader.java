package com.jinpei.product.category.config;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置数据
 * Created by liuzhaoming on 2017/8/24.
 */
@Configuration
public class ResourceLoader {
    /**
     * spark配置属性
     *
     * @return spark配置属性
     */
    @Bean
    @ConfigurationProperties(prefix = "category.spark")
    public SparkConfigProperties sparkConfigProperties() {
        return new SparkConfigProperties();
    }

    /**
     * APP配置参数
     *
     * @return 从配置文件中加载配置参数
     */
    @Bean
    @ConfigurationProperties(prefix = "category")
    public AppConfigProperties appConfigProperties() {
        return new AppConfigProperties();
    }

    /**
     * spark上下文
     *
     * @param sparkConfigProperties spark配置属性
     * @return java spark 上下文
     */
    @Bean
    public JavaSparkContext sparkContext(SparkConfigProperties sparkConfigProperties) {
        if (StringUtils.isBlank(sparkConfigProperties.getAppName())
                || StringUtils.isBlank(sparkConfigProperties.getMasterUrl())) {
            throw new IllegalArgumentException("The property : [category.spark.appName] [category.spark.masterUrl] cannot be null ");
        }

        SparkConf sparkConf = new SparkConf().setAppName(sparkConfigProperties.getAppName())
                .setMaster(sparkConfigProperties.getMasterUrl());
        if (StringUtils.isNoneBlank(sparkConfigProperties.getDependenceJar())) {
            sparkConf.setJars(sparkConfigProperties.getDependenceJar().split(","));
        }

        if (MapUtils.isNotEmpty(sparkConfigProperties.getProperties())) {
            sparkConfigProperties.getProperties().forEach((key, value) -> sparkConf.set((String) key, (String) value));
        }

        return new JavaSparkContext(sparkConf);
    }

    /**
     * Spark SQL上下文
     *
     * @param sparkContext java spark上下文
     * @return SQL上下文
     */
    @Bean
    public SQLContext sparkSqlContext(JavaSparkContext sparkContext) {
        return new SQLContext(sparkContext);
    }
}
