package com.atguigu.gmall.wms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface WmsService {
    /**
     *  获取某个sku的库存信息
     * @param
     * @return
     */
    @GetMapping("wms/waresku/{skuId}")
    public Resp<List<WareSkuEntity>> queryWareSkuBySkuId(@PathVariable("skuId") Long skuId);
}
