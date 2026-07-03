/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.apache.shiro.event.EventBus;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.ShiroEventBusBeanPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

/**
 * 平台 Shiro 配置测试。
 *
 * <p>该测试约束 Shiro 基础设施 Bean 必须通过静态工厂方法声明，避免 Spring Boot 4 启动阶段
 * BeanPostProcessor 过早实例化数据源、MyBatis 和业务服务 Bean。</p>
 */
class ShiroConfigTest {

  /**
   * 验证 Shiro 生命周期处理器通过静态 Bean 方法声明。
   *
   * @throws NoSuchMethodException 配置方法不存在时抛出
   */
  @Test
  void shouldDeclareStaticLifecycleBeanPostProcessor() throws NoSuchMethodException {
    assertStaticBeanMethod("lifecycleBeanPostProcessor", LifecycleBeanPostProcessor.class);
  }

  /**
   * 验证 Shiro 事件总线通过静态 Bean 方法声明。
   *
   * @throws NoSuchMethodException 配置方法不存在时抛出
   */
  @Test
  void shouldDeclareStaticEventBus() throws NoSuchMethodException {
    assertStaticBeanMethod("eventBus", EventBus.class);
  }

  /**
   * 验证 Shiro 事件总线处理器通过静态 Bean 方法声明。
   *
   * @throws NoSuchMethodException 配置方法不存在时抛出
   */
  @Test
  void shouldDeclareStaticEventBusBeanPostProcessor() throws NoSuchMethodException {
    assertStaticBeanMethod("shiroEventBusAwareBeanPostProcessor", ShiroEventBusBeanPostProcessor.class, EventBus.class);
  }

  /**
   * 断言指定配置方法是静态 Bean 方法。
   *
   * @param methodName 配置方法名称
   * @param returnType 配置方法返回类型
   * @param parameterTypes 配置方法参数类型
   * @throws NoSuchMethodException 配置方法不存在时抛出
   */
  private void assertStaticBeanMethod(String methodName, Class<?> returnType, Class<?>... parameterTypes)
      throws NoSuchMethodException {
    Method method = ShiroConfig.class.getDeclaredMethod(methodName, parameterTypes);
    assertTrue(Modifier.isStatic(method.getModifiers()), methodName + " 必须声明为 static");
    assertTrue(method.isAnnotationPresent(Bean.class), methodName + " 必须声明 @Bean");
    assertTrue(returnType.isAssignableFrom(method.getReturnType()), methodName + " 返回类型不符合预期");
  }
}
