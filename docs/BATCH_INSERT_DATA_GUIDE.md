# 批量插入测试数据指南

## 📋 概述

本文档说明如何使用批量插入脚本向数据库添加大量测试数据。

## 📊 数据规模

批量插入脚本将添加以下数据：
- **学生**: 500 人
- **教师**: 50 人
- **课程**: 30 门
- **班级**: 20 个
- **教室**: 30 个

## 🚀 使用方法

### 方法一：直接在 MySQL 命令行执行

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 选择数据库
USE aischedule;

# 3. 执行批量插入脚本
source src/main/resources/batch_insert_data.sql;
```

### 方法二：使用 MySQL Workbench

1. 打开 MySQL Workbench 并连接到数据库
2. 打开文件 `src/main/resources/batch_insert_data.sql`
3. 执行整个脚本（⚡ Execute All）

### 方法三：在 Spring Boot 启动时自动执行

如果需要每次启动都重新生成数据，可以：

1. 备份原有数据（可选）
2. 将 `batch_insert_data.sql` 的内容复制到 `data.sql`
3. 确保 `application.properties` 中配置了：
   ```properties
   spring.datasource.initialization-mode=always
   spring.jpa.hibernate.ddl-auto=create-drop
   ```

## ⚠️ 注意事项

1. **数据重复**: 执行前请确认是否需要清空现有数据
   - 脚本默认不清空现有数据
   - 如需清空，取消注释脚本开头的 `DELETE` 语句

2. **学号规则**: 学生学号格式为 `202300001` - `202300500`
   - 如果与现有学号冲突，请修改生成规则

3. **外键约束**: 确保先插入教师、班级、教室，再插入学生

4. **执行时间**: 500 条学生数据大约需要 5-10 秒

## 📝 数据分布

### 学生专业分布
- 1-100: 计算机科学与技术
- 101-200: 软件工程
- 201-280: 人工智能
- 281-360: 网络空间安全
- 361-420: 数据科学
- 421-470: 信息工程
- 471-500: 自动化

### 班级列表
计科 2301-2304、软工 2301-2303、人工智能 2301-2302、网安 2301-2302、数据科学 2301-2302、信工 2301-2302、自动化 2301-2302、电子 2301-2302、通信 2301

### 教室类型
- 多媒体教室：A101-A102, A401-A402, B301-B302, C301-C302, E101-E201
- 阶梯教室：A201-A202, C201-C202, E301
- 实验室：B101-B202, D101-D302
- 报告厅：C101-C102
- 研讨室：F101

## 🔍 验证数据

执行以下 SQL 查询验证数据：

```sql
-- 统计各表数据量
SELECT '学生' AS 表名, COUNT(*) AS 数量 FROM student
UNION ALL
SELECT '教师', COUNT(*) FROM teacher
UNION ALL
SELECT '课程', COUNT(*) FROM course
UNION ALL
SELECT '班级', COUNT(*) FROM class_group
UNION ALL
SELECT '教室', COUNT(*) FROM classroom;

-- 查看学生专业分布
SELECT major, COUNT(*) as 人数 
FROM student 
GROUP BY major 
ORDER BY 人数 DESC;

-- 查看班级人数分布
SELECT cg.name, cg.major, COUNT(s.id) as 实际人数
FROM class_group cg
LEFT JOIN student s ON cg.id = s.class_group_id
GROUP BY cg.id, cg.name, cg.major
ORDER BY cg.name;
```

## 🛠️ 故障排除

### 问题 1: 外键约束错误
**原因**: 尝试插入不存在的班级 ID  
**解决**: 确保先执行班级插入，再执行学生插入

### 问题 2: 学号重复错误
**原因**: 学号已存在  
**解决**: 
- 清空学生表：`TRUNCATE TABLE student;`
- 或修改学号生成起始值

### 问题 3: 存储过程权限不足
**原因**: MySQL 用户没有创建存储过程的权限  
**解决**: 
```sql
GRANT CREATE ROUTINE ON database_name.* TO 'your_user'@'localhost';
FLUSH PRIVILEGES;
```

## 📞 支持

如有问题，请联系开发者或查看项目文档。
