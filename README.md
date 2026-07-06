# 心理健康AI助手

这是一个基于Spring Boot和Vue的AI心理健康助手应用。

## 项目结构

```
├── ai-springboot/     # Spring Boot后端
├── ai-vue/           # Vue前端
├── .env.example      # 环境变量配置示例
└── init.sql          # 数据库初始化脚本
```

## 技术栈

### 后端
- Spring Boot 3.2.0
- MyBatis
- Spring Security
- MySQL

### 前端
- Vue 3
- Element Plus
- Axios
- Pinia

## 快速开始

### 1. 配置环境变量

复制 `.env.example` 文件为 `.env` 并填写你的配置：

```bash
cp .env.example .env
```

然后编辑 `.env` 文件，填写以下配置：

```env
DATABASE_URL=jdbc:mysql://localhost:3306/mental_health_assistant?useSSL=false&serverTimezone=UTC
DATABASE_USERNAME=root
DATABASE_PASSWORD=your_database_password

AI_API_KEY=your_api_key_here
AI_BASE_URL=https://api.siliconflow.cn

JWT_SECRET=your_jwt_secret_here
```

### 2. 初始化数据库

使用 `init.sql` 文件初始化数据库：

```bash
mysql -u root -p < init.sql
```

### 3. 启动后端

```bash
cd ai-springboot
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd ai-vue
npm install
npm run dev
```

## 主要功能

- 用户注册和登录
- AI心理咨询聊天
- 情绪日记记录
- 心理测评问卷
- 知识库浏览
- 数据分析统计

## 配置说明

### 数据库配置
- `DATABASE_URL`: 数据库连接地址
- `DATABASE_USERNAME`: 数据库用户名
- `DATABASE_PASSWORD`: 数据库密码

### AI配置
- `AI_API_KEY`: AI服务API密钥（硅基流动平台）
- `AI_BASE_URL`: AI服务基础URL

### JWT配置
- `JWT_SECRET`: JWT密钥（用于token加密）

## 注意事项

1. **安全警告**: 请勿将 `.env` 文件提交到Git仓库
2. 确保已安装Java 17+和Node.js 16+
3. 确保MySQL服务已启动并创建了相应的数据库

## 许可证

MIT License