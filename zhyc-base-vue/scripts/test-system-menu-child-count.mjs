/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';

const menuPageSource = readFileSync(new URL('../src/views/system/menu/index.vue', import.meta.url), 'utf8');

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

assertIncludes(
  menuPageSource,
  ':expand-icon="renderMenuExpandIcon"',
  '菜单权限表格必须自定义展开图标。',
);
assertIncludes(
  menuPageSource,
  'column.key === \'name\'',
  '菜单名称列必须自定义渲染。',
);
assertIncludes(
  menuPageSource,
  'getSecondLevelMenuCount(record)',
  '菜单名称列必须展示二级菜单数量。',
);
assertIncludes(
  menuPageSource,
  '<a-badge',
  '二级菜单数量必须使用数字徽标展示。',
);
assertIncludes(
  menuPageSource,
  'menu-child-badge',
  '二级菜单数量必须使用统一徽标样式。',
);
assertIncludes(
  menuPageSource,
  ':show-zero="true"',
  '无二级菜单时必须显示 0 数字徽标。',
);
assertNotIncludes(
  menuPageSource,
  'menu-child-count',
  '二级菜单数量不能使用高权重标签样式。',
);
assertNotIncludes(
  menuPageSource,
  'formatSecondLevelMenuCount',
  '二级菜单数量不能再展示说明文案。',
);
assertNotIncludes(
  menuPageSource,
  '个二级菜单',
  '二级菜单数量只能显示数字。',
);
assertIncludes(
  menuPageSource,
  'menu-expand-empty',
  '无二级菜单时必须渲染减号占位。',
);
assertIncludes(
  menuPageSource,
  'ant-table-row-expand-icon',
  '展开收起图标必须复用 Ant Design Vue 表格默认样式。',
);
assertIncludes(
  menuPageSource,
  'ant-table-row-expand-icon-expanded',
  '无二级菜单图标必须复用默认减号状态。',
);

console.log('system menu child count checks passed');
