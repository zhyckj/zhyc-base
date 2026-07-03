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
import com.zhyc.visual.mapper.VisualMapper;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 基于 MyBatis 的可视化报表仓储实现。
 */
@Repository
public class MyBatisVisualRepository implements VisualRepository {

  /** 可视化报表 Mapper。 */
  private final VisualMapper visualMapper;

  /**
   * 创建可视化报表仓储。
   *
   * @param visualMapper 可视化报表 Mapper
   */
  public MyBatisVisualRepository(VisualMapper visualMapper) {
    this.visualMapper = Objects.requireNonNull(visualMapper, "可视化报表 Mapper 不能为空");
  }

  @Override
  public List<VisualDataset> findDatasets(String tenantId, String status) {
    return visualMapper.selectDatasets(tenantId, status);
  }

  @Override
  public Optional<VisualDataset> findDatasetByCode(String tenantId, String datasetCode) {
    return Optional.ofNullable(visualMapper.selectDatasetByCode(tenantId, datasetCode));
  }

  @Override
  public void saveDataset(VisualDataset dataset) {
    visualMapper.upsertDataset(dataset);
  }

  @Override
  public List<VisualReport> findReports(String tenantId, String status) {
    return visualMapper.selectReports(tenantId, status);
  }

  @Override
  public void saveReport(VisualReport report) {
    visualMapper.upsertReport(report);
  }

  @Override
  public Optional<VisualReport> findPublishedReport(String tenantId, String reportCode) {
    return Optional.ofNullable(visualMapper.selectPublishedReport(tenantId, reportCode));
  }

  @Override
  public void updateReportStatus(String tenantId, Long id, String status) {
    visualMapper.updateReportStatus(tenantId, id, status);
  }

  @Override
  public List<VisualScreen> findScreens(String tenantId, String status) {
    return visualMapper.selectScreens(tenantId, status);
  }

  @Override
  public Optional<VisualScreen> findPublishedScreen(String tenantId, String screenCode) {
    return Optional.ofNullable(visualMapper.selectPublishedScreen(tenantId, screenCode));
  }

  @Override
  public void saveScreen(VisualScreen screen) {
    visualMapper.upsertScreen(screen);
  }

  @Override
  public void updateScreenStatus(String tenantId, Long id, String status) {
    visualMapper.updateScreenStatus(tenantId, id, status);
  }
}
