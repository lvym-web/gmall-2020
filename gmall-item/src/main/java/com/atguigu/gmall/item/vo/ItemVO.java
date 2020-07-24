package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.BaseGroupVO;
import com.atguigu.gmall.sms.vo.SaleVO;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
/**
 * @author jiangli
 * @since 2020/1/26 13:15
 * 商品详情页
 */

@Data
public class ItemVO {

	//1、当前sku的基本信息
	private Long skuId;
	private BrandEntity brandEntity;
	private CategoryEntity categoryEntity;
	private Long spuId;
	private String spuName;
	//private Long catalogId;
	//private Long brandId;
	private String skuTitle;
	private String skuSubtitle;
	private BigDecimal price;
	private BigDecimal weight;

	//2、sku的所有图片
	//private List<String> pics;
	private List<SkuImagesEntity> pics;
	//3、sku的所有促销信息
	private List<SaleVO> sales;

    private boolean store;//是否有货

	//4、sku的所有销售属性组合
	private List<SkuSaleAttrValueEntity> saleAttrs;

	private List<String> images;//spu海报

	//5、spu的所有基本属性
	private List<BaseGroupVO> attrGroups;

	//6、详情介绍
	//private SpuInfoDescEntity desc;
}