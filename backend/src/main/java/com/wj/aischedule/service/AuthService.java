package com.wj.aischedule.service;

import com.wj.aischedule.entity.AdminUser;
import com.wj.aischedule.entity.ClassGroup;
import com.wj.aischedule.entity.Student;
import com.wj.aischedule.entity.Teacher;
import com.wj.aischedule.security.AppRole;
import com.wj.aischedule.security.AuthenticatedUser;
import com.wj.aischedule.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ClassGroupService classGroupService;

    @Autowired
    private JwtTokenService jwtTokenService;

    public Map<String, Object> loginAdmin(String username, String password) {
        AdminUser admin = adminUserService.authenticate(username, password);
        AuthenticatedUser user = new AuthenticatedUser(admin.getId(), admin.getUsername(), admin.getUsername(), AppRole.ADMIN, null);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("name", admin.getUsername());
        result.put("role", AppRole.ADMIN.getValue());
        result.put("token", jwtTokenService.generateToken(user));
        return result;
    }

    public Map<String, Object> loginTeacher(String name, String password) {
        Teacher teacher = teacherService.authenticate(name, password);
        AuthenticatedUser user = new AuthenticatedUser(teacher.getId(), teacher.getName(), teacher.getName(), AppRole.TEACHER, null);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", teacher.getId());
        result.put("name", teacher.getName());
        result.put("title", teacher.getTitle());
        result.put("department", teacher.getDepartment());
        result.put("isMuted", Boolean.TRUE.equals(teacher.getIsMuted()));
        result.put("role", AppRole.TEACHER.getValue());
        result.put("token", jwtTokenService.generateToken(user));
        return result;
    }

    public Map<String, Object> loginStudent(String name, String password) {
        Student student = studentService.authenticate(name, password);
        Map<String, Object> result = buildStudentProfile(student);
        AuthenticatedUser user = new AuthenticatedUser(
                student.getId(),
                student.getName(),
                student.getName(),
                AppRole.STUDENT,
                student.getClassGroupId());
        result.put("token", jwtTokenService.generateToken(user));
        return result;
    }

    public Map<String, Object> getCurrentUserProfile(AuthenticatedUser currentUser) {
        if (currentUser.getRole() == AppRole.ADMIN) {
            AdminUser admin = adminUserService.findById(currentUser.getUserId());
            if (admin == null) {
                throw new RuntimeException("管理员不存在");
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", admin.getId());
            result.put("username", admin.getUsername());
            result.put("name", admin.getUsername());
            result.put("role", AppRole.ADMIN.getValue());
            return result;
        }

        if (currentUser.getRole() == AppRole.TEACHER) {
            Teacher teacher = teacherService.findById(currentUser.getUserId());
            if (teacher == null) {
                throw new RuntimeException("教师不存在");
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", teacher.getId());
            result.put("name", teacher.getName());
            result.put("title", teacher.getTitle());
            result.put("department", teacher.getDepartment());
            result.put("isMuted", Boolean.TRUE.equals(teacher.getIsMuted()));
            result.put("role", AppRole.TEACHER.getValue());
            return result;
        }

        Student student = studentService.findById(currentUser.getUserId());
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        return buildStudentProfile(student);
    }

    private Map<String, Object> buildStudentProfile(Student student) {
        String className = "";
        if (student.getClassGroupId() != null) {
            ClassGroup classGroup = classGroupService.getClassGroupById(student.getClassGroupId());
            if (classGroup != null) {
                className = classGroup.getName();
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", student.getId());
        result.put("name", student.getName());
        result.put("email", student.getEmail());
        result.put("major", student.getMajor());
        result.put("classGroupId", student.getClassGroupId());
        result.put("className", className);
        result.put("isMuted", Boolean.TRUE.equals(student.getIsMuted()));
        result.put("role", AppRole.STUDENT.getValue());
        return result;
    }
}
