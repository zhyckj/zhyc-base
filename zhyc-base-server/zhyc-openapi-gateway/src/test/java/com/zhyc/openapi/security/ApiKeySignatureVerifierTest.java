/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * API Key 签名校验器测试，覆盖规范化、空请求体和错误凭证等边界。
 */
class ApiKeySignatureVerifierTest {

  private final ApiKeySignatureVerifier verifier = new ApiKeySignatureVerifier();

  @Test
  void verifyReturnsTrueWhenSignatureMatchesCanonicalRequest() {
    String signature = verifier.sign("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret");

    assertTrue(verifier.verify("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret", signature));
  }

  @Test
  void verifyReturnsFalseWhenBodyChanges() {
    String signature = verifier.sign("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret");

    assertFalse(verifier.verify("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":200}", "test-secret", signature));
  }

  @Test
  void verifyNormalizesHttpMethodToUppercase() {
    String signature = verifier.sign("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret");

    assertTrue(verifier.verify("post", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret", signature));
  }

  @Test
  void verifyTreatsNullBodyAsEmptyString() {
    String signature = verifier.sign("GET", "/openapi/orders/1", "1719200000", "nonce-001",
        null, "test-secret");

    assertTrue(verifier.verify("GET", "/openapi/orders/1", "1719200000", "nonce-001",
        "", "test-secret", signature));
  }

  @Test
  void verifyReturnsFalseWhenSecretIsWrong() {
    String signature = verifier.sign("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret");

    assertFalse(verifier.verify("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "wrong-secret", signature));
  }

  @Test
  void verifyAcceptsUppercaseHexSignature() {
    String signature = verifier.sign("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret");

    assertTrue(verifier.verify("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret", signature.toUpperCase()));
  }

  @Test
  void verifyReturnsFalseWhenRequiredParameterIsBlank() {
    String signature = verifier.sign("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret");

    assertFalse(verifier.verify(" ", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret", signature));
    assertFalse(verifier.verify("POST", " ", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret", signature));
    assertFalse(verifier.verify("POST", "/openapi/orders", " ", "nonce-001",
        "{\"amount\":100}", "test-secret", signature));
    assertFalse(verifier.verify("POST", "/openapi/orders", "1719200000", " ",
        "{\"amount\":100}", "test-secret", signature));
    assertFalse(verifier.verify("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", " ", signature));
    assertFalse(verifier.verify("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret", " "));
  }

  @Test
  void verifyReturnsFalseWhenSignatureIsNotSixtyFourHexCharacters() {
    assertFalse(verifier.verify("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret", "abc123"));
    assertFalse(verifier.verify("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret",
        "gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg"));
  }

  @Test
  void verifyReturnsFalseWhenCanonicalInputsContainCrOrLf() {
    String signature = verifier.sign("POST", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret");

    assertFalse(verifier.verify("POST\n", "/openapi/orders", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret", signature));
    assertFalse(verifier.verify("POST", "/openapi/orders\r\nx-extra:1", "1719200000", "nonce-001",
        "{\"amount\":100}", "test-secret", signature));
    assertFalse(verifier.verify("POST", "/openapi/orders", "1719200000\nx-extra:1", "nonce-001",
        "{\"amount\":100}", "test-secret", signature));
    assertFalse(verifier.verify("POST", "/openapi/orders", "1719200000", "nonce-001\r\nx-extra:1",
        "{\"amount\":100}", "test-secret", signature));
  }
}
