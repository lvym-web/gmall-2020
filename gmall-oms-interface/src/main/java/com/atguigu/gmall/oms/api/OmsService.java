package com.atguigu.gmall.oms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface OmsService {
    /**
     *   创建订单
     * @param orderSubmitVO
     * @return
     */
    @PostMapping("oms/order")
    Resp<OrderEntity> saveOrder(@RequestBody OrderSubmitVO orderSubmitVO);
}
