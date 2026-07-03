/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.schema;

import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodePhysicalTable;
import java.util.List;

/**
 * 低代码数据库结构访问网关。
 *
 * <p>隔离 JDBC 连接、密钥解析和数据库元数据读取细节，服务层只按租户内数据源发起受控结构读取或 DDL 执行。</p>
 */
public interface LowcodeDatabaseSchemaGateway {

  /**
   * 查询数据源中的物理表清单。
   *
   * @param dataSource 当前租户内数据源
   * @return 物理表清单
   */
  List<LowcodePhysicalTable> listTables(LowcodeDataSource dataSource);

  /**
   * 读取指定物理表结构。
   *
   * @param dataSource 当前租户内数据源
   * @param tableName 物理表名
   * @return 物理表结构
   */
  LowcodePhysicalTable readTable(LowcodeDataSource dataSource, String tableName);

  /**
   * 在指定数据源上执行低代码生成的 DDL。
   *
   * @param dataSource 当前租户内数据源
   * @param ddl 由方言生成器生成的 DDL
   */
  void executeDdl(LowcodeDataSource dataSource, String ddl);
}
