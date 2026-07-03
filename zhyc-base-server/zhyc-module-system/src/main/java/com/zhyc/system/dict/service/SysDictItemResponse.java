/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.service;

import java.io.Serializable;

/**
 * 系统字典项响应对象。
 */
public class SysDictItemResponse implements Serializable {

    /** 序列化版本号，用于 Redis 缓存反序列化兼容。 */
    private static final long serialVersionUID = 1L;

    /** 字典项主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 字典编码。 */
    private final String dictCode;
    /** 字典项显示标签。 */
    private final String itemLabel;
    /** 字典项实际值。 */
    private final String itemValue;
    /** 字典项前端展示颜色。 */
    private final String itemColor;
    /** 字典项排序号。 */
    private final Integer sortOrder;
    /** 字典项状态。 */
    private final String status;

    /**
     * 创建系统字典项响应对象。
     *
     * @param id 字典项主键
     * @param tenantId 租户业务编码
     * @param dictCode 字典编码
     * @param itemLabel 字典项显示标签
     * @param itemValue 字典项实际值
     * @param itemColor 字典项前端展示颜色
     * @param sortOrder 字典项排序号
     * @param status 字典项状态
     */
    public SysDictItemResponse(Long id, String tenantId, String dictCode, String itemLabel, String itemValue,
                               String itemColor, Integer sortOrder, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.dictCode = dictCode;
        this.itemLabel = itemLabel;
        this.itemValue = itemValue;
        this.itemColor = itemColor;
        this.sortOrder = sortOrder;
        this.status = status;
    }

    /**
     * 返回字典项主键。
     *
     * @return 字典项主键
     */
    public Long getId() {
        return id;
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
     * 返回字典编码。
     *
     * @return 字典编码
     */
    public String getDictCode() {
        return dictCode;
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
     * 返回字典项实际值。
     *
     * @return 字典项实际值
     */
    public String getItemValue() {
        return itemValue;
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
     * 返回字典项排序号。
     *
     * @return 字典项排序号
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 返回字典项状态。
     *
     * @return 字典项状态
     */
    public String getStatus() {
        return status;
    }
}
