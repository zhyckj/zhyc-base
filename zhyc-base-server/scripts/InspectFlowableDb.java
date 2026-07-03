/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 本地诊断 Flowable 表状态的只读脚本。
 */
public final class InspectFlowableDb {

  private InspectFlowableDb() {
  }

  /**
   * 输出当前数据库中的 Flowable 表和属性表内容。
   *
   * @param args 未使用
   * @throws Exception 数据库访问失败时抛出
   */
  public static void main(String[] args) throws Exception {
    String url = requireEnv("ZHYC_PLATFORM_DATASOURCE_URL");
    String username = requireEnv("ZHYC_PLATFORM_DATASOURCE_USERNAME");
    String password = requireEnv("ZHYC_PLATFORM_DATASOURCE_PASSWORD");
    try (Connection connection = DriverManager.getConnection(url, username, password)) {
      System.out.println("== Flowable Tables ==");
      try (PreparedStatement statement = connection.prepareStatement("""
          SELECT TABLE_NAME
          FROM information_schema.tables
          WHERE TABLE_SCHEMA = DATABASE()
            AND UPPER(TABLE_NAME) REGEXP '^(ACT|FLW)_'
          ORDER BY TABLE_NAME
          """);
           ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          System.out.println(resultSet.getString(1));
        }
      }
      System.out.println("== ACT_GE_PROPERTY ==");
      try (PreparedStatement statement = connection.prepareStatement("""
          SELECT NAME_, VALUE_
          FROM ACT_GE_PROPERTY
          ORDER BY NAME_
          """);
           ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          System.out.println(resultSet.getString(1) + "=" + resultSet.getString(2));
        }
      } catch (Exception exception) {
        System.out.println("读取 ACT_GE_PROPERTY 失败: " + exception.getMessage());
      }
    }
  }

  /**
   * 读取必填环境变量，避免本地诊断脚本写死数据库口令。
   *
   * @param name 环境变量名称
   * @return 环境变量值
   */
  private static String requireEnv(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("缺少必填环境变量: " + name);
    }
    return value.trim();
  }
}
