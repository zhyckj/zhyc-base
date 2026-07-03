/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.coderule.domain.SysCodeRule;
import com.zhyc.system.coderule.repository.SysCodeRuleRepository;
import com.zhyc.system.coderule.service.DefaultSysCodeRuleService;
import com.zhyc.system.coderule.service.SysCodeRuleResponse;
import com.zhyc.system.coderule.service.SysCodeRuleSaveCommand;
import com.zhyc.system.coderule.service.SysCodeRuleService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 系统编码规则业务服务测试。
 */
class SysCodeRuleServiceTest {

    /**
     * 验证编码规则服务按租户查询规则列表。
     */
    @Test
    void shouldListCodeRulesByTenant() {
        RecordingCodeRuleRepository repository = new RecordingCodeRuleRepository();
        SysCodeRuleService service = new DefaultSysCodeRuleService(repository);

        List<SysCodeRuleResponse> rules = service.listRules(" tenant_a ");

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals(1, rules.size());
        assertEquals("purchase.request", rules.get(0).getRuleCode());
        assertEquals("采购申请单号", rules.get(0).getRuleName());
    }

    /**
     * 验证编码规则保存会裁剪关键字段并保留序列长度和当前值。
     */
    @Test
    void shouldSaveCodeRuleWithNormalizedFields() {
        RecordingCodeRuleRepository repository = new RecordingCodeRuleRepository();
        SysCodeRuleService service = new DefaultSysCodeRuleService(repository);

        service.save(new SysCodeRuleSaveCommand(" tenant_a ", " purchase.request ", " 采购申请单号 ",
                " PR-", "yyyyMMdd", 5, 12, true));

        assertEquals("tenant_a", repository.lastSaved.getTenantId());
        assertEquals("purchase.request", repository.lastSaved.getRuleCode());
        assertEquals("采购申请单号", repository.lastSaved.getRuleName());
        assertEquals("PR-", repository.lastSaved.getPrefix());
        assertEquals("yyyyMMdd", repository.lastSaved.getDatePattern());
        assertEquals(5, repository.lastSaved.getSequenceLength());
        assertEquals(12, repository.lastSaved.getCurrentValue());
    }

    /**
     * 验证编码规则生成下一个编码时会递增当前值，并按前缀、日期和补零序列拼接。
     */
    @Test
    void shouldGenerateNextCodeAndIncreaseCurrentValue() {
        RecordingCodeRuleRepository repository = new RecordingCodeRuleRepository();
        SysCodeRuleService service = new DefaultSysCodeRuleService(repository);

        String code = service.generateNextCode(" tenant_a ", " purchase.request ",
                LocalDate.of(2026, 6, 24));

        assertEquals("tenant_a", repository.lastTenantId);
        assertEquals("purchase.request", repository.lastRuleCode);
        assertEquals(13, repository.lastNextValue);
        assertEquals("PR-2026062400013", code);
    }

    /**
     * 验证编码规则保存会拒绝不合法的日期格式。
     */
    @Test
    void shouldRejectInvalidDatePatternWhenSaving() {
        RecordingCodeRuleRepository repository = new RecordingCodeRuleRepository();
        SysCodeRuleService service = new DefaultSysCodeRuleService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new SysCodeRuleSaveCommand("tenant_a", "purchase.request", "采购申请单号",
                        "PR-", "bad pattern", 5, 12, true)));

        assertEquals("ZHYC_SYS_CODE_RULE_DATE_PATTERN_INVALID", exception.getCode());
        assertEquals("编码规则日期格式不合法: bad pattern", exception.getMessage());
    }

    /**
     * 测试用编码规则仓储。
     */
    private static class RecordingCodeRuleRepository implements SysCodeRuleRepository {

        /** 最近一次查询的租户业务编码。 */
        private String lastTenantId;
        /** 最近一次查询的编码规则编码。 */
        private String lastRuleCode;
        /** 最近一次保存的编码规则。 */
        private SysCodeRule lastSaved;
        /** 最近一次递增后的当前值。 */
        private Integer lastNextValue;

        @Override
        public List<SysCodeRule> findByTenantId(String tenantId) {
            lastTenantId = tenantId;
            return List.of(rule(tenantId, 12));
        }

        @Override
        public Optional<SysCodeRule> findByTenantIdAndRuleCode(String tenantId, String ruleCode) {
            lastTenantId = tenantId;
            lastRuleCode = ruleCode;
            return Optional.of(rule(tenantId, 12));
        }

        @Override
        public void save(SysCodeRule rule) {
            lastSaved = rule;
        }

        @Override
        public void updateCurrentValue(String tenantId, String ruleCode, Integer currentValue) {
            lastTenantId = tenantId;
            lastRuleCode = ruleCode;
            lastNextValue = currentValue;
        }

        private SysCodeRule rule(String tenantId, Integer currentValue) {
            return new SysCodeRule(1L, tenantId, "purchase.request", "采购申请单号",
                    "PR-", "yyyyMMdd", 5, currentValue, true, LocalDateTime.now(), LocalDateTime.now());
        }
    }
}
