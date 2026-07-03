/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');
const supportedDialects = new Set(['postgresql', 'oracle', 'sqlserver', 'dm']);
const dialect = resolveDialect(process.argv);
const outputPath = resolveOutputPath(process.argv, dialect);

const sqlSections = [
  { name: '认证中心核心表', path: 'zhyc-auth-server/src/main/resources/db/V1__auth_server_core.sql' },
  { name: '系统与租户核心表', path: 'zhyc-module-system/src/main/resources/db/V1__system_core.sql' },
  { name: '低代码元数据表', path: 'zhyc-module-lowcode/src/main/resources/db/V1__lowcode_core.sql' },
  { name: '工作流运行表', path: 'zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql' },
  { name: 'Flowable 引擎运行表', path: 'zhyc-module-workflow/src/main/resources/db/V2__flowable_engine_mysql.sql' },
  { name: '开放平台管理表', path: 'zhyc-module-openapi/src/main/resources/db/V1__openapi_core.sql' },
  { name: 'AI 能力中心表', path: 'zhyc-module-ai/src/main/resources/db/V1__ai_core.sql' },
  { name: '消息中心表', path: 'zhyc-module-message/src/main/resources/db/V1__message_core.sql' },
  { name: '文件中心表', path: 'zhyc-module-file/src/main/resources/db/V1__file_core.sql' },
  { name: '内容管理表', path: 'zhyc-module-cms/src/main/resources/db/V1__cms_core.sql' },
  { name: '在线作业表', path: 'zhyc-module-job/src/main/resources/db/V1__job_core.sql' },
  { name: '全文检索表', path: 'zhyc-module-search/src/main/resources/db/V1__search_core.sql' },
  { name: '可视化大屏表', path: 'zhyc-module-visual/src/main/resources/db/V1__visual_core.sql' },
  { name: '国际化资源表', path: 'zhyc-module-i18n/src/main/resources/db/V1__i18n_core.sql' },
  { name: '采购样板业务表', path: 'zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql' },
];

buildDatabaseInitSql();

/**
 * 生成指定数据库方言的初始化总脚本。
 *
 * <p>输入只读取仓库内受控 DDL，输出不写入账号、密码、Token 或管理员口令。</p>
 */
function buildDatabaseInitSql() {
  const missingSections = sqlSections.filter((section) => !existsSync(resolve(serverRoot, section.path)));
  if (missingSections.length > 0) {
    console.error('数据库初始化 SQL 生成失败。');
    missingSections.forEach((section) => console.error(`- 缺少 SQL 文件：${section.path}`));
    process.exit(1);
  }

  const content = [
    `-- ZHYC 快速开发平台 ${displayDialect(dialect)} 初始化脚本`,
    '-- 说明：由 zhyc-base-server/scripts/build-database-init-sql.mjs 从模块 DDL 生成。',
    '-- 说明：当前脚本只包含表结构初始化；基础种子数据需按目标数据库单独审阅后导入。',
    '',
  ];

  sqlSections.forEach((section, index) => {
    const sourcePath = resolve(serverRoot, section.path);
    const sourceSql = readFileSync(sourcePath, 'utf8').trim();
    content.push(
      '-- ============================================================',
      `-- ${index + 1}. ${section.name}`,
      `-- 来源：zhyc-base-server/${section.path}`,
      '-- ============================================================',
      convertSql(sourceSql, dialect),
      '',
    );
  });

  content.push('-- 初始化脚本结束。', '');
  mkdirSync(dirname(outputPath), { recursive: true });
  writeFileSync(outputPath, content.join('\n'), 'utf8');
  console.log(`${displayDialect(dialect)} 初始化 SQL 已生成：${outputPath}`);
}

/**
 * 解析目标数据库方言。
 *
 * @param argv 命令行参数
 * @returns 数据库方言编码
 */
function resolveDialect(argv) {
  const dialectIndex = argv.indexOf('--dialect');
  const rawDialect = dialectIndex >= 0 && argv[dialectIndex + 1] ? argv[dialectIndex + 1] : '';
  const normalizedDialect = rawDialect.trim().toLowerCase();
  if (!supportedDialects.has(normalizedDialect)) {
    console.error(`不支持的初始化脚本方言：${rawDialect || '<empty>'}`);
    console.error(`可选值：${Array.from(supportedDialects).join(', ')}`);
    process.exit(1);
  }
  return normalizedDialect;
}

/**
 * 解析初始化 SQL 输出路径。
 *
 * @param argv 命令行参数
 * @param targetDialect 数据库方言编码
 * @returns 输出文件绝对路径
 */
function resolveOutputPath(argv, targetDialect) {
  const outputIndex = argv.indexOf('--output');
  const rawOutput = outputIndex >= 0 && argv[outputIndex + 1]
    ? argv[outputIndex + 1]
    : `db/init-zhyc-base-v1.${targetDialect}.sql`;
  return resolve(workspaceRoot, rawOutput);
}

/**
 * 转换单个模块 DDL。
 *
 * @param sourceSql MySQL 方言 DDL
 * @param targetDialect 目标数据库方言
 * @returns 目标数据库方言 DDL
 */
function convertSql(sourceSql, targetDialect) {
  const lines = sourceSql.split(/\r?\n/);
  const output = [];
  for (let index = 0; index < lines.length; index++) {
    const line = lines[index];
    const match = line.match(/^\s*CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(\s*$/i);
    if (!match) {
      if (/^\s*INSERT\s+INTO\b/i.test(line)) {
        const insertLines = [line];
        while (index + 1 < lines.length && !/;\s*$/.test(lines[index])) {
          index += 1;
          insertLines.push(lines[index]);
        }
        output.push(skipInsertStatement(insertLines));
        continue;
      }
      output.push(convertLooseStatement(line, targetDialect));
      continue;
    }
    const tableName = match[1];
    const tableLines = [];
    index += 1;
    while (index < lines.length && !isCreateTableEnd(lines[index])) {
      tableLines.push(lines[index]);
      index += 1;
    }
    const endLine = lines[index] || '';
    output.push(convertCreateTable(tableName, tableLines, endLine, targetDialect));
  }
  return output.join('\n').replace(/\n{3,}/g, '\n\n').trim();
}

/**
 * 判断当前行是否为 MySQL 建表结束行。
 *
 * @param line SQL 行
 * @returns 是结束行时返回 true
 */
function isCreateTableEnd(line) {
  return /^\s*\)\s*(ENGINE=|COMMENT=|;)/i.test(line);
}

/**
 * 转换建表语句。
 *
 * @param tableName 表名
 * @param tableLines 表内字段、索引、约束行
 * @param endLine MySQL 建表结束行
 * @param targetDialect 目标数据库方言
 * @returns 目标数据库建表语句
 */
function convertCreateTable(tableName, tableLines, endLine, targetDialect) {
  const tableComment = extractTableComment(endLine);
  const columnComments = [];
  const indexes = [];
  const definitions = [];

  for (const rawLine of combineTableLines(tableLines)) {
    const parsed = parseTableLine(rawLine);
    if (!parsed) {
      continue;
    }
    if (parsed.kind === 'index') {
      indexes.push(parsed);
      continue;
    }
    if (parsed.kind === 'unique') {
      definitions.push(`CONSTRAINT ${quote(parsed.name, targetDialect)} UNIQUE ${quoteColumnList(parsed.columns, targetDialect)}`);
      continue;
    }
    if (parsed.kind === 'constraint') {
      definitions.push(convertConstraint(parsed.sql, targetDialect));
      continue;
    }
    const convertedColumn = convertColumn(parsed.sql, targetDialect);
    definitions.push(convertedColumn.sql);
    if (parsed.comment) {
      columnComments.push({ columnName: parsed.columnName, comment: parsed.comment });
    }
  }

  const createTable = createTableHeader(tableName, targetDialect);
  const statement = [
    createTable,
    definitions.map((definition, index) => `    ${definition}${index === definitions.length - 1 ? '' : ','}`).join('\n'),
    createTableFooter(targetDialect),
  ];
  statement.push(...commentStatements(tableName, tableComment, columnComments, targetDialect));
  statement.push(...indexStatements(tableName, indexes, targetDialect));
  return statement.filter(Boolean).join('\n');
}

/**
 * 解析表内单行定义。
 *
 * @param rawLine MySQL 表内行
 * @returns 解析后的行定义
 */
function parseTableLine(rawLine) {
  const trimmed = rawLine.trim().replace(/,$/, '');
  if (!trimmed) {
    return null;
  }
  const normalized = trimmed.replace(/\s+/g, ' ');
  const indexMatch = normalized.match(/^KEY\s+([A-Za-z_][A-Za-z0-9_]*)\s+(\(.+\))$/i);
  if (indexMatch) {
    return { kind: 'index', unique: false, name: indexMatch[1], columns: indexMatch[2] };
  }
  const uniqueMatch = normalized.match(/^UNIQUE\s+KEY\s+([A-Za-z_][A-Za-z0-9_]*)\s+(\(.+\))$/i);
  if (uniqueMatch) {
    return { kind: 'unique', name: uniqueMatch[1], columns: uniqueMatch[2] };
  }
  if (/^(PRIMARY\s+KEY|CONSTRAINT)\b/i.test(trimmed)) {
    return { kind: 'constraint', sql: trimmed };
  }
  const columnMatch = trimmed.match(/^([A-Za-z_][A-Za-z0-9_]*)(\s+.+)$/);
  if (!columnMatch) {
    return { kind: 'constraint', sql: trimmed };
  }
  const commentMatch = trimmed.match(/\s+COMMENT\s+'((?:''|[^'])*)'\s*$/i);
  return {
    kind: 'column',
    columnName: columnMatch[1],
    sql: trimmed,
    comment: commentMatch ? commentMatch[1].replaceAll("''", "'") : '',
  };
}

/**
 * 合并表内多行索引或约束定义。
 *
 * @param tableLines 原始表内行
 * @returns 合并后的表内行
 */
function combineTableLines(tableLines) {
  const combined = [];
  let buffer = '';
  let balance = 0;
  for (const line of tableLines) {
    const trimmed = line.trim();
    if (!trimmed) {
      continue;
    }
    buffer = buffer ? `${buffer} ${trimmed}` : trimmed;
    balance += countChar(trimmed, '(') - countChar(trimmed, ')');
    if (balance <= 0 && isTableLineComplete(buffer)) {
      combined.push(buffer);
      buffer = '';
      balance = 0;
    }
  }
  if (buffer) {
    combined.push(buffer);
  }
  return combined;
}

/**
 * 判断合并后的表内定义是否完整。
 *
 * @param value 当前缓冲内容
 * @returns 定义完整时返回 true
 */
function isTableLineComplete(value) {
  if (/^CONSTRAINT\b/i.test(value) && /\bFOREIGN\s+KEY\b/i.test(value) && !/\bREFERENCES\b/i.test(value)) {
    return false;
  }
  return true;
}

/**
 * 统计字符出现次数。
 *
 * @param value 原始文本
 * @param char 目标字符
 * @returns 出现次数
 */
function countChar(value, char) {
  return Array.from(value).filter((item) => item === char).length;
}

/**
 * 转换列定义。
 *
 * @param sql MySQL 列定义
 * @param targetDialect 目标数据库方言
 * @returns 转换后的列定义和列名
 */
function convertColumn(sql, targetDialect) {
  const columnName = sql.match(/^([A-Za-z_][A-Za-z0-9_]*)\b/)?.[1] || '';
  let converted = sql
    .replace(/\s+COMMENT\s+'(?:''|[^'])*'\s*$/i, '')
    .replace(/\bLONGBLOB\b/gi, targetDialect === 'postgresql' ? 'BYTEA' : ['oracle', 'dm'].includes(targetDialect) ? 'BLOB' : 'VARBINARY(MAX)')
    .replace(/\bLONGTEXT\b/gi, targetDialect === 'postgresql' ? 'TEXT' : ['oracle', 'dm'].includes(targetDialect) ? 'CLOB' : 'NVARCHAR(MAX)')
    .replace(/\bDOUBLE\b/gi, targetDialect === 'sqlserver' ? 'FLOAT' : 'DOUBLE PRECISION')
    .replace(/\bTINYINT\s*\(\s*1\s*\)/gi, targetDialect === 'postgresql' ? 'BOOLEAN' : ['oracle', 'dm'].includes(targetDialect) ? 'NUMBER(1)' : 'BIT')
    .replace(/\bTINYINT\b/gi, targetDialect === 'postgresql' ? 'SMALLINT' : ['oracle', 'dm'].includes(targetDialect) ? 'NUMBER(3)' : 'SMALLINT')
    .replace(/\bDATETIME\b/gi, targetDialect === 'postgresql' ? 'TIMESTAMP' : ['oracle', 'dm'].includes(targetDialect) ? 'TIMESTAMP' : 'DATETIME2')
    .replace(/\bTIMESTAMP\b/gi, targetDialect === 'sqlserver' ? 'DATETIME2' : 'TIMESTAMP')
    .replace(/\bTEXT\b/gi, targetDialect === 'postgresql' ? 'TEXT' : ['oracle', 'dm'].includes(targetDialect) ? 'CLOB' : 'NVARCHAR(MAX)')
    .replace(/\bBLOB\b/gi, targetDialect === 'postgresql' ? 'BYTEA' : ['oracle', 'dm'].includes(targetDialect) ? 'BLOB' : 'VARBINARY(MAX)');

  converted = convertIntegerTypes(converted, targetDialect);
  converted = convertStringTypes(converted, targetDialect);
  converted = convertAutoIncrement(converted, targetDialect);
  converted = convertCurrentTimestampDefault(converted, targetDialect);
  return { columnName, sql: convertIdentifierPrefix(converted, targetDialect) };
}

/**
 * 转换整数类型。
 *
 * @param sql 列定义
 * @param targetDialect 目标数据库方言
 * @returns 转换后的列定义
 */
function convertIntegerTypes(sql, targetDialect) {
  if (targetDialect === 'oracle') {
    return sql.replace(/\bBIGINT\b/gi, 'NUMBER(19)').replace(/\bINT\b/gi, 'NUMBER(10)');
  }
  if (targetDialect === 'dm') {
    return sql.replace(/\bBIGINT\b/gi, 'BIGINT').replace(/\bINT\b/gi, 'INT');
  }
  return sql.replace(/\bINT\b/gi, targetDialect === 'postgresql' ? 'INTEGER' : 'INT');
}

/**
 * 转换字符串类型。
 *
 * @param sql 列定义
 * @param targetDialect 目标数据库方言
 * @returns 转换后的列定义
 */
function convertStringTypes(sql, targetDialect) {
  if (targetDialect === 'oracle') {
    return sql.replace(/\bVARCHAR\s*\(/gi, 'VARCHAR2(');
  }
  if (targetDialect === 'sqlserver') {
    return sql.replace(/\bVARCHAR\s*\(/gi, 'NVARCHAR(');
  }
  return sql;
}

/**
 * 转换自增语法。
 *
 * @param sql 列定义
 * @param targetDialect 目标数据库方言
 * @returns 转换后的列定义
 */
function convertAutoIncrement(sql, targetDialect) {
  if (!/\bAUTO_INCREMENT\b/i.test(sql)) {
    return sql;
  }
  if (targetDialect === 'sqlserver') {
    return sql
      .replace(/\s+PRIMARY\s+KEY\s+AUTO_INCREMENT\b/i, ' IDENTITY(1,1) PRIMARY KEY')
      .replace(/\s+NOT\s+NULL\s+AUTO_INCREMENT\b/i, ' IDENTITY(1,1) NOT NULL')
      .replace(/\s+AUTO_INCREMENT\b/i, ' IDENTITY(1,1)');
  }
  if (targetDialect === 'dm') {
    return sql
      .replace(/\s+PRIMARY\s+KEY\s+AUTO_INCREMENT\b/i, ' IDENTITY(1,1) PRIMARY KEY')
      .replace(/\s+NOT\s+NULL\s+AUTO_INCREMENT\b/i, ' IDENTITY(1,1) NOT NULL')
      .replace(/\s+AUTO_INCREMENT\b/i, ' IDENTITY(1,1)');
  }
  return sql.replace(/\s+PRIMARY\s+KEY\s+AUTO_INCREMENT\b/i, ' GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY')
    .replace(/\s+NOT\s+NULL\s+AUTO_INCREMENT\b/i, ' GENERATED BY DEFAULT AS IDENTITY NOT NULL')
    .replace(/\s+AUTO_INCREMENT\b/i, ' GENERATED BY DEFAULT AS IDENTITY');
}

/**
 * 转换更新时间默认值。
 *
 * @param sql 列定义
 * @param targetDialect 目标数据库方言
 * @returns 转换后的列定义
 */
function convertCurrentTimestampDefault(sql, targetDialect) {
  let converted = sql.replace(/\s+ON\s+UPDATE\s+CURRENT_TIMESTAMP\b/gi, '');
  if (targetDialect === 'sqlserver') {
    converted = converted.replace(/\bDEFAULT\s+CURRENT_TIMESTAMP\s*\(\s*\d+\s*\)/gi, 'DEFAULT SYSDATETIME()');
    converted = converted.replace(/\bDEFAULT\s+CURRENT_TIMESTAMP\b/gi, 'DEFAULT SYSDATETIME()');
    converted = converted.replace(/\bSYSDATETIME\(\)\s*\(\s*\d+\s*\)/gi, 'SYSDATETIME()');
  }
  return converted;
}

/**
 * 转换约束定义。
 *
 * @param sql MySQL 约束定义
 * @param targetDialect 目标数据库方言
 * @returns 转换后的约束定义
 */
function convertConstraint(sql, targetDialect) {
  return sql
    .replace(/^CONSTRAINT\s+([A-Za-z_][A-Za-z0-9_]*)/i, (_matched, constraintName) => {
      return `CONSTRAINT ${quote(constraintName, targetDialect)}`;
    })
    .replace(/\bPRIMARY\s+KEY\s*(\([^)]*\))/i, (_matched, columns) => {
      return `PRIMARY KEY ${quoteColumnList(columns, targetDialect)}`;
    })
    .replace(/\bFOREIGN\s+KEY\s*(\([^)]*\))/i, (_matched, columns) => {
      return `FOREIGN KEY ${quoteColumnList(columns, targetDialect)}`;
    })
    .replace(/\bREFERENCES\s+([A-Za-z_][A-Za-z0-9_]*)\s*(\([^)]*\))/i, (_matched, tableName, columns) => {
      return `REFERENCES ${quote(tableName, targetDialect)} ${quoteColumnList(columns, targetDialect)}`;
    });
}

/**
 * 转换约束或索引中的列列表引用。
 *
 * @param columns MySQL 列列表，例如 (tenant_id, code)
 * @param targetDialect 目标数据库方言
 * @returns 使用目标数据库引用符的列列表
 */
function quoteColumnList(columns, targetDialect) {
  return columns.replace(/\(([^()]*)\)/g, (_matched, columnList) => {
    const quotedColumns = columnList.split(',').map((item) => quoteColumnReference(item.trim(), targetDialect));
    return `(${quotedColumns.join(', ')})`;
  });
}

/**
 * 转换单个列引用，保留 ASC/DESC 等简单后缀。
 *
 * @param columnReference 列引用文本
 * @param targetDialect 目标数据库方言
 * @returns 使用目标数据库引用符的列引用
 */
function quoteColumnReference(columnReference, targetDialect) {
  const match = columnReference.match(/^([A-Za-z_][A-Za-z0-9_]*)(.*)$/);
  if (!match) {
    return columnReference;
  }
  return `${quote(match[1], targetDialect)}${match[2]}`;
}

/**
 * 转换行首标识符引用。
 *
 * @param sql SQL 片段
 * @param targetDialect 目标数据库方言
 * @returns 转换后的 SQL 片段
 */
function convertIdentifierPrefix(sql, targetDialect) {
  const match = sql.match(/^([A-Za-z_][A-Za-z0-9_]*)(\s+.+)$/);
  if (!match || /^(PRIMARY|CONSTRAINT|FOREIGN|UNIQUE|KEY)$/i.test(match[1])) {
    return sql;
  }
  return `${quote(match[1], targetDialect)}${match[2]}`;
}

/**
 * 生成建表头。
 *
 * @param tableName 表名
 * @param targetDialect 目标数据库方言
 * @returns 建表头
 */
function createTableHeader(tableName, targetDialect) {
  if (targetDialect === 'sqlserver') {
    return `IF OBJECT_ID(N'${tableName}', N'U') IS NULL\nBEGIN\nCREATE TABLE ${quote(tableName, targetDialect)} (`;
  }
  if (['oracle', 'dm'].includes(targetDialect)) {
    return `CREATE TABLE ${quote(tableName, targetDialect)} (`;
  }
  return `CREATE TABLE IF NOT EXISTS ${quote(tableName, targetDialect)} (`;
}

/**
 * 生成建表尾。
 *
 * @param targetDialect 目标数据库方言
 * @returns 建表尾
 */
function createTableFooter(targetDialect) {
  return targetDialect === 'sqlserver' ? ');\nEND;\nGO' : ');';
}

/**
 * 生成表和字段注释语句。
 *
 * @param tableName 表名
 * @param tableComment 表注释
 * @param columnComments 字段注释列表
 * @param targetDialect 目标数据库方言
 * @returns 注释语句列表
 */
function commentStatements(tableName, tableComment, columnComments, targetDialect) {
  const comments = [];
  if (tableComment) {
    comments.push(commentStatement(tableName, '', tableComment, targetDialect));
  }
  for (const column of columnComments) {
    comments.push(commentStatement(tableName, column.columnName, column.comment, targetDialect));
  }
  return comments;
}

/**
 * 生成单条注释语句。
 *
 * @param tableName 表名
 * @param columnName 字段名，表注释传空字符串
 * @param comment 中文注释
 * @param targetDialect 目标数据库方言
 * @returns 注释语句
 */
function commentStatement(tableName, columnName, comment, targetDialect) {
  const escapedComment = escapeSql(comment);
  if (targetDialect === 'sqlserver') {
    if (!columnName) {
      return `EXEC sp_addextendedproperty N'MS_Description', N'${escapedComment}', N'SCHEMA', N'dbo', N'TABLE', N'${tableName}';`;
    }
    return `EXEC sp_addextendedproperty N'MS_Description', N'${escapedComment}', N'SCHEMA', N'dbo', N'TABLE', N'${tableName}', N'COLUMN', N'${columnName}';`;
  }
  if (!columnName) {
    return `COMMENT ON TABLE ${quote(tableName, targetDialect)} IS '${escapedComment}';`;
  }
  return `COMMENT ON COLUMN ${quote(tableName, targetDialect)}.${quote(columnName, targetDialect)} IS '${escapedComment}';`;
}

/**
 * 生成索引语句。
 *
 * @param tableName 表名
 * @param indexes MySQL KEY 定义列表
 * @param targetDialect 目标数据库方言
 * @returns 索引语句列表
 */
function indexStatements(tableName, indexes, targetDialect) {
  return indexes.map((index) => {
    const indexName = quote(index.name, targetDialect);
    const tableIdentifier = quote(tableName, targetDialect);
    if (targetDialect === 'postgresql') {
      return `CREATE INDEX IF NOT EXISTS ${indexName} ON ${tableIdentifier} ${quoteColumnList(index.columns, targetDialect)};`;
    }
    if (targetDialect === 'sqlserver') {
      return `IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'${index.name}' AND object_id = OBJECT_ID(N'${tableName}'))\nCREATE INDEX ${indexName} ON ${tableIdentifier} ${quoteColumnList(index.columns, targetDialect)};`;
    }
    return `CREATE INDEX ${indexName} ON ${tableIdentifier} ${quoteColumnList(index.columns, targetDialect)};`;
  });
}

/**
 * 转换散落在建表外的语句。
 *
 * @param line SQL 行
 * @param targetDialect 目标数据库方言
 * @returns 转换后的 SQL 行
 */
function convertLooseStatement(line, targetDialect) {
  if (/^\s*SET\s+NAMES\b/i.test(line)) {
    return targetDialect === 'postgresql' ? 'SET client_encoding = UTF8;' : `-- ${line.trim()}`;
  }
  return line;
}

/**
 * 跳过非 MySQL 暂不自动转换的幂等数据初始化语句。
 *
 * @param insertLines INSERT 语句行
 * @returns 跳过说明
 */
function skipInsertStatement(insertLines) {
  const targetTable = insertLines[0].match(/INSERT\s+INTO\s+([A-Za-z_][A-Za-z0-9_]*)/i)?.[1] || 'unknown_table';
  return `-- 已跳过 MySQL 幂等数据初始化语句：INSERT INTO ${targetTable}，请按目标数据库语法单独审阅后导入。`;
}

/**
 * 提取表注释。
 *
 * @param endLine MySQL 建表结束行
 * @returns 表注释
 */
function extractTableComment(endLine) {
  const match = endLine.match(/COMMENT\s*=\s*'((?:''|[^'])*)'/i);
  return match ? match[1].replaceAll("''", "'") : '';
}

/**
 * 引用数据库标识符。
 *
 * @param identifier 标识符
 * @param targetDialect 目标数据库方言
 * @returns 引用后的标识符
 */
function quote(identifier, targetDialect) {
  if (!/^[A-Za-z_][A-Za-z0-9_]*$/.test(identifier)) {
    throw new Error(`非法数据库标识符：${identifier}`);
  }
  if (targetDialect === 'sqlserver') {
    return `[${identifier}]`;
  }
  return `"${identifier}"`;
}

/**
 * 转义 SQL 字符串字面量。
 *
 * @param value 原始文本
 * @returns 转义后的文本
 */
function escapeSql(value) {
  return value.replaceAll("'", "''");
}

/**
 * 返回数据库显示名称。
 *
 * @param targetDialect 目标数据库方言
 * @returns 数据库显示名称
 */
function displayDialect(targetDialect) {
  return {
    postgresql: 'PostgreSQL',
    oracle: 'Oracle',
    sqlserver: 'SQL Server',
    dm: '达梦数据库',
  }[targetDialect];
}
