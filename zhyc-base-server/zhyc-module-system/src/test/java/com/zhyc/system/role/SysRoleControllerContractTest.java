/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.role.controller.SysRoleController;
import com.zhyc.system.role.service.RoleMenuBindCommand;
import com.zhyc.system.role.service.SysRoleResponse;
import com.zhyc.system.role.service.SysRoleSaveCommand;
import com.zhyc.system.role.service.SysRoleService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统角色管理接口契约测试。
 */
class SysRoleControllerContractTest {

    /**
     * 验证角色控制器暴露角色列表和菜单绑定路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeRoleRoutesWithShiroPermission() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.role.controller.SysRoleController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/roles");
        assertMethodMapping(controllerClass, "listRoles", "org.springframework.web.bind.annotation.GetMapping",
                "", "system:role:query");
        assertMethodMapping(controllerClass, "listRoleMenuIds", "org.springframework.web.bind.annotation.GetMapping",
                "/{roleId}/menus", "system:role:authorize");
        assertMethodMapping(controllerClass, "bindMenus", "org.springframework.web.bind.annotation.PutMapping",
                "/{roleId}/menus", "system:role:authorize");
    }

    /**
     * 验证角色菜单绑定接口拒绝空请求体，避免误触发权限替换。
     */
    @Test
    void shouldRejectNullBindMenuRequest() {
        SysRoleController controller = new SysRoleController(new RejectingRoleService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.bindMenus(1L, null));

        assertEquals("ZHYC_SYSTEM_ROLE_MENU_BIND_REQUEST_REQUIRED", exception.getCode());
        assertEquals("角色菜单绑定请求不能为空", exception.getMessage());
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
     * 拒绝调用的角色服务桩。
     *
     * <p>用于证明请求体为空时控制器会提前失败，不会继续执行菜单授权绑定。</p>
     */
    private static final class RejectingRoleService implements SysRoleService {

        @Override
        public List<SysRoleResponse> listRoles(String tenantId) {
            return List.of();
        }

        @Override
        public void saveRole(SysRoleSaveCommand command) {
            throw new AssertionError("角色保存服务不应被调用");
        }

        @Override
        public void updateStatus(String tenantId, Long roleId, String status) {
            throw new AssertionError("角色状态服务不应被调用");
        }

        @Override
        public void deleteRole(String tenantId, Long roleId) {
            throw new AssertionError("角色删除服务不应被调用");
        }

        @Override
        public List<Long> listRoleMenuIds(String tenantId, Long roleId) {
            throw new AssertionError("角色菜单查询服务不应被调用");
        }

        @Override
        public void bindRoleMenus(RoleMenuBindCommand command) {
            throw new AssertionError("角色菜单绑定服务不应被调用");
        }
    }
}
