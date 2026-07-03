/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.module;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.module.ModuleRegistry;
import com.zhyc.system.module.controller.SysModuleController;
import com.zhyc.system.module.controller.SysModuleEnabledRequest;
import com.zhyc.system.module.service.SysModuleResponse;
import com.zhyc.system.module.service.SysModuleService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统模块管理接口契约测试。
 */
class SysModuleControllerContractTest {

    /**
     * 验证模块控制器暴露模块清单和启停路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeModuleRoutesWithShiroPermission() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.module.controller.SysModuleController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/modules");
        assertMethodMapping(controllerClass, "listModules", "org.springframework.web.bind.annotation.GetMapping",
                "", "system:module:query");
        assertMethodMapping(controllerClass, "changeEnabled", "org.springframework.web.bind.annotation.PutMapping",
                "/{moduleCode}/enabled", "system:module:update");
    }

    /**
     * 验证模块启停请求体为空时返回明确业务错误。
     */
    @Test
    void shouldRejectNullEnabledRequestWithReadableMessage() {
        SysModuleController controller = new SysModuleController(new RecordingModuleService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.changeEnabled("purchase", null));

        assertEquals("ZHYC_SYSTEM_MODULE_ENABLED_REQUEST_REQUIRED", exception.getCode());
        assertEquals("模块启用状态不能为空", exception.getMessage());
    }

    /**
     * 验证模块启停请求缺少启用值时返回明确业务错误。
     */
    @Test
    void shouldRejectMissingEnabledValueWithReadableMessage() {
        SysModuleController controller = new SysModuleController(new RecordingModuleService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.changeEnabled("purchase", new SysModuleEnabledRequest()));

        assertEquals("ZHYC_SYSTEM_MODULE_ENABLED_REQUEST_REQUIRED", exception.getCode());
        assertEquals("模块启用状态不能为空", exception.getMessage());
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
     * 测试用系统模块业务服务。
     */
    private static class RecordingModuleService implements SysModuleService {

        @Override
        public List<SysModuleResponse> listModules() {
            return List.of();
        }

        @Override
        public ModuleRegistry moduleRegistry() {
            throw new UnsupportedOperationException("测试不使用模块注册表");
        }

        @Override
        public void syncModuleRegistry(ModuleRegistry registry) {
            throw new UnsupportedOperationException("测试不使用模块注册表同步");
        }

        @Override
        public void changeEnabled(String moduleCode, boolean enabled) {
        }
    }
}
