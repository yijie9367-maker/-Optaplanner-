-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS aischedule CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aischedule;

-- 管理员用户表
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);

-- 教师表
CREATE TABLE IF NOT EXISTS teacher (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    title VARCHAR(50),
    department VARCHAR(100),
    password VARCHAR(100) DEFAULT '123456',
    is_muted BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    updated_at DATETIME
);

-- 班级表
CREATE TABLE IF NOT EXISTS class_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    grade VARCHAR(10),
    major VARCHAR(100),
    student_count INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME
);

-- 教室表
CREATE TABLE IF NOT EXISTS classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    building VARCHAR(100),
    capacity INT NOT NULL,
    type VARCHAR(50),
    created_at DATETIME,
    updated_at DATETIME
);

-- 课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20),
    credit DOUBLE,
    teacher_id BIGINT,
    max_hours INT,
    total_classes_needed INT DEFAULT 12,
    course_type VARCHAR(50) DEFAULT '专业课',
    description VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS class_group_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_group_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    semester VARCHAR(50) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT uk_class_group_course_semester UNIQUE (class_group_id, course_id, semester),
    FOREIGN KEY (class_group_id) REFERENCES class_group(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    student_id VARCHAR(20) UNIQUE,
    major VARCHAR(100),
    age INT,
    class_group_id BIGINT,
    is_muted BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (class_group_id) REFERENCES class_group(id) ON DELETE SET NULL
);

-- 排课表（原Schedule实体）
CREATE TABLE IF NOT EXISTS schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    class_group_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    teacher_id BIGINT,
    semester VARCHAR(50),
    week_number INT,
    week_day INT NOT NULL,
    time_slot INT NOT NULL,
    duration INT DEFAULT 2,
    is_conflict BOOLEAN DEFAULT FALSE,
    conflict_reason TEXT,
    score INT,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    FOREIGN KEY (class_group_id) REFERENCES class_group(id) ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
);

-- Optaplanner相关表
CREATE TABLE IF NOT EXISTS timeslot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timetable_id BIGINT,
    semester VARCHAR(50),
    week_number INT,
    day_of_week VARCHAR(20),
    start_time TIME,
    end_time TIME,
    display_name VARCHAR(200),
    FOREIGN KEY (timetable_id) REFERENCES timetable(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timetable_id BIGINT,
    name VARCHAR(50) NOT NULL,
    building VARCHAR(100),
    capacity INT NOT NULL,
    room_type VARCHAR(50),
    has_projector BOOLEAN DEFAULT FALSE,
    has_computer BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (timetable_id) REFERENCES timetable(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS lesson (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timetable_id BIGINT,
    course_code VARCHAR(20),
    course_name VARCHAR(100),
    credit INT,
    student_count INT,
    teacher_id BIGINT,
    teacher_name VARCHAR(50),
    class_group_id BIGINT,
    class_name VARCHAR(50),
    need_projector BOOLEAN DEFAULT FALSE,
    need_computer BOOLEAN DEFAULT FALSE,
    required_room_type VARCHAR(50),
    timeslot_id BIGINT,
    room_id BIGINT,
    has_conflict BOOLEAN DEFAULT FALSE,
    conflict_reason TEXT,
    duration INT DEFAULT 2,
    FOREIGN KEY (timetable_id) REFERENCES timetable(id) ON DELETE CASCADE,
    FOREIGN KEY (timeslot_id) REFERENCES timeslot(id) ON DELETE SET NULL,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS timetable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id VARCHAR(100),
    semester VARCHAR(50),
    description TEXT,
    solved BOOLEAN DEFAULT FALSE,
    solving_time BIGINT,
    iteration_count INT
);

-- 创建索引以提高查询性能
CREATE INDEX idx_schedule_course ON schedule(course_id);
CREATE INDEX idx_schedule_classroom ON schedule(classroom_id);
CREATE INDEX idx_schedule_teacher ON schedule(teacher_id);
CREATE INDEX idx_schedule_time ON schedule(week_day, time_slot);
CREATE INDEX idx_course_teacher ON course(teacher_id);
CREATE INDEX idx_student_class ON student(class_group_id);

-- 期末考试安排表
CREATE TABLE IF NOT EXISTS exam (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    class_group_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    exam_date DATE NOT NULL,
    start_time VARCHAR(10) NOT NULL,
    end_time VARCHAR(10) NOT NULL,
    location VARCHAR(100),
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    FOREIGN KEY (class_group_id) REFERENCES class_group(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE
);

-- 期末考试成绩表
CREATE TABLE IF NOT EXISTS exam_grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    score DECIMAL(5,2),
    created_at DATETIME,
    updated_at DATETIME,
    UNIQUE KEY uk_exam_student (exam_id, student_id),
    FOREIGN KEY (exam_id) REFERENCES exam(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE
);

-- 创建视图方便查询
CREATE VIEW IF NOT EXISTS v_course_schedule AS
SELECT 
    s.id,
    c.name as course_name,
    c.code as course_code,
    t.name as teacher_name,
    cg.name as class_name,
    cl.name as classroom_name,
    cl.building,
    s.week_day,
    s.time_slot,
    s.semester,
    s.week_number,
    s.is_conflict,
    s.conflict_reason
FROM schedule s
JOIN course c ON s.course_id = c.id
LEFT JOIN teacher t ON s.teacher_id = t.id
JOIN class_group cg ON s.class_group_id = cg.id
JOIN classroom cl ON s.classroom_id = cl.id;

-- 创建存储过程：检查排课冲突
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS check_schedule_conflicts()
BEGIN
    -- 检查教师时间冲突
    UPDATE schedule s1
    JOIN schedule s2 ON s1.teacher_id = s2.teacher_id 
        AND s1.week_day = s2.week_day 
        AND s1.time_slot = s2.time_slot
        AND s1.id != s2.id
    SET s1.is_conflict = TRUE,
        s1.conflict_reason = CONCAT('教师时间冲突: 与课程ID ', s2.course_id, ' 冲突');
    
    -- 检查教室时间冲突
    UPDATE schedule s1
    JOIN schedule s2 ON s1.classroom_id = s2.classroom_id 
        AND s1.week_day = s2.week_day 
        AND s1.time_slot = s2.time_slot
        AND s1.id != s2.id
    SET s1.is_conflict = TRUE,
        s1.conflict_reason = CONCAT('教室时间冲突: 与课程ID ', s2.course_id, ' 冲突');
    
    -- 检查班级时间冲突
    UPDATE schedule s1
    JOIN schedule s2 ON s1.class_group_id = s2.class_group_id 
        AND s1.week_day = s2.week_day 
        AND s1.time_slot = s2.time_slot
        AND s1.id != s2.id
    SET s1.is_conflict = TRUE,
        s1.conflict_reason = CONCAT('班级时间冲突: 与课程ID ', s2.course_id, ' 冲突');
    
    -- 检查教室容量
    UPDATE schedule s
    JOIN classroom c ON s.classroom_id = c.id
    JOIN course co ON s.course_id = co.id
    JOIN class_group cg ON s.class_group_id = cg.id
    WHERE c.capacity < cg.student_count
    SET s.is_conflict = TRUE,
        s.conflict_reason = CONCAT('教室容量不足: 需要', cg.student_count, '人，教室容量', c.capacity, '人');
END //
DELIMITER ;