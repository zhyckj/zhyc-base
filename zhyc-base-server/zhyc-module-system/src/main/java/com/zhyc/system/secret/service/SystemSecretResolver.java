/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.secret.SecretResolver;
import com.zhyc.common.tenant.TenantContext;
import com.zhyc.system.secret.domain.SysSecret;
import com.zhyc.system.secret.repository.SysSecretRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 基于系统密钥中心的密钥解析器。
 *
 * <p>运行期按当前线程租户上下文读取启用密钥并解密，供低代码数据源、开放平台等模块通过 common 契约复用。</p>
 */
@Component
public class SystemSecretResolver implements SecretResolver {

    /** 租户上下文缺失错误码。 */
    private static final String ERROR_TENANT_CONTEXT_REQUIRED = "ZHYC_SYSTEM_SECRET_TENANT_CONTEXT_REQUIRED";
    /** 密钥编码缺失错误码。 */
    private static final String ERROR_SECRET_CODE_REQUIRED = "ZHYC_SYSTEM_SECRET_CODE_REQUIRED";
    /** 密钥不存在错误码。 */
    private static final String ERROR_SECRET_NOT_FOUND = "ZHYC_SYSTEM_SECRET_NOT_FOUND";
    /** 密钥状态不可用错误码。 */
    private static final String ERROR_SECRET_DISABLED = "ZHYC_SYSTEM_SECRET_DISABLED";
    /** 密钥已过期错误码。 */
    private static final String ERROR_SECRET_EXPIRED = "ZHYC_SYSTEM_SECRET_EXPIRED";

    /** 系统密钥仓储。 */
    private final SysSecretRepository secretRepository;
    /** 系统密钥加解密服务。 */
    private final SystemSecretCipherService cipherService;

    /**
     * 创建系统密钥解析器。
     *
     * @param secretRepository 系统密钥仓储
     * @param cipherService 系统密钥加解密服务
     */
    public SystemSecretResolver(SysSecretRepository secretRepository, SystemSecretCipherService cipherService) {
        this.secretRepository = Objects.requireNonNull(secretRepository, "系统密钥仓储不能为空");
        this.cipherService = Objects.requireNonNull(cipherService, "系统密钥加解密服务不能为空");
    }

    @Override
    public String resolve(String code) {
        String tenantId = requireTenantContext();
        String secretCode = requireSecretCode(code);
        SysSecret secret = secretRepository.findByTenantIdAndSecretCode(tenantId, secretCode)
                .orElseThrow(() -> new BusinessException(ERROR_SECRET_NOT_FOUND, "密钥不存在或不属于当前租户: " + secretCode));
        if (!"enabled".equals(secret.getStatus())) {
            throw new BusinessException(ERROR_SECRET_DISABLED, "密钥未启用，不能解析: " + secretCode);
        }
        LocalDateTime expireAt = secret.getExpireAt();
        if (expireAt != null && expireAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ERROR_SECRET_EXPIRED, "密钥已过期，不能解析: " + secretCode);
        }
        return cipherService.decrypt(secret.getSecretCipher());
    }

    private String requireTenantContext() {
        String tenantId = trimToNull(TenantContext.getTenantId());
        if (tenantId == null) {
            throw new BusinessException(ERROR_TENANT_CONTEXT_REQUIRED, "解析密钥前必须绑定租户上下文");
        }
        return tenantId;
    }

    private String requireSecretCode(String code) {
        String normalized = trimToNull(code);
        if (normalized == null) {
            throw new BusinessException(ERROR_SECRET_CODE_REQUIRED, "密钥编码不能为空");
        }
        if (normalized.chars().anyMatch(Character::isWhitespace)) {
            throw new BusinessException(ERROR_SECRET_CODE_REQUIRED, "密钥编码不能包含空白字符");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
