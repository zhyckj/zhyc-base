/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.service;

import java.util.List;

/**
 * 全文检索业务服务。
 */
public interface SearchService {

  /**
   * 查询索引配置列表。
   *
   * @param tenantId 租户业务编码
   * @param status 索引状态
   * @return 索引配置列表
   */
  List<SearchIndexConfigResponse> listIndexConfigs(String tenantId, String status);

  /**
   * 保存索引配置。
   *
   * @param command 索引配置保存命令
   */
  void saveIndexConfig(SearchIndexConfigSaveCommand command);

  /**
   * 创建索引重建任务。
   *
   * @param command 重建任务创建命令
   */
  void createRebuildTask(SearchRebuildTaskCommand command);

  /**
   * 查询索引重建任务列表。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 重建任务列表
   */
  List<SearchRebuildTaskResponse> listRebuildTasks(String tenantId, String indexCode);

  /**
   * 执行全文检索。
   *
   * @param command 查询命令
   * @return 查询响应
   */
  SearchQueryResponse search(SearchQueryCommand command);

  /**
   * 查询检索日志列表。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 检索日志列表
   */
  List<SearchQueryLogResponse> listQueryLogs(String tenantId, String indexCode);
}
