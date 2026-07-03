/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.zhyc.common.cache.RedisBusinessCacheHelper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 平台缓存配置测试。
 */
class PlatformCacheConfigTest {

  /** 应用上下文启动器。 */
  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(PlatformCacheConfig.class)
      .withBean(RedisConnectionFactory.class, () -> mock(RedisConnectionFactory.class))
      .withPropertyValues("zhyc.cache.enabled=true", "zhyc.cache.prefix=test");

  /**
   * 验证平台缓存配置会注册统一 Redis 业务缓存帮助类。
   */
  @Test
  void shouldRegisterRedisBusinessCacheHelper() {
    contextRunner.run(context -> {
      RedisBusinessCacheHelper cacheHelper = context.getBean(RedisBusinessCacheHelper.class);
      assertNotNull(cacheHelper);
      assertEquals("test:biz:order:detail:1001", cacheHelper.buildKey("order:detail", "1001"));
    });
  }

  /**
   * 验证关闭平台缓存开关后不注册业务缓存帮助类。
   */
  @Test
  void shouldSkipRedisBusinessCacheHelperWhenCacheDisabled() {
    contextRunner.withPropertyValues("zhyc.cache.enabled=false").run(context ->
        assertEquals(0, context.getBeansOfType(RedisBusinessCacheHelper.class).size()));
  }
}
