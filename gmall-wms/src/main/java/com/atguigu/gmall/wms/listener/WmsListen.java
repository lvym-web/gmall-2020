package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WmsListen {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private WareSkuDao wareSkuDao;

    /**
     *   下单失败，解锁库存
     * @param orderToken
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "stock.unlock",durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"stock.unlock"}
    ))
    public void unlockListen(String orderToken){
        String skuLockVOSJson = redisTemplate.opsForValue().get("WMS_LOCK" + orderToken);
        List<SkuLockVO> lockSkus = JSON.parseArray(skuLockVOSJson, SkuLockVO.class);
        lockSkus.forEach(skuLockVO -> {
            wareSkuDao.unLockStore(skuLockVO.getWareId(), skuLockVO.getCount());
        });
       // redisTemplate.delete("WMS_LOCK" + orderToken);

    }
    /**
     *   下单成功，减库存
     * @param orderToken
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "stock.minus",durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"stock.minus"}
    ))
    public void minusStockListen(String orderToken){
        String skuLockVOSJson = redisTemplate.opsForValue().get("WMS_LOCK" + orderToken);
        List<SkuLockVO> lockSkus = JSON.parseArray(skuLockVOSJson, SkuLockVO.class);
        lockSkus.forEach(skuLockVO -> {
            wareSkuDao.minusStockStore(skuLockVO.getWareId(), skuLockVO.getCount());
        });

    }
}
