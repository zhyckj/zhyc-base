/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.controller;

import com.zhyc.auth.config.AuthFrontendLoginUris;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 认证中心登录入口控制器。
 *
 * <p>登录页面由后台管理前端承载，本控制器只负责把用户可见的登录入口重定向到前端；
 * {@code POST /login} 仍由 Spring Security 表单登录过滤器处理账号密码认证。</p>
 */
@Controller
public class AuthLoginPageController {

  /**
   * 后台前端登录页地址。
   */
  private final String frontendLoginUri;

  /**
   * 创建认证中心登录入口控制器。
   *
   * @param frontendLoginUri 后台前端登录页地址
   */
  public AuthLoginPageController(
      @Value("${zhyc.auth.frontend-login-uri:http://127.0.0.1:5173/login?authRequest=1}")
      String frontendLoginUri) {
    this.frontendLoginUri = requireText(frontendLoginUri, "前端登录页地址不能为空");
  }

  /**
   * 将认证中心登录页入口重定向到后台前端。
   *
   * @param error 登录失败标记
   * @param logout 退出登录标记
   * @return 前端登录页重定向地址
   */
  @GetMapping("/login")
  public String loginPage(@RequestParam(value = "error", required = false) String error,
                          @RequestParam(value = "logout", required = false) String logout) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
        AuthFrontendLoginUris.normalizeAuthRequestLoginUri(frontendLoginUri));
    if (error != null) {
      builder.queryParam("error", "1");
    }
    if (logout != null) {
      builder.queryParam("loggedOut", "1");
    }
    return "redirect:" + builder.build(true).toUriString();
  }

  /**
   * 校验必填文本。
   *
   * @param value 原始文本
   * @param message 文本为空时的错误消息
   * @return 去除首尾空白后的文本
   */
  private static String requireText(String value, String message) {
    if (value == null) {
      throw new IllegalStateException(message);
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalStateException(message);
    }
    return trimmed;
  }
}
