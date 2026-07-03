/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenant.controller.SysTenantController;
import com.zhyc.system.tenant.service.SysTenantCreateCommand;
import com.zhyc.system.tenant.service.SysTenantResponse;
import com.zhyc.system.tenant.service.SysTenantService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统租户管理接口契约测试。
 */
class SysTenantControllerContractTest {

    /**
     * 验证租户控制器暴露租户清单和状态变更路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeTenantRoutesWithShiroPermission() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.tenant.controller.SysTenantController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/tenants");
        assertMethodMapping(controllerClass, "listTenants", "org.springframework.web.bind.annotation.GetMapping",
                "", "system:tenant:query");
        assertMethodMapping(controllerClass, "listAuthorizedTenants", "org.springframework.web.bind.annotation.GetMapping",
                "/authorized", "system:tenant:authorized-query");
        assertMethodMapping(controllerClass, "createTenant", "org.springframework.web.bind.annotation.PostMapping",
                "", "system:tenant:create");
        assertMethodMapping(controllerClass, "updateTenant", "org.springframework.web.bind.annotation.PutMapping",
                "/{tenantId}", "system:tenant:update");
        assertMethodMapping(controllerClass, "changeStatus", "org.springframework.web.bind.annotation.PutMapping",
                "/{tenantId}/status", "system:tenant:update-status");
        assertMethodMapping(controllerClass, "deleteTenant", "org.springframework.web.bind.annotation.DeleteMapping",
                "/{tenantId}", "system:tenant:delete");
    }

    /**
     * 验证租户创建请求体为空时返回明确业务错误。
     */
    @Test
    void shouldRejectNullTenantCreateRequestWithReadableMessage() {
        SysTenantController controller = new SysTenantController(new RejectingTenantService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.createTenant(null));

        assertEquals("ZHYC_SYSTEM_TENANT_CREATE_REQUEST_REQUIRED", exception.getCode());
        assertEquals("租户创建请求不能为空", exception.getMessage());
    }

    /**
     * 验证租户更新请求体为空时返回明确业务错误。
     */
    @Test
    void shouldRejectNullTenantUpdateRequestWithReadableMessage() {
        SysTenantController controller = new SysTenantController(new RejectingTenantService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.updateTenant("tenant_a", null));

        assertEquals("ZHYC_SYSTEM_TENANT_UPDATE_REQUEST_REQUIRED", exception.getCode());
        assertEquals("租户更新请求不能为空", exception.getMessage());
    }

    /**
     * 验证租户状态变更请求体为空时返回明确业务错误。
     */
    @Test
    void shouldRejectNullTenantStatusRequestWithReadableMessage() {
        SysTenantController controller = new SysTenantController(new RejectingTenantService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.changeStatus("tenant_a", null));

        assertEquals("ZHYC_SYSTEM_TENANT_STATUS_REQUEST_REQUIRED", exception.getCode());
        assertEquals("租户状态不能为空", exception.getMessage());
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

    /**
     * 拒绝调用的系统租户业务服务。
     *
     * <p>用于证明请求体为空时控制器会提前失败，不会继续执行租户创建或状态变更。</p>
     */
    private static class RejectingTenantService implements SysTenantService {

        @Override
        public List<SysTenantResponse> listTenants(String status) {
            return List.of();
        }

        @Override
        public List<SysTenantResponse> listAuthorizedTenants(String username) {
            return List.of();
        }

        @Override
        public void createTenant(SysTenantCreateCommand command) {
            throw new AssertionError("租户创建服务不应被调用");
        }

        @Override
        public void updateTenant(String tenantId, SysTenantCreateCommand command) {
            throw new AssertionError("租户更新服务不应被调用");
        }

        @Override
        public void changeStatus(String tenantId, String status) {
            throw new AssertionError("租户状态变更服务不应被调用");
        }

        @Override
        public void deleteTenant(String tenantId) {
            throw new AssertionError("租户删除服务不应被调用");
        }
    }
}
