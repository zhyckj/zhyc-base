/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.search;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.search.controller.SearchController;
import com.zhyc.search.service.SearchIndexConfigResponse;
import com.zhyc.search.service.SearchIndexConfigSaveCommand;
import com.zhyc.search.service.SearchQueryCommand;
import com.zhyc.search.service.SearchQueryLogResponse;
import com.zhyc.search.service.SearchQueryResponse;
import com.zhyc.search.service.SearchRebuildTaskCommand;
import com.zhyc.search.service.SearchRebuildTaskResponse;
import com.zhyc.search.service.SearchService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 全文检索接口契约测试。
 */
class SearchControllerContractTest {

  /**
   * 验证全文检索控制器声明路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeSearchRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.search.controller.SearchController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/search");
    assertMethodMapping(controllerClass, "listIndexConfigs", "org.springframework.web.bind.annotation.GetMapping",
        "/index-configs", "search:index:query");
    assertMethodMapping(controllerClass, "saveIndexConfig", "org.springframework.web.bind.annotation.PostMapping",
        "/index-configs", "search:index:save");
    assertMethodMapping(controllerClass, "createRebuildTask", "org.springframework.web.bind.annotation.PostMapping",
        "/rebuild-tasks", "search:task:create");
    assertMethodMapping(controllerClass, "search", "org.springframework.web.bind.annotation.PostMapping",
        "/query", "search:query:execute");
  }

  /**
   * 验证索引配置保存空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullIndexConfigSaveCommand() {
    SearchController controller = new SearchController(new RejectingSearchService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.saveIndexConfig(null));

    assertEquals("ZHYC_SEARCH_INDEX_CONFIG_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("索引配置保存请求不能为空", exception.getMessage());
  }

  /**
   * 验证索引重建任务创建空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullRebuildTaskCommand() {
    SearchController controller = new SearchController(new RejectingSearchService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.createRebuildTask(null));

    assertEquals("ZHYC_SEARCH_REBUILD_TASK_CREATE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("索引重建任务创建请求不能为空", exception.getMessage());
  }

  /**
   * 验证全文检索查询空请求体会返回明确错误。
   */
  @Test
  void shouldRejectNullSearchQueryCommand() {
    SearchController controller = new SearchController(new RejectingSearchService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.search(null));

    assertEquals("ZHYC_SEARCH_QUERY_REQUEST_REQUIRED", exception.getCode());
    assertEquals("全文检索查询请求不能为空", exception.getMessage());
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
   * 拒绝被调用的全文检索服务测试桩。
   */
  private static final class RejectingSearchService implements SearchService {

    /**
     * 返回空索引配置列表。
     *
     * @param tenantId 租户业务编码
     * @param status 索引状态
     * @return 空索引配置列表
     */
    @Override
    public List<SearchIndexConfigResponse> listIndexConfigs(String tenantId, String status) {
      return List.of();
    }

    /**
     * 索引配置保存时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 索引配置保存命令
     */
    @Override
    public void saveIndexConfig(SearchIndexConfigSaveCommand command) {
      throw new AssertionError("索引配置保存服务不应被调用");
    }

    /**
     * 创建重建任务时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 重建任务创建命令
     */
    @Override
    public void createRebuildTask(SearchRebuildTaskCommand command) {
      throw new AssertionError("索引重建任务服务不应被调用");
    }

    /**
     * 返回空索引重建任务列表。
     *
     * @param tenantId 租户业务编码
     * @param indexCode 索引编码
     * @return 空索引重建任务列表
     */
    @Override
    public List<SearchRebuildTaskResponse> listRebuildTasks(String tenantId, String indexCode) {
      return List.of();
    }

    /**
     * 执行查询时直接失败，用于确认空请求体被 Controller 拦截。
     *
     * @param command 查询命令
     * @return 不会返回
     */
    @Override
    public SearchQueryResponse search(SearchQueryCommand command) {
      throw new AssertionError("全文检索查询服务不应被调用");
    }

    /**
     * 返回空检索日志列表。
     *
     * @param tenantId 租户业务编码
     * @param indexCode 索引编码
     * @return 空检索日志列表
     */
    @Override
    public List<SearchQueryLogResponse> listQueryLogs(String tenantId, String indexCode) {
      return List.of();
    }
  }
}
