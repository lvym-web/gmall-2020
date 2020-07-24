package com.atguigu.gmall.search.controller;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.search.service.SearchServiceImpls;
import com.atguigu.gmall.search.service.impl.SearchServiceImpl;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.IOException;

@Controller
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchServiceImpls searchService;

    @GetMapping
    @ResponseBody
    public Resp<SearchResponseVO> querySearch(SearchParamVO searchParamVO) throws IOException {
        SearchResponseVO searchResponseVO  =searchService.querySearch(searchParamVO);
        return Resp.ok(searchResponseVO);
    }
}
//http://localhost:8086/search?catelog3=255&brand=4,5&props=25:3324&order=2:asc&priceFrom=10&priceTo=100000&pageNum=1&pageSize=10&keyword=%E5%AE%A2%E6%88%B7
