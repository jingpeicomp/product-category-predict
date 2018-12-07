package com.jinpei.product.category.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 类目对象
 * Created by liuzhaoming on 2017/8/19.
 */
@Data
@NoArgsConstructor
public class CategoryVo implements Serializable {
    /**
     * 商品名称
     */
    private String productName;

    /**
     * 三级分类ID
     */
    private int thirdCateId;

    /**
     * 三级分类名称
     */
    private String thirdCate;

    /**
     * 二级分类ID
     */
    private String secondCateId;

    /**
     * 二级分类名称
     */
    private String secondCate;

    /**
     * 一级分类ID
     */
    private String firstCateId;

    /**
     * 一级分类名称
     */
    private String firstCate;

    public CategoryVo(String productName, double thirdCategoryId) {
        this.productName = productName;
        this.thirdCateId = (int) thirdCategoryId;
    }

    /**
     * 复制类目属性
     *
     * @param other 原始类目
     * @return 本类目
     */
    public CategoryVo copyCategory(CategoryVo other) {
        if (null == other) {
            return this;
        }

        firstCate = other.firstCate;
        firstCateId = other.firstCateId;
        secondCate = other.secondCate;
        secondCateId = other.secondCateId;
        thirdCate = other.thirdCate;
        thirdCateId = other.thirdCateId;

        return this;
    }
}
