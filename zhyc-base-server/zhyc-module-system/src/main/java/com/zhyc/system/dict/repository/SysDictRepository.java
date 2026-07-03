/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.repository;

import com.zhyc.system.dict.domain.SysDictItem;
import com.zhyc.system.dict.domain.SysDictType;

import java.util.List;

/**
 * 系统字典仓储。
 */
public interface SysDictRepository {

    /**
     * 查询租户字典类型列表。
     *
     * @param tenantId 租户业务编码
     * @return 字典类型列表
     */
    List<SysDictType> findTypesByTenantId(String tenantId);

    void insertType(SysDictType type);

    void updateType(SysDictType type);

    void deleteTypeByTenantIdAndId(String tenantId, Long typeId);

    /**
     * 查询租户指定字典编码下的字典项。
     *
     * @param tenantId 租户业务编码
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    List<SysDictItem> findItemsByTenantIdAndDictCode(String tenantId, String dictCode);

    void insertItem(SysDictItem item);

    void updateItem(SysDictItem item);

    void deleteItemByTenantIdAndId(String tenantId, Long itemId);
}
