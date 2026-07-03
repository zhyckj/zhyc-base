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
    passed: /class="tool-card/.test(designerSource) && /tool-title/.test(designerSource),
    message: '节点工具箱未升级为卡片式入口。',
  },
  {
    passed: /property-tabs/.test(designerSource)
      && /<a-tab-pane[\s\S]*key="basic"/.test(designerSource)
      && /<a-tab-pane[\s\S]*key="assignee"/.test(designerSource)
      && /<a-tab-pane[\s\S]*key="policy"/.test(designerSource)
      && /property-section/.test(designerSource)
      && /基础信息/.test(designerSource)
      && /审批配置/.test(designerSource)
      && /流程策略/.test(designerSource),
    message: '右侧属性面板缺少页签和分组结构。',
  },
  {
    passed: /grid-template-columns:\s*176px minmax\(0, 1fr\) 320px/.test(designerSource)
      && /overflow-y:\s*auto/.test(designerSource),
    message: '设计器布局未优化为更合理的左右面板宽度和滚动属性面板。',
  },
  {
    passed: /background:\s*#f7f9fc/.test(designerSource)
      && /background:\s*#eef4ff/.test(designerSource),
    message: '画布背景和节点栏视觉层次未优化。',
  },
  {
    passed: /toolbar-group/.test(designerSource)
      && /canvas-meta/.test(designerSource)
      && /property-footer/.test(designerSource),
    message: '工具栏或属性底部操作区仍缺少工作台化布局。',
  },
  {
    passed: /canvas-stage/.test(designerSource)
      && /canvas-hud/.test(designerSource)
      && /canvas-tip/.test(designerSource)
      && /graphStats/.test(designerSource)
      && /zoomPercent/.test(designerSource),
    message: '画布缺少状态条、操作提示或缩放信息。',
  },
  {
    passed: /type:\s*'dot'/.test(designerSource)
      && /radial-gradient/.test(designerSource)
      && /drop-shadow/.test(designerSource)
      && /lf-node-selected/.test(designerSource),
    message: '画布背景、节点质感或选中态未完成美化。',
  },
  {
    passed: /workflow-start/.test(converterSource)
      && /workflow-approval/.test(converterSource)
      && /workflow-condition/.test(converterSource)
      && /width:\s*196/.test(converterSource)
      && /height:\s*64/.test(converterSource),
    message: '默认流程节点未切换为自定义卡片节点或尺寸未统一。',
  },
  {
    passed: /class WorkflowNodeView extends BaseNode/.test(designerSource)
      && /workflow-node-shell/.test(designerSource)
      && /处理人 \/ 表单 \/ 策略/.test(designerSource),
    message: '流程节点仍未升级为自定义 SVG 卡片样式。',
  },
];

const failures = checks.filter((check) => !check.passed);

if (failures.length > 0) {
  console.error('LogicFlow 设计器界面优化校验失败：');
  failures.forEach((failure) => console.error(`- ${failure.message}`));
  process.exit(1);
}

console.log('LogicFlow 设计器界面优化校验通过。');
