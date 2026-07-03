/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.dict.mapper;

import com.zhyc.system.dict.domain.SysDictItem;
import com.zhyc.system.dict.domain.SysDictType;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统字典 MyBatis Mapper。
 */
@Mapper
public interface SysDictMapper {

    /**
     * 查询租户字典类型列表。
     *
     * @param tenantId 租户业务编码
     * @return 字典类型列表
     */
    @SelectProvider(type = SysDictSqlProvider.class, method = "selectTypesByTenantId")
    List<SysDictType> selectTypesByTenantId(@Param("tenantId") String tenantId);

    @InsertProvider(type = SysDictSqlProvider.class, method = "insertType")
    void insertType(SysDictType type);

    @UpdateProvider(type = SysDictSqlProvider.class, method = "updateType")
    void updateType(SysDictType type);

    @DeleteProvider(type = SysDictSqlProvider.class, method = "deleteTypeByTenantIdAndId")
    void deleteTypeByTenantIdAndId(@Param("tenantId") String tenantId, @Param("typeId") Long typeId);

    /**
     * 查询租户指定字典编码下的字典项。
     *
     * @param tenantId 租户业务编码
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    @SelectProvider(type = SysDictSqlProvider.class, method = "selectItemsByTenantIdAndDictCode")
    List<SysDictItem> selectItemsByTenantIdAndDictCode(@Param("tenantId") String tenantId,
                                                       @Param("dictCode") String dictCode);

    @InsertProvider(type = SysDictSqlProvider.class, method = "insertItem")
    void insertItem(SysDictItem item);

    @UpdateProvider(type = SysDictSqlProvider.class, method = "updateItem")
    void updateItem(SysDictItem item);

    @DeleteProvider(type = SysDictSqlProvider.class, method = "deleteItemByTenantIdAndId")
    void deleteItemByTenantIdAndId(@Param("tenantId") String tenantId, @Param("itemId") Long itemId);
}
