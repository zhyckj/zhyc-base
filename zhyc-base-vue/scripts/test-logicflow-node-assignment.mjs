/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));
const designerSource = readFileSync(
  resolve(rootDir, 'src/views/workflow/model/components/LogicFlowDesigner.vue'),
  'utf8',
);
const converterSource = readFileSync(
  resolve(rootDir, 'src/views/workflow/model/components/logicFlowBpmnConverter.ts'),
  'utf8',
);

const checks = [
  {
    passed: /处理人类型/.test(designerSource)
      && /指定用户/.test(designerSource)
      && /角色/.test(designerSource)
      && /岗位/.test(designerSource),
    message: '节点属性面板缺少处理人、角色或岗位配置入口。',
  },
  {
    passed: /listSystemUsers/.test(designerSource)
      && /listSystemRoles/.test(designerSource)
      && /listSystemPosts/.test(designerSource)
      && /listWorkflowFormBindings/.test(designerSource),
    message: '流程设计器未加载用户、角色、岗位和表单绑定选项。',
  },
  {
    passed: /flowable:assignee/.test(converterSource)
      && /flowable:candidateUsers/.test(converterSource)
      && /flowable:candidateGroups/.test(converterSource)
      && /flowable:formKey/.test(converterSource),
    message: 'LogicFlow 到 BPMN 转换缺少 Flowable 节点选人或表单属性。',
  },
  {
    passed: /assigneeType/.test(converterSource)
      && /candidateUserIds/.test(converterSource)
      && /candidateRoleCodes/.test(converterSource)
      && /candidatePostCodes/.test(converterSource)
      && /formKey/.test(converterSource),
    message: 'BPMN 回显未覆盖节点处理人和表单配置。',
  },
  {
    passed: /审批方式/.test(designerSource)
      && /通过条件/.test(designerSource)
      && /超时小时/.test(designerSource)
      && /抄送用户/.test(designerSource),
    message: '节点属性面板缺少常用审批策略、超时或抄送预留配置。',
  },
];

const failures = checks.filter((check) => !check.passed);

if (failures.length > 0) {
  console.error('LogicFlow 节点审批配置校验失败：');
  failures.forEach((failure) => console.error(`- ${failure.message}`));
  process.exit(1);
}

console.log('LogicFlow 节点审批配置校验通过。');
