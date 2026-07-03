/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform;

import com.zhyc.lowcode.metadata.controller.LowcodeMetadataController;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.common.module.ModuleDescriptorClasspathLoader;
import com.zhyc.common.module.ModuleRegistry;
import com.zhyc.lowcode.metadata.mybatis.LowcodeDataSourceMapper;
import com.zhyc.lowcode.metadata.service.LowcodeMetadataService;
import com.zhyc.lowcode.generator.LowcodeGeneratorController;
import com.zhyc.lowcode.generator.LowcodeGeneratorService;
import com.zhyc.lowcode.generator.GeneratedFileWriter;
import com.zhyc.lowcode.generator.LowcodeGenerationExecuteCommand;
import com.zhyc.lowcode.generator.GenerationTarget;
import com.zhyc.lowcode.generator.GeneratedFileOverwriteStrategy;
import com.zhyc.lowcode.generator.mybatis.LowcodeGenerationRecordMapper;
import com.zhyc.cms.controller.CmsController;
import com.zhyc.cms.mapper.CmsMapper;
import com.zhyc.cms.service.CmsService;
import com.zhyc.visual.controller.VisualController;
import com.zhyc.visual.mapper.VisualMapper;
import com.zhyc.visual.service.VisualService;
import com.zhyc.i18n.controller.I18nController;
import com.zhyc.i18n.mapper.I18nMapper;
import com.zhyc.i18n.service.I18nService;
import com.zhyc.search.controller.SearchController;
import com.zhyc.search.mapper.SearchMapper;
import com.zhyc.search.service.SearchService;
import com.zhyc.file.object.controller.FileObjectController;
import com.zhyc.file.object.mapper.FileObjectMapper;
import com.zhyc.file.object.service.FileObjectService;
import com.zhyc.file.preview.controller.FilePreviewController;
import com.zhyc.file.preview.mapper.FilePreviewMapper;
import com.zhyc.file.preview.service.FilePreviewService;
import com.zhyc.file.storage.controller.FileStorageConfigController;
import com.zhyc.file.storage.mapper.FileStorageConfigMapper;
import com.zhyc.file.storage.service.FileStorageConfigService;
import com.zhyc.job.task.controller.JobTaskController;
import com.zhyc.job.task.mapper.JobTaskMapper;
import com.zhyc.job.task.service.JobTaskService;
import com.zhyc.message.inbox.controller.MsgMessageController;
import com.zhyc.message.inbox.mapper.MsgMessageMapper;
import com.zhyc.message.inbox.service.MsgMessageService;
import com.zhyc.message.template.controller.MsgTemplateController;
import com.zhyc.message.template.mapper.MsgTemplateMapper;
import com.zhyc.message.template.service.MsgTemplateService;
import com.zhyc.openapi.app.controller.OpenApiAppController;
import com.zhyc.openapi.app.mapper.OpenApiAppMapper;
import com.zhyc.openapi.app.service.OpenApiAppService;
import com.zhyc.openapi.apikey.controller.OpenApiApiKeyController;
import com.zhyc.openapi.apikey.mapper.OpenApiApiKeyMapper;
import com.zhyc.openapi.apikey.service.OpenApiApiKeyService;
import com.zhyc.openapi.audit.controller.OpenApiCallAuditController;
import com.zhyc.openapi.audit.mapper.OpenApiCallAuditMapper;
import com.zhyc.openapi.audit.service.OpenApiCallAuditService;
import com.zhyc.openapi.permission.controller.OpenApiPermissionController;
import com.zhyc.openapi.permission.mapper.OpenApiPermissionMapper;
import com.zhyc.openapi.permission.service.OpenApiPermissionService;
import com.zhyc.platform.config.ShiroConfig;
import com.zhyc.platform.security.PlatformTokenPrincipalMapper;
import com.zhyc.platform.security.PlatformShiroSubjectAuthenticator;
import com.zhyc.platform.monitor.RuntimeMonitorController;
import com.zhyc.platform.monitor.RuntimeMonitorService;
import com.zhyc.platform.security.PlatformUserRealm;
import com.zhyc.common.workflow.WorkflowService;
import com.zhyc.purchase.order.controller.PurOrderAdminController;
import com.zhyc.purchase.order.controller.PurOrderOpenApiController;
import com.zhyc.purchase.order.mapper.PurOrderMapper;
import com.zhyc.purchase.order.service.PurOrderCommandService;
import com.zhyc.purchase.request.controller.PurRequestAdminController;
import com.zhyc.purchase.request.controller.PurRequestOpenApiController;
import com.zhyc.purchase.request.mapper.PurRequestMapper;
import com.zhyc.purchase.request.service.PurRequestCommandService;
import com.zhyc.purchase.request.service.PurRequestStatusService;
import com.zhyc.workflow.controller.WorkflowTaskController;
import com.zhyc.workflow.repository.WorkflowRuntimeRepository;
import com.zhyc.workflow.service.WorkflowTaskService;
import com.zhyc.system.adminscope.controller.SysAdminScopeController;
import com.zhyc.system.adminscope.mapper.SysAdminScopeMapper;
import com.zhyc.system.adminscope.service.AdminScopeBindCommand;
import com.zhyc.system.adminscope.service.SysAdminScopeService;
import com.zhyc.system.accessrestriction.controller.SysAccessRestrictionController;
import com.zhyc.system.accessrestriction.mapper.SysAccessRestrictionMapper;
import com.zhyc.system.accessrestriction.service.SysAccessRestrictionService;
import com.zhyc.system.audit.controller.SysAuditLogController;
import com.zhyc.system.audit.mapper.SysAuditLogMapper;
import com.zhyc.system.audit.service.SysAuditLogService;
import com.zhyc.system.coderule.controller.SysCodeRuleController;
import com.zhyc.system.coderule.mapper.SysCodeRuleMapper;
import com.zhyc.system.coderule.service.SysCodeRuleService;
import com.zhyc.system.passwordpolicy.controller.SysPasswordPolicyController;
import com.zhyc.system.passwordpolicy.mapper.SysPasswordPolicyMapper;
import com.zhyc.system.passwordpolicy.service.SysPasswordPolicyService;
import com.zhyc.system.dict.controller.SysDictController;
import com.zhyc.system.dict.mapper.SysDictMapper;
import com.zhyc.system.dict.service.SysDictService;
import com.zhyc.system.exceptionlog.controller.SysExceptionLogController;
import com.zhyc.system.exceptionlog.mapper.SysExceptionLogMapper;
import com.zhyc.system.exceptionlog.service.SysExceptionLogRecordCommand;
import com.zhyc.system.exceptionlog.service.SysExceptionLogService;
import com.zhyc.system.loginlog.controller.SysLoginLogController;
import com.zhyc.system.loginlog.mapper.SysLoginLogMapper;
import com.zhyc.system.loginlog.service.SysLoginLogRecordCommand;
import com.zhyc.system.loginlog.service.SysLoginLogService;
import com.zhyc.system.menu.controller.SysMenuController;
import com.zhyc.system.menu.mapper.SysMenuMapper;
import com.zhyc.system.menu.service.SysMenuService;
import com.zhyc.system.module.controller.SysModuleController;
import com.zhyc.system.module.mapper.SysModuleMapper;
import com.zhyc.system.module.service.SysModuleResponse;
import com.zhyc.system.module.service.SysModuleService;
import com.zhyc.system.org.controller.SysOrgController;
import com.zhyc.system.org.mapper.SysOrgMapper;
import com.zhyc.system.org.service.SysOrgService;
import com.zhyc.system.param.controller.SysParamController;
import com.zhyc.system.param.mapper.SysParamMapper;
import com.zhyc.system.param.service.SysParamService;
import com.zhyc.system.post.controller.SysPostController;
import com.zhyc.system.post.mapper.SysPostMapper;
import com.zhyc.system.post.service.SysPostService;
import com.zhyc.system.permissionaudit.controller.SysPermissionAuditController;
import com.zhyc.system.permissionaudit.mapper.SysPermissionAuditMapper;
import com.zhyc.system.permissionaudit.service.SysPermissionAuditRecordCommand;
import com.zhyc.system.permissionaudit.service.SysPermissionAuditService;
import com.zhyc.system.permission.mapper.SysPermissionMapper;
import com.zhyc.system.permission.service.SysPermissionService;
import com.zhyc.system.role.controller.SysRoleController;
import com.zhyc.system.role.controller.SysRoleDataScopeController;
import com.zhyc.system.role.mapper.SysRoleDataScopeMapper;
import com.zhyc.system.role.mapper.SysRoleMapper;
import com.zhyc.system.role.service.RoleDataScopeBindCommand;
import com.zhyc.system.role.service.RoleMenuBindCommand;
import com.zhyc.system.role.service.SysRoleDataScopeService;
import com.zhyc.system.role.service.SysRoleService;
import com.zhyc.system.tenant.controller.SysTenantController;
import com.zhyc.system.tenant.mapper.SysTenantMapper;
import com.zhyc.system.tenant.service.SysTenantService;
import com.zhyc.system.tenantpackage.controller.SysTenantPackageController;
import com.zhyc.system.tenantpackage.mapper.SysTenantPackageMapper;
import com.zhyc.system.tenantpackage.service.SysTenantPackageService;
import com.zhyc.system.tenantpackagemodule.controller.SysTenantPackageModuleController;
import com.zhyc.system.tenantpackagemodule.mapper.SysTenantPackageModuleMapper;
import com.zhyc.system.tenantpackagemodule.service.SysTenantPackageModuleService;
import com.zhyc.system.tenantparam.controller.SysTenantParamController;
import com.zhyc.system.tenantparam.mapper.SysTenantParamMapper;
import com.zhyc.system.tenantparam.service.SysTenantParamService;
import com.zhyc.system.user.controller.SysUserPostController;
import com.zhyc.system.user.mapper.SysUserMapper;
import com.zhyc.system.user.mapper.SysUserPostMapper;
import com.zhyc.system.user.service.SysUserPostBindCommand;
import com.zhyc.system.user.service.SysUserAuthService;
import com.zhyc.system.user.service.SysUserPostService;
import org.apache.shiro.realm.Realm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 平台应用启动烟测，验证 Spring 容器在测试配置下可以正常创建。
 */
@ActiveProfiles("test")
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:zhyc_platform;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema-lowcode-test.sql",
    "zhyc.workflow.flowable.enabled=false"
})
class ZhycPlatformApplicationTests {

  /** Spring 应用上下文。 */
  @Autowired
  private ApplicationContext applicationContext;

  /** 可配置 Spring 应用上下文，用于读取基础设施 Bean 定义。 */
  @Autowired
  private ConfigurableApplicationContext configurableApplicationContext;

  /** 测试数据库访问模板，用于准备跨模块集成烟测的最小业务数据。 */
  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * 验证应用上下文可以加载完成。
   */
  @Test
  void contextLoads() {
  }

  /**
   * 验证 Shiro 事件总线处理器由平台静态配置接管，避免回退到 Shiro starter 非静态自动配置。
   */
  @Test
  void shouldUsePlatformStaticShiroEventBusPostProcessorDefinition() {
    BeanDefinition beanDefinition = configurableApplicationContext.getBeanFactory()
        .getBeanDefinition("shiroEventBusAwareBeanPostProcessor");
    assertEquals("shiroEventBusAwareBeanPostProcessor", beanDefinition.getFactoryMethodName());
    assertEquals(ShiroConfig.class.getName(), beanDefinition.getBeanClassName());
    assertNull(beanDefinition.getFactoryBeanName());
  }

  /**
   * 验证平台应用会组合低代码模块并注册其 MyBatis Mapper。
   */
  @Test
  void shouldRegisterLowcodeMappers() {
    assertNotNull(applicationContext.getBean(LowcodeDataSourceMapper.class));
    assertNotNull(applicationContext.getBean(LowcodeGenerationRecordMapper.class));
  }

  /**
   * 验证平台应用会注册低代码元数据服务，供后续后台接口直接注入使用。
   */
  @Test
  void shouldRegisterLowcodeMetadataService() {
    assertNotNull(applicationContext.getBean(LowcodeMetadataService.class));
  }

  /**
   * 验证平台应用会注册低代码元数据管理接口，供后台管理端调用。
   */
  @Test
  void shouldRegisterLowcodeMetadataController() {
    assertNotNull(applicationContext.getBean(LowcodeMetadataController.class));
  }

  /**
   * 验证平台应用会注册低代码生成服务，供后台生成预览和后续写入任务复用。
   */
  @Test
  void shouldRegisterLowcodeGeneratorService() {
    assertNotNull(applicationContext.getBean(LowcodeGeneratorService.class));
  }

  /**
   * 验证平台应用会注册生成文件写入器，供低代码生成执行服务使用。
   */
  @Test
  void shouldRegisterGeneratedFileWriter() {
    assertNotNull(applicationContext.getBean(GeneratedFileWriter.class));
  }

  /**
   * 验证平台生成服务已装配执行依赖，不会因为缺少写入器或记录仓储直接失败。
   */
  @Test
  void shouldWireLowcodeGeneratorExecutionDependencies() {
    LowcodeGeneratorService service = applicationContext.getBean(LowcodeGeneratorService.class);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.execute(new LowcodeGenerationExecuteCommand(
            "tenant_a", "missing_table", GenerationTarget.UNIAPP, "purchase", "purchaseOrder",
            GeneratedFileOverwriteStrategy.FAIL_IF_EXISTS)));

    assertEquals("ZHYC_LOWCODE_METADATA_TABLE_MODEL_NOT_FOUND", exception.getCode());
    assertEquals("低代码表模型不存在: missing_table", exception.getMessage());
  }

  /**
   * 验证平台应用会注册低代码生成接口，供后台管理端调用模板清单和生成预览。
   */
  @Test
  void shouldRegisterLowcodeGeneratorController() {
    assertNotNull(applicationContext.getBean(LowcodeGeneratorController.class));
  }

  /**
   * 验证平台应用会注册系统租户接口、服务和 Mapper，供 SaaS 租户管理页面使用。
   */
  @Test
  void shouldRegisterSystemTenantBeans() {
    assertNotNull(applicationContext.getBean(SysTenantController.class));
    assertNotNull(applicationContext.getBean(SysTenantService.class));
    assertNotNull(applicationContext.getBean(SysTenantMapper.class));
  }

  /**
   * 验证平台应用中的租户服务可以按状态查询空租户列表，避免缺少租户主表初始化。
   */
  @Test
  void shouldQueryEmptyTenantsByStatus() {
    SysTenantService service = applicationContext.getBean(SysTenantService.class);

    assertTrue(service.listTenants("enabled").isEmpty());
  }

  /**
   * 验证平台应用会注册系统租户套餐接口、服务和 Mapper，供 SaaS 套餐管理页面使用。
   */
  @Test
  void shouldRegisterSystemTenantPackageBeans() {
    assertNotNull(applicationContext.getBean(SysTenantPackageController.class));
    assertNotNull(applicationContext.getBean(SysTenantPackageService.class));
    assertNotNull(applicationContext.getBean(SysTenantPackageMapper.class));
  }

  /**
   * 验证平台应用中的租户套餐服务可以按状态查询空套餐列表，避免缺少套餐表初始化。
   */
  @Test
  void shouldQueryEmptyTenantPackagesByStatus() {
    SysTenantPackageService service = applicationContext.getBean(SysTenantPackageService.class);

    assertTrue(service.listPackages("enabled").isEmpty());
  }

  /**
   * 验证平台应用会注册租户套餐模块授权接口、服务和 Mapper，供 SaaS 套餐授权页面使用。
   */
  @Test
  void shouldRegisterSystemTenantPackageModuleBeans() {
    assertNotNull(applicationContext.getBean(SysTenantPackageModuleController.class));
    assertNotNull(applicationContext.getBean(SysTenantPackageModuleService.class));
    assertNotNull(applicationContext.getBean(SysTenantPackageModuleMapper.class));
  }

  /**
   * 验证平台应用中的套餐模块授权服务可以查询空授权列表，避免缺少授权表初始化。
   */
  @Test
  void shouldQueryEmptyTenantPackageModuleGrants() {
    SysTenantPackageModuleService service = applicationContext.getBean(SysTenantPackageModuleService.class);

    assertTrue(service.listGrants(10L).isEmpty());
  }

  /**
   * 验证平台应用会注册租户参数接口、服务和 Mapper，供 SaaS 租户参数页面使用。
   */
  @Test
  void shouldRegisterSystemTenantParamBeans() {
    assertNotNull(applicationContext.getBean(SysTenantParamController.class));
    assertNotNull(applicationContext.getBean(SysTenantParamService.class));
    assertNotNull(applicationContext.getBean(SysTenantParamMapper.class));
  }

  /**
   * 验证平台应用中的租户参数服务可以查询空参数列表，避免缺少租户参数表初始化。
   */
  @Test
  void shouldQueryEmptyTenantParamsByTenant() {
    SysTenantParamService service = applicationContext.getBean(SysTenantParamService.class);

    assertTrue(service.listParams("tenant_a").isEmpty());
  }

  /**
   * 验证平台应用会注册系统菜单接口、服务和 Mapper，供后台菜单权限页面使用。
   */
  @Test
  void shouldRegisterSystemMenuBeans() {
    assertNotNull(applicationContext.getBean(SysMenuController.class));
    assertNotNull(applicationContext.getBean(SysMenuService.class));
    assertNotNull(applicationContext.getBean(SysMenuMapper.class));
  }

  /**
   * 验证平台应用中的菜单服务可以按租户查询空菜单树，避免缺少系统表初始化。
   */
  @Test
  void shouldQueryEmptySystemMenuTreeByTenant() {
    SysMenuService service = applicationContext.getBean(SysMenuService.class);

    assertTrue(service.listMenuTree("tenant_a", false).isEmpty());
  }

  /**
   * 验证平台应用会注册系统权限服务和 Mapper，供 Shiro 授权链路使用。
   */
  @Test
  void shouldRegisterSystemPermissionBeans() {
    assertNotNull(applicationContext.getBean(SysPermissionService.class));
    assertNotNull(applicationContext.getBean(SysPermissionMapper.class));
  }

  /**
   * 验证平台应用会注册正式 Shiro 用户 Realm，而不是启动骨架阶段的临时 Realm。
   */
  @Test
  void shouldRegisterPlatformUserRealm() {
    Realm realm = applicationContext.getBean(Realm.class);

    assertTrue(realm instanceof PlatformUserRealm);
  }

  /**
   * 验证平台应用会注册认证中心令牌主体映射器，供认证中心身份映射到 Shiro 授权上下文。
   */
  @Test
  void shouldRegisterPlatformTokenPrincipalMapper() {
    assertNotNull(applicationContext.getBean(PlatformTokenPrincipalMapper.class));
  }

  /**
   * 验证平台应用会注册 Shiro Subject 登录适配器，供认证中心 Bearer Token 过滤器建立授权上下文。
   */
  @Test
  void shouldRegisterPlatformShiroSubjectAuthenticator() {
    assertNotNull(applicationContext.getBean(PlatformShiroSubjectAuthenticator.class));
  }

  /**
   * 验证平台应用会注册系统用户认证服务和 Mapper，供 Shiro Realm 查询用户凭证。
   */
  @Test
  void shouldRegisterSystemUserAuthBeans() {
    assertNotNull(applicationContext.getBean(SysUserAuthService.class));
    assertNotNull(applicationContext.getBean(SysUserMapper.class));
  }

  /**
   * 验证平台应用中的用户认证服务可以按租户和账号查询空账号，避免缺少系统用户表初始化。
   */
  @Test
  void shouldQueryEmptyLoginAccountByTenantAndUsername() {
    SysUserAuthService service = applicationContext.getBean(SysUserAuthService.class);

    assertTrue(service.findLoginAccount("tenant_a", "missing").isEmpty());
  }

  /**
   * 验证平台应用会注册系统用户岗位接口、服务和 Mapper，供用户岗位绑定页面使用。
   */
  @Test
  void shouldRegisterSystemUserPostBeans() {
    assertNotNull(applicationContext.getBean(SysUserPostController.class));
    assertNotNull(applicationContext.getBean(SysUserPostService.class));
    assertNotNull(applicationContext.getBean(SysUserPostMapper.class));
  }

  /**
   * 验证平台应用中的用户岗位服务可以按租户查询空绑定，并可执行空岗位绑定。
   */
  @Test
  void shouldQueryAndBindEmptyUserPostsByTenant() {
    SysUserPostService service = applicationContext.getBean(SysUserPostService.class);

    assertTrue(service.listUserPosts("tenant_a", 1001L).isEmpty());
    service.bindUserPosts(new SysUserPostBindCommand("tenant_a", 1001L, List.of()));
  }

  /**
   * 验证平台应用中的权限服务可以按租户和用户查询空授权列表。
   */
  @Test
  void shouldQueryEmptyUserPermissionsByTenantAndUser() {
    SysPermissionService service = applicationContext.getBean(SysPermissionService.class);

    assertTrue(service.listUserPermissions("tenant_a", 1001L).isEmpty());
  }

  /**
   * 验证平台应用会注册系统角色接口、服务和 Mapper，供后台角色授权页面使用。
   */
  @Test
  void shouldRegisterSystemRoleBeans() {
    assertNotNull(applicationContext.getBean(SysRoleController.class));
    assertNotNull(applicationContext.getBean(SysRoleService.class));
    assertNotNull(applicationContext.getBean(SysRoleMapper.class));
  }

  /**
   * 验证平台应用中的角色服务可以按租户查询空角色列表，并可执行空菜单绑定。
   */
  @Test
  void shouldQueryEmptyRolesAndBindEmptyMenusByTenant() {
    SysRoleService service = applicationContext.getBean(SysRoleService.class);

    assertTrue(service.listRoles("tenant_a").isEmpty());
    ensureTenantRole("tenant_seed", 1001L);
    service.bindRoleMenus(new RoleMenuBindCommand("tenant_seed", 1001L, List.of()));
  }

  /**
   * 验证平台应用会注册系统角色自定义数据权限接口、服务和 Mapper，供角色数据权限页面使用。
   */
  @Test
  void shouldRegisterSystemRoleDataScopeBeans() {
    assertNotNull(applicationContext.getBean(SysRoleDataScopeController.class));
    assertNotNull(applicationContext.getBean(SysRoleDataScopeService.class));
    assertNotNull(applicationContext.getBean(SysRoleDataScopeMapper.class));
  }

  /**
   * 验证平台应用中的角色自定义数据权限服务可以按租户查询空范围，并可执行空绑定。
   */
  @Test
  void shouldQueryAndBindEmptyRoleDataScopesByTenant() {
    SysRoleDataScopeService service = applicationContext.getBean(SysRoleDataScopeService.class);

    assertTrue(service.listRoleDataScopes("tenant_a", 1001L).isEmpty());
    ensureTenantRole("tenant_seed", 1001L);
    service.bindRoleDataScopes(new RoleDataScopeBindCommand("tenant_seed", 1001L, List.of()));
  }

  /**
   * 验证平台应用会注册系统管理员范围接口、服务和 Mapper，供租户管理员范围页面使用。
   */
  @Test
  void shouldRegisterSystemAdminScopeBeans() {
    assertNotNull(applicationContext.getBean(SysAdminScopeController.class));
    assertNotNull(applicationContext.getBean(SysAdminScopeService.class));
    assertNotNull(applicationContext.getBean(SysAdminScopeMapper.class));
  }

  /**
   * 验证平台应用中的管理员范围服务可以按租户查询空范围，并可执行空绑定。
   */
  @Test
  void shouldQueryAndBindEmptyAdminScopesByTenant() {
    SysAdminScopeService service = applicationContext.getBean(SysAdminScopeService.class);

    assertTrue(service.listAdminScopes("tenant_a", 1001L).isEmpty());
    ensureTenantUser("tenant_seed", 1001L);
    service.bindAdminScopes(new AdminScopeBindCommand("tenant_seed", 1001L, List.of()));
  }

  /**
   * 准备指定租户下的测试角色。
   *
   * @param tenantId 租户业务编码
   * @param roleId 角色主键
   */
  private void ensureTenantRole(String tenantId, Long roleId) {
    jdbcTemplate.update("""
        MERGE INTO sys_role (id, tenant_id, role_code, name, data_scope, status)
        KEY(id)
        VALUES (?, ?, ?, ?, 'SELF', 'enabled')
        """, roleId, tenantId, "role_" + roleId, "测试角色" + roleId);
  }

  /**
   * 准备指定租户下的测试用户。
   *
   * @param tenantId 租户业务编码
   * @param userId 用户主键
   */
  private void ensureTenantUser(String tenantId, Long userId) {
    jdbcTemplate.update("""
        MERGE INTO sys_user (id, tenant_id, username, nickname, password_hash, status)
        KEY(id)
        VALUES (?, ?, ?, ?, 'test-password-hash', 'enabled')
        """, userId, tenantId, "user_" + userId, "测试用户" + userId);
  }

  /**
   * 验证平台应用会注册系统审计日志接口、服务和 Mapper，供权限审计页面使用。
   */
  @Test
  void shouldRegisterSystemAuditLogBeans() {
    assertNotNull(applicationContext.getBean(SysAuditLogController.class));
    assertNotNull(applicationContext.getBean(SysAuditLogService.class));
    assertNotNull(applicationContext.getBean(SysAuditLogMapper.class));
  }

  /**
   * 验证平台应用中的审计日志服务可以按租户查询空审计日志列表。
   */
  @Test
  void shouldQueryEmptyAuditLogsByTenant() {
    SysAuditLogService service = applicationContext.getBean(SysAuditLogService.class);

    assertTrue(service.listRecent("tenant_a", 10).isEmpty());
  }

  /**
   * 验证平台应用会注册系统登录日志接口、服务和 Mapper，供登录审计页面使用。
   */
  @Test
  void shouldRegisterSystemLoginLogBeans() {
    assertNotNull(applicationContext.getBean(SysLoginLogController.class));
    assertNotNull(applicationContext.getBean(SysLoginLogService.class));
    assertNotNull(applicationContext.getBean(SysLoginLogMapper.class));
  }

  /**
   * 验证平台应用中的登录日志服务可以按租户查询空日志，并可以记录一次登录失败日志。
   */
  @Test
  void shouldQueryAndRecordEmptyLoginLogsByTenant() {
    SysLoginLogService service = applicationContext.getBean(SysLoginLogService.class);

    assertTrue(service.listRecent("tenant_a", 10).isEmpty());
    service.record(new SysLoginLogRecordCommand(
        "tenant_a", null, "missing", "password", "failure", "127.0.0.1", "test-agent"));
  }

  /**
   * 验证平台应用会注册系统异常日志接口、服务和 Mapper，供异常审计页面使用。
   */
  @Test
  void shouldRegisterSystemExceptionLogBeans() {
    assertNotNull(applicationContext.getBean(SysExceptionLogController.class));
    assertNotNull(applicationContext.getBean(SysExceptionLogService.class));
    assertNotNull(applicationContext.getBean(SysExceptionLogMapper.class));
  }

  /**
   * 验证平台应用中的异常日志服务可以按租户查询空日志，并可以记录一次接口异常日志。
   */
  @Test
  void shouldQueryAndRecordEmptyExceptionLogsByTenant() {
    SysExceptionLogService service = applicationContext.getBean(SysExceptionLogService.class);

    assertTrue(service.listRecent("tenant_a", 10).isEmpty());
    service.record(new SysExceptionLogRecordCommand("tenant_a", "trace-1", null, "missing",
        "/system/users", "POST", "java.lang.IllegalStateException", "failed", "stack", "127.0.0.1"));
  }

  /**
   * 验证平台应用会注册系统权限变更审计接口、服务和 Mapper，供权限审计页面使用。
   */
  @Test
  void shouldRegisterSystemPermissionAuditBeans() {
    assertNotNull(applicationContext.getBean(SysPermissionAuditController.class));
    assertNotNull(applicationContext.getBean(SysPermissionAuditService.class));
    assertNotNull(applicationContext.getBean(SysPermissionAuditMapper.class));
  }

  /**
   * 验证平台应用中的权限变更审计服务可以按租户查询空审计，并可以记录一次角色授权变更。
   */
  @Test
  void shouldQueryAndRecordEmptyPermissionAuditsByTenant() {
    SysPermissionAuditService service = applicationContext.getBean(SysPermissionAuditService.class);

    assertTrue(service.listRecent("tenant_a", 10).isEmpty());
    service.record(new SysPermissionAuditRecordCommand(
        "tenant_a", 1001L, "role", "10", "[]", "[1,2]", "BIND_MENU"));
  }

  /**
   * 验证平台应用会注册系统参数接口、服务和 Mapper，供基础配置页面使用。
   */
  @Test
  void shouldRegisterSystemParamBeans() {
    assertNotNull(applicationContext.getBean(SysParamController.class));
    assertNotNull(applicationContext.getBean(SysParamService.class));
    assertNotNull(applicationContext.getBean(SysParamMapper.class));
  }

  /**
   * 验证平台应用中的系统参数服务可以按租户查询空参数列表。
   */
  @Test
  void shouldQueryEmptyParamsByTenant() {
    SysParamService service = applicationContext.getBean(SysParamService.class);

    assertTrue(service.listParams("tenant_a").isEmpty());
  }

  /**
   * 验证平台应用会注册系统访问限制接口、服务和 Mapper，供安全审计访问限制页面使用。
   */
  @Test
  void shouldRegisterSystemAccessRestrictionBeans() {
    assertNotNull(applicationContext.getBean(SysAccessRestrictionController.class));
    assertNotNull(applicationContext.getBean(SysAccessRestrictionService.class));
    assertNotNull(applicationContext.getBean(SysAccessRestrictionMapper.class));
  }

  /**
   * 验证平台应用中的访问限制服务可以按租户和限制类型查询空生效规则。
   */
  @Test
  void shouldQueryEmptyAccessRestrictionsByTenantAndType() {
    SysAccessRestrictionService service = applicationContext.getBean(SysAccessRestrictionService.class);

    assertTrue(service.listActiveRestrictions("tenant_a", "ip", LocalDateTime.now()).isEmpty());
  }

  /**
   * 验证平台应用会注册系统密码策略接口、服务和 Mapper，供账号安全策略页面使用。
   */
  @Test
  void shouldRegisterSystemPasswordPolicyBeans() {
    assertNotNull(applicationContext.getBean(SysPasswordPolicyController.class));
    assertNotNull(applicationContext.getBean(SysPasswordPolicyService.class));
    assertNotNull(applicationContext.getBean(SysPasswordPolicyMapper.class));
  }

  /**
   * 验证平台应用中的密码策略服务可以按租户返回默认策略，避免缺少密码策略表初始化。
   */
  @Test
  void shouldQueryDefaultPasswordPolicyByTenant() {
    SysPasswordPolicyService service = applicationContext.getBean(SysPasswordPolicyService.class);

    assertNotNull(service.getPolicy("tenant_a"));
  }

  /**
   * 验证平台应用会注册系统编码规则接口、服务和 Mapper，供基础配置编码规则页面使用。
   */
  @Test
  void shouldRegisterSystemCodeRuleBeans() {
    assertNotNull(applicationContext.getBean(SysCodeRuleController.class));
    assertNotNull(applicationContext.getBean(SysCodeRuleService.class));
    assertNotNull(applicationContext.getBean(SysCodeRuleMapper.class));
  }

  /**
   * 验证平台应用中的编码规则服务可以按租户查询空规则列表。
   */
  @Test
  void shouldQueryEmptyCodeRulesByTenant() {
    SysCodeRuleService service = applicationContext.getBean(SysCodeRuleService.class);

    assertTrue(service.listRules("tenant_a").isEmpty());
  }

  /**
   * 验证平台应用会注册开放平台开发者应用接口、服务和 Mapper，供开发者门户使用。
   */
  @Test
  void shouldRegisterOpenApiAppBeans() {
    assertNotNull(applicationContext.getBean(OpenApiAppController.class));
    assertNotNull(applicationContext.getBean(OpenApiAppService.class));
    assertNotNull(applicationContext.getBean(OpenApiAppMapper.class));
  }

  /**
   * 验证平台应用中的开发者应用服务可以按租户查询空应用列表。
   */
  @Test
  void shouldQueryEmptyOpenApiAppsByTenant() {
    OpenApiAppService service = applicationContext.getBean(OpenApiAppService.class);

    assertTrue(service.listApps("tenant_a").isEmpty());
  }

  /**
   * 验证平台应用会注册开放平台 API Key 接口、服务和 Mapper，供开发者门户管理访问凭证。
   */
  @Test
  void shouldRegisterOpenApiApiKeyBeans() {
    assertNotNull(applicationContext.getBean(OpenApiApiKeyController.class));
    assertNotNull(applicationContext.getBean(OpenApiApiKeyService.class));
    assertNotNull(applicationContext.getBean(OpenApiApiKeyMapper.class));
  }

  /**
   * 验证平台应用中的 API Key 服务可以按租户和应用查询空凭证列表。
   */
  @Test
  void shouldQueryEmptyOpenApiApiKeysByTenantAndApp() {
    OpenApiApiKeyService service = applicationContext.getBean(OpenApiApiKeyService.class);

    assertTrue(service.listApiKeys("tenant_a", "purchase-app").isEmpty());
  }

  /**
   * 验证平台应用会注册开放 API 权限授权接口、服务和 Mapper，供开发者门户配置应用 API 范围。
   */
  @Test
  void shouldRegisterOpenApiPermissionBeans() {
    assertNotNull(applicationContext.getBean(OpenApiPermissionController.class));
    assertNotNull(applicationContext.getBean(OpenApiPermissionService.class));
    assertNotNull(applicationContext.getBean(OpenApiPermissionMapper.class));
  }

  /**
   * 验证平台应用中的开放 API 权限服务可以按租户和应用查询空授权列表。
   */
  @Test
  void shouldQueryEmptyOpenApiPermissionsByTenantAndApp() {
    OpenApiPermissionService service = applicationContext.getBean(OpenApiPermissionService.class);

    assertTrue(service.listPermissions("tenant_a", "purchase-app").isEmpty());
  }

  /**
   * 验证平台应用会注册开放 API 调用审计接口、服务和 Mapper，供开发者门户查询调用记录。
   */
  @Test
  void shouldRegisterOpenApiCallAuditBeans() {
    assertNotNull(applicationContext.getBean(OpenApiCallAuditController.class));
    assertNotNull(applicationContext.getBean(OpenApiCallAuditService.class));
    assertNotNull(applicationContext.getBean(OpenApiCallAuditMapper.class));
  }

  /**
   * 验证平台应用中的开放 API 调用审计服务可以按租户和应用查询空审计列表。
   */
  @Test
  void shouldQueryEmptyOpenApiCallAuditsByTenantAndApp() {
    OpenApiCallAuditService service = applicationContext.getBean(OpenApiCallAuditService.class);

    assertTrue(service.listAudits("tenant_a", "purchase-app").isEmpty());
  }

  /**
   * 验证平台应用会注册系统字典接口、服务和 Mapper，供基础配置和低代码字段选项使用。
   */
  @Test
  void shouldRegisterSystemDictBeans() {
    assertNotNull(applicationContext.getBean(SysDictController.class));
    assertNotNull(applicationContext.getBean(SysDictService.class));
    assertNotNull(applicationContext.getBean(SysDictMapper.class));
  }

  /**
   * 验证平台应用中的系统字典服务可以按租户查询空字典类型和字典项列表。
   */
  @Test
  void shouldQueryEmptyDictsByTenant() {
    SysDictService service = applicationContext.getBean(SysDictService.class);

    assertTrue(service.listTypes("tenant_a").isEmpty());
    assertTrue(service.listItems("tenant_a", "user_status").isEmpty());
  }

  /**
   * 验证平台应用会注册系统模块接口、服务和 Mapper，供模块管理和插件资源展示使用。
   */
  @Test
  void shouldRegisterSystemModuleBeans() {
    assertNotNull(applicationContext.getBean(SysModuleController.class));
    assertNotNull(applicationContext.getBean(SysModuleService.class));
    assertNotNull(applicationContext.getBean(SysModuleMapper.class));
  }

  /**
   * 验证平台应用启动后会把 classpath 模块描述同步到系统模块表。
   */
  @Test
  void shouldSynchronizeClasspathModulesIntoSystemModuleTables() {
    SysModuleService service = applicationContext.getBean(SysModuleService.class);

    List<String> moduleCodes = service.listModules().stream()
        .map(SysModuleResponse::getModuleCode)
        .toList();
    assertTrue(moduleCodes.containsAll(List.of("common", "system", "lowcode", "workflow", "openapi")));
    assertTrue(service.listModules().stream()
        .filter(module -> "workflow".equals(module.getModuleCode()))
        .findFirst()
        .orElseThrow()
        .getResources()
        .stream()
        .anyMatch(resource -> "workflow:task:approve".equals(resource.getResourceCode())));
  }

  /**
   * 验证平台应用 classpath 中存在首期模块描述文件，供微内核模块发现和插件元数据同步使用。
   */
  @Test
  void shouldDiscoverFirstReleaseModuleDescriptorsFromClasspath() {
    ModuleRegistry registry = new ModuleDescriptorClasspathLoader().loadRegistry();
    List<String> moduleCodes = registry.enabledModules().stream()
        .map(descriptor -> descriptor.getCode())
        .toList();

    assertTrue(moduleCodes.containsAll(List.of(
        "common", "system", "lowcode", "openapi", "workflow", "purchase",
        "message", "file", "job", "cms", "visual", "i18n", "search")));
    assertEquals(List.of("common", "system"), registry.findByCode("lowcode").orElseThrow().getDependencies());
    assertTrue(registry.findByCode("workflow").orElseThrow().getPermissions().contains("workflow:task:approve"));
    assertTrue(registry.findByCode("openapi").orElseThrow().getMenus().contains("openapi:app"));
  }

  /**
   * 验证平台应用会注册系统组织机构接口、服务和 Mapper，供组织架构页面使用。
   */
  @Test
  void shouldRegisterSystemOrgBeans() {
    assertNotNull(applicationContext.getBean(SysOrgController.class));
    assertNotNull(applicationContext.getBean(SysOrgService.class));
    assertNotNull(applicationContext.getBean(SysOrgMapper.class));
  }

  /**
   * 验证平台应用中的组织机构服务可以按租户查询空组织树。
   */
  @Test
  void shouldQueryEmptyOrgTreeByTenant() {
    SysOrgService service = applicationContext.getBean(SysOrgService.class);

    assertTrue(service.listOrgTree("tenant_a").isEmpty());
  }

  /**
   * 验证平台应用会注册系统岗位接口、服务和 Mapper，供岗位管理页面使用。
   */
  @Test
  void shouldRegisterSystemPostBeans() {
    assertNotNull(applicationContext.getBean(SysPostController.class));
    assertNotNull(applicationContext.getBean(SysPostService.class));
    assertNotNull(applicationContext.getBean(SysPostMapper.class));
  }

  /**
   * 验证平台应用中的岗位服务可以按租户查询空岗位列表。
   */
  @Test
  void shouldQueryEmptyPostsByTenant() {
    SysPostService service = applicationContext.getBean(SysPostService.class);

    assertTrue(service.listPosts("tenant_a", null).isEmpty());
  }

  /**
   * 验证平台应用会注册采购申请开放 API、状态服务和 Mapper，供开放 API 查询采购申请状态使用。
   */
  @Test
  void shouldRegisterPurchaseRequestOpenApiBeans() {
    assertNotNull(applicationContext.getBean(PurRequestOpenApiController.class));
    assertNotNull(applicationContext.getBean(PurRequestAdminController.class));
    assertNotNull(applicationContext.getBean(PurRequestStatusService.class));
    assertNotNull(applicationContext.getBean(PurRequestCommandService.class));
    assertNotNull(applicationContext.getBean(PurRequestMapper.class));
  }

  /**
   * 验证平台应用会注册采购订单开放 API、后台接口、服务和 Mapper，供开放 API 查询采购订单详情使用。
   */
  @Test
  void shouldRegisterPurchaseOrderOpenApiBeans() {
    assertNotNull(applicationContext.getBean(PurOrderOpenApiController.class));
    assertNotNull(applicationContext.getBean(PurOrderAdminController.class));
    assertNotNull(applicationContext.getBean(PurOrderCommandService.class));
    assertNotNull(applicationContext.getBean(PurOrderMapper.class));
  }

  /**
   * 验证平台应用会注册工作流门面，业务模块只能通过门面发起和处理流程。
   */
  @Test
  void shouldRegisterWorkflowFacadeBean() {
    assertNotNull(applicationContext.getBean(WorkflowService.class));
  }

  /**
   * 验证平台应用会注册工作流任务接口、服务和运行仓储，供后台管理端和 uni-app 处理待办任务。
   */
  @Test
  void shouldRegisterWorkflowTaskBeans() {
    assertNotNull(applicationContext.getBean(WorkflowTaskController.class));
    assertNotNull(applicationContext.getBean(WorkflowTaskService.class));
    assertNotNull(applicationContext.getBean(WorkflowRuntimeRepository.class));
  }

  /**
   * 验证平台应用中的采购申请状态服务会按租户查询，空库时返回明确业务异常。
   */
  @Test
  void shouldRejectMissingPurchaseRequestStatusByTenant() {
    PurRequestStatusService service = applicationContext.getBean(PurRequestStatusService.class);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> service.queryStatus("tenant_a", "PR404"));
    assertEquals("ZHYC_PUR_REQUEST_NOT_FOUND", exception.getCode());
    assertEquals("采购申请不存在", exception.getMessage());
  }

  /**
   * 验证平台应用会注册运行监控接口和服务，供监控中心查询服务与数据源状态。
   */
  @Test
  void shouldRegisterRuntimeMonitorBeans() {
    assertNotNull(applicationContext.getBean(RuntimeMonitorController.class));
    assertNotNull(applicationContext.getBean(RuntimeMonitorService.class));
  }

  /**
   * 验证平台应用会注册消息中心接口、服务和 Mapper，供首期站内信和模板管理使用。
   */
  @Test
  void shouldRegisterMessageCenterBeans() {
    assertNotNull(applicationContext.getBean(MsgTemplateController.class));
    assertNotNull(applicationContext.getBean(MsgTemplateService.class));
    assertNotNull(applicationContext.getBean(MsgTemplateMapper.class));
    assertNotNull(applicationContext.getBean(MsgMessageController.class));
    assertNotNull(applicationContext.getBean(MsgMessageService.class));
    assertNotNull(applicationContext.getBean(MsgMessageMapper.class));
  }

  /**
   * 验证平台应用会注册文件中心接口、服务和 Mapper，供首期文件管理使用。
   */
  @Test
  void shouldRegisterFileCenterBeans() {
    assertNotNull(applicationContext.getBean(FileStorageConfigController.class));
    assertNotNull(applicationContext.getBean(FileStorageConfigService.class));
    assertNotNull(applicationContext.getBean(FileStorageConfigMapper.class));
    assertNotNull(applicationContext.getBean(FileObjectController.class));
    assertNotNull(applicationContext.getBean(FileObjectService.class));
    assertNotNull(applicationContext.getBean(FileObjectMapper.class));
    assertNotNull(applicationContext.getBean(FilePreviewController.class));
    assertNotNull(applicationContext.getBean(FilePreviewService.class));
    assertNotNull(applicationContext.getBean(FilePreviewMapper.class));
  }

  /**
   * 验证平台应用会注册在线作业调度接口、服务和 Mapper，供首期任务配置和执行日志使用。
   */
  @Test
  void shouldRegisterJobCenterBeans() {
    assertNotNull(applicationContext.getBean(JobTaskController.class));
    assertNotNull(applicationContext.getBean(JobTaskService.class));
    assertNotNull(applicationContext.getBean(JobTaskMapper.class));
  }

  /**
   * 验证平台应用会注册内容管理接口、服务和 Mapper，供首期栏目和文章管理使用。
   */
  @Test
  void shouldRegisterCmsCenterBeans() {
    assertNotNull(applicationContext.getBean(CmsController.class));
    assertNotNull(applicationContext.getBean(CmsService.class));
    assertNotNull(applicationContext.getBean(CmsMapper.class));
  }

  /**
   * 验证平台应用会注册可视化报表接口、服务和 Mapper，供首期报表设计器和数据大屏使用。
   */
  @Test
  void shouldRegisterVisualCenterBeans() {
    assertNotNull(applicationContext.getBean(VisualController.class));
    assertNotNull(applicationContext.getBean(VisualService.class));
    assertNotNull(applicationContext.getBean(VisualMapper.class));
  }

  /**
   * 验证平台应用真实注册可视化公开访问和发布状态路由，避免发布链接进入前端后请求后端资源不存在。
   */
  @Test
  void shouldRegisterVisualPublicScreenRoutes() {
    RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
    Set<String> routePatterns = handlerMapping.getHandlerMethods().keySet().stream()
        .flatMap(mappingInfo -> mappingInfo.getPatternValues().stream())
        .collect(Collectors.toSet());

    assertTrue(routePatterns.contains("/visual/reports/{id}/status"));
    assertTrue(routePatterns.contains("/visual/public/reports/{tenantId}/{reportCode}"));
    assertTrue(routePatterns.contains("/visual/public/reports/{tenantId}/{reportCode}/datasets/{datasetCode}/preview"));
    assertTrue(routePatterns.contains("/visual/public/screens/{tenantId}/{screenCode}"));
    assertTrue(routePatterns.contains("/visual/public/screens/{tenantId}/{screenCode}/datasets/{datasetCode}/preview"));
  }

  /**
   * 验证平台应用会注册国际化词条接口、服务和 Mapper，供后台和生成模板多语言能力使用。
   */
  @Test
  void shouldRegisterI18nCenterBeans() {
    assertNotNull(applicationContext.getBean(I18nController.class));
    assertNotNull(applicationContext.getBean(I18nService.class));
    assertNotNull(applicationContext.getBean(I18nMapper.class));
  }

  /**
   * 验证平台应用会注册全文检索中心接口、服务和 Mapper。
   */
  @Test
  void shouldRegisterSearchCenterBeans() {
    assertNotNull(applicationContext.getBean(SearchController.class));
    assertNotNull(applicationContext.getBean(SearchService.class));
    assertNotNull(applicationContext.getBean(SearchMapper.class));
  }

  /**
   * 验证平台运行监控服务可以返回服务状态和主数据源状态。
   */
  @Test
  void shouldQueryRuntimeMonitorStatus() {
    RuntimeMonitorService service = applicationContext.getBean(RuntimeMonitorService.class);

    assertFalse(service.listServiceStatus().isEmpty());
    assertFalse(service.listDataSourceStatus().isEmpty());
  }
}
