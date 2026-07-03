/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.tenant;

import com.zhyc.common.tenant.TenantContext;
import java.sql.Connection;
import java.util.Objects;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/**
 * MyBatis 租户条件拦截器。
 *
 * <p>该拦截器在 SQL 预编译前读取 {@link TenantContext}，对已声明租户表自动追加
 * {@code tenant_id} 条件，作为共享表 SaaS 隔离的兜底防线。</p>
 */
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class TenantMyBatisInterceptor implements Interceptor {

  /** 租户 SQL 重写器。 */
  private final TenantSqlRewriter sqlRewriter;

  /**
   * 创建 MyBatis 租户条件拦截器。
   *
   * @param sqlRewriter 租户 SQL 重写器
   */
  public TenantMyBatisInterceptor(TenantSqlRewriter sqlRewriter) {
    this.sqlRewriter = Objects.requireNonNull(sqlRewriter, "租户 SQL 重写器不能为空");
  }

  /**
   * 拦截 MyBatis StatementHandler，按当前租户重写 SQL。
   *
   * @param invocation MyBatis 调用上下文
   * @return 原始调用结果
   * @throws Throwable MyBatis 执行失败时抛出
   */
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    BoundSql boundSql = statementHandler.getBoundSql();
    String rewrittenSql = sqlRewriter.rewrite(boundSql.getSql(), TenantContext.getTenantId());
    if (!Objects.equals(boundSql.getSql(), rewrittenSql)) {
      MetaObject metaObject = SystemMetaObject.forObject(boundSql);
      metaObject.setValue("sql", rewrittenSql);
    }
    return invocation.proceed();
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
}
