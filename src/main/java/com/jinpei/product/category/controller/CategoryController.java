package com.jinpei.product.category.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinpei.product.category.common.CategoryVo;
import com.jinpei.product.category.ml.CategoryModel;
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

    private static final TypeReference<List<CategoryVo>> categoryVoTypeReference = new TypeReference<List<CategoryVo>>() {
    };

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Map<Integer, CategoryVo> standardCategoryVoMap = new HashMap<>();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 根据商品名称预测类目
     *
     * @param names 商品名称列表
     * @return 类目列表
     */
    @RequestMapping(value = "/predict", method = {RequestMethod.GET, RequestMethod.POST})
    public List<CategoryVo> predict(@RequestParam List<String> names, @RequestBody Map params) {
        List<String> titles = CollectionUtils.isNotEmpty(names) ? names : (List) params.get("names");
        long startTime = System.currentTimeMillis();
        List<CategoryVo> categoryVoList = model.predict(titles);
        log.info("Predict category spends {}", (System.currentTimeMillis() - startTime));

        return categoryVoList.stream()
                .peek(categoryVo -> {
                    CategoryVo standardVo = standardCategoryVoMap.get(categoryVo.getThirdCateId());
                    categoryVo.copyCategory(standardVo);
                })
                .collect(Collectors.toList());
    }


    /**
     * 返回所有标准类目
     *
     * @return 标准类目列表
     */
    @RequestMapping(method = RequestMethod.GET)
    public Collection<CategoryVo> query() {
        return standardCategoryVoMap.values();
    }

    /**
     * 加载标准类目到内存中
     */
    @PostConstruct
    public void loadCategoryMap() {
        try (InputStream is = getClass().getResourceAsStream("/category.json")) {
            String jsonString = IOUtils.toString(is);
            List<CategoryVo> voList = OBJECT_MAPPER.readValue(jsonString, categoryVoTypeReference);
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
