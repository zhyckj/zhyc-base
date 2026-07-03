/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 低代码元数据 MyBatis Mapper 契约测试。
 */
class LowcodeMetadataMapperContractTest {

  /**
   * 验证数据源 Mapper 通过 Provider 绑定租户隔离查询。
   *
   * @throws NoSuchMethodException 方法签名缺失时抛出
   */
  @Test
  void shouldBindDataSourceMapperProviders() throws NoSuchMethodException {
    assertNotNull(LowcodeDataSourceMapper.class.getAnnotation(Mapper.class));

    Method insert = LowcodeDataSourceMapper.class.getMethod("insert", LowcodeDataSourceRecord.class);
    InsertProvider insertProvider = insert.getAnnotation(InsertProvider.class);
    assertEquals(LowcodeDataSourceSqlProvider.class, insertProvider.type());
    assertEquals("insert", insertProvider.method());

    Method select = LowcodeDataSourceMapper.class.getMethod("selectByTenantIdAndCode", String.class, String.class);
    SelectProvider selectProvider = select.getAnnotation(SelectProvider.class);
    assertEquals(LowcodeDataSourceSqlProvider.class, selectProvider.type());
    assertEquals("selectByTenantIdAndCode", selectProvider.method());

    Method selectById = LowcodeDataSourceMapper.class.getMethod("selectByTenantIdAndId", String.class, Long.class);
    SelectProvider selectByIdProvider = selectById.getAnnotation(SelectProvider.class);
    assertEquals(LowcodeDataSourceSqlProvider.class, selectByIdProvider.type());
    assertEquals("selectByTenantIdAndId", selectByIdProvider.method());
  }

  /**
   * 验证表模型 Mapper 通过 Provider 绑定表模型写入和查询。
   *
   * @throws NoSuchMethodException 方法签名缺失时抛出
   */
  @Test
  void shouldBindTableModelMapperProviders() throws NoSuchMethodException {
    assertNotNull(LowcodeTableModelMapper.class.getAnnotation(Mapper.class));

    Method insert = LowcodeTableModelMapper.class.getMethod("insert", LowcodeTableModelRecord.class);
    InsertProvider insertProvider = insert.getAnnotation(InsertProvider.class);
    assertEquals(LowcodeTableModelSqlProvider.class, insertProvider.type());
    assertEquals("insert", insertProvider.method());

    Method selectById = LowcodeTableModelMapper.class.getMethod("selectByTenantIdAndId", String.class, Long.class);
    SelectProvider selectByIdProvider = selectById.getAnnotation(SelectProvider.class);
    assertEquals(LowcodeTableModelSqlProvider.class, selectByIdProvider.type());
    assertEquals("selectByTenantIdAndId", selectByIdProvider.method());
  }

  /**
   * 验证字段模型 Mapper 通过 Provider 绑定重建字段能力。
   *
   * @throws NoSuchMethodException 方法签名缺失时抛出
   */
  @Test
  void shouldBindColumnModelMapperProviders() throws NoSuchMethodException {
    assertNotNull(LowcodeColumnModelMapper.class.getAnnotation(Mapper.class));

    Method insert = LowcodeColumnModelMapper.class.getMethod("insert", LowcodeColumnModelRecord.class);
    InsertProvider insertProvider = insert.getAnnotation(InsertProvider.class);
    assertEquals(LowcodeColumnModelSqlProvider.class, insertProvider.type());
    assertEquals("insert", insertProvider.method());

    Method delete = LowcodeColumnModelMapper.class.getMethod("deleteByTenantIdAndTableModelId",
        String.class, Long.class);
    DeleteProvider deleteProvider = delete.getAnnotation(DeleteProvider.class);
    assertEquals(LowcodeColumnModelSqlProvider.class, deleteProvider.type());
    assertEquals("deleteByTenantIdAndTableModelId", deleteProvider.method());
  }

  /**
   * 验证页面模型 Mapper 通过 Provider 绑定保存、更新和租户隔离查询。
   *
   * @throws NoSuchMethodException 方法签名缺失时抛出
   */
  @Test
  void shouldBindPageModelMapperProviders() throws NoSuchMethodException {
    assertNotNull(LowcodePageModelMapper.class.getAnnotation(Mapper.class));

    Method insert = LowcodePageModelMapper.class.getMethod("insert", LowcodePageModelRecord.class);
    InsertProvider insertProvider = insert.getAnnotation(InsertProvider.class);
    assertEquals(LowcodePageModelSqlProvider.class, insertProvider.type());
    assertEquals("insert", insertProvider.method());

    Method select = LowcodePageModelMapper.class.getMethod("selectByTenantId", String.class);
    SelectProvider selectProvider = select.getAnnotation(SelectProvider.class);
    assertEquals(LowcodePageModelSqlProvider.class, selectProvider.type());
    assertEquals("selectByTenantId", selectProvider.method());

    Method update = LowcodePageModelMapper.class.getMethod("updateByTenantTableAndType", LowcodePageModelRecord.class);
    UpdateProvider updateProvider = update.getAnnotation(UpdateProvider.class);
    assertEquals(LowcodePageModelSqlProvider.class, updateProvider.type());
    assertEquals("updateByTenantTableAndType", updateProvider.method());
  }

  /**
   * 验证表关系 Mapper 通过 Provider 绑定保存、更新和租户隔离查询。
   *
   * @throws NoSuchMethodException 方法签名缺失时抛出
   */
  @Test
  void shouldBindTableRelationMapperProviders() throws NoSuchMethodException {
    assertNotNull(LowcodeTableRelationMapper.class.getAnnotation(Mapper.class));

    Method insert = LowcodeTableRelationMapper.class.getMethod("insert", LowcodeTableRelationRecord.class);
    InsertProvider insertProvider = insert.getAnnotation(InsertProvider.class);
    assertEquals(LowcodeTableRelationSqlProvider.class, insertProvider.type());
    assertEquals("insert", insertProvider.method());

    Method select = LowcodeTableRelationMapper.class.getMethod("selectByTenantId", String.class);
    SelectProvider selectProvider = select.getAnnotation(SelectProvider.class);
    assertEquals(LowcodeTableRelationSqlProvider.class, selectProvider.type());
    assertEquals("selectByTenantId", selectProvider.method());

    Method update = LowcodeTableRelationMapper.class.getMethod("updateByTenantAndTables",
        LowcodeTableRelationRecord.class);
    UpdateProvider updateProvider = update.getAnnotation(UpdateProvider.class);
    assertEquals(LowcodeTableRelationSqlProvider.class, updateProvider.type());
    assertEquals("updateByTenantAndTables", updateProvider.method());
  }

  /**
   * 验证生成记录 Mapper 通过 Provider 绑定保存和租户隔离查询。
   *
   * @throws NoSuchMethodException 方法签名缺失时抛出
   */
  @Test
  void shouldBindGenerationRecordMapperProviders() throws NoSuchMethodException {
    assertNotNull(com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordMapper.class.getAnnotation(Mapper.class));

    Method insert = com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordMapper.class.getMethod(
        "insert", com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordRecord.class);
    InsertProvider insertProvider = insert.getAnnotation(InsertProvider.class);
    assertEquals(com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordSqlProvider.class, insertProvider.type());
    assertEquals("insert", insertProvider.method());

    Method select = com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordMapper.class.getMethod(
        "selectByTenantId", String.class);
    SelectProvider selectProvider = select.getAnnotation(SelectProvider.class);
    assertEquals(com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordSqlProvider.class, selectProvider.type());
    assertEquals("selectByTenantId", selectProvider.method());
  }
}
