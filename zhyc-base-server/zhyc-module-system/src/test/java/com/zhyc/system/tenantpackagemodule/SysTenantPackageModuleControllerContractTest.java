/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenantpackagemodule.controller.SysTenantPackageModuleController;
import com.zhyc.system.tenantpackagemodule.service.SysTenantPackageModuleService;
import com.zhyc.system.tenantpackagemodule.service.TenantPackageModuleBindCommand;
import com.zhyc.system.tenantpackagemodule.service.TenantPackageModuleResponse;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户套餐模块授权接口契约测试。
 */
class SysTenantPackageModuleControllerContractTest {

    /**
     * 验证控制器暴露套餐授权查询和绑定路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposePackageGrantRoutesWithShiroPermission() throws Exception {
        Class<?> controllerClass = Class.forName(
                "com.zhyc.system.tenantpackagemodule.controller.SysTenantPackageModuleController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/tenant-package-modules");
        assertMethodMapping(controllerClass, "listGrants", "org.springframework.web.bind.annotation.GetMapping",
                "/{packageId}", "system:tenant-package:query");
        assertMethodMapping(controllerClass, "bindGrants", "org.springframework.web.bind.annotation.PutMapping",
                "/{packageId}", "system:tenant-package:update");
    }

    /**
     * 验证套餐授权绑定请求体为空时返回明确业务错误。
     */
    @Test
    void shouldRejectNullBindRequestWithReadableMessage() {
        SysTenantPackageModuleController controller =
                new SysTenantPackageModuleController(new RecordingTenantPackageModuleService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.bindGrants(1L, null));

        assertEquals("ZHYC_SYSTEM_TENANT_PACKAGE_MODULE_BIND_REQUEST_REQUIRED", exception.getCode());
        assertEquals("套餐授权绑定请求不能为空", exception.getMessage());
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
     * 测试用租户套餐模块授权业务服务。
     */
    private static class RecordingTenantPackageModuleService implements SysTenantPackageModuleService {

        @Override
        public List<TenantPackageModuleResponse> listGrants(Long packageId) {
            return List.of();
        }

        @Override
        public void bindGrants(TenantPackageModuleBindCommand command) {
        }
    }
}
