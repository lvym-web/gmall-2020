package com.atguigu.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.cart.service.CartServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartServiceImpl  cartServiceImpl;

    /**
     *   添加购物车
     * @param cart
     * @return
     */
    @PostMapping
    public Resp<Object> addCart(@RequestBody Cart cart){
        cartServiceImpl.addCart(cart);
        return Resp.ok("添加成功");
    }

    /**
     *      查询购物车
     * @return
     */
    @GetMapping
    public Resp<List<Cart>> queryCarts(){
        List<Cart> cartList=cartServiceImpl.queryCarts();
        return Resp.ok(cartList);
    }
    /**
     *   更新购物车  或选中状态
     */
    @PostMapping("/update")
    public Resp updateCart(@RequestBody Cart cart){
        cartServiceImpl.updateCart(cart);
        return Resp.ok("更新成功");
    }

    /**
     *   删除购物车
     * @param skuId
     * @return
     */
    @PostMapping("/delete/{skuId}")
    public Resp deleteCart(@PathVariable("skuId")Long skuId){
        cartServiceImpl.deleteCart(skuId);
        return Resp.ok("删除成功");
    }
    @PostMapping("/deletes")
    public Resp deletes(@RequestBody Long[] skuIds){
        System.out.println(">>>>>>>>>>>>>>>>>>"+skuIds);
        cartServiceImpl.deleteCarts(skuIds);
        return Resp.ok("删除成功");
    }

    /**
     *   查询购物车选中状态
     * @param userId
     * @return
     */
    @GetMapping("{userId}")
    public Resp<List<Cart>> queryCheckCartsByUserId(@PathVariable("userId") Long userId){
        List<Cart> cartList=cartServiceImpl.queryCheckCartsByUserId(userId);
        return Resp.ok(cartList);
    }
}
