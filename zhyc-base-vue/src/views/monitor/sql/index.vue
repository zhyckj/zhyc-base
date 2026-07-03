<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="monitor-page">
    <a-card title="SQL 监控" :bordered="false">
      <template #extra>
        <a-space>
          <span class="query-label">展示阈值</span>
          <a-input-number
            v-model:value="thresholdMs"
            class="threshold-input"
            :min="1"
            :max="60000"
            :precision="0"
            addon-after="ms"
          />
          <a-button :loading="loading" @click="loadSqlRecords">刷新</a-button>
        </a-space>
      </template>

      <a-alert
        class="state-alert"
        message="优先展示应用内最近执行的 SQL 摘要，并结合数据库性能视图补充慢 SQL 指标；SQL 参数会被归一化，不展示明文入参。"
        type="info"
        show-icon
      />
      <a-alert
        v-if="errorMessage"
        class="state-alert"
        :message="errorMessage"
        type="error"
        show-icon
      />

      <div class="monitor-summary">
        <div class="summary-item">
          <span>慢 SQL</span>
          <strong>{{ slowSqlCount }}</strong>
        </div>
        <div class="summary-item">
          <span>严重慢 SQL</span>
          <strong>{{ criticalSqlCount }}</strong>
        </div>
        <div class="summary-item">
          <span>平均耗时</span>
          <strong>{{ averageCostMs }} ms</strong>
        </div>
        <div class="summary-item">
          <span>最高耗时</span>
          <strong>{{ maxCostMs }} ms</strong>
        </div>
      </div>

      <a-table
        row-key="sqlDigest"
        size="small"
        :columns="columns"
        :data-source="sqlRecords"
        :loading="loading"
        :locale="{ emptyText: '暂无 SQL 监控记录，请先访问业务页面触发查询后刷新。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'severity'">
            <a-tag :color="severityColor(record.severity)">{{ severityLabel(record.severity) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'sqlDigest'">
            <a-typography-paragraph class="sql-digest" :ellipsis="{ rows: 2, expandable: true, symbol: '展开' }">
              {{ record.sqlDigest }}
            </a-typography-paragraph>
          </template>
          <template v-else-if="column.key === 'suggestion'">
            <span class="suggestion-text">{{ record.suggestion }}</span>
          </template>
          <template v-else-if="column.key === 'avgCostMs'">
            <a-tag :color="costColor(record.avgCostMs)">{{ record.avgCostMs }} ms</a-tag>
          </template>
          <template v-else-if="column.key === 'maxCostMs'">
            <a-tag :color="costColor(record.maxCostMs)">{{ record.maxCostMs }} ms</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import { listRuntimeSqlMonitorRecords, type RuntimeSqlMonitorRecord } from '@/api/monitor/runtime';

/** SQL 监控记录列表。 */
const sqlRecords = ref<RuntimeSqlMonitorRecord[]>([]);
/** SQL 展示耗时阈值，单位毫秒。 */
const thresholdMs = ref(1);
/** 加载状态。 */
const loading = ref(false);
/** SQL 监控加载错误提示。 */
const errorMessage = ref('');

/** SQL 监控表格列。 */
const columns = [
  { title: '数据源', dataIndex: 'sourceCode', key: 'sourceCode', width: 150 },
  { title: '等级', dataIndex: 'severity', key: 'severity', width: 110 },
  { title: 'SQL 摘要', dataIndex: 'sqlDigest', key: 'sqlDigest', width: 420 },
  { title: '次数', dataIndex: 'executeCount', key: 'executeCount', width: 90 },
  { title: '平均耗时', dataIndex: 'avgCostMs', key: 'avgCostMs', width: 120 },
  { title: '最高耗时', dataIndex: 'maxCostMs', key: 'maxCostMs', width: 120 },
  { title: '扫描/返回', key: 'rows', width: 130, customRender: ({ record }: { record: RuntimeSqlMonitorRecord }) => `${record.rowsExamined}/${record.rowsSent}` },
  { title: '优化建议', dataIndex: 'suggestion', key: 'suggestion' },
  { title: '最近采集', dataIndex: 'lastSeen', key: 'lastSeen', width: 180 },
];

/** 慢 SQL 数量。 */
const slowSqlCount = computed(() => sqlRecords.value.filter((record) => record.severity === 'SLOW').length);
/** 严重慢 SQL 数量。 */
const criticalSqlCount = computed(() => sqlRecords.value.filter((record) => record.severity === 'CRITICAL').length);
/** 平均执行耗时。 */
const averageCostMs = computed(() => averageLatency(sqlRecords.value.map((record) => record.avgCostMs)));
/** 最高执行耗时。 */
const maxCostMs = computed(() => Math.max(0, ...sqlRecords.value.map((record) => record.maxCostMs ?? 0)));

/**
 * 加载 SQL 执行效率监控记录。
 */
async function loadSqlRecords(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    sqlRecords.value = await listRuntimeSqlMonitorRecords(thresholdMs.value, 20);
  } catch (error) {
    sqlRecords.value = [];
    errorMessage.value = error instanceof Error ? error.message : 'SQL 监控记录加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadSqlRecords();
});

/**
 * 获取慢 SQL 等级标签颜色。
 *
 * @param severity 慢 SQL 等级
 * @returns Ant Design 标签颜色
 */
function severityColor(severity: RuntimeSqlMonitorRecord['severity']): string {
  if (severity === 'CRITICAL') {
    return 'red';
  }
  if (severity === 'SLOW') {
    return 'orange';
  }
  if (severity === 'UNAVAILABLE') {
    return 'default';
  }
  return 'green';
}

/**
 * 获取慢 SQL 等级显示文本。
 *
 * @param severity 慢 SQL 等级
 * @returns 等级显示文本
 */
function severityLabel(severity: RuntimeSqlMonitorRecord['severity']): string {
  const labels: Record<RuntimeSqlMonitorRecord['severity'], string> = {
    CRITICAL: '严重',
    SLOW: '偏慢',
    NORMAL: '正常',
    UNAVAILABLE: '不可用',
  };
  return labels[severity] ?? severity;
}

/**
 * 获取耗时标签颜色。
 *
 * @param costMs 执行耗时，单位毫秒
 * @returns Ant Design 标签颜色
 */
function costColor(costMs: number): string {
  if (costMs >= 1000) {
    return 'red';
  }
  if (costMs >= 300) {
    return 'orange';
  }
  return 'green';
}

/**
 * 计算平均耗时。
 *
 * @param latencies 耗时列表
 * @returns 平均耗时，单位毫秒
 */
function averageLatency(latencies: number[]): number {
  if (latencies.length === 0) {
    return 0;
  }
  const total = latencies.reduce((sum, latency) => sum + (Number.isFinite(latency) ? latency : 0), 0);
  return Math.round(total / latencies.length);
}
</script>

<style scoped>
.monitor-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.query-label {
  color: #64748b;
  font-size: 13px;
}

.threshold-input {
  width: 150px;
}

.monitor-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.summary-item {
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.summary-item span {
  display: block;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
}

.summary-item strong {
  color: #111827;
  font-size: 18px;
}

.sql-digest {
  max-width: 420px;
  margin-bottom: 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}

.suggestion-text {
  color: #334155;
  line-height: 1.6;
}

@media (max-width: 920px) {
  .monitor-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .monitor-summary {
    grid-template-columns: 1fr;
  }
}
</style>
