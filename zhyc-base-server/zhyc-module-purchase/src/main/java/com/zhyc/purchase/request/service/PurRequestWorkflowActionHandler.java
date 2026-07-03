/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.workflow.WorkflowTaskActionContext;
import com.zhyc.common.workflow.WorkflowTaskActionHandler;
import com.zhyc.purchase.request.repository.PurRequestRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * 采购申请工作流动作处理器。
 */
@Component
public class PurRequestWorkflowActionHandler implements WorkflowTaskActionHandler {

  /** 采购申请审批流程定义 key。 */
  private static final String PROCESS_KEY = "purchase.request.approval";
  /** 工作流审批通过动作。 */
  private static final String ACTION_APPROVE = "APPROVE";
  /** 工作流驳回动作。 */
  private static final String ACTION_REJECT = "REJECT";
  /** 采购申请已通过状态。 */
  private static final String STATUS_APPROVED = "APPROVED";
  /** 采购申请已驳回状态。 */
  private static final String STATUS_REJECTED = "REJECTED";
  /** 工作流任务动作上下文不能为空错误码。 */
  private static final String ERROR_CONTEXT_REQUIRED = "ZHYC_PUR_REQUEST_WORKFLOW_CONTEXT_REQUIRED";
  /** 采购申请工作流动作不支持错误码。 */
  private static final String ERROR_ACTION_UNSUPPORTED =
      "ZHYC_PUR_REQUEST_WORKFLOW_ACTION_UNSUPPORTED";

  /** 采购申请仓储。 */
  private final PurRequestRepository purRequestRepository;

  /**
   * 创建采购申请工作流动作处理器。
   *
   * @param purRequestRepository 采购申请仓储
   */
  public PurRequestWorkflowActionHandler(PurRequestRepository purRequestRepository) {
    this.purRequestRepository = Objects.requireNonNull(purRequestRepository,
        "采购申请仓储不能为空");
  }

  @Override
  public void handle(WorkflowTaskActionContext context) {
    WorkflowTaskActionContext requiredContext = requireContext(context);
    if (!PROCESS_KEY.equals(requiredContext.getProcessKey())) {
      return;
    }
    String status = resolveStatus(requiredContext.getAction());
    purRequestRepository.updateProcessStatus(requiredContext.getTenantId(),
        requiredContext.getBusinessKey(), status, LocalDateTime.now());
  }

  /**
   * 将工作流动作转换为采购申请流程状态。
   *
   * @param action 工作流动作
   * @return 采购申请流程状态
   */
  private String resolveStatus(String action) {
    if (ACTION_APPROVE.equals(action)) {
      return STATUS_APPROVED;
    }
    if (ACTION_REJECT.equals(action)) {
      return STATUS_REJECTED;
    }
    throw new BusinessException(ERROR_ACTION_UNSUPPORTED, "采购申请工作流动作不支持: " + action);
  }

  /**
   * 校验工作流任务动作上下文不能为空。
   *
   * @param context 工作流任务动作上下文
   * @return 校验后的工作流任务动作上下文
   */
  private WorkflowTaskActionContext requireContext(WorkflowTaskActionContext context) {
    if (context == null) {
      throw new BusinessException(ERROR_CONTEXT_REQUIRED, "工作流任务动作上下文不能为空");
    }
    return context;
  }
}
