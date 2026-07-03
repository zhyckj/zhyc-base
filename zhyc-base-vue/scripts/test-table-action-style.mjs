/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(rootDir, '..');
const stylePath = resolve(rootDir, 'src/styles/table-actions.css');

assert.ok(existsSync(stylePath), '后台前端必须提供统一表格操作列样式文件。');

const mainTs = readFileSync(resolve(rootDir, 'src/main.ts'), 'utf8');
const tableActionCss = readFileSync(stylePath, 'utf8');
const lowcodeTemplate = readFileSync(
  resolve(workspaceRoot, 'zhyc-base-server/zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/generator/BuiltInCodeTemplateProvider.java'),
  'utf8',
);

assert.match(mainTs, /import\s+['"]\.\/styles\/table-actions\.css['"]/, '后台入口必须导入统一表格操作列样式。');
assert.match(tableActionCss, /\.zhyc-table-actions/, '统一样式必须提供 zhyc-table-actions 操作容器。');
assert.match(tableActionCss, /td:last-child[\s\S]*\.ant-btn/, '统一样式必须兜底覆盖现有表格最后一列按钮。');
assert.match(tableActionCss, /\.ant-btn-link/, '统一样式必须把 link 按钮统一为操作框按钮。');
assert.match(lowcodeTemplate, /class="zhyc-table-actions"/, '低代码后台列表模板必须生成统一操作容器。');
