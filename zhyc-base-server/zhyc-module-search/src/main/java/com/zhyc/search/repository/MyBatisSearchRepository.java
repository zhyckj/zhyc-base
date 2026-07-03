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
import com.zhyc.search.mapper.SearchMapper;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的全文检索仓储实现。
 */
@Repository
public class MyBatisSearchRepository implements SearchRepository {

  /** 全文检索 Mapper。 */
  private final SearchMapper searchMapper;

  /**
   * 创建全文检索仓储。
   *
   * @param searchMapper 全文检索 Mapper
   */
  public MyBatisSearchRepository(SearchMapper searchMapper) {
    this.searchMapper = Objects.requireNonNull(searchMapper, "全文检索 Mapper 不能为空");
  }

  @Override
  public List<SearchIndexConfig> findIndexConfigs(String tenantId, String status) {
    return searchMapper.selectIndexConfigs(tenantId, status);
  }

  @Override
  public Optional<SearchIndexConfig> findEnabledIndexConfig(String tenantId, String indexCode) {
    return searchMapper.selectEnabledIndexConfig(tenantId, indexCode).stream().findFirst();
  }

  @Override
  public void saveIndexConfig(SearchIndexConfig config) {
    searchMapper.upsertIndexConfig(config);
  }

  @Override
  public void createRebuildTask(SearchRebuildTask task) {
    searchMapper.insertRebuildTask(task);
  }

  @Override
  public List<SearchRebuildTask> findRebuildTasks(String tenantId, String indexCode) {
    return searchMapper.selectRebuildTasks(tenantId, indexCode);
  }

  @Override
  public void recordQueryLog(SearchQueryLog log) {
    searchMapper.insertQueryLog(log);
  }

  @Override
  public List<String> searchItems(SearchRuntimeQuery query) {
    return searchMapper.selectSearchItems(query);
  }

  @Override
  public List<SearchQueryLog> findQueryLogs(String tenantId, String indexCode) {
    return searchMapper.selectQueryLogs(tenantId, indexCode);
  }
}
