package com.hmdp.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Override
    public Result queryById(Long id) {
        return null;
    }

    @Override
    public Result updateShop(Shop shop) {
        return null;
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        return null;
    }
}




