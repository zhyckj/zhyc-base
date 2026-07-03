/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.accessrestriction.domain.SysAccessRestriction;
import com.zhyc.system.accessrestriction.repository.SysAccessRestrictionRepository;
import com.zhyc.system.accessrestriction.service.DefaultSysAccessRestrictionService;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionEvaluationResult;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionResponse;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionSaveCommand;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 系统访问限制业务服务测试。
 */
class SysAccessRestrictionServiceTest {

    /**
     * 验证访问限制服务按租户、限制类型和当前时间查询生效规则。
     */
    @Test
    void shouldListActiveRestrictionsByTenantTypeAndTime() {
        RecordingAccessRestrictionRepository repository = new RecordingAccessRestrictionRepository();
        SysAccessRestrictionService service = new DefaultSysAccessRestrictionService(repository);
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 10, 0);

        List<SysAccessRestrictionResponse> restrictions =
                service.listActiveRestrictions(" tenant_a ", " ip ", now);

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("ip", repository.lastRestrictionType);
        assertEquals(now, repository.lastNow);
        assertEquals(1, restrictions.size());
        assertEquals("192.168.1.10", restrictions.get(0).getRuleValue());
        assertEquals("deny", restrictions.get(0).getEffect());
    }

    /**
     * 验证访问限制保存会裁剪租户、类型、规则值和生效动作。
     */
    @Test
    void shouldSaveRestrictionWithNormalizedFields() {
        RecordingAccessRestrictionRepository repository = new RecordingAccessRestrictionRepository();
        SysAccessRestrictionService service = new DefaultSysAccessRestrictionService(repository);
        LocalDateTime startAt = LocalDateTime.of(2026, 6, 24, 9, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 6, 25, 9, 0);

        service.save(new SysAccessRestrictionSaveCommand(" tenant_a ", " ip ",
                " 192.168.1.10 ", " deny ", startAt, endAt));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("ip", repository.lastSaved.getRestrictionType());
        assertEquals("192.168.1.10", repository.lastSaved.getRuleValue());
        assertEquals("deny", repository.lastSaved.getEffect());
        assertEquals(startAt, repository.lastSaved.getStartAt());
        assertEquals(endAt, repository.lastSaved.getEndAt());
    }

    /**
     * 验证访问限制判定命中拒绝规则时返回拒绝结果和命中规则。
     */
    @Test
    void shouldEvaluateAccessAsDeniedWhenDenyRestrictionMatched() {
        RecordingAccessRestrictionRepository repository = new RecordingAccessRestrictionRepository();
        SysAccessRestrictionService service = new DefaultSysAccessRestrictionService(repository);
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 10, 0);

        SysAccessRestrictionEvaluationResult result =
                service.evaluateAccess(" tenant_a ", " ip ", " 192.168.1.10 ", now);

        assertFalse(result.isAllowed());
        assertEquals("deny", result.getEffect());
        assertEquals("192.168.1.10", result.getMatchedRuleValue());
    }

    /**
     * 验证 IP 访问限制判定支持 IPv4 CIDR 网段规则。
     */
    @Test
    void shouldEvaluateIpAccessByIpv4CidrRestriction() {
        RecordingAccessRestrictionRepository repository = new RecordingAccessRestrictionRepository(
                "192.168.1.0/24");
        SysAccessRestrictionService service = new DefaultSysAccessRestrictionService(repository);
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 10, 0);

        SysAccessRestrictionEvaluationResult result =
                service.evaluateAccess("tenant_a", "ip", "192.168.1.88", now);

        assertFalse(result.isAllowed());
        assertEquals("deny", result.getEffect());
        assertEquals("192.168.1.0/24", result.getMatchedRuleValue());
    }

    /**
     * 验证访问限制类型只能使用安全策略支持的 IP、账号和设备类型。
     */
    @Test
    void shouldRejectUnsupportedRestrictionType() {
        RecordingAccessRestrictionRepository repository = new RecordingAccessRestrictionRepository();
        SysAccessRestrictionService service = new DefaultSysAccessRestrictionService(repository);
        LocalDateTime now = LocalDateTime.of(2026, 6, 24, 10, 0);

        BusinessException listException = assertThrows(BusinessException.class,
                () -> service.listActiveRestrictions("tenant_a", "region", now));
        BusinessException evaluateException = assertThrows(BusinessException.class,
                () -> service.evaluateAccess("tenant_a", "region", "cn-east", now));
        BusinessException saveException = assertThrows(BusinessException.class,
                () -> service.save(new SysAccessRestrictionSaveCommand("tenant_a", "region",
                        "cn-east", "deny", null, null)));

        assertEquals("ZHYC_SYS_ACCESS_RESTRICTION_TYPE_UNSUPPORTED", listException.getCode());
        assertEquals("ZHYC_SYS_ACCESS_RESTRICTION_TYPE_UNSUPPORTED", evaluateException.getCode());
        assertEquals("ZHYC_SYS_ACCESS_RESTRICTION_TYPE_UNSUPPORTED", saveException.getCode());
        assertEquals("限制类型不支持: region", listException.getMessage());
        assertEquals("限制类型不支持: region", evaluateException.getMessage());
        assertEquals("限制类型不支持: region", saveException.getMessage());
    }

    /**
     * 验证访问限制生效动作只能使用允许或拒绝。
     */
    @Test
    void shouldRejectUnsupportedEffect() {
        RecordingAccessRestrictionRepository repository = new RecordingAccessRestrictionRepository();
        SysAccessRestrictionService service = new DefaultSysAccessRestrictionService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new SysAccessRestrictionSaveCommand("tenant_a", "ip",
                        "192.168.1.10", "audit", null, null)));

        assertEquals("ZHYC_SYS_ACCESS_RESTRICTION_EFFECT_UNSUPPORTED", exception.getCode());
        assertEquals("生效动作不支持: audit", exception.getMessage());
    }

    /**
     * 测试用访问限制仓储。
     */
    private static class RecordingAccessRestrictionRepository implements SysAccessRestrictionRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的限制类型。 */
        private String lastRestrictionType;
        /** 最近一次查询的当前时间。 */
        private LocalDateTime lastNow;
        /** 最近一次保存的访问限制。 */
        private SysAccessRestriction lastSaved;
        /** 测试规则值。 */
        private final String ruleValue;

        private RecordingAccessRestrictionRepository() {
            this("192.168.1.10");
        }

        private RecordingAccessRestrictionRepository(String ruleValue) {
            this.ruleValue = ruleValue;
        }

        @Override
        public List<SysAccessRestriction> findActive(String tenantId, String restrictionType, LocalDateTime now) {
            lastTenantId = tenantId;
            lastRestrictionType = restrictionType;
            lastNow = now;
            return List.of(new SysAccessRestriction(1L, tenantId, restrictionType, ruleValue,
                    "deny", null, null, LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void save(SysAccessRestriction restriction) {
            lastSaved = restriction;
        }
    }
}
