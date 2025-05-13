package com.hmdp.utils;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * @author 11247
 */
@Component
public class BloomFilterUtil {
    // 预期插入数量（根据业务调整）
    private static final int EXPECTED_INSERTIONS = 1000000;
    // 误判率（根据业务调整）
    private static final double FALSE_POSITIVE_RATE = 0.01;

    private BloomFilter<String> bloomFilter;

    @PostConstruct
    public void init() {
        bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                EXPECTED_INSERTIONS,
                FALSE_POSITIVE_RATE);
    }

    /**
     * 添加元素到布隆过滤器
     */
    public void put(String key) {
        bloomFilter.put(key);
    }

    /**
     * 判断元素是否可能存在
     * @return true-可能存在, false-绝对不存在
     */
    public boolean mightContain(String key) {
        return bloomFilter.mightContain(key);
    }
}