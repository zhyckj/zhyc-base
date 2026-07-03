/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.repository;

import com.zhyc.system.coderule.domain.SysCodeRule;
import com.zhyc.system.coderule.mapper.SysCodeRuleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的系统编码规则仓储实现。
 */
@Repository
public class MyBatisSysCodeRuleRepository implements SysCodeRuleRepository {

    /** 系统编码规则 Mapper。 */
    private final SysCodeRuleMapper codeRuleMapper;

    /**
     * 创建系统编码规则仓储实现。
     *
     * @param codeRuleMapper 系统编码规则 Mapper
     */
    public MyBatisSysCodeRuleRepository(SysCodeRuleMapper codeRuleMapper) {
        this.codeRuleMapper = Objects.requireNonNull(codeRuleMapper, "系统编码规则 Mapper 不能为空");
    }

    @Override
    public List<SysCodeRule> findByTenantId(String tenantId) {
        return codeRuleMapper.selectByTenantId(tenantId);
    }

    @Override
    public Optional<SysCodeRule> findByTenantIdAndRuleCode(String tenantId, String ruleCode) {
        return Optional.ofNullable(codeRuleMapper.selectByTenantIdAndRuleCode(tenantId, ruleCode));
    }

    @Override
    public void save(SysCodeRule rule) {
        codeRuleMapper.upsert(rule);
    }

    @Override
    public void updateCurrentValue(String tenantId, String ruleCode, Integer currentValue) {
        codeRuleMapper.updateCurrentValue(tenantId, ruleCode, currentValue);
    }
}
