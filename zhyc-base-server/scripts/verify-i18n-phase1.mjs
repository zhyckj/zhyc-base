/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');

const requiredSnippets = [
  ['zhyc-module-i18n/src/main/resources/db/V1__i18n_core.sql', 'CREATE TABLE IF NOT EXISTS i18n_message'],
  ['zhyc-module-i18n/src/main/resources/db/V1__i18n_core.sql', 'tenant_id VARCHAR(64) NOT NULL'],
  ['zhyc-module-i18n/src/main/java/com/zhyc/i18n/controller/I18nController.java', '@RequestMapping("/i18n")'],
  ['zhyc-module-i18n/src/main/java/com/zhyc/i18n/controller/I18nController.java', '@RequiresPermissions("i18n:message:resolve")'],
  ['zhyc-module-i18n/src/main/java/com/zhyc/i18n/controller/I18nController.java', '@PostMapping("/messages/resolve")'],
  ['zhyc-module-i18n/src/main/java/com/zhyc/i18n/service/I18nResolveCommand.java', 'Map<String, String> defaults'],
  ['zhyc-module-i18n/src/main/java/com/zhyc/i18n/service/I18nResolveResponse.java', 'Map<String, String> messages'],
  ['zhyc-module-i18n/src/main/java/com/zhyc/i18n/service/DefaultI18nService.java', 'resolveMessages(I18nResolveCommand command)'],
  ['zhyc-module-i18n/src/main/java/com/zhyc/i18n/service/DefaultI18nService.java', 'findEnabledMessage(normalizedTenantId, normalizedLocale, messageKey)'],
  ['zhyc-module-i18n/src/test/java/com/zhyc/i18n/I18nServiceTest.java', 'shouldResolveMessagesInBatchWithDefaultFallback'],
  ['zhyc-module-i18n/src/test/java/com/zhyc/i18n/I18nControllerContractTest.java', 'shouldRejectNullResolveCommand'],
  ['zhyc-base-vue/src/api/i18n/message.ts', 'resolveI18nMessages'],
  ['zhyc-base-vue/src/api/i18n/message.ts', '/i18n/messages/resolve'],
];

const missingSnippets = requiredSnippets.filter(([file, snippet]) => {
  const absolutePath = file.startsWith('zhyc-base-vue/')
    ? resolve(workspaceRoot, file)
    : resolve(serverRoot, file);
  return !existsSync(absolutePath) || !readFileSync(absolutePath, 'utf8').includes(snippet);
});

if (missingSnippets.length > 0) {
  console.error('国际化首期契约校验失败。');
  for (const [file, snippet] of missingSnippets) {
    console.error(`缺少关键内容: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

console.log('国际化首期契约校验通过。');
