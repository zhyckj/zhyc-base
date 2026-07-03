/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';

/**
 * 拆分 Ant Design Vue 依赖包，降低单个 vendor chunk 体积。
 *
 * @param id 模块路径
 * @returns 分包名称
 */
function resolveAntDesignChunk(id: string): string | undefined {
  const normalizedId = id.replace(/\\/g, '/').toLowerCase();
  if (normalizedId.includes('/@ant-design/icons-vue/')) {
    return 'vendor-ant-icons';
  }
  if (normalizedId.includes('/@ant-design/')) {
    return 'vendor-ant-shared';
  }
  return normalizedId.includes('/ant-design-vue/') ? 'vendor-ant-design' : undefined;
}

/**
 * 拆分 LogicFlow 流程编排依赖，避免画布运行时进入 Vue 基础包。
 *
 * @param id 模块路径
 * @returns 分包名称
 */
function resolveLogicFlowChunk(id: string): string | undefined {
  const normalizedId = id.replace(/\\/g, '/').toLowerCase();
  return normalizedId.includes('/@logicflow/') ? 'vendor-logicflow' : undefined;
}

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  build: {
    /**
     * 首期后台端全局注册 Ant Design Vue，基础 UI 依赖包大于 Vite 默认 500k 阈值属于已知预算。
     * 后续接入按需组件注册或 Vben 组件体系时，再收紧该阈值。
     */
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        /**
         * 拆分后台管理端基础依赖，避免所有页面共享代码被打进单个首屏包。
         *
         * @param id 模块路径
         * @returns 分包名称
         */
        manualChunks(id): string | undefined {
          if (!id.includes('node_modules')) {
            return undefined;
          }
          const antDesignChunk = resolveAntDesignChunk(id);
          if (antDesignChunk) {
            return antDesignChunk;
          }
          const logicFlowChunk = resolveLogicFlowChunk(id);
          if (logicFlowChunk) {
            return logicFlowChunk;
          }
          if (id.includes('vue') || id.includes('vue-router') || id.includes('pinia')) {
            return 'vendor-vue';
          }
          return 'vendor-common';
        },
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
});
