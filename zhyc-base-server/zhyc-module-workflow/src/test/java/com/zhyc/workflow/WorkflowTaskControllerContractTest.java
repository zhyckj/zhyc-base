/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhyc.workflow.controller.WorkflowTaskController;
import com.zhyc.workflow.service.WorkflowTaskDetailResponse;
import com.zhyc.workflow.service.WorkflowTaskHandleCommand;
import com.zhyc.workflow.service.WorkflowProcessRevokeCommand;
import com.zhyc.workflow.service.WorkflowProcessMonitorItem;
import com.zhyc.workflow.service.WorkflowCcTaskItem;
import com.zhyc.workflow.service.WorkflowStartedProcessItem;
import com.zhyc.workflow.service.WorkflowTaskService;
import com.zhyc.workflow.service.WorkflowTaskTodoItem;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 工作流任务后台接口契约测试。
 */
class WorkflowTaskControllerContractTest {

  /**
   * 验证工作流任务接口路径和权限编码稳定，便于后台管理端和 uni-app 复用。
   *
   * @throws NoSuchMethodException 反射方法不存在时抛出
   */
  @Test
  void shouldExposeTodoDoneApproveRejectAndRevokeEndpoints() throws NoSuchMethodException {
    RequestMapping requestMapping = WorkflowTaskController.class.getAnnotation(RequestMapping.class);
    assertEquals("/workflow/tasks", requestMapping.value()[0]);

    Method todoMethod = WorkflowTaskController.class.getMethod("todo", String.class, Long.class);
    assertEquals("", todoMethod.getAnnotation(GetMapping.class).value()[0]);
    assertTrue(hasPermission(todoMethod, "workflow:task:todo"));

    Method detailMethod = WorkflowTaskController.class.getMethod("detail", String.class,
        String.class, Long.class);
    assertEquals("/{taskId}", detailMethod.getAnnotation(GetMapping.class).value()[0]);
    assertTrue(hasPermission(detailMethod, "workflow:task:detail"));

    Method approveMethod = WorkflowTaskController.class.getMethod("approve", String.class,
        WorkflowTaskHandleCommand.class, String.class, Long.class);
    assertEquals("/{taskId}/approve", approveMethod.getAnnotation(PostMapping.class).value()[0]);
    assertTrue(hasPermission(approveMethod, "workflow:task:approve"));

    Method rejectMethod = WorkflowTaskController.class.getMethod("reject", String.class,
        WorkflowTaskHandleCommand.class, String.class, Long.class);
    assertEquals("/{taskId}/reject", rejectMethod.getAnnotation(PostMapping.class).value()[0]);
    assertTrue(hasPermission(rejectMethod, "workflow:task:reject"));

    Method doneMethod = WorkflowTaskController.class.getMethod("done", String.class, Long.class);
    assertEquals("/done", doneMethod.getAnnotation(GetMapping.class).value()[0]);
    assertTrue(hasPermission(doneMethod, "workflow:task:done"));

    Method startedMethod = WorkflowTaskController.class.getMethod("started", String.class,
        Long.class);
    assertEquals("/started", startedMethod.getAnnotation(GetMapping.class).value()[0]);
    assertTrue(hasPermission(startedMethod, "workflow:task:started"));

    Method ccMethod = WorkflowTaskController.class.getMethod("cc", String.class, Long.class);
    assertEquals("/cc", ccMethod.getAnnotation(GetMapping.class).value()[0]);
    assertTrue(hasPermission(ccMethod, "workflow:task:cc"));

    Method monitorMethod = WorkflowTaskController.class.getMethod("monitor", String.class);
    assertEquals("/monitor", monitorMethod.getAnnotation(GetMapping.class).value()[0]);
    assertTrue(hasPermission(monitorMethod, "workflow:task:monitor"));

    Method revokeMethod = WorkflowTaskController.class.getMethod("revoke", String.class,
        WorkflowProcessRevokeCommand.class, String.class, Long.class);
    assertEquals("/process-instances/{processInstanceId}/revoke",
        revokeMethod.getAnnotation(PostMapping.class).value()[0]);
    assertTrue(hasPermission(revokeMethod, "workflow:task:revoke"));
  }

  /**
   * 验证审批空请求体会合并路径任务、租户和操作人，避免接口空 body 触发空指针。
   */
  @Test
  void shouldMergeNullApproveCommandWithPathAndHeaders() {
    RecordingTaskService taskService = new RecordingTaskService();
    WorkflowTaskController controller = new WorkflowTaskController(taskService);

    controller.approve("task-001", null, "tenant-a", 1001L);

    WorkflowTaskHandleCommand command = taskService.approveCommand;
    assertEquals("tenant-a", command.getTenantId());
    assertEquals("task-001", command.getTaskId());
    assertEquals(1001L, command.getOperatorUserId());
    assertNull(command.getComment());
    assertNull(command.getVariables());
  }

  /**
   * 验证驳回空请求体会合并路径任务、租户和操作人，保持任务处理接口兼容。
   */
  @Test
  void shouldMergeNullRejectCommandWithPathAndHeaders() {
    RecordingTaskService taskService = new RecordingTaskService();
    WorkflowTaskController controller = new WorkflowTaskController(taskService);

    controller.reject("task-002", null, "tenant-b", 1002L);

    WorkflowTaskHandleCommand command = taskService.rejectCommand;
    assertEquals("tenant-b", command.getTenantId());
    assertEquals("task-002", command.getTaskId());
    assertEquals(1002L, command.getOperatorUserId());
    assertNull(command.getComment());
    assertNull(command.getVariables());
  }

  /**
   * 验证撤回空请求体会合并路径流程实例、租户和操作人，保持撤回接口兼容。
   */
  @Test
  void shouldMergeNullRevokeCommandWithPathAndHeaders() {
    RecordingTaskService taskService = new RecordingTaskService();
    WorkflowTaskController controller = new WorkflowTaskController(taskService);

    controller.revoke("proc-001", null, "tenant-c", 1003L);

    WorkflowProcessRevokeCommand command = taskService.revokeCommand;
    assertEquals("tenant-c", command.getTenantId());
    assertEquals("proc-001", command.getProcessInstanceId());
    assertEquals(1003L, command.getOperatorUserId());
    assertNull(command.getReason());
  }

  /**
   * 验证我发起的和抄送我的接口会把租户、当前用户传给服务层。
   */
  @Test
  void shouldExposeStartedAndCcTaskContracts() {
    RecordingTaskService taskService = new RecordingTaskService();
    WorkflowTaskController controller = new WorkflowTaskController(taskService);

    controller.started("tenant-start", 2001L);
    assertEquals("tenant-start", taskService.lastTenantId);
    assertEquals(2001L, taskService.lastUserId);
    assertEquals("started", taskService.lastQueryType);

    controller.cc("tenant-cc", 2002L);

    assertEquals("tenant-cc", taskService.lastTenantId);
    assertEquals(2002L, taskService.lastUserId);
    assertEquals("cc", taskService.lastQueryType);
  }

  /**
   * 验证流程监控接口只按租户查询流程实例，避免后台运维误绑定当前用户。
   */
  @Test
  void shouldExposeProcessMonitorContract() {
    RecordingTaskService taskService = new RecordingTaskService();
    WorkflowTaskController controller = new WorkflowTaskController(taskService);

    controller.monitor("tenant-monitor");

    assertEquals("tenant-monitor", taskService.lastTenantId);
    assertEquals("monitor", taskService.lastQueryType);
    assertNull(taskService.lastUserId);
  }

  /**
   * 判断方法是否声明指定权限编码。
   *
   * @param method 接口方法
   * @param permission 权限编码
   * @return 已声明时返回 {@code true}
   */
  private boolean hasPermission(Method method, String permission) {
    RequiresPermissions annotation = method.getAnnotation(RequiresPermissions.class);
    return annotation != null && annotation.value().length == 1
        && permission.equals(annotation.value()[0]);
  }

  /**
   * 记录工作流任务服务入参的测试桩。
   */
  private static final class RecordingTaskService implements WorkflowTaskService {

    /** 最近一次审批命令。 */
    private WorkflowTaskHandleCommand approveCommand;
    /** 最近一次驳回命令。 */
    private WorkflowTaskHandleCommand rejectCommand;
    /** 最近一次撤回命令。 */
    private WorkflowProcessRevokeCommand revokeCommand;
    /** 最近一次查询租户。 */
    private String lastTenantId;
    /** 最近一次查询用户。 */
    private Long lastUserId;
    /** 最近一次查询类型。 */
    private String lastQueryType;

    /**
     * 返回空待办列表。
     *
     * @param tenantId 租户业务编码
     * @param assigneeUserId 任务处理人用户 ID
     * @return 空待办列表
     */
    @Override
    public List<WorkflowTaskTodoItem> listTodoTasks(String tenantId, Long assigneeUserId) {
      return List.of();
    }

    /**
     * 返回空已办列表。
     *
     * @param tenantId 租户业务编码
     * @param operatorUserId 当前操作用户 ID
     * @return 空已办列表
     */
    @Override
    public List<WorkflowTaskTodoItem> listDoneTasks(String tenantId, Long operatorUserId) {
      return List.of();
    }

    /**
     * 记录发起流程查询入参。
     *
     * @param tenantId 租户业务编码
     * @param starterUserId 流程发起人用户 ID
     * @return 空发起流程列表
     */
    @Override
    public List<WorkflowStartedProcessItem> listStartedProcesses(String tenantId,
        Long starterUserId) {
      this.lastTenantId = tenantId;
      this.lastUserId = starterUserId;
      this.lastQueryType = "started";
      return List.of();
    }

    /**
     * 记录抄送任务查询入参。
     *
     * @param tenantId 租户业务编码
     * @param receiverId 抄送接收人用户 ID
     * @return 空抄送任务列表
     */
    @Override
    public List<WorkflowCcTaskItem> listCcTasks(String tenantId, Long receiverId) {
      this.lastTenantId = tenantId;
      this.lastUserId = receiverId;
      this.lastQueryType = "cc";
      return List.of();
    }

    /**
     * 记录流程监控查询入参。
     *
     * @param tenantId 租户业务编码
     * @return 空流程监控列表
     */
    @Override
    public List<WorkflowProcessMonitorItem> listMonitoredProcesses(String tenantId) {
      this.lastTenantId = tenantId;
      this.lastUserId = null;
      this.lastQueryType = "monitor";
      return List.of();
    }

    /**
     * 返回空任务详情。
     *
     * @param tenantId 租户业务编码
     * @param taskId 任务 ID
     * @param assigneeUserId 当前处理人用户 ID
     * @return 空任务详情
     */
    @Override
    public WorkflowTaskDetailResponse getTaskDetail(String tenantId, String taskId,
        Long assigneeUserId) {
      return null;
    }

    /**
     * 记录审批命令。
     *
     * @param command 任务处理命令
     */
    @Override
    public void approve(WorkflowTaskHandleCommand command) {
      this.approveCommand = command;
    }

    /**
     * 记录驳回命令。
     *
     * @param command 任务处理命令
     */
    @Override
    public void reject(WorkflowTaskHandleCommand command) {
      this.rejectCommand = command;
    }

    /**
     * 记录撤回命令。
     *
     * @param command 流程撤回命令
     */
    @Override
    public void revoke(WorkflowProcessRevokeCommand command) {
      this.revokeCommand = command;
    }
  }
}
