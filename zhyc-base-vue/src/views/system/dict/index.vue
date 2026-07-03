<!--
  Copyright (c) 2026 众汇云创科技（深圳）有限公司.
  This file is part of ZHYC and is licensed for non-commercial use only.
  Commercial use requires a separate written license from the copyright holder.
  SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
-->

<template>
  <section class="dict-page">
    <a-card title="字典管理" :bordered="false">
      <template #extra>
        <a-space>
          <a-button type="primary" v-permission="'system:dict:create'" @click="openTypeForm()">新增字典编码</a-button>
          <a-input v-model:value="tenantId" class="tenant-id" />
          <a-button :loading="typeStatus === 'loading'" @click="loadTypes">查询字典</a-button>
        </a-space>
      </template>

      <a-alert v-if="typeStatus === 'error'" type="error" show-icon :message="errorMessage" class="state-alert" />

      <a-row :gutter="16">
        <a-col :xs="24" :lg="10">
          <section class="dict-section">
            <div class="section-header">
              <div>
                <h3>字典编码列表</h3>
                <p>先查询当前租户全部字典编码，再选择编码维护字典项。</p>
              </div>
              <a-tag :color="dictTypes.length ? 'blue' : 'default'">
                {{ dictTypes.length ? `${dictTypes.length} 个字典` : '无字典' }}
              </a-tag>
            </div>

            <a-table
              row-key="dictCode"
              :columns="typeColumns"
              :data-source="dictTypes"
              :loading="typeStatus === 'loading'"
              :pagination="$tablePagination"
              size="small"
            >
              <template #emptyText>
                <a-empty description="当前租户暂无字典编码" />
              </template>
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'dictCode'">
                  <div class="dict-code-cell">
                    <strong>{{ record.dictCode }}</strong>
                    <span>{{ record.dictName }}</span>
                  </div>
                </template>
                <template v-if="column.key === 'status'">
                  <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
                    {{ $statusLabel(record.status) }}
                  </a-tag>
                </template>
                <template v-if="column.key === 'action'">
                  <a-space>
                    <a-button size="small" type="link" v-permission="'system:dict:query'" @click="openDictItemEditor(record)">
                      维护字典项
                    </a-button>
                    <a-button size="small" v-permission="'system:dict:update'" @click="openTypeForm(record)">编辑</a-button>
                    <a-button size="small" danger v-permission="'system:dict:delete'" @click="removeType(record)">删除</a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </section>
        </a-col>
        <a-col :xs="24" :lg="14">
          <section class="dict-section item-section">
            <div class="section-header">
              <div>
                <h3>字典项维护</h3>
                <p>{{ selectedDictLabel }}</p>
              </div>
              <a-space>
                <a-tag :color="dictItems.length ? 'blue' : 'default'">
                  {{ dictItems.length ? `${dictItems.length} 个字典项` : '未维护' }}
                </a-tag>
                <a-button
                  type="primary"
                  v-permission="'system:dict:item:create'"
                  :disabled="!currentDictCode"
                  @click="openItemForm()"
                >
                  新增字典项
                </a-button>
              </a-space>
            </div>

            <a-empty
              v-if="!currentDictCode"
              description="请先在字典编码列表中点击维护字典项"
              class="page-empty"
            />
            <a-table
              v-else
              row-key="id"
              :columns="itemColumns"
              :data-source="dictItems"
              :loading="itemStatus === 'loading'"
              :pagination="$tablePagination"
              size="small"
            >
              <template #emptyText>
                <a-empty description="当前字典编码暂无字典项" />
              </template>
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'itemColor'">
                  <a-tag :color="record.itemColor || 'default'">{{ record.itemColor || '默认' }}</a-tag>
                </template>
                <template v-if="column.key === 'status'">
                  <a-tag :color="record.status === 'enabled' ? 'green' : 'default'">
                    {{ $statusLabel(record.status) }}
                  </a-tag>
                </template>
                <template v-if="column.key === 'action'">
                  <a-space>
                    <a-button size="small" v-permission="'system:dict:item:update'" @click="openItemForm(record)">编辑</a-button>
                    <a-button size="small" danger v-permission="'system:dict:item:delete'" @click="removeItem(record)">删除</a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </section>
        </a-col>
      </a-row>

      <span class="permission-code">system:dict:query system:dict:create system:dict:update system:dict:delete system:dict:item:create system:dict:item:update system:dict:item:delete</span>
    </a-card>

    <a-modal v-model:open="typeFormOpen" :title="editingType ? '编辑字典' : '新增字典'" :confirm-loading="saving" @ok="submitTypeForm">
      <a-form layout="vertical">
        <a-form-item label="字典编码" required>
          <a-input v-model:value="typeForm.dictCode" />
        </a-form-item>
        <a-form-item label="字典名称" required>
          <a-input v-model:value="typeForm.dictName" />
        </a-form-item>
        <a-form-item label="系统内置">
          <a-switch v-model:checked="typeForm.systemFlag" />
        </a-form-item>
        <a-form-item label="状态" required>
          <a-select v-model:value="typeForm.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="itemFormOpen" :title="editingItem ? '编辑字典项' : '新增字典项'" :confirm-loading="saving" @ok="submitItemForm">
      <a-form layout="vertical">
        <a-form-item label="字典编码" required>
          <a-input v-model:value="itemForm.dictCode" disabled />
        </a-form-item>
        <a-form-item label="标签" required>
          <a-input v-model:value="itemForm.itemLabel" />
        </a-form-item>
        <a-form-item label="值" required>
          <a-input v-model:value="itemForm.itemValue" />
        </a-form-item>
        <a-form-item label="颜色">
          <a-input v-model:value="itemForm.itemColor" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="itemForm.sortOrder" :min="0" class="full-field" />
        </a-form-item>
        <a-form-item label="状态" required>
          <a-select v-model:value="itemForm.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { Modal, message } from 'ant-design-vue';
import { computed, onMounted, reactive, ref } from 'vue';

import {
  createSystemDictItem,
  createSystemDictType,
  deleteSystemDictItem,
  deleteSystemDictType,
  listSystemDictItems,
  listSystemDictTypes,
  updateSystemDictItem,
  updateSystemDictType,
  type SystemDictItem,
  type SystemDictItemSavePayload,
  type SystemDictType,
  type SystemDictTypeSavePayload,
} from '@/api/system/dict';
import type { LoadStatus } from '@/types/platform';
import { getAdminRuntimeContext, requireAdminTenantId } from '@/utils/adminContext';

/** 当前租户业务编码。 */
const tenantId = ref(getAdminRuntimeContext().tenantId);
/** 字典类型加载状态。 */
const typeStatus = ref<LoadStatus>('idle');
/** 字典项加载状态。 */
const itemStatus = ref<LoadStatus>('idle');
/** 异常提示文案。 */
const errorMessage = ref('');
const saving = ref(false);
const typeFormOpen = ref(false);
const itemFormOpen = ref(false);
/** 当前选中字典编码。 */
const currentDictCode = ref('');
/** 字典类型列表。 */
const dictTypes = ref<SystemDictType[]>([]);
/** 字典项列表。 */
const dictItems = ref<SystemDictItem[]>([]);
const editingType = ref<SystemDictType>();
const editingItem = ref<SystemDictItem>();
const typeForm = reactive<SystemDictTypeSavePayload>({
  tenantId: '',
  dictCode: '',
  dictName: '',
  systemFlag: false,
  status: 'enabled',
});
const itemForm = reactive<SystemDictItemSavePayload>({
  tenantId: '',
  dictCode: '',
  itemLabel: '',
  itemValue: '',
  itemColor: '',
  sortOrder: 0,
  status: 'enabled',
});
const statusOptions = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
];

/** 字典类型表格列定义。 */
const typeColumns = [
  { title: '字典编码', dataIndex: 'dictCode', key: 'dictCode' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 220 },
];

/** 字典项表格列定义。 */
const itemColumns = [
  { title: '标签', dataIndex: 'itemLabel', key: 'itemLabel' },
  { title: '值', dataIndex: 'itemValue', key: 'itemValue' },
  { title: '颜色', dataIndex: 'itemColor', key: 'itemColor', width: 100 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 130 },
];

/** 当前选中字典展示文案。 */
const selectedDictLabel = computed(() => {
  const currentType = dictTypes.value.find((type) => type.dictCode === currentDictCode.value);
  return currentType ? `${currentType.dictName}（${currentType.dictCode}）` : '请先从左侧字典编码列表进入维护';
});

/**
 * 加载字典类型列表。
 */
async function loadTypes(): Promise<void> {
  typeStatus.value = 'loading';
  errorMessage.value = '';
  try {
    const currentTenantId = syncTenantContext();
    dictTypes.value = await listSystemDictTypes(currentTenantId);
    typeStatus.value = 'success';
    if (currentDictCode.value && dictTypes.value.some((type) => type.dictCode === currentDictCode.value)) {
      await loadItems(currentDictCode.value);
      return;
    }
    if (!currentDictCode.value || !dictTypes.value.some((type) => type.dictCode === currentDictCode.value)) {
      currentDictCode.value = '';
      dictItems.value = [];
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '字典类型加载失败';
    typeStatus.value = 'error';
  }
}

/**
 * 打开字典项维护区。
 *
 * @param type 当前待维护字典项的字典编码
 */
async function openDictItemEditor(type: SystemDictType): Promise<void> {
  currentDictCode.value = type.dictCode;
  await loadItems(type.dictCode);
}

/**
 * 加载指定字典编码下的字典项。
 *
 * @param dictCode 字典编码
 */
async function loadItems(dictCode: string): Promise<void> {
  itemStatus.value = 'loading';
  errorMessage.value = '';
  try {
    dictItems.value = await listSystemDictItems(syncTenantContext(), dictCode);
    itemStatus.value = 'success';
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '字典项加载失败';
    itemStatus.value = 'error';
  }
}

function openTypeForm(type?: SystemDictType): void {
  editingType.value = type;
  Object.assign(typeForm, {
    tenantId: syncTenantContext(),
    dictCode: type?.dictCode ?? '',
    dictName: type?.dictName ?? '',
    systemFlag: type?.systemFlag ?? false,
    status: type?.status ?? 'enabled',
  });
  typeFormOpen.value = true;
}

async function submitTypeForm(): Promise<void> {
  if (!typeForm.dictCode.trim() || !typeForm.dictName.trim()) {
    message.error('请填写字典编码和字典名称');
    return;
  }
  saving.value = true;
  try {
    const payload = { ...typeForm, tenantId: syncTenantContext(), dictCode: typeForm.dictCode.trim(), dictName: typeForm.dictName.trim() };
    if (editingType.value) {
      await updateSystemDictType(editingType.value.id, payload);
    } else {
      await createSystemDictType(payload);
    }
    message.success('字典已保存');
    typeFormOpen.value = false;
    await loadTypes();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '字典保存失败');
  } finally {
    saving.value = false;
  }
}

function removeType(type: SystemDictType): void {
  Modal.confirm({
    title: '确认删除字典',
    content: `删除字典类型：${type.dictName}`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      await deleteSystemDictType(type.id, syncTenantContext());
      message.success('字典已删除');
      if (currentDictCode.value === type.dictCode) {
        currentDictCode.value = '';
        dictItems.value = [];
      }
      await loadTypes();
    },
  });
}

function openItemForm(item?: SystemDictItem): void {
  const targetDictCode = item?.dictCode ?? currentDictCode.value;
  if (!targetDictCode) {
    message.warning('请先选择要维护的字典编码');
    return;
  }
  editingItem.value = item;
  Object.assign(itemForm, {
    tenantId: syncTenantContext(),
    dictCode: targetDictCode,
    itemLabel: item?.itemLabel ?? '',
    itemValue: item?.itemValue ?? '',
    itemColor: item?.itemColor ?? '',
    sortOrder: item?.sortOrder ?? 0,
    status: item?.status ?? 'enabled',
  });
  itemFormOpen.value = true;
}

async function submitItemForm(): Promise<void> {
  if (!itemForm.dictCode.trim() || !itemForm.itemLabel.trim() || !itemForm.itemValue.trim()) {
    message.error('请填写字典编码、标签和值');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      ...itemForm,
      tenantId: syncTenantContext(),
      dictCode: itemForm.dictCode.trim(),
      itemLabel: itemForm.itemLabel.trim(),
      itemValue: itemForm.itemValue.trim(),
      itemColor: itemForm.itemColor?.trim(),
    };
    if (editingItem.value) {
      await updateSystemDictItem(editingItem.value.id, payload);
    } else {
      await createSystemDictItem(payload);
    }
    message.success('字典项已保存');
    itemFormOpen.value = false;
    currentDictCode.value = payload.dictCode;
    await loadItems(payload.dictCode);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '字典项保存失败');
  } finally {
    saving.value = false;
  }
}

function removeItem(item: SystemDictItem): void {
  Modal.confirm({
    title: '确认删除字典项',
    content: `删除字典项：${item.itemLabel}`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      await deleteSystemDictItem(item.id, syncTenantContext());
      message.success('字典项已删除');
      await loadItems(item.dictCode);
    },
  });
}

/**
 * 同步后台租户到字典查询条件。
 *
 * @returns 当前租户编码
 */
function syncTenantContext(): string {
  const currentTenantId = requireAdminTenantId();
  tenantId.value = currentTenantId;
  return currentTenantId;
}

onMounted(() => {
  void loadTypes();
});
</script>

<style scoped>
.dict-page {
  min-width: 0;
}

.tenant-id {
  width: 180px;
}

.state-alert {
  margin-bottom: 12px;
}

.dict-section {
  min-width: 0;
  padding-top: 4px;
}

.item-section {
  min-height: 360px;
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
  color: #111827;
  font-size: 15px;
  font-weight: 600;
}

.section-header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.dict-code-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dict-code-cell span {
  color: #64748b;
  font-size: 12px;
}

.page-empty {
  padding: 96px 0;
}

.full-field {
  width: 100%;
}

.permission-code {
  display: none;
}

@media (max-width: 992px) {
  .item-section {
    margin-top: 16px;
  }
}

@media (max-width: 640px) {
  .tenant-id {
    width: 100%;
  }
}
</style>
