package com.atguigu.gmall.auth.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.exception.MemberException;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.UmsClient;
import com.atguigu.gmall.ums.entity.MemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties(JwtProperties.class) //spring单例，只声明一次就好，容器中已存在
public class AuthServiceImpl{

    @Autowired
    private UmsClient umsClient;

    @Autowired
    private JwtProperties jwtProperties;

    public String accredit(String username, String password) {
        //远程调用 ，检验用户名密码
        Resp<MemberEntity> memberEntityResp = umsClient.queryMemLogin(username, password);
        MemberEntity memberEntity = memberEntityResp.getData();

        if (memberEntity==null){
            return null;
        }
        //制作JWT
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("id",memberEntity.getId());
            map.put("username",memberEntity.getUsername());
           return JwtUtils.generateToken(map,jwtProperties.getPrivateKey(),jwtProperties.getExpire());
        } catch (Exception e) {
          throw new MemberException("JWT制作出错");
        }

    }
}
