/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.repository;

import com.zhyc.message.inbox.domain.MsgMessage;
import com.zhyc.message.inbox.mapper.MsgMessageMapper;
import com.zhyc.message.inbox.service.MsgMessageQuery;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的站内消息仓储实现。
 */
@Repository
public class MyBatisMsgMessageRepository implements MsgMessageRepository {

  /** 站内消息 Mapper。 */
  private final MsgMessageMapper messageMapper;

  /**
   * 创建站内消息仓储实现。
   *
   * @param messageMapper 站内消息 Mapper
   */
  public MyBatisMsgMessageRepository(MsgMessageMapper messageMapper) {
    this.messageMapper = Objects.requireNonNull(messageMapper, "站内消息 Mapper 不能为空");
  }

  @Override
  public long countByQuery(MsgMessageQuery query) {
    return messageMapper.countByQuery(query.tenantId(), query.receiverId(), query.readFlag());
  }

  @Override
  public List<MsgMessage> findPageByQuery(MsgMessageQuery query, int offset) {
    return messageMapper.selectPageByQuery(query.tenantId(), query.receiverId(), query.readFlag(),
        query.pageSize(), offset);
  }

  @Override
  public void save(MsgMessage message) {
    messageMapper.insert(message);
  }

  @Override
  public int markRead(String tenantId, String messageCode, Long receiverId) {
    return messageMapper.markRead(tenantId, messageCode, receiverId);
  }
}
