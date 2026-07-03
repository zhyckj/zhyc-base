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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MyBatis 低代码生成记录仓储测试。
 */
class MyBatisLowcodeGenerationRecordRepositoryTest {

  /**
   * 验证生成记录保存后会转换为领域对象返回。
   */
  @Test
  void shouldSaveGenerationRecord() {
    FakeGenerationRecordMapper mapper = new FakeGenerationRecordMapper();
    LowcodeGenerationRecordRepository repository = new MyBatisLowcodeGenerationRecordRepository(mapper);
    LowcodeGenerationRecord record = LowcodeGenerationRecord.success(
        "tenant_a", "purchase_order", GenerationTarget.ADMIN_BACKEND, "purchase", "purchaseOrder",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS, 2, "[{\"targetPath\":\"src/a.java\"}]");

    LowcodeGenerationRecord saved = repository.save(record);

    assertEquals(101L, saved.getId());
    assertEquals("tenant_a", saved.getTenantId());
    assertEquals("admin-backend", mapper.records.get(0).target());
    assertEquals("[{\"targetPath\":\"src/a.java\"}]", saved.getFileManifestJson());
    assertEquals(LowcodeGenerationRecordStatus.SUCCESS, saved.getStatus());
  }

  /**
   * 验证仓储按租户查询生成记录列表，避免跨租户展示生成审计数据。
   */
  @Test
  void shouldListGenerationRecordsByTenant() {
    FakeGenerationRecordMapper mapper = new FakeGenerationRecordMapper();
    mapper.records.add(new LowcodeGenerationRecordRecord(
        1L, "tenant_a", "purchase_order", "ADMIN_BACKEND", "purchase", "purchaseOrder",
        "FAIL_IF_EXISTS", 2, "[{\"targetPath\":\"src/a.java\"}]", "SUCCESS", ""));
    mapper.records.add(new LowcodeGenerationRecordRecord(
        2L, "tenant_b", "supplier", "ADMIN_FRONTEND", "purchase", "supplier",
        "OVERWRITE", 1, "[]", "FAILED", "失败"));
    LowcodeGenerationRecordRepository repository = new MyBatisLowcodeGenerationRecordRepository(mapper);

    List<LowcodeGenerationRecord> records = repository.findByTenantId("tenant_a");

    assertEquals(1, records.size());
    assertEquals("purchase_order", records.get(0).getTableModelCode());
  }

  /**
   * 验证仓储读取旧枚举名生成记录，兼容首期历史数据。
   */
  @Test
  void shouldRestoreLegacyEnumNameTargetCode() {
    FakeGenerationRecordMapper mapper = new FakeGenerationRecordMapper();
    mapper.records.add(new LowcodeGenerationRecordRecord(
        1L, "tenant_a", "purchase_order", "ADMIN_BACKEND", "purchase", "purchaseOrder",
        "FAIL_IF_EXISTS", 2, "[{\"targetPath\":\"src/a.java\"}]", "SUCCESS", ""));
    LowcodeGenerationRecordRepository repository = new MyBatisLowcodeGenerationRecordRepository(mapper);

    List<LowcodeGenerationRecord> records = repository.findByTenantId("tenant_a");

    assertEquals(GenerationTarget.ADMIN_BACKEND, records.get(0).getTarget());
  }

  /**
   * 测试用生成记录 Mapper。
   */
  private static class FakeGenerationRecordMapper implements LowcodeGenerationRecordMapper {

    /** 已保存生成记录。 */
    private final List<LowcodeGenerationRecordRecord> records = new ArrayList<>();

    @Override
    public int insert(LowcodeGenerationRecordRecord record) {
      records.add(new LowcodeGenerationRecordRecord(
          101L,
          record.tenantId(),
          record.tableModelCode(),
          record.target(),
          record.moduleName(),
          record.entityName(),
          record.overwriteStrategy(),
          record.fileCount(),
          record.fileManifestJson(),
          record.status(),
          record.errorMessage()));
      return 1;
    }

    @Override
    public List<LowcodeGenerationRecordRecord> selectByTenantId(String tenantId) {
      return records.stream()
          .filter(record -> record.tenantId().equals(tenantId))
          .toList();
    }
  }
}
