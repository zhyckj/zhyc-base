/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * API Key 鉴权服务测试，覆盖凭证状态、签名、时间戳和 nonce 防重放。
 */
class ApiKeyAuthenticatorTest {

  /** 固定测试时间。 */
  private final MutableClock clock = new MutableClock(Instant.parse("2026-06-24T00:00:00Z"));
  /** 签名校验器。 */
  private final ApiKeySignatureVerifier verifier = new ApiKeySignatureVerifier();
  /** 测试凭证仓储。 */
  private final RecordingCredentialRepository repository = new RecordingCredentialRepository();
  /** API Key 鉴权服务。 */
  private final ApiKeyAuthenticator authenticator = new ApiKeyAuthenticator(repository, verifier,
      new ReplayProtector(Duration.ofMinutes(5), clock, 100), clock);

  @Test
  void authenticateReturnsSuccessWhenCredentialSignatureAndNonceAreValid() {
    String signature = sign("POST", "/openapi/v1/purchase/requests", "1782259200", "nonce-001", "{}");

    ApiKeyAuthenticationResult result = authenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "POST", "/openapi/v1/purchase/requests", "1782259200", "nonce-001", "{}",
        signature));

    assertTrue(result.isAuthenticated());
    assertEquals("tenant_a", result.getTenantId());
    assertEquals("purchase-app", result.getAppCode());
  }

  @Test
  void authenticateRejectsWrongSignatureWithoutRecordingNonce() {
    ApiKeyAuthenticationRequest badRequest = new ApiKeyAuthenticationRequest("ak_test", "POST",
        "/openapi/v1/purchase/requests", "1782259200", "nonce-002", "{}", "bad-signature");

    ApiKeyAuthenticationResult badResult = authenticator.authenticate(badRequest);
    String signature = sign("POST", "/openapi/v1/purchase/requests", "1782259200", "nonce-002", "{}");
    ApiKeyAuthenticationResult retryResult = authenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "POST", "/openapi/v1/purchase/requests", "1782259200", "nonce-002", "{}",
        signature));

    assertFalse(badResult.isAuthenticated());
    assertEquals("INVALID_SIGNATURE", badResult.getFailureCode());
    assertTrue(retryResult.isAuthenticated());
  }

  @Test
  void authenticateRejectsReplayNonce() {
    String signature = sign("POST", "/openapi/v1/purchase/requests", "1782259200", "nonce-003", "{}");
    ApiKeyAuthenticationRequest request = new ApiKeyAuthenticationRequest("ak_test", "POST",
        "/openapi/v1/purchase/requests", "1782259200", "nonce-003", "{}", signature);

    assertTrue(authenticator.authenticate(request).isAuthenticated());
    ApiKeyAuthenticationResult replayResult = authenticator.authenticate(request);

    assertFalse(replayResult.isAuthenticated());
    assertEquals("REPLAY_REQUEST", replayResult.getFailureCode());
  }

  @Test
  void authenticateRejectsDisabledCredential() {
    repository.credential = repository.credential.withStatus("disabled");
    String signature = sign("GET", "/openapi/v1/purchase/requests/1", "1782259200", "nonce-004", null);

    ApiKeyAuthenticationResult result = authenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "GET", "/openapi/v1/purchase/requests/1", "1782259200", "nonce-004", null,
        signature));

    assertFalse(result.isAuthenticated());
    assertEquals("API_KEY_DISABLED", result.getFailureCode());
  }

  /**
   * 验证未知凭证状态会按禁用凭证拒绝，避免运行态异常暴露给调用方。
   */
  @Test
  void authenticateRejectsUnsupportedCredentialStatusAsDisabled() {
    repository.credential = repository.credential.withStatus("pending");
    String signature = sign("GET", "/openapi/v1/purchase/requests/1", "1782259200", "nonce-011", null);

    ApiKeyAuthenticationResult result = authenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "GET", "/openapi/v1/purchase/requests/1", "1782259200", "nonce-011", null,
        signature));

    assertFalse(result.isAuthenticated());
    assertEquals("API_KEY_DISABLED", result.getFailureCode());
  }

  @Test
  void authenticateRejectsExpiredCredential() {
    repository.credential = repository.credential.withExpireAt(Instant.parse("2026-06-23T23:59:59Z"));
    String signature = sign("GET", "/openapi/v1/purchase/requests/1", "1782259200", "nonce-005", null);

    ApiKeyAuthenticationResult result = authenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "GET", "/openapi/v1/purchase/requests/1", "1782259200", "nonce-005", null,
        signature));

    assertFalse(result.isAuthenticated());
    assertEquals("API_KEY_EXPIRED", result.getFailureCode());
  }

  /**
   * 验证 API Key 到达过期时间点后立即失效，避免边界时刻继续放行。
   */
  @Test
  void authenticateRejectsCredentialExpiringAtCurrentTime() {
    repository.credential = repository.credential.withExpireAt(Instant.parse("2026-06-24T00:00:00Z"));
    String signature = sign("GET", "/openapi/v1/purchase/requests/1", "1782259200", "nonce-008", null);

    ApiKeyAuthenticationResult result = authenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "GET", "/openapi/v1/purchase/requests/1", "1782259200", "nonce-008", null,
        signature));

    assertFalse(result.isAuthenticated());
    assertEquals("API_KEY_EXPIRED", result.getFailureCode());
  }

  @Test
  void authenticateRejectsUnknownAccessKey() {
    ApiKeyAuthenticationResult result = authenticator.authenticate(new ApiKeyAuthenticationRequest(
        "missing", "GET", "/openapi/v1/purchase/requests/1", "1782259200", "nonce-006", null,
        "signature"));

    assertFalse(result.isAuthenticated());
    assertEquals("API_KEY_NOT_FOUND", result.getFailureCode());
  }

  @Test
  void authenticateRejectsInvalidTimestamp() {
    ApiKeyAuthenticationResult result = authenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "GET", "/openapi/v1/purchase/requests/1", "not-number", "nonce-007", null,
        "signature"));

    assertFalse(result.isAuthenticated());
    assertEquals("INVALID_TIMESTAMP", result.getFailureCode());
  }

  /**
   * 验证时间戳超出允许窗口时返回稳定的时间戳错误码，而不是混入重放失败。
   */
  @Test
  void authenticateRejectsTimestampOutsideReplayWindowAsInvalidTimestamp() {
    String oldTimestamp = "1782258899";
    String futureTimestamp = "1782259501";
    String oldSignature = sign("GET", "/openapi/v1/purchase/requests/1",
        oldTimestamp, "nonce-009", null);
    String futureSignature = sign("GET", "/openapi/v1/purchase/requests/1",
        futureTimestamp, "nonce-010", null);

    ApiKeyAuthenticationResult oldResult = authenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "GET", "/openapi/v1/purchase/requests/1", oldTimestamp, "nonce-009", null,
        oldSignature));
    ApiKeyAuthenticationResult futureResult = authenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "GET", "/openapi/v1/purchase/requests/1", futureTimestamp, "nonce-010", null,
        futureSignature));

    assertFalse(oldResult.isAuthenticated());
    assertEquals("INVALID_TIMESTAMP", oldResult.getFailureCode());
    assertFalse(futureResult.isAuthenticated());
    assertEquals("INVALID_TIMESTAMP", futureResult.getFailureCode());
  }

  /**
   * 验证启用签名策略会收紧 API Key 时间戳窗口，避免管理端配置不进入网关运行态。
   */
  @Test
  void authenticateUsesEnabledSignaturePolicyTimestampWindow() {
    ApiKeyAuthenticator strictAuthenticator = new ApiKeyAuthenticator(repository, verifier,
        new ReplayProtector(Duration.ofMinutes(5), clock, 100),
        new FixedSignaturePolicyRepository(new OpenApiSignaturePolicy("tenant_a", "purchase-app",
            "HMAC_SHA256", Duration.ofSeconds(60), Duration.ofSeconds(120), true)),
        clock);
    String timestamp = "1782259139";
    String signature = sign("GET", "/openapi/v1/purchase/requests/1", timestamp, "nonce-012", null);

    ApiKeyAuthenticationResult result = strictAuthenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "GET", "/openapi/v1/purchase/requests/1", timestamp, "nonce-012", null,
        signature));

    assertFalse(result.isAuthenticated());
    assertEquals("INVALID_TIMESTAMP", result.getFailureCode());
  }

  /**
   * 验证签名策略要求请求体摘要时，缺失摘要头会被拒绝。
   */
  @Test
  void authenticateRejectsMissingBodyHashWhenSignaturePolicyRequiresIt() {
    ApiKeyAuthenticator strictAuthenticator = new ApiKeyAuthenticator(repository, verifier,
        new ReplayProtector(Duration.ofMinutes(5), clock, 100),
        new FixedSignaturePolicyRepository(new OpenApiSignaturePolicy("tenant_a", "purchase-app",
            "HMAC_SHA256", Duration.ofSeconds(300), Duration.ofSeconds(300), true)),
        clock);
    String body = "{\"amount\":100}";
    String signature = sign("POST", "/openapi/v1/purchase/requests", "1782259200", "nonce-013", body);

    ApiKeyAuthenticationResult result = strictAuthenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "POST", "/openapi/v1/purchase/requests", "1782259200", "nonce-013", body,
        signature));

    assertFalse(result.isAuthenticated());
    assertEquals("INVALID_BODY_HASH", result.getFailureCode());
  }

  /**
   * 验证签名策略要求请求体摘要时，合法摘要和签名会一起放行。
   */
  @Test
  void authenticateAcceptsBodyHashWhenSignaturePolicyRequiresIt() {
    ApiKeyAuthenticator strictAuthenticator = new ApiKeyAuthenticator(repository, verifier,
        new ReplayProtector(Duration.ofMinutes(5), clock, 100),
        new FixedSignaturePolicyRepository(new OpenApiSignaturePolicy("tenant_a", "purchase-app",
            "HMAC_SHA256", Duration.ofSeconds(300), Duration.ofSeconds(300), true)),
        clock);
    String body = "{\"amount\":100}";
    String signature = sign("POST", "/openapi/v1/purchase/requests", "1782259200", "nonce-014", body);

    ApiKeyAuthenticationResult result = strictAuthenticator.authenticate(new ApiKeyAuthenticationRequest(
        "ak_test", "POST", "/openapi/v1/purchase/requests", "1782259200", "nonce-014", body,
        verifier.bodySha256Hex(body), signature));

    assertTrue(result.isAuthenticated());
    assertEquals("tenant_a", result.getTenantId());
    assertEquals("purchase-app", result.getAppCode());
  }

  private String sign(String method, String path, String timestamp, String nonce, String body) {
    return verifier.sign(method, path, timestamp, nonce, body, "runtime-secret");
  }

  /**
   * 测试用 API Key 凭证仓储。
   */
  private static final class RecordingCredentialRepository implements ApiKeyCredentialRepository {

    /** 当前测试凭证。 */
    private ApiKeyCredential credential = new ApiKeyCredential("tenant_a", "purchase-app",
        "ak_test", "runtime-secret", "enabled", Instant.parse("2026-12-31T00:00:00Z"));

    @Override
    public Optional<ApiKeyCredential> findByAccessKey(String accessKey) {
      if (credential.getAccessKey().equals(accessKey)) {
        return Optional.of(credential);
      }
      return Optional.empty();
    }
  }

  /**
   * 测试用固定签名策略仓储。
   */
  private static final class FixedSignaturePolicyRepository implements OpenApiSignaturePolicyRepository {

    /** 固定返回的签名策略。 */
    private final OpenApiSignaturePolicy policy;

    private FixedSignaturePolicyRepository(OpenApiSignaturePolicy policy) {
      this.policy = policy;
    }

    @Override
    public Optional<OpenApiSignaturePolicy> findEnabledPolicy(String tenantId, String appCode) {
      if (policy.getTenantId().equals(tenantId) && policy.getAppCode().equals(appCode)) {
        return Optional.of(policy);
      }
      return Optional.empty();
    }
  }

  /**
   * 测试用可变时钟。
   */
  private static final class MutableClock extends Clock {

    /** 当前测试时间。 */
    private Instant instant;

    private MutableClock(Instant instant) {
      this.instant = instant;
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }
}
