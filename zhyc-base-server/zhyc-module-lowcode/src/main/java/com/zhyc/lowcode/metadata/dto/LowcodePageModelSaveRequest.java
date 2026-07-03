/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodePageModel;

/**
 * 低代码页面模型保存请求。
 *
 * @param tenantId 租户业务编码
 * @param tableModelId 表模型主键
 * @param pageType 页面类型
 * @param routePath 前端路由路径
 * @param componentPath 组件路径
 * @param layoutType 页面布局类型
 */
public record LowcodePageModelSaveRequest(
    String tenantId,
    Long tableModelId,
    String pageType,
    String routePath,
    String componentPath,
    String layoutType) {

  /**
   * 转换为页面模型领域对象。
   *
   * @return 页面模型领域对象
   */
  public LowcodePageModel toDomain() {
    return new LowcodePageModel(null, tenantId, tableModelId, pageType, routePath, componentPath, layoutType);
  }
}
