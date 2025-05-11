package com.hmdp.service.impl;

import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<ShopType> queryTypeList() {
        // 1. 先查缓存
        String cacheKey = CACHE_SHOP_TYPE_KEY;
        List<ShopType> typeList = (List<ShopType>) redisTemplate.opsForValue().get(cacheKey);

        // 2. 缓存未命中，查数据库
        if (typeList == null || typeList.isEmpty()) {
            typeList = this.query()
                    .orderByAsc("sort")
                    .list();
            // 3. 写入缓存（设置10分钟过期）
            redisTemplate.opsForValue().set(cacheKey, typeList, 10, TimeUnit.MINUTES);
        }

        return typeList;
    }
}
