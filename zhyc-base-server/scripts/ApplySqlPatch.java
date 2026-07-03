/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地 SQL 补丁执行工具。
 *
 * <p>仅用于开发机按环境文件执行幂等 SQL 补丁，输出不包含数据库密码。</p>
 */
public class ApplySqlPatch {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("用法：ApplySqlPatch <env-file> <sql-file>");
        }
        Map<String, String> env = loadEnv(Path.of(args[0]));
        String url = require(env, "ZHYC_PLATFORM_DATASOURCE_URL");
        String username = require(env, "ZHYC_PLATFORM_DATASOURCE_USERNAME");
        String password = require(env, "ZHYC_PLATFORM_DATASOURCE_PASSWORD");
        List<String> statements = splitSql(Files.readString(Path.of(args[1]), StandardCharsets.UTF_8));

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            connection.setAutoCommit(false);
            int executed = executeStatements(connection, statements);
            connection.commit();
            System.out.println("SQL 补丁执行完成，语句数：" + executed);
            printVerification(connection);
        }
    }

    private static int executeStatements(Connection connection, List<String> statements) throws SQLException {
        int executed = 0;
        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                if (sql.isBlank()) {
                    continue;
                }
                statement.execute(sql);
                executed++;
            }
        }
        return executed;
    }

    private static void printVerification(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            printCount(statement, "AI 菜单数量", """
                SELECT COUNT(*) FROM sys_menu
                WHERE tenant_id = 'zhyc-platform'
                  AND (menu_code = 'ai' OR permission LIKE 'ai:%')
                """);
            printCount(statement, "AI 角色授权数量", """
                SELECT COUNT(*) FROM sys_role_menu rm
                JOIN sys_menu m ON rm.tenant_id = m.tenant_id AND rm.menu_id = m.id
                WHERE rm.tenant_id = 'zhyc-platform'
                  AND rm.role_id = 1
                  AND (m.menu_code = 'ai' OR m.permission LIKE 'ai:%')
                """);
            printCount(statement, "AI 套餐授权数量", """
                SELECT COUNT(*) FROM sys_tenant_package_module
                WHERE package_id = 1
                  AND module_code = 'ai'
                """);
            printCount(statement, "AI 模块登记数量", "SELECT COUNT(*) FROM sys_module WHERE module_code = 'ai'");
            printCount(statement, "AI 业务表数量", """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name IN ('ai_provider', 'ai_model_config', 'ai_app', 'ai_prompt_template', 'ai_invocation_audit')
                """);
        }
    }

    private static void printCount(Statement statement, String label, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            System.out.println(label + "：" + resultSet.getInt(1));
        }
    }

    private static Map<String, String> loadEnv(Path envPath) throws IOException {
        Map<String, String> env = new LinkedHashMap<>();
        for (String line : Files.readAllLines(envPath, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int separatorIndex = trimmed.indexOf('=');
            if (separatorIndex <= 0) {
                continue;
            }
            env.put(trimmed.substring(0, separatorIndex).trim(), trimmed.substring(separatorIndex + 1).trim());
        }
        return env;
    }

    private static String require(Map<String, String> env, String key) {
        String value = env.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("环境变量缺失：" + key);
        }
        return value;
    }

    private static List<String> splitSql(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inLineComment = false;
        for (int i = 0; i < sql.length(); i++) {
            char currentChar = sql.charAt(i);
            char nextChar = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';
            if (!inSingleQuote && currentChar == '-' && nextChar == '-') {
                inLineComment = true;
            }
            if (inLineComment && (currentChar == '\n' || currentChar == '\r')) {
                inLineComment = false;
                continue;
            }
            if (inLineComment) {
                continue;
            }
            if (currentChar == '\'' && nextChar == '\'') {
                current.append(currentChar).append(nextChar);
                i++;
                continue;
            }
            if (currentChar == '\'') {
                inSingleQuote = !inSingleQuote;
            }
            if (currentChar == ';' && !inSingleQuote) {
                statements.add(current.toString().trim());
                current.setLength(0);
                continue;
            }
            current.append(currentChar);
        }
        if (!current.toString().isBlank()) {
            statements.add(current.toString().trim());
        }
        return statements;
    }
}
