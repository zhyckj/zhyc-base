/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox.domain;

import java.time.LocalDateTime;

/**
 * 站内消息领域模型。
 */
public class MsgMessage {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 消息编码，租户内唯一。 */
  private String messageCode;
  /** 接收人用户 ID。 */
  private Long receiverId;
  /** 接收人名称。 */
  private String receiverName;
  /** 消息类型。 */
  private String messageType;
  /** 消息标题。 */
  private String title;
  /** 消息内容。 */
  private String content;
  /** 是否已读。 */
  private boolean readFlag;
  /** 阅读时间。 */
  private LocalDateTime readAt;
  /** 创建时间。 */
  private LocalDateTime createdAt;

  /**
   * 创建空站内消息对象。
   */
  public MsgMessage() {
  }

  /**
   * 创建完整站内消息对象。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param messageCode 消息编码
   * @param receiverId 接收人用户 ID
   * @param receiverName 接收人名称
   * @param messageType 消息类型
   * @param title 消息标题
   * @param content 消息内容
   * @param readFlag 是否已读
   * @param readAt 阅读时间
   * @param createdAt 创建时间
   */
  public MsgMessage(Long id, String tenantId, String messageCode, Long receiverId, String receiverName,
      String messageType, String title, String content, boolean readFlag, LocalDateTime readAt,
      LocalDateTime createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.messageCode = messageCode;
    this.receiverId = receiverId;
    this.receiverName = receiverName;
    this.messageType = messageType;
    this.title = title;
    this.content = content;
    this.readFlag = readFlag;
    this.readAt = readAt;
    this.createdAt = createdAt;
  }

  /**
   * 返回数据库主键。
   *
   * @return 数据库主键
   */
  public Long getId() {
    return id;
  }

  /**
   * 设置数据库主键。
   *
   * @param id 数据库主键
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * 返回租户业务编码。
   *
   * @return 租户业务编码
   */
  public String getTenantId() {
    return tenantId;
  }

  /**
   * 设置租户业务编码。
   *
   * @param tenantId 租户业务编码
   */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  /**
   * 返回消息编码。
   *
   * @return 消息编码
   */
  public String getMessageCode() {
    return messageCode;
  }

  /**
   * 设置消息编码。
   *
   * @param messageCode 消息编码
   */
  public void setMessageCode(String messageCode) {
    this.messageCode = messageCode;
  }

  /**
   * 返回接收人用户 ID。
   *
   * @return 接收人用户 ID
   */
  public Long getReceiverId() {
    return receiverId;
  }

  /**
   * 设置接收人用户 ID。
   *
   * @param receiverId 接收人用户 ID
   */
  public void setReceiverId(Long receiverId) {
    this.receiverId = receiverId;
  }

  /**
   * 返回接收人名称。
   *
   * @return 接收人名称
   */
  public String getReceiverName() {
    return receiverName;
  }

  /**
   * 设置接收人名称。
   *
   * @param receiverName 接收人名称
   */
  public void setReceiverName(String receiverName) {
    this.receiverName = receiverName;
  }

  /**
   * 返回消息类型。
   *
   * @return 消息类型
   */
  public String getMessageType() {
    return messageType;
  }

  /**
   * 设置消息类型。
   *
   * @param messageType 消息类型
   */
  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  /**
   * 返回消息标题。
   *
   * @return 消息标题
   */
  public String getTitle() {
    return title;
  }

  /**
   * 设置消息标题。
   *
   * @param title 消息标题
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * 返回消息内容。
   *
   * @return 消息内容
   */
  public String getContent() {
    return content;
  }

  /**
   * 设置消息内容。
   *
   * @param content 消息内容
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * 返回是否已读。
   *
   * @return 已读返回 {@code true}
   */
  public boolean isReadFlag() {
    return readFlag;
  }

  /**
   * 设置是否已读。
   *
   * @param readFlag 是否已读
   */
  public void setReadFlag(boolean readFlag) {
    this.readFlag = readFlag;
  }

  /**
   * 返回阅读时间。
   *
   * @return 阅读时间
   */
  public LocalDateTime getReadAt() {
    return readAt;
  }

  /**
   * 设置阅读时间。
   *
   * @param readAt 阅读时间
   */
  public void setReadAt(LocalDateTime readAt) {
    this.readAt = readAt;
  }

  /**
   * 返回创建时间。
   *
   * @return 创建时间
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * 设置创建时间。
   *
   * @param createdAt 创建时间
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
