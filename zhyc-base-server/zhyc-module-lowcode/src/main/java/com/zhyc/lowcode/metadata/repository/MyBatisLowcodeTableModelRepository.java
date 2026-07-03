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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 MyBatis 的低代码表模型仓储实现。
 *
 * <p>表模型和字段模型按同一租户模型编码保存，字段模型采用先删后插的整体重建策略。</p>
 */
@Repository
public class MyBatisLowcodeTableModelRepository implements LowcodeTableModelRepository {

  /** 低代码表模型 Mapper。 */
  private final LowcodeTableModelMapper tableModelMapper;
  /** 低代码字段模型 Mapper。 */
  private final LowcodeColumnModelMapper columnModelMapper;

  /**
   * 创建 MyBatis 表模型仓储。
   *
   * @param tableModelMapper 低代码表模型 Mapper
   * @param columnModelMapper 低代码字段模型 Mapper
   */
  public MyBatisLowcodeTableModelRepository(LowcodeTableModelMapper tableModelMapper,
                                            LowcodeColumnModelMapper columnModelMapper) {
    this.tableModelMapper = Objects.requireNonNull(tableModelMapper, "低代码表模型 Mapper 不能为空");
    this.columnModelMapper = Objects.requireNonNull(columnModelMapper, "低代码字段模型 Mapper 不能为空");
  }

  @Override
  @Transactional
  public LowcodeTableModel save(LowcodeTableModel tableModel) {
    Objects.requireNonNull(tableModel, "表模型不能为空");
    LowcodeTableModelRecord record = toRecord(tableModel);
    LowcodeTableModelRecord existing = tableModelMapper.selectByTenantIdAndCode(tableModel.getTenantId(), tableModel.getCode());
    if (existing == null) {
      tableModelMapper.insert(record);
    } else {
      tableModelMapper.updateByTenantIdAndCode(record);
    }
    LowcodeTableModelRecord persisted = tableModelMapper.selectByTenantIdAndCode(tableModel.getTenantId(), tableModel.getCode());
    if (persisted == null || persisted.id() == null) {
      throw new IllegalStateException("保存表模型后无法获取表模型主键: " + tableModel.getCode());
    }
    rebuildColumns(tableModel.getTenantId(), persisted.id(), tableModel.getColumns());
    return toDomain(persisted, columnModelMapper.selectByTenantIdAndTableModelId(tableModel.getTenantId(), persisted.id()));
  }

  @Override
  public Optional<LowcodeTableModel> findByTenantIdAndCode(String tenantId, String code) {
    LowcodeTableModelRecord table = tableModelMapper.selectByTenantIdAndCode(tenantId, code);
    if (table == null) {
      return Optional.empty();
    }
    return Optional.of(toDomain(table, columnModelMapper.selectByTenantIdAndTableModelId(tenantId, table.id())));
  }

  @Override
  public Optional<LowcodeTableModel> findByTenantIdAndId(String tenantId, Long id) {
    LowcodeTableModelRecord table = tableModelMapper.selectByTenantIdAndId(tenantId, id);
    if (table == null) {
      return Optional.empty();
    }
    return Optional.of(toDomain(table, columnModelMapper.selectByTenantIdAndTableModelId(tenantId, table.id())));
  }

  @Override
  public List<LowcodeTableModel> findByTenantId(String tenantId) {
    return tableModelMapper.selectByTenantId(tenantId).stream()
        .map(table -> toDomain(table, columnModelMapper.selectByTenantIdAndTableModelId(tenantId, table.id())))
        .toList();
  }

  /**
   * 按租户边界重建字段模型。
   *
   * @param tenantId 租户业务编码
   * @param tableModelId 表模型主键
   * @param columns 当前表模型字段列表
   */
  private void rebuildColumns(String tenantId, Long tableModelId, List<LowcodeColumnModel> columns) {
    columnModelMapper.deleteByTenantIdAndTableModelId(tenantId, tableModelId);
    int sortOrder = 0;
    for (LowcodeColumnModel column : columns) {
      columnModelMapper.insert(toRecord(tableModelId, column, sortOrder));
      sortOrder++;
    }
  }

  private LowcodeTableModelRecord toRecord(LowcodeTableModel tableModel) {
    return new LowcodeTableModelRecord(
        tableModel.getId(),
        tableModel.getTenantId(),
        tableModel.getDataSourceId(),
        tableModel.getCode(),
        tableModel.getName(),
        tableModel.getTableName(),
        tableModel.getStatus().name());
  }

  private LowcodeColumnModelRecord toRecord(Long tableModelId, LowcodeColumnModel column, int sortOrder) {
    return new LowcodeColumnModelRecord(
        null,
        tableModelId,
        column.getCode(),
        column.getName(),
        column.getFieldType().name(),
        column.getLength(),
        column.getScale(),
        column.isRequired(),
        column.isPrimaryKey(),
        column.isAutoIncrement(),
        column.isListVisible(),
        column.isFormVisible(),
        column.isQueryable(),
        column.getDictCode(),
        sortOrder,
        column.getComment());
  }

  private LowcodeTableModel toDomain(LowcodeTableModelRecord table, List<LowcodeColumnModelRecord> columns) {
    return new LowcodeTableModel(
        table.id(),
        table.tenantId(),
        table.dataSourceId(),
        table.code(),
        table.name(),
        table.tableName(),
        LowcodeModelStatus.valueOf(table.status()),
        toDomainColumns(columns));
  }

  private List<LowcodeColumnModel> toDomainColumns(List<LowcodeColumnModelRecord> records) {
    List<LowcodeColumnModel> columns = new ArrayList<>();
    for (LowcodeColumnModelRecord record : records) {
      columns.add(LowcodeColumnModel.builder(record.fieldCode(), record.fieldName(), LowcodeFieldType.valueOf(record.fieldType()))
          .length(record.lengthValue())
          .scale(record.scaleValue())
          .required(Boolean.TRUE.equals(record.required()))
          .primaryKey(Boolean.TRUE.equals(record.primaryKey()))
          .autoIncrement(Boolean.TRUE.equals(record.autoIncrement()))
          .listVisible(Boolean.TRUE.equals(record.listVisible()))
          .formVisible(Boolean.TRUE.equals(record.formVisible()))
          .queryable(Boolean.TRUE.equals(record.queryable()))
          .dictCode(record.dictCode())
          .comment(record.comment())
          .build());
    }
    return columns;
  }
}
