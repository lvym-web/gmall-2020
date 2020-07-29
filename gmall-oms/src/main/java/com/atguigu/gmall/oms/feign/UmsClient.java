package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.ums.api.UmsServicer;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-server")
public interface UmsClient extends UmsServicer {
}
