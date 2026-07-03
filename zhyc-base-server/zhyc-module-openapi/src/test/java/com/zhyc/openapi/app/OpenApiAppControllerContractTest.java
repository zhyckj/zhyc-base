/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.controller.OpenApiAppController;
import com.zhyc.openapi.app.service.OpenApiAppResponse;
import com.zhyc.openapi.app.service.OpenApiAppSaveCommand;
import com.zhyc.openapi.app.service.OpenApiAppService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 开发者应用接口契约测试。
 */
class OpenApiAppControllerContractTest {

    /**
     * 验证开发者应用控制器暴露开放平台路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeOpenApiAppRoutesWithShiroPermissions() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.openapi.app.controller.OpenApiAppController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/openapi/apps");
        assertMethodMapping(controllerClass, "listApps", "org.springframework.web.bind.annotation.GetMapping",
                "", "openapi:app:query");
        assertMethodMapping(controllerClass, "save", "org.springframework.web.bind.annotation.PutMapping",
                "", "openapi:app:save");
    }

    /**
     * 验证空开发者应用保存请求会被转换为可读的参数错误。
     */
    @Test
    void shouldRejectNullSaveRequestWithReadableMessage() {
        OpenApiAppController controller = new OpenApiAppController(new RecordingOpenApiAppService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.save(null));

        assertEquals("ZHYC_OPENAPI_APP_SAVE_REQUEST_REQUIRED", exception.getCode());
        assertEquals("开发者应用保存请求不能为空", exception.getMessage());
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
     * 记录调用的开发者应用服务桩。
     */
    private static class RecordingOpenApiAppService implements OpenApiAppService {

        @Override
        public List<OpenApiAppResponse> listApps(String tenantId) {
            return List.of();
        }

        @Override
        public void save(OpenApiAppSaveCommand command) {
            throw new AssertionError("空请求不应进入开发者应用保存服务");
        }
    }
}
