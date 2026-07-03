/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.service;

import com.zhyc.common.api.PageResult;

/**
 * 站内消息业务服务。
 */
public interface MsgMessageService {

  /**
   * 发送站内消息。
   *
   * @param command 站内消息发送命令
   * @return 消息编码
   */
  String send(MsgMessageSendCommand command);

  /**
   * 按启用模板发送站内消息。
   *
   * @param command 站内消息模板发送命令
   * @return 消息编码
   */
  String sendByTemplate(MsgMessageTemplateSendCommand command);

  /**
   * 分页查询接收人消息。
   *
   * @param query 消息查询条件
   * @return 消息分页响应
   */
  PageResult<MsgMessageResponse> listMessages(MsgMessageQuery query);

  /**
   * 标记站内消息为已读。
   *
   * @param tenantId 租户业务编码
   * @param messageCode 消息编码
   * @param receiverId 接收人用户 ID
   */
  void markRead(String tenantId, String messageCode, Long receiverId);
}
