/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 低代码字段类型。
 */
export type LowcodeFieldType = 'STRING' | 'TEXT' | 'INTEGER' | 'LONG' | 'DECIMAL' | 'BOOLEAN' | 'DATE' | 'DATETIME';

/**
 * 低代码字段模型。
 */
export interface LowcodeColumnModel {
  /** 字段编码，对应 Java 属性名。 */
  code: string;
  /** 字段中文名称。 */
  name: string;
  /** 平台统一字段类型。 */
  fieldType: LowcodeFieldType;
  /** 字符串或数值字段长度。 */
  length?: number;
  /** 小数字段精度。 */
  scale?: number;
  /** 是否必填。 */
  required: boolean;
  /** 是否主键字段。 */
  primaryKey: boolean;
  /** 是否自增字段。 */
  autoIncrement: boolean;
  /** 是否在列表展示。 */
  listVisible: boolean;
  /** 是否在表单展示。 */
  formVisible: boolean;
  /** 是否支持查询。 */
  queryable: boolean;
  /** 绑定的系统字典编码；用于生成下拉、标签和枚举展示。 */
  dictCode?: string;
  /** 字段备注说明。 */
  comment?: string;
}

/**
 * 低代码表模型响应。
 */
export interface LowcodeTableModelResponse {
  /** 表模型主键。 */
  id: number;
  /** 租户编码。 */
  tenantId: string;
  /** 数据源主键。 */
  dataSourceId: number;
  /** 模型编码。 */
  code: string;
  /** 模型名称。 */
  name: string;
  /** 物理表名。 */
  tableName: string;
  /** 模型状态。 */
  status: string;
  /** 字段模型列表。 */
  columns: LowcodeColumnModel[];
}

/**
 * 低代码表模型保存参数。
 */
export interface LowcodeTableModelSaveRequest {
  /** 租户编码。 */
  tenantId: string;
  /** 数据源主键。 */
  dataSourceId: number;
  /** 模型编码。 */
  code: string;
  /** 模型名称。 */
  name: string;
  /** 物理表名。 */
  tableName: string;
  /** 模型状态。 */
  status: string;
  /** 字段模型列表。 */
  columns: LowcodeColumnModel[];
}

/**
 * 数据源物理表。
 */
export interface LowcodePhysicalTable {
  /** 物理表名。 */
  tableName: string;
  /** 数据库表注释。 */
  comment?: string;
  /** 已读取字段数量；仅查询表清单时可能为 0。 */
  columnCount: number;
}

/**
 * 数据源物理表导入参数。
 */
export interface LowcodePhysicalTableImportRequest {
  /** 租户编码。 */
  tenantId: string;
  /** 数据源主键。 */
  dataSourceId: number;
  /** 物理表名。 */
  tableName: string;
  /** 导入后的模型编码。 */
  modelCode: string;
  /** 导入后的模型名称。 */
  modelName: string;
}

/**
 * 低代码表关系。
 */
export interface LowcodeTableRelation {
  /** 表关系主键。 */
  id?: number;
  /** 租户编码。 */
  tenantId: string;
  /** 主表模型主键。 */
  mainTableId: number;
  /** 子表模型主键。 */
  subTableId: number;
  /** 关系类型。 */
  relationType: string;
  /** 主表关联字段编码。 */
  joinColumn: string;
  /** 子表引用字段编码。 */
  refColumn: string;
}

/**
 * 低代码页面模型。
 */
export interface LowcodePageModel {
  /** 页面模型主键。 */
  id?: number;
  /** 租户编码。 */
  tenantId: string;
  /** 表模型主键。 */
  tableModelId: number;
  /** 页面类型。 */
  pageType: string;
  /** 前端路由路径。 */
  routePath: string;
  /** 组件文件路径。 */
  componentPath: string;
  /** 页面布局类型。 */
  layoutType: string;
}

/**
 * 查询租户下的数据表模型。
 */
export function listTableModels(tenantId: string): Promise<LowcodeTableModelResponse[]> {
  return request<LowcodeTableModelResponse[]>('/lowcode/metadata/table-models', {
    query: { tenantId },
  });
}

/**
 * 保存数据表模型和字段配置。
 */
export function saveTableModel(command: LowcodeTableModelSaveRequest): Promise<LowcodeTableModelResponse> {
  return request<LowcodeTableModelResponse, LowcodeTableModelSaveRequest>('/lowcode/metadata/table-models', {
    method: 'POST',
    body: command,
  });
}

/**
 * 查询数据表模型详情。
 */
export function getTableModel(code: string, tenantId: string): Promise<LowcodeTableModelResponse> {
  return request<LowcodeTableModelResponse>(`/lowcode/metadata/table-models/${code}`, {
    query: { tenantId },
  });
}

/**
 * 查询数据源中的物理表清单。
 */
export function listPhysicalTables(tenantId: string, dataSourceId: number): Promise<LowcodePhysicalTable[]> {
  return request<LowcodePhysicalTable[]>(`/lowcode/metadata/data-sources/${dataSourceId}/tables`, {
    query: { tenantId },
  });
}

/**
 * 从数据源物理表导入数据表模型。
 */
export function importTableModel(
  command: LowcodePhysicalTableImportRequest,
): Promise<LowcodeTableModelResponse> {
  return request<LowcodeTableModelResponse, LowcodePhysicalTableImportRequest>(
    '/lowcode/metadata/table-models/import',
    {
      method: 'POST',
      body: command,
    },
  );
}

/**
 * 发布数据表模型。
 */
export function publishTableModel(code: string, tenantId: string): Promise<LowcodeTableModelResponse> {
  return request<LowcodeTableModelResponse>(`/lowcode/metadata/table-models/${code}/publish`, {
    method: 'POST',
    query: { tenantId },
  });
}

/**
 * 查询低代码表关系。
 */
export function listTableRelations(tenantId: string): Promise<LowcodeTableRelation[]> {
  return request<LowcodeTableRelation[]>('/lowcode/metadata/table-relations', {
    query: { tenantId },
  });
}

/**
 * 保存低代码表关系。
 */
export function saveTableRelation(command: LowcodeTableRelation): Promise<LowcodeTableRelation> {
  return request<LowcodeTableRelation, LowcodeTableRelation>('/lowcode/metadata/table-relations', {
    method: 'POST',
    body: command,
  });
}

/**
 * 查询低代码页面模型。
 */
export function listPageModels(tenantId: string): Promise<LowcodePageModel[]> {
  return request<LowcodePageModel[]>('/lowcode/metadata/page-models', {
    query: { tenantId },
  });
}

/**
 * 保存低代码页面模型。
 */
export function savePageModel(command: LowcodePageModel): Promise<LowcodePageModel> {
  return request<LowcodePageModel, LowcodePageModel>('/lowcode/metadata/page-models', {
    method: 'POST',
    body: command,
  });
}
