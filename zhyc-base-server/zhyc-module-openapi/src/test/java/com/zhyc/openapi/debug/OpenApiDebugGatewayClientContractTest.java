/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.debug;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.zhyc.openapi.debug.service.HttpOpenApiDebugGatewayClient;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * 开放 API 调试网关客户端契约测试。
 */
class OpenApiDebugGatewayClientContractTest {

    /**
     * 验证网关客户端声明 Spring 构造器注入，避免应用上下文按无参构造器实例化失败。
     *
     * @throws NoSuchMethodException 构造器签名变更时抛出
     */
    @Test
    void shouldExposeAutowiredGatewayUrlConstructorForSpringContext() throws NoSuchMethodException {
        Constructor<HttpOpenApiDebugGatewayClient> constructor =
                HttpOpenApiDebugGatewayClient.class.getConstructor(String.class);

        assertNotNull(constructor.getAnnotation(Autowired.class), "网关客户端构造器必须声明 @Autowired");
        assertNotNull(constructor.getParameters()[0].getAnnotation(Value.class), "网关地址参数必须声明 @Value");
    }
}
