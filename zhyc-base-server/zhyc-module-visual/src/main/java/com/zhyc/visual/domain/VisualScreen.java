/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.domain;

import java.time.LocalDateTime;

/**
 * 可视化大屏实体。
 *
 * <p>大屏保存多个报表组件的布局 JSON 和发布状态，首期用于快速配置企业运营看板。</p>
 */
public class VisualScreen {

  /** 主键。 */
  private Long id;
  /** 租户业务编码，用于共享表模式下的数据隔离。 */
  private String tenantId;
  /** 大屏编码，租户内唯一，用于访问和发布。 */
  private String screenCode;
  /** 大屏名称，用于后台管理端展示。 */
  private String screenName;
  /** 布局 JSON，保存组件位置、尺寸和报表编码。 */
  private String layoutJson;
  /** 大屏状态，取值如 draft、published、offline。 */
  private String status;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 更新时间。 */
  private LocalDateTime updatedAt;

  /**
   * 创建可视化大屏实体。
   *
   * @param id 主键
   * @param tenantId 租户业务编码
   * @param screenCode 大屏编码
   * @param screenName 大屏名称
   * @param layoutJson 布局 JSON
   * @param status 大屏状态
   * @param createdAt 创建时间
   * @param updatedAt 更新时间
   */
  public VisualScreen(Long id, String tenantId, String screenCode, String screenName,
      String layoutJson, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.screenCode = screenCode;
    this.screenName = screenName;
    this.layoutJson = layoutJson;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** @return 主键 */
  public Long getId() {
    return id;
  }

  /** @return 租户业务编码 */
  public String getTenantId() {
    return tenantId;
  }

  /** @return 大屏编码 */
  public String getScreenCode() {
    return screenCode;
  }

  /** @return 大屏名称 */
  public String getScreenName() {
    return screenName;
  }

  /** @return 布局 JSON */
  public String getLayoutJson() {
    return layoutJson;
  }

  /** @return 大屏状态 */
  public String getStatus() {
    return status;
  }

  /** @return 创建时间 */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /** @return 更新时间 */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
