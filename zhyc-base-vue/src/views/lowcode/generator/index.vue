<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="lowcode-generator-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="8">
        <a-card title="代码生成" :bordered="false">
          <a-alert
            message="生成器默认使用保护策略，目标文件存在时阻断写入，避免覆盖人工代码。"
            type="warning"
            show-icon
          />

          <a-form class="generator-form" layout="vertical" :model="command">
            <a-form-item label="表模型编码" required>
              <a-select
                v-model:value="command.tableModelCode"
                show-search
                :options="tableModelOptions"
                placeholder="请选择表模型"
                @change="handleTableModelChange"
              />
            </a-form-item>
            <a-form-item label="生成目标端" required>
              <a-select v-model:value="command.target" :options="targetOptions" @change="handleTargetChange" />
            </a-form-item>
            <a-form-item label="业务模块名" required>
              <a-input v-model:value="command.moduleName" placeholder="purchase" />
            </a-form-item>
            <a-form-item label="业务实体名" required>
              <a-input v-model:value="command.entityName" placeholder="purchaseRequest" />
            </a-form-item>
            <a-form-item label="覆盖策略" required>
              <a-select v-model:value="overwriteStrategy" :options="overwriteOptions" />
            </a-form-item>
          </a-form>

          <a-alert
            class="page-filter-alert"
            type="info"
            show-icon
            :message="pageFilterTitle"
            :description="pageFilterDescription"
          />

          <a-alert
            class="dialect-capability-alert"
            type="success"
            show-icon
            message="数据库方言能力"
            :description="dialectCapabilityDescription"
          />

          <a-alert
            class="target-governance-alert"
            :type="command.target === 'OPEN_API_PORTAL' ? 'warning' : 'info'"
            show-icon
            message="生成目标治理"
            :description="targetGovernanceDescription"
          />

          <a-alert
            v-if="validationResult"
            class="generation-validation-alert"
            :type="validationResult.passed ? (validationResult.warnings.length > 0 ? 'warning' : 'success') : 'error'"
            show-icon
            :message="validationResult.passed ? '生成前校验通过' : '生成前校验未通过'"
          >
            <template #description>
              <div v-if="validationResult.errors.length > 0" class="validation-list">
                <div v-for="item in validationResult.errors" :key="`error-${item.code}-${item.message}`">
                  {{ item.code }}：{{ item.message }}
                </div>
              </div>
              <div v-if="validationResult.warnings.length > 0" class="validation-list">
                <div v-for="item in validationResult.warnings" :key="`warning-${item.code}-${item.message}`">
                  {{ item.code }}：{{ item.message }}
                </div>
              </div>
            </template>
          </a-alert>

          <a-space>
            <a-button :loading="templateLoading" @click="loadTemplates">刷新模板</a-button>
            <a-button :loading="validating" @click="handleValidate">生成校验</a-button>
            <a-button :loading="previewing" type="primary" @click="handlePreview">
              生成预览
            </a-button>
            <a-button :loading="executing" danger @click="handleExecute">执行生成</a-button>
          </a-space>

          <div class="permission-code">
            lowcode:generator:query / lowcode:generator:execute
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="16">
        <a-card title="模板清单" :bordered="false">
          <a-table
            row-key="code"
            size="small"
            :columns="templateColumns"
            :data-source="templates"
            :loading="templateLoading"
            :pagination="$tablePagination"
          />
        </a-card>

        <a-card class="preview-card" title="生成预览" :bordered="false">
          <template #extra>
            <span v-if="generationRecord" class="record-summary">
              记录 #{{ generationRecord.id }}，{{ generationRecord.fileCount }} 个文件，状态 {{ $statusLabel(generationRecord.status) }}
            </span>
          </template>
          <a-table
            row-key="path"
            size="small"
            :columns="previewColumns"
            :data-source="previewFiles"
            :pagination="$tablePagination"
            :scroll="{ x: 960 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'content'">
                <pre class="file-content">{{ record.content }}</pre>
              </template>
            </template>
          </a-table>
        </a-card>

        <a-card class="preview-card" title="生成历史" :bordered="false">
          <template #extra>
            <a-button :loading="recordLoading" @click="loadRecords">刷新历史</a-button>
          </template>
          <a-table
            row-key="id"
            size="small"
            :columns="recordColumns"
            :data-source="records"
            :loading="recordLoading"
            :pagination="$tablePagination"
            :scroll="{ x: 1120 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'fileManifestJson'">
                <pre class="file-manifest">{{ record.fileManifestJson }}</pre>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" size="small" @click="loadGenerationFiles(record)">查看文件</a-button>
              </template>
            </template>
          </a-table>
        </a-card>

        <a-card class="preview-card" title="生成文件明细" :bordered="false">
          <a-table
            row-key="id"
            size="small"
            :columns="generationFileColumns"
            :data-source="generationFiles"
            :loading="generationFileLoading"
            :pagination="$tablePagination"
            :scroll="{ x: 960 }"
          />
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

import {
  executeGeneration,
  listGenerationFiles,
  listGenerationRecords,
  listGeneratorTemplates,
  previewGeneratedFiles,
  validateGeneratedFiles,
  type LowcodeGenerationFileResponse,
  type LowcodeGeneratedFileResponse,
  type LowcodeGenerationPreviewRequest,
  type LowcodeGenerationRecordResponse,
  type LowcodeGenerationTarget,
  type LowcodeGenerationValidationResult,
  type LowcodeOverwriteStrategy,
  type LowcodeTemplateResponse,
} from '@/api/lowcode/generator';
import {
  listPageModels,
  listTableModels,
  type LowcodePageModel,
  type LowcodeTableModelResponse,
} from '@/api/lowcode/model';
import {
  DEFAULT_LOWCODE_DIALECT_CAPABILITIES,
  buildSupportedLowcodeDialectCodes,
  listLowcodeDialectCapabilities,
  type LowcodeDialectCapabilitiesResponse,
} from '@/api/lowcode/dialect';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 生成命令基础参数。 */
const command = reactive<LowcodeGenerationPreviewRequest>({
  tenantId: getAdminRuntimeContext().tenantId,
  tableModelCode: '',
  target: 'ADMIN_BACKEND',
  moduleName: '',
  entityName: '',
});

/** 当前覆盖策略。 */
const overwriteStrategy = ref<LowcodeOverwriteStrategy>('FAIL_IF_EXISTS');
/** 模板加载状态。 */
const templateLoading = ref(false);
/** 元数据加载状态。 */
const metadataLoading = ref(false);
/** 数据库方言能力加载状态。 */
const dialectLoading = ref(false);
/** 生成前校验提交状态。 */
const validating = ref(false);
/** 预览提交状态。 */
const previewing = ref(false);
/** 执行生成状态。 */
const executing = ref(false);
/** 生成历史加载状态。 */
const recordLoading = ref(false);
/** 生成文件明细加载状态。 */
const generationFileLoading = ref(false);
/** 当前目标端模板清单。 */
const templates = ref<LowcodeTemplateResponse[]>([]);
/** 当前预览文件清单。 */
const previewFiles = ref<LowcodeGeneratedFileResponse[]>([]);
/** 当前租户生成历史。 */
const records = ref<LowcodeGenerationRecordResponse[]>([]);
/** 当前生成文件明细。 */
const generationFiles = ref<LowcodeGenerationFileResponse[]>([]);
/** 最近一次生成记录。 */
const generationRecord = ref<LowcodeGenerationRecordResponse>();
/** 最近一次生成前校验结果。 */
const validationResult = ref<LowcodeGenerationValidationResult>();
/** 表模型列表。 */
const tableModels = ref<LowcodeTableModelResponse[]>([]);
/** 页面模型列表。 */
const pageModels = ref<LowcodePageModel[]>([]);
/** 当前平台数据库方言能力。 */
const dialectCapabilities = ref<LowcodeDialectCapabilitiesResponse>(DEFAULT_LOWCODE_DIALECT_CAPABILITIES);

/** 生成目标端选项。 */
const targetOptions: Array<{ label: string; value: LowcodeGenerationTarget; disabled?: boolean }> = [
  { label: '后台后端', value: 'ADMIN_BACKEND' },
  { label: '后台前端', value: 'ADMIN_FRONTEND' },
  { label: 'uni-app 移动端', value: 'UNIAPP' },
  { label: '开放 API/开发者门户', value: 'OPEN_API_PORTAL' },
  { label: '微服务模块', value: 'MICROSERVICE_MODULE' },
];

/** 覆盖策略选项。 */
const overwriteOptions: Array<{ label: string; value: LowcodeOverwriteStrategy }> = [
  { label: '存在则阻断', value: 'FAIL_IF_EXISTS' },
  { label: '确认后覆盖', value: 'OVERWRITE' },
];

/** 模板表格列。 */
const templateColumns = [
  { title: '模板编码', dataIndex: 'code', key: 'code' },
  { title: '模板名称', dataIndex: 'name', key: 'name' },
  { title: '目标端', dataIndex: 'target', key: 'target' },
  { title: '输出路径模式', dataIndex: 'outputPathPattern', key: 'outputPathPattern' },
];

/** 预览文件表格列。 */
const previewColumns = [
  { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode', width: 180 },
  { title: '输出路径', dataIndex: 'path', key: 'path', width: 260 },
  { title: '内容哈希', dataIndex: 'contentHash', key: 'contentHash', width: 280 },
  { title: '文件内容', dataIndex: 'content', key: 'content' },
];

/** 生成历史表格列。 */
const recordColumns = [
  { title: '记录 ID', dataIndex: 'id', key: 'id', width: 90 },
  { title: '表模型', dataIndex: 'tableModelCode', key: 'tableModelCode' },
  { title: '目标端', dataIndex: 'target', key: 'target' },
  { title: '模块', dataIndex: 'moduleName', key: 'moduleName' },
  { title: '实体', dataIndex: 'entityName', key: 'entityName' },
  { title: '文件数', dataIndex: 'fileCount', key: 'fileCount', width: 90 },
  { title: '文件清单', dataIndex: 'fileManifestJson', key: 'fileManifestJson', width: 320 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '操作', key: 'action', width: 100 },
];

/** 生成文件明细表格列。 */
const generationFileColumns = [
  { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode', width: 180 },
  { title: '文件路径', dataIndex: 'filePath', key: 'filePath', width: 320 },
  { title: '文件类型', dataIndex: 'fileType', key: 'fileType', width: 100 },
  { title: '覆盖模式', dataIndex: 'overwriteMode', key: 'overwriteMode', width: 130 },
  { title: '内容哈希', dataIndex: 'contentHash', key: 'contentHash', width: 300 },
];

/** 当前表模型。 */
const currentTableModel = computed(() => tableModels.value.find((model) => model.code === command.tableModelCode));

/** 表模型选择项。 */
const tableModelOptions = computed(() => tableModels.value.map((model) => ({
  label: `${model.code} / ${model.name}`,
  value: model.code,
})));

/** 当前目标端可参与过滤的页面模型。 */
const currentPageModels = computed(() => {
  const tableModel = currentTableModel.value;
  if (!tableModel) {
    return [];
  }
  return pageModels.value.filter((pageModel) => pageModel.tableModelId === tableModel.id)
    .filter((pageModel) => isPageModelForTarget(pageModel, command.target));
});

/** 三类方言能力均支持的数据库方言编码。 */
const supportedDialectCodes = computed(() => buildSupportedLowcodeDialectCodes(dialectCapabilities.value));

/** 当前生成目标的治理约束展示文案。 */
const targetGovernanceDescription = computed(() => {
  if (command.target === 'OPEN_API_PORTAL') {
    return '开放 API 生成目标会同步 API Key、OAuth2/OIDC、X-ZHYC-Request-Id、稳定错误码和调用审计字段，所有调用必须通过开放 API 网关。';
  }
  if (command.target === 'ADMIN_BACKEND') {
    return '后台后端模板会生成 Shiro 权限、统一响应、租户参数和数据库脚本，执行前请确认表模型已发布。';
  }
  if (command.target === 'UNIAPP') {
    return 'uni-app 模板会生成移动端 API、列表、表单和详情页面，页面路径以保存的页面模型为准。';
  }
  if (command.target === 'ADMIN_FRONTEND') {
    return '后台前端模板会生成 API client、列表、表单、详情和路由片段，并绑定后台租户上下文。';
  }
  return '微服务模块模板会生成独立 Maven 工程、启动类、模块描述文件、配置样例和交付说明，用于后续拆分 Spring Cloud 服务。';
});

/** 数据库方言能力展示文案。 */
const dialectCapabilityDescription = computed(() => {
  if (dialectLoading.value) {
    return '正在加载当前平台注册的 DDL、字段映射和分页方言能力。';
  }
  const supportedCodes = supportedDialectCodes.value;
  if (supportedCodes.length === 0) {
    return '当前没有三类能力同时可用的数据库方言，后端模板和 SQL 生成将使用 MySQL 兜底能力。';
  }
  return `当前可完整生成的数据库方言：${supportedCodes.join('、')}。`;
});

/** 页面模型过滤标题。 */
const pageFilterTitle = computed(() => {
  if (metadataLoading.value) {
    return '页面模型过滤加载中';
  }
  if (!currentTableModel.value) {
    return '页面模型过滤未匹配表模型';
  }
  if (currentPageModels.value.length === 0) {
    return '页面模型过滤未启用';
  }
  return '页面模型过滤已启用';
});

/** 页面模型过滤描述。 */
const pageFilterDescription = computed(() => {
  if (!currentTableModel.value) {
    return '请输入已存在的表模型编码，生成器会读取该表模型下保存的页面模型。';
  }
  if (currentPageModels.value.length === 0) {
    return '当前目标端没有匹配页面模型，生成预览将使用目标端完整模板清单。';
  }
  return currentPageModels.value
    .map((pageModel) => `${pageModel.pageType}: ${pageModel.componentPath}`)
    .join('；');
});

/**
 * 加载当前目标端模板。
 */
async function loadTemplates(): Promise<void> {
  templateLoading.value = true;
  try {
    templates.value = await listGeneratorTemplates(command.target);
  } finally {
    templateLoading.value = false;
  }
}

/**
 * 加载当前租户生成历史。
 */
async function loadRecords(): Promise<void> {
  recordLoading.value = true;
  try {
    records.value = await listGenerationRecords(syncTenantContext());
  } finally {
    recordLoading.value = false;
  }
}

/**
 * 加载生成文件明细。
 *
 * @param record 生成记录
 */
async function loadGenerationFiles(record: LowcodeGenerationRecordResponse): Promise<void> {
  generationFileLoading.value = true;
  try {
    generationFiles.value = await listGenerationFiles(syncTenantContext(), record.id);
  } catch (error) {
    generationFiles.value = [];
    message.error(error instanceof Error ? error.message : '生成文件明细加载失败');
  } finally {
    generationFileLoading.value = false;
  }
}

/**
 * 加载表模型和页面模型元数据。
 */
async function loadMetadata(): Promise<void> {
  metadataLoading.value = true;
  try {
    const tenantId = syncTenantContext();
    const [loadedTableModels, loadedPageModels] = await Promise.all([
      listTableModels(tenantId),
      listPageModels(tenantId),
    ]);
    tableModels.value = loadedTableModels;
    pageModels.value = loadedPageModels;
    if (!command.tableModelCode && loadedTableModels.length > 0) {
      command.tableModelCode = loadedTableModels[0].code;
      handleTableModelChange();
    }
  } finally {
    metadataLoading.value = false;
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
    message.warning('数据库方言能力加载失败，已使用 MySQL 兜底能力');
  } finally {
    dialectLoading.value = false;
  }
}

/**
 * 切换表模型后回填生成命令。
 */
function handleTableModelChange(): void {
  const model = currentTableModel.value;
  if (!model) {
    return;
  }
  command.moduleName = resolveModuleCode(model.code);
  command.entityName = resolveEntityCode(model.code);
  previewFiles.value = [];
  generationFiles.value = [];
  generationRecord.value = undefined;
  validationResult.value = undefined;
}

/**
 * 切换目标端后刷新模板清单。
 */
function handleTargetChange(): void {
  previewFiles.value = [];
  generationFiles.value = [];
  generationRecord.value = undefined;
  validationResult.value = undefined;
  void loadTemplates();
}

/**
 * 手动执行生成前校验。
 */
async function handleValidate(): Promise<void> {
  await runGenerationValidation(true);
}

/**
 * 执行生成前校验并返回是否可继续。
 *
 * @param showSuccess 是否展示成功提示
 * @returns 校验通过时返回 true
 */
async function runGenerationValidation(showSuccess: boolean): Promise<boolean> {
  validating.value = true;
  try {
    syncTenantContext();
    validationResult.value = await validateGeneratedFiles(command);
    if (!validationResult.value.passed) {
      message.error('生成前校验未通过');
      return false;
    }
    if (showSuccess) {
      message.success('生成前校验通过');
    }
    if (validationResult.value.warnings.length > 0) {
      message.warning('生成前校验存在警告，请确认命名和治理建议');
    }
    return true;
  } finally {
    validating.value = false;
  }
}

/**
 * 判断页面模型是否适用于当前生成目标端。
 *
 * @param pageModel 页面模型
 * @param target 生成目标端
 * @returns 是否适用
 */
function isPageModelForTarget(pageModel: LowcodePageModel, target: LowcodeGenerationTarget): boolean {
  if (target === 'ADMIN_FRONTEND') {
    return ['LIST', 'FORM', 'DETAIL'].includes(pageModel.pageType);
  }
  if (target === 'UNIAPP') {
    return ['MOBILE', 'MOBILE_FORM', 'MOBILE_DETAIL'].includes(pageModel.pageType);
  }
  return false;
}

/**
 * 从模型编码解析模块编码。
 *
 * @param modelCode 模型编码
 * @returns 模块编码
 */
function resolveModuleCode(modelCode: string): string {
  return modelCode.split('_').filter(Boolean)[0] || 'business';
}

/**
 * 从模型编码解析实体编码，和页面模型保持一致。
 *
 * @param modelCode 模型编码
 * @returns 小驼峰实体编码
 */
function resolveEntityCode(modelCode: string): string {
  const parts = modelCode.split('_').filter(Boolean);
  if (parts.length <= 1) {
    return toCamelEntityCode(parts);
  }
  return toCamelEntityCode(parts.slice(1));
}

/**
 * 将模型编码片段转换为小驼峰实体编码。
 *
 * @param parts 模型编码片段
 * @returns 小驼峰实体编码
 */
function toCamelEntityCode(parts: string[]): string {
  if (parts.length === 0) {
    return 'business';
  }
  return parts
    .map((part, index) => {
      const normalized = part.trim();
      if (index === 0) {
        return normalized;
      }
      return normalized.charAt(0).toUpperCase() + normalized.slice(1);
    })
    .join('');
}

/**
 * 预览生成文件。
 */
async function handlePreview(): Promise<void> {
  if (!(await runGenerationValidation(false))) {
    return;
  }
  previewing.value = true;
  try {
    syncTenantContext();
    previewFiles.value = await previewGeneratedFiles(command);
    generationFiles.value = [];
    generationRecord.value = undefined;
    message.success('生成预览已刷新');
  } finally {
    previewing.value = false;
  }
}

/**
 * 执行代码生成。
 */
async function handleExecute(): Promise<void> {
  if (!(await runGenerationValidation(false))) {
    return;
  }
  executing.value = true;
  try {
    syncTenantContext();
    generationRecord.value = await executeGeneration({
      ...command,
      overwriteStrategy: overwriteStrategy.value,
    });
    previewFiles.value = await previewGeneratedFiles(command);
    await loadRecords();
    await loadGenerationFiles(generationRecord.value);
    message.success('代码生成已执行');
  } finally {
    executing.value = false;
  }
}

/**
 * 同步后台租户到生成命令。
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
  void loadTemplates();
  void loadRecords();
  void loadMetadata();
});
</script>

<style scoped>
.lowcode-generator-page {
  padding: 16px;
}

.generator-form {
  margin-top: 16px;
}

.page-filter-alert {
  margin-bottom: 16px;
}

.dialect-capability-alert {
  margin-bottom: 16px;
}

.target-governance-alert {
  margin-bottom: 16px;
}

.generation-validation-alert {
  margin-bottom: 16px;
}

.validation-list {
  line-height: 1.7;
}

.permission-code {
  color: #6b7280;
  font-size: 12px;
  margin-top: 12px;
}

.preview-card {
  margin-top: 16px;
}

.record-summary {
  color: #374151;
  font-size: 12px;
}

.file-content {
  background: #111827;
  color: #e5e7eb;
  max-height: 220px;
  overflow: auto;
  padding: 12px;
  white-space: pre-wrap;
}

.file-manifest {
  max-height: 160px;
  margin: 0;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
