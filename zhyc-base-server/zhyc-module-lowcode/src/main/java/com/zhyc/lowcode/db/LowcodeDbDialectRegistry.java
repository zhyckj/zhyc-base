/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 数据库方言能力注册中心。
 *
 * <p>统一维护字段类型映射器、DDL 生成器和分页方言三类扩展能力，按数据库方言编码进行路由，
 * 并在系统启动阶段做重复注册和空实例检查，避免运行期出现静默配置冲突。</p>
 */
public class LowcodeDbDialectRegistry {

  /** 已注册的字段类型映射器。 */
  private final Map<String, FieldTypeMapper> fieldTypeMappers;
  /** 已注册的 DDL 生成器。 */
  private final Map<String, DdlGenerator> ddlGenerators;
  /** 已注册的分页方言实现。 */
  private final Map<String, PaginationDialect> paginationDialects;

  /**
   * 创建低代码数据库方言注册中心。
   *
   * @param ddlGenerators DDL 生成器列表
   * @param fieldTypeMappers 字段类型映射器列表
   * @param paginationDialects 分页方言列表
   */
  public LowcodeDbDialectRegistry(List<DdlGenerator> ddlGenerators,
                                  List<FieldTypeMapper> fieldTypeMappers,
                                  List<PaginationDialect> paginationDialects) {
    this.ddlGenerators = buildDdlGenerators(ddlGenerators);
    this.fieldTypeMappers = buildFieldTypeMappers(fieldTypeMappers);
    this.paginationDialects = buildPaginationDialects(paginationDialects);
  }

  /**
   * 按数据库方言编码获取 DDL 生成器。
   *
   * <p>返回前会做数据库方言归一化（trim + 小写）并校验是否支持。</p>
   *
   * @param dialectCode 数据库方言编码
   * @return DDL 生成器
   */
  public DdlGenerator getDdlGenerator(String dialectCode) {
    return getRegistered("DDL 生成器", ddlGenerators, normalizeDialectCode(dialectCode));
  }

  /**
   * 按数据库方言编码获取字段类型映射器。
   *
   * <p>返回前会做数据库方言归一化（trim + 小写）并校验是否支持。</p>
   *
   * @param dialectCode 数据库方言编码
   * @return 字段类型映射器
   */
  public FieldTypeMapper getFieldTypeMapper(String dialectCode) {
    return getRegistered("字段类型映射器", fieldTypeMappers, normalizeDialectCode(dialectCode));
  }

  /**
   * 按数据库方言编码获取分页方言实现。
   *
   * <p>返回前会做数据库方言归一化（trim + 小写）并校验是否支持。</p>
   *
   * @param dialectCode 数据库方言编码
   * @return 分页方言实现
   */
  public PaginationDialect getPaginationDialect(String dialectCode) {
    return getRegistered("分页方言", paginationDialects, normalizeDialectCode(dialectCode));
  }

  /**
   * 返回已注册 DDL 生成器支持的数据库方言编码清单。
   *
   * @return DDL 方言编码清单
   */
  public List<String> listDdlDialectCodes() {
    return List.copyOf(ddlGenerators.keySet());
  }

  /**
   * 返回已注册字段类型映射器支持的数据库方言编码清单。
   *
   * @return 字段类型映射方言编码清单
   */
  public List<String> listFieldTypeDialectCodes() {
    return List.copyOf(fieldTypeMappers.keySet());
  }

  /**
   * 返回已注册分页方言支持的数据库方言编码清单。
   *
   * @return 分页方言编码清单
   */
  public List<String> listPaginationDialectCodes() {
    return List.copyOf(paginationDialects.keySet());
  }

  /**
   * 从指定注册表中获取数据库方言能力实例。
   *
   * @param resourceType 能力类型名称
   * @param registry 方言能力注册表
   * @param code 已归一化的数据库方言编码
   * @param <T> 方言能力类型
   * @return 方言能力实例
   */
  private static <T> T getRegistered(String resourceType, Map<String, T> registry, String code) {
    T registered = registry.get(code);
    if (registered == null) {
      throw new IllegalArgumentException(resourceType + "不支持该数据库方言: " + code);
    }
    return registered;
  }

  private static Map<String, DdlGenerator> buildDdlGenerators(List<DdlGenerator> generators) {
    Map<String, DdlGenerator> registry = new LinkedHashMap<>();
    List<DdlGenerator> candidates = generators == null ? List.of() : new ArrayList<>(generators);
    for (DdlGenerator generator : candidates) {
      if (generator == null) {
        continue;
      }
      String dialectCode = normalizeDialectCode(generator.getDialectName());
      ensureUniqueDialect("DDL 生成器", dialectCode, registry.containsKey(dialectCode));
      registry.put(dialectCode, generator);
    }
    return registry;
  }

  private static Map<String, FieldTypeMapper> buildFieldTypeMappers(List<FieldTypeMapper> mappers) {
    Map<String, FieldTypeMapper> registry = new LinkedHashMap<>();
    List<FieldTypeMapper> candidates = mappers == null ? List.of() : new ArrayList<>(mappers);
    for (FieldTypeMapper mapper : candidates) {
      if (mapper == null) {
        continue;
      }
      String dialectCode = normalizeDialectCode(mapper.getDialectName());
      ensureUniqueDialect("字段类型映射器", dialectCode, registry.containsKey(dialectCode));
      registry.put(dialectCode, mapper);
    }
    return registry;
  }

  private static Map<String, PaginationDialect> buildPaginationDialects(List<PaginationDialect> dialects) {
    Map<String, PaginationDialect> registry = new LinkedHashMap<>();
    List<PaginationDialect> candidates = dialects == null ? List.of() : new ArrayList<>(dialects);
    for (PaginationDialect dialect : candidates) {
      if (dialect == null) {
        continue;
      }
      String dialectCode = normalizeDialectCode(dialect.getDialectName());
      ensureUniqueDialect("分页方言", dialectCode, registry.containsKey(dialectCode));
      registry.put(dialectCode, dialect);
    }
    return registry;
  }

  private static void ensureUniqueDialect(String resourceType, String dialectCode, boolean duplicated) {
    if (duplicated) {
      throw new IllegalArgumentException(resourceType + "重复注册数据库方言: " + dialectCode);
    }
  }

  private static String normalizeDialectCode(String dialectCode) {
    Objects.requireNonNull(dialectCode, "数据库方言编码不能为空");
    String normalized = dialectCode.trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("数据库方言编码不能为空");
    }
    return normalized;
  }
}
