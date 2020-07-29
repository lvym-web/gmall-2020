package com.atguigu.gmall.oms.dao;


import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-07-26 10:48:49
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    Integer closeOrder(String orderToken);

    Integer payOrder(String orderToken);

}
