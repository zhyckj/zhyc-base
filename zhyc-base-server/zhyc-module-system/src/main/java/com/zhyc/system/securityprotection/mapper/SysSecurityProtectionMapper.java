/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.mapper;

import com.zhyc.system.securityprotection.domain.SysSecurityEvent;
import com.zhyc.system.securityprotection.domain.SysSecurityIpBlock;
import com.zhyc.system.securityprotection.domain.SysSecurityPolicy;
import com.zhyc.system.securityprotection.repository.SysSecurityProtectionRepository.SecurityRankRow;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * 系统安全防护中心 MyBatis Mapper。
 */
@Mapper
public interface SysSecurityProtectionMapper {

    /**
     * 统计今日请求来源 IP 数量。
     *
     * @param tenantId 租户业务编码
     * @param dayStart 统计开始时间
     * @param dayEnd 统计结束时间
     * @return 来源 IP 数量
     */
    @SelectProvider(type = SysSecurityProtectionSqlProvider.class, method = "countDistinctSourceIp")
    long countDistinctSourceIp(@Param("tenantId") String tenantId,
                               @Param("dayStart") LocalDateTime dayStart,
                               @Param("dayEnd") LocalDateTime dayEnd);

    /**
     * 统计今日单 IP 最高请求次数。
     *
     * @param tenantId 租户业务编码
     * @param dayStart 统计开始时间
     * @param dayEnd 统计结束时间
     * @return 单 IP 最高请求次数
     */
    @SelectProvider(type = SysSecurityProtectionSqlProvider.class, method = "maxIpRequestCount")
    long maxIpRequestCount(@Param("tenantId") String tenantId,
                           @Param("dayStart") LocalDateTime dayStart,
                           @Param("dayEnd") LocalDateTime dayEnd);

    /**
     * 统计今日存在违规处置的 IP 数量。
     *
     * @param tenantId 租户业务编码
     * @param dayStart 统计开始时间
     * @param dayEnd 统计结束时间
     * @return 违规 IP 数量
     */
    @SelectProvider(type = SysSecurityProtectionSqlProvider.class, method = "countViolationIp")
    long countViolationIp(@Param("tenantId") String tenantId,
                          @Param("dayStart") LocalDateTime dayStart,
                          @Param("dayEnd") LocalDateTime dayEnd);

    /**
     * 统计当前有效封禁 IP 数量。
     *
     * @param tenantId 租户业务编码
     * @param now 当前时间
     * @return 有效封禁数量
     */
    @SelectProvider(type = SysSecurityProtectionSqlProvider.class, method = "countActiveIpBlock")
    long countActiveIpBlock(@Param("tenantId") String tenantId, @Param("now") LocalDateTime now);

    /**
     * 查询租户安全防护策略。
     *
     * @param tenantId 租户业务编码
     * @return 安全防护策略列表
     */
    @SelectProvider(type = SysSecurityProtectionSqlProvider.class, method = "selectPolicies")
    List<SysSecurityPolicy> selectPolicies(@Param("tenantId") String tenantId);

    /**
     * 保存或更新安全防护策略。
     *
     * @param policy 安全防护策略
     */
    @InsertProvider(type = SysSecurityProtectionSqlProvider.class, method = "savePolicy")
    void savePolicy(SysSecurityPolicy policy);

    /**
     * 写入安全事件。
     *
     * @param event 安全事件
     */
    @InsertProvider(type = SysSecurityProtectionSqlProvider.class, method = "insertEvent")
    void insertEvent(SysSecurityEvent event);

    /**
     * 查询最近安全事件。
     *
     * @param tenantId 租户业务编码
     * @param limit 返回数量
     * @return 最近安全事件列表
     */
    @SelectProvider(type = SysSecurityProtectionSqlProvider.class, method = "selectRecentEvents")
    List<SysSecurityEvent> selectRecentEvents(@Param("tenantId") String tenantId, @Param("limit") int limit);

    /**
     * 查询来源 IP 请求排行。
     *
     * @param tenantId 租户业务编码
     * @param dayStart 统计开始时间
     * @param dayEnd 统计结束时间
     * @param limit 返回数量
     * @return 来源 IP 排行
     */
    @SelectProvider(type = SysSecurityProtectionSqlProvider.class, method = "selectTopSourceIps")
    List<SecurityRankRow> selectTopSourceIps(@Param("tenantId") String tenantId,
                                             @Param("dayStart") LocalDateTime dayStart,
                                             @Param("dayEnd") LocalDateTime dayEnd,
                                             @Param("limit") int limit);

    /**
     * 查询接口访问排行。
     *
     * @param tenantId 租户业务编码
     * @param dayStart 统计开始时间
     * @param dayEnd 统计结束时间
     * @param limit 返回数量
     * @return 接口访问排行
     */
    @SelectProvider(type = SysSecurityProtectionSqlProvider.class, method = "selectTopRequestPaths")
    List<SecurityRankRow> selectTopRequestPaths(@Param("tenantId") String tenantId,
                                                @Param("dayStart") LocalDateTime dayStart,
                                                @Param("dayEnd") LocalDateTime dayEnd,
                                                @Param("limit") int limit);

    /**
     * 保存或更新 IP 封禁。
     *
     * @param block IP 封禁
     */
    @InsertProvider(type = SysSecurityProtectionSqlProvider.class, method = "saveIpBlock")
    void saveIpBlock(SysSecurityIpBlock block);

    /**
     * 同步拒绝访问限制。
     *
     * @param block IP 封禁
     */
    @InsertProvider(type = SysSecurityProtectionSqlProvider.class, method = "syncDenyAccessRestriction")
    void syncDenyAccessRestriction(SysSecurityIpBlock block);

    /**
     * 解除 IP 封禁。
     *
     * @param tenantId 租户业务编码
     * @param ipValue IP 或 CIDR
     */
    @UpdateProvider(type = SysSecurityProtectionSqlProvider.class, method = "deactivateIpBlock")
    void deactivateIpBlock(@Param("tenantId") String tenantId, @Param("ipValue") String ipValue);

    /**
     * 解除同步的拒绝访问限制。
     *
     * @param tenantId 租户业务编码
     * @param ipValue IP 或 CIDR
     */
    @UpdateProvider(type = SysSecurityProtectionSqlProvider.class, method = "deactivateDenyAccessRestriction")
    void deactivateDenyAccessRestriction(@Param("tenantId") String tenantId, @Param("ipValue") String ipValue);

    /**
     * 查询当前有效的 IP 封禁规则。
     *
     * @param tenantId 租户业务编码
     * @param now 当前时间
     * @return IP 或 CIDR 规则列表
     */
    @SelectProvider(type = SysSecurityProtectionSqlProvider.class, method = "selectActiveIpBlockRules")
    List<String> selectActiveIpBlockRules(@Param("tenantId") String tenantId, @Param("now") LocalDateTime now);

    /**
     * 统计指定 IP 是否存在有效精确封禁。
     *
     * @param tenantId 租户业务编码
     * @param ipValue IP 或 CIDR
     * @param now 当前时间
     * @return 有效精确封禁数量
     */
    @SelectProvider(type = SysSecurityProtectionSqlProvider.class, method = "countActiveIpBlockByValue")
    long countActiveIpBlockByValue(@Param("tenantId") String tenantId,
                                   @Param("ipValue") String ipValue,
                                   @Param("now") LocalDateTime now);
}
