/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;

/**
 * 移动端登录服务测试。
 */
class AuthMobileLoginServiceTest {

  /**
   * 验证移动端账号密码登录成功后会签发可被平台解析的访问令牌。
   */
  @Test
  void shouldIssuePlatformAccessTokenForMobileLogin() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    AuthMobileLoginService service = new AuthMobileLoginService(
        new InMemoryUserDetailsManager(User.withUsername("auth-admin")
            .password(passwordEncoder.encode("auth-password"))
            .roles("AUTH_USER")
            .build()),
        passwordEncoder,
        new NimbusJwtEncoder((jwkSelector, securityContext) -> jwkSelector.select(new JWKSet(toRsaKey(keyPair)))),
        "http://127.0.0.1:8090",
        "zhyc-platform",
        "1",
        "admin",
        30L);

    AuthMobileLoginResponse response = service.login(new AuthMobileLoginRequest("auth-admin", "auth-password"));
    JwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
    Jwt jwt = decoder.decode(response.accessToken());

    assertEquals("Bearer", response.tokenType());
    assertEquals(1800L, response.expiresIn());
    assertEquals("zhyc-platform", response.tenantId());
    assertEquals(1L, response.userId());
    assertEquals("admin", response.accountName());
    assertEquals("zhyc-platform", jwt.getClaimAsString("tenant_id"));
    assertEquals(1L, ((Number) jwt.getClaim("user_id")).longValue());
    assertEquals("admin", jwt.getClaimAsString("preferred_username"));
    assertEquals("http://127.0.0.1:8090", jwt.getIssuer().toString());
  }

  /**
   * 验证密码错误时拒绝登录，避免移动端获得无效令牌。
   */
  @Test
  void shouldRejectInvalidMobilePassword() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    AuthMobileLoginService service = new AuthMobileLoginService(
        new InMemoryUserDetailsManager(User.withUsername("auth-admin")
            .password(passwordEncoder.encode("auth-password"))
            .roles("AUTH_USER")
            .build()),
        passwordEncoder,
        new NimbusJwtEncoder((jwkSelector, securityContext) -> jwkSelector.select(new JWKSet(toRsaKey(keyPair)))),
        "http://127.0.0.1:8090",
        "zhyc-platform",
        "1",
        "admin",
        30L);

    BadCredentialsException exception = assertThrows(BadCredentialsException.class,
        () -> service.login(new AuthMobileLoginRequest("auth-admin", "wrong-password")));

    assertTrue(exception.getMessage().contains("账号或密码错误"));
  }

  /**
   * 生成测试 RSA 密钥对。
   *
   * @return RSA 密钥对
   */
  private static KeyPair generateRsaKeyPair() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    return keyPairGenerator.generateKeyPair();
  }

  /**
   * 转换为 JWK RSA 密钥。
   *
   * @param keyPair RSA 密钥对
   * @return RSA JWK
   */
  private static RSAKey toRsaKey(KeyPair keyPair) {
    return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
        .privateKey((RSAPrivateKey) keyPair.getPrivate())
        .keyID(UUID.randomUUID().toString())
        .build();
  }
}
