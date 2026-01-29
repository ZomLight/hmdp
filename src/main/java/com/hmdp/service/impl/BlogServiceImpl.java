package com.hmdp.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import org.springframework.stereotype.Service;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Override
    public Result queryBlogById(Long id) {
        return null;
    }

    @Override
    public Result queryHotBlog(Integer current) {
        return null;
    }

    @Override
    public Result likeBlog(Long id) {
        return null;
    }

    @Override
    public Result likesBlog(Long id) {
        return null;
    }

    @Override
    public Result saveBlog(Blog blog) {
        return null;
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        return null;
    }
}




