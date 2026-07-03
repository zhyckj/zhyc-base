/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.cache;

/**
 * 平台统一缓存名称。
 *
 * <p>缓存名称用于 Spring Cache、Redis Key 前缀和后续监控统计，业务模块不得散落硬编码缓存名。</p>
 */
public final class ZhycCacheNames {

  /** 当前用户权限缓存。 */
  public static final String SYS_USER_PERMISSIONS = "sys:user-permissions";
  /** 系统菜单树缓存。 */
  public static final String SYS_MENU_TREE = "sys:menu-tree";
  /** 系统字典类型缓存。 */
  public static final String SYS_DICT_TYPES = "sys:dict-types";
  /** 系统字典项缓存。 */
  public static final String SYS_DICT_ITEMS = "sys:dict-items";
  /** 系统参数缓存。 */
  public static final String SYS_PARAMS = "sys:params";
  /** 租户参数缓存。 */
  public static final String SYS_TENANT_PARAMS = "sys:tenant-params";
  /** AI 供应商配置缓存。 */
  public static final String AI_PROVIDERS = "ai:providers";
  /** AI 模型配置缓存。 */
  public static final String AI_MODELS = "ai:models";
  /** AI 应用配置缓存。 */
  public static final String AI_APPS = "ai:apps";
  /** AI 提示词模板缓存。 */
  public static final String AI_PROMPTS = "ai:prompts";

  private ZhycCacheNames() {
  }
}
