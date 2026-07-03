/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const pageModelView = readFileSync(resolve(root, 'src/views/lowcode/page/index.vue'), 'utf8');
const lowcodeModelApi = readFileSync(resolve(root, 'src/api/lowcode/model.ts'), 'utf8');

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

assertIncludes(
  lowcodeModelApi,
  'dictCode?: string',
  '低代码字段模型类型必须包含字典编码，供页面模型配置绑定系统字典。',
);
assertIncludes(
  pageModelView,
  'listSystemDictTypes',
  '页面模型配置页必须加载系统字典类型，供字段绑定字典项。',
);
assertIncludes(
  pageModelView,
  "column.key === 'listVisible'",
  '页面模型配置页必须支持在线配置列表展示字段。',
);
assertIncludes(
  pageModelView,
  "column.key === 'formVisible'",
  '页面模型配置页必须支持在线配置表单展示字段。',
);
assertIncludes(
  pageModelView,
  "column.key === 'queryable'",
  '页面模型配置页必须支持在线配置查询字段。',
);
assertIncludes(
  pageModelView,
  "column.key === 'dictCode'",
  '页面模型配置页必须支持字段绑定字典编码。',
);
assertIncludes(
  pageModelView,
  'saveTableModel',
  '页面模型保存时必须同时保存字段展示、查询和字典配置。',
);
assertIncludes(
  pageModelView,
  '字段配置已保存',
  '页面模型保存成功提示必须明确字段配置已同步保存。',
);

console.log('页面模型字段配置门禁通过。');
