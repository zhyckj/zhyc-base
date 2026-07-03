/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.config;

import com.zhyc.common.cache.ZhycCacheNames;
import com.zhyc.common.cache.RedisBusinessCacheHelper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 核心平台 Redis 缓存配置。
 *
 * <p>默认使用 JDK 序列化保存缓存值，避免现有响应对象没有无参构造器导致 JSON 反序列化失败。
 * Redis 异常会被缓存错误处理器记录并降级为直接查库，不影响主业务流程。</p>
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(prefix = "zhyc.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PlatformCacheConfig implements CachingConfigurer {

  /** 缓存异常日志。 */
  private static final Logger LOGGER = LoggerFactory.getLogger(PlatformCacheConfig.class);

  /**
   * 创建 Redis 缓存管理器。
   *
   * @param connectionFactory Redis 连接工厂
   * @param cachePrefix 平台缓存 Key 前缀
   * @param defaultTtl 默认缓存过期时间
   * @return Redis 缓存管理器
   */
  @Bean
  @ConditionalOnBean(RedisConnectionFactory.class)
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
      @Value("${zhyc.cache.prefix:zhyc}") String cachePrefix,
      @Value("${zhyc.cache.default-ttl:PT10M}") String defaultTtl) {
    Duration parsedDefaultTtl = parsePositiveDuration(defaultTtl, "缓存默认过期时间");
    RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(parsedDefaultTtl)
        .disableCachingNullValues()
        .computePrefixWith(cacheName -> normalizePrefix(cachePrefix) + ':' + cacheName + ':')
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
    return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
        .cacheDefaults(defaultConfiguration)
        .withInitialCacheConfigurations(cacheConfigurations(defaultConfiguration))
        .transactionAware()
        .build();
  }

  /**
   * 创建业务缓存 RedisTemplate。
   *
   * <p>该模板供命令式业务缓存帮助类使用，Key 使用字符串序列化，Value 使用 JDK 序列化，与 Spring Cache
   * 当前缓存值策略保持一致。</p>
   *
   * @param connectionFactory Redis 连接工厂
   * @return 业务缓存 RedisTemplate
   */
  @Bean(name = "zhycBusinessRedisTemplate")
  @ConditionalOnBean(RedisConnectionFactory.class)
  @ConditionalOnMissingBean(name = "zhycBusinessRedisTemplate")
  public RedisTemplate<String, Object> zhycBusinessRedisTemplate(RedisConnectionFactory connectionFactory) {
    RedisSerializer<String> keySerializer = new StringRedisSerializer();
    JdkSerializationRedisSerializer valueSerializer = new JdkSerializationRedisSerializer();
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setKeySerializer(keySerializer);
    redisTemplate.setHashKeySerializer(keySerializer);
    redisTemplate.setValueSerializer(valueSerializer);
    redisTemplate.setHashValueSerializer(valueSerializer);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

  /**
   * 创建统一 Redis 业务缓存帮助类。
   *
   * @param redisOperations 业务缓存 Redis 操作入口
   * @param cachePrefix 平台缓存 Key 前缀
   * @return 业务缓存帮助类
   */
  @Bean
  @ConditionalOnBean(name = "zhycBusinessRedisTemplate")
  @ConditionalOnMissingBean
  public RedisBusinessCacheHelper redisBusinessCacheHelper(
      @Qualifier("zhycBusinessRedisTemplate") RedisOperations<String, Object> redisOperations,
      @Value("${zhyc.cache.prefix:zhyc}") String cachePrefix) {
    return new RedisBusinessCacheHelper(redisOperations, cachePrefix);
  }

  /**
   * 创建缓存异常处理器。
   *
   * @return 缓存异常处理器
   */
  @Bean(name = "cacheErrorHandler")
  @Override
  public CacheErrorHandler errorHandler() {
    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        logCacheError("读取", exception, cache, key);
      }

      @Override
      public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        logCacheError("写入", exception, cache, key);
      }

      @Override
      public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        logCacheError("删除", exception, cache, key);
      }

      @Override
      public void handleCacheClearError(RuntimeException exception, Cache cache) {
        LOGGER.warn("缓存清空失败，已降级为直接访问数据源，cacheName={}", cache.getName(), exception);
      }
    };
  }

  private Map<String, RedisCacheConfiguration> cacheConfigurations(
      RedisCacheConfiguration defaultConfiguration) {
    Map<String, RedisCacheConfiguration> configurations = new LinkedHashMap<>();
    configurations.put(ZhycCacheNames.SYS_USER_PERMISSIONS, defaultConfiguration.entryTtl(Duration.ofMinutes(5)));
    configurations.put(ZhycCacheNames.SYS_MENU_TREE, defaultConfiguration.entryTtl(Duration.ofMinutes(10)));
    configurations.put(ZhycCacheNames.SYS_DICT_TYPES, defaultConfiguration.entryTtl(Duration.ofMinutes(30)));
    configurations.put(ZhycCacheNames.SYS_DICT_ITEMS, defaultConfiguration.entryTtl(Duration.ofMinutes(30)));
    configurations.put(ZhycCacheNames.SYS_PARAMS, defaultConfiguration.entryTtl(Duration.ofMinutes(15)));
    configurations.put(ZhycCacheNames.SYS_TENANT_PARAMS, defaultConfiguration.entryTtl(Duration.ofMinutes(15)));
    configurations.put(ZhycCacheNames.AI_PROVIDERS, defaultConfiguration.entryTtl(Duration.ofMinutes(5)));
    configurations.put(ZhycCacheNames.AI_MODELS, defaultConfiguration.entryTtl(Duration.ofMinutes(5)));
    configurations.put(ZhycCacheNames.AI_APPS, defaultConfiguration.entryTtl(Duration.ofMinutes(5)));
    configurations.put(ZhycCacheNames.AI_PROMPTS, defaultConfiguration.entryTtl(Duration.ofMinutes(10)));
    return configurations;
  }

  private void logCacheError(String action, RuntimeException exception, Cache cache, Object key) {
    LOGGER.warn("缓存{}失败，已降级为直接访问数据源，cacheName={}, key={}", action, cache.getName(), key, exception);
  }

  private String normalizePrefix(String cachePrefix) {
    if (cachePrefix == null || cachePrefix.isBlank()) {
      return "zhyc";
    }
    return cachePrefix.trim();
  }

  private Duration parsePositiveDuration(String value, String label) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(label + "不能为空");
    }
    Duration duration;
    try {
      duration = Duration.parse(value.trim());
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException(label + "必须使用 ISO-8601 Duration 格式，例如 PT10M", exception);
    }
    if (duration.isZero() || duration.isNegative()) {
      throw new IllegalArgumentException(label + "必须大于 0");
    }
    return duration;
  }
}
