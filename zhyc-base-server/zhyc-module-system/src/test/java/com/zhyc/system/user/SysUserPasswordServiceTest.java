/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.passwordpolicy.service.PasswordPolicyValidationResult;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicyResponse;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicySaveCommand;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicyService;
import com.zhyc.system.user.domain.SysUser;
import com.zhyc.system.user.repository.SysUserRepository;
import com.zhyc.system.user.service.DefaultSysUserService;
import com.zhyc.system.user.service.SysUserPasswordChangeCommand;
import com.zhyc.system.user.service.SysUserService;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统用户密码修改服务测试。
 */
class SysUserPasswordServiceTest {

    /** 密码服务，用于生成与 Shiro 登录校验一致的密码哈希。 */
    private final PasswordService passwordService = new DefaultPasswordService();

    /**
     * 验证用户可在旧密码正确且新密码满足策略时修改密码。
     */
    @Test
    void shouldChangePasswordWhenOldPasswordAndPolicyAreValid() {
        RecordingUserRepository repository = new RecordingUserRepository(passwordService.encryptPassword("old-secret"));
        SysUserService service = new DefaultSysUserService(repository,
                new RecordingPasswordPolicyService(true), passwordService);

        assertDoesNotThrow(() -> service.changePassword(new SysUserPasswordChangeCommand(
                " tenant_a ", " admin ", "old-secret", "NewSecret1")));

        assertEquals("tenant_a", repository.updatedTenantId);
        assertEquals("admin", repository.updatedUsername);
        assertNotEquals(repository.originalPasswordHash, repository.updatedPasswordHash);
        assertTrue(passwordService.passwordsMatch("NewSecret1", repository.updatedPasswordHash));
    }

    /**
     * 验证旧密码不匹配时拒绝修改，避免越权改密。
     */
    @Test
    void shouldRejectPasswordChangeWhenOldPasswordMismatch() {
        RecordingUserRepository repository = new RecordingUserRepository(passwordService.encryptPassword("old-secret"));
        SysUserService service = new DefaultSysUserService(repository,
                new RecordingPasswordPolicyService(true), passwordService);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.changePassword(new SysUserPasswordChangeCommand(
                        "tenant_a", "admin", "wrong-secret", "NewSecret1")));

        assertEquals("当前密码不正确", exception.getMessage());
        assertEquals(0, repository.updateCount);
    }

    /**
     * 验证新密码不满足租户密码策略时拒绝修改。
     */
    @Test
    void shouldRejectPasswordChangeWhenPolicyRejectsNewPassword() {
        RecordingUserRepository repository = new RecordingUserRepository(passwordService.encryptPassword("old-secret"));
        SysUserService service = new DefaultSysUserService(repository,
                new RecordingPasswordPolicyService(false, List.of("PASSWORD_MIN_LENGTH")), passwordService);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.changePassword(new SysUserPasswordChangeCommand(
                        "tenant_a", "admin", "old-secret", "weak")));

        assertEquals("新密码不符合密码策略：密码长度不能小于策略要求", exception.getMessage());
        assertEquals(0, repository.updateCount);
    }

    /**
     * 验证重置密码不满足租户密码策略时返回中文策略提示。
     */
    @Test
    void shouldRejectPasswordResetWithChinesePolicyMessages() {
        RecordingUserRepository repository = new RecordingUserRepository(passwordService.encryptPassword("old-secret"));
        SysUserService service = new DefaultSysUserService(repository,
                new RecordingPasswordPolicyService(false,
                        List.of("PASSWORD_MIN_LENGTH", "PASSWORD_LOWERCASE_REQUIRED")), passwordService);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.resetPassword("tenant_a", 1001L, "ABC123"));

        assertEquals("密码不符合密码策略：密码长度不能小于策略要求、密码必须包含小写字母", exception.getMessage());
        assertEquals(0, repository.updateCount);
    }

    /**
     * 测试用系统用户仓储。
     */
    private static final class RecordingUserRepository implements SysUserRepository {

        /** 原始密码哈希。 */
        private final String originalPasswordHash;
        /** 最近一次更新的租户业务编码。 */
        private String updatedTenantId;
        /** 最近一次更新的登录账号。 */
        private String updatedUsername;
        /** 最近一次更新的新密码哈希。 */
        private String updatedPasswordHash;
        /** 密码更新次数。 */
        private int updateCount;

        private RecordingUserRepository(String originalPasswordHash) {
            this.originalPasswordHash = originalPasswordHash;
        }

        @Override
        public List<SysUser> findByTenantId(String tenantId) {
            return List.of(new SysUser(1001L, tenantId, "admin", "管理员", originalPasswordHash,
                    "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public Optional<SysUser> findByTenantIdAndUsername(String tenantId, String username) {
            return Optional.of(new SysUser(1001L, tenantId, username, "管理员", originalPasswordHash,
                    "enabled", LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void insert(String tenantId, String username, String nickname, String passwordHash, String status) {
            throw new AssertionError("密码修改测试不应新增用户");
        }

        @Override
        public void update(String tenantId, Long userId, String username, String nickname, String status) {
            throw new AssertionError("密码修改测试不应更新用户信息");
        }

        @Override
        public void updateStatus(String tenantId, Long userId, String status) {
            throw new AssertionError("密码修改测试不应更新用户状态");
        }

        @Override
        public void updatePasswordHash(String tenantId, String username, String passwordHash) {
            updatedTenantId = tenantId;
            updatedUsername = username;
            updatedPasswordHash = passwordHash;
            updateCount++;
        }

        @Override
        public void updatePasswordHashById(String tenantId, Long userId, String passwordHash) {
            throw new AssertionError("密码修改测试不应按用户主键重置密码");
        }

        @Override
        public void deleteByTenantIdAndId(String tenantId, Long userId) {
            throw new AssertionError("密码修改测试不应删除用户");
        }
    }

    /**
     * 测试用密码策略服务。
     */
    private static final class RecordingPasswordPolicyService implements SysPasswordPolicyService {

        /** 新密码是否通过策略校验。 */
        private final boolean valid;
        /** 密码策略违规编码列表。 */
        private final List<String> violationCodes;

        private RecordingPasswordPolicyService(boolean valid) {
            this(valid, List.of("PASSWORD_MIN_LENGTH"));
        }

        private RecordingPasswordPolicyService(boolean valid, List<String> violationCodes) {
            this.valid = valid;
            this.violationCodes = violationCodes;
        }

        @Override
        public SysPasswordPolicyResponse getPolicy(String tenantId) {
            throw new AssertionError("密码修改不应查询策略详情");
        }

        @Override
        public void save(SysPasswordPolicySaveCommand command) {
            throw new AssertionError("密码修改不应保存密码策略");
        }

        @Override
        public PasswordPolicyValidationResult validatePassword(String tenantId, String password) {
            return valid
                    ? new PasswordPolicyValidationResult(true, List.of())
                    : new PasswordPolicyValidationResult(false, violationCodes);
        }

        @Override
        public PasswordPolicyValidationResult validatePasswordHistory(String tenantId, String passwordHash,
                                                                      List<String> recentPasswordHashes) {
            return new PasswordPolicyValidationResult(true, List.of());
        }
    }
}
