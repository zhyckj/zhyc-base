/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodePageModel;

/**
 * 低代码页面模型响应对象。
 */
public class LowcodePageModelResponse {

  /** 页面模型主键。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 表模型主键。 */
  private final Long tableModelId;
  /** 页面类型。 */
  private final String pageType;
  /** 前端路由路径。 */
  private final String routePath;
  /** 组件路径。 */
  private final String componentPath;
  /** 页面布局类型。 */
  private final String layoutType;

  private LowcodePageModelResponse(LowcodePageModel pageModel) {
    this.id = pageModel.getId();
    this.tenantId = pageModel.getTenantId();
    this.tableModelId = pageModel.getTableModelId();
    this.pageType = pageModel.getPageType();
    this.routePath = pageModel.getRoutePath();
    this.componentPath = pageModel.getComponentPath();
    this.layoutType = pageModel.getLayoutType();
  }

  /**
   * 从领域对象构造响应。
   *
   * @param pageModel 页面模型领域对象
   * @return 页面模型响应
   */
  public static LowcodePageModelResponse from(LowcodePageModel pageModel) {
    return new LowcodePageModelResponse(pageModel);
  }

  public Long getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Long getTableModelId() {
    return tableModelId;
  }

  public String getPageType() {
    return pageType;
  }

  public String getRoutePath() {
    return routePath;
  }

  public String getComponentPath() {
    return componentPath;
  }

  public String getLayoutType() {
    return layoutType;
  }
}
