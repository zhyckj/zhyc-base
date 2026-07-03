/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category.mapper;

/**
 * 工作流分类 SQL Provider。
 */
public class WorkflowCategorySqlProvider {

  /**
   * 生成租户内工作流分类查询 SQL。
   *
   * @return 工作流分类查询 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               category_code AS categoryCode,
               category_name AS categoryName,
               sort_order AS sortOrder,
               status,
               created_at AS createdAt,
               updated_at AS updatedAt,
               remark
        FROM wf_category
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        ORDER BY sort_order, id
        """;
  }

  /**
   * 生成工作流分类新增或更新 SQL。
   *
   * @return 工作流分类保存 SQL
   */
  public String upsertCategory() {
    return """
        INSERT INTO wf_category (
            tenant_id,
            category_code,
            category_name,
            sort_order,
            status,
            remark
        ) VALUES (
            #{tenantId},
            #{categoryCode},
            #{categoryName},
            #{sortOrder},
            #{status},
            #{remark}
        )
        ON DUPLICATE KEY UPDATE
            category_name = VALUES(category_name),
            sort_order = VALUES(sort_order),
            status = VALUES(status),
            remark = VALUES(remark),
            deleted = 0,
            updated_at = CURRENT_TIMESTAMP
        """;
  }
}
