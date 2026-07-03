/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.repository;

import com.zhyc.system.dict.domain.SysDictItem;
import com.zhyc.system.dict.domain.SysDictType;
import com.zhyc.system.dict.mapper.SysDictMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统字典仓储实现。
 */
@Repository
public class MyBatisSysDictRepository implements SysDictRepository {

    /** 系统字典 Mapper。 */
    private final SysDictMapper dictMapper;

    /**
     * 创建系统字典仓储实现。
     *
     * @param dictMapper 系统字典 Mapper
     */
    public MyBatisSysDictRepository(SysDictMapper dictMapper) {
        this.dictMapper = Objects.requireNonNull(dictMapper, "系统字典 Mapper 不能为空");
    }

    @Override
    public List<SysDictType> findTypesByTenantId(String tenantId) {
        return dictMapper.selectTypesByTenantId(tenantId);
    }

    @Override
    public void insertType(SysDictType type) {
        dictMapper.insertType(type);
    }

    @Override
    public void updateType(SysDictType type) {
        dictMapper.updateType(type);
    }

    @Override
    public void deleteTypeByTenantIdAndId(String tenantId, Long typeId) {
        dictMapper.deleteTypeByTenantIdAndId(tenantId, typeId);
    }

    @Override
    public List<SysDictItem> findItemsByTenantIdAndDictCode(String tenantId, String dictCode) {
        return dictMapper.selectItemsByTenantIdAndDictCode(tenantId, dictCode);
    }

    @Override
    public void insertItem(SysDictItem item) {
        dictMapper.insertItem(item);
    }

    @Override
    public void updateItem(SysDictItem item) {
        dictMapper.updateItem(item);
    }

    @Override
    public void deleteItemByTenantIdAndId(String tenantId, Long itemId) {
        dictMapper.deleteItemByTenantIdAndId(tenantId, itemId);
    }
}
