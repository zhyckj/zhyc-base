/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.i18n.service.I18nMessageResponse;
import com.zhyc.i18n.service.I18nMessageSaveCommand;
import com.zhyc.i18n.service.I18nResolveCommand;
import com.zhyc.i18n.service.I18nResolveResponse;
import com.zhyc.i18n.service.I18nService;
import java.util.List;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 国际化词条管理接口。
 */
@RestController
@RequestMapping("/i18n")
public class I18nController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 国际化词条保存请求为空错误码。 */
  private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_I18N_MESSAGE_SAVE_REQUEST_REQUIRED";

  /** 国际化词条解析请求为空错误码。 */
  private static final String ERROR_RESOLVE_REQUEST_REQUIRED = "ZHYC_I18N_MESSAGE_RESOLVE_REQUEST_REQUIRED";

  /** 国际化词条业务服务。 */
  private final I18nService i18nService;

  /**
   * 创建国际化词条接口。
   *
   * @param i18nService 国际化词条业务服务
   */
  public I18nController(I18nService i18nService) {
    this.i18nService = Objects.requireNonNull(i18nService, "国际化词条业务服务不能为空");
  }

  /**
   * 查询国际化词条列表。
   *
   * @param tenantId 租户业务编码
   * @param locale 语言标识
   * @param status 词条状态
   * @return 国际化词条列表
   */
  @RequiresPermissions("i18n:message:query")
  @GetMapping("/messages")
  public ApiResult<List<I18nMessageResponse>> listMessages(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "locale", required = false) String locale,
      @RequestParam(value = "status", required = false) String status) {
    return ApiResult.ok(i18nService.listMessages(tenantId, locale, status));
  }

  /**
   * 保存国际化词条。
   *
   * @param command 国际化词条保存命令
   * @return 空响应
   */
  @RequiresPermissions("i18n:message:save")
  @PostMapping("/messages")
  public ApiResult<Void> saveMessage(@RequestBody I18nMessageSaveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "国际化词条保存请求不能为空");
    }
    i18nService.saveMessage(command);
    return ApiResult.ok(null);
  }

  /**
   * 批量解析国际化词条。
   *
   * @param tenantId 租户业务编码
   * @param command 国际化词条批量解析命令
   * @return 国际化词条批量解析响应
   */
  @RequiresPermissions("i18n:message:resolve")
  @PostMapping("/messages/resolve")
  public ApiResult<I18nResolveResponse> resolveMessages(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestBody I18nResolveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_RESOLVE_REQUEST_REQUIRED, "国际化词条解析请求不能为空");
    }
    return ApiResult.ok(i18nService.resolveMessages(new I18nResolveCommand(tenantId,
        command.locale(), command.defaults())));
  }
}
