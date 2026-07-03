/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth.config;

import com.zhyc.auth.audit.AuthLoginAuditService;
import com.zhyc.auth.security.JdbcPlatformUserDetailsService;
import com.zhyc.auth.security.ShiroPasswordEncoder;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.ClientSecretAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Spring Authorization Server 配置。
 *
 * <p>首期支持 OAuth2/OIDC 客户端、授权记录和授权确认持久化；未配置认证中心数据源时仅回退到本地开发内存模式。</p>
 */
@Configuration
public class AuthorizationServerConfig {

  /** 日志记录器，用于输出认证中心启动期配置同步状态。 */
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationServerConfig.class);

  /**
   * 配置授权服务器安全过滤器链，启用 Spring Authorization Server 默认端点安全规则。
   *
   * @param http Spring Security HTTP 配置对象
   * @return 授权服务器过滤器链
   * @throws Exception 安全配置构建失败时抛出
   */
  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public SecurityFilterChain authorizationServerSecurityFilterChain(
      HttpSecurity http,
      @Qualifier("clientPasswordEncoder") PasswordEncoder clientPasswordEncoder,
      @Value("${zhyc.auth.frontend-login-uri:http://127.0.0.1:5173/login?authRequest=1}")
      String frontendLoginUri) throws Exception {
    PathPatternRequestMatcher tokenEndpointMatcher =
        PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/oauth2/token");
    PathPatternRequestMatcher introspectionEndpointMatcher =
        PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/oauth2/introspect");
    PathPatternRequestMatcher revocationEndpointMatcher =
        PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/oauth2/revoke");
    String normalizedFrontendLoginUri = AuthFrontendLoginUris.normalizeAuthRequestLoginUri(frontendLoginUri);
    return http
        .securityMatcher("/oauth2/**", "/.well-known/**")
        .authorizeHttpRequests(authorizeRequests -> authorizeRequests
            .requestMatchers(tokenEndpointMatcher, introspectionEndpointMatcher, revocationEndpointMatcher).permitAll()
            .requestMatchers("/oauth2/jwks", "/.well-known/**").permitAll()
            .anyRequest().authenticated())
        .oauth2AuthorizationServer(authorizationServer -> authorizationServer
            .clientAuthentication(clientAuthentication -> clientAuthentication.authenticationProviders(
                authenticationProviders -> authenticationProviders.forEach(authenticationProvider -> {
                  if (authenticationProvider instanceof ClientSecretAuthenticationProvider clientSecretProvider) {
                    clientSecretProvider.setPasswordEncoder(clientPasswordEncoder);
                  }
                })))
            .oidc(Customizer.withDefaults()))
        .csrf(csrf -> csrf.ignoringRequestMatchers(
            tokenEndpointMatcher, introspectionEndpointMatcher, revocationEndpointMatcher))
        .exceptionHandling(exceptionHandling -> exceptionHandling.defaultAuthenticationEntryPointFor(
            new LoginUrlAuthenticationEntryPoint(normalizedFrontendLoginUri),
            new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
        .build();
  }

  /**
   * 配置基础 Web 安全过滤器链，用于登录页等非授权服务器端点。
   *
   * @param http Spring Security HTTP 配置对象
   * @param authLoginAuditService 认证中心登录审计服务
   * @return 基础 Web 安全过滤器链
   * @throws Exception 安全配置构建失败时抛出
   */
  @Bean
  @Order(Ordered.LOWEST_PRECEDENCE)
  public SecurityFilterChain defaultSecurityFilterChain(
      HttpSecurity http,
      AuthLoginAuditService authLoginAuditService,
      @Value("${zhyc.auth.login-success-redirect-uri:http://127.0.0.1:5173/login}")
      String loginSuccessRedirectUri,
      @Value("${zhyc.auth.frontend-login-uri:http://127.0.0.1:5173/login?authRequest=1}")
      String frontendLoginUri,
      @Value("${zhyc.auth.post-logout-redirect-uri:http://127.0.0.1:5173/login?loggedOut=1}")
      String postLogoutRedirectUri) throws Exception {
    PathPatternRequestMatcher loginRequestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/login");
    PathPatternRequestMatcher logoutRequestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/logout");
    PathPatternRequestMatcher mobileLoginRequestMatcher =
        PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/mobile/auth/login");
    String normalizedFrontendLoginUri = AuthFrontendLoginUris.normalizeAuthRequestLoginUri(frontendLoginUri);
    String normalizedLoginSuccessRedirectUri = requireText(
        loginSuccessRedirectUri, "认证中心登录成功兜底回跳地址不能为空");
    SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    successHandler.setDefaultTargetUrl(normalizedLoginSuccessRedirectUri);
    SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler(
        AuthFrontendLoginUris.appendQueryParam(normalizedFrontendLoginUri, "error", "1"));
    return http
        .authorizeHttpRequests(authorizeRequests -> authorizeRequests
            .requestMatchers("/login").permitAll()
            .requestMatchers(mobileLoginRequestMatcher).permitAll()
            .anyRequest().authenticated())
        .formLogin(formLogin -> formLogin
            .loginPage("/login")
            .successHandler((request, response, authentication) -> {
              authLoginAuditService.recordSuccess(authentication, request);
              successHandler.onAuthenticationSuccess(request, response, authentication);
            })
            .failureHandler((request, response, exception) -> {
              authLoginAuditService.recordFailure(request.getParameter("username"), request, exception);
              failureHandler.onAuthenticationFailure(request, response, exception);
            })
            .permitAll())
        .logout(logout -> logout
            .logoutRequestMatcher(logoutRequestMatcher)
            .logoutSuccessUrl(requireText(postLogoutRedirectUri, "认证中心登出回跳地址不能为空"))
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID"))
        .csrf(csrf -> csrf.ignoringRequestMatchers(loginRequestMatcher, logoutRequestMatcher,
            mobileLoginRequestMatcher))
        .build();
  }

  /**
   * 注册 OAuth2 客户端仓库。
   *
   * <p>配置 `zhyc.auth.datasource.url` 后使用 JDBC 仓库，并在客户端不存在时写入首期启动客户端；
   * 未配置认证中心数据源时仅用于本地开发回退。</p>
   *
   * @param clientPasswordEncoder OAuth2 客户端密钥密码编码器
   * @param clientId OAuth2 客户端标识
   * @param clientSecret OAuth2 客户端密钥
   * @param redirectUri OAuth2 授权码回调地址
   * @param accessTokenTimeToLiveMinutes 访问令牌有效期分钟数
   * @param refreshTokenTimeToLiveHours 刷新令牌有效期小时数
   * @param datasourceUrl 认证中心数据源地址
   * @param datasourceUsername 认证中心数据源用户名
   * @param datasourcePassword 认证中心数据源密码
   * @param datasourceDriverClassName 认证中心数据源驱动类名
   * @return OAuth2 客户端仓库
   */
  @Bean
  public RegisteredClientRepository registeredClientRepository(
      @Qualifier("clientPasswordEncoder") PasswordEncoder clientPasswordEncoder,
      @Value("${zhyc.auth.client-id}") String clientId,
      @Value("${zhyc.auth.client-secret}") String clientSecret,
      @Value("${zhyc.auth.redirect-uri}") String redirectUri,
      @Value("${zhyc.auth.access-token-time-to-live-minutes:30}") long accessTokenTimeToLiveMinutes,
      @Value("${zhyc.auth.refresh-token-time-to-live-hours:8}") long refreshTokenTimeToLiveHours,
      @Value("${zhyc.auth.datasource.url:}") String datasourceUrl,
      @Value("${zhyc.auth.datasource.username:}") String datasourceUsername,
      @Value("${zhyc.auth.datasource.password:}") String datasourcePassword,
      @Value("${zhyc.auth.datasource.driver-class-name:}") String datasourceDriverClassName) {
    TokenSettings tokenSettings = buildTokenSettings(accessTokenTimeToLiveMinutes, refreshTokenTimeToLiveHours);
    RegisteredClient zhycClient = buildBootstrapClient(
        clientPasswordEncoder, clientId, clientSecret, redirectUri, tokenSettings);
    JdbcOperations jdbcOperations = createJdbcOperations(
        datasourceUrl, datasourceUsername, datasourcePassword, datasourceDriverClassName);
    if (jdbcOperations == null) {
      return new InMemoryRegisteredClientRepository(zhycClient);
    }
    JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcOperations);
    synchronizeBootstrapClient(
        repository, jdbcOperations, clientPasswordEncoder, zhycClient, clientSecret, redirectUri, tokenSettings);
    return repository;
  }

  /**
   * 同步首期启动客户端配置。
   *
   * <p>JDBC 仓库已有同 clientId 记录时不会自动更新密钥和回调地址；本方法在配置漂移时替换启动客户端，
   * 避免本地或部署环境修改 client secret 后出现授权码换令牌失败。</p>
   *
   * @param repository OAuth2 注册客户端仓库
   * @param jdbcOperations 认证中心 JDBC 操作对象
   * @param passwordEncoder 密码编码器
   * @param zhycClient 当前配置构建的启动客户端
   * @param clientSecret 当前配置中的客户端密钥明文
   * @param redirectUri 当前配置中的授权回调地址
   * @param tokenSettings 当前配置中的令牌有效期策略
   */
  private static void synchronizeBootstrapClient(
      JdbcRegisteredClientRepository repository,
      JdbcOperations jdbcOperations,
      PasswordEncoder passwordEncoder,
      RegisteredClient zhycClient,
      String clientSecret,
      String redirectUri,
      TokenSettings tokenSettings) {
    RegisteredClient existingClient = repository.findByClientId(zhycClient.getClientId());
    if (existingClient == null) {
      repository.save(zhycClient);
      LOGGER.info("认证中心 OAuth2 启动客户端已创建：clientId={}，redirectUri={}，secretFingerprint={}",
          zhycClient.getClientId(), redirectUri, secretFingerprint(clientSecret));
      return;
    }
    if (isBootstrapClientCurrent(existingClient, passwordEncoder, clientSecret, redirectUri, tokenSettings)) {
      LOGGER.info("认证中心 OAuth2 启动客户端配置已一致：clientId={}，redirectUri={}，secretFingerprint={}",
          zhycClient.getClientId(), redirectUri, secretFingerprint(clientSecret));
      return;
    }
    jdbcOperations.update("delete from oauth2_authorization_consent where registered_client_id = ?", existingClient.getId());
    jdbcOperations.update("delete from oauth2_authorization where registered_client_id = ?", existingClient.getId());
    jdbcOperations.update("delete from oauth2_registered_client where client_id = ?", zhycClient.getClientId());
    repository.save(zhycClient);
    LOGGER.info("认证中心 OAuth2 启动客户端配置已刷新：clientId={}，redirectUri={}，secretFingerprint={}",
        zhycClient.getClientId(), redirectUri, secretFingerprint(clientSecret));
  }

  /**
   * 提供 OAuth2 授权记录服务。
   *
   * <p>配置认证中心数据源后使用 `oauth2_authorization` 表持久化授权码、访问令牌和刷新令牌；
   * 未配置时仅使用本地开发内存服务。</p>
   *
   * @param registeredClientRepository OAuth2 客户端仓库
   * @param datasourceUrl 认证中心数据源地址
   * @param datasourceUsername 认证中心数据源用户名
   * @param datasourcePassword 认证中心数据源密码
   * @param datasourceDriverClassName 认证中心数据源驱动类名
   * @return OAuth2 授权记录服务
   */
  @Bean
  public OAuth2AuthorizationService authorizationService(
      RegisteredClientRepository registeredClientRepository,
      @Value("${zhyc.auth.datasource.url:}") String datasourceUrl,
      @Value("${zhyc.auth.datasource.username:}") String datasourceUsername,
      @Value("${zhyc.auth.datasource.password:}") String datasourcePassword,
      @Value("${zhyc.auth.datasource.driver-class-name:}") String datasourceDriverClassName) {
    JdbcOperations jdbcOperations = createJdbcOperations(
        datasourceUrl, datasourceUsername, datasourcePassword, datasourceDriverClassName);
    if (jdbcOperations == null) {
      return new InMemoryOAuth2AuthorizationService();
    }
    return new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
  }

  /**
   * 提供 OAuth2 授权确认服务。
   *
   * <p>配置认证中心数据源后使用 `oauth2_authorization_consent` 表保存用户授权确认结果；
   * 未配置时仅使用本地开发内存服务。</p>
   *
   * @param registeredClientRepository OAuth2 客户端仓库
   * @param datasourceUrl 认证中心数据源地址
   * @param datasourceUsername 认证中心数据源用户名
   * @param datasourcePassword 认证中心数据源密码
   * @param datasourceDriverClassName 认证中心数据源驱动类名
   * @return OAuth2 授权确认服务
   */
  @Bean
  public OAuth2AuthorizationConsentService authorizationConsentService(
      RegisteredClientRepository registeredClientRepository,
      @Value("${zhyc.auth.datasource.url:}") String datasourceUrl,
      @Value("${zhyc.auth.datasource.username:}") String datasourceUsername,
      @Value("${zhyc.auth.datasource.password:}") String datasourcePassword,
      @Value("${zhyc.auth.datasource.driver-class-name:}") String datasourceDriverClassName) {
    JdbcOperations jdbcOperations = createJdbcOperations(
        datasourceUrl, datasourceUsername, datasourcePassword, datasourceDriverClassName);
    if (jdbcOperations == null) {
      return new InMemoryOAuth2AuthorizationConsentService();
    }
    return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository);
  }

  /**
   * 提供 Shiro 密码编码器，用于校验平台用户密码哈希。
   *
   * @return Shiro 密码编码器
   */
  @Bean
  @Primary
  public PasswordEncoder passwordEncoder() {
    return new ShiroPasswordEncoder();
  }

  /**
   * 提供 BCrypt 密码编码器，用于 OAuth2 客户端密钥编码。
   *
   * @return BCrypt 密码编码器
   */
  @Bean
  public PasswordEncoder clientPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * 提供认证中心登录用户查询服务。
   *
   * <p>配置认证中心数据源时使用平台 `sys_user` 表校验密码，确保后台用户管理重置密码后统一认证登录立即生效；
   * 未配置数据源时回退到配置注入的本地开发账号。</p>
   *
   * @param passwordEncoder 密码编码器
   * @param authUsername 认证中心登录用户名
   * @param authPassword 认证中心登录密码
   * @param platformTenantId 核心平台租户业务编码
   * @param datasourceUrl 认证中心数据源地址
   * @param datasourceUsername 认证中心数据源用户名
   * @param datasourcePassword 认证中心数据源密码
   * @param datasourceDriverClassName 认证中心数据源驱动类名
   * @return 内存用户服务
   */
  @Bean
  public UserDetailsService userDetailsService(
      PasswordEncoder passwordEncoder,
      @Value("${zhyc.auth.user-name}") String authUsername,
      @Value("${zhyc.auth.user-password}") String authPassword,
      @Value("${zhyc.auth.platform-tenant-id}") String platformTenantId,
      @Value("${zhyc.auth.datasource.url:}") String datasourceUrl,
      @Value("${zhyc.auth.datasource.username:}") String datasourceUsername,
      @Value("${zhyc.auth.datasource.password:}") String datasourcePassword,
      @Value("${zhyc.auth.datasource.driver-class-name:}") String datasourceDriverClassName) {
    JdbcOperations jdbcOperations = createJdbcOperations(
        datasourceUrl, datasourceUsername, datasourcePassword, datasourceDriverClassName);
    if (jdbcOperations != null) {
      return new JdbcPlatformUserDetailsService(jdbcOperations, platformTenantId);
    }
    UserDetails authUser = User.withUsername(requireText(authUsername, "认证中心登录用户名不能为空"))
        .password(passwordEncoder.encode(requireText(authPassword, "认证中心登录密码不能为空")))
        .roles("AUTH_USER")
        .build();
    return new InMemoryUserDetailsManager(authUser);
  }

  /**
   * 提供表单登录认证处理器。
   *
   * <p>显式绑定用户查询服务和 Shiro 密码编码器，避免存在 OAuth2 客户端 BCrypt 编码器时，
   * Spring Security 自动推断出错误的密码校验器。</p>
   *
   * @param userDetailsService 认证中心用户查询服务
   * @param passwordEncoder 平台用户密码编码器
   * @return 认证处理器
   */
  @Bean
  public AuthenticationProvider authenticationProvider(
      UserDetailsService userDetailsService,
      @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  /**
   * 提供认证中心登录审计服务。
   *
   * <p>认证中心通过平台映射用户信息写入 `sys_login_log`，保持与后台登录日志页面的数据口径一致。</p>
   *
   * @param platformTenantId 核心平台租户业务编码
   * @param platformUserId 核心平台本地用户主键
   * @param platformUsername 核心平台登录账号
   * @param datasourceUrl 认证中心数据源地址
   * @param datasourceUsername 认证中心数据源用户名
   * @param datasourcePassword 认证中心数据源密码
   * @param datasourceDriverClassName 认证中心数据源驱动类名
   * @return 登录审计服务
   */
  @Bean
  public AuthLoginAuditService authLoginAuditService(
      @Value("${zhyc.auth.platform-tenant-id}") String platformTenantId,
      @Value("${zhyc.auth.platform-user-id}") String platformUserId,
      @Value("${zhyc.auth.platform-username}") String platformUsername,
      @Value("${zhyc.auth.datasource.url:}") String datasourceUrl,
      @Value("${zhyc.auth.datasource.username:}") String datasourceUsername,
      @Value("${zhyc.auth.datasource.password:}") String datasourcePassword,
      @Value("${zhyc.auth.datasource.driver-class-name:}") String datasourceDriverClassName) {
    JdbcOperations jdbcOperations = createJdbcOperations(
        datasourceUrl, datasourceUsername, datasourcePassword, datasourceDriverClassName);
    return new AuthLoginAuditService(
        jdbcOperations,
        platformTenantId,
        parsePositiveLong(platformUserId, "认证中心平台用户主键必须为正整数"),
        platformUsername);
  }

  /**
   * 提供 RSA JWK Source，用于签发 JWT 令牌。
   *
   * <p>生产环境应通过配置注入持久化 RSA PEM 密钥；未配置时仅为本地开发自动生成运行时密钥。</p>
   *
   * @param privateKeyPem RSA PKCS#8 私钥 PEM
   * @param publicKeyPem RSA X.509 公钥 PEM
   * @param keyId JWK Key ID
   * @return RSA JWK Source
   */
  @Bean
  public JWKSource<SecurityContext> jwkSource(
      @Value("${zhyc.auth.jwk.private-key-pem:}") String privateKeyPem,
      @Value("${zhyc.auth.jwk.public-key-pem:}") String publicKeyPem,
      @Value("${zhyc.auth.jwk.key-id:}") String keyId) {
    RSAKey rsaKey = loadConfiguredRsa(privateKeyPem, publicKeyPem, keyId);
    JWKSet jwkSet = new JWKSet(rsaKey);
    return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
  }

  /**
   * 提供 JWT 解码器。
   *
   * <p>OIDC 能力启用后，授权服务器需要基于同一套 JWK Source 校验和解析令牌，
   * 保证发现端点、UserInfo 和 ID Token 能按 Spring Authorization Server 标准工作。</p>
   *
   * @param jwkSource RSA JWK Source
   * @return JWT 解码器
   */
  @Bean
  public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return NimbusJwtDecoder.withJwkSource(jwkSource).build();
  }

  /**
   * 提供 JWT 编码器。
   *
   * <p>移动端首期采用一方账号密码登录接口，认证通过后仍由认证中心使用同一套 JWK 签发访问令牌，
   * 避免移动端持有 OAuth2 客户端密钥。</p>
   *
   * @param jwkSource RSA JWK Source
   * @return JWT 编码器
   */
  @Bean
  public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
    return new NimbusJwtEncoder(jwkSource);
  }

  /**
   * 定义核心平台 JWT Claims 定制器。
   *
   * <p>认证中心签发访问令牌时写入核心平台 Shiro 主体映射所需 Claims，核心平台只依赖标准 JWT
   * 和这些稳定字段，不依赖认证中心内部用户实现。</p>
   *
   * @param platformTenantId 核心平台租户业务编码
   * @param platformUserId 核心平台本地用户主键
   * @param platformUsername 核心平台登录账号
   * @return JWT Claims 定制器
   */
  @Bean
  public OAuth2TokenCustomizer<JwtEncodingContext> platformJwtTokenCustomizer(
      @Value("${zhyc.auth.platform-tenant-id}") String platformTenantId,
      @Value("${zhyc.auth.platform-user-id}") String platformUserId,
      @Value("${zhyc.auth.platform-username}") String platformUsername) {
    String tenantId = requireText(platformTenantId, "认证中心平台租户编码不能为空");
    long userId = parsePositiveLong(platformUserId, "认证中心平台用户主键必须为正整数");
    String username = requireText(platformUsername, "认证中心平台登录账号不能为空");
    return context -> {
      if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
        return;
      }
      context.getClaims()
          .claim("tenant_id", tenantId)
          .claim("user_id", userId)
          .claim("preferred_username", username);
    };
  }

  /**
   * 配置授权服务器基础设置。
   *
   * @param issuer 令牌签发方地址
   * @return 授权服务器设置
   */
  @Bean
  public AuthorizationServerSettings authorizationServerSettings(
      @Value("${zhyc.auth.issuer:}") String issuer) {
    AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder();
    String normalizedIssuer = trimToNull(issuer);
    if (normalizedIssuer != null) {
      builder.issuer(normalizedIssuer);
    }
    return builder.build();
  }

  /**
   * 构建首期启动客户端。
   *
   * @param passwordEncoder 密码编码器
   * @param clientId OAuth2 客户端标识
   * @param clientSecret OAuth2 客户端密钥
   * @param redirectUri OAuth2 授权码回调地址
   * @param tokenSettings 令牌有效期策略
   * @return OAuth2 注册客户端
   */
  private static RegisteredClient buildBootstrapClient(
      PasswordEncoder passwordEncoder,
      String clientId,
      String clientSecret,
      String redirectUri,
      TokenSettings tokenSettings) {
    return RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId(requireText(clientId, "认证中心客户端标识不能为空"))
        .clientSecret(passwordEncoder.encode(requireText(clientSecret, "认证中心客户端密钥不能为空")))
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
        .redirectUri(requireText(redirectUri, "认证中心客户端回调地址不能为空"))
        .scope(OidcScopes.OPENID)
        .scope(OidcScopes.PROFILE)
        .tokenSettings(tokenSettings)
        .build();
  }

  /**
   * 构建认证中心启动客户端令牌有效期策略。
   *
   * <p>访问令牌默认 30 分钟，刷新令牌默认 8 小时；刷新令牌采用轮换方式，降低浏览器本地令牌泄露后的持续风险。</p>
   *
   * @param accessTokenTimeToLiveMinutes 访问令牌有效期分钟数
   * @param refreshTokenTimeToLiveHours 刷新令牌有效期小时数
   * @return 认证中心令牌策略
   */
  private static TokenSettings buildTokenSettings(
      long accessTokenTimeToLiveMinutes, long refreshTokenTimeToLiveHours) {
    return TokenSettings.builder()
        .accessTokenTimeToLive(Duration.ofMinutes(requirePositive(
            accessTokenTimeToLiveMinutes, "访问令牌有效期分钟数必须大于 0")))
        .refreshTokenTimeToLive(Duration.ofHours(requirePositive(
            refreshTokenTimeToLiveHours, "刷新令牌有效期小时数必须大于 0")))
        .reuseRefreshTokens(false)
        .build();
  }

  /**
   * 判断已注册客户端是否与当前启动配置一致。
   *
   * @param existingClient 数据库中已有的注册客户端
   * @param passwordEncoder 密码编码器
   * @param clientSecret 当前配置中的客户端密钥明文
   * @param redirectUri 当前配置中的授权回调地址
   * @param tokenSettings 当前配置中的令牌有效期策略
   * @return 已有客户端可继续使用时返回 true
   */
  private static boolean isBootstrapClientCurrent(
      RegisteredClient existingClient,
      PasswordEncoder passwordEncoder,
      String clientSecret,
      String redirectUri,
      TokenSettings tokenSettings) {
    return isCurrentClientSecretFormat(existingClient.getClientSecret())
        && matchesClientSecret(passwordEncoder, clientSecret, existingClient.getClientSecret())
        && existingClient.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        && existingClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.AUTHORIZATION_CODE)
        && existingClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)
        && existingClient.getRedirectUris().contains(requireText(redirectUri, "认证中心客户端回调地址不能为空"))
        && existingClient.getScopes().contains(OidcScopes.OPENID)
        && existingClient.getScopes().contains(OidcScopes.PROFILE)
        && tokenSettings.getAccessTokenTimeToLive()
            .equals(existingClient.getTokenSettings().getAccessTokenTimeToLive())
        && tokenSettings.getRefreshTokenTimeToLive()
            .equals(existingClient.getTokenSettings().getRefreshTokenTimeToLive())
        && tokenSettings.isReuseRefreshTokens() == existingClient.getTokenSettings().isReuseRefreshTokens();
  }

  /**
   * 校验明文客户端密钥是否匹配数据库中的 BCrypt 密文。
   *
   * @param passwordEncoder 密码编码器
   * @param rawSecret 当前配置中的客户端密钥明文
   * @param encodedSecret 数据库中的客户端密钥密文
   * @return 密钥匹配时返回 true
   */
  private static boolean matchesClientSecret(PasswordEncoder passwordEncoder, String rawSecret, String encodedSecret) {
    String normalizedRawSecret = requireText(rawSecret, "认证中心客户端密钥不能为空");
    String normalizedEncodedSecret = trimToNull(encodedSecret);
    if (normalizedEncodedSecret == null) {
      return false;
    }
    if (normalizedEncodedSecret.startsWith("{bcrypt}")) {
      normalizedEncodedSecret = normalizedEncodedSecret.substring("{bcrypt}".length());
    }
    return passwordEncoder.matches(normalizedRawSecret, normalizedEncodedSecret);
  }

  /**
   * 生成客户端密钥指纹，便于排查多服务配置是否一致。
   *
   * <p>仅输出 SHA-256 前 12 位十六进制摘要，禁止在日志中输出客户端密钥明文。</p>
   *
   * @param secret 客户端密钥明文
   * @return 客户端密钥指纹
   */
  private static String secretFingerprint(String secret) {
    String normalizedSecret = requireText(secret, "认证中心客户端密钥不能为空");
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(normalizedSecret.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest).substring(0, 12);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("当前 JDK 不支持 SHA-256 摘要算法", ex);
    }
  }

  /**
   * 判断客户端密钥密文是否符合当前客户端 BCrypt 密码编码器存储格式。
   *
   * @param encodedSecret 数据库中的客户端密钥密文
   * @return 当前格式可用时返回 true
   */
  private static boolean isCurrentClientSecretFormat(String encodedSecret) {
    String normalizedEncodedSecret = trimToNull(encodedSecret);
    return normalizedEncodedSecret != null && !normalizedEncodedSecret.startsWith("{bcrypt}");
  }

  /**
   * 创建认证中心 JDBC 操作对象。
   *
   * @param datasourceUrl 认证中心数据源地址
   * @param datasourceUsername 认证中心数据源用户名
   * @param datasourcePassword 认证中心数据源密码
   * @param datasourceDriverClassName 认证中心数据源驱动类名
   * @return JDBC 操作对象；未配置数据源地址时返回空
   */
  private static JdbcOperations createJdbcOperations(
      String datasourceUrl,
      String datasourceUsername,
      String datasourcePassword,
      String datasourceDriverClassName) {
    String normalizedUrl = trimToNull(datasourceUrl);
    if (normalizedUrl == null) {
      return null;
    }
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl(normalizedUrl);
    dataSource.setUsername(requireText(datasourceUsername, "认证中心数据源用户名不能为空"));
    dataSource.setPassword(requireText(datasourcePassword, "认证中心数据源密码不能为空"));
    String normalizedDriverClassName = trimToNull(datasourceDriverClassName);
    if (normalizedDriverClassName != null) {
      dataSource.setDriverClassName(normalizedDriverClassName);
    }
    return new JdbcTemplate(dataSource);
  }

  /**
   * 生成 RSA JWK，供授权服务器令牌签名使用。
   *
   * @return RSA JWK
   */
  private static RSAKey generateRsa() {
    KeyPair keyPair = generateRsaKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    return new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyID(UUID.randomUUID().toString())
        .build();
  }

  /**
   * 生成 2048 位 RSA 密钥对。
   *
   * @return RSA 密钥对
   */
  private static KeyPair generateRsaKeyPair() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      return keyPairGenerator.generateKeyPair();
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("无法生成认证中心 RSA 密钥对", ex);
    }
  }

  /**
   * 加载配置中的 RSA 密钥，未配置时生成本地开发用运行时密钥。
   *
   * @param privateKeyPem RSA PKCS#8 私钥 PEM
   * @param publicKeyPem RSA X.509 公钥 PEM
   * @param keyId JWK Key ID
   * @return RSA JWK
   */
  private static RSAKey loadConfiguredRsa(String privateKeyPem, String publicKeyPem, String keyId) {
    String normalizedPrivateKey = trimToNull(privateKeyPem);
    String normalizedPublicKey = trimToNull(publicKeyPem);
    if (normalizedPrivateKey == null && normalizedPublicKey == null) {
      return generateRsa();
    }
    if (normalizedPrivateKey == null || normalizedPublicKey == null) {
      throw new IllegalStateException("认证中心 RSA 私钥和公钥必须同时配置");
    }
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
          new PKCS8EncodedKeySpec(decodePem(normalizedPrivateKey, "PRIVATE KEY")));
      RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
          new X509EncodedKeySpec(decodePem(normalizedPublicKey, "PUBLIC KEY")));
      return new RSAKey.Builder(publicKey)
          .privateKey(privateKey)
          .keyID(resolveKeyId(keyId))
          .build();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new IllegalStateException("无法加载认证中心 RSA 密钥配置", ex);
    }
  }

  /**
   * 解析 PEM 文本中的 Base64 DER 内容。
   *
   * @param pem PEM 文本
   * @param keyType 密钥类型
   * @return DER 字节
   */
  private static byte[] decodePem(String pem, String keyType) {
    String normalizedPem = pem.replace("\\n", "\n")
        .replace("-----BEGIN " + keyType + "-----", "")
        .replace("-----END " + keyType + "-----", "")
        .replaceAll("\\s", "");
    return Base64.getDecoder().decode(normalizedPem);
  }

  /**
   * 解析 JWK Key ID，未配置时生成随机值。
   *
   * @param keyId 配置的 Key ID
   * @return JWK Key ID
   */
  private static String resolveKeyId(String keyId) {
    String normalizedKeyId = trimToNull(keyId);
    return normalizedKeyId == null ? UUID.randomUUID().toString() : normalizedKeyId;
  }

  /**
   * 校验配置项必须包含非空文本。
   *
   * @param value 配置值
   * @param message 配置为空时的错误消息
   * @return 去除首尾空白后的配置值
   */
  private static String requireText(String value, String message) {
    String normalizedValue = trimToNull(value);
    if (normalizedValue == null) {
      throw new IllegalStateException(message);
    }
    return normalizedValue;
  }

  /**
   * 解析正整数配置。
   *
   * @param value 原始配置值
   * @param message 配置无效时的错误消息
   * @return 正整数配置值
   */
  private static long parsePositiveLong(String value, String message) {
    String normalizedValue = requireText(value, message);
    try {
      long parsedValue = Long.parseLong(normalizedValue);
      if (parsedValue > 0) {
        return parsedValue;
      }
      throw new IllegalStateException(message);
    } catch (NumberFormatException ex) {
      throw new IllegalStateException(message, ex);
    }
  }

  /**
   * 校验正整数配置项。
   *
   * @param value 原始配置值
   * @param message 配置无效时的错误消息
   * @return 通过校验的正整数值
   */
  private static long requirePositive(long value, String message) {
    if (value <= 0) {
      throw new IllegalStateException(message);
    }
    return value;
  }

  /**
   * 将空白字符串归一化为空。
   *
   * @param value 原始字符串
   * @return 非空文本或空
   */
  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
