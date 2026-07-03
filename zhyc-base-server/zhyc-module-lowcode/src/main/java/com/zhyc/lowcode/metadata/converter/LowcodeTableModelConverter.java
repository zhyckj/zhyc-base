/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.converter;

import com.zhyc.lowcode.db.LowcodeColumn;
import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.db.LowcodeTable;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 低代码表模型转换器。
 *
 * <p>用于把在线建模元数据转换为 DDL 生成器使用的数据库表结构。</p>
 */
@Component
public class LowcodeTableModelConverter {

  /**
   * 将低代码表模型转换为 DDL 数据表模型。
   *
   * @param tableModel 低代码表模型
   * @return DDL 数据表模型
   */
  public LowcodeTable toDdlTable(LowcodeTableModel tableModel) {
    Objects.requireNonNull(tableModel, "表模型不能为空");
    tableModel.validate();
    List<LowcodeColumn> columns = new ArrayList<>(tableModel.getColumns().stream()
        .map(this::toDdlColumn)
        .toList());
    appendTenantAndAuditColumns(columns);
    return new LowcodeTable(tableModel.getTableName(), tableModel.getName(), columns);
  }

  /**
   * 追加租户、审计、逻辑删除和乐观锁通用字段。
   *
   * <p>表模型已有同名字段时不重复追加，避免覆盖人工建模结果。</p>
   *
   * @param columns 已转换的业务字段
   */
  private void appendTenantAndAuditColumns(List<LowcodeColumn> columns) {
    int tenantInsertIndex = Math.min(primaryKeyTailIndex(columns) + 1, columns.size());
    if (!hasColumn(columns, "tenant_id")) {
      columns.add(tenantInsertIndex, LowcodeColumn.builder("tenant_id", LowcodeFieldType.STRING)
          .length(64)
          .nullable(false)
          .comment("租户业务编码，用于共享表模式下的数据隔离")
          .build());
    }
    appendIfAbsent(columns, LowcodeColumn.builder("created_by", LowcodeFieldType.LONG)
        .nullable(true)
        .comment("创建人用户ID")
        .build());
    appendIfAbsent(columns, LowcodeColumn.builder("created_at", LowcodeFieldType.DATETIME)
        .nullable(false)
        .comment("创建时间")
        .build());
    appendIfAbsent(columns, LowcodeColumn.builder("updated_by", LowcodeFieldType.LONG)
        .nullable(true)
        .comment("最后更新人用户ID")
        .build());
    appendIfAbsent(columns, LowcodeColumn.builder("updated_at", LowcodeFieldType.DATETIME)
        .nullable(false)
        .comment("最后更新时间")
        .build());
    appendIfAbsent(columns, LowcodeColumn.builder("deleted", LowcodeFieldType.BOOLEAN)
        .nullable(false)
        .comment("逻辑删除标识，0未删除，1已删除")
        .build());
    appendIfAbsent(columns, LowcodeColumn.builder("version", LowcodeFieldType.INTEGER)
        .nullable(false)
        .comment("乐观锁版本号")
        .build());
    appendIfAbsent(columns, LowcodeColumn.builder("remark", LowcodeFieldType.STRING)
        .length(500)
        .nullable(true)
        .comment("备注")
        .build());
  }

  private void appendIfAbsent(List<LowcodeColumn> columns, LowcodeColumn column) {
    if (!hasColumn(columns, column.getName())) {
      columns.add(column);
    }
  }

  private static int primaryKeyTailIndex(List<LowcodeColumn> columns) {
    int index = -1;
    for (int i = 0; i < columns.size(); i++) {
      if (columns.get(i).isPrimaryKey()) {
        index = i;
      }
    }
    return index;
  }

  private static boolean hasColumn(List<LowcodeColumn> columns, String name) {
    return columns.stream().anyMatch(column -> column.getName().equalsIgnoreCase(name));
  }

  private LowcodeColumn toDdlColumn(LowcodeColumnModel columnModel) {
    return LowcodeColumn.builder(columnModel.getCode(), columnModel.getFieldType())
        .length(columnModel.getLength())
        .scale(columnModel.getScale())
        .nullable(!columnModel.isRequired() && !columnModel.isPrimaryKey())
        .primaryKey(columnModel.isPrimaryKey())
        .autoIncrement(columnModel.isAutoIncrement())
        .comment(columnModel.getName())
        .build();
  }
}
