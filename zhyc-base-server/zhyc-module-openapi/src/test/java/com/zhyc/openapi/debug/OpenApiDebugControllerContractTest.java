/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.debug.controller.OpenApiDebugController;
import com.zhyc.openapi.debug.controller.OpenApiDebugInvokeRequest;
import com.zhyc.openapi.debug.service.OpenApiDebugCommand;
import com.zhyc.openapi.debug.service.OpenApiDebugResponse;
import com.zhyc.openapi.debug.service.OpenApiDebugService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * 开放 API 调试代理接口契约测试。
 */
class OpenApiDebugControllerContractTest {

    /**
     * 验证调试代理控制器暴露后台代理路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeDebugInvokeRouteWithShiroPermission() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.openapi.debug.controller.OpenApiDebugController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/openapi/debug");
        assertMethodMapping(controllerClass, "invoke", "org.springframework.web.bind.annotation.PostMapping",
                "/invoke", "openapi:debug:invoke");
        assertRequestHeaderParameter(controllerClass, "invoke", "X-ZHYC-Tenant-Id");
    }

    /**
     * 验证空调试请求会被转换为可读的参数错误。
     */
    @Test
    void shouldRejectNullInvokeRequestWithReadableMessage() {
        OpenApiDebugController controller = new OpenApiDebugController(new RecordingOpenApiDebugService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.invoke("tenant-a", null));

        assertEquals("ZHYC_OPENAPI_DEBUG_REQUEST_REQUIRED", exception.getCode());
        assertEquals("开放 API 调试请求不能为空", exception.getMessage());
    }

    /**
     * 验证调试代理拒绝请求体租户与当前请求头租户不一致，避免跨租户代理调用开放 API。
     *
     * @throws Exception 反射构造请求失败时抛出
     */
    @Test
    void shouldRejectTenantMismatchBeforeInvokingDebugService() throws Exception {
        OpenApiDebugController controller = new OpenApiDebugController(new RecordingOpenApiDebugService());
        OpenApiDebugInvokeRequest request = requestWithTenantId("tenant-b");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.invoke("tenant-a", request));

        assertEquals("ZHYC_OPENAPI_DEBUG_TENANT_MISMATCH", exception.getCode());
        assertEquals("开放 API 调试租户与当前请求租户不一致", exception.getMessage());
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

    private static void assertRequestHeaderParameter(Class<?> controllerClass, String methodName,
                                                     String expectedHeader) throws Exception {
        Method method = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("缺少控制器方法: " + methodName));
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        boolean found = Arrays.stream(parameterAnnotations)
                .flatMap(Arrays::stream)
                .filter(annotation -> annotation.annotationType().getName()
                        .equals("org.springframework.web.bind.annotation.RequestHeader"))
                .anyMatch(annotation -> {
                    try {
                        Method value = annotation.annotationType().getMethod("value");
                        return expectedHeader.equals(value.invoke(annotation));
                    } catch (ReflectiveOperationException ex) {
                        throw new AssertionError("读取 RequestHeader 注解失败", ex);
                    }
                });
        assertTrue(found, "缺少租户请求头参数: " + expectedHeader);
    }

    private static OpenApiDebugInvokeRequest requestWithTenantId(String tenantId) throws Exception {
        OpenApiDebugInvokeRequest request = new OpenApiDebugInvokeRequest();
        Field tenantIdField = OpenApiDebugInvokeRequest.class.getDeclaredField("tenantId");
        tenantIdField.setAccessible(true);
        tenantIdField.set(request, tenantId);
        return request;
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
     * 记录调用的开放 API 调试服务桩。
     */
    private static class RecordingOpenApiDebugService implements OpenApiDebugService {

        @Override
        public OpenApiDebugResponse invoke(OpenApiDebugCommand command) {
            throw new AssertionError("空请求不应进入开放 API 调试服务");
        }
    }
}
