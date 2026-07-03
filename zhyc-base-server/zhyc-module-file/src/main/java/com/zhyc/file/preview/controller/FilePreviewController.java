/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.preview.service.FilePreviewCreateCommand;
import com.zhyc.file.preview.service.FilePreviewLogQuery;
import com.zhyc.file.preview.service.FilePreviewLogResponse;
import com.zhyc.file.preview.service.FilePreviewRenderResponse;
import com.zhyc.file.preview.service.FilePreviewResponse;
import com.zhyc.file.preview.service.FilePreviewService;
import java.util.List;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件预览管理接口。
 */
@RestController
@RequestMapping("/file/preview")
public class FilePreviewController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 文件预览创建请求为空错误码。 */
  private static final String ERROR_CREATE_REQUEST_REQUIRED = "ZHYC_FILE_PREVIEW_CREATE_REQUEST_REQUIRED";

  /** 文件预览业务服务。 */
  private final FilePreviewService filePreviewService;

  /**
   * 创建文件预览管理接口。
   *
   * @param filePreviewService 文件预览业务服务
   */
  public FilePreviewController(FilePreviewService filePreviewService) {
    this.filePreviewService = Objects.requireNonNull(filePreviewService, "文件预览业务服务不能为空");
  }

  /**
   * 创建文件预览。
   *
   * @param command 文件预览创建命令
   * @return 文件预览响应
   */
  @RequiresPermissions("file:preview:create")
  @PostMapping("")
  public ApiResult<FilePreviewResponse> createPreview(@RequestBody FilePreviewCreateCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_CREATE_REQUEST_REQUIRED, "文件预览创建请求不能为空");
    }
    return ApiResult.ok(filePreviewService.createPreview(command));
  }

  /**
   * 渲染文件预览。
   *
   * @param tenantId 租户业务编码
   * @param fileCode 文件业务编码
   * @param previewType 预览类型
   * @return 文件预览渲染响应
   */
  @RequiresPermissions("file:preview:view")
  @GetMapping("/render/{fileCode}")
  public ApiResult<FilePreviewRenderResponse> renderPreview(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("fileCode") String fileCode,
      @RequestParam(value = "type", required = false) String previewType) {
    return ApiResult.ok(filePreviewService.renderPreview(tenantId, fileCode, previewType));
  }

  /**
   * 查询文件预览日志。
   *
   * @param tenantId 租户业务编码
   * @param fileCode 文件业务编码
   * @return 文件预览日志列表
   */
  @RequiresPermissions("file:preview:query")
  @GetMapping("/logs")
  public ApiResult<List<FilePreviewLogResponse>> listPreviewLogs(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "fileCode", required = false) String fileCode) {
    return ApiResult.ok(filePreviewService.listPreviewLogs(new FilePreviewLogQuery(tenantId, fileCode)));
  }
}
