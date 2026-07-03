/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.workflow.category.domain.WorkflowCategory;
import com.zhyc.workflow.category.repository.WorkflowCategoryRepository;
import com.zhyc.workflow.category.service.DefaultWorkflowCategoryService;
import com.zhyc.workflow.category.service.WorkflowCategoryResponse;
import com.zhyc.workflow.category.service.WorkflowCategorySaveCommand;
import com.zhyc.workflow.category.service.WorkflowCategoryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 工作流分类业务服务测试。
 */
class WorkflowCategoryServiceTest {

  /**
   * 验证按租户查询流程分类时会裁剪租户编码，并按排序号和主键排序。
   */
  @Test
  void shouldListTenantCategoriesBySortOrder() {
    RecordingCategoryRepository repository = new RecordingCategoryRepository();
    WorkflowCategoryService service = new DefaultWorkflowCategoryService(repository);

    List<WorkflowCategoryResponse> categories = service.listCategories(" tenant_a ");

    assertEquals("tenant_a", repository.lastTenantId);
    assertEquals(2, categories.size());
    assertEquals("采购流程", categories.get(0).getCategoryName());
    assertEquals("行政流程", categories.get(1).getCategoryName());
  }

  /**
   * 验证保存流程分类时会裁剪租户、编码、名称和状态字段。
   */
  @Test
  void shouldSaveCategoryWithNormalizedFields() {
    RecordingCategoryRepository repository = new RecordingCategoryRepository();
    WorkflowCategoryService service = new DefaultWorkflowCategoryService(repository);

    service.saveCategory(new WorkflowCategorySaveCommand(" tenant_a ", 10L,
        " purchase ", " 采购流程 ", 1, " enabled ", " 采购审批分类 "));

    assertEquals("tenant_a", repository.savedCategory.getTenantId());
    assertEquals("purchase", repository.savedCategory.getCategoryCode());
    assertEquals("采购流程", repository.savedCategory.getCategoryName());
    assertEquals("enabled", repository.savedCategory.getStatus());
    assertEquals("采购审批分类", repository.savedCategory.getRemark());
  }

  /**
   * 验证保存流程分类时会拒绝不受支持的配置状态。
   */
  @Test
  void shouldRejectUnsupportedStatusWhenSavingCategory() {
    RecordingCategoryRepository repository = new RecordingCategoryRepository();
    WorkflowCategoryService service = new DefaultWorkflowCategoryService(repository);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.saveCategory(new WorkflowCategorySaveCommand("tenant_a", 10L,
            "purchase", "采购流程", 1, "archived", "采购审批分类")));

    assertEquals("ZHYC_WORKFLOW_ARGUMENT_INVALID", exception.getCode());
    assertEquals("工作流配置状态不支持: archived", exception.getMessage());
  }

  /**
   * 测试用工作流分类仓储。
   */
  private static class RecordingCategoryRepository implements WorkflowCategoryRepository {

    /** 最近一次查询的租户业务编码。 */
    private String lastTenantId;
    /** 最近一次保存的工作流分类。 */
    private WorkflowCategory savedCategory;

    @Override
    public List<WorkflowCategory> findByTenantId(String tenantId) {
      lastTenantId = tenantId;
      LocalDateTime now = LocalDateTime.now();
      return List.of(
          new WorkflowCategory(2L, tenantId, "admin", "行政流程", 2,
              "enabled", now, now, "行政审批分类"),
          new WorkflowCategory(1L, tenantId, "purchase", "采购流程", 1,
              "enabled", now, now, "采购审批分类"));
    }

    @Override
    public void save(WorkflowCategory category) {
      savedCategory = category;
    }
  }
}
