<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="lowcode-datasource-page">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="10">
        <a-card title="数据源管理" :bordered="false">
          <template #extra>
            <a-space>
              <a-input v-model:value="tenantId" class="tenant-id" />
              <a-button :loading="loading" @click="loadDataSources">刷新</a-button>
            </a-space>
          </template>

          <a-table
            row-key="code"
            :columns="columns"
            :data-source="dataSources"
            :loading="loading"
            :pagination="$tablePagination"
            :custom-row="buildDataSourceRowProps"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'dialect'">
                {{ formatLowcodeDialectLabel(record.dialect) }}
              </template>
              <template v-if="column.key === 'enabled'">
                <a-tag :color="record.enabled ? 'green' : 'default'">
                  {{ record.enabled ? '启用' : '停用' }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="14">
        <a-card title="数据源配置" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="resetCommand">新建数据源</a-button>
              <a-button :loading="testing" @click="handleTestConnection">连接测试</a-button>
              <a-button type="primary" :loading="saving" @click="handleSave">保存数据源</a-button>
            </a-space>
          </template>

          <a-alert
            message="数据库口令只填写密钥引用，不在页面和接口中传输明文口令。"
            type="info"
            show-icon
          />

          <a-form class="datasource-form" layout="vertical" :model="command">
            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <a-form-item label="数据源编码" required>
                  <a-input v-model:value="command.code" placeholder="main" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="数据源名称" required>
                  <a-input v-model:value="command.name" placeholder="主数据源" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="数据库方言" required>
                  <a-select
                    v-model:value="command.dialect"
                    :loading="dialectLoading"
                    :options="dialectOptions"
                    @change="handleDialectChange"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="用户名" required>
                  <a-input v-model:value="command.username" placeholder="platform_user" />
                </a-form-item>
              </a-col>
              <a-col :xs="24">
                <a-form-item label="JDBC 地址" required>
                  <a-input
                    :value="command.jdbcUrl"
                    :placeholder="currentJdbcUrlTemplate"
                    @update:value="handleJdbcUrlValueUpdate"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24">
                <a-form-item label="口令密钥引用" required>
                  <a-select
                    v-model:value="command.passwordSecretRef"
                    :loading="secretOptionsLoading"
                    :options="passwordSecretSelectOptions"
                    allow-clear
                    show-search
                    option-filter-prop="label"
                    placeholder="请选择启用的数据库口令密钥"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="启用状态">
                  <a-switch v-model:checked="command.enabled" checked-children="启用" un-checked-children="停用" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <span class="permission-code">lowcode:datasource:save</span>
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';

import {
  getLowcodeDataSource,
  listLowcodeDataSources,
  listLowcodePasswordSecretOptions,
  saveLowcodeDataSource,
  testLowcodeDataSourceConnection,
  type LowcodeDataSourceResponse,
  type LowcodeDataSourceSaveRequest,
  type LowcodePasswordSecretOption,
  type LowcodeDatabaseDialect,
} from '@/api/lowcode/datasource';
import {
  DEFAULT_LOWCODE_DIALECT_CAPABILITIES,
  buildSupportedLowcodeDialectCodes,
  formatLowcodeDialectLabel,
  listLowcodeDialectCapabilities,
  type LowcodeDialectCapabilitiesResponse,
} from '@/api/lowcode/dialect';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 数据源列表。 */
const dataSources = ref<LowcodeDataSourceResponse[]>([]);
/** 数据库口令密钥选项。 */
const passwordSecretOptions = ref<LowcodePasswordSecretOption[]>([]);
/** 数据库口令密钥下拉项；在编辑态保留当前引用可回显。 */
const passwordSecretSelectOptions = computed(() => {
  const normalizedRef = command.passwordSecretRef.trim();
  if (!normalizedRef || passwordSecretOptions.value.some((option) => option.value === normalizedRef)) {
    return passwordSecretOptions.value;
  }
  return [
    {
      label: `${normalizedRef}（当前引用）`,
      value: normalizedRef,
      secretCode: normalizedRef.startsWith('secret:') ? normalizedRef.slice('secret:'.length) : normalizedRef,
      secretName: '当前引用',
      secretKind: 'db_password',
    },
    ...passwordSecretOptions.value,
  ];
});
/** 列表加载状态。 */
const loading = ref(false);
/** 密钥选项加载状态。 */
const secretOptionsLoading = ref(false);
/** 保存提交状态。 */
const saving = ref(false);
/** 连接测试提交状态。 */
const testing = ref(false);
/** 数据库方言能力加载状态。 */
const dialectLoading = ref(false);
/** JDBC 地址是否已经被用户手动修改。 */
const jdbcUrlManuallyEdited = ref(false);
/** 上一次选择的数据库方言，用于判断是否仍可安全替换模板地址。 */
const previousDialect = ref<LowcodeDatabaseDialect>('mysql');

/**
 * JDBC 地址模板定义。
 */
interface JdbcUrlTemplate {
  /** 数据库方言编码。 */
  dialect: string;
  /** 默认 JDBC 地址，可作为表单自动带出值。 */
  url: string;
}

/** 不同数据库方言对应的 JDBC 默认地址模板。 */
const jdbcUrlTemplates: JdbcUrlTemplate[] = [
  {
    dialect: 'mysql',
    url: 'jdbc:mysql://127.0.0.1:3306/zhyc-base-v1?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai',
  },
  {
    dialect: 'postgresql',
    url: 'jdbc:postgresql://127.0.0.1:5432/zhyc-base-v1',
  },
  {
    dialect: 'oracle',
    url: 'jdbc:oracle:thin:@127.0.0.1:1521:ORCL',
  },
  {
    dialect: 'sqlserver',
    url: 'jdbc:sqlserver://127.0.0.1:1433;databaseName=zhyc-base-v1;encrypt=false',
  },
  {
    dialect: 'dm',
    url: 'jdbc:dm://127.0.0.1:5236/zhyc-base-v1',
  },
];

/** 数据源权限编码，供页面按钮和后续权限指令统一引用。 */
const permissionCodes = {
  /** 保存数据源权限。 */
  save: 'lowcode:datasource:save',
  /** 测试数据源连接权限。 */
  test: 'lowcode:datasource:test',
};

/** 数据库方言下拉选项，默认保留 MySQL 兜底能力。 */
const dialectOptions = ref<Array<{ label: string; value: LowcodeDatabaseDialect }>>(
  buildDialectOptions(DEFAULT_LOWCODE_DIALECT_CAPABILITIES),
);

/** 数据源列表列定义。 */
const columns = [
  { title: '编码', dataIndex: 'code', key: 'code', width: 140 },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '方言', dataIndex: 'dialect', key: 'dialect', width: 130 },
  { title: '状态', dataIndex: 'enabled', key: 'enabled', width: 100 },
];

/** 当前编辑的数据源命令。 */
const command = reactive<LowcodeDataSourceSaveRequest>(createEmptyCommand());

/** 当前数据库方言对应的 JDBC 地址模板。 */
const currentJdbcUrlTemplate = computed(() => buildJdbcUrlTemplate(command.dialect));

/**
 * 创建空数据源保存命令。
 *
 * @returns 数据源保存命令
 */
function createEmptyCommand(): LowcodeDataSourceSaveRequest {
  const dialect: LowcodeDatabaseDialect = 'mysql';
  return {
    tenantId: tenantId.value,
    code: '',
    name: '',
    dialect,
    jdbcUrl: buildJdbcUrlTemplate(dialect),
    username: '',
    passwordSecretRef: '',
    enabled: true,
  };
}

/**
 * 将数据源响应复制到编辑命令。
 *
 * @param dataSource 数据源响应
 */
function applyDataSource(dataSource: LowcodeDataSourceResponse): void {
  command.tenantId = dataSource.tenantId;
  command.code = dataSource.code;
  command.name = dataSource.name;
  command.dialect = dataSource.dialect;
  command.jdbcUrl = dataSource.jdbcUrl;
  command.username = dataSource.username;
  command.passwordSecretRef = dataSource.passwordSecretRef ?? '';
  command.enabled = dataSource.enabled;
  previousDialect.value = dataSource.dialect;
  jdbcUrlManuallyEdited.value = true;
}

/**
 * 重置数据源编辑表单。
 */
function resetCommand(): void {
  Object.assign(command, createEmptyCommand());
  previousDialect.value = command.dialect;
  jdbcUrlManuallyEdited.value = false;
}

/**
 * 处理数据库方言切换。
 *
 * <p>仅当 JDBC 地址为空、仍是旧方言模板，或用户尚未手动编辑时自动替换；用户已修改的地址不会被覆盖。</p>
 *
 * @param nextDialect 新选择的数据库方言编码
 */
function handleDialectChange(nextDialect: LowcodeDatabaseDialect): void {
  const previousTemplate = buildJdbcUrlTemplate(previousDialect.value);
  const currentJdbcUrl = command.jdbcUrl.trim();
  const canAutoFill = !jdbcUrlManuallyEdited.value || !currentJdbcUrl || currentJdbcUrl === previousTemplate;
  command.dialect = nextDialect;
  if (canAutoFill) {
    command.jdbcUrl = buildJdbcUrlTemplate(nextDialect);
    jdbcUrlManuallyEdited.value = false;
  }
  previousDialect.value = nextDialect;
}

/**
 * 处理 JDBC 地址输入。
 *
 * @param value 用户输入的 JDBC 地址
 */
function handleJdbcUrlValueUpdate(value: string): void {
  command.jdbcUrl = value;
  jdbcUrlManuallyEdited.value = command.jdbcUrl.trim() !== buildJdbcUrlTemplate(command.dialect);
}

/**
 * 加载数据源列表。
 */
async function loadDataSources(): Promise<void> {
  loading.value = true;
  try {
    const currentTenantId = syncTenantContext();
    dataSources.value = await listLowcodeDataSources(currentTenantId);
    await loadPasswordSecretOptions(currentTenantId);
  } finally {
    loading.value = false;
  }
}

/**
 * 加载当前租户启用的数据库口令密钥。
 *
 * @param currentTenantId 当前租户编码
 */
async function loadPasswordSecretOptions(currentTenantId = syncTenantContext()): Promise<void> {
  secretOptionsLoading.value = true;
  try {
    passwordSecretOptions.value = await listLowcodePasswordSecretOptions(currentTenantId);
  } catch {
    passwordSecretOptions.value = [];
    message.warning('数据库口令密钥选项加载失败，已保留当前引用');
  } finally {
    secretOptionsLoading.value = false;
  }
}

/**
 * 加载当前平台已注册的数据库方言能力。
 */
async function loadDialectCapabilities(): Promise<void> {
  dialectLoading.value = true;
  try {
    const capabilities = await listLowcodeDialectCapabilities();
    dialectOptions.value = buildDialectOptions(capabilities);
  } catch {
    dialectOptions.value = buildDialectOptions(DEFAULT_LOWCODE_DIALECT_CAPABILITIES);
    message.warning('数据库方言能力加载失败，已使用主流数据库兜底选项');
  } finally {
    dialectLoading.value = false;
  }
}

/**
 * 根据方言能力清单构建数据源方言选项。
 *
 * @param capabilities 低代码数据库方言能力响应
 * @returns 数据库方言下拉选项
 */
function buildDialectOptions(
  capabilities: LowcodeDialectCapabilitiesResponse,
): Array<{ label: string; value: LowcodeDatabaseDialect }> {
  const availableCodes = buildSupportedLowcodeDialectCodes(capabilities);
  const uniqueCodes = Array.from(new Set([
    ...DEFAULT_LOWCODE_DIALECT_CAPABILITIES.ddlDialectCodes,
    ...availableCodes,
  ]));
  return uniqueCodes.map((code) => ({
    label: formatLowcodeDialectLabel(code),
    value: code,
  }));
}

/**
 * 根据数据库方言构建 JDBC 默认地址。
 *
 * @param dialect 数据库方言编码
 * @returns JDBC 地址模板
 */
function buildJdbcUrlTemplate(dialect: string): string {
  const normalizedDialect = dialect.trim().toLowerCase();
  return jdbcUrlTemplates.find((template) => template.dialect === normalizedDialect)?.url
    ?? `jdbc:${normalizedDialect}://127.0.0.1:3306/zhyc-base-v1`;
}

/**
 * 保存数据源定义。
 */
async function handleSave(): Promise<void> {
  if (!command.passwordSecretRef.trim()) {
    message.error('请选择数据库口令密钥');
    return;
  }
  saving.value = true;
  try {
    command.tenantId = syncTenantContext();
    command.passwordSecretRef = command.passwordSecretRef.trim();
    const saved = await saveLowcodeDataSource(command);
    applyDataSource(saved);
    await loadDataSources();
    message.success('数据源已保存');
  } finally {
    saving.value = false;
  }
}

/**
 * 测试当前数据源连接配置。
 */
async function handleTestConnection(): Promise<void> {
  if (!command.code) {
    message.error('请先选择或填写数据源编码');
    return;
  }
  testing.value = true;
  try {
    command.tenantId = syncTenantContext();
    const result = await testLowcodeDataSourceConnection({
      tenantId: command.tenantId,
      code: command.code,
    });
    if (result.success) {
      message.success('数据源连接测试通过');
      return;
    }
    message.warning(result.message);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '数据源连接测试失败');
  } finally {
    testing.value = false;
  }
}

/**
 * 构建数据源表格行属性。
 *
 * @param record 数据源响应
 * @returns 表格行属性
 */
function buildDataSourceRowProps(record: LowcodeDataSourceResponse): { onClick: () => void } {
  return {
    onClick: () => {
      void handleSelectDataSource(record);
    },
  };
}

/**
 * 选择数据源并加载详情回填编辑表单。
 *
 * <p>列表数据可能因缓存或旧接口缺少口令密钥引用，编辑态必须优先读取详情再回填。</p>
 *
 * @param record 数据源列表行
 */
async function handleSelectDataSource(record: LowcodeDataSourceResponse): Promise<void> {
  try {
    const detail = await getLowcodeDataSource(record.tenantId, record.code);
    applyDataSource(detail);
  } catch {
    applyDataSource(record);
    message.warning('数据源详情加载失败，已使用列表数据回填');
  }
}

/**
 * 同步后台租户到数据源请求参数。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  command.tenantId = currentTenantId;
  return currentTenantId;
}

onMounted(() => {
  void loadDialectCapabilities();
  void loadDataSources();
});
</script>

<style scoped>
.lowcode-datasource-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.datasource-form {
  margin-top: 16px;
}

.permission-code {
  display: none;
}
</style>
