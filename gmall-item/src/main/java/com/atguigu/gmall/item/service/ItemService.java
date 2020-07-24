package com.atguigu.gmall.item.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.config.ThreadPoolConfig;
import com.atguigu.gmall.item.feign.PmsClient;
import com.atguigu.gmall.item.feign.SmsClient;
import com.atguigu.gmall.item.feign.WmsClient;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.BaseGroupVO;
import com.atguigu.gmall.sms.vo.SaleVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemService {

    @Autowired
    private PmsClient pmsClient;
    @Autowired
    private SmsClient smsClient;
    @Autowired
    private WmsClient wmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    public ItemVO loadData(Long skuId) {
        ItemVO itemVO = new ItemVO();

        itemVO.setSkuId(skuId);
        /*异步编排*/
        // supplyAsync 有返回值
        CompletableFuture<Object> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = pmsClient.querySkuinfoById(skuId);
            SkuInfoEntity skuData = skuInfoEntityResp.getData();
            if (skuData == null) {
                return itemVO;
            }
            //查询sku
            itemVO.setSkuSubtitle(skuData.getSkuSubtitle());
            itemVO.setSkuTitle(skuData.getSkuTitle());
            itemVO.setPrice(skuData.getPrice());
            itemVO.setWeight(skuData.getWeight());
            itemVO.setSpuId(skuData.getSpuId());
            return skuData;
        }, threadPoolExecutor);

        //查询spu          依赖于skuCompletableFuture
        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<SpuInfoEntity> spuInfoEntityResp = pmsClient.queryspuinfo(((SkuInfoEntity) sku).getSpuId());
            SpuInfoEntity spuData = spuInfoEntityResp.getData();
            if (spuData != null) {
                itemVO.setSpuName(spuData.getSpuName());

            }
        }, threadPoolExecutor);

        //查询图片列表
        CompletableFuture<Void> imagesCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<SkuImagesEntity>> listResp = pmsClient.querySkuImagesBySkiId(skuId);
            if (listResp != null && listResp.getData() != null) {
                itemVO.setPics(listResp.getData());
            }
        }, threadPoolExecutor);

        //查询品牌和分类
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<BrandEntity> brandEntityResp = pmsClient.queryBrandById(((SkuInfoEntity) sku).getBrandId());
            if (brandEntityResp != null && brandEntityResp.getData() != null) {
                itemVO.setBrandEntity(brandEntityResp.getData());
            }
        }, threadPoolExecutor);

        //分类
        CompletableFuture<Void> categoryCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<CategoryEntity> categoryEntityResp = pmsClient.queryCategoryById(((SkuInfoEntity) sku).getCatalogId());
            if (categoryEntityResp != null && categoryEntityResp.getData() != null) {
                itemVO.setCategoryEntity(categoryEntityResp.getData());
            }
        }, threadPoolExecutor);

        //查询营销消息
        CompletableFuture<Void> saleVOCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<SaleVO>> querySaleVoBySkuId = smsClient.querySaleVoBySkuId(skuId);
            if (querySaleVoBySkuId != null && querySaleVoBySkuId.getData() != null) {
                itemVO.setSales(querySaleVoBySkuId.getData());
            }
        }, threadPoolExecutor);
        //查询库存
        CompletableFuture<Void> wareSkuCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<WareSkuEntity>> queryWareSkuBySkuId = wmsClient.queryWareSkuBySkuId(skuId);
            if (queryWareSkuBySkuId != null && queryWareSkuBySkuId.getData() != null) {
                itemVO.setStore(queryWareSkuBySkuId.getData().stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }
        }, threadPoolExecutor);
        //销售属性
        CompletableFuture<Void> skuSaleCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrValueBySpuId = pmsClient.querySkuSaleAttrValueBySpuId(((SkuInfoEntity) sku).getSpuId());
            if (querySkuSaleAttrValueBySpuId != null && querySkuSaleAttrValueBySpuId.getData() != null) {
                itemVO.setSaleAttrs(querySkuSaleAttrValueBySpuId.getData());
            }
        }, threadPoolExecutor);
        //查询海报
        CompletableFuture<Void> spuInfoDescCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = pmsClient.querySpuInfoDesc(((SkuInfoEntity) sku).getSpuId());
            if (spuInfoDescEntityResp != null && spuInfoDescEntityResp.getData() != null) {
                String decript = spuInfoDescEntityResp.getData().getDecript();
                String[] split = StringUtils.split(decript, ",");
                itemVO.setImages(Arrays.asList(split));
            }
        }, threadPoolExecutor);
        //查询组及组下规格参数
        CompletableFuture<Void> BaseGroupCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<List<BaseGroupVO>> baseGroupVOByCateIdAndSpuId = pmsClient.queryBaseGroupVOByCateIdAndSpuId(((SkuInfoEntity) sku).getCatalogId(), ((SkuInfoEntity) sku).getSpuId());
            if (baseGroupVOByCateIdAndSpuId != null && baseGroupVOByCateIdAndSpuId.getData() != null) {
                itemVO.setAttrGroups(baseGroupVOByCateIdAndSpuId.getData());
            }
        }, threadPoolExecutor);

        CompletableFuture.allOf(spuCompletableFuture, imagesCompletableFuture, brandCompletableFuture,
                                categoryCompletableFuture, saleVOCompletableFuture, wareSkuCompletableFuture,
                                skuSaleCompletableFuture, spuInfoDescCompletableFuture, BaseGroupCompletableFuture).join();

        return itemVO;
    }

}
