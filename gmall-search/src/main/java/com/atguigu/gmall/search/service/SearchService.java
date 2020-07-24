package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponseVO;

import java.io.IOException;

public interface SearchService {
    SearchResponseVO querySearch(SearchParamVO searchParamVO) throws IOException;
}
