/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.tenant.TenantContext;
import com.zhyc.lowcode.db.LowcodeColumn;
import com.zhyc.lowcode.db.LowcodeDbDialectService;
import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.db.LowcodeTable;
import com.zhyc.lowcode.metadata.converter.LowcodeTableModelConverter;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;
import com.zhyc.lowcode.metadata.domain.LowcodeModelStatus;
import com.zhyc.lowcode.metadata.domain.LowcodePageModel;
import com.zhyc.lowcode.metadata.domain.LowcodePhysicalColumn;
import com.zhyc.lowcode.metadata.domain.LowcodePhysicalTable;
import com.zhyc.lowcode.metadata.domain.LowcodeTableRelation;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import com.zhyc.lowcode.metadata.schema.LowcodeDatabaseSchemaGateway;
import com.zhyc.lowcode.metadata.repository.LowcodeDataSourceRepository;
import com.zhyc.lowcode.metadata.repository.LowcodePageModelRepository;
import com.zhyc.lowcode.metadata.repository.LowcodeTableRelationRepository;
import com.zhyc.lowcode.metadata.repository.LowcodeTableModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 默认低代码元数据服务测试。
 */
class DefaultLowcodeMetadataServiceTest {

  /**
   * 验证保存数据源入口拒绝空数据源定义，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectNullDataSourceWhenSavingWithBusinessCode() {
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), new MemoryTableRepository());

    BusinessException exception = assertThrows(BusinessException.class, () -> service.saveDataSource(null));

    assertEquals("ZHYC_LOWCODE_METADATA_DATASOURCE_REQUIRED", exception.getCode());
    assertEquals("数据源定义不能为空", exception.getMessage());
  }

  /**
   * 验证保存表模型入口拒绝空表模型，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectNullTableModelWhenSavingWithBusinessCode() {
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), new MemoryTableRepository());

    BusinessException exception = assertThrows(BusinessException.class, () -> service.saveTableModel(null));

    assertEquals("ZHYC_LOWCODE_METADATA_TABLE_MODEL_REQUIRED", exception.getCode());
    assertEquals("表模型不能为空", exception.getMessage());
  }

  /**
   * 验证保存表关系入口拒绝空表关系，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectNullTableRelationWhenSavingWithBusinessCode() {
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), new MemoryTableRepository());

    BusinessException exception = assertThrows(BusinessException.class, () -> service.saveTableRelation(null));

    assertEquals("ZHYC_LOWCODE_METADATA_TABLE_RELATION_REQUIRED", exception.getCode());
    assertEquals("表关系不能为空", exception.getMessage());
  }

  /**
   * 验证保存页面模型入口拒绝空页面模型，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectNullPageModelWhenSavingWithBusinessCode() {
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), new MemoryTableRepository());

    BusinessException exception = assertThrows(BusinessException.class, () -> service.savePageModel(null));

    assertEquals("ZHYC_LOWCODE_METADATA_PAGE_MODEL_REQUIRED", exception.getCode());
    assertEquals("页面模型不能为空", exception.getMessage());
  }

  /**
   * 验证保存表模型前会执行领域校验，阻止重复字段进入后续生成流程。
   */
  @Test
  void shouldValidateTableModelBeforeSaving() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), tableRepository);
    LowcodeTableModel table = new LowcodeTableModel(
        1L, "tenant_a", "purchase_order", "采购订单", "pur_order",
        java.util.List.of(
            LowcodeColumnModel.builder("order_no", "订单号", LowcodeFieldType.STRING).length(64).build(),
            LowcodeColumnModel.builder("order_no", "订单编号", LowcodeFieldType.STRING).length(64).build()
        ));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveTableModel(table));

    assertEquals("ZHYC_LOWCODE_METADATA_TABLE_MODEL_INVALID", exception.getCode());
    assertEquals("字段编码不能重复: order_no", exception.getMessage());
    assertEquals(0, tableRepository.savedCount);
  }

  /**
   * 验证保存表模型前会校验数据源主键属于当前租户，避免跨租户数据源被绑定到表模型。
   */
  @Test
  void shouldRejectTableModelDataSourceOutsideTenantBeforeSaving() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    LowcodeMetadataService service = service(dataSourceRepository, tableRepository);
    dataSourceRepository.save(new LowcodeDataSource(
        20L, "tenant_b", "main_mysql", "其他租户主库", LowcodeDatabaseDialect.MYSQL,
        "jdbc:mysql://tenant-b", "root", "secret:b", true));
    LowcodeTableModel table = new LowcodeTableModel(
        1L, "tenant_a", 20L, "purchase_order", "采购订单", "pur_order",
        LowcodeModelStatus.DRAFT,
        List.of(LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
            .primaryKey(true).autoIncrement(true).build()));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveTableModel(table));

    assertEquals("ZHYC_LOWCODE_METADATA_TABLE_DATASOURCE_NOT_FOUND", exception.getCode());
    assertEquals("表模型绑定的数据源不属于当前租户: 20", exception.getMessage());
    assertEquals(0, tableRepository.savedCount);
  }

  /**
   * 验证发布表模型会调用领域发布规则并保存发布后的状态。
   */
  @Test
  void shouldPublishTableModelAndSavePublishedStatus() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), tableRepository);
    LowcodeTableModel table = new LowcodeTableModel(
        1L, "tenant_a", "purchase_order", "采购订单", "pur_order",
        java.util.List.of(LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
            .primaryKey(true)
            .autoIncrement(true)
            .required(true)
            .build()));
    tableRepository.save(table);

    LowcodeTableModel published = service.publishTableModel("tenant_a", "purchase_order");

    assertEquals(LowcodeModelStatus.PUBLISHED, published.getStatus());
    assertEquals(LowcodeModelStatus.PUBLISHED,
        tableRepository.findByTenantIdAndCode("tenant_a", "purchase_order").orElseThrow().getStatus());
  }

  /**
   * 验证发布不存在的表模型时返回清晰错误。
   */
  @Test
  void shouldRejectPublishingMissingTableModel() {
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), new MemoryTableRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.publishTableModel("tenant_a", "missing"));

    assertEquals("低代码表模型不存在: missing", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_TABLE_MODEL_NOT_FOUND", exception.getCode());
  }

  /**
   * 验证可以从当前租户的数据源读取物理表结构并保存为低代码表模型。
   */
  @Test
  void shouldImportTableModelFromTenantDataSourceSchema() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    RecordingSchemaGateway schemaGateway = new RecordingSchemaGateway();
    LowcodeMetadataService service = serviceWithSchema(dataSourceRepository, tableRepository, schemaGateway);
    dataSourceRepository.save(new LowcodeDataSource(10L, "tenant_a", "main", "主数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/zhyc", "platform",
        "secret:lowcode/main/password", true));
    schemaGateway.tableToRead = new LowcodePhysicalTable("pur_request", "采购申请",
        List.of(
            LowcodePhysicalColumn.builder("id", LowcodeFieldType.LONG)
                .primaryKey(true)
                .autoIncrement(true)
                .nullable(false)
                .comment("主键")
                .build(),
            LowcodePhysicalColumn.builder("request_no", LowcodeFieldType.STRING)
                .length(64)
                .nullable(false)
                .comment("申请单号")
                .build()));

    LowcodeTableModel imported = service.importTableModel("tenant_a", 10L, "pur_request",
        "purchase_request", "采购申请");

    assertEquals("purchase_request", imported.getCode());
    assertEquals("采购申请", imported.getName());
    assertEquals("pur_request", imported.getTableName());
    assertEquals(10L, imported.getDataSourceId());
    assertEquals(2, imported.getColumns().size());
    assertEquals("request_no", imported.getColumns().get(1).getCode());
    assertEquals("申请单号", imported.getColumns().get(1).getName());
    assertEquals("tenant_a:10:pur_request", schemaGateway.readTableKey);
    assertEquals(imported, tableRepository.findByTenantIdAndCode("tenant_a", "purchase_request").orElseThrow());
  }

  /**
   * 验证按数据源加载物理表时只返回业务表，过滤系统、开放平台、流程和可视化等平台内部表。
   */
  @Test
  void shouldListOnlyBusinessPhysicalTablesFromDataSource() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    RecordingSchemaGateway schemaGateway = new RecordingSchemaGateway();
    LowcodeMetadataService service = serviceWithSchema(dataSourceRepository, new MemoryTableRepository(),
        schemaGateway);
    dataSourceRepository.save(new LowcodeDataSource(10L, "tenant_a", "main", "主数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/zhyc", "platform",
        "secret:lowcode/main/password", true));
    schemaGateway.tablesToList = List.of(
        new LowcodePhysicalTable("sys_user", "系统用户", List.of()),
        new LowcodePhysicalTable("openapi_app", "开放平台应用", List.of()),
        new LowcodePhysicalTable("visual_report", "可视化报表", List.of()),
        new LowcodePhysicalTable("wf_task", "流程任务", List.of()),
        new LowcodePhysicalTable("pur_request", "采购申请", List.of()),
        new LowcodePhysicalTable("biz_customer", "客户档案", List.of()));

    List<LowcodePhysicalTable> tables = service.listPhysicalTables("tenant_a", 10L);

    assertEquals(2, tables.size());
    assertEquals("pur_request", tables.get(0).getTableName());
    assertEquals("biz_customer", tables.get(1).getTableName());
  }

  /**
   * 验证读取物理表清单时会把当前租户绑定到线程上下文，供密钥解析器按租户读取数据库口令。
   */
  @Test
  void shouldBindTenantContextWhenListingPhysicalTables() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    RecordingSchemaGateway schemaGateway = new RecordingSchemaGateway();
    LowcodeMetadataService service = serviceWithSchema(dataSourceRepository, new MemoryTableRepository(),
        schemaGateway);
    dataSourceRepository.save(new LowcodeDataSource(10L, "tenant_a", "main", "主数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/zhyc", "platform",
        "secret:lowcode/main/password", true));
    schemaGateway.tablesToList = List.of(new LowcodePhysicalTable("pur_request", "采购申请", List.of()));

    List<LowcodePhysicalTable> tables = service.listPhysicalTables("tenant_a", 10L);

    assertEquals(1, tables.size());
    assertEquals("tenant_a", schemaGateway.tenantContextDuringListTables);
    assertNull(TenantContext.getTenantId());
  }

  /**
   * 验证直接导入平台内部物理表会被拒绝，避免绕过前端过滤导入系统表。
   */
  @Test
  void shouldRejectImportingPlatformInternalPhysicalTable() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    RecordingSchemaGateway schemaGateway = new RecordingSchemaGateway();
    LowcodeMetadataService service = serviceWithSchema(dataSourceRepository, new MemoryTableRepository(),
        schemaGateway);
    dataSourceRepository.save(new LowcodeDataSource(10L, "tenant_a", "main", "主数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/zhyc", "platform",
        "secret:lowcode/main/password", true));
    schemaGateway.tableToRead = new LowcodePhysicalTable("sys_user", "系统用户",
        List.of(LowcodePhysicalColumn.builder("id", LowcodeFieldType.LONG)
            .primaryKey(true)
            .nullable(false)
            .build()));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.importTableModel("tenant_a", 10L, "sys_user", "sys_user", "系统用户"));

    assertEquals("ZHYC_LOWCODE_METADATA_PLATFORM_TABLE_UNSUPPORTED", exception.getCode());
    assertEquals("平台内部表不能导入低代码业务模型: sys_user", exception.getMessage());
  }

  /**
   * 验证发布新建模型时会生成建表 DDL 并在模型绑定的数据源上执行。
   */
  @Test
  void shouldExecuteCreateTableDdlWhenPublishingNewModel() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    RecordingSchemaGateway schemaGateway = new RecordingSchemaGateway();
    RecordingDialectService dialectService = new RecordingDialectService();
    LowcodeMetadataService service = serviceWithSchema(dataSourceRepository, tableRepository, schemaGateway,
        dialectService);
    dataSourceRepository.save(new LowcodeDataSource(10L, "tenant_a", "main", "主数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/zhyc", "platform",
        "secret:lowcode/main/password", true));
    tableRepository.save(new LowcodeTableModel(
        1L, "tenant_a", 10L, "purchase_request", "采购申请", "pur_request",
        LowcodeModelStatus.DRAFT,
        List.of(LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
            .primaryKey(true)
            .autoIncrement(true)
            .required(true)
            .build())));

    LowcodeTableModel published = service.publishTableModel("tenant_a", "purchase_request");

    assertEquals(LowcodeModelStatus.PUBLISHED, published.getStatus());
    assertEquals("mysql:pur_request", dialectService.generatedDdlKey);
    assertEquals("tenant_a:10:CREATE TABLE `pur_request` (...);", schemaGateway.executedDdlKey);
  }

  /**
   * 验证可以按租户和编码查询数据源。
   */
  @Test
  void shouldGetDataSourceByTenantAndCode() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    LowcodeMetadataService service = service(dataSourceRepository, new MemoryTableRepository());
    dataSourceRepository.save(new LowcodeDataSource(1L, "tenant_a", "main", "主数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/zhyc", "root", "secret/main", true));

    LowcodeDataSource dataSource = service.getDataSource("tenant_a", "main");

    assertEquals("主数据源", dataSource.getName());
  }

  /**
   * 验证保存启用数据源时会校验连接配置，避免非法 JDBC 地址进入后续建模和生成流程。
   */
  @Test
  void shouldRejectEnabledDataSourceWhenJdbcUrlInvalidBeforeSaving() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    LowcodeMetadataService service = service(dataSourceRepository, new MemoryTableRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveDataSource(new LowcodeDataSource(1L, "tenant_a", "main", "主数据源",
            LowcodeDatabaseDialect.MYSQL, "http://127.0.0.1:3306/zhyc", "root", "secret/main", true)));

    assertEquals("JDBC 连接地址必须以 jdbc: 开头", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_JDBC_URL_INVALID", exception.getCode());
    assertEquals(0, dataSourceRepository.dataSources.size());
  }

  /**
   * 验证启用数据源的 JDBC 地址必须与数据库方言匹配，避免达梦数据源误填其他数据库地址。
   */
  @Test
  void shouldRejectEnabledDataSourceWhenJdbcUrlDoesNotMatchDialect() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    LowcodeMetadataService service = service(dataSourceRepository, new MemoryTableRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveDataSource(new LowcodeDataSource(1L, "tenant_a", "dm_main", "达梦主库",
            LowcodeDatabaseDialect.DM, "jdbc:mysql://127.0.0.1:3306/zhyc", "SYSDBA", "secret/dm", true)));

    assertEquals("JDBC 连接地址必须与数据库类型 dm 匹配，前缀应为 jdbc:dm:", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_JDBC_URL_INVALID", exception.getCode());
    assertEquals(0, dataSourceRepository.dataSources.size());
  }

  /**
   * 验证保存启用数据源时会拒绝缺失数据库类型的对象，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectEnabledDataSourceWhenDialectMissingWithBusinessCode() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    LowcodeMetadataService service = service(dataSourceRepository, new MemoryTableRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveDataSource(new MissingDialectDataSource()));

    assertEquals("数据库类型不能为空", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_DB_DIALECT_REQUIRED", exception.getCode());
    assertEquals(0, dataSourceRepository.dataSources.size());
  }

  /**
   * 验证查询不存在的数据源时返回清晰错误。
   */
  @Test
  void shouldRejectGettingMissingDataSource() {
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), new MemoryTableRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.getDataSource("tenant_a", "missing"));

    assertEquals("低代码数据源不存在: missing", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_DATASOURCE_NOT_FOUND", exception.getCode());
  }

  /**
   * 验证查询元数据时租户编码不能为空，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectBlankTenantWhenListingDataSources() {
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), new MemoryTableRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.listDataSources(" "));

    assertEquals("租户业务编码不能为空", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_TENANT_REQUIRED", exception.getCode());
  }

  /**
   * 验证可以按租户和编码查询表模型。
   */
  @Test
  void shouldGetTableModelByTenantAndCode() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), tableRepository);
    LowcodeTableModel table = new LowcodeTableModel(
        1L, "tenant_a", "purchase_order", "采购订单", "pur_order",
        java.util.List.of(LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
            .primaryKey(true)
            .build()));
    tableRepository.save(table);

    LowcodeTableModel found = service.getTableModel("tenant_a", "purchase_order");

    assertEquals("采购订单", found.getName());
  }

  /**
   * 验证只返回指定租户下的数据源列表。
   */
  @Test
  void shouldListDataSourcesByTenant() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    LowcodeMetadataService service = service(dataSourceRepository, new MemoryTableRepository());
    dataSourceRepository.save(new LowcodeDataSource(1L, "tenant_a", "main", "主数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/a", "root", null, true));
    dataSourceRepository.save(new LowcodeDataSource(2L, "tenant_b", "other", "其他数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/b", "root", null, true));

    List<LowcodeDataSource> dataSources = service.listDataSources("tenant_a");

    assertEquals(1, dataSources.size());
    assertEquals("main", dataSources.get(0).getCode());
  }

  /**
   * 验证数据源连接测试会真实读取数据库结构清单，并在读取前绑定租户上下文。
   */
  @Test
  void shouldTestEnabledDataSourceConnectionByConfiguration() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    RecordingSchemaGateway schemaGateway = new RecordingSchemaGateway();
    LowcodeMetadataService service = serviceWithSchema(dataSourceRepository, new MemoryTableRepository(),
        schemaGateway);
    dataSourceRepository.save(new LowcodeDataSource(1L, "tenant_a", "main", "主数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/zhyc", "platform",
        "secret:lowcode/main/password", true));
    schemaGateway.tablesToList = List.of(new LowcodePhysicalTable("pur_request", "采购申请", List.of()));

    LowcodeDataSourceConnectionTestResult result = service.testDataSourceConnection(" tenant_a ", " main ");

    assertEquals("main", result.getCode());
    assertEquals(true, result.isSuccess());
    assertEquals("数据源连接配置预检查通过，数据源连接测试通过", result.getMessage());
    assertEquals("tenant_a", schemaGateway.tenantContextDuringListTables);
    assertNull(TenantContext.getTenantId());
  }

  /**
   * 验证连接测试会把数据库结构网关返回的业务异常转成可读失败结果。
   */
  @Test
  void shouldReturnReadableFailureWhenConnectionTestCannotReadSchema() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    RecordingSchemaGateway schemaGateway = new RecordingSchemaGateway();
    LowcodeMetadataService service = serviceWithSchema(dataSourceRepository, new MemoryTableRepository(),
        schemaGateway);
    dataSourceRepository.save(new LowcodeDataSource(1L, "tenant_a", "main", "主数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/zhyc", "platform",
        "secret:lowcode/main/password", true));
    schemaGateway.listTablesException = new BusinessException("TEST_SCHEMA_FAILED", "未配置密钥解析器");

    LowcodeDataSourceConnectionTestResult result = service.testDataSourceConnection("tenant_a", "main");

    assertEquals("main", result.getCode());
    assertEquals(false, result.isSuccess());
    assertEquals("数据源连接测试失败：未配置密钥解析器", result.getMessage());
    assertEquals("tenant_a", schemaGateway.tenantContextDuringListTables);
    assertNull(TenantContext.getTenantId());
  }

  /**
   * 验证停用数据源不会进入连接测试。
   */
  @Test
  void shouldRejectConnectionTestWhenDataSourceDisabled() {
    MemoryDataSourceRepository dataSourceRepository = new MemoryDataSourceRepository();
    LowcodeMetadataService service = service(dataSourceRepository, new MemoryTableRepository());
    dataSourceRepository.save(new LowcodeDataSource(1L, "tenant_a", "main", "主数据源",
        LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/zhyc", "platform",
        "secret:lowcode/main/password", false));

    LowcodeDataSourceConnectionTestResult result = service.testDataSourceConnection("tenant_a", "main");

    assertEquals("main", result.getCode());
    assertEquals(false, result.isSuccess());
    assertEquals("数据源未启用，不能执行连接测试", result.getMessage());
  }

  /**
   * 验证只返回指定租户下的表模型列表。
   */
  @Test
  void shouldListTableModelsByTenant() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    LowcodeMetadataService service = service(new MemoryDataSourceRepository(), tableRepository);
    tableRepository.save(new LowcodeTableModel(
        1L, "tenant_a", "purchase_order", "采购订单", "pur_order",
        java.util.List.of(LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
            .primaryKey(true)
            .build())));
    tableRepository.save(new LowcodeTableModel(
        2L, "tenant_b", "supplier", "供应商", "pur_supplier",
        java.util.List.of(LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
            .primaryKey(true)
            .build())));

    List<LowcodeTableModel> tableModels = service.listTableModels("tenant_a");

    assertEquals(1, tableModels.size());
    assertEquals("purchase_order", tableModels.get(0).getCode());
  }

  /**
   * 验证表关系模型可以按租户保存和查询。
   */
  @Test
  void shouldSaveAndListTableRelationsByTenant() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryRelationRepository relationRepository = new MemoryRelationRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, relationRepository,
        new MemoryPageModelRepository());
    tableRepository.save(table(1L, "tenant_a", "purchase_request", "id"));
    tableRepository.save(table(2L, "tenant_a", "purchase_item", "request_id"));
    tableRepository.save(table(3L, "tenant_b", "sale_order", "id"));
    tableRepository.save(table(4L, "tenant_b", "sale_item", "order_id"));

    service.saveTableRelation(new LowcodeTableRelation(
        null, "tenant_a", 1L, 2L, "ONE_TO_MANY", "id", "request_id"));
    service.saveTableRelation(new LowcodeTableRelation(
        null, "tenant_b", 3L, 4L, "ONE_TO_ONE", "id", "order_id"));

    List<LowcodeTableRelation> relations = service.listTableRelations("tenant_a");

    assertEquals(1, relations.size());
    assertEquals("ONE_TO_MANY", relations.get(0).getRelationType());
  }

  /**
   * 验证表关系模型会拒绝不受支持的关系类型。
   */
  @Test
  void shouldRejectUnsupportedTableRelationType() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> new LowcodeTableRelation(
            null, "tenant_a", 1L, 2L, "MASTER_DETAIL", "id", "request_id"));

    assertEquals("表关系类型不支持: MASTER_DETAIL", exception.getMessage());
  }

  /**
   * 验证保存表关系时会拒绝非当前租户下的主表模型，避免跨租户关系进入后续生成流程。
   */
  @Test
  void shouldRejectRelationWhenMainTableNotInTenant() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryRelationRepository relationRepository = new MemoryRelationRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, relationRepository,
        new MemoryPageModelRepository());
    tableRepository.save(table(1L, "tenant_b", "purchase_order", "id"));
    tableRepository.save(table(2L, "tenant_a", "purchase_item", "order_id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveTableRelation(new LowcodeTableRelation(
            null, "tenant_a", 1L, 2L, "ONE_TO_MANY", "id", "order_id")));

    assertEquals("主表模型不存在: 1", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_RELATION_MAIN_TABLE_NOT_FOUND", exception.getCode());
    assertEquals(0, relationRepository.relations.size());
  }

  /**
   * 验证保存表关系时会校验关联字段存在，避免生成不存在的联表查询和页面联动字段。
   */
  @Test
  void shouldRejectRelationWhenJoinColumnMissing() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryRelationRepository relationRepository = new MemoryRelationRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, relationRepository,
        new MemoryPageModelRepository());
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));
    tableRepository.save(table(2L, "tenant_a", "purchase_item", "order_id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveTableRelation(new LowcodeTableRelation(
            null, "tenant_a", 1L, 2L, "ONE_TO_MANY", "missing_id", "order_id")));

    assertEquals("主表关联字段不存在: missing_id", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_RELATION_JOIN_COLUMN_NOT_FOUND", exception.getCode());
    assertEquals(0, relationRepository.relations.size());
  }

  /**
   * 验证保存表关系时会校验子表引用字段存在，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectRelationWhenRefColumnMissingWithBusinessCode() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryRelationRepository relationRepository = new MemoryRelationRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, relationRepository,
        new MemoryPageModelRepository());
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));
    tableRepository.save(table(2L, "tenant_a", "purchase_item", "order_id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveTableRelation(new LowcodeTableRelation(
            null, "tenant_a", 1L, 2L, "ONE_TO_MANY", "id", "missing_order_id")));

    assertEquals("子表引用字段不存在: missing_order_id", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_RELATION_REF_COLUMN_NOT_FOUND", exception.getCode());
    assertEquals(0, relationRepository.relations.size());
  }

  /**
   * 验证页面模型可以按租户保存和查询。
   */
  @Test
  void shouldSaveAndListPageModelsByTenant() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));
    tableRepository.save(table(2L, "tenant_b", "sale_order", "id"));

    service.savePageModel(new LowcodePageModel(
        null, "tenant_a", 1L, "LIST", "/purchase/order",
        "zhyc-base-vue/src/views/purchase/order/index.vue", "TABLE"));
    service.savePageModel(new LowcodePageModel(
        null, "tenant_b", 2L, "MOBILE", "/pages/sale/order/list",
        "zhyc-base-uniapp/src/pages/sale/order/list.vue", "UNIAPP_PAGE"));

    List<LowcodePageModel> pageModels = service.listPageModels("tenant_a");

    assertEquals(1, pageModels.size());
    assertEquals("LIST", pageModels.get(0).getPageType());
  }

  /**
   * 验证保存页面模型时会拒绝不存在或非当前租户的表模型，避免后续生成孤立页面。
   */
  @Test
  void shouldRejectPageModelWhenTableNotInTenant() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_b", "purchase_order", "id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.savePageModel(new LowcodePageModel(
            null, "tenant_a", 1L, "LIST", "/purchase/order",
            "zhyc-base-vue/src/views/purchase/order/index.vue", "TABLE")));

    assertEquals("页面表模型不存在: 1", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_PAGE_TABLE_NOT_FOUND", exception.getCode());
    assertEquals(0, pageModelRepository.pageModels.size());
  }

  /**
   * 验证后台页面模型必须使用后台管理端组件路径，避免后台生成目标混入移动端页面。
   */
  @Test
  void shouldRejectAdminPageModelWhenComponentPathIsNotAdminView() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.savePageModel(new LowcodePageModel(
            null, "tenant_a", 1L, "LIST", "/purchase/order",
            "zhyc-base-uniapp/src/pages/purchase/order/list.vue", "TABLE")));

    assertEquals("后台页面组件路径必须位于 zhyc-base-vue/src/views/", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_ADMIN_COMPONENT_PATH_INVALID", exception.getCode());
    assertEquals(0, pageModelRepository.pageModels.size());
  }

  /**
   * 验证后台页面模型不能使用 uni-app 页面路由。
   */
  @Test
  void shouldRejectAdminPageModelWhenRoutePathIsMobileRoute() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.savePageModel(new LowcodePageModel(
            null, "tenant_a", 1L, "LIST", "/pages/purchase/order/list",
            "zhyc-base-vue/src/views/purchase/order/index.vue", "TABLE")));

    assertEquals("后台页面路由不能以 /pages/ 开头", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_ADMIN_ROUTE_INVALID", exception.getCode());
    assertEquals(0, pageModelRepository.pageModels.size());
  }

  /**
   * 验证页面路由必须使用绝对路径。
   */
  @Test
  void shouldRejectPageModelWhenRoutePathIsNotAbsolute() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.savePageModel(new LowcodePageModel(
            null, "tenant_a", 1L, "LIST", "purchase/order",
            "zhyc-base-vue/src/views/purchase/order/index.vue", "TABLE")));

    assertEquals("页面路由必须以 / 开头", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_PAGE_ROUTE_REQUIRED_ABSOLUTE", exception.getCode());
    assertEquals(0, pageModelRepository.pageModels.size());
  }

  /**
   * 验证页面组件路径必须指向 Vue 单文件组件。
   */
  @Test
  void shouldRejectPageModelWhenComponentPathIsNotVueFile() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.savePageModel(new LowcodePageModel(
            null, "tenant_a", 1L, "LIST", "/purchase/order",
            "zhyc-base-vue/src/views/purchase/order/index.ts", "TABLE")));

    assertEquals("页面组件路径必须以 .vue 结尾", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_PAGE_COMPONENT_MUST_BE_VUE", exception.getCode());
    assertEquals(0, pageModelRepository.pageModels.size());
  }

  /**
   * 验证保存页面模型时会拒绝服务入口处不受支持的页面类型，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectUnsupportedPageTypeAtServiceEntryWithBusinessCode() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.savePageModel(new UnsupportedPageTypeModel()));

    assertEquals("页面类型不支持: KANBAN", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_PAGE_TYPE_UNSUPPORTED", exception.getCode());
    assertEquals(0, pageModelRepository.pageModels.size());
  }

  /**
   * 验证后台页面模型的页面类型和布局类型必须匹配，避免生成错误页面结构。
   *
   * @param pageType 页面类型
   * @param layoutType 错误布局类型
   * @param expectedMessage 预期错误消息
   * @param expectedCode 预期业务错误码
   */
  @ParameterizedTest
  @CsvSource({
      "LIST,FORM,列表页布局类型必须为 TABLE,ZHYC_LOWCODE_METADATA_ADMIN_LAYOUT_INVALID",
      "FORM,TABLE,表单页布局类型必须为 FORM,ZHYC_LOWCODE_METADATA_ADMIN_LAYOUT_INVALID",
      "DETAIL,TABLE,详情页布局类型必须为 DESCRIPTIONS,ZHYC_LOWCODE_METADATA_ADMIN_LAYOUT_INVALID"
  })
  void shouldRejectAdminPageModelWhenLayoutTypeMismatch(String pageType, String layoutType, String expectedMessage,
                                                        String expectedCode) {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.savePageModel(new LowcodePageModel(
            null, "tenant_a", 1L, pageType, "/purchase/order",
            "zhyc-base-vue/src/views/purchase/order/index.vue", layoutType)));

    assertEquals(expectedMessage, exception.getMessage());
    assertEquals(expectedCode, exception.getCode());
    assertEquals(0, pageModelRepository.pageModels.size());
  }

  /**
   * 验证移动端页面模型必须使用 uni-app 组件路径和移动端布局，避免生成目标错配。
   */
  @Test
  void shouldRejectMobilePageModelWhenLayoutOrComponentPathInvalid() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.savePageModel(new LowcodePageModel(
            null, "tenant_a", 1L, "MOBILE", "/pages/purchase/order/list",
            "zhyc-base-vue/src/views/purchase/order/index.vue", "TABLE")));

    assertEquals("移动端页面组件路径必须位于 zhyc-base-uniapp/src/pages/", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_MOBILE_COMPONENT_PATH_INVALID", exception.getCode());
    assertEquals(0, pageModelRepository.pageModels.size());
  }

  /**
   * 验证移动端页面模型必须使用移动端布局类型。
   */
  @Test
  void shouldRejectMobilePageModelWhenLayoutTypeInvalid() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.savePageModel(new LowcodePageModel(
            null, "tenant_a", 1L, "MOBILE", "/pages/purchase/order/list",
            "zhyc-base-uniapp/src/pages/purchase/order/list.vue", "TABLE")));

    assertEquals("移动端页面布局类型必须为 UNIAPP_PAGE", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_MOBILE_LAYOUT_INVALID", exception.getCode());
    assertEquals(0, pageModelRepository.pageModels.size());
  }

  /**
   * 验证移动端页面模型必须使用 uni-app 页面路由。
   */
  @Test
  void shouldRejectMobilePageModelWhenRoutePathIsNotMobileRoute() {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.savePageModel(new LowcodePageModel(
            null, "tenant_a", 1L, "MOBILE", "/purchase/order",
            "zhyc-base-uniapp/src/pages/purchase/order/list.vue", "UNIAPP_PAGE")));

    assertEquals("移动端页面路由必须以 /pages/ 开头", exception.getMessage());
    assertEquals("ZHYC_LOWCODE_METADATA_MOBILE_ROUTE_INVALID", exception.getCode());
    assertEquals(0, pageModelRepository.pageModels.size());
  }

  /**
   * 验证移动端表单和详情页面模型使用 uni-app 规则校验，支持移动端多页面生成。
   *
   * @param pageType 移动端页面类型
   * @param routePath 移动端页面路由
   * @param componentPath 移动端组件路径
   */
  @ParameterizedTest
  @CsvSource({
      "MOBILE_FORM,/pages/purchase/order/form,zhyc-base-uniapp/src/pages/purchase/order/form.vue",
      "MOBILE_DETAIL,/pages/purchase/order/detail,zhyc-base-uniapp/src/pages/purchase/order/detail.vue"
  })
  void shouldSaveMobileFormAndDetailPageModels(String pageType, String routePath, String componentPath) {
    MemoryTableRepository tableRepository = new MemoryTableRepository();
    MemoryPageModelRepository pageModelRepository = new MemoryPageModelRepository();
    LowcodeMetadataService service = new DefaultLowcodeMetadataService(
        new MemoryDataSourceRepository(), tableRepository, new MemoryRelationRepository(),
        pageModelRepository);
    tableRepository.save(table(1L, "tenant_a", "purchase_order", "id"));

    service.savePageModel(new LowcodePageModel(
        null, "tenant_a", 1L, pageType, routePath, componentPath, "UNIAPP_PAGE"));

    assertEquals(1, pageModelRepository.pageModels.size());
    assertEquals(pageType, pageModelRepository.pageModels.get(0).getPageType());
  }

  /**
   * 验证页面模型会拒绝不受支持的页面类型。
   */
  @Test
  void shouldRejectUnsupportedPageType() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> new LowcodePageModel(
            null, "tenant_a", 1L, "KANBAN", "/purchase/order",
            "zhyc-base-vue/src/views/purchase/order/index.vue", "TABLE"));

    assertEquals("页面类型不支持: KANBAN", exception.getMessage());
  }

  /**
   * 验证页面模型会拒绝不受支持的页面布局类型。
   */
  @Test
  void shouldRejectUnsupportedPageLayoutType() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> new LowcodePageModel(
            null, "tenant_a", 1L, "LIST", "/purchase/order",
            "zhyc-base-vue/src/views/purchase/order/index.vue", "CARD"));

    assertEquals("页面布局类型不支持: CARD", exception.getMessage());
  }

  private static LowcodeMetadataService service(LowcodeDataSourceRepository dataSourceRepository,
                                                LowcodeTableModelRepository tableRepository) {
    return new DefaultLowcodeMetadataService(dataSourceRepository, tableRepository, new MemoryRelationRepository(),
        new MemoryPageModelRepository());
  }

  private static LowcodeMetadataService serviceWithSchema(LowcodeDataSourceRepository dataSourceRepository,
                                                          LowcodeTableModelRepository tableRepository,
                                                          RecordingSchemaGateway schemaGateway) {
    return serviceWithSchema(dataSourceRepository, tableRepository, schemaGateway, new RecordingDialectService());
  }

  private static LowcodeMetadataService serviceWithSchema(LowcodeDataSourceRepository dataSourceRepository,
                                                          LowcodeTableModelRepository tableRepository,
                                                          RecordingSchemaGateway schemaGateway,
                                                          RecordingDialectService dialectService) {
    return new DefaultLowcodeMetadataService(dataSourceRepository, tableRepository, new MemoryRelationRepository(),
        new MemoryPageModelRepository(), schemaGateway, dialectService, new LowcodeTableModelConverter());
  }

  private static LowcodeTableModel table(Long id, String tenantId, String code, String columnCode) {
    return new LowcodeTableModel(
        id, tenantId, code, code, "lc_" + code,
        java.util.List.of(LowcodeColumnModel.builder(columnCode, columnCode, LowcodeFieldType.LONG).build()));
  }

  /**
   * 测试用缺失数据库类型的数据源，用于覆盖服务入口防御性校验。
   */
  private static class MissingDialectDataSource extends LowcodeDataSource {

    /**
     * 创建测试数据源。
     */
    MissingDialectDataSource() {
      super(1L, "tenant_a", "main", "主数据源",
          LowcodeDatabaseDialect.MYSQL, "jdbc:mysql://127.0.0.1:3306/zhyc", "root", "secret/main", true);
    }

    @Override
    public LowcodeDatabaseDialect getDialect() {
      return null;
    }
  }

  /**
   * 测试用未知页面类型模型，用于覆盖服务入口防御性校验。
   */
  private static class UnsupportedPageTypeModel extends LowcodePageModel {

    /**
     * 创建测试页面模型。
     */
    UnsupportedPageTypeModel() {
      super(null, "tenant_a", 1L, "LIST", "/purchase/order",
          "zhyc-base-vue/src/views/purchase/order/index.vue", "TABLE");
    }

    @Override
    public String getPageType() {
      return "KANBAN";
    }
  }

  /**
   * 测试用数据源仓储。
   */
  private static class MemoryDataSourceRepository implements LowcodeDataSourceRepository {

    /** 已保存数据源集合。 */
    private final Map<String, LowcodeDataSource> dataSources = new HashMap<>();

    @Override
    public LowcodeDataSource save(LowcodeDataSource dataSource) {
      dataSources.put(key(dataSource.getTenantId(), dataSource.getCode()), dataSource);
      return dataSource;
    }

    @Override
    public Optional<LowcodeDataSource> findByTenantIdAndCode(String tenantId, String code) {
      return Optional.ofNullable(dataSources.get(key(tenantId, code)));
    }

    @Override
    public Optional<LowcodeDataSource> findByTenantIdAndId(String tenantId, Long id) {
      return dataSources.values().stream()
          .filter(dataSource -> dataSource.getTenantId().equals(tenantId))
          .filter(dataSource -> id.equals(dataSource.getId()))
          .findFirst();
    }

    @Override
    public List<LowcodeDataSource> findByTenantId(String tenantId) {
      return dataSources.values().stream()
          .filter(dataSource -> dataSource.getTenantId().equals(tenantId))
          .toList();
    }

    private String key(String tenantId, String code) {
      return tenantId + ":" + code;
    }
  }

  /**
   * 测试用表模型仓储。
   */
  private static class MemoryTableRepository implements LowcodeTableModelRepository {

    /** 已保存表模型集合。 */
    private final Map<String, LowcodeTableModel> tables = new HashMap<>();
    /** 保存次数。 */
    private int savedCount;

    @Override
    public LowcodeTableModel save(LowcodeTableModel tableModel) {
      savedCount++;
      tables.put(key(tableModel.getTenantId(), tableModel.getCode()), tableModel);
      return tableModel;
    }

    @Override
    public Optional<LowcodeTableModel> findByTenantIdAndCode(String tenantId, String code) {
      return Optional.ofNullable(tables.get(key(tenantId, code)));
    }

    @Override
    public Optional<LowcodeTableModel> findByTenantIdAndId(String tenantId, Long id) {
      return tables.values().stream()
          .filter(table -> table.getTenantId().equals(tenantId))
          .filter(table -> id.equals(table.getId()))
          .findFirst();
    }

    @Override
    public List<LowcodeTableModel> findByTenantId(String tenantId) {
      return tables.values().stream()
          .filter(table -> table.getTenantId().equals(tenantId))
          .toList();
    }

    private String key(String tenantId, String code) {
      return tenantId + ":" + code;
    }
  }

  /**
   * 测试用表关系仓储。
   */
  private static class MemoryRelationRepository implements LowcodeTableRelationRepository {

    /** 已保存表关系集合。 */
    private final List<LowcodeTableRelation> relations = new java.util.ArrayList<>();

    @Override
    public LowcodeTableRelation save(LowcodeTableRelation relation) {
      relations.add(relation);
      return relation;
    }

    @Override
    public List<LowcodeTableRelation> findByTenantId(String tenantId) {
      return relations.stream()
          .filter(relation -> relation.getTenantId().equals(tenantId))
          .toList();
    }
  }

  /**
   * 测试用页面模型仓储。
   */
  private static class MemoryPageModelRepository implements LowcodePageModelRepository {

    /** 已保存页面模型集合。 */
    private final List<LowcodePageModel> pageModels = new java.util.ArrayList<>();

    @Override
    public LowcodePageModel save(LowcodePageModel pageModel) {
      pageModels.add(pageModel);
      return pageModel;
    }

    @Override
    public List<LowcodePageModel> findByTenantId(String tenantId) {
      return pageModels.stream()
          .filter(pageModel -> pageModel.getTenantId().equals(tenantId))
          .toList();
    }
  }

  /**
   * 测试用数据库结构网关，记录读取和执行动作。
   */
  private static class RecordingSchemaGateway implements LowcodeDatabaseSchemaGateway {

    /** 预设返回的物理表结构。 */
    private LowcodePhysicalTable tableToRead;
    /** 预设返回的物理表清单。 */
    private List<LowcodePhysicalTable> tablesToList = List.of();
    /** 读取表结构调用标记。 */
    private String readTableKey;
    /** 执行 DDL 调用标记。 */
    private String executedDdlKey;
    /** 读取表清单时的租户上下文。 */
    private String tenantContextDuringListTables;
    /** 读取表清单时主动抛出的业务异常。 */
    private BusinessException listTablesException;
    /** 读取表结构时的租户上下文。 */
    private String tenantContextDuringReadTable;
    /** 执行 DDL 时的租户上下文。 */
    private String tenantContextDuringExecuteDdl;

    @Override
    public List<LowcodePhysicalTable> listTables(LowcodeDataSource dataSource) {
      tenantContextDuringListTables = TenantContext.getTenantId();
      if (listTablesException != null) {
        throw listTablesException;
      }
      return tablesToList.isEmpty() && tableToRead != null ? List.of(tableToRead) : tablesToList;
    }

    @Override
    public LowcodePhysicalTable readTable(LowcodeDataSource dataSource, String tableName) {
      tenantContextDuringReadTable = TenantContext.getTenantId();
      readTableKey = dataSource.getTenantId() + ":" + dataSource.getId() + ":" + tableName;
      return tableToRead;
    }

    @Override
    public void executeDdl(LowcodeDataSource dataSource, String ddl) {
      tenantContextDuringExecuteDdl = TenantContext.getTenantId();
      executedDdlKey = dataSource.getTenantId() + ":" + dataSource.getId() + ":" + ddl;
    }
  }

  /**
   * 测试用数据库方言服务，记录建表 DDL 生成请求。
   */
  private static class RecordingDialectService implements LowcodeDbDialectService {

    /** 生成 DDL 调用标记。 */
    private String generatedDdlKey;

    @Override
    public String generateCreateTable(String dialectCode, LowcodeTable table) {
      generatedDdlKey = dialectCode + ":" + table.getName();
      return "CREATE TABLE `" + table.getName() + "` (...);";
    }

    @Override
    public String mapFieldType(String dialectCode, LowcodeColumn column) {
      return column.getFieldType().name();
    }

    @Override
    public String applyPagination(String dialectCode, String sql, long offset, long pageSize) {
      return sql + " LIMIT " + offset + ", " + pageSize;
    }

    @Override
    public List<String> listDdlDialectCodes() {
      return List.of("mysql");
    }

    @Override
    public List<String> listFieldTypeDialectCodes() {
      return List.of("mysql");
    }

    @Override
    public List<String> listPaginationDialectCodes() {
      return List.of("mysql");
    }
  }
}
