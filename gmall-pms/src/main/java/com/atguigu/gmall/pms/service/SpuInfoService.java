package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * spu信息
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-07-11 14:15:26
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryPageSpu(QueryCondition queryCondition, Long catId);

    void bigSave(SpuInfoVO spuInfoVO);
}

