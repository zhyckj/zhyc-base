/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.object.service.FileObjectQuery;
import com.zhyc.file.object.service.FileObjectRegisterCommand;
import com.zhyc.file.object.service.FileObjectResponse;
import com.zhyc.file.object.service.FileObjectService;
import com.zhyc.file.object.service.FileObjectUploadCommand;
import com.zhyc.file.object.service.FileObjectUploadResponse;
import com.zhyc.file.object.service.FileObjectUploadService;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件对象管理接口。
 */
@RestController
@RequestMapping("/file/objects")
public class FileObjectController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 用户 ID 请求头。 */
  public static final String HEADER_USER_ID = "X-ZHYC-User-Id";

  /** 文件对象登记请求为空错误码。 */
  private static final String ERROR_REGISTER_REQUEST_REQUIRED = "ZHYC_FILE_OBJECT_REGISTER_REQUEST_REQUIRED";

  /** 文件对象业务服务。 */
  private final FileObjectService fileObjectService;
  /** 文件对象上传服务。 */
  private final FileObjectUploadService fileObjectUploadService;

  /**
   * 创建文件对象管理接口。
   *
   * @param fileObjectService 文件对象业务服务
   * @param fileObjectUploadService 文件对象上传服务
   */
  public FileObjectController(FileObjectService fileObjectService, FileObjectUploadService fileObjectUploadService) {
    this.fileObjectService = Objects.requireNonNull(fileObjectService, "文件对象业务服务不能为空");
    this.fileObjectUploadService = Objects.requireNonNull(fileObjectUploadService, "文件对象上传服务不能为空");
  }

  /**
   * 分页查询文件对象。
   *
   * @param tenantId 租户业务编码
   * @param keyword 文件名关键词
   * @param pageNo 当前页码
   * @param pageSize 每页记录数
   * @return 文件对象分页响应
   */
  @RequiresPermissions("file:object:query")
  @GetMapping("")
  public ApiResult<PageResult<FileObjectResponse>> listFiles(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
    return ApiResult.ok(fileObjectService.listFiles(new FileObjectQuery(tenantId, keyword, pageNo, pageSize)));
  }

  /**
   * 登记文件对象元数据。
   *
   * @param command 文件对象登记命令
   * @return 文件业务编码
   */
  @RequiresPermissions("file:object:register")
  @PostMapping("")
  public ApiResult<String> register(@RequestBody FileObjectRegisterCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_REGISTER_REQUEST_REQUIRED, "文件对象登记请求不能为空");
    }
    return ApiResult.ok(fileObjectService.register(command));
  }

  /**
   * 上传文件并登记文件对象元数据。
   *
   * @param tenantId 租户业务编码
   * @param userId 上传人用户 ID
   * @param storageCode 存储配置编码
   * @param file 上传文件
   * @return 文件对象上传响应
   */
  @RequiresPermissions("file:object:upload")
  @PostMapping("/upload")
  public ApiResult<FileObjectUploadResponse> upload(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(value = HEADER_USER_ID, required = false) Long userId,
      @RequestParam(value = "storageCode", required = false) String storageCode,
      @RequestParam("file") MultipartFile file) {
    return ApiResult.ok(fileObjectUploadService.upload(new FileObjectUploadCommand(tenantId, storageCode, userId, file)));
  }
}
