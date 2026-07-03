/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.category;

import com.zhyc.workflow.category.controller.WorkflowCategoryController;
import com.zhyc.workflow.category.service.WorkflowCategoryResponse;
import com.zhyc.workflow.category.service.WorkflowCategorySaveCommand;
import com.zhyc.workflow.category.service.WorkflowCategoryService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流分类管理接口契约测试。
 */
class WorkflowCategoryControllerContractTest {

  /**
   * 验证流程分类控制器暴露查询和保存路由，并声明 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeCategoryRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass =
        Class.forName("com.zhyc.workflow.category.controller.WorkflowCategoryController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/workflow/categories");
    assertMethodMapping(controllerClass, "listCategories",
        "org.springframework.web.bind.annotation.GetMapping", "", "workflow:model:query");
    assertMethodMapping(controllerClass, "saveCategory",
        "org.springframework.web.bind.annotation.PostMapping", "", "workflow:model:update");
  }

  /**
   * 验证保存分类空请求体会转换为安全命令，并保留租户隔离上下文。
   */
  @Test
  void shouldConvertNullCategoryRequestToCommandWithTenant() {
    RecordingCategoryService categoryService = new RecordingCategoryService();
    WorkflowCategoryController controller = new WorkflowCategoryController(categoryService);

    controller.saveCategory("tenant-a", null);

    WorkflowCategorySaveCommand command = categoryService.saveCommand;
    assertEquals("tenant-a", command.getTenantId());
    assertNull(command.getId());
    assertNull(command.getCategoryCode());
    assertNull(command.getCategoryName());
    assertNull(command.getSortOrder());
    assertNull(command.getStatus());
    assertNull(command.getRemark());
  }

  private static void assertMethodMapping(Class<?> controllerClass, String methodName,
      String mappingAnnotation, String mappingValue, String permission) throws Exception {
    Method method = Arrays.stream(controllerClass.getDeclaredMethods())
        .filter(candidate -> candidate.getName().equals(methodName))
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少控制器方法: " + methodName));
    assertAnnotationValue(method, mappingAnnotation, "value", mappingValue);
    assertAnnotationValue(method, "org.apache.shiro.authz.annotation.RequiresPermissions",
        "value", permission);
  }

  private static void assertAnnotation(Class<?> targetClass, String annotationName) {
    assertTrue(Arrays.stream(targetClass.getAnnotations())
        .map(annotation -> annotation.annotationType().getName())
        .anyMatch(annotationName::equals), "缺少注解: " + annotationName);
  }

  private static void assertAnnotationValue(Object annotatedElement, String annotationName,
      String attributeName, String expectedValue) throws Exception {
    Annotation annotation = findAnnotation(annotatedElement, annotationName);
    Method attribute = annotation.annotationType().getMethod(attributeName);
    Object actualValue = attribute.invoke(annotation);
    if (actualValue instanceof String[] values) {
      assertEquals(expectedValue, values.length == 0 ? "" : values[0]);
      return;
    }
    assertEquals(expectedValue, actualValue);
  }

  private static Annotation findAnnotation(Object annotatedElement, String annotationName) {
    Annotation[] annotations = annotatedElement instanceof Class<?> targetClass
        ? targetClass.getAnnotations()
        : ((Method) annotatedElement).getAnnotations();
    return Arrays.stream(annotations)
        .filter(annotation -> annotation.annotationType().getName().equals(annotationName))
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少注解: " + annotationName));
  }

  /**
   * 记录工作流分类服务入参的测试桩。
   */
  private static final class RecordingCategoryService implements WorkflowCategoryService {

    /** 最近一次分类保存命令。 */
    private WorkflowCategorySaveCommand saveCommand;

    /**
     * 返回空分类列表。
     *
     * @param tenantId 租户业务编码
     * @return 空分类列表
     */
    @Override
    public List<WorkflowCategoryResponse> listCategories(String tenantId) {
      return List.of();
    }

    /**
     * 记录分类保存命令。
     *
     * @param command 工作流分类保存命令
     */
    @Override
    public void saveCategory(WorkflowCategorySaveCommand command) {
      this.saveCommand = command;
    }
  }
}
