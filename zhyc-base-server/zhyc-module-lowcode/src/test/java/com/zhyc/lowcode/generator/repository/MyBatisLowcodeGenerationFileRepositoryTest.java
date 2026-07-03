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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MyBatis 低代码生成文件明细仓储测试。
 */
class MyBatisLowcodeGenerationFileRepositoryTest {

  /**
   * 验证生成文件明细保存时会保留路径、覆盖模式和内容哈希。
   */
  @Test
  void shouldSaveGenerationFileDetail() {
    FakeGenerationFileMapper mapper = new FakeGenerationFileMapper();
    LowcodeGenerationFileRepository repository = new MyBatisLowcodeGenerationFileRepository(mapper);

    repository.saveAll(List.of(new LowcodeGenerationFile(
        null,
        "tenant_a",
        101L,
        "admin-frontend-list",
        "zhyc-base-vue/src/views/purchase/order/index.vue",
        "vue",
        GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS,
        "abc123")));

    assertEquals(1, mapper.records.size());
    assertEquals("tenant_a", mapper.records.get(0).tenantId());
    assertEquals("abc123", mapper.records.get(0).contentHash());
  }

  /**
   * 验证按租户和生成记录查询文件明细，避免跨租户泄露生成路径和哈希。
   */
  @Test
  void shouldFindGenerationFilesByTenantAndRecordId() {
    FakeGenerationFileMapper mapper = new FakeGenerationFileMapper();
    mapper.records.add(new LowcodeGenerationFileRecord(
        1L, "tenant_a", 101L, "uniapp-list", "a.vue", "vue", "FAIL_IF_EXISTS", "hash-a"));
    mapper.records.add(new LowcodeGenerationFileRecord(
        2L, "tenant_b", 101L, "uniapp-list", "b.vue", "vue", "FAIL_IF_EXISTS", "hash-b"));
    LowcodeGenerationFileRepository repository = new MyBatisLowcodeGenerationFileRepository(mapper);

    List<LowcodeGenerationFile> files = repository.findByTenantIdAndRecordId("tenant_a", 101L);

    assertEquals(1, files.size());
    assertEquals("hash-a", files.get(0).getContentHash());
  }

  /**
   * 测试用生成文件明细 Mapper。
   */
  private static class FakeGenerationFileMapper implements LowcodeGenerationFileMapper {

    /** 已保存生成文件明细。 */
    private final List<LowcodeGenerationFileRecord> records = new ArrayList<>();

    @Override
    public int insert(LowcodeGenerationFileRecord record) {
      records.add(record);
      return 1;
    }

    @Override
    public List<LowcodeGenerationFileRecord> selectByTenantIdAndRecordId(String tenantId, Long recordId) {
      return records.stream()
          .filter(record -> record.tenantId().equals(tenantId))
          .filter(record -> record.recordId().equals(recordId))
          .toList();
    }
  }
}
