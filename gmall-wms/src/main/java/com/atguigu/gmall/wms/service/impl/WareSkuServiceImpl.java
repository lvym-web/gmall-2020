package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    @Transactional
    public String checkAndLockStore(List<SkuLockVO> skuLockVOS) {
        if (CollectionUtils.isEmpty(skuLockVOS)){
            return "没有选中商品";
        }
            // 检验并锁定库存
        skuLockVOS.forEach(this::lockStore);

        // 查出库存不够的商品进行提示                                     lockStore方法设置了没有库存的 skuLockVO.setLock(false);
        List<SkuLockVO> unLockSkus = skuLockVOS.stream().filter(skuLockVO -> !skuLockVO.getLock()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(unLockSkus)) {
            // 恢复已锁定的库存
            List<SkuLockVO> lockSkus = skuLockVOS.stream().filter(SkuLockVO::getLock).collect(Collectors.toList());
            lockSkus.forEach(skuLockVO -> {
                this.baseMapper.unLockStore(skuLockVO.getWareId(), skuLockVO.getCount());
            });

            List<Long> ids = unLockSkus.stream().map(SkuLockVO::getSkuId).collect(Collectors.toList());
            return "商品" + ids.toString() + "库存不足,请重新购买";
        }

        // 将锁定的商品信息保存在redis
        String orderToken = skuLockVOS.get(0).getOrderToken();
        redisTemplate.opsForValue().set("WMS_LOCK" + orderToken, JSON.toJSONString(skuLockVOS));

        // 锁定库存后,发送延时消息,定时释放库存
        amqpTemplate.convertAndSend("ORDER-EXCHANGE", "store.ttl", orderToken);


        return null;
    }

    private void lockStore(SkuLockVO skuLockVO) {
        Integer count = skuLockVO.getCount();
        RLock lock = redissonClient.getLock("stock:" + skuLockVO.getSkuId());
        lock.lock();
        // 查询剩余库存够不够
       List<WareSkuEntity> wareSkuEntities=this.baseMapper.checkStore(skuLockVO.getSkuId(), count);
        if (!CollectionUtils.isEmpty(wareSkuEntities)){
            // 锁定库存信息
            Long id = wareSkuEntities.get(0).getId();
            this.baseMapper.lockStore(id, count);
            skuLockVO.setLock(true);
            skuLockVO.setWareId(wareSkuEntities.get(0).getId());
        }else {
            skuLockVO.setLock(false);//方便上面过滤
        }
        lock.unlock();
    }

}