package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.service.SkuFullReductionService;
import com.atguigu.gmall.sms.service.SkuLadderService;
import com.atguigu.gmall.sms.vo.SaleVO;
import com.atguigu.gmall.sms.vo.SkuSlaseVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionService skuFullReductionService;
    @Autowired
    private SkuLadderService skuLadderService;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Override
    @Transactional
    public void saveSkuSaleInfo(SkuSlaseVo skuSlaseVo) {
        // 3.1. 积分优惠
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSlaseVo, skuBoundsEntity);
        // 数据库保存的是整数0-15，页面绑定是0000-1111
        List<Integer> work = skuSlaseVo.getWork();
        if (!CollectionUtils.isEmpty(work)) {
            skuBoundsEntity.setWork(work.get(0) * 8 + work.get(1) * 4 + work.get(2) * 2 + work.get(3));
        }
        this.save(skuBoundsEntity);

        // 3.2. 满减优惠
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSlaseVo, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSlaseVo.getFullAddOther());
        skuFullReductionService.save(skuFullReductionEntity);

        // 3.3. 数量折扣
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSlaseVo, skuLadderEntity);
        skuLadderService.save(skuLadderEntity);
    }

    @Override
    public List<SaleVO> querySaleVoBySkuId(Long skuId) {

        List<SaleVO> saleVOS=new ArrayList<>();

        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq(skuId != null, "sku_id", skuId));

        if (skuBoundsEntity!=null){
            SaleVO saleVO = new SaleVO();
            saleVO.setType("积分");

            BigDecimal growBounds = skuBoundsEntity.getGrowBounds();
            BigDecimal buyBounds = skuBoundsEntity.getBuyBounds();

            StringBuffer buffer = new StringBuffer();
            if (growBounds!=null && growBounds.intValue()>0){
                       buffer.append("成长积分送:"+growBounds);
            }
            if (buyBounds!=null && buyBounds.intValue()>0){
                if (StringUtils.isNotBlank(buffer)){
                    buffer.append(",");
                }
                buffer.append("购物积分送："+buyBounds);
            }

            saleVO.setName(buffer.toString());
            saleVOS.add(saleVO);
        }
        //查询打折
        SkuLadderEntity skuLadderEntity = skuLadderService.getOne(new QueryWrapper<SkuLadderEntity>().eq(skuId != null, "sku_id", skuId));
         if (skuLadderEntity!=null){
             SaleVO skuLadder = new SaleVO();

             skuLadder.setType("打折");

             BigDecimal discount = skuLadderEntity.getDiscount();
             Integer fullCount = skuLadderEntity.getFullCount();
             StringBuffer stringBuffer = new StringBuffer();
             if (fullCount!=null && fullCount>0){
               stringBuffer.append("满"+fullCount+"件");
             }
             if (discount!=null && discount.intValue()>0){
                 if (fullCount!=null && fullCount>0){
                     stringBuffer.append(",");
                 }
               stringBuffer.append("打"+discount.divide(new BigDecimal(10))+"折");
             }
             skuLadder.setName(stringBuffer.toString());
             saleVOS.add(skuLadder);
         }
        //查询满减
        SkuFullReductionEntity skuFullReductionEntity = skuFullReductionService.getOne(new QueryWrapper<SkuFullReductionEntity>().eq(skuId != null, "sku_id", skuId));
        if (skuFullReductionEntity!=null){
            SaleVO skuFullReduction = new SaleVO();

            skuFullReduction.setType("满减");

            BigDecimal fullPrice = skuFullReductionEntity.getFullPrice();
            BigDecimal reducePrice = skuFullReductionEntity.getReducePrice();

            StringBuffer stringBuffer = new StringBuffer();
            if (fullPrice!=null && fullPrice.intValue()>0){
                stringBuffer.append("满"+fullPrice+"元");
            }
            if (reducePrice!=null && reducePrice.intValue()>0){
                if (fullPrice!=null && fullPrice.intValue()>0){
                    stringBuffer.append(",");
                }
                stringBuffer.append("减"+reducePrice+"元");
            }
            skuFullReduction.setName(stringBuffer.toString());
            saleVOS.add(skuFullReduction);
        }
        return saleVOS;
    }

}