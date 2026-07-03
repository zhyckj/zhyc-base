/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统异常日志接口契约测试。
 */
class SysExceptionLogControllerContractTest {

    /**
     * 验证异常日志控制器暴露最近日志查询路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeRecentExceptionLogsRouteWithShiroPermission() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.exceptionlog.controller.SysExceptionLogController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/exception-logs");
        assertMethodMapping(controllerClass, "listRecent", "org.springframework.web.bind.annotation.GetMapping",
                "/recent", "system:audit:query");
    }

    /**
     * 断言控制器方法路由和权限配置。
     *
     * @param controllerClass 控制器类型
     * @param methodName 方法名
     * @param mappingAnnotation 路由注解类名
     * @param mappingValue 路由值
     * @param permission Shiro 权限标识
     * @throws Exception 读取注解属性失败时抛出
     */
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

    /**
     * 断言目标类型包含指定注解。
     *
     * @param targetClass 目标类型
     * @param annotationName 注解类名
     */
    private static void assertAnnotation(Class<?> targetClass, String annotationName) {
        assertTrue(Arrays.stream(targetClass.getAnnotations())
                .map(annotation -> annotation.annotationType().getName())
                .anyMatch(annotationName::equals), "缺少注解: " + annotationName);
    }

    /**
     * 断言注解属性值。
     *
     * @param annotatedElement 被注解元素
     * @param annotationName 注解类名
     * @param attributeName 注解属性名
     * @param expectedValue 期望值
     * @throws Exception 读取注解属性失败时抛出
     */
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

    /**
     * 查找指定注解。
     *
     * @param annotatedElement 被注解元素
     * @param annotationName 注解类名
     * @return 注解实例
     */
    private static Annotation findAnnotation(Object annotatedElement, String annotationName) {
        Annotation[] annotations = annotatedElement instanceof Class<?> targetClass
                ? targetClass.getAnnotations()
                : ((Method) annotatedElement).getAnnotations();
        return Arrays.stream(annotations)
                .filter(annotation -> annotation.annotationType().getName().equals(annotationName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("缺少注解: " + annotationName));
    }
}
