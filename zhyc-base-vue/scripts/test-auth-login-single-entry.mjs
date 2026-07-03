/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const loginView = readFileSync(resolve(root, 'src/views/auth/login.vue'), 'utf8');

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

function assertMatches(file, pattern, message) {
  if (!pattern.test(file)) {
    throw new Error(message);
  }
}

function assertNotIncludes(file, unexpected, message) {
  if (file.includes(unexpected)) {
    throw new Error(message);
  }
}

assertIncludes(
  loginView,
  '<h1>统一认证登录</h1>',
  '登录页标题必须统一为“统一认证登录”，避免用户误以为有两个登录页。',
);
assertMatches(
  loginView,
  /<a-form\s+[^>]*v-if="authRequestReady"/s,
  '账号密码表单必须只在授权请求准备好后展示，避免未准备状态显示禁用表单。',
);
assertIncludes(
  loginView,
  '<div v-else class="auth-handoff">',
  '授权请求未准备好时必须展示单一登录入口说明，而不是禁用账号密码表单。',
);
assertIncludes(
  loginView,
  '授权请求准备完成后填写账号密码，仍由同一个统一认证中心校验身份。',
  '登录页必须说明这是同一个统一认证中心，而不是两个登录页面。',
);
assertIncludes(
  loginView,
  "username: '',",
  '登录页账号默认值必须为空，避免未登录页面暴露默认测试账号。',
);
assertIncludes(
  loginView,
  "password: '',",
  '登录页密码默认值必须为空，禁止在源码或页面中暴露默认密码。',
);
assertNotIncludes(
  loginView,
  'DEFAULT_LOGIN_USERNAME',
  '登录页不能再保留默认账号常量。',
);
assertNotIncludes(
  loginView,
  'DEFAULT_LOGIN_PASSWORD',
  '登录页不能再保留默认密码常量。',
);
assertNotIncludes(loginView, 'local_auth_user_password', '登录页不能硬编码本地认证密码。');
assertNotIncludes(
  loginView,
  '进入登录流程',
  '登录页不能再出现“进入登录流程”，避免和“登录平台”形成两个入口。',
);
assertNotIncludes(
  loginView,
  '账号密码登录',
  '登录页标题和主文案不能再使用“账号密码登录”作为独立入口名称。',
);
assertNotIncludes(
  loginView,
  'default-value=',
  '受控输入框不能再使用 default-value，否则默认账号密码不会回显。',
);

console.log('登录页单入口体验门禁通过。');
