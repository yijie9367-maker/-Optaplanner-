package com.wj.aischedule.service;

import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.repository.TeacherRepository;
import com.wj.aischedule.security.PasswordCodecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TeacherService {

    private static final String DEFAULT_TEACHER_PASSWORD = "123456";

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordCodecService passwordCodecService;

    // 查询所有教师
    public List<Teacher> findAll() {
        return teacherRepository.findAll();
    }

    // 根据 ID 查询
    public Teacher findById(Long id) {
        return teacherRepository.findById(id).orElse(null);
    }

    // 根据名字查询（用于登录）
    public Teacher findByName(String name) {
        return teacherRepository.findByName(name);
    }

    public Teacher authenticate(String name, String password) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(password)) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        Teacher teacher = teacherRepository.findByName(name);
        if (teacher == null) {
            throw new RuntimeException("教师不存在");
        }

        String storedPassword = teacher.getPassword();
        String comparablePassword = StringUtils.hasText(storedPassword) ? storedPassword : DEFAULT_TEACHER_PASSWORD;
        if (!passwordCodecService.matches(password, comparablePassword)) {
            throw new RuntimeException("密码错误");
        }

        if (!passwordCodecService.isEncoded(storedPassword)) {
            teacher.setPassword(passwordCodecService.encode(password));
            teacher = teacherRepository.save(teacher);
        }

        return teacher;
    }

    // 新增 / 更新
    public Teacher save(Teacher teacher) {
        Teacher existing = teacher.getId() == null ? null : teacherRepository.findById(teacher.getId()).orElse(null);
        if (!StringUtils.hasText(teacher.getPassword())) {
            if (existing != null && StringUtils.hasText(existing.getPassword())) {
                teacher.setPassword(existing.getPassword());
            } else {
                teacher.setPassword(passwordCodecService.encode(DEFAULT_TEACHER_PASSWORD));
            }
        } else {
            teacher.setPassword(passwordCodecService.encodeIfNecessary(teacher.getPassword()));
        }
        return teacherRepository.save(teacher);
    }

    // 删除
    public void deleteById(Long id) {
        teacherRepository.deleteById(id);
    }

    public Teacher updateMuteStatus(Long id, boolean muted) {
        Teacher teacher = findById(id);
        if (teacher == null) {
            throw new RuntimeException("教师不存在");
        }
        teacher.setIsMuted(muted);
        return teacherRepository.save(teacher);
    }
}
