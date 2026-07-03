/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));

const requiredSnippets = [
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DdlGenerator.java', 'generateCreateTable'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/FieldTypeMapper.java', 'toDatabaseType'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/PaginationDialect.java', 'applyPagination'],
  ['pom.xml', '<dameng.jdbc.version>8.1.3.140</dameng.jdbc.version>'],
  ['zhyc-platform-app/pom.xml', '<artifactId>postgresql</artifactId>'],
  ['zhyc-platform-app/pom.xml', '<artifactId>mssql-jdbc</artifactId>'],
  ['zhyc-platform-app/pom.xml', '<artifactId>ojdbc11</artifactId>'],
  ['zhyc-platform-app/pom.xml', '<artifactId>DmJdbcDriver18</artifactId>'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDbDialectService.java', 'listDdlDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDbDialectService.java', 'listFieldTypeDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDbDialectService.java', 'listPaginationDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'listDdlDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'listFieldTypeDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'listPaginationDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'import com.zhyc.common.exception.BusinessException;'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'ERROR_TABLE_REQUIRED'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'ZHYC_LOWCODE_DIALECT_TABLE_REQUIRED'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'ERROR_COLUMN_REQUIRED'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'ZHYC_LOWCODE_DIALECT_COLUMN_REQUIRED'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'ERROR_SQL_REQUIRED'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'ZHYC_LOWCODE_DIALECT_SQL_REQUIRED'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'ZHYC_LOWCODE_DIALECT_CODE_REQUIRED'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'ZHYC_LOWCODE_DIALECT_DDL_UNSUPPORTED'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'ZHYC_LOWCODE_DIALECT_FIELD_TYPE_UNSUPPORTED'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'ZHYC_LOWCODE_DIALECT_PAGINATION_UNSUPPORTED'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'new BusinessException(ERROR_TABLE_REQUIRED, "建表模型不能为空")'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'new BusinessException(ERROR_COLUMN_REQUIRED, "字段模型不能为空")'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'new BusinessException(ERROR_SQL_REQUIRED, "SQL 不能为空")'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'requireDialectCode'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'resolveDdlGenerator'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'resolveFieldTypeMapper'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectService.java', 'resolvePaginationDialect'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDialectCapabilitiesResponse.java', 'ddlDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDialectCapabilitiesResponse.java', 'fieldTypeDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDialectCapabilitiesResponse.java', 'paginationDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDialectController.java', '@RequestMapping("/lowcode/dialects")'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDialectController.java', '@GetMapping("/capabilities")'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDialectController.java', '@RequiresPermissions("lowcode:dialect:query")'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDbDialectRegistry.java', 'listDdlDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDbDialectRegistry.java', 'listFieldTypeDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDbDialectRegistry.java', 'listPaginationDialectCodes'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDbDialectRegistry.java', '重复注册数据库方言'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/LowcodeDbDialectRegistry.java', '不支持该数据库方言'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/metadata/domain/LowcodeDatabaseDialect.java', 'public static LowcodeDatabaseDialect fromCode(String code)'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/metadata/domain/LowcodeDatabaseDialect.java', '不支持的低代码数据库方言编码'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/metadata/domain/LowcodeDatabaseDialectTest.java', 'shouldParseDatabaseDialectFromCode'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/metadata/domain/LowcodeDatabaseDialectTest.java', 'shouldExposeStableDatabaseDialectCode'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlDdlGenerator.java', 'COMMENT'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlDdlGenerator.java', 'LowcodeDatabaseDialect.MYSQL.getCode()'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlDdlGenerator.java', 'tenantDeletedIndex'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DdlGenerationSupport.java', 'tenantDeletedIndexName'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/DdlGenerationSupport.java', '"idx_" + tableName + "_tenant_deleted"'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlDdlGenerator.java', '`tenant_id`, `deleted`'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlDdlGenerator.java', 'commonColumnDefaultClause'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlDdlGenerator.java', 'DEFAULT CURRENT_TIMESTAMP'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlDdlGenerator.java', 'ON UPDATE CURRENT_TIMESTAMP'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlDdlGenerator.java', 'DEFAULT 0'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/metadata/converter/LowcodeTableModelConverter.java', 'appendTenantAndAuditColumns'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/metadata/converter/LowcodeTableModelConverter.java', '"tenant_id"'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/metadata/converter/LowcodeTableModelConverter.java', '"deleted"'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/metadata/converter/LowcodeTableModelConverter.java', '"version"'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/metadata/converter/LowcodeTableModelConverter.java', '"租户业务编码，用于共享表模式下的数据隔离"'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/metadata/converter/LowcodeTableModelConverterTest.java', '`tenant_id` VARCHAR(64) NOT NULL COMMENT'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/metadata/converter/LowcodeTableModelConverterTest.java', '`deleted` TINYINT NOT NULL DEFAULT 0 COMMENT'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/metadata/converter/LowcodeTableModelConverterTest.java', '`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/metadata/converter/LowcodeTableModelConverterTest.java', 'idx_pur_order_tenant_deleted'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/generator/LowcodeGeneratorServiceTest.java', '`tenant_id` VARCHAR(64) NOT NULL COMMENT'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/generator/LowcodeGeneratorServiceTest.java', 'idx_pur_order_tenant_deleted'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlFieldTypeMapper.java', 'DECIMAL'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlFieldTypeMapper.java', 'LowcodeDatabaseDialect.MYSQL.getCode()'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlPaginationDialect.java', 'LIMIT'],
  ['zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/db/mysql/MySqlPaginationDialect.java', 'LowcodeDatabaseDialect.MYSQL.getCode()'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/LowcodeDbDialectRegistryTest.java', 'shouldListRegisteredDialectCapabilities'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/LowcodeDbDialectRegistryTest.java', 'rejectsUnknownDialect'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/LowcodeDbDialectRegistryTest.java', 'rejectsDuplicateDialectRegistrationForDdlGenerator'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldGenerateCreateTableUsingMysqlDialect'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldRejectNullTableWhenGeneratingCreateTable'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldMapFieldTypeByDialect'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldRejectNullColumnWhenMappingFieldType'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldApplyPaginationByDialect'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldRejectBlankSqlWhenApplyingPagination'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'BusinessException exception = assertThrows(BusinessException.class'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldRejectUnknownDialectWhenMappingFieldTypeWithBusinessCode'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldRejectUnknownDialectWhenGeneratingCreateTableWithBusinessCode'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldRejectUnknownDialectWhenApplyingPaginationWithBusinessCode'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldRejectBlankDialectCodeWithBusinessCode'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/DefaultLowcodeDbDialectServiceTest.java', 'shouldListDialectCapabilities'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/LowcodeDialectControllerContractTest.java', 'shouldExposeDialectCapabilityRouteWithShiroPermission'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/LowcodeDialectControllerContractTest.java', '/lowcode/dialects'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/LowcodeDialectControllerContractTest.java', '/capabilities'],
  ['zhyc-module-lowcode/src/test/java/com/zhyc/lowcode/db/LowcodeDialectControllerContractTest.java', 'lowcode:dialect:query'],
  ['../zhyc-base-vue/src/api/lowcode/dialect.ts', 'DEFAULT_LOWCODE_DIALECT_CAPABILITIES'],
  ['../zhyc-base-vue/src/api/lowcode/dialect.ts', "ddlDialectCodes: ['mysql', 'postgresql', 'oracle', 'sqlserver', 'dm']"],
  ['../zhyc-base-vue/src/api/lowcode/dialect.ts', "fieldTypeDialectCodes: ['mysql', 'postgresql', 'oracle', 'sqlserver', 'dm']"],
  ['../zhyc-base-vue/src/api/lowcode/dialect.ts', "paginationDialectCodes: ['mysql', 'postgresql', 'oracle', 'sqlserver', 'dm']"],
  ['../zhyc-base-vue/src/api/lowcode/dialect.ts', 'buildSupportedLowcodeDialectCodes'],
  ['../zhyc-base-vue/src/api/lowcode/dialect.ts', 'formatLowcodeDialectLabel'],
  ['../zhyc-base-vue/src/api/lowcode/dialect.ts', "dm: '达梦数据库'"],
  ['../zhyc-base-vue/src/views/lowcode/datasource/index.vue', 'DEFAULT_LOWCODE_DIALECT_CAPABILITIES'],
  ['../zhyc-base-vue/src/views/lowcode/datasource/index.vue', 'buildSupportedLowcodeDialectCodes'],
  ['../zhyc-base-vue/src/views/lowcode/datasource/index.vue', 'formatLowcodeDialectLabel'],
  ['../zhyc-base-vue/src/views/lowcode/datasource/index.vue', "dialect: 'dm'"],
  ['../zhyc-base-vue/src/views/lowcode/datasource/index.vue', 'jdbc:dm://127.0.0.1:5236/zhyc-base-v1'],
  ['../zhyc-base-vue/src/views/lowcode/model/index.vue', 'DEFAULT_LOWCODE_DIALECT_CAPABILITIES'],
  ['../zhyc-base-vue/src/views/lowcode/model/index.vue', 'isLowcodeDialectFullySupported'],
  ['../zhyc-base-vue/src/views/lowcode/model/index.vue', 'formatLowcodeDialectLabel(dataSource.dialect)'],
  ['../zhyc-base-vue/src/views/lowcode/generator/index.vue', 'DEFAULT_LOWCODE_DIALECT_CAPABILITIES'],
  ['../zhyc-base-vue/src/views/lowcode/generator/index.vue', 'buildSupportedLowcodeDialectCodes'],
];

const forbiddenSnippets = [
  ['../zhyc-base-vue/src/views/lowcode/datasource/index.vue', "value: 'MYSQL'"],
  ['../zhyc-base-vue/src/views/lowcode/datasource/index.vue', "availableCodes : ['MYSQL'"],
  ['../zhyc-base-vue/src/views/lowcode/model/index.vue', "ddlDialectCodes: ['MYSQL']"],
  ['../zhyc-base-vue/src/views/lowcode/model/index.vue', "fieldTypeDialectCodes: ['MYSQL']"],
  ['../zhyc-base-vue/src/views/lowcode/model/index.vue', "paginationDialectCodes: ['MYSQL']"],
  ['../zhyc-base-vue/src/views/lowcode/generator/index.vue', "ddlDialectCodes: ['MYSQL']"],
  ['../zhyc-base-vue/src/views/lowcode/generator/index.vue', "fieldTypeDialectCodes: ['MYSQL']"],
  ['../zhyc-base-vue/src/views/lowcode/generator/index.vue', "paginationDialectCodes: ['MYSQL']"],
];

const missingSnippets = requiredSnippets.filter(([file, snippet]) => {
  const path = resolve(rootDir, file);
  return !existsSync(path) || !readFileSync(path, 'utf8').includes(snippet);
});

if (missingSnippets.length > 0) {
  console.error('低代码数据库方言契约校验失败。');
  for (const [file, snippet] of missingSnippets) {
    console.error(`缺少关键内容: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

const existingForbiddenSnippets = forbiddenSnippets.filter(([file, snippet]) => {
  const path = resolve(rootDir, file);
  return existsSync(path) && readFileSync(path, 'utf8').includes(snippet);
});

if (existingForbiddenSnippets.length > 0) {
  console.error('低代码数据库方言契约校验失败。');
  for (const [file, snippet] of existingForbiddenSnippets) {
    console.error(`存在禁止内容: ${file} -> ${snippet}`);
  }
  process.exit(1);
}

console.log('低代码数据库方言契约校验通过。');
