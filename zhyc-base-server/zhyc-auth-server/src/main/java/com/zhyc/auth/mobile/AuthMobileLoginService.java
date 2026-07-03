/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.mobile;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * 移动端登录服务。
 *
 * <p>该服务面向 uni-app、小程序和后续 App 的一方客户端登录场景，只校验用户账号密码并签发认证中心访问令牌；
 * OAuth2 客户端密钥仍只存在服务端。</p>
 */
@Service
public class AuthMobileLoginService {

  /** 令牌类型。 */
  private static final String TOKEN_TYPE_BEARER = "Bearer";
  /** 移动端默认授权范围。 */
  private static final String DEFAULT_SCOPE = "openid profile";

  /** 认证中心用户查询服务。 */
  private final UserDetailsService userDetailsService;
  /** 密码编码器。 */
  private final PasswordEncoder passwordEncoder;
  /** JWT 编码器。 */
  private final JwtEncoder jwtEncoder;
  /** 认证中心签发方。 */
  private final String issuer;
  /** 平台租户编码 Claim。 */
  private final String platformTenantId;
  /** 平台用户主键 Claim。 */
  private final long platformUserId;
  /** 平台登录账号 Claim。 */
  private final String platformUsername;
  /** 访问令牌有效期。 */
  private final Duration accessTokenTimeToLive;

  /**
   * 创建移动端登录服务。
   *
   * @param userDetailsService 认证中心用户查询服务
   * @param passwordEncoder 密码编码器
   * @param jwtEncoder JWT 编码器
   * @param issuer 认证中心签发方
   * @param platformTenantId 平台租户编码
   * @param platformUserId 平台用户主键
   * @param platformUsername 平台登录账号
   * @param accessTokenTimeToLiveMinutes 访问令牌有效期分钟数
   */
  public AuthMobileLoginService(
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder,
      JwtEncoder jwtEncoder,
      @Value("${zhyc.auth.issuer:http://127.0.0.1:8090}") String issuer,
      @Value("${zhyc.auth.platform-tenant-id}") String platformTenantId,
      @Value("${zhyc.auth.platform-user-id}") String platformUserId,
      @Value("${zhyc.auth.platform-username}") String platformUsername,
      @Value("${zhyc.auth.access-token-time-to-live-minutes:30}") long accessTokenTimeToLiveMinutes) {
    this.userDetailsService = Objects.requireNonNull(userDetailsService, "认证中心用户查询服务不能为空");
    this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "密码编码器不能为空");
    this.jwtEncoder = Objects.requireNonNull(jwtEncoder, "JWT 编码器不能为空");
    this.issuer = requireText(issuer, "认证中心签发方不能为空");
    this.platformTenantId = requireText(platformTenantId, "平台租户编码不能为空");
    this.platformUserId = parsePositiveLong(platformUserId, "平台用户主键必须为正整数");
    this.platformUsername = requireText(platformUsername, "平台登录账号不能为空");
    this.accessTokenTimeToLive = Duration.ofMinutes(requirePositive(
        accessTokenTimeToLiveMinutes, "访问令牌有效期分钟数必须大于 0"));
  }

  /**
   * 执行移动端账号密码登录。
   *
   * @param request 登录请求
   * @return 移动端登录令牌响应
   */
  public AuthMobileLoginResponse login(AuthMobileLoginRequest request) {
    Objects.requireNonNull(request, "移动端登录请求不能为空");
    String username = requireText(request.username(), "请输入账号");
    String password = requireText(request.password(), "请输入密码");
    UserDetails userDetails = loadUser(username);
    if (!userDetails.isEnabled()) {
      throw new DisabledException("账号已被禁用");
    }
    if (!passwordEncoder.matches(password, userDetails.getPassword())) {
      throw new BadCredentialsException("账号或密码错误");
    }
    return buildTokenResponse();
  }

  /**
   * 查询认证中心用户。
   *
   * @param username 登录账号
   * @return 用户详情
   */
  private UserDetails loadUser(String username) {
    try {
      return userDetailsService.loadUserByUsername(username);
    } catch (UsernameNotFoundException exception) {
      throw new BadCredentialsException("账号或密码错误");
    }
  }

  /**
   * 构建移动端访问令牌响应。
   *
   * @return 登录令牌响应
   */
  private AuthMobileLoginResponse buildTokenResponse() {
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plus(accessTokenTimeToLive);
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(issuer)
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .subject(platformUsername)
        .claim("scope", DEFAULT_SCOPE)
        .claim("tenant_id", platformTenantId)
        .claim("user_id", platformUserId)
        .claim("preferred_username", platformUsername)
        .build();
    String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
        JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
    return new AuthMobileLoginResponse(
        accessToken,
        TOKEN_TYPE_BEARER,
        accessTokenTimeToLive.toSeconds(),
        platformTenantId,
        platformUserId,
        platformUsername);
  }

  /**
   * 校验必填文本。
   *
   * @param value 原始文本
   * @param message 文本为空时的错误消息
   * @return 去除首尾空白后的文本
   */
  private static String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  /**
   * 解析正整数。
   *
   * @param value 原始文本
   * @param message 非正整数时的错误消息
   * @return 正整数
   */
  private static long parsePositiveLong(String value, String message) {
    try {
      return requirePositive(Long.parseLong(requireText(value, message)), message);
    } catch (NumberFormatException exception) {
      throw new IllegalStateException(message);
    }
  }

  /**
   * 校验正整数。
   *
   * @param value 原始数值
   * @param message 非正数时的错误消息
   * @return 正整数
   */
  private static long requirePositive(long value, String message) {
    if (value <= 0) {
      throw new IllegalStateException(message);
    }
    return value;
  }
}
