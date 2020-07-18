package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.vo.SpuInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.entity.SpuInfoDescEntity;
import com.atguigu.gmall.pms.service.SpuInfoDescService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoDescService")
public class SpuInfoDescServiceImpl extends ServiceImpl<SpuInfoDescDao, SpuInfoDescEntity> implements SpuInfoDescService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoDescEntity> page = this.page(
                new Query<SpuInfoDescEntity>().getPage(params),
                new QueryWrapper<SpuInfoDescEntity>()
        );

        return new PageVo(page);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long getaLong(SpuInfoVO spuInfoVO) {
        Long id = spuInfoVO.getId();// 获取新增后的spuId
        List<String> spuImages = spuInfoVO.getSpuImages();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(id);
        spuInfoDescEntity.setDecript(StringUtils.join(spuImages,","));// 把商品的图片描述，保存到spu详情中，图片地址以逗号进行分割
        this.save(spuInfoDescEntity);
        return id;
    }

}