package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.WmsService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-server")
public interface WmsClient extends WmsService {
}
