package com.jinpei.product.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * 主程序
 * Created by liuzhaoming on 2017/8/24.
 */
@SpringBootApplication
public class ProductCategoryPredictApp {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(ProductCategoryPredictApp.class, args);
//        Trainer trainer = applicationContext.getBean(Trainer.class);
//        try {
//            trainer.train();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
