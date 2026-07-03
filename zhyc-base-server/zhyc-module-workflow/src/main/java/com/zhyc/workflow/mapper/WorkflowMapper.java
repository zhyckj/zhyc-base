/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.mapper;

import com.zhyc.common.workflow.WorkflowTaskActionContext;
import com.zhyc.workflow.service.WorkflowCcTaskItem;
import com.zhyc.workflow.service.WorkflowProcessMonitorItem;
import com.zhyc.workflow.service.WorkflowStartedProcessItem;
import com.zhyc.workflow.service.WorkflowTaskTodoItem;
import com.zhyc.workflow.service.WorkflowApprovalRecordItem;
import com.zhyc.workflow.service.WorkflowTaskDetailResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 工作流运行 MyBatis Mapper。
 */
@Mapper
public interface WorkflowMapper {

  /**
   * 写入流程实例。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param processKey 流程定义 key
   * @param businessKey 业务对象唯一标识
   * @param starterUserId 流程发起人用户 ID
   */
  @InsertProvider(type = WorkflowSqlProvider.class, method = "insertProcessInstance")
  void insertProcessInstance(@Param("tenantId") String tenantId,
                             @Param("processInstanceId") String processInstanceId,
                             @Param("processKey") String processKey,
                             @Param("businessKey") String businessKey,
                             @Param("starterUserId") Long starterUserId);

  /**
   * 写入首个待办任务。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param processInstanceId 流程实例 ID
   * @param taskName 任务名称
   * @param businessKey 业务对象唯一标识
   * @param assigneeUserId 任务处理人用户 ID
   */
  @InsertProvider(type = WorkflowSqlProvider.class, method = "insertTask")
  void insertTask(@Param("tenantId") String tenantId, @Param("taskId") String taskId,
                  @Param("processInstanceId") String processInstanceId,
                  @Param("taskName") String taskName, @Param("businessKey") String businessKey,
                  @Param("assigneeUserId") Long assigneeUserId);

  /**
   * 查询当前用户待办任务。
   *
   * @param tenantId 租户业务编码
   * @param assigneeUserId 任务处理人用户 ID
   * @return 待办任务列表
   */
  @SelectProvider(type = WorkflowSqlProvider.class, method = "selectTodoTasks")
  List<WorkflowTaskTodoItem> selectTodoTasks(@Param("tenantId") String tenantId,
                                             @Param("assigneeUserId") Long assigneeUserId);

  /**
   * 查询当前用户已办任务。
   *
   * @param tenantId 租户业务编码
   * @param operatorUserId 操作用户 ID
   * @return 已办任务列表
   */
  @SelectProvider(type = WorkflowSqlProvider.class, method = "selectDoneTasks")
  List<WorkflowTaskTodoItem> selectDoneTasks(@Param("tenantId") String tenantId,
                                             @Param("operatorUserId") Long operatorUserId);

  /**
   * 查询当前用户发起的流程实例。
   *
   * @param tenantId 租户业务编码
   * @param starterUserId 流程发起人用户 ID
   * @return 发起流程列表
   */
  @SelectProvider(type = WorkflowSqlProvider.class, method = "selectStartedProcesses")
  List<WorkflowStartedProcessItem> selectStartedProcesses(@Param("tenantId") String tenantId,
                                                          @Param("starterUserId") Long starterUserId);

  /**
   * 查询当前用户收到的抄送任务。
   *
   * @param tenantId 租户业务编码
   * @param receiverId 抄送接收人用户 ID
   * @return 抄送任务列表
   */
  @SelectProvider(type = WorkflowSqlProvider.class, method = "selectCcTasks")
  List<WorkflowCcTaskItem> selectCcTasks(@Param("tenantId") String tenantId,
                                         @Param("receiverId") Long receiverId);

  /**
   * 查询租户下全部流程实例监控列表。
   *
   * @param tenantId 租户业务编码
   * @return 流程监控列表
   */
  @SelectProvider(type = WorkflowSqlProvider.class, method = "selectMonitoredProcesses")
  List<WorkflowProcessMonitorItem> selectMonitoredProcesses(@Param("tenantId") String tenantId);

  /**
   * 查询当前用户可访问的任务详情。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param assigneeUserId 当前处理人用户 ID
   * @return 任务详情
   */
  @SelectProvider(type = WorkflowSqlProvider.class, method = "selectTaskDetail")
  WorkflowTaskDetailResponse selectTaskDetail(@Param("tenantId") String tenantId,
                                              @Param("taskId") String taskId,
                                              @Param("assigneeUserId") Long assigneeUserId);

  /**
   * 查询流程实例审批记录。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @return 审批记录列表
   */
  @SelectProvider(type = WorkflowSqlProvider.class, method = "selectApprovalRecords")
  List<WorkflowApprovalRecordItem> selectApprovalRecords(@Param("tenantId") String tenantId,
                                                        @Param("processInstanceId") String processInstanceId);

  /**
   * 查询任务动作上下文。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param operatorUserId 操作用户 ID
   * @param action 任务处理动作
   * @param comment 审批意见
   * @param variables 任务处理变量
   * @return 任务动作上下文
   */
  @SelectProvider(type = WorkflowSqlProvider.class, method = "selectTaskActionContext")
  WorkflowTaskActionContext selectTaskActionContext(@Param("tenantId") String tenantId,
                                                    @Param("taskId") String taskId,
                                                    @Param("operatorUserId") Long operatorUserId,
                                                    @Param("action") String action,
                                                    @Param("comment") String comment,
                                                    @Param("variables") Map<String, Object> variables);

  /**
   * 更新任务处理状态。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param operatorUserId 当前处理人用户 ID
   * @param status 处理后的任务状态
   * @param completedAt 任务完成时间
   */
  @UpdateProvider(type = WorkflowSqlProvider.class, method = "updateTaskHandled")
  void updateTaskHandled(@Param("tenantId") String tenantId, @Param("taskId") String taskId,
                         @Param("operatorUserId") Long operatorUserId,
                         @Param("status") String status,
                         @Param("completedAt") LocalDateTime completedAt);

  /**
   * 更新流程实例为已撤回。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param endedAt 流程结束时间
   */
  @UpdateProvider(type = WorkflowSqlProvider.class, method = "updateProcessRevoked")
  void updateProcessRevoked(@Param("tenantId") String tenantId,
                            @Param("processInstanceId") String processInstanceId,
                            @Param("endedAt") LocalDateTime endedAt);

  /**
   * 更新流程实例下未完成任务为指定状态。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param status 任务状态
   * @param completedAt 任务完成时间
   */
  @UpdateProvider(type = WorkflowSqlProvider.class, method = "updateProcessTodoTasksHandled")
  void updateProcessTodoTasksHandled(@Param("tenantId") String tenantId,
                                     @Param("processInstanceId") String processInstanceId,
                                     @Param("status") String status,
                                     @Param("completedAt") LocalDateTime completedAt);

  /**
   * 写入审批记录。
   *
   * @param tenantId 租户业务编码
   * @param taskId 任务 ID
   * @param operatorUserId 操作用户 ID
   * @param action 审批动作
   * @param comment 审批意见
   */
  @InsertProvider(type = WorkflowSqlProvider.class, method = "insertApprovalRecord")
  void insertApprovalRecord(@Param("tenantId") String tenantId, @Param("taskId") String taskId,
                            @Param("operatorUserId") Long operatorUserId,
                            @Param("action") String action, @Param("comment") String comment);

  /**
   * 写入流程级审批记录。
   *
   * @param tenantId 租户业务编码
   * @param processInstanceId 流程实例 ID
   * @param operatorUserId 操作用户 ID
   * @param action 审批动作
   * @param comment 审批意见
   */
  @InsertProvider(type = WorkflowSqlProvider.class, method = "insertProcessApprovalRecord")
  void insertProcessApprovalRecord(@Param("tenantId") String tenantId,
                                   @Param("processInstanceId") String processInstanceId,
                                   @Param("operatorUserId") Long operatorUserId,
                                   @Param("action") String action,
                                   @Param("comment") String comment);
}
