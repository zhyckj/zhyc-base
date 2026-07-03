/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.passwordpolicy.controller.SysPasswordPolicyController;
import com.zhyc.system.passwordpolicy.service.PasswordPolicyValidationResult;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicyResponse;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicySaveCommand;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicyService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统密码策略接口契约测试。
 */
class SysPasswordPolicyControllerContractTest {

    /**
     * 验证密码策略控制器暴露安全配置路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposePasswordPolicyRoutesWithShiroPermissions() throws Exception {
        Class<?> controllerClass =
                Class.forName("com.zhyc.system.passwordpolicy.controller.SysPasswordPolicyController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/password-policies");
        assertMethodMapping(controllerClass, "getPolicy", "org.springframework.web.bind.annotation.GetMapping",
                "", "system:password-policy:query");
        assertMethodMapping(controllerClass, "save", "org.springframework.web.bind.annotation.PutMapping",
                "", "system:password-policy:save");
        assertMethodMapping(controllerClass, "validatePassword",
                "org.springframework.web.bind.annotation.PostMapping",
                "/validate", "system:password-policy:validate");
        assertMethodMapping(controllerClass, "validatePasswordHistory",
                "org.springframework.web.bind.annotation.PostMapping",
                "/validate-history", "system:password-policy:validate");
    }

    /**
     * 验证密码策略写入和校验接口拒绝空请求体，避免敏感密码数据校验流程接收无效入参。
     */
    @Test
    void shouldRejectNullWriteRequests() {
        SysPasswordPolicyController controller = new SysPasswordPolicyController(new RejectingPasswordPolicyService());

        BusinessException saveException = assertThrows(BusinessException.class, () -> controller.save(null));
        BusinessException validateException = assertThrows(BusinessException.class,
                () -> controller.validatePassword(null));
        BusinessException historyException = assertThrows(BusinessException.class,
                () -> controller.validatePasswordHistory(null));

        assertEquals("ZHYC_SYSTEM_PASSWORD_POLICY_SAVE_REQUEST_REQUIRED", saveException.getCode());
        assertEquals("系统密码策略保存请求不能为空", saveException.getMessage());
        assertEquals("ZHYC_SYSTEM_PASSWORD_POLICY_VALIDATE_REQUEST_REQUIRED", validateException.getCode());
        assertEquals("密码策略校验请求不能为空", validateException.getMessage());
        assertEquals("ZHYC_SYSTEM_PASSWORD_POLICY_HISTORY_VALIDATE_REQUEST_REQUIRED", historyException.getCode());
        assertEquals("密码历史策略校验请求不能为空", historyException.getMessage());
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
     * 拒绝调用的密码策略服务桩。
     *
     * <p>用于证明请求体为空时控制器会提前失败，不会进入密码策略保存或校验服务。</p>
     */
    private static final class RejectingPasswordPolicyService implements SysPasswordPolicyService {

        @Override
        public SysPasswordPolicyResponse getPolicy(String tenantId) {
            return null;
        }

        @Override
        public void save(SysPasswordPolicySaveCommand command) {
            throw new AssertionError("系统密码策略保存服务不应被调用");
        }

        @Override
        public PasswordPolicyValidationResult validatePassword(String tenantId, String password) {
            throw new AssertionError("密码策略校验服务不应被调用");
        }

        @Override
        public PasswordPolicyValidationResult validatePasswordHistory(String tenantId, String passwordHash,
                                                                     List<String> recentPasswordHashes) {
            throw new AssertionError("密码历史策略校验服务不应被调用");
        }
    }
}
