/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.service;

import java.util.List;

/**
 * 系统字典业务服务。
 */
public interface SysDictService {

    /**
     * 查询租户字典类型列表。
     *
     * @param tenantId 租户业务编码
     * @return 字典类型列表
     */
    List<SysDictTypeResponse> listTypes(String tenantId);

    void saveType(SysDictTypeSaveCommand command);

    void deleteType(String tenantId, Long typeId);

    /**
     * 查询租户指定字典编码下的字典项。
     *
     * @param tenantId 租户业务编码
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    List<SysDictItemResponse> listItems(String tenantId, String dictCode);

    void saveItem(SysDictItemSaveCommand command);

    void deleteItem(String tenantId, Long itemId);
}
