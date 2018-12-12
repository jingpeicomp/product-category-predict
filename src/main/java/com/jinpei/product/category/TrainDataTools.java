package com.jinpei.product.category;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 测试数据处理
 * Created by liuzhaoming on 2017/8/24.
 */
public class TrainDataTools {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("/Users/liuzhaoming/project/java/mine-github/product-category-predict/data/train.data.origin");
        FileWriter fw = new FileWriter("/Users/liuzhaoming/project/java/mine-github/product-category-predict/data/train.data");
        BufferedWriter bw = new BufferedWriter(fw);
        Files.lines(path)
                .map(rowString -> {
                    String[] tempStrArray = rowString.split(" ,, ");
                    if (tempStrArray.length != 3) {
                        System.out.println("**************** " + rowString);
                        return null;
                    }
                    double label = NumberUtils.toDouble(StringUtils.strip(tempStrArray[0]));
                    String originText = StringUtils.strip(tempStrArray[1]);
                    return String.join(" |&| ", new String[]{String.valueOf(label), originText, tempStrArray[2]});
                })
                .filter(Objects::nonNull)
                .forEach(line -> {
                    try {
                        bw.write(line);
                        bw.write("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        bw.flush();
    }
}
