/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.zhyc.lowcode.db.LowcodeFieldType;
import com.zhyc.lowcode.metadata.domain.LowcodeColumnModel;
import com.zhyc.lowcode.metadata.domain.LowcodeTableModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 生成后端源码编译测试。
 *
 * <p>该测试把低代码生成结果落盘到临时目录，并用最小框架桩编译生成出的后端源码，
 * 避免模板语法、导入和方法签名问题进入后续阶段。</p>
 */
class GeneratedBackendCompilationTest {

  /** 生成文件临时输出根目录。 */
  @TempDir
  private Path outputRoot;

  /**
   * 验证生成出的后台后端源码可以通过 Java 编译。
   *
   * @throws IOException 写入临时源码文件失败时抛出
   */
  @Test
  void shouldCompileGeneratedAdminBackendSources() throws IOException {
    compileGeneratedSources(GenerationTarget.ADMIN_BACKEND, false, "生成后台后端源码必须可以编译");
  }

  /**
   * 验证生成出的后台后端测试源码可以通过 Java 编译。
   *
   * @throws IOException 写入临时源码文件失败时抛出
   */
  @Test
  void shouldCompileGeneratedAdminBackendTestSources() throws IOException {
    compileGeneratedSources(GenerationTarget.ADMIN_BACKEND, true, "生成后台后端测试源码必须可以编译");
  }

  /**
   * 验证生成出的开放 API 后端源码可以通过 Java 编译。
   *
   * @throws IOException 写入临时源码文件失败时抛出
   */
  @Test
  void shouldCompileGeneratedOpenApiBackendSources() throws IOException {
    compileGeneratedSources(GenerationTarget.OPEN_API_PORTAL, false, "生成开放 API 后端源码必须可以编译");
  }

  /**
   * 验证生成出的微服务模块源码可以通过 Java 编译。
   *
   * @throws IOException 写入临时源码文件失败时抛出
   */
  @Test
  void shouldCompileGeneratedMicroserviceModuleSources() throws IOException {
    compileGeneratedSources(GenerationTarget.MICROSERVICE_MODULE, false, "生成微服务模块源码必须可以编译");
  }

  /**
   * 验证同一测试内连续编译不同生成目标时使用独立输出目录。
   *
   * @throws IOException 写入或扫描临时源码文件失败时抛出
   */
  @Test
  void shouldIsolateGeneratedCompilationOutputByInvocation() throws IOException {
    compileGeneratedSources(GenerationTarget.ADMIN_BACKEND, false, "生成后台后端源码必须可以编译");
    compileGeneratedSources(GenerationTarget.OPEN_API_PORTAL, false, "生成开放 API 后端源码必须可以编译");

    long compileDirectoryCount;
    try (Stream<Path> paths = Files.list(outputRoot)) {
      compileDirectoryCount = paths
          .filter(Files::isDirectory)
          .filter(path -> path.getFileName().toString().startsWith("compile-"))
          .count();
    }

    assertEquals(2L, compileDirectoryCount, "每次生成源码编译必须使用独立临时目录");
  }

  /**
   * 验证同一测试内重复编译相同生成目标时仍使用独立输出目录。
   *
   * @throws IOException 写入或扫描临时源码文件失败时抛出
   */
  @Test
  void shouldIsolateRepeatedGeneratedCompilationOutputForSameTarget() throws IOException {
    compileGeneratedSources(GenerationTarget.ADMIN_BACKEND, false, "第一次生成后台后端源码必须可以编译");
    compileGeneratedSources(GenerationTarget.ADMIN_BACKEND, false, "第二次生成后台后端源码必须可以编译");

    long compileDirectoryCount;
    try (Stream<Path> paths = Files.list(outputRoot)) {
      compileDirectoryCount = paths
          .filter(Files::isDirectory)
          .filter(path -> path.getFileName().toString().startsWith("compile-admin-backend-main"))
          .count();
    }

    assertEquals(2L, compileDirectoryCount, "重复编译相同生成目标也必须使用独立临时目录");
  }

  /**
   * 编译指定生成目标输出的 Java 源码。
   *
   * @param target 生成目标
   * @param includeTestSources 是否同时编译测试源码
   * @param failureMessage 编译失败提示
   * @throws IOException 写入或扫描临时源码文件失败时抛出
   */
  private void compileGeneratedSources(GenerationTarget target, boolean includeTestSources, String failureMessage)
      throws IOException {
    String compileDirectoryPrefix = "compile-" + target.getCode() + "-" + (includeTestSources ? "test" : "main")
        + "-";
    Path compileRoot = Files.createTempDirectory(outputRoot, compileDirectoryPrefix);
    CodeTemplateRegistry registry = new CodeTemplateRegistry(List.of(new BuiltInCodeTemplateProvider()));
    CodeGenerator generator = new DefaultCodeGenerator(registry, new SimpleStringTemplateRenderer());
    List<GeneratedFile> files = generator.generate(new CodeGenerationRequest(target, "purchase", "purchaseOrder",
        purchaseOrderTable()));
    new FileSystemGeneratedFileWriter(compileRoot).write(files, GeneratedFileOverwriteStrategy.OVERWRITE);
    writeFrameworkStubs(compileRoot);

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "当前 JDK 必须提供 JavaCompiler");
    List<String> javaSources;
    try (Stream<Path> paths = Files.walk(compileRoot)) {
      javaSources = paths
          .filter(path -> path.toString().endsWith(".java"))
          .filter(path -> path.toString().contains("/src/main/java/")
              || (includeTestSources && path.toString().contains("/src/test/java/"))
              || path.toString().contains("/stubs/"))
          .map(Path::toString)
          .sorted()
          .toList();
    }

    int exitCode = compiler.run(null, null, null,
        concat(List.of("--release", "21", "-proc:none", "-d", compileRoot.resolve("classes").toString()),
            javaSources).toArray(String[]::new));

    assertEquals(0, exitCode, failureMessage);
  }

  private List<String> concat(List<String> options, List<String> sources) {
    return Stream.concat(options.stream(), sources.stream()).toList();
  }

  private void writeFrameworkStubs(Path root) throws IOException {
    writeStub(root, "stubs/com/zhyc/common/api/ApiResult.java", """
        package com.zhyc.common.api;

        /** 统一响应结构测试桩。 */
        public class ApiResult<T> {
          public static <T> ApiResult<T> ok(T data) {
            return new ApiResult<>();
          }
        }
        """);
    writeStub(root, "stubs/com/zhyc/common/exception/BusinessException.java", """
        package com.zhyc.common.exception;

        /** 业务异常测试桩。 */
        public class BusinessException extends RuntimeException {
          private final String code;

          public BusinessException(String code, String message) {
            super(message);
            this.code = code;
          }

          public String getCode() {
            return code;
          }
        }
        """);
    writeStub(root, "stubs/org/apache/shiro/authz/annotation/RequiresPermissions.java", """
        package org.apache.shiro.authz.annotation;

        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;

        /** Shiro 权限注解测试桩。 */
        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.METHOD, ElementType.TYPE})
        public @interface RequiresPermissions {
          String[] value();
        }
        """);
    writeSpringWebAnnotation(root, "DeleteMapping");
    writeSpringWebAnnotation(root, "GetMapping");
    writeSpringWebAnnotation(root, "PostMapping");
    writeSpringWebAnnotation(root, "PutMapping");
    writeSpringWebAnnotation(root, "RequestBody");
    writeSpringWebAnnotation(root, "RequestHeader", "boolean required() default true;");
    writeSpringWebAnnotation(root, "RequestMapping");
    writeSpringWebAnnotation(root, "RestController");
    writeSpringWebAnnotation(root, "PathVariable");
    writeSpringStereotypeAnnotation(root, "Service");
    writeSpringStereotypeAnnotation(root, "Repository");
    writeJakartaValidationAnnotation(root, "Valid", "");
    writeJakartaValidationAnnotation(root, "constraints/NotBlank", "String message() default \"\";");
    writeJakartaValidationAnnotation(root, "constraints/NotEmpty", "String message() default \"\";");
    writeMyBatisAnnotation(root, "InsertProvider", "Class<?> type(); String method();");
    writeMyBatisAnnotation(root, "Mapper", "");
    writeMyBatisAnnotation(root, "Param", "String value();");
    writeMyBatisAnnotation(root, "SelectProvider", "Class<?> type(); String method();");
    writeMyBatisAnnotation(root, "UpdateProvider", "Class<?> type(); String method();");
  }

  private void writeSpringWebAnnotation(Path root, String name) throws IOException {
    writeSpringWebAnnotation(root, name, "");
  }

  private void writeSpringWebAnnotation(Path root, String name, String body) throws IOException {
    writeStub(root, "stubs/org/springframework/web/bind/annotation/" + name + ".java", """
        package org.springframework.web.bind.annotation;

        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;

        /** Spring Web 注解测试桩。 */
        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
        public @interface %s {
          String value() default "";
          %s
        }
        """.formatted(name, body));
  }

  private void writeSpringStereotypeAnnotation(Path root, String name) throws IOException {
    writeStub(root, "stubs/org/springframework/stereotype/" + name + ".java", """
        package org.springframework.stereotype;

        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;

        /** Spring 组件注解测试桩。 */
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        public @interface %s {
        }
        """.formatted(name));
  }

  private void writeMyBatisAnnotation(Path root, String name, String body) throws IOException {
    writeStub(root, "stubs/org/apache/ibatis/annotations/" + name + ".java", """
        package org.apache.ibatis.annotations;

        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;

        /** MyBatis 注解测试桩。 */
        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
        public @interface %s {
          %s
        }
        """.formatted(name, body));
  }

  private void writeJakartaValidationAnnotation(Path root, String name, String body) throws IOException {
    String simpleName = name.substring(name.lastIndexOf('/') + 1);
    String packageName = name.contains("/") ? "jakarta.validation.constraints" : "jakarta.validation";
    writeStub(root, "stubs/jakarta/validation/" + name + ".java", """
        package %s;

        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;

        /** Jakarta Validation 注解测试桩。 */
        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
        public @interface %s {
          %s
        }
        """.formatted(packageName, simpleName, body));
  }

  private void writeStub(Path root, String relativePath, String content) throws IOException {
    Path file = root.resolve(relativePath);
    Files.createDirectories(file.getParent());
    Files.writeString(file, content);
  }

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
