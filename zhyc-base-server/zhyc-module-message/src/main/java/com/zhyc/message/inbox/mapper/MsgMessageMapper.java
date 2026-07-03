/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.mapper;

import com.zhyc.message.inbox.domain.MsgMessage;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 站内消息 MyBatis Mapper。
 */
@Mapper
public interface MsgMessageMapper {

  /**
   * 统计接收人消息数量。
   *
   * @param tenantId 租户业务编码
   * @param receiverId 接收人用户 ID
   * @param readFlag 已读状态
   * @return 消息数量
   */
  @SelectProvider(type = MsgMessageSqlProvider.class, method = "countByQuery")
  long countByQuery(@Param("tenantId") String tenantId, @Param("receiverId") Long receiverId,
      @Param("readFlag") Boolean readFlag);

  /**
   * 分页查询接收人消息。
   *
   * @param tenantId 租户业务编码
   * @param receiverId 接收人用户 ID
   * @param readFlag 已读状态
   * @param pageSize 每页记录数
   * @param offset 分页偏移量
   * @return 消息列表
   */
  @SelectProvider(type = MsgMessageSqlProvider.class, method = "selectPageByQuery")
  List<MsgMessage> selectPageByQuery(@Param("tenantId") String tenantId,
      @Param("receiverId") Long receiverId, @Param("readFlag") Boolean readFlag,
      @Param("pageSize") int pageSize, @Param("offset") int offset);

  /**
   * 写入站内消息。
   *
   * @param message 站内消息
   */
  @InsertProvider(type = MsgMessageSqlProvider.class, method = "insert")
  void insert(MsgMessage message);

  /**
   * 标记站内消息为已读。
   *
   * @param tenantId 租户业务编码
   * @param messageCode 消息编码
   * @param receiverId 接收人用户 ID
   * @return 更新记录数
   */
  @UpdateProvider(type = MsgMessageSqlProvider.class, method = "markRead")
  int markRead(@Param("tenantId") String tenantId, @Param("messageCode") String messageCode,
      @Param("receiverId") Long receiverId);
}
