/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.tenant;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 租户 SQL 重写器。
 *
 * <p>该组件面向 MyBatis 拦截器使用，只对首期明确声明的租户业务表追加
 * {@code tenant_id} 条件，避免误改平台全局表或复杂非租户 SQL。</p>
 */
public class TenantSqlRewriter {

  /** SELECT 表名提取表达式。 */
  private static final Pattern SELECT_TABLE_PATTERN = Pattern.compile("(?is)\\bfrom\\s+([`\\w.]+)");

  /** UPDATE 表名提取表达式。 */
  private static final Pattern UPDATE_TABLE_PATTERN = Pattern.compile("(?is)^\\s*update\\s+([`\\w.]+)");

  /** DELETE 表名提取表达式。 */
  private static final Pattern DELETE_TABLE_PATTERN = Pattern.compile("(?is)^\\s*delete\\s+from\\s+([`\\w.]+)");

  /** 需要租户隔离的表名集合。 */
  private final Set<String> tenantTables;

  /**
   * 创建租户 SQL 重写器。
   *
   * @param tenantTables 需要追加租户条件的物理表名集合
   */
  public TenantSqlRewriter(Set<String> tenantTables) {
    this.tenantTables = Set.copyOf(Objects.requireNonNull(tenantTables, "租户表集合不能为空"));
  }

  /**
   * 返回首期已知租户业务表集合。
   *
   * @return 首期租户业务表集合
   */
  public static Set<String> firstReleaseTenantTables() {
    return Set.of(
        "sys_user", "sys_org", "sys_post", "sys_role", "sys_role_menu", "sys_role_data_scope",
        "sys_user_role", "sys_user_post", "sys_admin_scope", "sys_dict_type", "sys_dict_item",
        "sys_param", "sys_tenant_param", "sys_code_rule", "sys_audit_log", "sys_login_log",
        "sys_exception_log", "sys_permission_audit", "sys_access_restriction",
        "lc_data_source", "lc_table_model", "lc_column_model", "lc_table_relation", "lc_page_model",
        "lc_generation_record", "lc_generation_file",
        "wf_category", "wf_definition", "wf_form_binding", "wf_task", "wf_approval_record",
        "pur_request", "pur_request_item", "pur_order", "pur_order_item",
        "openapi_app", "openapi_api_key", "openapi_oauth_client", "openapi_api_permission",
        "openapi_catalog", "openapi_version", "openapi_signature_policy", "openapi_rate_limit_policy",
        "openapi_rate_limit_counter", "openapi_replay_nonce", "openapi_call_audit",
        "msg_template", "msg_message", "file_storage_config", "file_object", "file_preview",
        "job_task", "job_execution_log", "cms_channel", "cms_content",
        "visual_dataset", "visual_report", "visual_screen", "i18n_message", "search_index_config",
        "search_rebuild_task");
  }

  /**
   * 按当前租户重写 SQL。
   *
   * @param sql MyBatis 原始 SQL
   * @param tenantId 当前租户业务编码
   * @return 已追加租户条件的 SQL；无需处理时返回原 SQL
   */
  public String rewrite(String sql, String tenantId) {
    if (isBlank(sql) || isBlank(tenantId) || containsTenantCondition(sql)) {
      return sql;
    }
    String operation = firstToken(sql);
    String tableName = extractTableName(sql, operation);
    if (tableName == null || !tenantTables.contains(tableName)) {
      return sql;
    }
    return switch (operation) {
      case "select", "update", "delete" -> appendTenantCondition(sql, tenantId.trim());
      default -> sql;
    };
  }

  /**
   * 提取 SQL 首个操作关键字。
   *
   * @param sql 原始 SQL
   * @return 小写操作关键字
   */
  private String firstToken(String sql) {
    String trimmedSql = sql.stripLeading();
    int end = 0;
    while (end < trimmedSql.length() && Character.isLetter(trimmedSql.charAt(end))) {
      end++;
    }
    return trimmedSql.substring(0, end).toLowerCase(Locale.ROOT);
  }

  /**
   * 提取目标表名。
   *
   * @param sql 原始 SQL
   * @param operation SQL 操作类型
   * @return 规范化后的表名；无法识别时返回 {@code null}
   */
  private String extractTableName(String sql, String operation) {
    Pattern pattern = switch (operation) {
      case "select" -> SELECT_TABLE_PATTERN;
      case "update" -> UPDATE_TABLE_PATTERN;
      case "delete" -> DELETE_TABLE_PATTERN;
      default -> null;
    };
    if (pattern == null) {
      return null;
    }
    Matcher matcher = pattern.matcher(sql);
    if (!matcher.find()) {
      return null;
    }
    return normalizeTableName(matcher.group(1));
  }

  /**
   * 规范化物理表名。
   *
   * @param rawTableName SQL 中提取的表名
   * @return 小写物理表名
   */
  private String normalizeTableName(String rawTableName) {
    String normalized = rawTableName.replace("`", "").toLowerCase(Locale.ROOT);
    int schemaSeparator = normalized.lastIndexOf('.');
    return schemaSeparator >= 0 ? normalized.substring(schemaSeparator + 1) : normalized;
  }

  /**
   * 追加租户条件。
   *
   * @param sql 原始 SQL
   * @param tenantId 当前租户业务编码
   * @return 包含租户条件的 SQL
   */
  private String appendTenantCondition(String sql, String tenantId) {
    int tailIndex = findTailIndex(sql);
    String mainSql = tailIndex < 0 ? sql : sql.substring(0, tailIndex).stripTrailing();
    String tailSql = tailIndex < 0 ? "" : " " + sql.substring(tailIndex).stripLeading();
    int whereIndex = findTopLevelKeyword(mainSql, "where", 0);
    String tenantCondition = "tenant_id = '" + escapeSqlLiteral(tenantId) + "'";
    if (whereIndex >= 0) {
      int conditionStart = whereIndex + "where".length();
      return mainSql.substring(0, conditionStart) + " " + tenantCondition + " AND"
          + mainSql.substring(conditionStart) + tailSql;
    }
    return mainSql + " WHERE " + tenantCondition + tailSql;
  }

  /**
   * 查找 ORDER BY、GROUP BY、LIMIT 等尾部关键字位置。
   *
   * @param sql 原始 SQL
   * @return 尾部关键字起始位置；不存在时返回 {@code -1}
   */
  private int findTailIndex(String sql) {
    int result = -1;
    for (String keyword : new String[] {"group by", "having", "order by", "limit", "offset", "fetch", "for update"}) {
      int index = findTopLevelKeyword(sql, keyword, 0);
      if (index >= 0 && (result < 0 || index < result)) {
        result = index;
      }
    }
    return result;
  }

  /**
   * 查找顶层 SQL 关键字，忽略字符串字面量和括号内部内容。
   *
   * @param sql 原始 SQL
   * @param keyword 需要查找的小写关键字
   * @param startIndex 起始位置
   * @return 关键字位置；不存在时返回 {@code -1}
   */
  private int findTopLevelKeyword(String sql, String keyword, int startIndex) {
    String lowerSql = sql.toLowerCase(Locale.ROOT);
    int depth = 0;
    boolean inQuote = false;
    for (int index = startIndex; index <= lowerSql.length() - keyword.length(); index++) {
      char current = lowerSql.charAt(index);
      if (current == '\'') {
        inQuote = !inQuote;
        continue;
      }
      if (inQuote) {
        continue;
      }
      if (current == '(') {
        depth++;
        continue;
      }
      if (current == ')' && depth > 0) {
        depth--;
        continue;
      }
      if (depth == 0 && lowerSql.startsWith(keyword, index) && isKeywordBoundary(lowerSql, index, keyword.length())) {
        return index;
      }
    }
    return -1;
  }

  /**
   * 判断命中位置是否是完整 SQL 关键字边界。
   *
   * @param lowerSql 小写 SQL
   * @param start 关键字起始位置
   * @param length 关键字长度
   * @return 是完整关键字时返回 {@code true}
   */
  private boolean isKeywordBoundary(String lowerSql, int start, int length) {
    int before = start - 1;
    int after = start + length;
    return (before < 0 || !isIdentifierChar(lowerSql.charAt(before)))
        && (after >= lowerSql.length() || !isIdentifierChar(lowerSql.charAt(after)));
  }

  /**
   * 判断字符是否为 SQL 标识符字符。
   *
   * @param value 待判断字符
   * @return 是标识符字符时返回 {@code true}
   */
  private boolean isIdentifierChar(char value) {
    return Character.isLetterOrDigit(value) || value == '_';
  }

  /**
   * 判断 SQL 是否已经包含租户条件。
   *
   * @param sql 原始 SQL
   * @return 已包含租户字段时返回 {@code true}
   */
  private boolean containsTenantCondition(String sql) {
    return Pattern.compile("(?i)\\btenant_id\\b").matcher(sql).find();
  }

  /**
   * 转义 SQL 字符串字面量。
   *
   * @param value 原始值
   * @return 可放入单引号字面量的值
   */
  private String escapeSqlLiteral(String value) {
    return value.replace("'", "''");
  }

  /**
   * 判断文本是否为空。
   *
   * @param value 待判断文本
   * @return 为空时返回 {@code true}
   */
  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
