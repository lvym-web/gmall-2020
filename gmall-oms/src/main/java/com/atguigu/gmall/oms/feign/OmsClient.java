package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.oms.api.OmsService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("oms-server")
public interface OmsClient extends OmsService {
}
