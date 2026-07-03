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
import com.zhyc.lowcode.db.LowcodeTable;
import com.zhyc.lowcode.metadata.converter.LowcodeTableModelConverter;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;
import com.zhyc.lowcode.metadata.domain.LowcodePageModel;
import com.zhyc.lowcode.metadata.domain.LowcodePhysicalColumn;
import com.zhyc.lowcode.metadata.domain.LowcodePhysicalTable;
import com.zhyc.lowcode.metadata.domain.LowcodeTableRelation;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import com.zhyc.lowcode.metadata.schema.LowcodeDatabaseSchemaGateway;
import com.zhyc.lowcode.metadata.repository.LowcodePageModelRepository;
import com.zhyc.lowcode.metadata.repository.LowcodeDataSourceRepository;
import com.zhyc.lowcode.metadata.repository.LowcodeTableRelationRepository;
import com.zhyc.lowcode.metadata.repository.LowcodeTableModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 默认低代码元数据服务实现。
 */
@Service
public class DefaultLowcodeMetadataService implements LowcodeMetadataService {

  /** 租户业务编码缺失错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_LOWCODE_METADATA_TENANT_REQUIRED";
  /** 数据源编码缺失错误码。 */
  private static final String ERROR_DATASOURCE_CODE_REQUIRED = "ZHYC_LOWCODE_METADATA_DATASOURCE_CODE_REQUIRED";
  /** 表模型编码缺失错误码。 */
  private static final String ERROR_TABLE_MODEL_CODE_REQUIRED = "ZHYC_LOWCODE_METADATA_TABLE_MODEL_CODE_REQUIRED";
  /** 数据源不存在错误码。 */
  private static final String ERROR_DATASOURCE_NOT_FOUND = "ZHYC_LOWCODE_METADATA_DATASOURCE_NOT_FOUND";
  /** 表模型不存在错误码。 */
  private static final String ERROR_TABLE_MODEL_NOT_FOUND = "ZHYC_LOWCODE_METADATA_TABLE_MODEL_NOT_FOUND";
  /** 表模型绑定数据源不存在错误码。 */
  private static final String ERROR_TABLE_DATASOURCE_NOT_FOUND = "ZHYC_LOWCODE_METADATA_TABLE_DATASOURCE_NOT_FOUND";
  /** 表关系主表不存在错误码。 */
  private static final String ERROR_RELATION_MAIN_TABLE_NOT_FOUND =
      "ZHYC_LOWCODE_METADATA_RELATION_MAIN_TABLE_NOT_FOUND";
  /** 表关系子表不存在错误码。 */
  private static final String ERROR_RELATION_SUB_TABLE_NOT_FOUND =
      "ZHYC_LOWCODE_METADATA_RELATION_SUB_TABLE_NOT_FOUND";
  /** 表关系主表关联字段不存在错误码。 */
  private static final String ERROR_RELATION_JOIN_COLUMN_NOT_FOUND =
      "ZHYC_LOWCODE_METADATA_RELATION_JOIN_COLUMN_NOT_FOUND";
  /** 表关系子表引用字段不存在错误码。 */
  private static final String ERROR_RELATION_REF_COLUMN_NOT_FOUND =
      "ZHYC_LOWCODE_METADATA_RELATION_REF_COLUMN_NOT_FOUND";
  /** 页面绑定表模型不存在错误码。 */
  private static final String ERROR_PAGE_TABLE_NOT_FOUND = "ZHYC_LOWCODE_METADATA_PAGE_TABLE_NOT_FOUND";
  /** 页面路由不是绝对路径错误码。 */
  private static final String ERROR_PAGE_ROUTE_REQUIRED_ABSOLUTE =
      "ZHYC_LOWCODE_METADATA_PAGE_ROUTE_REQUIRED_ABSOLUTE";
  /** 后台页面组件路径非法错误码。 */
  private static final String ERROR_ADMIN_COMPONENT_PATH_INVALID =
      "ZHYC_LOWCODE_METADATA_ADMIN_COMPONENT_PATH_INVALID";
  /** 移动端页面布局非法错误码。 */
  private static final String ERROR_MOBILE_LAYOUT_INVALID = "ZHYC_LOWCODE_METADATA_MOBILE_LAYOUT_INVALID";
  /** 页面组件不是 Vue 单文件组件错误码。 */
  private static final String ERROR_PAGE_COMPONENT_MUST_BE_VUE =
      "ZHYC_LOWCODE_METADATA_PAGE_COMPONENT_MUST_BE_VUE";
  /** 移动端页面路由非法错误码。 */
  private static final String ERROR_MOBILE_ROUTE_INVALID = "ZHYC_LOWCODE_METADATA_MOBILE_ROUTE_INVALID";
  /** 移动端页面组件路径非法错误码。 */
  private static final String ERROR_MOBILE_COMPONENT_PATH_INVALID =
      "ZHYC_LOWCODE_METADATA_MOBILE_COMPONENT_PATH_INVALID";
  /** 后台页面路由非法错误码。 */
  private static final String ERROR_ADMIN_ROUTE_INVALID = "ZHYC_LOWCODE_METADATA_ADMIN_ROUTE_INVALID";
  /** 后台页面布局非法错误码。 */
  private static final String ERROR_ADMIN_LAYOUT_INVALID = "ZHYC_LOWCODE_METADATA_ADMIN_LAYOUT_INVALID";
  /** JDBC 连接地址缺失错误码。 */
  private static final String ERROR_JDBC_URL_REQUIRED = "ZHYC_LOWCODE_METADATA_JDBC_URL_REQUIRED";
  /** JDBC 连接地址格式非法错误码。 */
  private static final String ERROR_JDBC_URL_INVALID = "ZHYC_LOWCODE_METADATA_JDBC_URL_INVALID";
  /** 数据库用户名缺失错误码。 */
  private static final String ERROR_DB_USERNAME_REQUIRED = "ZHYC_LOWCODE_METADATA_DB_USERNAME_REQUIRED";
  /** 数据库口令密钥引用缺失错误码。 */
  private static final String ERROR_DB_PASSWORD_SECRET_REQUIRED = "ZHYC_LOWCODE_METADATA_DB_PASSWORD_SECRET_REQUIRED";
  /** 数据库类型缺失错误码。 */
  private static final String ERROR_DB_DIALECT_REQUIRED = "ZHYC_LOWCODE_METADATA_DB_DIALECT_REQUIRED";
  /** 页面类型不支持错误码。 */
  private static final String ERROR_PAGE_TYPE_UNSUPPORTED = "ZHYC_LOWCODE_METADATA_PAGE_TYPE_UNSUPPORTED";
  /** 数据源定义缺失错误码。 */
  private static final String ERROR_DATASOURCE_REQUIRED = "ZHYC_LOWCODE_METADATA_DATASOURCE_REQUIRED";
  /** 表模型缺失错误码。 */
  private static final String ERROR_TABLE_MODEL_REQUIRED = "ZHYC_LOWCODE_METADATA_TABLE_MODEL_REQUIRED";
  /** 表模型领域校验失败错误码。 */
  private static final String ERROR_TABLE_MODEL_INVALID = "ZHYC_LOWCODE_METADATA_TABLE_MODEL_INVALID";
  /** 表关系缺失错误码。 */
  private static final String ERROR_TABLE_RELATION_REQUIRED = "ZHYC_LOWCODE_METADATA_TABLE_RELATION_REQUIRED";
  /** 页面模型缺失错误码。 */
  private static final String ERROR_PAGE_MODEL_REQUIRED = "ZHYC_LOWCODE_METADATA_PAGE_MODEL_REQUIRED";
  /** 数据源主键缺失错误码。 */
  private static final String ERROR_DATASOURCE_ID_REQUIRED = "ZHYC_LOWCODE_METADATA_DATASOURCE_ID_REQUIRED";
  /** 物理表结构缺失错误码。 */
  private static final String ERROR_PHYSICAL_TABLE_REQUIRED = "ZHYC_LOWCODE_METADATA_PHYSICAL_TABLE_REQUIRED";
  /** 平台内部表不允许导入错误码。 */
  private static final String ERROR_PLATFORM_TABLE_UNSUPPORTED =
      "ZHYC_LOWCODE_METADATA_PLATFORM_TABLE_UNSUPPORTED";
  /** 不允许低代码业务建模导入的平台内部表名前缀。 */
  private static final Set<String> PLATFORM_TABLE_PREFIXES = Set.of(
      "sys_", "auth_", "openapi_", "wf_", "act_", "flw_", "lc_",
      "visual_", "mon_", "job_", "msg_", "file_");

  /** 数据源仓储。 */
  private final LowcodeDataSourceRepository dataSourceRepository;
  /** 表模型仓储。 */
  private final LowcodeTableModelRepository tableModelRepository;
  /** 表关系仓储。 */
  private final LowcodeTableRelationRepository tableRelationRepository;
  /** 页面模型仓储。 */
  private final LowcodePageModelRepository pageModelRepository;
  /** 数据库结构访问网关。 */
  private final LowcodeDatabaseSchemaGateway schemaGateway;
  /** 数据库方言服务。 */
  private final LowcodeDbDialectService dialectService;
  /** 表模型转换器。 */
  private final LowcodeTableModelConverter tableModelConverter;

  /**
   * 创建默认低代码元数据服务。
   *
   * @param dataSourceRepository 数据源仓储
   * @param tableModelRepository 表模型仓储
   * @param tableRelationRepository 表关系仓储
   * @param pageModelRepository 页面模型仓储
   */
  public DefaultLowcodeMetadataService(LowcodeDataSourceRepository dataSourceRepository,
                                       LowcodeTableModelRepository tableModelRepository,
                                       LowcodeTableRelationRepository tableRelationRepository,
                                       LowcodePageModelRepository pageModelRepository) {
    this(dataSourceRepository, tableModelRepository, tableRelationRepository, pageModelRepository,
        new UnsupportedLowcodeDatabaseSchemaGateway(), new UnsupportedLowcodeDbDialectService(),
        new LowcodeTableModelConverter());
  }

  /**
   * 创建默认低代码元数据服务。
   *
   * @param dataSourceRepository 数据源仓储
   * @param tableModelRepository 表模型仓储
   * @param tableRelationRepository 表关系仓储
   * @param pageModelRepository 页面模型仓储
   * @param schemaGateway 数据库结构访问网关
   * @param dialectService 数据库方言服务
   * @param tableModelConverter 表模型转换器
   */
  @Autowired
  public DefaultLowcodeMetadataService(LowcodeDataSourceRepository dataSourceRepository,
                                       LowcodeTableModelRepository tableModelRepository,
                                       LowcodeTableRelationRepository tableRelationRepository,
                                       LowcodePageModelRepository pageModelRepository,
                                       LowcodeDatabaseSchemaGateway schemaGateway,
                                       LowcodeDbDialectService dialectService,
                                       LowcodeTableModelConverter tableModelConverter) {
    this.dataSourceRepository = Objects.requireNonNull(dataSourceRepository, "数据源仓储不能为空");
    this.tableModelRepository = Objects.requireNonNull(tableModelRepository, "表模型仓储不能为空");
    this.tableRelationRepository = Objects.requireNonNull(tableRelationRepository, "表关系仓储不能为空");
    this.pageModelRepository = Objects.requireNonNull(pageModelRepository, "页面模型仓储不能为空");
    this.schemaGateway = Objects.requireNonNull(schemaGateway, "数据库结构访问网关不能为空");
    this.dialectService = Objects.requireNonNull(dialectService, "数据库方言服务不能为空");
    this.tableModelConverter = Objects.requireNonNull(tableModelConverter, "表模型转换器不能为空");
  }

  @Override
  public LowcodeDataSource saveDataSource(LowcodeDataSource dataSource) {
    dataSource = requireObject(dataSource, ERROR_DATASOURCE_REQUIRED, "数据源定义不能为空");
    if (dataSource.isEnabled()) {
      validateDataSourceConnectionConfig(dataSource);
    }
    return dataSourceRepository.save(dataSource);
  }

  @Override
  public LowcodeDataSource getDataSource(String tenantId, String code) {
    String normalizedCode = requireText(code, ERROR_DATASOURCE_CODE_REQUIRED, "数据源编码不能为空");
    return dataSourceRepository.findByTenantIdAndCode(
            requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"), normalizedCode)
        .orElseThrow(() -> new BusinessException(ERROR_DATASOURCE_NOT_FOUND, "低代码数据源不存在: " + normalizedCode));
  }

  @Override
  public List<LowcodeDataSource> listDataSources(String tenantId) {
    return dataSourceRepository.findByTenantId(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"));
  }

  @Override
  public List<LowcodePhysicalTable> listPhysicalTables(String tenantId, Long dataSourceId) {
    String normalizedTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    LowcodeDataSource dataSource = findDataSourceByTenantAndId(normalizedTenantId, dataSourceId);
    return withTenantContext(normalizedTenantId, () -> schemaGateway.listTables(dataSource))
            .stream()
            .filter(table -> isBusinessPhysicalTable(table.getTableName()))
            .toList();
  }

  @Override
  public LowcodeDataSourceConnectionTestResult testDataSourceConnection(String tenantId, String code) {
    String normalizedTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    LowcodeDataSource dataSource = getDataSource(normalizedTenantId, code);
    if (!dataSource.isEnabled()) {
      return new LowcodeDataSourceConnectionTestResult(dataSource.getCode(), false,
          "数据源未启用，不能执行连接测试");
    }
    String configCheckMessage = validateDataSourceConnectionConfig(dataSource);
    try {
      withTenantContext(normalizedTenantId, () -> schemaGateway.listTables(dataSource));
    } catch (BusinessException exception) {
      return new LowcodeDataSourceConnectionTestResult(dataSource.getCode(), false,
          "数据源连接测试失败：" + exception.getMessage());
    }
    return new LowcodeDataSourceConnectionTestResult(dataSource.getCode(), true,
        configCheckMessage + "，数据源连接测试通过");
  }

  @Override
  public LowcodeTableModel saveTableModel(LowcodeTableModel tableModel) {
    tableModel = requireObject(tableModel, ERROR_TABLE_MODEL_REQUIRED, "表模型不能为空");
    validateTableModel(tableModel);
    validateTableModelDataSource(tableModel);
    return tableModelRepository.save(tableModel);
  }

  @Override
  public LowcodeTableModel getTableModel(String tenantId, String code) {
    String normalizedCode = requireText(code, ERROR_TABLE_MODEL_CODE_REQUIRED, "模型编码不能为空");
    return tableModelRepository.findByTenantIdAndCode(
            requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"), normalizedCode)
        .orElseThrow(() -> new BusinessException(ERROR_TABLE_MODEL_NOT_FOUND, "低代码表模型不存在: " + normalizedCode));
  }

  @Override
  public LowcodeTableModel importTableModel(String tenantId, Long dataSourceId, String tableName,
                                            String modelCode, String modelName) {
    String normalizedTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    LowcodeDataSource dataSource = findDataSourceByTenantAndId(normalizedTenantId, dataSourceId);
    LowcodePhysicalTable physicalTable = requirePhysicalTable(
        withTenantContext(normalizedTenantId, () -> schemaGateway.readTable(dataSource, tableName)));
    ensureBusinessPhysicalTable(physicalTable.getTableName());
    LowcodeTableModel tableModel = new LowcodeTableModel(
        null,
        normalizedTenantId,
        dataSource.getId(),
        requireText(modelCode, ERROR_TABLE_MODEL_CODE_REQUIRED, "模型编码不能为空"),
        trimToFallback(modelName, trimToFallback(physicalTable.getComment(), physicalTable.getTableName())),
        physicalTable.getTableName(),
        null,
        physicalTable.getColumns().stream()
            .map(DefaultLowcodeMetadataService::toColumnModel)
            .toList());
    return saveTableModel(tableModel);
  }

  @Override
  public List<LowcodeTableModel> listTableModels(String tenantId) {
    return tableModelRepository.findByTenantId(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"));
  }

  @Override
  public LowcodeTableModel publishTableModel(String tenantId, String code) {
    String normalizedCode = requireText(code, ERROR_TABLE_MODEL_CODE_REQUIRED, "模型编码不能为空");
    LowcodeTableModel tableModel = getTableModel(tenantId, normalizedCode);
    tableModel.publish();
    executeCreateTableDdlIfBoundToDataSource(tableModel);
    return tableModelRepository.save(tableModel);
  }

  @Override
  public LowcodeTableRelation saveTableRelation(LowcodeTableRelation relation) {
    relation = requireObject(relation, ERROR_TABLE_RELATION_REQUIRED, "表关系不能为空");
    validateTableRelation(relation);
    return tableRelationRepository.save(relation);
  }

  @Override
  public List<LowcodeTableRelation> listTableRelations(String tenantId) {
    return tableRelationRepository.findByTenantId(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"));
  }

  @Override
  public LowcodePageModel savePageModel(LowcodePageModel pageModel) {
    pageModel = requireObject(pageModel, ERROR_PAGE_MODEL_REQUIRED, "页面模型不能为空");
    validatePageModel(pageModel);
    return pageModelRepository.save(pageModel);
  }

  @Override
  public List<LowcodePageModel> listPageModels(String tenantId) {
    return pageModelRepository.findByTenantId(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"));
  }

  private static String requireText(String value, String errorCode, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new BusinessException(errorCode, message);
    }
    return value.trim();
  }

  /**
   * 校验服务入口对象不能为空。
   *
   * @param value 服务入口对象
   * @param errorCode 业务错误码
   * @param message 用户可读错误消息
   * @param <T> 服务入口对象类型
   * @return 非空服务入口对象
   */
  private static <T> T requireObject(T value, String errorCode, String message) {
    if (value == null) {
      throw new BusinessException(errorCode, message);
    }
    return value;
  }

  /**
   * 执行表模型领域校验并转换为稳定业务异常。
   *
   * @param tableModel 表模型
   */
  private static void validateTableModel(LowcodeTableModel tableModel) {
    try {
      tableModel.validate();
    } catch (IllegalArgumentException exception) {
      throw new BusinessException(ERROR_TABLE_MODEL_INVALID, exception.getMessage());
    }
  }

  /**
   * 校验表模型绑定的数据源属于当前租户。
   *
   * <p>数据源主键来自后台建模请求，必须在保存表模型前确认租户归属，避免跨租户数据源参与 DDL 和代码生成。</p>
   *
   * @param tableModel 表模型
   */
  private void validateTableModelDataSource(LowcodeTableModel tableModel) {
    Long dataSourceId = tableModel.getDataSourceId();
    if (dataSourceId == null) {
      return;
    }
    dataSourceRepository.findByTenantIdAndId(tableModel.getTenantId(), dataSourceId)
        .orElseThrow(() -> new BusinessException(ERROR_TABLE_DATASOURCE_NOT_FOUND,
            "表模型绑定的数据源不属于当前租户: " + dataSourceId));
  }

  /**
   * 按租户和数据源主键查找数据源。
   *
   * @param tenantId 租户业务编码
   * @param dataSourceId 数据源主键
   * @return 当前租户下的数据源
   */
  private LowcodeDataSource findDataSourceByTenantAndId(String tenantId, Long dataSourceId) {
    if (dataSourceId == null) {
      throw new BusinessException(ERROR_DATASOURCE_ID_REQUIRED, "数据源主键不能为空");
    }
    return dataSourceRepository.findByTenantIdAndId(tenantId, dataSourceId)
        .orElseThrow(() -> new BusinessException(ERROR_TABLE_DATASOURCE_NOT_FOUND,
            "表模型绑定的数据源不属于当前租户: " + dataSourceId));
  }

  /**
   * 模型绑定数据源时生成并执行建表 DDL。
   *
   * @param tableModel 已通过发布校验的表模型
   */
  private void executeCreateTableDdlIfBoundToDataSource(LowcodeTableModel tableModel) {
    Long dataSourceId = tableModel.getDataSourceId();
    if (dataSourceId == null) {
      return;
    }
    LowcodeDataSource dataSource = findDataSourceByTenantAndId(tableModel.getTenantId(), dataSourceId);
    String ddl = dialectService.generateCreateTable(dataSource.getDialect().getCode(),
        tableModelConverter.toDdlTable(tableModel));
    withTenantContext(tableModel.getTenantId(), () -> schemaGateway.executeDdl(dataSource, ddl));
  }

  /**
   * 在当前线程临时绑定租户上下文执行数据库结构操作。
   *
   * <p>低代码结构网关会通过通用密钥解析器读取数据库口令，解析器只能从线程上下文确认租户边界，
   * 因此这里在调用 JDBC 网关前绑定当前请求租户，并在结束后恢复原上下文。</p>
   *
   * @param tenantId 租户业务编码
   * @param action 数据库结构操作
   */
  private static void withTenantContext(String tenantId, Runnable action) {
    withTenantContext(tenantId, () -> {
      action.run();
      return null;
    });
  }

  /**
   * 在当前线程临时绑定租户上下文执行数据库结构操作并返回结果。
   *
   * @param tenantId 租户业务编码
   * @param supplier 数据库结构查询操作
   * @param <T> 返回值类型
   * @return 操作返回值
   */
  private static <T> T withTenantContext(String tenantId, Supplier<T> supplier) {
    String previousTenantId = TenantContext.getTenantId();
    TenantContext.setTenantId(tenantId);
    try {
      return supplier.get();
    } finally {
      if (previousTenantId == null) {
        TenantContext.clear();
      } else {
        TenantContext.setTenantId(previousTenantId);
      }
    }
  }

  /**
   * 校验数据源连接配置。
   *
   * <p>首期不读取密钥明文，只确认连接测试所需的非敏感配置已经具备。</p>
   *
   * @param dataSource 数据源定义
   * @return 数据源连接配置预检查结果
   */
  private static String validateDataSourceConnectionConfig(LowcodeDataSource dataSource) {
    String jdbcUrl = requireText(dataSource.getJdbcUrl(), ERROR_JDBC_URL_REQUIRED, "JDBC 连接地址不能为空");
    requireText(dataSource.getUsername(), ERROR_DB_USERNAME_REQUIRED, "数据库用户名不能为空");
    requireText(dataSource.getPasswordSecretRef(), ERROR_DB_PASSWORD_SECRET_REQUIRED, "数据库口令密钥引用不能为空");
    if (dataSource.getDialect() == null) {
      throw new BusinessException(ERROR_DB_DIALECT_REQUIRED, "数据库类型不能为空");
    }
    if (!jdbcUrl.startsWith("jdbc:")) {
      throw new BusinessException(ERROR_JDBC_URL_INVALID, "JDBC 连接地址必须以 jdbc: 开头");
    }
    String expectedPrefix = expectedJdbcUrlPrefix(dataSource.getDialect());
    if (!jdbcUrl.toLowerCase(Locale.ROOT).startsWith(expectedPrefix)) {
      throw new BusinessException(ERROR_JDBC_URL_INVALID,
          "JDBC 连接地址必须与数据库类型 " + dataSource.getDialect().getCode() + " 匹配，前缀应为 " + expectedPrefix);
    }
    return "数据源连接配置预检查通过";
  }

  private static String expectedJdbcUrlPrefix(LowcodeDatabaseDialect dialect) {
    return switch (dialect) {
      case MYSQL -> "jdbc:mysql:";
      case POSTGRESQL -> "jdbc:postgresql:";
      case ORACLE -> "jdbc:oracle:";
      case SQLSERVER -> "jdbc:sqlserver:";
      case DM -> "jdbc:dm:";
    };
  }

  private static LowcodePhysicalTable requirePhysicalTable(LowcodePhysicalTable physicalTable) {
    if (physicalTable == null) {
      throw new BusinessException(ERROR_PHYSICAL_TABLE_REQUIRED, "数据源物理表结构不能为空");
    }
    if (physicalTable.getColumns().isEmpty()) {
      throw new BusinessException(ERROR_PHYSICAL_TABLE_REQUIRED, "数据源物理表字段不能为空");
    }
    return physicalTable;
  }

  private static void ensureBusinessPhysicalTable(String tableName) {
    if (!isBusinessPhysicalTable(tableName)) {
      throw new BusinessException(ERROR_PLATFORM_TABLE_UNSUPPORTED,
          "平台内部表不能导入低代码业务模型: " + tableName);
    }
  }

  private static boolean isBusinessPhysicalTable(String tableName) {
    if (tableName == null || tableName.trim().isEmpty()) {
      return false;
    }
    String normalized = tableName.trim().toLowerCase(Locale.ROOT);
    return PLATFORM_TABLE_PREFIXES.stream().noneMatch(normalized::startsWith);
  }

  private static LowcodeColumnModel toColumnModel(LowcodePhysicalColumn physicalColumn) {
    return LowcodeColumnModel.builder(
            physicalColumn.getName(),
            trimToFallback(physicalColumn.getComment(), physicalColumn.getName()),
            physicalColumn.getFieldType())
        .length(physicalColumn.getLength())
        .scale(physicalColumn.getScale())
        .required(!physicalColumn.isNullable())
        .primaryKey(physicalColumn.isPrimaryKey())
        .autoIncrement(physicalColumn.isAutoIncrement())
        .listVisible(!physicalColumn.isPrimaryKey())
        .formVisible(!physicalColumn.isPrimaryKey() && !physicalColumn.isAutoIncrement())
        .queryable(false)
        .comment(physicalColumn.getComment())
        .build();
  }

  private static String trimToFallback(String value, String fallback) {
    if (value == null || value.trim().isEmpty()) {
      return fallback;
    }
    return value.trim();
  }

  /**
   * 校验表关系引用的表模型和字段模型都归属当前租户。
   *
   * @param relation 表关系模型
   */
  private void validateTableRelation(LowcodeTableRelation relation) {
    String tenantId = requireText(relation.getTenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    LowcodeTableModel mainTable = findTableByTenantAndId(tenantId, relation.getMainTableId(),
        ERROR_RELATION_MAIN_TABLE_NOT_FOUND, "主表模型不存在: ");
    LowcodeTableModel subTable = findTableByTenantAndId(tenantId, relation.getSubTableId(),
        ERROR_RELATION_SUB_TABLE_NOT_FOUND, "子表模型不存在: ");
    ensureColumnExists(mainTable, relation.getJoinColumn(),
        ERROR_RELATION_JOIN_COLUMN_NOT_FOUND, "主表关联字段不存在: ");
    ensureColumnExists(subTable, relation.getRefColumn(),
        ERROR_RELATION_REF_COLUMN_NOT_FOUND, "子表引用字段不存在: ");
  }

  /**
   * 校验页面模型引用的表模型归属当前租户。
   *
   * @param pageModel 页面模型
   */
  private void validatePageModel(LowcodePageModel pageModel) {
    String tenantId = requireText(pageModel.getTenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    findTableByTenantAndId(tenantId, pageModel.getTableModelId(),
        ERROR_PAGE_TABLE_NOT_FOUND, "页面表模型不存在: ");
    validatePageTarget(pageModel);
  }

  /**
   * 校验页面类型、组件路径和布局类型是否匹配目标端。
   *
   * @param pageModel 页面模型
   */
  private static void validatePageTarget(LowcodePageModel pageModel) {
    if (!pageModel.getRoutePath().startsWith("/")) {
      throw new BusinessException(ERROR_PAGE_ROUTE_REQUIRED_ABSOLUTE, "页面路由必须以 / 开头");
    }
    if (!pageModel.getComponentPath().endsWith(".vue")) {
      throw new BusinessException(ERROR_PAGE_COMPONENT_MUST_BE_VUE, "页面组件路径必须以 .vue 结尾");
    }
    if (Set.of("MOBILE", "MOBILE_FORM", "MOBILE_DETAIL").contains(pageModel.getPageType())) {
      if (!pageModel.getRoutePath().startsWith("/pages/")) {
        throw new BusinessException(ERROR_MOBILE_ROUTE_INVALID, "移动端页面路由必须以 /pages/ 开头");
      }
      if (!pageModel.getComponentPath().startsWith("zhyc-base-uniapp/src/pages/")) {
        throw new BusinessException(ERROR_MOBILE_COMPONENT_PATH_INVALID,
            "移动端页面组件路径必须位于 zhyc-base-uniapp/src/pages/");
      }
      if (!"UNIAPP_PAGE".equals(pageModel.getLayoutType())) {
        throw new BusinessException(ERROR_MOBILE_LAYOUT_INVALID, "移动端页面布局类型必须为 UNIAPP_PAGE");
      }
      return;
    }
    if (!Set.of("LIST", "FORM", "DETAIL").contains(pageModel.getPageType())) {
      throw new BusinessException(ERROR_PAGE_TYPE_UNSUPPORTED, "页面类型不支持: " + pageModel.getPageType());
    }
    if (pageModel.getRoutePath().startsWith("/pages/")) {
      throw new BusinessException(ERROR_ADMIN_ROUTE_INVALID, "后台页面路由不能以 /pages/ 开头");
    }
    if (!pageModel.getComponentPath().startsWith("zhyc-base-vue/src/views/")) {
      throw new BusinessException(ERROR_ADMIN_COMPONENT_PATH_INVALID, "后台页面组件路径必须位于 zhyc-base-vue/src/views/");
    }
    validateAdminLayout(pageModel);
  }

  /**
   * 校验后台页面类型和布局类型是否匹配。
   *
   * @param pageModel 页面模型
   */
  private static void validateAdminLayout(LowcodePageModel pageModel) {
    if ("LIST".equals(pageModel.getPageType()) && !"TABLE".equals(pageModel.getLayoutType())) {
      throw new BusinessException(ERROR_ADMIN_LAYOUT_INVALID, "列表页布局类型必须为 TABLE");
    }
    if ("FORM".equals(pageModel.getPageType()) && !"FORM".equals(pageModel.getLayoutType())) {
      throw new BusinessException(ERROR_ADMIN_LAYOUT_INVALID, "表单页布局类型必须为 FORM");
    }
    if ("DETAIL".equals(pageModel.getPageType()) && !"DESCRIPTIONS".equals(pageModel.getLayoutType())) {
      throw new BusinessException(ERROR_ADMIN_LAYOUT_INVALID, "详情页布局类型必须为 DESCRIPTIONS");
    }
  }

  /**
   * 按租户和表模型主键查找表模型。
   *
   * @param tenantId 租户业务编码
   * @param tableId 表模型主键
   * @param errorCode 表模型不存在时的业务错误码
   * @param missingMessage 未找到表模型时的错误消息前缀
   * @return 当前租户下的表模型
   */
  private LowcodeTableModel findTableByTenantAndId(String tenantId, Long tableId,
                                                   String errorCode, String missingMessage) {
    return tableModelRepository.findByTenantIdAndId(tenantId, tableId)
        .orElseThrow(() -> new BusinessException(errorCode, missingMessage + tableId));
  }

  /**
   * 校验表模型中存在指定字段编码。
   *
   * @param tableModel 表模型
   * @param columnCode 字段编码
   * @param errorCode 字段不存在时的业务错误码
   * @param missingMessage 未找到字段时的错误消息前缀
   */
  private static void ensureColumnExists(LowcodeTableModel tableModel, String columnCode,
                                         String errorCode, String missingMessage) {
    boolean exists = tableModel.getColumns().stream()
        .anyMatch(column -> column.getCode().equals(columnCode));
    if (!exists) {
      throw new BusinessException(errorCode, missingMessage + columnCode);
    }
  }

  /**
   * 未配置数据库结构网关时的兜底实现。
   */
  private static class UnsupportedLowcodeDatabaseSchemaGateway implements LowcodeDatabaseSchemaGateway {

    /** 数据库结构网关缺失错误码。 */
    private static final String ERROR_SCHEMA_GATEWAY_UNSUPPORTED = "ZHYC_LOWCODE_SCHEMA_GATEWAY_UNSUPPORTED";

    @Override
    public List<LowcodePhysicalTable> listTables(LowcodeDataSource dataSource) {
      throw unsupported();
    }

    @Override
    public LowcodePhysicalTable readTable(LowcodeDataSource dataSource, String tableName) {
      throw unsupported();
    }

    @Override
    public void executeDdl(LowcodeDataSource dataSource, String ddl) {
      throw unsupported();
    }

    private static BusinessException unsupported() {
      return new BusinessException(ERROR_SCHEMA_GATEWAY_UNSUPPORTED, "未配置数据库结构访问网关");
    }
  }

  /**
   * 未配置数据库方言服务时的兜底实现。
   */
  private static class UnsupportedLowcodeDbDialectService implements LowcodeDbDialectService {

    /** 数据库方言服务缺失错误码。 */
    private static final String ERROR_DIALECT_SERVICE_UNSUPPORTED = "ZHYC_LOWCODE_DIALECT_SERVICE_UNSUPPORTED";

    @Override
    public String generateCreateTable(String dialectCode, LowcodeTable table) {
      throw unsupported();
    }

    @Override
    public String mapFieldType(String dialectCode, LowcodeColumn column) {
      throw unsupported();
    }

    @Override
    public String applyPagination(String dialectCode, String sql, long offset, long pageSize) {
      throw unsupported();
    }

    @Override
    public List<String> listDdlDialectCodes() {
      return List.of();
    }

    @Override
    public List<String> listFieldTypeDialectCodes() {
      return List.of();
    }

    @Override
    public List<String> listPaginationDialectCodes() {
      return List.of();
    }

    private static BusinessException unsupported() {
      return new BusinessException(ERROR_DIALECT_SERVICE_UNSUPPORTED, "未配置数据库方言服务");
    }
  }
}
