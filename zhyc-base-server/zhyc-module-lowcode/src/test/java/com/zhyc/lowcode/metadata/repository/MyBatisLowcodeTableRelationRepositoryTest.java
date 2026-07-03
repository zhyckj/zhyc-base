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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MyBatis 低代码表关系仓储测试。
 */
class MyBatisLowcodeTableRelationRepositoryTest {

  /**
   * 验证保存新表关系后返回持久化主键，供主子表生成链路引用。
   */
  @Test
  void shouldReturnPersistedRelationAfterInsert() {
    FakeRelationMapper mapper = new FakeRelationMapper();
    LowcodeTableRelationRepository repository = new MyBatisLowcodeTableRelationRepository(mapper);

    LowcodeTableRelation saved = repository.save(new LowcodeTableRelation(
        null, "tenant_a", 100L, 101L, "ONE_TO_MANY", "id", "order_id"));

    assertEquals(1, mapper.insertCount);
    assertEquals(0, mapper.updateCount);
    assertEquals(300L, saved.getId());
    assertEquals("order_id", saved.getRefColumn());
  }

  /**
   * 验证同一租户、主表、子表和关系类型再次保存时更新原关系字段。
   */
  @Test
  void shouldUpdateExistingRelationByUniqueKey() {
    FakeRelationMapper mapper = new FakeRelationMapper();
    LowcodeTableRelationRepository repository = new MyBatisLowcodeTableRelationRepository(mapper);
    repository.save(new LowcodeTableRelation(
        null, "tenant_a", 100L, 101L, "ONE_TO_MANY", "id", "order_id"));

    LowcodeTableRelation saved = repository.save(new LowcodeTableRelation(
        null, "tenant_a", 100L, 101L, "ONE_TO_MANY", "order_no", "parent_order_no"));

    assertEquals(1, mapper.insertCount);
    assertEquals(1, mapper.updateCount);
    assertEquals(300L, saved.getId());
    assertEquals("order_no", saved.getJoinColumn());
    assertEquals("parent_order_no", saved.getRefColumn());
  }

  /**
   * 验证按租户查询表关系时隔离其他租户数据。
   */
  @Test
  void shouldFindRelationsByTenant() {
    FakeRelationMapper mapper = new FakeRelationMapper();
    LowcodeTableRelationRepository repository = new MyBatisLowcodeTableRelationRepository(mapper);
    mapper.records.put("tenant_a:100:101:ONE_TO_MANY", new LowcodeTableRelationRecord(
        300L, "tenant_a", 100L, 101L, "ONE_TO_MANY", "id", "order_id"));
    mapper.records.put("tenant_b:200:201:ONE_TO_MANY", new LowcodeTableRelationRecord(
        301L, "tenant_b", 200L, 201L, "ONE_TO_MANY", "id", "order_id"));

    List<LowcodeTableRelation> relations = repository.findByTenantId("tenant_a");

    assertEquals(1, relations.size());
    assertEquals(100L, relations.get(0).getMainTableId());
    assertEquals("ONE_TO_MANY", relations.get(0).getRelationType());
  }

  /**
   * 测试用表关系 Mapper。
   */
  private static class FakeRelationMapper implements LowcodeTableRelationMapper {

    /** 表关系记录集合。 */
    private final Map<String, LowcodeTableRelationRecord> records = new HashMap<>();
    /** 新增次数。 */
    private int insertCount;
    /** 更新次数。 */
    private int updateCount;

    @Override
    public int insert(LowcodeTableRelationRecord record) {
      insertCount++;
      records.put(key(record), new LowcodeTableRelationRecord(
          300L, record.tenantId(), record.mainTableId(), record.subTableId(), record.relationType(),
          record.joinColumn(), record.refColumn()));
      return 1;
    }

    @Override
    public List<LowcodeTableRelationRecord> selectByTenantId(String tenantId) {
      return records.values().stream()
          .filter(record -> record.tenantId().equals(tenantId))
          .toList();
    }

    @Override
    public int updateByTenantAndTables(LowcodeTableRelationRecord record) {
      LowcodeTableRelationRecord existing = records.get(key(record));
      if (existing == null) {
        return 0;
      }
      updateCount++;
      records.put(key(record), new LowcodeTableRelationRecord(
          existing.id(), record.tenantId(), record.mainTableId(), record.subTableId(), record.relationType(),
          record.joinColumn(), record.refColumn()));
      return 1;
    }

    private String key(LowcodeTableRelationRecord record) {
      return record.tenantId() + ":" + record.mainTableId() + ":" + record.subTableId() + ":"
          + record.relationType();
    }
  }
}
