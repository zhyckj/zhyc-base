/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

/**
 * DDL 生成器扩展接口。
 */
public interface DdlGenerator {

  /**
   * 返回当前生成器支持的数据库方言名称。
   *
   * @return 数据库方言名称
   */
  String getDialectName();

  /**
   * 生成创建数据表 DDL。
   *
   * @param table 低代码数据表模型
   * @return 创建数据表 DDL
   */
  String generateCreateTable(LowcodeTable table);
}
