/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.platform.security;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;

import java.io.PrintStream;
import java.util.Map;

/**
 * 平台本地密码哈希生成命令。
 *
 * <p>该命令仅用于本地初始化管理员账号时生成 Shiro 可校验的密码哈希；明文密码必须通过本机环境变量传入，
 * 命令只输出哈希值，禁止把明文密码写入 SQL、脚本或日志。</p>
 */
public final class PlatformPasswordHashCli {

    /** 本地管理员明文密码环境变量名。 */
    static final String PASSWORD_ENV = "ZHYC_LOCAL_ADMIN_PASSWORD";
    /** 本地初始化密码最小长度。 */
    private static final int MIN_PASSWORD_LENGTH = 10;

    private PlatformPasswordHashCli() {
    }

    /**
     * 执行密码哈希生成命令。
     *
     * @param args 命令行参数，当前支持 {@code --help}
     */
    public static void main(String[] args) {
        int exitCode = run(args, System.getenv(), System.out, System.err, new DefaultPasswordService());
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /**
     * 执行命令主体，便于单元测试覆盖。
     *
     * @param args 命令行参数
     * @param env 环境变量映射
     * @param out 标准输出
     * @param err 错误输出
     * @param passwordService Shiro 密码服务
     * @return 退出码，0 表示成功
     */
    static int run(String[] args, Map<String, String> env, PrintStream out, PrintStream err,
                   PasswordService passwordService) {
        if (containsHelp(args)) {
            printUsage(out);
            return 0;
        }
        String password = trimToNull(env.get(PASSWORD_ENV));
        if (password == null) {
            err.printf("缺少本地密码环境变量：%s%n", PASSWORD_ENV);
            printUsage(err);
            return 2;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            err.printf("本地初始化密码长度不能小于 %d 位%n", MIN_PASSWORD_LENGTH);
            return 3;
        }
        out.println(passwordService.encryptPassword(password));
        return 0;
    }

    /**
     * 输出命令使用说明。
     *
     * @param out 输出流
     */
    private static void printUsage(PrintStream out) {
        out.printf("使用方式：设置 %s 后运行 Maven exec 生成 Shiro 密码哈希。%n", PASSWORD_ENV);
        out.println("示例：rtk mvn -pl zhyc-platform-app -Dexec.mainClass=com.zhyc.platform.security.PlatformPasswordHashCli exec:java");
    }

    /**
     * 判断命令行参数是否包含帮助标记。
     *
     * @param args 命令行参数
     * @return 包含帮助标记时返回 true
     */
    private static boolean containsHelp(String[] args) {
        for (String arg : args) {
            if ("--help".equals(arg) || "-h".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 规范化文本，空白文本返回空。
     *
     * @param value 原始文本
     * @return 去除首尾空白后的文本，空白时返回空
     */
    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
