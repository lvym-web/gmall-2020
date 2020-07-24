package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.annotation.RedissonCache;
import com.atguigu.gmall.index.feign.PmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {


    @Autowired
    private PmsClient pmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX="index:cates";

    @Autowired
    private RedissonClient redissonClient;

    public List<CategoryEntity> getCategoryByPidOrLevel() {

        Resp<List<CategoryEntity>> listResp = pmsClient.queryCategoryByPidOrLevel(0l,1);
        return listResp.getData();
    }
    @RedissonCache(prefix = "index:cates",timeout = 6,random = 10)
    public List<CategoryVO> queryCategoryByPidCategoryVO(Long parentCid) {
        //判断缓存是否存在

         //开始分布式锁

        //判断缓存是否存在

            //查询数据库
            List<CategoryVO> categoryVOS = pmsClient.queryCategoryByPidCategoryVO(parentCid).getData();
            //存入redis   没有判空，防止缓存穿透（存空值）                                                 设置随机过期时间，防止缓存雪崩


        return categoryVOS;
    }
}
