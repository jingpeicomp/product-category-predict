package com.jinpei.product.category.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 标准类目
 * Created by liuzhaoming on 2018/12/12.
 */
@Data
public class StandardCategory implements Serializable {
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
    private int secondCateId;

    /**
     * 二级分类名称
     */
    private String secondCate;

    /**
     * 一级分类ID
     */
    private int firstCateId;

    /**
     * 一级分类名称
     */
    private String firstCate;
}
