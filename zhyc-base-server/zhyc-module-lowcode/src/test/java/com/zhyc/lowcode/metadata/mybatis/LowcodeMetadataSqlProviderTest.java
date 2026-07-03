/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 低代码元数据 MyBatis SQL Provider 测试。
 */
class LowcodeMetadataSqlProviderTest {

  /**
   * 验证数据源插入 SQL 覆盖租户、方言、连接和密钥引用字段。
   */
  @Test
  void shouldBuildDataSourceInsertSql() {
    String sql = new LowcodeDataSourceSqlProvider().insert();

    assertTrue(sql.contains("INSERT INTO lowcode_data_source"));
    assertTrue(sql.contains("tenant_id, code, name, dialect, jdbc_url, username, password_secret_ref, enabled"));
    assertTrue(sql.contains("#{tenantId}, #{code}, #{name}, #{dialect}, #{jdbcUrl}, #{username}, #{passwordSecretRef}, #{enabled}"));
  }

  /**
   * 验证数据源按租户和编码查询，避免跨租户读取。
   */
  @Test
  void shouldBuildTenantScopedDataSourceSelectSql() {
    String sql = new LowcodeDataSourceSqlProvider().selectByTenantIdAndCode();

    assertEquals("""
        SELECT id, tenant_id, code, name, dialect, jdbc_url, username, password_secret_ref, enabled
        FROM lowcode_data_source
        WHERE tenant_id = #{tenantId} AND code = #{code}
        """.strip(), sql);
  }

  /**
   * 验证数据源按租户和主键查询，避免跨租户绑定数据源。
   */
  @Test
  void shouldBuildTenantScopedDataSourceSelectByIdSql() {
    String sql = new LowcodeDataSourceSqlProvider().selectByTenantIdAndId();

    assertEquals("""
        SELECT id, tenant_id, code, name, dialect, jdbc_url, username, password_secret_ref, enabled
        FROM lowcode_data_source
        WHERE tenant_id = #{tenantId} AND id = #{id}
        """.strip(), sql);
  }

  /**
   * 验证数据源列表 SQL 按租户隔离并稳定排序。
   */
  @Test
  void shouldBuildTenantScopedDataSourceListSql() {
    String sql = new LowcodeDataSourceSqlProvider().selectByTenantId();

    assertEquals("""
        SELECT id, tenant_id, code, name, dialect, jdbc_url, username, password_secret_ref, enabled
        FROM lowcode_data_source
        WHERE tenant_id = #{tenantId}
        ORDER BY code ASC
        """.strip(), sql);
  }

  /**
   * 验证表模型插入 SQL 覆盖状态和数据源引用字段。
   */
  @Test
  void shouldBuildTableModelInsertSql() {
    String sql = new LowcodeTableModelSqlProvider().insert();

    assertTrue(sql.contains("INSERT INTO lowcode_table_model"));
    assertTrue(sql.contains("tenant_id, data_source_id, code, name, table_name, status"));
    assertTrue(sql.contains("#{tenantId}, #{dataSourceId}, #{code}, #{name}, #{tableName}, #{status}"));
  }

  /**
   * 验证表模型列表 SQL 按租户隔离并稳定排序。
   */
  @Test
  void shouldBuildTenantScopedTableModelListSql() {
    String sql = new LowcodeTableModelSqlProvider().selectByTenantId();

    assertEquals("""
        SELECT id, tenant_id, data_source_id, code, name, table_name, status
        FROM lowcode_table_model
        WHERE tenant_id = #{tenantId}
        ORDER BY code ASC
        """.strip(), sql);
  }

  /**
   * 验证表模型按租户和主键精确查询，避免页面模型和表关系校验扫描租户下全部表模型。
   */
  @Test
  void shouldBuildTenantScopedTableModelSelectByIdSql() {
    String sql = new LowcodeTableModelSqlProvider().selectByTenantIdAndId();

    assertEquals("""
        SELECT id, tenant_id, data_source_id, code, name, table_name, status
        FROM lowcode_table_model
        WHERE tenant_id = #{tenantId} AND id = #{id}
        """.strip(), sql);
  }

  /**
   * 验证字段模型重建前按租户和表模型删除旧字段。
   */
  @Test
  void shouldBuildDeleteColumnsByTableModelIdSql() {
    String sql = new LowcodeColumnModelSqlProvider().deleteByTenantIdAndTableModelId();

    assertEquals("""
        DELETE c FROM lowcode_column_model c
        INNER JOIN lowcode_table_model t ON c.table_model_id = t.id
        WHERE t.tenant_id = #{tenantId} AND c.table_model_id = #{tableModelId}
        """.strip(), sql);
  }

  /**
   * 验证字段模型查询按租户和表模型隔离，避免仅凭表模型主键读取字段。
   */
  @Test
  void shouldBuildTenantScopedColumnModelSelectSql() {
    String sql = new LowcodeColumnModelSqlProvider().selectByTenantIdAndTableModelId();

    assertTrue(sql.contains("FROM lowcode_column_model c"));
    assertTrue(sql.contains("c.list_visible, c.form_visible, c.queryable, c.dict_code"));
    assertTrue(sql.contains("INNER JOIN lowcode_table_model t ON c.table_model_id = t.id"));
    assertTrue(sql.contains("WHERE t.tenant_id = #{tenantId} AND c.table_model_id = #{tableModelId}"));
    assertTrue(sql.contains("ORDER BY c.sort_order ASC, c.id ASC"));
  }

  /**
   * 验证字段模型插入 SQL 覆盖类型、精度、UI 和查询配置。
   */
  @Test
  void shouldBuildColumnModelInsertSql() {
    String sql = new LowcodeColumnModelSqlProvider().insert();

    assertTrue(sql.contains("(table_model_id, code, name, field_type"));
    assertTrue(sql.contains("field_type, length_value, scale_value"));
    assertTrue(sql.contains("required, primary_key_flag, auto_increment_flag"));
    assertTrue(sql.contains("list_visible, form_visible, queryable, dict_code, sort_order, comment"));
  }

  /**
   * 验证表关系插入 SQL 覆盖主表、子表、关系类型和关联字段。
   */
  @Test
  void shouldBuildTableRelationInsertSql() {
    String sql = new LowcodeTableRelationSqlProvider().insert();

    assertTrue(sql.contains("INSERT INTO lowcode_table_relation"));
    assertTrue(sql.contains("tenant_id, main_table_id, sub_table_id, relation_type, join_column, ref_column"));
    assertTrue(sql.contains("#{tenantId}, #{mainTableId}, #{subTableId}, #{relationType}, #{joinColumn}, #{refColumn}"));
  }

  /**
   * 验证表关系列表 SQL 必须按租户隔离。
   */
  @Test
  void shouldBuildTenantScopedTableRelationListSql() {
    String sql = new LowcodeTableRelationSqlProvider().selectByTenantId();

    assertEquals("""
        SELECT r.id, r.tenant_id, r.main_table_id, r.sub_table_id, r.relation_type, r.join_column, r.ref_column
        FROM lowcode_table_relation r
        INNER JOIN lowcode_table_model main_table
          ON r.main_table_id = main_table.id AND main_table.tenant_id = #{tenantId}
        INNER JOIN lowcode_table_model sub_table
          ON r.sub_table_id = sub_table.id AND sub_table.tenant_id = #{tenantId}
        WHERE r.tenant_id = #{tenantId}
        ORDER BY r.id DESC
        """.strip(), sql);
  }

  /**
   * 验证表关系更新 SQL 使用租户、主表、子表和关系类型作为唯一定位条件。
   */
  @Test
  void shouldBuildTableRelationUpdateSqlByUniqueKey() {
    String sql = new LowcodeTableRelationSqlProvider().updateByTenantAndTables();

    assertTrue(sql.contains("UPDATE lowcode_table_relation"));
    assertTrue(sql.contains("join_column = #{joinColumn}"));
    assertTrue(sql.contains("ref_column = #{refColumn}"));
    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND main_table_id = #{mainTableId}"));
    assertTrue(sql.contains("AND sub_table_id = #{subTableId}"));
    assertTrue(sql.contains("AND relation_type = #{relationType}"));
    assertTrue(sql.contains("main_table.tenant_id = #{tenantId}"));
    assertTrue(sql.contains("sub_table.tenant_id = #{tenantId}"));
  }

  /**
   * 验证页面模型插入 SQL 覆盖表模型、页面类型、路由、组件和布局字段。
   */
  @Test
  void shouldBuildPageModelInsertSql() {
    String sql = new LowcodePageModelSqlProvider().insert();

    assertTrue(sql.contains("INSERT INTO lowcode_page_model"));
    assertTrue(sql.contains("tenant_id, table_model_id, page_type, route_path, component_path, layout_type"));
    assertTrue(sql.contains("#{tenantId}, #{tableModelId}, #{pageType}, #{routePath}, #{componentPath}, #{layoutType}"));
  }

  /**
   * 验证页面模型列表 SQL 必须按租户隔离并稳定排序。
   */
  @Test
  void shouldBuildTenantScopedPageModelListSql() {
    String sql = new LowcodePageModelSqlProvider().selectByTenantId();

    assertEquals("""
        SELECT p.id, p.tenant_id, p.table_model_id, p.page_type, p.route_path, p.component_path, p.layout_type
        FROM lowcode_page_model p
        INNER JOIN lowcode_table_model t ON p.table_model_id = t.id AND t.tenant_id = #{tenantId}
        WHERE p.tenant_id = #{tenantId}
        ORDER BY p.table_model_id ASC, p.page_type ASC
        """.strip(), sql);
  }

  /**
   * 验证页面模型更新 SQL 使用租户、表模型和页面类型作为唯一定位条件。
   */
  @Test
  void shouldBuildPageModelUpdateSqlByTenantTableAndType() {
    String sql = new LowcodePageModelSqlProvider().updateByTenantTableAndType();

    assertTrue(sql.contains("UPDATE lowcode_page_model"));
    assertTrue(sql.contains("route_path = #{routePath}"));
    assertTrue(sql.contains("component_path = #{componentPath}"));
    assertTrue(sql.contains("layout_type = #{layoutType}"));
    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND table_model_id = #{tableModelId}"));
    assertTrue(sql.contains("AND page_type = #{pageType}"));
    assertTrue(sql.contains("t.tenant_id = #{tenantId}"));
  }

  /**
   * 验证生成记录插入 SQL 覆盖租户、模型、目标端、覆盖策略、文件数量和状态字段。
   */
  @Test
  void shouldBuildGenerationRecordInsertSql() {
    String sql = new com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordSqlProvider().insert();

    assertTrue(sql.contains("INSERT INTO lowcode_generation_record"));
    assertTrue(sql.contains("tenant_id, table_model_code, target, module_name, entity_name"));
    assertTrue(sql.contains("overwrite_strategy, file_count, file_manifest_json, status, error_message"));
    assertTrue(sql.contains("#{tenantId}"));
    assertTrue(sql.contains("#{overwriteStrategy}"));
    assertTrue(sql.contains("#{fileManifestJson}"));
  }

  /**
   * 验证生成记录列表 SQL 必须按租户隔离并按创建顺序倒序。
   */
  @Test
  void shouldBuildGenerationRecordListSqlByTenant() {
    String sql = new com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordSqlProvider().selectByTenantId();

    assertEquals("""
        SELECT id, tenant_id, table_model_code, target, module_name, entity_name,
               overwrite_strategy, file_count, file_manifest_json, status, error_message
        FROM lowcode_generation_record
        WHERE tenant_id = #{tenantId}
        ORDER BY id DESC
        """.strip(), sql);
  }

  /**
   * 验证生成文件明细插入 SQL 包含内容哈希和覆盖模式。
   */
  @Test
  void shouldBuildGenerationFileInsertSql() {
    String sql = new com.zhyc.lowcode.generator.mybatis.LowcodeGenerationFileSqlProvider().insert();

    assertTrue(sql.contains("INSERT INTO lc_generation_file"));
    assertTrue(sql.contains("template_code, file_path, file_type, overwrite_mode, content_hash"));
    assertTrue(sql.contains("#{contentHash}"));
  }

  /**
   * 验证生成文件明细查询 SQL 必须同时按租户和生成记录隔离。
   */
  @Test
  void shouldBuildGenerationFileListSqlByTenantAndRecord() {
    String sql = new com.zhyc.lowcode.generator.mybatis.LowcodeGenerationFileSqlProvider()
        .selectByTenantIdAndRecordId();

    assertTrue(sql.contains("FROM lc_generation_file"));
    assertTrue(sql.contains("WHERE tenant_id = #{tenantId}"));
    assertTrue(sql.contains("AND record_id = #{recordId}"));
    assertTrue(sql.contains("content_hash"));
  }
}
