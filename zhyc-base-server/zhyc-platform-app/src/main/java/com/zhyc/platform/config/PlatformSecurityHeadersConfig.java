/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.config;

import com.zhyc.platform.security.PlatformSecurityHeadersFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 平台管理端安全响应头配置。
 */
@Configuration
public class PlatformSecurityHeadersConfig {

    /**
     * 注册平台管理端安全响应头过滤器。
     *
     * @return 安全响应头过滤器注册 Bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "platformSecurityHeadersFilterRegistration")
    public FilterRegistrationBean<PlatformSecurityHeadersFilter> platformSecurityHeadersFilterRegistration() {
        FilterRegistrationBean<PlatformSecurityHeadersFilter> registrationBean =
                new FilterRegistrationBean<>(new PlatformSecurityHeadersFilter());
        registrationBean.setName("platformSecurityHeadersFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
