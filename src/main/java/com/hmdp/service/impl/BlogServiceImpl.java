package com.hmdp.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.constant.RedisConstants;
import com.hmdp.constant.SystemConstants;
import com.hmdp.dto.Result;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    @Resource
    private IUserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IFollowService followService;

    @Override
    public Result queryBlogById(Long id) {
        Blog blog = getById(id);
        if(blog==null){
            return Result.fail("笔记不存在");
        }

        queryBlogUser(blog);
        isBlogLiked(blog);
        return Result.ok(blog);
    }

    @Override
    public Result queryHotBlog(Integer current) {
        Page<Blog> page = query().orderByDesc("liked").page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));

        List<Blog> records = page.getRecords();

        records.forEach(blog -> {
            queryBlogUser(blog);
            isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    private void isBlogLiked(Blog blog) {
        UserDTO user = UserHolder.getUser();
        if(user != null){
            blog.setIsLike(stringRedisTemplate.opsForZSet().score(RedisConstants.BLOG_LIKED_KEY + blog.getId(), user.getId().toString()) != null);
        }
    }

    private void  queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setIcon(user.getIcon());
        blog.setName(user.getNickName());
    }

    @Override
    public Result likeBlog(Long id) {
        UserDTO user = UserHolder.getUser();

        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, user.getId().toString());

        if(score == null) {
            boolean success = update().setSql("liked = liked + 1").eq("id", id).update();
            if(success){
                stringRedisTemplate.opsForZSet().add(key, user.getId().toString(), System.currentTimeMillis());
            }
        }else{
            boolean success = update().setSql("liked = liked - 1").eq("id", id).update();
            if(success){
                stringRedisTemplate.opsForZSet().remove(key, user.getId().toString());
            }
        }

        return Result.ok();
    }

    @Override
    public Result likesBlog(Long id) {
        Set<String> userIds = stringRedisTemplate.opsForZSet().range(RedisConstants.BLOG_LIKED_KEY + id, 0L, 4L);

        if(userIds == null || userIds.isEmpty()){
            return Result.ok(Collections.emptyList());
        }

        ArrayList<String> list = ListUtil.toList(userIds);

        String idStr = StrUtil.join(",", userIds);

        List<UserDTO> userDTOS = userService.query().in("id", userIds).last("order by field(id" + idStr + ")")
                .list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());

        return Result.ok(userDTOS);
    }

    @Override
    public Result saveBlog(Blog blog) {
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());

        boolean isSave = save(blog);
        if(!isSave){
            return Result.fail("笔记保存失败");
        }

        List<Follow> followList = followService.query().eq("follow_user_id", blog.getUserId()).list();
        for(Follow follow : followList){
            Long userId = follow.getUserId();
            String key = RedisConstants.FEED_KEY + userId;

            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }

        return Result.ok(blog.getId());
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        Long id = UserHolder.getUser().getId();

        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(RedisConstants.FEED_KEY + id, 0, max, offset, 3);
        if(typedTuples == null || typedTuples.isEmpty()){
            return Result.ok();
        }

        ArrayList<Long> blogIds = new ArrayList<>(typedTuples.size());

        long minTime = 0;
        offset = 1;
        for(ZSetOperations.TypedTuple<String> tuple : typedTuples){
            long tmp = tuple.getScore().longValue();
            if(tmp == minTime){
                ++offset;
            }else{
                offset = 1;
                minTime = tmp;
            }

            blogIds.add(Long.valueOf(tuple.getValue()));
        }

        String idStr = StrUtil.join(",", blogIds);
        List<Blog> blogs = query().in("id", blogIds).last("order by field(id" + idStr + ")").list();

        for(Blog blog : blogs){
            queryBlogUser(blog);
            isBlogLiked(blog);
        }

        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setMinTime(minTime);
        scrollResult.setList(blogs);
        scrollResult.setOffset(offset);

        return Result.ok(scrollResult);
    }
}









