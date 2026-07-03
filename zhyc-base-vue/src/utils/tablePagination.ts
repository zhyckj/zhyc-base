/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

/**
 * 后台表格默认分页配置。
 *
 * <p>用于当前仍返回数组列表的管理页面，先提供统一前端分页体验；后续大数据量页面应继续升级为后端分页。</p>
 */
export const tablePagination = {
  pageSize: 10,
  pageSizeOptions: ['10', '20', '50', '100'],
  showSizeChanger: true,
  showQuickJumper: true,
  hideOnSinglePage: false,
  showTotal: (total: number): string => `共 ${total} 条`,
};
