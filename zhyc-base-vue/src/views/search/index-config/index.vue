<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="search-page">
    <a-card title="全文检索" :bordered="false">
      <template #extra>
        <a-space>
          <a-select v-model:value="status" class="status-select" allow-clear placeholder="状态">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
          <a-input v-model:value="queryKeyword" class="keyword-input" placeholder="检索关键词" allow-clear />
          <a-button :loading="loading" @click="loadSearchData">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增索引</a-button>
          <a-button :loading="creatingTask" @click="rebuildDefaultIndex">重建索引</a-button>
          <a-button :loading="querying" @click="executeQuery">执行检索</a-button>
        </a-space>
      </template>

      <a-alert
        v-if="errorMessage"
        class="state-alert"
        :message="errorMessage"
        type="error"
        show-icon
      />

      <a-table
        row-key="id"
        size="small"
        :columns="configColumns"
        :data-source="configs"
        :loading="loading"
        :locale="{ emptyText: '暂无全文检索索引。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
              {{ $statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'indexCode'">
            <a-tag color="blue">{{ record.indexCode }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openEditForm(record)">编辑</a-button>
          </template>
        </template>
      </a-table>

      <a-tabs class="detail-tabs" size="small">
        <a-tab-pane key="tasks" tab="重建任务">
          <a-table
            row-key="id"
            size="small"
            :columns="taskColumns"
            :data-source="tasks"
            :locale="{ emptyText: '暂无索引重建任务。' }"
            :pagination="$tablePagination"
          />
        </a-tab-pane>
        <a-tab-pane key="logs" tab="搜索日志">
          <a-table
            row-key="id"
            size="small"
            :columns="logColumns"
            :data-source="logs"
            :locale="{ emptyText: '暂无全文检索日志。' }"
            :pagination="$tablePagination"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      title="全文检索索引"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="索引编码" required>
          <a-input v-model:value="formState.indexCode" class="full-input" placeholder="例如 cms_content" />
        </a-form-item>
        <a-form-item label="索引名称" required>
          <a-input v-model:value="formState.indexName" class="full-input" placeholder="请输入索引名称" />
        </a-form-item>
        <a-form-item label="来源表" required>
          <a-input v-model:value="formState.sourceTable" class="full-input" placeholder="请输入来源表名" />
        </a-form-item>
        <a-form-item label="检索字段" required>
          <a-input v-model:value="formState.searchFields" class="full-input" placeholder="多个字段用英文逗号分隔" />
        </a-form-item>
        <a-form-item label="过滤字段">
          <a-input v-model:value="formState.filterFields" class="full-input" placeholder="多个字段用英文逗号分隔" />
        </a-form-item>
        <a-form-item label="索引状态" required>
          <a-select v-model:value="formState.status" class="full-input">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="formState.remark" :rows="3" placeholder="请输入配置备注" />
        </a-form-item>
        <span class="permission-code">search:index:save</span>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { onMounted, reactive, ref } from 'vue';

import {
  createSearchRebuildTask,
  executeSearchQuery,
  listSearchIndexConfigs,
  listSearchQueryLogs,
  listSearchRebuildTasks,
  saveSearchIndexConfig,
  type SearchIndexConfig,
  type SearchIndexConfigSavePayload,
  type SearchQueryLog,
  type SearchRebuildTask,
} from '@/api/search';
import { requireAdminTenantId } from '@/utils/adminContext';

/** 索引配置列表。 */
const configs = ref<SearchIndexConfig[]>([]);
/** 重建任务列表。 */
const tasks = ref<SearchRebuildTask[]>([]);
/** 检索日志列表。 */
const logs = ref<SearchQueryLog[]>([]);
/** 状态筛选值。 */
const status = ref<string | undefined>('enabled');
/** 检索关键词。 */
const queryKeyword = ref('');
/** 列表加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 创建重建任务状态。 */
const creatingTask = ref(false);
/** 检索状态。 */
const querying = ref(false);
/** 列表加载错误提示。 */
const errorMessage = ref('');
/** 索引配置表单弹窗状态。 */
const formOpen = ref(false);
/** 索引配置表单数据，提交时写入当前租户。 */
const formState = reactive<SearchIndexConfigSavePayload>({
  tenantId: '',
  indexCode: '',
  indexName: '',
  sourceTable: '',
  searchFields: '',
  filterFields: '',
  status: 'enabled',
  remark: '',
});

/** 索引配置表格列。 */
const configColumns = [
  { title: '索引编码', dataIndex: 'indexCode', key: 'indexCode', width: 160 },
  { title: '索引名称', dataIndex: 'indexName', key: 'indexName', width: 180 },
  { title: '来源表', dataIndex: 'sourceTable', key: 'sourceTable', width: 160 },
  { title: '检索字段', dataIndex: 'searchFields', key: 'searchFields' },
  { title: '过滤字段', dataIndex: 'filterFields', key: 'filterFields' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 100 },
];

/** 重建任务表格列。 */
const taskColumns = [
  { title: '索引编码', dataIndex: 'indexCode', key: 'indexCode', width: 160 },
  { title: '任务状态', dataIndex: 'taskStatus', key: 'taskStatus', width: 120 },
  { title: '触发类型', dataIndex: 'triggerType', key: 'triggerType', width: 120 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
];

/** 搜索日志表格列。 */
const logColumns = [
  { title: '索引编码', dataIndex: 'indexCode', key: 'indexCode', width: 160 },
  { title: '关键词', dataIndex: 'keyword', key: 'keyword' },
  { title: '结果数', dataIndex: 'resultCount', key: 'resultCount', width: 100 },
  { title: '耗时(ms)', dataIndex: 'costMs', key: 'costMs', width: 110 },
  { title: '状态', dataIndex: 'queryStatus', key: 'queryStatus', width: 100 },
];

/**
 * 加载全文检索配置、任务和日志。
 */
async function loadSearchData(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    configs.value = await listSearchIndexConfigs(status.value);
    const indexCode = resolveActiveIndexCode();
    if (indexCode) {
      tasks.value = await listSearchRebuildTasks(indexCode);
      logs.value = await listSearchQueryLogs(indexCode);
    } else {
      tasks.value = [];
      logs.value = [];
    }
  } catch (error) {
    configs.value = [];
    tasks.value = [];
    logs.value = [];
    errorMessage.value = error instanceof Error ? error.message : '全文检索数据加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 打开新增索引配置表单。
 */
function openCreateForm(): void {
  resetForm();
  formOpen.value = true;
}

/**
 * 打开编辑索引配置表单。
 *
 * @param config 当前选中的索引配置
 */
function openEditForm(config: SearchIndexConfig): void {
  formState.tenantId = requireAdminTenantId();
  formState.indexCode = config.indexCode;
  formState.indexName = config.indexName;
  formState.sourceTable = config.sourceTable;
  formState.searchFields = config.searchFields;
  formState.filterFields = config.filterFields || '';
  formState.status = config.status || 'enabled';
  formState.remark = config.remark || '';
  formOpen.value = true;
}

/**
 * 提交索引配置表单。
 */
async function submitForm(): Promise<void> {
  if (!validateForm()) {
    return;
  }
  saving.value = true;
  try {
    await saveSearchIndexConfig({
      tenantId: requireAdminTenantId(),
      indexCode: formState.indexCode.trim(),
      indexName: formState.indexName.trim(),
      sourceTable: formState.sourceTable.trim(),
      searchFields: formState.searchFields.trim(),
      filterFields: formState.filterFields?.trim(),
      status: formState.status || 'enabled',
      remark: formState.remark?.trim(),
    });
    message.success('全文检索索引配置已保存');
    formOpen.value = false;
    await loadSearchData();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '全文检索索引配置保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 重置索引配置表单。
 */
function resetForm(): void {
  formState.tenantId = requireAdminTenantId();
  formState.indexCode = '';
  formState.indexName = '';
  formState.sourceTable = '';
  formState.searchFields = '';
  formState.filterFields = '';
  formState.status = 'enabled';
  formState.remark = '';
}

/**
 * 校验索引配置表单。
 *
 * @returns 表单是否满足提交条件
 */
function validateForm(): boolean {
  if (!formState.indexCode.trim()) {
    message.warning('请输入索引编码');
    return false;
  }
  if (!formState.indexName.trim()) {
    message.warning('请输入索引名称');
    return false;
  }
  if (!formState.sourceTable.trim()) {
    message.warning('请输入来源表');
    return false;
  }
  if (!formState.searchFields.trim()) {
    message.warning('请输入检索字段');
    return false;
  }
  return true;
}

/**
 * 创建首期默认索引重建任务。
 */
async function rebuildDefaultIndex(): Promise<void> {
  const indexCode = resolveActiveIndexCode();
  if (!indexCode) {
    message.warning('请先保存或加载一个可用索引');
    return;
  }
  creatingTask.value = true;
  try {
    await createSearchRebuildTask(indexCode, requireAdminTenantId());
    message.success('索引重建任务已创建');
    await loadSearchData();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '索引重建任务创建失败');
  } finally {
    creatingTask.value = false;
  }
}

/**
 * 执行检索并刷新日志。
 */
async function executeQuery(): Promise<void> {
  const indexCode = resolveActiveIndexCode();
  if (!indexCode) {
    message.warning('请先保存或加载一个可用索引');
    return;
  }
  if (!queryKeyword.value.trim()) {
    message.warning('请输入检索关键词');
    return;
  }
  querying.value = true;
  try {
    await executeSearchQuery(indexCode, queryKeyword.value.trim(), requireAdminTenantId());
    message.success('全文检索请求已记录');
    await loadSearchData();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '全文检索请求失败');
  } finally {
    querying.value = false;
  }
}

/**
 * 解析当前可操作的索引编码。
 *
 * @returns 当前索引编码；没有可用索引时返回 undefined
 */
function resolveActiveIndexCode(): string | undefined {
  return configs.value[0]?.indexCode;
}

onMounted(() => {
  void loadSearchData();
});
</script>

<style scoped>
.search-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.status-select {
  width: 130px;
}

.keyword-input {
  width: 180px;
}

.full-input {
  width: 100%;
}

.permission-code {
  display: none;
}

.detail-tabs {
  margin-top: 16px;
}
</style>
