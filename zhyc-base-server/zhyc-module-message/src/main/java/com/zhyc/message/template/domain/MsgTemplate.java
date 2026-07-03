/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template.domain;

import java.time.LocalDateTime;

/**
 * 消息模板领域模型。
 *
 * <p>消息模板按租户隔离，用于站内信、推送、短信、邮件等通道复用统一内容模板。</p>
 */
public class MsgTemplate {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 模板编码，租户内唯一。 */
  private String templateCode;
  /** 模板名称。 */
  private String templateName;
  /** 消息通道类型。 */
  private String channelType;
  /** 标题模板。 */
  private String titleTemplate;
  /** 内容模板。 */
  private String contentTemplate;
  /** 模板状态。 */
  private String status;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;

  /**
   * 创建空消息模板对象。
   */
  public MsgTemplate() {
  }

  /**
   * 创建完整消息模板对象。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param templateCode 模板编码
   * @param templateName 模板名称
   * @param channelType 消息通道类型
   * @param titleTemplate 标题模板
   * @param contentTemplate 内容模板
   * @param status 模板状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public MsgTemplate(Long id, String tenantId, String templateCode, String templateName,
      String channelType, String titleTemplate, String contentTemplate, String status,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.templateCode = templateCode;
    this.templateName = templateName;
    this.channelType = channelType;
    this.titleTemplate = titleTemplate;
    this.contentTemplate = contentTemplate;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
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
   * 返回模板编码。
   *
   * @return 模板编码
   */
  public String getTemplateCode() {
    return templateCode;
  }

  /**
   * 设置模板编码。
   *
   * @param templateCode 模板编码
   */
  public void setTemplateCode(String templateCode) {
    this.templateCode = templateCode;
  }

  /**
   * 返回模板名称。
   *
   * @return 模板名称
   */
  public String getTemplateName() {
    return templateName;
  }

  /**
   * 设置模板名称。
   *
   * @param templateName 模板名称
   */
  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  /**
   * 返回消息通道类型。
   *
   * @return 消息通道类型
   */
  public String getChannelType() {
    return channelType;
  }

  /**
   * 设置消息通道类型。
   *
   * @param channelType 消息通道类型
   */
  public void setChannelType(String channelType) {
    this.channelType = channelType;
  }

  /**
   * 返回标题模板。
   *
   * @return 标题模板
   */
  public String getTitleTemplate() {
    return titleTemplate;
  }

  /**
   * 设置标题模板。
   *
   * @param titleTemplate 标题模板
   */
  public void setTitleTemplate(String titleTemplate) {
    this.titleTemplate = titleTemplate;
  }

  /**
   * 返回内容模板。
   *
   * @return 内容模板
   */
  public String getContentTemplate() {
    return contentTemplate;
  }

  /**
   * 设置内容模板。
   *
   * @param contentTemplate 内容模板
   */
  public void setContentTemplate(String contentTemplate) {
    this.contentTemplate = contentTemplate;
  }

  /**
   * 返回模板状态。
   *
   * @return 模板状态
   */
  public String getStatus() {
    return status;
  }

  /**
   * 设置模板状态。
   *
   * @param status 模板状态
   */
  public void setStatus(String status) {
    this.status = status;
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

  /**
   * 返回更新时间。
   *
   * @return 更新时间
   */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /**
   * 设置更新时间。
   *
   * @param updatedAt 更新时间
   */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
