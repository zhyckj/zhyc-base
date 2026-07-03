/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 低代码生成管理接口。
 *
 * <p>面向后台管理端提供模板清单、生成预览和生成执行能力。</p>
 */
@RestController
@RequestMapping("/lowcode/generator")
public class LowcodeGeneratorController {

  /** 生成预览请求缺失错误码。 */
  private static final String ERROR_PREVIEW_REQUEST_REQUIRED = "ZHYC_LOWCODE_GENERATION_PREVIEW_REQUEST_REQUIRED";

  /** 生成校验请求缺失错误码。 */
  private static final String ERROR_VALIDATE_REQUEST_REQUIRED = "ZHYC_LOWCODE_GENERATION_VALIDATE_REQUEST_REQUIRED";

  /** 生成执行请求缺失错误码。 */
  private static final String ERROR_EXECUTE_REQUEST_REQUIRED = "ZHYC_LOWCODE_GENERATION_EXECUTE_REQUEST_REQUIRED";

  /** 低代码生成服务。 */
  private final LowcodeGeneratorService generatorService;

  /**
   * 创建低代码生成管理接口。
   *
   * @param generatorService 低代码生成服务
   */
  public LowcodeGeneratorController(LowcodeGeneratorService generatorService) {
    this.generatorService = Objects.requireNonNull(generatorService, "低代码生成服务不能为空");
  }

  /**
   * 查询指定目标端的模板清单。
   *
   * @param target 生成目标端编码
   * @return 模板响应列表
   */
  @RequiresPermissions("lowcode:generator:query")
  @GetMapping("/templates")
  public ApiResult<List<LowcodeTemplateResponse>> listTemplates(@RequestParam("target") String target) {
    return ApiResult.ok(generatorService.listTemplates(GenerationTarget.fromCode(target)).stream()
        .map(LowcodeTemplateResponse::from)
        .toList());
  }

  /**
   * 预览生成文件清单和内容。
   *
   * @param request 生成预览请求
   * @return 生成文件预览列表
   */
  @RequiresPermissions("lowcode:generator:query")
  @PostMapping("/preview")
  public ApiResult<List<LowcodeGeneratedFileResponse>> preview(
      @RequestBody LowcodeGenerationPreviewRequest request) {
    if (request == null) {
      throw new BusinessException(ERROR_PREVIEW_REQUEST_REQUIRED, "低代码生成预览请求不能为空");
    }
    return ApiResult.ok(generatorService.preview(request.toCommand()).stream()
        .map(LowcodeGeneratedFileResponse::from)
        .toList());
  }

  /**
   * 执行生成前校验。
   *
   * @param request 生成预览请求
   * @return 生成前校验结果
   */
  @RequiresPermissions("lowcode:generator:query")
  @PostMapping("/validate")
  public ApiResult<LowcodeGenerationValidationResult> validate(
      @RequestBody LowcodeGenerationPreviewRequest request) {
    if (request == null) {
      throw new BusinessException(ERROR_VALIDATE_REQUEST_REQUIRED, "低代码生成校验请求不能为空");
    }
    return ApiResult.ok(generatorService.validate(request.toCommand()));
  }

  /**
   * 执行生成并返回生成记录。
   *
   * @param request 生成执行请求
   * @return 生成记录响应对象
   */
  @RequiresPermissions("lowcode:generator:execute")
  @PostMapping("/execute")
  public ApiResult<LowcodeGenerationRecordResponse> execute(
      @RequestBody LowcodeGenerationExecuteRequest request) {
    if (request == null) {
      throw new BusinessException(ERROR_EXECUTE_REQUEST_REQUIRED, "低代码生成执行请求不能为空");
    }
    return ApiResult.ok(LowcodeGenerationRecordResponse.from(generatorService.execute(request.toCommand())));
  }

  /**
   * 查询租户内生成记录。
   *
   * @param tenantId 租户业务编码
   * @return 生成记录响应列表
   */
  @RequiresPermissions("lowcode:generator:query")
  @GetMapping("/records")
  public ApiResult<List<LowcodeGenerationRecordResponse>> listRecords(@RequestParam("tenantId") String tenantId) {
    return ApiResult.ok(generatorService.listRecords(tenantId).stream()
        .map(LowcodeGenerationRecordResponse::from)
        .toList());
  }

  /**
   * 查询生成记录对应的文件明细。
   *
   * @param tenantId 租户业务编码
   * @param recordId 生成记录主键
   * @return 生成文件明细响应列表
   */
  @RequiresPermissions("lowcode:generator:query")
  @GetMapping("/files")
  public ApiResult<List<LowcodeGenerationFileResponse>> listGenerationFiles(
      @RequestParam("tenantId") String tenantId,
      @RequestParam("recordId") Long recordId) {
    return ApiResult.ok(generatorService.listGenerationFiles(tenantId, recordId).stream()
        .map(LowcodeGenerationFileResponse::from)
        .toList());
  }
}
