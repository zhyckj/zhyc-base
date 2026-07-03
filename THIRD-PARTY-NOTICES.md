# 第三方依赖许可证说明

本项目包含或依赖多个第三方开源组件。第三方组件不适用本项目的非商业授权条款，而是继续遵守其各自许可证、版权声明和通知要求。

## 后端主要依赖

| 组件 | 用途 | 常见许可证 |
| --- | --- | --- |
| Spring Boot / Spring Framework | 后端应用框架 | Apache-2.0 |
| Spring Authorization Server | OAuth2/OIDC 认证中心 | Apache-2.0 |
| MyBatis / MyBatis Spring Boot | 数据访问 | Apache-2.0 |
| Apache Shiro | 权限与安全框架 | Apache-2.0 |
| Flowable | 工作流引擎 | Apache-2.0 |
| MySQL Connector/J | MySQL 数据库驱动 | GPL-2.0 with FOSS exception |
| HikariCP | 数据库连接池 | Apache-2.0 |
| Jackson | JSON 序列化 | Apache-2.0 |
| JUnit / Mockito | 测试框架 | EPL-2.0 / MIT |

## 前端主要依赖

| 组件 | 用途 | 常见许可证 |
| --- | --- | --- |
| Vue | 前端框架 | MIT |
| Vite | 前端构建工具 | MIT |
| TypeScript | 类型系统和编译工具 | Apache-2.0 |
| Ant Design Vue | 后台管理 UI 组件 | MIT |
| Pinia | 前端状态管理 | MIT |
| Vue Router | 前端路由 | MIT |
| LogicFlow | 流程设计器 | MIT |
| dayjs | 时间处理 | MIT |

## 移动端主要依赖

| 组件 | 用途 | 常见许可证 |
| --- | --- | --- |
| uni-app / DCloud 相关包 | 移动端跨端框架 | 以 DCloud 官方发布许可证为准 |
| Vue | 移动端页面框架 | MIT |
| Vite | 构建工具 | MIT |
| TypeScript | 类型系统和编译工具 | Apache-2.0 |

## 使用要求

1. 分发本项目或其修改版本时，应保留第三方依赖自带的版权声明、许可证文件和 NOTICE 文件。
2. 若发布二进制包、Docker 镜像、安装包或离线依赖包，应同步提供对应第三方许可证清单。
3. 新增依赖前应确认其许可证与本项目“非商业免费 + 商业授权”模式兼容。
4. 本文件为人工维护摘要，不替代第三方组件官方许可证文本。最终以各组件发布包内的许可证文件为准。
