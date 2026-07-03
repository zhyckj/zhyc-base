/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.definition;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.workflow.definition.domain.WorkflowDefinition;
import com.zhyc.workflow.definition.repository.WorkflowDefinitionRepository;
import com.zhyc.workflow.definition.service.DefaultWorkflowDefinitionService;
import com.zhyc.workflow.definition.service.WorkflowDefinitionResponse;
import com.zhyc.workflow.definition.service.WorkflowDefinitionSaveCommand;
import com.zhyc.workflow.definition.service.WorkflowDefinitionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 工作流流程定义业务服务测试。
 */
class WorkflowDefinitionServiceTest {

  /**
   * 验证按租户查询流程定义时会裁剪租户编码，并按流程 key 和版本排序。
   */
  @Test
  void shouldListTenantDefinitionsByProcessKeyAndVersion() {
    RecordingDefinitionRepository repository = new RecordingDefinitionRepository();
    WorkflowDefinitionService service = new DefaultWorkflowDefinitionService(repository);

    List<WorkflowDefinitionResponse> definitions = service.listDefinitions(" tenant_a ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(2, definitions.size());
    assertEquals("purchase_request", definitions.get(0).getProcessKey());
    assertEquals(2, definitions.get(0).getVersion());
    assertEquals("travel_apply", definitions.get(1).getProcessKey());
  }

  /**
   * 验证保存流程定义时会裁剪流程 key、名称、部署 ID 和状态字段。
   */
  @Test
  void shouldSaveDefinitionWithNormalizedFields() {
    RecordingDefinitionRepository repository = new RecordingDefinitionRepository();
    WorkflowDefinitionService service = new DefaultWorkflowDefinitionService(repository);

    service.saveDefinition(new WorkflowDefinitionSaveCommand(" tenant_a ", 8L,
        " purchase_request ", " 采购申请流程 ", 2, " deploy_100 ",
        " active ", " 采购申请审批 "));

    assertEquals("tenant_a", repository.savedDefinition.getTenantId());
    assertEquals("purchase_request", repository.savedDefinition.getProcessKey());
    assertEquals("采购申请流程", repository.savedDefinition.getProcessName());
    assertEquals(2, repository.savedDefinition.getVersion());
    assertEquals("deploy_100", repository.savedDefinition.getDeploymentId());
    assertEquals("active", repository.savedDefinition.getStatus());
  }

  /**
   * 验证保存流程定义时会拒绝不受支持的定义状态。
   */
  @Test
  void shouldRejectUnsupportedStatusWhenSavingDefinition() {
    RecordingDefinitionRepository repository = new RecordingDefinitionRepository();
    WorkflowDefinitionService service = new DefaultWorkflowDefinitionService(repository);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveDefinition(new WorkflowDefinitionSaveCommand("tenant_a", 8L,
            "purchase_request", "采购申请流程", 2, "deploy_100",
            "archived", "采购申请审批")));

    assertEquals("ZHYC_WORKFLOW_ARGUMENT_INVALID", exception.getCode());
    assertEquals("工作流流程定义状态不支持: archived", exception.getMessage());
  }

  /**
   * 测试用流程定义仓储。
   */
  private static class RecordingDefinitionRepository implements WorkflowDefinitionRepository {

    /** 最近一次查询的租户业务编码。 */
    private String lastTenantId;
    /** 最近一次保存的流程定义。 */
    private WorkflowDefinition savedDefinition;

    @Override
    public List<WorkflowDefinition> findByTenantId(String tenantId) {
      lastTenantId = tenantId;
      LocalDateTime now = LocalDateTime.now();
      return List.of(
          new WorkflowDefinition(2L, tenantId, "travel_apply", "出差申请流程", 1,
              "deploy_200", "active", now, now, "出差申请审批"),
          new WorkflowDefinition(1L, tenantId, "purchase_request", "采购申请流程", 2,
              "deploy_100", "active", now, now, "采购申请审批"));
    }

    @Override
    public void save(WorkflowDefinition definition) {
      savedDefinition = definition;
    }
  }
}
