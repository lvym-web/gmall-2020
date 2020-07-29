package com.atguigu.gmall.wms.vo;

import lombok.Data;

@Data
public class SkuLockVO {
    private Long skuId;
    private Integer count;
    private Boolean lock;
    private Long WareId;
    private String orderToken;

}
