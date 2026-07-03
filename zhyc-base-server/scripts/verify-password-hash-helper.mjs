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
const errors = [];

const requiredSnippets = [
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/security/PlatformPasswordHashCli.java', 'DefaultPasswordService'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/security/PlatformPasswordHashCli.java', 'ZHYC_LOCAL_ADMIN_PASSWORD'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/security/PlatformPasswordHashCli.java', 'encryptPassword'],
  ['zhyc-base-server/zhyc-platform-app/src/test/java/com/zhyc/platform/security/PlatformPasswordHashCliTest.java', 'passwordsMatch'],
  ['zhyc-base-server/zhyc-platform-app/pom.xml', 'exec-maven-plugin'],
  ['docs/release/phase1-local-runbook.md', 'ZHYC_LOCAL_ADMIN_PASSWORD'],
  ['docs/release/phase1-local-runbook.md', 'PlatformPasswordHashCli'],
];

for (const [file, snippet] of requiredSnippets) {
  const absolutePath = resolve(workspaceRoot, file);
  if (!existsSync(absolutePath)) {
    errors.push(`缺少文件：${file}`);
    continue;
  }
  const content = readFileSync(absolutePath, 'utf8');
  if (!content.includes(snippet)) {
    errors.push(`缺少关键内容：${file} -> ${snippet}`);
  }
}

if (errors.length > 0) {
  console.error('Shiro 密码哈希生成工具门禁失败。');
  errors.forEach((error) => console.error(`- ${error}`));
  process.exit(1);
}

console.log('Shiro 密码哈希生成工具门禁通过。');
