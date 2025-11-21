package com.admin.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 - 增强版
 * @author suYan
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    // =============================String操作=============================

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
     * 设置缓存并指定过期时间
     *
     * @param key      缓存key
     * @param value    缓存value
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置缓存（秒为单位）
     *
     * @param key     缓存key
     * @param value   缓存value
     * @param seconds 过期时间（秒）
     */
    public void setWithExpire(String key, Object value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 只有在key不存在时设置
     *
     * @param key   缓存key
     * @param value 缓存value
     * @return 是否设置成功
     */
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 只有在key不存在时设置，并指定过期时间
     *
     * @param key      缓存key
     * @param value    缓存value
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
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
     * 获取缓存（泛型）
     *
     * @param key   缓存key
     * @param clazz 值的类型
     * @param <T>   泛型类型
     * @return 缓存value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T) value : null;
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 递增因子（大于0）
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递增1
     *
     * @param key 键
     * @return 递增后的值
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 递减因子（大于0）
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 递减1
     *
     * @param key 键
     * @return 递减后的值
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    // =============================通用操作=============================

    /**
     * 删除缓存
     *
     * @param key 缓存key
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存key集合
     * @return 删除的数量
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true-存在 false-不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     *
     * @param key      键
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return redisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * 设置过期时间（秒）
     *
     * @param key     键
     * @param seconds 过期时间（秒）
     * @return 是否设置成功
     */
    public Boolean expire(String key, long seconds) {
        return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 根据pattern获取所有的key
     *
     * @param pattern 匹配模式
     * @return key集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    // =============================List操作=============================

    /**
     * 向列表右边添加元素
     *
     * @param key   列表key
     * @param value 要添加的值
     * @return 列表长度
     */
    public Long listRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 向列表左边添加元素
     *
     * @param key   列表key
     * @param value 要添加的值
     * @return 列表长度
     */
    public Long listLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 批量向列表右边添加元素
     *
     * @param key    列表key
     * @param values 要添加的值集合
     * @return 列表长度
     */
    public Long listRightPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 从列表右边弹出元素
     *
     * @param key 列表key
     * @return 弹出的元素
     */
    public Object listRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 从列表左边弹出元素
     *
     * @param key 列表key
     * @return 弹出的元素
     */
    public Object listLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 获取列表指定索引的元素
     *
     * @param key   列表key
     * @param index 元素索引
     * @return 元素值
     */
    public Object listGet(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 获取列表指定范围的元素
     *
     * @param key   列表key
     * @param start 开始索引
     * @param end   结束索引
     * @return 元素列表
     */
    public List<Object> listRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取列表长度
     *
     * @param key 列表key
     * @return 列表长度
     */
    public Long listSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // =============================Set操作=============================

    /**
     * 向集合添加元素
     *
     * @param key    集合key
     * @param values 要添加的值（可变参数）
     * @return 成功添加的元素数量
     */
    public Long setAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 从集合中移除元素
     *
     * @param key    集合key
     * @param values 要移除的值
     * @return 成功移除的元素数量
     */
    public Long setRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 判断元素是否是集合成员
     *
     * @param key   集合key
     * @param value 元素值
     * @return true-是成员 false-不是成员
     */
    public Boolean setIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取集合中的所有元素
     *
     * @param key 集合key
     * @return 集合中的元素
     */
    public Set<Object> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 获取集合大小
     *
     * @param key 集合key
     * @return 集合大小
     */
    public Long setSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 随机获取集合中的一个元素
     *
     * @param key 集合key
     * @return 随机元素
     */
    public Object setRandomMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    // =============================Hash操作=============================

    /**
     * 将字段与给定值映射到哈希表
     *
     * @param key   哈希表key
     * @param field 字段名
     * @param value 字段值
     */
    public void hashPut(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 批量设置哈希表的字段值
     *
     * @param key 哈希表key
     * @param map 字段-值映射
     */
    public void hashPutAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 从哈希表中获取字段的值
     *
     * @param key   哈希表key
     * @param field 字段名
     * @return 字段值
     */
    public Object hashGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取哈希表中所有字段和值
     *
     * @param key 哈希表key
     * @return 字段-值映射
     */
    public Map<Object, Object> hashGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除哈希表中的字段
     *
     * @param key    哈希表key
     * @param fields 字段名（可变参数）
     * @return 成功删除的字段数量
     */
    public Long hashDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断哈希表中是否存在字段
     *
     * @param key   哈希表key
     * @param field 字段名
     * @return true-存在 false-不存在
     */
    public Boolean hashHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 获取哈希表中字段的数量
     *
     * @param key 哈希表key
     * @return 字段数量
     */
    public Long hashSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 哈希表字段值递增
     *
     * @param key   哈希表key
     * @param field 字段名
     * @param delta 递增量
     * @return 递增后的值
     */
    public Long hashIncrement(String key, String field, long delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    // =============================ZSet操作=============================

    /**
     * 向有序集合添加元素
     *
     * @param key   有序集合key
     * @param value 要添加的值
     * @param score 分数
     * @return 是否添加成功
     */
    public Boolean zSetAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 批量向有序集合添加元素
     *
     * @param key    有序集合key
     * @param tuples 元素-分数对集合
     * @return 成功添加的元素数量
     */
    public Long zSetAdd(String key, Set<ZSetOperations.TypedTuple<Object>> tuples) {
        return redisTemplate.opsForZSet().add(key, tuples);
    }

    /**
     * 从有序集合中移除元素
     *
     * @param key    有序集合key
     * @param values 要移除的值
     * @return 成功移除的元素数量
     */
    public Long zSetRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 增加元素的分数
     *
     * @param key   有序集合key
     * @param value 元素值
     * @param delta 增加的分数
     * @return 增加后的分数
     */
    public Double zSetIncrementScore(String key, Object value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    /**
     * 获取元素的排名（从小到大）
     *
     * @param key   有序集合key
     * @param value 元素值
     * @return 排名（从0开始）
     */
    public Long zSetRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 获取元素的排名（从大到小）
     *
     * @param key   有序集合key
     * @param value 元素值
     * @return 排名（从0开始）
     */
    public Long zSetReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 获取有序集合的范围（从小到大）
     *
     * @param key   有序集合key
     * @param start 开始位置
     * @param end   结束位置
     * @return 范围内的元素
     */
    public Set<Object> zSetRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取有序集合的范围（从大到小）
     *
     * @param key   有序集合key
     * @param start 开始位置
     * @param end   结束位置
     * @return 范围内的元素
     */
    public Set<Object> zSetReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 根据分数范围获取元素
     *
     * @param key 有序集合key
     * @param min 最小分数
     * @param max 最大分数
     * @return 范围内的元素
     */
    public Set<Object> zSetRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 获取有序集合大小
     *
     * @param key 有序集合key
     * @return 集合大小
     */
    public Long zSetSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 获取指定分数范围内的元素数量
     *
     * @param key 有序集合key
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素数量
     */
    public Long zSetCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * 获取元素的分数
     *
     * @param key   有序集合key
     * @param value 元素值
     * @return 分数
     */
    public Double zSetScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }
}