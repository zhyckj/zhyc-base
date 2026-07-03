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
const workspaceRoot = resolve(rootDir, '..');

const requiredSnippets = [
  ['zhyc-module-visual/src/main/resources/db/V1__visual_core.sql', 'CREATE TABLE IF NOT EXISTS visual_dataset'],
  ['zhyc-module-visual/src/main/resources/db/V1__visual_core.sql', 'tenant_id VARCHAR(64) NOT NULL'],
  ['zhyc-module-visual/src/main/java/com/zhyc/visual/controller/VisualController.java', '@RequestMapping("/visual")'],
  ['zhyc-module-visual/src/main/java/com/zhyc/visual/controller/VisualController.java', '@RequiresPermissions("visual:dataset:save")'],
  ['zhyc-module-visual/src/main/java/com/zhyc/visual/controller/VisualController.java', '@RequiresPermissions("visual:report:save")'],
  ['zhyc-module-visual/src/main/java/com/zhyc/visual/controller/VisualController.java', '@RequiresPermissions("visual:screen:publish")'],
  ['zhyc-module-visual/src/main/java/com/zhyc/visual/service/DefaultVisualService.java', 'requireReadOnlySql(command.sqlText())'],
  ['zhyc-module-visual/src/main/java/com/zhyc/visual/service/DefaultVisualService.java', 'ZHYC_VISUAL_DATASET_SQL_UNSAFE'],
  ['zhyc-module-visual/src/main/java/com/zhyc/visual/service/DefaultVisualService.java', 'UNSAFE_SQL_KEYWORDS'],
  ['zhyc-module-visual/src/test/java/com/zhyc/visual/VisualServiceTest.java', 'shouldRejectUnsafeDatasetSql'],
  ['zhyc-module-visual/src/test/java/com/zhyc/visual/VisualSqlProviderTest.java', 'shouldBuildDatasetQueryWithTenantStatusAndDeletedFilter'],
  ['zhyc-base-vue/src/api/visual/report.ts', '/visual/datasets'],
  ['zhyc-base-vue/src/views/visual/dataset/index.vue', 'visual:dataset:save'],
  ['zhyc-base-vue/src/views/visual/report/index.vue', 'visual:report:save'],
  ['zhyc-base-vue/src/views/visual/screen/index.vue', 'visual:screen:save'],
];

const missingSnippets = requiredSnippets.filter(([file, snippet]) => {
  const path = file.startsWith('zhyc-base-vue/')
    ? resolve(workspaceRoot, file)
    : resolve(rootDir, file);
  return !existsSync(path) || !readFileSync(path, 'utf8').includes(snippet);
});

if (missingSnippets.length > 0) {
  console.error('可视化首期契约校验失败。');
  for (const [file, snippet] of missingSnippets) {
    console.error(`缺少关键内容: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

console.log('可视化首期契约校验通过。');
