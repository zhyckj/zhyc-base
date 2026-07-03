/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.workflow.WorkflowTaskActionContext;
import com.zhyc.common.workflow.WorkflowTaskActionHandler;
import com.zhyc.workflow.repository.WorkflowRuntimeRepository;
import com.zhyc.workflow.service.DefaultWorkflowTaskService;
import com.zhyc.workflow.service.WorkflowApprovalRecordItem;
import com.zhyc.workflow.service.WorkflowCcTaskItem;
import com.zhyc.workflow.service.WorkflowProcessRevokeCommand;
import com.zhyc.workflow.service.WorkflowProcessMonitorItem;
import com.zhyc.workflow.service.WorkflowStartedProcessItem;
import com.zhyc.workflow.service.WorkflowTaskHandleCommand;
import com.zhyc.workflow.service.WorkflowTaskDetailResponse;
import com.zhyc.workflow.service.WorkflowTaskTodoItem;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * 工作流任务服务测试。
 */
class WorkflowTaskServiceTest {

  /**
   * 验证待办任务按租户和处理人查询。
   */
  @Test
  void shouldListTodoTasksByTenantAndAssignee() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    repository.todoTasks.add(new WorkflowTaskTodoItem("tenant_a", "TASK001", "PI001",
        "采购审批", "PR202606240001", 1001L, "TODO", LocalDateTime.now()));
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository);

    List<WorkflowTaskTodoItem> items = service.listTodoTasks("tenant_a", 1001L);

    assertEquals(1, items.size());
    assertEquals("TASK001", items.get(0).getTaskId());
    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(1001L, repository.lastAssigneeUserId);
  }

  /**
   * 验证已办任务按租户和操作人查询。
   */
  @Test
  void shouldListDoneTasksByTenantAndOperator() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    repository.doneTasks.add(new WorkflowTaskTodoItem("tenant_a", "TASK001", "PI001",
        "采购审批", "PR202606240001", 1001L, "APPROVED", LocalDateTime.now()));
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository);

    List<WorkflowTaskTodoItem> items = service.listDoneTasks("tenant_a", 1001L);

    assertEquals(1, items.size());
    assertEquals("APPROVED", items.get(0).getStatus());
    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(1001L, repository.lastAssigneeUserId);
  }

  /**
   * 验证我发起的流程按租户和发起人查询。
   */
  @Test
  void shouldListStartedProcessesByTenantAndStarter() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    repository.startedProcesses.add(new WorkflowStartedProcessItem("tenant_a", "PI001",
        "purchase.request.approval", "PR202606240001", 1001L, "RUNNING",
        LocalDateTime.now()));
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository);

    List<WorkflowStartedProcessItem> items = service.listStartedProcesses("tenant_a", 1001L);

    assertEquals(1, items.size());
    assertEquals("PI001", items.get(0).getProcessInstanceId());
    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(1001L, repository.lastAssigneeUserId);
  }

  /**
   * 验证抄送我的任务按租户和接收人查询。
   */
  @Test
  void shouldListCcTasksByTenantAndReceiver() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    repository.ccTasks.add(new WorkflowCcTaskItem("tenant_a", 1L, "PI001",
        "purchase.request.approval", "PR202606240001", 1001L, 0,
        LocalDateTime.now()));
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository);

    List<WorkflowCcTaskItem> items = service.listCcTasks("tenant_a", 1001L);

    assertEquals(1, items.size());
    assertEquals("PI001", items.get(0).getProcessInstanceId());
    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(1001L, repository.lastAssigneeUserId);
  }

  /**
   * 验证流程监控按租户查询全部流程实例，不绑定当前处理人或发起人。
   */
  @Test
  void shouldListMonitoredProcessesByTenant() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    repository.monitoredProcesses.add(new WorkflowProcessMonitorItem("tenant_a", "PI001",
        "purchase.request.approval", "PR202606240001", 1001L, "RUNNING",
        LocalDateTime.of(2026, 6, 24, 10, 0), null));
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository);

    List<WorkflowProcessMonitorItem> items = service.listMonitoredProcesses(" tenant_a ");

    assertEquals(1, items.size());
    assertEquals("PI001", items.get(0).getProcessInstanceId());
    assertEquals("tenant_a", repository.lastTenantId);
  }

  /**
   * 验证审批通过时会写入统一任务处理动作。
   */
  @Test
  void shouldApproveTaskThroughRuntimeRepository() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository, List.of());

    service.approve(new WorkflowTaskHandleCommand("tenant_a", "TASK001", 1001L,
        "同意采购", Map.of("approved", true)));

    assertEquals("APPROVE", repository.lastAction);
    assertEquals("同意采购", repository.lastComment);
  }

  /**
   * 验证驳回时会写入统一任务处理动作。
   */
  @Test
  void shouldRejectTaskThroughRuntimeRepository() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository, List.of());

    service.reject(new WorkflowTaskHandleCommand("tenant_a", "TASK001", 1001L,
        "预算不足", Map.of()));

    assertEquals("REJECT", repository.lastAction);
    assertEquals("预算不足", repository.lastComment);
  }

  /**
   * 验证撤回流程实例时会记录撤回原因和操作人。
   */
  @Test
  void shouldRevokeProcessInstanceThroughRuntimeRepository() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository, List.of());

    service.revoke(new WorkflowProcessRevokeCommand("tenant_a", "PI001", 1001L,
        "申请内容需要调整"));

    assertEquals("PI001", repository.lastProcessInstanceId);
    assertEquals("REVOKE", repository.lastAction);
    assertEquals("申请内容需要调整", repository.lastComment);
  }

  /**
   * 验证任务处理命令必须包含租户、任务和操作人。
   */
  @Test
  void shouldRejectInvalidHandleCommand() {
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(
        new RecordingWorkflowRuntimeRepository(), List.of());

    assertWorkflowBusinessException(() -> service.approve(null), "工作流任务处理命令不能为空");
    assertWorkflowBusinessException(() -> service.revoke(null), "工作流流程撤回命令不能为空");
    assertWorkflowBusinessException(
        () -> service.approve(new WorkflowTaskHandleCommand(" ", "TASK001", 1001L, null, null)),
        "租户业务编码不能为空");
    assertWorkflowBusinessException(
        () -> service.approve(new WorkflowTaskHandleCommand("tenant_a", " ", 1001L, null, null)),
        "任务 ID 不能为空");
    assertWorkflowBusinessException(
        () -> service.approve(new WorkflowTaskHandleCommand("tenant_a", "TASK001", null, null, null)),
        "操作用户 ID 不能为空");
  }

  /**
   * 验证任务处理后会通知业务模块回写业务状态，工作流模块不直接依赖具体业务模块。
   */
  @Test
  void shouldNotifyActionHandlersAfterTaskHandled() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    repository.lastContext = new WorkflowTaskActionContext("tenant_a", "TASK001", "PI001",
        "purchase.request.approval", "PR202606240001", 1001L, "APPROVE", "同意", Map.of());
    RecordingTaskActionHandler handler = new RecordingTaskActionHandler();
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository, List.of(handler));

    service.approve(new WorkflowTaskHandleCommand("tenant_a", "TASK001", 1001L,
        "同意", Map.of()));

    assertEquals("purchase.request.approval", handler.lastContext.getProcessKey());
    assertEquals("PR202606240001", handler.lastContext.getBusinessKey());
    assertEquals("APPROVE", handler.lastContext.getAction());
  }

  /**
   * 验证可查询当前用户的任务详情和审批历史，支撑后台管理端与 uni-app 审批详情页。
   */
  @Test
  void shouldGetTaskDetailWithApprovalRecords() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    repository.taskDetail = new WorkflowTaskDetailResponse("tenant_a", "TASK001", "PI001",
        "purchase.request.approval", "采购审批", "PR202606240001", 1001L, "TODO",
        LocalDateTime.of(2026, 6, 24, 10, 0),
        List.of(new WorkflowApprovalRecordItem("TASK000", 1000L, "SUBMIT", "提交采购申请",
            LocalDateTime.of(2026, 6, 24, 9, 30))));
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository, List.of());

    WorkflowTaskDetailResponse detail = service.getTaskDetail("tenant_a", "TASK001", 1001L);

    assertEquals("purchase.request.approval", detail.getProcessKey());
    assertEquals("采购审批", detail.getTaskName());
    assertEquals(1, detail.getApprovalRecords().size());
    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("TASK001", repository.lastTaskId);
    assertEquals(1001L, repository.lastAssigneeUserId);
  }

  /**
   * 验证任务不存在或当前用户无权访问时，任务详情查询返回明确的中文业务异常。
   */
  @Test
  void shouldRejectMissingTaskDetail() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    DefaultWorkflowTaskService service = new DefaultWorkflowTaskService(repository, List.of());

    BusinessException exception = assertWorkflowBusinessException(
        () -> service.getTaskDetail("tenant_a", "TASK404", 1001L),
        "工作流任务不存在或无权访问");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("TASK404", repository.lastTaskId);
    assertEquals(1001L, repository.lastAssigneeUserId);
  }

  /**
   * 断言工作流服务边界返回稳定业务异常。
   *
   * @param executable 待执行的服务动作
   * @param message 期望的中文错误消息
   * @return 捕获到的业务异常
   */
  private BusinessException assertWorkflowBusinessException(Executable executable, String message) {
    BusinessException exception = assertThrows(BusinessException.class, executable);
    assertEquals("ZHYC_WORKFLOW_ARGUMENT_INVALID", exception.getCode());
    assertEquals(message, exception.getMessage());
    return exception;
  }

  /**
   * 记录型工作流运行仓储，用于验证服务层传参与动作类型。
   */
  private static class RecordingWorkflowRuntimeRepository implements WorkflowRuntimeRepository {

    /** 待办任务集合。 */
    private final List<WorkflowTaskTodoItem> todoTasks = new ArrayList<>();
    /** 已办任务集合。 */
    private final List<WorkflowTaskTodoItem> doneTasks = new ArrayList<>();
    /** 发起流程集合。 */
    private final List<WorkflowStartedProcessItem> startedProcesses = new ArrayList<>();
    /** 抄送任务集合。 */
    private final List<WorkflowCcTaskItem> ccTasks = new ArrayList<>();
    /** 流程监控集合。 */
    private final List<WorkflowProcessMonitorItem> monitoredProcesses = new ArrayList<>();
    /** 最近一次租户业务编码。 */
    private String lastTenantId;
    /** 最近一次处理人用户 ID。 */
    private Long lastAssigneeUserId;
    /** 最近一次任务动作。 */
    private String lastAction;
    /** 最近一次任务 ID。 */
    private String lastTaskId;
    /** 最近一次流程实例 ID。 */
    private String lastProcessInstanceId;
    /** 最近一次处理意见。 */
    private String lastComment;
    /** 最近一次任务处理上下文。 */
    private WorkflowTaskActionContext lastContext;
    /** 当前测试任务详情。 */
    private WorkflowTaskDetailResponse taskDetail;

    @Override
    public void startProcess(String tenantId, String processInstanceId, String processKey,
        String businessKey, Long starterUserId, String firstTaskId, String firstTaskName,
        Long firstTaskAssigneeUserId) {
    }

    @Override
    public List<WorkflowTaskTodoItem> findTodoTasks(String tenantId, Long assigneeUserId) {
      this.lastTenantId = tenantId;
      this.lastAssigneeUserId = assigneeUserId;
      return todoTasks;
    }

    @Override
    public List<WorkflowTaskTodoItem> findDoneTasks(String tenantId, Long operatorUserId) {
      this.lastTenantId = tenantId;
      this.lastAssigneeUserId = operatorUserId;
      return doneTasks;
    }

    /**
     * 返回当前测试发起流程集合。
     *
     * @param tenantId 租户业务编码
     * @param starterUserId 发起人用户 ID
     * @return 发起流程集合
     */
    @Override
    public List<WorkflowStartedProcessItem> findStartedProcesses(String tenantId,
        Long starterUserId) {
      this.lastTenantId = tenantId;
      this.lastAssigneeUserId = starterUserId;
      return startedProcesses;
    }

    /**
     * 返回当前测试抄送任务集合。
     *
     * @param tenantId 租户业务编码
     * @param receiverId 抄送接收人用户 ID
     * @return 抄送任务集合
     */
    @Override
    public List<WorkflowCcTaskItem> findCcTasks(String tenantId, Long receiverId) {
      this.lastTenantId = tenantId;
      this.lastAssigneeUserId = receiverId;
      return ccTasks;
    }

    /**
     * 返回当前测试流程监控集合。
     *
     * @param tenantId 租户业务编码
     * @return 流程监控集合
     */
    @Override
    public List<WorkflowProcessMonitorItem> findMonitoredProcesses(String tenantId) {
      this.lastTenantId = tenantId;
      return monitoredProcesses;
    }

    @Override
    public WorkflowTaskDetailResponse findTaskDetail(String tenantId, String taskId,
        Long assigneeUserId) {
      this.lastTenantId = tenantId;
      this.lastTaskId = taskId;
      this.lastAssigneeUserId = assigneeUserId;
      return taskDetail;
    }

    @Override
    public WorkflowTaskActionContext handleTask(String tenantId, String taskId, Long operatorUserId, String action,
        String comment, Map<String, Object> variables) {
      this.lastTenantId = tenantId;
      this.lastAction = action;
      this.lastComment = comment;
      return lastContext;
    }

    @Override
    public void revokeProcess(String tenantId, String processInstanceId, Long operatorUserId,
        String reason) {
      this.lastTenantId = tenantId;
      this.lastProcessInstanceId = processInstanceId;
      this.lastAction = "REVOKE";
      this.lastComment = reason;
    }
  }

  /**
   * 记录型任务动作处理器。
   */
  private static class RecordingTaskActionHandler implements WorkflowTaskActionHandler {

    /** 最近一次任务动作上下文。 */
    private WorkflowTaskActionContext lastContext;

    @Override
    public void handle(WorkflowTaskActionContext context) {
      this.lastContext = context;
    }
  }
}
