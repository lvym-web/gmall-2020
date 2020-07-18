package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库信息
 * 
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-07-13 08:42:49
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {
	
}
