/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator.repository;

import com.zhyc.lowcode.generator.GeneratedFileOverwriteStrategy;
import com.zhyc.lowcode.generator.GenerationTarget;
import com.zhyc.lowcode.generator.LowcodeGenerationRecord;
import com.zhyc.lowcode.generator.LowcodeGenerationRecordStatus;
import com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordMapper;
import com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的低代码生成记录仓储实现。
 */
@Repository
public class MyBatisLowcodeGenerationRecordRepository implements LowcodeGenerationRecordRepository {

  /** 低代码生成记录 Mapper。 */
  private final LowcodeGenerationRecordMapper recordMapper;

  /**
   * 创建低代码生成记录仓储。
   *
   * @param recordMapper 低代码生成记录 Mapper
   */
  public MyBatisLowcodeGenerationRecordRepository(LowcodeGenerationRecordMapper recordMapper) {
    this.recordMapper = Objects.requireNonNull(recordMapper, "低代码生成记录 Mapper 不能为空");
  }

  /**
   * 保存低代码生成记录。
   *
   * @param record 低代码生成记录，包含租户、表模型、目标端、覆盖策略和生成状态
   * @return 保存后的低代码生成记录
   */
  @Override
  public LowcodeGenerationRecord save(LowcodeGenerationRecord record) {
    Objects.requireNonNull(record, "低代码生成记录不能为空");
    recordMapper.insert(toRecord(record));
    return findByTenantId(record.getTenantId()).stream()
        .filter(saved -> saved.getTableModelCode().equals(record.getTableModelCode()))
        .findFirst()
        .orElse(record);
  }

  /**
   * 查询租户下的低代码生成记录。
   *
   * @param tenantId 租户业务编码
   * @return 租户生成记录列表
   */
  @Override
  public List<LowcodeGenerationRecord> findByTenantId(String tenantId) {
    return recordMapper.selectByTenantId(tenantId).stream()
        .map(this::toDomain)
        .toList();
  }

  private LowcodeGenerationRecordRecord toRecord(LowcodeGenerationRecord record) {
    return new LowcodeGenerationRecordRecord(
        record.getId(),
        record.getTenantId(),
        record.getTableModelCode(),
        record.getTarget().getCode(),
        record.getModuleName(),
        record.getEntityName(),
        record.getOverwriteStrategy().name(),
        record.getFileCount(),
        record.getFileManifestJson(),
        record.getStatus().name(),
        record.getErrorMessage());
  }

  private LowcodeGenerationRecord toDomain(LowcodeGenerationRecordRecord record) {
    return LowcodeGenerationRecord.restore(
        record.id(),
        record.tenantId(),
        record.tableModelCode(),
        GenerationTarget.fromCode(record.target()),
        record.moduleName(),
        record.entityName(),
        GeneratedFileOverwriteStrategy.valueOf(record.overwriteStrategy()),
        record.fileCount(),
        record.fileManifestJson(),
        LowcodeGenerationRecordStatus.valueOf(record.status()),
        record.errorMessage());
  }
}
