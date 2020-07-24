package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.exception.MemberException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;

import java.util.Date;
import java.util.UUID;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean CheckMemberData(String data, Integer type) {
        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        switch (type){
            case 1:
                queryWrapper.eq("username",data);
                break;
            case 2:
                queryWrapper.eq("mobile",data);
                break;
            case 3:
                queryWrapper.eq("email",data);
                break;
            default:
                return false;
        }
        int count = this.count(queryWrapper);
        return count==0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) {
        //校验手机验证码

        //加盐
        String uuid = UUID.randomUUID().toString().substring(0, 6);
        memberEntity.setSalt(uuid);
        //加盐加密
        memberEntity.setPassword(DigestUtils.md5Hex(memberEntity.getPassword()+uuid));

        //注册
        memberEntity.setCreateTime(new Date());
        memberEntity.setGender(0);
        memberEntity.setGrowth(0);
        memberEntity.setLevelId(0L);
        memberEntity.setStatus(1);
        this.save(memberEntity);
        //删除redis所存验证码
    }

    @Override
    public MemberEntity queryMemLogin(String useranme, String password) {
        //验证用户名
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq(StringUtils.isNotBlank(useranme), "username", useranme));

        if (memberEntity==null){
           throw new MemberException("用户名或密码错误");
        }
        //加盐加密
        password=DigestUtils.md5Hex(password+memberEntity.getSalt());
        //验证密码
        if (!StringUtils.equals(memberEntity.getPassword(),password)){
            throw new MemberException("用户名或密码错误");
        }
        return memberEntity;
    }

}