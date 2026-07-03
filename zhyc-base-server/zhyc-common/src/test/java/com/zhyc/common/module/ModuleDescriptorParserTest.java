/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.module;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 模块描述文件解析器测试。
 */
class ModuleDescriptorParserTest {

    /**
     * 验证可解析低代码生成器输出的微服务模块描述文件。
     */
    @Test
    void shouldParseGeneratedMicroserviceDescriptor() {
        String content = """
                moduleCode: purchase-purchaseOrder
                moduleName: 采购订单微服务模块
                moduleType: MICROSERVICE
                serviceName: zhyc-service-purchase-purchaseOrder
                tenantMode: tenant_id
                permissionPrefix: purchase:purchaseOrder
                backendPackage: com.zhyc.service.purchase.purchaseOrder
                openApiGatewayRequired: true
                authServerRequired: true
                dependencies:
                  - common
                  - system
                databaseScripts:
                  - classpath:db/V1__purchase_purchaseOrder_service.sql
                extensionPoints:
                  - name: field-type-mapper
                    description: 字段类型映射扩展点
                  - name: ddl-generator
                    description: DDL 生成器扩展点
                  - name: pagination-dialect
                    description: 分页方言扩展点
                """;

        ModuleDescriptor descriptor = ModuleDescriptorParser.parse(content);

        assertEquals("purchase-purchaseOrder", descriptor.getCode());
        assertEquals("采购订单微服务模块", descriptor.getName());
        assertEquals("MICROSERVICE", descriptor.getModuleType());
        assertEquals("zhyc-service-purchase-purchaseOrder", descriptor.getServiceName());
        assertEquals("tenant_id", descriptor.getTenantMode());
        assertEquals("purchase:purchaseOrder", descriptor.getPermissionPrefix());
        assertEquals("com.zhyc.service.purchase.purchaseOrder", descriptor.getBackendPackage());
        assertTrue(descriptor.isOpenApiGatewayRequired());
        assertTrue(descriptor.isAuthServerRequired());
        assertEquals(List.of("common", "system"), descriptor.getDependencies());
        assertEquals(List.of("classpath:db/V1__purchase_purchaseOrder_service.sql"), descriptor.getDbScripts());
        assertEquals(List.of("field-type-mapper", "ddl-generator", "pagination-dialect"),
                descriptor.getExtensionPoints());
    }

    /**
     * 验证缺少模块编码时拒绝描述文件，避免模块注册表出现不可识别模块。
     */
    @Test
    void shouldRejectDescriptorWithoutModuleCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ModuleDescriptorParser.parse("moduleName: 缺少编码模块"));

        assertEquals("模块描述缺少 moduleCode", exception.getMessage());
    }
}
