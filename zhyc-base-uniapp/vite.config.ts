/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import uniPlugin from '@dcloudio/vite-plugin-uni';
import { defineConfig } from 'vite';

const uni = typeof uniPlugin === 'function' ? uniPlugin : uniPlugin.default;

export default defineConfig({
  plugins: [uni()],
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/u, ''),
      },
      '/auth-center': {
        target: 'http://127.0.0.1:8090',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/auth-center/u, ''),
      },
    },
  },
});
