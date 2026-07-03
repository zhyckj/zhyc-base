<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section>
    <a-card title="开发者应用" :bordered="false">
      <template #extra>
        <a-button :loading="loading" @click="loadApps">刷新</a-button>
      </template>
      <a-table row-key="id" :columns="columns" :data-source="apps" :loading="loading" :pagination="$tablePagination" />
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { listOpenApiApps, type OpenApiAppResponse } from '@/api/openapi/app';
import { requireAdminTenantId } from '@/utils/adminContext';

/** 开放平台应用列表。 */
const apps = ref<OpenApiAppResponse[]>([]);
/** 页面加载状态。 */
const loading = ref(false);

/** 应用表格列定义。 */
const columns = [
  { title: '应用编码', dataIndex: 'appCode', key: 'appCode' },
  { title: '应用名称', dataIndex: 'appName', key: 'appName' },
  { title: '状态', dataIndex: 'status', key: 'status' },
];

/**
 * 加载开发者应用。
 */
async function loadApps(): Promise<void> {
  loading.value = true;
  try {
    const tenantId = requireAdminTenantId();
    apps.value = await listOpenApiApps(tenantId);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadApps();
});
</script>
