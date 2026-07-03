/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.workflow.WorkflowTaskActionContext;
import com.zhyc.purchase.request.repository.PurRequestRepository;
import com.zhyc.purchase.request.service.PurRequestWorkflowActionHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 采购申请工作流动作处理器测试。
 */
class PurRequestWorkflowActionHandlerTest {

  /**
   * 验证采购申请审批通过后会回写为已通过状态。
   */
  @Test
  void shouldMarkPurchaseRequestApprovedWhenWorkflowApproved() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    PurRequestWorkflowActionHandler handler = new PurRequestWorkflowActionHandler(repository);

    handler.handle(new WorkflowTaskActionContext("tenant_a", "TASK001", "PI001",
        "purchase.request.approval", "PR202606240001", 1001L, "APPROVE", "同意", Map.of()));

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals("PR202606240001", repository.lastRequestNo);
    assertEquals("APPROVED", repository.lastStatus);
  }

  /**
   * 验证采购申请被驳回后会回写为已驳回状态。
   */
  @Test
  void shouldMarkPurchaseRequestRejectedWhenWorkflowRejected() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    PurRequestWorkflowActionHandler handler = new PurRequestWorkflowActionHandler(repository);

    handler.handle(new WorkflowTaskActionContext("tenant_a", "TASK001", "PI001",
        "purchase.request.approval", "PR202606240001", 1001L, "REJECT", "预算不足", Map.of()));

    assertEquals("REJECTED", repository.lastStatus);
  }

  /**
   * 验证非采购申请流程不会影响采购申请状态。
   */
  @Test
  void shouldIgnoreOtherProcessKeys() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    PurRequestWorkflowActionHandler handler = new PurRequestWorkflowActionHandler(repository);

    handler.handle(new WorkflowTaskActionContext("tenant_a", "TASK001", "PI001",
        "other.process", "PR202606240001", 1001L, "APPROVE", "同意", Map.of()));

    assertEquals(0, repository.updateCount);
  }

  /**
   * 验证采购申请流程收到未知工作流动作时返回中文异常，避免审批状态被静默遗漏。
   */
  @Test
  void shouldRejectUnknownPurchaseWorkflowAction() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    PurRequestWorkflowActionHandler handler = new PurRequestWorkflowActionHandler(repository);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> handler.handle(new WorkflowTaskActionContext("tenant_a", "TASK001", "PI001",
            "purchase.request.approval", "PR202606240001", 1001L, "TRANSFER", "转交", Map.of())));

    assertEquals("ZHYC_PUR_REQUEST_WORKFLOW_ACTION_UNSUPPORTED", exception.getCode());
    assertEquals("采购申请工作流动作不支持: TRANSFER", exception.getMessage());
    assertEquals(0, repository.updateCount);
  }

  /**
   * 记录型采购申请仓储。
   */
  private static class RecordingPurRequestRepository implements PurRequestRepository {

    /** 最近一次租户业务编码。 */
    private String lastTenantId;
    /** 最近一次采购申请单号。 */
    private String lastRequestNo;
    /** 最近一次采购申请状态。 */
    private String lastStatus;
    /** 状态更新次数。 */
    private int updateCount;

    @Override
    public Optional<com.zhyc.purchase.request.domain.PurRequest> findByTenantIdAndRequestNo(
        String tenantId, String requestNo) {
      return Optional.empty();
    }

    @Override
    public void save(com.zhyc.purchase.request.domain.PurRequest purRequest) {
    }

    @Override
    public void updateSubmitted(String tenantId, String requestNo, String processInstanceId,
        String processStatus, LocalDateTime submittedAt) {
    }

    @Override
    public long countByTenantIdAndProcessStatus(String tenantId, String processStatus) {
      return 0;
    }

    @Override
    public List<com.zhyc.purchase.request.domain.PurRequest> findPageByTenantIdAndProcessStatus(
        String tenantId, String processStatus, long offset, int pageSize) {
      return List.of();
    }

    @Override
    public void updateProcessStatus(String tenantId, String requestNo, String processStatus,
        LocalDateTime updatedAt) {
      this.lastTenantId = tenantId;
      this.lastRequestNo = requestNo;
      this.lastStatus = processStatus;
      this.updateCount++;
    }
  }
}
