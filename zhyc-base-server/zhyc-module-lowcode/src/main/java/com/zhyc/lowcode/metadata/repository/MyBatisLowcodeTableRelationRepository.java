/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.repository;

import com.zhyc.lowcode.metadata.domain.LowcodeTableRelation;
import com.zhyc.lowcode.metadata.mybatis.LowcodeTableRelationMapper;
import com.zhyc.lowcode.metadata.mybatis.LowcodeTableRelationRecord;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基于 MyBatis 的低代码表关系仓储。
 */
@Repository
public class MyBatisLowcodeTableRelationRepository implements LowcodeTableRelationRepository {

  /** 表关系 Mapper。 */
  private final LowcodeTableRelationMapper mapper;

  /**
   * 创建表关系仓储。
   *
   * @param mapper 表关系 Mapper
   */
  public MyBatisLowcodeTableRelationRepository(LowcodeTableRelationMapper mapper) {
    this.mapper = Objects.requireNonNull(mapper, "表关系 Mapper 不能为空");
  }

  @Override
  @Transactional
  public LowcodeTableRelation save(LowcodeTableRelation relation) {
    Objects.requireNonNull(relation, "表关系不能为空");
    LowcodeTableRelationRecord record = toRecord(relation);
    if (mapper.updateByTenantAndTables(record) == 0) {
      mapper.insert(record);
    }
    return findPersistedRelation(relation);
  }

  @Override
  public List<LowcodeTableRelation> findByTenantId(String tenantId) {
    return mapper.selectByTenantId(tenantId).stream()
        .map(this::toDomain)
        .toList();
  }

  private LowcodeTableRelationRecord toRecord(LowcodeTableRelation relation) {
    return new LowcodeTableRelationRecord(
        relation.getId(),
        relation.getTenantId(),
        relation.getMainTableId(),
        relation.getSubTableId(),
        relation.getRelationType(),
        relation.getJoinColumn(),
        relation.getRefColumn());
  }

  private LowcodeTableRelation findPersistedRelation(LowcodeTableRelation relation) {
    return mapper.selectByTenantId(relation.getTenantId()).stream()
        .filter(record -> record.mainTableId().equals(relation.getMainTableId()))
        .filter(record -> record.subTableId().equals(relation.getSubTableId()))
        .filter(record -> record.relationType().equals(relation.getRelationType()))
        .findFirst()
        .map(this::toDomain)
        .orElseThrow(() -> new IllegalStateException(
            "保存表关系后无法获取表关系主键: " + relation.getMainTableId() + ":" + relation.getSubTableId()));
  }

  private LowcodeTableRelation toDomain(LowcodeTableRelationRecord record) {
    return new LowcodeTableRelation(
        record.id(),
        record.tenantId(),
        record.mainTableId(),
        record.subTableId(),
        record.relationType(),
        record.joinColumn(),
        record.refColumn());
  }
}
