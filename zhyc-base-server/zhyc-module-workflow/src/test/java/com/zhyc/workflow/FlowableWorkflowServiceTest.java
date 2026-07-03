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
import com.zhyc.workflow.repository.WorkflowRuntimeRepository;
import com.zhyc.workflow.service.WorkflowCcTaskItem;
import com.zhyc.workflow.service.WorkflowProcessMonitorItem;
import com.zhyc.workflow.service.WorkflowStartedProcessItem;
import com.zhyc.workflow.service.WorkflowTaskDetailResponse;
import com.zhyc.workflow.service.WorkflowTaskTodoItem;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Flowable 工作流门面适配器测试。
 */
class FlowableWorkflowServiceTest {

  /**
   * 验证启动流程会调用 Flowable RuntimeService，并返回真实流程实例 ID。
   */
  @Test
  void shouldStartProcessThroughFlowableRuntimeService() {
    RecordingFlowableClient client = new RecordingFlowableClient();
    FlowableWorkflowService service = new FlowableWorkflowService(client.runtimeService(), client.taskService());

    String processInstanceId = service.startProcess("purchase.request.approval", "PR202606250001",
        Map.of("tenantId", "tenant_a", "starterUserId", 1001L));

    assertEquals("flowable-pi-001", processInstanceId);
    assertEquals("purchase.request.approval", client.processKey);
    assertEquals("PR202606250001", client.businessKey);
    assertEquals("tenant_a", client.variables.get("tenantId"));
  }

  /**
   * 验证 Flowable 启动流程后会同步平台运行仓储，支撑后台待办、我发起的和流程监控查询。
   */
  @Test
  void shouldSyncRuntimeRepositoryAfterStartingFlowableProcess() {
    RecordingFlowableClient client = new RecordingFlowableClient();
    RecordingWorkflowRuntimeRepository repository = new RecordingWorkflowRuntimeRepository();
    FlowableWorkflowService service = new FlowableWorkflowService(client.runtimeService(), client.taskService(),
        repository);

    String processInstanceId = service.startProcess("purchase.request.approval", "PR202606260001",
        Map.of("tenantId", "tenant_a", "starterUserId", 1001L));

    assertEquals("flowable-pi-001", processInstanceId);
    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("flowable-pi-001", repository.lastProcessInstanceId);
    assertEquals("purchase.request.approval", repository.lastProcessKey);
    assertEquals("PR202606260001", repository.lastBusinessKey);
    assertEquals(1001L, repository.lastStarterUserId);
    assertEquals("flowable-task-001", repository.lastTaskId);
    assertEquals("部门审批", repository.lastTaskName);
    assertEquals(1002L, repository.lastTaskAssigneeUserId);
  }

  /**
   * 验证审批、驳回、撤回会进入 Flowable 任务和流程实例 API。
   */
  @Test
  void shouldHandleTaskOperationsThroughFlowableServices() {
    RecordingFlowableClient client = new RecordingFlowableClient();
    FlowableWorkflowService service = new FlowableWorkflowService(client.runtimeService(), client.taskService());

    service.approve("TASK001", "同意", Map.of("approved", true));
    service.reject("TASK002", "资料不完整");
    service.revoke("PI001", "申请人撤回");

    assertEquals("TASK001", client.completedTaskIds.get("approve"));
    assertEquals(true, client.completedVariables.get("approved"));
    assertEquals("同意", client.comments.get("TASK001"));
    assertEquals("TASK002", client.completedTaskIds.get("reject"));
    assertEquals(false, client.rejectedVariables.get("approved"));
    assertEquals("资料不完整", client.comments.get("TASK002"));
    assertEquals("PI001", client.deletedProcessInstanceId);
    assertEquals("申请人撤回", client.deleteReason);
  }

  /**
   * 验证 Flowable 适配器会把服务边界入参错误转换为稳定业务异常。
   */
  @Test
  void shouldRejectBlankArgumentsWithBusinessException() {
    RecordingFlowableClient client = new RecordingFlowableClient();
    FlowableWorkflowService service = new FlowableWorkflowService(client.runtimeService(), client.taskService());

    assertWorkflowBusinessException(() -> service.startProcess(" ", "PR202606250001", Map.of()),
        "流程定义 key 不能为空");
    assertWorkflowBusinessException(() -> service.startProcess("purchase.request.approval", " ", Map.of()),
        "业务对象唯一标识不能为空");
    assertWorkflowBusinessException(() -> service.approve(" ", "同意", Map.of()),
        "待审批任务 ID 不能为空");
    assertWorkflowBusinessException(() -> service.reject(" ", "驳回"), "待驳回任务 ID 不能为空");
    assertWorkflowBusinessException(() -> service.revoke(" ", "撤回"), "流程实例 ID 不能为空");
  }

  /**
   * 断言 Flowable 工作流门面返回统一业务异常。
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
   * 测试用 Flowable 客户端，记录门面适配器对 Flowable API 的调用参数。
   */
  private static final class RecordingFlowableClient {

    /** 最近一次流程定义 key。 */
    private String processKey;
    /** 最近一次业务唯一键。 */
    private String businessKey;
    /** 最近一次流程变量。 */
    private Map<String, Object> variables = Map.of();
    /** 已完成任务 ID。 */
    private final Map<String, String> completedTaskIds = new LinkedHashMap<>();
    /** 审批通过变量。 */
    private Map<String, Object> completedVariables = Map.of();
    /** 驳回变量。 */
    private Map<String, Object> rejectedVariables = Map.of();
    /** 任务审批意见。 */
    private final Map<String, String> comments = new LinkedHashMap<>();
    /** 删除的流程实例 ID。 */
    private String deletedProcessInstanceId;
    /** 删除流程实例原因。 */
    private String deleteReason;

    /**
     * 创建测试 RuntimeService 代理。
     *
     * @return RuntimeService 代理
     */
    @SuppressWarnings("unchecked")
    private RuntimeService runtimeService() {
      return (RuntimeService) Proxy.newProxyInstance(RuntimeService.class.getClassLoader(),
          new Class<?>[] {RuntimeService.class}, (proxy, method, args) -> {
            if ("startProcessInstanceByKey".equals(method.getName()) && args.length == 3) {
              processKey = String.valueOf(args[0]);
              businessKey = String.valueOf(args[1]);
              variables = Map.copyOf((Map<String, Object>) args[2]);
              return processInstance("flowable-pi-001");
            }
            if ("deleteProcessInstance".equals(method.getName()) && args.length == 2) {
              deletedProcessInstanceId = String.valueOf(args[0]);
              deleteReason = String.valueOf(args[1]);
              return null;
            }
            return defaultValue(method.getReturnType());
          });
    }

    /**
     * 创建测试 TaskService 代理。
     *
     * @return TaskService 代理
     */
    @SuppressWarnings("unchecked")
    private TaskService taskService() {
      return (TaskService) Proxy.newProxyInstance(TaskService.class.getClassLoader(),
          new Class<?>[] {TaskService.class}, (proxy, method, args) -> {
            if ("addComment".equals(method.getName()) && args.length >= 3) {
              comments.put(String.valueOf(args[0]), String.valueOf(args[2]));
              return null;
            }
            if ("complete".equals(method.getName()) && args.length == 2) {
              Map<String, Object> taskVariables = Map.copyOf((Map<String, Object>) args[1]);
              if (Boolean.FALSE.equals(taskVariables.get("approved"))) {
                completedTaskIds.put("reject", String.valueOf(args[0]));
                rejectedVariables = taskVariables;
              } else {
                completedTaskIds.put("approve", String.valueOf(args[0]));
                completedVariables = taskVariables;
              }
              return null;
            }
            if ("createTaskQuery".equals(method.getName()) && args == null) {
              return Proxy.newProxyInstance(method.getReturnType().getClassLoader(),
                  new Class<?>[] {method.getReturnType()}, (queryProxy, queryMethod, queryArgs) -> {
                    if ("processInstanceId".equals(queryMethod.getName()) && queryArgs.length == 1) {
                      return queryProxy;
                    }
                    if ("singleResult".equals(queryMethod.getName()) && queryArgs == null) {
                      return task("flowable-task-001", "部门审批", "1002");
                    }
                    return defaultValue(queryMethod.getReturnType());
                  });
            }
            return defaultValue(method.getReturnType());
          });
    }

    /**
     * 创建测试流程实例代理。
     *
     * @param processInstanceId 流程实例 ID
     * @return 流程实例代理
     */
    private ProcessInstance processInstance(String processInstanceId) {
      return (ProcessInstance) Proxy.newProxyInstance(ProcessInstance.class.getClassLoader(),
          new Class<?>[] {ProcessInstance.class}, (proxy, method, args) -> {
            if ("getProcessInstanceId".equals(method.getName()) || "getId".equals(method.getName())) {
              return processInstanceId;
            }
            return defaultValue(method.getReturnType());
          });
    }

    /**
     * 创建测试任务代理。
     *
     * @param taskId Flowable 任务 ID
     * @param taskName Flowable 任务名称
     * @param assignee Flowable 任务处理人
     * @return 任务代理
     */
    private Task task(String taskId, String taskName, String assignee) {
      return (Task) Proxy.newProxyInstance(Task.class.getClassLoader(), new Class<?>[] {Task.class},
          (proxy, method, args) -> {
            if ("getId".equals(method.getName())) {
              return taskId;
            }
            if ("getName".equals(method.getName())) {
              return taskName;
            }
            if ("getAssignee".equals(method.getName())) {
              return assignee;
            }
            return defaultValue(method.getReturnType());
          });
    }

    /**
     * 返回代理方法的基础默认值。
     *
     * @param returnType 方法返回类型
     * @return 默认返回值
     */
    private Object defaultValue(Class<?> returnType) {
      if (returnType == Boolean.TYPE) {
        return false;
      }
      if (returnType == Integer.TYPE || returnType == Long.TYPE || returnType == Short.TYPE
          || returnType == Byte.TYPE) {
        return 0;
      }
      return null;
    }
  }

  /**
   * 记录型工作流运行仓储，用于验证 Flowable 门面同步平台运行数据。
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

    /**
     * 记录 Flowable 启动流程后同步的平台运行数据。
     *
     * @param tenantId 租户业务编码
     * @param processInstanceId 流程实例 ID
     * @param processKey 流程定义 key
     * @param businessKey 业务对象唯一标识
     * @param starterUserId 流程发起人用户 ID
     * @param firstTaskId 首个任务 ID
     * @param firstTaskName 首个任务名称
     * @param firstTaskAssigneeUserId 首个任务处理人用户 ID
     */
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

    @Override
    public List<WorkflowTaskTodoItem> findTodoTasks(String tenantId, Long assigneeUserId) {
      return List.of();
    }

    @Override
    public List<WorkflowTaskTodoItem> findDoneTasks(String tenantId, Long operatorUserId) {
      return List.of();
    }

    @Override
    public List<WorkflowStartedProcessItem> findStartedProcesses(String tenantId, Long starterUserId) {
      return List.of();
    }

    @Override
    public List<WorkflowCcTaskItem> findCcTasks(String tenantId, Long receiverId) {
      return List.of();
    }

    @Override
    public List<WorkflowProcessMonitorItem> findMonitoredProcesses(String tenantId) {
      return List.of();
    }

    @Override
    public WorkflowTaskDetailResponse findTaskDetail(String tenantId, String taskId, Long assigneeUserId) {
      return null;
    }

    @Override
    public WorkflowTaskActionContext handleTask(String tenantId, String taskId, Long operatorUserId,
        String action, String comment, Map<String, Object> variables) {
      return null;
    }

    @Override
    public void revokeProcess(String tenantId, String processInstanceId, Long operatorUserId, String reason) {
    }
  }
}
