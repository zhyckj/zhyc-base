/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.lowcode.db.LowcodeDbDialectService;
import com.zhyc.lowcode.metadata.converter.LowcodeTableModelConverter;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;
import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodeModelStatus;
import com.zhyc.lowcode.metadata.domain.LowcodePageModel;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import com.zhyc.lowcode.metadata.service.LowcodeMetadataService;
import com.zhyc.lowcode.generator.repository.LowcodeGenerationFileRepository;
import com.zhyc.lowcode.generator.repository.LowcodeGenerationRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 默认低代码生成应用服务实现。
 */
@Service
public class DefaultLowcodeGeneratorService implements LowcodeGeneratorService {

  /** 数据库标识符命名规则，覆盖表名和字段编码。 */
  private static final Pattern DATABASE_IDENTIFIER_PATTERN = Pattern.compile("[a-z][a-z0-9_]*");
  /** 生成命令安全命名规则，覆盖路径、包名和类型名片段。 */
  private static final Pattern SAFE_GENERATION_NAME_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9]*");
  /** 生成命令小驼峰命名推荐规则，覆盖业务模块名和实体名。 */
  private static final Pattern LOWER_CAMEL_PATTERN = Pattern.compile("[a-z][A-Za-z0-9]*");
  /** 模板查询生成目标缺失错误码。 */
  private static final String ERROR_TEMPLATE_TARGET_REQUIRED = "ZHYC_LOWCODE_GENERATION_TEMPLATE_TARGET_REQUIRED";
  /** 生成预览命令缺失错误码。 */
  private static final String ERROR_PREVIEW_COMMAND_REQUIRED = "ZHYC_LOWCODE_GENERATION_PREVIEW_COMMAND_REQUIRED";
  /** 生成执行命令缺失错误码。 */
  private static final String ERROR_EXECUTE_COMMAND_REQUIRED = "ZHYC_LOWCODE_GENERATION_EXECUTE_COMMAND_REQUIRED";
  /** 表模型未发布错误码。 */
  private static final String ERROR_TABLE_MODEL_NOT_PUBLISHED = "ZHYC_LOWCODE_GENERATION_TABLE_MODEL_NOT_PUBLISHED";
  /** 生成前校验未通过错误码。 */
  private static final String ERROR_VALIDATION_FAILED = "ZHYC_LOWCODE_GENERATION_VALIDATION_FAILED";
  /** 生成执行依赖缺失错误码。 */
  private static final String ERROR_EXECUTION_DEPENDENCY_MISSING =
      "ZHYC_LOWCODE_GENERATION_EXECUTION_DEPENDENCY_MISSING";
  /** 生成记录仓储缺失错误码。 */
  private static final String ERROR_RECORD_REPOSITORY_MISSING = "ZHYC_LOWCODE_GENERATION_RECORD_REPOSITORY_MISSING";
  /** 生成文件明细仓储缺失错误码。 */
  private static final String ERROR_FILE_REPOSITORY_MISSING = "ZHYC_LOWCODE_GENERATION_FILE_REPOSITORY_MISSING";
  /** 生成记录查询租户缺失错误码。 */
  private static final String ERROR_RECORD_TENANT_REQUIRED = "ZHYC_LOWCODE_GENERATION_RECORD_TENANT_REQUIRED";
  /** 生成文件明细查询记录主键缺失错误码。 */
  private static final String ERROR_FILE_RECORD_ID_REQUIRED = "ZHYC_LOWCODE_GENERATION_FILE_RECORD_ID_REQUIRED";

  /** 低代码元数据服务。 */
  private final LowcodeMetadataService metadataService;
  /** 代码模板注册表。 */
  private final CodeTemplateRegistry templateRegistry;
  /** 代码生成器。 */
  private final CodeGenerator codeGenerator;
  /** 低代码生成记录仓储。 */
  private final LowcodeGenerationRecordRepository recordRepository;
  /** 低代码生成文件明细仓储。 */
  private final LowcodeGenerationFileRepository generationFileRepository;
  /** 生成文件写入器。 */
  private final GeneratedFileWriter fileWriter;
  /** 低代码数据库方言服务。 */
  private final LowcodeDbDialectService dialectService;
  /** 表模型到 DDL 表模型转换器。 */
  private final LowcodeTableModelConverter tableModelConverter;

  /**
   * 创建默认低代码生成服务。
   *
   * @param metadataService 低代码元数据服务
   * @param templateRegistry 代码模板注册表
   * @param codeGenerator 代码生成器
   */
  public DefaultLowcodeGeneratorService(LowcodeMetadataService metadataService,
                                        CodeTemplateRegistry templateRegistry,
                                        CodeGenerator codeGenerator) {
    this(metadataService, templateRegistry, codeGenerator, null, null, null, null);
  }

  /**
   * 创建默认低代码生成服务。
   *
   * @param metadataService 低代码元数据服务
   * @param templateRegistry 代码模板注册表
   * @param codeGenerator 代码生成器
   * @param recordRepository 低代码生成记录仓储
   * @param fileWriter 生成文件写入器
   */
  public DefaultLowcodeGeneratorService(LowcodeMetadataService metadataService,
                                        CodeTemplateRegistry templateRegistry,
                                        CodeGenerator codeGenerator,
                                        LowcodeGenerationRecordRepository recordRepository,
                                        GeneratedFileWriter fileWriter) {
    this(metadataService, templateRegistry, codeGenerator, recordRepository, null, fileWriter, null);
  }

  /**
   * 创建默认低代码生成服务。
   *
   * @param metadataService 低代码元数据服务
   * @param templateRegistry 代码模板注册表
   * @param codeGenerator 代码生成器
   * @param recordRepository 低代码生成记录仓储
   * @param generationFileRepository 低代码生成文件明细仓储
   * @param fileWriter 生成文件写入器
   */
  public DefaultLowcodeGeneratorService(LowcodeMetadataService metadataService,
                                        CodeTemplateRegistry templateRegistry,
                                        CodeGenerator codeGenerator,
                                        LowcodeGenerationRecordRepository recordRepository,
                                        LowcodeGenerationFileRepository generationFileRepository,
                                        GeneratedFileWriter fileWriter) {
    this(metadataService, templateRegistry, codeGenerator, recordRepository, generationFileRepository, fileWriter,
        null);
  }

  /**
   * 创建默认低代码生成服务。
   *
   * @param metadataService 低代码元数据服务
   * @param templateRegistry 代码模板注册表
   * @param codeGenerator 代码生成器
   * @param recordRepository 低代码生成记录仓储
   * @param generationFileRepository 低代码生成文件明细仓储
   * @param fileWriter 生成文件写入器
   * @param dialectService 低代码数据库方言服务
   */
  @Autowired
  public DefaultLowcodeGeneratorService(LowcodeMetadataService metadataService,
                                        CodeTemplateRegistry templateRegistry,
                                        CodeGenerator codeGenerator,
                                        LowcodeGenerationRecordRepository recordRepository,
                                        LowcodeGenerationFileRepository generationFileRepository,
                                        GeneratedFileWriter fileWriter,
                                        LowcodeDbDialectService dialectService) {
    this.metadataService = Objects.requireNonNull(metadataService, "低代码元数据服务不能为空");
    this.templateRegistry = Objects.requireNonNull(templateRegistry, "代码模板注册表不能为空");
    this.codeGenerator = Objects.requireNonNull(codeGenerator, "代码生成器不能为空");
    this.recordRepository = recordRepository;
    this.generationFileRepository = generationFileRepository;
    this.fileWriter = fileWriter;
    this.dialectService = dialectService;
    this.tableModelConverter = new LowcodeTableModelConverter();
  }

  /**
   * 查询指定目标端可用的代码模板。
   *
   * @param target 生成目标端
   * @return 目标端模板描述列表
   */
  @Override
  public List<CodeTemplateDescriptor> listTemplates(GenerationTarget target) {
    return templateRegistry.findByTarget(requireTemplateTarget(target));
  }

  /**
   * 校验生成预览命令。
   *
   * <p>校验表模型发布状态、字段合法性、租户隔离字段、覆盖策略和命名规范，返回错误与警告清单。</p>
   *
   * @param command 生成预览命令
   * @return 生成校验结果
   */
  @Override
  public LowcodeGenerationValidationResult validate(LowcodeGenerationPreviewCommand command) {
    command = requirePreviewCommand(command);
    LowcodeTableModel tableModel = metadataService.getTableModel(command.getTenantId(), command.getTableModelCode());
    List<LowcodeGenerationValidationItem> errors = new ArrayList<>();
    List<LowcodeGenerationValidationItem> warnings = new ArrayList<>();
    validateTableModelForGeneration(tableModel, errors);
    validateGenerationCommandSafety(command, errors);
    validateGenerationCommandNaming(command, warnings);
    return new LowcodeGenerationValidationResult(errors, warnings);
  }

  /**
   * 生成文件预览。
   *
   * <p>按表模型和页面模型渲染文件内容，只返回预览结果，不写入磁盘。</p>
   *
   * @param command 生成预览命令
   * @return 生成文件预览列表
   */
  @Override
  public List<GeneratedFile> preview(LowcodeGenerationPreviewCommand command) {
    command = requirePreviewCommand(command);
    LowcodeTableModel tableModel = metadataService.getTableModel(command.getTenantId(), command.getTableModelCode());
    if (tableModel.getStatus() != LowcodeModelStatus.PUBLISHED) {
      throw new BusinessException(ERROR_TABLE_MODEL_NOT_PUBLISHED, "表模型未发布，不能生成代码: " + tableModel.getCode());
    }
    assertValidationPassed(command);
    String ddl = generateCreateTable(command, tableModel);
    List<GeneratedFile> files = codeGenerator.generate(new CodeGenerationRequest(
        command.getTarget(), command.getModuleName(), command.getEntityName(), tableModel, ddl));
    return filterByPageModels(command, tableModel, files);
  }

  /**
   * 校验模板查询生成目标不能为空。
   *
   * @param target 生成目标端
   * @return 非空生成目标端
   */
  private static GenerationTarget requireTemplateTarget(GenerationTarget target) {
    if (target == null) {
      throw new BusinessException(ERROR_TEMPLATE_TARGET_REQUIRED, "生成目标不能为空");
    }
    return target;
  }

  /**
   * 校验生成预览命令不能为空。
   *
   * @param command 生成预览命令
   * @return 非空生成预览命令
   */
  private static LowcodeGenerationPreviewCommand requirePreviewCommand(LowcodeGenerationPreviewCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_PREVIEW_COMMAND_REQUIRED, "生成预览命令不能为空");
    }
    return command;
  }

  /**
   * 校验生成命令并在存在阻断错误时抛出异常。
   *
   * @param command 生成预览命令
   */
  private void assertValidationPassed(LowcodeGenerationPreviewCommand command) {
    LowcodeGenerationValidationResult validationResult = validate(command);
    if (!validationResult.isPassed()) {
      String message = validationResult.getErrors().stream()
          .map(LowcodeGenerationValidationItem::getMessage)
          .findFirst()
          .orElse("生成前校验未通过");
      throw new BusinessException(ERROR_VALIDATION_FAILED, "生成前校验未通过: " + message);
    }
  }

  /**
   * 校验表模型是否满足生成硬性要求。
   *
   * @param tableModel 表模型
   * @param errors 阻断错误列表
   */
  private void validateTableModelForGeneration(LowcodeTableModel tableModel,
                                               List<LowcodeGenerationValidationItem> errors) {
    if (tableModel.getStatus() != LowcodeModelStatus.PUBLISHED) {
      errors.add(new LowcodeGenerationValidationItem("TABLE_MODEL_NOT_PUBLISHED",
          "表模型未发布，不能生成代码: " + tableModel.getCode()));
    }
    if (!DATABASE_IDENTIFIER_PATTERN.matcher(tableModel.getTableName()).matches()) {
      errors.add(new LowcodeGenerationValidationItem("TABLE_NAME_ILLEGAL",
          "物理表名只能使用小写字母、数字和下划线，且必须以小写字母开头: " + tableModel.getTableName()));
    }
    if (!tableModel.hasPrimaryKey()) {
      errors.add(new LowcodeGenerationValidationItem("PRIMARY_KEY_REQUIRED",
          "发布表模型必须配置主键字段，才能生成查询、更新和删除代码"));
    }
    if (!hasColumn(tableModel, "tenant_id")) {
      errors.add(new LowcodeGenerationValidationItem("TENANT_COLUMN_REQUIRED",
          "发布表模型必须配置 tenant_id 字段，才能满足首期 SaaS 共享表数据隔离要求"));
    }
    if (!hasColumn(tableModel, "deleted")) {
      errors.add(new LowcodeGenerationValidationItem("DELETED_COLUMN_REQUIRED",
          "发布表模型必须配置 deleted 字段，才能生成逻辑删除和租户内查询代码"));
    }
    Set<String> columnCodes = new HashSet<>();
    for (var column : tableModel.getColumns()) {
      if (!DATABASE_IDENTIFIER_PATTERN.matcher(column.getCode()).matches()) {
        errors.add(new LowcodeGenerationValidationItem("COLUMN_CODE_ILLEGAL",
            "字段编码只能使用小写字母、数字和下划线，且必须以小写字母开头: " + column.getCode()));
      }
      if (!columnCodes.add(column.getCode())) {
        errors.add(new LowcodeGenerationValidationItem("COLUMN_CODE_DUPLICATED",
            "字段编码不能重复: " + column.getCode()));
      }
    }
  }

  /**
   * 校验生成命令命名建议。
   *
   * @param command 生成预览命令
   * @param warnings 非阻断警告列表
   */
  private void validateGenerationCommandNaming(LowcodeGenerationPreviewCommand command,
                                               List<LowcodeGenerationValidationItem> warnings) {
    if (!LOWER_CAMEL_PATTERN.matcher(command.getModuleName()).matches()) {
      warnings.add(new LowcodeGenerationValidationItem("MODULE_NAME_RECOMMENDED_LOWER_CAMEL",
          "业务模块名称建议使用小驼峰英文编码，避免生成包名、路由和 API 文件名不一致: " + command.getModuleName()));
    }
    if (!LOWER_CAMEL_PATTERN.matcher(command.getEntityName()).matches()) {
      warnings.add(new LowcodeGenerationValidationItem("ENTITY_NAME_RECOMMENDED_LOWER_CAMEL",
          "业务实体名称建议使用小驼峰英文编码，避免生成类名和前端类型名不一致: " + command.getEntityName()));
    }
  }

  /**
   * 判断表模型是否包含指定字段编码。
   *
   * @param tableModel 表模型
   * @param columnCode 字段编码
   * @return 包含指定字段返回 {@code true}
   */
  private boolean hasColumn(LowcodeTableModel tableModel, String columnCode) {
    return tableModel.getColumns().stream()
        .anyMatch(column -> columnCode.equals(column.getCode()));
  }

  /**
   * 校验生成命令中的模块名和实体名是否满足安全路径片段要求。
   *
   * @param command 生成预览命令
   * @param errors 阻断错误列表
   */
  private void validateGenerationCommandSafety(LowcodeGenerationPreviewCommand command,
                                               List<LowcodeGenerationValidationItem> errors) {
    if (!SAFE_GENERATION_NAME_PATTERN.matcher(command.getModuleName()).matches()) {
      errors.add(new LowcodeGenerationValidationItem("MODULE_NAME_ILLEGAL",
          "业务模块名称只能使用英文字母和数字，且必须以字母开头: " + command.getModuleName()));
    }
    if (!SAFE_GENERATION_NAME_PATTERN.matcher(command.getEntityName()).matches()) {
      errors.add(new LowcodeGenerationValidationItem("ENTITY_NAME_ILLEGAL",
          "业务实体名称只能使用英文字母和数字，且必须以字母开头: " + command.getEntityName()));
    }
  }

  /**
   * 按生成目标和数据源方言生成建表 DDL。
   *
   * @param command 生成预览命令
   * @param tableModel 表模型
   * @return 建表 DDL；非后台后端目标或未配置方言服务时返回 {@code null}
   */
  private String generateCreateTable(LowcodeGenerationPreviewCommand command, LowcodeTableModel tableModel) {
    if (command.getTarget() != GenerationTarget.ADMIN_BACKEND || dialectService == null) {
      return null;
    }
    String dialectCode = resolveDatabaseDialectCode(command.getTenantId(), tableModel);
    return dialectService.generateCreateTable(dialectCode, tableModelConverter.toDdlTable(tableModel));
  }

  /**
   * 解析表模型绑定数据源的数据库方言编码。
   *
   * <p>历史表模型未绑定数据源时使用首期默认 MySQL，保持生成预览兼容。</p>
   *
   * @param tenantId 租户业务编码
   * @param tableModel 表模型
   * @return 数据库方言编码
   */
  private String resolveDatabaseDialectCode(String tenantId, LowcodeTableModel tableModel) {
    if (tableModel.getDataSourceId() == null) {
      return LowcodeDatabaseDialect.MYSQL.getCode();
    }
    return metadataService.listDataSources(tenantId).stream()
        .filter(dataSource -> tableModel.getDataSourceId().equals(dataSource.getId()))
        .findFirst()
        .map(LowcodeDataSource::getDialect)
        .map(LowcodeDatabaseDialect::getCode)
        .orElse(LowcodeDatabaseDialect.MYSQL.getCode());
  }

  /**
   * 执行代码生成并写入受控输出目录。
   *
   * <p>执行前复用预览校验，写入后记录生成记录和文件哈希，失败时写入失败记录并继续抛出异常。</p>
   *
   * @param command 生成执行命令，包含预览命令和覆盖策略
   * @return 已保存的生成记录
   */
  @Override
  public LowcodeGenerationRecord execute(LowcodeGenerationExecuteCommand command) {
    command = requireExecuteCommand(command);
    if (recordRepository == null || fileWriter == null) {
      throw new BusinessException(ERROR_EXECUTION_DEPENDENCY_MISSING, "生成执行依赖未配置");
    }
    List<GeneratedFile> files = preview(command.getPreviewCommand());
    LowcodeGenerationPreviewCommand previewCommand = command.getPreviewCommand();
    String fileManifestJson = buildFileManifestJson(files, command.getOverwriteStrategy());
    try {
      fileWriter.write(files, command.getOverwriteStrategy());
      LowcodeGenerationRecord savedRecord = recordRepository.save(LowcodeGenerationRecord.success(
          previewCommand.getTenantId(),
          previewCommand.getTableModelCode(),
          previewCommand.getTarget(),
          previewCommand.getModuleName(),
          previewCommand.getEntityName(),
          command.getOverwriteStrategy(),
          files.size(),
          fileManifestJson));
      saveGenerationFileDetails(previewCommand.getTenantId(), savedRecord.getId(), files,
          command.getOverwriteStrategy());
      return savedRecord;
    } catch (RuntimeException ex) {
      recordRepository.save(LowcodeGenerationRecord.failed(
          previewCommand.getTenantId(),
          previewCommand.getTableModelCode(),
          previewCommand.getTarget(),
          previewCommand.getModuleName(),
          previewCommand.getEntityName(),
          command.getOverwriteStrategy(),
          files.size(),
          fileManifestJson,
          ex.getMessage()));
      throw ex;
    }
  }

  /**
   * 校验生成执行命令不能为空。
   *
   * @param command 生成执行命令
   * @return 非空生成执行命令
   */
  private static LowcodeGenerationExecuteCommand requireExecuteCommand(LowcodeGenerationExecuteCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_EXECUTE_COMMAND_REQUIRED, "生成执行命令不能为空");
    }
    return command;
  }

  /**
   * 查询租户下的代码生成记录。
   *
   * @param tenantId 租户业务编码
   * @return 租户生成记录列表
   */
  @Override
  public List<LowcodeGenerationRecord> listRecords(String tenantId) {
    if (recordRepository == null) {
      throw new BusinessException(ERROR_RECORD_REPOSITORY_MISSING, "生成记录仓储未配置");
    }
    if (tenantId == null || tenantId.isBlank()) {
      throw new BusinessException(ERROR_RECORD_TENANT_REQUIRED, "租户业务编码不能为空");
    }
    return recordRepository.findByTenantId(tenantId);
  }

  /**
   * 查询指定生成记录的文件明细。
   *
   * @param tenantId 租户业务编码
   * @param recordId 生成记录主键
   * @return 生成文件明细列表
   */
  @Override
  public List<LowcodeGenerationFile> listGenerationFiles(String tenantId, Long recordId) {
    if (generationFileRepository == null) {
      throw new BusinessException(ERROR_FILE_REPOSITORY_MISSING, "生成文件明细仓储未配置");
    }
    if (tenantId == null || tenantId.isBlank()) {
      throw new BusinessException(ERROR_RECORD_TENANT_REQUIRED, "租户业务编码不能为空");
    }
    if (recordId == null) {
      throw new BusinessException(ERROR_FILE_RECORD_ID_REQUIRED, "生成记录主键不能为空");
    }
    return generationFileRepository.findByTenantIdAndRecordId(tenantId, recordId);
  }

  private List<GeneratedFile> filterByPageModels(
      LowcodeGenerationPreviewCommand command,
      LowcodeTableModel tableModel,
      List<GeneratedFile> files) {
    List<LowcodePageModel> pageModels = metadataService.listPageModels(command.getTenantId()).stream()
        .filter(pageModel -> pageModel.getTableModelId().equals(tableModel.getId()))
        .filter(pageModel -> isPageModelForTarget(pageModel, command.getTarget()))
        .toList();
    if (pageModels.isEmpty()) {
      return files;
    }
    return files.stream()
        .map(file -> rewritePageFilePath(file, pageModels))
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * 按页面模型重写页面模板输出路径。
   *
   * @param file 原始生成文件
   * @param pageModels 页面模型列表
   * @return 重写后的生成文件；页面模板未匹配页面模型时返回 {@code null}
   */
  private GeneratedFile rewritePageFilePath(GeneratedFile file, List<LowcodePageModel> pageModels) {
    if ("admin-frontend-route".equals(file.getTemplateCode())) {
      return rewriteAdminRouteFile(file, pageModels);
    }
    if ("uniapp-pages-json".equals(file.getTemplateCode())) {
      return rewriteUniappPagesJsonFile(file, pageModels);
    }
    String pageType = pageTypeForTemplate(file.getTemplateCode());
    if (pageType == null) {
      return file;
    }
    return pageModels.stream()
        .filter(pageModel -> pageModel.getPageType().equals(pageType))
        .findFirst()
        .map(pageModel -> new GeneratedFile(file.getTarget(), file.getTemplateCode(),
            pageModel.getComponentPath(), file.getContent()))
        .orElse(null);
  }

  /**
   * 按后台列表页面模型重写路由片段内容。
   *
   * @param file 原始路由文件
   * @param pageModels 页面模型列表
   * @return 重写后的路由文件
   */
  private GeneratedFile rewriteAdminRouteFile(GeneratedFile file, List<LowcodePageModel> pageModels) {
    List<LowcodePageModel> adminPageModels = pageModels.stream()
        .filter(pageModel -> Set.of("LIST", "FORM", "DETAIL").contains(pageModel.getPageType()))
        .toList();
    if (adminPageModels.isEmpty()) {
      return file;
    }
    LowcodePageModel listPageModel = findPageModel(adminPageModels, "LIST");
    return new GeneratedFile(file.getTarget(), file.getTemplateCode(), file.getPath(),
        rewriteAdminRouteContent(file.getContent(), adminPageModels, listPageModel));
  }

  /**
   * 按移动端页面模型重写 uni-app 页面注册片段。
   *
   * @param file 原始页面注册片段
   * @param pageModels 页面模型列表
   * @return 重写后的页面注册片段
   */
  private GeneratedFile rewriteUniappPagesJsonFile(GeneratedFile file, List<LowcodePageModel> pageModels) {
    List<LowcodePageModel> mobilePageModels = pageModels.stream()
        .filter(pageModel -> Set.of("MOBILE", "MOBILE_FORM", "MOBILE_DETAIL").contains(pageModel.getPageType()))
        .sorted((left, right) -> Integer.compare(
            uniappPageTypeOrder(left.getPageType()), uniappPageTypeOrder(right.getPageType())))
        .toList();
    if (mobilePageModels.isEmpty()) {
      return file;
    }
    return new GeneratedFile(file.getTarget(), file.getTemplateCode(), file.getPath(),
        rewriteUniappPagesJsonContent(file.getContent(), mobilePageModels));
  }

  /**
   * 重写后台路由内容中的路由地址和组件引用。
   *
   * @param content 原始路由内容
   * @param pageModels 后台页面模型列表
   * @param listPageModel 后台列表页面模型；为空时移除列表路由
   * @return 重写后的路由内容
   */
  private String rewriteAdminRouteContent(
      String content,
      List<LowcodePageModel> pageModels,
      LowcodePageModel listPageModel) {
    String rewritten = content;
    if (listPageModel == null) {
      rewritten = removeAdminRouteBlock(rewritten, "(?<!Create)(?<!Edit)(?<!Detail)Route");
    } else {
      rewritten = rewritten
          .replaceFirst("path: '[^']+'", "path: '" + listPageModel.getRoutePath() + "'")
          .replaceFirst("component: \\(\\) => import\\('[^']+'\\)",
              "component: () => import('" + toAdminImportPath(listPageModel.getComponentPath()) + "')");
    }
    LowcodePageModel formPageModel = findPageModel(pageModels, "FORM");
    if (formPageModel == null) {
      rewritten = removeAdminRouteBlock(rewritten, "CreateRoute");
      rewritten = removeAdminRouteBlock(rewritten, "EditRoute");
    } else {
      rewritten = rewritten
          .replaceFirst("path: '[^']+/create'", "path: '" + formPageModel.getRoutePath() + "'")
          .replaceFirst("path: '[^']+/:id/edit'", "path: '" + formPageModel.getRoutePath() + "/:id/edit'")
          .replaceAll("component: \\(\\) => import\\('[^']*/form\\.vue'\\)",
              "component: () => import('" + toAdminImportPath(formPageModel.getComponentPath()) + "')");
    }
    LowcodePageModel detailPageModel = findPageModel(pageModels, "DETAIL");
    if (detailPageModel == null) {
      rewritten = removeAdminRouteBlock(rewritten, "DetailRoute");
    } else {
      rewritten = rewritten
          .replaceFirst("path: '[^']+/:id'", "path: '" + detailPageModel.getRoutePath() + "'")
          .replaceAll("component: \\(\\) => import\\('[^']*/detail\\.vue'\\)",
              "component: () => import('" + toAdminImportPath(detailPageModel.getComponentPath()) + "')");
    }
    return rewritten;
  }

  /**
   * 查询指定类型后台页面模型。
   *
   * @param pageModels 后台页面模型列表
   * @param pageType 页面类型
   * @return 匹配页面模型；不存在时返回 {@code null}
   */
  private LowcodePageModel findPageModel(List<LowcodePageModel> pageModels, String pageType) {
    return pageModels.stream()
        .filter(pageModel -> pageType.equals(pageModel.getPageType()))
        .findFirst()
        .orElse(null);
  }

  /**
   * 移除指定后缀的后台路由导出块。
   *
   * @param content 路由文件内容
   * @param routeSuffix 路由常量后缀
   * @return 移除后的路由文件内容
   */
  private String removeAdminRouteBlock(String content, String routeSuffix) {
    String exportPrefix = "export const ";
    int searchFrom = 0;
    while (searchFrom < content.length()) {
      int exportIndex = content.indexOf(exportPrefix, searchFrom);
      if (exportIndex < 0) {
        return content;
      }
      int nameStart = exportIndex + exportPrefix.length();
      int nameEnd = content.indexOf(" = ", nameStart);
      if (nameEnd < 0) {
        return content;
      }
      String routeName = content.substring(nameStart, nameEnd);
      if (!matchesAdminRouteSuffix(routeName, routeSuffix)) {
        searchFrom = nameEnd;
        continue;
      }
      int blockStart = content.lastIndexOf("/**", exportIndex);
      if (blockStart < 0) {
        blockStart = exportIndex;
      }
      int blockEnd = content.indexOf("};", nameEnd);
      if (blockEnd < 0) {
        return content;
      }
      blockEnd += 2;
      while (blockEnd < content.length()
          && (content.charAt(blockEnd) == '\r' || content.charAt(blockEnd) == '\n')) {
        blockEnd++;
      }
      return content.substring(0, blockStart) + content.substring(blockEnd);
    }
    return content;
  }

  /**
   * 判断后台路由常量名是否匹配要移除的路由类型。
   *
   * @param routeName 路由常量名
   * @param routeSuffix 路由后缀；列表路由使用特殊后缀表达式
   * @return 是否匹配目标路由类型
   */
  private boolean matchesAdminRouteSuffix(String routeName, String routeSuffix) {
    if ("(?<!Create)(?<!Edit)(?<!Detail)Route".equals(routeSuffix)) {
      return routeName.endsWith("Route")
          && !routeName.endsWith("CreateRoute")
          && !routeName.endsWith("EditRoute")
          && !routeName.endsWith("DetailRoute");
    }
    return routeName.endsWith(routeSuffix);
  }

  /**
   * 重写 uni-app 页面注册片段中的页面路径。
   *
   * @param content 原始页面注册片段
   * @param pageModels 移动端页面模型列表
   * @return 重写后的页面注册片段
   */
  private String rewriteUniappPagesJsonContent(String content, List<LowcodePageModel> pageModels) {
    String navigationTitle = extractUniappNavigationTitle(content);
    String pages = pageModels.stream()
        .map(pageModel -> """
                {
                  "path": "%s",
                  "style": {
                    "navigationBarTitleText": "%s",
                    "navigationStyle": "custom"
                  }
                }""".formatted(toUniappPagePath(pageModel.getRoutePath()), navigationTitle))
        .reduce((left, right) -> left + ",\n" + right)
        .orElse("");
    return """
        {
          "pages": [
        %s
          ]
        }
        """.formatted(pages);
  }

  /**
   * 提取 uni-app 页面注册模板中的导航标题。
   *
   * @param content 原始页面注册片段
   * @return 导航标题；未匹配时返回空字符串
   */
  private String extractUniappNavigationTitle(String content) {
    java.util.regex.Matcher matcher = java.util.regex.Pattern
        .compile("\"navigationBarTitleText\"\\s*:\\s*\"([^\"]*)\"")
        .matcher(content);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "";
  }

  /**
   * 返回 uni-app 页面类型排序值。
   *
   * @param pageType 页面类型
   * @return 排序值
   */
  private int uniappPageTypeOrder(String pageType) {
    return Map.of(
        "MOBILE", 1,
        "MOBILE_FORM", 2,
        "MOBILE_DETAIL", 3).getOrDefault(pageType, 99);
  }

  /**
   * 将后台组件文件路径转换为 Vue 路由 import 路径。
   *
   * @param componentPath 页面模型组件路径
   * @return Vue import 路径
   */
  private String toAdminImportPath(String componentPath) {
    return componentPath.replaceFirst("^zhyc-base-vue/src/views/", "@/views/");
  }

  /**
   * 将页面模型路由转换为 uni-app pages.json 所需的相对页面路径。
   *
   * @param routePath 页面模型路由
   * @return uni-app 页面路径
   */
  private String toUniappPagePath(String routePath) {
    return routePath.replaceFirst("^/", "");
  }

  /**
   * 根据模板编码解析页面类型。
   *
   * @param templateCode 模板编码
   * @return 页面类型；非页面模板返回 {@code null}
   */
  private String pageTypeForTemplate(String templateCode) {
    return Map.of(
        "admin-frontend-list", "LIST",
        "admin-frontend-form", "FORM",
        "admin-frontend-detail", "DETAIL",
        "uniapp-list", "MOBILE",
        "uniapp-form", "MOBILE_FORM",
        "uniapp-detail", "MOBILE_DETAIL").get(templateCode);
  }

  private boolean isPageModelForTarget(LowcodePageModel pageModel, GenerationTarget target) {
    if (target == GenerationTarget.ADMIN_FRONTEND) {
      return Set.of("LIST", "FORM", "DETAIL").contains(pageModel.getPageType());
    }
    if (target == GenerationTarget.UNIAPP) {
      return Set.of("MOBILE", "MOBILE_FORM", "MOBILE_DETAIL").contains(pageModel.getPageType());
    }
    return false;
  }

  /**
   * 保存生成文件明细。
   *
   * @param tenantId 租户业务编码
   * @param recordId 生成记录主键
   * @param files 生成文件列表
   * @param overwriteStrategy 覆盖策略
   */
  private void saveGenerationFileDetails(String tenantId, Long recordId, List<GeneratedFile> files,
                                         GeneratedFileOverwriteStrategy overwriteStrategy) {
    if (generationFileRepository == null || recordId == null) {
      return;
    }
    generationFileRepository.saveAll(files.stream()
        .map(file -> new LowcodeGenerationFile(
            null,
            tenantId,
            recordId,
            file.getTemplateCode(),
            file.getPath(),
            resolveFileType(file.getPath()),
            overwriteStrategy,
            file.getContentHash()))
        .toList());
  }

  /**
   * 解析生成文件类型。
   *
   * @param filePath 生成文件路径
   * @return 文件类型
   */
  private String resolveFileType(String filePath) {
    int dotIndex = filePath.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == filePath.length() - 1) {
      return "unknown";
    }
    return filePath.substring(dotIndex + 1).toLowerCase();
  }

  /**
   * 构建生成文件清单 JSON。
   *
   * @param files 生成文件列表
   * @param overwriteStrategy 写入覆盖策略
   * @return 生成文件清单 JSON
   */
  private String buildFileManifestJson(List<GeneratedFile> files,
                                       GeneratedFileOverwriteStrategy overwriteStrategy) {
    return files.stream()
        .map(file -> """
            {"templateCode":"%s","targetPath":"%s","contentHash":"%s","writeMode":"%s"}"""
            .formatted(
                escapeJson(file.getTemplateCode()),
                escapeJson(file.getPath()),
                escapeJson(file.getContentHash()),
                escapeJson(overwriteStrategy.name())))
        .collect(java.util.stream.Collectors.joining(",", "[", "]"));
  }

  /**
   * 转义 JSON 字符串片段。
   *
   * @param value 原始文本
   * @return JSON 安全文本
   */
  private String escapeJson(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }
}
