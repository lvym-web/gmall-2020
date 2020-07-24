package com.atguigu.gmall.item.vo;

import lombok.Data;

/**
 * 基本属性名及值
 */
@Data
public class BaseAttrVO {

    private Long attrId;
    private String attrName;
    private String[]  attrValues;
}