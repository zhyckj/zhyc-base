/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.search.service.SearchIndexConfigResponse;
import com.zhyc.search.service.SearchIndexConfigSaveCommand;
import com.zhyc.search.service.SearchQueryCommand;
import com.zhyc.search.service.SearchQueryLogResponse;
import com.zhyc.search.service.SearchQueryResponse;
import com.zhyc.search.service.SearchRebuildTaskCommand;
import com.zhyc.search.service.SearchRebuildTaskResponse;
import com.zhyc.search.service.SearchService;
import java.util.List;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全文检索管理接口。
 */
@RestController
@RequestMapping("/search")
public class SearchController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 索引配置保存请求为空错误码。 */
  private static final String ERROR_INDEX_CONFIG_SAVE_REQUEST_REQUIRED =
      "ZHYC_SEARCH_INDEX_CONFIG_SAVE_REQUEST_REQUIRED";

  /** 索引重建任务创建请求为空错误码。 */
  private static final String ERROR_REBUILD_TASK_CREATE_REQUEST_REQUIRED =
      "ZHYC_SEARCH_REBUILD_TASK_CREATE_REQUEST_REQUIRED";

  /** 全文检索查询请求为空错误码。 */
  private static final String ERROR_QUERY_REQUEST_REQUIRED = "ZHYC_SEARCH_QUERY_REQUEST_REQUIRED";

  /** 全文检索业务服务。 */
  private final SearchService searchService;

  /**
   * 创建全文检索控制器。
   *
   * @param searchService 全文检索业务服务
   */
  public SearchController(SearchService searchService) {
    this.searchService = Objects.requireNonNull(searchService, "全文检索业务服务不能为空");
  }

  /**
   * 查询索引配置列表。
   *
   * @param tenantId 租户业务编码
   * @param status 索引状态
   * @return 索引配置列表
   */
  @RequiresPermissions("search:index:query")
  @GetMapping("/index-configs")
  public ApiResult<List<SearchIndexConfigResponse>> listIndexConfigs(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "status", required = false) String status) {
    return ApiResult.ok(searchService.listIndexConfigs(tenantId, status));
  }

  /**
   * 保存索引配置。
   *
   * @param command 索引配置保存命令
   * @return 空响应
   */
  @RequiresPermissions("search:index:save")
  @PostMapping("/index-configs")
  public ApiResult<Void> saveIndexConfig(@RequestBody SearchIndexConfigSaveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_INDEX_CONFIG_SAVE_REQUEST_REQUIRED, "索引配置保存请求不能为空");
    }
    searchService.saveIndexConfig(command);
    return ApiResult.ok(null);
  }

  /**
   * 创建索引重建任务。
   *
   * @param command 重建任务创建命令
   * @return 空响应
   */
  @RequiresPermissions("search:task:create")
  @PostMapping("/rebuild-tasks")
  public ApiResult<Void> createRebuildTask(@RequestBody SearchRebuildTaskCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_REBUILD_TASK_CREATE_REQUEST_REQUIRED, "索引重建任务创建请求不能为空");
    }
    searchService.createRebuildTask(command);
    return ApiResult.ok(null);
  }

  /**
   * 查询索引重建任务列表。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 索引重建任务列表
   */
  @RequiresPermissions("search:task:query")
  @GetMapping("/rebuild-tasks")
  public ApiResult<List<SearchRebuildTaskResponse>> listRebuildTasks(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "indexCode", required = false) String indexCode) {
    return ApiResult.ok(searchService.listRebuildTasks(tenantId, indexCode));
  }

  /**
   * 执行全文检索。
   *
   * @param command 检索查询命令
   * @return 检索查询结果
   */
  @RequiresPermissions("search:query:execute")
  @PostMapping("/query")
  public ApiResult<SearchQueryResponse> search(@RequestBody SearchQueryCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_QUERY_REQUEST_REQUIRED, "全文检索查询请求不能为空");
    }
    return ApiResult.ok(searchService.search(command));
  }

  /**
   * 查询检索日志列表。
   *
   * @param tenantId 租户业务编码
   * @param indexCode 索引编码
   * @return 检索日志列表
   */
  @RequiresPermissions("search:log:query")
  @GetMapping("/query-logs")
  public ApiResult<List<SearchQueryLogResponse>> listQueryLogs(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "indexCode", required = false) String indexCode) {
    return ApiResult.ok(searchService.listQueryLogs(tenantId, indexCode));
  }
}
