package com.admin.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * 配置RedisTemplate的序列化方式，使用Jackson2进行JSON序列化
 * 
 * @author suYan
 */
@Configuration
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisConfig implements CachingConfigurer {

    /**
     * RedisTemplate 序列化配置
     * 默认的JdkSerializationRedisSerializer序列化方式存在问题：
     * 1. 不可读，由二进制字节组成
     * 2. 占用空间大
     * 3. 安全问题
     * 
     * 使用Jackson2JsonRedisSerializer替代：
     * 1. 可读性好，以JSON格式存储
     * 2. 占用空间小
     * 3. 安全性高
     *
     * @param redisConnectionFactory Redis连接工厂
     * @return RedisTemplate
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 配置Jackson2JsonRedisSerializer
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJackson2JsonRedisSerializer();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        redisTemplate.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        // value序列化方式采用jackson
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 创建Jackson2JsonRedisSerializer
     * 配置ObjectMapper以支持更多类型
     */
    private Jackson2JsonRedisSerializer<Object> createJackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是non-final修饰的，final修饰的类，比如String,Integer等会抛出异常
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }
}
