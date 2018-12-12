package com.jinpei.product.category.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jinpei.product.category.common.CategoryUtils;
import com.jinpei.product.category.common.ProductCategory;
import com.jinpei.product.category.common.StandardCategory;
import com.jinpei.product.category.ml.CategoryModel;
import com.jinpei.product.category.ml.NlpTokenizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 类目服务
 * Created by liuzhaoming on 2017/8/25.
 */
@RestController
@RequestMapping("/api/categories")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryModel model;

    @Autowired
    private NlpTokenizer nlpTokenizer;

    private static final TypeReference<List<StandardCategory>> categoryVoTypeReference = new TypeReference<List<StandardCategory>>() {
    };

    private Map<Integer, StandardCategory> standardCategoryVoMap = new HashMap<>();


    /**
     * 根据商品名称预测类目
     *
     * @param names 商品名称列表
     * @return 类目列表
     */
    @RequestMapping(value = "/predict", method = {RequestMethod.GET, RequestMethod.POST})
    public List<ProductCategory> predict(@RequestParam(required = false) List<String> names,
                                         @RequestBody(required = false) Map params) {
        List<String> titles = CollectionUtils.isNotEmpty(names) ? names : (List) params.get("names");
        long startTime = System.currentTimeMillis();
        List<ProductCategory> categoryVoList = model.predict(titles);
        log.info("Predict category spends {}", (System.currentTimeMillis() - startTime));

        return categoryVoList.stream()
                .peek(categoryVo -> {
                    StandardCategory standardVo = standardCategoryVoMap.get(categoryVo.getThirdCateId());
                    categoryVo.copyCategory(standardVo);
                })
                .collect(Collectors.toList());
    }

    /**
     * 对商品名称进行分词
     *
     * @param name 商品名称
     * @return 分词结果，不同词条之间用" "分隔
     */
    @RequestMapping(value = "/segment", method = RequestMethod.GET)
    public String segment(@RequestParam String name) {
        return nlpTokenizer.segment(name);
    }


    /**
     * 返回所有标准类目
     *
     * @return 标准类目列表
     */
    @RequestMapping(method = RequestMethod.GET)
    public Collection<StandardCategory> query() {
        return standardCategoryVoMap.values();
    }

    /**
     * 加载标准类目到内存中
     */
    @PostConstruct
    public void loadCategoryMap() {
        try (InputStream is = getClass().getResourceAsStream("/category.json")) {
            String jsonString = IOUtils.toString(is);
            List<StandardCategory> voList = CategoryUtils.fromJson(jsonString, categoryVoTypeReference);
            if (CollectionUtils.isEmpty(voList)) {
                log.error("Cannot load category json file");
                return;
            }
            voList.forEach(vo -> standardCategoryVoMap.put(vo.getThirdCateId(), vo));
        } catch (IOException e) {
            log.error("Cannot load category json file", e);
        }
    }
}
