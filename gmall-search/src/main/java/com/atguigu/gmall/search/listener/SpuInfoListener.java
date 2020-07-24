package com.atguigu.gmall.search.listener;

import cn.hutool.core.collection.CollUtil;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttr;
import com.atguigu.gmall.search.feign.PmsClient;
import com.atguigu.gmall.search.feign.WmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpuInfoListener {
    @Autowired
    private PmsClient gmallPmsFeign;
    @Autowired
    private WmsClient wmsClient;
    @Autowired
    private GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "item.create.queue",durable = "true"),
            exchange =@Exchange(name = "SPU_ITEM_EXCHANGE",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
             key = {"item.insert"}))
    public void listenCreate(Long id){
        if (id == null) {
            return;
        }
        Resp<List<SkuInfoEntity>> listResp = gmallPmsFeign.querySkuBySpuId(id);
        List<SkuInfoEntity> skuInfoList = listResp.getData();
        if (CollUtil.isNotEmpty(skuInfoList)) {
            Goods goods = new Goods();
            skuInfoList.forEach(skuInfo -> {
                goods.setSkuId(skuInfo.getSkuId());
                //查询搜索属性
                Resp<List<ProductAttrValueEntity>> attrValueBySpuId = gmallPmsFeign.querySearchAttrValueBySpuId(id);
                List<ProductAttrValueEntity> attrValueBySpuIdData = attrValueBySpuId.getData();
                // lambda的anyMatch使用
                // boolean b = attrValueBySpuIdData.stream().anyMatch(e -> e.getQuickShow() > 0);
                if (!org.springframework.util.CollectionUtils.isEmpty(attrValueBySpuIdData)) {
                    List<SearchAttr> searchAttrs = attrValueBySpuIdData.stream().map(e -> {
                        SearchAttr searchAttr = new SearchAttr();
                        searchAttr.setAttrId(e.getAttrId());
                        searchAttr.setAttrName(e.getAttrName());
                        searchAttr.setAttrValue(e.getAttrValue());
                        return searchAttr;
                    }).collect(Collectors.toList());
                    goods.setAttrs(searchAttrs);
                }
                goods.setBrandId(skuInfo.getBrandId());
                //查询品牌名称
                Resp<BrandEntity> brandResp = gmallPmsFeign.queryBrandById(skuInfo.getBrandId());
                if (brandResp.getData() != null) {
                    goods.setBrandName(brandResp.getData().getName());
                }
                goods.setCategoryId(skuInfo.getCatalogId());
                //查询分类名称
                Resp<CategoryEntity> categoryResp = gmallPmsFeign.queryCategoryById(skuInfo.getCatalogId());
                if (categoryResp.getData() != null) {
                    goods.setCategoryName(categoryResp.getData().getName());
                }
                //查询是否有货
                Resp<List<WareSkuEntity>> wareSkuBySkuId = wmsClient.queryWareSkuBySkuId(skuInfo.getSkuId());
                if (wareSkuBySkuId != null || !CollectionUtils.isEmpty(wareSkuBySkuId.getData())) {
                    List<WareSkuEntity> wareSkuEntities = wareSkuBySkuId.getData();
                    boolean anyMatch = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);

                    goods.setStore(anyMatch);
                }
                Resp<SpuInfoEntity> spuInfoResp = gmallPmsFeign.queryspuinfo(id);
                SpuInfoEntity spuInfo = spuInfoResp.getData();
                goods.setCreateTime(spuInfo.getCreateTime());
                goods.setPic(skuInfo.getSkuDefaultImg());
                goods.setPrice(skuInfo.getPrice().doubleValue());
                goods.setSale(0L);
                goods.setTitle(skuInfo.getSkuTitle());
                goodsRepository.save(goods);
            });
        }
    }
}
