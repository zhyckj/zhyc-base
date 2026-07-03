/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import { createApp } from 'vue';

import App from './App.vue';
import { router } from './router/routes';
import './styles/table-actions.css';
import { permissionDirective } from './utils/permission';
import { formatStatusLabel } from './utils/statusLabel';
import { tablePagination } from './utils/tablePagination';

dayjs.locale('zh-cn');

const app = createApp(App);

app.config.globalProperties.$statusLabel = formatStatusLabel;
app.config.globalProperties.$tablePagination = tablePagination;
app.use(Antd).use(router).directive('permission', permissionDirective);

void router.isReady().then(() => {
  app.mount('#app');
});
