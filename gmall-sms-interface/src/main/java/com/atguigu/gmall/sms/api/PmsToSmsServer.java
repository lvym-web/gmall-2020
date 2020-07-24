package com.atguigu.gmall.sms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.vo.SaleVO;
import com.atguigu.gmall.sms.vo.SkuSlaseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface PmsToSmsServer {
    @PostMapping("sms/skubounds/sku/sale/save")
     Resp<Object> saveSkuSaleInfo(@RequestBody SkuSlaseVo skuSlaseVo);
    @GetMapping("sms/skubounds/{skuId}")
    Resp<List<SaleVO>> querySaleVoBySkuId(@PathVariable("skuId") Long skuId);
}
