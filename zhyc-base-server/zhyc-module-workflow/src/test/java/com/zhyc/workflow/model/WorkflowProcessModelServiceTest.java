/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.workflow.model.domain.WorkflowProcessModel;
import com.zhyc.workflow.model.repository.WorkflowProcessModelRepository;
import com.zhyc.workflow.model.service.DefaultWorkflowProcessModelService;
import com.zhyc.workflow.model.service.WorkflowProcessModelResponse;
import com.zhyc.workflow.model.service.WorkflowProcessModelSaveCommand;
import com.zhyc.workflow.model.service.WorkflowProcessModelService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 工作流流程模型业务服务测试。
 */
class WorkflowProcessModelServiceTest {

  /**
   * 验证按租户查询流程模型时会裁剪租户编码，并按分类和模型编码排序。
   */
  @Test
  void shouldListTenantModelsByCategoryAndModelCode() {
    RecordingModelRepository repository = new RecordingModelRepository();
    WorkflowProcessModelService service = new DefaultWorkflowProcessModelService(repository);

    List<WorkflowProcessModelResponse> models = service.listModels(" tenant_a ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(2, models.size());
    assertEquals("purchase_request", models.get(0).getModelCode());
    assertEquals("travel_apply", models.get(1).getModelCode());
  }

  /**
   * 验证保存流程模型时会裁剪模型编码、名称、Flowable 模型 ID 和状态字段。
   */
  @Test
  void shouldSaveModelWithNormalizedFields() {
    RecordingModelRepository repository = new RecordingModelRepository();
    WorkflowProcessModelService service = new DefaultWorkflowProcessModelService(repository);

    service.saveModel(new WorkflowProcessModelSaveCommand(" tenant_a ", 7L,
        " purchase_request ", " 采购申请流程 ", 1L, " flowable_model_1 ",
        " <bpmn:definitions/> ", " enabled ", " 采购申请模型 "));

    assertEquals("tenant_a", repository.savedModel.getTenantId());
    assertEquals("purchase_request", repository.savedModel.getModelCode());
    assertEquals("采购申请流程", repository.savedModel.getModelName());
    assertEquals(1L, repository.savedModel.getCategoryId());
    assertEquals("flowable_model_1", repository.savedModel.getFlowableModelId());
    assertEquals("<bpmn:definitions/>", repository.savedModel.getBpmnXml());
    assertEquals("enabled", repository.savedModel.getStatus());
  }

  /**
   * 验证 Flowable 模型 ID 为空时默认使用流程模型编码。
   */
  @Test
  void shouldDefaultFlowableModelIdToModelCodeWhenSavingModel() {
    RecordingModelRepository repository = new RecordingModelRepository();
    WorkflowProcessModelService service = new DefaultWorkflowProcessModelService(repository);

    service.saveModel(new WorkflowProcessModelSaveCommand("tenant_a", null,
        "leave_apply", "请假申请流程", null, " ",
        null, "enabled", null));

    assertEquals("leave_apply", repository.savedModel.getFlowableModelId());
  }

  /**
   * 验证保存流程模型时会拒绝不受支持的配置状态。
   */
  @Test
  void shouldRejectUnsupportedStatusWhenSavingModel() {
    RecordingModelRepository repository = new RecordingModelRepository();
    WorkflowProcessModelService service = new DefaultWorkflowProcessModelService(repository);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveModel(new WorkflowProcessModelSaveCommand("tenant_a", 7L,
            "purchase_request", "采购申请流程", 1L, "flowable_model_1",
            null, "archived", "采购申请模型")));

    assertEquals("ZHYC_WORKFLOW_ARGUMENT_INVALID", exception.getCode());
    assertEquals("工作流配置状态不支持: archived", exception.getMessage());
  }

  /**
   * 测试用流程模型仓储。
   */
  private static class RecordingModelRepository implements WorkflowProcessModelRepository {

    /** 最近一次查询的租户业务编码。 */
    private String lastTenantId;
    /** 最近一次保存的流程模型。 */
    private WorkflowProcessModel savedModel;

    @Override
    public List<WorkflowProcessModel> findByTenantId(String tenantId) {
      lastTenantId = tenantId;
      LocalDateTime now = LocalDateTime.now();
      return List.of(
          new WorkflowProcessModel(2L, tenantId, "travel_apply", "出差申请流程",
              2L, "flowable_model_2", "<bpmn:definitions id=\"travel\"/>", "enabled",
              now, now, "出差申请模型"),
          new WorkflowProcessModel(1L, tenantId, "purchase_request", "采购申请流程",
              1L, "flowable_model_1", "<bpmn:definitions id=\"purchase\"/>", "enabled",
              now, now, "采购申请模型"));
    }

    @Override
    public void save(WorkflowProcessModel model) {
      savedModel = model;
    }
  }
}
