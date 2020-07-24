package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.BaseGroupVO;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性分组
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-07-11 14:15:26
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryGroupByPage(QueryCondition condition, Long catId);

    GroupVO queryGroupByGid(Long gid);

    List<GroupVO> queryGroupBycateId(Long catId);

    List<BaseGroupVO> queryBaseGroupVOByCateIdAndSpuId(Long cid, Long spuId);
}

