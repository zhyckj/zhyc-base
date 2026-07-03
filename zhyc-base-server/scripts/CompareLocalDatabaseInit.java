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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地数据库与初始化脚本对比工具。
 *
 * <p>只读取结构和基础数据数量，不输出数据库密码、密钥密文、Token 或 AI 供应商密钥引用值。</p>
 */
public class CompareLocalDatabaseInit {

    private static final Set<String> SENSITIVE_COLUMNS = Set.of(
        "password_hash",
        "client_secret",
        "secret_cipher",
        "secret_ref"
    );

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("用法：CompareLocalDatabaseInit <env-file> <init-sql-file>");
        }
        Map<String, String> env = loadEnv(Path.of(args[0]));
        String url = require(env, "ZHYC_PLATFORM_DATASOURCE_URL");
        String username = require(env, "ZHYC_PLATFORM_DATASOURCE_USERNAME");
        String password = require(env, "ZHYC_PLATFORM_DATASOURCE_PASSWORD");
        InitSnapshot initSnapshot = parseInitSql(Path.of(args[1]));

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            DatabaseSnapshot databaseSnapshot = readDatabaseSnapshot(connection);
            printStructureDiff(initSnapshot, databaseSnapshot);
            printSeedSummary(connection);
            printSensitiveSummary(connection);
            dumpExtraDdlIfRequested(args, connection, initSnapshot, databaseSnapshot);
        }
    }

    private static InitSnapshot parseInitSql(Path sqlPath) throws IOException {
        String sql = Files.readString(sqlPath, StandardCharsets.UTF_8);
        Map<String, Set<String>> tables = new LinkedHashMap<>();
        int cursor = 0;
        while (cursor < sql.length()) {
            int createIndex = indexOfIgnoreCase(sql, "CREATE TABLE IF NOT EXISTS", cursor);
            if (createIndex < 0) {
                break;
            }
            int nameStart = createIndex + "CREATE TABLE IF NOT EXISTS".length();
            int parenIndex = sql.indexOf('(', nameStart);
            if (parenIndex < 0) {
                break;
            }
            String tableName = sql.substring(nameStart, parenIndex).trim().replace("`", "");
            int endIndex = findMatchingParen(sql, parenIndex);
            if (endIndex < 0) {
                break;
            }
            tables.put(normalize(tableName), parseColumnNames(sql.substring(parenIndex + 1, endIndex)));
            cursor = endIndex + 1;
        }
        return new InitSnapshot(tables);
    }

    private static Set<String> parseColumnNames(String body) {
        Set<String> columns = new LinkedHashSet<>();
        for (String definition : splitTableDefinitions(body)) {
            String trimmed = definition.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String firstToken = trimmed.split("\\s+", 2)[0].replace("`", "").replace(",", "");
            if (Set.of("PRIMARY", "UNIQUE", "KEY", "CONSTRAINT", "INDEX", "FOREIGN").contains(firstToken.toUpperCase(Locale.ROOT))) {
                continue;
            }
            columns.add(normalize(firstToken));
        }
        return columns;
    }

    private static List<String> splitTableDefinitions(String body) {
        List<String> definitions = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inSingleQuote = false;
        for (int i = 0; i < body.length(); i++) {
            char currentChar = body.charAt(i);
            char nextChar = i + 1 < body.length() ? body.charAt(i + 1) : '\0';
            if (currentChar == '\'' && nextChar == '\'') {
                current.append(currentChar).append(nextChar);
                i++;
                continue;
            }
            if (currentChar == '\'') {
                inSingleQuote = !inSingleQuote;
            }
            if (!inSingleQuote) {
                if (currentChar == '(') {
                    depth++;
                } else if (currentChar == ')') {
                    depth--;
                } else if (currentChar == ',' && depth == 0) {
                    definitions.add(current.toString());
                    current.setLength(0);
                    continue;
                }
            }
            current.append(currentChar);
        }
        if (!current.toString().isBlank()) {
            definitions.add(current.toString());
        }
        return definitions;
    }

    private static DatabaseSnapshot readDatabaseSnapshot(Connection connection) throws SQLException {
        Map<String, Set<String>> tables = new TreeMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("""
                 SELECT TABLE_NAME, COLUMN_NAME
                 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                 ORDER BY TABLE_NAME, ORDINAL_POSITION
                 """)) {
            while (resultSet.next()) {
                String tableName = normalize(resultSet.getString("TABLE_NAME"));
                String columnName = normalize(resultSet.getString("COLUMN_NAME"));
                tables.computeIfAbsent(tableName, ignored -> new LinkedHashSet<>()).add(columnName);
            }
        }
        return new DatabaseSnapshot(tables);
    }

    private static void printStructureDiff(InitSnapshot initSnapshot, DatabaseSnapshot databaseSnapshot) {
        List<String> missingTables = new ArrayList<>();
        List<String> extraTables = new ArrayList<>();
        List<String> columnDiffs = new ArrayList<>();

        for (String tableName : initSnapshot.tables().keySet()) {
            if (!databaseSnapshot.tables().containsKey(tableName)) {
                missingTables.add(tableName);
                continue;
            }
            Set<String> initColumns = initSnapshot.tables().get(tableName);
            Set<String> databaseColumns = databaseSnapshot.tables().get(tableName);
            for (String columnName : databaseColumns) {
                if (!initColumns.contains(columnName)) {
                    columnDiffs.add("本地多字段：" + tableName + "." + columnName);
                }
            }
            for (String columnName : initColumns) {
                if (!databaseColumns.contains(columnName)) {
                    columnDiffs.add("脚本多字段：" + tableName + "." + columnName);
                }
            }
        }
        for (String tableName : databaseSnapshot.tables().keySet()) {
            if (!initSnapshot.tables().containsKey(tableName)) {
                extraTables.add(tableName);
            }
        }

        System.out.println("结构对比");
        System.out.println("- 初始化脚本表数量：" + initSnapshot.tables().size());
        System.out.println("- 本地数据库表数量：" + databaseSnapshot.tables().size());
        printList("初始化脚本缺少的本地表", extraTables);
        printList("本地库缺少的脚本表", missingTables);
        printList("字段差异", columnDiffs);
    }

    private static void printSeedSummary(Connection connection) throws SQLException {
        System.out.println("基础数据概览");
        try (Statement statement = connection.createStatement()) {
            printCount(statement, "sys_menu 菜单与按钮", "SELECT COUNT(*) FROM sys_menu WHERE tenant_id = 'zhyc-platform'");
            printCount(statement, "sys_role_menu 超级管理员授权", "SELECT COUNT(*) FROM sys_role_menu WHERE tenant_id = 'zhyc-platform' AND role_id = 1");
            printCount(statement, "sys_tenant_package_module 默认套餐模块权限", "SELECT COUNT(*) FROM sys_tenant_package_module WHERE package_id = 1");
            printCount(statement, "sys_module 模块登记", "SELECT COUNT(*) FROM sys_module");
            printCount(statement, "sys_dict_type 字典类型", "SELECT COUNT(*) FROM sys_dict_type WHERE tenant_id = 'zhyc-platform'");
            printCount(statement, "sys_dict_item 字典项", "SELECT COUNT(*) FROM sys_dict_item WHERE tenant_id = 'zhyc-platform'");
            printCount(statement, "i18n_message 国际化词条", "SELECT COUNT(*) FROM i18n_message WHERE tenant_id = 'zhyc-platform'");
            printCount(statement, "search_index_config 全文检索索引", "SELECT COUNT(*) FROM search_index_config WHERE tenant_id = 'zhyc-platform'");
            printCount(statement, "ai_provider AI 供应商配置", "SELECT COUNT(*) FROM ai_provider WHERE tenant_id = 'zhyc-platform'");
            printCount(statement, "ai_model_config AI 模型配置", "SELECT COUNT(*) FROM ai_model_config WHERE tenant_id = 'zhyc-platform'");
            printCount(statement, "ai_app AI 应用", "SELECT COUNT(*) FROM ai_app WHERE tenant_id = 'zhyc-platform'");
            printCount(statement, "ai_prompt_template AI 提示词", "SELECT COUNT(*) FROM ai_prompt_template WHERE tenant_id = 'zhyc-platform'");
        }
    }

    private static void printSensitiveSummary(Connection connection) throws SQLException {
        System.out.println("敏感字段处理");
        try (Statement statement = connection.createStatement()) {
            for (String columnName : SENSITIVE_COLUMNS) {
                printCount(statement, columnName + " 字段存在数量", """
                    SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND COLUMN_NAME = '%s'
                    """.formatted(columnName));
            }
        }
        System.out.println("- 对比报告不输出 password_hash、client_secret、secret_cipher、secret_ref 的任何值。");
        System.out.println("- AI 供应商密钥不导出；初始化脚本仅保留 AI 表结构和菜单权限种子。");
    }

    private static void dumpExtraDdlIfRequested(
        String[] args,
        Connection connection,
        InitSnapshot initSnapshot,
        DatabaseSnapshot databaseSnapshot
    ) throws IOException, SQLException {
        int outputIndex = -1;
        for (int i = 0; i < args.length; i++) {
            if ("--dump-extra-ddl".equals(args[i])) {
                outputIndex = i;
                break;
            }
        }
        if (outputIndex < 0) {
            return;
        }
        if (args.length <= outputIndex + 1 || args[outputIndex + 1].isBlank()) {
            throw new IllegalArgumentException("缺少导出路径：--dump-extra-ddl <file>");
        }
        List<String> extraTables = databaseSnapshot.tables().keySet().stream()
            .filter((tableName) -> !initSnapshot.tables().containsKey(tableName))
            .filter(CompareLocalDatabaseInit::isFlowableEngineTable)
            .sorted(Comparator.naturalOrder())
            .toList();
        Map<String, String> ddlByTable = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement()) {
            for (String tableName : extraTables) {
                try (ResultSet resultSet = statement.executeQuery("SHOW CREATE TABLE `" + tableName + "`")) {
                    resultSet.next();
                    ddlByTable.put(tableName, normalizeCreateTable(resultSet.getString(2), tableName));
                }
            }
        }
        List<String> orderedTables = sortByDependencies(ddlByTable);
        List<String> content = new ArrayList<>();
        content.add("-- Flowable 引擎运行表结构。");
        content.add("-- 说明：由 CompareLocalDatabaseInit 从本地开发库 SHOW CREATE TABLE 导出，只包含 DDL，不包含流程实例、变量、任务或历史数据。");
        content.add("-- 说明：本脚本不包含 AI 模型供应商密钥、系统密钥、用户密码或 OAuth2 客户端密钥。");
        content.add("");
        for (String tableName : orderedTables) {
            content.add(ddlByTable.get(tableName));
            content.add("");
        }
        Files.writeString(Path.of(args[outputIndex + 1]), String.join("\n", content), StandardCharsets.UTF_8);
        System.out.println("已导出 Flowable 引擎表 DDL：" + args[outputIndex + 1]);
    }

    private static boolean isFlowableEngineTable(String tableName) {
        return tableName.startsWith("act_") || tableName.startsWith("flw_");
    }

    private static String normalizeCreateTable(String ddl, String tableName) {
        String normalized = ddl
            .replace("CREATE TABLE `" + tableName + "`", "CREATE TABLE IF NOT EXISTS " + tableName)
            .replace("`", "")
            .replaceAll("(?i)\\s+COLLATE\\s+[A-Za-z0-9_]+", "")
            .replaceAll("(?i)\\s+COLLATE=[A-Za-z0-9_]+", "")
            .replaceAll("(?i)\\bBIGINT\\s*\\(\\s*20\\s*\\)", "BIGINT")
            .replaceAll("(?i)\\bINT\\s*\\(\\s*11\\s*\\)", "INT")
            .replaceAll("(?i)\\bTINYINT\\s*\\(\\s*4\\s*\\)", "TINYINT")
            .replaceAll("(?i) DEFAULT CHARSET=[A-Za-z0-9_]+", " DEFAULT CHARSET=utf8mb4")
            .replaceAll("(?i) AUTO_INCREMENT=\\d+\\s*", "");
        normalized = lowerFlowableReferences(normalized);
        normalized = normalized.replaceFirst(
            "(?is)\\)\\s*ENGINE=InnoDB\\s+DEFAULT CHARSET=utf8mb4",
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Flowable 引擎表 " + tableName + "'"
        );
        return normalized + ";";
    }

    private static String lowerFlowableReferences(String ddl) {
        Matcher matcher = Pattern.compile("(?i)REFERENCES\\s+((?:ACT|FLW)_[A-Z0-9_]+)").matcher(ddl);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, "REFERENCES " + matcher.group(1).toLowerCase(Locale.ROOT));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static List<String> sortByDependencies(Map<String, String> ddlByTable) {
        List<String> ordered = new ArrayList<>();
        Set<String> visiting = new LinkedHashSet<>();
        Set<String> visited = new LinkedHashSet<>();
        for (String tableName : ddlByTable.keySet()) {
            visitTable(tableName, ddlByTable, visiting, visited, ordered);
        }
        return ordered;
    }

    private static void visitTable(
        String tableName,
        Map<String, String> ddlByTable,
        Set<String> visiting,
        Set<String> visited,
        List<String> ordered
    ) {
        if (visited.contains(tableName)) {
            return;
        }
        if (visiting.contains(tableName)) {
            return;
        }
        visiting.add(tableName);
        for (String dependency : findDependencies(ddlByTable.get(tableName))) {
            if (ddlByTable.containsKey(dependency)) {
                visitTable(dependency, ddlByTable, visiting, visited, ordered);
            }
        }
        visiting.remove(tableName);
        visited.add(tableName);
        ordered.add(tableName);
    }

    private static Set<String> findDependencies(String ddl) {
        Set<String> dependencies = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("(?i)REFERENCES\\s+([A-Za-z_][A-Za-z0-9_]*)").matcher(ddl);
        while (matcher.find()) {
            dependencies.add(normalize(matcher.group(1)));
        }
        return dependencies;
    }

    private static void printCount(Statement statement, String label, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            System.out.println("- " + label + "：" + resultSet.getLong(1));
        }
    }

    private static void printList(String label, List<String> values) {
        System.out.println("- " + label + "：" + values.size());
        for (String value : values) {
            System.out.println("  - " + value);
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

    private static int indexOfIgnoreCase(String source, String target, int fromIndex) {
        return source.toLowerCase(Locale.ROOT).indexOf(target.toLowerCase(Locale.ROOT), fromIndex);
    }

    private static int findMatchingParen(String source, int openIndex) {
        int depth = 0;
        boolean inSingleQuote = false;
        for (int i = openIndex; i < source.length(); i++) {
            char current = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';
            if (current == '\'' && next == '\'') {
                i++;
                continue;
            }
            if (current == '\'') {
                inSingleQuote = !inSingleQuote;
            }
            if (inSingleQuote) {
                continue;
            }
            if (current == '(') {
                depth++;
            } else if (current == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String normalize(String value) {
        return value.trim().replace("`", "").toLowerCase(Locale.ROOT);
    }

    private record InitSnapshot(Map<String, Set<String>> tables) {
    }

    private record DatabaseSnapshot(Map<String, Set<String>> tables) {
    }
}
