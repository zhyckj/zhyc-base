/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.module;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块元数据描述。
 *
 * <p>用于声明模块的基础信息、依赖关系和可注册资源，不包含模块安装或加载逻辑。</p>
 */
public class ModuleDescriptor {

    /** 模块唯一编码，用于依赖引用和模块识别。 */
    private String code;
    /** 模块显示名称。 */
    private String name;
    /** 模块版本号。 */
    private String version;
    /** 模块类型，例如 PLATFORM、BUSINESS、MICROSERVICE。 */
    private String moduleType;
    /** 独立部署时的服务名称。 */
    private String serviceName;
    /** 租户隔离模式，例如 tenant_id、schema、database。 */
    private String tenantMode;
    /** 模块权限编码前缀。 */
    private String permissionPrefix;
    /** 模块后端基础包名。 */
    private String backendPackage;
    /** 是否必须通过开放 API 网关访问。 */
    private boolean openApiGatewayRequired;
    /** 是否依赖统一认证中心。 */
    private boolean authServerRequired;
    /** 当前模块依赖的其他模块编码列表，默认空列表。 */
    private List<String> dependencies = new ArrayList<>();
    /** 模块提供的菜单标识列表，默认空列表。 */
    private List<String> menus = new ArrayList<>();
    /** 模块提供的权限标识列表，默认空列表。 */
    private List<String> permissions = new ArrayList<>();
    /** 模块提供的数据字典标识列表，默认空列表。 */
    private List<String> dictionaries = new ArrayList<>();
    /** 模块关联的数据库脚本路径或标识列表，默认空列表。 */
    private List<String> dbScripts = new ArrayList<>();
    /** 模块关联的代码生成模板路径或标识列表，默认空列表。 */
    private List<String> generatorTemplates = new ArrayList<>();
    /** 模块扩展点编码列表，默认空列表。 */
    private List<String> extensionPoints = new ArrayList<>();
    /** 模块是否启用。 */
    private boolean enabled;

    /**
     * 创建空模块描述，供序列化框架或手动赋值场景使用。
     */
    public ModuleDescriptor() {
    }

    /**
     * 创建完整模块描述。
     *
     * @param code 模块唯一编码
     * @param name 模块显示名称
     * @param version 模块版本号
     * @param dependencies 依赖模块编码列表；传入 {@code null} 时按空列表处理
     * @param menus 菜单标识列表；传入 {@code null} 时按空列表处理
     * @param permissions 权限标识列表；传入 {@code null} 时按空列表处理
     * @param dictionaries 数据字典标识列表；传入 {@code null} 时按空列表处理
     * @param dbScripts 数据库脚本路径或标识列表；传入 {@code null} 时按空列表处理
     * @param generatorTemplates 代码生成模板路径或标识列表；传入 {@code null} 时按空列表处理
     * @param enabled 模块是否启用
     */
    public ModuleDescriptor(String code, String name, String version, List<String> dependencies,
                            List<String> menus, List<String> permissions, List<String> dictionaries,
                            List<String> dbScripts, List<String> generatorTemplates, boolean enabled) {
        this.code = code;
        this.name = name;
        this.version = version;
        this.dependencies = copyList(dependencies);
        this.menus = copyList(menus);
        this.permissions = copyList(permissions);
        this.dictionaries = copyList(dictionaries);
        this.dbScripts = copyList(dbScripts);
        this.generatorTemplates = copyList(generatorTemplates);
        this.enabled = enabled;
    }

    /**
     * 返回模块唯一编码。
     *
     * @return 模块编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置模块唯一编码。
     *
     * @param code 模块编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 返回模块显示名称。
     *
     * @return 模块名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置模块显示名称。
     *
     * @param name 模块名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回模块版本号。
     *
     * @return 模块版本号
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置模块版本号。
     *
     * @param version 模块版本号
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 返回模块类型。
     *
     * @return 模块类型
     */
    public String getModuleType() {
        return moduleType;
    }

    /**
     * 设置模块类型。
     *
     * @param moduleType 模块类型
     */
    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    /**
     * 返回独立部署服务名称。
     *
     * @return 服务名称
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 设置独立部署服务名称。
     *
     * @param serviceName 服务名称
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * 返回租户隔离模式。
     *
     * @return 租户隔离模式
     */
    public String getTenantMode() {
        return tenantMode;
    }

    /**
     * 设置租户隔离模式。
     *
     * @param tenantMode 租户隔离模式
     */
    public void setTenantMode(String tenantMode) {
        this.tenantMode = tenantMode;
    }

    /**
     * 返回模块权限编码前缀。
     *
     * @return 权限编码前缀
     */
    public String getPermissionPrefix() {
        return permissionPrefix;
    }

    /**
     * 设置模块权限编码前缀。
     *
     * @param permissionPrefix 权限编码前缀
     */
    public void setPermissionPrefix(String permissionPrefix) {
        this.permissionPrefix = permissionPrefix;
    }

    /**
     * 返回模块后端基础包名。
     *
     * @return 后端基础包名
     */
    public String getBackendPackage() {
        return backendPackage;
    }

    /**
     * 设置模块后端基础包名。
     *
     * @param backendPackage 后端基础包名
     */
    public void setBackendPackage(String backendPackage) {
        this.backendPackage = backendPackage;
    }

    /**
     * 返回是否必须经过开放 API 网关。
     *
     * @return 必须经过开放 API 网关时返回 {@code true}
     */
    public boolean isOpenApiGatewayRequired() {
        return openApiGatewayRequired;
    }

    /**
     * 设置是否必须经过开放 API 网关。
     *
     * @param openApiGatewayRequired 开放 API 网关依赖标记
     */
    public void setOpenApiGatewayRequired(boolean openApiGatewayRequired) {
        this.openApiGatewayRequired = openApiGatewayRequired;
    }

    /**
     * 返回是否依赖统一认证中心。
     *
     * @return 依赖统一认证中心时返回 {@code true}
     */
    public boolean isAuthServerRequired() {
        return authServerRequired;
    }

    /**
     * 设置是否依赖统一认证中心。
     *
     * @param authServerRequired 统一认证中心依赖标记
     */
    public void setAuthServerRequired(boolean authServerRequired) {
        this.authServerRequired = authServerRequired;
    }

    /**
     * 返回依赖模块编码列表。
     *
     * @return 依赖模块编码列表的副本
     */
    public List<String> getDependencies() {
        return copyList(dependencies);
    }

    /**
     * 设置依赖模块编码列表。
     *
     * @param dependencies 依赖模块编码列表；传入 {@code null} 时按空列表处理
     */
    public void setDependencies(List<String> dependencies) {
        this.dependencies = copyList(dependencies);
    }

    /**
     * 返回模块提供的菜单标识列表。
     *
     * @return 菜单标识列表的副本
     */
    public List<String> getMenus() {
        return copyList(menus);
    }

    /**
     * 设置模块提供的菜单标识列表。
     *
     * @param menus 菜单标识列表；传入 {@code null} 时按空列表处理
     */
    public void setMenus(List<String> menus) {
        this.menus = copyList(menus);
    }

    /**
     * 返回模块提供的权限标识列表。
     *
     * @return 权限标识列表的副本
     */
    public List<String> getPermissions() {
        return copyList(permissions);
    }

    /**
     * 设置模块提供的权限标识列表。
     *
     * @param permissions 权限标识列表；传入 {@code null} 时按空列表处理
     */
    public void setPermissions(List<String> permissions) {
        this.permissions = copyList(permissions);
    }

    /**
     * 返回模块提供的数据字典标识列表。
     *
     * @return 数据字典标识列表的副本
     */
    public List<String> getDictionaries() {
        return copyList(dictionaries);
    }

    /**
     * 设置模块提供的数据字典标识列表。
     *
     * @param dictionaries 数据字典标识列表；传入 {@code null} 时按空列表处理
     */
    public void setDictionaries(List<String> dictionaries) {
        this.dictionaries = copyList(dictionaries);
    }

    /**
     * 返回模块关联的数据库脚本路径或标识列表。
     *
     * @return 数据库脚本路径或标识列表的副本
     */
    public List<String> getDbScripts() {
        return copyList(dbScripts);
    }

    /**
     * 设置模块关联的数据库脚本路径或标识列表。
     *
     * @param dbScripts 数据库脚本路径或标识列表；传入 {@code null} 时按空列表处理
     */
    public void setDbScripts(List<String> dbScripts) {
        this.dbScripts = copyList(dbScripts);
    }

    /**
     * 返回模块关联的代码生成模板路径或标识列表。
     *
     * @return 代码生成模板路径或标识列表的副本
     */
    public List<String> getGeneratorTemplates() {
        return copyList(generatorTemplates);
    }

    /**
     * 设置模块关联的代码生成模板路径或标识列表。
     *
     * @param generatorTemplates 代码生成模板路径或标识列表；传入 {@code null} 时按空列表处理
     */
    public void setGeneratorTemplates(List<String> generatorTemplates) {
        this.generatorTemplates = copyList(generatorTemplates);
    }

    /**
     * 返回模块扩展点编码列表。
     *
     * @return 扩展点编码列表的副本
     */
    public List<String> getExtensionPoints() {
        return copyList(extensionPoints);
    }

    /**
     * 设置模块扩展点编码列表。
     *
     * @param extensionPoints 扩展点编码列表；传入 {@code null} 时按空列表处理
     */
    public void setExtensionPoints(List<String> extensionPoints) {
        this.extensionPoints = copyList(extensionPoints);
    }

    /**
     * 返回模块是否启用。
     *
     * @return 启用返回 {@code true}，停用返回 {@code false}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置模块是否启用。
     *
     * @param enabled 启用标记
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private static List<String> copyList(List<String> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(source);
    }
}
