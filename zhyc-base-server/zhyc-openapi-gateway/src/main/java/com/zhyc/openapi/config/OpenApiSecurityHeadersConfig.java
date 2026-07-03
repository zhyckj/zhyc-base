/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.config;

import com.zhyc.openapi.security.OpenApiSecurityHeadersFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 开放 API 网关安全响应头配置。
 */
@Configuration
public class OpenApiSecurityHeadersConfig {

  /**
   * 注册开放 API 网关安全响应头过滤器。
   *
   * @return 安全响应头过滤器注册 Bean
   */
  @Bean
  @ConditionalOnMissingBean(name = "openApiSecurityHeadersFilterRegistration")
  public FilterRegistrationBean<OpenApiSecurityHeadersFilter> openApiSecurityHeadersFilterRegistration() {
    FilterRegistrationBean<OpenApiSecurityHeadersFilter> registrationBean =
        new FilterRegistrationBean<>(new OpenApiSecurityHeadersFilter());
    registrationBean.setName("openApiSecurityHeadersFilter");
    registrationBean.addUrlPatterns("/*");
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registrationBean;
  }
}
