/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.file.object;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.file.object.controller.FileObjectController;
import com.zhyc.file.object.service.FileObjectQuery;
import com.zhyc.file.object.service.FileObjectRegisterCommand;
import com.zhyc.file.object.service.FileObjectResponse;
import com.zhyc.file.object.service.FileObjectService;
import com.zhyc.file.object.service.FileObjectUploadCommand;
import com.zhyc.file.object.service.FileObjectUploadResponse;
import com.zhyc.file.object.service.FileObjectUploadService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文件对象接口契约测试。
 */
class FileObjectControllerContractTest {

  /**
   * 验证文件对象控制器声明路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeObjectRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.file.object.controller.FileObjectController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/file/objects");
    assertMethodMapping(controllerClass, "listFiles", "org.springframework.web.bind.annotation.GetMapping",
        "", "file:object:query");
    assertMethodMapping(controllerClass, "register", "org.springframework.web.bind.annotation.PostMapping",
        "", "file:object:register");
    assertMethodMapping(controllerClass, "upload", "org.springframework.web.bind.annotation.PostMapping",
        "/upload", "file:object:upload");
  }

  /**
   * 验证文件对象登记空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullRegisterCommand() {
    FileObjectController controller = new FileObjectController(new RejectingObjectService(),
        new RejectingUploadService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.register(null));

    assertEquals("ZHYC_FILE_OBJECT_REGISTER_REQUEST_REQUIRED", exception.getCode());
    assertEquals("文件对象登记请求不能为空", exception.getMessage());
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
   * 拒绝被调用的文件对象服务测试桩。
   */
  private static final class RejectingObjectService implements FileObjectService {

    /**
     * 登记文件对象时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 文件对象登记命令
     * @return 不会返回
     */
    @Override
    public String register(FileObjectRegisterCommand command) {
      throw new AssertionError("文件对象登记服务不应被调用");
    }

    /**
     * 返回空文件对象分页。
     *
     * @param query 文件对象查询条件
     * @return 空文件对象分页
     */
    @Override
    public PageResult<FileObjectResponse> listFiles(FileObjectQuery query) {
      return PageResult.of(0, query.pageNo(), query.pageSize(), java.util.List.of());
    }
  }

  /**
   * 拒绝被调用的文件上传服务测试桩。
   */
  private static final class RejectingUploadService implements FileObjectUploadService {

    @Override
    public FileObjectUploadResponse upload(FileObjectUploadCommand command) {
      throw new AssertionError("文件上传服务不应被调用");
    }
  }
}
