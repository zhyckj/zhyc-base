/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.catalog.controller.OpenApiCatalogController;
import com.zhyc.openapi.catalog.service.OpenApiCatalogResponse;
import com.zhyc.openapi.catalog.service.OpenApiCatalogSaveCommand;
import com.zhyc.openapi.catalog.service.OpenApiCatalogService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开放 API 目录接口契约测试。
 */
class OpenApiCatalogControllerContractTest {

    /**
     * 验证 API 目录控制器暴露开放平台路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeCatalogRoutesWithShiroPermissions() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.openapi.catalog.controller.OpenApiCatalogController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/openapi/catalogs");
        assertMethodMapping(controllerClass, "listCatalogs", "org.springframework.web.bind.annotation.GetMapping",
                "", "openapi:catalog:query");
        assertMethodMapping(controllerClass, "save", "org.springframework.web.bind.annotation.PutMapping",
                "", "openapi:catalog:save");
    }

    /**
     * 验证空 API 目录保存请求会被转换为可读的参数错误。
     */
    @Test
    void shouldRejectNullSaveRequestWithReadableMessage() {
        OpenApiCatalogController controller = new OpenApiCatalogController(new RecordingOpenApiCatalogService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.save(null));

        assertEquals("ZHYC_OPENAPI_CATALOG_SAVE_REQUEST_REQUIRED", exception.getCode());
        assertEquals("API 目录保存请求不能为空", exception.getMessage());
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
     * 记录调用的 API 目录服务桩。
     */
    private static class RecordingOpenApiCatalogService implements OpenApiCatalogService {

        @Override
        public List<OpenApiCatalogResponse> listCatalogs(String groupCode) {
            return List.of();
        }

        @Override
        public void save(OpenApiCatalogSaveCommand command) {
            throw new AssertionError("空请求不应进入 API 目录保存服务");
        }
    }
}
