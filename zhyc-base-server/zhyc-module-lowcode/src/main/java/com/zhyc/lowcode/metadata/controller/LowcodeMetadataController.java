/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.lowcode.metadata.dto.LowcodeDataSourceResponse;
import com.zhyc.lowcode.metadata.dto.LowcodeDataSourceSaveRequest;
import com.zhyc.lowcode.metadata.service.LowcodeDataSourceConnectionTestResult;
import com.zhyc.lowcode.metadata.dto.LowcodePageModelResponse;
import com.zhyc.lowcode.metadata.dto.LowcodePageModelSaveRequest;
import com.zhyc.lowcode.metadata.dto.LowcodePhysicalTableImportRequest;
import com.zhyc.lowcode.metadata.dto.LowcodePhysicalTableResponse;
import com.zhyc.lowcode.metadata.dto.LowcodeTableRelationResponse;
import com.zhyc.lowcode.metadata.dto.LowcodeTableRelationSaveRequest;
import com.zhyc.lowcode.metadata.dto.LowcodeTableModelResponse;
import com.zhyc.lowcode.metadata.dto.LowcodeTableModelSaveRequest;
import com.zhyc.lowcode.metadata.service.LowcodeMetadataService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 低代码元数据管理接口。
 *
 * <p>面向后台管理端提供数据源、表模型和发布操作，具体建模规则由服务层和领域模型负责。</p>
 */
@RestController
@RequestMapping("/lowcode/metadata")
public class LowcodeMetadataController {

  /** 数据源保存请求缺失错误码。 */
  private static final String ERROR_DATASOURCE_SAVE_REQUEST_REQUIRED =
      "ZHYC_LOWCODE_METADATA_DATASOURCE_SAVE_REQUEST_REQUIRED";

  /** 数据源连接测试请求缺失错误码。 */
  private static final String ERROR_DATASOURCE_TEST_REQUEST_REQUIRED =
      "ZHYC_LOWCODE_METADATA_DATASOURCE_TEST_REQUEST_REQUIRED";

  /** 表模型保存请求缺失错误码。 */
  private static final String ERROR_TABLE_MODEL_SAVE_REQUEST_REQUIRED =
      "ZHYC_LOWCODE_METADATA_TABLE_MODEL_SAVE_REQUEST_REQUIRED";

  /** 物理表导入请求缺失错误码。 */
  private static final String ERROR_PHYSICAL_TABLE_IMPORT_REQUEST_REQUIRED =
      "ZHYC_LOWCODE_METADATA_PHYSICAL_TABLE_IMPORT_REQUEST_REQUIRED";

  /** 表关系保存请求缺失错误码。 */
  private static final String ERROR_TABLE_RELATION_SAVE_REQUEST_REQUIRED =
      "ZHYC_LOWCODE_METADATA_TABLE_RELATION_SAVE_REQUEST_REQUIRED";

  /** 页面模型保存请求缺失错误码。 */
  private static final String ERROR_PAGE_MODEL_SAVE_REQUEST_REQUIRED =
      "ZHYC_LOWCODE_METADATA_PAGE_MODEL_SAVE_REQUEST_REQUIRED";

  /** 低代码元数据服务。 */
  private final LowcodeMetadataService metadataService;

  /**
   * 创建低代码元数据管理接口。
   *
   * @param metadataService 低代码元数据服务
   */
  public LowcodeMetadataController(LowcodeMetadataService metadataService) {
    this.metadataService = Objects.requireNonNull(metadataService, "低代码元数据服务不能为空");
  }

  /**
   * 查询数据源列表。
   *
   * @param tenantId 租户业务编码
   * @return 数据源响应列表，不包含口令密钥引用
   */
  @RequiresPermissions("lowcode:datasource:query")
  @GetMapping("/data-sources")
  public ApiResult<List<LowcodeDataSourceResponse>> listDataSources(@RequestParam("tenantId") String tenantId) {
    return ApiResult.ok(metadataService.listDataSources(tenantId).stream()
        .map(LowcodeDataSourceResponse::from)
        .toList());
  }

  /**
   * 保存数据源定义。
   *
   * @param request 数据源保存请求
   * @return 保存后的数据源响应，不包含口令密钥引用
   */
  @RequiresPermissions("lowcode:datasource:save")
  @PostMapping("/data-sources")
  public ApiResult<LowcodeDataSourceResponse> saveDataSource(@RequestBody LowcodeDataSourceSaveRequest request) {
    if (request == null) {
      throw new BusinessException(ERROR_DATASOURCE_SAVE_REQUEST_REQUIRED, "数据源保存请求不能为空");
    }
    return ApiResult.ok(LowcodeDataSourceResponse.from(metadataService.saveDataSource(request.toDomain())));
  }

  /**
   * 查询数据源定义。
   *
   * @param tenantId 租户业务编码
   * @param code 数据源编码
   * @return 数据源响应，不包含口令密钥引用
   */
  @RequiresPermissions("lowcode:datasource:query")
  @GetMapping("/data-sources/{code}")
  public ApiResult<LowcodeDataSourceResponse> getDataSource(@RequestParam("tenantId") String tenantId,
                                                            @PathVariable("code") String code) {
    return ApiResult.ok(LowcodeDataSourceResponse.from(metadataService.getDataSource(tenantId, code)));
  }

  /**
   * 测试数据源连接配置。
   *
   * <p>首期仅做不含明文口令的轻量预检查，避免后台管理端直接接触数据库口令。</p>
   *
   * @param request 数据源连接测试请求
   * @return 数据源连接测试结果
   */
  @RequiresPermissions("lowcode:datasource:test")
  @PostMapping("/data-sources/test-connection")
  public ApiResult<LowcodeDataSourceConnectionTestResult> testDataSourceConnection(
      @RequestBody LowcodeDataSourceConnectionTestRequest request) {
    if (request == null) {
      throw new BusinessException(ERROR_DATASOURCE_TEST_REQUEST_REQUIRED, "数据源连接测试请求不能为空");
    }
    return ApiResult.ok(metadataService.testDataSourceConnection(request.getTenantId(), request.getCode()));
  }

  /**
   * 查询数据源物理表清单。
   *
   * @param tenantId 租户业务编码
   * @param dataSourceId 数据源主键
   * @return 物理表清单
   */
  @RequiresPermissions("lowcode:table:import")
  @GetMapping("/data-sources/{dataSourceId}/tables")
  public ApiResult<List<LowcodePhysicalTableResponse>> listPhysicalTables(
      @RequestParam("tenantId") String tenantId,
      @PathVariable("dataSourceId") Long dataSourceId) {
    return ApiResult.ok(metadataService.listPhysicalTables(tenantId, dataSourceId).stream()
        .map(LowcodePhysicalTableResponse::from)
        .toList());
  }

  /**
   * 保存低代码表模型。
   *
   * @param request 表模型保存请求
   * @return 保存后的表模型响应
   */
  @RequiresPermissions("lowcode:table:save")
  @PostMapping("/table-models")
  public ApiResult<LowcodeTableModelResponse> saveTableModel(@RequestBody LowcodeTableModelSaveRequest request) {
    if (request == null) {
      throw new BusinessException(ERROR_TABLE_MODEL_SAVE_REQUEST_REQUIRED, "表模型保存请求不能为空");
    }
    return ApiResult.ok(LowcodeTableModelResponse.from(metadataService.saveTableModel(request.toDomain())));
  }

  /**
   * 查询低代码表模型列表。
   *
   * @param tenantId 租户业务编码
   * @return 表模型响应列表
   */
  @RequiresPermissions("lowcode:table:query")
  @GetMapping("/table-models")
  public ApiResult<List<LowcodeTableModelResponse>> listTableModels(@RequestParam("tenantId") String tenantId) {
    return ApiResult.ok(metadataService.listTableModels(tenantId).stream()
        .map(LowcodeTableModelResponse::from)
        .toList());
  }

  /**
   * 查询低代码表模型。
   *
   * @param tenantId 租户业务编码
   * @param code 模型编码
   * @return 表模型响应
   */
  @RequiresPermissions("lowcode:table:query")
  @GetMapping("/table-models/{code}")
  public ApiResult<LowcodeTableModelResponse> getTableModel(@RequestParam("tenantId") String tenantId,
                                                            @PathVariable("code") String code) {
    return ApiResult.ok(LowcodeTableModelResponse.from(metadataService.getTableModel(tenantId, code)));
  }

  /**
   * 从数据源物理表导入低代码表模型。
   *
   * @param request 物理表导入请求
   * @return 导入并保存后的表模型响应
   */
  @RequiresPermissions("lowcode:table:import")
  @PostMapping("/table-models/import")
  public ApiResult<LowcodeTableModelResponse> importTableModel(
      @RequestBody LowcodePhysicalTableImportRequest request) {
    if (request == null) {
      throw new BusinessException(ERROR_PHYSICAL_TABLE_IMPORT_REQUEST_REQUIRED, "物理表导入请求不能为空");
    }
    return ApiResult.ok(LowcodeTableModelResponse.from(metadataService.importTableModel(
        request.getTenantId(),
        request.getDataSourceId(),
        request.getTableName(),
        request.getModelCode(),
        request.getModelName())));
  }

  /**
   * 发布低代码表模型。
   *
   * @param tenantId 租户业务编码
   * @param code 模型编码
   * @return 发布后的表模型响应
   */
  @RequiresPermissions("lowcode:table:publish")
  @PostMapping("/table-models/{code}/publish")
  public ApiResult<LowcodeTableModelResponse> publishTableModel(@RequestParam("tenantId") String tenantId,
                                                                @PathVariable("code") String code) {
    return ApiResult.ok(LowcodeTableModelResponse.from(metadataService.publishTableModel(tenantId, code)));
  }

  /**
   * 保存低代码表关系。
   *
   * @param request 表关系保存请求
   * @return 保存后的表关系响应
   */
  @RequiresPermissions("lowcode:relation:save")
  @PostMapping("/table-relations")
  public ApiResult<LowcodeTableRelationResponse> saveTableRelation(
      @RequestBody LowcodeTableRelationSaveRequest request) {
    if (request == null) {
      throw new BusinessException(ERROR_TABLE_RELATION_SAVE_REQUEST_REQUIRED, "表关系保存请求不能为空");
    }
    return ApiResult.ok(LowcodeTableRelationResponse.from(metadataService.saveTableRelation(request.toDomain())));
  }

  /**
   * 查询低代码表关系列表。
   *
   * @param tenantId 租户业务编码
   * @return 表关系响应列表
   */
  @RequiresPermissions("lowcode:relation:query")
  @GetMapping("/table-relations")
  public ApiResult<List<LowcodeTableRelationResponse>> listTableRelations(@RequestParam("tenantId") String tenantId) {
    return ApiResult.ok(metadataService.listTableRelations(tenantId).stream()
        .map(LowcodeTableRelationResponse::from)
        .toList());
  }

  /**
   * 保存低代码页面模型。
   *
   * @param request 页面模型保存请求
   * @return 保存后的页面模型响应
   */
  @RequiresPermissions("lowcode:page:save")
  @PostMapping("/page-models")
  public ApiResult<LowcodePageModelResponse> savePageModel(@RequestBody LowcodePageModelSaveRequest request) {
    if (request == null) {
      throw new BusinessException(ERROR_PAGE_MODEL_SAVE_REQUEST_REQUIRED, "页面模型保存请求不能为空");
    }
    return ApiResult.ok(LowcodePageModelResponse.from(metadataService.savePageModel(request.toDomain())));
  }

  /**
   * 查询低代码页面模型列表。
   *
   * @param tenantId 租户业务编码
   * @return 页面模型响应列表
   */
  @RequiresPermissions("lowcode:page:query")
  @GetMapping("/page-models")
  public ApiResult<List<LowcodePageModelResponse>> listPageModels(@RequestParam("tenantId") String tenantId) {
    return ApiResult.ok(metadataService.listPageModels(tenantId).stream()
        .map(LowcodePageModelResponse::from)
        .toList());
  }
}
