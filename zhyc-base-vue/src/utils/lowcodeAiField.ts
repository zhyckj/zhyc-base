/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import type { LowcodeColumnModel, LowcodeFieldType } from '@/api/lowcode/model';

/** 低代码字段生成专用 AI 应用编码。 */
export const LOWCODE_AI_FIELD_APP_CODE = 'lowcode-model-assistant';

/** 低代码字段生成专用提示词编码。 */
export const LOWCODE_AI_FIELD_PROMPT_CODE = 'lowcode-field-generate';

/** 低代码字段生成默认提示词版本。 */
export const LOWCODE_AI_FIELD_PROMPT_VERSION = 'v1';

/** 低代码字段生成允许返回的字段类型。 */
export const LOWCODE_AI_ALLOWED_FIELD_TYPES: readonly LowcodeFieldType[] = [
  'STRING',
  'TEXT',
  'INTEGER',
  'LONG',
  'DECIMAL',
  'BOOLEAN',
  'DATE',
  'DATETIME',
];

/** 发布和代码生成必需的平台字段。 */
export const REQUIRED_PLATFORM_FIELD_CODES = ['id', 'tenant_id', 'deleted'] as const;

const FIELD_CODE_PATTERN = /^[a-z][a-z0-9_]{0,63}$/;
const REQUIRED_PLATFORM_FIELD_SET = new Set<string>(REQUIRED_PLATFORM_FIELD_CODES);

/**
 * 低代码 AI 字段生成上下文。
 */
export interface LowcodeAiFieldGenerationInput {
  /** 模型编码。 */
  modelCode?: string;
  /** 模型名称。 */
  modelName: string;
  /** 物理表名。 */
  tableName: string;
  /** 当前页面已有字段。 */
  existingFields: LowcodeColumnModel[];
}

/**
 * 低代码 AI 字段合并结果。
 */
export interface LowcodeAiColumnMergeResult {
  /** 合并后的字段列表。 */
  columns: LowcodeColumnModel[];
  /** 本次实际追加的字段。 */
  addedColumns: LowcodeColumnModel[];
  /** 因重复而跳过的字段编码。 */
  skippedCodes: string[];
}

/**
 * 构造 AI runtime 变量，统一约束字段输出格式。
 *
 * @param input 低代码建模上下文
 * @returns AI 提示词模板变量
 */
export function buildLowcodeAiFieldVariables(input: LowcodeAiFieldGenerationInput): Record<string, string> {
  const existingCodes = new Set(input.existingFields.map((field) => normalizeFieldCode(field.code)).filter(Boolean));
  const missingPlatformFields = REQUIRED_PLATFORM_FIELD_CODES
    .filter((fieldCode) => !existingCodes.has(fieldCode))
    .join('、') || '无';
  const existingFieldSummary = input.existingFields
    .map((field) => {
      const code = normalizeFieldCode(field.code);
      return code ? `${code}(${field.name || field.fieldType})` : '';
    })
    .filter(Boolean)
    .join('、') || '无';
  const allowedFieldTypes = LOWCODE_AI_ALLOWED_FIELD_TYPES.join('、');

  return {
    modelCode: input.modelCode?.trim() || '未填写',
    modelName: input.modelName.trim(),
    tableName: input.tableName.trim(),
    existingFields: existingFieldSummary,
    requiredPlatformFields: missingPlatformFields,
    allowedFieldTypes,
    instruction: [
      '你是 ZHYC 快速开发平台的低代码数据表建模助手。',
      `请根据模型名称「${input.modelName.trim()}」和物理表名「${input.tableName.trim()}」生成字段建议。`,
      `模型编码：${input.modelCode?.trim() || '未填写'}。`,
      `已有字段：${existingFieldSummary}。`,
      `如果缺少发布必需字段，请补齐：${missingPlatformFields}。`,
      `字段类型只能使用：${allowedFieldTypes}。`,
      '字段编码必须使用小写字母、数字和下划线，必须以小写字母开头，例如 request_no。',
      '每个字段对象必须包含 code、name、fieldType、length、scale、required、primaryKey、autoIncrement、listVisible、formVisible、queryable、comment。',
      '不要返回重复字段，不要返回解释说明，不要使用 Markdown 代码块，只返回 JSON 数组。',
    ].join('\n'),
  };
}

/**
 * 解析 AI 返回的字段 JSON。
 *
 * @param content AI 模型输出内容
 * @returns 规范化后的字段列表
 */
export function parseLowcodeAiColumns(content: string): LowcodeColumnModel[] {
  const value = parseJsonPayload(content);
  const rawColumns = Array.isArray(value) ? value : extractColumnsFromObject(value);
  const columns = rawColumns
    .map(normalizeLowcodeAiColumn)
    .filter((column): column is LowcodeColumnModel => Boolean(column));
  if (columns.length === 0) {
    throw new Error('AI 未返回可用字段，请确认提示词只输出字段 JSON 数组');
  }
  return columns.slice(0, 30);
}

/**
 * 将 AI 字段建议追加到已有字段后面，重复字段不覆盖。
 *
 * @param existingColumns 页面已有字段
 * @param generatedColumns AI 生成字段
 * @returns 合并结果
 */
export function mergeLowcodeAiColumns(
  existingColumns: LowcodeColumnModel[],
  generatedColumns: LowcodeColumnModel[],
): LowcodeAiColumnMergeResult {
  const meaningfulExistingColumns = existingColumns.filter(hasMeaningfulColumn);
  const existingCodes = new Set(
    meaningfulExistingColumns.map((column) => normalizeFieldCode(column.code)).filter(Boolean),
  );
  const addedColumns: LowcodeColumnModel[] = [];
  const skippedCodes: string[] = [];

  for (const generatedColumn of generatedColumns) {
    const fieldCode = normalizeFieldCode(generatedColumn.code);
    if (!fieldCode || existingCodes.has(fieldCode)) {
      if (fieldCode) {
        skippedCodes.push(fieldCode);
      }
      continue;
    }
    existingCodes.add(fieldCode);
    addedColumns.push({ ...generatedColumn, code: fieldCode });
  }

  return {
    columns: [...meaningfulExistingColumns, ...addedColumns],
    addedColumns,
    skippedCodes,
  };
}

function parseJsonPayload(content: string): unknown {
  const cleaned = stripMarkdownFence(content);
  const direct = tryParseJson(cleaned);
  if (direct.ok) {
    return direct.value;
  }

  const start = cleaned.indexOf('[');
  const end = cleaned.lastIndexOf(']');
  if (start >= 0 && end > start) {
    const arrayPayload = cleaned.slice(start, end + 1);
    const parsed = tryParseJson(arrayPayload);
    if (parsed.ok) {
      return parsed.value;
    }
  }
  throw new Error('AI 返回内容不是有效 JSON，请重试或检查提示词模板');
}

function stripMarkdownFence(content: string): string {
  return content
    .trim()
    .replace(/^```(?:json)?/i, '')
    .replace(/```$/i, '')
    .trim();
}

function tryParseJson(content: string): { ok: true; value: unknown } | { ok: false } {
  try {
    return { ok: true, value: JSON.parse(content) as unknown };
  } catch {
    return { ok: false };
  }
}

function extractColumnsFromObject(value: unknown): unknown[] {
  if (!isRecord(value)) {
    throw new Error('AI 返回内容必须是字段数组或包含 fields/columns 的对象');
  }
  const fields = value.fields ?? value.columns;
  if (!Array.isArray(fields)) {
    throw new Error('AI 返回对象中缺少 fields 或 columns 字段数组');
  }
  return fields;
}

function normalizeLowcodeAiColumn(value: unknown): LowcodeColumnModel | undefined {
  if (!isRecord(value)) {
    return undefined;
  }
  const code = normalizeFieldCode(value.code ?? value.fieldCode);
  if (!FIELD_CODE_PATTERN.test(code)) {
    return undefined;
  }
  const fieldType = normalizeFieldType(value.fieldType ?? value.type);
  const name = trimText(value.name ?? value.fieldName) || code;
  return {
    code,
    name,
    fieldType,
    length: normalizeLength(value.length ?? value.lengthValue, fieldType),
    scale: normalizeScale(value.scale ?? value.scaleValue, fieldType),
    required: toBoolean(value.required, REQUIRED_PLATFORM_FIELD_SET.has(code)),
    primaryKey: toBoolean(value.primaryKey, code === 'id'),
    autoIncrement: toBoolean(value.autoIncrement, code === 'id'),
    listVisible: toBoolean(value.listVisible, code !== 'id' && code !== 'deleted'),
    formVisible: toBoolean(value.formVisible, !REQUIRED_PLATFORM_FIELD_SET.has(code)),
    queryable: toBoolean(value.queryable, code !== 'id' && code !== 'deleted'),
    comment: trimText(value.comment ?? value.description) || name,
  };
}

function normalizeFieldType(value: unknown): LowcodeFieldType {
  const fieldType = trimText(value).toUpperCase();
  return LOWCODE_AI_ALLOWED_FIELD_TYPES.includes(fieldType as LowcodeFieldType)
    ? fieldType as LowcodeFieldType
    : 'STRING';
}

function normalizeLength(value: unknown, fieldType: LowcodeFieldType): number | undefined {
  const fallback = defaultLength(fieldType);
  const length = toNumber(value, fallback);
  if (length === undefined) {
    return undefined;
  }
  return Math.min(Math.max(Math.floor(length), 0), 4000);
}

function normalizeScale(value: unknown, fieldType: LowcodeFieldType): number {
  if (fieldType !== 'DECIMAL') {
    return 0;
  }
  return Math.min(Math.max(Math.floor(toNumber(value, 2) ?? 2), 0), 10);
}

function defaultLength(fieldType: LowcodeFieldType): number | undefined {
  if (fieldType === 'STRING') {
    return 64;
  }
  if (fieldType === 'INTEGER') {
    return 10;
  }
  if (fieldType === 'LONG') {
    return 20;
  }
  if (fieldType === 'DECIMAL') {
    return 18;
  }
  return undefined;
}

function toBoolean(value: unknown, fallback: boolean): boolean {
  if (typeof value === 'boolean') {
    return value;
  }
  if (typeof value === 'number') {
    return value === 1;
  }
  const text = trimText(value).toLowerCase();
  if (['true', '1', 'yes', 'y', '是'].includes(text)) {
    return true;
  }
  if (['false', '0', 'no', 'n', '否'].includes(text)) {
    return false;
  }
  return fallback;
}

function toNumber(value: unknown, fallback?: number): number | undefined {
  if (value === undefined || value === null || value === '') {
    return fallback;
  }
  const numberValue = Number(value);
  return Number.isFinite(numberValue) ? numberValue : fallback;
}

function normalizeFieldCode(value: unknown): string {
  return trimText(value)
    .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
    .replace(/[\s-]+/g, '_')
    .toLowerCase()
    .replace(/[^a-z0-9_]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_+|_+$/g, '');
}

function hasMeaningfulColumn(column: LowcodeColumnModel): boolean {
  return Boolean(column.code.trim() || column.name.trim() || column.comment?.trim());
}

function trimText(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}
