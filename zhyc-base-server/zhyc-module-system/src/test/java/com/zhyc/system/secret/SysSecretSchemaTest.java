/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.secret;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 系统密钥表结构与种子数据测试。
 */
class SysSecretSchemaTest {

    /**
     * 验证密钥主表符合租户级密钥中心的首期建模要求。
     *
     * @throws IOException 读取建表脚本失败时抛出
     */
    @Test
    void shouldDeclareSysSecretTableInSchema() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V1__system_core.sql"), StandardCharsets.UTF_8)
                .toLowerCase();

        assertTrue(sql.contains("create table if not exists sys_secret"),
                "should create sys_secret table");
        assertTrue(sql.contains("tenant_id varchar(64) not null comment '租户业务编码'"),
                "sys_secret should include tenant_id");
        assertTrue(sql.contains("secret_code varchar(64) not null comment '密钥编码'"),
                "sys_secret should include secret_code");
        assertTrue(sql.contains("secret_name varchar(128) not null comment '密钥名称'"),
                "sys_secret should include secret_name");
        assertTrue(sql.contains("secret_kind varchar(64) not null comment '密钥类型'"),
                "sys_secret should include secret_kind");
        assertTrue(sql.contains("secret_cipher longtext not null comment '密钥密文'"),
                "sys_secret should include secret_cipher");
        assertTrue(sql.contains("secret_mask varchar(255) not null comment '脱敏展示值'"),
                "sys_secret should include secret_mask");
        assertTrue(sql.contains("status varchar(32) not null default 'enabled' comment '密钥状态'"),
                "sys_secret should include status");
        assertTrue(sql.contains("expire_at datetime default null comment '过期时间'"),
                "sys_secret should include expire_at");
        assertTrue(sql.contains("last_rotated_at datetime default null comment '最近轮换时间'"),
                "sys_secret should include last_rotated_at");
        assertTrue(sql.contains("created_by bigint default null comment '创建人主键'"),
                "sys_secret should include created_by");
        assertTrue(sql.contains("updated_by bigint default null comment '更新人主键'"),
                "sys_secret should include updated_by");
        assertTrue(sql.contains("deleted tinyint(1) not null default 0 comment '是否删除'"),
                "sys_secret should include deleted");
        assertTrue(sql.contains("version int not null default 0 comment '乐观锁版本号'"),
                "sys_secret should include version");
        assertTrue(sql.contains("remark varchar(500) default null comment '备注'"),
                "sys_secret should include remark");
        assertTrue(sql.contains("unique key uk_sys_secret_tenant_code (tenant_id, secret_code)"),
                "sys_secret should include unique tenant secret code index");
        assertTrue(sql.contains("key idx_sys_secret_tenant_status (tenant_id, status)"),
                "sys_secret should include tenant status index");
        assertTrue(sql.contains("key idx_sys_secret_tenant_kind (tenant_id, secret_kind)"),
                "sys_secret should include tenant kind index");
        assertTrue(sql.contains("key idx_sys_secret_tenant_expire (tenant_id, expire_at)"),
                "sys_secret should include tenant expire index");
        assertTrue(sql.contains("comment='系统密钥表'"),
                "sys_secret should include chinese table comment");
    }

    /**
     * 验证系统菜单种子已加入密钥管理入口和按钮权限。
     *
     * @throws IOException 读取种子脚本失败时抛出
     */
    @Test
    void shouldDeclareSysSecretMenuAndPermissionsInSeed() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/V2__system_seed.sql"), StandardCharsets.UTF_8)
                .toLowerCase();

        assertTrue(sql.contains("'system-secret', '密钥管理', 'menu', '/system/secrets', 'system/secret/index', 'system:secret:query'"),
                "seed should create system secret menu");
        assertTrue(sql.contains("'system-secret-query', '密钥查看', 'button', null, null, 'system:secret:query'"),
                "seed should create system secret query button");
        assertTrue(sql.contains("'system-secret-create', '密钥新增', 'button', null, null, 'system:secret:create'"),
                "seed should create system secret create button");
        assertTrue(sql.contains("'system-secret-update', '密钥编辑', 'button', null, null, 'system:secret:update'"),
                "seed should create system secret update button");
        assertTrue(sql.contains("'system-secret-delete', '密钥删除', 'button', null, null, 'system:secret:delete'"),
                "seed should create system secret delete button");
        assertTrue(sql.contains("'system-secret-enable', '密钥启用', 'button', null, null, 'system:secret:enable'"),
                "seed should create system secret enable button");
        assertTrue(sql.contains("'system-secret-disable', '密钥禁用', 'button', null, null, 'system:secret:disable'"),
                "seed should create system secret disable button");
        assertTrue(sql.contains("'system-secret-rotate', '密钥轮换', 'button', null, null, 'system:secret:rotate'"),
                "seed should create system secret rotate button");
        assertTrue(sql.contains("'system-secret-copy-ref', '复制引用', 'button', null, null, 'system:secret:copy-ref'"),
                "seed should create system secret copy-ref button");
    }
}
