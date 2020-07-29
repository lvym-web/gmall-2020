package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.CartService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("cart-server")
public interface CartClient extends CartService {
}
