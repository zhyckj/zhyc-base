/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.secret.domain.SysSecret;
import com.zhyc.system.secret.repository.SysSecretRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 默认系统密钥业务服务实现。
 */
@Service
public class DefaultSysSecretService implements SysSecretService {

    /** 保存命令缺失错误码。 */
    private static final String ERROR_SAVE_COMMAND_REQUIRED = "ZHYC_SYSTEM_SECRET_SAVE_COMMAND_REQUIRED";
    /** 租户业务编码缺失错误码。 */
    private static final String ERROR_TENANT_ID_REQUIRED = "ZHYC_SYSTEM_SECRET_TENANT_ID_REQUIRED";
    /** 密钥主键缺失错误码。 */
    private static final String ERROR_SECRET_ID_REQUIRED = "ZHYC_SYSTEM_SECRET_ID_REQUIRED";
    /** 密钥编码缺失错误码。 */
    private static final String ERROR_SECRET_CODE_REQUIRED = "ZHYC_SYSTEM_SECRET_CODE_REQUIRED";
    /** 密钥名称缺失错误码。 */
    private static final String ERROR_SECRET_NAME_REQUIRED = "ZHYC_SYSTEM_SECRET_NAME_REQUIRED";
    /** 密钥类型缺失错误码。 */
    private static final String ERROR_SECRET_KIND_REQUIRED = "ZHYC_SYSTEM_SECRET_KIND_REQUIRED";
    /** 密钥状态缺失错误码。 */
    private static final String ERROR_SECRET_STATUS_REQUIRED = "ZHYC_SYSTEM_SECRET_STATUS_REQUIRED";
    /** 密钥状态不支持错误码。 */
    private static final String ERROR_SECRET_STATUS_UNSUPPORTED = "ZHYC_SYSTEM_SECRET_STATUS_UNSUPPORTED";
    /** 密钥明文缺失错误码。 */
    private static final String ERROR_SECRET_PLAINTEXT_REQUIRED = "ZHYC_SYSTEM_SECRET_PLAINTEXT_REQUIRED";
    /** 密钥编码重复错误码。 */
    private static final String ERROR_SECRET_CODE_DUPLICATE = "ZHYC_SYSTEM_SECRET_CODE_DUPLICATE";
    /** 密钥不存在错误码。 */
    private static final String ERROR_SECRET_NOT_FOUND = "ZHYC_SYSTEM_SECRET_NOT_FOUND";

    /** 系统密钥仓储。 */
    private final SysSecretRepository secretRepository;
    /** 系统密钥加解密服务。 */
    private final SystemSecretCipherService cipherService;

    /**
     * 创建默认系统密钥业务服务。
     *
     * @param secretRepository 系统密钥仓储
     */
    public DefaultSysSecretService(SysSecretRepository secretRepository) {
        this(secretRepository, new SystemSecretCipherService());
    }

    /**
     * 创建默认系统密钥业务服务。
     *
     * @param secretRepository 系统密钥仓储
     * @param cipherService 系统密钥加解密服务
     */
    @Autowired
    public DefaultSysSecretService(SysSecretRepository secretRepository, SystemSecretCipherService cipherService) {
        this.secretRepository = Objects.requireNonNull(secretRepository, "系统密钥仓储不能为空");
        this.cipherService = Objects.requireNonNull(cipherService, "系统密钥加解密服务不能为空");
    }

    @Override
    public List<SysSecretResponse> listSecrets(String tenantId) {
        String requiredTenantId = requireTenantId(tenantId);
        return secretRepository.findByTenantId(requiredTenantId).stream()
                .map(SysSecretResponse::from)
                .toList();
    }

    @Override
    public SysSecretResponse getSecret(String tenantId, Long secretId) {
        SysSecret secret = requireSecret(requireTenantId(tenantId), secretId);
        return SysSecretResponse.from(secret);
    }

    @Override
    public List<SysSecretResponse> listOptions(String tenantId, String secretKind, String status) {
        String requiredTenantId = requireTenantId(tenantId);
        String normalizedSecretKind = trimToNull(secretKind);
        String normalizedStatus = Optional.ofNullable(trimToNull(status)).orElse("enabled");
        return secretRepository.findSelectableSecrets(requiredTenantId, normalizedSecretKind, normalizedStatus).stream()
                .map(SysSecretResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void saveSecret(SysSecretSaveCommand command) {
        SysSecretSaveCommand requiredCommand = requireSaveCommand(command);
        String tenantId = requireTenantId(requiredCommand.getTenantId());
        String secretCode = requireSecretCode(requiredCommand.getSecretCode());
        String secretName = requireSecretName(requiredCommand.getSecretName());
        String secretKind = requireSecretKind(requiredCommand.getSecretKind());
        String status = requireStatus(requiredCommand.getStatus());
        LocalDateTime expireAt = requiredCommand.getExpireAt();
        Optional<SysSecret> existingByCode = secretRepository.findByTenantIdAndSecretCode(tenantId, secretCode);
        if (requiredCommand.getId() == null) {
            if (existingByCode.isPresent()) {
                throw businessFailure(ERROR_SECRET_CODE_DUPLICATE, "当前租户的密钥编码已存在: " + secretCode);
            }
            secretRepository.insert(buildSecret(null, tenantId, secretCode, secretName, secretKind,
                    cipherService.encrypt(requirePlaintext(requiredCommand.getSecretPlaintext())),
                    maskSecret(requirePlaintext(requiredCommand.getSecretPlaintext())), status, expireAt, null, null,
                    null));
            return;
        }
        SysSecret existing = requireSecret(tenantId, requiredCommand.getId());
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(requiredCommand.getId())) {
            throw businessFailure(ERROR_SECRET_CODE_DUPLICATE, "当前租户的密钥编码已存在: " + secretCode);
        }
        String secretPlaintext = trimToNull(requiredCommand.getSecretPlaintext());
        String secretCipher = secretPlaintext == null ? existing.getSecretCipher() : cipherService.encrypt(secretPlaintext);
        String secretMask = secretPlaintext == null ? normalizeMask(existing.getSecretMask(), existing.getSecretCode())
                : maskSecret(secretPlaintext);
        LocalDateTime lastRotatedAt = secretPlaintext == null ? existing.getLastRotatedAt() : LocalDateTime.now();
        SysSecret updated = buildSecret(existing.getId(), tenantId, secretCode, secretName, secretKind, secretCipher,
                secretMask, status, expireAt, lastRotatedAt, existing.getCreatedAt(), LocalDateTime.now());
        secretRepository.update(updated);
    }

    @Override
    @Transactional
    public void updateStatus(String tenantId, Long secretId, String status) {
        String requiredTenantId = requireTenantId(tenantId);
        Long requiredSecretId = requireSecretId(secretId);
        SysSecret secret = requireSecret(requiredTenantId, requiredSecretId);
        secret.setStatus(requireStatus(status));
        secretRepository.update(secret);
    }

    @Override
    @Transactional
    public void rotateSecret(String tenantId, Long secretId, String secretPlaintext, LocalDateTime expireAt) {
        String requiredTenantId = requireTenantId(tenantId);
        Long requiredSecretId = requireSecretId(secretId);
        SysSecret secret = requireSecret(requiredTenantId, requiredSecretId);
        String plaintext = requirePlaintext(secretPlaintext);
        secret.setSecretCipher(cipherService.encrypt(plaintext));
        secret.setSecretMask(maskSecret(plaintext));
        secret.setExpireAt(expireAt);
        secret.setLastRotatedAt(LocalDateTime.now());
        secretRepository.update(secret);
    }

    @Override
    @Transactional
    public void deleteSecret(String tenantId, Long secretId) {
        String requiredTenantId = requireTenantId(tenantId);
        Long requiredSecretId = requireSecretId(secretId);
        requireSecret(requiredTenantId, requiredSecretId);
        secretRepository.deleteByTenantIdAndId(requiredTenantId, requiredSecretId);
    }

    private SysSecret requireSecret(String tenantId, Long secretId) {
        return secretRepository.findByTenantIdAndId(tenantId, requireSecretId(secretId))
                .orElseThrow(() -> businessFailure(ERROR_SECRET_NOT_FOUND, "密钥不存在或不属于当前租户: " + secretId));
    }

    private SysSecretSaveCommand requireSaveCommand(SysSecretSaveCommand command) {
        if (command == null) {
            throw businessFailure(ERROR_SAVE_COMMAND_REQUIRED, "密钥保存命令不能为空");
        }
        return command;
    }

    private String requireTenantId(String tenantId) {
        String normalized = trimToNull(tenantId);
        if (normalized == null) {
            throw businessFailure(ERROR_TENANT_ID_REQUIRED, "租户业务编码不能为空");
        }
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_TENANT_ID_REQUIRED, "租户业务编码不能包含空白字符");
        }
        return normalized;
    }

    private Long requireSecretId(Long secretId) {
        if (secretId == null || secretId <= 0) {
            throw businessFailure(ERROR_SECRET_ID_REQUIRED, "密钥主键不能为空");
        }
        return secretId;
    }

    private String requireSecretCode(String secretCode) {
        String normalized = trimToNull(secretCode);
        if (normalized == null) {
            throw businessFailure(ERROR_SECRET_CODE_REQUIRED, "密钥编码不能为空");
        }
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw businessFailure(ERROR_SECRET_CODE_REQUIRED, "密钥编码不能包含空白字符");
        }
        return normalized;
    }

    private String requireSecretName(String secretName) {
        String normalized = trimToNull(secretName);
        if (normalized == null) {
            throw businessFailure(ERROR_SECRET_NAME_REQUIRED, "密钥名称不能为空");
        }
        return normalized;
    }

    private String requireSecretKind(String secretKind) {
        String normalized = trimToNull(secretKind);
        if (normalized == null) {
            throw businessFailure(ERROR_SECRET_KIND_REQUIRED, "密钥类型不能为空");
        }
        return normalized;
    }

    private String requireStatus(String status) {
        String normalized = trimToNull(status);
        if (normalized == null) {
            throw businessFailure(ERROR_SECRET_STATUS_REQUIRED, "密钥状态不能为空");
        }
        if (!"enabled".equals(normalized) && !"disabled".equals(normalized)) {
            throw businessFailure(ERROR_SECRET_STATUS_UNSUPPORTED, "密钥状态仅支持 enabled 或 disabled");
        }
        return normalized;
    }

    private String requirePlaintext(String secretPlaintext) {
        String normalized = trimToNull(secretPlaintext);
        if (normalized == null) {
            throw businessFailure(ERROR_SECRET_PLAINTEXT_REQUIRED, "密钥明文不能为空");
        }
        return normalized;
    }

    private SysSecret buildSecret(Long id, String tenantId, String secretCode, String secretName, String secretKind,
                                  String secretCipher, String secretMask, String status, LocalDateTime expireAt,
                                  LocalDateTime lastRotatedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        SysSecret secret = new SysSecret();
        secret.setId(id);
        secret.setTenantId(tenantId);
        secret.setSecretCode(secretCode);
        secret.setSecretName(secretName);
        secret.setSecretKind(secretKind);
        secret.setSecretCipher(secretCipher);
        secret.setSecretMask(secretMask);
        secret.setStatus(status);
        secret.setExpireAt(expireAt);
        secret.setLastRotatedAt(lastRotatedAt);
        secret.setCreatedAt(createdAt);
        secret.setUpdatedAt(updatedAt);
        return secret;
    }

    private String maskSecret(String secretPlaintext) {
        String normalized = trimToNull(secretPlaintext);
        if (normalized == null || normalized.length() <= 4) {
            return "****";
        }
        return normalized.substring(0, 2) + "****" + normalized.substring(normalized.length() - 2);
    }

    private String normalizeMask(String storedMask, String secretCode) {
        String normalizedMask = trimToNull(storedMask);
        if (normalizedMask != null) {
            return normalizedMask;
        }
        String normalizedCode = trimToNull(secretCode);
        return normalizedCode == null ? "****" : maskSecret(normalizedCode);
    }

    private BusinessException businessFailure(String code, String message) {
        return new BusinessException(code, message);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
