/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import com.zhyc.common.exception.BusinessException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 默认低代码数据库方言服务实现。
 *
 * <p>按数据库方言编码向注册中心获取能力实例，并委托具体实现完成实际能力调用。</p>
 */
public class DefaultLowcodeDbDialectService implements LowcodeDbDialectService {

  /** 建表模型缺失错误码。 */
  private static final String ERROR_TABLE_REQUIRED = "ZHYC_LOWCODE_DIALECT_TABLE_REQUIRED";
  /** 字段模型缺失错误码。 */
  private static final String ERROR_COLUMN_REQUIRED = "ZHYC_LOWCODE_DIALECT_COLUMN_REQUIRED";
  /** 分页 SQL 缺失错误码。 */
  private static final String ERROR_SQL_REQUIRED = "ZHYC_LOWCODE_DIALECT_SQL_REQUIRED";
  /** 数据库方言编码缺失错误码。 */
  private static final String ERROR_DIALECT_CODE_REQUIRED = "ZHYC_LOWCODE_DIALECT_CODE_REQUIRED";
  /** DDL 方言不支持错误码。 */
  private static final String ERROR_DDL_UNSUPPORTED = "ZHYC_LOWCODE_DIALECT_DDL_UNSUPPORTED";
  /** 字段类型映射方言不支持错误码。 */
  private static final String ERROR_FIELD_TYPE_UNSUPPORTED = "ZHYC_LOWCODE_DIALECT_FIELD_TYPE_UNSUPPORTED";
  /** 分页方言不支持错误码。 */
  private static final String ERROR_PAGINATION_UNSUPPORTED = "ZHYC_LOWCODE_DIALECT_PAGINATION_UNSUPPORTED";

  /** 数据库方言能力注册中心。 */
  private final LowcodeDbDialectRegistry dialectRegistry;

  /**
   * 创建默认方言服务。
   *
   * @param dialectRegistry 数据库方言能力注册中心
   */
  public DefaultLowcodeDbDialectService(LowcodeDbDialectRegistry dialectRegistry) {
    this.dialectRegistry = Objects.requireNonNull(dialectRegistry, "数据库方言能力注册中心不能为空");
  }

  /**
   * 生成建表 SQL。
   *
   * @param dialectCode 数据库方言编码
   * @param table 低代码数据库表模型
   * @return 建表 SQL
   */
  @Override
  public String generateCreateTable(String dialectCode, LowcodeTable table) {
    table = requireTable(table);
    return resolveDdlGenerator(requireDialectCode(dialectCode)).generateCreateTable(table);
  }

  /**
   * 映射统一字段模型到目标数据库字段类型。
   *
   * @param dialectCode 数据库方言编码
   * @param column 低代码字段模型
   * @return 目标数据库字段类型定义
   */
  @Override
  public String mapFieldType(String dialectCode, LowcodeColumn column) {
    column = requireColumn(column);
    return resolveFieldTypeMapper(requireDialectCode(dialectCode)).toDatabaseType(column);
  }

  /**
   * 为 SQL 追加目标数据库分页语法。
   *
   * @param dialectCode 数据库方言编码
   * @param sql 原始 SQL
   * @param offset 分页偏移量，从 0 开始
   * @param pageSize 每页返回条数
   * @return 追加分页后的 SQL
   */
  @Override
  public String applyPagination(String dialectCode, String sql, long offset, long pageSize) {
    if (sql == null || sql.trim().isEmpty()) {
      throw new BusinessException(ERROR_SQL_REQUIRED, "SQL 不能为空");
    }
    return resolvePaginationDialect(requireDialectCode(dialectCode)).applyPagination(sql, offset, pageSize);
  }

  /**
   * 查询可用 DDL 生成器方言编码清单。
   *
   * @return DDL 方言编码清单
   */
  @Override
  public List<String> listDdlDialectCodes() {
    return dialectRegistry.listDdlDialectCodes();
  }

  /**
   * 查询可用字段类型映射方言编码清单。
   *
   * @return 字段类型映射方言编码清单
   */
  @Override
  public List<String> listFieldTypeDialectCodes() {
    return dialectRegistry.listFieldTypeDialectCodes();
  }

  /**
   * 查询可用分页方言编码清单。
   *
   * @return 分页方言编码清单
   */
  @Override
  public List<String> listPaginationDialectCodes() {
    return dialectRegistry.listPaginationDialectCodes();
  }

  /**
   * 校验建表模型不能为空。
   *
   * @param table 低代码数据库表模型
   * @return 非空低代码数据库表模型
   */
  private static LowcodeTable requireTable(LowcodeTable table) {
    if (table == null) {
      throw new BusinessException(ERROR_TABLE_REQUIRED, "建表模型不能为空");
    }
    return table;
  }

  /**
   * 校验字段模型不能为空。
   *
   * @param column 低代码字段模型
   * @return 非空低代码字段模型
   */
  private static LowcodeColumn requireColumn(LowcodeColumn column) {
    if (column == null) {
      throw new BusinessException(ERROR_COLUMN_REQUIRED, "字段模型不能为空");
    }
    return column;
  }

  /**
   * 校验并归一化数据库方言编码。
   *
   * @param dialectCode 数据库方言编码
   * @return 去除空白并转小写后的数据库方言编码
   */
  private static String requireDialectCode(String dialectCode) {
    if (dialectCode == null || dialectCode.trim().isEmpty()) {
      throw new BusinessException(ERROR_DIALECT_CODE_REQUIRED, "数据库方言编码不能为空");
    }
    return dialectCode.trim().toLowerCase(Locale.ROOT);
  }

  /**
   * 解析 DDL 生成器，并把注册中心路由失败转换为稳定业务错误码。
   *
   * @param dialectCode 已归一化的数据库方言编码
   * @return DDL 生成器
   */
  private DdlGenerator resolveDdlGenerator(String dialectCode) {
    try {
      return dialectRegistry.getDdlGenerator(dialectCode);
    } catch (IllegalArgumentException ex) {
      throw new BusinessException(ERROR_DDL_UNSUPPORTED, ex.getMessage());
    }
  }

  /**
   * 解析字段类型映射器，并把注册中心路由失败转换为稳定业务错误码。
   *
   * @param dialectCode 已归一化的数据库方言编码
   * @return 字段类型映射器
   */
  private FieldTypeMapper resolveFieldTypeMapper(String dialectCode) {
    try {
      return dialectRegistry.getFieldTypeMapper(dialectCode);
    } catch (IllegalArgumentException ex) {
      throw new BusinessException(ERROR_FIELD_TYPE_UNSUPPORTED, ex.getMessage());
    }
  }

  /**
   * 解析分页方言，并把注册中心路由失败转换为稳定业务错误码。
   *
   * @param dialectCode 已归一化的数据库方言编码
   * @return 分页方言
   */
  private PaginationDialect resolvePaginationDialect(String dialectCode) {
    try {
      return dialectRegistry.getPaginationDialect(dialectCode);
    } catch (IllegalArgumentException ex) {
      throw new BusinessException(ERROR_PAGINATION_UNSUPPORTED, ex.getMessage());
    }
  }
}
