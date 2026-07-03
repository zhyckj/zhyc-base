<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="ai-core-page">
    <a-card :bordered="false">
      <template #title>
        <a-space>
          <span>{{ activeModuleTitle }}</span>
          <a-tag color="blue">AI 能力中心</a-tag>
        </a-space>
      </template>
      <template #extra>
        <a-space>
          <a-input v-model:value="tenantId" class="tenant-input" />
          <a-button :loading="loading" @click="loadActiveTab">刷新</a-button>
        </a-space>
      </template>

      <template v-if="activeTab === 'providers'">
        <div class="provider-workbench">
          <section class="provider-list-panel">
            <div class="provider-panel-header">
              <div class="provider-panel-title">
                <h3>供应商接入</h3>
                <p>统一维护大模型供应商、访问地址和密钥引用。</p>
              </div>
              <a-tag color="blue">{{ providerStats.total }} 个</a-tag>
            </div>
            <div class="provider-summary-row">
              <div>
                <strong>{{ providerStats.enabled }}</strong>
                <span>启用</span>
              </div>
              <div>
                <strong>{{ providerStats.disabled }}</strong>
                <span>停用</span>
              </div>
              <div>
                <strong>{{ providerStats.currentType }}</strong>
                <span>当前类型</span>
              </div>
            </div>
            <a-table
              class="provider-table"
              :row-key="buildProviderRowKey"
              size="small"
              :columns="providerColumns"
              :data-source="providers"
              :loading="loading"
              :pagination="$tablePagination"
              :scroll="{ x: 820 }"
              :custom-row="buildProviderRowProps"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'provider'">
                  <div class="provider-code-cell">
                    <strong>{{ record.providerCode }}</strong>
                    <span>{{ record.providerName }}</span>
                  </div>
                </template>
                <template v-else-if="column.key === 'providerType'">
                  <a-tag color="blue">{{ providerTypeText(record.providerType) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'baseUrl'">
                  <span class="provider-url-text" :title="record.baseUrl">{{ record.baseUrl }}</span>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="enabledStatusColor(record.status)">{{ enabledStatusText(record.status) }}</a-tag>
                </template>
              </template>
            </a-table>
          </section>

          <section class="provider-editor-panel">
            <div class="provider-panel-header">
              <div class="provider-panel-title">
                <h3>{{ providerCommand.providerCode ? '编辑供应商' : '新建供应商' }}</h3>
                <p>选择供应商类型后会自动带出默认基础地址，仍可手动调整。</p>
              </div>
              <a-tag :color="enabledStatusColor(providerCommand.status)">
                {{ enabledStatusText(providerCommand.status) }}
              </a-tag>
            </div>
            <a-form class="provider-form" layout="vertical" :model="providerCommand">
              <div class="provider-form-grid">
                <a-form-item label="供应商编码" required>
                  <a-input v-model:value="providerCommand.providerCode" allow-clear placeholder="openai-main" />
                </a-form-item>
                <a-form-item label="供应商名称" required>
                  <a-input v-model:value="providerCommand.providerName" allow-clear placeholder="OpenAI 兼容服务" />
                </a-form-item>
                <a-form-item label="供应商类型" required>
                  <a-select
                    v-model:value="providerCommand.providerType"
                    :options="providerTypeOptions"
                    @change="handleProviderTypeChange"
                  />
                </a-form-item>
                <a-form-item label="状态" required>
                  <a-select v-model:value="providerCommand.status" :options="enabledStatusOptions" />
                </a-form-item>
              </div>
              <a-form-item label="基础地址" required>
                <a-input
                  v-model:value="providerCommand.baseUrl"
                  allow-clear
                  placeholder="选择供应商类型后自动带出，可手动修改"
                  @change="markProviderBaseUrlTouched"
                />
              </a-form-item>
              <a-form-item label="密钥引用" required>
                <a-select
                  v-model:value="providerCommand.secretRef"
                  show-search
                  allow-clear
                  option-filter-prop="label"
                  :loading="providerSecretLoading"
                  :options="providerSecretSelectOptions"
                  placeholder="请选择启用的系统密钥"
                />
              </a-form-item>
              <div class="provider-editor-actions">
                <a-button @click="resetProvider">新建</a-button>
                <a-button type="primary" :loading="saving" @click="saveProvider">保存供应商</a-button>
                <a-button :loading="providerTesting" @click="testProvider">测试供应商</a-button>
              </div>
              <div
                v-if="providerTestResult"
                class="provider-test-panel"
                :class="providerTestResult.success ? 'is-success' : 'is-error'"
              >
                <div>
                  <strong>{{ providerTestResult.success ? '供应商可用' : '供应商不可用' }}</strong>
                  <span>{{ providerTestResult.providerCode }} · {{ providerTestResult.latencyMs }} ms</span>
                </div>
                <p>{{ providerTestResult.message }}</p>
              </div>
            </a-form>
          </section>
        </div>
      </template>

      <template v-else-if="activeTab === 'models'">
        <div class="model-workbench">
          <section class="model-list-panel">
            <div class="model-panel-header">
              <div class="model-panel-title">
                <h3>模型配置</h3>
                <p>维护可供应用接入使用的模型编码、能力和上下文窗口。</p>
              </div>
              <a-tag color="blue">{{ modelStats.total }} 个</a-tag>
            </div>
            <div class="model-summary-row">
              <div>
                <strong>{{ modelStats.enabled }}</strong>
                <span>启用</span>
              </div>
              <div>
                <strong>{{ modelStats.stream }}</strong>
                <span>流式输出</span>
              </div>
              <div>
                <strong>{{ modelStats.tool }}</strong>
                <span>工具调用</span>
              </div>
            </div>
            <a-table
              class="model-table"
              :row-key="buildModelRowKey"
              size="small"
              :columns="modelColumns"
              :data-source="models"
              :loading="loading"
              :pagination="$tablePagination"
              :scroll="{ x: 920 }"
              :custom-row="buildModelRowProps"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'model'">
                  <div class="model-code-cell">
                    <strong>{{ record.modelCode }}</strong>
                    <span>{{ record.modelName }}</span>
                  </div>
                </template>
                <template v-else-if="column.key === 'providerId'">
                  <span class="model-provider-text">{{ modelProviderText(record.providerId) }}</span>
                </template>
                <template v-else-if="column.key === 'modelType'">
                  <a-tag :color="modelTypeColor(record.modelType)">{{ modelTypeText(record.modelType) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'capabilities'">
                  <div class="model-capability-list">
                    <a-tag v-if="record.supportStream" color="blue">流式</a-tag>
                    <a-tag v-if="record.supportTool" color="purple">工具</a-tag>
                    <span v-if="!record.supportStream && !record.supportTool" class="muted-text">基础对话</span>
                  </div>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="enabledStatusColor(record.status)">{{ enabledStatusText(record.status) }}</a-tag>
                </template>
              </template>
            </a-table>
          </section>

          <section class="model-editor-panel">
            <div class="model-panel-header">
              <div class="model-panel-title">
                <h3>{{ modelCommand.modelCode ? '编辑模型' : '新建模型' }}</h3>
                <p>模型编码需与供应商接口中的实际模型标识保持一致。</p>
              </div>
              <a-tag :color="enabledStatusColor(modelCommand.status)">
                {{ enabledStatusText(modelCommand.status) }}
              </a-tag>
            </div>
            <a-form class="model-form" layout="vertical" :model="modelCommand">
              <a-form-item label="供应商" required>
                <a-select
                  v-model:value="modelCommand.providerId"
                  show-search
                  allow-clear
                  option-filter-prop="label"
                  :loading="modelProviderLoading"
                  :options="modelProviderOptions"
                  placeholder="请选择供应商"
                />
              </a-form-item>
              <div class="model-form-grid">
                <a-form-item label="模型编码" required>
                  <a-input v-model:value="modelCommand.modelCode" allow-clear placeholder="gpt-4.1-mini" />
                </a-form-item>
                <a-form-item label="模型名称" required>
                  <a-input v-model:value="modelCommand.modelName" allow-clear placeholder="GPT-4.1 Mini" />
                </a-form-item>
                <a-form-item label="模型类型" required>
                  <a-select v-model:value="modelCommand.modelType" :options="modelTypeOptions" />
                </a-form-item>
                <a-form-item label="上下文长度" required>
                  <a-input-number v-model:value="modelCommand.contextWindow" class="full-width" :min="1" />
                </a-form-item>
              </div>
              <div class="model-capability-panel">
                <span>模型能力</span>
                <div>
                  <a-checkbox v-model:checked="modelCommand.supportStream">流式输出</a-checkbox>
                  <a-checkbox v-model:checked="modelCommand.supportTool">工具调用</a-checkbox>
                </div>
              </div>
              <a-form-item label="状态" required>
                <a-select v-model:value="modelCommand.status" :options="enabledStatusOptions" />
              </a-form-item>
              <div class="model-editor-actions">
                <a-button @click="resetModel">新建</a-button>
                <a-button type="primary" :loading="saving" @click="saveModel">保存模型</a-button>
              </div>
            </a-form>
          </section>
        </div>
      </template>

      <template v-else-if="activeTab === 'apps'">
        <div class="app-workbench">
          <section class="app-list-panel">
            <div class="app-panel-header">
              <div class="app-panel-title">
                <h3>应用接入</h3>
                <p>为业务场景绑定默认模型、系统提示词和每日令牌额度。</p>
              </div>
              <a-tag color="blue">{{ appStats.total }} 个</a-tag>
            </div>
            <div class="app-summary-row">
              <div>
                <strong>{{ appStats.enabled }}</strong>
                <span>启用</span>
              </div>
              <div>
                <strong>{{ appStats.disabled }}</strong>
                <span>停用</span>
              </div>
              <div>
                <strong>{{ formatNumber(appStats.totalQuota) }}</strong>
                <span>总额度</span>
              </div>
            </div>
            <a-table
              class="app-table"
              :row-key="buildAppRowKey"
              size="small"
              :columns="appColumns"
              :data-source="apps"
              :loading="loading"
              :pagination="$tablePagination"
              :scroll="{ x: 860 }"
              :custom-row="buildAppRowProps"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'app'">
                  <div class="app-code-cell">
                    <strong>{{ record.appCode }}</strong>
                    <span>{{ record.appName }}</span>
                  </div>
                </template>
                <template v-else-if="column.key === 'defaultModel'">
                  <span class="app-model-text">{{ appDefaultModelText(record.defaultModelId) }}</span>
                </template>
                <template v-else-if="column.key === 'dailyTokenQuota'">
                  {{ formatNumber(record.dailyTokenQuota) }}
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="enabledStatusColor(record.status)">{{ enabledStatusText(record.status) }}</a-tag>
                </template>
              </template>
            </a-table>
          </section>

          <section class="app-editor-panel">
            <div class="app-panel-header">
              <div class="app-panel-title">
                <h3>{{ appCommand.appCode ? '编辑应用' : '新建应用' }}</h3>
                <p>应用用于承载业务场景的模型、提示词和调用额度。</p>
              </div>
              <a-tag :color="enabledStatusColor(appCommand.status)">
                {{ enabledStatusText(appCommand.status) }}
              </a-tag>
            </div>
            <a-form class="app-form" layout="vertical" :model="appCommand">
              <div class="app-form-grid">
                <a-form-item label="应用编码" required>
                  <a-input v-model:value="appCommand.appCode" allow-clear placeholder="contract-assistant" />
                </a-form-item>
                <a-form-item label="应用名称" required>
                  <a-input v-model:value="appCommand.appName" allow-clear placeholder="合同助手" />
                </a-form-item>
                <a-form-item label="默认模型" required>
                  <a-select
                    v-model:value="appCommand.defaultModelId"
                    show-search
                    option-filter-prop="label"
                    :options="appModelOptions"
                    placeholder="请选择默认模型"
                  />
                </a-form-item>
                <a-form-item label="每日令牌额度" required>
                  <a-input-number v-model:value="appCommand.dailyTokenQuota" class="full-width" :min="1" />
                </a-form-item>
              </div>
              <a-form-item label="系统提示词" required>
                <a-textarea
                  v-model:value="appCommand.systemPrompt"
                  class="app-system-prompt"
                  :rows="5"
                  placeholder="请输入应用级系统提示词"
                />
              </a-form-item>
              <a-form-item label="状态" required>
                <a-select v-model:value="appCommand.status" :options="enabledStatusOptions" />
              </a-form-item>
              <div class="app-editor-actions">
                <a-button @click="resetApp">新建</a-button>
                <a-button type="primary" :loading="saving" @click="saveApp">保存应用</a-button>
              </div>
            </a-form>

            <div class="runtime-panel">
              <div class="app-panel-header">
                <div class="app-panel-title">
                  <h3>测试调用</h3>
                  <p>基于当前应用快速验证模型和提示词渲染结果。</p>
                </div>
              </div>
              <a-form layout="vertical" :model="runtimeTestCommand">
                <div class="app-form-grid">
                  <a-form-item label="应用编码" required>
                    <a-select
                      v-model:value="runtimeTestCommand.appCode"
                      show-search
                      allow-clear
                      option-filter-prop="label"
                      :options="runtimeAppOptions"
                      placeholder="请选择应用"
                    />
                  </a-form-item>
                  <a-form-item label="提示词" required>
                    <a-select
                      v-model:value="runtimePromptSelection"
                      show-search
                      allow-clear
                      option-filter-prop="label"
                      :options="runtimePromptOptions"
                      placeholder="请选择提示词"
                    />
                  </a-form-item>
                </div>
                <a-form-item label="变量 JSON">
                  <a-textarea
                    v-model:value="runtimeTestCommand.variablesJson"
                    class="runtime-json-editor"
                    :rows="5"
                    placeholder='{"content":"采购电脑 3 台"}'
                  />
                </a-form-item>
                <div class="app-editor-actions">
                  <a-button type="primary" :loading="runtimeTesting" @click="testRuntimeChat">测试调用</a-button>
                </div>
                <template v-if="runtimeTestResult">
                  <a-descriptions class="runtime-result-meta" size="small" bordered :column="2">
                    <a-descriptions-item label="供应商">{{ runtimeTestResult.providerCode }}</a-descriptions-item>
                    <a-descriptions-item label="模型">{{ runtimeTestResult.modelCode }}</a-descriptions-item>
                    <a-descriptions-item label="Token">{{ runtimeTestResult.totalTokens }}</a-descriptions-item>
                    <a-descriptions-item label="耗时">{{ runtimeTestResult.latencyMs }} ms</a-descriptions-item>
                  </a-descriptions>
                  <a-textarea class="runtime-result-content" :value="runtimeTestResult.content" :rows="5" readonly />
                </template>
              </a-form>
            </div>
          </section>
        </div>
      </template>

      <template v-else-if="activeTab === 'prompts'">
        <div class="prompt-workbench">
          <section class="prompt-list-panel">
            <div class="prompt-panel-header">
              <div class="prompt-panel-title">
                <h3>提示词模板</h3>
                <p>沉淀可复用业务提示词，点击列表项后在右侧编辑。</p>
              </div>
              <a-tag color="blue">{{ promptStats.total }} 条</a-tag>
            </div>
            <div class="prompt-summary-row">
              <div>
                <strong>{{ promptStats.published }}</strong>
                <span>已发布</span>
              </div>
              <div>
                <strong>{{ promptStats.draft }}</strong>
                <span>草稿</span>
              </div>
              <div>
                <strong>{{ promptStats.disabled }}</strong>
                <span>停用</span>
              </div>
            </div>
            <a-table
              class="prompt-table"
              :row-key="buildPromptRowKey"
              size="small"
              :columns="promptColumns"
              :data-source="prompts"
              :loading="loading"
              :pagination="false"
              :scroll="{ x: 760, y: 520 }"
              :custom-row="buildPromptRowProps"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'prompt'">
                  <div class="prompt-code-cell">
                    <strong>{{ record.promptCode }}</strong>
                    <span>{{ record.promptName }}</span>
                  </div>
                </template>
                <template v-else-if="column.key === 'variables'">
                  <div class="variable-tag-list">
                    <a-tag v-for="variable in splitVariables(record.variables)" :key="variable">{{ variable }}</a-tag>
                    <span v-if="splitVariables(record.variables).length === 0" class="muted-text">无变量</span>
                  </div>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="promptStatusColor(record.status)">{{ promptStatusText(record.status) }}</a-tag>
                </template>
              </template>
            </a-table>
          </section>

          <section class="prompt-editor-panel">
            <div class="prompt-panel-header">
              <div class="prompt-panel-title">
                <h3>{{ promptCommand.promptCode ? '编辑提示词' : '新建提示词' }}</h3>
                <p>模板变量使用 <code v-pre>{{变量名}}</code> 引用，变量清单用英文逗号分隔。</p>
              </div>
              <a-tag :color="promptStatusColor(promptCommand.status)">
                {{ promptStatusText(promptCommand.status) }}
              </a-tag>
            </div>
            <a-form class="prompt-form" layout="vertical" :model="promptCommand">
              <div class="prompt-form-grid">
                <a-form-item label="提示词编码" required>
                  <a-input v-model:value="promptCommand.promptCode" allow-clear placeholder="summary" />
                </a-form-item>
                <a-form-item label="提示词名称" required>
                  <a-input v-model:value="promptCommand.promptName" allow-clear placeholder="摘要生成" />
                </a-form-item>
                <a-form-item label="版本" required>
                  <a-input v-model:value="promptCommand.version" allow-clear placeholder="v1" />
                </a-form-item>
                <a-form-item label="状态" required>
                  <a-select v-model:value="promptCommand.status" :options="promptStatusOptions" />
                </a-form-item>
              </div>
              <a-form-item label="变量清单">
                <a-input v-model:value="promptCommand.variables" allow-clear placeholder="content,language" />
              </a-form-item>
              <div class="prompt-variable-preview">
                <span>变量预览</span>
                <div>
                  <a-tag v-for="variable in promptVariablePreview" :key="variable" color="blue">{{ variable }}</a-tag>
                  <span v-if="promptVariablePreview.length === 0" class="muted-text">暂无变量</span>
                </div>
              </div>
              <a-form-item label="模板内容" required>
                <a-textarea
                  v-model:value="promptCommand.templateContent"
                  class="prompt-template-editor"
                  :rows="12"
                  placeholder="请输入提示词模板，例如：请基于 {{content}} 生成 {{language}} 摘要。"
                />
              </a-form-item>
              <div class="prompt-editor-actions">
                <a-button @click="resetPrompt">新建</a-button>
                <a-button type="primary" :loading="saving" @click="savePrompt">保存提示词</a-button>
              </div>
            </a-form>
          </section>
        </div>
      </template>

      <template v-else>
        <a-space class="audit-toolbar">
          <a-select
            v-model:value="auditAppCode"
            class="audit-app-select"
            show-search
            allow-clear
            option-filter-prop="label"
            :loading="auditAppLoading"
            :options="auditAppOptions"
            placeholder="请选择应用编码"
            @change="handleAuditAppChange"
          />
          <a-button :loading="loading" @click="loadAudits">查询审计</a-button>
        </a-space>
        <a-table
          row-key="traceId"
          size="small"
          :columns="auditColumns"
          :data-source="audits"
          :loading="loading"
          :pagination="$tablePagination"
        />
      </template>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { message } from 'ant-design-vue';

import {
  chatWithAiRuntime,
  listAiApps,
  listAiInvocationAudits,
  listAiModels,
  listAiPrompts,
  listAiProviders,
  saveAiApp,
  saveAiModel,
  saveAiPrompt,
  saveAiProvider,
  testAiProvider,
  type AiAppResponse,
  type AiAppSaveRequest,
  type AiInvocationAuditResponse,
  type AiModelConfigResponse,
  type AiModelConfigSaveRequest,
  type AiPromptTemplateResponse,
  type AiPromptTemplateSaveRequest,
  type AiProviderResponse,
  type AiProviderSaveRequest,
  type AiProviderTestResponse,
  type AiRuntimeChatResponse,
} from '@/api/ai/core';
import {
  buildSystemSecretSelectOptions,
  listSystemSecretOptions,
  type SystemSecretSelectOption,
} from '@/api/system/secret';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';
import {
  getAiProviderDefaultBaseUrl,
  shouldApplyAiProviderDefaultBaseUrl,
} from '@/utils/aiProviderDefaults';

const route = useRoute();
const tenantId = ref(getAdminRuntimeContext().tenantId);
const activeTab = ref(resolveTabFromPath(route.path));
const loading = ref(false);
const saving = ref(false);
const providerTesting = ref(false);
const providerBaseUrlTouched = ref(false);
const lastProviderTypeForDefault = ref('openai_compatible');
const providerSecretLoading = ref(false);
const providerSecretOptions = ref<SystemSecretSelectOption[]>([]);
const modelProviderLoading = ref(false);
const auditAppLoading = ref(false);
const providers = ref<AiProviderResponse[]>([]);
const models = ref<AiModelConfigResponse[]>([]);
const apps = ref<AiAppResponse[]>([]);
const prompts = ref<AiPromptTemplateResponse[]>([]);
const audits = ref<AiInvocationAuditResponse[]>([]);
const auditAppCode = ref('');
const runtimeTesting = ref(false);
const providerTestResult = ref<AiProviderTestResponse | null>(null);
const runtimeTestResult = ref<AiRuntimeChatResponse | null>(null);
const currentTenantId = computed(() => tenantId.value || getAdminRuntimeContext().tenantId);

const providerCommand = reactive<AiProviderSaveRequest>(createProviderCommand());
const modelCommand = reactive<AiModelConfigSaveRequest>(createModelCommand());
const appCommand = reactive<AiAppSaveRequest>(createAppCommand());
const promptCommand = reactive<AiPromptTemplateSaveRequest>(createPromptCommand());
const runtimeTestCommand = reactive(createRuntimeTestCommand());

const moduleTitleMap: Record<string, string> = {
  providers: '供应商',
  models: '模型配置',
  apps: '应用接入',
  prompts: '提示词',
  audits: '调用审计',
};

const enabledStatusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];
const promptStatusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' },
  { label: '停用', value: 'disabled' },
];
const providerTypeOptions = [
  { label: 'OpenAI 兼容', value: 'openai_compatible' },
  { label: '通义千问', value: 'dashscope' },
  { label: '火山方舟', value: 'volcengine' },
  { label: 'DeepSeek', value: 'deepseek' },
  { label: '智谱', value: 'zhipu' },
  { label: '本地模型', value: 'local' },
];
const modelTypeOptions = [
  { label: '对话', value: 'chat' },
  { label: '向量', value: 'embedding' },
  { label: '多模态', value: 'multimodal' },
];

const providerColumns = [
  { title: '供应商', dataIndex: 'providerCode', key: 'provider', width: 260 },
  { title: '类型', dataIndex: 'providerType', key: 'providerType', width: 140 },
  { title: '基础地址', dataIndex: 'baseUrl', key: 'baseUrl', width: 320 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 96 },
];
const modelColumns = [
  { title: '模型', dataIndex: 'modelCode', key: 'model', width: 260 },
  { title: '供应商', dataIndex: 'providerId', key: 'providerId', width: 220 },
  { title: '类型', dataIndex: 'modelType', key: 'modelType', width: 100 },
  { title: '能力', dataIndex: 'supportStream', key: 'capabilities', width: 150 },
  { title: '上下文', dataIndex: 'contextWindow', key: 'contextWindow', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 96 },
];
const appColumns = [
  { title: '应用', dataIndex: 'appCode', key: 'app', width: 260 },
  { title: '默认模型', dataIndex: 'defaultModelId', key: 'defaultModel', width: 260 },
  { title: '每日额度', dataIndex: 'dailyTokenQuota', key: 'dailyTokenQuota', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 96 },
];
const promptColumns = [
  { title: '提示词', dataIndex: 'promptCode', key: 'prompt', width: 260 },
  { title: '版本', dataIndex: 'version', key: 'version', width: 88 },
  { title: '变量', dataIndex: 'variables', key: 'variables', width: 320 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 96 },
];
const auditColumns = [
  { title: '应用', dataIndex: 'appCode', key: 'appCode' },
  { title: '类型', dataIndex: 'invocationType', key: 'invocationType', width: 100 },
  { title: '总令牌', dataIndex: 'totalTokens', key: 'totalTokens', width: 110 },
  { title: '耗时 ms', dataIndex: 'latencyMs', key: 'latencyMs', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: 'Trace ID', dataIndex: 'traceId', key: 'traceId' },
];

const activeModuleTitle = computed(() => moduleTitleMap[activeTab.value] ?? '供应商');
const selectedProviderKey = computed(() => providerCommand.providerCode);
const providerStats = computed(() => ({
  total: providers.value.length,
  enabled: providers.value.filter((provider) => provider.status === 'enabled').length,
  disabled: providers.value.filter((provider) => provider.status === 'disabled').length,
  currentType: providerTypeText(providerCommand.providerType),
}));
const selectedModelKey = computed(() => modelCommand.modelCode);
const modelStats = computed(() => ({
  total: models.value.length,
  enabled: models.value.filter((model) => model.status === 'enabled').length,
  disabled: models.value.filter((model) => model.status === 'disabled').length,
  stream: models.value.filter((model) => model.supportStream).length,
  tool: models.value.filter((model) => model.supportTool).length,
}));
const selectedAppKey = computed(() => appCommand.appCode);
const appStats = computed(() => ({
  total: apps.value.length,
  enabled: apps.value.filter((app) => app.status === 'enabled').length,
  disabled: apps.value.filter((app) => app.status === 'disabled').length,
  totalQuota: apps.value.reduce((sum, app) => sum + Number(app.dailyTokenQuota || 0), 0),
}));
const modelProviderOptions = computed(() => {
  const options = providers.value.map((provider) => ({
    label: `${provider.providerName}（${provider.providerCode}）`,
    value: provider.id,
  }));
  const currentProviderId = modelCommand.providerId;
  if (!currentProviderId || options.some((option) => option.value === currentProviderId)) {
    return options;
  }
  return [{ label: `ID ${currentProviderId}（当前供应商）`, value: currentProviderId }, ...options];
});
const appModelOptions = computed(() => {
  const options = models.value.map((model) => ({
    label: `${model.modelName}（${model.modelCode} / ID ${model.id}）`,
    value: model.id,
  }));
  const currentModelId = appCommand.defaultModelId;
  if (!currentModelId || options.some((option) => option.value === currentModelId)) {
    return options;
  }
  return [{ label: `ID ${currentModelId}（当前模型）`, value: currentModelId }, ...options];
});
const runtimeAppOptions = computed(() => {
  const options = apps.value.map((app) => ({
    label: `${app.appName}（${app.appCode}）`,
    value: app.appCode,
  }));
  const currentAppCode = runtimeTestCommand.appCode;
  if (!currentAppCode || options.some((option) => option.value === currentAppCode)) {
    return options;
  }
  return [{ label: `${currentAppCode}（当前应用）`, value: currentAppCode }, ...options];
});
const runtimePromptSelection = computed({
  get: () => buildPromptSelection(runtimeTestCommand.promptCode, runtimeTestCommand.promptVersion),
  set: (selection?: string) => {
    const [promptCode, promptVersion] = parsePromptSelection(selection);
    runtimeTestCommand.promptCode = promptCode;
    runtimeTestCommand.promptVersion = promptVersion;
  },
});
const runtimePromptOptions = computed(() => {
  const options = prompts.value.map((prompt) => ({
    label: `${prompt.promptName}（${prompt.promptCode} / ${prompt.version}）`,
    value: buildPromptSelection(prompt.promptCode, prompt.version),
  }));
  const currentSelection = runtimePromptSelection.value;
  if (!currentSelection || options.some((option) => option.value === currentSelection)) {
    return options;
  }
  return [{ label: `${runtimeTestCommand.promptCode}（当前提示词）`, value: currentSelection }, ...options];
});
const auditAppOptions = computed(() => {
  const options = apps.value.map((app) => ({
    label: `${app.appName}（${app.appCode}）`,
    value: app.appCode,
  }));
  const currentAppCode = auditAppCode.value;
  if (!currentAppCode || options.some((option) => option.value === currentAppCode)) {
    return options;
  }
  return [{ label: `${currentAppCode}（当前应用编码）`, value: currentAppCode }, ...options];
});
const providerSecretSelectOptions = computed<SystemSecretSelectOption[]>(() => {
  const options = providerSecretOptions.value;
  const currentSecretRef = normalizeSecretRef(providerCommand.secretRef);
  if (!currentSecretRef || options.some((option) => option.value === currentSecretRef)) {
    return options;
  }
  return [
    {
      label: `${currentSecretRef}（当前引用）`,
      value: currentSecretRef,
      secretCode: currentSecretRef.replace(/^secret:/, ''),
      secretName: currentSecretRef,
      secretKind: 'generic',
    },
    ...options,
  ];
});
const selectedPromptKey = computed(() => `${promptCommand.promptCode}::${promptCommand.version}`);
const promptVariablePreview = computed(() => splitVariables(promptCommand.variables));
const promptStats = computed(() => ({
  total: prompts.value.length,
  published: prompts.value.filter((prompt) => prompt.status === 'published').length,
  draft: prompts.value.filter((prompt) => prompt.status === 'draft').length,
  disabled: prompts.value.filter((prompt) => prompt.status === 'disabled').length,
}));

watch(
  () => route.path,
  () => {
    activeTab.value = resolveTabFromPath(route.path);
    void loadActiveTab();
  },
  { immediate: true },
);

function createProviderCommand(): AiProviderSaveRequest {
  return {
    tenantId: currentTenantId.value,
    providerCode: '',
    providerName: '',
    providerType: 'openai_compatible',
    baseUrl: '',
    secretRef: '',
    status: 'enabled',
  };
}

function createModelCommand(): AiModelConfigSaveRequest {
  return {
    tenantId: currentTenantId.value,
    providerId: undefined,
    modelCode: '',
    modelName: '',
    modelType: 'chat',
    contextWindow: 8192,
    supportStream: true,
    supportTool: false,
    status: 'enabled',
  };
}

function createAppCommand(): AiAppSaveRequest {
  return {
    tenantId: currentTenantId.value,
    appCode: '',
    appName: '',
    defaultModelId: models.value.find((model) => model.status === 'enabled')?.id ?? 1,
    systemPrompt: '',
    dailyTokenQuota: 100000,
    status: 'enabled',
  };
}

function createPromptCommand(): AiPromptTemplateSaveRequest {
  return {
    tenantId: currentTenantId.value,
    promptCode: '',
    promptName: '',
    version: 'v1',
    templateContent: '',
    variables: '',
    status: 'draft',
  };
}

function createRuntimeTestCommand(): { appCode: string; promptCode: string; promptVersion: string; variablesJson: string } {
  return {
    appCode: '',
    promptCode: 'summary',
    promptVersion: 'v1',
    variablesJson: '{\n  "content": ""\n}',
  };
}

function resolveTabFromPath(path: string): string {
  if (path.includes('/ai/models')) return 'models';
  if (path.includes('/ai/apps')) return 'apps';
  if (path.includes('/ai/prompts')) return 'prompts';
  if (path.includes('/ai/invocation-audits')) return 'audits';
  return 'providers';
}

async function loadActiveTab(): Promise<void> {
  if (activeTab.value === 'audits') {
    const currentTenant = syncTenantContext();
    await loadAuditAppOptions(currentTenant);
    if (auditAppCode.value) {
      await loadAudits();
    }
    return;
  }
  loading.value = true;
  try {
    const currentTenant = syncTenantContext();
    if (activeTab.value === 'providers') {
      const [providerList] = await Promise.all([listAiProviders(currentTenant), loadProviderSecretOptions(currentTenant)]);
      providers.value = providerList;
    }
    if (activeTab.value === 'models') {
      const [modelList] = await Promise.all([listAiModels(currentTenant), loadModelProviderOptions(currentTenant)]);
      models.value = modelList;
    }
    if (activeTab.value === 'apps') {
      const [appList, modelList, promptList] = await Promise.all([
        listAiApps(currentTenant),
        listAiModels(currentTenant),
        listAiPrompts(currentTenant),
      ]);
      apps.value = appList;
      models.value = modelList;
      prompts.value = promptList;
    }
    if (activeTab.value === 'prompts') prompts.value = await listAiPrompts(currentTenant);
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'AI 能力中心数据加载失败');
  } finally {
    loading.value = false;
  }
}

async function loadAudits(): Promise<void> {
  if (!auditAppCode.value) {
    message.error('请先输入应用编码');
    return;
  }
  loading.value = true;
  try {
    audits.value = await listAiInvocationAudits(syncTenantContext(), auditAppCode.value);
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'AI 调用审计查询失败');
  } finally {
    loading.value = false;
  }
}

async function loadProviderSecretOptions(currentTenant: string): Promise<void> {
  providerSecretLoading.value = true;
  try {
    providerSecretOptions.value = buildSystemSecretSelectOptions(
      await listSystemSecretOptions(currentTenant, 'api_secret', 'enabled'),
    );
  } catch (error) {
    providerSecretOptions.value = [];
    message.error(error instanceof Error ? error.message : '系统密钥选项加载失败');
  } finally {
    providerSecretLoading.value = false;
  }
}

async function loadModelProviderOptions(currentTenant: string): Promise<void> {
  modelProviderLoading.value = true;
  try {
    providers.value = await listAiProviders(currentTenant);
  } catch (error) {
    providers.value = [];
    message.error(error instanceof Error ? error.message : 'AI 供应商选项加载失败');
  } finally {
    modelProviderLoading.value = false;
  }
}

async function loadAuditAppOptions(currentTenant: string): Promise<void> {
  auditAppLoading.value = true;
  try {
    apps.value = await listAiApps(currentTenant);
  } catch (error) {
    apps.value = [];
    message.error(error instanceof Error ? error.message : 'AI 应用选项加载失败');
  } finally {
    auditAppLoading.value = false;
  }
}

async function saveProvider(): Promise<void> {
  await saveCommand(() => saveAiProvider(providerCommand), 'AI 供应商已保存');
  providers.value = await listAiProviders(syncTenantContext());
}

async function testProvider(): Promise<void> {
  providerTesting.value = true;
  providerTestResult.value = null;
  try {
    syncTenantContext();
    providerTestResult.value = await testAiProvider(providerCommand);
    if (providerTestResult.value.success) {
      message.success('AI 供应商测试通过');
      return;
    }
    message.error(providerTestResult.value.message || 'AI 供应商测试失败');
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'AI 供应商测试失败');
  } finally {
    providerTesting.value = false;
  }
}

async function saveModel(): Promise<void> {
  await saveCommand(() => saveAiModel(modelCommand), 'AI 模型已保存');
  models.value = await listAiModels(syncTenantContext());
}

async function saveApp(): Promise<void> {
  await saveCommand(() => saveAiApp(appCommand), 'AI 应用已保存');
  apps.value = await listAiApps(syncTenantContext());
}

async function savePrompt(): Promise<void> {
  await saveCommand(() => saveAiPrompt(promptCommand), 'AI 提示词已保存');
  prompts.value = await listAiPrompts(syncTenantContext());
}

async function testRuntimeChat(): Promise<void> {
  runtimeTesting.value = true;
  runtimeTestResult.value = null;
  try {
    const variables = parseRuntimeVariables(runtimeTestCommand.variablesJson);
    runtimeTestResult.value = await chatWithAiRuntime({
      tenantId: syncTenantContext(),
      appCode: runtimeTestCommand.appCode || appCommand.appCode,
      promptCode: runtimeTestCommand.promptCode,
      promptVersion: runtimeTestCommand.promptVersion || 'v1',
      variables,
      stream: false,
    });
    message.success('AI 测试调用完成');
  } catch (error) {
    message.error(error instanceof Error ? error.message : 'AI 测试调用失败');
  } finally {
    runtimeTesting.value = false;
  }
}

async function saveCommand(action: () => Promise<void>, successMessage: string): Promise<void> {
  saving.value = true;
  try {
    syncTenantContext();
    await action();
    message.success(successMessage);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function applyProvider(provider: AiProviderResponse): void {
  Object.assign(providerCommand, {
    tenantId: currentTenantId.value,
    providerCode: provider.providerCode,
    providerName: provider.providerName,
    providerType: provider.providerType,
    baseUrl: provider.baseUrl,
    secretRef: provider.secretRef ?? '',
    status: provider.status,
  });
  providerBaseUrlTouched.value = Boolean(provider.baseUrl);
  lastProviderTypeForDefault.value = provider.providerType;
}

function applyModel(model: AiModelConfigResponse): void {
  const { id: _modelId, ...editableModel } = model;
  Object.assign(modelCommand, { ...editableModel, tenantId: currentTenantId.value });
}

function applyApp(app: AiAppResponse): void {
  Object.assign(appCommand, { ...app, tenantId: currentTenantId.value });
  runtimeTestCommand.appCode = app.appCode;
}

function applyPrompt(prompt: AiPromptTemplateResponse): void {
  Object.assign(promptCommand, { ...prompt, tenantId: currentTenantId.value });
}

function buildProviderRowKey(record: AiProviderResponse): string {
  return record.providerCode;
}

function buildProviderRowProps(record: AiProviderResponse): { class?: string; onClick: () => void } {
  return {
    class: selectedProviderKey.value === buildProviderRowKey(record) ? 'is-selected' : undefined,
    onClick: () => applyProvider(record),
  };
}

function buildModelRowKey(record: AiModelConfigResponse): string {
  return record.modelCode;
}

function buildModelRowProps(record: AiModelConfigResponse): { class?: string; onClick: () => void } {
  return {
    class: selectedModelKey.value === buildModelRowKey(record) ? 'is-selected' : undefined,
    onClick: () => applyModel(record),
  };
}

function buildAppRowKey(record: AiAppResponse): string {
  return record.appCode;
}

function buildAppRowProps(record: AiAppResponse): { class?: string; onClick: () => void } {
  return {
    class: selectedAppKey.value === buildAppRowKey(record) ? 'is-selected' : undefined,
    onClick: () => applyApp(record),
  };
}

function buildPromptRowKey(record: AiPromptTemplateResponse): string {
  return `${record.promptCode}::${record.version}`;
}

function buildPromptRowProps(record: AiPromptTemplateResponse): { class?: string; onClick: () => void } {
  return {
    class: selectedPromptKey.value === buildPromptRowKey(record) ? 'is-selected' : undefined,
    onClick: () => applyPrompt(record),
  };
}

function resetProvider(): void {
  Object.assign(providerCommand, createProviderCommand());
  providerBaseUrlTouched.value = false;
  lastProviderTypeForDefault.value = providerCommand.providerType;
  providerTestResult.value = null;
}

function resetModel(): void {
  Object.assign(modelCommand, createModelCommand());
}

function resetApp(): void {
  Object.assign(appCommand, createAppCommand());
  runtimeTestCommand.appCode = '';
  runtimeTestResult.value = null;
}

function resetPrompt(): void {
  Object.assign(promptCommand, createPromptCommand());
}

function syncTenantContext(): string {
  const currentTenant = tenantId.value || requireAdminTenantId();
  tenantId.value = currentTenant;
  providerCommand.tenantId = currentTenant;
  modelCommand.tenantId = currentTenant;
  appCommand.tenantId = currentTenant;
  promptCommand.tenantId = currentTenant;
  return currentTenant;
}

function handleProviderTypeChange(providerType: string): void {
  if (
    shouldApplyAiProviderDefaultBaseUrl(
      providerCommand.baseUrl,
      lastProviderTypeForDefault.value,
      providerBaseUrlTouched.value,
    )
  ) {
    providerCommand.baseUrl = getAiProviderDefaultBaseUrl(providerType);
  }
  lastProviderTypeForDefault.value = providerType;
}

function markProviderBaseUrlTouched(): void {
  providerBaseUrlTouched.value = true;
}

function handleAuditAppChange(value: string | undefined): void {
  if (!value) {
    audits.value = [];
  }
}

function providerTypeText(providerType?: string): string {
  return providerTypeOptions.find((option) => option.value === providerType)?.label ?? providerType ?? '未知';
}

function enabledStatusColor(status?: string): string {
  return status === 'enabled' ? 'green' : 'default';
}

function enabledStatusText(status?: string): string {
  return status === 'enabled' ? '启用' : '停用';
}

function modelProviderText(providerId?: number): string {
  const provider = providers.value.find((item) => item.id === providerId);
  if (provider) {
    return `${provider.providerName}（${provider.providerCode}）`;
  }
  return providerId ? `ID ${providerId}` : '未绑定';
}

function modelTypeText(modelType?: string): string {
  return modelTypeOptions.find((option) => option.value === modelType)?.label ?? modelType ?? '未知';
}

function modelTypeColor(modelType?: string): string {
  if (modelType === 'embedding') return 'cyan';
  if (modelType === 'multimodal') return 'purple';
  return 'blue';
}

function appDefaultModelText(modelId?: number): string {
  const model = models.value.find((item) => item.id === modelId);
  if (model) {
    return `${model.modelName}（${model.modelCode}）`;
  }
  return modelId ? `ID ${modelId}` : '未绑定';
}

function formatNumber(value: number): string {
  return new Intl.NumberFormat('zh-CN').format(value);
}

function buildPromptSelection(promptCode?: string, promptVersion?: string): string {
  if (!promptCode) return '';
  return `${promptCode}::${promptVersion || 'v1'}`;
}

function parsePromptSelection(selection?: string): [string, string] {
  if (!selection) return ['', 'v1'];
  const [promptCode, promptVersion] = selection.split('::');
  return [promptCode || '', promptVersion || 'v1'];
}

function normalizeSecretRef(secretRef?: string): string {
  return (secretRef ?? '').trim();
}

function splitVariables(variables?: string): string[] {
  return (variables ?? '')
    .split(',')
    .map((variable) => variable.trim())
    .filter(Boolean);
}

function promptStatusColor(status?: string): string {
  if (status === 'published') return 'green';
  if (status === 'disabled') return 'default';
  return 'blue';
}

function promptStatusText(status?: string): string {
  if (status === 'published') return '已发布';
  if (status === 'disabled') return '停用';
  return '草稿';
}

function parseRuntimeVariables(jsonText: string): Record<string, string> {
  const normalized = jsonText.trim();
  if (!normalized) {
    return {};
  }
  const parsed = JSON.parse(normalized) as Record<string, unknown>;
  if (!parsed || Array.isArray(parsed) || typeof parsed !== 'object') {
    throw new Error('变量 JSON 必须是对象');
  }
  return Object.fromEntries(
    Object.entries(parsed).map(([key, value]) => [key, value === null || value === undefined ? '' : String(value)]),
  );
}
</script>

<style scoped>
.ai-core-page {
  min-width: 0;
}

.tenant-input {
  width: 180px;
}

.full-width {
  width: 100%;
}

.switch-row {
  margin-bottom: 16px;
}

.audit-toolbar {
  margin-bottom: 16px;
}

.audit-app-select {
  width: 300px;
}

.runtime-result-meta {
  margin-top: 16px;
}

.runtime-result-content {
  margin-top: 12px;
}

.provider-test-result {
  margin-top: 16px;
}

.provider-workbench {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(420px, 0.9fr);
  gap: 16px;
  align-items: start;
}

.provider-list-panel,
.provider-editor-panel {
  min-width: 0;
  padding: 16px;
  background: #fff;
  border: 1px solid #edf1f7;
  border-radius: 8px;
}

.provider-panel-header {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}

.provider-panel-title {
  min-width: 0;
}

.provider-panel-title h3 {
  margin: 0;
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.provider-panel-title p {
  margin: 4px 0 0;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.provider-summary-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.provider-summary-row > div {
  min-width: 0;
  padding: 10px 12px;
  background: linear-gradient(180deg, #f8fbff 0%, #fff 100%);
  border: 1px solid #e8eef8;
  border-radius: 8px;
}

.provider-summary-row strong {
  display: block;
  overflow: hidden;
  color: #1f2937;
  font-size: 18px;
  font-weight: 650;
  line-height: 24px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.provider-summary-row span {
  display: block;
  margin-top: 2px;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.provider-table :deep(.ant-table-row) {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.provider-table :deep(.ant-table-row:hover > td),
.provider-table :deep(.is-selected > td) {
  background: #eef5ff;
}

.provider-code-cell {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.provider-code-cell strong,
.provider-code-cell span,
.provider-url-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.provider-code-cell strong {
  color: #1f2937;
  font-size: 13px;
  line-height: 20px;
}

.provider-code-cell span {
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.provider-url-text {
  display: block;
  max-width: 100%;
  color: #475467;
}

.provider-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 12px;
}

.provider-editor-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.provider-test-panel {
  padding: 12px 14px;
  margin-top: 14px;
  border: 1px solid #d9e3f0;
  border-radius: 8px;
  background: #f8fafc;
}

.provider-test-panel.is-success {
  border-color: #b7eb8f;
  background: #f6ffed;
}

.provider-test-panel.is-error {
  border-color: #ffccc7;
  background: #fff2f0;
}

.provider-test-panel > div {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
}

.provider-test-panel strong {
  color: #1f2937;
  font-size: 13px;
  line-height: 20px;
}

.provider-test-panel span,
.provider-test-panel p {
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.provider-test-panel p {
  margin: 8px 0 0;
}

.model-workbench {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(420px, 0.9fr);
  gap: 16px;
  align-items: start;
}

.model-list-panel,
.model-editor-panel {
  min-width: 0;
  padding: 16px;
  background: #fff;
  border: 1px solid #edf1f7;
  border-radius: 8px;
}

.model-panel-header {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}

.model-panel-title {
  min-width: 0;
}

.model-panel-title h3 {
  margin: 0;
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.model-panel-title p {
  margin: 4px 0 0;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.model-summary-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.model-summary-row > div {
  min-width: 0;
  padding: 10px 12px;
  background: linear-gradient(180deg, #f8fbff 0%, #fff 100%);
  border: 1px solid #e8eef8;
  border-radius: 8px;
}

.model-summary-row strong {
  display: block;
  color: #1f2937;
  font-size: 18px;
  font-weight: 650;
  line-height: 24px;
}

.model-summary-row span {
  display: block;
  margin-top: 2px;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.model-table :deep(.ant-table-row) {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.model-table :deep(.ant-table-row:hover > td),
.model-table :deep(.is-selected > td) {
  background: #eef5ff;
}

.model-code-cell {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.model-code-cell strong,
.model-code-cell span,
.model-provider-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-code-cell strong {
  color: #1f2937;
  font-size: 13px;
  line-height: 20px;
}

.model-code-cell span,
.model-provider-text {
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.model-provider-text {
  display: block;
}

.model-capability-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.model-capability-list :deep(.ant-tag) {
  margin-inline-end: 0;
}

.model-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 12px;
}

.model-capability-panel {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  min-height: 42px;
  padding: 9px 12px;
  margin-bottom: 14px;
  background: #f8fafc;
  border: 1px dashed #d9e3f0;
  border-radius: 8px;
}

.model-capability-panel > span {
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.model-capability-panel > div {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.model-editor-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.app-workbench {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(460px, 0.9fr);
  gap: 16px;
  align-items: start;
}

.app-list-panel,
.app-editor-panel {
  min-width: 0;
  padding: 16px;
  background: #fff;
  border: 1px solid #edf1f7;
  border-radius: 8px;
}

.app-panel-header {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}

.app-panel-title {
  min-width: 0;
}

.app-panel-title h3 {
  margin: 0;
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.app-panel-title p {
  margin: 4px 0 0;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.app-summary-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.app-summary-row > div {
  min-width: 0;
  padding: 10px 12px;
  background: linear-gradient(180deg, #f8fbff 0%, #fff 100%);
  border: 1px solid #e8eef8;
  border-radius: 8px;
}

.app-summary-row strong {
  display: block;
  overflow: hidden;
  color: #1f2937;
  font-size: 18px;
  font-weight: 650;
  line-height: 24px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-summary-row span {
  display: block;
  margin-top: 2px;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.app-table :deep(.ant-table-row) {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.app-table :deep(.ant-table-row:hover > td),
.app-table :deep(.is-selected > td) {
  background: #eef5ff;
}

.app-code-cell {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.app-code-cell strong,
.app-code-cell span,
.app-model-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-code-cell strong {
  color: #1f2937;
  font-size: 13px;
  line-height: 20px;
}

.app-code-cell span,
.app-model-text {
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.app-model-text {
  display: block;
}

.app-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 12px;
}

.app-system-prompt,
.runtime-json-editor {
  font-family: Menlo, Monaco, Consolas, 'Courier New', monospace;
  line-height: 1.7;
}

.app-editor-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.runtime-panel {
  padding-top: 16px;
  margin-top: 18px;
  border-top: 1px solid #edf1f7;
}

.prompt-workbench {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(420px, 0.9fr);
  gap: 16px;
  align-items: start;
}

.prompt-list-panel,
.prompt-editor-panel {
  min-width: 0;
  padding: 16px;
  background: #fff;
  border: 1px solid #edf1f7;
  border-radius: 8px;
}

.prompt-panel-header {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}

.prompt-panel-title {
  min-width: 0;
}

.prompt-panel-title h3 {
  margin: 0;
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.prompt-panel-title p {
  margin: 4px 0 0;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.prompt-panel-title code {
  padding: 1px 6px;
  color: #2563eb;
  background: #eff6ff;
  border-radius: 4px;
}

.prompt-summary-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.prompt-summary-row > div {
  min-width: 0;
  padding: 10px 12px;
  background: linear-gradient(180deg, #f8fbff 0%, #fff 100%);
  border: 1px solid #e8eef8;
  border-radius: 8px;
}

.prompt-summary-row strong {
  display: block;
  color: #1f2937;
  font-size: 18px;
  font-weight: 650;
  line-height: 24px;
}

.prompt-summary-row span {
  display: block;
  margin-top: 2px;
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.prompt-table :deep(.ant-table-row) {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.prompt-table :deep(.ant-table-row:hover > td),
.prompt-table :deep(.is-selected > td) {
  background: #eef5ff;
}

.prompt-code-cell {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.prompt-code-cell strong,
.prompt-code-cell span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.prompt-code-cell strong {
  color: #1f2937;
  font-size: 13px;
  line-height: 20px;
}

.prompt-code-cell span {
  color: #667085;
  font-size: 12px;
  line-height: 18px;
}

.variable-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-width: 0;
}

.variable-tag-list :deep(.ant-tag),
.prompt-variable-preview :deep(.ant-tag) {
  margin-inline-end: 0;
}

.muted-text {
  color: #98a2b3;
  font-size: 12px;
}

.prompt-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 12px;
}

.prompt-variable-preview {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  min-height: 38px;
  padding: 9px 12px;
  margin: -6px 0 14px;
  background: #f8fafc;
  border: 1px dashed #d9e3f0;
  border-radius: 8px;
}

.prompt-variable-preview > span {
  flex: 0 0 auto;
  color: #667085;
  font-size: 12px;
  line-height: 22px;
}

.prompt-variable-preview > div {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  gap: 6px;
  min-width: 0;
}

.prompt-template-editor {
  font-family: Menlo, Monaco, Consolas, 'Courier New', monospace;
  line-height: 1.7;
}

.prompt-editor-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

@media (max-width: 1280px) {
  .provider-workbench,
  .model-workbench,
  .app-workbench,
  .prompt-workbench {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .provider-list-panel,
  .provider-editor-panel,
  .model-list-panel,
  .model-editor-panel,
  .app-list-panel,
  .app-editor-panel,
  .prompt-list-panel,
  .prompt-editor-panel {
    padding: 12px;
  }

  .provider-panel-header,
  .provider-editor-actions,
  .provider-test-panel > div,
  .model-panel-header,
  .model-capability-panel,
  .model-editor-actions,
  .app-panel-header,
  .app-editor-actions,
  .prompt-panel-header,
  .prompt-variable-preview,
  .prompt-editor-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .provider-summary-row,
  .provider-form-grid,
  .model-summary-row,
  .model-form-grid,
  .app-summary-row,
  .app-form-grid,
  .prompt-summary-row,
  .prompt-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
