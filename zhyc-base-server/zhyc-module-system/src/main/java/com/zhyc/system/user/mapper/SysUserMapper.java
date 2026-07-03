/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.mapper;

import com.zhyc.system.user.domain.SysUser;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统用户 MyBatis Mapper。
 */
@Mapper
public interface SysUserMapper {

    /**
     * 查询租户内系统用户列表。
     *
     * @param tenantId 租户业务编码
     * @return 系统用户列表
     */
    @SelectProvider(type = SysUserSqlProvider.class, method = "selectByTenantId")
    List<SysUser> selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按租户和登录账号查询系统用户凭证信息。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @return 系统用户，不存在时返回空
     */
    @SelectProvider(type = SysUserSqlProvider.class, method = "selectByTenantIdAndUsername")
    SysUser selectByTenantIdAndUsername(@Param("tenantId") String tenantId, @Param("username") String username);

    /**
     * 新增系统用户。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param nickname 用户显示名称
     * @param passwordHash 密码哈希
     * @param status 用户状态
     */
    @InsertProvider(type = SysUserSqlProvider.class, method = "insert")
    void insert(@Param("tenantId") String tenantId, @Param("username") String username,
                @Param("nickname") String nickname, @Param("passwordHash") String passwordHash,
                @Param("status") String status);

    /**
     * 更新系统用户基础信息。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param username 登录账号
     * @param nickname 用户显示名称
     * @param status 用户状态
     */
    @UpdateProvider(type = SysUserSqlProvider.class, method = "update")
    void update(@Param("tenantId") String tenantId, @Param("userId") Long userId,
                @Param("username") String username, @Param("nickname") String nickname,
                @Param("status") String status);

    /**
     * 更新系统用户状态。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param status 用户状态
     */
    @UpdateProvider(type = SysUserSqlProvider.class, method = "updateStatus")
    void updateStatus(@Param("tenantId") String tenantId, @Param("userId") Long userId,
                      @Param("status") String status);

    /**
     * 按租户和登录账号更新用户密码哈希。
     *
     * @param tenantId 租户业务编码
     * @param username 登录账号
     * @param passwordHash 新密码哈希值
     */
    @UpdateProvider(type = SysUserSqlProvider.class, method = "updatePasswordHash")
    void updatePasswordHash(@Param("tenantId") String tenantId, @Param("username") String username,
                            @Param("passwordHash") String passwordHash);

    /**
     * 按主键更新系统用户密码哈希。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param passwordHash 新密码哈希值
     */
    @UpdateProvider(type = SysUserSqlProvider.class, method = "updatePasswordHashById")
    void updatePasswordHashById(@Param("tenantId") String tenantId, @Param("userId") Long userId,
                                @Param("passwordHash") String passwordHash);

    /**
     * 删除用户岗位、角色和管理员范围关联。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     */
    @DeleteProvider(type = SysUserSqlProvider.class, method = "deleteUserPostsByTenantIdAndUserId")
    void deleteUserPostsByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    /**
     * 删除用户角色关联。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     */
    @DeleteProvider(type = SysUserSqlProvider.class, method = "deleteUserRolesByTenantIdAndUserId")
    void deleteUserRolesByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    /**
     * 删除管理员范围关联。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     */
    @DeleteProvider(type = SysUserSqlProvider.class, method = "deleteAdminScopesByTenantIdAndUserId")
    void deleteAdminScopesByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    /**
     * 删除租户内系统用户。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     */
    @DeleteProvider(type = SysUserSqlProvider.class, method = "deleteByTenantIdAndId")
    void deleteByTenantIdAndId(@Param("tenantId") String tenantId, @Param("userId") Long userId);
}
