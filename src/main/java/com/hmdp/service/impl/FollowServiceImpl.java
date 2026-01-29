package com.hmdp.service.impl;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import org.springframework.stereotype.Service;



@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        return null;
    }

    @Override
    public Result ifFollow(Long followUserId) {
        return null;
    }

    @Override
    public Result commonFollow(Long userId) {
        return null;
    }
}




