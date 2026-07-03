/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.visual.service.VisualDatasetPreviewResponse;
import com.zhyc.visual.service.VisualDatasetResponse;
import com.zhyc.visual.service.VisualDatasetSaveCommand;
import com.zhyc.visual.service.VisualReportResponse;
import com.zhyc.visual.service.VisualReportSaveCommand;
import com.zhyc.visual.service.VisualScreenResponse;
import com.zhyc.visual.service.VisualScreenSaveCommand;
import com.zhyc.visual.service.VisualService;
import com.zhyc.visual.service.VisualStatusCommand;
import java.util.List;
import java.util.Objects;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 可视化报表接口。
 */
@RestController
@RequestMapping("/visual")
public class VisualController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";

  /** 可视化数据集保存请求为空错误码。 */
  private static final String ERROR_DATASET_SAVE_REQUEST_REQUIRED =
      "ZHYC_VISUAL_DATASET_SAVE_REQUEST_REQUIRED";

  /** 可视化报表保存请求为空错误码。 */
  private static final String ERROR_REPORT_SAVE_REQUEST_REQUIRED = "ZHYC_VISUAL_REPORT_SAVE_REQUEST_REQUIRED";

  /** 可视化大屏保存请求为空错误码。 */
  private static final String ERROR_SCREEN_SAVE_REQUEST_REQUIRED = "ZHYC_VISUAL_SCREEN_SAVE_REQUEST_REQUIRED";

  /** 可视化资源状态变更请求为空错误码。 */
  private static final String ERROR_RESOURCE_STATUS_REQUEST_REQUIRED =
      "ZHYC_VISUAL_RESOURCE_STATUS_REQUEST_REQUIRED";

  /** 可视化报表业务服务。 */
  private final VisualService visualService;

  /**
   * 创建可视化报表接口。
   *
   * @param visualService 可视化报表业务服务
   */
  public VisualController(VisualService visualService) {
    this.visualService = Objects.requireNonNull(visualService, "可视化报表业务服务不能为空");
  }

  /**
   * 查询数据集列表。
   *
   * @param tenantId 租户业务编码
   * @param status 数据集状态
   * @return 数据集列表
   */
  @RequiresPermissions("visual:dataset:query")
  @GetMapping("/datasets")
  public ApiResult<List<VisualDatasetResponse>> listDatasets(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "status", required = false) String status) {
    return ApiResult.ok(visualService.listDatasets(tenantId, status));
  }

  /**
   * 保存数据集。
   *
   * @param command 数据集保存命令
   * @return 空响应
   */
  @RequiresPermissions("visual:dataset:save")
  @PostMapping("/datasets")
  public ApiResult<Void> saveDataset(@RequestBody VisualDatasetSaveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_DATASET_SAVE_REQUEST_REQUIRED, "可视化数据集保存请求不能为空");
    }
    visualService.saveDataset(command);
    return ApiResult.ok(null);
  }

  /**
   * 预览数据集字段和样例数据。
   *
   * @param tenantId 租户业务编码
   * @param datasetCode 数据集编码
   * @param limit 预览行数上限
   * @return 数据集预览响应
   */
  @RequiresPermissions("visual:dataset:query")
  @GetMapping("/datasets/{datasetCode}/preview")
  public ApiResult<VisualDatasetPreviewResponse> previewDataset(
      @RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("datasetCode") String datasetCode,
      @RequestParam(value = "limit", required = false) Integer limit) {
    return ApiResult.ok(visualService.previewDataset(tenantId, datasetCode, limit));
  }

  /**
   * 查询报表列表。
   *
   * @param tenantId 租户业务编码
   * @param status 报表状态
   * @return 报表列表
   */
  @RequiresPermissions("visual:report:query")
  @GetMapping("/reports")
  public ApiResult<List<VisualReportResponse>> listReports(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "status", required = false) String status) {
    return ApiResult.ok(visualService.listReports(tenantId, status));
  }

  /**
   * 保存报表。
   *
   * @param command 报表保存命令
   * @return 空响应
   */
  @RequiresPermissions("visual:report:save")
  @PostMapping("/reports")
  public ApiResult<Void> saveReport(@RequestBody VisualReportSaveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_REPORT_SAVE_REQUEST_REQUIRED, "可视化报表保存请求不能为空");
    }
    visualService.saveReport(command);
    return ApiResult.ok(null);
  }

  /**
   * 查询公开访问的已发布报表。
   *
   * @param tenantId 租户业务编码
   * @param reportCode 报表编码
   * @return 已发布报表
   */
  @GetMapping("/public/reports/{tenantId}/{reportCode}")
  public ApiResult<VisualReportResponse> getPublishedReport(@PathVariable("tenantId") String tenantId,
      @PathVariable("reportCode") String reportCode) {
    return ApiResult.ok(visualService.getPublishedReport(tenantId, reportCode));
  }

  /**
   * 预览公开报表引用的数据集字段和样例数据。
   *
   * @param tenantId 租户业务编码
   * @param reportCode 报表编码
   * @param datasetCode 数据集编码
   * @param limit 预览行数上限
   * @return 数据集预览响应
   */
  @GetMapping("/public/reports/{tenantId}/{reportCode}/datasets/{datasetCode}/preview")
  public ApiResult<VisualDatasetPreviewResponse> previewPublishedReportDataset(
      @PathVariable("tenantId") String tenantId,
      @PathVariable("reportCode") String reportCode,
      @PathVariable("datasetCode") String datasetCode,
      @RequestParam(value = "limit", required = false) Integer limit) {
    return ApiResult.ok(visualService.previewPublishedReportDataset(tenantId, reportCode, datasetCode, limit));
  }

  /**
   * 更新报表状态。
   *
   * @param tenantId 租户业务编码
   * @param id 报表主键
   * @param command 状态变更命令
   * @return 空响应
   */
  @RequiresPermissions("visual:report:publish")
  @PostMapping("/reports/{id}/status")
  public ApiResult<Void> updateReportStatus(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("id") Long id, @RequestBody VisualStatusCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_RESOURCE_STATUS_REQUEST_REQUIRED, "可视化资源状态变更请求不能为空");
    }
    visualService.updateReportStatus(tenantId, id, command.status());
    return ApiResult.ok(null);
  }

  /**
   * 查询大屏列表。
   *
   * @param tenantId 租户业务编码
   * @param status 大屏状态
   * @return 大屏列表
   */
  @RequiresPermissions("visual:screen:query")
  @GetMapping("/screens")
  public ApiResult<List<VisualScreenResponse>> listScreens(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "status", required = false) String status) {
    return ApiResult.ok(visualService.listScreens(tenantId, status));
  }

  /**
   * 查询公开访问的已发布大屏。
   *
   * @param tenantId 租户业务编码
   * @param screenCode 大屏编码
   * @return 已发布大屏
   */
  @GetMapping("/public/screens/{tenantId}/{screenCode}")
  public ApiResult<VisualScreenResponse> getPublishedScreen(@PathVariable("tenantId") String tenantId,
      @PathVariable("screenCode") String screenCode) {
    return ApiResult.ok(visualService.getPublishedScreen(tenantId, screenCode));
  }

  /**
   * 预览公开大屏引用的数据集字段和样例数据。
   *
   * @param tenantId 租户业务编码
   * @param screenCode 大屏编码
   * @param datasetCode 数据集编码
   * @param limit 预览行数上限
   * @return 数据集预览响应
   */
  @GetMapping("/public/screens/{tenantId}/{screenCode}/datasets/{datasetCode}/preview")
  public ApiResult<VisualDatasetPreviewResponse> previewPublishedScreenDataset(
      @PathVariable("tenantId") String tenantId,
      @PathVariable("screenCode") String screenCode,
      @PathVariable("datasetCode") String datasetCode,
      @RequestParam(value = "limit", required = false) Integer limit) {
    return ApiResult.ok(visualService.previewPublishedScreenDataset(tenantId, screenCode, datasetCode, limit));
  }

  /**
   * 保存大屏。
   *
   * @param command 大屏保存命令
   * @return 空响应
   */
  @RequiresPermissions("visual:screen:save")
  @PostMapping("/screens")
  public ApiResult<Void> saveScreen(@RequestBody VisualScreenSaveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_SCREEN_SAVE_REQUEST_REQUIRED, "可视化大屏保存请求不能为空");
    }
    visualService.saveScreen(command);
    return ApiResult.ok(null);
  }

  /**
   * 更新大屏状态。
   *
   * @param tenantId 租户业务编码
   * @param id 大屏主键
   * @param command 状态变更命令
   * @return 空响应
   */
  @RequiresPermissions("visual:screen:publish")
  @PostMapping("/screens/{id}/status")
  public ApiResult<Void> updateScreenStatus(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("id") Long id, @RequestBody VisualStatusCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_RESOURCE_STATUS_REQUEST_REQUIRED, "可视化资源状态变更请求不能为空");
    }
    visualService.updateScreenStatus(tenantId, id, command.status());
    return ApiResult.ok(null);
  }
}
