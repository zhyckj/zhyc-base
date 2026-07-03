/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.generator;

import java.util.List;

  /**
   * 内置代码模板提供者。
   */
public class BuiltInCodeTemplateProvider implements CodeTemplateProvider {

  /**
   * 列出平台内置代码模板。
   *
   * <p>覆盖后台后端、后台前端、uni-app、开放 API 和微服务模块模板，是首期代码生成范围的模板入口。</p>
   *
   * @return 内置代码模板描述列表
   */
  @Override
  public List<CodeTemplateDescriptor> listTemplates() {
    return List.of(
        adminBackendControllerTemplate(),
        adminBackendServiceTemplate(),
        adminBackendServiceImplTemplate(),
        adminBackendRepositoryTemplate(),
        adminBackendMyBatisRepositoryTemplate(),
        adminBackendDtoTemplate(),
        adminBackendSaveRequestTemplate(),
        adminBackendResponseTemplate(),
        adminBackendMapperTemplate(),
        adminBackendSqlProviderTemplate(),
        adminBackendDdlTemplate(),
        adminBackendTestTemplate(),
        adminFrontendListTemplate(),
        adminFrontendFormTemplate(),
        adminFrontendDetailTemplate(),
        adminFrontendApiTemplate(),
        adminFrontendRouteTemplate(),
        uniappListTemplate(),
        uniappFormTemplate(),
        uniappDetailTemplate(),
        uniappApiTemplate(),
        uniappPagesJsonTemplate(),
        openApiControllerTemplate(),
        openApiDtoTemplate(),
        openApiSignatureConfigTemplate(),
        openApiRegistrationSqlTemplate(),
        openApiDocTemplate(),
        openApiPortalDebugApiTemplate(),
        openApiPortalPageTemplate(),
        microservicePomTemplate(),
        microserviceApplicationTemplate(),
        microserviceControllerTemplate(),
        microserviceModuleDescriptorTemplate(),
        microserviceApplicationYamlTemplate(),
        microserviceReadmeTemplate());
  }

  private CodeTemplateDescriptor adminBackendControllerTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-controller",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端 Controller",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/controller/{Entity}Controller.java",
        """
            package com.zhyc.{module}.controller;

            import com.zhyc.common.api.ApiResult;
            import com.zhyc.common.api.PageResult;
            import com.zhyc.{module}.dto.{Entity}Response;
            import com.zhyc.{module}.dto.{Entity}SaveRequest;
            import com.zhyc.{module}.service.{Entity}Service;
            import jakarta.validation.Valid;
            import org.apache.shiro.authz.annotation.RequiresPermissions;
            import org.springframework.web.bind.annotation.DeleteMapping;
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.PathVariable;
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.PutMapping;
            import org.springframework.web.bind.annotation.RequestBody;
            import org.springframework.web.bind.annotation.RequestHeader;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RequestParam;
            import org.springframework.web.bind.annotation.RestController;

            /**
             * {table} 后台管理接口。
             */
            @RestController
            @RequestMapping("/{module}/{entity}")
            public class {Entity}Controller {

              /** {table} 后台管理服务。 */
              private final {Entity}Service {entity}Service;

              /**
               * 创建 {table} 后台管理接口。
               *
               * @param {entity}Service {table} 后台管理服务
               */
              public {Entity}Controller({Entity}Service {entity}Service) {
                this.{entity}Service = {entity}Service;
              }

              /**
               * 查询 {table} 列表。
               *
               * @param tenantId 租户业务编码
               * @param pageNo 当前页码，从 1 开始
               * @param pageSize 每页记录数
               * @return {table} 分页字段清单
               */
              @RequiresPermissions("{module}:{entity}:query")
              @GetMapping
              public ApiResult<PageResult<{Entity}Response>> list(
                  @RequestHeader("X-ZHYC-Tenant-Id") String tenantId,
                  @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
                  @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
                return ApiResult.ok({entity}Service.list(tenantId, pageNo, pageSize));
              }

              /**
               * 查询 {table} 详情。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @return {table} 详情响应
               */
              @RequiresPermissions("{module}:{entity}:query")
              @GetMapping("/{id}")
              public ApiResult<{Entity}Response> detail(
                  @RequestHeader("X-ZHYC-Tenant-Id") String tenantId,
                  @PathVariable("id") Long id) {
                return ApiResult.ok({entity}Service.detail(tenantId, id));
              }

              /**
               * 保存 {table} 表单。
               *
               * @param tenantId 租户业务编码
               * @param request {table} 保存请求
               * @return {table} 保存后的响应
               */
              @RequiresPermissions("{module}:{entity}:save")
              @PostMapping
              public ApiResult<{Entity}Response> save(
                  @RequestHeader("X-ZHYC-Tenant-Id") String tenantId,
                  @Valid @RequestBody {Entity}SaveRequest request) {
                return ApiResult.ok({entity}Service.save(tenantId, request));
              }

              /**
               * 更新 {table} 表单。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @param request {table} 保存请求
               * @return {table} 更新后的响应
               */
              @RequiresPermissions("{module}:{entity}:save")
              @PutMapping("/{id}")
              public ApiResult<{Entity}Response> update(
                  @RequestHeader("X-ZHYC-Tenant-Id") String tenantId,
                  @PathVariable("id") Long id,
                  @Valid @RequestBody {Entity}SaveRequest request) {
                return ApiResult.ok({entity}Service.update(tenantId, id, request));
              }

              /**
               * 逻辑删除 {table}。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @return 删除结果
               */
              @RequiresPermissions("{module}:{entity}:delete")
              @DeleteMapping("/{id}")
              public ApiResult<Void> delete(
                  @RequestHeader("X-ZHYC-Tenant-Id") String tenantId,
                  @PathVariable("id") Long id) {
                {entity}Service.delete(tenantId, id);
                return ApiResult.ok(null);
              }
            }
            """);
  }

  private CodeTemplateDescriptor adminBackendServiceTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-service",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端 Service",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/service/{Entity}Service.java",
        """
            package com.zhyc.{module}.service;

            import com.zhyc.common.api.PageResult;
            import com.zhyc.{module}.dto.{Entity}Response;
            import com.zhyc.{module}.dto.{Entity}SaveRequest;

            /**
             * {table} 后台管理服务接口。
             */
            public interface {Entity}Service {

              /**
               * 查询租户内 {table} 字段编码。
               *
               * @param tenantId 租户业务编码
               * @param pageNo 当前页码，从 1 开始
               * @param pageSize 每页记录数
               * @return {table} 分页列表数据
               */
              PageResult<{Entity}Response> list(String tenantId, int pageNo, int pageSize);

              /**
               * 查询租户内 {table} 详情。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @return {table} 详情数据
               */
              {Entity}Response detail(String tenantId, Long id);

              /**
               * 保存租户内 {table} 表单。
               *
               * @param tenantId 租户业务编码
               * @param request {table} 保存请求
               * @return 保存后的 {table} 响应
               */
              {Entity}Response save(String tenantId, {Entity}SaveRequest request);

              /**
               * 更新租户内 {table} 表单。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @param request {table} 保存请求
               * @return 更新后的 {table} 响应
               */
              {Entity}Response update(String tenantId, Long id, {Entity}SaveRequest request);

              /**
               * 逻辑删除租户内 {table}。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               */
              void delete(String tenantId, Long id);
            }
            """);
  }

  private CodeTemplateDescriptor adminBackendServiceImplTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-service-impl",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端 Service 实现",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/service/impl/Default{Entity}Service.java",
        """
            package com.zhyc.{module}.service.impl;

            import com.zhyc.common.api.PageResult;
            import com.zhyc.common.exception.BusinessException;
            import com.zhyc.{module}.dto.{Entity}Response;
            import com.zhyc.{module}.dto.{Entity}SaveRequest;
            import com.zhyc.{module}.repository.{Entity}Repository;
            import com.zhyc.{module}.service.{Entity}Service;
            import java.util.List;
            import java.util.Map;
            import org.springframework.stereotype.Service;

            /**
             * {table} 后台管理服务默认实现。
             */
            @Service
            public class Default{Entity}Service implements {Entity}Service {

              /** 单页最大记录数，避免生成模块被过大分页参数拖垮。 */
              private static final int MAX_PAGE_SIZE = 100;

              /** 租户编码缺失错误码。 */
              private static final String ERROR_TENANT_REQUIRED = "ZHYC_GENERATED_TENANT_REQUIRED";

              /** 主键缺失错误码。 */
              private static final String ERROR_ID_REQUIRED = "ZHYC_GENERATED_ID_REQUIRED";

              /** 记录不存在或无权限访问错误码。 */
              private static final String ERROR_RECORD_NOT_FOUND = "ZHYC_GENERATED_RECORD_NOT_FOUND";

              /** 表单值缺失错误码。 */
              private static final String ERROR_FORM_VALUES_REQUIRED = "ZHYC_GENERATED_FORM_VALUES_REQUIRED";

              /** 请求租户与当前租户不一致错误码。 */
              private static final String ERROR_TENANT_MISMATCH = "ZHYC_GENERATED_TENANT_MISMATCH";

              /** {table} 领域仓储，用于隔离业务服务和 MyBatis 细节。 */
              private final {Entity}Repository {entity}Repository;

              /**
               * 创建 {table} 后台管理服务默认实现。
               *
               * @param {entity}Repository {table} 领域仓储
               */
              public Default{Entity}Service({Entity}Repository {entity}Repository) {
                this.{entity}Repository = {entity}Repository;
              }

              /**
               * 查询租户内 {table} 字段编码。
               *
               * @param tenantId 租户业务编码
               * @param pageNo 当前页码，从 1 开始
               * @param pageSize 每页记录数
               * @return {table} 分页列表数据
               */
              @Override
              public PageResult<{Entity}Response> list(String tenantId, int pageNo, int pageSize) {
                if (tenantId == null || tenantId.isBlank()) {
                  throw new BusinessException(ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
                }
                int normalizedPageNo = Math.max(pageNo, 1);
                int normalizedPageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
                long offset = (long) (normalizedPageNo - 1) * normalizedPageSize;
                long totalCount = {entity}Repository.countByTenantId(tenantId);
                List<{Entity}Response> records = {entity}Repository.findByTenantId(tenantId, offset, normalizedPageSize).stream()
                    .map(row -> new {Entity}Response(String.valueOf(row.get("id")), tenantId, String.valueOf(row.get("{firstDataField}")), totalCount))
                    .toList();
                return PageResult.of(totalCount, normalizedPageNo, normalizedPageSize, records);
              }

              /**
               * 查询租户内 {table} 详情。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @return {table} 详情数据
               */
              @Override
              public {Entity}Response detail(String tenantId, Long id) {
                if (tenantId == null || tenantId.isBlank()) {
                  throw new BusinessException(ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
                }
                if (id == null) {
                  throw new BusinessException(ERROR_ID_REQUIRED, "{table}主键不能为空");
                }
                Map<String, Object> row = {entity}Repository.findById(tenantId, id);
                if (row == null || row.isEmpty()) {
                  throw new BusinessException(ERROR_RECORD_NOT_FOUND, "{table}不存在或无权限访问");
                }
                long totalCount = {entity}Repository.countByTenantId(tenantId);
                return new {Entity}Response(String.valueOf(row.get("id")), tenantId, String.valueOf(row.get("{firstDataField}")), totalCount);
              }

              /**
               * 保存租户内 {table} 表单。
               *
               * @param tenantId 租户业务编码
               * @param request {table} 保存请求
               * @return 保存后的 {table} 响应
               */
              @Override
              public {Entity}Response save(String tenantId, {Entity}SaveRequest request) {
                if (tenantId == null || tenantId.isBlank()) {
                  throw new BusinessException(ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
                }
                if (request == null || request.values() == null || request.values().isEmpty()) {
                  throw new BusinessException(ERROR_FORM_VALUES_REQUIRED, "{table}表单值不能为空");
                }
                if (request.tenantId() == null || !tenantId.equals(request.tenantId())) {
                  throw new BusinessException(ERROR_TENANT_MISMATCH, "请求租户与当前租户不一致");
                }
                {entity}Repository.insert(tenantId, request.values());
                long totalCount = {entity}Repository.countByTenantId(tenantId);
                return new {Entity}Response("{entity}-saved", tenantId, request.values().toString(), totalCount);
              }

              /**
               * 更新租户内 {table} 表单。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @param request {table} 保存请求
               * @return 更新后的 {table} 响应
               */
              @Override
              public {Entity}Response update(String tenantId, Long id, {Entity}SaveRequest request) {
                if (tenantId == null || tenantId.isBlank()) {
                  throw new BusinessException(ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
                }
                if (id == null) {
                  throw new BusinessException(ERROR_ID_REQUIRED, "{table}主键不能为空");
                }
                if (request == null || request.values() == null || request.values().isEmpty()) {
                  throw new BusinessException(ERROR_FORM_VALUES_REQUIRED, "{table}表单值不能为空");
                }
                if (request.tenantId() == null || !tenantId.equals(request.tenantId())) {
                  throw new BusinessException(ERROR_TENANT_MISMATCH, "请求租户与当前租户不一致");
                }
                {entity}Repository.updateById(tenantId, id, request.values());
                return detail(tenantId, id);
              }

              /**
               * 逻辑删除租户内 {table}。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               */
              @Override
              public void delete(String tenantId, Long id) {
                if (tenantId == null || tenantId.isBlank()) {
                  throw new BusinessException(ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
                }
                if (id == null) {
                  throw new BusinessException(ERROR_ID_REQUIRED, "{table}主键不能为空");
                }
                {entity}Repository.deleteById(tenantId, id);
              }
            }
            """);
  }

  private CodeTemplateDescriptor adminBackendRepositoryTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-repository",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端 Repository",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/repository/{Entity}Repository.java",
        """
            package com.zhyc.{module}.repository;

            import java.util.Map;
            import java.util.List;

            /**
             * {table} 领域仓储接口。
             *
             * <p>该接口表达业务服务需要的数据访问能力，避免 Service 直接依赖 MyBatis Mapper。</p>
             */
            public interface {Entity}Repository {

              /**
               * 统计租户内未删除的 {table} 数据量。
               *
               * @param tenantId 租户业务编码，用于共享表模式下的数据隔离
               * @return 租户内未删除数据量
               */
              long countByTenantId(String tenantId);

              /**
               * 查询租户内未删除的 {table} 数据。
               *
               * @param tenantId 租户业务编码，用于共享表模式下的数据隔离
               * @param offset 分页偏移量
               * @param pageSize 每页记录数
               * @return 租户内 {table} 行数据
               */
              List<Map<String, Object>> findByTenantId(String tenantId, long offset, int pageSize);

              /**
               * 按主键查询租户内未删除的 {table} 数据。
               *
               * @param tenantId 租户业务编码，用于共享表模式下的数据隔离
               * @param id {table} 主键
               * @return 租户内 {table} 行数据
               */
              Map<String, Object> findById(String tenantId, Long id);

              /**
               * 写入租户内 {table} 表单值。
               *
               * @param tenantId 租户业务编码，用于共享表模式下的数据隔离
               * @param values 已通过模板字段白名单约束的表单值
               */
              void insert(String tenantId, Map<String, Object> values);

              /**
               * 更新租户内 {table} 表单值。
               *
               * @param tenantId 租户业务编码，用于共享表模式下的数据隔离
               * @param id {table} 主键
               * @param values 已通过模板字段白名单约束的表单值
               */
              void updateById(String tenantId, Long id, Map<String, Object> values);

              /**
               * 逻辑删除租户内 {table}。
               *
               * @param tenantId 租户业务编码，用于共享表模式下的数据隔离
               * @param id {table} 主键
               */
              void deleteById(String tenantId, Long id);
            }
            """);
  }

  private CodeTemplateDescriptor adminBackendMyBatisRepositoryTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-mybatis-repository",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端 MyBatis Repository 实现",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/repository/MyBatis{Entity}Repository.java",
            """
            package com.zhyc.{module}.repository;

            import com.zhyc.common.exception.BusinessException;
            import com.zhyc.{module}.mapper.{Entity}Mapper;
            import java.util.List;
            import java.util.Map;
            import org.springframework.stereotype.Repository;

            /**
             * {table} MyBatis 仓储实现。
             *
             * <p>该实现只负责调用 Mapper，业务规则、权限和租户业务校验保留在 Service 层。</p>
             */
            @Repository
            public class MyBatis{Entity}Repository implements {Entity}Repository {

              /** 保存失败错误码。 */
              private static final String ERROR_SAVE_FAILED = "ZHYC_GENERATED_SAVE_FAILED";

              /** 更新失败错误码。 */
              private static final String ERROR_UPDATE_FAILED = "ZHYC_GENERATED_UPDATE_FAILED";

              /** 删除失败错误码。 */
              private static final String ERROR_DELETE_FAILED = "ZHYC_GENERATED_DELETE_FAILED";

              /** {table} MyBatis Mapper。 */
              private final {Entity}Mapper {entity}Mapper;

              /**
               * 创建 {table} MyBatis 仓储实现。
               *
               * @param {entity}Mapper {table} MyBatis Mapper
               */
              public MyBatis{Entity}Repository({Entity}Mapper {entity}Mapper) {
                this.{entity}Mapper = {entity}Mapper;
              }

              /**
               * 统计租户内未删除的 {table} 数据量。
               *
               * @param tenantId 租户业务编码
               * @return 租户内未删除数据量
               */
              @Override
              public long countByTenantId(String tenantId) {
                return {entity}Mapper.countByTenantId(tenantId);
              }

              /**
               * 查询租户内未删除的 {table} 数据。
               *
               * @param tenantId 租户业务编码
               * @param offset 分页偏移量
               * @param pageSize 每页记录数
               * @return 租户内 {table} 行数据
               */
              @Override
              public List<Map<String, Object>> findByTenantId(String tenantId, long offset, int pageSize) {
                return {entity}Mapper.selectByTenantId(tenantId, offset, pageSize);
              }

              /**
               * 按主键查询租户内未删除的 {table} 数据。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @return 租户内 {table} 行数据
               */
              @Override
              public Map<String, Object> findById(String tenantId, Long id) {
                return {entity}Mapper.selectById(tenantId, id);
              }

              /**
               * 写入租户内 {table} 表单值。
               *
               * @param tenantId 租户业务编码
               * @param values 表单值
               */
              @Override
              public void insert(String tenantId, Map<String, Object> values) {
                int affectedRows = {entity}Mapper.insert(tenantId, values);
                if (affectedRows != 1) {
                  throw new BusinessException(ERROR_SAVE_FAILED, "{table}保存失败");
                }
              }

              /**
               * 更新租户内 {table} 表单值。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @param values 表单值
               */
              @Override
              public void updateById(String tenantId, Long id, Map<String, Object> values) {
                int affectedRows = {entity}Mapper.updateById(tenantId, id, values);
                if (affectedRows != 1) {
                  throw new BusinessException(ERROR_UPDATE_FAILED, "{table}更新失败");
                }
              }

              /**
               * 逻辑删除租户内 {table}。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               */
              @Override
              public void deleteById(String tenantId, Long id) {
                int affectedRows = {entity}Mapper.deleteById(tenantId, id);
                if (affectedRows != 1) {
                  throw new BusinessException(ERROR_DELETE_FAILED, "{table}删除失败");
                }
              }
            }
            """);
  }

  private CodeTemplateDescriptor adminBackendDtoTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-dto",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端 DTO",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/dto/{Entity}QueryRequest.java",
        """
            package com.zhyc.{module}.dto;

            /**
             * {table} 查询请求。
             *
             * @param tenantId 租户业务编码
             * @param keyword 查询关键字
             */
            public record {Entity}QueryRequest(String tenantId, String keyword) {
            }
            """);
  }

  private CodeTemplateDescriptor adminBackendMapperTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-mapper",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端 Mapper",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/mapper/{Entity}Mapper.java",
        """
            package com.zhyc.{module}.mapper;

            import java.util.Map;
            import java.util.List;
            import org.apache.ibatis.annotations.InsertProvider;
            import org.apache.ibatis.annotations.Mapper;
            import org.apache.ibatis.annotations.Param;
            import org.apache.ibatis.annotations.SelectProvider;
            import org.apache.ibatis.annotations.UpdateProvider;

            /**
             * {table} 数据访问 Mapper。
             */
            @Mapper
            public interface {Entity}Mapper {

              /**
               * 统计租户内 {table} 数据量。
               *
               * @param tenantId 租户业务编码
               * @return 数据量
               */
              @SelectProvider(type = {Entity}SqlProvider.class, method = "countByTenantId")
              long countByTenantId(@Param("tenantId") String tenantId);

              /**
               * 查询租户内未删除的 {table} 数据。
               *
               * @param tenantId 租户业务编码
               * @param offset 分页偏移量
               * @param pageSize 每页记录数
               * @return 租户内 {table} 行数据
               */
              @SelectProvider(type = {Entity}SqlProvider.class, method = "selectByTenantId")
              List<Map<String, Object>> selectByTenantId(@Param("tenantId") String tenantId, @Param("offset") long offset, @Param("pageSize") int pageSize);

              /**
               * 按主键查询租户内未删除的 {table} 数据。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @return 租户内 {table} 行数据
               */
              @SelectProvider(type = {Entity}SqlProvider.class, method = "selectById")
              Map<String, Object> selectById(@Param("tenantId") String tenantId, @Param("id") Long id);

              /**
               * 写入租户内 {table} 表单值。
               *
               * @param tenantId 租户业务编码
               * @param values 表单值
               * @return 影响行数
               */
              @InsertProvider(type = {Entity}SqlProvider.class, method = "insert")
              int insert(@Param("tenantId") String tenantId, @Param("values") Map<String, Object> values);

              /**
               * 更新租户内 {table} 表单值。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @param values 表单值
               * @return 影响行数
               */
              @UpdateProvider(type = {Entity}SqlProvider.class, method = "updateById")
              int updateById(@Param("tenantId") String tenantId, @Param("id") Long id, @Param("values") Map<String, Object> values);

              /**
               * 逻辑删除租户内 {table}。
               *
               * @param tenantId 租户业务编码
               * @param id {table} 主键
               * @return 影响行数
               */
              @UpdateProvider(type = {Entity}SqlProvider.class, method = "deleteById")
              int deleteById(@Param("tenantId") String tenantId, @Param("id") Long id);
            }
            """);
  }

  private CodeTemplateDescriptor adminBackendSaveRequestTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-save-request",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端保存请求",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/dto/{Entity}SaveRequest.java",
        """
            package com.zhyc.{module}.dto;

            import jakarta.validation.constraints.NotBlank;
            import jakarta.validation.constraints.NotEmpty;
            import java.util.Map;

            /**
             * {table} 保存请求。
             *
             * @param tenantId 租户业务编码，用于保存时复核隔离边界
             * @param values 表单字段值，字段名必须来自当前模型字段白名单
             */
            public record {Entity}SaveRequest(
                @NotBlank(message = "租户业务编码不能为空") String tenantId,
                @NotEmpty(message = "表单字段值不能为空") Map<String, Object> values) {
            }
            """);
  }

  private CodeTemplateDescriptor adminBackendResponseTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-response",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端 Response DTO",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/dto/{Entity}Response.java",
        """
            package com.zhyc.{module}.dto;

            /**
             * {table} 列表响应。
             *
             * @param id 列表行唯一标识，用于前端表格行键
             * @param tenantId 租户业务编码，用于确认数据隔离边界
             * @param fields 字段编码清单，用于首期列表展示
             * @param totalCount 租户内未删除数据量
             */
            public record {Entity}Response(String id, String tenantId, String fields, long totalCount) {
            }
            """);
  }

  private CodeTemplateDescriptor adminBackendSqlProviderTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-sql-provider",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端 SQL Provider",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/mapper/{Entity}SqlProvider.java",
        """
            package com.zhyc.{module}.mapper;

            import com.zhyc.common.exception.BusinessException;
            import java.util.List;
            import java.util.Map;
            import java.util.stream.Collectors;

            /**
             * {table} SQL Provider。
             *
             * <p>集中构造 {table} 动态 SQL，必须显式保留租户隔离和逻辑删除条件。</p>
             */
            public class {Entity}SqlProvider {

              /** SQL 表单值缺失错误码。 */
              private static final String ERROR_SQL_VALUES_REQUIRED = "ZHYC_GENERATED_SQL_VALUES_REQUIRED";

              /** SQL 写入字段为空错误码。 */
              private static final String ERROR_SQL_INSERT_COLUMNS_EMPTY = "ZHYC_GENERATED_SQL_INSERT_COLUMNS_EMPTY";

              /** SQL 更新字段为空错误码。 */
              private static final String ERROR_SQL_UPDATE_COLUMNS_EMPTY = "ZHYC_GENERATED_SQL_UPDATE_COLUMNS_EMPTY";

              /**
               * 构建统计租户内未删除 {table} 数据量的 SQL。
               *
               * @return 带租户隔离和逻辑删除条件的统计 SQL
               */
              public String countByTenantId() {
                return "SELECT COUNT(1) "
                    + "FROM {table} "
                    + "WHERE tenant_id = #{tenantId} "
                    + "AND deleted = 0";
              }

              /**
               * 构建查询租户内未删除 {table} 数据的 SQL。
               *
               * @return 带租户隔离和逻辑删除条件的查询 SQL
               */
              public String selectByTenantId() {
                return "SELECT id, tenant_id, {firstDataField} "
                    + "FROM {table} "
                    + "WHERE tenant_id = #{tenantId} "
                    + "AND deleted = 0 "
                    + "ORDER BY id DESC LIMIT #{offset}, #{pageSize}";
              }

              /**
               * 构建按主键查询租户内未删除 {table} 数据的 SQL。
               *
               * @return 带租户隔离、主键和逻辑删除条件的查询 SQL
               */
              public String selectById() {
                return "SELECT id, tenant_id, {firstDataField} "
                    + "FROM {table} "
                    + "WHERE tenant_id = #{tenantId} "
                    + "AND id = #{id} "
                    + "AND deleted = 0";
              }

              /**
               * 构建写入租户内 {table} 表单值的 SQL。
               *
               * @param params MyBatis 参数集合
               * @return 带租户字段和字段白名单的写入 SQL
               */
              @SuppressWarnings("unchecked")
              public String insert(Map<String, Object> params) {
                Object rawValues = params.get("values");
                if (!(rawValues instanceof Map<?, ?> rawMap) || rawMap.isEmpty()) {
                  throw new BusinessException(ERROR_SQL_VALUES_REQUIRED, "{table}表单值不能为空");
                }
                Map<String, Object> values = (Map<String, Object>) rawMap;
                List<String> allowedColumns = List.of("{firstDataField}");
                List<String> columns = allowedColumns.stream()
                    .filter(values::containsKey)
                    .toList();
                if (columns.isEmpty()) {
                  throw new BusinessException(ERROR_SQL_INSERT_COLUMNS_EMPTY, "{table}没有可写入字段");
                }
                String columnSql = columns.stream().map(this::quote).collect(Collectors.joining(", "));
                String valueSql = columns.stream()
                    .map(column -> "#{values." + column + "}")
                    .collect(Collectors.joining(", "));
                return "INSERT INTO {table} (tenant_id, " + columnSql + ") "
                    + "VALUES (#{tenantId}, " + valueSql + ")";
              }

              /**
               * 构建更新租户内 {table} 表单值的 SQL。
               *
               * @param params MyBatis 参数集合
               * @return 带租户隔离、主键、逻辑删除和字段白名单的更新 SQL
               */
              @SuppressWarnings("unchecked")
              public String updateById(Map<String, Object> params) {
                Object rawValues = params.get("values");
                if (!(rawValues instanceof Map<?, ?> rawMap) || rawMap.isEmpty()) {
                  throw new BusinessException(ERROR_SQL_VALUES_REQUIRED, "{table}表单值不能为空");
                }
                Map<String, Object> values = (Map<String, Object>) rawMap;
                List<String> allowedColumns = List.of("{firstDataField}");
                List<String> columns = allowedColumns.stream()
                    .filter(values::containsKey)
                    .toList();
                if (columns.isEmpty()) {
                  throw new BusinessException(ERROR_SQL_UPDATE_COLUMNS_EMPTY, "{table}没有可更新字段");
                }
                String setSql = columns.stream()
                    .map(column -> quote(column) + " = #{values." + column + "}")
                    .collect(Collectors.joining(", "));
                return "UPDATE {table} SET " + setSql + ", updated_at = CURRENT_TIMESTAMP "
                    + "WHERE tenant_id = #{tenantId} "
                    + "AND id = #{id} "
                    + "AND deleted = 0";
              }

              /**
               * 构建逻辑删除租户内 {table} 的 SQL。
               *
               * @return 带租户隔离和未删除条件的逻辑删除 SQL
               */
              public String deleteById() {
                return "UPDATE {table} SET deleted = 1 "
                    + "WHERE tenant_id = #{tenantId} "
                    + "AND id = #{id} "
                    + "AND deleted = 0";
              }

              /**
               * 转义 SQL 字段名，避免字段名和数据库关键字冲突。
               *
               * @param column 字段物理列名
               * @return 带 MySQL 反引号的安全字段片段
               */
              private String quote(String column) {
                return "`" + column + "`";
              }
            }
            """);
  }

  private CodeTemplateDescriptor adminBackendDdlTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-ddl",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端 DDL 脚本",
        "zhyc-module-{module}/src/main/resources/db/V1__{module}_{entity}.sql",
        """
            -- {table} 建表脚本。
            -- 本脚本由低代码生成器根据在线表模型生成，执行前需按目标环境迁移规范复核。
            {ddl}
            """);
  }

  private CodeTemplateDescriptor adminBackendTestTemplate() {
    return new CodeTemplateDescriptor(
        "admin-backend-test",
        GenerationTarget.ADMIN_BACKEND,
        "后台后端单元测试",
        "zhyc-module-{module}/src/test/java/com/zhyc/{module}/service/impl/Default{Entity}ServiceTest.java",
        """
            package com.zhyc.{module}.service.impl;

            import static org.junit.jupiter.api.Assertions.assertEquals;
            import static org.junit.jupiter.api.Assertions.assertThrows;

            import com.zhyc.common.exception.BusinessException;
            import com.zhyc.{module}.dto.{Entity}SaveRequest;
            import com.zhyc.{module}.repository.{Entity}Repository;
            import java.util.List;
            import java.util.Map;
            import org.junit.jupiter.api.Test;

            /**
             * {table} 后台管理服务测试。
             */
            class {Entity}ServiceTest {

              /**
               * 验证生成服务保留租户字段清单。
               */
              @Test
              void shouldReturnGeneratedFieldCodes() {
                {Entity}Repository repository = new {Entity}Repository() {
                  @Override
                  public long countByTenantId(String tenantId) {
                    return 1L;
                  }

                  @Override
                  public List<Map<String, Object>> findByTenantId(String tenantId, long offset, int pageSize) {
                    return List.of(Map.of("id", "{entity}-1", "{firstDataField}", "sample"));
                  }

                  @Override
                  public Map<String, Object> findById(String tenantId, Long id) {
                    return Map.of("id", id, "{firstDataField}", "sample");
                  }

                  @Override
                  public void insert(String tenantId, Map<String, Object> values) {
                  }

                  @Override
                  public void updateById(String tenantId, Long id, Map<String, Object> values) {
                  }

                  @Override
                  public void deleteById(String tenantId, Long id) {
                  }
                };
                Default{Entity}Service service = new Default{Entity}Service(repository);
                String tenantId = "{tenant}";

                assertEquals("{fields}", service.list(tenantId, 1, 20).getRecords().get(0).fields());
              }

              /**
               * 验证生成服务在保存时会拦截跨租户请求并返回稳定错误码。
               */
              @Test
              void shouldRejectTenantMismatchWhenSaving() {
                {Entity}Repository repository = new {Entity}Repository() {
                  @Override
                  public long countByTenantId(String tenantId) {
                    return 0L;
                  }

                  @Override
                  public List<Map<String, Object>> findByTenantId(String tenantId, long offset, int pageSize) {
                    return List.of();
                  }

                  @Override
                  public Map<String, Object> findById(String tenantId, Long id) {
                    return Map.of();
                  }

                  @Override
                  public void insert(String tenantId, Map<String, Object> values) {
                  }

                  @Override
                  public void updateById(String tenantId, Long id, Map<String, Object> values) {
                  }

                  @Override
                  public void deleteById(String tenantId, Long id) {
                  }
                };
                Default{Entity}Service service = new Default{Entity}Service(repository);
                {Entity}SaveRequest request = new {Entity}SaveRequest("other-tenant", Map.of("{firstDataField}", "sample"));

                BusinessException exception = assertThrows(BusinessException.class, () -> service.save("{tenant}", request));

                assertEquals("ZHYC_GENERATED_TENANT_MISMATCH", exception.getCode());
              }
            }
            """);
  }

  private CodeTemplateDescriptor adminFrontendListTemplate() {
    return new CodeTemplateDescriptor(
        "admin-frontend-list",
        GenerationTarget.ADMIN_FRONTEND,
        "后台前端列表页",
        "zhyc-base-vue/src/views/{module}/{entity}/index.vue",
        """
            <template>
              <section data-module="{module}" data-entity="{entity}">
                <a-card title="{table}" :bordered="false">
                  <a-alert v-if="errorMessage" type="error" show-icon :message="`后台列表加载失败：${errorMessage}`" />
                  <div class="generated-hero-actions">
                    <a-button class="generated-list-create-action" type="primary" @click="handleCreate">新建</a-button>
                    <a-button :loading="loading" @click="loadRecords">刷新</a-button>
                  </div>
                  <span class="permission-code">{{ queryPermission }}</span>
                  <a-table row-key="id" :columns="columns" :data-source="records" :loading="loading" :pagination="pagination" @change="handlePageChange">
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'actions'">
                        <div class="zhyc-table-actions">
                          <a-button danger size="small" @click="handleDelete(record)">删除</a-button>
                        </div>
                      </template>
                    </template>
                  </a-table>
                </a-card>
              </section>
            </template>

            <script setup lang="ts">
            import { computed, onMounted, ref } from 'vue';
            import { message, Modal } from 'ant-design-vue';
            import type { TableColumnsType, TablePaginationConfig } from 'ant-design-vue';

            import { delete{Entity}, list{Entity}, type {Entity}Record } from '@/api/{module}/{entity}';
            import { requireAdminTenantId } from '@/utils/adminContext';

            /** {table} 列表列定义。 */
            const columns: TableColumnsType<{Entity}Record> = [
              { title: '租户编码', dataIndex: 'tenantId', width: 180 },
              { title: '字段清单', dataIndex: 'fields' },
              { title: '数据量', dataIndex: 'totalCount', width: 120 },
              { title: '操作', key: 'actions', width: 120 },
            ];
            /** {table} 列表数据。 */
            const records = ref<{Entity}Record[]>([]);
            /** {table} 当前页码。 */
            const pageNo = ref(1);
            /** {table} 每页记录数。 */
            const pageSize = ref(20);
            /** {table} 总记录数。 */
            const total = ref(0);
            /** {table} 列表加载状态。 */
            const loading = ref(false);
            /** {table} 列表错误提示。 */
            const errorMessage = ref('');
            /** 查询权限编码。 */
            const queryPermission = '{module}:{entity}:query';
            /** 删除权限编码。 */
            const deletePermission = '{module}:{entity}:delete';
            /** {table} 表格分页配置。 */
            const pagination = computed(() => ({
              current: pageNo.value,
              pageSize: pageSize.value,
              total: total.value,
              showSizeChanger: true,
            }));

            /**
             * 加载 {table} 列表。
             */
            async function loadRecords(): Promise<void> {
              loading.value = true;
              errorMessage.value = '';
              try {
                requireAdminTenantId();
                const page = await list{Entity}(pageNo.value, pageSize.value);
                records.value = page.records;
                total.value = page.total;
              } catch (error) {
                errorMessage.value = error instanceof Error ? error.message : '{table}列表加载失败';
                message.error(`后台列表加载失败：${errorMessage.value}`);
              } finally {
                loading.value = false;
              }
            }

            /**
             * 切换 {table} 表格分页。
             *
             * @param paginationInfo Ant Design Vue 表格分页参数
             */
            async function handlePageChange(paginationInfo: TablePaginationConfig): Promise<void> {
              pageNo.value = Number(paginationInfo.current ?? 1);
              pageSize.value = Number(paginationInfo.pageSize ?? 20);
              await loadRecords();
            }

            /**
             * 打开 {table} 新建入口。
             */
            function handleCreate(): void {
              message.info('请在生成后的业务模块中配置新建表单路由');
            }

            /**
             * 删除租户内 {table} 列表记录。
             *
             * @param record 当前表格行记录
             */
            async function handleDelete(record: {Entity}Record): Promise<void> {
              Modal.confirm({
                title: '删除{table}',
                content: `二次确认：确定删除${record.id}？权限：${deletePermission}`,
                async onOk() {
                  requireAdminTenantId();
                  await delete{Entity}(record.id);
                  message.success('删除成功');
                  await loadRecords();
                },
              });
            }

            onMounted(() => {
              void loadRecords();
            });
            </script>

            <style scoped>
            .permission-code {
              display: inline-flex;
              margin-left: 12px;
              color: #64748b;
              font-size: 12px;
            }

            .generated-hero-actions {
              display: inline-flex;
              gap: 8px;
              margin-bottom: 12px;
            }

            .generated-list-create-action {
              min-width: 72px;
            }
            </style>
            """);
  }

  private CodeTemplateDescriptor adminFrontendApiTemplate() {
    return new CodeTemplateDescriptor(
        "admin-frontend-api",
        GenerationTarget.ADMIN_FRONTEND,
        "后台前端 API",
        "zhyc-base-vue/src/api/{module}/{entity}.ts",
        """
            import { request, type PageResult } from '@/api/http';

            /**
             * {table} 列表记录。
             */
            export interface {Entity}Record {
              /** {tableLabel}记录主键，用于列表行键、详情查询和编辑定位。 */
              id: string;
              /** 租户业务编码，用于端侧请求头和后端租户隔离复核。 */
              tenantId: string;
              /** 字段展示值，用于首期列表、表单和详情页展示。 */
              fields: string;
              /** 当前租户内未删除记录总数，用于列表统计展示。 */
              totalCount: number;
            }

            /**
             * {table} 保存载荷。
             */
            export interface {Entity}SavePayload {
              /** 保存请求租户业务编码，用于后端复核租户隔离边界。 */
              tenantId: string;
              /** 表单字段值，字段名来自当前模型字段白名单。 */
              values: Record<string, unknown>;
            }

            /**
             * 查询 {table} 列表。
             *
             * @param pageNo 当前页码，从 1 开始
             * @param pageSize 每页记录数
             * @returns {table} 分页列表记录
             */
            export function list{Entity}(pageNo = 1, pageSize = 20): Promise<PageResult<{Entity}Record>> {
              return request<PageResult<{Entity}Record>>('/{module}/{entity}', {
                query: { pageNo, pageSize },
              });
            }

            /**
             * 查询 {table} 详情。
             *
             * @param id {table} 主键
             * @returns {table} 详情记录
             */
            export function get{Entity}(id: string): Promise<{Entity}Record> {
              return request<{Entity}Record>(`/{module}/{entity}/${id}`);
            }

            /**
             * 保存 {table} 表单。
             *
             * @param payload {table} 保存载荷
             * @returns 保存后的 {table} 记录
             */
            export function save{Entity}(payload: {Entity}SavePayload): Promise<{Entity}Record> {
              return request<{Entity}Record, {Entity}SavePayload>('/{module}/{entity}', {
                method: 'POST',
                body: payload,
              });
            }

            /**
             * 更新 {table} 表单。
             *
             * @param id {table} 主键
             * @param payload {table} 保存载荷
             * @returns 更新后的 {table} 记录
             */
            export function update{Entity}(id: string, payload: {Entity}SavePayload): Promise<{Entity}Record> {
              return request<{Entity}Record, {Entity}SavePayload>(`/{module}/{entity}/${id}`, {
                method: 'PUT',
                body: payload,
              });
            }

            /**
             * 删除 {table} 记录。
             *
             * @param id {table} 主键
             * @returns 删除完成信号
             */
            export function delete{Entity}(id: string): Promise<void> {
              return request<void>(`/{module}/{entity}/${id}`, {
                method: 'DELETE',
              });
            }
            """);
  }

  private CodeTemplateDescriptor adminFrontendFormTemplate() {
    return new CodeTemplateDescriptor(
        "admin-frontend-form",
        GenerationTarget.ADMIN_FRONTEND,
        "后台前端表单页",
        "zhyc-base-vue/src/views/{module}/{entity}/form.vue",
        """
            <template>
              <a-card title="{table}表单" :bordered="false">
                <a-form layout="vertical" :model="formState" :rules="formRules">
                  <a-form-item label="租户编码">
                    <a-input :value="tenantId" disabled />
                  </a-form-item>
                  <a-form-item label="字段清单" name="fields">
                    <a-textarea v-model:value="formState.fields" />
                  </a-form-item>
                  <a-button type="primary" :loading="submitting" @click="handleSubmit">提交</a-button>
                  <span class="permission-code">{{ savePermission }}</span>
                </a-form>
              </a-card>
            </template>

            <script setup lang="ts">
            import { computed, onMounted, ref } from 'vue';
            import { useRoute } from 'vue-router';
            import { message, Modal } from 'ant-design-vue';

            import { get{Entity}, save{Entity}, update{Entity} } from '@/api/{module}/{entity}';
            import { requireAdminTenantId } from '@/utils/adminContext';

            /** 当前表单页路由。 */
            const route = useRoute();
            /** 当前后台租户编码。 */
            const tenantId = computed(() => requireAdminTenantId());
            /** 当前编辑记录主键；为空时表示新增模式。 */
            const recordId = computed(() => String(route.params.id ?? ''));
            /** 是否为编辑模式。 */
            const isEditMode = computed(() => Boolean(recordId.value));
            /** 保存权限编码。 */
            const savePermission = '{module}:{entity}:save';
            /** {table} 后台表单状态。 */
            const formState = ref({ fields: "{fields}" });
            /** {table} 后台表单校验规则。 */
            const formRules = {
              fields: [{ required: true, message: '请输入字段清单', trigger: 'blur' }],
            };
            /** {table} 后台表单提交状态。 */
            const submitting = ref(false);

            /**
             * 加载 {table} 编辑表单。
             */
            async function loadFormDetail(): Promise<void> {
              if (!isEditMode.value) {
                return;
              }
              const record = await get{Entity}(recordId.value);
              formState.value.fields = record.fields;
            }

            /**
             * 提交 {table} 后台表单。
             */
            async function handleSubmit(): Promise<void> {
              if (!formState.value.fields.trim()) {
                message.warning('请输入字段清单');
                return;
              }
              Modal.confirm({
                title: '提交{table}',
                content: '二次确认：确定提交当前{table}？',
                async onOk() {
                  submitting.value = true;
                  try {
                    const payload = {
                      tenantId: tenantId.value,
                      values: { {firstDataField}: formState.value.fields.trim() },
                    };
                    await (isEditMode.value ? update{Entity}(recordId.value, payload) : save{Entity}(payload));
                    message.success(`提交成功，权限：${savePermission}`);
                  } finally {
                    submitting.value = false;
                  }
                },
              });
            }

            onMounted(() => {
              void loadFormDetail();
            });
            </script>

            <style scoped>
            .permission-code {
              display: inline-flex;
              margin-left: 12px;
              color: #64748b;
              font-size: 12px;
            }
            </style>
            """);
  }

  private CodeTemplateDescriptor adminFrontendDetailTemplate() {
    return new CodeTemplateDescriptor(
        "admin-frontend-detail",
        GenerationTarget.ADMIN_FRONTEND,
        "后台前端详情页",
        "zhyc-base-vue/src/views/{module}/{entity}/detail.vue",
        """
            <template>
              <a-card title="{table}详情" :bordered="false">
                <a-alert v-if="errorMessage" type="error" show-icon :message="`详情加载失败：${errorMessage}`" />
                <a-spin :spinning="loading">
                <a-descriptions v-if="currentRecord" :column="1" bordered>
                  <a-descriptions-item label="模块">{module}</a-descriptions-item>
                  <a-descriptions-item label="实体">{entity}</a-descriptions-item>
                  <a-descriptions-item label="字段">{{ currentRecord.fields }}</a-descriptions-item>
                  <a-descriptions-item label="数据量">{{ currentRecord.totalCount }}</a-descriptions-item>
                </a-descriptions>
                <a-empty v-else description="暂无详情数据" />
                </a-spin>
              </a-card>
            </template>

            <script setup lang="ts">
            import { onMounted, ref } from 'vue';
            import { useRoute } from 'vue-router';
            import { message } from 'ant-design-vue';

            import { get{Entity}, type {Entity}Record } from '@/api/{module}/{entity}';
            import { requireAdminTenantId } from '@/utils/adminContext';

            /** 当前详情页路由。 */
            const route = useRoute();
            /** {table} 当前详情记录。 */
            const currentRecord = ref<{Entity}Record | null>(null);
            /** {table} 详情加载状态。 */
            const loading = ref(false);
            /** {table} 详情错误提示。 */
            const errorMessage = ref('');

            /**
             * 加载 {table} 后台详情。
             */
            async function loadDetail(): Promise<void> {
              loading.value = true;
              errorMessage.value = '';
              try {
                requireAdminTenantId();
                const recordId = String(route.params.id ?? '');
                if (!recordId) {
                  throw new Error('{table}主键不能为空');
                }
                currentRecord.value = await get{Entity}(recordId);
              } catch (error) {
                errorMessage.value = error instanceof Error ? error.message : '{table}详情加载失败';
                message.error(`详情加载失败：${errorMessage.value}`);
              } finally {
                loading.value = false;
              }
            }

            onMounted(() => {
              void loadDetail();
            });
            </script>
            """);
  }

  private CodeTemplateDescriptor adminFrontendRouteTemplate() {
    return new CodeTemplateDescriptor(
        "admin-frontend-route",
        GenerationTarget.ADMIN_FRONTEND,
        "后台前端路由片段",
        "zhyc-base-vue/src/router/routes/modules/{module}-{entity}.ts",
        """
            /**
             * {table} 后台路由片段。
             */
            export const {entity}Route = {
              path: '/{module}/{entity}',
              name: '{Entity}List',
              component: () => import('@/views/{module}/{entity}/index.vue'),
              meta: {
                title: '{table}',
                permission: '{module}:{entity}:query',
              },
            };

            /**
             * {table} 后台新增路由片段。
             */
            export const {entity}CreateRoute = {
              path: '/{module}/{entity}/create',
              name: '{Entity}Create',
              component: () => import('@/views/{module}/{entity}/form.vue'),
              meta: {
                title: '新增{table}',
                permission: '{module}:{entity}:save',
              },
            };

            /**
             * {table} 后台编辑路由片段。
             */
            export const {entity}EditRoute = {
              path: '/{module}/{entity}/:id/edit',
              name: '{Entity}Edit',
              component: () => import('@/views/{module}/{entity}/form.vue'),
              meta: {
                title: '编辑{table}',
                permission: '{module}:{entity}:save',
              },
            };

            /**
             * {table} 后台详情路由片段。
             */
            export const {entity}DetailRoute = {
              path: '/{module}/{entity}/:id',
              name: '{Entity}Detail',
              component: () => import('@/views/{module}/{entity}/detail.vue'),
              meta: {
                title: '{table}详情',
                permission: '{module}:{entity}:query',
              },
            };
            """);
  }

  private CodeTemplateDescriptor uniappListTemplate() {
    return new CodeTemplateDescriptor(
        "uniapp-list",
        GenerationTarget.UNIAPP,
        "UniApp 列表页",
        "zhyc-base-uniapp/src/pages/{module}/{entity}/list.vue",
        """
            <template>
              <view class="mobile-page mobile-bottom-safe" data-module="{module}" data-entity="{entity}">
                <MobilePageTopBar title="{tableLabel}" eyebrow="移动列表" action-text="新建" @action="goCreate" />
                <view class="mobile-hero compact-hero">
                  <view class="mobile-hero-header">
                    <view class="mobile-hero-main">
                      <view class="mobile-hero-kicker">移动列表</view>
                      <view class="mobile-title">数据列表</view>
                      <view class="mobile-subtitle">查看当前租户下的{tableLabel}记录，点击卡片进入详情。</view>
                    </view>
                    <button class="mobile-hero-action generated-list-refresh-action" :disabled="loading" @tap="list{Entity}">
                      {{ loading ? '同步中' : '同步' }}
                    </button>
                  </view>
                  <view class="mobile-summary-strip">
                    <view class="mobile-summary-item">
                      <view class="mobile-summary-value">{{ total }}</view>
                      <view class="mobile-summary-label">总数</view>
                    </view>
                    <view class="mobile-summary-item">
                      <view class="mobile-summary-value">{{ records.length }}</view>
                      <view class="mobile-summary-label">本页</view>
                    </view>
                    <view class="mobile-summary-item">
                      <view class="mobile-summary-value">{{ pageNo }}</view>
                      <view class="mobile-summary-label">页码</view>
                    </view>
                  </view>
                </view>

                <MobileState
                  v-if="stateType"
                  :type="stateType"
                  :title="stateTitle"
                  :description="stateDescription"
                  :action-text="stateActionText"
                  @action="list{Entity}"
                />

                <scroll-view v-else scroll-y class="generated-list-scroll">
                  <view
                    v-for="item in records"
                    :key="item.id"
                    class="mobile-list-card mobile-rich-list-card"
                    hover-class="generated-list-hover"
                    @tap="goDetail(item.id)"
                  >
                    <view class="mobile-list-leading">
                      <view class="mobile-list-icon tone-blue">{{ getRecordIcon(item) }}</view>
                      <view class="mobile-list-body">
                        <view class="mobile-list-top">
                          <view class="mobile-card-title">{{ item.fields }}</view>
                          <view class="mobile-status-chip success">可用</view>
                        </view>
                        <view class="mobile-list-meta">
                          <view class="mobile-list-meta-item">ID {{ item.id }}</view>
                          <view class="mobile-list-meta-item">租户 {{ item.tenantId }}</view>
                        </view>
                        <view class="mobile-list-footer">
                          <view class="mobile-mini-tag-row">
                            <view class="mobile-mini-tag">合计 {{ item.totalCount }}</view>
                          </view>
                          <view class="mobile-list-action-text">查看</view>
                        </view>
                      </view>
                    </view>
                  </view>
                </scroll-view>
              </view>
            </template>

            <script setup lang="ts">
            import { computed, onMounted, ref } from 'vue';

            import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
            import MobileState from '@/components/MobileState.vue';
            import { listMobile{Entity}, type {Entity}Record } from '@/api/{module}-{entity}';
            import { getMobileUserContext, requireMobileTenantId, requireMobileUserId, showMobileToast } from '@/utils/platform';

            /** 移动端统一状态类型。 */
            type MobileStateType = 'loading' | 'empty' | 'error' | 'info' | 'success';

            /** {table} 移动端列表数据。 */
            const records = ref<{Entity}Record[]>([]);
            /** {table} 移动端当前页码。 */
            const pageNo = ref(1);
            /** {table} 移动端每页记录数。 */
            const pageSize = ref(20);
            /** {table} 移动端总记录数。 */
            const total = ref(0);
            /** {table} 移动端加载状态。 */
            const loading = ref(false);
            /** {table} 移动端错误提示。 */
            const errorMessage = ref('');

            /** 当前移动列表状态。 */
            const stateType = computed<MobileStateType | ''>(() => {
              if (loading.value) {
                return 'loading';
              }
              if (errorMessage.value) {
                return 'error';
              }
              return records.value.length === 0 ? 'empty' : '';
            });

            /** 当前移动列表状态标题。 */
            const stateTitle = computed(() => {
              if (loading.value) {
                return '正在加载{tableLabel}';
              }
              if (errorMessage.value) {
                return '{tableLabel}加载失败';
              }
              return '暂无{tableLabel}数据';
            });

            /** 当前移动列表状态说明。 */
            const stateDescription = computed(() => (
              errorMessage.value || '当前租户下还没有可展示的{tableLabel}记录'
            ));

            /** 当前移动列表状态操作文案。 */
            const stateActionText = computed(() => (loading.value ? '' : '重试'));

            /**
             * 获取移动端列表记录图标短字。
             *
             * @param item 当前列表记录
             * @return 移动端图标短字
             */
            function getRecordIcon(item: {Entity}Record): string {
              return item.fields?.trim().slice(0, 1) || '数';
            }

            /**
             * 进入 {tableLabel} 移动端新建表单。
             */
            function goCreate(): void {
              uni.navigateTo({ url: `/pages/{module}/{entity}/form` });
            }

            /**
             * 跳转到 {tableLabel} 移动端详情。
             *
             * @param id 当前记录主键
             */
            function goDetail(id: string): void {
              uni.navigateTo({ url: `/pages/{module}/{entity}/detail?id=${encodeURIComponent(id)}` });
            }

            /**
             * 加载 {tableLabel} 移动端列表。
             */
            async function list{Entity}(): Promise<void> {
              loading.value = true;
              errorMessage.value = '';
              try {
                const userContext = getMobileUserContext();
                requireMobileUserId(userContext);
                requireMobileTenantId(userContext);
                const page = await listMobile{Entity}(pageNo.value, pageSize.value);
                records.value = page.records;
                total.value = page.total;
              } catch (error) {
                errorMessage.value = error instanceof Error ? error.message : '{tableLabel}加载失败';
                showMobileToast(`加载失败：${errorMessage.value}`, 'none');
              } finally {
                loading.value = false;
              }
            }

            onMounted(() => {
              void list{Entity}();
            });
            </script>

            <style scoped>
            .generated-list-scroll {
              max-height: calc(100vh - 260rpx);
            }

            .generated-list-refresh-action {
              min-width: 96rpx;
            }
            </style>
            """);
  }

  private CodeTemplateDescriptor uniappFormTemplate() {
    return new CodeTemplateDescriptor(
        "uniapp-form",
        GenerationTarget.UNIAPP,
        "UniApp 表单页",
        "zhyc-base-uniapp/src/pages/{module}/{entity}/form.vue",
        """
            <template>
              <view class="mobile-page mobile-bottom-safe" data-module="{module}" data-entity="{entity}">
                <MobilePageTopBar title="{tableLabel}表单" eyebrow="移动表单" fallback-url="/pages/{module}/{entity}/list" />
                <view class="mobile-hero compact-hero">
                  <view class="mobile-hero-header">
                    <view class="mobile-hero-main">
                      <view class="mobile-hero-kicker">移动表单</view>
                      <view class="mobile-title">{tableLabel}</view>
                      <view class="mobile-subtitle">{{ isEditMode ? '编辑已有记录' : '新建移动端记录' }}</view>
                    </view>
                  </view>
                </view>

                <view class="mobile-card generated-form-summary-card">
                  <view class="generated-form-summary-main">
                    <view class="generated-form-summary-label">{{ formModeText }}</view>
                    <view class="generated-form-summary-title">{tableLabel}</view>
                    <view class="mobile-mini-tag-row">
                      <view class="mobile-mini-tag">{{ isEditMode ? '编辑记录' : '新建记录' }}</view>
                      <view class="mobile-mini-tag">{{ submitting ? '提交中' : '待填写' }}</view>
                    </view>
                  </view>
                  <view class="mobile-status-chip warning">{{ isEditMode ? '编辑' : '新建' }}</view>
                </view>

                <view class="mobile-form-card">
                  <view class="mobile-form-item">
                    <view class="mobile-form-label">表单内容</view>
                    <textarea
                      v-model="formText"
                      class="mobile-textarea"
                      maxlength="500"
                      placeholder="请输入表单内容"
                    />
                  </view>
                  <view v-if="validateMessage" class="mobile-form-alert">{{ validateMessage }}</view>
                  <view class="generated-form-tips">
                    <view class="generated-form-tip">保存前会校验移动端账号、租户和表单内容。</view>
                    <view class="generated-form-tip">提交成功后可从列表进入详情继续处理。</view>
                  </view>
                  <view class="mobile-action-grid">
                    <button class="mobile-ghost-button" :disabled="submitting || !isEditMode" @tap="loadFormDetail">重载</button>
                    <button class="mobile-action-button" :disabled="submitting" @tap="submitForm">
                      {{ submitting ? '提交中' : '提交' }}
                    </button>
                  </view>
                </view>
              </view>
            </template>

            <script setup lang="ts">
            import { computed, onMounted, ref } from 'vue';

            import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
            import { getMobile{Entity}, saveMobile{Entity}, updateMobile{Entity} } from '@/api/{module}-{entity}';
            import { getMobileUserContext, requireMobileTenantId, requireMobileUserId, showConfirm, showMobileToast } from '@/utils/platform';

            /** 当前移动端页面参数。 */
            const query = defineProps<{ id?: string }>();
            /** 当前编辑记录主键；为空时表示新增模式。 */
            const recordId = computed(() => String(query.id ?? ''));
            /** 是否为编辑模式。 */
            const isEditMode = computed(() => Boolean(recordId.value));
            /** 移动端表单模式展示文本。 */
            const formModeText = computed(() => (isEditMode.value ? '编辑已有记录' : '创建新记录'));
            /** {tableLabel} 移动端表单内容。 */
            const formText = ref('{fields}');
            /** {tableLabel} 移动端提交状态。 */
            const submitting = ref(false);
            /** {tableLabel} 移动端表单内联提示。 */
            const validateMessage = ref('');

            /**
             * 加载 {tableLabel} 移动端编辑表单。
             */
            async function loadFormDetail(): Promise<void> {
              if (!isEditMode.value) {
                return;
              }
              validateMessage.value = '';
              try {
                const userContext = getMobileUserContext();
                requireMobileUserId(userContext);
                requireMobileTenantId(userContext);
                const record = await getMobile{Entity}(recordId.value);
                formText.value = record.fields;
              } catch (error) {
                validateMessage.value = error instanceof Error ? error.message : '{tableLabel}加载失败';
                showMobileToast(`加载失败：${validateMessage.value}`, 'none');
              }
            }

            /**
             * 提交 {tableLabel} 移动端表单。
             */
            async function submitForm(): Promise<void> {
              validateMessage.value = '';
              if (!formText.value.trim()) {
                validateMessage.value = '请输入表单内容';
                return;
              }
              const confirmed = await showConfirm('提交{tableLabel}', '二次确认：确定提交当前{tableLabel}？');
              if (!confirmed) {
                return;
              }
              submitting.value = true;
              try {
                const userContext = getMobileUserContext();
                requireMobileUserId(userContext);
                const tenantId = requireMobileTenantId(userContext);
                const payload = {
                  tenantId,
                  values: { {firstDataField}: formText.value.trim() },
                };
                await (isEditMode.value ? updateMobile{Entity}(recordId.value, payload) : saveMobile{Entity}(payload));
                showMobileToast('提交成功', 'success');
              } catch (error) {
                const message = error instanceof Error ? error.message : '请稍后重试';
                validateMessage.value = message;
                showMobileToast(`提交失败：${message}`, 'none');
              } finally {
                submitting.value = false;
              }
            }

            onMounted(() => {
              void loadFormDetail();
            });
            </script>

            <style scoped>
            .generated-form-summary-card {
              display: flex;
              align-items: flex-start;
              justify-content: space-between;
              gap: 18rpx;
            }

            .generated-form-summary-main {
              min-width: 0;
              flex: 1;
            }

            .generated-form-summary-label {
              color: #64748b;
              font-size: 23rpx;
              font-weight: 800;
              line-height: 1.35;
            }

            .generated-form-summary-title {
              margin: 8rpx 0 12rpx;
              color: #111827;
              font-size: 32rpx;
              font-weight: 900;
              line-height: 1.25;
            }

            .generated-form-tips {
              margin: 0 0 22rpx;
              padding: 18rpx;
              border: 1rpx solid #dbeafe;
              border-radius: 18rpx;
              background: #f8fbff;
            }

            .generated-form-tip {
              color: #64748b;
              font-size: 23rpx;
              line-height: 1.5;
            }

            .generated-form-tip + .generated-form-tip {
              margin-top: 8rpx;
            }
            </style>
            """);
  }

  private CodeTemplateDescriptor uniappDetailTemplate() {
    return new CodeTemplateDescriptor(
        "uniapp-detail",
        GenerationTarget.UNIAPP,
        "UniApp 详情和审批页",
        "zhyc-base-uniapp/src/pages/{module}/{entity}/detail.vue",
        """
            <template>
              <view class="mobile-page mobile-bottom-safe" data-module="{module}" data-entity="{entity}">
                <MobilePageTopBar
                  title="{tableLabel}详情"
                  eyebrow="移动详情"
                  action-text="刷新"
                  fallback-url="/pages/{module}/{entity}/list"
                  :action-disabled="loading"
                  @action="loadDetail"
                />
                <view class="mobile-hero compact-hero">
                  <view class="mobile-hero-header">
                    <view class="mobile-hero-main">
                      <view class="mobile-hero-kicker">移动详情</view>
                      <view class="mobile-title">{tableLabel}</view>
                      <view class="mobile-subtitle">查看记录详情并处理关联流程任务</view>
                    </view>
                  </view>
                </view>

                <MobileState
                  v-if="stateType"
                  :type="stateType"
                  :title="stateTitle"
                  :description="stateDescription"
                  :action-text="stateActionText"
                  @action="loadDetail"
                />

                <view v-if="currentRecord" class="mobile-card generated-detail-summary-card">
                  <view class="generated-detail-summary-main">
                    <view class="generated-detail-label">当前记录</view>
                    <view class="generated-detail-title">{{ currentRecord.fields || currentRecord.id }}</view>
                    <view class="mobile-mini-tag-row">
                      <view class="mobile-mini-tag">ID {{ currentRecord.id }}</view>
                      <view class="mobile-mini-tag">数据量 {{ currentRecord.totalCount }}</view>
                    </view>
                  </view>
                  <view :class="['mobile-status-chip', taskId ? 'warning' : '']">
                    {{ taskId ? '待处理' : '仅查看' }}
                  </view>
                </view>

                <view v-if="currentRecord" class="mobile-field-card">
                  <view class="mobile-field-row">
                    <view class="mobile-field-label">记录主键</view>
                    <view class="mobile-field-value">{{ currentRecord.id }}</view>
                  </view>
                  <view class="mobile-field-row">
                    <view class="mobile-field-label">字段内容</view>
                    <view class="mobile-field-value">{{ currentRecord.fields }}</view>
                  </view>
                  <view class="mobile-field-row">
                    <view class="mobile-field-label">数据量</view>
                    <view class="mobile-field-value">{{ currentRecord.totalCount }}</view>
                  </view>
                </view>

                <view v-if="currentRecord" class="mobile-form-card">
                  <view class="mobile-section-header">
                    <view class="mobile-section-title">流程处理</view>
                    <view class="mobile-status-chip">{{ submitting ? '处理中' : '待操作' }}</view>
                  </view>
                  <view class="generated-workflow-tip">
                    {{ taskId ? '当前记录已带出流程任务，可直接处理。' : '当前记录未带出流程任务，只展示业务详情。' }}
                  </view>
                  <view class="mobile-action-grid three">
                    <button class="mobile-action-button" :disabled="!canApproveReject" @tap="approve">通过</button>
                    <button class="mobile-danger-button" :disabled="!canApproveReject" @tap="reject">驳回</button>
                    <button class="mobile-ghost-button" :disabled="!canRevoke" @tap="revoke">撤回</button>
                  </view>
                </view>
              </view>
            </template>

            <script setup lang="ts">
            import { computed, onMounted, ref } from 'vue';

            import MobilePageTopBar from '@/components/MobilePageTopBar.vue';
            import MobileState from '@/components/MobileState.vue';
            import { approveTask, rejectTask, revokeMobileTask } from '@/api/workflow';
            import { getMobile{Entity}, type {Entity}Record } from '@/api/{module}-{entity}';
            import { getMobileUserContext, requireMobileTenantId, requireMobileUserId, showConfirm, showMobileToast } from '@/utils/platform';

            /** 移动端统一状态类型。 */
            type MobileStateType = 'loading' | 'empty' | 'error' | 'info' | 'success';

            /** 当前移动端详情页参数。 */
            const query = defineProps<{ id?: string; taskId?: string; processInstanceId?: string }>();
            /** 当前工作流任务 ID，用于通过平台工作流门面完成审批动作。 */
            const taskId = String(query.taskId ?? '');
            /** 当前流程实例 ID，用于通过平台工作流门面完成撤回动作。 */
            const processInstanceId = String(query.processInstanceId ?? '');
            /** {tableLabel} 当前移动端详情记录。 */
            const currentRecord = ref<{Entity}Record | null>(null);
            /** {tableLabel} 移动端详情加载状态。 */
            const loading = ref(false);
            /** {tableLabel} 移动端审批提交状态。 */
            const submitting = ref(false);
            /** {tableLabel} 移动端详情错误提示。 */
            const errorMessage = ref('');
            /** 是否允许审批或驳回。 */
            const canApproveReject = computed(() => Boolean(taskId) && !submitting.value);
            /** 是否允许撤回流程。 */
            const canRevoke = computed(() => Boolean(processInstanceId) && !submitting.value);

            /** 当前移动详情状态。 */
            const stateType = computed<MobileStateType | ''>(() => {
              if (loading.value) {
                return 'loading';
              }
              if (errorMessage.value) {
                return 'error';
              }
              return currentRecord.value ? '' : 'info';
            });

            /** 当前移动详情状态标题。 */
            const stateTitle = computed(() => {
              if (loading.value) {
                return '正在加载{tableLabel}详情';
              }
              if (errorMessage.value) {
                return '{tableLabel}详情加载失败';
              }
              return '请选择{tableLabel}记录';
            });

            /** 当前移动详情状态说明。 */
            const stateDescription = computed(() => (
              errorMessage.value || '请从{tableLabel}列表进入详情页'
            ));

            /** 当前移动详情状态操作文案。 */
            const stateActionText = computed(() => (loading.value ? '' : '重试'));

            /**
             * 加载 {tableLabel} 移动端详情。
             */
            async function loadDetail(): Promise<void> {
              loading.value = true;
              errorMessage.value = '';
              try {
                const userContext = getMobileUserContext();
                requireMobileUserId(userContext);
                requireMobileTenantId(userContext);
                const recordId = String(query.id ?? '');
                if (!recordId) {
                  throw new Error('{tableLabel}主键不能为空');
                }
                currentRecord.value = await getMobile{Entity}(recordId);
              } catch (error) {
                errorMessage.value = error instanceof Error ? error.message : '{tableLabel}详情加载失败';
                showMobileToast(`加载失败：${errorMessage.value}`, 'none');
              } finally {
                loading.value = false;
              }
            }

            /**
             * 审批 {tableLabel} 移动端详情。
             */
            async function approve(): Promise<void> {
              const confirmed = await showConfirm('审批{tableLabel}', '二次确认：确定审批通过？');
              if (!confirmed) {
                return;
              }
              if (!taskId) {
                showMobileToast('工作流任务 ID 不能为空', 'none');
                return;
              }
              submitting.value = true;
              try {
                await approveTask(taskId, { comment: '同意' });
                showMobileToast('处理成功', 'success');
              } catch (error) {
                const message = error instanceof Error ? error.message : '请稍后重试';
                showMobileToast(`处理失败：${message}`, 'none');
              } finally {
                submitting.value = false;
              }
            }

            /**
             * 驳回 {tableLabel} 移动端详情。
             */
            async function reject(): Promise<void> {
              const confirmed = await showConfirm('驳回{tableLabel}', '二次确认：确定驳回当前任务？');
              if (!confirmed) {
                return;
              }
              if (!taskId) {
                showMobileToast('工作流任务 ID 不能为空', 'none');
                return;
              }
              submitting.value = true;
              try {
                await rejectTask(taskId, { comment: '不同意' });
                showMobileToast('处理成功', 'success');
              } catch (error) {
                const message = error instanceof Error ? error.message : '请稍后重试';
                showMobileToast(`处理失败：${message}`, 'none');
              } finally {
                submitting.value = false;
              }
            }

            /**
             * 撤回 {tableLabel} 移动端详情。
             */
            async function revoke(): Promise<void> {
              const confirmed = await showConfirm('撤回{tableLabel}', '二次确认：确定撤回当前流程？');
              if (!confirmed) {
                return;
              }
              if (!processInstanceId) {
                showMobileToast('流程实例 ID 不能为空', 'none');
                return;
              }
              submitting.value = true;
              try {
                await revokeMobileTask(processInstanceId, { reason: '移动端撤回' });
                showMobileToast('处理成功', 'success');
              } catch (error) {
                const message = error instanceof Error ? error.message : '请稍后重试';
                showMobileToast(`处理失败：${message}`, 'none');
              } finally {
                submitting.value = false;
              }
            }

            onMounted(() => {
              void loadDetail();
            });
            </script>

            <style scoped>
            .generated-detail-summary-card {
              display: flex;
              align-items: flex-start;
              justify-content: space-between;
              gap: 18rpx;
            }

            .generated-detail-summary-main {
              min-width: 0;
              flex: 1;
            }

            .generated-detail-label {
              color: #64748b;
              font-size: 23rpx;
              font-weight: 800;
              line-height: 1.35;
            }

            .generated-detail-title {
              margin: 8rpx 0 12rpx;
              color: #111827;
              font-size: 32rpx;
              font-weight: 900;
              line-height: 1.25;
              word-break: break-word;
            }

            .generated-workflow-tip {
              margin-bottom: 18rpx;
              padding: 16rpx;
              border: 1rpx solid #dbeafe;
              border-radius: 18rpx;
              color: #64748b;
              background: #f8fbff;
              font-size: 23rpx;
              line-height: 1.45;
            }
            </style>
            """);
  }

  private CodeTemplateDescriptor uniappApiTemplate() {
    return new CodeTemplateDescriptor(
        "uniapp-api",
        GenerationTarget.UNIAPP,
        "UniApp API",
        "zhyc-base-uniapp/src/api/{module}-{entity}.ts",
        """
            import { mobileRequest, type MobilePageResult } from './request';

            /**
             * 移动端 {table} 列表记录。
             */
            export interface {Entity}Record {
              /** {tableLabel}移动端记录主键，用于列表行键、详情查询和编辑定位。 */
              id: string;
              /** 租户业务编码，用于移动端请求头和后端租户隔离复核。 */
              tenantId: string;
              /** 字段展示值，用于移动端列表、表单和详情页展示。 */
              fields: string;
              /** 当前租户内未删除记录总数，用于移动端列表统计展示。 */
              totalCount: number;
            }

            /**
             * 移动端 {table} 保存载荷。
             */
            export interface {Entity}SavePayload {
              /** 移动端保存请求租户业务编码，用于后端复核租户隔离边界。 */
              tenantId: string;
              /** 移动端表单字段值，字段名来自当前模型字段白名单。 */
              values: Record<string, unknown>;
            }

            /**
             * 查询移动端 {table} 列表。
             *
             * @param pageNo 当前页码，从 1 开始
             * @param pageSize 每页记录数
             * @returns {table} 分页列表记录
             */
            export function listMobile{Entity}(pageNo = 1, pageSize = 20): Promise<MobilePageResult<{Entity}Record>> {
              return mobileRequest<MobilePageResult<{Entity}Record>>('/{module}/{entity}', {
                query: { pageNo, pageSize },
              });
            }

            /**
             * 查询移动端 {table} 详情。
             *
             * @param id {table} 主键
             * @returns {table} 详情记录
             */
            export function getMobile{Entity}(id: string): Promise<{Entity}Record> {
              return mobileRequest<{Entity}Record>(`/{module}/{entity}/${id}`);
            }

            /**
             * 保存移动端 {table} 表单。
             *
             * @param payload {table} 保存载荷
             * @returns 保存后的 {table} 记录
             */
            export function saveMobile{Entity}(payload: {Entity}SavePayload): Promise<{Entity}Record> {
              return mobileRequest<{Entity}Record, {Entity}SavePayload>('/{module}/{entity}', {
                method: 'POST',
                data: payload,
              });
            }

            /**
             * 更新移动端 {table} 表单。
             *
             * @param id {table} 主键
             * @param payload {table} 保存载荷
             * @returns 更新后的 {table} 记录
             */
            export function updateMobile{Entity}(id: string, payload: {Entity}SavePayload): Promise<{Entity}Record> {
              return mobileRequest<{Entity}Record, {Entity}SavePayload>(`/{module}/{entity}/${id}`, {
                method: 'PUT',
                data: payload,
              });
            }
            """);
  }

  private CodeTemplateDescriptor uniappPagesJsonTemplate() {
    return new CodeTemplateDescriptor(
        "uniapp-pages-json",
        GenerationTarget.UNIAPP,
        "UniApp 页面注册片段",
        "zhyc-base-uniapp/src/generated-pages/{module}-{entity}.pages.json",
        """
            {
              "pages": [
                {
                  "path": "pages/{module}/{entity}/list",
                  "style": {
                    "navigationBarTitleText": "{tableLabel}",
                    "navigationStyle": "custom"
                  }
                },
                {
                  "path": "pages/{module}/{entity}/form",
                  "style": {
                    "navigationBarTitleText": "{tableLabel}表单",
                    "navigationStyle": "custom"
                  }
                },
                {
                  "path": "pages/{module}/{entity}/detail",
                  "style": {
                    "navigationBarTitleText": "{tableLabel}详情",
                    "navigationStyle": "custom"
                  }
                }
              ]
            }
            """);
  }

  private CodeTemplateDescriptor openApiControllerTemplate() {
    return new CodeTemplateDescriptor(
        "openapi-controller",
        GenerationTarget.OPEN_API_PORTAL,
        "开放 API Controller",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/openapi/{Entity}OpenApiController.java",
        """
            package com.zhyc.{module}.openapi;

            import com.zhyc.common.api.ApiResult;
            import com.zhyc.common.exception.BusinessException;
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.RequestHeader;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RestController;

            /**
             * {table} 开放 API。
             */
            @RestController
            @RequestMapping("/openapi/v1/{module}/{entity}")
            public class {Entity}OpenApiController {

              /** 开放 API 编码，用于网关授权、审计和开发者门户定位。 */
              private static final String API_CODE = "{module}-{entity}";

              /**
               * 查询开放 API {table} 摘要。
               *
               * @param tenantId 租户业务编码
               * @param appCode 开放 API 应用编码，由开放 API 网关注入
               * @param apiCode 开放 API 接口编码，由开放 API 网关注入
               * @param requestId 调用方请求追踪编号，由开放 API 网关透传
               * @return {table} 摘要
               */
              @GetMapping
              public ApiResult<{Entity}OpenApiResponse> summary(
                  @RequestHeader(value = "X-ZHYC-Tenant-Id", required = false) String tenantId,
                  @RequestHeader(value = "X-ZHYC-App-Code", required = false) String appCode,
                  @RequestHeader(value = "X-ZHYC-Api-Code", required = false) String apiCode,
                  @RequestHeader(value = "X-ZHYC-Request-Id", required = false) String requestId) {
                requireGatewayContext(tenantId, appCode, apiCode);
                return ApiResult.ok(new {Entity}OpenApiResponse(API_CODE, requestId, tenantId, "{fields}"));
              }

              /**
               * 校验开放 API 网关注入的内部上下文。
               *
               * @param tenantId 租户业务编码
               * @param appCode 开放 API 应用编码
               * @param apiCode 开放 API 接口编码
               */
              private void requireGatewayContext(String tenantId, String appCode, String apiCode) {
                if (tenantId == null || tenantId.isBlank() || appCode == null || appCode.isBlank()
                    || apiCode == null || apiCode.isBlank()) {
                  throw new BusinessException("ZHYC_OPENAPI_GATEWAY_CONTEXT_MISSING", "开放 API 网关上下文不能为空");
                }
                if (!API_CODE.equals(apiCode)) {
                  throw new BusinessException("ZHYC_OPENAPI_API_CODE_MISMATCH", "开放 API 编码不匹配");
                }
              }
            }
            """);
  }

  private CodeTemplateDescriptor openApiDtoTemplate() {
    return new CodeTemplateDescriptor(
        "openapi-dto",
        GenerationTarget.OPEN_API_PORTAL,
        "开放 API DTO",
        "zhyc-module-{module}/src/main/java/com/zhyc/{module}/openapi/{Entity}OpenApiResponse.java",
        """
            package com.zhyc.{module}.openapi;

            /**
             * {table} 开放 API 响应。
             *
             * @param apiCode 开放 API 编码，用于调用方定位接口契约
             * @param requestId 请求追踪编号，用于联动网关审计日志
             * @param tenantId 租户业务编码
             * @param fields 字段清单
             */
            public record {Entity}OpenApiResponse(String apiCode, String requestId, String tenantId, String fields) {
            }
            """);
  }

  private CodeTemplateDescriptor openApiSignatureConfigTemplate() {
    return new CodeTemplateDescriptor(
        "openapi-signature-config",
        GenerationTarget.OPEN_API_PORTAL,
        "开放 API 签名配置",
        "zhyc-openapi-gateway/src/main/resources/openapi/{module}-{entity}-route.yml",
        """
            apiCode: {module}-{entity}
            pathPattern: /openapi/v1/{module}/{entity}
            httpMethod: GET
            backendRoute: http://zhyc-platform-app/openapi/v1/{module}/{entity}
            authModes:
              - API_KEY
              - OAUTH2
            signatureHeaders:
              - X-ZHYC-Access-Key
              - X-ZHYC-Timestamp
              - X-ZHYC-Nonce
              - X-ZHYC-Signature
            traceHeaders:
              - X-ZHYC-Request-Id
            auditEnabled: true
            auditFields:
              - apiCode
              - tenantId
              - accessKey
              - requestId
              - resultCode
              - costMillis
            rateLimitEnabled: true
            tenantHeader: X-ZHYC-Tenant-Id
            """);
  }

  private CodeTemplateDescriptor openApiRegistrationSqlTemplate() {
    return new CodeTemplateDescriptor(
        "openapi-registration-sql",
        GenerationTarget.OPEN_API_PORTAL,
        "开放 API 目录注册 SQL",
        "zhyc-module-{module}/src/main/resources/db/V1__{module}_{entity}_openapi.sql",
        """
            -- {table} 开放 API 目录与版本注册脚本。
            -- 本脚本用于让开放 API 网关通过 openapi_catalog/openapi_version 解析运行态后端路由。
            INSERT INTO openapi_catalog (
                api_code,
                api_name,
                group_code,
                http_method,
                path_pattern,
                status
            ) VALUES (
                '{module}-{entity}',
                '{table}开放 API',
                '{module}',
                'GET',
                '/openapi/v1/{module}/{entity}',
                'enabled'
            ) ON DUPLICATE KEY UPDATE
                api_name = VALUES(api_name),
                group_code = VALUES(group_code),
                http_method = VALUES(http_method),
                path_pattern = VALUES(path_pattern),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP;

            INSERT INTO openapi_version (
                api_code,
                version,
                backend_route,
                request_schema,
                response_schema,
                status
            ) VALUES (
                '{module}-{entity}',
                'v1',
                'http://zhyc-platform-app/openapi/v1/{module}/{entity}',
                JSON_OBJECT('method', 'GET', 'tenantHeader', 'X-ZHYC-Tenant-Id'),
                JSON_OBJECT('apiCode', '{module}-{entity}', 'fields', '{fields}'),
                'published'
            ) ON DUPLICATE KEY UPDATE
                backend_route = VALUES(backend_route),
                request_schema = VALUES(request_schema),
                response_schema = VALUES(response_schema),
                status = VALUES(status),
                updated_at = CURRENT_TIMESTAMP;
            """);
  }

  private CodeTemplateDescriptor openApiDocTemplate() {
    return new CodeTemplateDescriptor(
        "openapi-doc",
        GenerationTarget.OPEN_API_PORTAL,
        "开放 API 文档",
        "zhyc-openapi-portal/docs/{module}/{entity}.md",
        """
            # {table}开放 API

            - 模块编码：{module}
            - 实体编码：{entity}
            - 业务表名：{table}
            - 字段清单：{fields}

            ## 认证方式

            - API Key 面向系统集成，必须携带 `X-ZHYC-Access-Key`、`X-ZHYC-Timestamp`、`X-ZHYC-Nonce`、`X-ZHYC-Signature`。
            - OAuth2/OIDC 面向第三方应用，必须携带 `Authorization: Bearer <access_token>`。
            - 请求追踪：建议携带 `X-ZHYC-Request-Id`，用于网关审计关联。
            - 所有请求必须先进入开放 API 网关，由网关完成鉴权、限流、路由和调用审计。

            ## 接口地址

            - GET /openapi/v1/{module}/{entity}

            ## 后端路由

            - http://zhyc-platform-app/openapi/v1/{module}/{entity}

            ## 错误码

            - `ZHYC_OPENAPI_UNAUTHORIZED`：鉴权失败或 Token 已失效。
            - `ZHYC_OPENAPI_FORBIDDEN`：当前应用未获得该接口授权。
            - `ZHYC_OPENAPI_RATE_LIMITED`：租户、应用或接口维度触发限流。
            - `ZHYC_OPENAPI_INVALID_SIGNATURE`：API Key 签名请求头无效。

            ## 审计字段

            - `apiCode`: {module}-{entity}
            - `tenantId`：通过 `X-ZHYC-Tenant-Id` 进行共享表租户隔离。
            - `requestId`：来自 `X-ZHYC-Request-Id` 的网关追踪编号。
            - `resultCode`：稳定的网关或业务结果码。
            - `costMillis`：网关统计的请求耗时。
            """);
  }

  private CodeTemplateDescriptor openApiPortalDebugApiTemplate() {
    return new CodeTemplateDescriptor(
        "openapi-portal-debug-api",
        GenerationTarget.OPEN_API_PORTAL,
        "开放 API 门户调试接口",
        "zhyc-base-vue/src/api/developer/{module}-{entity}-debug.ts",
        """
            import { request } from '@/api/http';

            /**
             * {table} 开放 API 调试请求。
             */
            export interface {Entity}OpenApiDebugRequest {
              /** 租户业务编码，用于后台调试代理校验租户边界。 */
              tenantId: string;
              /** 开放 API 业务编码。 */
              apiCode: string;
              /** HTTP 请求方法。 */
              method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
              /** 开放 API 请求路径。 */
              path: string;
              /** API Key 或 OAuth2/OIDC 调试认证方式。 */
              authMode: 'API_KEY' | 'OAUTH2';
              /** API Key Access Key；不得包含 Secret 明文。 */
              accessKey?: string;
              /** API Key 签名时间戳，用于网关防重放校验。 */
              timestamp?: string;
              /** API Key 签名 nonce，用于网关防重放校验。 */
              nonce?: string;
              /** API Key 签名值。 */
              signature?: string;
              /** OAuth2/OIDC 访问令牌，仅用于调试代理本次请求。 */
              bearerToken?: string;
              /** 请求追踪编号，用于关联网关审计日志。 */
              requestId: string;
              /** 请求体文本，GET 请求可为空字符串。 */
              body: string;
            }

            /**
             * {table} 开放 API 调试响应。
             */
            export interface {Entity}OpenApiDebugResponse {
              /** 请求追踪编号。 */
              requestId: string;
              /** 开放 API 业务编码。 */
              apiCode: string;
              /** 网关或后端响应状态码。 */
              httpStatus: number;
              /** 调试调用是否成功。 */
              success: boolean;
              /** 对外稳定错误码，成功时为空。 */
              errorCode?: string;
              /** 网关统计的调用耗时毫秒数。 */
              costMillis: number;
              /** 响应体文本，用于开发者核对接口契约。 */
              responseBody: string;
            }

            /**
             * 通过后台调试代理调用 {table} 开放 API。
             *
             * <p>页面只调用后台管理 API client，由服务端代理进入开放 API 网关，避免前端直接拼接网关地址。</p>
             *
             * @param command 开放 API 调试请求
             * @returns 开放 API 调试响应
             */
            export function invoke{Entity}OpenApiDebug(
              command: {Entity}OpenApiDebugRequest,
            ): Promise<{Entity}OpenApiDebugResponse> {
              return request<{Entity}OpenApiDebugResponse, {Entity}OpenApiDebugRequest>('/openapi/debug/invoke', {
                method: 'POST',
                body: command,
              });
            }
            """);
  }

  private CodeTemplateDescriptor openApiPortalPageTemplate() {
    return new CodeTemplateDescriptor(
        "openapi-portal-page",
        GenerationTarget.OPEN_API_PORTAL,
        "开放 API 门户页面",
        "zhyc-base-vue/src/views/developer/{module}-{entity}/index.vue",
        """
            <template>
              <section data-module="{module}" data-entity="{entity}">
                <a-card title="{table} API 文档" :bordered="false">
                  <a-descriptions size="small" :column="1" bordered>
                    <a-descriptions-item label="模块">{module}</a-descriptions-item>
                    <a-descriptions-item label="实体">{entity}</a-descriptions-item>
                    <a-descriptions-item label="字段">{fields}</a-descriptions-item>
                    <a-descriptions-item label="接口">GET /openapi/v1/{module}/{entity}</a-descriptions-item>
                  </a-descriptions>
                </a-card>

                <a-card class="debug-panel" title="调试控制台" :bordered="false">
                  <a-alert
                    message="API Key 面向系统集成，OAuth2/OIDC 面向第三方应用，所有请求必须进入开放 API 网关。"
                    type="info"
                    show-icon
                  />
                    <a-form class="debug-form" layout="vertical" :model="debugCommand">
                      <a-form-item label="租户编码">
                      <a-input v-model:value="currentTenantId" disabled />
                    </a-form-item>
                    <a-form-item label="认证方式">
                      <a-select v-model:value="debugCommand.authMode" :options="authModeOptions" />
                    </a-form-item>
                    <a-form-item v-if="debugCommand.authMode === 'API_KEY'" label="Access Key">
                      <a-input v-model:value="debugCommand.accessKey" placeholder="X-ZHYC-Access-Key" />
                    </a-form-item>
                    <a-form-item v-if="debugCommand.authMode === 'API_KEY'" label="时间戳">
                      <a-input v-model:value="debugCommand.timestamp" placeholder="X-ZHYC-Timestamp" />
                    </a-form-item>
                    <a-form-item v-if="debugCommand.authMode === 'API_KEY'" label="Nonce">
                      <a-input v-model:value="debugCommand.nonce" placeholder="X-ZHYC-Nonce" />
                    </a-form-item>
                    <a-form-item v-if="debugCommand.authMode === 'API_KEY'" label="签名">
                      <a-input v-model:value="debugCommand.signature" placeholder="X-ZHYC-Signature" />
                    </a-form-item>
                    <a-form-item v-if="debugCommand.authMode === 'OAUTH2'" label="Bearer Token">
                      <a-input v-model:value="debugCommand.bearerToken" placeholder="OAuth2/OIDC Access Token" />
                    </a-form-item>
                    <a-form-item label="请求追踪编号">
                      <a-input v-model:value="debugCommand.requestId" placeholder="X-ZHYC-Request-Id" />
                    </a-form-item>
                    <a-space>
                      <a-button @click="buildDebugSnapshot">生成调试快照</a-button>
                      <a-button type="primary" :loading="debugInvoking" @click="invokeDebugRequest">发送调试请求</a-button>
                    </a-space>
                  </a-form>
                  <pre>{{ debugSnapshot }}</pre>
                  <pre v-if="debugResponse" class="debug-response">{{ debugResponse }}</pre>
                </a-card>
              </section>
            </template>

            <script setup lang="ts">
            import { reactive, ref } from 'vue';
            import { message } from 'ant-design-vue';
            import {
              invoke{Entity}OpenApiDebug,
              type {Entity}OpenApiDebugRequest,
              type {Entity}OpenApiDebugResponse,
            } from '@/api/developer/{module}-{entity}-debug';
            import { requireAdminTenantId } from '@/utils/adminContext';

            /**
             * {table} 开放 API 调试命令。
             */
            interface OpenApiDebugCommand {
              /** 开放 API 认证方式。 */
              authMode: 'API_KEY' | 'OAUTH2';
              /** API Key Access Key，请勿在页面中保存 Secret。 */
              accessKey: string;
              /** API Key 签名时间戳，用于网关防重放校验。 */
              timestamp: string;
              /** API Key 签名 nonce，用于网关防重放校验。 */
              nonce: string;
              /** API Key 签名值，由调用方后端按签名原文计算。 */
              signature: string;
              /** OAuth2/OIDC 访问令牌。 */
              bearerToken: string;
              /** 请求追踪编号，用于关联网关审计日志。 */
              requestId: string;
            }

            /** {table} 调试命令，默认指向当前生成接口。 */
            const debugCommand = reactive<OpenApiDebugCommand>({
              authMode: 'API_KEY',
              accessKey: '<access-key>',
              timestamp: String(Date.now()),
              nonce: createRequestNonce(),
              signature: '<signature>',
              bearerToken: '',
              requestId: `debug-{module}-{entity}-${Date.now()}`,
            });

            /** 当前后台租户业务编码，只读展示，实际请求以统一上下文为准。 */
            const currentTenantId = ref(requireAdminTenantId());

            /** 开放 API 调试认证方式选项。 */
            const authModeOptions = [
              { label: 'API Key 签名', value: 'API_KEY' },
              { label: 'OAuth2/OIDC Bearer Token', value: 'OAUTH2' },
            ];

            /** {table} 开放 API 调试快照。 */
            const debugSnapshot = ref('点击“生成调试快照”查看请求头、签名原文和审计字段。');
            /** {table} 开放 API 调试代理响应。 */
            const debugResponse = ref('');
            /** {table} 开放 API 调试请求提交状态。 */
            const debugInvoking = ref(false);

            /**
             * 构建开放 API 调试快照。
             */
            function buildDebugSnapshot(): void {
              currentTenantId.value = requireAdminTenantId();
              debugCommand.timestamp = String(Date.now());
              debugCommand.nonce = createRequestNonce();
              const signaturePayload = `GET\\n/openapi/v1/{module}/{entity}\\n${debugCommand.timestamp}\\n${debugCommand.nonce}\\n`;
              const headers = debugCommand.authMode === 'OAUTH2'
                ? {
                    'Authorization': `Bearer ${debugCommand.bearerToken || '<access-token>'}`,
                    'X-ZHYC-Tenant-Id': currentTenantId.value,
                    'X-ZHYC-Request-Id': debugCommand.requestId,
                  }
                : {
                    'X-ZHYC-Access-Key': debugCommand.accessKey,
                    'X-ZHYC-Timestamp': debugCommand.timestamp,
                    'X-ZHYC-Nonce': debugCommand.nonce,
                    'X-ZHYC-Signature': debugCommand.signature,
                    'X-ZHYC-Tenant-Id': currentTenantId.value,
                    'X-ZHYC-Request-Id': debugCommand.requestId,
                  };

              debugSnapshot.value = JSON.stringify(
                {
                  method: 'GET',
                  path: '/openapi/v1/{module}/{entity}',
                  tenantHeader: 'X-ZHYC-Tenant-Id',
                  requestIdHeader: 'X-ZHYC-Request-Id',
                  authMode: debugCommand.authMode,
                  headers,
                  signaturePayload,
                  apiCode: '{module}-{entity}',
                  auditFields: ['apiCode', 'tenantId', 'accessKey', 'requestId', 'resultCode', 'costMillis'],
                  errorCodes: [
                    'ZHYC_OPENAPI_UNAUTHORIZED',
                    'ZHYC_OPENAPI_FORBIDDEN',
                    'ZHYC_OPENAPI_RATE_LIMITED',
                    'ZHYC_OPENAPI_INVALID_SIGNATURE',
                  ],
                },
                null,
                2,
              );
              message.success('调试快照已生成');
            }

            /**
             * 通过后台代理发送开放 API 调试请求。
             */
            async function invokeDebugRequest(): Promise<void> {
              debugInvoking.value = true;
              try {
                const response: {Entity}OpenApiDebugResponse = await invoke{Entity}OpenApiDebug(buildDebugRequest());
                debugResponse.value = JSON.stringify(response, null, 2);
                message.success('调试请求已发送');
              } catch (error) {
                message.error(error instanceof Error ? error.message : '调试请求失败');
              } finally {
                debugInvoking.value = false;
              }
            }

            /**
             * 构建开放 API 调试代理请求。
             *
             * @returns 开放 API 调试代理请求
             */
            function buildDebugRequest(): {Entity}OpenApiDebugRequest {
              currentTenantId.value = requireAdminTenantId();
              return {
                tenantId: requireAdminTenantId(),
                apiCode: '{module}-{entity}',
                method: 'GET',
                path: '/openapi/v1/{module}/{entity}',
                authMode: debugCommand.authMode,
                accessKey: debugCommand.authMode === 'API_KEY' ? debugCommand.accessKey : undefined,
                timestamp: debugCommand.authMode === 'API_KEY' ? debugCommand.timestamp : undefined,
                nonce: debugCommand.authMode === 'API_KEY' ? debugCommand.nonce : undefined,
                signature: debugCommand.authMode === 'API_KEY' ? debugCommand.signature : undefined,
                bearerToken: debugCommand.authMode === 'OAUTH2' ? debugCommand.bearerToken : undefined,
                requestId: debugCommand.requestId,
                body: '',
              };
            }

            /**
             * 生成调试请求 nonce。
             *
             * @returns 每次调试请求独立的 nonce
             */
            function createRequestNonce(): string {
              const cryptoApi = globalThis.crypto;
              if (cryptoApi?.getRandomValues) {
                const randomValues = new Uint32Array(4);
                cryptoApi.getRandomValues(randomValues);
                return Array.from(randomValues)
                  .map((value) => value.toString(16).padStart(8, '0'))
                  .join('');
              }
              return `${Date.now()}${Math.random().toString(16).slice(2)}`;
            }
            </script>

            <style scoped>
            .debug-panel {
              margin-top: 16px;
            }

            .debug-form {
              margin-top: 16px;
              max-width: 560px;
            }

            .debug-response {
              border-color: #bfdbfe;
              background: #eff6ff;
            }

            pre {
              margin: 16px 0 0;
              padding: 12px;
              overflow: auto;
              white-space: pre-wrap;
              background: #f8fafc;
              border: 1px solid #e5e7eb;
              border-radius: 6px;
            }
            </style>
            """);
  }

  private CodeTemplateDescriptor microservicePomTemplate() {
    return new CodeTemplateDescriptor(
        "microservice-pom",
        GenerationTarget.MICROSERVICE_MODULE,
        "微服务模块 Maven 工程",
        "zhyc-service-{module}-{entity}/pom.xml",
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>

              <parent>
                <groupId>com.zhyc</groupId>
                <artifactId>zhyc-base-server</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <relativePath>../pom.xml</relativePath>
              </parent>

              <artifactId>zhyc-service-{module}-{entity}</artifactId>
              <name>zhyc-service-{module}-{entity}</name>
              <description>{table} 微服务模块工程骨架</description>

              <dependencies>
                <dependency>
                  <groupId>com.zhyc</groupId>
                  <artifactId>zhyc-common</artifactId>
                  <version>${project.version}</version>
                </dependency>
                <dependency>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-starter-web</artifactId>
                </dependency>
                <dependency>
                  <groupId>org.mybatis.spring.boot</groupId>
                  <artifactId>mybatis-spring-boot-starter</artifactId>
                </dependency>
              </dependencies>
            </project>
            """);
  }

  private CodeTemplateDescriptor microserviceApplicationTemplate() {
    return new CodeTemplateDescriptor(
        "microservice-application",
        GenerationTarget.MICROSERVICE_MODULE,
        "微服务模块启动类",
        "zhyc-service-{module}-{entity}/src/main/java/com/zhyc/service/{module}/{entity}/{Entity}ServiceApplication.java",
        """
            package com.zhyc.service.{module}.{entity};

            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;

            /**
             * {table} 微服务模块启动入口。
             *
             * <p>该工程用于未来 Spring Cloud 拆分部署，首期只生成模块化单体之外的独立服务骨架，
             * 不直接复用核心平台 Shiro 内部实现。</p>
             */
            @SpringBootApplication(scanBasePackages = "com.zhyc")
            public class {Entity}ServiceApplication {

              /**
               * 启动 {table} 微服务模块。
               *
               * @param args 命令行启动参数
               */
              public static void main(String[] args) {
                SpringApplication.run({Entity}ServiceApplication.class, args);
              }
            }
            """);
  }

  private CodeTemplateDescriptor microserviceControllerTemplate() {
    return new CodeTemplateDescriptor(
        "microservice-controller",
        GenerationTarget.MICROSERVICE_MODULE,
        "微服务模块基础接口",
        "zhyc-service-{module}-{entity}/src/main/java/com/zhyc/service/{module}/{entity}/controller/{Entity}ServiceInfoController.java",
        """
            package com.zhyc.service.{module}.{entity}.controller;

            import com.zhyc.common.api.ApiResult;
            import com.zhyc.common.exception.BusinessException;
            import java.util.Map;
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.RequestHeader;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RestController;

            /**
             * {table} 微服务模块基础信息接口。
             *
             * <p>该接口用于服务注册、联调和网关探活，所有调用必须携带租户请求头，
             * 避免微服务拆分后绕过共享表租户隔离边界。</p>
             */
            @RestController
            @RequestMapping("/internal/{module}/{entity}")
            public class {Entity}ServiceInfoController {

              /** 租户业务编码请求头。 */
              public static final String HEADER_TENANT_ID = "X-ZHYC-Tenant-Id";
              /** 租户请求头缺失错误码。 */
              private static final String ERROR_TENANT_REQUIRED = "ZHYC_MICROSERVICE_TENANT_REQUIRED";

              /**
               * 查询 {table} 微服务模块基础信息。
               *
               * @param tenantId 租户业务编码
               * @return 当前微服务模块基础信息
               */
              @GetMapping("/info")
              public ApiResult<Map<String, String>> info(@RequestHeader(HEADER_TENANT_ID) String tenantId) {
                if (tenantId == null || tenantId.isBlank()) {
                  throw new BusinessException(ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
                }
                return ApiResult.ok(Map.of(
                    "moduleCode", "{module}-{entity}",
                    "serviceName", "zhyc-service-{module}-{entity}",
                    "tenantId", tenantId,
                    "permissionPrefix", "{module}:{entity}"));
              }
            }
            """);
  }

  private CodeTemplateDescriptor microserviceModuleDescriptorTemplate() {
    return new CodeTemplateDescriptor(
        "microservice-module-descriptor",
        GenerationTarget.MICROSERVICE_MODULE,
        "微服务模块描述文件",
        "zhyc-service-{module}-{entity}/src/main/resources/META-INF/zhyc-module.yml",
        """
            moduleCode: {module}-{entity}
            moduleName: {table}微服务模块
            moduleType: MICROSERVICE
            serviceName: zhyc-service-{module}-{entity}
            tenantMode: tenant_id
            permissionPrefix: {module}:{entity}
            backendPackage: com.zhyc.service.{module}.{entity}
            openApiGatewayRequired: true
            authServerRequired: true
            databaseScripts:
              - classpath:db/V1__{module}_{entity}_service.sql
            extensionPoints:
              - name: field-type-mapper
                description: 字段类型映射扩展点
              - name: ddl-generator
                description: DDL 生成器扩展点
              - name: pagination-dialect
                description: 分页方言扩展点
            """);
  }

  private CodeTemplateDescriptor microserviceApplicationYamlTemplate() {
    return new CodeTemplateDescriptor(
        "microservice-application-yml",
        GenerationTarget.MICROSERVICE_MODULE,
        "微服务模块配置样例",
        "zhyc-service-{module}-{entity}/src/main/resources/application.yml",
        """
            spring:
              application:
                name: zhyc-service-{module}-{entity}
              datasource:
                url: jdbc:mysql://127.0.0.1:3306/zhyc_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
                username: ${ZHYC_DB_USERNAME:zhyc}
                password: ${ZHYC_DB_PASSWORD:}

            zhyc:
              tenant:
                isolation-mode: tenant_id
              security:
                auth-server-url: ${ZHYC_AUTH_SERVER_URL:http://127.0.0.1:9000}
                openapi-gateway-url: ${ZHYC_OPENAPI_GATEWAY_URL:http://127.0.0.1:9100}
              module:
                code: {module}-{entity}
                name: {table}微服务模块
            """);
  }

  private CodeTemplateDescriptor microserviceReadmeTemplate() {
    return new CodeTemplateDescriptor(
        "microservice-readme",
        GenerationTarget.MICROSERVICE_MODULE,
        "微服务模块说明文档",
        "zhyc-service-{module}-{entity}/README.md",
        """
            # {table} 微服务模块

            本工程由低代码生成器创建，用于未来从核心平台模块化单体拆分为独立微服务。

            ## 边界约束

            - 服务名：`zhyc-service-{module}-{entity}`
            - 业务模块：`{module}`
            - 业务对象：`{entity}`
            - 租户隔离：共享库共享表，必须通过 `tenant_id` 隔离。
            - 认证边界：通过认证中心校验 OAuth2/OIDC Token，不共享核心平台 Shiro 内部实现。
            - 开放 API：外部调用必须先进入开放 API 网关，由网关完成 API Key、OAuth2、签名、限流和审计。

            ## 后续接入步骤

            1. 在父工程 `pom.xml` 的 `modules` 中登记本模块。
            2. 按模块描述文件补齐菜单、权限、字典、数据库脚本和初始化数据。
            3. 为所有租户业务查询补充 `tenant_id` 条件和越权测试。
            4. 通过平台工作流门面接入审批流程，禁止直接依赖 Flowable API。
            """);
  }
}
