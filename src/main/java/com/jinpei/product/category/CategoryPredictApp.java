package com.jinpei.product.category;

import com.jinpei.product.category.config.AppConfigProperties;
import com.jinpei.product.category.ml.CategoryModel;
import com.jinpei.product.category.ml.Trainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * 主程序
 * Created by liuzhaoming on 2017/8/24.
 */
@SpringBootApplication
@Slf4j
public class CategoryPredictApp {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(CategoryPredictApp.class, args);

        try {
            Trainer trainer = applicationContext.getBean(Trainer.class);
            AppConfigProperties configProperties = applicationContext.getBean(AppConfigProperties.class);
            if (configProperties.isTrainWhenStart() && !trainer.isModelExist()) {
                trainer.trainWithTfIdf();
            }
            CategoryModel categoryModel = applicationContext.getBean(CategoryModel.class);
            categoryModel.loadModel();
            log.info("---------------------------------");
            log.info("Finish to start application !");
            log.info("---------------------------------");
        } catch (Exception e) {
            log.error("Cannot start application ", e);
        }
    }
}
