/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.message.inbox;

import com.zhyc.common.api.PageResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.message.inbox.controller.MsgMessageController;
import com.zhyc.message.inbox.service.MsgMessageQuery;
import com.zhyc.message.inbox.service.MsgMessageResponse;
import com.zhyc.message.inbox.service.MsgMessageSendCommand;
import com.zhyc.message.inbox.service.MsgMessageService;
import com.zhyc.message.inbox.service.MsgMessageTemplateSendCommand;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 站内消息接口契约测试。
 */
class MsgMessageControllerContractTest {

  /**
   * 验证站内消息控制器声明路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeInboxRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.message.inbox.controller.MsgMessageController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/message/inbox");
    assertMethodMapping(controllerClass, "send", "org.springframework.web.bind.annotation.PostMapping",
        "", "message:inbox:send");
    assertMethodMapping(controllerClass, "sendByTemplate", "org.springframework.web.bind.annotation.PostMapping",
        "/template", "message:inbox:send");
    assertMethodMapping(controllerClass, "listMessages", "org.springframework.web.bind.annotation.GetMapping",
        "", "message:inbox:query");
    assertMethodMapping(controllerClass, "markRead", "org.springframework.web.bind.annotation.PatchMapping",
        "/{messageCode}/read", "message:inbox:read");
  }

  /**
   * 验证站内消息发送空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullSendCommand() {
    MsgMessageController controller = new MsgMessageController(new RejectingMessageService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.send(null));

    assertEquals("ZHYC_MESSAGE_INBOX_SEND_REQUEST_REQUIRED", exception.getCode());
    assertEquals("站内消息发送请求不能为空", exception.getMessage());
  }

  /**
   * 验证按模板发送站内消息空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullTemplateSendCommand() {
    MsgMessageController controller = new MsgMessageController(new RejectingMessageService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.sendByTemplate(null));

    assertEquals("ZHYC_MESSAGE_INBOX_TEMPLATE_SEND_REQUEST_REQUIRED", exception.getCode());
    assertEquals("站内消息模板发送请求不能为空", exception.getMessage());
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
   * 拒绝被调用的站内消息服务测试桩。
   */
  private static final class RejectingMessageService implements MsgMessageService {

    /**
     * 发送消息时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 站内消息发送命令
     * @return 不会返回
     */
    @Override
    public String send(MsgMessageSendCommand command) {
      throw new AssertionError("站内消息发送服务不应被调用");
    }

    /**
     * 按模板发送消息时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 站内消息模板发送命令
     * @return 不会返回
     */
    @Override
    public String sendByTemplate(MsgMessageTemplateSendCommand command) {
      throw new AssertionError("站内消息模板发送服务不应被调用");
    }

    /**
     * 返回空分页结果。
     *
     * @param query 消息查询条件
     * @return 空分页结果
     */
    @Override
    public PageResult<MsgMessageResponse> listMessages(MsgMessageQuery query) {
      return PageResult.of(0, query.pageNo(), query.pageSize(), java.util.List.of());
    }

    /**
     * 标记已读时不做处理。
     *
     * @param tenantId 租户业务编码
     * @param messageCode 消息编码
     * @param receiverId 接收人用户 ID
     */
    @Override
    public void markRead(String tenantId, String messageCode, Long receiverId) {
    }
  }
}
