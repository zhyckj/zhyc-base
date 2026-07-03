/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 低代码数据库方言接口契约测试。
 */
class LowcodeDialectControllerContractTest {

  /**
   * 校验方言能力接口暴露后台管理端路由，并声明 Shiro 查询权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeDialectCapabilityRouteWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.lowcode.db.LowcodeDialectController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/lowcode/dialects");
    assertMethodMapping(controllerClass, "capabilities", "org.springframework.web.bind.annotation.GetMapping",
        "/capabilities", "lowcode:dialect:query");
  }

  /**
   * 校验方言能力响应对象暴露三类能力清单字段。
   *
   * @throws Exception 反射读取响应对象失败时抛出
   */
  @Test
  void shouldExposeDialectCapabilityResponseFields() throws Exception {
    Class<?> responseClass = Class.forName("com.zhyc.lowcode.db.LowcodeDialectCapabilitiesResponse");

    assertTrue(Arrays.stream(responseClass.getDeclaredFields())
        .anyMatch(field -> field.getName().equals("ddlDialectCodes")), "缺少 DDL 方言能力字段");
    assertTrue(Arrays.stream(responseClass.getDeclaredFields())
        .anyMatch(field -> field.getName().equals("fieldTypeDialectCodes")), "缺少字段类型方言能力字段");
    assertTrue(Arrays.stream(responseClass.getDeclaredFields())
        .anyMatch(field -> field.getName().equals("paginationDialectCodes")), "缺少分页方言能力字段");
  }

  private static void assertMethodMapping(Class<?> controllerClass, String methodName, String mappingAnnotation,
                                          String mappingValue, String permission) throws Exception {
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

  private static void assertAnnotationValue(Object annotatedElement, String annotationName, String attributeName,
                                            String expectedValue) throws Exception {
    Annotation annotation = findAnnotation(annotatedElement, annotationName);
    Method attribute = annotation.annotationType().getMethod(attributeName);
    Object actualValue = attribute.invoke(annotation);
    if (actualValue instanceof String[] values) {
      assertEquals(expectedValue, values[0]);
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
}
