/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.user.controller.SysUserController;
import com.zhyc.system.user.service.SysUserPasswordChangeCommand;
import com.zhyc.system.user.service.SysUserResponse;
import com.zhyc.system.user.service.SysUserSaveCommand;
import com.zhyc.system.user.service.SysUserService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统用户管理接口契约测试。
 */
class SysUserControllerContractTest {

    /**
     * 验证用户控制器暴露用户列表路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeUserListRouteWithShiroPermission() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.user.controller.SysUserController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/users");
        assertMethodMapping(controllerClass, "listUsers", "org.springframework.web.bind.annotation.GetMapping",
                "", "system:user:query");
        assertMethodMapping(controllerClass, "changePassword", "org.springframework.web.bind.annotation.PostMapping",
                "/password", "system:user:change-password");
    }

    /**
     * 验证修改密码接口拒绝空请求体，避免敏感改密流程收到无效入参。
     */
    @Test
    void shouldRejectNullPasswordChangeRequestBody() {
        SysUserController controller = new SysUserController(new RejectingUserService());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.changePassword(null));

        assertEquals("修改密码请求不能为空", exception.getMessage());
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
     * 拒绝调用的系统用户服务桩。
     */
    private static final class RejectingUserService implements SysUserService {

        @Override
        public List<SysUserResponse> listUsers(String tenantId) {
            throw new AssertionError("修改密码空请求体不应查询用户列表");
        }

        @Override
        public void saveUser(SysUserSaveCommand command) {
            throw new AssertionError("修改密码空请求体不应保存用户");
        }

        @Override
        public void updateStatus(String tenantId, Long userId, String status) {
            throw new AssertionError("修改密码空请求体不应更新用户状态");
        }

        @Override
        public void resetPassword(String tenantId, Long userId, String password) {
            throw new AssertionError("修改密码空请求体不应重置密码");
        }

        @Override
        public void deleteUser(String tenantId, Long userId) {
            throw new AssertionError("修改密码空请求体不应删除用户");
        }

        @Override
        public void changePassword(SysUserPasswordChangeCommand command) {
            throw new AssertionError("修改密码空请求体不应进入服务层");
        }
    }
}
