# 订货管理系统

微信小程序订货系统，包含用户下单、商家后台管理和 Spark 销售分析。

适合毕业设计和个人作品集展示。当前版本可在本地完整运行核心业务流程。

## 技术栈

- 微信小程序
- Spring Boot 4 / Java 17
- MyBatis
- MySQL 8
- Vue 3 + ECharts
- Apache Spark 3.5.1

## 项目结构

```text
order-miniapp     微信小程序用户端
order-admin       Vue 商家后台
order-system      Spring Boot 后端
order-analysis    Spark 销售分析
```

## 主要功能

用户端：

- 商品浏览、搜索、分类筛选、商品详情
- 购物车、收货地址、提交订单
- 模拟支付、10 分钟超时自动取消
- 取消待支付订单、确认收货、订单详情
- 浏览记录

商家端：

- 管理员登录、修改账号密码
- 商品新增/编辑、图片上传、上下架
- 分类管理
- 订单确认、发货、完成
- Spark 销售统计图表

后端能力：

- 条件更新防止库存超卖
- 订单事务回滚
- 超时订单自动取消并恢复库存
- BCrypt 管理员密码加密
- 微信登录接口（可配置 AppID/AppSecret）

## 本地运行

1. 安装 Java 17、MySQL 8、Node.js、微信开发者工具。
2. 复制配置文件：

```text
order-system/src/main/resources/application-example.properties
→ application.properties
```

3. 修改数据库密码后启动后端：

```powershell
cd order-system
.\mvnw.cmd spring-boot:run
```

4. 启动商家后台：

```powershell
cd order-admin
npm install
npm run dev
```

或直接访问已嵌入后端的页面：

```text
http://localhost:8080/
```

商家测试账号：

```text
admin
123456
```

5. 用微信开发者工具打开 `order-miniapp`。

未配置微信 AppID/AppSecret 时，小程序使用本地演示登录。

## 说明

- 当前支付为模拟支付，不是微信支付。
- Spark 分析在商家点击“刷新数据”后异步运行。
- 正式上线需要 HTTPS 域名、微信合法域名和真实 AppID/AppSecret。

## License

仅用于学习与毕业设计展示。
