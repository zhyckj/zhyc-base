/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task;

import com.zhyc.job.task.domain.JobTask;
import com.zhyc.job.task.repository.JobTaskRepository;
import com.zhyc.job.task.service.DefaultJobTaskService;
import com.zhyc.job.task.service.JobTaskLogResponse;
import com.zhyc.job.task.service.JobTaskResponse;
import com.zhyc.job.task.service.JobTaskSaveCommand;
import com.zhyc.job.task.service.JobTaskService;
import com.zhyc.job.task.service.JobTaskExecutionContext;
import com.zhyc.job.task.service.JobTaskHandler;
import com.zhyc.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 在线作业任务业务服务测试。
 */
class JobTaskServiceTest {

  /**
   * 验证保存作业任务会裁剪租户、编码、名称、Cron 和处理器名称。
   */
  @Test
  void shouldSaveJobTaskWithNormalizedFields() {
    RecordingRepository repository = new RecordingRepository();
    JobTaskService service = new DefaultJobTaskService(repository);

    service.save(new JobTaskSaveCommand(" tenant_a ", " daily-sync ", " 每日同步 ",
        " 0 0 2 * * ? ", " purchaseSyncHandler ", " 业务数据同步 ", "enabled"));

    assertEquals("tenant_a", repository.lastSaved.getTenantId());
    assertEquals("daily-sync", repository.lastSaved.getJobCode());
    assertEquals("每日同步", repository.lastSaved.getJobName());
    assertEquals("0 0 2 * * ?", repository.lastSaved.getCronExpression());
    assertEquals("purchaseSyncHandler", repository.lastSaved.getHandlerName());
    assertEquals("enabled", repository.lastSaved.getStatus());
  }

  /**
   * 验证保存作业任务时必须提供处理器名称。
   */
  @Test
  void shouldRejectMissingHandlerName() {
    JobTaskService service = new DefaultJobTaskService(new RecordingRepository());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.save(new JobTaskSaveCommand("tenant_a", "daily-sync", "每日同步",
            "0 0 2 * * ?", " ", "业务数据同步", "enabled")));

    assertEquals("ZHYC_JOB_TASK_HANDLER_REQUIRED", exception.getCode());
    assertEquals("任务处理器名称不能为空", exception.getMessage());
  }

  /**
   * 验证手动触发作业会写入首期执行日志。
   */
  @Test
  void shouldTriggerJobTaskAndWriteLog() {
    RecordingRepository repository = new RecordingRepository();
    RecordingHandler handler = new RecordingHandler("purchaseSyncHandler");
    JobTaskService service = new DefaultJobTaskService(repository, List.of(handler));

    service.trigger(" tenant_a ", 10L, 1001L);

    assertEquals("tenant_a", handler.lastContext.tenantId());
    assertEquals(10L, handler.lastContext.jobId());
    assertEquals("daily-sync", handler.lastContext.jobCode());
    assertEquals(1001L, handler.lastContext.operatorId());
    assertEquals("tenant_a", repository.lastTriggeredTenantId);
    assertEquals(10L, repository.lastLogJobId);
    assertEquals("manual", repository.lastLogTriggerType);
    assertEquals("success", repository.lastLogResult);
    assertEquals(1001L, repository.lastLogOperatorId);
  }

  /**
   * 验证作业处理器执行失败时会写入失败日志并返回稳定业务异常。
   */
  @Test
  void shouldWriteFailureLogWhenHandlerFails() {
    RecordingRepository repository = new RecordingRepository();
    RecordingHandler handler = new RecordingHandler("purchaseSyncHandler");
    handler.failure = new IllegalStateException("外部系统不可用");
    JobTaskService service = new DefaultJobTaskService(repository, List.of(handler));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.trigger("tenant_a", 10L, 1001L));

    assertEquals("ZHYC_JOB_TASK_HANDLER_FAILED", exception.getCode());
    assertEquals("作业任务执行失败: 外部系统不可用", exception.getMessage());
    assertEquals("failure", repository.lastLogResult);
    assertEquals("外部系统不可用", repository.lastLogErrorMessage);
  }

  /**
   * 验证作业处理器不存在时不能写入成功日志。
   */
  @Test
  void shouldRejectTriggerWhenHandlerMissing() {
    RecordingRepository repository = new RecordingRepository();
    JobTaskService service = new DefaultJobTaskService(repository, List.of());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.trigger("tenant_a", 10L, 1001L));

    assertEquals("ZHYC_JOB_TASK_HANDLER_NOT_FOUND", exception.getCode());
    assertEquals("作业任务处理器不存在: purchaseSyncHandler", exception.getMessage());
    assertEquals("failure", repository.lastLogResult);
    assertEquals("作业任务处理器不存在: purchaseSyncHandler", repository.lastLogErrorMessage);
  }

  /**
   * 验证禁用作业不能被手动触发，避免误写成功执行日志。
   */
  @Test
  void shouldRejectTriggerWhenJobTaskDisabled() {
    RecordingRepository repository = new RecordingRepository();
    repository.triggerTaskStatus = "disabled";
    JobTaskService service = new DefaultJobTaskService(repository);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.trigger("tenant_a", 10L, 1001L));

    assertEquals("ZHYC_JOB_TASK_TRIGGER_DISABLED", exception.getCode());
    assertEquals("作业任务未启用，不能手动触发", exception.getMessage());
    assertEquals(0, repository.insertLogCount);
  }

  /**
   * 验证查询作业任务和执行日志会按租户隔离。
   */
  @Test
  void shouldListTasksAndLogsByTenant() {
    RecordingRepository repository = new RecordingRepository();
    JobTaskService service = new DefaultJobTaskService(repository);

    List<JobTaskResponse> tasks = service.listTasks(" tenant_a ", " enabled ");
    List<JobTaskLogResponse> logs = service.listLogs(" tenant_a ", 10L);

    assertEquals("tenant_a", repository.lastListTenantId);
    assertEquals("enabled", repository.lastListStatus);
    assertEquals("daily-sync", tasks.getFirst().jobCode());
    assertEquals("success", logs.getFirst().result());
  }

  /**
   * 验证作业任务拒绝非法状态，避免任意状态进入调度任务和状态筛选。
   */
  @Test
  void shouldRejectInvalidJobTaskStatuses() {
    JobTaskService service = new DefaultJobTaskService(new RecordingRepository());

    BusinessException saveException = assertThrows(BusinessException.class,
        () -> service.save(new JobTaskSaveCommand("tenant_a", "daily-sync", "每日同步",
            "0 0 2 * * ?", "purchaseSyncHandler", "业务数据同步", "paused")));
    BusinessException listException = assertThrows(BusinessException.class,
        () -> service.listTasks("tenant_a", "paused"));
    BusinessException updateException = assertThrows(BusinessException.class,
        () -> service.updateStatus("tenant_a", 10L, "paused"));

    assertEquals("ZHYC_JOB_TASK_STATUS_UNSUPPORTED", saveException.getCode());
    assertEquals("作业状态不支持: paused", saveException.getMessage());
    assertEquals("ZHYC_JOB_TASK_STATUS_UNSUPPORTED", listException.getCode());
    assertEquals("作业状态不支持: paused", listException.getMessage());
    assertEquals("ZHYC_JOB_TASK_STATUS_UNSUPPORTED", updateException.getCode());
    assertEquals("作业状态不支持: paused", updateException.getMessage());
  }

  /**
   * 测试用作业任务仓储。
   */
  private static class RecordingRepository implements JobTaskRepository {

    /** 最近一次保存的作业任务。 */
    private JobTask lastSaved;
    /** 最近一次查询租户。 */
    private String lastListTenantId;
    /** 最近一次查询状态。 */
    private String lastListStatus;
    /** 最近一次触发租户。 */
    private String lastTriggeredTenantId;
    /** 最近一次日志作业主键。 */
    private Long lastLogJobId;
    /** 最近一次日志触发类型。 */
    private String lastLogTriggerType;
    /** 最近一次日志结果。 */
    private String lastLogResult;
    /** 最近一次日志错误信息。 */
    private String lastLogErrorMessage;
    /** 最近一次日志操作人。 */
    private Long lastLogOperatorId;
    /** 触发前查询到的作业状态。 */
    private String triggerTaskStatus = "enabled";
    /** 写入执行日志次数。 */
    private int insertLogCount;

    @Override
    public List<JobTask> findByTenantAndStatus(String tenantId, String status) {
      lastListTenantId = tenantId;
      lastListStatus = status;
      return List.of(new JobTask(10L, tenantId, "daily-sync", "每日同步",
          "0 0 2 * * ?", "purchaseSyncHandler", "业务数据同步", "enabled",
          LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public Optional<JobTask> findByTenantAndId(String tenantId, Long id) {
      return Optional.of(new JobTask(id, tenantId, "daily-sync", "每日同步",
          "0 0 2 * * ?", "purchaseSyncHandler", "业务数据同步", triggerTaskStatus,
          LocalDateTime.now(), LocalDateTime.now()));
    }

    @Override
    public void save(JobTask task) {
      lastSaved = task;
    }

    @Override
    public void updateStatus(String tenantId, Long id, String status) {
      lastTriggeredTenantId = tenantId;
    }

    @Override
    public void insertLog(String tenantId, Long jobId, String triggerType, String result,
        String errorMessage, Long operatorId) {
      insertLogCount += 1;
      lastTriggeredTenantId = tenantId;
      lastLogJobId = jobId;
      lastLogTriggerType = triggerType;
      lastLogResult = result;
      lastLogErrorMessage = errorMessage;
      lastLogOperatorId = operatorId;
    }

    @Override
    public List<JobTaskLogResponse> findLogs(String tenantId, Long jobId) {
      lastListTenantId = tenantId;
      List<JobTaskLogResponse> logs = new ArrayList<>();
      logs.add(new JobTaskLogResponse(1L, tenantId, jobId, "manual", LocalDateTime.now(),
          LocalDateTime.now(), "success", null, 1001L));
      return logs;
    }
  }

  /**
   * 测试用作业处理器。
   */
  private static class RecordingHandler implements JobTaskHandler {

    /** 处理器名称。 */
    private final String handlerName;
    /** 最近一次执行上下文。 */
    private JobTaskExecutionContext lastContext;
    /** 触发时抛出的异常。 */
    private RuntimeException failure;

    /**
     * 创建测试处理器。
     *
     * @param handlerName 处理器名称
     */
    private RecordingHandler(String handlerName) {
      this.handlerName = handlerName;
    }

    @Override
    public String getHandlerName() {
      return handlerName;
    }

    @Override
    public void handle(JobTaskExecutionContext context) {
      this.lastContext = context;
      if (failure != null) {
        throw failure;
      }
    }
  }
}
