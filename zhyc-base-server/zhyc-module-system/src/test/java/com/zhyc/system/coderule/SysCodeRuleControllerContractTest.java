/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.coderule.controller.SysCodeRuleController;
import com.zhyc.system.coderule.service.SysCodeRuleResponse;
import com.zhyc.system.coderule.service.SysCodeRuleSaveCommand;
import com.zhyc.system.coderule.service.SysCodeRuleService;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统编码规则接口契约测试。
 */
class SysCodeRuleControllerContractTest {

    /**
     * 验证编码规则控制器暴露基础配置路由，并声明 Shiro 权限。
     *
     * @throws Exception 反射读取控制器失败时抛出
     */
    @Test
    void shouldExposeCodeRuleRoutesWithShiroPermissions() throws Exception {
        Class<?> controllerClass = Class.forName("com.zhyc.system.coderule.controller.SysCodeRuleController");

        assertAnnotation(controllerClass, "org.springframework.web.bind.annotation.RestController");
        assertAnnotationValue(controllerClass, "org.springframework.web.bind.annotation.RequestMapping",
                "value", "/system/code-rules");
        assertMethodMapping(controllerClass, "listRules", "org.springframework.web.bind.annotation.GetMapping",
                "", "system:code-rule:query");
        assertMethodMapping(controllerClass, "save", "org.springframework.web.bind.annotation.PutMapping",
                "", "system:code-rule:save");
        assertMethodMapping(controllerClass, "generateNextCode", "org.springframework.web.bind.annotation.PostMapping",
                "/next-code", "system:code-rule:generate");
    }

    /**
     * 验证编码规则写入接口拒绝空请求体，避免控制器向服务层传递无效命令。
     */
    @Test
    void shouldRejectNullWriteRequests() {
        SysCodeRuleController controller = new SysCodeRuleController(new RejectingCodeRuleService());

        BusinessException saveException = assertThrows(BusinessException.class, () -> controller.save(null));
        BusinessException generateException = assertThrows(BusinessException.class,
                () -> controller.generateNextCode(null));

        assertEquals("ZHYC_SYSTEM_CODE_RULE_SAVE_REQUEST_REQUIRED", saveException.getCode());
        assertEquals("系统编码规则保存请求不能为空", saveException.getMessage());
        assertEquals("ZHYC_SYSTEM_CODE_RULE_GENERATE_REQUEST_REQUIRED", generateException.getCode());
        assertEquals("系统编码规则生成请求不能为空", generateException.getMessage());
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
     * 拒绝调用的编码规则服务桩。
     *
     * <p>用于证明请求体为空时控制器会提前失败，不会继续执行业务保存或编码生成。</p>
     */
    private static final class RejectingCodeRuleService implements SysCodeRuleService {

        @Override
        public List<SysCodeRuleResponse> listRules(String tenantId) {
            return List.of();
        }

        @Override
        public void save(SysCodeRuleSaveCommand command) {
            throw new AssertionError("系统编码规则保存服务不应被调用");
        }

        @Override
        public String generateNextCode(String tenantId, String ruleCode, LocalDate businessDate) {
            throw new AssertionError("系统编码规则生成服务不应被调用");
        }
    }
}
