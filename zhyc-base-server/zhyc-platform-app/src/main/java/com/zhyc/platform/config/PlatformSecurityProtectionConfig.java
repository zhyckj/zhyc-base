/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.config;

import com.zhyc.platform.security.PlatformSecurityProtectionFilter;
import com.zhyc.system.securityprotection.service.SysSecurityProtectionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 平台运行时安全防护配置。
 */
@Configuration
@ConditionalOnProperty(prefix = "zhyc.security.protection", name = "enabled", havingValue = "true",
        matchIfMissing = true)
public class PlatformSecurityProtectionConfig {

    /**
     * 注册平台运行时安全防护过滤器。
     *
     * @param securityProtectionService 系统安全防护中心业务服务
     * @return 安全防护过滤器注册 Bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "platformSecurityProtectionFilterRegistration")
    public FilterRegistrationBean<PlatformSecurityProtectionFilter> platformSecurityProtectionFilterRegistration(
            SysSecurityProtectionService securityProtectionService) {
        FilterRegistrationBean<PlatformSecurityProtectionFilter> registrationBean =
                new FilterRegistrationBean<>(new PlatformSecurityProtectionFilter(securityProtectionService));
        registrationBean.setName("platformSecurityProtectionFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
        return registrationBean;
    }
}
