package com.hmdp.service.impl;



import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.constant.RedisConstants;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        if(isFollow){
            Follow follow = new Follow();
            follow.setFollowUserId(userId);
            follow.setUserId(userId);

            boolean isSave = save(follow);

            if(isSave){
                stringRedisTemplate.opsForSet().add(RedisConstants.FOLLOW_KEY + userId, followUserId.toString());
            }
        }else {
            remove(new QueryWrapper<Follow>().eq("user_id", userId).eq("follow_user_id", followUserId));
            stringRedisTemplate.opsForSet().remove(RedisConstants.FOLLOW_KEY + userId, followUserId.toString());
        }
        return Result.ok();
    }

    @Override
    public Result ifFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        Integer count = query().eq("follow_user_id", followUserId).eq("user_id", userId).count();
        return Result.ok(count > 0);
    }

    @Override
    public Result commonFollow(Long userId) {
        Long id = UserHolder.getUser().getId();
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(RedisConstants.FOLLOW_KEY + userId, RedisConstants.FOLLOW_KEY + id);

        if(intersect == null || intersect.isEmpty()){
            return Result.ok(Collections.emptyList());
        }

        List<Long> list = intersect.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        List<UserDTO> userDTOS = userService.listByIds(list).stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(userDTOS);
    }
}




