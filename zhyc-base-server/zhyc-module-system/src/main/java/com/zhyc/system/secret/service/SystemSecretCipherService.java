/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.service;

import com.zhyc.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 系统密钥加解密服务。
 *
 * <p>统一承载密钥中心的密文生成和运行期解析逻辑，避免低代码数据源连接和密钥管理写入使用两套算法。</p>
 */
@Component
public class SystemSecretCipherService {

    /** 加密密钥种子。 */
    private static final String SECRET_KEY_SEED = "zhyc-system-secret-center-v1";
    /** 密文版本前缀。 */
    private static final String CIPHER_PREFIX = "aesgcm:v1:";
    /** AES-GCM IV 长度。 */
    private static final int GCM_IV_LENGTH = 12;
    /** AES-GCM 鉴权长度。 */
    private static final int GCM_TAG_LENGTH = 128;

    /** 随机 IV 生成器。 */
    private final SecureRandom secureRandom = new SecureRandom();
    /** 对称加密密钥。 */
    private final SecretKeySpec secretKeySpec = new SecretKeySpec(deriveSecretKeyBytes(), "AES");

    /**
     * 加密密钥明文。
     *
     * @param secretPlaintext 密钥明文
     * @return 带版本前缀的密文
     */
    public String encrypt(String secretPlaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(secretPlaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return CIPHER_PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception exception) {
            throw new BusinessException("ZHYC_SYSTEM_SECRET_ENCRYPT_FAILED", "密钥加密失败");
        }
    }

    /**
     * 解密密钥密文。
     *
     * @param secretCipher 带版本前缀的密文
     * @return 密钥明文
     */
    public String decrypt(String secretCipher) {
        if (secretCipher == null || secretCipher.isBlank()) {
            throw new BusinessException("ZHYC_SYSTEM_SECRET_CIPHER_REQUIRED", "密钥密文不能为空");
        }
        if (!secretCipher.startsWith(CIPHER_PREFIX)) {
            throw new BusinessException("ZHYC_SYSTEM_SECRET_CIPHER_UNSUPPORTED", "密钥密文版本不支持");
        }
        try {
            byte[] payload = Base64.getDecoder().decode(secretCipher.substring(CIPHER_PREFIX.length()));
            if (payload.length <= GCM_IV_LENGTH) {
                throw new BusinessException("ZHYC_SYSTEM_SECRET_CIPHER_PAYLOAD_INVALID", "密钥密文载荷非法");
            }
            byte[] iv = Arrays.copyOfRange(payload, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(payload, GCM_IV_LENGTH, payload.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("ZHYC_SYSTEM_SECRET_DECRYPT_FAILED", "密钥解密失败");
        }
    }

    private byte[] deriveSecretKeyBytes() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] digestBytes = digest.digest(SECRET_KEY_SEED.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(digestBytes, 16);
        } catch (Exception exception) {
            throw new IllegalStateException("密钥派生失败", exception);
        }
    }
}
