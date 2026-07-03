/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.service;

import com.zhyc.system.passwordpolicy.domain.SysPasswordPolicy;
import com.zhyc.system.passwordpolicy.repository.SysPasswordPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 默认系统密码策略业务服务实现。
 */
@Service
public class DefaultSysPasswordPolicyService implements SysPasswordPolicyService {

    /** 默认密码策略编码。 */
    private static final String DEFAULT_POLICY_CODE = "default";
    /** 系统密码策略仓储。 */
    private final SysPasswordPolicyRepository passwordPolicyRepository;

    /**
     * 创建默认系统密码策略业务服务。
     *
     * @param passwordPolicyRepository 系统密码策略仓储
     */
    public DefaultSysPasswordPolicyService(SysPasswordPolicyRepository passwordPolicyRepository) {
        this.passwordPolicyRepository = Objects.requireNonNull(passwordPolicyRepository,
                "系统密码策略仓储不能为空");
    }

    @Override
    public SysPasswordPolicyResponse getPolicy(String tenantId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        return toResponse(findOrDefault(requiredTenantId));
    }

    @Override
    @Transactional
    public void save(SysPasswordPolicySaveCommand command) {
        Objects.requireNonNull(command, "系统密码策略保存命令不能为空");
        SysPasswordPolicy policy = new SysPasswordPolicy(null,
                requireText(command.getTenantId(), "租户业务编码不能为空"),
                requireText(command.getPolicyCode(), "密码策略编码不能为空"),
                requireText(command.getPolicyName(), "密码策略名称不能为空"),
                requirePositive(command.getMinLength(), "密码最小长度不能为空"),
                command.isRequireUppercase(), command.isRequireLowercase(), command.isRequireDigit(),
                command.isRequireSpecial(), normalizeNonNegative(command.getExpireDays()),
                normalizeNonNegative(command.getHistoryCount()), normalizeNonNegative(command.getMaxRetryCount()),
                normalizeNonNegative(command.getLockMinutes()), command.isEnabled(), null, null);
        passwordPolicyRepository.save(policy);
    }

    @Override
    public PasswordPolicyValidationResult validatePassword(String tenantId, String password) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        String requiredPassword = Objects.requireNonNull(password, "待校验密码不能为空");
        SysPasswordPolicy policy = findOrDefault(requiredTenantId);
        if (!policy.isEnabled()) {
            return new PasswordPolicyValidationResult(true, List.of());
        }
        List<String> violations = new ArrayList<>();
        if (requiredPassword.length() < requirePositive(policy.getMinLength(), "密码最小长度不能为空")) {
            violations.add("PASSWORD_MIN_LENGTH");
        }
        if (policy.isRequireUppercase() && requiredPassword.chars().noneMatch(Character::isUpperCase)) {
            violations.add("PASSWORD_UPPERCASE_REQUIRED");
        }
        if (policy.isRequireLowercase() && requiredPassword.chars().noneMatch(Character::isLowerCase)) {
            violations.add("PASSWORD_LOWERCASE_REQUIRED");
        }
        if (policy.isRequireDigit() && requiredPassword.chars().noneMatch(Character::isDigit)) {
            violations.add("PASSWORD_DIGIT_REQUIRED");
        }
        if (policy.isRequireSpecial() && requiredPassword.chars().noneMatch(this::isSpecialCharacter)) {
            violations.add("PASSWORD_SPECIAL_REQUIRED");
        }
        return new PasswordPolicyValidationResult(violations.isEmpty(), violations);
    }

    @Override
    public PasswordPolicyValidationResult validatePasswordHistory(String tenantId, String passwordHash,
                                                                  List<String> recentPasswordHashes) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        String requiredPasswordHash = requireText(passwordHash, "新密码哈希不能为空");
        List<String> requiredRecentHashes = recentPasswordHashes == null ? List.of() : recentPasswordHashes;
        SysPasswordPolicy policy = findOrDefault(requiredTenantId);
        if (!policy.isEnabled() || policy.getHistoryCount() == null || policy.getHistoryCount() <= 0) {
            return new PasswordPolicyValidationResult(true, List.of());
        }
        boolean reused = requiredRecentHashes.stream()
                .limit(policy.getHistoryCount())
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .anyMatch(requiredPasswordHash::equals);
        if (reused) {
            return new PasswordPolicyValidationResult(false, List.of("PASSWORD_HISTORY_REUSED"));
        }
        return new PasswordPolicyValidationResult(true, List.of());
    }

    private SysPasswordPolicy findOrDefault(String tenantId) {
        return passwordPolicyRepository.findDefaultByTenantId(tenantId)
                .orElseGet(() -> defaultPolicy(tenantId));
    }

    private SysPasswordPolicy defaultPolicy(String tenantId) {
        return new SysPasswordPolicy(null, tenantId, DEFAULT_POLICY_CODE, "默认密码策略",
                8, false, true, true, false, 90, 3, 5, 30, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private SysPasswordPolicyResponse toResponse(SysPasswordPolicy policy) {
        return new SysPasswordPolicyResponse(policy.getPolicyCode(), policy.getPolicyName(), policy.getMinLength(),
                policy.isRequireUppercase(), policy.isRequireLowercase(), policy.isRequireDigit(),
                policy.isRequireSpecial(), policy.getExpireDays(), policy.getHistoryCount(),
                policy.getMaxRetryCount(), policy.getLockMinutes(), policy.isEnabled());
    }

    /**
     * 判断字符是否属于密码策略认可的特殊字符。
     *
     * <p>空白字符不计入特殊字符，避免用户通过尾随空格绕过复杂度要求。</p>
     *
     * @param character 待检查字符编码
     * @return 是否为有效特殊字符
     */
    private boolean isSpecialCharacter(int character) {
        return !Character.isLetterOrDigit(character) && !Character.isWhitespace(character);
    }

    private Integer normalizeNonNegative(Integer value) {
        return value == null || value < 0 ? 0 : value;
    }

    private Integer requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value;
    }

    private String requireText(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
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
