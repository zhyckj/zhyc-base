/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy;

import com.zhyc.system.passwordpolicy.domain.SysPasswordPolicy;
import com.zhyc.system.passwordpolicy.repository.SysPasswordPolicyRepository;
import com.zhyc.system.passwordpolicy.service.DefaultSysPasswordPolicyService;
import com.zhyc.system.passwordpolicy.service.PasswordPolicyValidationResult;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicyResponse;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicySaveCommand;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicyService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 系统密码策略业务服务测试。
 */
class SysPasswordPolicyServiceTest {

    /**
     * 验证密码策略服务按租户查询默认策略。
     */
    @Test
    void shouldGetPasswordPolicyByTenant() {
        RecordingPasswordPolicyRepository repository = new RecordingPasswordPolicyRepository();
        SysPasswordPolicyService service = new DefaultSysPasswordPolicyService(repository);

        SysPasswordPolicyResponse policy = service.getPolicy(" tenant_a ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("default", policy.getPolicyCode());
        assertEquals(12, policy.getMinLength());
    }

    /**
     * 验证保存密码策略时会裁剪租户、策略编码和策略名称。
     */
    @Test
    void shouldSavePasswordPolicyWithNormalizedFields() {
        RecordingPasswordPolicyRepository repository = new RecordingPasswordPolicyRepository();
        SysPasswordPolicyService service = new DefaultSysPasswordPolicyService(repository);

        service.save(new SysPasswordPolicySaveCommand(" tenant_a ", " default ", " 默认密码策略 ",
                12, true, true, true, true, 90, 5, 5, 30, true));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("default", repository.lastSaved.getPolicyCode());
        assertEquals("默认密码策略", repository.lastSaved.getPolicyName());
        assertEquals(12, repository.lastSaved.getMinLength());
        assertEquals(90, repository.lastSaved.getExpireDays());
        assertEquals(5, repository.lastSaved.getHistoryCount());
    }

    /**
     * 验证密码强度校验会返回未满足的策略项。
     */
    @Test
    void shouldValidatePasswordAgainstEnabledPolicy() {
        RecordingPasswordPolicyRepository repository = new RecordingPasswordPolicyRepository();
        SysPasswordPolicyService service = new DefaultSysPasswordPolicyService(repository);

        PasswordPolicyValidationResult result = service.validatePassword(" tenant_a ", "abc");

        assertFalse(result.isValid());
        assertEquals(List.of("PASSWORD_MIN_LENGTH", "PASSWORD_UPPERCASE_REQUIRED",
                "PASSWORD_DIGIT_REQUIRED", "PASSWORD_SPECIAL_REQUIRED"), result.getViolationCodes());
    }

    /**
     * 验证密码特殊字符校验不把空白字符当作有效特殊字符。
     */
    @Test
    void shouldNotTreatWhitespaceAsPasswordSpecialCharacter() {
        RecordingPasswordPolicyRepository repository = new RecordingPasswordPolicyRepository();
        SysPasswordPolicyService service = new DefaultSysPasswordPolicyService(repository);

        PasswordPolicyValidationResult result = service.validatePassword("tenant_a", "Abcdefghijk1 ");

        assertFalse(result.isValid());
        assertEquals(List.of("PASSWORD_SPECIAL_REQUIRED"), result.getViolationCodes());
    }

    /**
     * 验证密码历史校验只检查策略声明的最近 N 个密码哈希，命中时阻断复用。
     */
    @Test
    void shouldRejectPasswordHistoryReuseWithinConfiguredHistoryCount() {
        RecordingPasswordPolicyRepository repository = new RecordingPasswordPolicyRepository();
        SysPasswordPolicyService service = new DefaultSysPasswordPolicyService(repository);

        PasswordPolicyValidationResult result = service.validatePasswordHistory(" tenant_a ",
                " hash-new ", List.of("hash-1", "hash-new", "hash-3", "hash-out-of-window"));

        assertFalse(result.isValid());
        assertEquals(List.of("PASSWORD_HISTORY_REUSED"), result.getViolationCodes());
    }

    /**
     * 测试用密码策略仓储。
     */
    private static class RecordingPasswordPolicyRepository implements SysPasswordPolicyRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次保存的密码策略。 */
        private SysPasswordPolicy lastSaved;

        @Override
        public Optional<SysPasswordPolicy> findDefaultByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return Optional.of(policy(tenantId));
        }

        @Override
        public void save(SysPasswordPolicy policy) {
            lastSaved = policy;
        }

        private SysPasswordPolicy policy(String tenantId) {
            return new SysPasswordPolicy(1L, tenantId, "default", "默认密码策略",
                    12, true, true, true, true, 90, 5, 5, 30, true,
                    LocalDateTime.now(), LocalDateTime.now());
        }
    }
}
