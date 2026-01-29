package com.hmdp.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.JwtHelper;
import com.hmdp.utils.MD5Util;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.constant.RedisConstants.*;
import static com.hmdp.constant.SystemConstants.*;
import static com.hmdp.constant.UserBackMessage.*;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JwtHelper jwtHelper;

    /**
     * 发送验证码
     * @param phone
     * @param session
     * @return
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //如果电话号码不对
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail(UserErrorPhone);
        }
        //生成code
        String code = RandomUtil.randomNumbers(6);
        //保存
        stringRedisTemplate.opsForValue().set(
                LOGIN_CODE_KEY_PREFIX + phone,
                code,
                LOGIN_CODE_TTL,
                TimeUnit.MINUTES);

        log.info("验证码为：{}", code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginFormDTO, HttpSession session) {
        String phone = loginFormDTO.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail(UserErrorPhone);
        }
        String code = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY_PREFIX + phone);
        if(!loginFormDTO.getCode().equals(code) || loginFormDTO.getCode()==null){
            return Result.fail(UserErrorCode);
        }
        User user = query().eq("phone", phone).one();
        if(user == null){
            log.info("用户为null");
            user = createByPhone(phone);
        }

        String token = jwtHelper.createToken(MD5Util.encrypt(phone));
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userDTOMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldname, fieldValue) -> {
                    //stringRedis 默认要字符串存入
                    if (fieldValue != null) {
                        return fieldValue.toString();
                    }
                    return null;
                }));
        //存入redis
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY_PREFIX+token, userDTOMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY_PREFIX+phone, LOGIN_USER_TTL, TimeUnit.MINUTES);

        return Result.ok(token);

    }

    @Override
    public Result sign() {
        UserDTO user = UserHolder.getUser();

        LocalDateTime now = LocalDateTime.now();
        String timeSuffix = now.format(DateTimeFormatter.ofPattern(":yyyy/MM"));

        String key = USER_SIGN_KEY + user.getId() + timeSuffix;

        int day = now.getDayOfMonth();
        //TODO:设置签到功能
        return Result.ok();
    }

    @Override
    public Result signCount() {
        //TODO:签退功能实现
        return Result.ok();
    }


    private User createByPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));

        save(user);
        return user;
    }

}




