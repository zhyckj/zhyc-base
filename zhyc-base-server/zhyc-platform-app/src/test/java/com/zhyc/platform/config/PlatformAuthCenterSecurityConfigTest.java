/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhyc.platform.security.PlatformAuthCenterBearerFilter;
import com.zhyc.platform.security.PlatformShiroSubjectAuthenticator;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * 平台认证中心安全配置测试。
 *
 * <p>约束 JWK Set 地址启用条件和 Bearer Token 过滤器注册行为，避免默认启动或空配置影响核心平台。</p>
 */
class PlatformAuthCenterSecurityConfigTest {

    /** 应用上下文启动器。 */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PlatformAuthCenterSecurityConfig.class);

    /**
     * 验证未配置 JWK Set 地址时只注册 Shiro 登录适配器，不启用认证中心 JWT 过滤链。
     */
    @Test
    void shouldSkipJwtDecoderAndBearerFilterWhenJwkSetUriMissing() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(PlatformShiroSubjectAuthenticator.class));
            assertEquals(0, context.getBeansOfType(JwtDecoder.class).size());
            assertEquals(0, context.getBeansOfType(FilterRegistrationBean.class).size());
        });
    }

    /**
     * 验证 JWK Set 地址为空白字符串时不启用 JWT 解码器和 Bearer Token 过滤器。
     */
    @Test
    void shouldSkipJwtDecoderAndBearerFilterWhenJwkSetUriBlank() {
        contextRunner.withPropertyValues("zhyc.platform.auth.jwk-set-uri=  ").run(context -> {
            assertNotNull(context.getBean(PlatformShiroSubjectAuthenticator.class));
            assertEquals(0, context.getBeansOfType(JwtDecoder.class).size());
            assertEquals(0, context.getBeansOfType(FilterRegistrationBean.class).size());
        });
    }

    /**
     * 验证配置 JWK Set 地址但未配置签发方时启动失败，避免平台接受缺少 issuer 约束的令牌。
     */
    @Test
    void shouldFailWhenJwkSetUriConfiguredButIssuerMissing() {
        contextRunner.withPropertyValues("zhyc.platform.auth.jwk-set-uri=http://127.0.0.1:8090/oauth2/jwks")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(hasCauseMessage(context.getStartupFailure(),
                            "启用认证中心 JWK Set 时必须配置 zhyc.platform.auth.issuer"));
                });
    }

    /**
     * 验证配置 JWK Set 地址后会注册 JWT 解码器和 Bearer Token 过滤器。
     */
    @Test
    void shouldRegisterJwtDecoderAndBearerFilterWhenJwkSetUriConfigured() {
        contextRunner.withBean(SecurityManager.class, DefaultSecurityManager::new)
                .withPropertyValues(
                        "zhyc.platform.auth.jwk-set-uri=http://127.0.0.1:8090/oauth2/jwks",
                        "zhyc.platform.auth.issuer=http://127.0.0.1:8090")
                .run(context -> {
                    assertNotNull(context.getBean(JwtDecoder.class));
                    assertTrue(context.containsBean("platformAuthCenterBearerFilter"));
                    FilterRegistrationBean<?> registrationBean =
                            context.getBean("platformAuthCenterBearerFilter", FilterRegistrationBean.class);
                    assertEquals(1, registrationBean.getUrlPatterns().size());
                    assertEquals("/*", registrationBean.getUrlPatterns().iterator().next());
                    assertEquals(PlatformAuthCenterBearerFilter.class, registrationBean.getFilter().getClass());
                });
    }

    /**
     * 判断异常链中是否包含指定错误消息。
     *
     * @param throwable 待检查异常
     * @param expectedMessage 期望出现的错误消息片段
     * @return 任一层异常消息包含期望文本时返回 true
     */
    private boolean hasCauseMessage(Throwable throwable, String expectedMessage) {
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(expectedMessage)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
