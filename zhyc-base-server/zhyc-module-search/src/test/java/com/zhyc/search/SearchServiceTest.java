/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.search.domain.SearchIndexConfig;
import com.zhyc.search.domain.SearchQueryLog;
import com.zhyc.search.domain.SearchRebuildTask;
import com.zhyc.search.repository.SearchRepository;
import com.zhyc.search.repository.SearchRuntimeQuery;
import com.zhyc.search.service.DefaultSearchService;
import com.zhyc.search.service.SearchIndexConfigResponse;
import com.zhyc.search.service.SearchIndexConfigSaveCommand;
import com.zhyc.search.service.SearchQueryCommand;
import com.zhyc.search.service.SearchRebuildTaskCommand;
import com.zhyc.search.service.SearchService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 全文检索业务服务测试。
 */
class SearchServiceTest {

  /**
   * 验证索引配置保存会裁剪关键字段并默认启用状态。
   */
  @Test
  void shouldSaveIndexConfigWithNormalizedFields() {
    RecordingRepository repository = new RecordingRepository();
    SearchService service = new DefaultSearchService(repository);

    service.saveIndexConfig(new SearchIndexConfigSaveCommand(" tenant_a ", " cms_content ",
        " 内容索引 ", " cms_content ", " title,content ", " channel_id ", null, " 文章检索 "));

    assertEquals("tenant_a", repository.lastConfig.getTenantId());
    assertEquals("cms_content", repository.lastConfig.getIndexCode());
    assertEquals("内容索引", repository.lastConfig.getIndexName());
    assertEquals("cms_content", repository.lastConfig.getSourceTable());
    assertEquals("title,content", repository.lastConfig.getSearchFields());
    assertEquals("channel_id", repository.lastConfig.getFilterFields());
    assertEquals("enabled", repository.lastConfig.getStatus());
  }

  /**
   * 验证索引配置列表必须按租户和状态过滤。
   */
  @Test
  void shouldListIndexConfigsByTenantAndStatus() {
    RecordingRepository repository = new RecordingRepository();
    SearchService service = new DefaultSearchService(repository);

    List<SearchIndexConfigResponse> configs = service.listIndexConfigs(" tenant_a ", " enabled ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("enabled", repository.lastStatus);
    assertEquals("cms_content", configs.getFirst().indexCode());
  }

  /**
   * 验证创建重建任务时默认待执行状态和手动触发类型。
   */
  @Test
  void shouldCreatePendingRebuildTask() {
    RecordingRepository repository = new RecordingRepository();
    SearchService service = new DefaultSearchService(repository);

    service.createRebuildTask(new SearchRebuildTaskCommand(" tenant_a ", " cms_content ", null));

    assertEquals("tenant_a", repository.lastTask.getTenantId());
    assertEquals("cms_content", repository.lastTask.getIndexCode());
    assertEquals("pending", repository.lastTask.getTaskStatus());
    assertEquals("manual", repository.lastTask.getTriggerType());
  }

  /**
   * 验证搜索请求会读取启用索引配置、执行数据库检索并记录结果数量。
   */
  @Test
  void shouldRunDatabaseSearchAndRecordQueryLogWhenSearch() {
    RecordingRepository repository = new RecordingRepository();
    SearchService service = new DefaultSearchService(repository);

    var response = service.search(new SearchQueryCommand(" tenant_a ", " cms_content ", " 采购制度 "));

    assertEquals("tenant_a", repository.lastLog.getTenantId());
    assertEquals("cms_content", repository.lastLog.getIndexCode());
    assertEquals("采购制度", repository.lastLog.getKeyword());
    assertEquals(1, repository.lastLog.getResultCount());
    assertEquals("success", repository.lastLog.getQueryStatus());
    assertEquals(1, response.total());
    assertEquals("采购制度 | 管理办法", response.items().getFirst());
    assertEquals("tenant_a", repository.lastRuntimeQuery.tenantId());
    assertTrue(repository.lastRuntimeQuery.whereExpression().contains("title LIKE"));
    assertTrue(repository.lastRuntimeQuery.whereExpression().contains("content LIKE"));
  }

  /**
   * 验证不合法的检索字段会被拒绝并记录失败日志。
   */
  @Test
  void shouldRejectUnsafeSearchFieldAndRecordFailedLog() {
    RecordingRepository repository = new RecordingRepository();
    repository.config = new SearchIndexConfig(1L, "tenant_a", "cms_content", "内容索引",
        "cms_content", "title;drop", null, "enabled", "文章检索",
        LocalDateTime.now(), LocalDateTime.now());
    SearchService service = new DefaultSearchService(repository);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.search(new SearchQueryCommand("tenant_a", "cms_content", "采购制度")));

    assertEquals("ZHYC_SEARCH_FIELD_INVALID", exception.getCode());
    assertTrue(exception.getMessage().contains("可检索字段不合法"));
    assertEquals("failed", repository.lastLog.getQueryStatus());
  }

  /**
   * 测试用全文检索仓储。
   */
  private static class RecordingRepository implements SearchRepository {

    /** 最近一次保存的索引配置。 */
    private SearchIndexConfig lastConfig;
    /** 最近一次创建的重建任务。 */
    private SearchRebuildTask lastTask;
    /** 最近一次记录的搜索日志。 */
    private SearchQueryLog lastLog;
    /** 最近一次运行时检索查询。 */
    private SearchRuntimeQuery lastRuntimeQuery;
    /** 测试索引配置。 */
    private SearchIndexConfig config = new SearchIndexConfig(1L, "tenant_a", "cms_content",
        "内容索引", "cms_content", "title,content", "channel_id", "enabled", "文章检索",
        LocalDateTime.now(), LocalDateTime.now());
    /** 最近一次查询租户。 */
    private String lastTenantId;
    /** 最近一次查询状态。 */
    private String lastStatus;

    @Override
    public List<SearchIndexConfig> findIndexConfigs(String tenantId, String status) {
      lastTenantId = tenantId;
      lastStatus = status;
      return List.of(config);
    }

    @Override
    public Optional<SearchIndexConfig> findEnabledIndexConfig(String tenantId, String indexCode) {
      lastTenantId = tenantId;
      return Optional.of(config);
    }

    @Override
    public void saveIndexConfig(SearchIndexConfig config) {
      lastConfig = config;
    }

    @Override
    public void createRebuildTask(SearchRebuildTask task) {
      lastTask = task;
    }

    @Override
    public List<SearchRebuildTask> findRebuildTasks(String tenantId, String indexCode) {
      lastTenantId = tenantId;
      return List.of();
    }

    @Override
    public void recordQueryLog(SearchQueryLog log) {
      lastLog = log;
    }

    @Override
    public List<String> searchItems(SearchRuntimeQuery query) {
      lastRuntimeQuery = query;
      return List.of("采购制度 | 管理办法");
    }

    @Override
    public List<SearchQueryLog> findQueryLogs(String tenantId, String indexCode) {
      lastTenantId = tenantId;
      return List.of();
    }
  }
}
