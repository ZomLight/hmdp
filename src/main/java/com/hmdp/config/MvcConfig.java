package com.hmdp.config;

import com.hmdp.interceptor.LoginInterceptor;
import com.hmdp.interceptor.RefreshInterceptor;
import com.hmdp.utils.JwtHelper;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JwtHelper jwtHelper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                new RefreshInterceptor(stringRedisTemplate, jwtHelper)
        ).addPathPatterns("/**");

        registry.addInterceptor(
                new LoginInterceptor()
        ).excludePathPatterns(
                "/user/login",//登录
                "/user/code",//验证码
                "/blog/hot",//热点
                "/shop-type/**",
                "/shop/**",
                "/upload/**",
                "/voucher/**"
        );
    }
}
