package com.admin.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置缓存
     *
     * @param key   缓存key
     * @param value 缓存value
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取缓存
     *
     * @param key 缓存key
     * @return 缓存value
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     *
     * @param key 缓存key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 添加元素到列表
     *
     * @param key   列表key
     * @param value 要添加的值
     */
    public void listAdd(String key, Object value) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        listOps.rightPush(key, value);
    }

    /**
     * 从列表中获取元素
     *
     * @param key   列表key
     * @param index 元素索引
     * @return 元素值
     */
    public Object listGet(String key, long index) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        return listOps.index(key, index);
    }

    /**
     * 向集合添加元素
     *
     * @param key   集合key
     * @param value 要添加的值
     */
    public void setAdd(String key, Object value) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        setOps.add(key, value);
    }

    /**
     * 获取集合中的所有元素
     *
     * @param key 集合key
     * @return 集合中的元素
     */
    public Set<Object> setMembers(String key) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        return setOps.members(key);
    }

    /**
     * 将字段与给定值映射到哈希表
     *
     * @param key   哈希表key
     * @param field 字段名
     * @param value 字段值
     */
    public void hashPut(String key, String field, Object value) {
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        hashOps.put(key, field, value);
    }

    /**
     * 从哈希表中获取字段的值
     *
     * @param key   哈希表key
     * @param field 字段名
     * @return 字段值
     */
    public Object hashGet(String key, String field) {
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        return hashOps.get(key, field);
    }

    /**
     * 向有序集合添加元素
     *
     * @param key   有序集合key
     * @param value 要添加的值
     * @param score 分数
     */
    public void sortedSetAdd(String key, Object value, double score) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        zSetOps.add(key, value, score);
    }

    /**
     * 获取有序集合的范围
     *
     * @param key   有序集合key
     * @param start 开始位置
     * @param end   结束位置
     * @return 范围内的元素
     */
    public Set<Object> sortedSetRange(String key, long start, long end) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.range(key, start, end);
    }
}