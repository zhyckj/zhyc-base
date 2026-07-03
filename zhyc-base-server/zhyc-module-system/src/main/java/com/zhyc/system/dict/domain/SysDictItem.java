/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.domain;

import java.time.LocalDateTime;

/**
 * 系统字典项领域模型。
 */
public class SysDictItem {

    /** 数据库主键。 */
    private Long id;
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
    /** 字典项状态，例如 enabled、disabled。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空字典项对象。
     */
    public SysDictItem() {
    }

    /**
     * 创建完整字典项对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param dictCode 字典编码
     * @param itemLabel 字典项显示标签
     * @param itemValue 字典项实际值
     * @param itemColor 字典项前端展示颜色
     * @param sortOrder 字典项排序号
     * @param status 字典项状态
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysDictItem(Long id, String tenantId, String dictCode, String itemLabel, String itemValue,
                       String itemColor, Integer sortOrder, String status, LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.dictCode = dictCode;
        this.itemLabel = itemLabel;
        this.itemValue = itemValue;
        this.itemColor = itemColor;
        this.sortOrder = sortOrder;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 返回数据库主键。
     *
     * @return 数据库主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置数据库主键。
     *
     * @param id 数据库主键
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * 返回字典编码。
     *
     * @return 字典编码
     */
    public String getDictCode() {
        return dictCode;
    }

    /**
     * 设置字典编码。
     *
     * @param dictCode 字典编码
     */
    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    /**
     * 返回字典项显示标签。
     *
     * @return 字典项显示标签
     */
    public String getItemLabel() {
        return itemLabel;
    }

    /**
     * 设置字典项显示标签。
     *
     * @param itemLabel 字典项显示标签
     */
    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    /**
     * 返回字典项实际值。
     *
     * @return 字典项实际值
     */
    public String getItemValue() {
        return itemValue;
    }

    /**
     * 设置字典项实际值。
     *
     * @param itemValue 字典项实际值
     */
    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    /**
     * 返回字典项前端展示颜色。
     *
     * @return 字典项前端展示颜色
     */
    public String getItemColor() {
        return itemColor;
    }

    /**
     * 设置字典项前端展示颜色。
     *
     * @param itemColor 字典项前端展示颜色
     */
    public void setItemColor(String itemColor) {
        this.itemColor = itemColor;
    }

    /**
     * 返回字典项排序号。
     *
     * @return 字典项排序号
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 设置字典项排序号。
     *
     * @param sortOrder 字典项排序号
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 返回字典项状态。
     *
     * @return 字典项状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置字典项状态。
     *
     * @param status 字典项状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 返回更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间。
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
