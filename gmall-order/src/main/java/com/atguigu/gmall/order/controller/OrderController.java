package com.atguigu.gmall.order.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.order.pay.AlipayTemplate;
import com.atguigu.gmall.order.pay.PayAsyncVo;
import com.atguigu.gmall.order.pay.PayVo;
import com.atguigu.gmall.order.service.OrderServiceImpl;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderServiceImpl orderServiceImpl;
    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     *     订单确认
     * @return
     */
    @GetMapping("/confirm")
    public Resp<OrderConfirmVO> confirmOrder(){
        OrderConfirmVO orderConfirmVO=orderServiceImpl.confirmOrder();
        return Resp.ok(orderConfirmVO);
    }

    @PostMapping("submit")
    public Resp submit(@RequestBody OrderSubmitVO orderSubmitVO){
        OrderEntity orderEntity = orderServiceImpl.submitOrder(orderSubmitVO);

        try {
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no(orderEntity.getOrderSn());
            payVo.setBody("支付平台");
            payVo.setSubject("幂");
            payVo.setTotal_amount("100");
            String form = alipayTemplate.pay(payVo);
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new OrderException("支付失败");
        }
        return Resp.ok("订单提交成功");
    }
    @PostMapping("/pay/success")
    public Resp paySuccess(PayAsyncVo payAsyncVo){

        amqpTemplate.convertAndSend("ORDER-EXCHANGE","order.pay",payAsyncVo.getOut_trade_no());


        return Resp.ok("支付成功");
    }

}
