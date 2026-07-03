/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import java.util.List;

/**
 * 低代码数据库方言服务。
 *
 * <p>统一对外暴露方言能力的调用入口：字段类型映射、DDL 生成、分页语句追加。
 * 该服务只负责路由与参数校验，不直接关心方言实现细节。</p>
 */
public interface LowcodeDbDialectService {

  /**
   * 生成建表 SQL。
   *
   * @param dialectCode 数据库方言编码
   * @param table 低代码数据库表模型
   * @return 建表 SQL
   */
  String generateCreateTable(String dialectCode, LowcodeTable table);

  /**
   * 映射统一字段模型到目标数据库字段类型。
   *
   * @param dialectCode 数据库方言编码
   * @param column 低代码字段模型
   * @return 目标数据库字段类型定义
   */
  String mapFieldType(String dialectCode, LowcodeColumn column);

  /**
   * 为 SQL 追加目标数据库分页语法。
   *
   * @param dialectCode 数据库方言编码
   * @param sql 原始 SQL
   * @param offset 分页偏移量，从 0 开始
   * @param pageSize 每页返回条数
   * @return 追加分页后的 SQL
   */
  String applyPagination(String dialectCode, String sql, long offset, long pageSize);

  /**
   * 查询可用 DDL 生成器方言编码清单。
   *
   * @return DDL 方言编码清单
   */
  List<String> listDdlDialectCodes();

  /**
   * 查询可用字段类型映射方言编码清单。
   *
   * @return 字段类型映射方言编码清单
   */
  List<String> listFieldTypeDialectCodes();

  /**
   * 查询可用分页方言编码清单。
   *
   * @return 分页方言编码清单
   */
  List<String> listPaginationDialectCodes();
}
