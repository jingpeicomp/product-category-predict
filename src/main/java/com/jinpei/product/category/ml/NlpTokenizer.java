package com.jinpei.product.category.ml;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;
import com.jinpei.product.category.common.CategoryUtils;
import com.jinpei.product.category.config.AppConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 使用结巴+自定义电商词典进行分词
 * Created by liuzhaoming on 2018/12/4.
 */
@Component
@Slf4j
public class NlpTokenizer {

    private static final Pattern VALID_TOKEN_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z]+");

    private JiebaSegmenter segmenter;

    @Autowired
    private AppConfigProperties configProperties;

    /**
     * 分词器初始化，加载用户词典
     */
    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(configProperties.getDictFile())
                && CategoryUtils.isFileExist(configProperties.getDictFile())) {
            log.info("Start loading product dictionary ...");
            WordDictionary.getInstance().loadUserDict(Paths.get(configProperties.getDictFile()));
            log.info("Finish loading product dictionary");
        }

        segmenter = new JiebaSegmenter();
    }

    /**
     * 对短语进行分词, 多个词条之间用“ ”分隔
     *
     * @param sentence 句子
     * @return 分词结果
     */
    public String segment(String sentence) {
        if (StringUtils.isBlank(sentence)) {
            return "";
        }

        List<SegToken> tokens = segmenter.process(sentence, JiebaSegmenter.SegMode.SEARCH);
        return tokens.stream()
                .map(segToken -> segToken.word)
                .filter(this::isValidToken)
                .collect(Collectors.joining(" "));
    }

    /**
     * 批量对短语进行分词
     *
     * @param sentences 句子列表
     * @return 分词列表
     */
    public List<String> segment(List<String> sentences) {
        if (CollectionUtils.isEmpty(sentences)) {
            return Collections.emptyList();
        }

        return sentences.stream()
                .map(this::segment)
                .collect(Collectors.toList());
    }

    /**
     * 过滤词条，过滤掉长度小于2和不以中英文开头的token
     *
     * @return boolean
     */
    private boolean isValidToken(String token) {
        return token.length() >= 2 && VALID_TOKEN_PATTERN.matcher(token).find();
    }
}
