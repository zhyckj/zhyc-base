<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="lowcode-model-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="9">
        <a-card title="数据表建模" :bordered="false">
          <template #extra>
            <a-button :loading="loading" @click="loadModels">刷新</a-button>
          </template>
          <a-table
            row-key="code"
            :columns="modelColumns"
            :data-source="models"
            :loading="loading"
            :pagination="$tablePagination"
            :custom-row="buildModelRowProps"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="15">
        <a-card title="字段配置" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="resetCommand">新建模型</a-button>
              <a-button :loading="saving" type="primary" @click="handleSave">保存模型</a-button>
              <a-button :disabled="!command.code" :loading="publishing" @click="handlePublish">发布模型</a-button>
            </a-space>
          </template>

          <a-alert
            message="字段配置保存后会作为代码生成、DDL 生成和页面生成的统一输入。"
            type="info"
            show-icon
          />

          <a-alert
            class="dialect-capability-alert"
            :type="selectedDataSource
              && !isLowcodeDialectFullySupported(dialectCapabilities, selectedDataSource.dialect)
              ? 'warning'
              : 'success'"
            show-icon
            message="数据库方言能力"
            :description="selectedDataSourceCapabilityDescription"
          />

          <a-form class="model-form" layout="vertical" :model="command">
            <a-row :gutter="16">
              <a-col :xs="24" :md="8">
                <a-form-item label="模型编码" required>
                  <a-input v-model:value="command.code" placeholder="purchaseRequest" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="模型名称" required>
                  <a-input v-model:value="command.name" placeholder="采购申请" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="物理表名" required>
                  <a-input v-model:value="command.tableName" placeholder="pur_request" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="数据源 ID" required>
                  <a-select
                    v-model:value="command.dataSourceId"
                    :loading="dataSourceLoading"
                    :options="dataSourceOptions"
                    class="full-width"
                    placeholder="请选择数据源"
                    @change="handleDataSourceChange"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="模型状态" required>
                  <a-select v-model:value="command.status" :options="statusOptions" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="16">
                <a-form-item label="数据源表结构">
                  <div class="schema-import-control">
                    <a-select
                      v-model:value="physicalTableName"
                      :loading="physicalTableLoading"
                      :options="physicalTableOptions"
                      class="schema-table-select"
                      placeholder="选择数据源后自动加载业务表"
                    />
                    <a-button :loading="physicalTableLoading" @click="handleLoadPhysicalTables">重新加载业务表</a-button>
                    <a-button
                      :disabled="!physicalTableName"
                      :loading="importingPhysicalTable"
                      type="primary"
                      @click="handleImportPhysicalTable"
                    >
                      导入为模型
                    </a-button>
                  </div>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <div class="field-toolbar">
            <a-space>
              <a-button @click="addColumn">添加字段</a-button>
              <a-button
                :disabled="!command.name.trim() || !command.tableName.trim()"
                :loading="aiGeneratingFields"
                type="primary"
                ghost
                @click="handleGenerateColumnsWithAi"
              >
                AI 生成字段
              </a-button>
              <span class="permission-code">
                lowcode:table:save / lowcode:table:import / lowcode:table:publish / lowcode:table:ai-generate
              </span>
            </a-space>
          </div>

          <a-alert
            v-if="aiGenerationNotice"
            class="ai-generation-alert"
            closable
            show-icon
            :type="aiGenerationNotice.type"
            :message="aiGenerationNotice.message"
            :description="aiGenerationNotice.description"
            @close="aiGenerationNotice = null"
          />

          <a-table
            row-key="code"
            size="small"
            :columns="columnColumns"
            :data-source="command.columns"
            :pagination="$tablePagination"
            :scroll="{ x: 1280 }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'code'">
                <a-input v-model:value="record.code" placeholder="字段编码" />
              </template>
              <template v-else-if="column.key === 'name'">
                <a-input v-model:value="record.name" placeholder="字段名称" />
              </template>
              <template v-else-if="column.key === 'fieldType'">
                <a-select v-model:value="record.fieldType" :options="fieldTypeOptions" class="field-type-select" />
              </template>
              <template v-else-if="column.key === 'length'">
                <a-input-number v-model:value="record.length" :min="0" class="number-input" />
              </template>
              <template v-else-if="column.key === 'scale'">
                <a-input-number v-model:value="record.scale" :min="0" class="number-input" />
              </template>
              <template v-else-if="column.key === 'flags'">
                <a-space>
                  <a-checkbox v-model:checked="record.required">必填</a-checkbox>
                  <a-checkbox v-model:checked="record.primaryKey">主键</a-checkbox>
                  <a-checkbox v-model:checked="record.autoIncrement">自增</a-checkbox>
                </a-space>
              </template>
              <template v-else-if="column.key === 'scene'">
                <a-space>
                  <a-checkbox v-model:checked="record.listVisible">列表</a-checkbox>
                  <a-checkbox v-model:checked="record.formVisible">表单</a-checkbox>
                  <a-checkbox v-model:checked="record.queryable">查询</a-checkbox>
                </a-space>
              </template>
              <template v-else-if="column.key === 'comment'">
                <a-input v-model:value="record.comment" placeholder="字段说明" />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button danger type="link" @click="removeColumn(index)">删除</a-button>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { Modal, message } from 'ant-design-vue';

import { chatWithAiRuntime } from '@/api/ai/core';
import {
  DEFAULT_LOWCODE_DIALECT_CAPABILITIES,
  formatLowcodeDialectLabel,
  isLowcodeDialectFullySupported,
  listLowcodeDialectCapabilities,
  type LowcodeDialectCapabilitiesResponse,
} from '@/api/lowcode/dialect';
import {
  listLowcodeDataSources,
  type LowcodeDataSourceResponse,
} from '@/api/lowcode/datasource';
import {
  getTableModel,
  importTableModel,
  listPhysicalTables,
  listTableModels,
  publishTableModel,
  saveTableModel,
  type LowcodeColumnModel,
  type LowcodeFieldType,
  type LowcodePhysicalTable,
  type LowcodeTableModelResponse,
  type LowcodeTableModelSaveRequest,
} from '@/api/lowcode/model';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';
import {
  LOWCODE_AI_FIELD_APP_CODE,
  LOWCODE_AI_FIELD_PROMPT_CODE,
  LOWCODE_AI_FIELD_PROMPT_VERSION,
  buildLowcodeAiFieldVariables,
  mergeLowcodeAiColumns,
  parseLowcodeAiColumns,
} from '@/utils/lowcodeAiField';

/** AI 生成字段状态提示。 */
interface AiGenerationNotice {
  /** 提示类型。 */
  type: 'success' | 'info' | 'warning' | 'error';
  /** 提示标题。 */
  message: string;
  /** 提示详情。 */
  description?: string;
}

/** 表模型列表。 */
const models = ref<LowcodeTableModelResponse[]>([]);
/** 页面加载状态。 */
const loading = ref(false);
/** 保存提交状态。 */
const saving = ref(false);
/** 发布提交状态。 */
const publishing = ref(false);
/** 数据库方言能力加载状态。 */
const dialectLoading = ref(false);
/** 数据源加载状态。 */
const dataSourceLoading = ref(false);
/** 当前租户数据源列表。 */
const dataSources = ref<LowcodeDataSourceResponse[]>([]);
/** 当前数据源物理表列表。 */
const physicalTables = ref<LowcodePhysicalTable[]>([]);
/** 当前平台数据库方言能力。 */
const dialectCapabilities = ref<LowcodeDialectCapabilitiesResponse>(DEFAULT_LOWCODE_DIALECT_CAPABILITIES);
/** 当前选中的数据源物理表名。 */
const physicalTableName = ref<string>();
/** 物理表清单加载状态。 */
const physicalTableLoading = ref(false);
/** 物理表导入状态。 */
const importingPhysicalTable = ref(false);
/** AI 字段生成状态。 */
const aiGeneratingFields = ref(false);
/** 最近一次 AI 字段生成状态。 */
const aiGenerationNotice = ref<AiGenerationNotice | null>(null);

/** 表模型权限编码，供页面按钮和后续权限指令统一引用。 */
const permissionCodes = {
  /** 保存表模型权限。 */
  save: 'lowcode:table:save',
  /** 发布表模型权限。 */
  publish: 'lowcode:table:publish',
  /** 导入物理表结构权限。 */
  import: 'lowcode:table:import',
  /** AI 生成字段权限。 */
  aiGenerate: 'lowcode:table:ai-generate',
};

/** 字段类型下拉选项。 */
const fieldTypeOptions: Array<{ label: string; value: LowcodeFieldType }> = [
  { label: '字符串', value: 'STRING' },
  { label: '长文本', value: 'TEXT' },
  { label: '整数', value: 'INTEGER' },
  { label: '长整数', value: 'LONG' },
  { label: '小数', value: 'DECIMAL' },
  { label: '布尔', value: 'BOOLEAN' },
  { label: '日期', value: 'DATE' },
  { label: '日期时间', value: 'DATETIME' },
];

/** 模型状态下拉选项。 */
const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
];

/** 表模型列定义。 */
const modelColumns = [
  { title: '模型编码', dataIndex: 'code', key: 'code' },
  { title: '模型名称', dataIndex: 'name', key: 'name' },
  { title: '物理表名', dataIndex: 'tableName', key: 'tableName' },
  { title: '状态', dataIndex: 'status', key: 'status' },
];

/** 字段配置列定义。 */
const columnColumns = [
  { title: '字段编码', dataIndex: 'code', key: 'code', width: 160 },
  { title: '字段名称', dataIndex: 'name', key: 'name', width: 160 },
  { title: '类型', dataIndex: 'fieldType', key: 'fieldType', width: 140 },
  { title: '长度', dataIndex: 'length', key: 'length', width: 110 },
  { title: '精度', dataIndex: 'scale', key: 'scale', width: 110 },
  { title: '约束', key: 'flags', width: 250 },
  { title: '场景', key: 'scene', width: 230 },
  { title: '说明', dataIndex: 'comment', key: 'comment', width: 180 },
  { title: '操作', key: 'action', width: 90, fixed: 'right' },
];

/** 字段类型映射能力展示文案。 */
const fieldTypeDialectDescription = computed(() => {
  if (dialectLoading.value) {
    return '正在加载当前平台注册的字段类型映射能力。';
  }
  const fieldTypeDialectCodes = dialectCapabilities.value.fieldTypeDialectCodes;
  if (fieldTypeDialectCodes.length === 0) {
    return '当前未读取到字段类型映射能力，保存字段模型时将使用 MySQL 兜底能力。';
  }
  return `字段类型映射可用方言：${fieldTypeDialectCodes.join('、')}。`;
});

/** 数据源选择项。 */
const dataSourceOptions = computed(() => dataSources.value.map((dataSource) => ({
  label: `${dataSource.name} / ${formatLowcodeDialectLabel(dataSource.dialect)}`,
  value: dataSource.id,
})));

/** 物理表选择项。 */
const physicalTableOptions = computed(() => physicalTables.value.map((table) => ({
  label: table.comment ? `${table.tableName} / ${table.comment}` : table.tableName,
  value: table.tableName,
})));

/** 当前选中的数据源。 */
const selectedDataSource = computed(() => dataSources.value
  .find((dataSource) => dataSource.id === command.dataSourceId));

/** 当前选中的物理表。 */
const selectedPhysicalTable = computed(() => physicalTables.value
  .find((table) => table.tableName === physicalTableName.value));

/** 所选数据源方言能力展示文案。 */
const selectedDataSourceCapabilityDescription = computed(() => {
  const dataSource = selectedDataSource.value;
  if (!dataSource) {
    return `${fieldTypeDialectDescription.value} 尚未选择数据源，保存前请先绑定租户内数据源。`;
  }
  if (isLowcodeDialectFullySupported(dialectCapabilities.value, dataSource.dialect)) {
    return `所选数据源方言 ${formatLowcodeDialectLabel(dataSource.dialect)} 已具备 DDL、字段类型映射和分页三类能力。${fieldTypeDialectDescription.value}`;
  }
  return `所选数据源方言 ${formatLowcodeDialectLabel(dataSource.dialect)} 尚未完整具备 DDL、字段类型映射和分页三类能力，请先扩展方言能力或切换数据源。${fieldTypeDialectDescription.value}`;
});

/** 当前正在编辑的表模型。 */
const command = reactive<LowcodeTableModelSaveRequest>(createEmptyCommand());

/**
 * 创建空表模型命令。
 */
function createEmptyCommand(): LowcodeTableModelSaveRequest {
  return {
    tenantId: getAdminRuntimeContext().tenantId,
    dataSourceId: 1,
    code: '',
    name: '',
    tableName: '',
    status: 'DRAFT',
    columns: [createDefaultColumn()],
  };
}

/**
 * 创建默认字段配置。
 */
function createDefaultColumn(): LowcodeColumnModel {
  return {
    code: '',
    name: '',
    fieldType: 'STRING',
    length: 64,
    scale: 0,
    required: false,
    primaryKey: false,
    autoIncrement: false,
    listVisible: true,
    formVisible: true,
    queryable: false,
    comment: '',
  };
}

/**
 * 将后端模型响应复制到编辑命令，避免直接修改列表对象。
 */
function applyModel(model: LowcodeTableModelResponse): void {
  command.tenantId = model.tenantId;
  command.dataSourceId = model.dataSourceId;
  command.code = model.code;
  command.name = model.name;
  command.tableName = model.tableName;
  command.status = model.status;
  command.columns = model.columns.map((column) => ({ ...column }));
}

/**
 * 重置编辑表单。
 */
function resetCommand(): void {
  Object.assign(command, createEmptyCommand());
  physicalTableName.value = undefined;
  physicalTables.value = [];
}

/**
 * 加载表模型列表。
 */
async function loadModels(): Promise<void> {
  loading.value = true;
  try {
    models.value = await listTableModels(syncTenantContext());
  } finally {
    loading.value = false;
  }
}

/**
 * 加载当前租户可用于低代码建模的数据源。
 */
async function loadDataSources(): Promise<void> {
  dataSourceLoading.value = true;
  try {
    dataSources.value = await listLowcodeDataSources(syncTenantContext());
    if (!dataSources.value.some((dataSource) => dataSource.id === command.dataSourceId)
        && dataSources.value.length > 0) {
      command.dataSourceId = dataSources.value[0].id;
    }
    if (command.dataSourceId) {
      await loadPhysicalTables(false);
    }
  } catch {
    dataSources.value = [];
    message.warning('数据源加载失败，请确认已配置当前租户的数据源');
  } finally {
    dataSourceLoading.value = false;
  }
}

/**
 * 切换数据源时清空已加载的物理表结构。
 */
async function handleDataSourceChange(): Promise<void> {
  physicalTables.value = [];
  physicalTableName.value = undefined;
  await loadPhysicalTables(false);
}

/**
 * 加载当前数据源的物理表清单。
 */
async function handleLoadPhysicalTables(): Promise<void> {
  await loadPhysicalTables(true);
}

/**
 * 加载当前数据源的业务物理表清单。
 *
 * @param showSuccess 是否展示加载成功提示
 */
async function loadPhysicalTables(showSuccess: boolean): Promise<void> {
  const dataSourceId = command.dataSourceId;
  if (!dataSourceId) {
    message.warning('请先选择数据源');
    return;
  }
  physicalTableLoading.value = true;
  try {
    physicalTables.value = await listPhysicalTables(syncTenantContext(), dataSourceId);
    physicalTableName.value = physicalTables.value[0]?.tableName;
    if (showSuccess) {
      message.success(`已加载 ${physicalTables.value.length} 张业务表，权限: ${permissionCodes.import}`);
    }
  } catch (error) {
    physicalTables.value = [];
    physicalTableName.value = undefined;
    message.error(error instanceof Error ? error.message : '物理表结构加载失败，请确认数据源已启用且密钥解析器可用');
  } finally {
    physicalTableLoading.value = false;
  }
}

/**
 * 加载当前平台数据库方言能力。
 */
async function loadDialectCapabilities(): Promise<void> {
  dialectLoading.value = true;
  try {
    dialectCapabilities.value = await listLowcodeDialectCapabilities();
  } catch {
    dialectCapabilities.value = DEFAULT_LOWCODE_DIALECT_CAPABILITIES;
    message.warning('字段类型映射能力加载失败，已使用 MySQL 兜底能力');
  } finally {
    dialectLoading.value = false;
  }
}

/**
 * 构建表格行点击属性。
 */
function buildModelRowProps(record: LowcodeTableModelResponse): { onClick: () => Promise<void> } {
  return {
    onClick: async () => {
      const detail = await getTableModel(record.code, syncTenantContext());
      applyModel(detail);
    },
  };
}

/**
 * 添加字段行。
 */
function addColumn(): void {
  command.columns.push(createDefaultColumn());
}

/**
 * 删除字段行。
 *
 * @param index 字段行下标
 */
function removeColumn(index: number): void {
  command.columns.splice(index, 1);
  if (command.columns.length === 0) {
    addColumn();
  }
}

/**
 * 通过 AI runtime 根据模型名称和物理表名生成字段建议。
 */
async function handleGenerateColumnsWithAi(): Promise<void> {
  const modelName = command.name.trim();
  const tableName = command.tableName.trim();
  if (!modelName || !tableName) {
    aiGenerationNotice.value = {
      type: 'warning',
      message: 'AI 生成字段前缺少必要信息',
      description: '请先填写模型名称和物理表名，再使用 AI 生成字段。',
    };
    message.warning('请先填写模型名称和物理表名，再使用 AI 生成字段');
    return;
  }

  aiGeneratingFields.value = true;
  aiGenerationNotice.value = {
    type: 'info',
    message: '正在调用 AI 生成字段',
    description: `应用 ${LOWCODE_AI_FIELD_APP_CODE}，提示词 ${LOWCODE_AI_FIELD_PROMPT_CODE}/${LOWCODE_AI_FIELD_PROMPT_VERSION}。`,
  };
  try {
    const result = await chatWithAiRuntime({
      tenantId: syncTenantContext(),
      appCode: LOWCODE_AI_FIELD_APP_CODE,
      promptCode: LOWCODE_AI_FIELD_PROMPT_CODE,
      promptVersion: LOWCODE_AI_FIELD_PROMPT_VERSION,
      variables: buildLowcodeAiFieldVariables({
        modelCode: command.code,
        modelName,
        tableName,
        existingFields: command.columns,
      }),
      stream: false,
    });
    const generatedColumns = parseLowcodeAiColumns(result.content);
    const mergeResult = mergeLowcodeAiColumns(command.columns, generatedColumns);
    if (mergeResult.addedColumns.length === 0) {
      aiGenerationNotice.value = {
        type: 'warning',
        message: 'AI 未追加新字段',
        description: 'AI 返回字段均已存在，当前表模型字段未发生变化。',
      };
      message.warning('AI 返回字段均已存在，未追加新字段');
      return;
    }
    aiGenerationNotice.value = {
      type: 'success',
      message: 'AI 字段建议已生成',
      description: `AI 返回 ${generatedColumns.length} 个字段，准备追加 ${mergeResult.addedColumns.length} 个字段。`,
    };
    confirmApplyAiColumns(mergeResult);
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : 'AI 字段生成失败';
    aiGenerationNotice.value = {
      type: 'error',
      message: 'AI 字段生成失败',
      description: `${errorMessage}。请确认 AI 能力中心已配置应用 ${LOWCODE_AI_FIELD_APP_CODE} 和提示词 ${LOWCODE_AI_FIELD_PROMPT_CODE}。`,
    };
    message.error(
      `${errorMessage}。请确认 AI 能力中心已配置应用 ${LOWCODE_AI_FIELD_APP_CODE} 和提示词 ${LOWCODE_AI_FIELD_PROMPT_CODE}`,
    );
  } finally {
    aiGeneratingFields.value = false;
  }
}

/**
 * 二次确认并追加 AI 字段建议。
 *
 * @param mergeResult AI 字段合并结果
 */
function confirmApplyAiColumns(mergeResult: ReturnType<typeof mergeLowcodeAiColumns>): void {
  const previewText = mergeResult.addedColumns
    .slice(0, 8)
    .map((column) => `${column.code}(${column.name})`)
    .join('、');
  const moreText = mergeResult.addedColumns.length > 8 ? ` 等 ${mergeResult.addedColumns.length} 个字段` : '';
  const skippedText = mergeResult.skippedCodes.length > 0
    ? `，已跳过重复字段 ${Array.from(new Set(mergeResult.skippedCodes)).join('、')}`
    : '';

  Modal.confirm({
    title: '应用 AI 字段建议',
    content: `将追加 ${mergeResult.addedColumns.length} 个字段：${previewText}${moreText}${skippedText}。已有字段不会被覆盖。`,
    okText: '追加字段',
    cancelText: '取消',
    onOk: () => {
      command.columns = mergeResult.columns;
      aiGenerationNotice.value = {
        type: 'success',
        message: 'AI 字段已追加',
        description: `已追加 ${mergeResult.addedColumns.length} 个字段，保存模型前仍可继续人工调整。`,
      };
      message.success(`已追加 ${mergeResult.addedColumns.length} 个 AI 字段建议`);
    },
  });
}

/**
 * 保存表模型和字段配置。
 */
async function handleSave(): Promise<void> {
  saving.value = true;
  try {
    syncTenantContext();
    const saved = await saveTableModel(command);
    applyModel(saved);
    await loadModels();
    message.success(`已保存模型，权限: ${permissionCodes.save}`);
  } finally {
    saving.value = false;
  }
}

/**
 * 从物理表导入字段模型。
 */
async function handleImportPhysicalTable(): Promise<void> {
  const dataSourceId = command.dataSourceId;
  const tableName = physicalTableName.value;
  if (!dataSourceId || !tableName) {
    message.warning('请先选择数据源和物理表');
    return;
  }
  importingPhysicalTable.value = true;
  try {
    const modelCode = command.code.trim() || toCamelCase(tableName);
    const modelName = command.name.trim() || selectedPhysicalTable.value?.comment || tableName;
    const imported = await importTableModel({
      tenantId: syncTenantContext(),
      dataSourceId,
      tableName,
      modelCode,
      modelName,
    });
    applyModel(imported);
    await loadModels();
    message.success(`已导入表结构，权限: ${permissionCodes.import}`);
  } finally {
    importingPhysicalTable.value = false;
  }
}

/**
 * 发布表模型。
 */
async function handlePublish(): Promise<void> {
  publishing.value = true;
  try {
    const published = await publishTableModel(command.code, syncTenantContext());
    applyModel(published);
    await loadModels();
    message.success(`已发布模型并生成数据库表，权限: ${permissionCodes.publish}`);
  } finally {
    publishing.value = false;
  }
}

/**
 * 将下划线表名转换为模型编码。
 *
 * @param tableName 物理表名
 * @returns camelCase 模型编码
 */
function toCamelCase(tableName: string): string {
  return tableName
    .split('_')
    .filter(Boolean)
    .map((part, index) => {
      const lower = part.toLowerCase();
      return index === 0 ? lower : lower.charAt(0).toUpperCase() + lower.slice(1);
    })
    .join('');
}

/**
 * 同步后台租户到表模型命令。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  command.tenantId = currentTenantId;
  return currentTenantId;
}

onMounted(() => {
  void loadDialectCapabilities();
  void loadDataSources();
  void loadModels();
});
</script>

<style scoped>
.lowcode-model-page {
  padding: 16px;
}

.model-form {
  margin-top: 16px;
}

.dialect-capability-alert {
  margin-top: 16px;
}

.field-toolbar {
  margin: 8px 0 12px;
}

.ai-generation-alert {
  margin-bottom: 12px;
}

.full-width {
  width: 100%;
}

.number-input {
  width: 96px;
}

.field-type-select {
  width: 128px;
}

.schema-import-control {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.schema-table-select {
  min-width: 260px;
  flex: 1 1 260px;
}

.permission-code {
  color: #6b7280;
  font-size: 12px;
}
</style>
