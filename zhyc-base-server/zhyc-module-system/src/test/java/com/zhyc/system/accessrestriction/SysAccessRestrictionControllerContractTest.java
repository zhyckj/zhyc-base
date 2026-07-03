/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.accessrestriction.controller.SysAccessRestrictionController;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionEvaluationResult;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionResponse;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionSaveCommand;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统访问限制接口契约测试。
 */
class SysAccessRestrictionControllerContractTest {

    /**
     * 验证访问限制控制器暴露查询和保存路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeAccessRestrictionRoutesWithShiroPermissions() throws Exception {
        Class<?> controllerClass =
                Class.forName("com.zhyc.system.accessrestriction.controller.SysAccessRestrictionController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/access-restrictions");
        assertMethodMapping(controllerClass, "listActiveRestrictions",
                "org.springframework.web.bind.annotation.GetMapping", "", "system:access-restriction:query");
        assertMethodMapping(controllerClass, "evaluateAccess",
                "org.springframework.web.bind.annotation.PostMapping", "/evaluate",
                "system:access-restriction:evaluate");
        assertMethodMapping(controllerClass, "save",
                "org.springframework.web.bind.annotation.PutMapping", "", "system:access-restriction:save");
    }

    /**
     * 验证访问限制判定和保存接口拒绝空请求体，避免访问控制服务接收无效入参。
     */
    @Test
    void shouldRejectNullWriteRequests() {
        SysAccessRestrictionController controller =
                new SysAccessRestrictionController(new RejectingAccessRestrictionService());

        BusinessException evaluateException = assertThrows(BusinessException.class,
                () -> controller.evaluateAccess(null));
        BusinessException saveException = assertThrows(BusinessException.class, () -> controller.save(null));

        assertEquals("ZHYC_SYSTEM_ACCESS_RESTRICTION_EVALUATE_REQUEST_REQUIRED", evaluateException.getCode());
        assertEquals("系统访问限制判定请求不能为空", evaluateException.getMessage());
        assertEquals("ZHYC_SYSTEM_ACCESS_RESTRICTION_SAVE_REQUEST_REQUIRED", saveException.getCode());
        assertEquals("系统访问限制保存请求不能为空", saveException.getMessage());
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
     * 拒绝调用的访问限制服务桩。
     *
     * <p>用于证明请求体为空时控制器会提前失败，不会进入访问判定或保存服务。</p>
     */
    private static final class RejectingAccessRestrictionService implements SysAccessRestrictionService {

        @Override
        public List<SysAccessRestrictionResponse> listActiveRestrictions(String tenantId, String restrictionType,
                                                                         LocalDateTime now) {
            return List.of();
        }

        @Override
        public SysAccessRestrictionEvaluationResult evaluateAccess(String tenantId, String restrictionType,
                                                                   String accessValue, LocalDateTime now) {
            throw new AssertionError("系统访问限制判定服务不应被调用");
        }

        @Override
        public void save(SysAccessRestrictionSaveCommand command) {
            throw new AssertionError("系统访问限制保存服务不应被调用");
        }
    }
}
