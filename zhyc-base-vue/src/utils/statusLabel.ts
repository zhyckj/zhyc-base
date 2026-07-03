/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

/** 状态编码中文文案映射。 */
const STATUS_LABEL_MAP: Record<string, string> = {
  enabled: '启用',
  disabled: '停用',
  active: '启用',
  inactive: '停用',
  draft: '草稿',
  published: '已发布',
  unpublished: '未发布',
  pending: '待处理',
  processing: '处理中',
  success: '成功',
  failed: '失败',
  canceled: '已取消',
  cancelled: '已取消',
  todo: '待办',
  done: '已办',
  approved: '已通过',
  rejected: '已驳回',
  revoked: '已撤回',
  running: '运行中',
  stopped: '已停止',
  up: '正常',
  down: '异常',
  connected: '已连接',
  disconnected: '未连接',
};

/**
 * 格式化状态编码为中文展示文案。
 *
 * @param status 后端返回的状态编码，可能为大小写混合格式
 * @returns 中文状态文案；未知状态返回原始值，避免空白展示
 */
export function formatStatusLabel(status?: string | null): string {
  if (!status) {
    return '未设置';
  }
  const normalizedStatus = status.trim();
  if (!normalizedStatus) {
    return '未设置';
  }
  return STATUS_LABEL_MAP[normalizedStatus.toLowerCase()] ?? normalizedStatus;
}
