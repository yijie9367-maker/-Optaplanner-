package com.wj.aischedule.service;

import com.wj.aischedule.entity.Student;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.repository.ClassGroupRepository;
import com.wj.aischedule.repository.StudentRepository;
import com.wj.aischedule.security.PasswordCodecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StudentService {

    private static final String DEFAULT_STUDENT_PASSWORD = "123456";

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private PasswordCodecService passwordCodecService;

    // 查询所有学生
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    // 根据 ID 查询
    public Student findById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    // 新增 / 更新
    public Student save(Student student) {
        Student existing = student.getId() == null ? null : studentRepository.findById(student.getId()).orElse(null);

        if (student.getClassGroupId() == null) {
            throw new RuntimeException("请选择班级");
        }

        ClassGroup classGroup = classGroupRepository.findById(student.getClassGroupId())
                .orElseThrow(() -> new RuntimeException("所选班级不存在，请重新选择"));

        if ((student.getMajor() == null || student.getMajor().isBlank())
                && classGroup.getMajor() != null && !classGroup.getMajor().isBlank()) {
            student.setMajor(classGroup.getMajor());
        }

        if (!StringUtils.hasText(student.getPassword())) {
            if (existing != null && StringUtils.hasText(existing.getPassword())) {
                student.setPassword(existing.getPassword());
            } else {
                student.setPassword(passwordCodecService.encode(DEFAULT_STUDENT_PASSWORD));
            }
        } else {
            student.setPassword(passwordCodecService.encodeIfNecessary(student.getPassword()));
        }

        return studentRepository.save(student);
    }

    // 删除
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }
    
    // 根据班级ID查询学生
    public List<Student> findByClassGroupId(Long classGroupId) {
        return studentRepository.findByClassGroupId(classGroupId);
    }
    
    // 学生登录
    public Student authenticate(String name, String password) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(password)) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        Student student = studentRepository.findByName(name);
        if (student == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        String storedPassword = student.getPassword();
        String comparablePassword = StringUtils.hasText(storedPassword) ? storedPassword : DEFAULT_STUDENT_PASSWORD;
        if (!passwordCodecService.matches(password, comparablePassword)) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!passwordCodecService.isEncoded(storedPassword)) {
            student.setPassword(passwordCodecService.encode(password));
            student = studentRepository.save(student);
        }

        return student;
    }

    public Student updateMuteStatus(Long id, boolean muted) {
        Student student = findById(id);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        student.setIsMuted(muted);
        return studentRepository.save(student);
    }
}
