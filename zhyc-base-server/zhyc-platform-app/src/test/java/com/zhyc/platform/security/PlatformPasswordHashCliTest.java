/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 平台本地密码哈希生成命令测试。
 */
class PlatformPasswordHashCliTest {

    /**
     * 应生成 Shiro 可校验的密码哈希。
     */
    @Test
    void shouldGenerateHashThatMatchesShiroPasswordService() {
        DefaultPasswordService passwordService = new DefaultPasswordService();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();

        int exitCode = PlatformPasswordHashCli.run(new String[0],
                Map.of(PlatformPasswordHashCli.PASSWORD_ENV, "LocalAdmin123"),
                new PrintStream(stdout), System.err, passwordService);

        String passwordHash = stdout.toString(StandardCharsets.UTF_8).trim();
        assertEquals(0, exitCode);
        assertTrue(passwordHash.length() > 40);
        assertTrue(passwordService.passwordsMatch("LocalAdmin123", passwordHash));
    }

    /**
     * 缺少本地密码环境变量时必须失败。
     */
    @Test
    void shouldRejectMissingPasswordEnvironment() {
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        int exitCode = PlatformPasswordHashCli.run(new String[0], Map.of(), System.out,
                new PrintStream(stderr), new DefaultPasswordService());

        assertEquals(2, exitCode);
        assertTrue(stderr.toString(StandardCharsets.UTF_8).contains(PlatformPasswordHashCli.PASSWORD_ENV));
    }

    /**
     * 本地初始化密码过短时必须失败。
     */
    @Test
    void shouldRejectShortPassword() {
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        int exitCode = PlatformPasswordHashCli.run(new String[0],
                Map.of(PlatformPasswordHashCli.PASSWORD_ENV, "short"),
                System.out, new PrintStream(stderr), new DefaultPasswordService());

        assertEquals(3, exitCode);
        assertTrue(stderr.toString(StandardCharsets.UTF_8).contains("不能小于"));
    }
}
