/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.post.mapper;

import com.zhyc.system.post.domain.SysPost;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统岗位 MyBatis Mapper。
 */
@Mapper
public interface SysPostMapper {

    /**
     * 查询租户内岗位列表。
     *
     * @param tenantId 租户业务编码
     * @param orgId 所属组织主键，为空时查询租户内全部岗位
     * @return 岗位列表
     */
    @SelectProvider(type = SysPostSqlProvider.class, method = "selectByTenantIdAndOrgId")
    List<SysPost> selectByTenantIdAndOrgId(@Param("tenantId") String tenantId, @Param("orgId") Long orgId);

    @InsertProvider(type = SysPostSqlProvider.class, method = "insert")
    void insert(SysPost post);

    @UpdateProvider(type = SysPostSqlProvider.class, method = "update")
    void update(SysPost post);

    @UpdateProvider(type = SysPostSqlProvider.class, method = "updateStatus")
    void updateStatus(@Param("tenantId") String tenantId, @Param("postId") Long postId,
                      @Param("status") String status);

    @DeleteProvider(type = SysPostSqlProvider.class, method = "deleteUserPostsByTenantAndPost")
    void deleteUserPostsByTenantAndPost(@Param("tenantId") String tenantId, @Param("postId") Long postId);

    @DeleteProvider(type = SysPostSqlProvider.class, method = "deleteByTenantIdAndId")
    void deleteByTenantIdAndId(@Param("tenantId") String tenantId, @Param("postId") Long postId);
}
