package com.jinpei.product.category.common;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品类目对象
 * Created by liuzhaoming on 2017/8/19.
 */
@Data
@NoArgsConstructor
public class ProductCategory extends StandardCategory {
    /**
     * 商品名称
     */
    private String productName;

    public ProductCategory(String productName, double thirdCategoryId) {
        this.productName = productName;
        setThirdCateId((int) thirdCategoryId);
    }

    /**
     * 复制类目属性
     *
     * @param other 原始类目
     * @return 本类目
     */
    public ProductCategory copyCategory(StandardCategory other) {
        if (null == other) {
            return this;
        }

        setFirstCate(other.getFirstCate());
        setFirstCateId(other.getFirstCateId());
        setSecondCate(other.getSecondCate());
        setSecondCateId(other.getSecondCateId());
        setThirdCate(other.getThirdCate());
        setThirdCateId(other.getThirdCateId());

        return this;
    }
}
