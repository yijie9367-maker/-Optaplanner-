package com.wj.aischedule.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "timeslot")
@Data
public class Timeslot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String semester;

    // 只要变量名写成 camelCase (小驼峰)，Spring 会自动对应数据库的下划线格式
    // 删掉重复的 @Column(name = "...")
    private Integer weekNumber;

    @Enumerated(EnumType.ORDINAL) // 对应数据库的整数类型
    private DayOfWeek dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private String displayName;
}