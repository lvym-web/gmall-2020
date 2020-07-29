package com.atguigu.gmall.cart.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.vo.Cart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface CartService {
    /**
     *   查询购物车选中状态
     * @param userId
     * @return
     */
    @GetMapping("cart/{userId}")
    Resp<List<Cart>> queryCheckCartsByUserId(@PathVariable("userId") Long userId);
}
