/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.template;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.message.template.controller.MsgTemplateController;
import com.zhyc.message.template.service.MsgTemplateResponse;
import com.zhyc.message.template.service.MsgTemplateSaveCommand;
import com.zhyc.message.template.service.MsgTemplateService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 消息模板接口契约测试。
 */
class MsgTemplateControllerContractTest {

  /**
   * 验证消息模板控制器声明路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeTemplateRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.message.template.controller.MsgTemplateController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/message/templates");
    assertMethodMapping(controllerClass, "listTemplates", "org.springframework.web.bind.annotation.GetMapping",
        "", "message:template:query");
    assertMethodMapping(controllerClass, "save", "org.springframework.web.bind.annotation.PutMapping",
        "", "message:template:save");
  }

  /**
   * 验证消息模板保存空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullSaveCommand() {
    MsgTemplateController controller = new MsgTemplateController(new RejectingTemplateService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.save(null));

    assertEquals("ZHYC_MESSAGE_TEMPLATE_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("消息模板保存请求不能为空", exception.getMessage());
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
   * 拒绝被调用的消息模板服务测试桩。
   */
  private static final class RejectingTemplateService implements MsgTemplateService {

    /**
     * 返回空模板列表。
     *
     * @param tenantId 租户业务编码
     * @return 空模板列表
     */
    @Override
    public List<MsgTemplateResponse> listTemplates(String tenantId) {
      return List.of();
    }

    /**
     * 保存模板时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 消息模板保存命令
     */
    @Override
    public void save(MsgTemplateSaveCommand command) {
      throw new AssertionError("消息模板保存服务不应被调用");
    }
  }
}
