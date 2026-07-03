/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model.mapper;

/**
 * 工作流流程模型 SQL Provider。
 */
public class WorkflowProcessModelSqlProvider {

  /**
   * 生成租户内流程模型查询 SQL。
   *
   * @return 流程模型查询 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               model_code AS modelCode,
               model_name AS modelName,
               category_id AS categoryId,
               flowable_model_id AS flowableModelId,
               bpmn_xml AS bpmnXml,
               status,
               created_at AS createdAt,
               updated_at AS updatedAt,
               remark
        FROM wf_process_model
        WHERE tenant_id = #{tenantId}
          AND deleted = 0
        ORDER BY category_id, model_code
        """;
  }

  /**
   * 生成流程模型新增或更新 SQL。
   *
   * @return 流程模型保存 SQL
   */
  public String upsertModel() {
    return """
        INSERT INTO wf_process_model (
            tenant_id,
            model_code,
            model_name,
            category_id,
            flowable_model_id,
            bpmn_xml,
            status,
            remark
        ) VALUES (
            #{tenantId},
            #{modelCode},
            #{modelName},
            #{categoryId},
            #{flowableModelId},
            #{bpmnXml},
            #{status},
            #{remark}
        )
        ON DUPLICATE KEY UPDATE
            model_name = VALUES(model_name),
            category_id = VALUES(category_id),
            flowable_model_id = VALUES(flowable_model_id),
            bpmn_xml = VALUES(bpmn_xml),
            status = VALUES(status),
            remark = VALUES(remark),
            deleted = 0,
            updated_at = CURRENT_TIMESTAMP
        """;
  }
}
