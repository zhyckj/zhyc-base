/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenantparam.controller.SysTenantParamController;
import com.zhyc.system.tenantparam.service.SysTenantParamResponse;
import com.zhyc.system.tenantparam.service.SysTenantParamSaveCommand;
import com.zhyc.system.tenantparam.service.SysTenantParamService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户参数管理接口契约测试。
 */
class SysTenantParamControllerContractTest {

    /**
     * 验证控制器暴露租户参数查询和保存路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeTenantParamRoutesWithShiroPermission() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.tenantparam.controller.SysTenantParamController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/tenant-params");
        assertMethodMapping(controllerClass, "listParams", "org.springframework.web.bind.annotation.GetMapping",
                "", "system:tenant-param:query");
        assertMethodMapping(controllerClass, "save", "org.springframework.web.bind.annotation.PutMapping",
                "", "system:tenant-param:save");
    }

    /**
     * 验证租户参数保存接口拒绝空请求体，避免控制器向服务层传递无效命令。
     */
    @Test
    void shouldRejectNullSaveRequest() {
        SysTenantParamController controller = new SysTenantParamController(new RejectingTenantParamService());

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.save(null));

        assertEquals("ZHYC_SYSTEM_TENANT_PARAM_SAVE_REQUEST_REQUIRED", exception.getCode());
        assertEquals("租户参数保存请求不能为空", exception.getMessage());
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
     * 拒绝调用的租户参数服务桩。
     *
     * <p>用于证明请求体为空时控制器会提前失败，不会继续执行参数保存。</p>
     */
    private static final class RejectingTenantParamService implements SysTenantParamService {

        @Override
        public List<SysTenantParamResponse> listParams(String tenantId) {
            return List.of();
        }

        @Override
        public Optional<SysTenantParamResponse> findByKey(String tenantId, String paramKey) {
            return Optional.empty();
        }

        @Override
        public void save(SysTenantParamSaveCommand command) {
            throw new AssertionError("租户参数保存服务不应被调用");
        }
    }
}
