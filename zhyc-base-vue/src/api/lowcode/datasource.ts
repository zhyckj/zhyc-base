/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';
import {
  buildSystemSecretSelectOptions,
  listSystemSecretOptions,
  type SystemSecretSelectOption,
} from '@/api/system/secret';

/**
 * 低代码数据库方言。
 */
export type LowcodeDatabaseDialect = string;

/**
 * 低代码数据源响应。
 */
export interface LowcodeDataSourceResponse {
  /** 数据库主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 数据源编码。 */
  code: string;
  /** 数据源名称。 */
  name: string;
  /** 数据库方言编码。 */
  dialect: LowcodeDatabaseDialect;
  /** JDBC 连接地址。 */
  jdbcUrl: string;
  /** 数据库登录用户名。 */
  username: string;
  /** 数据库口令密钥引用，后续版本由密钥中心下拉回填。 */
  passwordSecretRef?: string;
  /** 数据源是否启用。 */
  enabled: boolean;
}

/**
 * 低代码数据源保存参数。
 */
export interface LowcodeDataSourceSaveRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 数据源编码。 */
  code: string;
  /** 数据源名称。 */
  name: string;
  /** 数据库方言编码。 */
  dialect: LowcodeDatabaseDialect;
  /** JDBC 连接地址。 */
  jdbcUrl: string;
  /** 数据库登录用户名。 */
  username: string;
  /** 数据库口令密钥引用，不传明文口令。 */
  passwordSecretRef: string;
  /** 数据源是否启用。 */
  enabled: boolean;
}

/**
 * 低代码数据库密码密钥选择项。
 */
export type LowcodePasswordSecretOption = SystemSecretSelectOption;

/**
 * 低代码数据源连接测试请求。
 */
export interface LowcodeDataSourceConnectionTestRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 数据源编码。 */
  code: string;
}

/**
 * 低代码数据源连接测试结果。
 */
export interface LowcodeDataSourceConnectionTestResult {
  /** 数据源编码。 */
  code: string;
  /** 连接测试是否成功。 */
  success: boolean;
  /** 连接测试结果说明。 */
  message: string;
}

/**
 * 查询租户下的数据源列表。
 *
 * @param tenantId 租户业务编码
 * @returns 数据源响应列表
 */
export function listLowcodeDataSources(tenantId: string): Promise<LowcodeDataSourceResponse[]> {
  return request<LowcodeDataSourceResponse[]>('/lowcode/metadata/data-sources', {
    query: { tenantId },
  });
}

/**
 * 查询单个低代码数据源详情。
 *
 * <p>编辑态使用详情接口回填完整配置，尤其是数据库口令密钥引用，避免列表数据缺字段导致表单不回显。</p>
 *
 * @param tenantId 租户业务编码
 * @param code 数据源编码
 * @returns 数据源详情响应
 */
export function getLowcodeDataSource(tenantId: string, code: string): Promise<LowcodeDataSourceResponse> {
  return request<LowcodeDataSourceResponse>(`/lowcode/metadata/data-sources/${encodeURIComponent(code)}`, {
    query: { tenantId },
  });
}

/**
 * 保存低代码数据源。
 *
 * @param command 数据源保存参数
 * @returns 保存后的数据源响应
 */
export function saveLowcodeDataSource(
  command: LowcodeDataSourceSaveRequest,
): Promise<LowcodeDataSourceResponse> {
  return request<LowcodeDataSourceResponse, LowcodeDataSourceSaveRequest>('/lowcode/metadata/data-sources', {
    method: 'POST',
    body: command,
  });
}

/**
 * 测试低代码数据源连接配置。
 *
 * @param command 数据源连接测试请求
 * @returns 数据源连接测试结果
 */
export function testLowcodeDataSourceConnection(
  command: LowcodeDataSourceConnectionTestRequest,
): Promise<LowcodeDataSourceConnectionTestResult> {
  return request<LowcodeDataSourceConnectionTestResult, LowcodeDataSourceConnectionTestRequest>(
    '/lowcode/metadata/data-sources/test-connection',
    {
      method: 'POST',
      body: command,
    },
  );
}

/**
 * 查询当前租户可用于低代码数据源的数据库口令密钥。
 *
 * @param tenantId 租户业务编码
 * @returns 数据库口令密钥下拉项
 */
export async function listLowcodePasswordSecretOptions(
  tenantId: string,
): Promise<LowcodePasswordSecretOption[]> {
  const options = await listSystemSecretOptions(tenantId, undefined, 'enabled');
  return buildSystemSecretSelectOptions(options);
}
