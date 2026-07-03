/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.config;

import com.zhyc.openapi.security.ApiCallAuditRecorder;
import com.zhyc.openapi.security.ApiKeyAuthenticator;
import com.zhyc.openapi.security.ApiKeyCredentialRepository;
import com.zhyc.openapi.security.ApiKeySignatureVerifier;
import com.zhyc.openapi.security.ApiClientIpWhitelistRepository;
import com.zhyc.openapi.security.ApiPermissionRepository;
import com.zhyc.openapi.security.ApiSecretResolver;
import com.zhyc.openapi.security.DisabledOAuth2TokenVerifier;
import com.zhyc.openapi.security.FallbackOpenApiRateLimiter;
import com.zhyc.openapi.security.FallbackOpenApiReplayNonceStore;
import com.zhyc.openapi.security.IntrospectionOAuth2TokenVerifier;
import com.zhyc.openapi.security.JdbcApiCallAuditRecorder;
import com.zhyc.openapi.security.JdbcApiClientIpWhitelistRepository;
import com.zhyc.openapi.security.JdbcApiKeyCredentialRepository;
import com.zhyc.openapi.security.JdbcApiPermissionRepository;
import com.zhyc.openapi.security.JdbcOpenApiRateLimiter;
import com.zhyc.openapi.security.JdbcOpenApiOAuth2ClientMappingRepository;
import com.zhyc.openapi.security.JdbcOpenApiRateLimitPolicyRepository;
import com.zhyc.openapi.security.JdbcOpenApiReplayNonceStore;
import com.zhyc.openapi.security.JdbcOpenApiRouteRepository;
import com.zhyc.openapi.security.JdbcOpenApiSignaturePolicyRepository;
import com.zhyc.openapi.security.OAuth2TokenIntrospectionClient;
import com.zhyc.openapi.security.OAuth2TokenVerifier;
import com.zhyc.openapi.security.OpenApiApiKeyAuthenticationFilter;
import com.zhyc.openapi.security.OpenApiBackendInvoker;
import com.zhyc.openapi.security.OpenApiOAuth2AuthenticationFilter;
import com.zhyc.openapi.security.OpenApiOAuth2ClientMappingRepository;
import com.zhyc.openapi.security.OpenApiRateLimitFilter;
import com.zhyc.openapi.security.OpenApiRateLimitPolicyRepository;
import com.zhyc.openapi.security.OpenApiRateLimiter;
import com.zhyc.openapi.security.OpenApiReplayNonceStore;
import com.zhyc.openapi.security.OpenApiRouteRepository;
import com.zhyc.openapi.security.OpenApiRoutingFilter;
import com.zhyc.openapi.security.OpenApiSignaturePolicyRepository;
import com.zhyc.openapi.security.ReplayProtector;
import com.zhyc.openapi.security.RestClientOpenApiBackendInvoker;
import com.zhyc.openapi.security.RestClientOAuth2TokenIntrospectionClient;
import com.zhyc.openapi.security.RedisOpenApiRateLimiter;
import com.zhyc.openapi.security.RedisOpenApiReplayNonceStore;
import java.time.Clock;
import java.time.Duration;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

/**
 * Open API 网关安全配置。
 *
 * <p>注册 API Key 签名校验、OAuth2 Token 校验、防重放、应用接口授权、限流和调用审计的运行期组件。
 * 灰度路由仍保留为独立扩展点。</p>
 */
@Configuration
public class OpenApiGatewaySecurityConfig {

  /** 默认防重放时间窗口。 */
  private static final Duration DEFAULT_REPLAY_WINDOW = Duration.ofMinutes(5);
  /** 默认本地 nonce 最大记录数量。 */
  private static final int DEFAULT_MAX_NONCE_ENTRIES = 100_000;
  /** Open API 过滤器 URL 匹配表达式。 */
  private static final String OPENAPI_URL_PATTERN = "/openapi/*";

  /**
   * 创建网关运行期时钟。
   *
   * @return UTC 系统时钟
   */
  @Bean
  @ConditionalOnMissingBean
  public Clock openApiGatewayClock() {
    return Clock.systemUTC();
  }

  /**
   * 创建网关 JDBC 操作模板。
   *
   * <p>开放 API 网关核心仓储均基于 JDBC 访问数据库。此处提供显式兜底，避免 IDE 或精简运行
   * classpath 未加载 Spring Boot JDBC 模板自动配置时，安全过滤器链因缺少 JdbcTemplate 无法启动。</p>
   *
   * @param dataSource 网关数据源
   * @return JDBC 操作模板
   */
  @Bean
  @ConditionalOnMissingBean
  public JdbcTemplate openApiJdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  /**
   * 创建 API Secret 解析器。
   *
   * <p>默认实现把持久化值作为运行态 Secret 使用，生产环境可以替换为 KMS、配置中心或加密组件
   * 解密实现，避免在业务代码中直接处理密钥细节。</p>
   *
   * @return API Secret 解析器
   */
  @Bean
  @ConditionalOnMissingBean
  public ApiSecretResolver apiSecretResolver() {
    return secretCipher -> secretCipher;
  }

  /**
   * 创建 API Key 签名校验器。
   *
   * @return API Key 签名校验器
   */
  @Bean
  @ConditionalOnMissingBean
  public ApiKeySignatureVerifier apiKeySignatureVerifier() {
    return new ApiKeySignatureVerifier();
  }

  /**
   * 创建 API Key 凭证仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @param apiSecretResolver API Secret 解析器
   * @return API Key 凭证仓储
   */
  @Bean
  @ConditionalOnMissingBean
  public ApiKeyCredentialRepository apiKeyCredentialRepository(JdbcTemplate jdbcTemplate,
      ApiSecretResolver apiSecretResolver) {
    return new JdbcApiKeyCredentialRepository(jdbcTemplate, apiSecretResolver);
  }

  /**
   * 创建 Redis 优先的 Open API 防重放 nonce 存储。
   *
   * <p>Redis 异常时自动回退 JDBC 存储，避免缓存中间件抖动影响开放 API 防重放能力。</p>
   *
   * @param openApiGatewayClock 网关运行期时钟
   * @param jdbcTemplate JDBC 操作模板
   * @param redisTemplate Redis 字符串操作模板
   * @param cachePrefix Redis Key 前缀
   * @return nonce 存储
   */
  @Bean
  @ConditionalOnProperty(prefix = "zhyc.cache.redis", name = "enabled", havingValue = "true")
  @ConditionalOnMissingBean
  public OpenApiReplayNonceStore redisOpenApiReplayNonceStore(Clock openApiGatewayClock,
      JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate,
      @Value("${zhyc.cache.prefix:zhyc}") String cachePrefix) {
    return new FallbackOpenApiReplayNonceStore(
        new RedisOpenApiReplayNonceStore(redisTemplate, openApiGatewayClock, cachePrefix),
        new JdbcOpenApiReplayNonceStore(jdbcTemplate));
  }

  /**
   * 创建 JDBC Open API 防重放 nonce 存储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @return nonce 存储
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiReplayNonceStore openApiReplayNonceStore(JdbcTemplate jdbcTemplate) {
    return new JdbcOpenApiReplayNonceStore(jdbcTemplate);
  }

  /**
   * 创建 Open API 防重放保护器。
   *
   * @param openApiGatewayClock 网关运行期时钟
   * @param nonceStore nonce 存储
   * @return 防重放保护器
   */
  @Bean
  @ConditionalOnMissingBean
  public ReplayProtector replayProtector(Clock openApiGatewayClock, OpenApiReplayNonceStore nonceStore) {
    return new ReplayProtector(DEFAULT_REPLAY_WINDOW, openApiGatewayClock, DEFAULT_MAX_NONCE_ENTRIES, nonceStore);
  }

  /**
   * 创建开放 API 签名策略仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @return 开放 API 签名策略仓储
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiSignaturePolicyRepository openApiSignaturePolicyRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcOpenApiSignaturePolicyRepository(jdbcTemplate);
  }

  /**
   * 创建 API Key 鉴权器。
   *
   * @param apiKeyCredentialRepository API Key 凭证仓储
   * @param apiKeySignatureVerifier API Key 签名校验器
   * @param replayProtector 防重放保护器
   * @param openApiSignaturePolicyRepository 开放 API 签名策略仓储
   * @param openApiGatewayClock 网关运行期时钟
   * @return API Key 鉴权器
   */
  @Bean
  @ConditionalOnMissingBean
  public ApiKeyAuthenticator apiKeyAuthenticator(ApiKeyCredentialRepository apiKeyCredentialRepository,
      ApiKeySignatureVerifier apiKeySignatureVerifier, ReplayProtector replayProtector,
      OpenApiSignaturePolicyRepository openApiSignaturePolicyRepository, Clock openApiGatewayClock) {
    return new ApiKeyAuthenticator(apiKeyCredentialRepository, apiKeySignatureVerifier, replayProtector,
        openApiSignaturePolicyRepository, openApiGatewayClock);
  }

  /**
   * 创建开放 API 运行态权限仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @return 开放 API 运行态权限仓储
   */
  @Bean
  @ConditionalOnMissingBean
  public ApiPermissionRepository apiPermissionRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcApiPermissionRepository(jdbcTemplate);
  }

  /**
   * 创建开放 API 应用客户端 IP 白名单仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @return 开放 API 应用客户端 IP 白名单仓储
   */
  @Bean
  @ConditionalOnMissingBean
  public ApiClientIpWhitelistRepository apiClientIpWhitelistRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcApiClientIpWhitelistRepository(jdbcTemplate);
  }

  /**
   * 创建开放 API 调用审计记录器。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @return 开放 API 调用审计记录器
   */
  @Bean
  @ConditionalOnMissingBean
  public ApiCallAuditRecorder apiCallAuditRecorder(JdbcTemplate jdbcTemplate) {
    return new JdbcApiCallAuditRecorder(jdbcTemplate);
  }

  /**
   * 创建开放平台 OAuth2 客户端运行态映射仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @return OAuth2 客户端运行态映射仓储
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiOAuth2ClientMappingRepository openApiOAuth2ClientMappingRepository(
      JdbcTemplate jdbcTemplate) {
    return new JdbcOpenApiOAuth2ClientMappingRepository(jdbcTemplate);
  }

  /**
   * 创建开放 API 限流策略仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @return 开放 API 限流策略仓储
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiRateLimitPolicyRepository openApiRateLimitPolicyRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcOpenApiRateLimitPolicyRepository(jdbcTemplate);
  }

  /**
   * 创建开放 API 运行态路由仓储。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @return 开放 API 运行态路由仓储
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiRouteRepository openApiRouteRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcOpenApiRouteRepository(jdbcTemplate);
  }

  /**
   * 创建 Redis 优先的开放 API 限流器。
   *
   * <p>Redis 异常时自动回退 JDBC 限流器。</p>
   *
   * @param jdbcTemplate JDBC 操作模板
   * @param openApiGatewayClock 网关运行期时钟
   * @param redisTemplate Redis 字符串操作模板
   * @param cachePrefix Redis Key 前缀
   * @return 开放 API 限流器
   */
  @Bean
  @ConditionalOnProperty(prefix = "zhyc.cache.redis", name = "enabled", havingValue = "true")
  @ConditionalOnMissingBean
  public OpenApiRateLimiter redisOpenApiRateLimiter(JdbcTemplate jdbcTemplate, Clock openApiGatewayClock,
      StringRedisTemplate redisTemplate, @Value("${zhyc.cache.prefix:zhyc}") String cachePrefix) {
    return new FallbackOpenApiRateLimiter(
        new RedisOpenApiRateLimiter(redisTemplate, openApiGatewayClock, cachePrefix),
        new JdbcOpenApiRateLimiter(jdbcTemplate, openApiGatewayClock));
  }

  /**
   * 创建 JDBC 开放 API 限流器。
   *
   * @param jdbcTemplate JDBC 操作模板
   * @param openApiGatewayClock 网关运行期时钟
   * @return 开放 API 限流器
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiRateLimiter openApiRateLimiter(JdbcTemplate jdbcTemplate, Clock openApiGatewayClock) {
    return new JdbcOpenApiRateLimiter(jdbcTemplate, openApiGatewayClock);
  }

  /**
   * 创建 OAuth2 Token 校验器。
   *
   * <p>配置 {@code zhyc.openapi.oauth2.introspection-uri} 后启用认证中心 introspection 校验；
   * 未配置时返回稳定拒绝结果，避免 Bearer Token 在网关未接入认证中心时被误放行。</p>
   *
   * @param introspectionUri OAuth2 introspection 端点地址
   * @param introspectionClientId OAuth2 introspection 客户端 ID
   * @param introspectionClientSecret OAuth2 introspection 客户端密钥
   * @param mappingRepository OAuth2 客户端运行态映射仓储
   * @return OAuth2 Token 校验器
   */
  @Bean
  @ConditionalOnMissingBean
  public OAuth2TokenVerifier oauth2TokenVerifier(
      @Value("${zhyc.openapi.oauth2.introspection-uri:}") String introspectionUri,
      @Value("${zhyc.openapi.oauth2.client-id:}") String introspectionClientId,
      @Value("${zhyc.openapi.oauth2.client-secret:}") String introspectionClientSecret,
      OpenApiOAuth2ClientMappingRepository mappingRepository) {
    if (introspectionUri == null || introspectionUri.isBlank()) {
      return new DisabledOAuth2TokenVerifier();
    }
    OAuth2TokenIntrospectionClient introspectionClient = new RestClientOAuth2TokenIntrospectionClient(
        introspectionUri, introspectionClientId, introspectionClientSecret, RestClient.create());
    return new IntrospectionOAuth2TokenVerifier(introspectionClient, mappingRepository);
  }

  /**
   * 创建 OAuth2 Token 鉴权过滤器。
   *
   * @param oauth2TokenVerifier OAuth2 Token 校验器
   * @param apiPermissionRepository 开放 API 运行态权限仓储
   * @param apiClientIpWhitelistRepository 开放 API 应用客户端 IP 白名单仓储
   * @param apiCallAuditRecorder 开放 API 调用审计记录器
   * @param openApiGatewayClock 网关运行期时钟
   * @return OAuth2 Token 鉴权过滤器
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiOAuth2AuthenticationFilter openApiOAuth2AuthenticationFilter(
      OAuth2TokenVerifier oauth2TokenVerifier, ApiPermissionRepository apiPermissionRepository,
      ApiClientIpWhitelistRepository apiClientIpWhitelistRepository,
      ApiCallAuditRecorder apiCallAuditRecorder, Clock openApiGatewayClock) {
    return new OpenApiOAuth2AuthenticationFilter(oauth2TokenVerifier, apiPermissionRepository,
        apiClientIpWhitelistRepository, apiCallAuditRecorder, openApiGatewayClock);
  }

  /**
   * 创建 API Key 鉴权过滤器。
   *
   * @param apiKeyAuthenticator API Key 鉴权器
   * @param apiPermissionRepository 开放 API 运行态权限仓储
   * @param apiClientIpWhitelistRepository 开放 API 应用客户端 IP 白名单仓储
   * @param apiCallAuditRecorder 开放 API 调用审计记录器
   * @param openApiGatewayClock 网关运行期时钟
   * @return API Key 鉴权过滤器
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiApiKeyAuthenticationFilter openApiApiKeyAuthenticationFilter(
      ApiKeyAuthenticator apiKeyAuthenticator, ApiPermissionRepository apiPermissionRepository,
      ApiClientIpWhitelistRepository apiClientIpWhitelistRepository,
      ApiCallAuditRecorder apiCallAuditRecorder, Clock openApiGatewayClock) {
    return new OpenApiApiKeyAuthenticationFilter(apiKeyAuthenticator, apiPermissionRepository,
        apiClientIpWhitelistRepository, apiCallAuditRecorder, openApiGatewayClock);
  }

  /**
   * 创建开放 API 限流过滤器。
   *
   * @param policyRepository 限流策略仓储
   * @param rateLimiter 开放 API 限流器
   * @return 开放 API 限流过滤器
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiRateLimitFilter openApiRateLimitFilter(
      OpenApiRateLimitPolicyRepository policyRepository, OpenApiRateLimiter rateLimiter) {
    return new OpenApiRateLimitFilter(policyRepository, rateLimiter);
  }

  /**
   * 创建开放 API 后端服务调用器。
   *
   * @return 开放 API 后端服务调用器
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiBackendInvoker openApiBackendInvoker() {
    return new RestClientOpenApiBackendInvoker(RestClient.create());
  }

  /**
   * 创建开放 API 路由转发过滤器。
   *
   * @param routeRepository 运行态路由仓储
   * @param backendInvoker 后端服务调用器
   * @return 开放 API 路由转发过滤器
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiRoutingFilter openApiRoutingFilter(OpenApiRouteRepository routeRepository,
      OpenApiBackendInvoker backendInvoker) {
    return new OpenApiRoutingFilter(routeRepository, backendInvoker);
  }

  /**
   * 注册 Open API OAuth2 Token 鉴权过滤器。
   *
   * @param filter OAuth2 Token 鉴权过滤器
   * @return Servlet 过滤器注册 Bean
   */
  @Bean
  @ConditionalOnMissingBean(name = "openApiOAuth2AuthenticationFilterRegistration")
  public FilterRegistrationBean<OpenApiOAuth2AuthenticationFilter> openApiOAuth2AuthenticationFilterRegistration(
      OpenApiOAuth2AuthenticationFilter filter) {
    FilterRegistrationBean<OpenApiOAuth2AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setName("openApiOAuth2AuthenticationFilter");
    registrationBean.setFilter(filter);
    registrationBean.addUrlPatterns(OPENAPI_URL_PATTERN);
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    return registrationBean;
  }

  /**
   * 注册 Open API API Key 鉴权过滤器。
   *
   * @param filter API Key 鉴权过滤器
   * @return Servlet 过滤器注册 Bean
   */
  @Bean
  @ConditionalOnMissingBean(name = "openApiApiKeyAuthenticationFilterRegistration")
  public FilterRegistrationBean<OpenApiApiKeyAuthenticationFilter> openApiApiKeyAuthenticationFilterRegistration(
      OpenApiApiKeyAuthenticationFilter filter) {
    FilterRegistrationBean<OpenApiApiKeyAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setName("openApiApiKeyAuthenticationFilter");
    registrationBean.setFilter(filter);
    registrationBean.addUrlPatterns(OPENAPI_URL_PATTERN);
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
    return registrationBean;
  }

  /**
   * 注册 Open API 限流过滤器。
   *
   * @param filter 限流过滤器
   * @return Servlet 过滤器注册 Bean
   */
  @Bean
  @ConditionalOnMissingBean(name = "openApiRateLimitFilterRegistration")
  public FilterRegistrationBean<OpenApiRateLimitFilter> openApiRateLimitFilterRegistration(
      OpenApiRateLimitFilter filter) {
    FilterRegistrationBean<OpenApiRateLimitFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setName("openApiRateLimitFilter");
    registrationBean.setFilter(filter);
    registrationBean.addUrlPatterns(OPENAPI_URL_PATTERN);
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
    return registrationBean;
  }

  /**
   * 注册 Open API 路由转发过滤器。
   *
   * @param filter 路由转发过滤器
   * @return Servlet 过滤器注册 Bean
   */
  @Bean
  @ConditionalOnMissingBean(name = "openApiRoutingFilterRegistration")
  public FilterRegistrationBean<OpenApiRoutingFilter> openApiRoutingFilterRegistration(
      OpenApiRoutingFilter filter) {
    FilterRegistrationBean<OpenApiRoutingFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setName("openApiRoutingFilter");
    registrationBean.setFilter(filter);
    registrationBean.addUrlPatterns(OPENAPI_URL_PATTERN);
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 40);
    return registrationBean;
  }
}
