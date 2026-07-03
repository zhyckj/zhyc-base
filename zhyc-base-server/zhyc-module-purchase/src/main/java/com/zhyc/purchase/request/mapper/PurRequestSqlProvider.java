/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.mapper;

import java.util.Map;

/**
 * 采购申请 SQL Provider。
 */
public class PurRequestSqlProvider {

  /**
   * 生成按租户和申请单号查询采购申请状态的 SQL。
   *
   * @return 采购申请状态查询 SQL
   */
  public String selectByTenantIdAndRequestNo() {
    return """
        SELECT id,
               tenant_id AS tenantId,
               request_no AS requestNo,
               request_title AS requestTitle,
               applicant_id AS applicantId,
               org_id AS orgId,
               total_amount AS totalAmount,
               request_reason AS requestReason,
               process_status AS processStatus,
               process_instance_id AS processInstanceId,
               submitted_at AS submittedAt,
               created_at AS createdAt,
               updated_at AS updatedAt
        FROM pur_request
        WHERE tenant_id = #{tenantId}
          AND request_no = #{requestNo}
          AND deleted = 0
        """;
  }

  /**
   * 生成采购申请草稿写入 SQL。
   *
   * @return 采购申请草稿写入 SQL
   */
  public String insert() {
    return """
        INSERT INTO pur_request (
            tenant_id, request_no, request_title, applicant_id, org_id, total_amount,
            request_reason, process_status
        ) VALUES (
            #{tenantId}, #{requestNo}, #{requestTitle}, #{applicantId}, #{orgId}, #{totalAmount},
            #{requestReason}, #{processStatus}
        )
        """;
  }

  /**
   * 生成采购申请提交审批更新 SQL。
   *
   * @return 采购申请提交审批更新 SQL
   */
  public String updateSubmitted() {
    return """
        UPDATE pur_request
        SET process_instance_id = #{processInstanceId},
            process_status = #{processStatus},
            submitted_at = #{submittedAt},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId}
          AND request_no = #{requestNo}
          AND process_status = 'DRAFT'
          AND deleted = 0
        """;
  }

  /**
   * 生成采购申请流程状态更新 SQL。
   *
   * @return 采购申请流程状态更新 SQL
   */
  public String updateProcessStatus() {
    return """
        UPDATE pur_request
        SET process_status = #{processStatus},
            updated_at = #{updatedAt}
        WHERE tenant_id = #{tenantId}
          AND request_no = #{requestNo}
          AND process_status = 'APPROVING'
          AND deleted = 0
        """;
  }

  /**
   * 生成按租户和流程状态统计采购申请数量 SQL。
   *
   * @param params 查询参数
   * @return 采购申请数量统计 SQL
   */
  public String countByTenantIdAndProcessStatus(Map<String, Object> params) {
    return baseListSql("SELECT COUNT(1)", params, false);
  }

  /**
   * 生成按租户和流程状态分页查询采购申请 SQL。
   *
   * @param params 查询参数
   * @return 采购申请分页查询 SQL
   */
  public String selectPageByTenantIdAndProcessStatus(Map<String, Object> params) {
    return baseListSql("""
        SELECT id,
               tenant_id AS tenantId,
               request_no AS requestNo,
               request_title AS requestTitle,
               applicant_id AS applicantId,
               org_id AS orgId,
               total_amount AS totalAmount,
               request_reason AS requestReason,
               process_status AS processStatus,
               process_instance_id AS processInstanceId,
               submitted_at AS submittedAt,
               created_at AS createdAt,
               updated_at AS updatedAt
        """, params, true);
  }

  /**
   * 生成采购申请列表基础 SQL。
   *
   * @param selectSql SELECT 片段
   * @param params 查询参数
   * @param paging 是否追加排序和分页
   * @return 采购申请列表 SQL
   */
  private String baseListSql(String selectSql, Map<String, Object> params, boolean paging) {
    StringBuilder sql = new StringBuilder(selectSql)
        .append("""

            FROM pur_request
            WHERE tenant_id = #{tenantId}
              AND deleted = 0
            """);
    Object processStatus = params.get("processStatus");
    if (processStatus != null && !processStatus.toString().isBlank()) {
      sql.append("  AND process_status = #{processStatus}\n");
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
