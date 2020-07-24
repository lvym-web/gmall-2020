package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.PmsToSms;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.gmall.sms.vo.SkuSlaseVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.SpuInfoDao;
import org.springframework.util.CollectionUtils;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private PmsToSms pmsToSms;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryPageSpu(QueryCondition condition, Long catId) {

        LambdaQueryWrapper<SpuInfoEntity> wrapper = new LambdaQueryWrapper<>();
        // 如果分类id不为0，要根据分类id查，否则查全部
        if (catId != 0) {
            wrapper.eq(SpuInfoEntity::getCatalogId, catId);
        }
        // 如果用户输入了检索条件，根据检索条件查
        String key = condition.getKey();
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(q -> q.eq(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key));
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    @GlobalTransactional
    public void bigSave(SpuInfoVO spuInfoVO) {
        // 1.1. 保存spu基本信息 spu_info
        getSpuInfo(spuInfoVO);
        // 1.2. 保存spu的描述信息 spu_info_desc
        Long id =spuInfoDescService.getaLong(spuInfoVO);

       // int i=1/0;

        // 1.3. 保存spu的规格参数信息
        getSpuBase(spuInfoVO, id);

        // 2.1. 保存sku基本信息
        getSkus(spuInfoVO, id);


            this.sendMessage(id,"insert");


    }

    public void sendMessage(Long id, String type){
        System.out.println("----------------->>>>>sendMessage");
        amqpTemplate.convertAndSend("SPU_ITEM_EXCHANGE","item."+type,id);
    }

    public void getSkus(SpuInfoVO spuInfoVO, Long id) {
        List<SkuInfoVO> skus = spuInfoVO.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(result->{
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(result, skuInfoEntity);
              // 品牌和分类的id需要从spuInfo中获取
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            // 获取随机的uuid作为sku的编码
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString().substring(0, 10).toUpperCase());
            // 获取图片列表
            List<String> images = result.getImages();
            // 如果图片列表不为null，则设置默认图片
            if (!CollectionUtils.isEmpty(images)){
                // 设置第一张图片作为默认图片
                skuInfoEntity.setSkuDefaultImg(skuInfoEntity.getSkuDefaultImg()==null?images.get(0):skuInfoEntity.getSkuDefaultImg());
            }
            skuInfoEntity.setSpuId(id);
            skuInfoService.save(skuInfoEntity);
            // 2.2. 保存sku图片信息
            Long skuId = skuInfoEntity.getSkuId();
            if (!CollectionUtils.isEmpty(images)){

                String defaultImage = images.get(0);
                List<SkuImagesEntity> skuImageses = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setDefaultImg(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setImgUrl(image);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImageses);
            }
            // 2.3. 保存sku的规格参数（销售属性）
            List<SkuSaleAttrValueEntity> saleAttrs = result.getSaleAttrs();
                 saleAttrs.forEach(skuSaleAttrValueEntity -> {
                     // 设置属性名，需要根据id查询AttrEntity
                     skuSaleAttrValueEntity.setAttrName(attrService.getById(skuSaleAttrValueEntity.getAttrId()).getAttrName());
                     skuSaleAttrValueEntity.setAttrSort(0);
                     skuSaleAttrValueEntity.setSkuId(skuId);
                 });
            skuSaleAttrValueService.saveBatch(saleAttrs);

            // 3. 保存营销相关信息，需要远程调用gmall-sms
            SkuSlaseVo skuSlaseVo = new SkuSlaseVo();
          skuSlaseVo.setBuyBounds(result.getBuyBounds());
            skuSlaseVo.setDiscount(result.getDiscount());
            skuSlaseVo.setFullAddOther(result.getFullAddOther());
            skuSlaseVo.setSkuId(skuId);
            skuSlaseVo.setFullCount(result.getFullCount());
            skuSlaseVo.setFullPrice(result.getFullPrice());
            skuSlaseVo.setGrowBounds(result.getGrowBounds());
            skuSlaseVo.setLadderAddOther(result.getLadderAddOther());
            skuSlaseVo.setReducePrice(result.getReducePrice());
            skuSlaseVo.setWork(result.getWork());
                        pmsToSms.saveSkuSaleInfo(skuSlaseVo);
        });
    }

    public void getSpuBase(SpuInfoVO spuInfoVO, Long id) {
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){

            List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(result -> {
                result.setSpuId(id);
                result.setQuickShow(0);//快速展示【是否展示在介绍上；0-否 1-是】
                result.setAttrSort(0);
                return result;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(productAttrValueEntities);
        }
    }



    public void getSpuInfo(SpuInfoVO spuInfoVO) {
        spuInfoVO.setPublishStatus(1); // 默认是已上架
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime()); // 新增时，更新时间和创建时间一致
        this.save(spuInfoVO);
    }

}