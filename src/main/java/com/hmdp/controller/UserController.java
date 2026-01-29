package com.hmdp.controller;



import cn.hutool.core.bean.BeanUtil;
import com.hmdp.constant.UserBackMessage;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import static com.hmdp.constant.UserBackMessage.*;


@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private IUserInfoService userInfoService;

    @Resource
    private IUserService userService;
    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        return userService.sendCode(phone, session);
    }

    /**
     * 登录
     * @param loginFormDTO
     * @param session
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session){
        return userService.login(loginFormDTO,session);
    }

    @PostMapping("sign")
    public Result sign(){
        return userService.sign();
    }

    @GetMapping("sign/count")
    public Result signCount(){
        return userService.signCount();
    }

    @GetMapping("me")
    public Result me(){
        //
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @GetMapping("/{id}")
    public Result queryById(@PathVariable("id") Long id){
        //获取用户信息
        User user = userService.getById(id);
        if(user == null){
            return Result.fail(UserErrorNull);
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.ok(userDTO);
    }

    @GetMapping("info/{id}")
    public Result infoById(@PathVariable("id") Long id){
        UserInfo userInfo = userInfoService.getById(id);
        if(userInfo == null){
            return Result.fail(UserErrorNull);
        }
        userInfo.setUpdateTime(null);
        userInfo.setCreateTime(null);

        return Result.ok(userInfo);
    }

}
