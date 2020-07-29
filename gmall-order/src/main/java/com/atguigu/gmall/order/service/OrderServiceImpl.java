package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.core.exception.OrderException;
import com.atguigu.core.exception.RRException;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptors.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVO;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl {

    @Autowired
    private UmsClient umsClient;
    @Autowired
    private CartClient cartClient;
    @Autowired
    private PmsClient pmsClient;
    @Autowired
    private SmsClient smsClient;
    @Autowired
    private WmsClient wmsClient;
    @Autowired
    private OmsClient omsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String ORDER_TOKEN = "order:token:";


    public OrderConfirmVO confirmOrder() {

        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long UserId = userInfo.getId();
        if (UserId == null && UserId <= 0) {
            throw new OrderException("请登录");
        }

        //获取收货地址
        CompletableFuture<Void> addRessCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> memberReceiveAddressByUserId = umsClient.queryMemberReceiveAddressByUserId(UserId);
            List<MemberReceiveAddressEntity> MemberReceiveAddressData = memberReceiveAddressByUserId.getData();
            if (memberReceiveAddressByUserId != null && !CollectionUtils.isEmpty(MemberReceiveAddressData)) {
                orderConfirmVO.setAddress(MemberReceiveAddressData);
            }
        }, threadPoolExecutor);


        //获取购物车中选中的商品清单
        CompletableFuture<Void> CartCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<Cart>> queryCheckCartsByUserId = cartClient.queryCheckCartsByUserId(UserId);
            List<Cart> CartData = queryCheckCartsByUserId.getData();
            if (CollectionUtils.isEmpty(CartData)) {
                throw new OrderException("请勾选商品");
            }
            return CartData;
        }, threadPoolExecutor).thenAcceptAsync(CartData -> {

            List<OrderItemVO> orderItemVOS = CartData.stream().map(cart -> {
                OrderItemVO orderItemVO = new OrderItemVO();

                CompletableFuture<Void> skuInfoCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<SkuInfoEntity> skuInfoEntityResp = pmsClient.querySkuinfoById(cart.getSkuId());
                    SkuInfoEntity skuData = skuInfoEntityResp.getData();
                    if (skuData != null) {
                        orderItemVO.setDefaultImage(skuData.getSkuDefaultImg());
                        orderItemVO.setPrice(skuData.getPrice());
                        orderItemVO.setTitle(skuData.getSkuTitle());
                        orderItemVO.setWeight(skuData.getWeight());
                        orderItemVO.setCount(cart.getCount());
                        orderItemVO.setSkuId(skuData.getSkuId());
                    }
                }, threadPoolExecutor);

                CompletableFuture<Void> saleVOCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<SaleVO>> querySaleVoBySkuId = smsClient.querySaleVoBySkuId(cart.getSkuId());
                    List<SaleVO> saleVOData = querySaleVoBySkuId.getData();
                    if (!CollectionUtils.isEmpty(saleVOData)) {
                        orderItemVO.setSales(saleVOData);
                    }
                }, threadPoolExecutor);

                CompletableFuture<Void> skuSaleCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrValueBySkuId = pmsClient.querySkuSaleAttrValueBySkuId(cart.getSkuId());
                    List<SkuSaleAttrValueEntity> SkuSaleAttrValueData = querySkuSaleAttrValueBySkuId.getData();
                    if (!CollectionUtils.isEmpty(SkuSaleAttrValueData)) {
                        orderItemVO.setSkuSaleAttrValues(SkuSaleAttrValueData);
                    }
                }, threadPoolExecutor);
                CompletableFuture<Void> wareSkuCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<WareSkuEntity>> queryWareSkuBySkuId = wmsClient.queryWareSkuBySkuId(cart.getSkuId());
                    List<WareSkuEntity> WareSkuData = queryWareSkuBySkuId.getData();
                    if (!CollectionUtils.isEmpty(WareSkuData)) {
                        orderItemVO.setStore(WareSkuData.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                    }
                }, threadPoolExecutor);

                CompletableFuture.allOf(skuInfoCompletableFuture, saleVOCompletableFuture, skuSaleCompletableFuture, wareSkuCompletableFuture).join();

                return orderItemVO;

            }).collect(Collectors.toList());
            orderConfirmVO.setOrderItemVOS(orderItemVOS);
        }, threadPoolExecutor);


        //查询用户，获取积分
        CompletableFuture<Void> memberCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<MemberEntity> memberEntityResp = umsClient.queryMemberInfoByUserId(UserId);
            MemberEntity memberEntity = memberEntityResp.getData();
            if (memberEntity != null) {
                orderConfirmVO.setBounds(memberEntity.getIntegration());
            }
        }, threadPoolExecutor);


        //生成唯一标识，防止表单重复提交
        CompletableFuture<Void> TokenCompletableFuture = CompletableFuture.runAsync(() -> {
            String orderToken = IdWorker.getTimeId();
            orderConfirmVO.setOrderToken(orderToken);
            //存redis
            redisTemplate.opsForValue().set(ORDER_TOKEN + orderToken, orderToken);
        }, threadPoolExecutor);

        CompletableFuture.allOf(addRessCompletableFuture, CartCompletableFuture, memberCompletableFuture, TokenCompletableFuture).join();

        return orderConfirmVO;
    }

    public OrderEntity submitOrder(OrderSubmitVO orderSubmitVO) {
        // 1.防止重复提交验证.查询redis中有没有orderToken信息,有,则是第一次提交,放行并删除redis中的orderToken信息
        /*使用LUA脚本保证查询和删除的原子性,删除成功返回1,失败返回0*/
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = orderSubmitVO.getOrderToken();
        Long flag = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(ORDER_TOKEN + orderToken), orderToken);
        if (flag == 0L) {
            throw new RRException("订单不可重复提交");
        }
        // 2.校验价格,总价一致放行
        List<OrderItemVO> itemVOS = orderSubmitVO.getItemVOS();//传递过来的订单清单
        if (CollectionUtils.isEmpty(itemVOS)) {
            throw new RRException("请选择商品再提交");
        }

        BigDecimal currentTotalPrice = itemVOS.stream().map(item -> {
            // 查询数据库中商品的实时价格
            Resp<SkuInfoEntity> skuInfoEntityResp = pmsClient.querySkuinfoById(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null) {
                return new BigDecimal("0");
            }
            return skuInfoEntity.getPrice().multiply(new BigDecimal(item.getCount()));
        }).reduce(new BigDecimal(0), BigDecimal::add);

        BigDecimal totalPrice = orderSubmitVO.getTotalPrice();//总价
        if (currentTotalPrice.compareTo(totalPrice) != 0) {
            throw new RRException("页面已过期,请刷新后重试");
        }

        // 3.校验库存是否足够并锁定库存

        List<SkuLockVO> skuLockVOS = itemVOS.stream().map(item -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(item.getSkuId());
            skuLockVO.setCount(item.getCount());
            skuLockVO.setOrderToken(orderToken);//把order Token放入，做好下单失败，回滚锁定库存
            return skuLockVO;
        }).collect(Collectors.toList());
        Resp<Object> wareResp = wmsClient.checkAndLockStore(skuLockVOS);
        if (wareResp.getCode() != 0) {
            throw new OrderException(wareResp.getMsg());
        }

        // 4.下单
        Resp<OrderEntity> orderEntityResp =null;
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        try {
            orderSubmitVO.setUserId(userInfo.getId());
             orderEntityResp = omsClient.saveOrder(orderSubmitVO);

        } catch (Exception e) {
            e.printStackTrace();
            //解锁库存
            amqpTemplate.convertAndSend("ORDER-EXCHANGE", "stock.unlock", orderToken);

            throw new OrderException("服务器错误，创建订单失败");
        }

        // 5.删除购物车(异步)
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userInfo.getId());
        List<Long> skuIds = itemVOS.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
        map.put("skuId", skuIds);
        amqpTemplate.convertAndSend("ORDER-EXCHANGE", "cart.delete", map);
        // 6.支付
        if (orderEntityResp!=null){
            return orderEntityResp.getData();
        }
        return null;
    }
}
