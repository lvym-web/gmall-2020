package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;
import java.util.List;

/**
 * @author jiangli
 * @since 2020/1/18 22:30
 */
@Data
public class CategoryVO extends CategoryEntity {

	private List<CategoryEntity> subs;
}
