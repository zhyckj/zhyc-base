/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition.mapper;

/**
 * 工作流流程定义 SQL Provider。
 */
public class WorkflowDefinitionSqlProvider {

  /**
   * 生成租户内流程定义版本查询 SQL。
   *
   * @return 流程定义版本查询 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               process_key AS processKey,
               process_name AS processName,
               version,
               deployment_id AS deploymentId,
               status,
               created_at AS createdAt,
               updated_at AS updatedAt,
               remark
        FROM wf_process_definition
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        ORDER BY process_key, version DESC
        """;
  }

  /**
   * 生成流程定义版本新增或更新 SQL。
   *
   * @return 流程定义版本保存 SQL
   */
  public String upsertDefinition() {
    return """
        INSERT INTO wf_process_definition (
            tenant_id,
            process_key,
            process_name,
            version,
            deployment_id,
            status,
            remark
        ) VALUES (
            #{tenantId},
            #{processKey},
            #{processName},
            #{version},
            #{deploymentId},
            #{status},
            #{remark}
        )
        ON DUPLICATE KEY UPDATE
            process_name = VALUES(process_name),
            deployment_id = VALUES(deployment_id),
            status = VALUES(status),
            remark = VALUES(remark),
            deleted = 0,
            updated_at = CURRENT_TIMESTAMP
        """;
  }
}
