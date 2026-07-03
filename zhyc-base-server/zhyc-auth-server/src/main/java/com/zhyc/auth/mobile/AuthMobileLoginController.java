/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.mobile;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 移动端登录控制器。
 *
 * <p>该入口用于 uni-app、小程序和后续 App 的一方客户端登录，返回认证中心签发的访问令牌；
 * 请求和响应均不包含 OAuth2 客户端密钥。</p>
 */
@RestController
@RequestMapping("/mobile/auth")
public class AuthMobileLoginController {

  /** 移动端登录服务。 */
  private final AuthMobileLoginService loginService;

  /**
   * 创建移动端登录控制器。
   *
   * @param loginService 移动端登录服务
   */
  public AuthMobileLoginController(AuthMobileLoginService loginService) {
    this.loginService = loginService;
  }

  /**
   * 使用账号密码登录移动端。
   *
   * @param request 移动端登录请求
   * @return 移动端登录令牌响应
   */
  @PostMapping("/login")
  public AuthMobileApiResult<AuthMobileLoginResponse> login(@RequestBody AuthMobileLoginRequest request) {
    return AuthMobileApiResult.ok(loginService.login(request));
  }

  /**
   * 处理移动端登录失败。
   *
   * @param exception 认证失败异常
   * @return 失败响应
   */
  @ExceptionHandler({BadCredentialsException.class, DisabledException.class, IllegalArgumentException.class})
  public AuthMobileApiResult<Void> handleLoginException(RuntimeException exception) {
    return AuthMobileApiResult.fail("MOBILE_AUTH_LOGIN_FAILED", exception.getMessage());
  }
}
