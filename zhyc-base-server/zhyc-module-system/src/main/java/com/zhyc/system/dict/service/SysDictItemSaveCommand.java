/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.service;

/**
 * 系统字典项保存命令。
 */
public class SysDictItemSaveCommand {

    private final Long itemId;
    private final String tenantId;
    private final String dictCode;
    private final String itemLabel;
    private final String itemValue;
    private final String itemColor;
    private final Integer sortOrder;
    private final String status;

    public SysDictItemSaveCommand(Long itemId, String tenantId, String dictCode, String itemLabel, String itemValue,
                                  String itemColor, Integer sortOrder, String status) {
        this.itemId = itemId;
        this.tenantId = tenantId;
        this.dictCode = dictCode;
        this.itemLabel = itemLabel;
        this.itemValue = itemValue;
        this.itemColor = itemColor;
        this.sortOrder = sortOrder;
        this.status = status;
    }

    public Long getItemId() { return itemId; }

    public String getTenantId() { return tenantId; }

    public String getDictCode() { return dictCode; }

    public String getItemLabel() { return itemLabel; }

    public String getItemValue() { return itemValue; }

    public String getItemColor() { return itemColor; }

    public Integer getSortOrder() { return sortOrder; }

    public String getStatus() { return status; }
}
