/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.userrole;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.user.controller.SysUserRoleController;
import com.zhyc.system.user.service.SysUserRoleBindCommand;
import com.zhyc.system.user.service.SysUserRoleResponse;
import com.zhyc.system.user.service.SysUserRoleService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统用户角色接口契约测试。
 */
class SysUserRoleControllerContractTest {

    /**
     * 验证用户角色控制器暴露查询和绑定路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeUserRoleRoutesWithShiroPermissions() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.user.controller.SysUserRoleController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/users/{userId}/roles");
        assertMethodMapping(controllerClass, "listUserRoles", "org.springframework.web.bind.annotation.GetMapping",
                "", "system:user:query");
        assertMethodMapping(controllerClass, "bindUserRoles", "org.springframework.web.bind.annotation.PutMapping",
                "", "system:user:edit");
    }

    /**
     * 验证用户角色绑定接口拒绝空请求体，避免误触发用户授权替换。
     */
    @Test
    void shouldRejectNullBindRequest() {
        SysUserRoleController controller = new SysUserRoleController(new RejectingUserRoleService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.bindUserRoles(1L, "tenant-a", null));

        assertEquals("ZHYC_SYSTEM_USER_ROLE_BIND_REQUEST_REQUIRED", exception.getCode());
        assertEquals("用户角色绑定请求不能为空", exception.getMessage());
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
     * 拒绝调用的用户角色服务桩。
     *
     * <p>用于证明请求体为空时控制器会提前失败，不会继续执行用户角色绑定。</p>
     */
    private static final class RejectingUserRoleService implements SysUserRoleService {

        @Override
        public List<SysUserRoleResponse> listUserRoles(String tenantId, Long userId) {
            return List.of();
        }

        @Override
        public void bindUserRoles(SysUserRoleBindCommand command) {
            throw new AssertionError("用户角色绑定服务不应被调用");
        }
    }
}
