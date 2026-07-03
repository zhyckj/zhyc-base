/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 认证中心服务启动入口。
 */
@SpringBootApplication
public class ZhycAuthServerApplication {

  /**
   * 启动认证中心 Spring Boot 应用。
   *
   * @param args 命令行启动参数
   */
  public static void main(String[] args) {
    SpringApplication.run(ZhycAuthServerApplication.class, args);
  }
}
