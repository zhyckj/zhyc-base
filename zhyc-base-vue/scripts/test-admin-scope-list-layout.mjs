/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const adminScopeView = readFileSync(resolve(root, 'src/views/system/admin-scope/index.vue'), 'utf8');

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

function assertNotIncludes(file, unexpected, message) {
  if (file.includes(unexpected)) {
    throw new Error(message);
  }
}

assertIncludes(
  adminScopeView,
  'scope-list-panel',
  '管理员范围页必须改为列表式配置面板，避免左右分栏维护范围。',
);
assertIncludes(
  adminScopeView,
  'user-list-panel',
  '管理员范围页必须先展示当前租户下的用户列表。',
);
assertIncludes(
  adminScopeView,
  'userColumns',
  '管理员范围页必须声明租户用户列表列。',
);
assertIncludes(
  adminScopeView,
  'configureUserScope',
  '管理员范围页必须从用户列表行进入范围配置。',
);
assertIncludes(
  adminScopeView,
  'scope-edit-table',
  '管理员范围页必须使用列表表格承载范围编辑行。',
);
assertIncludes(
  adminScopeView,
  'editableColumns',
  '管理员范围页必须声明列表式编辑列，覆盖类型、范围、展示名和操作。',
);
assertIncludes(
  adminScopeView,
  "column.key === 'scopeRefCode'",
  '管理员范围列表必须支持行内编辑范围编码或组织选择。',
);
assertIncludes(
  adminScopeView,
  "column.key === 'action'",
  '管理员范围列表必须提供行内操作列。',
);
assertNotIncludes(
  adminScopeView,
  'binding-layout',
  '管理员范围页不应继续使用旧的左右分栏布局。',
);
assertNotIncludes(
  adminScopeView,
  'editor-section',
  '管理员范围页不应继续保留右侧编辑面板。',
);
assertNotIncludes(
  adminScopeView,
  'placeholder="请选择要维护范围的管理员"',
  '管理员范围页不应继续通过管理员下拉框作为配置入口。',
);

console.log('管理员范围列表式布局门禁通过。');
