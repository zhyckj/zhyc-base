/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.i18n;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.i18n.controller.I18nController;
import com.zhyc.i18n.service.I18nMessageResponse;
import com.zhyc.i18n.service.I18nMessageSaveCommand;
import com.zhyc.i18n.service.I18nResolveCommand;
import com.zhyc.i18n.service.I18nResolveResponse;
import com.zhyc.i18n.service.I18nService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 国际化词条接口契约测试。
 */
class I18nControllerContractTest {

  /**
   * 验证国际化控制器声明路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeI18nRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.i18n.controller.I18nController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/i18n");
    assertMethodMapping(controllerClass, "listMessages", "org.springframework.web.bind.annotation.GetMapping",
        "/messages", "i18n:message:query");
    assertMethodMapping(controllerClass, "saveMessage", "org.springframework.web.bind.annotation.PostMapping",
        "/messages", "i18n:message:save");
    assertMethodMapping(controllerClass, "resolveMessages", "org.springframework.web.bind.annotation.PostMapping",
        "/messages/resolve", "i18n:message:resolve");
  }

  /**
   * 验证国际化词条保存空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullSaveCommand() {
    I18nController controller = new I18nController(new RejectingI18nService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.saveMessage(null));

    assertEquals("ZHYC_I18N_MESSAGE_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("国际化词条保存请求不能为空", exception.getMessage());
  }

  /**
   * 验证国际化批量解析空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullResolveCommand() {
    I18nController controller = new I18nController(new RejectingI18nService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.resolveMessages("tenant-a", null));

    assertEquals("ZHYC_I18N_MESSAGE_RESOLVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("国际化词条解析请求不能为空", exception.getMessage());
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
   * 拒绝被调用的国际化词条服务测试桩。
   */
  private static final class RejectingI18nService implements I18nService {

    /**
     * 返回空词条列表。
     *
     * @param tenantId 租户业务编码
     * @param locale 语言标识
     * @param status 词条状态
     * @return 空词条列表
     */
    @Override
    public List<I18nMessageResponse> listMessages(String tenantId, String locale, String status) {
      return List.of();
    }

    /**
     * 保存词条时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 词条保存命令
     */
    @Override
    public void saveMessage(I18nMessageSaveCommand command) {
      throw new AssertionError("国际化词条保存服务不应被调用");
    }

    /**
     * 返回默认文案。
     *
     * @param tenantId 租户业务编码
     * @param locale 语言标识
     * @param messageKey 词条键
     * @param defaultMessage 默认文案
     * @return 默认文案
     */
    @Override
    public String resolveMessage(String tenantId, String locale, String messageKey,
        String defaultMessage) {
      return defaultMessage;
    }

    /**
     * 批量解析词条时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 国际化词条批量解析命令
     * @return 解析响应
     */
    @Override
    public I18nResolveResponse resolveMessages(I18nResolveCommand command) {
      throw new AssertionError("国际化词条批量解析服务不应被调用");
    }
  }
}
