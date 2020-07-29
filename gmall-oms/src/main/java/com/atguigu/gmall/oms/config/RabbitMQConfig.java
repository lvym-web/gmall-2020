package com.atguigu.gmall.oms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 1. 订单创建成功，发送消息到创建订单的路由
 *    2. 创建订单的路由转发消息给延时队列，延时队列的延时时间就是订单从创建到支付过程，允许的最大等待时间。延时队列不能有消费者（即消息不能被消费）
 *    3. 延时时间一到，消息被转入DLX（死信路由）
 *    4. 死信路由把死信消息转发给死信队列
 *    5. 订单系统监听死信队列，获取到死信消息后，执行关单解库存操作
 */
@Configuration
public class RabbitMQConfig {



    /**
     * 延时队列
     * @return
     */
    @Bean("ORDER-TTL-QUEUE")
    public Queue ttlQueue(){

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "ORDER-EXCHANGE");
        arguments.put("x-dead-letter-routing-key", "order.close");
        arguments.put("x-message-ttl", 120000); // 仅仅用于测试，实际根据需求，通常30分钟或者15分钟

        return new Queue("ORDER-TTL-QUEUE",true,false,false,arguments);
    }

    /**
     *         延时队列绑定到交换机
     * @return
     */
    @Bean("ORDER-TTL-BINDING")
    public Binding ttlBinding(){
        return new Binding("ORDER-TTL-QUEUE", Binding.DestinationType.QUEUE,"ORDER-EXCHANGE","order.ttl",null);
    }

    /**
     *  死信队列
     * @return
     */
    @Bean("ORDER-DEAD-QUEUE")
    public Queue dlQueue(){
        return new Queue("ORDER-DEAD-QUEUE",true,false,false,null);
    }

    /**
     * 死信队列绑定到交换机
     * @return
     */
    @Bean("ORDER-DEAD-BINDING")
    public Binding DeadBinding(){
        return new Binding("ORDER-DEAD-QUEUE", Binding.DestinationType.QUEUE,"ORDER-EXCHANGE","order.close",null);
    }


}
