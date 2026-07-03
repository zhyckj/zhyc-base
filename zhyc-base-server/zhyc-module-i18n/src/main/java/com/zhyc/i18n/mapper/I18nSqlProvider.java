/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.mapper;

import java.util.Map;

/**
 * 国际化词条 SQL Provider。
 */
public class I18nSqlProvider {

  /**
   * 生成测试可读的词条查询 SQL。
   *
   * @param tenantId 租户业务编码
   * @param locale 语言标识
   * @param status 词条状态
   * @return 词条查询 SQL
   */
  public String selectMessages(String tenantId, String locale, String status) {
    return selectMessagesSql(locale, status);
  }

  /**
   * 生成 MyBatis 词条查询 SQL。
   *
   * @param params 查询参数
   * @return 词条查询 SQL
   */
  public String selectMessagesForMapper(Map<String, Object> params) {
    return selectMessagesSql(params.get("locale"), params.get("status"));
  }

  /**
   * 生成启用词条查询 SQL。
   *
   * @return 启用词条查询 SQL
   */
  public String selectEnabledMessage() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               locale,
               message_key AS messageKey,
               message_value AS messageValue,
               message_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM i18n_message
        WHERE tenant_id = #{tenantId}
          AND locale = #{locale}
          AND message_key = #{messageKey}
          AND message_status = 'enabled'
          AND deleted = 0
        """;
  }

  /**
   * 生成词条保存 SQL。
   *
   * @return 词条保存 SQL
   */
  public String upsertMessage() {
    return """
        INSERT INTO i18n_message (
            tenant_id, locale, message_key, message_value, message_status
        ) VALUES (
            #{tenantId}, #{locale}, #{messageKey}, #{messageValue}, #{status}
        )
        ON DUPLICATE KEY UPDATE
            message_value = VALUES(message_value),
            message_status = VALUES(message_status),
            updated_at = CURRENT_TIMESTAMP,
            deleted = 0
        """;
  }

  private String selectMessagesSql(Object locale, Object status) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               locale,
               message_key AS messageKey,
               message_value AS messageValue,
               message_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM i18n_message
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        """);
    if (locale != null && !locale.toString().isBlank()) {
      sql.append("  AND locale = #{locale}\n");
    }
    if (status != null && !status.toString().isBlank()) {
      sql.append("  AND message_status = #{status}\n");
    }
    sql.append("ORDER BY locale ASC, message_key ASC\n");
    return sql.toString();
  }
}
