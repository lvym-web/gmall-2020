package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.PmsService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-server")
public interface PmsClient extends PmsService {
}
