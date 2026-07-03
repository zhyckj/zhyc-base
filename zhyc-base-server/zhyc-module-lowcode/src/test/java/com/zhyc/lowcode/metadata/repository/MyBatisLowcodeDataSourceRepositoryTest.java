/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.repository;

import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;
import com.zhyc.lowcode.metadata.mybatis.LowcodeDataSourceMapper;
import com.zhyc.lowcode.metadata.mybatis.LowcodeDataSourceRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MyBatis 低代码数据源仓储测试。
 */
class MyBatisLowcodeDataSourceRepositoryTest {

  /**
   * 验证保存新数据源时插入记录并返回数据库持久化后的主键和密钥引用。
   */
  @Test
  void shouldInsertMissingDataSourceAndReturnPersistedDomain() {
    FakeDataSourceMapper mapper = new FakeDataSourceMapper();
    LowcodeDataSourceRepository repository = new MyBatisLowcodeDataSourceRepository(mapper);
    LowcodeDataSource dataSource = new LowcodeDataSource(
        null, "tenant_a", "main_mysql", "主库", LowcodeDatabaseDialect.MYSQL,
        "jdbc:mysql://127.0.0.1:3306/zhyc", "root", "secret:main", true);

    LowcodeDataSource saved = repository.save(dataSource);

    assertEquals(1, mapper.insertCount);
    assertEquals(0, mapper.updateCount);
    assertEquals(101L, saved.getId());
    assertEquals("secret:main", saved.getPasswordSecretRef());
    assertEquals("mysql", mapper.records.get("tenant_a:main_mysql").dialect());
  }

  /**
   * 验证保存已有数据源时按租户和编码更新，不新增重复数据源。
   */
  @Test
  void shouldUpdateExistingDataSourceByTenantAndCode() {
    FakeDataSourceMapper mapper = new FakeDataSourceMapper();
    mapper.records.put("tenant_a:main_mysql", new LowcodeDataSourceRecord(
        101L, "tenant_a", "main_mysql", "旧主库", "MYSQL",
        "jdbc:mysql://old", "old", "secret:old", true));
    LowcodeDataSourceRepository repository = new MyBatisLowcodeDataSourceRepository(mapper);
    LowcodeDataSource dataSource = new LowcodeDataSource(
        null, "tenant_a", "main_mysql", "新主库", LowcodeDatabaseDialect.MYSQL,
        "jdbc:mysql://new", "new", "secret:new", false);

    LowcodeDataSource saved = repository.save(dataSource);

    assertEquals(0, mapper.insertCount);
    assertEquals(1, mapper.updateCount);
    assertEquals(101L, saved.getId());
    assertEquals("新主库", saved.getName());
    assertEquals("secret:new", saved.getPasswordSecretRef());
    assertEquals("mysql", mapper.records.get("tenant_a:main_mysql").dialect());
  }

  /**
   * 验证按租户查询数据源列表时不会返回其他租户数据。
   */
  @Test
  void shouldFindDataSourcesByTenant() {
    FakeDataSourceMapper mapper = new FakeDataSourceMapper();
    mapper.records.put("tenant_a:main_mysql", new LowcodeDataSourceRecord(
        101L, "tenant_a", "main_mysql", "主库", "mysql",
        "jdbc:mysql://tenant-a", "root", null, true));
    mapper.records.put("tenant_b:main_mysql", new LowcodeDataSourceRecord(
        102L, "tenant_b", "main_mysql", "其他租户主库", "mysql",
        "jdbc:mysql://tenant-b", "root", null, true));
    LowcodeDataSourceRepository repository = new MyBatisLowcodeDataSourceRepository(mapper);

    List<LowcodeDataSource> dataSources = repository.findByTenantId("tenant_a");

    assertEquals(1, dataSources.size());
    assertEquals("tenant_a", dataSources.get(0).getTenantId());
  }

  /**
   * 验证按租户和主键查询数据源时不会返回其他租户同主键数据。
   */
  @Test
  void shouldFindDataSourceByTenantAndId() {
    FakeDataSourceMapper mapper = new FakeDataSourceMapper();
    mapper.records.put("tenant_a:main_mysql", new LowcodeDataSourceRecord(
        101L, "tenant_a", "main_mysql", "主库", "mysql",
        "jdbc:mysql://tenant-a", "root", null, true));
    mapper.records.put("tenant_b:main_mysql", new LowcodeDataSourceRecord(
        101L, "tenant_b", "main_mysql", "其他租户主库", "mysql",
        "jdbc:mysql://tenant-b", "root", null, true));
    LowcodeDataSourceRepository repository = new MyBatisLowcodeDataSourceRepository(mapper);

    LowcodeDataSource dataSource = repository.findByTenantIdAndId("tenant_a", 101L).orElseThrow();

    assertEquals("tenant_a", dataSource.getTenantId());
    assertEquals("主库", dataSource.getName());
  }

  /**
   * 验证按租户和主键查询数据源时，其他租户数据不会被错误返回。
   */
  @Test
  void shouldNotFindDataSourceByOtherTenantId() {
    FakeDataSourceMapper mapper = new FakeDataSourceMapper();
    mapper.records.put("tenant_b:main_mysql", new LowcodeDataSourceRecord(
        101L, "tenant_b", "main_mysql", "其他租户主库", "mysql",
        "jdbc:mysql://tenant-b", "root", null, true));
    LowcodeDataSourceRepository repository = new MyBatisLowcodeDataSourceRepository(mapper);

    assertEquals(java.util.Optional.empty(), repository.findByTenantIdAndId("tenant_a", 101L));
  }

  /**
   * 测试用数据源 Mapper。
   */
  private static class FakeDataSourceMapper implements LowcodeDataSourceMapper {

    /** 已持久化的数据源记录。 */
    private final Map<String, LowcodeDataSourceRecord> records = new HashMap<>();
    /** 插入次数。 */
    private int insertCount;
    /** 更新次数。 */
    private int updateCount;

    @Override
    public int insert(LowcodeDataSourceRecord record) {
      insertCount++;
      records.put(key(record.tenantId(), record.code()), new LowcodeDataSourceRecord(
          101L, record.tenantId(), record.code(), record.name(), record.dialect(), record.jdbcUrl(),
          record.username(), record.passwordSecretRef(), record.enabled()));
      return 1;
    }

    @Override
    public LowcodeDataSourceRecord selectByTenantIdAndCode(String tenantId, String code) {
      return records.get(key(tenantId, code));
    }

    @Override
    public LowcodeDataSourceRecord selectByTenantIdAndId(String tenantId, Long id) {
      return records.values().stream()
          .filter(record -> record.tenantId().equals(tenantId))
          .filter(record -> id.equals(record.id()))
          .findFirst()
          .orElse(null);
    }

    @Override
    public List<LowcodeDataSourceRecord> selectByTenantId(String tenantId) {
      return records.values().stream()
          .filter(record -> record.tenantId().equals(tenantId))
          .toList();
    }

    @Override
    public int updateByTenantIdAndCode(LowcodeDataSourceRecord record) {
      updateCount++;
      LowcodeDataSourceRecord existing = records.get(key(record.tenantId(), record.code()));
      records.put(key(record.tenantId(), record.code()), new LowcodeDataSourceRecord(
          existing.id(), record.tenantId(), record.code(), record.name(), record.dialect(), record.jdbcUrl(),
          record.username(), record.passwordSecretRef(), record.enabled()));
      return 1;
    }

    private String key(String tenantId, String code) {
      return tenantId + ":" + code;
    }
  }
}
