/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.service;

import java.time.LocalDate;
import java.util.List;

/**
 * 系统编码规则业务服务。
 */
public interface SysCodeRuleService {

    /**
     * 查询租户编码规则列表。
     *
     * @param tenantId 租户业务编码
     * @return 编码规则列表
     */
    List<SysCodeRuleResponse> listRules(String tenantId);

    /**
     * 保存或更新编码规则。
     *
     * @param command 编码规则保存命令
     */
    void save(SysCodeRuleSaveCommand command);

    /**
     * 生成下一个业务编码。
     *
     * @param tenantId 租户业务编码
     * @param ruleCode 编码规则编码
     * @param businessDate 业务日期
     * @return 下一个业务编码
     */
    String generateNextCode(String tenantId, String ruleCode, LocalDate businessDate);
}
