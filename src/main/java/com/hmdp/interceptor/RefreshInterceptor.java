package com.hmdp.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.constant.RedisConstants;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.JwtHelper;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.constant.RedisConstants.LOGIN_USER_KEY_PREFIX;
import static com.hmdp.constant.RedisConstants.LOGIN_USER_TTL;

public class RefreshInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;
    private JwtHelper jwtHelper;

    public RefreshInterceptor(StringRedisTemplate stringRedisTemplate, JwtHelper jwtHelper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtHelper = jwtHelper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if(StrUtil.isBlank(token)){
            return true;
        }

        Map<Object, Object> userDTOMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY_PREFIX + token);
        if(userDTOMap.isEmpty()){
            return true;
        }
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userDTOMap, new UserDTO(), true);
        stringRedisTemplate.expire(LOGIN_USER_KEY_PREFIX + token, LOGIN_USER_TTL, TimeUnit.SECONDS);

        UserHolder.saveUser(BeanUtil.copyProperties(userDTO, UserDTO.class));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}

