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

const requiredFiles = [
  'zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/service/SysTenantService.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/mapper/SysTenantMapper.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackage/controller/SysTenantPackageController.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackage/service/SysTenantPackageService.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackage/mapper/SysTenantPackageMapper.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/controller/SysTenantPackageModuleController.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/service/SysTenantPackageModuleService.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/mapper/SysTenantPackageModuleMapper.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantparam/controller/SysTenantParamController.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantparam/service/SysTenantParamService.java',
  'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantparam/mapper/SysTenantParamMapper.java',
  'zhyc-base-vue/src/api/system/tenant.ts',
  'zhyc-base-vue/src/api/system/tenant-package.ts',
  'zhyc-base-vue/src/api/system/tenant-package-module.ts',
  'zhyc-base-vue/src/api/system/tenant-param.ts',
  'zhyc-base-vue/src/views/system/tenant/index.vue',
  'zhyc-base-vue/src/views/system/tenant-package/index.vue',
  'zhyc-base-vue/src/views/system/tenant-package-module/index.vue',
  'zhyc-base-vue/src/views/system/tenant-param/index.vue',
  'zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/config/MyBatisConfig.java',
  'zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantSqlRewriter.java',
  'zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantMyBatisInterceptor.java',
  'zhyc-base-server/zhyc-platform-app/src/test/java/com/zhyc/platform/tenant/TenantSqlRewriterTest.java',
];

const requiredSnippets = [
  ['zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql', 'CREATE TABLE IF NOT EXISTS sys_tenant'],
  ['zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql', 'CREATE TABLE IF NOT EXISTS sys_tenant_package'],
  ['zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql', 'CREATE TABLE IF NOT EXISTS sys_tenant_package_module'],
  ['zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql', 'CREATE TABLE IF NOT EXISTS sys_tenant_param'],
  ['zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql', "tenant_id VARCHAR(64) NOT NULL COMMENT '租户业务编码'"],
  ['zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql', 'isolation_mode VARCHAR(32) NOT NULL DEFAULT'],
  ['zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql', 'UNIQUE KEY uk_sys_tenant_tenant_id'],
  ['zhyc-base-server/zhyc-module-system/src/main/resources/db/V1__system_core.sql', 'UNIQUE KEY uk_sys_tenant_param_key'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', '@RequestMapping("/system/tenants")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackage/controller/SysTenantPackageController.java', '@RequestMapping("/system/tenant-packages")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/controller/SysTenantPackageModuleController.java', '@RequestMapping("/system/tenant-package-modules")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantparam/controller/SysTenantParamController.java', '@RequestMapping("/system/tenant-params")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', 'ERROR_CREATE_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', 'ZHYC_SYSTEM_TENANT_CREATE_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', 'ERROR_STATUS_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', 'ZHYC_SYSTEM_TENANT_STATUS_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', 'new BusinessException(ERROR_CREATE_REQUEST_REQUIRED, "租户创建请求不能为空")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', 'new BusinessException(ERROR_STATUS_REQUEST_REQUIRED, "租户状态不能为空")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackage/controller/SysTenantPackageController.java', 'ERROR_STATUS_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackage/controller/SysTenantPackageController.java', 'ZHYC_SYSTEM_TENANT_PACKAGE_STATUS_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackage/controller/SysTenantPackageController.java', 'new BusinessException(ERROR_STATUS_REQUEST_REQUIRED, "套餐状态不能为空")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/controller/SysTenantPackageModuleController.java', 'ERROR_BIND_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/controller/SysTenantPackageModuleController.java', 'ZHYC_SYSTEM_TENANT_PACKAGE_MODULE_BIND_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/controller/SysTenantPackageModuleController.java', 'new BusinessException(ERROR_BIND_REQUEST_REQUIRED, "套餐授权绑定请求不能为空")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantparam/controller/SysTenantParamController.java', 'ERROR_SAVE_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantparam/controller/SysTenantParamController.java', 'ZHYC_SYSTEM_TENANT_PARAM_SAVE_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantparam/controller/SysTenantParamController.java', 'new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "租户参数保存请求不能为空")'],
  ['zhyc-base-server/zhyc-module-system/src/test/java/com/zhyc/system/tenant/SysTenantControllerContractTest.java', 'ZHYC_SYSTEM_TENANT_CREATE_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/test/java/com/zhyc/system/tenant/SysTenantControllerContractTest.java', 'ZHYC_SYSTEM_TENANT_STATUS_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/test/java/com/zhyc/system/tenantpackage/SysTenantPackageControllerContractTest.java', 'ZHYC_SYSTEM_TENANT_PACKAGE_STATUS_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/test/java/com/zhyc/system/tenantpackagemodule/SysTenantPackageModuleControllerContractTest.java', 'ZHYC_SYSTEM_TENANT_PACKAGE_MODULE_BIND_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/test/java/com/zhyc/system/tenantparam/SysTenantParamControllerContractTest.java', 'ZHYC_SYSTEM_TENANT_PARAM_SAVE_REQUEST_REQUIRED'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/service/SysTenantService.java', 'listTenants'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/service/SysTenantService.java', 'createTenant'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/service/SysTenantService.java', 'changeStatus'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackage/service/SysTenantPackageService.java', 'listPackages'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/service/SysTenantPackageModuleService.java', 'bindGrants'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/service/DefaultSysTenantPackageModuleService.java', 'SysModuleRepository'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/service/DefaultSysTenantPackageModuleService.java', 'validateGrantResource'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/service/DefaultSysTenantPackageModuleService.java', '套餐授权菜单不属于模块'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/service/DefaultSysTenantPackageModuleService.java', '套餐授权权限不属于模块'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/service/DefaultSysTenantPackageModuleService.java', 'validateDuplicateGrantResources'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/service/DefaultSysTenantPackageModuleService.java', '套餐授权资源重复'],
  ['zhyc-base-server/zhyc-module-system/src/test/java/com/zhyc/system/tenantpackagemodule/SysTenantPackageModuleServiceTest.java', 'shouldRejectGrantWhenMenuIsNotDeclaredByModule'],
  ['zhyc-base-server/zhyc-module-system/src/test/java/com/zhyc/system/tenantpackagemodule/SysTenantPackageModuleServiceTest.java', 'shouldRejectGrantWhenPermissionIsNotDeclaredByModule'],
  ['zhyc-base-server/zhyc-module-system/src/test/java/com/zhyc/system/tenantpackagemodule/SysTenantPackageModuleServiceTest.java', 'shouldRejectDuplicateGrantBeforeDeletingOldGrants'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantparam/service/SysTenantParamService.java', 'findByKey'],
  ['zhyc-base-vue/src/api/system/tenant.ts', '/system/tenants'],
  ['zhyc-base-vue/src/api/system/tenant.ts', 'changeSystemTenantStatus'],
  ['zhyc-base-vue/src/api/system/tenant-package.ts', '/system/tenant-packages'],
  ['zhyc-base-vue/src/api/system/tenant-package-module.ts', '/system/tenant-package-modules'],
  ['zhyc-base-vue/src/api/system/tenant-param.ts', '/system/tenant-params'],
  ['zhyc-base-vue/src/views/system/tenant/index.vue', 'Modal.confirm'],
  ['zhyc-base-vue/src/views/system/tenant/index.vue', "message.success('租户状态已更新')"],
  ['zhyc-base-vue/src/views/system/tenant/index.vue', "message.error(error instanceof Error ? error.message : '租户保存失败')"],
  ['zhyc-base-vue/src/views/system/tenant-package/index.vue', 'Modal.confirm'],
  ['zhyc-base-vue/src/views/system/tenant-package/index.vue', "message.success('租户套餐状态已更新')"],
  ['zhyc-base-vue/src/views/system/tenant-package-module/index.vue', "from '@/api/system/module'"],
  ['zhyc-base-vue/src/views/system/tenant-package-module/index.vue', 'listSystemModules'],
  ['zhyc-base-vue/src/views/system/tenant-package-module/index.vue', 'moduleOptions'],
  ['zhyc-base-vue/src/views/system/tenant-package-module/index.vue', 'menuOptions'],
  ['zhyc-base-vue/src/views/system/tenant-package-module/index.vue', 'permissionOptions'],
  ['zhyc-base-vue/src/views/system/tenant-package-module/index.vue', 'findDuplicateGrant'],
  ['zhyc-base-vue/src/views/system/tenant-package-module/index.vue', "message.success('套餐授权已保存')"],
  ['zhyc-base-vue/src/views/system/tenant-param/index.vue', "message.success('租户参数已保存')"],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/config/MyBatisConfig.java', 'tenantMyBatisInterceptor'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/config/MyBatisConfig.java', 'TenantSqlRewriter.firstReleaseTenantTables()'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantMyBatisInterceptor.java', '@Intercepts'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantMyBatisInterceptor.java', 'StatementHandler.class'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantMyBatisInterceptor.java', 'TenantContext.getTenantId()'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantMyBatisInterceptor.java', 'metaObject.setValue("sql", rewrittenSql)'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantSqlRewriter.java', 'firstReleaseTenantTables'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantSqlRewriter.java', '"sys_user"'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantSqlRewriter.java', '"openapi_app"'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantSqlRewriter.java', '"wf_definition"'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantSqlRewriter.java', 'tenant_id = \''],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantSqlRewriter.java', 'containsTenantCondition'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/tenant/TenantSqlRewriter.java', 'escapeSqlLiteral'],
  ['zhyc-base-server/zhyc-platform-app/src/test/java/com/zhyc/platform/tenant/TenantSqlRewriterTest.java', 'shouldAppendTenantConditionToSelectWithWhere'],
  ['zhyc-base-server/zhyc-platform-app/src/test/java/com/zhyc/platform/tenant/TenantSqlRewriterTest.java', 'shouldAppendTenantConditionToUpdate'],
  ['zhyc-base-server/zhyc-platform-app/src/test/java/com/zhyc/platform/tenant/TenantSqlRewriterTest.java', 'shouldAppendTenantConditionToDelete'],
  ['zhyc-base-server/zhyc-platform-app/src/test/java/com/zhyc/platform/tenant/TenantSqlRewriterTest.java', 'shouldEscapeTenantLiteral'],
];

const forbiddenSnippets = [
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', 'throw new IllegalArgumentException("租户创建请求不能为空")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenant/controller/SysTenantController.java', 'throw new IllegalArgumentException("租户状态不能为空")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackage/controller/SysTenantPackageController.java', 'throw new IllegalArgumentException("套餐状态不能为空")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantpackagemodule/controller/SysTenantPackageModuleController.java', 'throw new IllegalArgumentException("套餐授权绑定请求不能为空")'],
  ['zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system/tenantparam/controller/SysTenantParamController.java', 'throw new IllegalArgumentException("租户参数保存请求不能为空")'],
];

const errors = [];

for (const file of requiredFiles) {
  const absolutePath = resolve(workspaceRoot, file);
  if (!existsSync(absolutePath)) {
    errors.push(`缺少文件：${file}`);
  }
}

for (const [file, snippet] of requiredSnippets) {
  const absolutePath = resolve(workspaceRoot, file);
  if (!existsSync(absolutePath)) {
    continue;
  }
  const content = readFileSync(absolutePath, 'utf8');
  if (!content.includes(snippet)) {
    errors.push(`缺少关键内容：${file} -> ${snippet}`);
  }
}

for (const [file, snippet] of forbiddenSnippets) {
  const absolutePath = resolve(workspaceRoot, file);
  if (!existsSync(absolutePath)) {
    continue;
  }
  const content = readFileSync(absolutePath, 'utf8');
  if (content.includes(snippet)) {
    errors.push(`存在过期内容：${file} -> ${snippet}`);
  }
}

if (errors.length > 0) {
  console.error('SaaS 租户首期契约校验失败。');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log('SaaS 租户首期契约校验通过。');
