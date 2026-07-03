/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Open API 网关服务启动入口。
 *
 * <p>当前模块提供 API Key 签名、防重放、OAuth2/OIDC Token 校验、接口授权、限流策略、
 * 调用审计和真实网关路由转发能力，是开放 API 运行时的独立服务入口。</p>
 */
@SpringBootApplication
public class ZhycOpenApiGatewayApplication {

  /**
   * 启动 Open API 网关 Spring Boot 应用，并提供默认应用名和端口。
   *
   * @param args 命令行启动参数
   */
  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(ZhycOpenApiGatewayApplication.class);
    application.setDefaultProperties(Map.of(
        "spring.application.name", "zhyc-openapi-gateway",
        "server.port", "8070"));
    application.run(args);
  }
}
