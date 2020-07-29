package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.sms.api.PmsToSmsServer;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-server")
public interface SmsClient extends PmsToSmsServer {
}
