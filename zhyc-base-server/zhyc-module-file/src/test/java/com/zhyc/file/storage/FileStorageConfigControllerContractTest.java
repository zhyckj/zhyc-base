/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.storage;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.storage.controller.FileStorageConfigController;
import com.zhyc.file.storage.service.FileStorageConfigResponse;
import com.zhyc.file.storage.service.FileStorageConfigSaveCommand;
import com.zhyc.file.storage.service.FileStorageConfigService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件存储配置接口契约测试。
 */
class FileStorageConfigControllerContractTest {

  /**
   * 验证文件存储配置控制器声明路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeStorageRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.file.storage.controller.FileStorageConfigController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/file/storage-configs");
    assertMethodMapping(controllerClass, "listConfigs", "org.springframework.web.bind.annotation.GetMapping",
        "", "file:storage:query");
    assertMethodMapping(controllerClass, "save", "org.springframework.web.bind.annotation.PutMapping",
        "", "file:storage:save");
  }

  /**
   * 验证文件存储配置保存空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullSaveCommand() {
    FileStorageConfigController controller = new FileStorageConfigController(
        new RejectingStorageConfigService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.save(null));

    assertEquals("ZHYC_FILE_STORAGE_CONFIG_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("文件存储配置保存请求不能为空", exception.getMessage());
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
   * 拒绝被调用的文件存储配置服务测试桩。
   */
  private static final class RejectingStorageConfigService implements FileStorageConfigService {

    /**
     * 返回空存储配置列表。
     *
     * @param tenantId 租户业务编码
     * @return 空存储配置列表
     */
    @Override
    public List<FileStorageConfigResponse> listConfigs(String tenantId) {
      return List.of();
    }

    /**
     * 保存存储配置时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 文件存储配置保存命令
     */
    @Override
    public void save(FileStorageConfigSaveCommand command) {
      throw new AssertionError("文件存储配置保存服务不应被调用");
    }
  }
}
