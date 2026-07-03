/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.config;

import com.zhyc.platform.monitor.RuntimeSqlMonitorCollector;
import com.zhyc.platform.monitor.RuntimeSqlMonitorInterceptor;
import com.zhyc.platform.tenant.TenantMyBatisInterceptor;
import com.zhyc.platform.tenant.TenantSqlRewriter;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 基础配置，统一扫描项目内带有 {@link Mapper} 注解的 Mapper 接口。
 */
@Configuration
@MapperScan(basePackages = "com.zhyc", annotationClass = Mapper.class)
public class MyBatisConfig {

  /**
   * 注册 MyBatis 租户条件拦截器。
   *
   * <p>首期 SaaS 采用共享库共享表，业务 SQL 仍应显式携带租户条件；该拦截器作为运行期兜底，
   * 防止租户表查询、更新和删除遗漏 {@code tenant_id} 隔离。</p>
   *
   * @return MyBatis 租户条件拦截器
   */
  @Bean
  public TenantMyBatisInterceptor tenantMyBatisInterceptor() {
    return new TenantMyBatisInterceptor(new TenantSqlRewriter(TenantSqlRewriter.firstReleaseTenantTables()));
  }

  /**
   * 注册 MyBatis SQL 运行监控拦截器。
   *
   * <p>采集平台实际执行的 SQL 摘要、耗时和返回/影响行数，避免 SQL 监控只依赖数据库性能视图导致无数据。</p>
   *
   * @param collector SQL 运行监控采集器
   * @return MyBatis SQL 运行监控拦截器
   */
  @Bean
  public RuntimeSqlMonitorInterceptor runtimeSqlMonitorInterceptor(RuntimeSqlMonitorCollector collector) {
    return new RuntimeSqlMonitorInterceptor(collector, "dataSource");
  }
}
