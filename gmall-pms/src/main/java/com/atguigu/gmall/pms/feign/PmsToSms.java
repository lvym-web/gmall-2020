package com.atguigu.gmall.pms.feign;


import com.atguigu.gmall.sms.api.PmsToSmsServer;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-server")
public interface PmsToSms extends PmsToSmsServer {

}
