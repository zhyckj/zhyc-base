/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.job.task;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.job.task.controller.JobTaskController;
import com.zhyc.job.task.service.JobTaskLogResponse;
import com.zhyc.job.task.service.JobTaskResponse;
import com.zhyc.job.task.service.JobTaskSaveCommand;
import com.zhyc.job.task.service.JobTaskService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 在线作业任务接口契约测试。
 */
class JobTaskControllerContractTest {

  /**
   * 验证在线作业任务控制器声明路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeJobTaskRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.job.task.controller.JobTaskController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/job/tasks");
    assertMethodMapping(controllerClass, "listTasks", "org.springframework.web.bind.annotation.GetMapping",
        "", "job:task:query");
    assertMethodMapping(controllerClass, "save", "org.springframework.web.bind.annotation.PostMapping",
        "", "job:task:save");
    assertMethodMapping(controllerClass, "updateStatus", "org.springframework.web.bind.annotation.PostMapping",
        "/{id}/status", "job:task:save");
    assertMethodMapping(controllerClass, "trigger", "org.springframework.web.bind.annotation.PostMapping",
        "/{id}/trigger", "job:task:trigger");
    assertMethodMapping(controllerClass, "listLogs", "org.springframework.web.bind.annotation.GetMapping",
        "/{id}/logs", "job:log:query");
  }

  /**
   * 验证作业任务保存空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullSaveCommand() {
    JobTaskController controller = new JobTaskController(new RejectingJobTaskService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.save(null));

    assertEquals("ZHYC_JOB_TASK_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("作业任务保存请求不能为空", exception.getMessage());
  }

  /**
   * 验证作业任务状态变更空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullStatusCommand() {
    JobTaskController controller = new JobTaskController(new RejectingJobTaskService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.updateStatus("tenant-a", 1001L, null));

    assertEquals("ZHYC_JOB_TASK_STATUS_REQUEST_REQUIRED", exception.getCode());
    assertEquals("作业任务状态变更请求不能为空", exception.getMessage());
  }

  private static void assertMethodMapping(Class<?> controllerClass, String methodName,
      String mappingAnnotation, String mappingValue, String permission) throws Exception {
    Method method = Arrays.stream(controllerClass.getDeclaredMethods())
        .filter(candidate -> candidate.getName().equals(methodName))
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少控制器方法: " + methodName));
    assertAnnotationValue(method, mappingAnnotation, "value", mappingValue);
    assertAnnotationValue(method, "org.apache.shiro.authz.annotation.RequiresPermissions", "value", permission);
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
   * 拒绝被调用的在线作业任务服务测试桩。
   */
  private static final class RejectingJobTaskService implements JobTaskService {

    /**
     * 返回空任务列表。
     *
     * @param tenantId 租户业务编码
     * @param status 作业状态
     * @return 空任务列表
     */
    @Override
    public List<JobTaskResponse> listTasks(String tenantId, String status) {
      return List.of();
    }

    /**
     * 保存任务时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 作业任务保存命令
     */
    @Override
    public void save(JobTaskSaveCommand command) {
      throw new AssertionError("作业任务保存服务不应被调用");
    }

    /**
     * 更新状态时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param tenantId 租户业务编码
     * @param id 作业任务主键
     * @param status 作业状态
     */
    @Override
    public void updateStatus(String tenantId, Long id, String status) {
      throw new AssertionError("作业任务状态变更服务不应被调用");
    }

    /**
     * 手动触发任务时不做处理。
     *
     * @param tenantId 租户业务编码
     * @param id 作业任务主键
     * @param operatorId 操作人用户主键
     */
    @Override
    public void trigger(String tenantId, Long id, Long operatorId) {
    }

    /**
     * 返回空执行日志列表。
     *
     * @param tenantId 租户业务编码
     * @param jobId 作业任务主键
     * @return 空执行日志列表
     */
    @Override
    public List<JobTaskLogResponse> listLogs(String tenantId, Long jobId) {
      return List.of();
    }
  }
}
