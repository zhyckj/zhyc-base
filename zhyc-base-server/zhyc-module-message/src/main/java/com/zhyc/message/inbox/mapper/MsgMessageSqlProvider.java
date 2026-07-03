/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.mapper;

import java.util.Map;

/**
 * 站内消息 SQL Provider。
 */
public class MsgMessageSqlProvider {

  /**
   * 生成消息数量统计 SQL。
   *
   * @param params 查询参数
   * @return 消息数量统计 SQL
   */
  public String countByQuery(Map<String, Object> params) {
    return baseQuerySql("SELECT COUNT(1)", params, false);
  }

  /**
   * 生成消息分页查询 SQL。
   *
   * @param params 查询参数
   * @return 消息分页查询 SQL
   */
  public String selectPageByQuery(Map<String, Object> params) {
    return baseQuerySql("""
        SELECT id,
               tenant_id AS tenantId,
               message_code AS messageCode,
               receiver_id AS receiverId,
               receiver_name AS receiverName,
               message_type AS messageType,
               title,
               content,
               read_flag AS readFlag,
               read_at AS readAt,
               created_at AS createdAt
        """, params, true);
  }

  /**
   * 生成站内消息写入 SQL。
   *
   * @return 站内消息写入 SQL
   */
  public String insert() {
    return """
        INSERT INTO msg_message (
            tenant_id, message_code, receiver_id, receiver_name, message_type, title, content, read_flag
        ) VALUES (
            #{tenantId}, #{messageCode}, #{receiverId}, #{receiverName}, #{messageType}, #{title}, #{content}, #{readFlag}
        )
        """;
  }

  /**
   * 生成标记已读 SQL。
   *
   * @return 标记已读 SQL
   */
  public String markRead() {
    return """
        UPDATE msg_message
        SET read_flag = 1,
            read_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId}
          AND message_code = #{messageCode}
          AND receiver_id = #{receiverId}
          AND deleted = 0
        """;
  }

  private String baseQuerySql(String selectSql, Map<String, Object> params, boolean paging) {
    StringBuilder sql = new StringBuilder(selectSql)
        .append("""

            FROM msg_message
            WHERE tenant_id = #{tenantId}
              AND receiver_id = #{receiverId}
              AND deleted = 0
            """);
    if (params.get("readFlag") != null) {
      sql.append("  AND read_flag = #{readFlag}\n");
    }
    if (paging) {
      sql.append("""
          ORDER BY created_at DESC, id DESC
          LIMIT #{pageSize} OFFSET #{offset}
          """);
    }
    return sql.toString();
  }
}
