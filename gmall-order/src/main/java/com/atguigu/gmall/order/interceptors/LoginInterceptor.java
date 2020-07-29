package com.atguigu.gmall.order.interceptors;

import com.atguigu.core.bean.UserInfo;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.order.config.JwtProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties jwtProperties;
                 //随着线程的结束而销毁，只存在当前线程
    private static final ThreadLocal<UserInfo> THREAD_LOCAL=new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();
        //获取cookie中的token信息 jwt
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());

        //判断有没有token
        if (StringUtils.isNotBlank(token)){
            Map<String, Object> infoFromToken = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

            userInfo.setId(new Long(infoFromToken.get("id").toString()));
        }
        THREAD_LOCAL.set(userInfo);
        return super.preHandle(request, response, handler);
    }

    /**
     *   获取  ThreadLocal的值
     * @return
     */
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //必须手动清除thread local中线程变量，因为使用tomcat线程池，一般不会线程结束，所以防止别人拿到你的变量值
        THREAD_LOCAL.remove();
    }
}
