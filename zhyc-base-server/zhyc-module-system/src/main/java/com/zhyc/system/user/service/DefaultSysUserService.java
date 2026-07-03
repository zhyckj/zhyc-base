/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

import com.zhyc.system.passwordpolicy.service.PasswordPolicyValidationResult;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicyService;
import com.zhyc.system.user.domain.SysUser;
import com.zhyc.system.user.repository.SysUserRepository;
import org.apache.shiro.authc.credential.PasswordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 默认系统用户业务服务实现。
 */
@Service
public class DefaultSysUserService implements SysUserService {

    /** 系统用户仓储。 */
    private final SysUserRepository userRepository;
    /** 系统密码策略服务。 */
    private final SysPasswordPolicyService passwordPolicyService;
    /** Shiro 密码服务，用于保持登录校验和改密哈希算法一致。 */
    private final PasswordService passwordService;

    /**
     * 创建默认系统用户业务服务。
     *
     * @param userRepository 系统用户仓储
     * @param passwordPolicyService 系统密码策略服务
     * @param passwordService Shiro 密码服务
     */
    public DefaultSysUserService(SysUserRepository userRepository, SysPasswordPolicyService passwordPolicyService,
                                 PasswordService passwordService) {
        this.userRepository = Objects.requireNonNull(userRepository, "系统用户仓储不能为空");
        this.passwordPolicyService = Objects.requireNonNull(passwordPolicyService, "系统密码策略服务不能为空");
        this.passwordService = Objects.requireNonNull(passwordService, "Shiro 密码服务不能为空");
    }

    @Override
    public List<SysUserResponse> listUsers(String tenantId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        return userRepository.findByTenantId(requiredTenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 新增或编辑系统用户。
     *
     * <p>新增用户必须提供密码并满足租户密码策略；编辑用户不允许跨租户修改，密码为空时保持原密码。</p>
     *
     * @param command 用户保存命令
     */
    @Override
    @Transactional
    public void saveUser(SysUserSaveCommand command) {
        Objects.requireNonNull(command, "用户保存命令不能为空");
        String tenantId = requireText(command.getTenantId(), "租户业务编码不能为空");
        String username = requireText(command.getUsername(), "登录账号不能为空");
        String nickname = requireText(command.getNickname(), "用户名称不能为空");
        String status = normalizeStatus(command.getStatus());
        if (command.getId() == null) {
            String passwordHash = encryptValidatedPassword(tenantId, requireText(command.getPassword(), "初始密码不能为空"));
            userRepository.insert(tenantId, username, nickname, passwordHash, status);
            return;
        }
        validateTenantUser(tenantId, command.getId());
        userRepository.update(tenantId, command.getId(), username, nickname, status);
        if (command.getPassword() != null && !command.getPassword().trim().isEmpty()) {
            resetPassword(tenantId, command.getId(), command.getPassword());
        }
    }

    @Override
    @Transactional
    public void updateStatus(String tenantId, Long userId, String status) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredUserId = requirePositive(userId, "用户主键不能为空");
        validateTenantUser(requiredTenantId, requiredUserId);
        userRepository.updateStatus(requiredTenantId, requiredUserId, normalizeStatus(status));
    }

    @Override
    @Transactional
    public void resetPassword(String tenantId, Long userId, String password) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredUserId = requirePositive(userId, "用户主键不能为空");
        validateTenantUser(requiredTenantId, requiredUserId);
        userRepository.updatePasswordHashById(requiredTenantId, requiredUserId,
                encryptValidatedPassword(requiredTenantId, requireText(password, "新密码不能为空")));
    }

    @Override
    @Transactional
    public void deleteUser(String tenantId, Long userId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredUserId = requirePositive(userId, "用户主键不能为空");
        validateTenantUser(requiredTenantId, requiredUserId);
        userRepository.deleteByTenantIdAndId(requiredTenantId, requiredUserId);
    }

    /**
     * 修改系统用户密码。
     *
     * <p>按租户和账号定位用户，先校验当前密码，再执行租户密码策略校验，最后只持久化新密码哈希。</p>
     *
     * @param command 修改密码命令
     */
    @Override
    @Transactional
    public void changePassword(SysUserPasswordChangeCommand command) {
        Objects.requireNonNull(command, "修改密码命令不能为空");
        String tenantId = requireText(command.getTenantId(), "租户业务编码不能为空");
        String username = requireText(command.getUsername(), "登录账号不能为空");
        String oldPassword = requireText(command.getOldPassword(), "当前密码不能为空");
        String newPassword = requireText(command.getNewPassword(), "新密码不能为空");
        SysUser user = userRepository.findByTenantIdAndUsername(tenantId, username)
                .orElseThrow(() -> com.zhyc.system.support.SystemServiceValidation.businessFailure("用户不存在"));
        if (!passwordService.passwordsMatch(oldPassword, user.getPasswordHash())) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("当前密码不正确");
        }
        PasswordPolicyValidationResult validationResult = passwordPolicyService.validatePassword(tenantId, newPassword);
        if (!validationResult.isValid()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(
                    "新密码不符合密码策略：" + formatPasswordPolicyViolations(validationResult.getViolationCodes()));
        }
        String newPasswordHash = passwordService.encryptPassword(newPassword);
        userRepository.updatePasswordHash(tenantId, username, newPasswordHash);
    }

    /**
     * 校验用户属于指定租户。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     */
    private void validateTenantUser(String tenantId, Long userId) {
        boolean exists = userRepository.findByTenantId(tenantId).stream()
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .anyMatch(userId::equals);
        if (!exists) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("用户主键不属于当前租户：" + userId);
        }
    }

    /**
     * 校验密码策略并生成 Shiro 密码哈希。
     *
     * @param tenantId 租户业务编码
     * @param password 明文密码
     * @return Shiro 密码哈希
     */
    private String encryptValidatedPassword(String tenantId, String password) {
        PasswordPolicyValidationResult validationResult = passwordPolicyService.validatePassword(tenantId, password);
        if (!validationResult.isValid()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(
                    "密码不符合密码策略：" + formatPasswordPolicyViolations(validationResult.getViolationCodes()));
        }
        return passwordService.encryptPassword(password);
    }

    /**
     * 格式化密码策略违规编码为中文提示。
     *
     * @param violationCodes 密码策略违规编码列表
     * @return 面向用户的中文违规说明
     */
    private String formatPasswordPolicyViolations(List<String> violationCodes) {
        if (violationCodes == null || violationCodes.isEmpty()) {
            return "未满足当前租户密码策略";
        }
        return violationCodes.stream()
                .map(this::translatePasswordPolicyViolation)
                .toList()
                .stream()
                .reduce((left, right) -> left + "、" + right)
                .orElse("未满足当前租户密码策略");
    }

    /**
     * 翻译单个密码策略违规编码。
     *
     * @param violationCode 密码策略违规编码
     * @return 中文提示
     */
    private String translatePasswordPolicyViolation(String violationCode) {
        return switch (violationCode) {
            case "PASSWORD_MIN_LENGTH" -> "密码长度不能小于策略要求";
            case "PASSWORD_UPPERCASE_REQUIRED" -> "密码必须包含大写字母";
            case "PASSWORD_LOWERCASE_REQUIRED" -> "密码必须包含小写字母";
            case "PASSWORD_DIGIT_REQUIRED" -> "密码必须包含数字";
            case "PASSWORD_SPECIAL_REQUIRED" -> "密码必须包含特殊字符";
            case "PASSWORD_HISTORY_REUSED" -> "不能使用最近已用过的密码";
            default -> "未满足密码策略要求";
        };
    }

    /**
     * 标准化用户状态。
     *
     * @param status 原始状态
     * @return 标准状态
     */
    private String normalizeStatus(String status) {
        String requiredStatus = requireText(status, "用户状态不能为空");
        if (!"enabled".equals(requiredStatus) && !"disabled".equals(requiredStatus)) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("用户状态只支持 enabled 或 disabled");
        }
        return requiredStatus;
    }

    private SysUserResponse toResponse(SysUser user) {
        return new SysUserResponse(user.getId(), user.getTenantId(), user.getUsername(), user.getNickname(),
                user.getStatus());
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value.trim();
    }

    private Long requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value;
    }
}
