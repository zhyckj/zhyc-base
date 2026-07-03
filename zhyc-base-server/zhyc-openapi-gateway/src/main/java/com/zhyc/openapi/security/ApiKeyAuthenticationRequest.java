/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

/**
 * API Key 鉴权请求。
 */
public class ApiKeyAuthenticationRequest {

  /** API 访问密钥。 */
  private final String accessKey;
  /** HTTP 请求方法。 */
  private final String method;
  /** 请求路径。 */
  private final String path;
  /** 客户端请求时间戳，Unix 秒。 */
  private final String timestamp;
  /** 客户端一次性随机串。 */
  private final String nonce;
  /** 请求体。 */
  private final String body;
  /** 客户端提交的请求体 SHA-256 摘要。 */
  private final String bodySha256;
  /** 客户端提交的 HMAC-SHA256 签名。 */
  private final String signature;

  /**
   * 创建 API Key 鉴权请求。
   *
   * @param accessKey API 访问密钥
   * @param method HTTP 请求方法
   * @param path 请求路径
   * @param timestamp 客户端请求时间戳，Unix 秒
   * @param nonce 客户端一次性随机串
   * @param body 请求体
   * @param signature 客户端提交的 HMAC-SHA256 签名
   */
  public ApiKeyAuthenticationRequest(String accessKey, String method, String path, String timestamp,
      String nonce, String body, String signature) {
    this(accessKey, method, path, timestamp, nonce, body, null, signature);
  }

  /**
   * 创建 API Key 鉴权请求。
   *
   * @param accessKey API 访问密钥
   * @param method HTTP 请求方法
   * @param path 请求路径
   * @param timestamp 客户端请求时间戳，Unix 秒
   * @param nonce 客户端一次性随机串
   * @param body 请求体
   * @param bodySha256 客户端提交的请求体 SHA-256 摘要
   * @param signature 客户端提交的 HMAC-SHA256 签名
   */
  public ApiKeyAuthenticationRequest(String accessKey, String method, String path, String timestamp,
      String nonce, String body, String bodySha256, String signature) {
    this.accessKey = accessKey;
    this.method = method;
    this.path = path;
    this.timestamp = timestamp;
    this.nonce = nonce;
    this.body = body;
    this.bodySha256 = bodySha256;
    this.signature = signature;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getMethod() {
    return method;
  }

  public String getPath() {
    return path;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getNonce() {
    return nonce;
  }

  public String getBody() {
    return body;
  }

  public String getBodySha256() {
    return bodySha256;
  }

  public String getSignature() {
    return signature;
  }
}
