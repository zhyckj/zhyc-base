/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding.mapper;

/**
 * 工作流表单绑定 SQL Provider。
 */
public class WorkflowFormBindingSqlProvider {

  /**
   * 生成租户内工作流表单绑定查询 SQL。
   *
   * @return 工作流表单绑定查询 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               process_key AS processKey,
               business_module AS businessModule,
               business_table AS businessTable,
               form_route AS formRoute,
               mobile_route AS mobileRoute,
               status,
               created_at AS createdAt,
               updated_at AS updatedAt,
               remark
        FROM wf_form_binding
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        ORDER BY business_module, process_key
        """;
  }

  /**
   * 生成工作流表单绑定新增或更新 SQL。
   *
   * @return 工作流表单绑定保存 SQL
   */
  public String upsertBinding() {
    return """
        INSERT INTO wf_form_binding (
            tenant_id,
            process_key,
            business_module,
            business_table,
            form_route,
            mobile_route,
            status,
            remark
        ) VALUES (
            #{tenantId},
            #{processKey},
            #{businessModule},
            #{businessTable},
            #{formRoute},
            #{mobileRoute},
            #{status},
            #{remark}
        )
        ON DUPLICATE KEY UPDATE
            business_module = VALUES(business_module),
            business_table = VALUES(business_table),
            form_route = VALUES(form_route),
            mobile_route = VALUES(mobile_route),
            status = VALUES(status),
            remark = VALUES(remark),
            deleted = 0,
            updated_at = CURRENT_TIMESTAMP
        """;
  }
}
