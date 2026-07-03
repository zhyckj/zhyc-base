/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenantpackage.controller.SysTenantPackageController;
import com.zhyc.system.tenantpackage.service.SysTenantPackageResponse;
import com.zhyc.system.tenantpackage.service.SysTenantPackageService;
import com.zhyc.system.tenantpackage.service.TenantPackageCreateCommand;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统租户套餐管理接口契约测试。
 */
class SysTenantPackageControllerContractTest {

    /**
     * 验证套餐控制器暴露套餐清单和状态变更路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeTenantPackageRoutesWithShiroPermission() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.tenantpackage.controller.SysTenantPackageController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/tenant-packages");
        assertMethodMapping(controllerClass, "listPackages", "org.springframework.web.bind.annotation.GetMapping",
                "", "system:tenant-package:query");
        assertMethodMapping(controllerClass, "createPackage", "org.springframework.web.bind.annotation.PostMapping",
                "", "system:tenant-package:update");
        assertMethodMapping(controllerClass, "changeStatus", "org.springframework.web.bind.annotation.PutMapping",
                "/{packageCode}/status", "system:tenant-package:update");
    }

    /**
     * 验证套餐创建请求体为空时返回明确业务错误。
     */
    @Test
    void shouldRejectNullPackageCreateRequestWithReadableMessage() {
        SysTenantPackageController controller = new SysTenantPackageController(new RejectingTenantPackageService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.createPackage(null));

        assertEquals("ZHYC_SYSTEM_TENANT_PACKAGE_CREATE_REQUEST_REQUIRED", exception.getCode());
        assertEquals("套餐创建请求不能为空", exception.getMessage());
    }

    /**
     * 验证套餐状态变更请求体为空时返回明确业务错误。
     */
    @Test
    void shouldRejectNullPackageStatusRequestWithReadableMessage() {
        SysTenantPackageController controller = new SysTenantPackageController(new RejectingTenantPackageService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.changeStatus("basic", null));

        assertEquals("ZHYC_SYSTEM_TENANT_PACKAGE_STATUS_REQUEST_REQUIRED", exception.getCode());
        assertEquals("套餐状态不能为空", exception.getMessage());
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
     * 拒绝调用的租户套餐业务服务。
     *
     * <p>用于证明请求体为空时控制器会提前失败，不会继续执行套餐状态变更。</p>
     */
    private static class RejectingTenantPackageService implements SysTenantPackageService {

        @Override
        public List<SysTenantPackageResponse> listPackages(String status) {
            return List.of();
        }

        @Override
        public SysTenantPackageResponse createPackage(TenantPackageCreateCommand command) {
            throw new AssertionError("套餐创建服务不应被调用");
        }

        @Override
        public void changeStatus(String packageCode, String status) {
            throw new AssertionError("套餐状态变更服务不应被调用");
        }
    }
}
