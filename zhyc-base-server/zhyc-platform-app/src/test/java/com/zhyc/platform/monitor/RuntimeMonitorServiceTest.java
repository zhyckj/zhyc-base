/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

/**
 * 平台运行监控服务测试。
 */
class RuntimeMonitorServiceTest {

  /**
   * 验证运行监控会返回容器中注册的每个数据源状态，避免只展示固定主库。
   */
  @Test
  void shouldReportEveryRegisteredDataSource() {
    Map<String, DataSource> dataSources = new LinkedHashMap<>();
    dataSources.put("primaryDataSource", availableDataSource());
    dataSources.put("reportDataSource", unavailableDataSource());
    RuntimeMonitorService service = new DefaultRuntimeMonitorService(dataSources, "platform", "1.0.0");

    List<RuntimeDataSourceStatus> statuses = service.listDataSourceStatus();

    assertEquals(2, statuses.size());
    assertEquals("primaryDataSource", statuses.get(0).getSourceCode());
    assertEquals("primary", statuses.get(0).getSourceName());
    assertEquals("CONNECTED", statuses.get(0).getStatus());
    assertEquals("reportDataSource", statuses.get(1).getSourceCode());
    assertEquals("report", statuses.get(1).getSourceName());
    assertEquals("DISCONNECTED", statuses.get(1).getStatus());
    assertTrue(statuses.get(0).getCostMs() >= 1L);
    assertTrue(statuses.get(1).getCostMs() >= 1L);
  }

  /**
   * 验证服务监控会返回服务健康检测耗时，供前端展示服务响应速度。
   */
  @Test
  void shouldReportServiceResponseTime() {
    RuntimeMonitorService service = new DefaultRuntimeMonitorService(Map.of(), "platform", "1.0.0");

    List<RuntimeServiceStatus> statuses = service.listServiceStatus();

    assertEquals(1, statuses.size());
    assertEquals("platform", statuses.get(0).getServiceName());
    assertEquals("UP", statuses.get(0).getStatus());
    assertTrue(statuses.get(0).getResponseTimeMs() >= 1L);
  }

  /**
   * 验证 SQL 监控会返回应用内采集到的 MyBatis 执行记录，避免仅依赖数据库性能视图导致页面空白。
   */
  @Test
  void shouldReportApplicationCollectedSqlRecords() {
    RuntimeSqlMonitorCollector collector = new RuntimeSqlMonitorCollector();
    collector.record("dataSource", "SELECT id, login_account FROM sys_user WHERE tenant_id = ?", 18L, 3L);
    RuntimeMonitorService service = new DefaultRuntimeMonitorService(Map.of(), "platform", "1.0.0", collector);

    List<RuntimeSqlMonitorRecord> records = service.listSqlMonitorRecords(1, 20);

    assertEquals(1, records.size());
    assertEquals("dataSource", records.get(0).sourceCode());
    assertEquals("SELECT id, login_account FROM sys_user WHERE tenant_id = ?", records.get(0).sqlDigest());
    assertEquals(1L, records.get(0).executeCount());
    assertEquals(18L, records.get(0).avgCostMs());
    assertEquals(18L, records.get(0).maxCostMs());
    assertEquals(3L, records.get(0).rowsSent());
  }

  /**
   * 构建可用数据源。
   *
   * @return 可用数据源
   */
  private static DataSource availableDataSource() {
    return new TestDataSource(true);
  }

  /**
   * 构建不可用数据源。
   *
   * @return 不可用数据源
   */
  private static DataSource unavailableDataSource() {
    return new TestDataSource(false);
  }

  /**
   * 用于监控服务单测的数据源实现。
   */
  private static final class TestDataSource implements DataSource {

    /** 是否可连接。 */
    private final boolean available;

    /**
     * 创建测试数据源。
     *
     * @param available 是否可连接
     */
    private TestDataSource(boolean available) {
      this.available = available;
    }

    @Override
    public Connection getConnection() throws SQLException {
      if (!available) {
        throw new SQLException("数据源不可用");
      }
      return (Connection) Proxy.newProxyInstance(
          RuntimeMonitorServiceTest.class.getClassLoader(),
          new Class<?>[] {Connection.class},
          (proxy, method, args) -> {
            if ("isValid".equals(method.getName())) {
              return true;
            }
            if ("close".equals(method.getName())) {
              return null;
            }
            return defaultValue(method.getReturnType());
          });
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
      return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() {
      return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
      // 测试数据源不需要日志输出。
    }

    @Override
    public void setLoginTimeout(int seconds) {
      // 测试数据源不需要登录超时控制。
    }

    @Override
    public int getLoginTimeout() {
      return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      throw new SQLFeatureNotSupportedException("测试数据源不支持父 Logger");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      throw new SQLException("测试数据源不支持 unwrap");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
      return false;
    }
  }

  /**
   * 返回 Java 基本类型默认值。
   *
   * @param returnType 返回类型
   * @return 默认值
   */
  private static Object defaultValue(Class<?> returnType) {
    if (returnType == boolean.class) {
      return false;
    }
    if (returnType == int.class) {
      return 0;
    }
    if (returnType == long.class) {
      return 0L;
    }
    return null;
  }
}
