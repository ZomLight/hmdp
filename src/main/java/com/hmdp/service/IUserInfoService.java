package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.UserInfo;


public interface IUserInfoService extends IService<UserInfo> {
    Result me(UserDTO userDTO);
}
