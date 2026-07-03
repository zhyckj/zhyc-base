/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.repository;

import com.zhyc.lowcode.metadata.domain.LowcodePageModel;
import com.zhyc.lowcode.metadata.mybatis.LowcodePageModelMapper;
import com.zhyc.lowcode.metadata.mybatis.LowcodePageModelRecord;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基于 MyBatis 的低代码页面模型仓储。
 */
@Repository
public class MyBatisLowcodePageModelRepository implements LowcodePageModelRepository {

  /** 页面模型 Mapper。 */
  private final LowcodePageModelMapper mapper;

  /**
   * 创建页面模型仓储。
   *
   * @param mapper 页面模型 Mapper
   */
  public MyBatisLowcodePageModelRepository(LowcodePageModelMapper mapper) {
    this.mapper = Objects.requireNonNull(mapper, "页面模型 Mapper 不能为空");
  }

  @Override
  @Transactional
  public LowcodePageModel save(LowcodePageModel pageModel) {
    Objects.requireNonNull(pageModel, "页面模型不能为空");
    LowcodePageModelRecord record = toRecord(pageModel);
    if (mapper.updateByTenantTableAndType(record) == 0) {
      mapper.insert(record);
    }
    return findPersistedPageModel(pageModel);
  }

  @Override
  public List<LowcodePageModel> findByTenantId(String tenantId) {
    return mapper.selectByTenantId(tenantId).stream()
        .map(this::toDomain)
        .toList();
  }

  private LowcodePageModelRecord toRecord(LowcodePageModel pageModel) {
    return new LowcodePageModelRecord(
        pageModel.getId(),
        pageModel.getTenantId(),
        pageModel.getTableModelId(),
        pageModel.getPageType(),
        pageModel.getRoutePath(),
        pageModel.getComponentPath(),
        pageModel.getLayoutType());
  }

  private LowcodePageModel findPersistedPageModel(LowcodePageModel pageModel) {
    return mapper.selectByTenantId(pageModel.getTenantId()).stream()
        .filter(record -> record.tableModelId().equals(pageModel.getTableModelId()))
        .filter(record -> record.pageType().equals(pageModel.getPageType()))
        .findFirst()
        .map(this::toDomain)
        .orElseThrow(() -> new IllegalStateException(
            "保存页面模型后无法获取页面模型主键: " + pageModel.getTableModelId() + ":" + pageModel.getPageType()));
  }

  private LowcodePageModel toDomain(LowcodePageModelRecord record) {
    return new LowcodePageModel(
        record.id(),
        record.tenantId(),
        record.tableModelId(),
        record.pageType(),
        record.routePath(),
        record.componentPath(),
        record.layoutType());
  }
}
