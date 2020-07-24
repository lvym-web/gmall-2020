package com.atguigu.gmall.index.annotation;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedissonCache {

    //相互等价 value=prefix
    @AliasFor("prefix")
    String value() default "";

    // 缓存中key的前缀
    @AliasFor("value")
    String prefix() default "";

    // 过期时间 单位:分
    int timeout() default 5;

    // 随机时间 单位:分
    int random() default 5;
}
