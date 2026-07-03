/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.service;

import java.util.List;

/**
 * 可视化报表业务服务。
 */
public interface VisualService {

  /**
   * 查询数据集列表。
   *
   * @param tenantId 租户业务编码
   * @param status 数据集状态
   * @return 数据集列表
   */
  List<VisualDatasetResponse> listDatasets(String tenantId, String status);

  /**
   * 保存数据集。
   *
   * @param command 数据集保存命令
   */
  void saveDataset(VisualDatasetSaveCommand command);

  /**
   * 预览数据集字段和样例数据。
   *
   * @param tenantId 租户业务编码
   * @param datasetCode 数据集编码
   * @param limit 预览行数上限
   * @return 数据集预览响应
   */
  VisualDatasetPreviewResponse previewDataset(String tenantId, String datasetCode, Integer limit);

  /**
   * 预览公开大屏引用的数据集字段和样例数据。
   *
   * @param tenantId 租户业务编码
   * @param screenCode 大屏编码
   * @param datasetCode 数据集编码
   * @param limit 预览行数上限
   * @return 数据集预览响应
   */
  VisualDatasetPreviewResponse previewPublishedScreenDataset(String tenantId, String screenCode,
      String datasetCode, Integer limit);

  /**
   * 预览公开报表引用的数据集字段和样例数据。
   *
   * @param tenantId 租户业务编码
   * @param reportCode 报表编码
   * @param datasetCode 数据集编码
   * @param limit 预览行数上限
   * @return 数据集预览响应
   */
  VisualDatasetPreviewResponse previewPublishedReportDataset(String tenantId, String reportCode,
      String datasetCode, Integer limit);

  /**
   * 查询报表列表。
   *
   * @param tenantId 租户业务编码
   * @param status 报表状态
   * @return 报表列表
   */
  List<VisualReportResponse> listReports(String tenantId, String status);

  /**
   * 保存报表。
   *
   * @param command 报表保存命令
   */
  void saveReport(VisualReportSaveCommand command);

  /**
   * 查询公开访问的已发布报表。
   *
   * @param tenantId 租户业务编码
   * @param reportCode 报表编码
   * @return 已发布报表
   */
  VisualReportResponse getPublishedReport(String tenantId, String reportCode);

  /**
   * 更新报表状态。
   *
   * @param tenantId 租户业务编码
   * @param id 报表主键
   * @param status 报表状态
   */
  void updateReportStatus(String tenantId, Long id, String status);

  /**
   * 查询大屏列表。
   *
   * @param tenantId 租户业务编码
   * @param status 大屏状态
   * @return 大屏列表
   */
  List<VisualScreenResponse> listScreens(String tenantId, String status);

  /**
   * 查询公开访问的已发布大屏。
   *
   * @param tenantId 租户业务编码
   * @param screenCode 大屏编码
   * @return 已发布大屏
   */
  VisualScreenResponse getPublishedScreen(String tenantId, String screenCode);

  /**
   * 保存大屏。
   *
   * @param command 大屏保存命令
   */
  void saveScreen(VisualScreenSaveCommand command);

  /**
   * 更新大屏状态。
   *
   * @param tenantId 租户业务编码
   * @param id 大屏主键
   * @param status 大屏状态
   */
  void updateScreenStatus(String tenantId, Long id, String status);
}
