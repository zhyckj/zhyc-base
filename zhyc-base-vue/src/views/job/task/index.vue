<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="job-page">
    <a-card title="在线作业任务" :bordered="false">
      <template #extra>
        <a-space>
          <a-select
            v-model:value="status"
            class="status-select"
            allow-clear
            placeholder="状态"
            @change="loadTasks"
          >
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
          <a-button :loading="loading" @click="loadTasks">刷新</a-button>
          <a-button type="primary" @click="openCreateForm">新增任务</a-button>
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
        row-key="jobCode"
        size="small"
        :columns="columns"
        :data-source="tasks"
        :loading="loading"
        :locale="{ emptyText: '暂无作业任务。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
              {{ $statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openEditForm(record)">编辑</a-button>
              <a-button size="small" @click="toggleStatus(record)">
                {{ record.status === 'enabled' ? '停用' : '启用' }}
              </a-button>
              <a-button size="small" type="primary" @click="triggerTask(record)">
                触发
              </a-button>
              <span class="permission-code">job:task:trigger</span>
              <a-button size="small" @click="openLogs(record)">日志</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:open="logDrawerOpen"
      width="720"
      title="执行日志"
      placement="right"
    >
      <a-table
        row-key="id"
        size="small"
        :columns="logColumns"
        :data-source="logs"
        :loading="logLoading"
        :locale="{ emptyText: '暂无作业执行日志。' }"
        :pagination="$tablePagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'result'">
            <a-tag :color="record.result === 'success' ? 'green' : 'red'">
              {{ record.result }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-drawer>

    <a-modal
      v-model:open="formOpen"
      title="作业任务"
      width="760px"
      :confirm-loading="saving"
      @ok="submitForm"
    >
      <a-form layout="vertical">
        <a-form-item label="任务编码" required>
          <a-input v-model:value="formState.jobCode" class="full-input" placeholder="例如 purchase-daily-sync" />
        </a-form-item>
        <a-form-item label="任务名称" required>
          <a-input v-model:value="formState.jobName" class="full-input" placeholder="请输入任务名称" />
        </a-form-item>
        <a-form-item label="执行周期" required>
          <a-radio-group v-model:value="cronBuilderMode" button-style="solid" @change="syncCronExpressionFromBuilder">
            <a-radio-button value="everyMinutes">每 N 分钟</a-radio-button>
            <a-radio-button value="hourly">每小时</a-radio-button>
            <a-radio-button value="daily">每天</a-radio-button>
            <a-radio-button value="weekly">每周</a-radio-button>
            <a-radio-button value="monthly">每月</a-radio-button>
            <a-radio-button value="custom">兼容 Cron</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <div class="cron-builder-panel">
          <a-form-item v-if="cronBuilderMode === 'everyMinutes'" label="间隔分钟" required>
            <a-input-number
              v-model:value="cronBuilderState.intervalMinutes"
              class="full-input"
              :min="1"
              :max="59"
              @change="syncCronExpressionFromBuilder"
            />
          </a-form-item>
          <a-form-item v-else-if="cronBuilderMode === 'hourly'" label="执行分钟" required>
            <a-input-number
              v-model:value="cronBuilderState.minute"
              class="full-input"
              :min="0"
              :max="59"
              @change="syncCronExpressionFromBuilder"
            />
          </a-form-item>
          <template v-else-if="cronBuilderMode === 'daily'">
            <a-row :gutter="12">
              <a-col :span="12">
                <a-form-item label="执行小时" required>
                  <a-input-number
                    v-model:value="cronBuilderState.hour"
                    class="full-input"
                    :min="0"
                    :max="23"
                    @change="syncCronExpressionFromBuilder"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="执行分钟" required>
                  <a-input-number
                    v-model:value="cronBuilderState.minute"
                    class="full-input"
                    :min="0"
                    :max="59"
                    @change="syncCronExpressionFromBuilder"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </template>
          <template v-else-if="cronBuilderMode === 'weekly'">
            <a-row :gutter="12">
              <a-col :span="8">
                <a-form-item label="星期" required>
                  <a-select
                    v-model:value="cronBuilderState.weekday"
                    class="full-input"
                    :options="weekdayOptions"
                    @change="syncCronExpressionFromBuilder"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="执行小时" required>
                  <a-input-number
                    v-model:value="cronBuilderState.hour"
                    class="full-input"
                    :min="0"
                    :max="23"
                    @change="syncCronExpressionFromBuilder"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="执行分钟" required>
                  <a-input-number
                    v-model:value="cronBuilderState.minute"
                    class="full-input"
                    :min="0"
                    :max="59"
                    @change="syncCronExpressionFromBuilder"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </template>
          <template v-else-if="cronBuilderMode === 'monthly'">
            <a-row :gutter="12">
              <a-col :span="8">
                <a-form-item label="日期" required>
                  <a-input-number
                    v-model:value="cronBuilderState.dayOfMonth"
                    class="full-input"
                    :min="1"
                    :max="28"
                    @change="syncCronExpressionFromBuilder"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="执行小时" required>
                  <a-input-number
                    v-model:value="cronBuilderState.hour"
                    class="full-input"
                    :min="0"
                    :max="23"
                    @change="syncCronExpressionFromBuilder"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="执行分钟" required>
                  <a-input-number
                    v-model:value="cronBuilderState.minute"
                    class="full-input"
                    :min="0"
                    :max="59"
                    @change="syncCronExpressionFromBuilder"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </template>
          <a-form-item label="Cron 表达式" required>
            <a-input :value="cronPreviewText" class="full-input" readonly />
          </a-form-item>
        </div>
        <a-alert
          v-if="cronBuilderMode === 'custom'"
          class="cron-alert"
          type="warning"
          show-icon
          message="当前表达式暂不支持可视化编辑，切换周期后将重新生成。"
        />
        <a-alert
          v-else
          class="cron-alert"
          type="info"
          show-icon
          :message="cronReadableText"
        />
        <a-form-item label="处理器名称" required>
          <a-input v-model:value="formState.handlerName" class="full-input" placeholder="请输入 Spring Bean 名称" />
        </a-form-item>
        <a-form-item label="任务说明">
          <a-textarea v-model:value="formState.jobDescription" :rows="3" placeholder="请输入任务说明" />
        </a-form-item>
        <a-form-item label="任务状态" required>
          <a-select v-model:value="formState.status" class="full-input">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
        </a-form-item>
        <span class="permission-code">job:task:save</span>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';

import {
  changeJobTaskStatus,
  listJobTaskLogs,
  listJobTasks,
  saveJobTask,
  triggerJobTask,
  type JobTaskLogRecord,
  type JobTaskRecord,
  type JobTaskSavePayload,
} from '@/api/job/task';
import { requireAdminTenantId } from '@/utils/adminContext';

type CronBuilderMode = 'everyMinutes' | 'hourly' | 'daily' | 'weekly' | 'monthly' | 'custom';

interface CronBuilderState {
  /** 分钟间隔，用于每 N 分钟执行。 */
  intervalMinutes: number;
  /** 小时，取值 0 到 23。 */
  hour: number;
  /** 分钟，取值 0 到 59。 */
  minute: number;
  /** 月内日期，取值 1 到 28，避免月底不存在日期导致任务跳过。 */
  dayOfMonth: number;
  /** 周内日期，使用 Quartz Cron 周英文缩写。 */
  weekday: string;
}

/** 作业任务列表。 */
const tasks = ref<JobTaskRecord[]>([]);
/** 作业执行日志列表。 */
const logs = ref<JobTaskLogRecord[]>([]);
/** 作业状态筛选值。 */
const status = ref<string | undefined>();
/** 列表加载状态。 */
const loading = ref(false);
/** 保存状态。 */
const saving = ref(false);
/** 日志加载状态。 */
const logLoading = ref(false);
/** 日志抽屉打开状态。 */
const logDrawerOpen = ref(false);
/** 列表加载错误提示。 */
const errorMessage = ref('');
/** 日志加载错误提示。 */
const logErrorMessage = ref('');
/** 作业任务表单弹窗状态。 */
const formOpen = ref(false);
/** 作业任务表单数据，提交时强制使用当前租户。 */
const formState = reactive<JobTaskSavePayload>({
  tenantId: '',
  jobCode: '',
  jobName: '',
  cronExpression: '',
  handlerName: '',
  jobDescription: '',
  status: 'disabled',
});
/** Cron 生成模式。 */
const cronBuilderMode = ref<CronBuilderMode>('daily');
/** Cron 生成参数。 */
const cronBuilderState = reactive<CronBuilderState>({
  intervalMinutes: 5,
  hour: 2,
  minute: 0,
  dayOfMonth: 1,
  weekday: 'MON',
});

/** 周期选择项。 */
const weekdayOptions = [
  { label: '周一', value: 'MON' },
  { label: '周二', value: 'TUE' },
  { label: '周三', value: 'WED' },
  { label: '周四', value: 'THU' },
  { label: '周五', value: 'FRI' },
  { label: '周六', value: 'SAT' },
  { label: '周日', value: 'SUN' },
];

/** 作业任务表格列。 */
const columns = [
  { title: '任务编码', dataIndex: 'jobCode', key: 'jobCode', width: 160 },
  { title: '任务名称', dataIndex: 'jobName', key: 'jobName', width: 150 },
  { title: 'Cron', dataIndex: 'cronExpression', key: 'cronExpression', width: 150 },
  { title: '处理器', dataIndex: 'handlerName', key: 'handlerName', width: 190 },
  { title: '说明', dataIndex: 'jobDescription', key: 'jobDescription' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 260 },
];

/** 作业执行日志表格列。 */
const logColumns = [
  { title: '触发类型', dataIndex: 'triggerType', key: 'triggerType', width: 100 },
  { title: '开始时间', dataIndex: 'startAt', key: 'startAt', width: 180 },
  { title: '结束时间', dataIndex: 'endAt', key: 'endAt', width: 180 },
  { title: '结果', dataIndex: 'result', key: 'result', width: 90 },
  { title: '错误信息', dataIndex: 'errorMessage', key: 'errorMessage' },
];

/** Cron 表达式预览文本。 */
const cronPreviewText = computed(() => formState.cronExpression || '-');

/** Cron 周期中文说明。 */
const cronReadableText = computed(() => {
  const minute = padNumber(cronBuilderState.minute);
  const hour = padNumber(cronBuilderState.hour);
  if (cronBuilderMode.value === 'everyMinutes') {
    return `每 ${cronBuilderState.intervalMinutes} 分钟执行一次`;
  }
  if (cronBuilderMode.value === 'hourly') {
    return `每小时第 ${minute} 分钟执行`;
  }
  if (cronBuilderMode.value === 'daily') {
    return `每天 ${hour}:${minute} 执行`;
  }
  if (cronBuilderMode.value === 'weekly') {
    return `每${weekdayLabel(cronBuilderState.weekday)} ${hour}:${minute} 执行`;
  }
  if (cronBuilderMode.value === 'monthly') {
    return `每月 ${cronBuilderState.dayOfMonth} 日 ${hour}:${minute} 执行`;
  }
  return '兼容 Cron';
});

/**
 * 加载作业任务列表。
 */
async function loadTasks(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    tasks.value = await listJobTasks(status.value);
  } catch (error) {
    tasks.value = [];
    errorMessage.value = error instanceof Error ? error.message : '作业任务加载失败';
  } finally {
    loading.value = false;
  }
}

/**
 * 打开新增作业任务表单。
 */
function openCreateForm(): void {
  resetForm();
  formOpen.value = true;
}

/**
 * 打开编辑作业任务表单。
 *
 * @param task 当前选中的作业任务
 */
function openEditForm(task: JobTaskRecord): void {
  formState.tenantId = requireAdminTenantId();
  formState.jobCode = task.jobCode;
  formState.jobName = task.jobName;
  formState.cronExpression = task.cronExpression;
  formState.handlerName = task.handlerName;
  formState.jobDescription = task.jobDescription || '';
  formState.status = task.status || 'disabled';
  applyCronExpressionToBuilder(task.cronExpression);
  formOpen.value = true;
}

/**
 * 提交作业任务表单。
 */
async function submitForm(): Promise<void> {
  if (!validateForm()) {
    return;
  }
  saving.value = true;
  try {
    await saveJobTask({
      tenantId: requireAdminTenantId(),
      jobCode: formState.jobCode.trim(),
      jobName: formState.jobName.trim(),
      cronExpression: formState.cronExpression.trim(),
      handlerName: formState.handlerName.trim(),
      jobDescription: formState.jobDescription?.trim(),
      status: formState.status,
    });
    message.success('作业任务已保存');
    formOpen.value = false;
    await loadTasks();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '作业任务保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 重置作业任务表单。
 */
function resetForm(): void {
  formState.tenantId = requireAdminTenantId();
  formState.jobCode = '';
  formState.jobName = '';
  resetCronBuilder();
  formState.handlerName = '';
  formState.jobDescription = '';
  formState.status = 'disabled';
}

/**
 * 重置 Cron 生成器为每日凌晨两点。
 */
function resetCronBuilder(): void {
  cronBuilderMode.value = 'daily';
  cronBuilderState.intervalMinutes = 5;
  cronBuilderState.hour = 2;
  cronBuilderState.minute = 0;
  cronBuilderState.dayOfMonth = 1;
  cronBuilderState.weekday = 'MON';
  syncCronExpressionFromBuilder();
}

/**
 * 根据当前选择项同步 Cron 表达式。
 */
function syncCronExpressionFromBuilder(): void {
  if (cronBuilderMode.value === 'custom') {
    return;
  }
  formState.cronExpression = buildCronExpression();
}

/**
 * 根据选择项生成 Quartz Cron 表达式。
 *
 * @returns Cron 表达式
 */
function buildCronExpression(): string {
  const intervalMinutes = normalizeNumber(cronBuilderState.intervalMinutes, 1, 59, 5);
  const minute = normalizeNumber(cronBuilderState.minute, 0, 59, 0);
  const hour = normalizeNumber(cronBuilderState.hour, 0, 23, 2);
  const dayOfMonth = normalizeNumber(cronBuilderState.dayOfMonth, 1, 28, 1);
  if (cronBuilderMode.value === 'everyMinutes') {
    return `0 0/${intervalMinutes} * * * ?`;
  }
  if (cronBuilderMode.value === 'hourly') {
    return `0 ${minute} * * * ?`;
  }
  if (cronBuilderMode.value === 'daily') {
    return `0 ${minute} ${hour} * * ?`;
  }
  if (cronBuilderMode.value === 'weekly') {
    return `0 ${minute} ${hour} ? * ${normalizeWeekday(cronBuilderState.weekday)}`;
  }
  return `0 ${minute} ${hour} ${dayOfMonth} * ?`;
}

/**
 * 将已有 Cron 表达式反向识别为生成器选择项。
 *
 * @param cronExpression 已保存的 Cron 表达式
 */
function applyCronExpressionToBuilder(cronExpression: string): void {
  const expression = cronExpression.trim();
  const everyMinutesMatch = expression.match(/^0 0\/([1-9]\d?) \* \* \* \?$/);
  if (everyMinutesMatch) {
    cronBuilderMode.value = 'everyMinutes';
    cronBuilderState.intervalMinutes = normalizeNumber(Number(everyMinutesMatch[1]), 1, 59, 5);
    syncCronExpressionFromBuilder();
    return;
  }

  const hourlyMatch = expression.match(/^0 ([0-5]?\d) \* \* \* \?$/);
  if (hourlyMatch) {
    cronBuilderMode.value = 'hourly';
    cronBuilderState.minute = normalizeNumber(Number(hourlyMatch[1]), 0, 59, 0);
    syncCronExpressionFromBuilder();
    return;
  }

  const dailyMatch = expression.match(/^0 ([0-5]?\d) ([01]?\d|2[0-3]) \* \* \?$/);
  if (dailyMatch) {
    cronBuilderMode.value = 'daily';
    cronBuilderState.minute = normalizeNumber(Number(dailyMatch[1]), 0, 59, 0);
    cronBuilderState.hour = normalizeNumber(Number(dailyMatch[2]), 0, 23, 2);
    syncCronExpressionFromBuilder();
    return;
  }

  const weeklyMatch = expression.match(/^0 ([0-5]?\d) ([01]?\d|2[0-3]) \? \* (MON|TUE|WED|THU|FRI|SAT|SUN)$/);
  if (weeklyMatch) {
    cronBuilderMode.value = 'weekly';
    cronBuilderState.minute = normalizeNumber(Number(weeklyMatch[1]), 0, 59, 0);
    cronBuilderState.hour = normalizeNumber(Number(weeklyMatch[2]), 0, 23, 2);
    cronBuilderState.weekday = weeklyMatch[3];
    syncCronExpressionFromBuilder();
    return;
  }

  const monthlyMatch = expression.match(/^0 ([0-5]?\d) ([01]?\d|2[0-3]) ([1-9]|1\d|2[0-8]) \* \?$/);
  if (monthlyMatch) {
    cronBuilderMode.value = 'monthly';
    cronBuilderState.minute = normalizeNumber(Number(monthlyMatch[1]), 0, 59, 0);
    cronBuilderState.hour = normalizeNumber(Number(monthlyMatch[2]), 0, 23, 2);
    cronBuilderState.dayOfMonth = normalizeNumber(Number(monthlyMatch[3]), 1, 28, 1);
    syncCronExpressionFromBuilder();
    return;
  }

  cronBuilderMode.value = 'custom';
  formState.cronExpression = expression;
}

/**
 * 规整数值边界。
 *
 * @param value 原始数值
 * @param min 最小值
 * @param max 最大值
 * @param defaultValue 兜底值
 * @returns 合法数值
 */
function normalizeNumber(value: number | null | undefined, min: number, max: number, defaultValue: number): number {
  const numberValue = Number(value);
  if (!Number.isFinite(numberValue)) {
    return defaultValue;
  }
  return Math.min(max, Math.max(min, Math.trunc(numberValue)));
}

/**
 * 规整周几编码。
 *
 * @param value 周几编码
 * @returns Quartz Cron 周几编码
 */
function normalizeWeekday(value: string): string {
  return weekdayOptions.some((item) => item.value === value) ? value : 'MON';
}

/**
 * 获取周几中文名称。
 *
 * @param value 周几编码
 * @returns 中文名称
 */
function weekdayLabel(value: string): string {
  return weekdayOptions.find((item) => item.value === value)?.label ?? '周一';
}

/**
 * 格式化两位数。
 *
 * @param value 原始数值
 * @returns 两位数字符串
 */
function padNumber(value: number): string {
  return String(normalizeNumber(value, 0, 99, 0)).padStart(2, '0');
}

/**
 * 校验作业任务表单。
 *
 * @returns 表单是否满足提交条件
 */
function validateForm(): boolean {
  if (!formState.jobCode.trim()) {
    message.warning('请输入任务编码');
    return false;
  }
  if (!formState.jobName.trim()) {
    message.warning('请输入任务名称');
    return false;
  }
  syncCronExpressionFromBuilder();
  if (!formState.cronExpression.trim()) {
    message.warning('请选择执行周期');
    return false;
  }
  if (!formState.handlerName.trim()) {
    message.warning('请输入处理器名称');
    return false;
  }
  return true;
}

/**
 * 启用或停用作业任务。
 *
 * @param task 作业任务
 */
async function toggleStatus(task: JobTaskRecord): Promise<void> {
  const nextStatus = task.status === 'enabled' ? 'disabled' : 'enabled';
  try {
    await changeJobTaskStatus(task.id, nextStatus);
    message.success('作业状态已更新');
    await loadTasks();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '作业状态更新失败');
  }
}

/**
 * 手动触发作业任务。
 *
 * @param task 作业任务
 */
async function triggerTask(task: JobTaskRecord): Promise<void> {
  try {
    await triggerJobTask(task.id);
    message.success('作业任务已触发');
    await openLogs(task);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '作业任务触发失败');
  }
}

/**
 * 打开作业执行日志抽屉。
 *
 * @param task 作业任务
 */
async function openLogs(task: JobTaskRecord): Promise<void> {
  logDrawerOpen.value = true;
  logLoading.value = true;
  logErrorMessage.value = '';
  try {
    logs.value = await listJobTaskLogs(task.id);
  } catch (error) {
    logs.value = [];
    logErrorMessage.value = error instanceof Error ? error.message : '作业日志加载失败';
    message.error(logErrorMessage.value);
  } finally {
    logLoading.value = false;
  }
}

onMounted(() => {
  void loadTasks();
});
</script>

<style scoped>
.job-page {
  min-width: 0;
}

.state-alert {
  margin-bottom: 12px;
}

.status-select {
  width: 130px;
}

.full-input {
  width: 100%;
}

.cron-builder-panel {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.cron-builder-panel :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

.cron-alert {
  margin: 10px 0 14px;
}

.permission-code {
  display: none;
}
</style>
