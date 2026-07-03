/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zhyc.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * 基于 RestClient 的平台 OAuth2 授权码换令牌服务。
 *
 * <p>该服务在核心平台服务端持有 OAuth2 client secret，浏览器只提交授权码和回调地址。</p>
 */
@Service
public class RestClientPlatformOAuthTokenExchangeService implements PlatformOAuthTokenExchangeService {

  /** 日志记录器，用于保留认证中心 OAuth2 错误码。 */
  private static final Logger LOGGER = LoggerFactory.getLogger(RestClientPlatformOAuthTokenExchangeService.class);
  /** OAuth2 标准错误码响应字段匹配器。 */
  private static final Pattern OAUTH_ERROR_PATTERN = Pattern.compile("\"error\"\\s*:\\s*\"([^\"]+)\"");
  /** 认证中心 token 端点地址。 */
  private final String tokenUri;
  /** OAuth2 客户端标识。 */
  private final String clientId;
  /** OAuth2 客户端密钥，只保存在服务端。 */
  private final String clientSecret;
  /** Spring HTTP 客户端。 */
  private final RestClient restClient;
  /** 认证中心 token 端点响应类型。 */
  private final ParameterizedTypeReference<AuthCenterTokenResponse> responseType =
      new ParameterizedTypeReference<>() {
      };

  /**
   * 创建平台 OAuth2 授权码换令牌服务。
   *
   * @param tokenUri 认证中心 token 端点地址
   * @param clientId OAuth2 客户端标识
   * @param clientSecret OAuth2 客户端密钥
   */
  @Autowired
  public RestClientPlatformOAuthTokenExchangeService(
      @Value("${zhyc.platform.auth.token-uri:http://127.0.0.1:8090/oauth2/token}") String tokenUri,
      @Value("${zhyc.platform.auth.client-id:zhyc-auth-client}") String clientId,
      @Value("${zhyc.platform.auth.client-secret:local_auth_client_secret}") String clientSecret) {
    this(tokenUri, clientId, clientSecret, RestClient.create());
  }

  /**
   * 创建平台 OAuth2 授权码换令牌服务。
   *
   * @param tokenUri 认证中心 token 端点地址
   * @param clientId OAuth2 客户端标识
   * @param clientSecret OAuth2 客户端密钥
   * @param restClient Spring HTTP 客户端
   */
  RestClientPlatformOAuthTokenExchangeService(String tokenUri, String clientId, String clientSecret,
      RestClient restClient) {
    this.tokenUri = requireText(tokenUri, "认证中心 token 端点不能为空");
    this.clientId = requireText(clientId, "平台 OAuth2 客户端标识不能为空");
    this.clientSecret = requireText(clientSecret, "平台 OAuth2 客户端密钥不能为空");
    this.restClient = Objects.requireNonNull(restClient, "HTTP 客户端不能为空");
    LOGGER.info("平台 OAuth2 换令牌服务已初始化：tokenUri={}，clientId={}，secretFingerprint={}",
        this.tokenUri, this.clientId, secretFingerprint(this.clientSecret));
  }

  /**
   * 使用服务端客户端密钥交换授权码。
   *
   * @param command 授权码换令牌命令
   * @return 平台 OAuth2 令牌响应
   */
  @Override
  public PlatformOAuthTokenResponse exchangeAuthorizationCode(PlatformOAuthTokenExchangeCommand command) {
    Objects.requireNonNull(command, "授权码换令牌命令不能为空");
    String code = requireText(command.code(), "统一认证授权码不能为空");
    String redirectUri = requireText(command.redirectUri(), "统一认证回调地址不能为空");
    String codeVerifier = requireText(command.codeVerifier(), "统一认证 PKCE 校验码不能为空");
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("code", code);
    body.add("redirect_uri", redirectUri);
    body.add("code_verifier", codeVerifier);
    return requestToken(body, "AUTH_CENTER_TOKEN_EXCHANGE_FAILED", "认证中心授权码换令牌失败");
  }

  /**
   * 使用服务端客户端密钥刷新访问令牌。
   *
   * @param command 刷新令牌命令
   * @return 平台 OAuth2 令牌响应
   */
  @Override
  public PlatformOAuthTokenResponse refreshAccessToken(PlatformOAuthRefreshTokenCommand command) {
    Objects.requireNonNull(command, "刷新令牌命令不能为空");
    String refreshToken = requireText(command.refreshToken(), "统一认证刷新令牌不能为空");
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "refresh_token");
    body.add("refresh_token", refreshToken);
    return requestToken(body, "AUTH_CENTER_TOKEN_REFRESH_FAILED", "认证中心刷新访问令牌失败");
  }

  /**
   * 调用认证中心 token 端点。
   *
   * @param body OAuth2 token 请求表单
   * @param errorCode 调用失败时的稳定错误码
   * @param errorMessage 调用失败时的业务提示
   * @return 平台 OAuth2 令牌响应
   */
  private PlatformOAuthTokenResponse requestToken(MultiValueMap<String, String> body, String errorCode,
      String errorMessage) {
    try {
      AuthCenterTokenResponse tokenResponse = restClient.post()
          .uri(tokenUri)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .header("Authorization", "Basic " + basicCredential())
          .body(body)
          .retrieve()
          .body(responseType);
      if (tokenResponse == null || !hasText(tokenResponse.accessToken())) {
        throw new BusinessException("AUTH_CENTER_TOKEN_EMPTY", "认证中心未返回访问令牌");
      }
      return tokenResponse.toPlatformResponse();
    } catch (RestClientResponseException exception) {
      String oauthError = resolveOAuthError(exception);
      LOGGER.warn("认证中心 token 端点返回 OAuth2 错误：status={}，error={}", exception.getStatusCode().value(), oauthError);
      throw new BusinessException(errorCode, errorMessage + "：" + oauthError);
    } catch (RestClientException exception) {
      throw new BusinessException(errorCode, errorMessage);
    }
  }

  /**
   * 解析认证中心 OAuth2 错误码。
   *
   * @param exception HTTP 错误响应异常
   * @return 标准 OAuth2 错误码，无法解析时返回 HTTP 状态码
   */
  private static String resolveOAuthError(RestClientResponseException exception) {
    String responseBody = exception.getResponseBodyAsString(StandardCharsets.UTF_8);
    if (!hasText(responseBody)) {
      return "http_status_" + exception.getStatusCode().value();
    }
    Matcher matcher = OAUTH_ERROR_PATTERN.matcher(responseBody);
    return matcher.find() && hasText(matcher.group(1))
        ? matcher.group(1)
        : "http_status_" + exception.getStatusCode().value();
  }

  /**
   * 构建 HTTP Basic 客户端凭证。
   *
   * @return Basic 凭证值
   */
  private String basicCredential() {
    return Base64.getEncoder()
        .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 生成客户端密钥指纹，便于排查平台和认证中心配置是否一致。
   *
   * <p>仅输出 SHA-256 前 12 位十六进制摘要，禁止在日志中输出客户端密钥明文。</p>
   *
   * @param secret 客户端密钥明文
   * @return 客户端密钥指纹
   */
  private static String secretFingerprint(String secret) {
    String normalizedSecret = requireText(secret, "平台 OAuth2 客户端密钥不能为空");
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(normalizedSecret.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest).substring(0, 12);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("当前 JDK 不支持 SHA-256 摘要算法", exception);
    }
  }

  /**
   * 校验必填文本。
   *
   * @param value 原始文本
   * @param message 文本为空时的错误消息
   * @return 去除首尾空白后的文本
   */
  private static String requireText(String value, String message) {
    if (!hasText(value)) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  /**
   * 判断文本是否包含非空白字符。
   *
   * @param value 原始文本
   * @return 包含非空白字符时返回 true
   */
  private static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  /**
   * 认证中心 token 端点响应。
   *
   * @param accessToken 访问令牌
   * @param refreshToken 刷新令牌
   * @param tokenType 令牌类型
   * @param expiresIn 有效期秒数
   * @param scope 授权范围
   */
  private record AuthCenterTokenResponse(
      @JsonProperty("access_token") String accessToken,
      @JsonProperty("refresh_token") String refreshToken,
      @JsonProperty("token_type") String tokenType,
      @JsonProperty("expires_in") Long expiresIn,
      @JsonProperty("scope") String scope) {

    /**
     * 转换为平台令牌响应。
     *
     * @return 平台 OAuth2 令牌响应
     */
    private PlatformOAuthTokenResponse toPlatformResponse() {
      return new PlatformOAuthTokenResponse(accessToken, refreshToken, tokenType, expiresIn, scope);
    }
  }
}
