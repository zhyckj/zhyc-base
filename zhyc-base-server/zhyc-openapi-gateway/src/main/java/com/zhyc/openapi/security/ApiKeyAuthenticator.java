/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * API Key 鉴权服务。
 *
 * <p>该服务负责组合 accessKey 凭证查询、凭证状态检查、HMAC 签名校验、时间戳窗口校验和 nonce
 * 防重放保护。当前类不直接依赖数据库，真实凭证读取由 {@link ApiKeyCredentialRepository} 的实现提供。</p>
 */
public class ApiKeyAuthenticator {

  /** API Key 凭证仓储。 */
  private final ApiKeyCredentialRepository credentialRepository;
  /** API Key HMAC-SHA256 签名校验器。 */
  private final ApiKeySignatureVerifier signatureVerifier;
  /** 请求防重放保护器。 */
  private final ReplayProtector replayProtector;
  /** 开放 API 签名策略仓储。 */
  private final OpenApiSignaturePolicyRepository signaturePolicyRepository;
  /** 当前时间来源。 */
  private final Clock clock;

  /**
   * 创建 API Key 鉴权服务。
   *
   * @param credentialRepository API Key 凭证仓储
   * @param signatureVerifier API Key HMAC-SHA256 签名校验器
   * @param replayProtector 请求防重放保护器
   * @param clock 当前时间来源
   */
  public ApiKeyAuthenticator(ApiKeyCredentialRepository credentialRepository,
      ApiKeySignatureVerifier signatureVerifier, ReplayProtector replayProtector, Clock clock) {
    this(credentialRepository, signatureVerifier, replayProtector,
        new EmptyOpenApiSignaturePolicyRepository(), clock);
  }

  /**
   * 创建 API Key 鉴权服务。
   *
   * @param credentialRepository API Key 凭证仓储
   * @param signatureVerifier API Key HMAC-SHA256 签名校验器
   * @param replayProtector 请求防重放保护器
   * @param signaturePolicyRepository 开放 API 签名策略仓储
   * @param clock 当前时间来源
   */
  public ApiKeyAuthenticator(ApiKeyCredentialRepository credentialRepository,
      ApiKeySignatureVerifier signatureVerifier, ReplayProtector replayProtector,
      OpenApiSignaturePolicyRepository signaturePolicyRepository, Clock clock) {
    this.credentialRepository = Objects.requireNonNull(credentialRepository,
        "API Key 凭证仓储不能为空");
    this.signatureVerifier = Objects.requireNonNull(signatureVerifier, "API Key 签名校验器不能为空");
    this.replayProtector = Objects.requireNonNull(replayProtector, "请求防重放保护器不能为空");
    this.signaturePolicyRepository = Objects.requireNonNull(signaturePolicyRepository,
        "开放 API 签名策略仓储不能为空");
    this.clock = Objects.requireNonNull(clock, "当前时间来源不能为空");
  }

  /**
   * 执行 API Key 鉴权。
   *
   * @param request API Key 鉴权请求
   * @return API Key 鉴权结果
   */
  public ApiKeyAuthenticationResult authenticate(ApiKeyAuthenticationRequest request) {
    if (request == null || isBlank(request.getAccessKey())) {
      return ApiKeyAuthenticationResult.failure("API_KEY_NOT_FOUND");
    }
    ApiKeyCredential credential = credentialRepository.findByAccessKey(request.getAccessKey())
        .orElse(null);
    if (credential == null) {
      return ApiKeyAuthenticationResult.failure("API_KEY_NOT_FOUND");
    }
    if (!credential.isEnabled()) {
      return ApiKeyAuthenticationResult.failure("API_KEY_DISABLED");
    }
    if (isExpired(credential.getExpireAt())) {
      return ApiKeyAuthenticationResult.failure("API_KEY_EXPIRED");
    }
    Instant requestTime = parseUnixSeconds(request.getTimestamp());
    OpenApiSignaturePolicy signaturePolicy = signaturePolicyRepository
        .findEnabledPolicy(credential.getTenantId(), credential.getAppCode())
        .orElse(null);
    if (requestTime == null || !isWithinTimestampWindow(requestTime, signaturePolicy)) {
      return ApiKeyAuthenticationResult.failure("INVALID_TIMESTAMP");
    }
    if (!isBodyHashAccepted(request, signaturePolicy)) {
      return ApiKeyAuthenticationResult.failure("INVALID_BODY_HASH");
    }
    boolean signatureValid = signatureVerifier.verify(request.getMethod(), request.getPath(),
        request.getTimestamp(), request.getNonce(), request.getBody(), credential.getSecretValue(),
        request.getSignature());
    if (!signatureValid) {
      return ApiKeyAuthenticationResult.failure("INVALID_SIGNATURE");
    }
    if (!acceptNonce(credential.getAccessKey(), request.getNonce(), requestTime, signaturePolicy)) {
      return ApiKeyAuthenticationResult.failure("REPLAY_REQUEST");
    }
    return ApiKeyAuthenticationResult.success(credential.getTenantId(), credential.getAppCode());
  }

  private boolean isWithinTimestampWindow(Instant requestTime, OpenApiSignaturePolicy signaturePolicy) {
    if (signaturePolicy == null) {
      return replayProtector.isWithinWindow(requestTime);
    }
    return replayProtector.isWithinWindow(requestTime, signaturePolicy.getTimestampTolerance());
  }

  private boolean acceptNonce(String accessKey, String nonce, Instant requestTime,
      OpenApiSignaturePolicy signaturePolicy) {
    if (signaturePolicy == null) {
      return replayProtector.accept(accessKey, nonce, requestTime);
    }
    return replayProtector.accept(accessKey, nonce, requestTime,
        signaturePolicy.getTimestampTolerance(), signaturePolicy.getNonceTtl());
  }

  private boolean isBodyHashAccepted(ApiKeyAuthenticationRequest request,
      OpenApiSignaturePolicy signaturePolicy) {
    return signaturePolicy == null
        || !signaturePolicy.isRequireBodyHash()
        || signatureVerifier.verifyBodySha256(request.getBody(), request.getBodySha256());
  }

  /**
   * 将 Unix 秒时间戳解析为 Instant。
   *
   * @param timestamp Unix 秒字符串
   * @return 解析后的时间，格式非法时为空
   */
  private Instant parseUnixSeconds(String timestamp) {
    if (isBlank(timestamp)) {
      return null;
    }
    try {
      return Instant.ofEpochSecond(Long.parseLong(timestamp));
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  /**
   * 判断 API Key 是否已经到达或超过过期时间。
   *
   * @param expireAt API Key 过期时间，为空表示长期有效
   * @return 已过期返回 {@code true}
   */
  private boolean isExpired(Instant expireAt) {
    return expireAt != null && !expireAt.isAfter(Instant.now(clock));
  }

  /**
   * 判断输入是否为空白。
   *
   * @param value 待检查字符串
   * @return 为空或空白返回 {@code true}
   */
  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
