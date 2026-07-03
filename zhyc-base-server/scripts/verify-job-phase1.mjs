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
  ['zhyc-base-server/zhyc-module-job/src/main/resources/db/V1__job_core.sql', 'CREATE TABLE IF NOT EXISTS job_task'],
  ['zhyc-base-server/zhyc-module-job/src/main/resources/db/V1__job_core.sql', 'CREATE TABLE IF NOT EXISTS job_task_log'],
  ['zhyc-base-server/zhyc-module-job/src/main/java/com/zhyc/job/task/service/JobTaskHandler.java', 'void handle(JobTaskExecutionContext context)'],
  ['zhyc-base-server/zhyc-module-job/src/main/java/com/zhyc/job/task/service/JobTaskExecutionContext.java', 'operatorId'],
  ['zhyc-base-server/zhyc-module-job/src/main/java/com/zhyc/job/task/service/DefaultJobTaskService.java', 'List<JobTaskHandler> taskHandlers'],
  ['zhyc-base-server/zhyc-module-job/src/main/java/com/zhyc/job/task/service/DefaultJobTaskService.java', 'handler.handle(context)'],
  ['zhyc-base-server/zhyc-module-job/src/main/java/com/zhyc/job/task/service/DefaultJobTaskService.java', 'ZHYC_JOB_TASK_HANDLER_NOT_FOUND'],
  ['zhyc-base-server/zhyc-module-job/src/main/java/com/zhyc/job/task/service/DefaultJobTaskService.java', 'ZHYC_JOB_TASK_HANDLER_FAILED'],
  ['zhyc-base-server/zhyc-module-job/src/main/java/com/zhyc/job/task/controller/JobTaskController.java', '@RequestMapping("/job/tasks")'],
  ['zhyc-base-server/zhyc-module-job/src/main/java/com/zhyc/job/task/controller/JobTaskController.java', '@RequiresPermissions("job:task:trigger")'],
  ['zhyc-base-server/zhyc-module-job/src/test/java/com/zhyc/job/task/JobTaskServiceTest.java', 'shouldWriteFailureLogWhenHandlerFails'],
  ['zhyc-base-server/zhyc-module-job/src/test/java/com/zhyc/job/task/JobTaskServiceTest.java', 'shouldRejectTriggerWhenHandlerMissing'],
  ['zhyc-base-server/zhyc-module-job/src/test/java/com/zhyc/job/task/JobTaskControllerContractTest.java', 'shouldExposeJobTaskRoutesWithShiroPermission'],
  ['zhyc-base-vue/src/api/job/task.ts', '/job/tasks'],
  ['zhyc-base-vue/src/api/job/task.ts', 'triggerJobTask'],
  ['zhyc-base-vue/src/views/job/task/index.vue', 'job:task:trigger'],
];

const errors = [];

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
  console.error('在线作业首期契约校验失败。');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log('在线作业首期契约校验通过。');
