/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.module;

import com.zhyc.common.module.ModuleDescriptor;
import com.zhyc.common.module.ModuleDescriptorParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AI 能力中心模块描述测试。
 */
class AiModuleDescriptorTest {

    /**
     * 验证 AI 能力中心模块描述可被平台模块注册器解析。
     *
     * @throws IOException 读取模块描述失败
     */
    @Test
    void shouldParseAiModuleDescriptor() throws IOException {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("META-INF/zhyc-module.yml")) {
            assertNotNull(input, "AI 模块描述文件不能为空");
            String content = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            ModuleDescriptor descriptor = ModuleDescriptorParser.parse(content);

            assertEquals("ai", descriptor.getCode());
            assertEquals("AI 能力中心", descriptor.getName());
            assertEquals("ai", descriptor.getPermissionPrefix());
            assertEquals("com.zhyc.ai", descriptor.getBackendPackage());
            assertTrue(descriptor.getPermissions().contains("ai:provider:query"));
            assertTrue(descriptor.getPermissions().contains("ai:app:save"));
            assertTrue(descriptor.getDbScripts().contains("classpath:db/V1__ai_core.sql"));
        }
    }
}
