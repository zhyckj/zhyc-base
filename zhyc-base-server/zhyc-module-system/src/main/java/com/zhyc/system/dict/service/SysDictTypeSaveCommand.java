/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.service;

/**
 * 系统字典类型保存命令。
 */
public class SysDictTypeSaveCommand {

    private final Long typeId;
    private final String tenantId;
    private final String dictCode;
    private final String dictName;
    private final Boolean systemFlag;
    private final String status;

    public SysDictTypeSaveCommand(Long typeId, String tenantId, String dictCode, String dictName, Boolean systemFlag,
                                  String status) {
        this.typeId = typeId;
        this.tenantId = tenantId;
        this.dictCode = dictCode;
        this.dictName = dictName;
        this.systemFlag = systemFlag;
        this.status = status;
    }

    public Long getTypeId() { return typeId; }

    public String getTenantId() { return tenantId; }

    public String getDictCode() { return dictCode; }

    public String getDictName() { return dictName; }

    public Boolean getSystemFlag() { return systemFlag; }

    public String getStatus() { return status; }
}
