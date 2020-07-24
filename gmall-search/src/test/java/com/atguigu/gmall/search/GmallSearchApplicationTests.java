package com.atguigu.gmall.search;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttr;
import com.atguigu.gmall.search.feign.PmsClient;
import com.atguigu.gmall.search.feign.WmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private PmsClient gmallPmsFeign;
    @Autowired
    private WmsClient wmsClient;

    @Test
    public void importData() {
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);

        Long pageSize = 100l;
        Long pageNum = 1l;

        do {
            // 分页查询已上架商品，即spu中publish_status=1的商品
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setPage(pageNum);
            queryCondition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> resp = gmallPmsFeign.page(queryCondition);
            if (resp == null || CollectionUtils.isEmpty(resp.getData())) {
                break;
            }
            List<SpuInfoEntity> spus = resp.getData();
            // 遍历spu, 查询sku
            //spu==>goods
            List<Goods> goodsList = spus.stream().map(e -> {
                Goods goods = new Goods();
                try {
                    goods = this.buildGoods(e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return goods;
            }).collect(Collectors.toList());

            //保存到elasticsearch
            goodsRepository.saveAll(goodsList);

            pageSize = Long.valueOf(spus.size());
            pageNum++;
        } while (pageSize == 100); // 当前页记录数不等于100，则退出循环
    }

    private Goods buildGoods(SpuInfoEntity spuInfo) {
        Goods goods = new Goods();
        Resp<List<SkuInfoEntity>> listResp = gmallPmsFeign.querySkuBySpuId(spuInfo.getId());
        List<SkuInfoEntity> skus = listResp.getData();
        skus.forEach(skuInfo -> {
            goods.setSkuId(skuInfo.getSkuId());
            //查询搜索属性
            Resp<List<ProductAttrValueEntity>> attrValueBySpuId = gmallPmsFeign.querySearchAttrValueBySpuId(spuInfo.getId());
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
            goods.setBrandId(spuInfo.getBrandId());
            //查询品牌名称
            Resp<BrandEntity> brandResp = gmallPmsFeign.queryBrandById(skuInfo.getBrandId());
            if (brandResp.getData() != null) {
                goods.setBrandName(brandResp.getData().getName());
            }
            goods.setCategoryId(spuInfo.getCatalogId());
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
            goods.setCreateTime(spuInfo.getCreateTime());
            goods.setPic(skuInfo.getSkuDefaultImg());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setSale(0L);
            goods.setTitle(skuInfo.getSkuTitle());

        });

        return goods;
    }


}
