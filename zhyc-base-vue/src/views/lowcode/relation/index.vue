<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="lowcode-relation-page">
    <a-row :gutter="16">
      <a-col :span="9">
        <a-card title="表关系配置" :bordered="false">
          <a-form layout="vertical">
            <a-form-item label="主表">
              <a-select
                v-model:value="form.mainTableId"
                :options="tableOptions"
                placeholder="请选择主表"
                @change="syncDefaultColumns"
              />
            </a-form-item>
            <a-form-item label="子表">
              <a-select
                v-model:value="form.subTableId"
                :options="tableOptions"
                placeholder="请选择子表"
                @change="syncDefaultColumns"
              />
            </a-form-item>
            <a-form-item label="关系类型">
              <a-select v-model:value="form.relationType" :options="relationTypeOptions" />
            </a-form-item>
            <a-form-item label="主表关联字段">
              <a-select v-model:value="form.joinColumn" :options="mainColumnOptions" placeholder="请选择主表字段" />
            </a-form-item>
            <a-form-item label="子表引用字段">
              <a-select v-model:value="form.refColumn" :options="subColumnOptions" placeholder="请选择子表字段" />
            </a-form-item>
            <a-button type="primary" :loading="saving" @click="saveRelation">保存关系</a-button>
          </a-form>
        </a-card>
      </a-col>

      <a-col :span="15">
        <a-card title="表关系列表" :bordered="false">
          <template #extra>
            <a-button :loading="loading" @click="loadData">刷新</a-button>
          </template>
          <a-table
            row-key="id"
            size="small"
            :columns="columns"
            :data-source="relations"
            :loading="loading"
            :pagination="$tablePagination"
          />
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';

import {
  listTableModels,
  listTableRelations,
  saveTableRelation,
  type LowcodeColumnModel,
  type LowcodeTableModelResponse,
  type LowcodeTableRelation,
} from '@/api/lowcode/model';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 表模型列表。 */
const tables = ref<LowcodeTableModelResponse[]>([]);
/** 表关系列表。 */
const relations = ref<LowcodeTableRelation[]>([]);
/** 加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);

/** 表关系表单。 */
const form = reactive<LowcodeTableRelation>({
  tenantId: getAdminRuntimeContext().tenantId,
  mainTableId: 0,
  subTableId: 0,
  relationType: 'ONE_TO_MANY',
  joinColumn: '',
  refColumn: '',
});

/** 表选项。 */
const tableOptions = computed(() => tables.value.map((table) => ({
  label: `${table.name}(${table.tableName})`,
  value: table.id,
})));

/** 主表字段选项。 */
const mainColumnOptions = computed(() => columnOptions(form.mainTableId));
/** 子表字段选项。 */
const subColumnOptions = computed(() => columnOptions(form.subTableId));

/** 关系类型选项。 */
const relationTypeOptions = [
  { label: '一对多', value: 'ONE_TO_MANY' },
  { label: '一对一', value: 'ONE_TO_ONE' },
];

/** 表关系列。 */
const columns = [
  { title: '主表ID', dataIndex: 'mainTableId', key: 'mainTableId', width: 100 },
  { title: '子表ID', dataIndex: 'subTableId', key: 'subTableId', width: 100 },
  { title: '关系类型', dataIndex: 'relationType', key: 'relationType', width: 130 },
  { title: '主表字段', dataIndex: 'joinColumn', key: 'joinColumn' },
  { title: '子表字段', dataIndex: 'refColumn', key: 'refColumn' },
];

/**
 * 加载表模型和表关系。
 */
async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const tenantId = syncTenantContext();
    const [tableModels, tableRelations] = await Promise.all([
      listTableModels(tenantId),
      listTableRelations(tenantId),
    ]);
    tables.value = tableModels;
    relations.value = tableRelations;
    if (!form.mainTableId && tableModels[0]) {
      form.mainTableId = tableModels[0].id;
    }
    if (!form.subTableId && tableModels[1]) {
      form.subTableId = tableModels[1].id;
    }
    syncDefaultColumns();
  } finally {
    loading.value = false;
  }
}

/**
 * 保存当前表关系。
 */
async function saveRelation(): Promise<void> {
  if (!form.mainTableId || !form.subTableId || !form.joinColumn || !form.refColumn) {
    message.warning('请完整选择主表、子表和关联字段');
    return;
  }
  saving.value = true;
  try {
    syncTenantContext();
    await saveTableRelation({ ...form });
    message.success('表关系已保存');
    await loadData();
  } finally {
    saving.value = false;
  }
}

/**
 * 同步默认关联字段。
 */
function syncDefaultColumns(): void {
  form.joinColumn = form.joinColumn || firstColumnCode(form.mainTableId);
  form.refColumn = form.refColumn || firstColumnCode(form.subTableId);
}

/**
 * 构建字段下拉选项。
 *
 * @param tableId 表模型主键
 * @returns 字段下拉选项
 */
function columnOptions(tableId: number): Array<{ label: string; value: string }> {
  return resolveColumns(tableId).map((column) => ({
    label: `${column.name}(${column.code})`,
    value: column.code,
  }));
}

/**
 * 读取表模型字段。
 *
 * @param tableId 表模型主键
 * @returns 字段模型列表
 */
function resolveColumns(tableId: number): LowcodeColumnModel[] {
  return tables.value.find((table) => table.id === tableId)?.columns ?? [];
}

/**
 * 返回表模型第一个字段编码。
 *
 * @param tableId 表模型主键
 * @returns 字段编码
 */
function firstColumnCode(tableId: number): string {
  return resolveColumns(tableId)[0]?.code ?? '';
}

/**
 * 同步后台租户到表关系表单。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  form.tenantId = currentTenantId;
  return currentTenantId;
}

onMounted(() => {
  void loadData();
});
</script>

<style scoped>
.lowcode-relation-page {
  min-width: 0;
}
</style>
