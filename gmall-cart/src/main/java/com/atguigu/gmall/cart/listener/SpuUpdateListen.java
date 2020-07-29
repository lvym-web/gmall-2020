package com.atguigu.gmall.cart.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.PmsClient;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SpuUpdateListen {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PmsClient pmsClient;

    private static final String PRICE_PREFIX="gamll:sku:";

    private static final String KEY_PREFIX="gamll:cart:";

    //购物车同步价格
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value ="SPU.UPDATE",durable = "true"),
            exchange = @Exchange(value="EXCHANGE_CART",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"spu.update"}
    ))
    public void listenUpdate(Long spuId){

        Resp<List<SkuInfoEntity>> querySkuBySpuId = pmsClient.querySkuBySpuId(spuId);
        List<SkuInfoEntity> SkuData = querySkuBySpuId.getData();
        SkuData.forEach(skuInfoEntity -> {
            redisTemplate.opsForValue().set(PRICE_PREFIX+skuInfoEntity.getSkuId(),skuInfoEntity.getPrice().toString());
        });

    }

    /**
     *    创建D订单   删除购物车
     * @param map
     */
     @RabbitListener(bindings = @QueueBinding(
             value = @Queue(value = "CART.DELETE",durable = "true"),
             exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
             key = {"cart.delete"}
     ))
    public void deleteCartListen(Map<String,Object> map){

         String userId = map.get("userId").toString();//用户id
         List<Object> skuIds = (List<Object>) map.get("skuId");//skuid
         BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
         List<String> stringList = skuIds.stream().map(skuid -> skuid.toString()).collect(Collectors.toList());
         String[] ids = stringList.toArray(new String[stringList.size()]);
         hashOps.delete(ids);


     }
}
