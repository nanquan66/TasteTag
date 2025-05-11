package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.BloomFilterUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TTL;

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

    @Resource
    private BloomFilterUtil bloomFilterUtil;

    @Override
    public Result queryById(Long id) {

        String key = CACHE_SHOP_KEY + id;

        // ===== 新增布隆过滤器检查 =====
        if (!bloomFilterUtil.mightContain(key)) {
            // 布隆过滤器确认不存在，直接返回
            return Result.fail("店铺不存在");
        }


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
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 新增：同步到布隆过滤器
        bloomFilterUtil.put(key);

        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null){
            return Result.fail("店铺id不能为空");
        }

        // 1.先更新数据库
        updateById(shop);

        // 2.再删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);

        return Result.ok(shop);
    }


}
