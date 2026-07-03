/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 众汇云创平台应用启动入口。
 */
@SpringBootApplication(
    scanBasePackages = "com.zhyc",
    excludeName = {
        "org.apache.shiro.spring.config.web.autoconfigure.ShiroWebAutoConfiguration",
        "org.apache.shiro.spring.config.web.autoconfigure.ShiroWebFilterConfiguration",
        "org.apache.shiro.spring.config.web.autoconfigure.ShiroWebMvcAutoConfiguration"
    })
public class ZhycPlatformApplication {

  /**
   * 启动平台 Spring Boot 应用。
   *
   * @param args 命令行启动参数
   */
  public static void main(String[] args) {
    SpringApplication.run(ZhycPlatformApplication.class, args);
  }
}
