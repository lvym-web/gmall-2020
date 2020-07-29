package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.PmsClient;
import com.atguigu.gmall.oms.feign.UmsClient;
import com.atguigu.gmall.oms.service.OrderItemService;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private UmsClient umsClient;
    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public OrderEntity saveOrder(OrderSubmitVO orderSubmitVO) {

        MemberReceiveAddressEntity address = orderSubmitVO.getAddress();
        //保存OrderEntity
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSubmitVO.getOrderToken());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverRegion(address.getRegion());
        //查询member
        Resp<MemberEntity> memberEntityResp = umsClient.queryMemberInfoByUserId(orderSubmitVO.getUserId());
        MemberEntity memberEntity = memberEntityResp.getData();
        orderEntity.setMemberUsername(memberEntity.getUsername());
        orderEntity.setMemberId(memberEntity.getId());

        orderEntity.setIntegration(orderSubmitVO.getBounds());
        orderEntity.setGrowth(0);
        orderEntity.setDeleteStatus(0);
        orderEntity.setStatus(0);
        orderEntity.setDeliveryCompany(orderSubmitVO.getDeliveryCompany());
        orderEntity.setSourceType(1);
        orderEntity.setPayType(orderSubmitVO.getTypePay());
        orderEntity.setTotalAmount(orderSubmitVO.getTotalPrice());
        orderEntity.setCreateTime(new Date());
        orderEntity.setModifyTime(orderEntity.getCreateTime());

        this.save(orderEntity);

            //保存OrderItemEntity
        List<OrderItemVO> itemVOS = orderSubmitVO.getItemVOS();
        itemVOS.forEach(item->{
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setSkuId(item.getSkuId());
            //查询SkuInfoEntity
            Resp<SkuInfoEntity> skuInfoEntityResp = pmsClient.querySkuinfoById(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();

            Resp<SpuInfoEntity> queryspuinfo = pmsClient.queryspuinfo(skuInfoEntity.getSpuId());
            SpuInfoEntity spuInfoEntity = queryspuinfo.getData();

            orderItemEntity.setSkuPrice(skuInfoEntity.getPrice());
            orderItemEntity.setSkuAttrsVals(JSON.toJSONString(item.getSkuSaleAttrValues()));
            orderItemEntity.setCategoryId(skuInfoEntity.getCatalogId());
            orderItemEntity.setOrderId(orderEntity.getId());
            orderItemEntity.setOrderSn(orderSubmitVO.getOrderToken());
            orderItemEntity.setSpuId(spuInfoEntity.getId());
            orderItemEntity.setSkuName(skuInfoEntity.getSkuName());
            orderItemEntity.setSkuPic(skuInfoEntity.getSkuDefaultImg());
            orderItemEntity.setSpuName(spuInfoEntity.getSpuName());
          orderItemEntity.setSkuQuantity(item.getCount());

          orderItemService.save(orderItemEntity);
        });
               // 延迟队列
          amqpTemplate.convertAndSend("ORDER-EXCHANGE","order.ttl",orderSubmitVO.getOrderToken());


        return orderEntity;
    }

}