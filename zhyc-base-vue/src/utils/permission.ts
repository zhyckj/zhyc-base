/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import type { Directive, DirectiveBinding } from 'vue';
import { computed, ref } from 'vue';

import { listCurrentUserPermissions } from '@/api/system/permission';
import { getAdminRuntimeContext } from '@/utils/adminContext';

/** 后台权限缓存键。 */
const PERMISSION_CACHE_KEY = 'ZHYC_ADMIN_PERMISSION_CODES';

/** 当前已加载的后台权限编码集合。 */
const permissionCodes = ref<Set<string>>(readCachedPermissionCodes());

/** 权限版本号，用于让模板中的函数式权限判断具备响应式依赖。 */
const permissionVersion = ref(0);

/**
 * 刷新当前后台账号的权限编码。
 *
 * <p>通过后端当前用户权限接口加载菜单和按钮权限，避免把菜单维护树误用为登录用户授权来源。</p>
 */
export async function refreshAdminPermissions(): Promise<void> {
  const context = getAdminRuntimeContext();
  if (!context.accessToken || context.userId === null || !context.tenantId) {
    replacePermissionCodes(new Set());
    return;
  }
  const permissions = await listCurrentUserPermissions();
  replacePermissionCodes(new Set(permissions.map((permission) => permission.trim()).filter(Boolean)));
}

/**
 * 判断当前账号是否拥有指定权限。
 *
 * @param permission 权限编码；为空时返回 true，便于可选按钮复用
 * @returns 拥有权限时返回 true
 */
export function hasPermission(permission?: string): boolean {
  if (!permission) {
    return true;
  }
  return permissionCodes.value.has('*')
    || permissionCodes.value.has(permission)
    || hasWildcardPermission(permission);
}

/**
 * 创建页面内权限判断工具。
 *
 * @returns 可在模板和脚本中使用的权限判断函数
 */
export function usePermission(): { hasPermission: (permission?: string) => boolean } {
  return {
    hasPermission: (permission?: string) => {
      permissionVersion.value;
      return hasPermission(permission);
    },
  };
}

/**
 * 后台按钮权限指令。
 *
 * <p>没有权限时直接移除节点展示，避免用户误触无权操作；后端仍必须通过 Shiro 做最终校验。</p>
 */
export const permissionDirective: Directive<HTMLElement, string | string[]> = {
  mounted(element, binding) {
    applyPermissionVisibility(element, binding);
  },
  updated(element, binding) {
    applyPermissionVisibility(element, binding);
  },
};

/**
 * 按权限判断结果控制元素展示。
 *
 * @param element 被绑定的 DOM 元素
 * @param binding 权限指令参数
 */
function applyPermissionVisibility(element: HTMLElement, binding: DirectiveBinding<string | string[]>): void {
  const permissions = Array.isArray(binding.value) ? binding.value : [binding.value];
  const visible = permissions.some((permission) => hasPermission(permission));
  element.style.display = visible ? '' : 'none';
}

/**
 * 替换当前权限集合并写入浏览器缓存。
 *
 * @param nextPermissionCodes 新权限编码集合
 */
function replacePermissionCodes(nextPermissionCodes: Set<string>): void {
  permissionCodes.value = nextPermissionCodes;
  permissionVersion.value += 1;
  window.localStorage.setItem(PERMISSION_CACHE_KEY, JSON.stringify(Array.from(nextPermissionCodes)));
}

/**
 * 从菜单树递归收集权限编码。
 *
 * @param nodes 菜单树节点
 * @returns 权限编码集合
 */
function hasWildcardPermission(permission: string): boolean {
  const requiredSegments = permission.split(':');
  for (const grantedPermission of permissionCodes.value) {
    const grantedSegments = grantedPermission.split(':');
    if (!grantedSegments.includes('*') || grantedSegments.length > requiredSegments.length) {
      continue;
    }
    const matched = grantedSegments.every((segment, index) => segment === '*' || segment === requiredSegments[index]);
    if (matched) {
      return true;
    }
  }
  return false;
}

/**
 * 读取本地缓存权限编码。
 *
 * @returns 本地缓存权限集合，格式错误时返回空集合
 */
function readCachedPermissionCodes(): Set<string> {
  try {
    const rawValue = window.localStorage.getItem(PERMISSION_CACHE_KEY);
    const parsedValue = rawValue ? JSON.parse(rawValue) : [];
    return Array.isArray(parsedValue)
      ? new Set(parsedValue.filter((item): item is string => typeof item === 'string' && item.length > 0))
      : new Set();
  } catch {
    return new Set();
  }
}
