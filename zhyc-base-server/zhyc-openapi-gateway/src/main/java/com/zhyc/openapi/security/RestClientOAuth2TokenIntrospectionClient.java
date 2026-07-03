/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * 基于 Spring RestClient 的 OAuth2 Token introspection 客户端。
 */
public class RestClientOAuth2TokenIntrospectionClient implements OAuth2TokenIntrospectionClient {

  /** OAuth2 introspection 端点地址。 */
  private final String introspectionUri;
  /** introspection 客户端 ID。 */
  private final String clientId;
  /** introspection 客户端密钥。 */
  private final String clientSecret;
  /** HTTP 客户端。 */
  private final RestClient restClient;
  /** introspection 响应类型引用。 */
  private final ParameterizedTypeReference<Map<String, Object>> responseType =
      new ParameterizedTypeReference<>() {
      };

  /**
   * 创建 RestClient OAuth2 Token introspection 客户端。
   *
   * @param introspectionUri OAuth2 introspection 端点地址
   * @param clientId introspection 客户端 ID
   * @param clientSecret introspection 客户端密钥
   * @param restClient HTTP 客户端
   */
  public RestClientOAuth2TokenIntrospectionClient(String introspectionUri, String clientId,
      String clientSecret, RestClient restClient) {
    this.introspectionUri = requireText(introspectionUri, "OAuth2 introspection 端点不能为空");
    this.clientId = requireText(clientId, "OAuth2 introspection 客户端 ID 不能为空");
    this.clientSecret = requireText(clientSecret, "OAuth2 introspection 客户端密钥不能为空");
    this.restClient = Objects.requireNonNull(restClient, "HTTP 客户端不能为空");
  }

  /**
   * 调用 OAuth2 introspection 端点解析访问令牌。
   *
   * @param accessToken OAuth2 访问令牌
   * @return introspection 响应，未激活时返回 inactive 响应
   */
  @Override
  public OAuth2TokenIntrospectionResponse introspect(String accessToken) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("token", accessToken);
    Map<String, Object> response = restClient.post()
        .uri(introspectionUri)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .header("Authorization", "Basic " + basicCredential())
        .body(body)
        .retrieve()
        .body(responseType);
    if (response == null || !Boolean.TRUE.equals(response.get("active"))) {
      return OAuth2TokenIntrospectionResponse.inactive();
    }
    return OAuth2TokenIntrospectionResponse.active(response);
  }

  private String basicCredential() {
    String raw = clientId + ":" + clientSecret;
    return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  private String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }
}
