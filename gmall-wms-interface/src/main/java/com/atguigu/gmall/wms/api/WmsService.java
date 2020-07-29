package com.atguigu.gmall.wms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface WmsService {
    /**
     *  获取某个sku的库存信息
     * @param
     * @return
     */
    @GetMapping("wms/waresku/{skuId}")
    Resp<List<WareSkuEntity>> queryWareSkuBySkuId(@PathVariable("skuId") Long skuId);
    /**
     * 检查并锁定库存
     * @param skuLockVOS
     * @return
     */
    @PostMapping("wms/waresku")
    Resp<Object> checkAndLockStore(@RequestBody List<SkuLockVO> skuLockVOS);
}
