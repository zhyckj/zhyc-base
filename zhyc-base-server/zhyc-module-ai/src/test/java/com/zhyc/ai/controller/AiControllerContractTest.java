/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.controller;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AI 能力中心控制器契约测试。
 */
class AiControllerContractTest {

    @Test
    void shouldExposeProviderModelAppPromptAndAuditRoutes() throws Exception {
        assertController("com.zhyc.ai.provider.controller.AiProviderController", "/ai/providers",
                new Route("listProviders", "org.springframework.web.bind.annotation.GetMapping", "", "ai:provider:query"),
                new Route("save", "org.springframework.web.bind.annotation.PutMapping", "", "ai:provider:save"),
                new Route("testProvider", "org.springframework.web.bind.annotation.PostMapping", "/test", "ai:provider:test"));
        assertController("com.zhyc.ai.model.controller.AiModelConfigController", "/ai/models",
                new Route("listModels", "org.springframework.web.bind.annotation.GetMapping", "", "ai:model:query"),
                new Route("save", "org.springframework.web.bind.annotation.PutMapping", "", "ai:model:save"));
        assertController("com.zhyc.ai.app.controller.AiAppController", "/ai/apps",
                new Route("listApps", "org.springframework.web.bind.annotation.GetMapping", "", "ai:app:query"),
                new Route("save", "org.springframework.web.bind.annotation.PutMapping", "", "ai:app:save"));
        assertController("com.zhyc.ai.prompt.controller.AiPromptTemplateController", "/ai/prompts",
                new Route("listTemplates", "org.springframework.web.bind.annotation.GetMapping", "", "ai:prompt:query"),
                new Route("save", "org.springframework.web.bind.annotation.PutMapping", "", "ai:prompt:save"));
        assertController("com.zhyc.ai.audit.controller.AiInvocationAuditController", "/ai/invocation-audits",
                new Route("listAudits", "org.springframework.web.bind.annotation.GetMapping", "", "ai:audit:query"),
                new Route("record", "org.springframework.web.bind.annotation.PostMapping", "", "ai:audit:record"));
        assertController("com.zhyc.ai.runtime.controller.AiRuntimeController", "/ai/runtime",
                new Route("chat", "org.springframework.web.bind.annotation.PostMapping", "/chat", "ai:runtime:chat"));
    }

    private static void assertController(String className, String requestPath, Route... routes) throws Exception {
        Class<?> controllerClass = Class.forName(className);
        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", requestPath);
        for (Route route : routes) {
            assertMethodMapping(controllerClass, route);
        }
    }

    private static void assertMethodMapping(Class<?> controllerClass, Route route) throws Exception {
        Method method = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(route.methodName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("缺少控制器方法: " + route.methodName()));
        assertAnnotationValue(method, route.mappingAnnotation(), "value", route.mappingValue());
        assertAnnotationValue(method, "org.apache.shiro.authz.annotation.RequiresPermissions",
                "value", route.permission());
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
            if (values.length == 0) {
                assertEquals(expectedValue, "");
                return;
            }
            assertEquals(expectedValue, values[0]);
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

    private record Route(String methodName, String mappingAnnotation, String mappingValue, String permission) {
    }
}
