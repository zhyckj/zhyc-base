/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');

function read(relativePath) {
  return readFileSync(resolve(root, relativePath), 'utf8');
}

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

const app = read('src/App.vue');
const main = read('src/main.ts');

assertIncludes(app, "import zhCN from 'ant-design-vue/es/locale/zh_CN'", 'App.vue 必须引入 Ant Design Vue 中文语言包');
assertIncludes(app, ':locale="zhCN"', '全局 a-config-provider 必须配置中文 locale');
assertIncludes(main, "import dayjs from 'dayjs'", 'main.ts 必须引入 dayjs 以设置日期控件语言环境');
assertIncludes(main, "import 'dayjs/locale/zh-cn'", 'main.ts 必须加载 dayjs 中文语言包');
assertIncludes(main, "dayjs.locale('zh-cn')", 'main.ts 必须设置 dayjs 中文 locale');

console.log('Ant Design Vue 日期中文化门禁通过。');
