/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

/**
 * 低代码页面模型 MyBatis SQL Provider。
 *
 * <p>页面模型除自身租户字段外，还必须校验被绑定的表模型归属同一租户。</p>
 */
public class LowcodePageModelSqlProvider {

  /**
   * 构建新增页面模型 SQL。
   *
   * @return 新增页面模型 SQL
   */
  public String insert() {
    return """
        INSERT INTO lowcode_page_model
        (tenant_id, table_model_id, page_type, route_path, component_path, layout_type)
        VALUES
        (#{tenantId}, #{tableModelId}, #{pageType}, #{routePath}, #{componentPath}, #{layoutType})
        """.strip();
  }

  /**
   * 构建按租户查询页面模型 SQL。
   *
   * @return 仅返回当前租户表模型下页面的 SQL
   */
  public String selectByTenantId() {
    return """
        SELECT p.id, p.tenant_id, p.table_model_id, p.page_type, p.route_path, p.component_path, p.layout_type
        FROM lowcode_page_model p
        INNER JOIN lowcode_table_model t ON p.table_model_id = t.id AND t.tenant_id = #{tenantId}
        WHERE p.tenant_id = #{tenantId}
        ORDER BY p.table_model_id ASC, p.page_type ASC
        """.strip();
  }

  /**
   * 构建页面模型更新 SQL。
   *
   * @return 同时校验表模型租户归属的页面模型更新 SQL
   */
  public String updateByTenantTableAndType() {
    return """
        UPDATE lowcode_page_model
        SET route_path = #{routePath},
            component_path = #{componentPath},
            layout_type = #{layoutType},
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = #{tenantId}
          AND table_model_id = #{tableModelId}
          AND page_type = #{pageType}
          AND EXISTS (
              SELECT 1 FROM lowcode_table_model t
              WHERE t.id = #{tableModelId} AND t.tenant_id = #{tenantId}
          )
        """.strip();
  }
}
