/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const runtimeApi = readFileSync(resolve(root, 'src/api/monitor/runtime.ts'), 'utf8');
const serviceView = readFileSync(resolve(root, 'src/views/monitor/service/index.vue'), 'utf8');
const datasourceView = readFileSync(resolve(root, 'src/views/monitor/datasource/index.vue'), 'utf8');
const sqlView = readFileSync(resolve(root, 'src/views/monitor/sql/index.vue'), 'utf8');
const routes = readFileSync(resolve(root, 'src/router/routes.ts'), 'utf8');

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

assertIncludes(
  runtimeApi,
  'responseTimeMs: number',
  '服务监控响应类型必须包含响应耗时 responseTimeMs。',
);
assertIncludes(
  serviceView,
  '平均响应',
  '服务监控页必须展示服务平均响应速度。',
);
assertIncludes(
  serviceView,
  "column.key === 'responseTimeMs'",
  '服务监控表格必须展示每个服务的响应耗时。',
);
assertIncludes(
  serviceView,
  'serviceLatencyColor',
  '服务监控页必须按响应耗时标识健康程度。',
);
assertIncludes(
  datasourceView,
  '连接正常',
  '数据源监控页必须展示连接正常的数据源数量。',
);
assertIncludes(
  datasourceView,
  '慢连接',
  '数据源监控页必须展示慢连接数据源数量。',
);
assertIncludes(
  datasourceView,
  'dataSourceLatencyColor',
  '数据源监控页必须按检测耗时标识健康程度。',
);
assertIncludes(
  datasourceView,
  "column.key === 'quality'",
  '数据源监控表格必须提供连接质量列。',
);
assertIncludes(
  runtimeApi,
  'RuntimeSqlMonitorRecord',
  '运行监控 API 必须声明 SQL 监控响应类型。',
);
assertIncludes(
  runtimeApi,
  '/monitor/runtime/sql',
  '运行监控 API 必须调用 SQL 监控接口。',
);
assertIncludes(
  routes,
  '/monitor/sql',
  '后台路由必须包含 SQL 监控页面。',
);
assertIncludes(
  routes,
  'monitor:sql:query',
  'SQL 监控路由必须绑定查询权限。',
);
assertIncludes(
  sqlView,
  '慢 SQL',
  'SQL 监控页必须展示慢 SQL 统计。',
);
assertIncludes(
  sqlView,
  '展示阈值',
  'SQL 监控页必须用展示阈值承载最近 SQL 和慢 SQL 分级。',
);
assertIncludes(
  sqlView,
  '最近执行的 SQL 摘要',
  'SQL 监控页必须说明优先展示应用内最近执行 SQL。',
);
assertIncludes(
  sqlView,
  'const thresholdMs = ref(1)',
  'SQL 监控页默认阈值必须足够低，避免正常 SQL 被全部过滤。',
);
assertIncludes(
  sqlView,
  '优化建议',
  'SQL 监控页必须展示优化建议。',
);
assertIncludes(
  sqlView,
  'severityColor',
  'SQL 监控页必须按慢 SQL 等级标识风险。',
);

console.log('运行监控看板门禁通过。');
