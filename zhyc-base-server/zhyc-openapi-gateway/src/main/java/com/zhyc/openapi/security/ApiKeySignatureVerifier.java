/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * API Key HMAC-SHA256 签名校验器。
 *
 * <p>签名原文格式固定为五行：HTTP_METHOD、PATH、TIMESTAMP、NONCE、SHA256_HEX_BODY。
 * HTTP 方法会统一转换为大写，请求体为 {@code null} 时按空字符串计算 SHA-256。
 * 校验时 method、path、timestamp、nonce、secret、signature 均不允许为空白；签名只接受
 * 64 位 hex 字符串；method、path、timestamp、nonce 不允许包含 CR/LF。</p>
 */
public class ApiKeySignatureVerifier {

  /** HMAC 签名算法名称。 */
  private static final String HMAC_SHA256 = "HmacSHA256";

  /** 请求体摘要算法名称。 */
  private static final String SHA_256 = "SHA-256";

  /** HMAC-SHA256 hex 签名格式，固定 64 位。 */
  private static final Pattern SIGNATURE_HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}$");

  /**
   * 校验请求签名是否匹配当前请求内容和密钥。
   *
   * @param method HTTP 请求方法，校验时会规范化为大写，不允许为空白或包含 CR/LF
   * @param path 请求路径，不包含域名，不允许为空白或包含 CR/LF
   * @param timestamp 客户端请求时间戳，不允许为空白或包含 CR/LF
   * @param nonce 客户端生成的一次性随机串，不允许为空白或包含 CR/LF
   * @param body 请求体，传入 {@code null} 时按空字符串参与摘要
   * @param secret 应用密钥，不允许为空白
   * @param signature 客户端提交的 64 位 HMAC-SHA256 hex 签名，大小写均可
   * @return 签名匹配返回 {@code true}，缺少必要参数或签名不匹配返回 {@code false}
   */
  public boolean verify(String method, String path, String timestamp, String nonce, String body,
      String secret, String signature) {
    if (isBlank(method) || isBlank(path) || isBlank(timestamp) || isBlank(nonce)
        || isBlank(secret) || isBlank(signature)) {
      return false;
    }

    String normalizedMethod = method.toUpperCase(Locale.ROOT);
    if (containsCrOrLf(normalizedMethod) || containsCrOrLf(path)
        || containsCrOrLf(timestamp) || containsCrOrLf(nonce)) {
      return false;
    }

    if (!SIGNATURE_HEX_PATTERN.matcher(signature).matches()) {
      return false;
    }

    String expectedSignature = sign(normalizedMethod, path, timestamp, nonce, body, secret);
    String normalizedSignature = signature.toLowerCase(Locale.ROOT);
    return constantTimeEquals(expectedSignature, normalizedSignature);
  }

  /**
   * 根据请求要素生成 HMAC-SHA256 hex 签名。
   *
   * <p>该方法供网关内部和测试构造合法签名使用，不负责读取数据库中的密钥。</p>
   *
   * @param method HTTP 请求方法，签名前会规范化为大写
   * @param path 请求路径，不包含域名
   * @param timestamp 客户端请求时间戳
   * @param nonce 客户端生成的一次性随机串
   * @param body 请求体，传入 {@code null} 时按空字符串参与摘要
   * @param secret 应用密钥
   * @return 小写 hex 格式的 HMAC-SHA256 签名
   */
  public String sign(String method, String path, String timestamp, String nonce, String body,
      String secret) {
    String canonicalString = canonicalize(method, path, timestamp, nonce, body);
    return hmacSha256Hex(secret, canonicalString);
  }

  /**
   * 校验客户端提交的请求体 SHA-256 摘要。
   *
   * @param body 请求体，传入 {@code null} 时按空字符串计算
   * @param bodySha256 客户端提交的 64 位 SHA-256 hex 摘要
   * @return 摘要匹配返回 {@code true}
   */
  public boolean verifyBodySha256(String body, String bodySha256) {
    if (isBlank(bodySha256) || !SIGNATURE_HEX_PATTERN.matcher(bodySha256).matches()) {
      return false;
    }
    String expectedHash = bodySha256Hex(body);
    return constantTimeEquals(expectedHash, bodySha256.toLowerCase(Locale.ROOT));
  }

  /**
   * 计算请求体 SHA-256 摘要。
   *
   * @param body 请求体，传入 {@code null} 时按空字符串计算
   * @return 小写 hex 格式的 SHA-256 摘要
   */
  public String bodySha256Hex(String body) {
    return sha256Hex(body == null ? "" : body);
  }

  /**
   * 构造参与签名的规范字符串。
   *
   * @param method HTTP 请求方法
   * @param path 请求路径
   * @param timestamp 请求时间戳
   * @param nonce 一次性随机串
   * @param body 请求体
   * @return 按约定五行格式拼接的规范字符串
   */
  String canonicalize(String method, String path, String timestamp, String nonce, String body) {
    Objects.requireNonNull(method, "method must not be null");
    Objects.requireNonNull(path, "path must not be null");
    Objects.requireNonNull(timestamp, "timestamp must not be null");
    Objects.requireNonNull(nonce, "nonce must not be null");

    return method.toUpperCase(Locale.ROOT)
        + "\n" + path
        + "\n" + timestamp
        + "\n" + nonce
        + "\n" + sha256Hex(body == null ? "" : body);
  }

  /**
   * 计算字符串的 SHA-256 小写 hex 摘要。
   *
   * @param value 待摘要字符串
   * @return 小写 hex 摘要
   */
  private String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance(SHA_256);
      return toLowerHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 algorithm is not available", exception);
    }
  }

  /**
   * 计算 HMAC-SHA256 小写 hex 签名。
   *
   * @param secret 应用密钥
   * @param value 待签名内容
   * @return 小写 hex 签名
   */
  private String hmacSha256Hex(String secret, String value) {
    Objects.requireNonNull(secret, "secret must not be null");
    try {
      Mac mac = Mac.getInstance(HMAC_SHA256);
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
      return toLowerHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      throw new IllegalStateException("HMAC-SHA256 signature generation failed", exception);
    }
  }

  /**
   * 使用常量时间比较两个签名，降低基于比较耗时推断签名内容的风险。
   *
   * @param expectedSignature 服务端计算的签名
   * @param actualSignature 客户端提交的签名
   * @return 两个签名完全一致返回 {@code true}
   */
  private boolean constantTimeEquals(String expectedSignature, String actualSignature) {
    byte[] expectedBytes = expectedSignature.getBytes(StandardCharsets.UTF_8);
    byte[] actualBytes = actualSignature.getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(expectedBytes, actualBytes);
  }

  /**
   * 将字节数组转换为小写 hex 字符串。
   *
   * @param bytes 待转换字节数组
   * @return 小写 hex 字符串
   */
  private String toLowerHex(byte[] bytes) {
    StringBuilder hex = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) {
      hex.append(Character.forDigit((value >>> 4) & 0x0f, 16));
      hex.append(Character.forDigit(value & 0x0f, 16));
    }
    return hex.toString();
  }

  /**
   * 判断输入是否为 {@code null} 或空白字符串。
   *
   * @param value 待检查字符串
   * @return 为空或空白返回 {@code true}
   */
  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  /**
   * 判断输入是否包含回车或换行字符。
   *
   * @param value 待检查字符串
   * @return 包含 CR 或 LF 返回 {@code true}
   */
  private boolean containsCrOrLf(String value) {
    return value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0;
  }
}
