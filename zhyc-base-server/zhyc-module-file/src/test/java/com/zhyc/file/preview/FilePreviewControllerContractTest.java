/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.preview;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.preview.controller.FilePreviewController;
import com.zhyc.file.preview.service.FilePreviewCreateCommand;
import com.zhyc.file.preview.service.FilePreviewLogQuery;
import com.zhyc.file.preview.service.FilePreviewLogResponse;
import com.zhyc.file.preview.service.FilePreviewRenderResponse;
import com.zhyc.file.preview.service.FilePreviewResponse;
import com.zhyc.file.preview.service.FilePreviewService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件预览接口契约测试。
 */
class FilePreviewControllerContractTest {

  /**
   * 验证文件预览控制器声明路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposePreviewRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.file.preview.controller.FilePreviewController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/file/preview");
    assertMethodMapping(controllerClass, "createPreview", "org.springframework.web.bind.annotation.PostMapping",
        "", "file:preview:create");
    assertMethodMapping(controllerClass, "renderPreview", "org.springframework.web.bind.annotation.GetMapping",
        "/render/{fileCode}", "file:preview:view");
    assertMethodMapping(controllerClass, "listPreviewLogs", "org.springframework.web.bind.annotation.GetMapping",
        "/logs", "file:preview:query");
  }

  /**
   * 验证文件预览创建空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullCreatePreviewCommand() {
    FilePreviewController controller = new FilePreviewController(new RejectingPreviewService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.createPreview(null));

    assertEquals("ZHYC_FILE_PREVIEW_CREATE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("文件预览创建请求不能为空", exception.getMessage());
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
   * 拒绝被调用的文件预览服务测试桩。
   */
  private static final class RejectingPreviewService implements FilePreviewService {

    /**
     * 创建预览时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 文件预览创建命令
     * @return 不会返回
     */
    @Override
    public FilePreviewResponse createPreview(FilePreviewCreateCommand command) {
      throw new AssertionError("文件预览创建服务不应被调用");
    }

    /**
     * 渲染预览时直接失败，用于避免测试桩被误调用。
     *
     * @param tenantId 租户业务编码
     * @param fileCode 文件业务编码
     * @param previewType 预览类型
     * @return 不会返回
     */
    @Override
    public FilePreviewRenderResponse renderPreview(String tenantId, String fileCode, String previewType) {
      throw new AssertionError("文件预览渲染服务不应被调用");
    }

    /**
     * 返回空预览日志列表。
     *
     * @param query 文件预览日志查询条件
     * @return 空预览日志列表
     */
    @Override
    public List<FilePreviewLogResponse> listPreviewLogs(FilePreviewLogQuery query) {
      return List.of();
    }
  }
}
