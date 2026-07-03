/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));

const requiredSnippets = [
  ['zhyc-platform-app/src/main/java/com/zhyc/platform/monitor/DefaultRuntimeMonitorService.java', 'Map<String, DataSource>'],
  ['zhyc-platform-app/src/main/java/com/zhyc/platform/monitor/DefaultRuntimeMonitorService.java', 'dataSources.entrySet()'],
  ['zhyc-platform-app/src/main/java/com/zhyc/platform/monitor/DefaultRuntimeMonitorService.java', 'buildDataSourceStatus'],
  ['zhyc-platform-app/src/main/java/com/zhyc/platform/monitor/DefaultRuntimeMonitorService.java', 'resolveDataSourceName'],
  ['zhyc-platform-app/src/main/java/com/zhyc/platform/monitor/DefaultRuntimeMonitorService.java', 'responseTimeMs'],
  ['zhyc-platform-app/src/main/java/com/zhyc/platform/monitor/RuntimeServiceStatus.java', 'getResponseTimeMs'],
  ['zhyc-platform-app/src/test/java/com/zhyc/platform/monitor/RuntimeMonitorServiceTest.java', 'shouldReportEveryRegisteredDataSource'],
  ['zhyc-platform-app/src/test/java/com/zhyc/platform/monitor/RuntimeMonitorServiceTest.java', 'shouldReportServiceResponseTime'],
];

const forbiddenSnippets = [
  ['zhyc-platform-app/src/main/java/com/zhyc/platform/monitor/DefaultRuntimeMonitorService.java', '"platform-main"'],
  ['zhyc-platform-app/src/main/java/com/zhyc/platform/monitor/DefaultRuntimeMonitorService.java', '"平台主库"'],
  ['zhyc-platform-app/src/main/java/com/zhyc/platform/monitor/DefaultRuntimeMonitorService.java', 'private final DataSource dataSource;'],
];

const missingSnippets = requiredSnippets.filter(([file, snippet]) => {
  const path = resolve(rootDir, file);
  return !existsSync(path) || !readFileSync(path, 'utf8').includes(snippet);
});

const forbiddenHits = forbiddenSnippets.filter(([file, snippet]) => {
  const path = resolve(rootDir, file);
  return existsSync(path) && readFileSync(path, 'utf8').includes(snippet);
});

if (missingSnippets.length > 0 || forbiddenHits.length > 0) {
  console.error('系统监控首期契约校验失败。');
  for (const [file, snippet] of missingSnippets) {
    console.error(`缺少关键内容: ${file} -> ${snippet}`);
  }
  for (const [file, snippet] of forbiddenHits) {
    console.error(`存在固定监控实现: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

console.log('系统监控首期契约校验通过。');
