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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MyBatis 低代码页面模型仓储测试。
 */
class MyBatisLowcodePageModelRepositoryTest {

  /**
   * 验证保存新页面模型后返回持久化主键，供后续生成任务引用页面模型。
   */
  @Test
  void shouldReturnPersistedPageModelAfterInsert() {
    FakePageModelMapper mapper = new FakePageModelMapper();
    LowcodePageModelRepository repository = new MyBatisLowcodePageModelRepository(mapper);

    LowcodePageModel saved = repository.save(new LowcodePageModel(
        null, "tenant_a", 100L, "LIST", "/purchase/order",
        "zhyc-base-vue/src/views/purchase/order/index.vue", "TABLE"));

    assertEquals(1, mapper.insertCount);
    assertEquals(0, mapper.updateCount);
    assertEquals(200L, saved.getId());
    assertEquals("TABLE", saved.getLayoutType());
  }

  /**
   * 验证同一租户、表模型和页面类型再次保存时更新原页面模型。
   */
  @Test
  void shouldUpdateExistingPageModelByTenantTableAndType() {
    FakePageModelMapper mapper = new FakePageModelMapper();
    LowcodePageModelRepository repository = new MyBatisLowcodePageModelRepository(mapper);
    repository.save(new LowcodePageModel(
        null, "tenant_a", 100L, "LIST", "/purchase/order",
        "zhyc-base-vue/src/views/purchase/order/index.vue", "TABLE"));

    LowcodePageModel saved = repository.save(new LowcodePageModel(
        null, "tenant_a", 100L, "LIST", "/purchase/order/list",
        "zhyc-base-vue/src/views/purchase/order/list.vue", "TABLE"));

    assertEquals(1, mapper.insertCount);
    assertEquals(1, mapper.updateCount);
    assertEquals(200L, saved.getId());
    assertEquals("/purchase/order/list", saved.getRoutePath());
    assertEquals("zhyc-base-vue/src/views/purchase/order/list.vue", saved.getComponentPath());
  }

  /**
   * 验证按租户查询页面模型时隔离其他租户数据。
   */
  @Test
  void shouldFindPageModelsByTenant() {
    FakePageModelMapper mapper = new FakePageModelMapper();
    LowcodePageModelRepository repository = new MyBatisLowcodePageModelRepository(mapper);
    mapper.records.put("tenant_a:100:LIST", new LowcodePageModelRecord(
        200L, "tenant_a", 100L, "LIST", "/purchase/order",
        "zhyc-base-vue/src/views/purchase/order/index.vue", "TABLE"));
    mapper.records.put("tenant_b:101:LIST", new LowcodePageModelRecord(
        201L, "tenant_b", 101L, "LIST", "/sale/order",
        "zhyc-base-vue/src/views/sale/order/index.vue", "TABLE"));

    List<LowcodePageModel> pageModels = repository.findByTenantId("tenant_a");

    assertEquals(1, pageModels.size());
    assertEquals("LIST", pageModels.get(0).getPageType());
    assertEquals(100L, pageModels.get(0).getTableModelId());
  }

  /**
   * 测试用页面模型 Mapper。
   */
  private static class FakePageModelMapper implements LowcodePageModelMapper {

    /** 页面模型记录集合。 */
    private final Map<String, LowcodePageModelRecord> records = new HashMap<>();
    /** 新增次数。 */
    private int insertCount;
    /** 更新次数。 */
    private int updateCount;

    @Override
    public int insert(LowcodePageModelRecord record) {
      insertCount++;
      records.put(key(record), new LowcodePageModelRecord(
          200L, record.tenantId(), record.tableModelId(), record.pageType(),
          record.routePath(), record.componentPath(), record.layoutType()));
      return 1;
    }

    @Override
    public List<LowcodePageModelRecord> selectByTenantId(String tenantId) {
      return records.values().stream()
          .filter(record -> record.tenantId().equals(tenantId))
          .toList();
    }

    @Override
    public int updateByTenantTableAndType(LowcodePageModelRecord record) {
      LowcodePageModelRecord existing = records.get(key(record));
      if (existing == null) {
        return 0;
      }
      updateCount++;
      records.put(key(record), new LowcodePageModelRecord(
          existing.id(), record.tenantId(), record.tableModelId(), record.pageType(),
          record.routePath(), record.componentPath(), record.layoutType()));
      return 1;
    }

    private String key(LowcodePageModelRecord record) {
      return record.tenantId() + ":" + record.tableModelId() + ":" + record.pageType();
    }
  }
}
