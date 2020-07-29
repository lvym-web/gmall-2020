package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-07-13 08:42:49
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    List<WareSkuEntity> checkStore(@Param("skuId") Long skuId,@Param("count") Integer count);

    void lockStore(@Param("id") Long id, @Param("count") Integer count);

    void unLockStore(@Param("wareId") Long wareId,@Param("count") Integer count);
    void minusStockStore(@Param("wareId") Long wareId,@Param("count") Integer count);
}
