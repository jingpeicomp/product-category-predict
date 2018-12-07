package com.jinpei.product.category.tools;

import com.jinpei.product.category.ml.NlpTokenizer;
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
        NlpTokenizer tokenizer = new NlpTokenizer();
        Path path = Paths.get("/Users/liuzhaoming/project/python/pure/user_profile/data/bayes/train.data");
        FileWriter fw = new FileWriter("/Users/liuzhaoming/project/python/pure/user_profile/data/bayes/train.data.merge");
        BufferedWriter bw = new BufferedWriter(fw);
        Files.lines(path)
                .map(rowString -> {
                    String[] tempStrArray = rowString.split(" ,, ");
                    if (tempStrArray.length != 2) {
                        System.out.println("**************** " + rowString);
                        return null;
                    }
                    double label = NumberUtils.toDouble(StringUtils.strip(tempStrArray[0]));
                    String originText = StringUtils.strip(tempStrArray[1]);
//                    List<String> terms = tokenizer.segment(originText);
                    String segmentText = String.join(" ", "");
                    return String.join(" ,, ", new String[]{String.valueOf(label), originText, segmentText});
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
