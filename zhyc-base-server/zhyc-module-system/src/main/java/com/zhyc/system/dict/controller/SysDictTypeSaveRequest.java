/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.controller;

/**
 * 系统字典类型保存请求。
 */
public class SysDictTypeSaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 字典编码。 */
    private String dictCode;
    /** 字典名称。 */
    private String dictName;
    /** 是否系统内置字典。 */
    private Boolean systemFlag;
    /** 字典状态。 */
    private String status;

    public String getTenantId() { return tenantId; }

    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getDictCode() { return dictCode; }

    public void setDictCode(String dictCode) { this.dictCode = dictCode; }

    public String getDictName() { return dictName; }

    public void setDictName(String dictName) { this.dictName = dictName; }

    public Boolean getSystemFlag() { return systemFlag; }

    public void setSystemFlag(Boolean systemFlag) { this.systemFlag = systemFlag; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
