/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 生成 uni-app 产物构建测试。
 *
 * <p>该测试将生成的移动端 API、页面和页面注册文件写入临时 uni-app 工程，并执行真实
 * uni build，用于发现静态字符串校验无法覆盖的 Vue SFC、路径别名和页面注册问题。</p>
 */
class GeneratedUniappBuildTest {

  /** 生成文件临时输出根目录。 */
  @TempDir
  private Path outputRoot;

  /**
   * 验证移动端生成产物可以通过 uni-app H5 构建。
   *
   * @throws IOException 创建临时工程失败时抛出
   * @throws InterruptedException 构建进程被中断时抛出
   */
  @Test
  void shouldBuildGeneratedUniappArtifacts() throws IOException, InterruptedException {
    Path projectRoot = outputRoot.resolve("zhyc-base-uniapp");
    createMinimalUniappProject(projectRoot);
    linkNodeModules(projectRoot);
    List<GeneratedFile> files = generateUniappFiles();
    new FileSystemGeneratedFileWriter(outputRoot).write(files, GeneratedFileOverwriteStrategy.OVERWRITE);
    promoteGeneratedPagesJson(projectRoot);
    runVueTypecheck(projectRoot);

    Path uni = findWorkspacePath("zhyc-base-uniapp/node_modules/.bin/uni");
    assertTrue(Files.exists(uni), "缺少 uni 可执行文件: " + uni);

    Process process = new ProcessBuilder(uni.toString(), "build")
        .directory(projectRoot.toFile())
        .redirectErrorStream(true)
        .start();
    String output = new String(process.getInputStream().readAllBytes());
    int exitCode = process.waitFor();

    assertEquals(0, exitCode, output);
    assertTrue(Files.exists(projectRoot.resolve("dist/build/h5/index.html")), output);
  }

  /**
   * 执行生成移动端产物的 Vue 类型检查。
   *
   * @param projectRoot 临时 uni-app 工程根目录
   * @throws IOException 启动类型检查进程失败时抛出
   * @throws InterruptedException 类型检查进程被中断时抛出
   */
  private void runVueTypecheck(Path projectRoot) throws IOException, InterruptedException {
    Path vueTsc = findWorkspacePath("zhyc-base-vue/node_modules/.bin/vue-tsc");
    assertTrue(Files.exists(vueTsc), "缺少 vue-tsc 可执行文件: " + vueTsc);
    Process process = new ProcessBuilder(
        vueTsc.toString(),
        "--noEmit",
        "-p",
        "tsconfig.json")
        .directory(projectRoot.toFile())
        .redirectErrorStream(true)
        .start();
    String output = new String(process.getInputStream().readAllBytes());
    int exitCode = process.waitFor();
    assertEquals(0, exitCode, output);
  }

  /**
   * 将真实移动端依赖链接到临时 uni-app 工程。
   *
   * @param projectRoot 临时 uni-app 工程根目录
   * @throws IOException 创建依赖软链接失败时抛出
   */
  private void linkNodeModules(Path projectRoot) throws IOException {
    Path nodeModules = findWorkspacePath("zhyc-base-uniapp/node_modules");
    assertTrue(Files.isDirectory(nodeModules), "缺少移动端 node_modules: " + nodeModules);
    Files.createSymbolicLink(projectRoot.resolve("node_modules"), nodeModules);
  }

  /**
   * 从当前工作目录向上查找工作区内的路径。
   *
   * @param relativePath 相对工作区根目录的路径
   * @return 已找到的绝对路径，未找到时返回基于当前目录解析的路径
   */
  private Path findWorkspacePath(String relativePath) {
    Path current = Path.of("").toAbsolutePath().normalize();
    while (current != null) {
      Path candidate = current.resolve(relativePath).normalize();
      if (Files.exists(candidate)) {
        return candidate;
      }
      current = current.getParent();
    }
    return Path.of(relativePath).toAbsolutePath().normalize();
  }

  /**
   * 创建用于构建校验的最小 uni-app 工程。
   *
   * @param projectRoot 临时 uni-app 工程根目录
   * @throws IOException 创建工程文件失败时抛出
   */
  private void createMinimalUniappProject(Path projectRoot) throws IOException {
    Files.createDirectories(projectRoot.resolve("src/api"));
    Files.createDirectories(projectRoot.resolve("src/components"));
    Files.createDirectories(projectRoot.resolve("src/styles"));
    Files.createDirectories(projectRoot.resolve("src/utils"));
    Files.copy(findWorkspacePath("zhyc-base-uniapp/src/components/MobilePageTopBar.vue"),
        projectRoot.resolve("src/components/MobilePageTopBar.vue"));
    Files.copy(findWorkspacePath("zhyc-base-uniapp/src/components/MobileState.vue"),
        projectRoot.resolve("src/components/MobileState.vue"));
    Files.copy(findWorkspacePath("zhyc-base-uniapp/src/styles/mobile-design.css"),
        projectRoot.resolve("src/styles/mobile-design.css"));
    Files.writeString(projectRoot.resolve("package.json"), """
        {
          "type": "module"
        }
        """);
    Files.writeString(projectRoot.resolve("index.html"), """
        <div id="app"></div>
        <script type="module" src="/src/main.ts"></script>
        """);
    Files.writeString(projectRoot.resolve("tsconfig.json"), """
        {
          "compilerOptions": {
            "target": "ES2022",
            "module": "ESNext",
            "moduleResolution": "Bundler",
            "strict": true,
            "jsx": "preserve",
            "resolveJsonModule": true,
            "isolatedModules": true,
            "esModuleInterop": true,
            "lib": ["ES2022", "DOM"],
            "baseUrl": ".",
            "paths": {
              "@/*": ["src/*"]
            }
          },
          "include": ["src/**/*.ts", "src/**/*.vue", "src/**/*.d.ts"]
        }
        """);
    Files.writeString(projectRoot.resolve("vite.config.ts"), """
        import uniPlugin from '@dcloudio/vite-plugin-uni';
        import { defineConfig } from 'vite';

        const uni = typeof uniPlugin === 'function' ? uniPlugin : uniPlugin.default;

        export default defineConfig({
          plugins: [uni()],
        });
        """);
    Files.writeString(projectRoot.resolve("src/manifest.json"), """
        {
          "name": "ZHYC 临时构建",
          "appid": "__UNI__ZHYC_TEMP",
          "description": "低代码生成产物临时构建工程",
          "versionName": "1.0.0",
          "versionCode": "100"
        }
        """);
    Files.writeString(projectRoot.resolve("src/main.ts"), """
        import { createSSRApp } from 'vue';

        import App from './App.vue';

        /**
         * 创建临时 uni-app 实例。
         *
         * @returns uni-app 应用实例
         */
        export function createApp() {
          const app = createSSRApp(App);
          return {
            app,
          };
        }
        """);
    Files.writeString(projectRoot.resolve("src/App.vue"), """
        <script setup lang="ts">
        /**
         * 低代码生成物临时构建根组件。
         */
        </script>

        <style>
        @import './styles/mobile-design.css';

        page {
          min-height: 100%;
        }
        </style>
        """);
    Files.writeString(projectRoot.resolve("src/env.d.ts"), """
        declare module '*.vue' {
          import type { DefineComponent } from 'vue';

          const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>;
          export default component;
        }

        declare const uni: {
          showToast(options: { title: string; icon: 'success' | 'error' | 'none' }): void;
          navigateTo(options: { url: string }): void;
          navigateBack(): void;
          switchTab(options: { url: string; fail?: () => void }): void;
          reLaunch(options: { url: string }): void;
        };

        declare function getCurrentPages(): unknown[];
        """);
    Files.writeString(projectRoot.resolve("src/api/request.ts"), """
        /**
         * 测试用移动端请求参数。
         */
        export interface MobileRequestOptions<TBody = unknown> {
          /** 请求方法。 */
          method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
          /** 请求体。 */
          data?: TBody;
          /** 查询参数。 */
          query?: Record<string, string | number | boolean | undefined>;
          /** 请求头。 */
          headers?: Record<string, string>;
        }

        /**
         * 测试用移动端分页响应结构。
         */
        export interface MobilePageResult<T> {
          /** 总记录数。 */
          total: number;
          /** 当前页码。 */
          pageNo: number;
          /** 每页记录数。 */
          pageSize: number;
          /** 当前页记录。 */
          records: T[];
        }

        /**
         * 测试用移动端请求函数。
         *
         * @param path 请求路径
         * @param options 请求参数
         * @returns 类型化响应
         */
        export function mobileRequest<TResponse, TBody = unknown>(
          path: string,
          options: MobileRequestOptions<TBody> = {},
        ): Promise<TResponse> {
          void path;
          void options;
          return Promise.resolve(undefined as TResponse);
        }
        """);
    Files.writeString(projectRoot.resolve("src/api/workflow.ts"), """
        /**
         * 测试用移动端审批命令。
         */
        export interface MobileWorkflowHandleCommand {
          /** 审批意见。 */
          comment: string;
          /** 流程变量。 */
          variables?: Record<string, unknown>;
        }

        /**
         * 测试用移动端撤回命令。
         */
        export interface MobileWorkflowRevokeCommand {
          /** 撤回原因。 */
          reason: string;
        }

        /**
         * 测试用移动端审批通过入口。
         *
         * @param taskId 工作流任务 ID
         * @param command 审批命令
         */
        export function approveTask(taskId: string, command: MobileWorkflowHandleCommand): Promise<void> {
          void taskId;
          void command;
          return Promise.resolve();
        }

        /**
         * 测试用移动端驳回入口。
         *
         * @param taskId 工作流任务 ID
         * @param command 驳回命令
         */
        export function rejectTask(taskId: string, command: MobileWorkflowHandleCommand): Promise<void> {
          void taskId;
          void command;
          return Promise.resolve();
        }

        /**
         * 测试用移动端撤回入口。
         *
         * @param processInstanceId 流程实例 ID
         * @param command 撤回命令
         */
        export function revokeMobileTask(processInstanceId: string, command: MobileWorkflowRevokeCommand): Promise<void> {
          void processInstanceId;
          void command;
          return Promise.resolve();
        }
        """);
    Files.writeString(projectRoot.resolve("src/utils/platform.ts"), """
        /**
         * 测试用移动端用户上下文。
         */
        export interface MobileUserContext {
          /** 当前租户业务编码。 */
          tenantId: string;
          /** 当前登录用户 ID。 */
          userId: number | null;
          /** 当前登录部门 ID。 */
          orgId: number | null;
          /** 当前登录账号。 */
          accountName: string;
          /** 当前角色名称。 */
          roleName: string;
          /** 当前访问令牌。 */
          accessToken: string;
          /** 是否已登录。 */
          loggedIn: boolean;
        }

        /**
         * 测试用移动端用户上下文读取函数。
         *
         * @returns 移动端用户上下文
         */
        export function getMobileUserContext(): MobileUserContext {
          return {
            tenantId: 'tenant_a',
            userId: 1001,
            orgId: 2001,
            accountName: 'tester',
            roleName: '测试人员',
            accessToken: 'test-token',
            loggedIn: true,
          };
        }

        /**
         * 测试用移动端用户 ID 校验函数。
         *
         * @param context 移动端用户上下文
         * @returns 当前用户 ID
         */
        export function requireMobileUserId(context: MobileUserContext = getMobileUserContext()): number {
          if (context.userId === null) {
            throw new Error('移动端用户 ID 缺失');
          }
          return context.userId;
        }

        /**
         * 测试用移动端租户读取函数。
         *
         * @param context 移动端用户上下文
         * @returns 租户业务编码
         */
        export function requireMobileTenantId(context: MobileUserContext = getMobileUserContext()): string {
          return context.tenantId;
        }

        /**
         * 测试用移动端上下文错误识别函数。
         *
         * @param message 错误消息
         * @returns 是否为上下文缺失错误
         */
        export function isMobileContextErrorMessage(message: string): boolean {
          return /登录|租户|用户|令牌|上下文/.test(message);
        }

        /**
         * 测试用移动端服务错误识别函数。
         *
         * @param message 错误消息
         * @returns 是否为服务或网络错误
         */
        export function isMobileServiceErrorMessage(message: string): boolean {
          return /服务|网络|超时|接口/.test(message);
        }

        /**
         * 测试用移动端确认弹窗。
         *
         * @param title 弹窗标题
         * @param content 弹窗内容
         * @returns 是否确认
         */
        export function showConfirm(title: string, content: string): Promise<boolean> {
          void title;
          void content;
          return Promise.resolve(true);
        }

        /**
         * 测试用移动端轻提示。
         *
         * @param title 提示内容
         * @param icon 提示图标
         */
        export function showMobileToast(title: string, icon: 'success' | 'error' | 'none' = 'none'): void {
          void title;
          void icon;
        }
        """);
  }

  /**
   * 将生成的页面注册片段提升为临时工程主 pages.json。
   *
   * @param projectRoot 临时 uni-app 工程根目录
   * @throws IOException 复制页面注册文件失败时抛出
   */
  private void promoteGeneratedPagesJson(Path projectRoot) throws IOException {
    Path generatedPagesJson = projectRoot.resolve("src/generated-pages/purchase-purchaseOrder.pages.json");
    assertTrue(Files.exists(generatedPagesJson), "缺少生成页面注册文件: " + generatedPagesJson);
    Files.copy(generatedPagesJson, projectRoot.resolve("src/pages.json"));
  }

  /**
   * 生成移动端代码产物。
   *
   * @return 生成后的文件列表
   */
  private List<GeneratedFile> generateUniappFiles() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new SimpleStringTemplateRenderer());
    return generator.generate(new CodeGenerationRequest(
        GenerationTarget.UNIAPP, "purchase", "purchaseOrder", purchaseOrderTable()));
  }

  /**
   * 构造采购订单测试表模型。
   *
   * @return 采购订单测试表模型
   */
  private LowcodeTableModel purchaseOrderTable() {
    return new LowcodeTableModel(
        1L, "tenant_a", "purchase_order", "采购订单", "pur_order",
        List.of(
            LowcodeColumnModel.builder("id", "主键", LowcodeFieldType.LONG)
                .primaryKey(true)
                .required(true)
                .build(),
            LowcodeColumnModel.builder("order_no", "订单编号", LowcodeFieldType.STRING)
                .length(64)
                .required(true)
                .build()));
  }
}
