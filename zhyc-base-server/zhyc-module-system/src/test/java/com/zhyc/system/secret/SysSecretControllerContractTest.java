/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret;

import com.zhyc.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统密钥中心控制器契约测试。
 */
class SysSecretControllerContractTest {

    /**
     * 验证密钥中心控制器暴露正确路由并声明严格的 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeSecretRoutesWithShiroPermission() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.secret.controller.SysSecretController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/secrets");
        assertMethodMapping(controllerClass, "listSecrets", "org.springframework.web.bind.annotation.GetMapping",
                "", "system:secret:query");
        assertMethodMapping(controllerClass, "getSecret", "org.springframework.web.bind.annotation.GetMapping",
                "/{secretId}", "system:secret:query");
        assertMethodMapping(controllerClass, "listOptions", "org.springframework.web.bind.annotation.GetMapping",
                "/options", "system:secret:query");
        assertMethodMapping(controllerClass, "createSecret", "org.springframework.web.bind.annotation.PostMapping",
                "", "system:secret:create");
        assertMethodMapping(controllerClass, "updateSecret", "org.springframework.web.bind.annotation.PutMapping",
                "/{secretId}", "system:secret:update");
        assertMethodMapping(controllerClass, "enableSecret", "org.springframework.web.bind.annotation.PutMapping",
                "/{secretId}/enable", "system:secret:enable");
        assertMethodMapping(controllerClass, "disableSecret", "org.springframework.web.bind.annotation.PutMapping",
                "/{secretId}/disable", "system:secret:disable");
        assertMethodMapping(controllerClass, "rotateSecret", "org.springframework.web.bind.annotation.PostMapping",
                "/{secretId}/rotate", "system:secret:rotate");
        assertMethodMapping(controllerClass, "deleteSecret", "org.springframework.web.bind.annotation.DeleteMapping",
                "/{secretId}", "system:secret:delete");
    }

    /**
     * 验证控制器响应对象不暴露明文密钥字段。
     *
     * @throws Exception 反射读取响应对象失败时抛出
     */
    @Test
    void shouldNotExposeSecretValueInResponse() throws Exception {
        Class<?> responseClass = Class.forName("com.zhyc.system.secret.service.SysSecretResponse");

        assertTrue(Arrays.stream(responseClass.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)
                .anyMatch("secretRef"::equals));
        assertFalse(Arrays.stream(responseClass.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)
                .anyMatch("secretCipher"::equals));
        assertFalse(Arrays.stream(responseClass.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)
                .anyMatch("secretMask"::equals));
    }

    /**
     * 验证创建密钥请求体为空时返回明确业务错误。
     *
     * @throws Exception 反射调用控制器失败时抛出
     */
    @Test
    void shouldRejectNullSecretCreateRequestWithReadableMessage() throws Exception {
        Object controller = newController();
        Class<?> requestClass = Class.forName("com.zhyc.system.secret.controller.SysSecretSaveRequest");

        BusinessException exception = invokeBusinessException(controller, "createSecret",
                new Class<?>[]{requestClass}, new Object[]{null});

        assertEquals("ZHYC_SYSTEM_SECRET_SAVE_REQUEST_REQUIRED", exception.getCode());
        assertEquals("密钥保存请求不能为空", exception.getMessage());
    }

    /**
     * 验证轮换密钥请求体为空时返回明确业务错误。
     *
     * @throws Exception 反射调用控制器失败时抛出
     */
    @Test
    void shouldRejectNullSecretRotateRequestWithReadableMessage() throws Exception {
        Object controller = newController();
        Class<?> requestClass = Class.forName("com.zhyc.system.secret.controller.SysSecretRotateRequest");

        BusinessException exception = invokeBusinessException(controller, "rotateSecret",
                new Class<?>[]{Long.class, requestClass}, new Object[]{1L, null});

        assertEquals("ZHYC_SYSTEM_SECRET_ROTATE_REQUEST_REQUIRED", exception.getCode());
        assertEquals("密钥轮换请求不能为空", exception.getMessage());
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
     * 创建拒绝调用的系统密钥服务代理。
     *
     * @return 系统密钥服务代理
     * @throws Exception 反射创建代理失败时抛出
     */
    private static Object newController() throws Exception {
        Class<?> serviceInterface = Class.forName("com.zhyc.system.secret.service.SysSecretService");
        Object serviceProxy = Proxy.newProxyInstance(SysSecretControllerContractTest.class.getClassLoader(),
                new Class<?>[]{serviceInterface}, (proxy, method, args) -> {
                    throw new AssertionError("密钥服务不应被调用: " + method.getName());
                });
        Class<?> controllerClass = Class.forName("com.zhyc.system.secret.controller.SysSecretController");
        return controllerClass.getConstructor(serviceInterface).newInstance(serviceProxy);
    }

    /**
     * 反射调用控制器并断言业务异常。
     *
     * @param controller 控制器实例
     * @param methodName 方法名
     * @param parameterTypes 方法参数类型
     * @param arguments 方法参数
     * @return 业务异常
     * @throws Exception 调用失败时抛出
     */
    private static BusinessException invokeBusinessException(Object controller, String methodName,
                                                             Class<?>[] parameterTypes, Object[] arguments)
            throws Exception {
        Method method = controller.getClass().getMethod(methodName, parameterTypes);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> method.invoke(controller, arguments));
        assertTrue(exception.getCause() instanceof BusinessException, "应抛出业务异常");
        return (BusinessException) exception.getCause();
    }
}
