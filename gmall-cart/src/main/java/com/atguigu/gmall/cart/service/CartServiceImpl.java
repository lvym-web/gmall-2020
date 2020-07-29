package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.PmsClient;
import com.atguigu.gmall.cart.feign.SmsClient;
import com.atguigu.gmall.cart.feign.WmsClient;
import com.atguigu.gmall.cart.interceptors.LoginInterceptor;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl {

    private static final String KEY_PREFIX="gamll:cart:";
    private static final String PRICE_PREFIX="gamll:sku:";
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PmsClient pmsClient;
    @Autowired
    private SmsClient smsClient;
    @Autowired
    private WmsClient wmsClient;

    /**
     *   不管什么状态都要添加
     * @param cart
     */
    public void addCart(Cart cart) {
        //获取登录状态
        String key = getLoginState();

        //获取购物车。获取的是用户的hash操作
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
         //获取传递过来的skuid
        String skuId = cart.getSkuId().toString();
           //获取传递过来的数量
        Integer count = cart.getCount();
         //判断购物车是否存在sku
        if (hashOps.hasKey(skuId)){
            //购物车存在
               //获取购物车的sku
            String carString = hashOps.get(skuId).toString();
            //反序列化
             cart = JSON.parseObject(carString, Cart.class);
             //更新数量
            cart.setCount(count+cart.getCount());

        }else {
            //新增购物车
            cart.setCheck(true);
            //sku相关
            Resp<SkuInfoEntity> skuInfoEntityResp = pmsClient.querySkuinfoById(cart.getSkuId());
            SkuInfoEntity skuData = skuInfoEntityResp.getData();
            if (skuData!=null){
                cart.setDefaultImage(skuData.getSkuDefaultImg());
                cart.setPrice(skuData.getPrice());
                cart.setTitle(skuData.getSkuTitle());
            }
             //营销
            Resp<List<SaleVO>> querySaleVoBySkuId = smsClient.querySaleVoBySkuId(cart.getSkuId());
            List<SaleVO> saleVodata = querySaleVoBySkuId.getData();
            if (saleVodata!=null){
                cart.setSales(saleVodata);
            }
            //销售属性
            Resp<List<SkuSaleAttrValueEntity>> listResp = pmsClient.querySkuSaleAttrValueBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> SkuSaleAttrValueData = listResp.getData();
            if (SkuSaleAttrValueData!=null){
                cart.setSkuSaleAttrValues(SkuSaleAttrValueData);
            }
            //库存
            Resp<List<WareSkuEntity>> listResp1 = wmsClient.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> WareSkuData = listResp1.getData();
            if (WareSkuData!=null){
                boolean stock = WareSkuData.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);
                cart.setStore(stock);
            }
              //多存份当前价格,方便同步购物车价格
             redisTemplate.opsForValue().set(PRICE_PREFIX+skuId,skuData.getPrice().toString());
        }
        //添加/更新
        hashOps.put(skuId,JSON.toJSONString(cart));

    }

    public String getLoginState() {
        //获取登录状态
        String key=KEY_PREFIX;
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getId()!=null){
           key+=userInfo.getId();
        }else {
            //游客
            key+=userInfo.getUserKey();
        }
        return key;
    }

    public List<Cart> queryCarts() {
              //获取登录状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //查询未登录的购物车
        String unLoginKey = KEY_PREFIX + userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(unLoginKey);
        List<Object> values = hashOps.values();
        List<Cart> unLoginCartList =null;
        if (!CollectionUtils.isEmpty(values)){
            unLoginCartList = values.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String price = redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(price));
                return cart;
            }).collect(Collectors.toList());
        }
        //判断是否登录，未登陆直接返回
        if (userInfo.getId()==null){
            return unLoginCartList;
        }

        //登录，购物车同步
        String loginKey = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> loginHashOps = redisTemplate.boundHashOps(loginKey);
       //判断未登录购物车是否有
        if (!CollectionUtils.isEmpty(unLoginCartList)){
            unLoginCartList.forEach(cart -> {
                Integer count = cart.getCount();
                if (loginHashOps.hasKey(cart.getSkuId().toString())){
                    String CartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                     cart = JSON.parseObject(CartJson, Cart.class);
                     cart.setCount(count+cart.getCount());
                }
                loginHashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
            });
            //delete未登录的数据库
            redisTemplate.delete(unLoginKey);
        }
        //查询购物车
        List<Object> loginCartList = loginHashOps.values();
        return loginCartList.stream().map(cartJson->{
          Cart  cart= JSON.parseObject(cartJson.toString(), Cart.class);
          //查询  设置当前价格
            String priceString = redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
            cart.setCurrentPrice(new BigDecimal(priceString));
            return cart;
        }).collect(Collectors.toList());
    }

    public void updateCart(Cart cart) {
        //判断登录状态
        String key = this.getLoginState();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

            String skuId = cart.getSkuId().toString();
           if (hashOps.hasKey(skuId)){
               //更新数量
               Integer count = cart.getCount();
               String carts = hashOps.get(skuId).toString();
               //覆盖所传的cart
               cart= JSON.parseObject(carts, Cart.class);
               cart.setCount(count);
                //
           }
        hashOps.put(skuId,JSON.toJSONString(cart));
    }

    public void deleteCart(Long skuId) {
        String loginState = this.getLoginState();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(loginState);
        if (hashOps.hasKey(skuId.toString())){
            redisTemplate.delete(loginState);
        }
    }

    public void deleteCarts(Long[] skuIds) {
//        String key=KEY_PREFIX;
//        UserInfo userInfo = LoginInterceptor.getUserInfo();
//        if (userInfo.getId()!=null){
//            key+=userInfo.getId();
//        }else {
//            //游客
//            key+=userInfo.getUserKey();
//        }
        String loginState = this.getLoginState();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(loginState);
        for (Long skuId : skuIds) {
            if (hashOps.hasKey(skuId.toString())){
                hashOps.delete(skuId.toString());
            }
        }

    }


    public List<Cart> queryCheckCartsByUserId(Long userId) {

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> Cartvalues = hashOps.values();
        List<Cart> cartList = Cartvalues.stream().map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class)).filter(Cart::getCheck).collect(Collectors.toList());
        return cartList;

    }
}
