/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.domain;

import java.time.LocalDateTime;

/**
 * 内容文章领域模型。
 */
public class CmsContent {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 栏目编码。 */
  private String channelCode;
  /** 文章标题。 */
  private String title;
  /** 文章摘要。 */
  private String summary;
  /** 文章正文。 */
  private String bodyContent;
  /** 文章状态。 */
  private String status;
  /** 作者用户主键。 */
  private Long authorId;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;

  /**
   * 创建空内容文章对象。
   */
  public CmsContent() {
  }

  /**
   * 创建完整内容文章对象。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param channelCode 栏目编码
   * @param title 文章标题
   * @param summary 文章摘要
   * @param bodyContent 文章正文
   * @param status 文章状态
   * @param authorId 作者用户主键
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public CmsContent(Long id, String tenantId, String channelCode, String title, String summary,
      String bodyContent, String status, Long authorId, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.channelCode = channelCode;
    this.title = title;
    this.summary = summary;
    this.bodyContent = bodyContent;
    this.status = status;
    this.authorId = authorId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** @return 数据库主键 */
  public Long getId() {
    return id;
  }

  /** @param id 数据库主键 */
  public void setId(Long id) {
    this.id = id;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @param tenantId 租户业务编码 */
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  /** @return 栏目编码 */
  public String getChannelCode() {
    return channelCode;
  }

  /** @param channelCode 栏目编码 */
  public void setChannelCode(String channelCode) {
    this.channelCode = channelCode;
  }

  /** @return 文章标题 */
  public String getTitle() {
    return title;
  }

  /** @param title 文章标题 */
  public void setTitle(String title) {
    this.title = title;
  }

  /** @return 文章摘要 */
  public String getSummary() {
    return summary;
  }

  /** @param summary 文章摘要 */
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /** @return 文章正文 */
  public String getBodyContent() {
    return bodyContent;
  }

  /** @param bodyContent 文章正文 */
  public void setBodyContent(String bodyContent) {
    this.bodyContent = bodyContent;
  }

  /** @return 文章状态 */
  public String getStatus() {
    return status;
  }

  /** @param status 文章状态 */
  public void setStatus(String status) {
    this.status = status;
  }

  /** @return 作者用户主键 */
  public Long getAuthorId() {
    return authorId;
  }

  /** @param authorId 作者用户主键 */
  public void setAuthorId(Long authorId) {
    this.authorId = authorId;
  }

  /** @return 创建时间 */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /** @param createdAt 创建时间 */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /** @return 更新时间 */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /** @param updatedAt 更新时间 */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
