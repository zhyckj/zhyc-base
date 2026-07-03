/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.mapper;

import java.util.Map;

/**
 * 内容管理 SQL Provider。
 */
public class CmsSqlProvider {

  /**
   * 生成内容栏目查询 SQL。
   *
   * @param params 查询参数
   * @return 内容栏目查询 SQL
   */
  public String selectChannels(Map<String, Object> params) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               parent_id AS parentId,
               channel_code AS channelCode,
               channel_name AS channelName,
               sort_order AS sortOrder,
               channel_status AS status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM cms_channel
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        """);
    Object status = params.get("status");
    if (status != null && !status.toString().isBlank()) {
      sql.append("  AND channel_status = #{status}\n");
    }
    sql.append("ORDER BY sort_order ASC, id ASC\n");
    return sql.toString();
  }

  /**
   * 生成内容栏目保存 SQL。
   *
   * @return 内容栏目保存 SQL
   */
  public String upsertChannel() {
    return """
        INSERT INTO cms_channel (
            tenant_id, parent_id, channel_code, channel_name, sort_order, channel_status
        ) VALUES (
            #{tenantId}, #{parentId}, #{channelCode}, #{channelName}, #{sortOrder}, #{status}
        )
        ON DUPLICATE KEY UPDATE
            parent_id = VALUES(parent_id),
            channel_name = VALUES(channel_name),
            sort_order = VALUES(sort_order),
            channel_status = VALUES(channel_status),
            updated_at = CURRENT_TIMESTAMP,
            deleted = 0
        """;
  }

  /**
   * 生成内容文章查询 SQL。
   *
   * @param params 查询参数
   * @return 内容文章查询 SQL
   */
  public String selectContents(Map<String, Object> params) {
    StringBuilder sql = new StringBuilder("""
        SELECT id,
               tenant_id AS tenantId,
               channel_code AS channelCode,
               title,
               summary,
               body_content AS bodyContent,
               content_status AS status,
               author_id AS authorId,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM cms_content
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        """);
    Object channelCode = params.get("channelCode");
    if (channelCode != null && !channelCode.toString().isBlank()) {
      sql.append("  AND channel_code = #{channelCode}\n");
    }
    Object status = params.get("status");
    if (status != null && !status.toString().isBlank()) {
      sql.append("  AND content_status = #{status}\n");
    }
    sql.append("ORDER BY updated_at DESC, id DESC\n");
    return sql.toString();
  }

  /**
   * 生成内容文章保存 SQL。
   *
   * @return 内容文章保存 SQL
   */
  public String insertContent() {
    return """
        INSERT INTO cms_content (
            tenant_id, channel_code, title, summary, body_content, content_status, author_id
        ) VALUES (
            #{tenantId}, #{channelCode}, #{title}, #{summary}, #{bodyContent}, #{status}, #{authorId}
        )
        """;
  }

  /**
   * 生成内容文章更新 SQL。
   *
   * @return 内容文章更新 SQL
   */
  public String updateContent() {
    return """
        UPDATE cms_content
        SET channel_code = #{channelCode},
            title = #{title},
            summary = #{summary},
            body_content = #{bodyContent},
            content_status = #{status},
            author_id = #{authorId},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId}
          AND id = #{id}
          AND deleted = 0
        """;
  }

  /**
   * 生成内容文章状态更新 SQL。
   *
   * @return 内容文章状态更新 SQL
   */
  public String updateContentStatus() {
    return """
        UPDATE cms_content
        SET content_status = #{status},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId}
          AND id = #{id}
          AND deleted = 0
        """;
  }
}
