package com.atguigu.gmall.oms.listener;

import com.atguigu.core.exception.OrderException;
import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.ums.vo.UserBoundsVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OmsListen {


    @Autowired
    private OrderDao orderDao;
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     *   监听死信队列
     * @param orderToken
     */
    @RabbitListener(queues = {"ORDER-DEAD-QUEUE"})
    public void orderClose(String orderToken){
               //判断订单是否超时未支付，
             if (orderDao.closeOrder(orderToken)==1){
                //解锁库存
                 amqpTemplate.convertAndSend("ORDER-EXCHANGE", "stock.unlock", orderToken);
             }
    }
    @RabbitListener(bindings =@QueueBinding(
            value = @Queue(value = "order.pay",durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"order.pay"}
    ))
    public void payOrder(String orderToken){
        //  更新订单
        if ( orderDao.payOrder(orderToken)==1){
            //减库存
            amqpTemplate.convertAndSend("ORDER-EXCHANGE","stock.minus",orderToken);

            //积分
            OrderEntity orderEntity = orderDao.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderToken));
            if (orderEntity==null){
                throw new OrderException("OrderEntity 为空");
            }
            UserBoundsVO userBoundsVO = new UserBoundsVO();
            userBoundsVO.setMemberId(orderEntity.getMemberId());
            userBoundsVO.setGrowth(orderEntity.getGrowth());
            userBoundsVO.setIntegration(orderEntity.getIntegration());
            amqpTemplate.convertAndSend("ORDER-EXCHANGE","user.bounds",orderToken);
        }
    }
}
