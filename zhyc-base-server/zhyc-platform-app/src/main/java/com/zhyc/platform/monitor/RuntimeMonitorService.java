/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import java.util.List;

/**
 * 平台运行监控服务。
 */
public interface RuntimeMonitorService {

  /**
   * 查询服务运行状态。
   *
   * @return 服务运行状态列表
   */
  List<RuntimeServiceStatus> listServiceStatus();

  /**
   * 查询数据源运行状态。
   *
   * @return 数据源运行状态列表
   */
  List<RuntimeDataSourceStatus> listDataSourceStatus();

  /**
   * 查询 SQL 执行效率监控记录。
   *
   * @param thresholdMs 慢 SQL 平均耗时阈值，单位毫秒
   * @param limit 最大返回记录数
   * @return SQL 执行效率监控记录列表
   */
  List<RuntimeSqlMonitorRecord> listSqlMonitorRecords(int thresholdMs, int limit);
}
