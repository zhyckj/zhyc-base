/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.repository;

import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeModelStatus;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import com.zhyc.lowcode.metadata.mybatis.LowcodeColumnModelMapper;
import com.zhyc.lowcode.metadata.mybatis.LowcodeColumnModelRecord;
import com.zhyc.lowcode.metadata.mybatis.LowcodeTableModelMapper;
import com.zhyc.lowcode.metadata.mybatis.LowcodeTableModelRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MyBatis 低代码表模型仓储测试。
 */
class MyBatisLowcodeTableModelRepositoryTest {

  /**
   * 验证保存表模型会写入表级元数据并按当前字段列表重建字段模型。
   */
  @Test
  void shouldSaveTableModelAndRebuildColumns() {
    FakeTableModelMapper tableMapper = new FakeTableModelMapper();
    FakeColumnModelMapper columnMapper = new FakeColumnModelMapper();
    LowcodeTableModelRepository repository = new MyBatisLowcodeTableModelRepository(tableMapper, columnMapper);
    LowcodeTableModel table = new LowcodeTableModel(
        null, "tenant_a", 10L, "purchase_order", "采购订单", "pur_order",
        LowcodeModelStatus.DRAFT,
        List.of(
            LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG).primaryKey(true).autoIncrement(true).build(),
            LowcodeColumnModel.builder("amount", "金额", LowcodeFieldType.DECIMAL)
                .length(18).scale(2).required(true).listVisible(true).formVisible(true).queryable(true)
                .dictCode("amount_unit").build()
        ));

    LowcodeTableModel saved = repository.save(table);

    assertEquals(1, tableMapper.insertCount);
    assertEquals(100L, saved.getId());
    assertEquals(10L, saved.getDataSourceId());
    assertEquals(1, columnMapper.deleteCount);
    assertEquals("tenant_a", columnMapper.lastDeleteTenantId);
    assertEquals(2, columnMapper.insertedColumns.size());
    assertEquals(0, columnMapper.insertedColumns.get(0).sortOrder());
    assertEquals(1, columnMapper.insertedColumns.get(1).sortOrder());
    assertEquals("amount_unit", columnMapper.insertedColumns.get(1).dictCode());
  }

  /**
   * 验证查询表模型会还原发布状态和字段模型配置。
   */
  @Test
  void shouldFindTableModelWithColumns() {
    FakeTableModelMapper tableMapper = new FakeTableModelMapper();
    FakeColumnModelMapper columnMapper = new FakeColumnModelMapper();
    tableMapper.records.put("tenant_a:purchase_order", new LowcodeTableModelRecord(
        100L, "tenant_a", 10L, "purchase_order", "采购订单", "pur_order", "PUBLISHED"));
    columnMapper.columns.put(100L, List.of(new LowcodeColumnModelRecord(
        1L, 100L, "id", "主键", "LONG", null, null,
        true, true, true, false, false, false, null, 0, "主键字段")));
    LowcodeTableModelRepository repository = new MyBatisLowcodeTableModelRepository(tableMapper, columnMapper);

    LowcodeTableModel table = repository.findByTenantIdAndCode("tenant_a", "purchase_order").orElseThrow();

    assertEquals(LowcodeModelStatus.PUBLISHED, table.getStatus());
    assertEquals(10L, table.getDataSourceId());
    assertEquals(1, table.getColumns().size());
    assertTrue(table.getColumns().get(0).isPrimaryKey());
    assertEquals("主键字段", table.getColumns().get(0).getComment());
  }

  /**
   * 验证仓储按租户和表模型主键精确查询，避免页面和表关系校验时加载租户下全部表模型。
   */
  @Test
  void shouldFindTableModelByTenantAndId() {
    FakeTableModelMapper tableMapper = new FakeTableModelMapper();
    FakeColumnModelMapper columnMapper = new FakeColumnModelMapper();
    tableMapper.records.put("tenant_a:purchase_order", new LowcodeTableModelRecord(
        100L, "tenant_a", 10L, "purchase_order", "采购订单", "pur_order", "PUBLISHED"));
    tableMapper.records.put("tenant_b:sale_order", new LowcodeTableModelRecord(
        100L, "tenant_b", 20L, "sale_order", "销售订单", "sale_order", "DRAFT"));
    columnMapper.columns.put(100L, List.of(new LowcodeColumnModelRecord(
        1L, 100L, "id", "主键", "LONG", null, null,
        true, true, true, false, false, false, null, 0, "主键字段")));
    LowcodeTableModelRepository repository = new MyBatisLowcodeTableModelRepository(tableMapper, columnMapper);

    LowcodeTableModel table = repository.findByTenantIdAndId("tenant_a", 100L).orElseThrow();

    assertEquals("purchase_order", table.getCode());
    assertEquals("tenant_a", table.getTenantId());
    assertEquals(1, table.getColumns().size());
  }

  /**
   * 验证按租户和主键查询不会返回其他租户同主键的表模型。
   */
  @Test
  void shouldNotFindTableModelByIdOutsideTenant() {
    FakeTableModelMapper tableMapper = new FakeTableModelMapper();
    FakeColumnModelMapper columnMapper = new FakeColumnModelMapper();
    tableMapper.records.put("tenant_b:sale_order", new LowcodeTableModelRecord(
        100L, "tenant_b", 20L, "sale_order", "销售订单", "sale_order", "DRAFT"));
    LowcodeTableModelRepository repository = new MyBatisLowcodeTableModelRepository(tableMapper, columnMapper);

    assertTrue(repository.findByTenantIdAndId("tenant_a", 100L).isEmpty());
  }

  /**
   * 验证按租户查询表模型列表时会携带字段并隔离其他租户数据。
   */
  @Test
  void shouldFindTableModelsByTenant() {
    FakeTableModelMapper tableMapper = new FakeTableModelMapper();
    FakeColumnModelMapper columnMapper = new FakeColumnModelMapper();
    tableMapper.records.put("tenant_a:purchase_order", new LowcodeTableModelRecord(
        100L, "tenant_a", 10L, "purchase_order", "采购订单", "pur_order", "DRAFT"));
    tableMapper.records.put("tenant_b:supplier", new LowcodeTableModelRecord(
        101L, "tenant_b", 20L, "supplier", "供应商", "pur_supplier", "DRAFT"));
    columnMapper.columns.put(100L, List.of(new LowcodeColumnModelRecord(
        1L, 100L, "status", "状态", "STRING", 32, null,
        true, false, false, true, true, true, "order_status", 0, "状态字段")));
    LowcodeTableModelRepository repository = new MyBatisLowcodeTableModelRepository(tableMapper, columnMapper);

    List<LowcodeTableModel> tables = repository.findByTenantId("tenant_a");

    assertEquals(1, tables.size());
    assertEquals("purchase_order", tables.get(0).getCode());
    assertEquals(1, tables.get(0).getColumns().size());
    assertEquals("order_status", tables.get(0).getColumns().get(0).getDictCode());
  }

  /**
   * 测试用表模型 Mapper。
   */
  private static class FakeTableModelMapper implements LowcodeTableModelMapper {

    /** 已持久化表模型记录。 */
    private final Map<String, LowcodeTableModelRecord> records = new HashMap<>();
    /** 插入次数。 */
    private int insertCount;
    /** 更新次数。 */
    private int updateCount;

    @Override
    public int insert(LowcodeTableModelRecord record) {
      insertCount++;
      records.put(key(record.tenantId(), record.code()), new LowcodeTableModelRecord(
          100L, record.tenantId(), record.dataSourceId(), record.code(), record.name(),
          record.tableName(), record.status()));
      return 1;
    }

    @Override
    public LowcodeTableModelRecord selectByTenantIdAndCode(String tenantId, String code) {
      return records.get(key(tenantId, code));
    }

    @Override
    public LowcodeTableModelRecord selectByTenantIdAndId(String tenantId, Long id) {
      return records.values().stream()
          .filter(record -> record.tenantId().equals(tenantId))
          .filter(record -> id.equals(record.id()))
          .findFirst()
          .orElse(null);
    }

    @Override
    public List<LowcodeTableModelRecord> selectByTenantId(String tenantId) {
      return records.values().stream()
          .filter(record -> record.tenantId().equals(tenantId))
          .toList();
    }

    @Override
    public int updateStatusByTenantIdAndCode(String tenantId, String code, String status) {
      throw new UnsupportedOperationException("测试不使用状态专用更新");
    }

    @Override
    public int updateByTenantIdAndCode(LowcodeTableModelRecord record) {
      updateCount++;
      LowcodeTableModelRecord existing = records.get(key(record.tenantId(), record.code()));
      records.put(key(record.tenantId(), record.code()), new LowcodeTableModelRecord(
          existing.id(), record.tenantId(), record.dataSourceId(), record.code(), record.name(),
          record.tableName(), record.status()));
      return 1;
    }

    private String key(String tenantId, String code) {
      return tenantId + ":" + code;
    }
  }

  /**
   * 测试用字段模型 Mapper。
   */
  private static class FakeColumnModelMapper implements LowcodeColumnModelMapper {

    /** 字段记录集合。 */
    private final Map<Long, List<LowcodeColumnModelRecord>> columns = new HashMap<>();
    /** 本次插入字段记录。 */
    private final List<LowcodeColumnModelRecord> insertedColumns = new ArrayList<>();
    /** 删除次数。 */
    private int deleteCount;
    /** 最近一次删除字段时传入的租户业务编码。 */
    private String lastDeleteTenantId;

    @Override
    public int insert(LowcodeColumnModelRecord record) {
      insertedColumns.add(record);
      columns.computeIfAbsent(record.tableModelId(), ignored -> new ArrayList<>()).add(record);
      return 1;
    }

    @Override
    public int deleteByTenantIdAndTableModelId(String tenantId, Long tableModelId) {
      deleteCount++;
      lastDeleteTenantId = tenantId;
      columns.remove(tableModelId);
      return 1;
    }

    @Override
    public List<LowcodeColumnModelRecord> selectByTenantIdAndTableModelId(String tenantId, Long tableModelId) {
      return columns.getOrDefault(tableModelId, List.of());
    }
  }
}
