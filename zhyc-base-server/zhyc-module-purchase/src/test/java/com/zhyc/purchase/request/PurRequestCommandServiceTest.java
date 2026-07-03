/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.purchase.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.workflow.WorkflowService;
import com.zhyc.purchase.request.domain.PurRequest;
import com.zhyc.purchase.request.repository.PurRequestRepository;
import com.zhyc.purchase.request.service.DefaultPurRequestCommandService;
import com.zhyc.purchase.request.service.PurRequestCreateCommand;
import com.zhyc.purchase.request.service.PurRequestQuery;
import com.zhyc.purchase.request.service.PurRequestStatusResponse;
import com.zhyc.purchase.request.service.PurRequestSubmitResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 采购申请命令服务测试。
 */
class PurRequestCommandServiceTest {

  /**
   * 验证创建采购申请会按租户保存草稿状态。
   */
  @Test
  void shouldCreateDraftPurchaseRequest() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    RecordingWorkflowService workflowService = new RecordingWorkflowService();
    DefaultPurRequestCommandService service = new DefaultPurRequestCommandService(repository, workflowService);

    String requestNo = service.create(new PurRequestCreateCommand(" tenant_a ", " PR202606240002 ",
        " 办公用品采购 ", 1001L, 2001L, new BigDecimal("2600.00"), " 项目启动物料 "));

    assertEquals("PR202606240002", requestNo);
    assertEquals("tenant_a", repository.saved.getTenantId());
    assertEquals("PR202606240002", repository.saved.getRequestNo());
    assertEquals("办公用品采购", repository.saved.getRequestTitle());
    assertEquals("DRAFT", repository.saved.getProcessStatus());
    assertEquals(0, workflowService.startCount);
  }

  /**
   * 验证提交采购申请会通过工作流门面启动流程，并把申请状态更新为审批中。
   */
  @Test
  void shouldSubmitPurchaseRequestThroughWorkflowFacade() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    repository.queryResult = Optional.of(new PurRequest(11L, "tenant_a", "PR202606240001",
        "办公设备采购", 1001L, 2001L, new BigDecimal("12800.00"), "新员工工位设备", "DRAFT",
        null, LocalDateTime.parse("2026-06-24T09:00:00"), LocalDateTime.parse("2026-06-24T09:30:00")));
    RecordingWorkflowService workflowService = new RecordingWorkflowService();
    DefaultPurRequestCommandService service = new DefaultPurRequestCommandService(repository, workflowService);

    PurRequestSubmitResponse response = service.submit(" tenant_a ", " PR202606240001 ", 1001L);

    assertEquals("PR202606240001", response.getRequestNo());
    assertEquals("wf-pr-001", response.getProcessInstanceId());
    assertEquals("APPROVING", response.getProcessStatus());
    assertEquals("purchase.request.approval", workflowService.lastProcessKey);
    assertEquals("PR202606240001", workflowService.lastBusinessKey);
    assertEquals(1001L, workflowService.lastVariables.get("starterUserId"));
    assertEquals("wf-pr-001", repository.submittedProcessInstanceId);
    assertEquals("APPROVING", repository.submittedStatus);
  }

  /**
   * 验证非草稿采购申请不能重复提交。
   */
  @Test
  void shouldRejectSubmitWhenRequestIsNotDraft() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    repository.queryResult = Optional.of(new PurRequest(11L, "tenant_a", "PR202606240001",
        "办公设备采购", 1001L, 2001L, new BigDecimal("12800.00"), "新员工工位设备", "APPROVING",
        null, LocalDateTime.parse("2026-06-24T09:00:00"), LocalDateTime.parse("2026-06-24T09:30:00")));
    DefaultPurRequestCommandService service = new DefaultPurRequestCommandService(repository,
        new RecordingWorkflowService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.submit("tenant_a", "PR202606240001", 1001L));

    assertEquals("ZHYC_PUR_REQUEST_SUBMIT_STATUS_INVALID", exception.getCode());
    assertEquals("只有草稿状态的采购申请可以提交", exception.getMessage());
  }

  /**
   * 验证采购申请总金额不能小于 0，并返回稳定业务错误码。
   */
  @Test
  void shouldRejectNegativeTotalAmountWithBusinessCode() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    DefaultPurRequestCommandService service = new DefaultPurRequestCommandService(repository,
        new RecordingWorkflowService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.create(new PurRequestCreateCommand("tenant_a", "PR202606240009",
            "异常金额采购", 1001L, 2001L, new BigDecimal("-0.01"), "异常金额")));

    assertEquals("ZHYC_PUR_REQUEST_TOTAL_AMOUNT_INVALID", exception.getCode());
    assertEquals("采购申请总金额不能小于 0", exception.getMessage());
  }

  /**
   * 验证可按租户和流程状态分页查询采购申请。
   */
  @Test
  void shouldListPurchaseRequestsByProcessStatusWithPagination() {
    RecordingPurRequestRepository repository = new RecordingPurRequestRepository();
    DefaultPurRequestCommandService service = new DefaultPurRequestCommandService(repository,
        new RecordingWorkflowService());
    repository.requests.add(new PurRequest(1L, "tenant_a", "PR202606240001", "办公电脑采购",
        1001L, 2001L, new BigDecimal("1200.00"), "新员工入职", "DRAFT", null,
        null, null, null));
    repository.requests.add(new PurRequest(2L, "tenant_a", "PR202606240002", "会议设备采购",
        1002L, 2001L, new BigDecimal("3200.00"), "会议室改造", "APPROVING", null,
        null, null, null));
    repository.requests.add(new PurRequest(3L, "tenant_b", "PR202606240003", "异租户采购",
        1003L, 2001L, new BigDecimal("800.00"), "隔离数据", "DRAFT", null,
        null, null, null));

    PageResult<PurRequestStatusResponse> response =
        service.list(new PurRequestQuery("tenant_a", "DRAFT", 1, 10));

    assertEquals(1, response.getTotal());
    assertEquals(1, response.getPageNo());
    assertEquals(10, response.getPageSize());
    assertEquals(1, response.getRecords().size());
    assertEquals("PR202606240001", response.getRecords().get(0).getRequestNo());
    assertEquals("DRAFT", response.getRecords().get(0).getProcessStatus());
  }


  /**
   * 测试用采购申请仓储。
   */
  private static class RecordingPurRequestRepository implements PurRequestRepository {

    /** 最近一次保存的采购申请。 */
    private PurRequest saved;
    /** 查询返回结果。 */
    private Optional<PurRequest> queryResult = Optional.empty();
    /** 最近一次提交的流程实例 ID。 */
    private String submittedProcessInstanceId;
    /** 最近一次提交后的状态。 */
    private String submittedStatus;
    /** 内存采购申请列表。 */
    private final List<PurRequest> requests = new ArrayList<>();

    @Override
    public Optional<PurRequest> findByTenantIdAndRequestNo(String tenantId, String requestNo) {
      return queryResult;
    }

    @Override
    public void save(PurRequest purRequest) {
      this.saved = purRequest;
      requests.add(purRequest);
    }

    @Override
    public void updateSubmitted(String tenantId, String requestNo, String processInstanceId,
                                String processStatus, LocalDateTime submittedAt) {
      this.submittedProcessInstanceId = processInstanceId;
      this.submittedStatus = processStatus;
    }

    @Override
    public long countByTenantIdAndProcessStatus(String tenantId, String processStatus) {
      return requests.stream()
          .filter(request -> tenantId.equals(request.getTenantId()))
          .filter(request -> processStatus == null || processStatus.equals(request.getProcessStatus()))
          .count();
    }

    @Override
    public List<PurRequest> findPageByTenantIdAndProcessStatus(String tenantId, String processStatus,
        long offset, int pageSize) {
      return requests.stream()
          .filter(request -> tenantId.equals(request.getTenantId()))
          .filter(request -> processStatus == null || processStatus.equals(request.getProcessStatus()))
          .skip(offset)
          .limit(pageSize)
          .toList();
    }

    @Override
    public void updateProcessStatus(String tenantId, String requestNo, String processStatus,
        LocalDateTime updatedAt) {
    }
  }

  /**
   * 测试用工作流门面。
   */
  private static class RecordingWorkflowService implements WorkflowService {

    /** 启动流程次数。 */
    private int startCount;
    /** 最近一次流程定义 Key。 */
    private String lastProcessKey;
    /** 最近一次业务唯一键。 */
    private String lastBusinessKey;
    /** 最近一次流程变量。 */
    private Map<String, Object> lastVariables;

    @Override
    public String startProcess(String processKey, String businessKey, Map<String, Object> variables) {
      this.startCount++;
      this.lastProcessKey = processKey;
      this.lastBusinessKey = businessKey;
      this.lastVariables = variables;
      return "wf-pr-001";
    }

    @Override
    public void approve(String taskId, String comment, Map<String, Object> variables) {
    }

    @Override
    public void reject(String taskId, String comment) {
    }

    @Override
    public void revoke(String processInstanceId, String reason) {
    }
  }
}
