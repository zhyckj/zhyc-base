/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.controller;

import java.time.LocalDate;

/**
 * 系统编码规则生成请求。
 */
public class SysCodeRuleNextRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 编码规则编码。 */
    private String ruleCode;
    /** 业务日期。 */
    private LocalDate businessDate;

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 返回编码规则编码。
     *
     * @return 编码规则编码
     */
    public String getRuleCode() {
        return ruleCode;
    }

    /**
     * 设置编码规则编码。
     *
     * @param ruleCode 编码规则编码
     */
    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    /**
     * 返回业务日期。
     *
     * @return 业务日期
     */
    public LocalDate getBusinessDate() {
        return businessDate;
    }

    /**
     * 设置业务日期。
     *
     * @param businessDate 业务日期
     */
    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }
}
