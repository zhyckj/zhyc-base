/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const view = readFileSync(resolve(root, 'src/views/lowcode/datasource/index.vue'), 'utf8');
const api = readFileSync(resolve(root, 'src/api/lowcode/datasource.ts'), 'utf8');

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

assertIncludes(
  api,
  'passwordSecretRef?: string;',
  '低代码数据源响应类型必须包含可选 passwordSecretRef，用于编辑态回显密钥引用。',
);
assertIncludes(
  api,
  'getLowcodeDataSource',
  '低代码数据源 API 必须提供详情查询，用于编辑态回显完整密钥引用。',
);
assertIncludes(
  api,
  '/lowcode/metadata/data-sources/${encodeURIComponent(code)}',
  '低代码数据源详情查询必须按数据源编码访问详情接口。',
);
assertIncludes(
  view,
  "command.passwordSecretRef = dataSource.passwordSecretRef ?? '';",
  '编辑数据源时必须把后端返回的密钥引用回填到表单。',
);
assertIncludes(
  view,
  'getLowcodeDataSource',
  '编辑数据源时必须查询详情后回填，避免列表数据缺失密钥引用。',
);
assertIncludes(
  view,
  'handleSelectDataSource(record)',
  '点击数据源行必须走详情加载流程，不能直接使用列表行回填。',
);
assertIncludes(
  view,
  'const detail = await getLowcodeDataSource(record.tenantId, record.code);',
  '数据源详情加载必须按租户和编码获取完整配置。',
);
assertIncludes(
  view,
  '`${normalizedRef}（当前引用）`',
  '当前密钥引用不在候选项中时，下拉必须保留当前引用显示。',
);
assertIncludes(
  view,
  'passwordSecretOptions.value = []',
  '密钥候选项加载失败时允许清空候选项，但不得清空表单当前引用。',
);

console.log('数据源编辑密钥引用回显门禁通过。');
