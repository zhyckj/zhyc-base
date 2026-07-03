/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.workflow.WorkflowTaskActionContext;
import com.zhyc.workflow.repository.WorkflowRuntimeRepository;
import com.zhyc.workflow.service.WorkflowCcTaskItem;
import com.zhyc.workflow.service.WorkflowProcessMonitorItem;
import com.zhyc.workflow.service.WorkflowStartedProcessItem;
import com.zhyc.workflow.service.WorkflowTaskDetailResponse;
import com.zhyc.workflow.service.WorkflowTaskTodoItem;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * 本地工作流服务测试。
 */
class LocalWorkflowServiceTest {

  /**
   * 验证启动流程时会返回稳定的本地流程实例 ID，便于首期业务模块完成流程门面对接。
   */
  @Test
  void shouldStartLocalProcessWithStableInstanceId() {
    LocalWorkflowService service = new LocalWorkflowService();

    String processInstanceId = service.startProcess("purchase.request.approval",
        "PR202606240001", Map.of("tenantId", "tenant_a"));

    assertEquals("local-purchase_request_approval-PR202606240001", processInstanceId);
  }

  /**
   * 验证启动流程时会创建流程实例和首个待办任务，支撑后续待办查询。
   */
  @Test
  void shouldCreateRuntimeRecordsWhenStartingProcess() {
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    LocalWorkflowService service = new LocalWorkflowService(repository);

    String processInstanceId = service.startProcess("purchase.request.approval",
        "PR202606240001", Map.of("tenantId", "tenant_a", "starterUserId", 1001L));

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(processInstanceId, repository.lastProcessInstanceId);
    assertEquals("purchase.request.approval", repository.lastProcessKey);
    assertEquals("PR202606240001", repository.lastBusinessKey);
    assertEquals(1001L, repository.lastStarterUserId);
    assertEquals("task-" + processInstanceId, repository.lastTaskId);
    assertEquals("发起审批", repository.lastTaskName);
    assertEquals(1001L, repository.lastTaskAssigneeUserId);
  }

  /**
   * 验证流程处理动作先完成门面参数校验，后续可无损替换为真实流程引擎适配器。
   */
  @Test
  void shouldAcceptValidTaskOperations() {
    LocalWorkflowService service = new LocalWorkflowService();

    assertDoesNotThrow(() -> service.approve("TASK001", "同意", null));
    assertDoesNotThrow(() -> service.reject("TASK001", "资料不完整"));
    assertDoesNotThrow(() -> service.revoke("PI001", "申请人撤回"));
  }

  /**
   * 验证关键标识不能为空，避免业务模块生成不可追踪的流程实例。
   */
  @Test
  void shouldRejectBlankProcessIdentifiers() {
    LocalWorkflowService service = new LocalWorkflowService();

    assertWorkflowBusinessException(() -> service.startProcess(" ", "PR202606240001", Map.of()),
        "流程定义 key 不能为空");
    assertWorkflowBusinessException(() -> service.startProcess("purchase.request.approval", " ", Map.of()),
        "业务对象唯一标识不能为空");
    assertWorkflowBusinessException(() -> service.approve(" ", "同意", Map.of()),
        "待审批任务 ID 不能为空");
    assertWorkflowBusinessException(() -> service.reject(" ", "驳回"), "待驳回任务 ID 不能为空");
    assertWorkflowBusinessException(() -> service.revoke(" ", "撤回"), "流程实例 ID 不能为空");
  }

  /**
   * 断言本地工作流服务返回统一业务异常。
   *
   * @param executable 待执行服务动作
   * @param message 期望中文错误消息
   */
  private void assertWorkflowBusinessException(Executable executable, String message) {
    BusinessException exception = assertThrows(BusinessException.class, executable);
    assertEquals("ZHYC_WORKFLOW_ARGUMENT_INVALID", exception.getCode());
    assertEquals(message, exception.getMessage());
  }

  /**
   * 记录型工作流运行仓储，用于验证本地工作流启动流程时的落库参数。
   */
  private static class RecordingWorkflowRuntimeRepository implements WorkflowRuntimeRepository {

    /** 最近一次租户业务编码。 */
    private String lastTenantId;
    /** 最近一次流程实例 ID。 */
    private String lastProcessInstanceId;
    /** 最近一次流程定义 key。 */
    private String lastProcessKey;
    /** 最近一次业务对象唯一标识。 */
    private String lastBusinessKey;
    /** 最近一次发起人用户 ID。 */
    private Long lastStarterUserId;
    /** 最近一次任务 ID。 */
    private String lastTaskId;
    /** 最近一次任务名称。 */
    private String lastTaskName;
    /** 最近一次首个任务处理人用户 ID。 */
    private Long lastTaskAssigneeUserId;

    @Override
    public List<WorkflowTaskTodoItem> findTodoTasks(String tenantId, Long assigneeUserId) {
      return List.of();
    }

    @Override
    public List<WorkflowTaskTodoItem> findDoneTasks(String tenantId, Long operatorUserId) {
      return List.of();
    }

    /**
     * 本地工作流测试不关心发起流程查询，返回空集合。
     *
     * @param tenantId 租户业务编码
     * @param starterUserId 发起人用户 ID
     * @return 空发起流程集合
     */
    @Override
    public List<WorkflowStartedProcessItem> findStartedProcesses(String tenantId,
        Long starterUserId) {
      return List.of();
    }

    /**
     * 本地工作流测试不关心抄送任务查询，返回空集合。
     *
     * @param tenantId 租户业务编码
     * @param receiverId 抄送接收人用户 ID
     * @return 空抄送任务集合
     */
    @Override
    public List<WorkflowCcTaskItem> findCcTasks(String tenantId, Long receiverId) {
      return List.of();
    }

    /**
     * 本地工作流测试不关心流程监控查询，返回空集合。
     *
     * @param tenantId 租户业务编码
     * @return 空流程监控集合
     */
    @Override
    public List<WorkflowProcessMonitorItem> findMonitoredProcesses(String tenantId) {
      return List.of();
    }

    @Override
    public WorkflowTaskDetailResponse findTaskDetail(String tenantId, String taskId,
        Long assigneeUserId) {
      return null;
    }

    @Override
    public WorkflowTaskActionContext handleTask(String tenantId, String taskId, Long operatorUserId, String action,
        String comment, Map<String, Object> variables) {
      return null;
    }

    @Override
    public void revokeProcess(String tenantId, String processInstanceId, Long operatorUserId,
        String reason) {
    }

    @Override
    public void startProcess(String tenantId, String processInstanceId, String processKey,
        String businessKey, Long starterUserId, String firstTaskId, String firstTaskName,
        Long firstTaskAssigneeUserId) {
      this.lastTenantId = tenantId;
      this.lastProcessInstanceId = processInstanceId;
      this.lastProcessKey = processKey;
      this.lastBusinessKey = businessKey;
      this.lastStarterUserId = starterUserId;
      this.lastTaskId = firstTaskId;
      this.lastTaskName = firstTaskName;
      this.lastTaskAssigneeUserId = firstTaskAssigneeUserId;
    }
  }
}
