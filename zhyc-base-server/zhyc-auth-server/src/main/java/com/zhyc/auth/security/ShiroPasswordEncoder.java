/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.security;

import java.util.Objects;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 密码编码器到 Shiro 密码服务的适配器。
 *
 * <p>平台后台用户密码由 Shiro {@link PasswordService} 生成哈希，认证中心登录必须使用同一套校验算法，
 * 避免后台重置密码后统一认证仍校验旧的配置密码。</p>
 */
public class ShiroPasswordEncoder implements PasswordEncoder {

  /** Shiro 密码服务。 */
  private final PasswordService passwordService;

  /**
   * 创建默认 Shiro 密码编码器。
   */
  public ShiroPasswordEncoder() {
    this(new DefaultPasswordService());
  }

  /**
   * 创建 Shiro 密码编码器。
   *
   * @param passwordService Shiro 密码服务
   */
  public ShiroPasswordEncoder(PasswordService passwordService) {
    this.passwordService = Objects.requireNonNull(passwordService, "Shiro 密码服务不能为空");
  }

  /**
   * 生成 Shiro 密码哈希。
   *
   * @param rawPassword 明文密码
   * @return Shiro 密码哈希
   */
  @Override
  public String encode(CharSequence rawPassword) {
    return passwordService.encryptPassword(requirePassword(rawPassword));
  }

  /**
   * 校验明文密码是否匹配 Shiro 密码哈希。
   *
   * @param rawPassword 明文密码
   * @param encodedPassword Shiro 密码哈希
   * @return 匹配时返回 true
   */
  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    if (encodedPassword == null || encodedPassword.isBlank()) {
      return false;
    }
    return passwordService.passwordsMatch(requirePassword(rawPassword), encodedPassword);
  }

  /**
   * 解析非空密码。
   *
   * @param rawPassword 明文密码
   * @return 密码字符串
   */
  private static String requirePassword(CharSequence rawPassword) {
    if (rawPassword == null || rawPassword.toString().isBlank()) {
      throw new IllegalArgumentException("密码不能为空");
    }
    return rawPassword.toString();
  }
}
