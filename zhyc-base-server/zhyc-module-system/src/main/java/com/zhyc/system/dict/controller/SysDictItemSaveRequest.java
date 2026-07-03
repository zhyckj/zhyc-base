/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.controller;

/**
 * 系统字典项保存请求。
 */
public class SysDictItemSaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 字典编码。 */
    private String dictCode;
    /** 字典项显示标签。 */
    private String itemLabel;
    /** 字典项实际值。 */
    private String itemValue;
    /** 字典项前端展示颜色。 */
    private String itemColor;
    /** 字典项排序号。 */
    private Integer sortOrder;
    /** 字典项状态。 */
    private String status;

    public String getTenantId() { return tenantId; }

    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getDictCode() { return dictCode; }

    public void setDictCode(String dictCode) { this.dictCode = dictCode; }

    public String getItemLabel() { return itemLabel; }

    public void setItemLabel(String itemLabel) { this.itemLabel = itemLabel; }

    public String getItemValue() { return itemValue; }

    public void setItemValue(String itemValue) { this.itemValue = itemValue; }

    public String getItemColor() { return itemColor; }

    public void setItemColor(String itemColor) { this.itemColor = itemColor; }

    public Integer getSortOrder() { return sortOrder; }

    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
