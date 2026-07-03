/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.repository;

import com.zhyc.message.inbox.domain.MsgMessage;
import com.zhyc.message.inbox.service.MsgMessageQuery;
import java.util.List;

/**
 * 站内消息仓储接口。
 */
public interface MsgMessageRepository {

  /**
   * 统计接收人消息数量。
   *
   * @param query 消息查询条件
   * @return 消息数量
   */
  long countByQuery(MsgMessageQuery query);

  /**
   * 分页查询接收人消息。
   *
   * @param query 消息查询条件
   * @param offset 分页偏移量
   * @return 消息列表
   */
  List<MsgMessage> findPageByQuery(MsgMessageQuery query, int offset);

  /**
   * 保存站内消息。
   *
   * @param message 站内消息
   */
  void save(MsgMessage message);

  /**
   * 标记站内消息为已读。
   *
   * @param tenantId 租户业务编码
   * @param messageCode 消息编码
   * @param receiverId 接收人用户 ID
   * @return 更新记录数
   */
  int markRead(String tenantId, String messageCode, Long receiverId);
}
