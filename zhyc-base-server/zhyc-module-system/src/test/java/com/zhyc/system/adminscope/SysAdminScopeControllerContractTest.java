/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.adminscope.controller.SysAdminScopeController;
import com.zhyc.system.adminscope.service.AdminScopeBindCommand;
import com.zhyc.system.adminscope.service.SysAdminScopeResponse;
import com.zhyc.system.adminscope.service.SysAdminScopeService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统管理员管理范围接口契约测试。
 */
class SysAdminScopeControllerContractTest {

    /**
     * 验证管理员范围控制器暴露查询和绑定路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeAdminScopeRoutesWithShiroPermissions() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.adminscope.controller.SysAdminScopeController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/admins/{userId}/scopes");
        assertMethodMapping(controllerClass, "listAdminScopes",
                "org.springframework.web.bind.annotation.GetMapping", "", "system:admin:query");
        assertMethodMapping(controllerClass, "bindAdminScopes",
                "org.springframework.web.bind.annotation.PutMapping", "", "system:admin:edit");
    }

    /**
     * 验证管理员管理范围绑定接口拒绝空请求体，避免误触发管理范围替换。
     */
    @Test
    void shouldRejectNullBindRequest() {
        SysAdminScopeController controller = new SysAdminScopeController(new RejectingAdminScopeService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.bindAdminScopes(1L, "tenant-a", null));

        assertEquals("ZHYC_SYSTEM_ADMIN_SCOPE_BIND_REQUEST_REQUIRED", exception.getCode());
        assertEquals("管理员管理范围绑定请求不能为空", exception.getMessage());
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
     * 拒绝调用的管理员管理范围服务桩。
     *
     * <p>用于证明请求体为空时控制器会提前失败，不会继续执行管理范围绑定。</p>
     */
    private static final class RejectingAdminScopeService implements SysAdminScopeService {

        @Override
        public List<SysAdminScopeResponse> listAdminScopes(String tenantId, Long userId) {
            return List.of();
        }

        @Override
        public void bindAdminScopes(AdminScopeBindCommand command) {
            throw new AssertionError("管理员管理范围绑定服务不应被调用");
        }
    }
}
