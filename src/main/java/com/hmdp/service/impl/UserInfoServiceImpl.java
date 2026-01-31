package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.dto.UserInfoDTO;
import com.hmdp.entity.UserInfo;
import com.hmdp.mapper.UserInfoMapper;
import com.hmdp.service.IUserInfoService;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceimpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {


    @Override
    public Result me(UserDTO userDTO) {
        //填充基本信息
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtil.copyProperties(userDTO, userInfoDTO);
        //填充其他信息
        UserInfo userInfo = getById(userDTO.getId());
        BeanUtil.copyProperties(userInfo, userInfoDTO);
        return Result.ok(userInfoDTO);
    }
}
