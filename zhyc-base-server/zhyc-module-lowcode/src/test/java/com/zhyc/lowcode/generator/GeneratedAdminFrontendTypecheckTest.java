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
 * 生成后台前端产物类型检查测试。
 *
 * <p>该测试将生成的后台 API、页面和路由写入临时 Vue 工程，并执行真实 vue-tsc 类型检查，
 * 用于发现静态字符串校验无法覆盖的 TypeScript、Vue SFC 和路径别名问题。</p>
 */
class GeneratedAdminFrontendTypecheckTest {

  /** 生成文件临时输出根目录。 */
  @TempDir
  private Path outputRoot;

  /**
   * 验证后台前端生成产物可以通过 vue-tsc 类型检查。
   *
   * @throws IOException 创建临时工程失败时抛出
   * @throws InterruptedException 类型检查进程被中断时抛出
   */
  @Test
  void shouldPassVueTypecheckForGeneratedAdminFrontendArtifacts()
      throws IOException, InterruptedException {
    Path projectRoot = outputRoot.resolve("zhyc-base-vue");
    createMinimalVueProject(projectRoot);
    linkNodeModules(projectRoot);
    List<GeneratedFile> files = generateAdminFrontendFiles();
    new FileSystemGeneratedFileWriter(outputRoot).write(files, GeneratedFileOverwriteStrategy.OVERWRITE);

    Path vueTsc = findWorkspaceTool("zhyc-base-vue/node_modules/.bin/vue-tsc");
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
    assertTrue(output.isBlank(), output);
  }

  /**
   * 将真实后台前端依赖链接到临时 Vue 工程。
   *
   * @param projectRoot 临时 Vue 工程根目录
   * @throws IOException 创建依赖软链接失败时抛出
   */
  private void linkNodeModules(Path projectRoot) throws IOException {
    Path nodeModules = findWorkspaceTool("zhyc-base-vue/node_modules");
    assertTrue(Files.isDirectory(nodeModules), "缺少后台前端 node_modules: " + nodeModules);
    Files.createSymbolicLink(projectRoot.resolve("node_modules"), nodeModules);
  }

  /**
   * 从当前工作目录向上查找工作区内的工具或目录。
   *
   * @param relativePath 相对工作区根目录的工具或目录路径
   * @return 已找到的绝对路径，未找到时返回基于当前目录解析的路径
   */
  private Path findWorkspaceTool(String relativePath) {
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
   * 创建用于类型检查的最小后台 Vue 工程。
   *
   * @param projectRoot 临时 Vue 工程根目录
   * @throws IOException 创建工程文件失败时抛出
   */
  private void createMinimalVueProject(Path projectRoot) throws IOException {
    Files.createDirectories(projectRoot.resolve("src/api"));
    Files.createDirectories(projectRoot.resolve("src/utils"));
    Files.createDirectories(projectRoot.resolve("src/router/routes/modules"));
    Files.writeString(projectRoot.resolve("package.json"), """
        {
          "type": "module"
        }
        """);
    Files.writeString(projectRoot.resolve("tsconfig.json"), """
        {
          "compilerOptions": {
            "target": "ES2022",
            "useDefineForClassFields": true,
            "module": "ESNext",
            "moduleResolution": "Bundler",
            "strict": true,
            "jsx": "preserve",
            "sourceMap": true,
            "resolveJsonModule": true,
            "isolatedModules": true,
            "esModuleInterop": true,
            "skipLibCheck": true,
            "lib": ["ES2022", "DOM", "DOM.Iterable"],
            "types": ["vite/client"],
            "baseUrl": ".",
            "paths": {
              "@/*": ["src/*"]
            }
          },
          "include": ["src/**/*.ts", "src/**/*.vue"]
        }
        """);
    Files.writeString(projectRoot.resolve("src/api/http.ts"), """
        /**
         * 测试用后台请求参数。
         */
        export interface HttpRequestOptions<TBody = unknown> {
          /** 请求方法。 */
          method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
          /** 请求体。 */
          body?: TBody;
          /** 查询参数。 */
          query?: Record<string, string | number | boolean | undefined>;
          /** 请求头。 */
          headers?: Record<string, string>;
        }

        /**
         * 测试用后台分页响应结构。
         */
        export interface PageResult<T> {
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
         * 测试用后台请求函数。
         *
         * @param path 请求路径
         * @param options 请求参数
         * @returns 类型化响应
         */
        export function request<TResponse, TBody = unknown>(
          path: string,
          options: HttpRequestOptions<TBody> = {},
        ): Promise<TResponse> {
          void path;
          void options;
          return Promise.resolve(undefined as TResponse);
        }
        """);
    Files.writeString(projectRoot.resolve("src/utils/adminContext.ts"), """
        /**
         * 测试用后台租户编码读取函数。
         *
         * @returns 租户业务编码
         */
        export function requireAdminTenantId(): string {
          return 'tenant_a';
        }
        """);
  }

  /**
   * 生成后台管理端前端代码产物。
   *
   * @return 生成后的文件列表
   */
  private List<GeneratedFile> generateAdminFrontendFiles() {
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new SimpleStringTemplateRenderer());
    return generator.generate(new CodeGenerationRequest(
        GenerationTarget.ADMIN_FRONTEND, "purchase", "purchaseOrder", purchaseOrderTable()));
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
