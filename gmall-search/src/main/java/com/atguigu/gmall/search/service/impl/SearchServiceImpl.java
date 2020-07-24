package com.atguigu.gmall.search.service.impl;

import cn.hutool.json.JSONUtil;
import com.atguigu.core.exception.RRException;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.service.SearchService;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import com.atguigu.gmall.search.vo.SearchResponseVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl{
//
//    @Autowired
//    private RestHighLevelClient restHighLevelClient;
//
//    @Override
//    public SearchResponseVO querySearch(SearchParamVO searchParamVO) throws IOException {
//
//        SearchRequest searchRequest = this.getSearchRequest(searchParamVO);
//
//        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        System.out.println(searchResponse.toString());
//        SearchResponseVO searchResponseVO = this.getSearchResponseVO(searchResponse);
//        searchResponseVO.setPageNum(searchParamVO.getPageNum());
//        searchResponseVO.setPageSize(searchParamVO.getPageSize());
//
//        return searchResponseVO;
//    }
//
//    private SearchResponseVO getSearchResponseVO(SearchResponse searchResponse){
//
//        SearchResponseVO searchResponseVO = new SearchResponseVO();
//        // 获取聚合桶集合
//        Map<String, Aggregation> asMap = searchResponse.getAggregations().asMap();
//        // 解析品牌
//        //获取品牌聚合桶
//
//        ParsedLongTerms brandIdAgg = (ParsedLongTerms)asMap.get("brandIdAgg");
//        List<String> brandList = brandIdAgg.getBuckets().stream().map(bucket -> {
//            Map<String, String> map = new HashMap<>();
//            map.put("id", bucket.getKeyAsString());
//            //获取品牌名称
//            Map<String, Aggregation> aggregationMap = bucket.getAggregations().asMap();
//            ParsedStringTerms brandNameAgg = (ParsedStringTerms) aggregationMap.get("brandNameAgg");
//            String keyAsString = brandNameAgg.getBuckets().get(0).getKeyAsString();
//            map.put("name", keyAsString);
//            return JSONUtil.toJsonStr(map);
//        }).collect(Collectors.toList());
//        SearchResponseAttrVO searchResponseAttrVO = new SearchResponseAttrVO();
//        searchResponseAttrVO.setName("品牌");
//        searchResponseAttrVO.setValue(brandList);
//        searchResponseVO.setBrand(searchResponseAttrVO);
//
//        //解析分类
//        //获取分类聚合桶
//        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) asMap.get("categoryIdAgg");
//        List<String> categoryList = categoryIdAgg.getBuckets().stream().map(bucket -> {
//            Map<String, String> catemap = new HashMap<>();
//            catemap.put("id", bucket.getKeyAsString());
//            Map<String, Aggregation> subAggregationMap = bucket.getAggregations().asMap();
//            ParsedStringTerms categoryNameAgg = (ParsedStringTerms) subAggregationMap.get("categoryNameAgg");
//            String categoryName = categoryNameAgg.getBuckets().get(0).getKeyAsString();
//            catemap.put("name", categoryName);
//            return JSONUtil.toJsonStr(catemap);
//        }).collect(Collectors.toList());
//        SearchResponseAttrVO searchResponseAttrVO1 = new SearchResponseAttrVO();
//        searchResponseAttrVO1.setName("分类");
//        searchResponseAttrVO1.setValue(categoryList);
//        searchResponseVO.setCatelog(searchResponseAttrVO1);
//
//        SearchHits hits = searchResponse.getHits();
//
//        searchResponseVO.setTotal(hits.getTotalHits());
//         // 规格参数
//        // 获取嵌套聚合对象
//        ParsedNested attrAgg = (ParsedNested) asMap.get("attrAgg");
//        // 规格参数id聚合对象
//        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
//        List<SearchResponseAttrVO> searchResponseAttrVOS = attrIdAgg.getBuckets().stream().map(buchet -> {
//            SearchResponseAttrVO searchAttrVO = new SearchResponseAttrVO();
//            // 设置规格参数id
//            searchAttrVO.setProductAttributeId(buchet.getKeyAsNumber().longValue());
//            // 设置规格参数名
//            searchAttrVO.setName(((ParsedStringTerms) buchet.getAggregations().get("attrNameAgg")).getBuckets().get(0).getKeyAsString());
//            // 设置规格参数值的列表
//            List<? extends Terms.Bucket> attrValueAgg = ((ParsedStringTerms) buchet.getAggregations().get("attrValueAgg")).getBuckets();
//            List<String> stringList = attrValueAgg.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
//            searchAttrVO.setValue(stringList);
//            return searchAttrVO;
//        }).collect(Collectors.toList());
//        searchResponseVO.setAttrs(searchResponseAttrVOS);
//        // 获取Goods集合
//        SearchHit[] hitsHits = hits.getHits();
//        ArrayList<Goods> goodsList = new ArrayList<>();
//        for (SearchHit hitsHit : hitsHits) {
//            String sourceAsString = hitsHit.getSourceAsString();
//             // 设置高亮属性
//            Text title = hitsHit.getHighlightFields().get("title").getFragments()[0];
//            Goods goods = JSONUtil.toBean(sourceAsString, Goods.class);
//            goods.setTitle(title.toString());
//            goodsList.add(goods);
//        }
//        searchResponseVO.setProducts(goodsList);
//        return searchResponseVO;
//    }
//
//    private SearchRequest getSearchRequest(SearchParamVO searchParamVO) {
//
//
//        // 查询条件构建起
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        // 构建查询条件和过滤条件
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        // 构建关键字查询条件
//        boolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParamVO.getKeyword()).operator(Operator.AND));
//
//        // 品牌过滤条件
//        String[] brand = searchParamVO.getBrand();
//        for (String s : brand) {
//            boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", s));
//        }
//        // 分类过滤
//        String[] catelog3 = searchParamVO.getCatelog3();
//        for (String s : catelog3) {
//            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId",s));
//        }
//
//        //构建规格属性嵌套过滤
//        String[] props = searchParamVO.getProps();
//        for (String prop : props) {
//            //分割
//            //2:win10-android-
//            //3:4g
//            String[] split = StringUtils.split(prop, ":");
//            if (split == null || split.length != 2) {
//                continue;
//            }
//            //构建嵌套查询
//            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//            //构建嵌套子查询
//            BoolQueryBuilder subBoolQueryBuilder = QueryBuilders.boolQuery();
//            //查询
//            subBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
//            //分割
//            String[] split1 = StringUtils.split(split[1], "-");
//            subBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue", split1));
//            //把嵌套子查询放入嵌套查询
//            boolQuery.must(QueryBuilders.nestedQuery("attrs", subBoolQueryBuilder, ScoreMode.None));
//            //把嵌套查询放入
//            boolQueryBuilder.filter(boolQuery);
//        }
//
//         // 价格区间过滤
//
//
//           boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gt(searchParamVO.getPriceFrom()).lt(searchParamVO.getPriceTo()));
//
//        searchSourceBuilder.query(boolQueryBuilder);
//        //分页
//        Integer pageNum = searchParamVO.getPageNum();
//        Integer pageSize = searchParamVO.getPageSize();
//        searchSourceBuilder.from((pageNum - 1) * pageSize);
//        searchSourceBuilder.size(pageSize);
//        //排序     // order=1:asc  排序规则   0:asc   // 0：综合排序  1：销量  2：价格
//        String order = searchParamVO.getOrder();
//        if (StringUtils.isNotBlank(order)) {
//            String[] split = StringUtils.split(order, ":");
//            if (split != null || split.length == 2) {
//                String field = "";
//                switch (split[0]) {
//                    case "1":
//                        field = "sale";
//                        break;
//                    case "2":
//                        field = "price";
//                        break;
//                }
//                searchSourceBuilder.sort(field, StringUtils.equals(split[1], "asc") ? SortOrder.ASC : SortOrder.DESC);
//            }
//        }
//        // 高亮
//        searchSourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<em>").postTags("</em>"));
//        // 构建聚合查询
//        // 品牌聚合
//        searchSourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
//                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));
//        // 分类聚合
//        searchSourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
//                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
//        // 搜索的规格属性聚合
//        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
//                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
//                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName")
//                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")))));
//
//
//        // 结果集过滤
//        searchSourceBuilder.fetchSource(new String[]{"skuId", "pic", "title", "price"}, null);
//
//        SearchRequest searchRequest = new SearchRequest("gmall");
//        searchRequest.types("goods");
//        searchRequest.source(searchSourceBuilder);
//        System.out.println(searchSourceBuilder.toString());
//
//        return searchRequest;
//    }

}
