/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import com.zhyc.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 低代码生成管理接口契约测试。
 */
class LowcodeGeneratorControllerContractTest {

  /**
   * 校验生成控制器暴露模板查询和生成预览路由，并声明 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeGeneratorRoutesWithShiroPermissions() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.lowcode.generator.LowcodeGeneratorController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/lowcode/generator");
    assertMethodMapping(controllerClass, "listTemplates", "org.springframework.web.bind.annotation.GetMapping",
        "/templates", "lowcode:generator:query");
    assertMethodMapping(controllerClass, "preview", "org.springframework.web.bind.annotation.PostMapping",
        "/preview", "lowcode:generator:query");
    assertMethodMapping(controllerClass, "validate", "org.springframework.web.bind.annotation.PostMapping",
        "/validate", "lowcode:generator:query");
    assertMethodMapping(controllerClass, "execute", "org.springframework.web.bind.annotation.PostMapping",
        "/execute", "lowcode:generator:execute");
    assertMethodMapping(controllerClass, "listRecords", "org.springframework.web.bind.annotation.GetMapping",
        "/records", "lowcode:generator:query");
    assertMethodMapping(controllerClass, "listGenerationFiles", "org.springframework.web.bind.annotation.GetMapping",
        "/files", "lowcode:generator:query");
  }

  /**
   * 校验空生成预览请求会返回可读的参数错误。
   */
  @Test
  void shouldRejectNullPreviewRequestWithReadableMessage() {
    LowcodeGeneratorController controller = new LowcodeGeneratorController(new RecordingLowcodeGeneratorService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.preview(null));

    assertEquals("ZHYC_LOWCODE_GENERATION_PREVIEW_REQUEST_REQUIRED", exception.getCode());
    assertEquals("低代码生成预览请求不能为空", exception.getMessage());
  }

  /**
   * 校验空生成校验请求会返回可读的参数错误。
   */
  @Test
  void shouldRejectNullValidateRequestWithReadableMessage() {
    LowcodeGeneratorController controller = new LowcodeGeneratorController(new RecordingLowcodeGeneratorService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.validate(null));

    assertEquals("ZHYC_LOWCODE_GENERATION_VALIDATE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("低代码生成校验请求不能为空", exception.getMessage());
  }

  /**
   * 校验空生成执行请求会返回可读的参数错误。
   */
  @Test
  void shouldRejectNullExecuteRequestWithReadableMessage() {
    LowcodeGeneratorController controller = new LowcodeGeneratorController(new RecordingLowcodeGeneratorService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.execute(null));

    assertEquals("ZHYC_LOWCODE_GENERATION_EXECUTE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("低代码生成执行请求不能为空", exception.getMessage());
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

  /**
   * 记录调用的低代码生成服务桩。
   */
  private static class RecordingLowcodeGeneratorService implements LowcodeGeneratorService {

    @Override
    public List<CodeTemplateDescriptor> listTemplates(GenerationTarget target) {
      return List.of();
    }

    @Override
    public LowcodeGenerationValidationResult validate(LowcodeGenerationPreviewCommand command) {
      throw new AssertionError("空请求不应进入低代码生成校验服务");
    }

    @Override
    public List<GeneratedFile> preview(LowcodeGenerationPreviewCommand command) {
      throw new AssertionError("空请求不应进入低代码生成预览服务");
    }

    @Override
    public LowcodeGenerationRecord execute(LowcodeGenerationExecuteCommand command) {
      throw new AssertionError("空请求不应进入低代码生成执行服务");
    }

    @Override
    public List<LowcodeGenerationRecord> listRecords(String tenantId) {
      return List.of();
    }

    @Override
    public List<LowcodeGenerationFile> listGenerationFiles(String tenantId, Long recordId) {
      return List.of();
    }
  }
}
