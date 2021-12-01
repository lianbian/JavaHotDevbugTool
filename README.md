# Java开发环境热加载工具

## 基于`Spring boot` + `Mybatis Plus` 热加载开发环境。
### 支持`Java`文件和`mapper xml`文件。
#### MybatisPlus热加载`mapper xml`调试插件
# 用于本地开发，不要用于生产环境。

# 使用简易教程
1. 配置 application.properties 环境为`dev`，在注解配置的时候，只有`dev`环境开启了注解；
2. 导入数据到数据库，sql脚本路径：resources/db/lianbian.sql；
3. 配置数据库连接 application.properties 文件；
4. 加载maven引用
5. 手动编译方式 ctrl + f9 查看对应的日志输出
