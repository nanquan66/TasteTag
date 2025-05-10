package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {

        String key = CACHE_SHOP_KEY + id;

        // 1.从Redis中查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        // 2.判断缓存是否命中
        if (StrUtil.isNotBlank(shopJson)){
            // 缓存命中，直接返回商铺信息
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }

        // 3.缓存未命中，查询数据库
        Shop shop = getById(id);

        // 4.数据库未命中，返回错误
        if (shop == null){
            return Result.fail("店铺不存在");
        }

        // 5.数据库命中，写入缓存后返回
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop));

        return Result.ok(shop);
    }
}
