/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.workflow.model;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.workflow.deployment.WorkflowModelDeploymentCommand;
import com.zhyc.workflow.deployment.WorkflowModelDeploymentResult;
import com.zhyc.workflow.deployment.WorkflowModelDeploymentService;
import com.zhyc.workflow.model.controller.WorkflowProcessModelController;
import com.zhyc.workflow.model.service.WorkflowProcessModelResponse;
import com.zhyc.workflow.model.service.WorkflowProcessModelSaveCommand;
import com.zhyc.workflow.model.service.WorkflowProcessModelService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工作流流程模型接口契约测试。
 */
class WorkflowProcessModelControllerContractTest {

  /**
   * 验证流程模型控制器暴露查询和保存路由，并声明 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeModelRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass =
        Class.forName("com.zhyc.workflow.model.controller.WorkflowProcessModelController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/workflow/models");
    assertMethodMapping(controllerClass, "listModels",
        "org.springframework.web.bind.annotation.GetMapping", "", "workflow:model:query");
    assertMethodMapping(controllerClass, "saveModel",
        "org.springframework.web.bind.annotation.PostMapping", "", "workflow:model:update");
    assertMethodMapping(controllerClass, "deployModel",
        "org.springframework.web.bind.annotation.PostMapping", "/{modelId}/deploy",
        "workflow:model:deploy");
    assertMethodParameterAnnotationValue(controllerClass, "deployModel", 1,
        "org.springframework.web.bind.annotation.PathVariable", "value", "modelId");
  }

  /**
   * 验证保存模型空请求体会转换为安全命令，并保留租户隔离上下文。
   */
  @Test
  void shouldConvertNullModelRequestToCommandWithTenant() {
    RecordingModelService modelService = new RecordingModelService();
    WorkflowProcessModelController controller = new WorkflowProcessModelController(modelService,
        deploymentProvider(new RecordingDeploymentService()));

    controller.saveModel("tenant-a", null);

    WorkflowProcessModelSaveCommand command = modelService.saveCommand;
    assertEquals("tenant-a", command.getTenantId());
    assertNull(command.getId());
    assertNull(command.getModelCode());
    assertNull(command.getModelName());
    assertNull(command.getCategoryId());
    assertNull(command.getFlowableModelId());
    assertNull(command.getBpmnXml());
    assertNull(command.getStatus());
    assertNull(command.getRemark());
  }

  /**
   * 验证发布模型空请求体会转换为安全命令，并保留路径模型和租户上下文。
   */
  @Test
  void shouldConvertNullDeployRequestToCommandWithTenantAndModel() {
    RecordingDeploymentService deploymentService = new RecordingDeploymentService();
    WorkflowProcessModelController controller = new WorkflowProcessModelController(
        new RecordingModelService(), deploymentProvider(deploymentService));

    controller.deployModel("tenant-b", 2001L, null);

    WorkflowModelDeploymentCommand command = deploymentService.deployCommand;
    assertEquals("tenant-b", command.getTenantId());
    assertEquals(2001L, command.getModelId());
    assertNull(command.getBpmnXml());
    assertNull(command.getRemark());
  }

  /**
   * 验证 Flowable 发布服务未装配时返回稳定业务异常，而不是暴露框架异常。
   */
  @Test
  void shouldRejectDeployWhenDeploymentServiceMissingWithBusinessException() {
    WorkflowProcessModelController controller = new WorkflowProcessModelController(
        new RecordingModelService(), emptyDeploymentProvider());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.deployModel("tenant-b", 2001L, null));

    assertEquals("ZHYC_WORKFLOW_ARGUMENT_INVALID", exception.getCode());
    assertEquals("Flowable 发布服务未配置，不能发布流程模型", exception.getMessage());
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

  private static void assertMethodParameterAnnotationValue(Class<?> controllerClass,
      String methodName, int parameterIndex, String annotationName, String attributeName,
      String expectedValue) throws Exception {
    Method method = Arrays.stream(controllerClass.getDeclaredMethods())
        .filter(candidate -> candidate.getName().equals(methodName))
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少控制器方法: " + methodName));
    Parameter parameter = method.getParameters()[parameterIndex];
    Annotation annotation = Arrays.stream(parameter.getAnnotations())
        .filter(candidate -> candidate.annotationType().getName().equals(annotationName))
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少参数注解: " + annotationName));
    Method attribute = annotation.annotationType().getMethod(attributeName);
    Object actualValue = attribute.invoke(annotation);
    if (actualValue instanceof String[] values) {
      assertEquals(expectedValue, values.length == 0 ? "" : values[0]);
      return;
    }
    assertEquals(expectedValue, actualValue);
  }

  /**
   * 创建模型发布服务提供器。
   *
   * @param deploymentService 模型发布服务
   * @return 模型发布服务提供器
   */
  private static ObjectProvider<WorkflowModelDeploymentService> deploymentProvider(
      WorkflowModelDeploymentService deploymentService) {
    StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
    beanFactory.addBean("workflowModelDeploymentService", deploymentService);
    return beanFactory.getBeanProvider(WorkflowModelDeploymentService.class);
  }

  /**
   * 创建空模型发布服务提供器。
   *
   * @return 空模型发布服务提供器
   */
  private static ObjectProvider<WorkflowModelDeploymentService> emptyDeploymentProvider() {
    StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
    return beanFactory.getBeanProvider(WorkflowModelDeploymentService.class);
  }

  /**
   * 记录工作流模型服务入参的测试桩。
   */
  private static final class RecordingModelService implements WorkflowProcessModelService {

    /** 最近一次模型保存命令。 */
    private WorkflowProcessModelSaveCommand saveCommand;

    /**
     * 返回空模型列表。
     *
     * @param tenantId 租户业务编码
     * @return 空模型列表
     */
    @Override
    public List<WorkflowProcessModelResponse> listModels(String tenantId) {
      return List.of();
    }

    /**
     * 记录模型保存命令。
     *
     * @param command 流程模型保存命令
     */
    @Override
    public void saveModel(WorkflowProcessModelSaveCommand command) {
      this.saveCommand = command;
    }
  }

  /**
   * 记录模型发布服务入参的测试桩。
   */
  private static final class RecordingDeploymentService implements WorkflowModelDeploymentService {

    /** 最近一次模型发布命令。 */
    private WorkflowModelDeploymentCommand deployCommand;

    /**
     * 记录模型发布命令。
     *
     * @param command 流程模型发布命令
     * @return 固定发布结果
     */
    @Override
    public WorkflowModelDeploymentResult deploy(WorkflowModelDeploymentCommand command) {
      this.deployCommand = command;
      return new WorkflowModelDeploymentResult(command.getTenantId(), "process-a",
          "流程A", 1, "deployment-a");
    }
  }
}
