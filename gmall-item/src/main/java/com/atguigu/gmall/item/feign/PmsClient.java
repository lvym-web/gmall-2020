package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.pms.api.PmsService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-server")
public interface PmsClient extends PmsService {
}
