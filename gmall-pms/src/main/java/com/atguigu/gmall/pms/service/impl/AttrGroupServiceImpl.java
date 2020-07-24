package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.vo.BaseGroupVO;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrDao attrDao;

    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryGroupByPage(QueryCondition condition, Long catId) {

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(condition),
                new LambdaQueryWrapper<AttrGroupEntity>().eq(catId != null, AttrGroupEntity::getCatelogId, catId));

        return new PageVo(page);
    }

    @Override
    public GroupVO queryGroupByGid(Long gid) {

        GroupVO groupVO = new GroupVO();

        AttrGroupEntity attrGroupEntity = this.getById(gid);
        BeanUtils.copyProperties(attrGroupEntity, groupVO);
        //查询中间表                                    不加this也可以
        List<AttrAttrgroupRelationEntity> relationEntityList = this.relationDao.
                selectList(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>().eq(AttrAttrgroupRelationEntity::getAttrGroupId, gid));
        if (CollectionUtils.isEmpty(relationEntityList)) {
            return groupVO;
        }
        groupVO.setRelations(relationEntityList);
        //获取attrId
        List<Long> attrIds = relationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        //查询AttrEntity
        List<AttrEntity> attrEntities = this.attrDao.selectBatchIds(attrIds);
        groupVO.setAttrEntities(attrEntities);

        return groupVO;
    }

    @Override
    public List<GroupVO> queryGroupBycateId(Long catId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq(catId != null, "catelog_id", catId));
        return attrGroupEntities.stream().map(result -> this.queryGroupByGid(result.getAttrGroupId())).collect(Collectors.toList());
    }

    @Override
    public List<BaseGroupVO> queryBaseGroupVOByCateIdAndSpuId(Long cid, Long spuId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>()
                .eq(cid != null, "catelog_id", cid));
        return attrGroupEntities.stream().map(attrGroupEntity -> {
            BaseGroupVO baseGroupVO = new BaseGroupVO();

            baseGroupVO.setName(attrGroupEntity.getAttrGroupName());
            //查询规格参数及值
            List<AttrAttrgroupRelationEntity> relationEntityList = relationDao
                    .selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_group_id", attrGroupEntity.getAttrGroupId()));
            //获取attr_id
            List<Long> attrIds = relationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            //查询ProductAttrValueEntity
            List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueDao
                    .selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attrIds));
            baseGroupVO.setAttrs(productAttrValueEntities);
            return baseGroupVO;
        }).collect(Collectors.toList());
    }

}