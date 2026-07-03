/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template.mapper;

/**
 * 消息模板 SQL Provider。
 */
public class MsgTemplateSqlProvider {

  /**
   * 生成租户消息模板查询 SQL。
   *
   * @return 租户消息模板查询 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               template_code AS templateCode,
               template_name AS templateName,
               channel_type AS channelType,
               title_template AS titleTemplate,
               content_template AS contentTemplate,
               status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM msg_template
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        ORDER BY template_code
        """;
  }

  /**
   * 生成启用消息模板精确查询 SQL。
   *
   * @return 启用消息模板查询 SQL
   */
  public String selectEnabledByTenantIdAndTemplateCode() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               template_code AS templateCode,
               template_name AS templateName,
               channel_type AS channelType,
               title_template AS titleTemplate,
               content_template AS contentTemplate,
               status,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM msg_template
        WHERE tenant_id = #{tenantId}
          AND template_code = #{templateCode}
          AND status = 'enabled'
          AND deleted = 0
        """;
  }

  /**
   * 生成消息模板新增或更新 SQL。
   *
   * @return 消息模板新增或更新 SQL
   */
  public String upsert() {
    return """
        INSERT INTO msg_template (
            tenant_id, template_code, template_name, channel_type, title_template, content_template, status
        ) VALUES (
            #{tenantId}, #{templateCode}, #{templateName}, #{channelType}, #{titleTemplate}, #{contentTemplate}, #{status}
        )
        ON DUPLICATE KEY UPDATE
            template_name = VALUES(template_name),
            channel_type = VALUES(channel_type),
            title_template = VALUES(title_template),
            content_template = VALUES(content_template),
            status = VALUES(status),
            updated_at = CURRENT_TIMESTAMP,
            version = version + 1
        """;
  }
}
