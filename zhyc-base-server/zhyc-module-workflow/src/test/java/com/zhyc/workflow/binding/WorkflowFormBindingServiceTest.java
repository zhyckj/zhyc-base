/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.binding;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.workflow.binding.domain.WorkflowFormBinding;
import com.zhyc.workflow.binding.repository.WorkflowFormBindingRepository;
import com.zhyc.workflow.binding.service.DefaultWorkflowFormBindingService;
import com.zhyc.workflow.binding.service.WorkflowFormBindingResponse;
import com.zhyc.workflow.binding.service.WorkflowFormBindingSaveCommand;
import com.zhyc.workflow.binding.service.WorkflowFormBindingService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 工作流表单绑定业务服务测试。
 */
class WorkflowFormBindingServiceTest {

  /**
   * 验证按租户查询表单绑定时会裁剪租户编码，并按业务模块和流程 key 排序。
   */
  @Test
  void shouldListTenantBindingsByBusinessModuleAndProcessKey() {
    RecordingBindingRepository repository = new RecordingBindingRepository();
    WorkflowFormBindingService service = new DefaultWorkflowFormBindingService(repository);

    List<WorkflowFormBindingResponse> bindings = service.listBindings(" tenant_a ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(2, bindings.size());
    assertEquals("purchase_request", bindings.get(0).getProcessKey());
    assertEquals("travel_apply", bindings.get(1).getProcessKey());
  }

  /**
   * 验证保存表单绑定时会裁剪租户、流程、业务模块、业务表和路由字段。
   */
  @Test
  void shouldSaveBindingWithNormalizedFields() {
    RecordingBindingRepository repository = new RecordingBindingRepository();
    WorkflowFormBindingService service = new DefaultWorkflowFormBindingService(repository);

    service.saveBinding(new WorkflowFormBindingSaveCommand(" tenant_a ", 9L,
        " purchase_request ", " purchase ", " pur_request ",
        " /purchase/requests ", " /pages/purchase/request-detail ", " enabled ", " 采购申请绑定 "));

    assertEquals("tenant_a", repository.savedBinding.getTenantId());
    assertEquals("purchase_request", repository.savedBinding.getProcessKey());
    assertEquals("purchase", repository.savedBinding.getBusinessModule());
    assertEquals("pur_request", repository.savedBinding.getBusinessTable());
    assertEquals("/purchase/requests", repository.savedBinding.getFormRoute());
    assertEquals("/pages/purchase/request-detail", repository.savedBinding.getMobileRoute());
    assertEquals("enabled", repository.savedBinding.getStatus());
  }

  /**
   * 验证保存表单绑定时会拒绝不受支持的配置状态。
   */
  @Test
  void shouldRejectUnsupportedStatusWhenSavingBinding() {
    RecordingBindingRepository repository = new RecordingBindingRepository();
    WorkflowFormBindingService service = new DefaultWorkflowFormBindingService(repository);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveBinding(new WorkflowFormBindingSaveCommand("tenant_a", 9L,
            "purchase_request", "purchase", "pur_request",
            "/purchase/requests", "/pages/purchase/request-detail", "archived", "采购申请绑定")));

    assertEquals("ZHYC_WORKFLOW_ARGUMENT_INVALID", exception.getCode());
    assertEquals("工作流配置状态不支持: archived", exception.getMessage());
  }

  /**
   * 测试用工作流表单绑定仓储。
   */
  private static class RecordingBindingRepository implements WorkflowFormBindingRepository {

    /** 最近一次查询的租户业务编码。 */
    private String lastTenantId;
    /** 最近一次保存的表单绑定。 */
    private WorkflowFormBinding savedBinding;

    @Override
    public List<WorkflowFormBinding> findByTenantId(String tenantId) {
      lastTenantId = tenantId;
      LocalDateTime now = LocalDateTime.now();
      return List.of(
          new WorkflowFormBinding(2L, tenantId, "travel_apply", "workflow",
              "wf_travel_apply", "/workflow/travel", "/pages/workflow/travel",
              "enabled", now, now, "出差申请"),
          new WorkflowFormBinding(1L, tenantId, "purchase_request", "purchase",
              "pur_request", "/purchase/requests", "/pages/purchase/request-detail",
              "enabled", now, now, "采购申请"));
    }

    @Override
    public void save(WorkflowFormBinding binding) {
      savedBinding = binding;
    }
  }
}
