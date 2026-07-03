/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.visual;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.visual.controller.VisualController;
import com.zhyc.visual.service.VisualDatasetPreviewResponse;
import com.zhyc.visual.service.VisualDatasetResponse;
import com.zhyc.visual.service.VisualDatasetSaveCommand;
import com.zhyc.visual.service.VisualReportResponse;
import com.zhyc.visual.service.VisualReportSaveCommand;
import com.zhyc.visual.service.VisualScreenResponse;
import com.zhyc.visual.service.VisualScreenSaveCommand;
import com.zhyc.visual.service.VisualService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 可视化报表接口契约测试。
 */
class VisualControllerContractTest {

  /**
   * 验证可视化报表控制器声明路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeVisualRoutesWithShiroPermission() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.visual.controller.VisualController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/visual");
    assertMethodMapping(controllerClass, "listDatasets", "org.springframework.web.bind.annotation.GetMapping",
        "/datasets", "visual:dataset:query");
    assertMethodMapping(controllerClass, "saveDataset", "org.springframework.web.bind.annotation.PostMapping",
        "/datasets", "visual:dataset:save");
    assertMethodMapping(controllerClass, "previewDataset", "org.springframework.web.bind.annotation.GetMapping",
        "/datasets/{datasetCode}/preview", "visual:dataset:query");
    assertMethodMapping(controllerClass, "listReports", "org.springframework.web.bind.annotation.GetMapping",
        "/reports", "visual:report:query");
    assertMethodMapping(controllerClass, "saveReport", "org.springframework.web.bind.annotation.PostMapping",
        "/reports", "visual:report:save");
    assertMethodMapping(controllerClass, "updateReportStatus", "org.springframework.web.bind.annotation.PostMapping",
        "/reports/{id}/status", "visual:report:publish");
    assertMethodMapping(controllerClass, "listScreens", "org.springframework.web.bind.annotation.GetMapping",
        "/screens", "visual:screen:query");
    assertMethodMapping(controllerClass, "saveScreen", "org.springframework.web.bind.annotation.PostMapping",
        "/screens", "visual:screen:save");
    assertMethodMapping(controllerClass, "updateScreenStatus", "org.springframework.web.bind.annotation.PostMapping",
        "/screens/{id}/status", "visual:screen:publish");
    assertPublicMethodMapping(controllerClass, "getPublishedScreen",
        "/public/screens/{tenantId}/{screenCode}");
    assertPublicMethodMapping(controllerClass, "previewPublishedScreenDataset",
        "/public/screens/{tenantId}/{screenCode}/datasets/{datasetCode}/preview");
    assertPublicMethodMapping(controllerClass, "getPublishedReport",
        "/public/reports/{tenantId}/{reportCode}");
    assertPublicMethodMapping(controllerClass, "previewPublishedReportDataset",
        "/public/reports/{tenantId}/{reportCode}/datasets/{datasetCode}/preview");
  }

  /**
   * 验证可视化写入接口拒绝空请求体，避免控制器向服务层传递无效命令。
   */
  @Test
  void shouldRejectNullWriteCommands() {
    VisualController controller = new VisualController(new RejectingVisualService());

    BusinessException datasetException = assertThrows(BusinessException.class,
        () -> controller.saveDataset(null));
    BusinessException reportException = assertThrows(BusinessException.class,
        () -> controller.saveReport(null));
    BusinessException reportStatusException = assertThrows(BusinessException.class,
        () -> controller.updateReportStatus("tenant-a", 1L, null));
    BusinessException screenException = assertThrows(BusinessException.class,
        () -> controller.saveScreen(null));
    BusinessException statusException = assertThrows(BusinessException.class,
        () -> controller.updateScreenStatus("tenant-a", 1L, null));

    assertEquals("ZHYC_VISUAL_DATASET_SAVE_REQUEST_REQUIRED", datasetException.getCode());
    assertEquals("可视化数据集保存请求不能为空", datasetException.getMessage());
    assertEquals("ZHYC_VISUAL_REPORT_SAVE_REQUEST_REQUIRED", reportException.getCode());
    assertEquals("可视化报表保存请求不能为空", reportException.getMessage());
    assertEquals("ZHYC_VISUAL_RESOURCE_STATUS_REQUEST_REQUIRED", reportStatusException.getCode());
    assertEquals("可视化资源状态变更请求不能为空", reportStatusException.getMessage());
    assertEquals("ZHYC_VISUAL_SCREEN_SAVE_REQUEST_REQUIRED", screenException.getCode());
    assertEquals("可视化大屏保存请求不能为空", screenException.getMessage());
    assertEquals("ZHYC_VISUAL_RESOURCE_STATUS_REQUEST_REQUIRED", statusException.getCode());
    assertEquals("可视化资源状态变更请求不能为空", statusException.getMessage());
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

  private static void assertPublicMethodMapping(Class<?> controllerClass, String methodName,
      String mappingValue) throws Exception {
    Method method = Arrays.stream(controllerClass.getDeclaredMethods())
        .filter(candidate -> candidate.getName().equals(methodName))
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少控制器方法: " + methodName));
    assertAnnotationValue(method, "org.springframework.web.bind.annotation.GetMapping", "value", mappingValue);
    assertFalse(hasAnnotation(method, "org.apache.shiro.authz.annotation.RequiresPermissions"),
        "公开大屏访问接口不应配置 Shiro 后台权限");
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

  private static boolean hasAnnotation(Method method, String annotationName) {
    return Arrays.stream(method.getAnnotations())
        .anyMatch(annotation -> annotation.annotationType().getName().equals(annotationName));
  }

  /**
   * 拒绝调用的可视化服务桩。
   *
   * <p>用于证明控制器在请求体为空时会提前失败，不会把无效命令继续传入服务层。</p>
   */
  private static final class RejectingVisualService implements VisualService {

    @Override
    public List<VisualDatasetResponse> listDatasets(String tenantId, String status) {
      return List.of();
    }

    @Override
    public void saveDataset(VisualDatasetSaveCommand command) {
      throw new AssertionError("可视化数据集保存服务不应被调用");
    }

    @Override
    public VisualDatasetPreviewResponse previewDataset(String tenantId, String datasetCode, Integer limit) {
      throw new AssertionError("可视化数据集预览服务不应被调用");
    }

    @Override
    public VisualDatasetPreviewResponse previewPublishedScreenDataset(String tenantId, String screenCode,
        String datasetCode, Integer limit) {
      return new VisualDatasetPreviewResponse(datasetCode, List.of("month", "amount"),
          List.of(), false, "数据源执行器未启用，当前返回字段解析和样例数据");
    }

    @Override
    public List<VisualReportResponse> listReports(String tenantId, String status) {
      return List.of();
    }

    @Override
    public void saveReport(VisualReportSaveCommand command) {
      throw new AssertionError("可视化报表保存服务不应被调用");
    }

    @Override
    public VisualReportResponse getPublishedReport(String tenantId, String reportCode) {
      return new VisualReportResponse(1L, tenantId, reportCode, "订单趋势", "sales_month",
          "line", "{}", "published", null, null);
    }

    @Override
    public VisualDatasetPreviewResponse previewPublishedReportDataset(String tenantId, String reportCode,
        String datasetCode, Integer limit) {
      return new VisualDatasetPreviewResponse(datasetCode, List.of("month", "amount"),
          List.of(), false, "数据源执行器未启用，当前返回字段解析和样例数据");
    }

    @Override
    public void updateReportStatus(String tenantId, Long id, String status) {
      throw new AssertionError("可视化报表状态变更服务不应被调用");
    }

    @Override
    public List<VisualScreenResponse> listScreens(String tenantId, String status) {
      return List.of();
    }

    @Override
    public VisualScreenResponse getPublishedScreen(String tenantId, String screenCode) {
      return new VisualScreenResponse(1L, tenantId, screenCode, "运营大屏", "[]",
          "published", null, null);
    }

    @Override
    public void saveScreen(VisualScreenSaveCommand command) {
      throw new AssertionError("可视化大屏保存服务不应被调用");
    }

    @Override
    public void updateScreenStatus(String tenantId, Long id, String status) {
      throw new AssertionError("可视化资源状态变更服务不应被调用");
    }
  }
}
