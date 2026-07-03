<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="module-page">
    <a-card title="模块管理" :bordered="false">
      <template #extra>
        <a-button :loading="status === 'loading'" @click="loadModules">刷新</a-button>
      </template>

      <a-alert v-if="status === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-table
        row-key="moduleCode"
        :columns="columns"
        :data-source="modules"
        :loading="status === 'loading'"
        :pagination="$tablePagination"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'enabled'">
            <a-tag :color="record.enabled ? 'green' : 'default'">
              {{ record.enabled ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'dependencies'">
            {{ record.dependencies.length ? record.dependencies.join(',') : '-' }}
          </template>
          <template v-else-if="column.key === 'resources'">
            {{ record.resources.length }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button
              size="small"
              v-permission="'system:module:update'"
              :loading="savingCode === record.moduleCode"
              @click="toggleModule(record)"
            >
              {{ record.enabled ? '停用' : '启用' }}
            </a-button>
          </template>
        </template>
      </a-table>

      <span class="permission-code">system:module:update</span>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { changeSystemModuleEnabled, listSystemModules, type SystemModule } from '@/api/system/module';
import type { LoadStatus } from '@/types/platform';

/** 页面加载状态。 */
const status = ref<LoadStatus>('idle');
/** 正在保存的模块编码。 */
const savingCode = ref('');
/** 异常提示文案。 */
const errorMessage = ref('');
/** 系统模块列表。 */
const modules = ref<SystemModule[]>([]);

/** 表格列定义。 */
const columns = [
  { title: '模块名称', dataIndex: 'moduleName', key: 'moduleName' },
  { title: '模块编码', dataIndex: 'moduleCode', key: 'moduleCode', width: 180 },
  { title: '版本', dataIndex: 'version', key: 'version', width: 120 },
  { title: '类型', dataIndex: 'moduleType', key: 'moduleType', width: 120 },
  { title: '状态', dataIndex: 'enabled', key: 'enabled', width: 100 },
  { title: '依赖模块', dataIndex: 'dependencies', key: 'dependencies' },
  { title: '资源数', dataIndex: 'resources', key: 'resources', width: 90 },
  { title: '操作', key: 'action', width: 100 },
];

/**
 * 加载系统模块列表。
 */
async function loadModules(): Promise<void> {
  status.value = 'loading';
  try {
    modules.value = await listSystemModules();
    status.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '模块加载失败';
    status.value = 'error';
  }
}

/**
 * 切换系统模块启用状态。
 *
 * @param module 系统模块
 */
async function toggleModule(module: SystemModule): Promise<void> {
  savingCode.value = module.moduleCode;
  try {
    await changeSystemModuleEnabled(module.moduleCode, {
      enabled: !module.enabled,
    });
    await loadModules();
  } finally {
    savingCode.value = '';
  }
}

onMounted(() => {
  void loadModules();
});
</script>

<style scoped>
.module-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.permission-code {
  display: none;
}
</style>
