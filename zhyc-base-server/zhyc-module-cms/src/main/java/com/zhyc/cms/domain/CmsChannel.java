/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms.domain;

import java.time.LocalDateTime;

/**
 * 内容栏目领域模型。
 */
public class CmsChannel {

  /** 数据库主键。 */
  private Long id;
  /** 租户业务编码。 */
  private String tenantId;
  /** 父栏目主键。 */
  private Long parentId;
  /** 栏目编码。 */
  private String channelCode;
  /** 栏目名称。 */
  private String channelName;
  /** 排序号。 */
  private Integer sortOrder;
  /** 栏目状态。 */
  private String status;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;

  /**
   * 创建空内容栏目对象。
   */
  public CmsChannel() {
  }

  /**
   * 创建完整内容栏目对象。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param parentId 父栏目主键
   * @param channelCode 栏目编码
   * @param channelName 栏目名称
   * @param sortOrder 排序号
   * @param status 栏目状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public CmsChannel(Long id, String tenantId, Long parentId, String channelCode, String channelName,
      Integer sortOrder, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.parentId = parentId;
    this.channelCode = channelCode;
    this.channelName = channelName;
    this.sortOrder = sortOrder;
    this.status = status;
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

  /** @return 父栏目主键 */
  public Long getParentId() {
    return parentId;
  }

  /** @param parentId 父栏目主键 */
  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  /** @return 栏目编码 */
  public String getChannelCode() {
    return channelCode;
  }

  /** @param channelCode 栏目编码 */
  public void setChannelCode(String channelCode) {
    this.channelCode = channelCode;
  }

  /** @return 栏目名称 */
  public String getChannelName() {
    return channelName;
  }

  /** @param channelName 栏目名称 */
  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  /** @return 排序号 */
  public Integer getSortOrder() {
    return sortOrder;
  }

  /** @param sortOrder 排序号 */
  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  /** @return 栏目状态 */
  public String getStatus() {
    return status;
  }

  /** @param status 栏目状态 */
  public void setStatus(String status) {
    this.status = status;
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
