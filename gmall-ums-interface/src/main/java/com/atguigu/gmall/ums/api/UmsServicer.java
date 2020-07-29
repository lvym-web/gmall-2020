package com.atguigu.gmall.ums.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface UmsServicer {
    /**
     * 查询功能，根据参数中的用户名和密码查询指定用户
     */
    @GetMapping("ums/member/query")
    Resp<MemberEntity> queryMemLogin(@RequestParam("username") String useranme, @RequestParam("password") String password);
    /**
     * 根据  member_id 查询地址
     * @param userId
     * @return
     */
    @GetMapping("ums/memberreceiveaddress/{userId}")
    Resp<List<MemberReceiveAddressEntity>> queryMemberReceiveAddressByUserId(@PathVariable("userId") Long userId);

    /**
     *   查询member信息
     * @param id
     * @return
     */
    @GetMapping("ums/member/info/{id}")
    Resp<MemberEntity> queryMemberInfoByUserId(@PathVariable("id") Long id);
}
