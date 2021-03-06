package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.HomeSubjectSpuEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 专题商品
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-07-11 15:39:34
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageVo queryPage(QueryCondition params);
}

