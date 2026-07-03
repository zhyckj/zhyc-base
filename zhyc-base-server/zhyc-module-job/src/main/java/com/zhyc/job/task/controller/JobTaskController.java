/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.job.task.service.JobTaskLogResponse;
import com.zhyc.job.task.service.JobTaskResponse;
import com.zhyc.job.task.service.JobTaskSaveCommand;
import com.zhyc.job.task.service.JobTaskService;
import com.zhyc.job.task.service.JobTaskStatusCommand;
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
 * 在线作业任务管理接口。
 */
@RestController
@RequestMapping("/job/tasks")
public class JobTaskController {

  /** 租户业务编码请求头。 */
  public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";
  /** 操作人用户主键请求头。 */
  public static final String HEADER_OPERATOR_ID = "X-ZHYC-User-Id";

  /** 作业任务保存请求为空错误码。 */
  private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_JOB_TASK_SAVE_REQUEST_REQUIRED";

  /** 作业任务状态变更请求为空错误码。 */
  private static final String ERROR_STATUS_REQUEST_REQUIRED = "ZHYC_JOB_TASK_STATUS_REQUEST_REQUIRED";

  /** 在线作业任务业务服务。 */
  private final JobTaskService taskService;

  /**
   * 创建在线作业任务管理接口。
   *
   * @param taskService 在线作业任务业务服务
   */
  public JobTaskController(JobTaskService taskService) {
    this.taskService = Objects.requireNonNull(taskService, "在线作业任务业务服务不能为空");
  }

  /**
   * 查询作业任务列表。
   *
   * @param tenantId 租户业务编码
   * @param status 作业状态
   * @return 作业任务列表
   */
  @RequiresPermissions("job:task:query")
  @GetMapping("")
  public ApiResult<List<JobTaskResponse>> listTasks(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestParam(value = "status", required = false) String status) {
    return ApiResult.ok(taskService.listTasks(tenantId, status));
  }

  /**
   * 保存作业任务。
   *
   * @param command 作业任务保存命令
   * @return 空响应
   */
  @RequiresPermissions("job:task:save")
  @PostMapping("")
  public ApiResult<Void> save(@RequestBody JobTaskSaveCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "作业任务保存请求不能为空");
    }
    taskService.save(command);
    return ApiResult.ok(null);
  }

  /**
   * 更新作业任务状态。
   *
   * @param tenantId 租户业务编码
   * @param id 作业任务主键
   * @param command 状态变更命令
   * @return 空响应
   */
  @RequiresPermissions("job:task:save")
  @PostMapping("/{id}/status")
  public ApiResult<Void> updateStatus(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("id") Long id, @RequestBody JobTaskStatusCommand command) {
    if (command == null) {
      throw new BusinessException(ERROR_STATUS_REQUEST_REQUIRED, "作业任务状态变更请求不能为空");
    }
    taskService.updateStatus(tenantId, id, command.status());
    return ApiResult.ok(null);
  }

  /**
   * 手动触发作业任务。
   *
   * @param tenantId 租户业务编码
   * @param operatorId 操作人用户主键
   * @param id 作业任务主键
   * @return 空响应
   */
  @RequiresPermissions("job:task:trigger")
  @PostMapping("/{id}/trigger")
  public ApiResult<Void> trigger(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @RequestHeader(value = HEADER_OPERATOR_ID, required = false) Long operatorId,
      @PathVariable("id") Long id) {
    taskService.trigger(tenantId, id, operatorId);
    return ApiResult.ok(null);
  }

  /**
   * 查询作业执行日志。
   *
   * @param tenantId 租户业务编码
   * @param id 作业任务主键
   * @return 作业执行日志列表
   */
  @RequiresPermissions("job:log:query")
  @GetMapping("/{id}/logs")
  public ApiResult<List<JobTaskLogResponse>> listLogs(@RequestHeader(HEADER_TENANT_ID) String tenantId,
      @PathVariable("id") Long id) {
    return ApiResult.ok(taskService.listLogs(tenantId, id));
  }
}
