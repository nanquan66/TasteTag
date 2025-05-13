package com.hmdp.config.runner;

import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.utils.BloomFilterUtil;
import com.hmdp.utils.RedisConstants;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 11247
 */
@Component
public class BloomInitRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BloomInitRunner.class);

    @Resource
    private IShopService shopService;

    @Resource
    private BloomFilterUtil bloomFilterUtil;

    @Override
    public void run(ApplicationArguments args) {
        List<Shop> shops = shopService.list();
        if (shops.isEmpty()) {
            logger.info("没有商铺数据");
        } else {
            for (Shop shop : shops) {
                String key = RedisConstants.CACHE_SHOP_KEY + shop.getId();
                bloomFilterUtil.put(key);
            }
            logger.info(">>> 布隆过滤器预热完成，商铺数量：{}", shops.size());
        }
    }
}
