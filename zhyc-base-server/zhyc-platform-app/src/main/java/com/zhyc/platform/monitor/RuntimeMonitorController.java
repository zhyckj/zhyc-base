/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import com.zhyc.common.api.ApiResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 平台运行监控接口。
 */
@RestController
@RequestMapping("/monitor/runtime")
public class RuntimeMonitorController {

  /** 平台运行监控服务。 */
  private final RuntimeMonitorService monitorService;

  /**
   * 创建平台运行监控接口。
   *
   * @param monitorService 平台运行监控服务
   */
  public RuntimeMonitorController(RuntimeMonitorService monitorService) {
    this.monitorService = Objects.requireNonNull(monitorService, "平台运行监控服务不能为空");
  }

  /**
   * 查询服务运行状态。
   *
   * @return 服务运行状态列表
   */
  @RequiresPermissions("monitor:service:query")
  @GetMapping("/services")
  public ApiResult<List<RuntimeServiceStatus>> listServiceStatus() {
    return ApiResult.ok(monitorService.listServiceStatus());
  }

  /**
   * 查询数据源运行状态。
   *
   * @return 数据源运行状态列表
   */
  @RequiresPermissions("monitor:data-source:query")
  @GetMapping("/data-sources")
  public ApiResult<List<RuntimeDataSourceStatus>> listDataSourceStatus() {
    return ApiResult.ok(monitorService.listDataSourceStatus());
  }

  /**
   * 查询 SQL 执行效率监控记录。
   *
   * @param thresholdMs 慢 SQL 平均耗时阈值，单位毫秒
   * @param limit 最大返回记录数
   * @return SQL 执行效率监控记录
   */
  @RequiresPermissions("monitor:sql:query")
  @GetMapping("/sql")
  public ApiResult<List<RuntimeSqlMonitorRecord>> listSqlMonitorRecords(
      @RequestParam(value = "thresholdMs", defaultValue = "1") int thresholdMs,
      @RequestParam(value = "limit", defaultValue = "20") int limit) {
    return ApiResult.ok(monitorService.listSqlMonitorRecords(thresholdMs, limit));
  }
}
