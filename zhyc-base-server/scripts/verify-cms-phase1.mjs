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
  ['zhyc-module-cms/src/main/resources/db/V1__cms_core.sql', 'CREATE TABLE IF NOT EXISTS cms_channel'],
  ['zhyc-module-cms/src/main/resources/db/V1__cms_core.sql', 'CREATE TABLE IF NOT EXISTS cms_content'],
  ['zhyc-module-cms/src/main/resources/db/V1__cms_core.sql', 'tenant_id VARCHAR(64) NOT NULL'],
  ['zhyc-module-cms/src/main/java/com/zhyc/cms/controller/CmsController.java', '@RequestMapping("/cms")'],
  ['zhyc-module-cms/src/main/java/com/zhyc/cms/controller/CmsController.java', '@RequiresPermissions("cms:channel:query")'],
  ['zhyc-module-cms/src/main/java/com/zhyc/cms/controller/CmsController.java', '@RequiresPermissions("cms:content:publish")'],
  ['zhyc-module-cms/src/main/java/com/zhyc/cms/service/CmsContentSaveCommand.java', 'Long id'],
  ['zhyc-module-cms/src/main/java/com/zhyc/cms/mapper/CmsSqlProvider.java', 'public String updateContent()'],
  ['zhyc-module-cms/src/main/java/com/zhyc/cms/mapper/CmsSqlProvider.java', 'WHERE tenant_id = #{tenantId}'],
  ['zhyc-module-cms/src/main/java/com/zhyc/cms/mapper/CmsSqlProvider.java', 'AND id = #{id}'],
  ['zhyc-module-cms/src/main/java/com/zhyc/cms/mapper/CmsSqlProvider.java', 'AND deleted = 0'],
  ['zhyc-module-cms/src/main/java/com/zhyc/cms/repository/MyBatisCmsRepository.java', 'cmsMapper.updateContent(content)'],
  ['zhyc-module-cms/src/test/java/com/zhyc/cms/CmsServiceTest.java', 'shouldKeepContentIdWhenSavingExistingContent'],
  ['zhyc-module-cms/src/test/java/com/zhyc/cms/CmsSqlProviderTest.java', 'shouldBuildTenantScopedContentUpdateSql'],
  ['zhyc-base-vue/src/api/cms/content.ts', 'id?: number'],
  ['zhyc-base-vue/src/views/cms/content/index.vue', 'formState.id = content.id'],
  ['zhyc-base-vue/src/views/cms/content/index.vue', 'message.success(\'内容文章已发布\')'],
];

const missingSnippets = requiredSnippets.filter(([file, snippet]) => {
  const absolutePath = file.startsWith('zhyc-base-vue/')
    ? resolve(workspaceRoot, file)
    : resolve(serverRoot, file);
  return !existsSync(absolutePath) || !readFileSync(absolutePath, 'utf8').includes(snippet);
});

if (missingSnippets.length > 0) {
  console.error('内容管理首期契约校验失败。');
  for (const [file, snippet] of missingSnippets) {
    console.error(`缺少关键内容: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

console.log('内容管理首期契约校验通过。');
