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
  ['zhyc-module-search/src/main/java/com/zhyc/search/service/DefaultSearchService.java', 'findEnabledIndexConfig'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/service/DefaultSearchService.java', 'searchItems(buildRuntimeQuery'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/service/DefaultSearchService.java', 'SQL_IDENTIFIER_PATTERN'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/service/DefaultSearchService.java', 'log.getQueryStatus()'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/controller/SearchController.java', '索引配置保存请求不能为空'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/controller/SearchController.java', '索引重建任务创建请求不能为空'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/controller/SearchController.java', '全文检索查询请求不能为空'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/repository/SearchRuntimeQuery.java', 'record SearchRuntimeQuery'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/repository/SearchRepository.java', 'List<String> searchItems'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/mapper/SearchMapper.java', 'selectSearchItems'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/mapper/SearchSqlProvider.java', 'FROM ${sourceTable}'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/mapper/SearchSqlProvider.java', 'tenant_id = #{tenantId}'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/mapper/SearchSqlProvider.java', 'deleted = 0'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/mapper/SearchSqlProvider.java', 'AND (${whereExpression})'],
  ['zhyc-module-search/src/test/java/com/zhyc/search/SearchControllerContractTest.java', 'shouldRejectNullIndexConfigSaveCommand'],
  ['zhyc-module-search/src/test/java/com/zhyc/search/SearchControllerContractTest.java', 'shouldRejectNullRebuildTaskCommand'],
  ['zhyc-module-search/src/test/java/com/zhyc/search/SearchControllerContractTest.java', 'shouldRejectNullSearchQueryCommand'],
  ['zhyc-module-search/src/test/java/com/zhyc/search/SearchServiceTest.java', 'shouldRunDatabaseSearchAndRecordQueryLogWhenSearch'],
  ['zhyc-module-search/src/test/java/com/zhyc/search/SearchServiceTest.java', 'lastRuntimeQuery.tenantId()'],
  ['zhyc-module-search/src/test/java/com/zhyc/search/SearchServiceTest.java', 'shouldRejectUnsafeSearchFieldAndRecordFailedLog'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/service/SearchQueryResponse.java', '命中文本记录'],
];

const forbiddenSnippets = [
  ['zhyc-module-search/src/main/java/com/zhyc/search/service/DefaultSearchService.java', 'return new SearchQueryResponse(normalizedIndexCode, normalizedKeyword, 0, List.of())'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/service/SearchQueryResponse.java', '返回空集合'],
  ['zhyc-module-search/src/main/java/com/zhyc/search/domain/SearchIndexConfig.java', '首期仅保存配置'],
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
  console.error('全文检索首期契约校验失败。');
  for (const [file, snippet] of missingSnippets) {
    console.error(`缺少关键内容: ${file} -> ${snippet}`);
  }
  for (const [file, snippet] of forbiddenHits) {
    console.error(`存在过期内容: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

console.log('全文检索首期契约校验通过。');
