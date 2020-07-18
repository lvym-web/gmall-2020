package com.atguigu.gmall.sms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.vo.SkuSlaseVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface PmsToSmsServer {
    @PostMapping("sms/skubounds/sku/sale/save")
    public Resp<Object> saveSkuSaleInfo(@RequestBody SkuSlaseVo skuSlaseVo);
}
