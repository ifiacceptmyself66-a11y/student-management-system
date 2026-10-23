# 学生管理系统

前后端分离的学生管理系统，第一版聚焦：管理员登录、班级管理、学生管理、搜索与分页。

## 技术栈

| 层 | 选型 |
| --- | --- |
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios |
| 后端 | Java 21 运行 / 编译目标 Java 17 + Spring Boot 3.2.x + MyBatis-Plus + Spring Security + JWT |
| 数据库 | MySQL 8.4（utf8mb4） |

## 目录结构

```
.
├── backend/                              # Spring Boot 后端
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/qinghe/sms/
│       │   ├── SmsApplication.java
│       │   ├── common/                   # 统一响应、状态码、异常处理
│       │   ├── config/                   # MyBatis-Plus、CORS、Spring Security
│       │   ├── security/                 # JWT 工具、过滤器、登录主体
│       │   ├── entity/  dto/  mapper/  service/  controller/
│       └── main/resources/
│           ├── application.yml
│           ├── mapper/StudentMapper.xml  # 学生联表分页 SQL
│           └── db/schema.sql             # 数据库初始化脚本
├── frontend/                             # Vue 3 前端
│   ├── package.json
│   ├── vite.config.ts                    # 端口 5173, /api 代理到 8080
│   ├── tsconfig.json
│   ├── index.html
│   ├── env.d.ts
│   ├── .env.development                  # VITE_API_BASE_URL=/api
│   └── src/
│       ├── main.ts                       # 应用入口, Element Plus 全量引入
│       ├── App.vue
│       ├── api/                          # request 封装 + auth/class/student 接口
│       ├── types/                        # api.ts 通用类型 + models.ts 业务模型
│       ├── utils/auth-storage.ts         # token/管理员信息本地存储
│       ├── stores/auth.ts                # Pinia 登录态
│       ├── router/index.ts               # 路由 + 登录守卫
│       ├── layouts/BasicLayout.vue       # 左侧菜单 + 顶栏
│       ├── views/                        # login(可用) / class、student(阶段4)
│       └── styles/index.css              # 主题变量(取自原型配色)
├── .gitignore
└── README.md
```

## 数据库初始化

1. 确认 MySQL 8.4 已启动。
2. 执行建表脚本（PowerShell 不支持 `<` 重定向，任选一种）：

```powershell
# 方式一：mysql 客户端 source（显式指定 utf8mb4，避免中文注释/昵称乱码）
mysql --default-character-set=utf8mb4 -u root -p -e "source d:/CODE/backend/src/main/resources/db/schema.sql"

# 方式二：cmd 重定向
cmd /c "mysql --default-character-set=utf8mb4 -u root -p < d:\CODE\backend\src\main\resources\db\schema.sql"
```

也可以用 MySQL Workbench / Navicat 打开 `schema.sql` 整体执行。

> 脚本是**幂等且非破坏性**的：只执行 `CREATE DATABASE IF NOT EXISTS` 与 `CREATE TABLE IF NOT EXISTS`，没有任何 `DROP` / `TRUNCATE`，重复执行不会删除已有表和数据；初始管理员用 `INSERT IGNORE` 写入，不会产生重复账号。
>
> 注意 `CREATE TABLE IF NOT EXISTS` **不做结构比对**：表已存在时会直接跳过，不会自动补字段或索引。需要"重置开发库"时, 请在确认无重要数据后由管理员另行手工处理, 再重跑本脚本。

### 建表要点

- 库名 `sms`，三张表：`admin`、`class_info`、`student`。
- 全部使用**逻辑删除**（`deleted` 字段，0 未删除 / 1 已删除）。
- “删除后可重新使用”的唯一约束通过**生成列 + 唯一索引**实现：例如 `student_no_active = IF(deleted = 0, student_no, NULL)`，唯一索引允许多个 `NULL`，因此已删除记录不占用学号。
- 学生表不存年龄，由 `birth_date` 实时计算；`class_id` 不建物理外键，由业务层保证引用有效。
- 删除班级前会校验班内是否仍有学生，有则拒绝删除。

### 应用数据库账号

应用使用独立账号 `sms_app`，只需 DML 权限，不需要 DDL 权限。建库建表用管理员账号执行，然后执行（**密码自行替换**）：

```sql
-- 在交互式 mysql 会话中执行, 密码当场输入, 不写入任何文件
CREATE USER IF NOT EXISTS 'sms_app'@'localhost' IDENTIFIED BY '<你的密码>';
GRANT SELECT, INSERT, UPDATE, DELETE ON sms.* TO 'sms_app'@'localhost';
FLUSH PRIVILEGES;
```

说明：`sms_app` 只有 DML 权限，没有 DDL 权限，因此**不能**用它执行 `schema.sql`；建库建表必须用 root。`CREATE USER IF NOT EXISTS` 可安全重复执行（重复执行不会改已存在账号的密码，需要改密码时用 `ALTER USER`）。

## 后端运行

数据库连接信息**不写入配置文件**，全部通过环境变量注入（见 `application.yml` 的 `${DB_URL}` 等占位符）。

```powershell
cd D:\CODE\backend

$env:DB_URL="jdbc:mysql://localhost:3306/sms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="sms_app"
$env:DB_PASSWORD="<你的密码>"
$env:JWT_SECRET="<至少 32 位、建议 48 位的随机字符串>"   # 不设置或过短会直接启动失败

mvn spring-boot:run
```

> 这 4 个变量必须在**启动 `mvn` 的同一个会话**里设置：Spring Boot 对配置属性中的占位符默认"解析不到就保留字面量"，
> 缺变量时应用仍能启动，直到第一次查库才暴露（且错误信息是 `${DB_URL}` 这类占位符，很难定位）。
> 因此 `StartupConfigValidator` 会在启动阶段检查 `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`，
> 缺失时**直接启动失败**并列出缺少的变量名。

服务启动在 `http://localhost:8080`，接口前缀 `/api`。

常用校验命令：

```powershell
mvn test        # 单元测试(不连接数据库)
mvn -q compile  # 只编译
```

## 前端运行

依赖安装（本机访问 npm 需要代理，用命令行参数指定，不写入 `~/.npmrc`）：

```powershell
cd D:\CODE\frontend
npm install --proxy=http://127.0.0.1:7892 --https-proxy=http://127.0.0.1:7892
```

启动开发服务器（端口 5173，`/api` 自动代理到 `http://localhost:8080`）：

```powershell
npm run dev
```

校验命令：

```powershell
npm run type-check   # vue-tsc 类型检查
npm run build        # 生产构建, 输出到 dist/
```

## 默认管理员（仅开发环境）

| 账号 | 密码 |
| --- | --- |
| `admin` | `123456` |

密码以 BCrypt 哈希存入数据库。**首次登录后请立即修改密码，生产环境不要使用默认密码**（生产部署时应删除 `schema.sql` 末尾的初始管理员 `INSERT`）。

## 分阶段实施计划

| 阶段 | 内容 | 状态 |
| --- | --- | --- |
| 1 | 数据库脚本（建库建表、索引、初始管理员） | 已完成（MySQL 8.4 实库验证：3 表、9 索引、4 生成列、脚本可重复执行） |
| 2 | 后端（工程骨架、登录鉴权、班级与学生接口） | 已完成（连库验证通过：登录/JWT/班级与学生 CRUD、2002/2003/2004 等业务码） |
| 3 | 前后端联调（代理、请求层、登录页、路由守卫） | 已完成（5173 代理到 8080，登录与受保护接口实测通过） |
| 4 | 前端页面（班级页、学生页：列表、搜索、分页、增删改） | 已完成（浏览器实测增删改查无异常） |

## 本地开发约定

- 后端端口 `8080`，前端开发服务器端口 `5173`，前端通过 Vite 代理把 `/api` 转发到后端。
- 接口统一前缀 `/api`，统一响应体 `{ code, message, data }`，`code = 0` 表示成功。
- 分页参数 `page`（从 1 开始）、`size`（10 / 20 / 50，默认 10）。
- 页面复用约定：列表页的搜索/分页/增删改逻辑放在 `src/views/<模块>/XxxListView.vue`，表单放在同目录 `XxxFormDialog.vue`；分页状态用 `src/composables/usePagination.ts`，下拉字典用 `src/utils/dict.ts`。

## 提交规范

提交信息使用 `feat: xxx`、`fix: xxx`、`docs: xxx`、`chore: xxx` 前缀。
