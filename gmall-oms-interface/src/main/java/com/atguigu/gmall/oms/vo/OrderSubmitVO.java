package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVO {
    private String orderToken; //订单号   防重
    private MemberReceiveAddressEntity address;  //收货地址
    private List<OrderItemVO> itemVOS;   //订单清单
    private Integer typePay;  //支付方式
    private String deliveryCompany;   //物流
    private Integer bounds;   //积分  京豆
    private BigDecimal totalPrice;//总价   用于验价
    private Long userId;//用户id，用于传递给订单
}
