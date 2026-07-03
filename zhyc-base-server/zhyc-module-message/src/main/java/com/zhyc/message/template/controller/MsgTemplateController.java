/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.message.template.service.MsgTemplateResponse;
import com.zhyc.message.template.service.MsgTemplateSaveCommand;
import com.zhyc.message.template.service.MsgTemplateService;
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
 * 消息模板管理接口。
 */
@RestController
@RequestMapping("/message/templates")
public class MsgTemplateController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 消息模板保存请求为空错误码。 */
  private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_MESSAGE_TEMPLATE_SAVE_REQUEST_REQUIRED";

  /** 消息模板业务服务。 */
  private final MsgTemplateService templateService;

  /**
   * 创建消息模板管理接口。
   *
   * @param templateService 消息模板业务服务
   */
  public MsgTemplateController(MsgTemplateService templateService) {
    this.templateService = Objects.requireNonNull(templateService, "消息模板业务服务不能为空");
  }

  /**
   * 查询租户消息模板。
   *
   * @param tenantId 租户业务编码
   * @return 消息模板列表
   */
  @RequiresPermissions("message:template:query")
  @GetMapping("")
  public ApiResult<List<MsgTemplateResponse>> listTemplates(
      @RequestHeader(HEADER_TENANT_ID) String tenantId) {
    return ApiResult.ok(templateService.listTemplates(tenantId));
  }

  /**
   * 保存或更新消息模板。
   *
   * @param command 消息模板保存命令
   * @return 空响应
   */
  @RequiresPermissions("message:template:save")
  @PutMapping("")
  public ApiResult<Void> save(@RequestBody MsgTemplateSaveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "消息模板保存请求不能为空");
    }
    templateService.save(command);
    return ApiResult.ok(null);
  }
}
