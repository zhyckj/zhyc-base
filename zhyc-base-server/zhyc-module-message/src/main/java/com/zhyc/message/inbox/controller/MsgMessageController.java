/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.message.inbox.service.MsgMessageQuery;
import com.zhyc.message.inbox.service.MsgMessageResponse;
import com.zhyc.message.inbox.service.MsgMessageSendCommand;
import com.zhyc.message.inbox.service.MsgMessageService;
import com.zhyc.message.inbox.service.MsgMessageTemplateSendCommand;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内消息管理接口。
 */
@RestController
@RequestMapping("/message/inbox")
public class MsgMessageController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";
  /** 当前用户 ID 请求头。 */
  public static final String HEADER_USER_ID = "X-ZHYC-User-Id";

  /** 站内消息发送请求为空错误码。 */
  private static final String ERROR_SEND_REQUEST_REQUIRED = "ZHYC_MESSAGE_INBOX_SEND_REQUEST_REQUIRED";
  /** 站内消息模板发送请求为空错误码。 */
  private static final String ERROR_TEMPLATE_SEND_REQUEST_REQUIRED =
      "ZHYC_MESSAGE_INBOX_TEMPLATE_SEND_REQUEST_REQUIRED";

  /** 站内消息业务服务。 */
  private final MsgMessageService messageService;

  /**
   * 创建站内消息管理接口。
   *
   * @param messageService 站内消息业务服务
   */
  public MsgMessageController(MsgMessageService messageService) {
    this.messageService = Objects.requireNonNull(messageService, "站内消息业务服务不能为空");
  }

  /**
   * 发送站内消息。
   *
   * @param command 站内消息发送命令
   * @return 消息编码
   */
  @RequiresPermissions("message:inbox:send")
  @PostMapping("")
  public ApiResult<String> send(@RequestBody MsgMessageSendCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_SEND_REQUEST_REQUIRED, "站内消息发送请求不能为空");
    }
    return ApiResult.ok(messageService.send(command));
  }

  /**
   * 按启用模板发送站内消息。
   *
   * @param command 站内消息模板发送命令
   * @return 消息编码
   */
  @RequiresPermissions("message:inbox:send")
  @PostMapping("/template")
  public ApiResult<String> sendByTemplate(@RequestBody MsgMessageTemplateSendCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_TEMPLATE_SEND_REQUEST_REQUIRED, "站内消息模板发送请求不能为空");
    }
    return ApiResult.ok(messageService.sendByTemplate(command));
  }

  /**
   * 分页查询当前用户站内消息。
   *
   * @param tenantId 租户业务编码
   * @param receiverId 当前用户 ID
   * @param readFlag 已读状态
   * @param pageNo 当前页码
   * @param pageSize 每页记录数
   * @return 消息分页响应
   */
  @RequiresPermissions("message:inbox:query")
  @GetMapping("")
  public ApiResult<PageResult<MsgMessageResponse>> listMessages(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_USER_ID) Long receiverId,
      @RequestParam(value = "readFlag", required = false) Boolean readFlag,
      @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
    return ApiResult.ok(messageService.listMessages(
        new MsgMessageQuery(tenantId, receiverId, readFlag, pageNo, pageSize)));
  }

  /**
   * 标记当前用户站内消息为已读。
   *
   * @param tenantId 租户业务编码
   * @param receiverId 当前用户 ID
   * @param messageCode 消息编码
   * @return 空响应
   */
  @RequiresPermissions("message:inbox:read")
  @PatchMapping("/{messageCode}/read")
  public ApiResult<Void> markRead(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(HEADER_USER_ID) Long receiverId, @PathVariable("messageCode") String messageCode) {
    messageService.markRead(tenantId, messageCode, receiverId);
    return ApiResult.ok(null);
  }
}
