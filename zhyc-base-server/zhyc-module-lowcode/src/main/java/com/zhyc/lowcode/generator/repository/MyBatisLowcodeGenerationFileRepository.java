/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator.repository;

import com.zhyc.lowcode.generator.GeneratedFileOverwriteStrategy;
import com.zhyc.lowcode.generator.LowcodeGenerationFile;
import com.zhyc.lowcode.generator.mybatis.LowcodeGenerationFileMapper;
import com.zhyc.lowcode.generator.mybatis.LowcodeGenerationFileRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的低代码生成文件明细仓储实现。
 */
@Repository
public class MyBatisLowcodeGenerationFileRepository implements LowcodeGenerationFileRepository {

  /** 低代码生成文件明细 Mapper。 */
  private final LowcodeGenerationFileMapper generationFileMapper;

  /**
   * 创建低代码生成文件明细仓储。
   *
   * @param generationFileMapper 低代码生成文件明细 Mapper
   */
  public MyBatisLowcodeGenerationFileRepository(LowcodeGenerationFileMapper generationFileMapper) {
    this.generationFileMapper = Objects.requireNonNull(generationFileMapper, "生成文件明细 Mapper 不能为空");
  }

  /**
   * 批量保存生成文件明细。
   *
   * @param files 生成文件明细列表，包含租户、生成记录、模板编码、路径、覆盖模式和内容哈希
   */
  @Override
  public void saveAll(List<LowcodeGenerationFile> files) {
    Objects.requireNonNull(files, "生成文件明细列表不能为空");
    files.stream()
        .map(this::toRecord)
        .forEach(generationFileMapper::insert);
  }

  /**
   * 按租户和生成记录查询文件明细。
   *
   * @param tenantId 租户业务编码
   * @param recordId 生成记录主键
   * @return 生成文件明细列表
   */
  @Override
  public List<LowcodeGenerationFile> findByTenantIdAndRecordId(String tenantId, Long recordId) {
    return generationFileMapper.selectByTenantIdAndRecordId(tenantId, recordId).stream()
        .map(this::toDomain)
        .toList();
  }

  private LowcodeGenerationFileRecord toRecord(LowcodeGenerationFile file) {
    return new LowcodeGenerationFileRecord(
        file.getId(),
        file.getTenantId(),
        file.getRecordId(),
        file.getTemplateCode(),
        file.getFilePath(),
        file.getFileType(),
        file.getOverwriteMode().name(),
        file.getContentHash());
  }

  private LowcodeGenerationFile toDomain(LowcodeGenerationFileRecord record) {
    return new LowcodeGenerationFile(
        record.id(),
        record.tenantId(),
        record.recordId(),
        record.templateCode(),
        record.filePath(),
        record.fileType(),
        GeneratedFileOverwriteStrategy.valueOf(record.overwriteMode()),
        record.contentHash());
  }
}
