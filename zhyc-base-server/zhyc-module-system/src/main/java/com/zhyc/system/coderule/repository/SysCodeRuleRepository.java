/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.repository;

import com.zhyc.system.coderule.domain.SysCodeRule;

import java.util.List;
import java.util.Optional;

/**
 * 系统编码规则仓储。
 */
public interface SysCodeRuleRepository {

    /**
     * 查询租户编码规则列表。
     *
     * @param tenantId 租户业务编码
     * @return 编码规则列表
     */
    List<SysCodeRule> findByTenantId(String tenantId);

    /**
     * 按编码规则编码查询租户编码规则。
     *
     * @param tenantId 租户业务编码
     * @param ruleCode 编码规则编码
     * @return 编码规则，不存在时为空
     */
    Optional<SysCodeRule> findByTenantIdAndRuleCode(String tenantId, String ruleCode);

    /**
     * 保存或更新编码规则。
     *
     * @param rule 系统编码规则
     */
    void save(SysCodeRule rule);

    /**
     * 更新编码规则当前序列值。
     *
     * @param tenantId 租户业务编码
     * @param ruleCode 编码规则编码
     * @param currentValue 当前序列值
     */
    void updateCurrentValue(String tenantId, String ruleCode, Integer currentValue);
}
