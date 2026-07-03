/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.visual.domain.VisualDataset;
import com.zhyc.visual.domain.VisualReport;
import com.zhyc.visual.domain.VisualScreen;
import com.zhyc.visual.repository.VisualRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认可视化报表业务服务实现。
 */
@Service
public class DefaultVisualService implements VisualService {

  /** 数据集允许使用的启停状态。 */
  private static final Set<String> ENABLED_STATUSES = Set.of("enabled", "disabled");
  /** 可视化报表允许使用的生命周期状态，保留 enabled/disabled 兼容历史数据。 */
  private static final Set<String> REPORT_STATUSES = Set.of("draft", "published", "offline", "enabled",
      "disabled");
  /** 可视化报表发布入口允许切换的生命周期状态。 */
  private static final Set<String> REPORT_LIFECYCLE_STATUSES = Set.of("draft", "published", "offline");
  /** 可视化大屏允许使用的生命周期状态。 */
  private static final Set<String> SCREEN_STATUSES = Set.of("draft", "published", "offline");
  /** 数据集 SQL 禁止出现的写操作或元数据操作关键字。 */
  private static final Set<String> UNSAFE_SQL_KEYWORDS = Set.of("insert", "update", "delete", "drop",
      "alter", "truncate", "create", "replace", "merge", "call", "exec", "grant", "revoke");

  /** 默认数据集预览行数。 */
  private static final int DEFAULT_PREVIEW_LIMIT = 20;

  /** 最大数据集预览行数，避免预览接口返回过大载荷。 */
  private static final int MAX_PREVIEW_LIMIT = 100;

  /** 未接入数据源执行器时的预览说明。 */
  private static final String DATASET_PREVIEW_MESSAGE = "数据源执行器未启用，当前返回字段解析和样例数据";

  /** 租户业务编码为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_VISUAL_TENANT_REQUIRED";

  /** 数据集编码为空错误码。 */
  private static final String ERROR_DATASET_CODE_REQUIRED = "ZHYC_VISUAL_DATASET_CODE_REQUIRED";

  /** 数据集名称为空错误码。 */
  private static final String ERROR_DATASET_NAME_REQUIRED = "ZHYC_VISUAL_DATASET_NAME_REQUIRED";

  /** 数据源编码为空错误码。 */
  private static final String ERROR_DATASOURCE_CODE_REQUIRED = "ZHYC_VISUAL_DATASOURCE_CODE_REQUIRED";

  /** 查询 SQL 为空错误码。 */
  private static final String ERROR_SQL_REQUIRED = "ZHYC_VISUAL_SQL_REQUIRED";

  /** 查询 SQL 不安全错误码。 */
  private static final String ERROR_SQL_UNSAFE = "ZHYC_VISUAL_DATASET_SQL_UNSAFE";

  /** 数据集状态不支持错误码。 */
  private static final String ERROR_DATASET_STATUS_UNSUPPORTED = "ZHYC_VISUAL_DATASET_STATUS_UNSUPPORTED";

  /** 数据集不存在错误码。 */
  private static final String ERROR_DATASET_NOT_FOUND = "ZHYC_VISUAL_DATASET_NOT_FOUND";

  /** 报表编码为空错误码。 */
  private static final String ERROR_REPORT_CODE_REQUIRED = "ZHYC_VISUAL_REPORT_CODE_REQUIRED";

  /** 报表名称为空错误码。 */
  private static final String ERROR_REPORT_NAME_REQUIRED = "ZHYC_VISUAL_REPORT_NAME_REQUIRED";

  /** 报表状态不支持错误码。 */
  private static final String ERROR_REPORT_STATUS_UNSUPPORTED = "ZHYC_VISUAL_REPORT_STATUS_UNSUPPORTED";

  /** 报表主键为空错误码。 */
  private static final String ERROR_REPORT_ID_REQUIRED = "ZHYC_VISUAL_REPORT_ID_REQUIRED";

  /** 已发布报表不存在错误码。 */
  private static final String ERROR_PUBLISHED_REPORT_NOT_FOUND = "ZHYC_VISUAL_PUBLISHED_REPORT_NOT_FOUND";

  /** 公开报表未引用数据集错误码。 */
  private static final String ERROR_PUBLISHED_REPORT_DATASET_DENIED =
      "ZHYC_VISUAL_PUBLISHED_REPORT_DATASET_DENIED";

  /** 大屏编码为空错误码。 */
  private static final String ERROR_SCREEN_CODE_REQUIRED = "ZHYC_VISUAL_SCREEN_CODE_REQUIRED";

  /** 大屏名称为空错误码。 */
  private static final String ERROR_SCREEN_NAME_REQUIRED = "ZHYC_VISUAL_SCREEN_NAME_REQUIRED";

  /** 大屏主键为空错误码。 */
  private static final String ERROR_SCREEN_ID_REQUIRED = "ZHYC_VISUAL_SCREEN_ID_REQUIRED";

  /** 大屏状态不支持错误码。 */
  private static final String ERROR_SCREEN_STATUS_UNSUPPORTED = "ZHYC_VISUAL_SCREEN_STATUS_UNSUPPORTED";

  /** 已发布大屏不存在错误码。 */
  private static final String ERROR_PUBLISHED_SCREEN_NOT_FOUND = "ZHYC_VISUAL_PUBLISHED_SCREEN_NOT_FOUND";

  /** 公开大屏未引用数据集错误码。 */
  private static final String ERROR_PUBLISHED_SCREEN_DATASET_DENIED =
      "ZHYC_VISUAL_PUBLISHED_SCREEN_DATASET_DENIED";

  /** 可视化报表仓储。 */
  private final VisualRepository visualRepository;

  /**
   * 创建可视化报表业务服务。
   *
   * @param visualRepository 可视化报表仓储
   */
  public DefaultVisualService(VisualRepository visualRepository) {
    this.visualRepository = Objects.requireNonNull(visualRepository, "可视化报表仓储不能为空");
  }

  @Override
  public List<VisualDatasetResponse> listDatasets(String tenantId, String status) {
    return visualRepository.findDatasets(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        trimToNull(status)).stream().map(this::toDatasetResponse).toList();
  }

  @Override
  @Transactional
  public void saveDataset(VisualDatasetSaveCommand command) {
    Objects.requireNonNull(command, "数据集保存命令不能为空");
    visualRepository.saveDataset(new VisualDataset(null, requireText(command.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), requireText(command.datasetCode(), ERROR_DATASET_CODE_REQUIRED, "数据集编码不能为空"),
        requireText(command.datasetName(), ERROR_DATASET_NAME_REQUIRED, "数据集名称不能为空"),
        requireText(command.datasourceCode(), ERROR_DATASOURCE_CODE_REQUIRED, "数据源编码不能为空"),
        requireReadOnlySql(command.sqlText()),
        requireStatus(defaultText(command.status(), "enabled"), ENABLED_STATUSES, "数据集",
            ERROR_DATASET_STATUS_UNSUPPORTED), null, null));
  }

  @Override
  public VisualDatasetPreviewResponse previewDataset(String tenantId, String datasetCode, Integer limit) {
    String normalizedTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String normalizedDatasetCode = requireText(datasetCode, ERROR_DATASET_CODE_REQUIRED, "数据集编码不能为空");
    VisualDataset dataset = visualRepository.findDatasetByCode(normalizedTenantId, normalizedDatasetCode)
        .orElseThrow(() -> new BusinessException(ERROR_DATASET_NOT_FOUND, "数据集不存在或已删除"));
    List<String> columns = parseDatasetColumns(dataset.getSqlText());
    return new VisualDatasetPreviewResponse(dataset.getDatasetCode(), columns,
        buildPreviewRows(columns, normalizePreviewLimit(limit)), false, DATASET_PREVIEW_MESSAGE);
  }

  @Override
  public VisualDatasetPreviewResponse previewPublishedScreenDataset(String tenantId, String screenCode,
      String datasetCode, Integer limit) {
    String normalizedTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String normalizedScreenCode = requireText(screenCode, ERROR_SCREEN_CODE_REQUIRED, "大屏编码不能为空");
    String normalizedDatasetCode = requireText(datasetCode, ERROR_DATASET_CODE_REQUIRED, "数据集编码不能为空");
    VisualScreen screen = visualRepository.findPublishedScreen(normalizedTenantId, normalizedScreenCode)
        .orElseThrow(() -> new BusinessException(ERROR_PUBLISHED_SCREEN_NOT_FOUND, "大屏未发布或不存在"));
    if (!screenReferencesDataset(screen.getLayoutJson(), normalizedDatasetCode)) {
      throw new BusinessException(ERROR_PUBLISHED_SCREEN_DATASET_DENIED, "公开大屏未引用该数据集");
    }
    return previewDataset(normalizedTenantId, normalizedDatasetCode, limit);
  }

  @Override
  public VisualDatasetPreviewResponse previewPublishedReportDataset(String tenantId, String reportCode,
      String datasetCode, Integer limit) {
    String normalizedTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String normalizedReportCode = requireText(reportCode, ERROR_REPORT_CODE_REQUIRED, "报表编码不能为空");
    String normalizedDatasetCode = requireText(datasetCode, ERROR_DATASET_CODE_REQUIRED, "数据集编码不能为空");
    VisualReport report = visualRepository.findPublishedReport(normalizedTenantId, normalizedReportCode)
        .orElseThrow(() -> new BusinessException(ERROR_PUBLISHED_REPORT_NOT_FOUND, "报表未发布或不存在"));
    if (!reportReferencesDataset(report, normalizedDatasetCode)) {
      throw new BusinessException(ERROR_PUBLISHED_REPORT_DATASET_DENIED, "公开报表未引用该数据集");
    }
    return previewDataset(normalizedTenantId, normalizedDatasetCode, limit);
  }

  @Override
  public List<VisualReportResponse> listReports(String tenantId, String status) {
    return visualRepository.findReports(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        trimToNull(status)).stream().map(this::toReportResponse).toList();
  }

  @Override
  @Transactional
  public void saveReport(VisualReportSaveCommand command) {
    Objects.requireNonNull(command, "报表保存命令不能为空");
    visualRepository.saveReport(new VisualReport(null, requireText(command.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), requireText(command.reportCode(), ERROR_REPORT_CODE_REQUIRED, "报表编码不能为空"),
        requireText(command.reportName(), ERROR_REPORT_NAME_REQUIRED, "报表名称不能为空"),
        requireText(command.datasetCode(), ERROR_DATASET_CODE_REQUIRED, "数据集编码不能为空"),
        defaultText(command.chartType(), "table"), defaultText(command.configJson(), "{}"),
        requireStatus(defaultText(command.status(), "draft"), REPORT_STATUSES, "报表",
            ERROR_REPORT_STATUS_UNSUPPORTED), null, null));
  }

  @Override
  public VisualReportResponse getPublishedReport(String tenantId, String reportCode) {
    String normalizedTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String normalizedReportCode = requireText(reportCode, ERROR_REPORT_CODE_REQUIRED, "报表编码不能为空");
    return visualRepository.findPublishedReport(normalizedTenantId, normalizedReportCode)
        .map(this::toReportResponse)
        .orElseThrow(() -> new BusinessException(ERROR_PUBLISHED_REPORT_NOT_FOUND, "报表未发布或不存在"));
  }

  @Override
  @Transactional
  public void updateReportStatus(String tenantId, Long id, String status) {
    visualRepository.updateReportStatus(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        requireId(id, ERROR_REPORT_ID_REQUIRED, "报表主键不能为空"),
        requireStatus(status, REPORT_LIFECYCLE_STATUSES, "报表", ERROR_REPORT_STATUS_UNSUPPORTED));
  }

  @Override
  public List<VisualScreenResponse> listScreens(String tenantId, String status) {
    return visualRepository.findScreens(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        trimToNull(status)).stream().map(this::toScreenResponse).toList();
  }

  @Override
  public VisualScreenResponse getPublishedScreen(String tenantId, String screenCode) {
    String normalizedTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String normalizedScreenCode = requireText(screenCode, ERROR_SCREEN_CODE_REQUIRED, "大屏编码不能为空");
    return visualRepository.findPublishedScreen(normalizedTenantId, normalizedScreenCode)
        .map(this::toScreenResponse)
        .orElseThrow(() -> new BusinessException(ERROR_PUBLISHED_SCREEN_NOT_FOUND, "大屏未发布或不存在"));
  }

  @Override
  @Transactional
  public void saveScreen(VisualScreenSaveCommand command) {
    Objects.requireNonNull(command, "大屏保存命令不能为空");
    visualRepository.saveScreen(new VisualScreen(null, requireText(command.tenantId(), ERROR_TENANT_REQUIRED,
        "租户业务编码不能为空"), requireText(command.screenCode(), ERROR_SCREEN_CODE_REQUIRED, "大屏编码不能为空"),
        requireText(command.screenName(), ERROR_SCREEN_NAME_REQUIRED, "大屏名称不能为空"),
        defaultText(command.layoutJson(), "[]"),
        requireStatus(defaultText(command.status(), "draft"), SCREEN_STATUSES, "大屏",
            ERROR_SCREEN_STATUS_UNSUPPORTED), null, null));
  }

  @Override
  @Transactional
  public void updateScreenStatus(String tenantId, Long id, String status) {
    visualRepository.updateScreenStatus(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        requireId(id, ERROR_SCREEN_ID_REQUIRED, "大屏主键不能为空"),
        requireStatus(status, SCREEN_STATUSES, "大屏", ERROR_SCREEN_STATUS_UNSUPPORTED));
  }

  private VisualDatasetResponse toDatasetResponse(VisualDataset dataset) {
    return new VisualDatasetResponse(dataset.getId(), dataset.getTenantId(), dataset.getDatasetCode(),
        dataset.getDatasetName(), dataset.getDatasourceCode(), dataset.getSqlText(), dataset.getStatus(),
        dataset.getCreatedAt(), dataset.getUpdatedAt());
  }

  private VisualReportResponse toReportResponse(VisualReport report) {
    return new VisualReportResponse(report.getId(), report.getTenantId(), report.getReportCode(),
        report.getReportName(), report.getDatasetCode(), report.getChartType(), report.getConfigJson(),
        report.getStatus(), report.getCreatedAt(), report.getUpdatedAt());
  }

  private VisualScreenResponse toScreenResponse(VisualScreen screen) {
    return new VisualScreenResponse(screen.getId(), screen.getTenantId(), screen.getScreenCode(),
        screen.getScreenName(), screen.getLayoutJson(), screen.getStatus(), screen.getCreatedAt(),
        screen.getUpdatedAt());
  }

  private Long requireId(Long value, String code, String message) {
    if (value == null || value <= 0) {
      throw new BusinessException(code, message);
    }
    return value;
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

  /**
   * 校验可视化资源状态必须属于首期支持的状态集合。
   *
   * @param value 原始状态值
   * @param allowedStatuses 允许状态集合
   * @param resourceName 可视化资源名称
   * @return 规范化后的状态值
   */
  private String requireStatus(String value, Set<String> allowedStatuses, String resourceName,
      String unsupportedCode) {
    String normalized = requireText(value, unsupportedCode, resourceName + "状态不能为空");
    if (!allowedStatuses.contains(normalized)) {
      throw new BusinessException(unsupportedCode, resourceName + "状态不支持: " + normalized);
    }
    return normalized;
  }

  /**
   * 校验数据集 SQL 只能是单条只读查询。
   *
   * <p>首期数据集执行器尚未引入 SQL 解析器，这里先做保守准入：只允许 SELECT/WITH 开头、
   * 禁止分号多语句、禁止常见写操作和权限操作关键字。</p>
   *
   * @param value 原始查询 SQL
   * @return 规范化后的查询 SQL
   */
  private String requireReadOnlySql(String value) {
    String normalized = requireText(value, ERROR_SQL_REQUIRED, "查询 SQL 不能为空");
    String lowerSql = normalized.toLowerCase();
    if ((!lowerSql.startsWith("select ") && !lowerSql.startsWith("with "))
        || lowerSql.contains(";")
        || UNSAFE_SQL_KEYWORDS.stream().anyMatch(keyword -> lowerSql.matches(".*\\b" + keyword + "\\b.*"))) {
      throw new BusinessException(ERROR_SQL_UNSAFE, "数据集 SQL 仅允许单条只读查询");
    }
    return normalized;
  }

  /**
   * 从只读查询 SQL 中解析报表设计器可绑定字段。
   *
   * <p>该解析仅用于首期设计器字段提示，不承担真实 SQL 执行安全校验职责。真实执行必须由后续公共
   * 数据源执行器统一完成。</p>
   *
   * @param sqlText 数据集查询 SQL
   * @return 可绑定字段列表
   */
  private List<String> parseDatasetColumns(String sqlText) {
    String sql = defaultText(sqlText, "");
    int selectIndex = sql.toLowerCase(Locale.ROOT).indexOf("select ");
    int fromIndex = findTopLevelFromIndex(sql);
    if (selectIndex < 0 || fromIndex <= selectIndex) {
      return List.of();
    }
    List<String> columns = new ArrayList<>();
    int columnIndex = 1;
    for (String expression : splitTopLevel(sql.substring(selectIndex + "select".length(), fromIndex))) {
      String column = resolveColumnName(expression, columnIndex);
      if (column != null && !columns.contains(column)) {
        columns.add(column);
      }
      columnIndex++;
    }
    return columns;
  }

  private int findTopLevelFromIndex(String sql) {
    String lowerSql = sql.toLowerCase(Locale.ROOT);
    int depth = 0;
    for (int index = 0; index < lowerSql.length(); index++) {
      char value = lowerSql.charAt(index);
      if (value == '(') {
        depth++;
      } else if (value == ')' && depth > 0) {
        depth--;
      } else if (depth == 0 && lowerSql.startsWith(" from ", index)) {
        return index;
      }
    }
    return -1;
  }

  private List<String> splitTopLevel(String value) {
    List<String> expressions = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    int depth = 0;
    for (int index = 0; index < value.length(); index++) {
      char item = value.charAt(index);
      if (item == '(') {
        depth++;
      } else if (item == ')' && depth > 0) {
        depth--;
      }
      if (item == ',' && depth == 0) {
        expressions.add(current.toString().trim());
        current.setLength(0);
      } else {
        current.append(item);
      }
    }
    String tail = current.toString().trim();
    if (!tail.isEmpty()) {
      expressions.add(tail);
    }
    return expressions;
  }

  private String resolveColumnName(String expression, int columnIndex) {
    String normalized = expression.trim();
    if (normalized.isEmpty() || "*".equals(normalized)) {
      return null;
    }
    String alias = resolveAlias(normalized);
    String candidate = alias == null ? normalized : alias;
    int dotIndex = candidate.lastIndexOf('.');
    if (dotIndex >= 0 && dotIndex < candidate.length() - 1) {
      candidate = candidate.substring(dotIndex + 1);
    }
    String cleaned = candidate.replace("`", "")
        .replace("\"", "")
        .replace("'", "")
        .replaceAll("[^A-Za-z0-9_]", "");
    return cleaned.isBlank() ? "column_" + columnIndex : cleaned;
  }

  private String resolveAlias(String expression) {
    String[] asParts = expression.split("(?i)\\s+as\\s+");
    if (asParts.length > 1) {
      return asParts[asParts.length - 1].trim();
    }
    String[] parts = expression.trim().split("\\s+");
    if (parts.length > 1) {
      String tail = parts[parts.length - 1];
      if (!tail.contains("(") && !tail.contains(")") && !"desc".equalsIgnoreCase(tail)
          && !"asc".equalsIgnoreCase(tail)) {
        return tail;
      }
    }
    return null;
  }

  private List<Map<String, Object>> buildPreviewRows(List<String> columns, int limit) {
    if (columns.isEmpty() || limit <= 0) {
      return List.of();
    }
    int rowCount = Math.min(limit, 5);
    List<Map<String, Object>> rows = new ArrayList<>(rowCount);
    for (int index = 1; index <= rowCount; index++) {
      Map<String, Object> row = new LinkedHashMap<>();
      for (String column : columns) {
        row.put(column, buildPreviewValue(column, index));
      }
      rows.add(row);
    }
    return rows;
  }

  private boolean screenReferencesDataset(String layoutJson, String datasetCode) {
    String normalized = defaultText(layoutJson, "");
    return normalized.contains("\"datasetCode\":\"" + datasetCode + "\"")
        || normalized.contains("\"datasetCode\": \"" + datasetCode + "\"");
  }

  private boolean reportReferencesDataset(VisualReport report, String datasetCode) {
    if (datasetCode.equals(report.getDatasetCode())) {
      return true;
    }
    String normalized = defaultText(report.getConfigJson(), "");
    return normalized.contains("\"datasetCode\":\"" + datasetCode + "\"")
        || normalized.contains("\"datasetCode\": \"" + datasetCode + "\"");
  }

  private Object buildPreviewValue(String column, int index) {
    String lowerColumn = column.toLowerCase(Locale.ROOT);
    if (lowerColumn.contains("amount") || lowerColumn.contains("price") || lowerColumn.contains("count")
        || lowerColumn.contains("total") || lowerColumn.contains("num") || lowerColumn.contains("rate")) {
      return index * 100;
    }
    if (lowerColumn.contains("date") || lowerColumn.contains("time")) {
      return "2026-06-%02d".formatted(index);
    }
    if (lowerColumn.contains("month")) {
      return "2026-%02d".formatted(index);
    }
    return column + "示例" + index;
  }

  private int normalizePreviewLimit(Integer limit) {
    if (limit == null) {
      return DEFAULT_PREVIEW_LIMIT;
    }
    if (limit <= 0) {
      return 0;
    }
    return Math.min(limit, MAX_PREVIEW_LIMIT);
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
