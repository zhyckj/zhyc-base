/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';

const pageSource = readFileSync(new URL('../src/views/system/tenant-package-module/index.vue', import.meta.url), 'utf8');

function assertIncludes(source, expected, message) {
  if (!source.includes(expected)) {
    throw new Error(message);
  }
}

function assertNotIncludes(source, unexpected, message) {
  if (source.includes(unexpected)) {
    throw new Error(message);
  }
}

assertIncludes(pageSource, 'grant-overview', '套餐授权页面必须展示当前套餐授权概览。');
assertIncludes(pageSource, "column.key === 'moduleCode'", '模块编码列必须自定义紧凑展示。');
assertIncludes(pageSource, "column.key === 'resource'", '菜单编码和权限标识必须合并为授权资源列。');
assertIncludes(pageSource, '模块全部', '模块级授权必须显示明确的授权范围。');
assertIncludes(pageSource, 'formatDateTime(record.createdAt)', '创建时间必须格式化展示。');
assertIncludes(pageSource, 'authorizedModuleCount', '套餐授权页面必须展示已授权模块数量。');
assertNotIncludes(
  pageSource,
  "{ title: '租户套餐'",
  '已选套餐不能在列表中重复展示为表格列。',
);

console.log('tenant package module display checks passed');
