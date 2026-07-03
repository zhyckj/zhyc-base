/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.service;

/**
 * 站内消息查询条件。
 *
 * @param tenantId 租户业务编码
 * @param receiverId 接收人用户 ID
 * @param readFlag 已读状态，空值表示全部
 * @param pageNo 当前页码
 * @param pageSize 每页记录数
 */
public record MsgMessageQuery(String tenantId, Long receiverId, Boolean readFlag, int pageNo, int pageSize) {
}
