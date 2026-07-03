/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.controller;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodePageModel;
import com.zhyc.lowcode.metadata.domain.LowcodePhysicalTable;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import com.zhyc.lowcode.metadata.domain.LowcodeTableRelation;
import com.zhyc.lowcode.metadata.service.LowcodeDataSourceConnectionTestResult;
import com.zhyc.lowcode.metadata.service.LowcodeMetadataService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 低代码元数据管理接口契约测试。
 */
class LowcodeMetadataControllerContractTest {

  /**
   * 校验控制器暴露后台管理端需要的路由和 Shiro 权限。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldExposeMetadataRoutesWithShiroPermissions() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.lowcode.metadata.controller.LowcodeMetadataController");

    assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
    assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
        "value", "/lowcode/metadata");
    assertMethodMapping(controllerClass, "listDataSources", "org.springframework.web.bind.annotation.GetMapping",
        "/data-sources", "lowcode:datasource:query");
    assertMethodMapping(controllerClass, "saveDataSource", "org.springframework.web.bind.annotation.PostMapping",
        "/data-sources", "lowcode:datasource:save");
    assertMethodMapping(controllerClass, "getDataSource", "org.springframework.web.bind.annotation.GetMapping",
        "/data-sources/{code}", "lowcode:datasource:query");
    assertMethodMapping(controllerClass, "testDataSourceConnection", "org.springframework.web.bind.annotation.PostMapping",
        "/data-sources/test-connection", "lowcode:datasource:test");
    assertMethodMapping(controllerClass, "listPhysicalTables", "org.springframework.web.bind.annotation.GetMapping",
        "/data-sources/{dataSourceId}/tables", "lowcode:table:import");
    assertMethodMapping(controllerClass, "listTableModels", "org.springframework.web.bind.annotation.GetMapping",
        "/table-models", "lowcode:table:query");
    assertMethodMapping(controllerClass, "saveTableModel", "org.springframework.web.bind.annotation.PostMapping",
        "/table-models", "lowcode:table:save");
    assertMethodMapping(controllerClass, "getTableModel", "org.springframework.web.bind.annotation.GetMapping",
        "/table-models/{code}", "lowcode:table:query");
    assertMethodMapping(controllerClass, "importTableModel", "org.springframework.web.bind.annotation.PostMapping",
        "/table-models/import", "lowcode:table:import");
    assertMethodMapping(controllerClass, "publishTableModel", "org.springframework.web.bind.annotation.PostMapping",
        "/table-models/{code}/publish", "lowcode:table:publish");
    assertMethodMapping(controllerClass, "listTableRelations", "org.springframework.web.bind.annotation.GetMapping",
        "/table-relations", "lowcode:relation:query");
    assertMethodMapping(controllerClass, "saveTableRelation", "org.springframework.web.bind.annotation.PostMapping",
        "/table-relations", "lowcode:relation:save");
    assertMethodMapping(controllerClass, "listPageModels", "org.springframework.web.bind.annotation.GetMapping",
        "/page-models", "lowcode:page:query");
    assertMethodMapping(controllerClass, "savePageModel", "org.springframework.web.bind.annotation.PostMapping",
        "/page-models", "lowcode:page:save");
  }

  /**
   * 校验路径变量显式声明变量名，避免本地或生产构建未保留 Java 参数名时接口绑定失败。
   *
   * @throws Exception 反射读取控制器失败时抛出
   */
  @Test
  void shouldDeclarePathVariableNamesExplicitly() throws Exception {
    Class<?> controllerClass = Class.forName("com.zhyc.lowcode.metadata.controller.LowcodeMetadataController");

    assertPathVariableName(controllerClass, "getDataSource", "code");
    assertPathVariableName(controllerClass, "listPhysicalTables", "dataSourceId");
    assertPathVariableName(controllerClass, "getTableModel", "code");
    assertPathVariableName(controllerClass, "publishTableModel", "code");
  }

  /**
   * 校验空数据源保存请求会返回可读的参数错误。
   */
  @Test
  void shouldRejectNullDataSourceSaveRequestWithReadableMessage() {
    LowcodeMetadataController controller = new LowcodeMetadataController(new RecordingLowcodeMetadataService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.saveDataSource(null));

    assertEquals("ZHYC_LOWCODE_METADATA_DATASOURCE_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("数据源保存请求不能为空", exception.getMessage());
  }

  /**
   * 校验空数据源连接测试请求会返回可读的参数错误。
   */
  @Test
  void shouldRejectNullConnectionTestRequestWithReadableMessage() {
    LowcodeMetadataController controller = new LowcodeMetadataController(new RecordingLowcodeMetadataService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.testDataSourceConnection(null));

    assertEquals("ZHYC_LOWCODE_METADATA_DATASOURCE_TEST_REQUEST_REQUIRED", exception.getCode());
    assertEquals("数据源连接测试请求不能为空", exception.getMessage());
  }

  /**
   * 校验空表模型保存请求会返回可读的参数错误。
   */
  @Test
  void shouldRejectNullTableModelSaveRequestWithReadableMessage() {
    LowcodeMetadataController controller = new LowcodeMetadataController(new RecordingLowcodeMetadataService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.saveTableModel(null));

    assertEquals("ZHYC_LOWCODE_METADATA_TABLE_MODEL_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("表模型保存请求不能为空", exception.getMessage());
  }

  /**
   * 校验空物理表导入请求会返回可读的参数错误。
   */
  @Test
  void shouldRejectNullPhysicalTableImportRequestWithReadableMessage() {
    LowcodeMetadataController controller = new LowcodeMetadataController(new RecordingLowcodeMetadataService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.importTableModel(null));

    assertEquals("ZHYC_LOWCODE_METADATA_PHYSICAL_TABLE_IMPORT_REQUEST_REQUIRED", exception.getCode());
    assertEquals("物理表导入请求不能为空", exception.getMessage());
  }

  /**
   * 校验空表关系保存请求会返回可读的参数错误。
   */
  @Test
  void shouldRejectNullTableRelationSaveRequestWithReadableMessage() {
    LowcodeMetadataController controller = new LowcodeMetadataController(new RecordingLowcodeMetadataService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.saveTableRelation(null));

    assertEquals("ZHYC_LOWCODE_METADATA_TABLE_RELATION_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("表关系保存请求不能为空", exception.getMessage());
  }

  /**
   * 校验空页面模型保存请求会返回可读的参数错误。
   */
  @Test
  void shouldRejectNullPageModelSaveRequestWithReadableMessage() {
    LowcodeMetadataController controller = new LowcodeMetadataController(new RecordingLowcodeMetadataService());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> controller.savePageModel(null));

    assertEquals("ZHYC_LOWCODE_METADATA_PAGE_MODEL_SAVE_REQUEST_REQUIRED", exception.getCode());
    assertEquals("页面模型保存请求不能为空", exception.getMessage());
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

  private static void assertPathVariableName(Class<?> controllerClass, String methodName, String expectedName)
      throws Exception {
    Method method = Arrays.stream(controllerClass.getDeclaredMethods())
        .filter(candidate -> candidate.getName().equals(methodName))
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少控制器方法: " + methodName));
    Annotation annotation = Arrays.stream(method.getParameters())
        .map(Parameter::getAnnotations)
        .flatMap(Arrays::stream)
        .filter(candidate -> candidate.annotationType().getName()
            .equals("org.springframework.web.bind.annotation.PathVariable"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("缺少路径变量注解: " + methodName));
    Method valueAttribute = annotation.annotationType().getMethod("value");
    assertEquals(expectedName, valueAttribute.invoke(annotation));
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
   * 记录调用的低代码元数据服务桩。
   */
  private static class RecordingLowcodeMetadataService implements LowcodeMetadataService {

    @Override
    public LowcodeDataSource saveDataSource(LowcodeDataSource dataSource) {
      throw new AssertionError("空请求不应进入数据源保存服务");
    }

    @Override
    public LowcodeDataSource getDataSource(String tenantId, String code) {
      return null;
    }

    @Override
    public List<LowcodeDataSource> listDataSources(String tenantId) {
      return List.of();
    }

    @Override
    public List<LowcodePhysicalTable> listPhysicalTables(String tenantId, Long dataSourceId) {
      return List.of();
    }

    @Override
    public LowcodeDataSourceConnectionTestResult testDataSourceConnection(String tenantId, String code) {
      throw new AssertionError("空请求不应进入数据源连接测试服务");
    }

    @Override
    public LowcodeTableModel saveTableModel(LowcodeTableModel tableModel) {
      throw new AssertionError("空请求不应进入表模型保存服务");
    }

    @Override
    public LowcodeTableModel getTableModel(String tenantId, String code) {
      return null;
    }

    @Override
    public LowcodeTableModel importTableModel(String tenantId, Long dataSourceId, String tableName,
                                              String modelCode, String modelName) {
      throw new AssertionError("空请求不应进入物理表导入服务");
    }

    @Override
    public List<LowcodeTableModel> listTableModels(String tenantId) {
      return List.of();
    }

    @Override
    public LowcodeTableModel publishTableModel(String tenantId, String code) {
      return null;
    }

    @Override
    public LowcodeTableRelation saveTableRelation(LowcodeTableRelation relation) {
      throw new AssertionError("空请求不应进入表关系保存服务");
    }

    @Override
    public List<LowcodeTableRelation> listTableRelations(String tenantId) {
      return List.of();
    }

    @Override
    public LowcodePageModel savePageModel(LowcodePageModel pageModel) {
      throw new AssertionError("空请求不应进入页面模型保存服务");
    }

    @Override
    public List<LowcodePageModel> listPageModels(String tenantId) {
      return List.of();
    }
  }
}
