/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.storage.service.FileStorageConfigResponse;
import com.zhyc.file.storage.service.FileStorageConfigSaveCommand;
import com.zhyc.file.storage.service.FileStorageConfigService;
import java.util.List;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件存储配置管理接口。
 */
@RestController
@RequestMapping("/file/storage-configs")
public class FileStorageConfigController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 文件存储配置保存请求为空错误码。 */
  private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_FILE_STORAGE_CONFIG_SAVE_REQUEST_REQUIRED";

  /** 文件存储配置业务服务。 */
  private final FileStorageConfigService configService;

  /**
   * 创建文件存储配置管理接口。
   *
   * @param configService 文件存储配置业务服务
   */
  public FileStorageConfigController(FileStorageConfigService configService) {
    this.configService = Objects.requireNonNull(configService, "文件存储配置业务服务不能为空");
  }

  /**
   * 查询租户文件存储配置。
   *
   * @param tenantId 租户业务编码
   * @return 文件存储配置列表
   */
  @RequiresPermissions("file:storage:query")
  @GetMapping("")
  public ApiResult<List<FileStorageConfigResponse>> listConfigs(
      @RequestHeader(HEADER_TENANT_ID) String tenantId) {
    return ApiResult.ok(configService.listConfigs(tenantId));
  }

  /**
   * 保存或更新文件存储配置。
   *
   * @param command 文件存储配置保存命令
   * @return 空响应
   */
  @RequiresPermissions("file:storage:save")
  @PutMapping("")
  public ApiResult<Void> save(@RequestBody FileStorageConfigSaveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "文件存储配置保存请求不能为空");
    }
    configService.save(command);
    return ApiResult.ok(null);
  }
}
