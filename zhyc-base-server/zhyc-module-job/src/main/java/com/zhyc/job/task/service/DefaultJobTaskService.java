/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.job.task.domain.JobTask;
import com.zhyc.job.task.repository.JobTaskRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 默认在线作业任务业务服务实现。
 */
@Service
public class DefaultJobTaskService implements JobTaskService {

  /** 作业任务首期允许使用的启停状态。 */
  private static final Set<String> JOB_STATUSES = Set.of("enabled", "disabled");

  /** 租户业务编码为空错误码。 */
  private static final String ERROR_TENANT_REQUIRED = "ZHYC_JOB_TASK_TENANT_REQUIRED";

  /** 作业任务编码为空错误码。 */
  private static final String ERROR_JOB_CODE_REQUIRED = "ZHYC_JOB_TASK_CODE_REQUIRED";

  /** 作业任务名称为空错误码。 */
  private static final String ERROR_JOB_NAME_REQUIRED = "ZHYC_JOB_TASK_NAME_REQUIRED";

  /** Cron 表达式为空错误码。 */
  private static final String ERROR_CRON_REQUIRED = "ZHYC_JOB_TASK_CRON_REQUIRED";

  /** 任务处理器名称为空错误码。 */
  private static final String ERROR_HANDLER_REQUIRED = "ZHYC_JOB_TASK_HANDLER_REQUIRED";

  /** 作业任务主键为空错误码。 */
  private static final String ERROR_ID_REQUIRED = "ZHYC_JOB_TASK_ID_REQUIRED";

  /** 作业任务不存在或无权触发错误码。 */
  private static final String ERROR_TRIGGER_NOT_FOUND = "ZHYC_JOB_TASK_TRIGGER_NOT_FOUND";

  /** 作业任务未启用错误码。 */
  private static final String ERROR_TRIGGER_DISABLED = "ZHYC_JOB_TASK_TRIGGER_DISABLED";

  /** 作业任务处理器不存在错误码。 */
  private static final String ERROR_HANDLER_NOT_FOUND = "ZHYC_JOB_TASK_HANDLER_NOT_FOUND";

  /** 作业任务处理器执行失败错误码。 */
  private static final String ERROR_HANDLER_FAILED = "ZHYC_JOB_TASK_HANDLER_FAILED";

  /** 作业状态为空错误码。 */
  private static final String ERROR_STATUS_REQUIRED = "ZHYC_JOB_TASK_STATUS_REQUIRED";

  /** 作业状态不支持错误码。 */
  private static final String ERROR_STATUS_UNSUPPORTED = "ZHYC_JOB_TASK_STATUS_UNSUPPORTED";

  /** 作业任务仓储。 */
  private final JobTaskRepository taskRepository;
  /** 作业任务处理器索引。 */
  private final Map<String, JobTaskHandler> taskHandlers;

  /**
   * 创建在线作业任务业务服务。
   *
   * @param taskRepository 作业任务仓储
   */
  public DefaultJobTaskService(JobTaskRepository taskRepository) {
    this.taskRepository = Objects.requireNonNull(taskRepository, "作业任务仓储不能为空");
    this.taskHandlers = Map.of();
  }

  /**
   * 创建支持真实处理器执行的在线作业任务业务服务。
   *
   * @param taskRepository 作业任务仓储
   * @param taskHandlers 作业任务处理器集合
   */
  @Autowired
  public DefaultJobTaskService(JobTaskRepository taskRepository, List<JobTaskHandler> taskHandlers) {
    this.taskRepository = Objects.requireNonNull(taskRepository, "作业任务仓储不能为空");
    this.taskHandlers = Objects.requireNonNull(taskHandlers, "作业任务处理器集合不能为空").stream()
        .collect(Collectors.toUnmodifiableMap(JobTaskHandler::getHandlerName, Function.identity()));
  }

  @Override
  public List<JobTaskResponse> listTasks(String tenantId, String status) {
    String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    String normalizedStatus = normalizeOptionalStatus(status);
    return taskRepository.findByTenantAndStatus(requiredTenantId, normalizedStatus).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void save(JobTaskSaveCommand command) {
    Objects.requireNonNull(command, "作业任务保存命令不能为空");
    JobTask task = new JobTask(null, requireText(command.tenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        requireText(command.jobCode(), ERROR_JOB_CODE_REQUIRED, "作业任务编码不能为空"),
        requireText(command.jobName(), ERROR_JOB_NAME_REQUIRED, "作业任务名称不能为空"),
        requireText(command.cronExpression(), ERROR_CRON_REQUIRED, "Cron 表达式不能为空"),
        requireText(command.handlerName(), ERROR_HANDLER_REQUIRED, "任务处理器名称不能为空"),
        trimToNull(command.jobDescription()), requireStatus(defaultText(command.status(), "disabled")), null, null);
    taskRepository.save(task);
  }

  @Override
  @Transactional
  public void updateStatus(String tenantId, Long id, String status) {
    taskRepository.updateStatus(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        requireId(id, ERROR_ID_REQUIRED, "作业任务主键不能为空"), requireStatus(status));
  }

  @Override
  @Transactional
  public void trigger(String tenantId, Long id, Long operatorId) {
    String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
    Long requiredId = requireId(id, ERROR_ID_REQUIRED, "作业任务主键不能为空");
    JobTask task = taskRepository.findByTenantAndId(requiredTenantId, requiredId)
        .orElseThrow(() -> new BusinessException(ERROR_TRIGGER_NOT_FOUND, "作业任务不存在或无权触发"));
    if (!"enabled".equalsIgnoreCase(task.getStatus())) {
      throw new BusinessException(ERROR_TRIGGER_DISABLED, "作业任务未启用，不能手动触发");
    }
    JobTaskExecutionContext context = new JobTaskExecutionContext(requiredTenantId, requiredId,
        task.getJobCode(), task.getJobName(), task.getHandlerName(), "manual", operatorId);
    executeHandler(context);
  }

  @Override
  public List<JobTaskLogResponse> listLogs(String tenantId, Long jobId) {
    return taskRepository.findLogs(requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
        requireId(jobId, ERROR_ID_REQUIRED, "作业任务主键不能为空"));
  }

  private JobTaskResponse toResponse(JobTask task) {
    return new JobTaskResponse(task.getId(), task.getTenantId(), task.getJobCode(), task.getJobName(),
        task.getCronExpression(), task.getHandlerName(), task.getJobDescription(), task.getStatus(),
        task.getCreatedAt(), task.getUpdatedAt());
  }

  /**
   * 执行作业处理器并写入执行日志。
   *
   * @param context 作业任务执行上下文
   */
  private void executeHandler(JobTaskExecutionContext context) {
    JobTaskHandler handler = taskHandlers.get(context.handlerName());
    if (handler == null) {
      String message = "作业任务处理器不存在: " + context.handlerName();
      taskRepository.insertLog(context.tenantId(), context.jobId(), context.triggerType(), "failure",
          message, context.operatorId());
      throw new BusinessException(ERROR_HANDLER_NOT_FOUND, message);
    }
    try {
      handler.handle(context);
      taskRepository.insertLog(context.tenantId(), context.jobId(), context.triggerType(), "success",
          null, context.operatorId());
    } catch (RuntimeException exception) {
      String message = trimToNull(exception.getMessage()) == null ? exception.getClass().getSimpleName()
          : exception.getMessage();
      taskRepository.insertLog(context.tenantId(), context.jobId(), context.triggerType(), "failure",
          message, context.operatorId());
      throw new BusinessException(ERROR_HANDLER_FAILED, "作业任务执行失败: " + message);
    }
  }

  private Long requireId(Long value, String code, String message) {
    if (value == null || value <= 0) {
      throw new BusinessException(code, message);
    }
    return value;
  }

  private String defaultText(String value, String defaultValue) {
    String normalized = trimToNull(value);
    return normalized == null ? defaultValue : normalized;
  }

  private String requireText(String value, String code, String message) {
    String normalized = trimToNull(value);
    if (normalized == null) {
      throw new BusinessException(code, message);
    }
    return normalized;
  }

  /**
   * 规范化可选作业状态筛选条件。
   *
   * @param value 原始状态值
   * @return 规范化后的状态值，未传入时返回 {@code null}
   */
  private String normalizeOptionalStatus(String value) {
    String normalized = trimToNull(value);
    return normalized == null ? null : requireStatus(normalized);
  }

  /**
   * 校验作业任务状态必须属于首期支持的启停状态。
   *
   * @param value 原始状态值
   * @return 小写规范化后的作业状态
   */
  private String requireStatus(String value) {
    String normalized = requireText(value, ERROR_STATUS_REQUIRED, "作业状态不能为空").toLowerCase(Locale.ROOT);
    if (!JOB_STATUSES.contains(normalized)) {
      throw new BusinessException(ERROR_STATUS_UNSUPPORTED, "作业状态不支持: " + normalized);
    }
    return normalized;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
