# Super Agent

基于 **Spring Boot 3** 与 **Spring AI / Spring AI Alibaba** 的多模块 Maven 工程，提供企业级对话、RAG 知识问答、文档管理与相关基础设施封装；配套 **Vue 3 + Vite** 管理端界面。

## 技术栈概览

| 类别 | 说明 |
|------|------|
| 语言与构建 | Java 17、Maven |
| 核心框架 | Spring Boot 3.5.x、Spring AI 1.1.x、Spring AI Alibaba |
| 主业务模块 | Web、JDBC、MyBatis-Plus、Kafka、MinIO、Elasticsearch、Neo4j 驱动、Apache Tika 等 |
| 前端 | Vue 3、Vite 6、Vue Router |

版本与依赖 BOM 以根目录 `pom.xml` 为准。

## 仓库结构

```
super-agent/
├── super-agent-business/          # 业务聚合
│   └── super-agent-business-chat/ # 对话与知识管理主应用（可执行 Spring Boot）
├── super-agent-common/            # 公共 Web 等能力
├── super-agent-id-generator-framework/   # ID 生成框架
├── super-agent-redisson-framework/       # 基于 Redisson 的锁、租约、延迟队列等
├── ai-example/                    # Spring AI / RAG / MCP / Memory 等示例子工程
└── vue/                           # 业务聊天与管理前端（Vite）
```

- **主应用入口类**：`org.javaup.ai.SuperBusinessChatAgentApplication`（模块 `super-agent-business-chat`）。
- **示例工程**：`ai-example` 下各子模块为独立演示用途，按需单独运行。

## 环境要求

- JDK **17**
- Maven **3.6+**
- 运行主业务时，按 `super-agent-business-chat` 中配置准备：**MySQL**、**Redis**、**Kafka**、**MinIO**、**Elasticsearch**、**Neo4j**（及 OpenAI 兼容对话/向量、可选 Tavily 等），具体以 `application.yaml` 为准。

前端开发另需 **Node.js**（建议 LTS）与 npm/pnpm/yarn 其一。

## 后端：编译与运行

在仓库根目录执行：

```bash
mvn clean install -DskipTests
```

仅运行对话主服务（安装依赖到本地仓库后）：

```bash
cd super-agent-business/super-agent-business-chat
mvn spring-boot:run
```

默认 HTTP 端口在配置中为 **9082**（若已修改 `application.yaml` 中的 `server.port`，以实际为准）。

打包可执行 Jar：

```bash
mvn -pl super-agent-business/super-agent-business-chat -am package -DskipTests
```

产物位于 `super-agent-business/super-agent-business-chat/target/`。

## 配置说明

主配置位于：

`super-agent-business/super-agent-business-chat/src/main/resources/application.yaml`

其中包含数据源、Redis、Kafka、Spring AI（OpenAI 兼容协议）、RAG 参数、MinIO、检索与管理员认证等。**生产环境务必通过环境变量或外部配置中心注入密钥与连接信息**，不要将真实密钥提交到版本库。管理员相关项已支持例如 `SUPER_AGENT_ADMIN_USERNAME`、`SUPER_AGENT_ADMIN_PASSWORD` 等环境变量覆盖，详见同文件 `app.admin-auth` 段。

## 前端：开发与构建

```bash
cd vue
npm install
npm run dev
```

- 开发服务器默认 **5173**，并通过 Vite 将 `/api`、`/admin/auth`、`/manage` 代理到后端。
- 后端地址可通过环境变量 **`VITE_PROXY_TARGET`** 指定，未设置时默认为 `http://127.0.0.1:9082`。

生产构建：

```bash
npm run build
```

## 许可证与源码

根 `pom.xml` 中声明许可证为 **Apache License 2.0**；SCM 信息指向 Gitee 上的关联仓库（以你本地实际远程为准）。

## 子模块文档

部分框架或示例子目录自带 README，例如 `super-agent-id-generator-framework`、`ai-example-memory` 下相关说明，可按需查阅。
