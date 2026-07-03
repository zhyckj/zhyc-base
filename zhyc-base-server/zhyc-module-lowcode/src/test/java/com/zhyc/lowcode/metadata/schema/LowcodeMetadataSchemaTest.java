/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.schema;

import com.zhyc.common.module.ModuleDescriptor;
import com.zhyc.common.module.ModuleDescriptorParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 低代码元数据建表脚本测试。
 */
class LowcodeMetadataSchemaTest {

  /**
   * 验证低代码核心元数据表存在，确保数据源、表模型、字段模型能独立落库。
   *
   * @throws IOException 读取 SQL 资源失败时抛出
   */
  @Test
  void shouldDeclareLowcodeCoreMetadataTables() throws IOException {
    String sql = normalizedLowcodeCoreSql();

    assertTrue(sql.contains("create table if not exists lowcode_data_source"));
    assertTrue(sql.contains("create table if not exists lowcode_table_model"));
    assertTrue(sql.contains("create table if not exists lowcode_column_model"));
  }

  /**
   * 验证元数据表按租户和编码建立唯一约束，避免 SaaS 场景下跨租户冲突。
   *
   * @throws IOException 读取 SQL 资源失败时抛出
   */
  @Test
  void shouldDeclareTenantScopedUniqueKeys() throws IOException {
    String sql = normalizedLowcodeCoreSql();

    assertTrue(sql.contains("unique key uk_lowcode_ds_tenant_code (tenant_id, code)"));
    assertTrue(sql.contains("unique key uk_lowcode_table_tenant_code (tenant_id, code)"));
    assertTrue(sql.contains("unique key uk_lowcode_column_table_code (table_model_id, code)"));
  }

  /**
   * 验证字段模型包含列表、表单、查询配置，供多端模板复用。
   *
   * @throws IOException 读取 SQL 资源失败时抛出
   */
  @Test
  void shouldDeclareColumnUiAndQueryFlags() throws IOException {
    String sql = normalizedLowcodeCoreSql();

    assertTrue(sql.contains("list_visible tinyint(1) not null default 0"));
    assertTrue(sql.contains("form_visible tinyint(1) not null default 0"));
    assertTrue(sql.contains("queryable tinyint(1) not null default 0"));
    assertTrue(sql.contains("dict_code varchar(64) default null"));
  }

  /**
   * 验证字典绑定字段有增量升级脚本，避免旧环境表结构缺列导致建模列表查询失败。
   *
   * @throws IOException 读取 SQL 或模块描述资源失败时抛出
   */
  @Test
  void shouldDeclareDictCodeUpgradeScript() throws IOException {
    ModuleDescriptor descriptor = lowcodeModuleDescriptor();
    String sql = normalizedResource("db/V2__lowcode_column_dict_code.sql");

    assertTrue(descriptor.getDbScripts().contains("classpath:db/V2__lowcode_column_dict_code.sql"));
    assertTrue(sql.contains("information_schema.columns"));
    assertTrue(sql.contains("table_name = 'lowcode_column_model'"));
    assertTrue(sql.contains("column_name = 'dict_code'"));
    assertTrue(sql.contains("alter table lowcode_column_model add column dict_code varchar(64) default null"));
  }

  /**
   * 验证数据源密码只保存密钥引用，不在元数据表明文存储。
   *
   * @throws IOException 读取 SQL 资源失败时抛出
   */
  @Test
  void shouldStoreDataSourcePasswordAsSecretReference() throws IOException {
    String sql = normalizedLowcodeCoreSql();

    assertTrue(sql.contains("password_secret_ref varchar(255) default null"));
    assertFalse(sql.contains(" password "));
    assertFalse(sql.contains("password_plain"));
  }

  /**
   * 验证代码生成记录表包含租户、模型、目标端、覆盖策略、文件数量和状态字段。
   *
   * @throws IOException 读取 SQL 资源失败时抛出
   */
  @Test
  void shouldDeclareGenerationRecordTable() throws IOException {
    String sql = normalizedLowcodeCoreSql();

    assertTrue(sql.contains("create table if not exists lowcode_generation_record"));
    assertTrue(sql.contains("tenant_id varchar(64) not null"));
    assertTrue(sql.contains("table_model_code varchar(64) not null"));
    assertTrue(sql.contains("target varchar(32) not null"));
    assertTrue(sql.contains("overwrite_strategy varchar(32) not null"));
    assertTrue(sql.contains("file_count int not null default 0"));
    assertTrue(sql.contains("file_manifest_json text default null"));
    assertTrue(sql.contains("status varchar(32) not null"));
    assertTrue(sql.contains("error_message varchar(1000) default null"));
    assertTrue(sql.contains("key idx_lowcode_gen_tenant_model (tenant_id, table_model_code)"));
  }

  /**
   * 验证生成文件明细表包含文件路径、覆盖模式和内容哈希，供覆盖风险复核。
   *
   * @throws IOException 读取 SQL 资源失败时抛出
   */
  @Test
  void shouldDeclareGenerationFileTable() throws IOException {
    String sql = normalizedLowcodeCoreSql();

    assertTrue(sql.contains("create table if not exists lc_generation_file"));
    assertTrue(sql.contains("tenant_id varchar(64) not null"));
    assertTrue(sql.contains("record_id bigint not null"));
    assertTrue(sql.contains("file_path varchar(500) not null"));
    assertTrue(sql.contains("overwrite_mode varchar(32) not null"));
    assertTrue(sql.contains("content_hash varchar(128) not null"));
    assertTrue(sql.contains("key idx_lc_gen_file_tenant_record (tenant_id, record_id)"));
  }

  /**
   * 验证表关系模型表包含主子表、关系类型和关联字段，供主子表生成使用。
   *
   * @throws IOException 读取 SQL 资源失败时抛出
   */
  @Test
  void shouldDeclareTableRelationModelTable() throws IOException {
    String sql = normalizedLowcodeCoreSql();

    assertTrue(sql.contains("create table if not exists lowcode_table_relation"));
    assertTrue(sql.contains("main_table_id bigint not null"));
    assertTrue(sql.contains("sub_table_id bigint not null"));
    assertTrue(sql.contains("relation_type varchar(32) not null"));
    assertTrue(sql.contains("join_column varchar(64) not null"));
    assertTrue(sql.contains("ref_column varchar(64) not null"));
    assertTrue(sql.contains("unique key uk_lowcode_relation_tenant_tables"));
  }

  /**
   * 验证页面模型表包含表模型、页面类型、路由、组件和布局信息，供前后端生成链路复用。
   *
   * @throws IOException 读取 SQL 资源失败时抛出
   */
  @Test
  void shouldDeclarePageModelTable() throws IOException {
    String sql = normalizedLowcodeCoreSql();

    assertTrue(sql.contains("create table if not exists lowcode_page_model"));
    assertTrue(sql.contains("table_model_id bigint not null"));
    assertTrue(sql.contains("page_type varchar(32) not null"));
    assertTrue(sql.contains("route_path varchar(255) not null"));
    assertTrue(sql.contains("component_path varchar(255) not null"));
    assertTrue(sql.contains("layout_type varchar(64) not null"));
    assertTrue(sql.contains("unique key uk_lowcode_page_tenant_table_type"));
  }

  private String normalizedLowcodeCoreSql() throws IOException {
    return normalizedResource("db/V1__lowcode_core.sql");
  }

  private ModuleDescriptor lowcodeModuleDescriptor() throws IOException {
    String descriptorContent = resourceContent("META-INF/zhyc-module.yml");
    return ModuleDescriptorParser.parse(descriptorContent);
  }

  private String normalizedResource(String resourcePath) throws IOException {
    return resourceContent(resourcePath)
        .toLowerCase()
        .replaceAll("\\s+", " ");
  }

  private String resourceContent(String resourcePath) throws IOException {
    try (InputStream inputStream = LowcodeMetadataSchemaTest.class.getClassLoader()
        .getResourceAsStream(resourcePath)) {
      assertNotNull(inputStream, resourcePath + " should be available on the test classpath");
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
