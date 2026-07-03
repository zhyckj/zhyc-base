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
  ['zhyc-base-server/zhyc-module-message/src/main/resources/db/V1__message_core.sql', 'CREATE TABLE IF NOT EXISTS msg_template'],
  ['zhyc-base-server/zhyc-module-message/src/main/resources/db/V1__message_core.sql', 'CREATE TABLE IF NOT EXISTS msg_message'],
  ['zhyc-base-server/zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/MsgMessageTemplateSendCommand.java', 'Map<String, String> variables'],
  ['zhyc-base-server/zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/MsgMessageService.java', 'sendByTemplate'],
  ['zhyc-base-server/zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/DefaultMsgMessageService.java', 'sendByTemplate'],
  ['zhyc-base-server/zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/DefaultMsgMessageService.java', 'renderTemplate'],
  ['zhyc-base-server/zhyc-module-message/src/main/java/com/zhyc/message/inbox/service/DefaultMsgMessageService.java', 'workflow'],
  ['zhyc-base-server/zhyc-module-message/src/main/java/com/zhyc/message/inbox/controller/MsgMessageController.java', '@PostMapping("/template")'],
  ['zhyc-base-server/zhyc-module-message/src/main/java/com/zhyc/message/inbox/controller/MsgMessageController.java', '@RequiresPermissions("message:inbox:send")'],
  ['zhyc-base-server/zhyc-module-message/src/main/java/com/zhyc/message/template/repository/MsgTemplateRepository.java', 'findEnabledByTenantIdAndTemplateCode'],
  ['zhyc-base-server/zhyc-module-message/src/main/java/com/zhyc/message/template/mapper/MsgTemplateSqlProvider.java', "AND status = 'enabled'"],
  ['zhyc-base-server/zhyc-module-message/src/test/java/com/zhyc/message/inbox/MsgMessageServiceTest.java', 'shouldSendMessageByEnabledTemplate'],
  ['zhyc-base-server/zhyc-module-message/src/test/java/com/zhyc/message/inbox/MsgMessageServiceTest.java', 'shouldRejectDisabledTemplateWhenSendingByTemplate'],
  ['zhyc-base-server/zhyc-module-message/src/test/java/com/zhyc/message/inbox/MsgMessageServiceTest.java', 'shouldSendWorkflowMessageType'],
  ['zhyc-base-server/zhyc-module-message/src/test/java/com/zhyc/message/inbox/MsgMessageControllerContractTest.java', 'shouldRejectNullTemplateSendCommand'],
  ['zhyc-base-vue/src/api/message/inbox.ts', 'sendInboxMessageByTemplate'],
  ['zhyc-base-vue/src/api/message/inbox.ts', '/message/inbox/template'],
  ['zhyc-base-uniapp/src/api/message.ts', '/message/inbox'],
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
  console.error('消息首期契约校验失败。');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log('消息首期契约校验通过。');
