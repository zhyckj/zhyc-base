/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.mapper;

import com.zhyc.visual.domain.VisualDataset;
import com.zhyc.visual.domain.VisualReport;
import com.zhyc.visual.domain.VisualScreen;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 可视化报表 MyBatis Mapper。
 */
@Mapper
public interface VisualMapper {

  /**
   * 查询数据集列表。
   *
   * @param tenantId 租户业务编码
   * @param status 数据集状态
   * @return 数据集列表
   */
  @SelectProvider(type = VisualSqlProvider.class, method = "selectDatasetsForMapper")
  List<VisualDataset> selectDatasets(@Param("tenantId") String tenantId, @Param("status") String status);

  /**
   * 按租户和编码查询单个数据集。
   *
   * @param tenantId 租户业务编码
   * @param datasetCode 数据集编码
   * @return 数据集实体
   */
  @SelectProvider(type = VisualSqlProvider.class, method = "selectDatasetByCode")
  VisualDataset selectDatasetByCode(@Param("tenantId") String tenantId,
      @Param("datasetCode") String datasetCode);

  /**
   * 保存或更新数据集。
   *
   * @param dataset 数据集实体
   */
  @InsertProvider(type = VisualSqlProvider.class, method = "upsertDataset")
  void upsertDataset(VisualDataset dataset);

  /**
   * 查询报表列表。
   *
   * @param tenantId 租户业务编码
   * @param status 报表状态
   * @return 报表列表
   */
  @SelectProvider(type = VisualSqlProvider.class, method = "selectReportsForMapper")
  List<VisualReport> selectReports(@Param("tenantId") String tenantId, @Param("status") String status);

  /**
   * 保存或更新报表。
   *
   * @param report 报表实体
   */
  @InsertProvider(type = VisualSqlProvider.class, method = "upsertReport")
  void upsertReport(VisualReport report);

  /**
   * 查询公开访问的已发布报表。
   *
   * @param tenantId 租户业务编码
   * @param reportCode 报表编码
   * @return 已发布报表实体
   */
  @SelectProvider(type = VisualSqlProvider.class, method = "selectPublishedReport")
  VisualReport selectPublishedReport(@Param("tenantId") String tenantId,
      @Param("reportCode") String reportCode);

  /**
   * 更新报表状态。
   *
   * @param tenantId 租户业务编码
   * @param id 报表主键
   * @param status 报表状态
   */
  @UpdateProvider(type = VisualSqlProvider.class, method = "updateReportStatus")
  void updateReportStatus(@Param("tenantId") String tenantId, @Param("id") Long id,
      @Param("status") String status);

  /**
   * 查询大屏列表。
   *
   * @param tenantId 租户业务编码
   * @param status 大屏状态
   * @return 大屏列表
   */
  @SelectProvider(type = VisualSqlProvider.class, method = "selectScreensForMapper")
  List<VisualScreen> selectScreens(@Param("tenantId") String tenantId, @Param("status") String status);

  /**
   * 查询公开访问的已发布大屏。
   *
   * @param tenantId 租户业务编码
   * @param screenCode 大屏编码
   * @return 已发布大屏实体
   */
  @SelectProvider(type = VisualSqlProvider.class, method = "selectPublishedScreen")
  VisualScreen selectPublishedScreen(@Param("tenantId") String tenantId,
      @Param("screenCode") String screenCode);

  /**
   * 保存或更新大屏。
   *
   * @param screen 大屏实体
   */
  @InsertProvider(type = VisualSqlProvider.class, method = "upsertScreen")
  void upsertScreen(VisualScreen screen);

  /**
   * 更新大屏状态。
   *
   * @param tenantId 租户业务编码
   * @param id 大屏主键
   * @param status 大屏状态
   */
  @UpdateProvider(type = VisualSqlProvider.class, method = "updateScreenStatus")
  void updateScreenStatus(@Param("tenantId") String tenantId, @Param("id") Long id,
      @Param("status") String status);
}
