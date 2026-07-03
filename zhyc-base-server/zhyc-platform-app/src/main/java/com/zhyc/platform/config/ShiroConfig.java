/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.config;

import com.zhyc.platform.security.PlatformUserRealm;
import com.zhyc.platform.security.PlatformTokenPrincipalMapper;
import com.zhyc.system.permission.service.SysPermissionService;
import com.zhyc.system.user.service.SysUserAuthService;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.event.EventBus;
import org.apache.shiro.event.support.DefaultEventBus;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.ShiroEventBusBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * 平台 Shiro 配置。
 */
@Configuration
public class ShiroConfig {

  /**
   * 定义 Shiro 生命周期处理器。
   *
   * <p>该 Bean 必须声明为静态工厂方法，避免 Spring Boot 4 创建 BeanPostProcessor 时提前实例化
   * Shiro 配置类和业务依赖，导致数据源、MyBatis、权限服务无法被完整后置处理。</p>
   *
   * @return Shiro 生命周期处理器
   */
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
    return new LifecycleBeanPostProcessor();
  }

  /**
   * 定义 Shiro 事件总线。
   *
   * <p>事件总线作为 Shiro 基础设施使用，不承载业务数据。</p>
   *
   * @return Shiro 事件总线
   */
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public static EventBus eventBus() {
    return new DefaultEventBus();
  }

  /**
   * 定义 Shiro 事件总线感知处理器。
   *
   * <p>该 Bean 必须声明为静态工厂方法，避免 Shiro starter 默认非静态方法在 BeanPostProcessor
   * 注册阶段提前拉起 Realm、MyBatis 和数据源。</p>
   *
   * @param eventBus Shiro 事件总线
   * @return Shiro 事件总线感知处理器
   */
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public static ShiroEventBusBeanPostProcessor shiroEventBusAwareBeanPostProcessor(EventBus eventBus) {
    return new ShiroEventBusBeanPostProcessor(eventBus);
  }

  /**
   * 定义后台用户密码服务。
   *
   * <p>密码服务负责解析和校验数据库中的密码哈希值，生产环境必须只存储哈希或密文。</p>
   *
   * @return Shiro 密码服务
   */
  @Bean
  public PasswordService passwordService() {
    return new DefaultPasswordService();
  }

  /**
   * 定义后台用户凭证匹配器。
   *
   * @param passwordService Shiro 密码服务
   * @return 密码凭证匹配器
   */
  @Bean
  public PasswordMatcher passwordMatcher(PasswordService passwordService) {
    PasswordMatcher matcher = new PasswordMatcher();
    matcher.setPasswordService(passwordService);
    return matcher;
  }

  /**
   * 定义平台后台用户 Realm。
   *
   * @param userAuthService 系统用户认证查询服务
   * @param permissionService 系统权限业务服务
   * @param passwordMatcher 密码凭证匹配器
   * @param tokenPrincipalMapper 认证中心令牌主体映射器
   * @return Shiro Realm 实例
   */
  @Bean
  public Realm platformUserRealm(SysUserAuthService userAuthService, SysPermissionService permissionService,
                                 PasswordMatcher passwordMatcher, PlatformTokenPrincipalMapper tokenPrincipalMapper) {
    return new PlatformUserRealm(userAuthService, permissionService, passwordMatcher, tokenPrincipalMapper);
  }

  /**
   * 定义认证中心令牌主体映射器。
   *
   * <p>该映射器只处理认证中心 Claims 到平台 Shiro 主体的转换，不依赖认证中心内部实现。</p>
   *
   * @return 认证中心令牌主体映射器
   */
  @Bean
  public PlatformTokenPrincipalMapper platformTokenPrincipalMapper() {
    return new PlatformTokenPrincipalMapper();
  }

}
