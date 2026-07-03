/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.mapper;

import com.zhyc.search.domain.SearchIndexConfig;
import com.zhyc.search.domain.SearchQueryLog;
import com.zhyc.search.domain.SearchRebuildTask;
import com.zhyc.search.repository.SearchRuntimeQuery;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * 全文检索 MyBatis Mapper。
 */
@Mapper
public interface SearchMapper {

  /**
   * 查询索引配置列表。
   *
   * @param tenantId 租户业务编码
   * @param status 索引状态
   * @return 索引配置列表
   */
  @SelectProvider(type = SearchSqlProvider.class, method = "selectIndexConfigsForMapper")
  List<SearchIndexConfig> selectIndexConfigs(@Param("tenantId") String tenantId,
      @Param("status") String status);

  /**
   * 查询启用的索引配置。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 启用的索引配置
   */
  @SelectProvider(type = SearchSqlProvider.class, method = "selectEnabledIndexConfig")
  List<SearchIndexConfig> selectEnabledIndexConfig(@Param("tenantId") String tenantId,
      @Param("indexCode") String indexCode);

  /**
   * 保存或更新索引配置。
   *
   * @param config 索引配置
   */
  @InsertProvider(type = SearchSqlProvider.class, method = "upsertIndexConfig")
  void upsertIndexConfig(SearchIndexConfig config);

  /**
   * 新增索引重建任务。
   *
   * @param task 索引重建任务
   */
  @InsertProvider(type = SearchSqlProvider.class, method = "insertRebuildTask")
  void insertRebuildTask(SearchRebuildTask task);

  /**
   * 查询索引重建任务列表。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 索引重建任务列表
   */
  @SelectProvider(type = SearchSqlProvider.class, method = "selectRebuildTasksForMapper")
  List<SearchRebuildTask> selectRebuildTasks(@Param("tenantId") String tenantId,
      @Param("indexCode") String indexCode);

  /**
   * 新增检索查询日志。
   *
   * @param log 查询日志
   */
  @InsertProvider(type = SearchSqlProvider.class, method = "insertQueryLog")
  void insertQueryLog(SearchQueryLog log);

  /**
   * 执行运行时数据库检索。
   *
   * @param query 运行时查询参数
   * @return 命中文本列表
   */
  @SelectProvider(type = SearchSqlProvider.class, method = "selectSearchItems")
  List<String> selectSearchItems(SearchRuntimeQuery query);

  /**
   * 查询检索日志列表。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 查询日志列表
   */
  @SelectProvider(type = SearchSqlProvider.class, method = "selectQueryLogsForMapper")
  List<SearchQueryLog> selectQueryLogs(@Param("tenantId") String tenantId,
      @Param("indexCode") String indexCode);
}
