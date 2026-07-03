/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.order.mapper;

import java.util.Map;

/**
 * 采购订单 SQL Provider。
 */
public class PurOrderSqlProvider {

  /**
   * 生成采购订单主表写入 SQL。
   *
   * @return 采购订单主表写入 SQL
   */
  public String insertOrder() {
    return """
        INSERT INTO pur_order (
            tenant_id, order_no, request_no, supplier_id, buyer_id, total_amount, order_status
        ) VALUES (
            #{tenantId}, #{orderNo}, #{requestNo}, #{supplierId}, #{buyerId}, #{totalAmount},
            #{orderStatus}
        )
        """;
  }

  /**
   * 生成采购订单明细写入 SQL。
   *
   * @return 采购订单明细写入 SQL
   */
  public String insertItem() {
    return """
        INSERT INTO pur_order_item (
            tenant_id, order_no, item_name, quantity, unit_price, amount
        ) VALUES (
            #{tenantId}, #{orderNo}, #{item.itemName}, #{item.quantity}, #{item.unitPrice},
            #{item.amount}
        )
        """;
  }

  /**
   * 生成按租户和订单号查询采购订单主表 SQL。
   *
   * @return 采购订单主表查询 SQL
   */
  public String selectOrderByTenantIdAndOrderNo() {
    return """
        SELECT tenant_id AS tenantId,
               order_no AS orderNo,
               request_no AS requestNo,
               supplier_id AS supplierId,
               buyer_id AS buyerId,
               total_amount AS totalAmount,
               order_status AS orderStatus
        FROM pur_order
        WHERE tenant_id = #{tenantId}
          AND order_no = #{orderNo}
          AND deleted = 0
        """;
  }

  /**
   * 生成按租户和订单号查询采购订单明细 SQL。
   *
   * @return 采购订单明细查询 SQL
   */
  public String selectItemsByTenantIdAndOrderNo() {
    return """
        SELECT item_name AS itemName,
               quantity,
               unit_price AS unitPrice,
               amount
        FROM pur_order_item
        WHERE tenant_id = #{tenantId}
          AND order_no = #{orderNo}
          AND deleted = 0
        ORDER BY id ASC
        """;
  }

  /**
   * 生成按租户和状态统计采购订单数量 SQL。
   *
   * @param params 查询参数
   * @return 采购订单数量统计 SQL
   */
  public String countByTenantIdAndStatus(Map<String, Object> params) {
    return baseListSql("SELECT COUNT(1)", params, false);
  }

  /**
   * 生成按租户和状态分页查询采购订单 SQL。
   *
   * @param params 查询参数
   * @return 采购订单分页查询 SQL
   */
  public String selectPageByTenantIdAndStatus(Map<String, Object> params) {
    return baseListSql("""
        SELECT tenant_id AS tenantId,
               order_no AS orderNo,
               request_no AS requestNo,
               supplier_id AS supplierId,
               buyer_id AS buyerId,
               total_amount AS totalAmount,
               order_status AS orderStatus
        """, params, true);
  }

  /**
   * 生成采购订单状态更新 SQL。
   *
   * @return 采购订单状态更新 SQL
   */
  public String updateStatus() {
    return """
        UPDATE pur_order
        SET order_status = #{orderStatus},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId}
          AND order_no = #{orderNo}
          AND deleted = 0
        """;
  }

  /**
   * 生成采购订单列表查询基础 SQL。
   *
   * @param selectSql SELECT 片段
   * @param params 查询参数
   * @param paging 是否追加排序和分页
   * @return 采购订单列表查询 SQL
   */
  private String baseListSql(String selectSql, Map<String, Object> params, boolean paging) {
    StringBuilder sql = new StringBuilder(selectSql)
        .append("""

            FROM pur_order
            WHERE tenant_id = #{tenantId}
              AND deleted = 0
            """);
    Object orderStatus = params.get("orderStatus");
    if (orderStatus != null && !orderStatus.toString().isBlank()) {
      sql.append("  AND order_status = #{orderStatus}\n");
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
