/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.service;

import java.util.Map;

/**
 * 站内消息模板发送命令。
 *
 * @param tenantId 租户业务编码
 * @param templateCode 消息模板编码
 * @param receiverId 接收人用户 ID
 * @param receiverName 接收人名称
 * @param messageType 消息类型
 * @param variables 模板变量，key 对应模板中的 ${变量名}
 */
public record MsgMessageTemplateSendCommand(String tenantId, String templateCode,
                                            Long receiverId, String receiverName,
                                            String messageType, Map<String, String> variables) {
}
