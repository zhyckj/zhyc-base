/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.repository;

import com.zhyc.search.domain.SearchIndexConfig;
import com.zhyc.search.domain.SearchQueryLog;
import com.zhyc.search.domain.SearchRebuildTask;
import java.util.List;
import java.util.Optional;

/**
 * 全文检索仓储接口。
 */
public interface SearchRepository {

  /**
   * 查询索引配置列表。
   *
   * @param tenantId 租户业务编码
   * @param status 索引状态
   * @return 索引配置列表
   */
  List<SearchIndexConfig> findIndexConfigs(String tenantId, String status);

  /**
   * 查询启用的索引配置。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 启用的索引配置
   */
  Optional<SearchIndexConfig> findEnabledIndexConfig(String tenantId, String indexCode);

  /**
   * 保存索引配置。
   *
   * @param config 索引配置
   */
  void saveIndexConfig(SearchIndexConfig config);

  /**
   * 创建索引重建任务。
   *
   * @param task 重建任务
   */
  void createRebuildTask(SearchRebuildTask task);

  /**
   * 查询索引重建任务列表。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 重建任务列表
   */
  List<SearchRebuildTask> findRebuildTasks(String tenantId, String indexCode);

  /**
   * 记录检索查询日志。
   *
   * @param log 查询日志
   */
  void recordQueryLog(SearchQueryLog log);

  /**
   * 执行运行时数据库检索。
   *
   * @param query 运行时查询参数
   * @return 命中文本列表
   */
  List<String> searchItems(SearchRuntimeQuery query);

  /**
   * 查询检索日志列表。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 查询日志列表
   */
  List<SearchQueryLog> findQueryLogs(String tenantId, String indexCode);
}
