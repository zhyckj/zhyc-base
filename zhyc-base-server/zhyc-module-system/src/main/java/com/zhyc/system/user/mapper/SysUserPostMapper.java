/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.mapper;

import com.zhyc.system.user.domain.SysUserPost;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 系统用户岗位 MyBatis Mapper。
 */
@Mapper
public interface SysUserPostMapper {

    /**
     * 查询租户内指定用户的岗位绑定。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @return 用户岗位绑定列表
     */
    @SelectProvider(type = SysUserPostSqlProvider.class, method = "selectByTenantIdAndUserId")
    List<SysUserPost> selectByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    /**
     * 删除租户内指定用户的岗位绑定。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     */
    @DeleteProvider(type = SysUserPostSqlProvider.class, method = "deleteByTenantIdAndUserId")
    void deleteByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    /**
     * 新增租户内用户岗位绑定。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param postId 岗位主键
     * @param primaryFlag 是否主岗位
     */
    @InsertProvider(type = SysUserPostSqlProvider.class, method = "insertUserPost")
    void insertUserPost(@Param("tenantId") String tenantId, @Param("userId") Long userId,
                        @Param("postId") Long postId, @Param("primaryFlag") boolean primaryFlag);
}
