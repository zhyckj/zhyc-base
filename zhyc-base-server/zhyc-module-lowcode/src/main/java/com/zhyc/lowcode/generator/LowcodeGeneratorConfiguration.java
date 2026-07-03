/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import com.zhyc.lowcode.db.DefaultLowcodeDbDialectService;
import com.zhyc.lowcode.db.DdlGenerator;
import com.zhyc.lowcode.db.FieldTypeMapper;
import com.zhyc.lowcode.db.LowcodeDbDialectRegistry;
import com.zhyc.lowcode.db.LowcodeDbDialectService;
import com.zhyc.lowcode.db.PaginationDialect;
import com.zhyc.lowcode.db.dm.DmDdlGenerator;
import com.zhyc.lowcode.db.dm.DmFieldTypeMapper;
import com.zhyc.lowcode.db.dm.DmPaginationDialect;
import com.zhyc.lowcode.db.mysql.MySqlDdlGenerator;
import com.zhyc.lowcode.db.mysql.MySqlFieldTypeMapper;
import com.zhyc.lowcode.db.mysql.MySqlPaginationDialect;
import com.zhyc.lowcode.db.oracle.OracleDdlGenerator;
import com.zhyc.lowcode.db.oracle.OracleFieldTypeMapper;
import com.zhyc.lowcode.db.oracle.OraclePaginationDialect;
import com.zhyc.lowcode.db.postgresql.PostgreSqlDdlGenerator;
import com.zhyc.lowcode.db.postgresql.PostgreSqlFieldTypeMapper;
import com.zhyc.lowcode.db.postgresql.PostgreSqlPaginationDialect;
import com.zhyc.lowcode.db.sqlserver.SqlServerDdlGenerator;
import com.zhyc.lowcode.db.sqlserver.SqlServerFieldTypeMapper;
import com.zhyc.lowcode.db.sqlserver.SqlServerPaginationDialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.List;

/**
 * 低代码生成器 Spring 配置。
 */
@Configuration
public class LowcodeGeneratorConfiguration {

  /**
   * 注册首期内置代码模板提供者。
   *
   * @return 内置代码模板提供者
   */
  @Bean
  public CodeTemplateProvider builtInCodeTemplateProvider() {
    return new BuiltInCodeTemplateProvider();
  }

  /**
   * 注册代码模板注册表。
   *
   * @param providers 模板提供者列表
   * @return 代码模板注册表
   */
  @Bean
  public CodeTemplateRegistry codeTemplateRegistry(List<CodeTemplateProvider> providers) {
    return new CodeTemplateRegistry(providers);
  }

  /**
   * 注册 MySQL 字段类型映射器。
   *
   * @return MySQL 字段类型映射器
   */
  @Bean
  public MySqlFieldTypeMapper mySqlFieldTypeMapper() {
    return new MySqlFieldTypeMapper();
  }

  /**
   * 注册 MySQL DDL 生成器。
   *
   * @param fieldTypeMapper MySQL 字段类型映射器
   * @return MySQL DDL 生成器
   */
  @Bean
  public MySqlDdlGenerator mySqlDdlGenerator(MySqlFieldTypeMapper fieldTypeMapper) {
    return new MySqlDdlGenerator(fieldTypeMapper);
  }

  /**
   * 注册 MySQL 分页方言。
   *
   * @return MySQL 分页方言
   */
  @Bean
  public MySqlPaginationDialect mySqlPaginationDialect() {
    return new MySqlPaginationDialect();
  }

  /**
   * 注册 PostgreSQL 字段类型映射器。
   *
   * @return PostgreSQL 字段类型映射器
   */
  @Bean
  public PostgreSqlFieldTypeMapper postgreSqlFieldTypeMapper() {
    return new PostgreSqlFieldTypeMapper();
  }

  /**
   * 注册 PostgreSQL DDL 生成器。
   *
   * @param fieldTypeMapper PostgreSQL 字段类型映射器
   * @return PostgreSQL DDL 生成器
   */
  @Bean
  public PostgreSqlDdlGenerator postgreSqlDdlGenerator(PostgreSqlFieldTypeMapper fieldTypeMapper) {
    return new PostgreSqlDdlGenerator(fieldTypeMapper);
  }

  /**
   * 注册 PostgreSQL 分页方言。
   *
   * @return PostgreSQL 分页方言
   */
  @Bean
  public PostgreSqlPaginationDialect postgreSqlPaginationDialect() {
    return new PostgreSqlPaginationDialect();
  }

  /**
   * 注册 Oracle 字段类型映射器。
   *
   * @return Oracle 字段类型映射器
   */
  @Bean
  public OracleFieldTypeMapper oracleFieldTypeMapper() {
    return new OracleFieldTypeMapper();
  }

  /**
   * 注册 Oracle DDL 生成器。
   *
   * @param fieldTypeMapper Oracle 字段类型映射器
   * @return Oracle DDL 生成器
   */
  @Bean
  public OracleDdlGenerator oracleDdlGenerator(OracleFieldTypeMapper fieldTypeMapper) {
    return new OracleDdlGenerator(fieldTypeMapper);
  }

  /**
   * 注册 Oracle 分页方言。
   *
   * @return Oracle 分页方言
   */
  @Bean
  public OraclePaginationDialect oraclePaginationDialect() {
    return new OraclePaginationDialect();
  }

  /**
   * 注册 SQL Server 字段类型映射器。
   *
   * @return SQL Server 字段类型映射器
   */
  @Bean
  public SqlServerFieldTypeMapper sqlServerFieldTypeMapper() {
    return new SqlServerFieldTypeMapper();
  }

  /**
   * 注册 SQL Server DDL 生成器。
   *
   * @param fieldTypeMapper SQL Server 字段类型映射器
   * @return SQL Server DDL 生成器
   */
  @Bean
  public SqlServerDdlGenerator sqlServerDdlGenerator(SqlServerFieldTypeMapper fieldTypeMapper) {
    return new SqlServerDdlGenerator(fieldTypeMapper);
  }

  /**
   * 注册 SQL Server 分页方言。
   *
   * @return SQL Server 分页方言
   */
  @Bean
  public SqlServerPaginationDialect sqlServerPaginationDialect() {
    return new SqlServerPaginationDialect();
  }

  /**
   * 注册达梦数据库字段类型映射器。
   *
   * @return 达梦数据库字段类型映射器
   */
  @Bean
  public DmFieldTypeMapper dmFieldTypeMapper() {
    return new DmFieldTypeMapper();
  }

  /**
   * 注册达梦数据库 DDL 生成器。
   *
   * @param fieldTypeMapper 达梦数据库字段类型映射器
   * @return 达梦数据库 DDL 生成器
   */
  @Bean
  public DmDdlGenerator dmDdlGenerator(DmFieldTypeMapper fieldTypeMapper) {
    return new DmDdlGenerator(fieldTypeMapper);
  }

  /**
   * 注册达梦数据库分页方言。
   *
   * @return 达梦数据库分页方言
   */
  @Bean
  public DmPaginationDialect dmPaginationDialect() {
    return new DmPaginationDialect();
  }

  /**
   * 注册数据库方言能力注册中心。
   *
   * <p>统一接收 DDL 生成器、字段映射器、分页方言列表并做启动期能力聚合。</p>
   *
   * @param generators DDL 生成器列表
   * @param fieldTypeMappers 字段类型映射器列表
   * @param paginationDialects 分页方言列表
   * @return 方言能力注册中心
   */
  @Bean
  public LowcodeDbDialectRegistry lowcodeDbDialectRegistry(List<DdlGenerator> generators,
                                                           List<FieldTypeMapper> fieldTypeMappers,
                                                           List<PaginationDialect> paginationDialects) {
    return new LowcodeDbDialectRegistry(generators, fieldTypeMappers, paginationDialects);
  }

  /**
   * 注册数据库方言统一服务。
   *
   * @param dialectRegistry 方言能力注册中心
   * @return 方言统一服务
   */
  @Bean
  public LowcodeDbDialectService lowcodeDbDialectService(LowcodeDbDialectRegistry dialectRegistry) {
    return new DefaultLowcodeDbDialectService(dialectRegistry);
  }

  /**
   * 注册首期简单字符串模板渲染器。
   *
   * @return 代码模板渲染器
   */
  @Bean
  public CodeTemplateRenderer codeTemplateRenderer() {
    return new SimpleStringTemplateRenderer();
  }

  /**
   * 注册默认代码生成器。
   *
   * @param templateRegistry 模板注册表
   * @param templateRenderer 模板渲染器
   * @return 代码生成器
   */
  @Bean
  public CodeGenerator codeGenerator(CodeTemplateRegistry templateRegistry, CodeTemplateRenderer templateRenderer) {
    return new DefaultCodeGenerator(templateRegistry, templateRenderer);
  }

  /**
   * 注册生成文件写入器。
   *
   * @param outputRoot 生成文件输出根目录
   * @return 生成文件写入器
   */
  @Bean
  public GeneratedFileWriter generatedFileWriter(
      @Value("${zhyc.lowcode.generator.output-root:target/lowcode-generated}") String outputRoot) {
    return new FileSystemGeneratedFileWriter(Path.of(outputRoot));
  }
}
