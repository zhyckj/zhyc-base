/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));
const tenantPackageVue = readFileSync(resolve(rootDir, 'src/views/system/tenant-package/index.vue'), 'utf8');
const tenantPackageApi = readFileSync(resolve(rootDir, 'src/api/system/tenant-package.ts'), 'utf8');

assert.match(tenantPackageVue, /新增套餐/, '租户套餐页面应展示“新增套餐”入口。');
assert.match(tenantPackageVue, /openCreateForm/, '租户套餐页面应提供打开新增表单的方法。');
assert.match(tenantPackageVue, /submitCreateForm/, '租户套餐页面应提供提交新增表单的方法。');
assert.match(tenantPackageVue, /packageCode/, '新增套餐表单应包含套餐编码字段。');
assert.match(tenantPackageVue, /packageName/, '新增套餐表单应包含套餐名称字段。');
assert.match(tenantPackageVue, /maxUserCount/, '新增套餐表单应包含最大用户数字段。');
assert.match(tenantPackageVue, /maxStorageMb/, '新增套餐表单应包含存储容量字段。');
assert.match(tenantPackageVue, /system:tenant-package:update/, '新增套餐入口应绑定套餐维护权限。');

assert.match(tenantPackageApi, /SystemTenantPackageCreatePayload/, '租户套餐 API 应声明新增套餐入参类型。');
assert.match(tenantPackageApi, /createSystemTenantPackage/, '租户套餐 API 应提供新增套餐方法。');
assert.match(tenantPackageApi, /method:\s*'POST'/, '新增套餐 API 应使用 POST 方法。');
assert.match(tenantPackageApi, /\/system\/tenant-packages/, '新增套餐 API 应调用租户套餐资源路径。');
