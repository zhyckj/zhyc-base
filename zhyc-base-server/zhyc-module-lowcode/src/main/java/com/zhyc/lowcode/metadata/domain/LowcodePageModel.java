/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.domain;

/**
 * 低代码页面模型。
 */
public class LowcodePageModel {

  /** 数据库主键。 */
  private final Long id;
  /** 租户业务编码。 */
  private final String tenantId;
  /** 表模型主键。 */
  private final Long tableModelId;
  /** 页面类型，取值见 {@link LowcodePageType}，用于区分后台管理端和 uni-app 生成目标。 */
  private final String pageType;
  /** 前端路由路径。 */
  private final String routePath;
  /** 组件路径。 */
  private final String componentPath;
  /** 页面布局类型，取值见 {@link LowcodePageLayoutType}，用于选择低代码页面生成模板。 */
  private final String layoutType;

  /**
   * 创建低代码页面模型。
   *
   * @param id 数据库主键
   * @param tenantId 租户业务编码
   * @param tableModelId 表模型主键
   * @param pageType 页面类型
   * @param routePath 前端路由路径
   * @param componentPath 组件路径
   * @param layoutType 页面布局类型
   */
  public LowcodePageModel(Long id, String tenantId, Long tableModelId, String pageType,
                          String routePath, String componentPath, String layoutType) {
    this.id = id;
    this.tenantId = requireText(tenantId, "租户业务编码不能为空");
    this.tableModelId = requireId(tableModelId, "表模型主键不能为空");
    this.pageType = LowcodePageType.fromCode(requireText(pageType, "页面类型不能为空")).getCode();
    this.routePath = requireText(routePath, "前端路由路径不能为空");
    this.componentPath = requireText(componentPath, "组件路径不能为空");
    this.layoutType = LowcodePageLayoutType.fromCode(requireText(layoutType, "页面布局类型不能为空")).getCode();
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

  private static Long requireId(Long value, String message) {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private static String requireText(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }
}
