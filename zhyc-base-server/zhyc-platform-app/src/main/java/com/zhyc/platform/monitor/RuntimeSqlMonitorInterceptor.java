/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import java.lang.reflect.Array;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;

/**
 * MyBatis SQL 运行监控拦截器。
 *
 * <p>拦截 MyBatis 真实执行阶段，记录 SQL 摘要、执行耗时和返回/影响行数。
 * 该拦截器不会记录参数明文，SQL 摘要由采集器进一步归一化。</p>
 */
@Intercepts({
    @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})
})
public class RuntimeSqlMonitorInterceptor implements Interceptor {

  /** SQL 监控采集器。 */
  private final RuntimeSqlMonitorCollector collector;
  /** 数据源编码。 */
  private final String sourceCode;

  /**
   * 创建 MyBatis SQL 运行监控拦截器。
   *
   * @param collector SQL 监控采集器
   * @param sourceCode 数据源编码
   */
  public RuntimeSqlMonitorInterceptor(RuntimeSqlMonitorCollector collector, String sourceCode) {
    this.collector = Objects.requireNonNull(collector, "SQL 监控采集器不能为空");
    this.sourceCode = sourceCode == null || sourceCode.trim().isEmpty() ? "dataSource" : sourceCode.trim();
  }

  /**
   * 拦截 MyBatis SQL 执行并记录耗时。
   *
   * @param invocation MyBatis 调用上下文
   * @return 原始调用结果
   * @throws Throwable SQL 执行失败时抛出
   */
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    BoundSql boundSql = statementHandler.getBoundSql();
    String sql = boundSql == null ? "" : boundSql.getSql();
    long start = System.nanoTime();
    try {
      Object result = invocation.proceed();
      collector.record(sourceCode, sql, elapsedMs(start), resolveRows(result));
      return result;
    } catch (Throwable ex) {
      collector.record(sourceCode, sql, elapsedMs(start), 0L);
      throw ex;
    }
  }

  /**
   * 包装 MyBatis 目标对象。
   *
   * @param target MyBatis 目标对象
   * @return 已包装对象
   */
  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  /**
   * 计算执行耗时。
   *
   * @param startNanos 开始纳秒时间
   * @return 执行耗时，单位毫秒
   */
  private static long elapsedMs(long startNanos) {
    return Math.max(1L, (System.nanoTime() - startNanos) / 1_000_000L);
  }

  /**
   * 解析返回或影响行数。
   *
   * @param result MyBatis 执行结果
   * @return 返回或影响行数
   */
  private static long resolveRows(Object result) {
    if (result instanceof Collection<?> collection) {
      return collection.size();
    }
    if (result instanceof Number number) {
      return Math.max(0L, number.longValue());
    }
    if (result != null && result.getClass().isArray()) {
      return Array.getLength(result);
    }
    return 0L;
  }
}
