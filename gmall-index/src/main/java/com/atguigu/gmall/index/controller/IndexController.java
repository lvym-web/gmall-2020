package com.atguigu.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping("/cates")
    public Resp<List<CategoryEntity>> getCategoryByPidOrLevel(){

      List<CategoryEntity> categoryEntityList=indexService.getCategoryByPidOrLevel();
      return Resp.ok(categoryEntityList);
    }
    @GetMapping("/cates/{pid}")
    public Resp<List<CategoryVO>> getCategoryByPidOrLeve(@PathVariable(value = "pid", required = true) Long parentCid){

        List<CategoryVO> categoryEntityList=indexService.queryCategoryByPidCategoryVO(parentCid);
        return Resp.ok(categoryEntityList);
    }
}
