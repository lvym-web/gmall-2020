package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.OmsService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("oms-server")
public interface OmsClient extends OmsService {
}
