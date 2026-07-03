/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.search.domain.SearchIndexConfig;
import com.zhyc.search.domain.SearchQueryLog;
import com.zhyc.search.domain.SearchRebuildTask;
import com.zhyc.search.repository.SearchRepository;
import com.zhyc.search.repository.SearchRuntimeQuery;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认全文检索业务服务实现。
 */
@Service
public class DefaultSearchService implements SearchService {

  /** SQL 标识符白名单，避免运行时检索配置注入危险 SQL。 */
  private static final Pattern SQL_IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
  /** 默认返回命中记录数量上限。 */
  private static final int DEFAULT_RESULT_LIMIT = 20;

  /** 租户业务编码为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_SEARCH_TENANT_REQUIRED";

  /** 索引编码为空错误码。 */
  private static final String ERROR_INDEX_CODE_REQUIRED = "ZHYC_SEARCH_INDEX_CODE_REQUIRED";

  /** 索引名称为空错误码。 */
  private static final String ERROR_INDEX_NAME_REQUIRED = "ZHYC_SEARCH_INDEX_NAME_REQUIRED";

  /** 数据来源表为空错误码。 */
  private static final String ERROR_SOURCE_TABLE_REQUIRED = "ZHYC_SEARCH_SOURCE_TABLE_REQUIRED";

  /** 可检索字段为空错误码。 */
  private static final String ERROR_SEARCH_FIELDS_REQUIRED = "ZHYC_SEARCH_FIELDS_REQUIRED";

  /** 查询关键词为空错误码。 */
  private static final String ERROR_KEYWORD_REQUIRED = "ZHYC_SEARCH_KEYWORD_REQUIRED";

  /** 启用索引不存在错误码。 */
  private static final String ERROR_INDEX_NOT_ENABLED = "ZHYC_SEARCH_INDEX_NOT_ENABLED";

  /** 数据来源表名不合法错误码。 */
  private static final String ERROR_SOURCE_TABLE_INVALID = "ZHYC_SEARCH_SOURCE_TABLE_INVALID";

  /** 可检索字段不合法错误码。 */
  private static final String ERROR_SEARCH_FIELD_INVALID = "ZHYC_SEARCH_FIELD_INVALID";

  /** 全文检索仓储。 */
  private final SearchRepository searchRepository;

  /**
   * 创建全文检索业务服务。
   *
   * @param searchRepository 全文检索仓储
   */
  public DefaultSearchService(SearchRepository searchRepository) {
    this.searchRepository = Objects.requireNonNull(searchRepository, "全文检索仓储不能为空");
  }

  @Override
  public List<SearchIndexConfigResponse> listIndexConfigs(String tenantId, String status) {
    return searchRepository.findIndexConfigs(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        trimToNull(status)).stream().map(this::toConfigResponse).toList();
  }

  @Override
  @Transactional
  public void saveIndexConfig(SearchIndexConfigSaveCommand command) {
    Objects.requireNonNull(command, "全文检索索引配置保存命令不能为空");
    searchRepository.saveIndexConfig(new SearchIndexConfig(null,
        requireText(command.tenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        requireText(command.indexCode(), ERROR_INDEX_CODE_REQUIRED, "索引编码不能为空"),
        requireText(command.indexName(), ERROR_INDEX_NAME_REQUIRED, "索引名称不能为空"),
        requireText(command.sourceTable(), ERROR_SOURCE_TABLE_REQUIRED, "数据来源表不能为空"),
        requireText(command.searchFields(), ERROR_SEARCH_FIELDS_REQUIRED, "可检索字段不能为空"),
        trimToNull(command.filterFields()),
        defaultText(command.status(), "enabled"),
        trimToNull(command.remark()), null, null));
  }

  @Override
  @Transactional
  public void createRebuildTask(SearchRebuildTaskCommand command) {
    Objects.requireNonNull(command, "全文检索重建任务创建命令不能为空");
    searchRepository.createRebuildTask(new SearchRebuildTask(null,
        requireText(command.tenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        requireText(command.indexCode(), ERROR_INDEX_CODE_REQUIRED, "索引编码不能为空"),
        "pending", defaultText(command.triggerType(), "manual"), null, null, null, null, null));
  }

  @Override
  public List<SearchRebuildTaskResponse> listRebuildTasks(String tenantId, String indexCode) {
    return searchRepository.findRebuildTasks(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        trimToNull(indexCode)).stream().map(this::toTaskResponse).toList();
  }

  @Override
  @Transactional
  public SearchQueryResponse search(SearchQueryCommand command) {
    Objects.requireNonNull(command, "全文检索查询命令不能为空");
    String normalizedTenantId = requireText(command.tenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String normalizedIndexCode = requireText(command.indexCode(), ERROR_INDEX_CODE_REQUIRED, "索引编码不能为空");
    String normalizedKeyword = requireText(command.keyword(), ERROR_KEYWORD_REQUIRED, "查询关键词不能为空");
    long startedAt = System.nanoTime();
    try {
      SearchIndexConfig config = searchRepository.findEnabledIndexConfig(normalizedTenantId,
          normalizedIndexCode).orElseThrow(() -> new BusinessException(ERROR_INDEX_NOT_ENABLED,
              "全文检索索引未启用或不存在"));
      List<String> fields = parseSearchFields(config.getSearchFields());
      List<String> items = searchRepository.searchItems(buildRuntimeQuery(normalizedTenantId, config, fields,
          normalizedKeyword));
      long costMs = elapsedMillis(startedAt);
      searchRepository.recordQueryLog(new SearchQueryLog(null, normalizedTenantId, normalizedIndexCode,
          normalizedKeyword, items.size(), costMs, "success", null));
      return new SearchQueryResponse(normalizedIndexCode, normalizedKeyword, items.size(), items);
    } catch (RuntimeException ex) {
      long costMs = elapsedMillis(startedAt);
      searchRepository.recordQueryLog(new SearchQueryLog(null, normalizedTenantId, normalizedIndexCode,
          normalizedKeyword, 0, costMs, "failed", null));
      throw ex;
    }
  }

  @Override
  public List<SearchQueryLogResponse> listQueryLogs(String tenantId, String indexCode) {
    return searchRepository.findQueryLogs(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        trimToNull(indexCode)).stream().map(this::toLogResponse).toList();
  }

  private SearchIndexConfigResponse toConfigResponse(SearchIndexConfig config) {
    return new SearchIndexConfigResponse(config.getId(), config.getTenantId(), config.getIndexCode(),
        config.getIndexName(), config.getSourceTable(), config.getSearchFields(), config.getFilterFields(),
        config.getStatus(), config.getRemark(), config.getCreatedAt(), config.getUpdatedAt());
  }

  private SearchRebuildTaskResponse toTaskResponse(SearchRebuildTask task) {
    return new SearchRebuildTaskResponse(task.getId(), task.getTenantId(), task.getIndexCode(),
        task.getTaskStatus(), task.getTriggerType(), task.getStartedAt(), task.getFinishedAt(),
        task.getErrorMessage(), task.getCreatedAt(), task.getUpdatedAt());
  }

  private SearchQueryLogResponse toLogResponse(SearchQueryLog log) {
    return new SearchQueryLogResponse(log.getId(), log.getTenantId(), log.getIndexCode(),
        log.getKeyword(), log.getResultCount(), log.getCostMs(), log.getQueryStatus(), log.getCreatedAt());
  }

  /**
   * 构建运行时数据库检索查询。
   *
   * @param config 索引配置
   * @param fields 检索字段列表
   * @param keyword 查询关键词
   * @return 运行时数据库检索查询
   */
  private SearchRuntimeQuery buildRuntimeQuery(String tenantId, SearchIndexConfig config, List<String> fields,
      String keyword) {
    String sourceTable = requireSqlIdentifier(config.getSourceTable(), ERROR_SOURCE_TABLE_INVALID,
        "数据来源表名不合法");
    String selectExpression = fields.stream()
        .map(field -> "COALESCE(CAST(" + field + " AS CHAR), '')")
        .reduce((left, right) -> "CONCAT(" + left + ", ' | ', " + right + ")")
        .orElseThrow(() -> new BusinessException(ERROR_SEARCH_FIELDS_REQUIRED, "可检索字段不能为空"));
    String whereExpression = fields.stream()
        .map(field -> field + " LIKE CONCAT('%', #{keyword}, '%')")
        .reduce((left, right) -> left + " OR " + right)
        .orElseThrow(() -> new BusinessException(ERROR_SEARCH_FIELDS_REQUIRED, "可检索字段不能为空"));
    return new SearchRuntimeQuery(tenantId, sourceTable, selectExpression, whereExpression, keyword,
        DEFAULT_RESULT_LIMIT);
  }

  /**
   * 解析检索字段配置。
   *
   * @param searchFields 逗号分隔字段列表
   * @return 校验后的检索字段列表
   */
  private List<String> parseSearchFields(String searchFields) {
    return Arrays.stream(requireText(searchFields, ERROR_SEARCH_FIELDS_REQUIRED, "可检索字段不能为空").split(","))
        .map(field -> requireSqlIdentifier(field, ERROR_SEARCH_FIELD_INVALID, "可检索字段不合法"))
        .distinct()
        .toList();
  }

  /**
   * 校验 SQL 标识符。
   *
   * @param value SQL 标识符
   * @param message 错误提示
   * @return 规范化后的 SQL 标识符
   */
  private String requireSqlIdentifier(String value, String code, String message) {
    String normalized = requireText(value, code, message);
    if (!SQL_IDENTIFIER_PATTERN.matcher(normalized).matches()) {
      throw new BusinessException(code, message + ": " + normalized);
    }
    return normalized;
  }

  /**
   * 计算已经消耗的毫秒数。
   *
   * @param startedAt 开始纳秒时间
   * @return 消耗毫秒数
   */
  private long elapsedMillis(long startedAt) {
    return Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
  }

  private String defaultText(String value, String defaultValue) {
    String normalized = trimToNull(value);
    return normalized == null ? defaultValue : normalized;
  }

  private String requireText(String value, String code, String message) {
    String normalized = trimToNull(value);
    if (normalized == null) {
      throw new BusinessException(code, message);
    }
    return normalized;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
