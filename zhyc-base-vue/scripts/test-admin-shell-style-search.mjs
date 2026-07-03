/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';

const appSource = readFileSync(new URL('../src/App.vue', import.meta.url), 'utf8');

function assertContains(source, pattern, message) {
  if (!pattern.test(source)) {
    throw new Error(message);
  }
}

function assertNotContains(source, pattern, message) {
  if (pattern.test(source)) {
    throw new Error(message);
  }
}

assertContains(
  appSource,
  /<a-auto-complete[\s\S]*class="platform-menu-search"/,
  '顶部必须提供菜单搜索输入框',
);
assertContains(
  appSource,
  /handleMenuSearchSelect/,
  '菜单搜索必须支持选择后直接跳转',
);
assertContains(
  appSource,
  /interfaceSettingsOpen/,
  '顶部必须提供系统界面设置入口',
);
assertContains(
  appSource,
  /primaryColor/,
  '系统界面设置必须支持主题颜色',
);
assertContains(
  appSource,
  /menuTheme/,
  '系统界面设置必须支持菜单主题',
);
assertContains(
  appSource,
  /autoCollapse/,
  '系统界面设置必须支持菜单自动收缩',
);
assertContains(
  appSource,
  /enableMenuTabs/,
  '系统界面设置必须支持多菜单标签页开关',
);
assertContains(
  appSource,
  /openedMenuTabs/,
  '后台框架必须维护已打开菜单标签页列表',
);
assertContains(
  appSource,
  /ensureMenuTab/,
  '菜单标签页必须按路由去重打开',
);
assertContains(
  appSource,
  /handlePageTabEdit/,
  '菜单标签页必须支持关闭',
);
assertContains(
  appSource,
  /platform-page-tabs-track/,
  '菜单标签页必须保留轨道容器用于布局。',
);
assertContains(
  appSource,
  /platform-page-tab-title/,
  '菜单标签页标题必须使用独立容器，便于截断和悬停提示。',
);
assertContains(
  appSource,
  /:title="tab\.title"/,
  '菜单标签页标题必须提供 title，长标题悬停时可查看完整名称。',
);
assertContains(
  appSource,
  /\.platform-page-tabs-track \{[\s\S]{0,260}background: transparent/,
  '菜单标签页轨道不能显示外层面板背景。',
);
assertNotContains(
  appSource,
  /\.platform-page-tabs-track \{[\s\S]{0,260}linear-gradient/,
  '菜单标签页轨道不能使用外层渐变面板。',
);
assertContains(
  appSource,
  /border-color: var\(--platform-primary-color\)/,
  '激活菜单标签页必须使用主题色边框。',
);
assertContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab-remove:hover\)/,
  '菜单标签页关闭按钮必须提供清晰 hover 状态。',
);
assertContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab-with-remove\) \{[\s\S]{0,180}display: inline-flex/,
  '可关闭菜单标签必须使用自适应横向布局，避免关闭按钮挤压标题。',
);
assertContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab-with-remove\) \{[\s\S]{0,220}width: fit-content/,
  '可关闭菜单标签必须按内容收缩，避免短标题标签过宽。',
);
assertContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab\) \{[\s\S]{0,180}min-width: 0/,
  '菜单标签页不能保留固定最小宽度，短标题必须按内容收缩。',
);
assertContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab\) \{[\s\S]{0,220}max-width: none/,
  '菜单标签页不能限制最大宽度，必须根据菜单文字长度自适应。',
);
assertNotContains(
  appSource,
  /min-width: 104px/,
  '菜单标签页不能继续使用 104px 最小宽度。',
);
assertNotContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab\) \{[\s\S]{0,220}max-width: 120px/,
  '菜单标签页不能继续使用 120px 最大宽度。',
);
assertContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab-btn\) \{[\s\S]{0,260}flex: 0 0 auto/,
  '菜单标签页标题区域必须按内容宽度展示，不能压缩成省略号。',
);
assertContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab-btn\) \{[\s\S]{0,320}overflow: visible/,
  '菜单标签页标题按钮不能隐藏溢出文字。',
);
assertNotContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab-btn\) \{[\s\S]{0,320}text-overflow: ellipsis/,
  '菜单标签页标题按钮不能使用省略号。',
);
assertContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab-remove\) \{[\s\S]{0,260}position: static/,
  '菜单标签页关闭按钮必须参与布局，不能绝对定位压住标题。',
);
assertContains(
  appSource,
  /\.platform-page-tab-title \{[\s\S]{0,220}max-width: none/,
  '菜单标签页标题不能限制最大宽度，必须完整显示菜单文字。',
);
assertContains(
  appSource,
  /\.platform-page-tab-title \{[\s\S]{0,260}overflow: visible/,
  '菜单标签页标题不能隐藏超出文字。',
);
assertNotContains(
  appSource,
  /\.platform-page-tab-title \{[\s\S]{0,280}text-overflow: ellipsis/,
  '菜单标签页标题不能使用省略号。',
);
assertContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab-remove\) \{[\s\S]{0,320}flex: 0 0 18px/,
  '菜单标签页关闭按钮必须保留稳定点击宽度。',
);
assertNotContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-tab-remove\) \{[\s\S]{0,140}position: absolute/,
  '菜单标签页关闭按钮不能使用绝对定位，否则标题和关闭图标会拥挤重叠。',
);
assertContains(
  appSource,
  /\.platform-page-tabs :deep\(\.ant-tabs-nav-wrap::after\)/,
  '菜单标签页横向溢出时必须提供右侧弱遮罩，提示还可滚动查看更多标签。',
);
assertContains(
  appSource,
  /\.platform-page-tab-title \{[\s\S]{0,220}max-width:/,
  '菜单标签页标题必须限制最大宽度，避免长标题撑开标签。',
);
assertContains(
  appSource,
  /ZHYC_ADMIN_INTERFACE_STORAGE_KEY/,
  '系统界面设置必须本地持久化',
);
assertContains(
  appSource,
  />\s*界面\s*</,
  '顶部设置入口名称必须叫界面，不能叫风格',
);
assertNotContains(
  appSource,
  />\s*运行时\s*</,
  '顶部不能暴露运行时上下文手工入口。',
);
assertNotContains(
  appSource,
  /title="运行时上下文"/,
  '后台框架不能暴露运行时上下文手工编辑弹窗。',
);
assertNotContains(
  appSource,
  /a-input-password[\s\S]{0,120}accessToken/,
  '后台框架不能提供手工填写访问令牌的用户入口。',
);

console.log('admin shell style and search checks passed');
