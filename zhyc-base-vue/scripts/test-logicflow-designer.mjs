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
const packageSource = readFileSync(resolve(rootDir, 'package.json'), 'utf8');
const modelViewSource = readFileSync(resolve(rootDir, 'src/views/workflow/model/index.vue'), 'utf8');

let designerSource = '';
let converterSource = '';
try {
  designerSource = readFileSync(resolve(rootDir, 'src/views/workflow/model/components/LogicFlowDesigner.vue'), 'utf8');
  converterSource = readFileSync(resolve(rootDir, 'src/views/workflow/model/components/logicFlowBpmnConverter.ts'), 'utf8');
} catch {
  designerSource = '';
  converterSource = '';
}

const checks = [
  {
    passed: /"@logicflow\/core"/.test(packageSource),
    message: '缺少 @logicflow/core 依赖。',
  },
  {
    passed: /LogicFlowDesigner/.test(modelViewSource) && !/<BpmnDesigner/.test(modelViewSource),
    message: '流程模型页未切换到 LogicFlowDesigner。',
  },
  {
    passed: /new LogicFlow/.test(designerSource)
      && /@logicflow\/core\/dist\/index\.css/.test(designerSource),
    message: 'LogicFlowDesigner 未初始化 LogicFlow 画布或样式。',
  },
  {
    passed: /convertLogicFlowToBpmnXml/.test(designerSource)
      && /export function convertLogicFlowToBpmnXml/.test(converterSource)
      && /bpmn:definitions/.test(converterSource),
    message: 'LogicFlow 设计器未提供 BPMN XML 转换输出。',
  },
  {
    passed: /审批节点/.test(designerSource)
      && /开始节点/.test(designerSource)
      && /结束节点/.test(designerSource),
    message: 'LogicFlow 设计器缺少流程节点快捷添加入口。',
  },
];

const failures = checks.filter((check) => !check.passed);

if (failures.length > 0) {
  console.error('LogicFlow 流程设计器校验失败：');
  failures.forEach((failure) => console.error(`- ${failure.message}`));
  process.exit(1);
}

console.log('LogicFlow 流程设计器校验通过。');
