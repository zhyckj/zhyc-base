/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.repository;

import com.zhyc.visual.domain.VisualDataset;
import com.zhyc.visual.domain.VisualReport;
import com.zhyc.visual.domain.VisualScreen;
import java.util.List;
import java.util.Optional;

/**
 * 可视化报表仓储接口。
 */
public interface VisualRepository {

  /**
   * 查询数据集列表。
   *
   * @param tenantId 租户业务编码
   * @param status 数据集状态
   * @return 数据集列表
   */
  List<VisualDataset> findDatasets(String tenantId, String status);

  /**
   * 按租户和编码查询单个数据集。
   *
   * @param tenantId 租户业务编码
   * @param datasetCode 数据集编码
   * @return 数据集实体
   */
  Optional<VisualDataset> findDatasetByCode(String tenantId, String datasetCode);

  /**
   * 保存数据集。
   *
   * @param dataset 数据集实体
   */
  void saveDataset(VisualDataset dataset);

  /**
   * 查询报表列表。
   *
   * @param tenantId 租户业务编码
   * @param status 报表状态
   * @return 报表列表
   */
  List<VisualReport> findReports(String tenantId, String status);

  /**
   * 保存报表。
   *
   * @param report 报表实体
   */
  void saveReport(VisualReport report);

  /**
   * 查询公开访问的已发布报表。
   *
   * @param tenantId 租户业务编码
   * @param reportCode 报表编码
   * @return 已发布报表实体
   */
  Optional<VisualReport> findPublishedReport(String tenantId, String reportCode);

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
  List<VisualScreen> findScreens(String tenantId, String status);

  /**
   * 查询公开访问的已发布大屏。
   *
   * @param tenantId 租户业务编码
   * @param screenCode 大屏编码
   * @return 已发布大屏实体
   */
  Optional<VisualScreen> findPublishedScreen(String tenantId, String screenCode);

  /**
   * 保存大屏。
   *
   * @param screen 大屏实体
   */
  void saveScreen(VisualScreen screen);

  /**
   * 更新大屏状态。
   *
   * @param tenantId 租户业务编码
   * @param id 大屏主键
   * @param status 大屏状态
   */
  void updateScreenStatus(String tenantId, Long id, String status);
}
