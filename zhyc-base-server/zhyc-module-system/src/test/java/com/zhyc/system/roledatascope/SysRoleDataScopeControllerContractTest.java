/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.roledatascope;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.role.controller.SysRoleDataScopeController;
import com.zhyc.system.role.service.RoleDataScopeBindCommand;
import com.zhyc.system.role.service.SysRoleDataScopeResponse;
import com.zhyc.system.role.service.SysRoleDataScopeService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统角色自定义数据权限接口契约测试。
 */
class SysRoleDataScopeControllerContractTest {

    /**
     * 验证角色数据权限控制器暴露查询和绑定路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeRoleDataScopeRoutesWithShiroPermissions() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.role.controller.SysRoleDataScopeController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/roles/{roleId}/data-scopes");
        assertMethodMapping(controllerClass, "listRoleDataScopes",
                "org.springframework.web.bind.annotation.GetMapping", "", "system:role:query");
        assertMethodMapping(controllerClass, "bindRoleDataScopes",
                "org.springframework.web.bind.annotation.PutMapping", "", "system:role:edit");
    }

    /**
     * 验证角色数据权限绑定接口拒绝空请求体，避免误触发数据范围替换。
     */
    @Test
    void shouldRejectNullBindRequest() {
        SysRoleDataScopeController controller =
                new SysRoleDataScopeController(new RejectingRoleDataScopeService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.bindRoleDataScopes(1L, "tenant-a", null));

        assertEquals("ZHYC_SYSTEM_ROLE_DATA_SCOPE_BIND_REQUEST_REQUIRED", exception.getCode());
        assertEquals("角色数据权限绑定请求不能为空", exception.getMessage());
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
     * 拒绝调用的角色数据权限服务桩。
     *
     * <p>用于证明请求体为空时控制器会提前失败，不会继续执行数据范围绑定。</p>
     */
    private static final class RejectingRoleDataScopeService implements SysRoleDataScopeService {

        @Override
        public List<SysRoleDataScopeResponse> listRoleDataScopes(String tenantId, Long roleId) {
            return List.of();
        }

        @Override
        public void bindRoleDataScopes(RoleDataScopeBindCommand command) {
            throw new AssertionError("角色数据权限绑定服务不应被调用");
        }
    }
}
