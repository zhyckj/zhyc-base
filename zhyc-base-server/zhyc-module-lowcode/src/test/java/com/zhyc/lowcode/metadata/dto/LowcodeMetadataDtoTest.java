/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.dto;

import com.zhyc.lowcode.metadata.domain.LowcodeDataSource;
import com.zhyc.lowcode.metadata.domain.LowcodeDatabaseDialect;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 低代码元数据 DTO 转换测试。
 */
class LowcodeMetadataDtoTest {

  /**
   * 校验数据源保存请求能转换成领域对象。
   *
   * @throws Exception 反射访问 DTO 失败时抛出
   */
  @Test
  void shouldConvertDataSourceRequestToDomainModel() throws Exception {
    Class<?> requestClass = Class.forName("com.zhyc.lowcode.metadata.dto.LowcodeDataSourceSaveRequest");
    Object request = requestClass.getDeclaredConstructor().newInstance();

    invokeSetter(request, "setTenantId", "tenant-a");
    invokeSetter(request, "setCode", "main");
    invokeSetter(request, "setName", "主数据源");
    invokeSetter(request, "setDialect", "MYSQL");
    invokeSetter(request, "setJdbcUrl", "jdbc:mysql://127.0.0.1:3306/zhyc");
    invokeSetter(request, "setUsername", "root");
    invokeSetter(request, "setPasswordSecretRef", "secret/lowcode/main");
    invokeSetter(request, "setEnabled", true);

    LowcodeDataSource dataSource = (LowcodeDataSource) requestClass.getMethod("toDomain").invoke(request);

    assertEquals("tenant-a", dataSource.getTenantId());
    assertEquals("main", dataSource.getCode());
    assertEquals("MYSQL", dataSource.getDialect().name());
    assertEquals("secret/lowcode/main", dataSource.getPasswordSecretRef());
  }

  /**
   * 校验数据源保存请求使用稳定数据库方言编码，而不是 Java 枚举名。
   *
   * @throws Exception 反射访问 DTO 失败时抛出
   */
  @Test
  void shouldConvertDataSourceRequestByDialectCode() throws Exception {
    Class<?> requestClass = Class.forName("com.zhyc.lowcode.metadata.dto.LowcodeDataSourceSaveRequest");
    Object request = requestClass.getDeclaredConstructor().newInstance();

    invokeSetter(request, "setTenantId", "tenant-a");
    invokeSetter(request, "setCode", "main");
    invokeSetter(request, "setName", "主数据源");
    invokeSetter(request, "setDialect", " mysql ");
    invokeSetter(request, "setJdbcUrl", "jdbc:mysql://127.0.0.1:3306/zhyc");
    invokeSetter(request, "setUsername", "root");
    invokeSetter(request, "setEnabled", true);

    LowcodeDataSource dataSource = (LowcodeDataSource) requestClass.getMethod("toDomain").invoke(request);

    assertEquals(LowcodeDatabaseDialect.MYSQL, dataSource.getDialect());
  }

  /**
   * 校验数据源响应返回稳定数据库方言编码，避免前端依赖 Java 枚举名。
   */
  @Test
  void shouldExposeDialectCodeInDataSourceResponse() {
    LowcodeDataSource dataSource = new LowcodeDataSource(
        1L, "tenant-a", "main", "主数据源", LowcodeDatabaseDialect.MYSQL,
        "jdbc:mysql://127.0.0.1:3306/zhyc", "root", true);

    LowcodeDataSourceResponse response = LowcodeDataSourceResponse.from(dataSource);

    assertEquals("mysql", response.getDialect());
  }

  @Test
  void shouldExposePasswordSecretReferenceForEditingWithoutPlainPassword() throws Exception {
    LowcodeDataSource dataSource = new LowcodeDataSource(
        1L, "tenant-a", "main", "主数据源", LowcodeDatabaseDialect.MYSQL,
        "jdbc:mysql://127.0.0.1:3306/zhyc", "root", "secret:db-main", true);

    LowcodeDataSourceResponse response = LowcodeDataSourceResponse.from(dataSource);
    Class<?> responseClass = response.getClass();

    assertEquals("secret:db-main", responseClass.getMethod("getPasswordSecretRef").invoke(response));
    assertFalse(hasMethod(responseClass, "setPasswordSecretRef", String.class));
    assertFalse(hasMethod(responseClass, "getPassword"));
    assertFalse(hasMethod(responseClass, "getSecretMask"));
  }

  private static void invokeSetter(Object target, String methodName, Object value) throws Exception {
    Class<?> parameterType = value instanceof Boolean ? boolean.class : String.class;
    target.getClass().getMethod(methodName, parameterType).invoke(target, value);
  }

  private static boolean hasMethod(Class<?> targetClass, String methodName, Class<?>... parameterTypes) {
    try {
      Method ignored = targetClass.getMethod(methodName, parameterTypes);
      return true;
    } catch (NoSuchMethodException ignored) {
      return false;
    }
  }
}
