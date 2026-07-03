/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permission;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 当前登录用户权限接口契约测试。
 */
class SysCurrentPermissionControllerContractTest {

    /**
     * 验证当前用户权限接口不依赖菜单维护权限，只要求登录态。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeCurrentPermissionRouteWithAuthenticationRequired() throws Exception {
        Class<?> controllerClass = Class.forName(
                "com.zhyc.system.permission.controller.SysCurrentPermissionController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/permissions");
        Method method = findMethod(controllerClass, "listCurrentPermissions");
        assertAnnotationValue(method, "org.springframework.web.bind.annotation.GetMapping", "value", "/current");
        assertAnnotation(method, "org.apache.shiro.authz.annotation.RequiresAuthentication");
    }

    private static Method findMethod(Class<?> controllerClass, String methodName) {
        return Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("缺少控制器方法: " + methodName));
    }

    private static void assertAnnotation(Object annotatedElement, String annotationName) {
        assertTrue(Arrays.stream(resolveAnnotations(annotatedElement))
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

    private static Annotation findAnnotation(Object annotatedElement, String annotationName) {
        return Arrays.stream(resolveAnnotations(annotatedElement))
                .filter(annotation -> annotation.annotationType().getName().equals(annotationName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("缺少注解: " + annotationName));
    }

    private static Annotation[] resolveAnnotations(Object annotatedElement) {
        return annotatedElement instanceof Class<?> targetClass
                ? targetClass.getAnnotations()
                : ((Method) annotatedElement).getAnnotations();
    }
}
