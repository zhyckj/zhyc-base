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
  ['zhyc-module-file/src/main/resources/db/V1__file_core.sql', 'CREATE TABLE IF NOT EXISTS file_storage_config'],
  ['zhyc-module-file/src/main/resources/db/V1__file_core.sql', 'CREATE TABLE IF NOT EXISTS file_object'],
  ['zhyc-module-file/src/main/resources/db/V1__file_core.sql', 'CREATE TABLE IF NOT EXISTS file_preview_log'],
  ['zhyc-module-file/src/main/resources/db/V1__file_core.sql', 'tenant_id VARCHAR(64) NOT NULL'],
  ['zhyc-module-file/src/main/java/com/zhyc/file/preview/controller/FilePreviewController.java', '@RequestMapping("/file/preview")'],
  ['zhyc-module-file/src/main/java/com/zhyc/file/preview/controller/FilePreviewController.java', '@RequiresPermissions("file:preview:create")'],
  ['zhyc-module-file/src/main/java/com/zhyc/file/preview/controller/FilePreviewController.java', '@RequiresPermissions("file:preview:view")'],
  ['zhyc-module-file/src/main/java/com/zhyc/file/preview/controller/FilePreviewController.java', '@GetMapping("/render/{fileCode}")'],
  ['zhyc-module-file/src/main/java/com/zhyc/file/preview/service/FilePreviewService.java', 'renderPreview(String tenantId, String fileCode, String previewType)'],
  ['zhyc-module-file/src/main/java/com/zhyc/file/preview/service/FilePreviewRenderResponse.java', 'record FilePreviewRenderResponse'],
  ['zhyc-module-file/src/main/java/com/zhyc/file/preview/service/DefaultFilePreviewService.java', 'saveSuccessLog(normalizedTenantId, normalizedFileCode, normalizedPreviewType, previewUrl)'],
  ['zhyc-module-file/src/test/java/com/zhyc/file/preview/FilePreviewServiceTest.java', 'shouldRenderPreviewAndRecordSuccessLog'],
  ['zhyc-module-file/src/test/java/com/zhyc/file/preview/FilePreviewControllerContractTest.java', 'file:preview:view'],
  ['zhyc-base-vue/src/api/file/object.ts', 'renderFilePreview'],
  ['zhyc-base-vue/src/api/file/object.ts', '/file/preview/render/'],
];

const missingSnippets = requiredSnippets.filter(([file, snippet]) => {
  const absolutePath = file.startsWith('zhyc-base-vue/')
    ? resolve(workspaceRoot, file)
    : resolve(serverRoot, file);
  return !existsSync(absolutePath) || !readFileSync(absolutePath, 'utf8').includes(snippet);
});

if (missingSnippets.length > 0) {
  console.error('文件中心首期契约校验失败。');
  for (const [file, snippet] of missingSnippets) {
    console.error(`缺少关键内容: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

console.log('文件中心首期契约校验通过。');
