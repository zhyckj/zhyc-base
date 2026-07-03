/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const dialectApi = readFileSync(resolve(root, 'src/api/lowcode/dialect.ts'), 'utf8');
const datasourceView = readFileSync(resolve(root, 'src/views/lowcode/datasource/index.vue'), 'utf8');
const modelView = readFileSync(resolve(root, 'src/views/lowcode/model/index.vue'), 'utf8');

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

assertIncludes(
  dialectApi,
  "dm: '达梦数据库'",
  '低代码方言展示名称必须包含达梦数据库，避免数据源下拉只显示 dm 编码。',
);
assertIncludes(
  dialectApi,
  "ddlDialectCodes: ['mysql', 'postgresql', 'oracle', 'sqlserver', 'dm']",
  '低代码数据库方言默认能力必须覆盖主流数据库和达梦，避免能力接口异常时下拉只剩 MySQL。',
);
assertIncludes(
  dialectApi,
  "fieldTypeDialectCodes: ['mysql', 'postgresql', 'oracle', 'sqlserver', 'dm']",
  '低代码字段类型映射默认能力必须覆盖主流数据库和达梦。',
);
assertIncludes(
  dialectApi,
  "paginationDialectCodes: ['mysql', 'postgresql', 'oracle', 'sqlserver', 'dm']",
  '低代码分页方言默认能力必须覆盖主流数据库和达梦。',
);
assertIncludes(
  datasourceView,
  "dialect: 'dm'",
  '低代码数据源配置页必须提供达梦数据库 JDBC 模板。',
);
assertIncludes(
  datasourceView,
  '...DEFAULT_LOWCODE_DIALECT_CAPABILITIES.ddlDialectCodes',
  '低代码数据源方言下拉必须合并前端默认主流数据库能力，避免后端仅返回 MySQL 时下拉只剩 MySQL。',
);
assertIncludes(
  datasourceView,
  'jdbc:dm://127.0.0.1:5236/zhyc-base-v1',
  '达梦数据库 JDBC 模板必须使用 dm 协议和 5236 默认端口。',
);
assertIncludes(
  datasourceView,
  "column.key === 'dialect'",
  '低代码数据源列表必须对数据库方言列做格式化展示。',
);
assertIncludes(
  datasourceView,
  'formatLowcodeDialectLabel(record.dialect)',
  '低代码数据源列表必须显示达梦数据库中文名称。',
);
assertIncludes(
  modelView,
  'formatLowcodeDialectLabel(dataSource.dialect)',
  '低代码建模页数据源下拉必须显示达梦数据库中文名称。',
);

console.log('低代码数据源达梦方言门禁通过。');
