package com.atguigu.gmall.ums.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.ums.entity.MemberEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface UmsServicer {
    /**
     * 查询功能，根据参数中的用户名和密码查询指定用户
     */
    @GetMapping("ums/member/query")
    Resp<MemberEntity> queryMemLogin(@RequestParam("username") String useranme, @RequestParam("password") String password);
}
