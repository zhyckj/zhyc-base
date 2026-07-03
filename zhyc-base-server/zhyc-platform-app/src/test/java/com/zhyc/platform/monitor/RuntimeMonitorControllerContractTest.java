/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.monitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestParam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 平台运行监控接口契约测试。
 */
class RuntimeMonitorControllerContractTest {

  /**
   * 验证运行监控控制器暴露 SQL 监控接口和权限编码。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeSqlMonitorRouteWithPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.platform.monitor.RuntimeMonitorController");

    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/monitor/runtime");
    assertMethodMapping(controllerClass, "listSqlMonitorRecords", "org.springframework.web.bind.annotation.GetMapping",
        "/sql", "monitor:sql:query");
    Method sqlMonitorMethod = Arrays.stream(controllerClass.getDeclaredMethods())
        .filter(candidate -> candidate.getName().equals("listSqlMonitorRecords"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少 SQL 监控控制器方法"));
    assertEquals("1", findRequestParam(sqlMonitorMethod, "thresholdMs").defaultValue());
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

  private static RequestParam findRequestParam(Method method, String parameterName) {
    return Arrays.stream(method.getParameters())
        .filter(parameter -> parameterName.equals(findRequestParam(parameter).value()))
        .map(RuntimeMonitorControllerContractTest::findRequestParam)
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少请求参数: " + parameterName));
  }

  private static RequestParam findRequestParam(Parameter parameter) {
    return Arrays.stream(parameter.getAnnotations())
        .filter(annotation -> annotation.annotationType() == RequestParam.class)
        .map(RequestParam.class::cast)
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少 RequestParam 注解"));
  }

  /**
   * 测试用运行监控服务。
   */
  private static final class EmptyRuntimeMonitorService implements RuntimeMonitorService {

    @Override
    public List<RuntimeServiceStatus> listServiceStatus() {
      return List.of();
    }

    @Override
    public List<RuntimeDataSourceStatus> listDataSourceStatus() {
      return List.of();
    }

    @Override
    public List<RuntimeSqlMonitorRecord> listSqlMonitorRecords(int thresholdMs, int limit) {
      return List.of();
    }
  }
}
