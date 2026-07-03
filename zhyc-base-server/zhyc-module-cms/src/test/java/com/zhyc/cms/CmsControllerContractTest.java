/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.cms;

import com.zhyc.cms.controller.CmsController;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.cms.service.CmsChannelResponse;
import com.zhyc.cms.service.CmsChannelSaveCommand;
import com.zhyc.cms.service.CmsContentResponse;
import com.zhyc.cms.service.CmsContentSaveCommand;
import com.zhyc.cms.service.CmsService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内容管理接口契约测试。
 */
class CmsControllerContractTest {

  /**
   * 验证内容管理控制器声明路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeCmsRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.cms.controller.CmsController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/cms");
    assertMethodMapping(controllerClass, "listChannels", "org.springframework.web.bind.annotation.GetMapping",
        "/channels", "cms:channel:query");
    assertMethodMapping(controllerClass, "saveChannel", "org.springframework.web.bind.annotation.PostMapping",
        "/channels", "cms:channel:save");
    assertMethodMapping(controllerClass, "listContents", "org.springframework.web.bind.annotation.GetMapping",
        "/contents", "cms:content:query");
    assertMethodMapping(controllerClass, "saveContent", "org.springframework.web.bind.annotation.PostMapping",
        "/contents", "cms:content:save");
    assertMethodMapping(controllerClass, "updateContentStatus", "org.springframework.web.bind.annotation.PostMapping",
        "/contents/{id}/status", "cms:content:publish");
  }

  /**
   * 验证内容栏目保存空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullChannelSaveCommand() {
    CmsController controller = new CmsController(new RejectingCmsService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.saveChannel(null));

    assertEquals("ZHYC_CMS_CHANNEL_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("内容栏目保存请求不能为空", exception.getMessage());
  }

  /**
   * 验证内容文章保存空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullContentSaveCommand() {
    CmsController controller = new CmsController(new RejectingCmsService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.saveContent(null));

    assertEquals("ZHYC_CMS_CONTENT_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("内容文章保存请求不能为空", exception.getMessage());
  }

  /**
   * 验证内容文章状态变更空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullContentStatusCommand() {
    CmsController controller = new CmsController(new RejectingCmsService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.updateContentStatus("tenant-a", 1001L, null));

    assertEquals("ZHYC_CMS_CONTENT_STATUS_REQUEST_REQUIRED", exception.getCode());
    assertEquals("内容文章状态变更请求不能为空", exception.getMessage());
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
   * 拒绝被调用的内容管理服务测试桩。
   */
  private static final class RejectingCmsService implements CmsService {

    /**
     * 返回空栏目列表。
     *
     * @param tenantId 租户业务编码
     * @param status 栏目状态
     * @return 空栏目列表
     */
    @Override
    public List<CmsChannelResponse> listChannels(String tenantId, String status) {
      return List.of();
    }

    /**
     * 保存栏目时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 内容栏目保存命令
     */
    @Override
    public void saveChannel(CmsChannelSaveCommand command) {
      throw new AssertionError("内容栏目保存服务不应被调用");
    }

    /**
     * 返回空文章列表。
     *
     * @param tenantId 租户业务编码
     * @param channelCode 栏目编码
     * @param status 文章状态
     * @return 空文章列表
     */
    @Override
    public List<CmsContentResponse> listContents(String tenantId, String channelCode,
        String status) {
      return List.of();
    }

    /**
     * 保存文章时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 内容文章保存命令
     */
    @Override
    public void saveContent(CmsContentSaveCommand command) {
      throw new AssertionError("内容文章保存服务不应被调用");
    }

    /**
     * 更新文章状态时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param tenantId 租户业务编码
     * @param id 文章主键
     * @param status 文章状态
     */
    @Override
    public void updateContentStatus(String tenantId, Long id, String status) {
      throw new AssertionError("内容文章状态变更服务不应被调用");
    }
  }
}
