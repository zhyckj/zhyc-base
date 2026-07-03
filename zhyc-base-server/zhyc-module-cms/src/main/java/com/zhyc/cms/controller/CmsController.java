/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.controller;

import com.zhyc.cms.service.CmsChannelResponse;
import com.zhyc.cms.service.CmsChannelSaveCommand;
import com.zhyc.cms.service.CmsContentResponse;
import com.zhyc.cms.service.CmsContentSaveCommand;
import com.zhyc.cms.service.CmsContentStatusCommand;
import com.zhyc.cms.service.CmsService;
import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
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
 * 内容管理接口。
 */
@RestController
@RequestMapping("/cms")
public class CmsController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 内容栏目保存请求为空错误码。 */
  private static final String ERROR_CHANNEL_SAVE_REQUEST_REQUIRED = "ZHYC_CMS_CHANNEL_SAVE_REQUEST_REQUIRED";

  /** 内容文章保存请求为空错误码。 */
  private static final String ERROR_CONTENT_SAVE_REQUEST_REQUIRED = "ZHYC_CMS_CONTENT_SAVE_REQUEST_REQUIRED";

  /** 内容文章状态变更请求为空错误码。 */
  private static final String ERROR_CONTENT_STATUS_REQUEST_REQUIRED = "ZHYC_CMS_CONTENT_STATUS_REQUEST_REQUIRED";

  /** 内容管理业务服务。 */
  private final CmsService cmsService;

  /**
   * 创建内容管理接口。
   *
   * @param cmsService 内容管理业务服务
   */
  public CmsController(CmsService cmsService) {
    this.cmsService = Objects.requireNonNull(cmsService, "内容管理业务服务不能为空");
  }

  /**
   * 查询内容栏目列表。
   *
   * @param tenantId 租户业务编码
   * @param status 栏目状态
   * @return 内容栏目列表
   */
  @RequiresPermissions("cms:channel:query")
  @GetMapping("/channels")
  public ApiResult<List<CmsChannelResponse>> listChannels(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "status", required = false) String status) {
    return ApiResult.ok(cmsService.listChannels(tenantId, status));
  }

  /**
   * 保存内容栏目。
   *
   * @param command 内容栏目保存命令
   * @return 空响应
   */
  @RequiresPermissions("cms:channel:save")
  @PostMapping("/channels")
  public ApiResult<Void> saveChannel(@RequestBody CmsChannelSaveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_CHANNEL_SAVE_REQUEST_REQUIRED, "内容栏目保存请求不能为空");
    }
    cmsService.saveChannel(command);
    return ApiResult.ok(null);
  }

  /**
   * 查询内容文章列表。
   *
   * @param tenantId 租户业务编码
   * @param channelCode 栏目编码
   * @param status 文章状态
   * @return 内容文章列表
   */
  @RequiresPermissions("cms:content:query")
  @GetMapping("/contents")
  public ApiResult<List<CmsContentResponse>> listContents(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "channelCode", required = false) String channelCode,
      @RequestParam(value = "status", required = false) String status) {
    return ApiResult.ok(cmsService.listContents(tenantId, channelCode, status));
  }

  /**
   * 保存内容文章。
   *
   * @param command 内容文章保存命令
   * @return 空响应
   */
  @RequiresPermissions("cms:content:save")
  @PostMapping("/contents")
  public ApiResult<Void> saveContent(@RequestBody CmsContentSaveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_CONTENT_SAVE_REQUEST_REQUIRED, "内容文章保存请求不能为空");
    }
    cmsService.saveContent(command);
    return ApiResult.ok(null);
  }

  /**
   * 更新内容文章状态。
   *
   * @param tenantId 租户业务编码
   * @param id 文章主键
   * @param command 文章状态变更命令
   * @return 空响应
   */
  @RequiresPermissions("cms:content:publish")
  @PostMapping("/contents/{id}/status")
  public ApiResult<Void> updateContentStatus(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("id") Long id, @RequestBody CmsContentStatusCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_CONTENT_STATUS_REQUEST_REQUIRED, "内容文章状态变更请求不能为空");
    }
    cmsService.updateContentStatus(tenantId, id, command.status());
    return ApiResult.ok(null);
  }
}
