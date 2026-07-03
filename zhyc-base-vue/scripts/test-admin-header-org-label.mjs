/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import assert from 'node:assert/strict';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));
const appVue = readFileSync(resolve(rootDir, 'src/App.vue'), 'utf8');

assert.match(
  appVue,
  /const\s+currentOrgText\s*=\s*computed\(\(\)\s*=>\s*adminContext\.orgId\?\.toString\(\)\s*\|\|\s*'全部组织'\)/,
  '顶部组织上下文未选中时应展示“全部组织”。',
);

assert.doesNotMatch(
  appVue,
  /组织\s*\{\{\s*currentOrgText\s*\}\}[\s\S]*未选择组织|未选择组织[\s\S]*组织\s*\{\{\s*currentOrgText\s*\}\}/,
  '顶部组织上下文不应继续暴露“未选择组织”。',
);
