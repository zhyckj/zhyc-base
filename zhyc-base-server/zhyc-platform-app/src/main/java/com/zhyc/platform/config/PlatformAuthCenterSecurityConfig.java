/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.config;

import com.zhyc.platform.security.PlatformAuthCenterBearerFilter;
import com.zhyc.platform.security.PlatformShiroSubjectAuthenticator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;
import org.apache.shiro.mgt.SecurityManager;

/**
 * 平台认证中心安全配置。
 *
 * <p>负责把认证中心签发的 OAuth2/OIDC Bearer Token 接入 Shiro Subject，不引入 Spring Security 业务授权链。</p>
 */
@Configuration
public class PlatformAuthCenterSecurityConfig {

    /**
     * 定义 Shiro Subject 登录适配器。
     *
     * @return Shiro Subject 登录适配器
     */
    @Bean
    public PlatformShiroSubjectAuthenticator platformShiroSubjectAuthenticator() {
        return new PlatformShiroSubjectAuthenticator();
    }

    /**
     * 定义认证中心 JWT 解码器。
     *
     * <p>配置 {@code zhyc.platform.auth.jwk-set-uri} 后启用，使用认证中心 JWK Set 校验签名，
     * 并通过 {@code zhyc.platform.auth.issuer} 约束令牌签发方。</p>
     *
     * @param jwkSetUri 认证中心 JWK Set 地址
     * @param issuer 认证中心令牌签发方
     * @return JWT 解码器
     */
    @Bean
    @Conditional(AuthCenterJwkSetUriCondition.class)
    public JwtDecoder platformAuthCenterJwtDecoder(
            @Value("${zhyc.platform.auth.jwk-set-uri}") String jwkSetUri,
            @Value("${zhyc.platform.auth.issuer:}") String issuer) {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalStateException("启用认证中心 JWK Set 时必须配置 zhyc.platform.auth.issuer");
        }
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return jwtDecoder;
    }

    /**
     * 注册认证中心 Bearer Token 过滤器。
     *
     * <p>过滤器只在 JWT 解码器存在时启用，并在业务 Controller 前建立 Shiro Subject。</p>
     *
     * @param jwtDecoder JWT 解码器
     * @param subjectAuthenticator Shiro Subject 登录适配器
     * @param securityManager Shiro 安全管理器
     * @return 过滤器注册 Bean
     */
    @Bean
    @ConditionalOnBean(JwtDecoder.class)
    public FilterRegistrationBean<PlatformAuthCenterBearerFilter> platformAuthCenterBearerFilter(
            JwtDecoder jwtDecoder, PlatformShiroSubjectAuthenticator subjectAuthenticator,
            SecurityManager securityManager) {
        FilterRegistrationBean<PlatformAuthCenterBearerFilter> registrationBean =
                new FilterRegistrationBean<>(new PlatformAuthCenterBearerFilter(jwtDecoder, subjectAuthenticator,
                        securityManager));
        registrationBean.setName("platformAuthCenterBearerFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return registrationBean;
    }

    /**
     * 认证中心 JWK Set 地址启用条件。
     *
     * <p>只有配置了非空白 {@code zhyc.platform.auth.jwk-set-uri} 时才启用 JWT 解码器，
     * 避免空环境变量导致核心平台启动失败。</p>
     */
    static final class AuthCenterJwkSetUriCondition implements Condition {

        /**
         * 判断认证中心 JWK Set 地址是否具备启用条件。
         *
         * @param context 条件判断上下文
         * @param metadata 注解元数据
         * @return 配置值存在且包含非空白文本时返回 true
         */
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String jwkSetUri = context.getEnvironment().getProperty("zhyc.platform.auth.jwk-set-uri");
            return StringUtils.hasText(jwkSetUri);
        }
    }
}
