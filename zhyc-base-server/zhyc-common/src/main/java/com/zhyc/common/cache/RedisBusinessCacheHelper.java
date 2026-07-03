/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.cache;

import com.zhyc.common.util.TextHelper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Redis 业务缓存帮助类。
 *
 * <p>该类面向少量需要命令式缓存读写的业务场景，例如详情缓存、外部接口结果缓存和临时查询结果缓存。
 * 普通列表、权限、字典等固定入口优先使用 Spring Cache 注解；分布式锁、队列、限流等能力应使用专门组件。</p>
 */
public class RedisBusinessCacheHelper {

  /** 默认 Redis Key 前缀。 */
  private static final String DEFAULT_PREFIX = "zhyc";
  /** 业务缓存命名空间。 */
  private static final String BUSINESS_NAMESPACE = "biz";
  /** 业务缓存日志。 */
  private static final Logger LOGGER = LoggerFactory.getLogger(RedisBusinessCacheHelper.class);

  /** Redis 操作入口。 */
  private final RedisOperations<String, Object> redisOperations;
  /** Redis Key 前缀。 */
  private final String keyPrefix;

  /**
   * 创建 Redis 业务缓存帮助类。
   *
   * @param redisOperations Redis 操作入口
   * @param keyPrefix Redis Key 前缀
   */
  public RedisBusinessCacheHelper(RedisOperations<String, Object> redisOperations, String keyPrefix) {
    this.redisOperations = Objects.requireNonNull(redisOperations, "Redis 操作入口不能为空");
    this.keyPrefix = normalizePrefix(keyPrefix);
  }

  /**
   * 读取业务缓存对象。
   *
   * <p>Redis 不可用时返回空结果，调用方可以继续走数据库或外部接口兜底。</p>
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @return 缓存对象
   */
  public Optional<Object> get(String cacheName, String cacheKey) {
    String redisKey = buildKey(cacheName, cacheKey);
    try {
      return Optional.ofNullable(valueOperations().get(redisKey));
    } catch (RuntimeException exception) {
      LOGGER.warn("读取业务缓存失败，已返回空结果，cacheName={}, cacheKey={}", cacheName, cacheKey, exception);
      return Optional.empty();
    }
  }

  /**
   * 按类型读取业务缓存对象。
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @param valueType 缓存对象类型
   * @param <T> 缓存对象泛型
   * @return 类型匹配时返回缓存对象，否则返回空结果
   */
  public <T> Optional<T> get(String cacheName, String cacheKey, Class<T> valueType) {
    Objects.requireNonNull(valueType, "缓存对象类型不能为空");
    Optional<Object> cached = get(cacheName, cacheKey);
    if (cached.isEmpty()) {
      return Optional.empty();
    }
    Object value = cached.get();
    if (valueType.isInstance(value)) {
      return Optional.of(valueType.cast(value));
    }
    LOGGER.warn("业务缓存类型不匹配，已忽略缓存值，cacheName={}, cacheKey={}, expectedType={}, actualType={}",
        cacheName, cacheKey, valueType.getName(), value.getClass().getName());
    return Optional.empty();
  }

  /**
   * 读取缓存，未命中时加载并写回缓存。
   *
   * <p>Redis 读取失败或类型不匹配时会执行加载器；加载器返回 {@code null} 时不会写入缓存。</p>
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @param valueType 缓存对象类型
   * @param ttl 缓存有效期
   * @param loader 缓存未命中时的数据加载器
   * @param <T> 缓存对象泛型
   * @return 缓存对象，加载器返回空时结果也为空
   */
  public <T> T getOrLoad(String cacheName, String cacheKey, Class<T> valueType, Duration ttl, Supplier<T> loader) {
    Objects.requireNonNull(loader, "缓存加载器不能为空");
    Optional<T> cached = get(cacheName, cacheKey, valueType);
    if (cached.isPresent()) {
      return cached.get();
    }
    T loaded = loader.get();
    if (loaded != null) {
      set(cacheName, cacheKey, loaded, ttl);
    }
    return loaded;
  }

  /**
   * 读取缓存，未命中时加载 {@link Optional} 并按需写回缓存。
   *
   * <p>加载器返回空结果时不会写入 Redis，适合数据库或外部接口可能不存在数据的详情缓存。</p>
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @param valueType 缓存对象类型
   * @param ttl 缓存有效期
   * @param loader 缓存未命中时的数据加载器
   * @param <T> 缓存对象泛型
   * @return 缓存对象
   */
  public <T> Optional<T> getOrLoadOptional(String cacheName, String cacheKey, Class<T> valueType, Duration ttl,
      Supplier<Optional<T>> loader) {
    Objects.requireNonNull(loader, "缓存加载器不能为空");
    Optional<T> cached = get(cacheName, cacheKey, valueType);
    if (cached.isPresent()) {
      return cached;
    }
    Optional<T> loaded = Objects.requireNonNull(loader.get(), "缓存加载器返回 Optional 不能为空");
    loaded.ifPresent(value -> set(cacheName, cacheKey, value, ttl));
    return loaded;
  }

  /**
   * 强制重新加载并刷新业务缓存。
   *
   * <p>加载器返回 {@code null} 时会删除旧缓存，避免脏数据继续命中。</p>
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @param valueType 缓存对象类型
   * @param ttl 缓存有效期
   * @param loader 数据加载器
   * @param <T> 缓存对象泛型
   * @return 最新业务对象
   */
  public <T> T refresh(String cacheName, String cacheKey, Class<T> valueType, Duration ttl, Supplier<T> loader) {
    Objects.requireNonNull(valueType, "缓存对象类型不能为空");
    Objects.requireNonNull(loader, "缓存加载器不能为空");
    T loaded = loader.get();
    if (loaded == null) {
      delete(cacheName, cacheKey);
      return null;
    }
    set(cacheName, cacheKey, loaded, ttl);
    return loaded;
  }

  /**
   * 批量读取业务缓存。
   *
   * <p>返回结果保留入参业务键，自动过滤未命中和类型不匹配的缓存值。</p>
   *
   * @param cacheName 缓存业务名称
   * @param cacheKeys 缓存业务键集合
   * @param valueType 缓存对象类型
   * @param <T> 缓存对象泛型
   * @return 按业务键组织的缓存结果
   */
  public <T> Map<String, T> multiGet(String cacheName, Collection<String> cacheKeys, Class<T> valueType) {
    TextHelper.requireText(cacheName, "缓存业务名称不能为空");
    Objects.requireNonNull(valueType, "缓存对象类型不能为空");
    if (cacheKeys == null || cacheKeys.isEmpty()) {
      return Map.of();
    }
    List<String> normalizedKeys = new ArrayList<>(cacheKeys.size());
    List<String> redisKeys = new ArrayList<>(cacheKeys.size());
    for (String cacheKey : cacheKeys) {
      String normalizedCacheKey = TextHelper.requireText(cacheKey, "缓存业务键不能为空");
      normalizedKeys.add(normalizedCacheKey);
      redisKeys.add(buildKey(cacheName, normalizedCacheKey));
    }
    try {
      List<Object> values = valueOperations().multiGet(redisKeys);
      if (values == null || values.isEmpty()) {
        return Map.of();
      }
      Map<String, T> result = new LinkedHashMap<>();
      int resultSize = Math.min(normalizedKeys.size(), values.size());
      for (int index = 0; index < resultSize; index++) {
        Object value = values.get(index);
        if (valueType.isInstance(value)) {
          result.put(normalizedKeys.get(index), valueType.cast(value));
        } else if (value != null) {
          LOGGER.warn("批量业务缓存类型不匹配，已忽略缓存值，cacheName={}, cacheKey={}, expectedType={}, actualType={}",
              cacheName, normalizedKeys.get(index), valueType.getName(), value.getClass().getName());
        }
      }
      return result;
    } catch (RuntimeException exception) {
      LOGGER.warn("批量读取业务缓存失败，已返回空结果，cacheName={}, size={}", cacheName, cacheKeys.size(), exception);
      return Map.of();
    }
  }

  /**
   * 写入业务缓存。
   *
   * <p>缓存值为空时会删除对应 Key，避免旧缓存继续命中。</p>
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @param value 缓存对象
   * @param ttl 缓存有效期
   * @return 写入成功返回 {@code true}
   */
  public boolean set(String cacheName, String cacheKey, Object value, Duration ttl) {
    String redisKey = buildKey(cacheName, cacheKey);
    requirePositiveTtl(ttl);
    if (value == null) {
      return delete(cacheName, cacheKey);
    }
    try {
      valueOperations().set(redisKey, value, ttl);
      return true;
    } catch (RuntimeException exception) {
      LOGGER.warn("写入业务缓存失败，cacheName={}, cacheKey={}", cacheName, cacheKey, exception);
      return false;
    }
  }

  /**
   * 批量写入业务缓存。
   *
   * <p>每个业务键使用相同有效期；值为空时删除对应旧缓存。</p>
   *
   * @param cacheName 缓存业务名称
   * @param values 缓存业务键和值映射
   * @param ttl 缓存有效期
   * @return 成功写入或删除的数量
   */
  public long setAll(String cacheName, Map<String, ?> values, Duration ttl) {
    TextHelper.requireText(cacheName, "缓存业务名称不能为空");
    requirePositiveTtl(ttl);
    if (values == null || values.isEmpty()) {
      return 0L;
    }
    long successCount = 0L;
    for (Map.Entry<String, ?> entry : values.entrySet()) {
      if (set(cacheName, entry.getKey(), entry.getValue(), ttl)) {
        successCount++;
      }
    }
    return successCount;
  }

  /**
   * 仅当 Key 不存在时写入业务缓存。
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @param value 缓存对象
   * @param ttl 缓存有效期
   * @return 写入成功返回 {@code true}
   */
  public boolean setIfAbsent(String cacheName, String cacheKey, Object value, Duration ttl) {
    String redisKey = buildKey(cacheName, cacheKey);
    requirePositiveTtl(ttl);
    Objects.requireNonNull(value, "缓存对象不能为空");
    try {
      return Boolean.TRUE.equals(valueOperations().setIfAbsent(redisKey, value, ttl));
    } catch (RuntimeException exception) {
      LOGGER.warn("写入业务缓存占位失败，cacheName={}, cacheKey={}", cacheName, cacheKey, exception);
      return false;
    }
  }

  /**
   * 删除单个业务缓存。
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @return 删除成功返回 {@code true}
   */
  public boolean delete(String cacheName, String cacheKey) {
    String redisKey = buildKey(cacheName, cacheKey);
    try {
      return Boolean.TRUE.equals(redisOperations.delete(redisKey));
    } catch (RuntimeException exception) {
      LOGGER.warn("删除业务缓存失败，cacheName={}, cacheKey={}", cacheName, cacheKey, exception);
      return false;
    }
  }

  /**
   * 批量删除业务缓存。
   *
   * @param cacheName 缓存业务名称
   * @param cacheKeys 缓存业务键集合
   * @return 实际删除数量
   */
  public long delete(String cacheName, Collection<String> cacheKeys) {
    TextHelper.requireText(cacheName, "缓存业务名称不能为空");
    if (cacheKeys == null || cacheKeys.isEmpty()) {
      return 0L;
    }
    List<String> redisKeys = new ArrayList<>(cacheKeys.size());
    for (String cacheKey : cacheKeys) {
      redisKeys.add(buildKey(cacheName, cacheKey));
    }
    try {
      Long deleted = redisOperations.delete(redisKeys);
      return deleted == null ? 0L : deleted;
    } catch (RuntimeException exception) {
      LOGGER.warn("批量删除业务缓存失败，cacheName={}, size={}", cacheName, cacheKeys.size(), exception);
      return 0L;
    }
  }

  /**
   * 判断业务缓存是否存在。
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @return 存在返回 {@code true}
   */
  public boolean exists(String cacheName, String cacheKey) {
    String redisKey = buildKey(cacheName, cacheKey);
    try {
      return Boolean.TRUE.equals(redisOperations.hasKey(redisKey));
    } catch (RuntimeException exception) {
      LOGGER.warn("判断业务缓存是否存在失败，cacheName={}, cacheKey={}", cacheName, cacheKey, exception);
      return false;
    }
  }

  /**
   * 刷新业务缓存过期时间。
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @param ttl 新的缓存有效期
   * @return 设置成功返回 {@code true}
   */
  public boolean expire(String cacheName, String cacheKey, Duration ttl) {
    String redisKey = buildKey(cacheName, cacheKey);
    requirePositiveTtl(ttl);
    try {
      return Boolean.TRUE.equals(redisOperations.expire(redisKey, ttl));
    } catch (RuntimeException exception) {
      LOGGER.warn("刷新业务缓存过期时间失败，cacheName={}, cacheKey={}", cacheName, cacheKey, exception);
      return false;
    }
  }

  /**
   * 查询业务缓存剩余有效期。
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @return 存在过期时间时返回剩余有效期
   */
  public Optional<Duration> ttl(String cacheName, String cacheKey) {
    String redisKey = buildKey(cacheName, cacheKey);
    try {
      Long seconds = redisOperations.getExpire(redisKey, TimeUnit.SECONDS);
      if (seconds == null || seconds < 0) {
        return Optional.empty();
      }
      return Optional.of(Duration.ofSeconds(seconds));
    } catch (RuntimeException exception) {
      LOGGER.warn("查询业务缓存过期时间失败，cacheName={}, cacheKey={}", cacheName, cacheKey, exception);
      return Optional.empty();
    }
  }

  /**
   * 构造业务缓存 Redis Key。
   *
   * @param cacheName 缓存业务名称
   * @param cacheKey 缓存业务键
   * @return Redis Key
   */
  public String buildKey(String cacheName, String cacheKey) {
    return keyPrefix + ':' + BUSINESS_NAMESPACE + ':' + TextHelper.requireText(cacheName, "缓存业务名称不能为空")
        + ':' + TextHelper.requireText(cacheKey, "缓存业务键不能为空");
  }

  /**
   * 构造带租户隔离的业务缓存键。
   *
   * <p>调用方可将返回值作为 {@link #buildKey(String, String)} 的 {@code cacheKey}，
   * 避免多租户业务缓存遗漏租户维度。</p>
   *
   * @param tenantId 租户业务标识
   * @param businessKey 业务缓存键
   * @return 带租户隔离的业务缓存键
   */
  public String buildTenantKey(String tenantId, String businessKey) {
    return TextHelper.requireText(tenantId, "租户标识不能为空") + ':'
        + TextHelper.requireText(businessKey, "业务缓存键不能为空");
  }

  private ValueOperations<String, Object> valueOperations() {
    return redisOperations.opsForValue();
  }

  private void requirePositiveTtl(Duration ttl) {
    Objects.requireNonNull(ttl, "缓存有效期不能为空");
    if (ttl.isZero() || ttl.isNegative()) {
      throw new IllegalArgumentException("缓存有效期必须大于 0");
    }
  }

  private String normalizePrefix(String prefix) {
    String normalized = TextHelper.defaultIfBlank(prefix, DEFAULT_PREFIX);
    String withoutTrailingColon = TextHelper.removeTrailingRepeated(normalized, ":");
    return TextHelper.defaultIfBlank(withoutTrailingColon, DEFAULT_PREFIX);
  }
}
