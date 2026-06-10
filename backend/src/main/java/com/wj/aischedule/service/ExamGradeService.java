package com.wj.aischedule.service;

import com.wj.aischedule.dto.GradeItem;
import com.wj.aischedule.entity.ExamGrade;
import com.wj.aischedule.entity.Student;
import com.wj.aischedule.repository.ExamGradeRepository;
import com.wj.aischedule.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExamGradeService {

    @Autowired
    private ExamGradeRepository examGradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * 查询某场考试的所有成绩条目（含学生姓名）
     */
    public List<GradeItem> findGradeItemsByExamId(Long examId) {
        List<ExamGrade> grades = examGradeRepository.findByExamId(examId);
        List<GradeItem> items = new ArrayList<>();
        for (ExamGrade grade : grades) {
            GradeItem item = new GradeItem();
            item.setStudentId(grade.getStudentId());
            item.setScore(grade.getScore());
            Student student = studentRepository.findById(grade.getStudentId()).orElse(null);
            if (student != null) {
                item.setStudentName(student.getName());
            }
            items.add(item);
        }
        return items;
    }

    /**
     * 查询某个班级在某场考试中的所有学生（含已有成绩，没有则 score=null）
     */
    public List<GradeItem> findStudentsWithGradeByExamAndClass(Long examId, Long classGroupId) {
        List<Student> students = studentRepository.findByClassGroupId(classGroupId);
        List<GradeItem> items = new ArrayList<>();
        for (Student student : students) {
            GradeItem item = new GradeItem();
            item.setStudentId(student.getId());
            item.setStudentName(student.getName());
            Optional<ExamGrade> grade = examGradeRepository.findByExamIdAndStudentId(examId, student.getId());
            grade.ifPresent(g -> item.setScore(g.getScore()));
            items.add(item);
        }
        return items;
    }

    /**
     * 批量 upsert 成绩
     */
    @Transactional
    public void saveAll(Long examId, List<GradeItem> items) {
        for (GradeItem item : items) {
            if (item.getStudentId() == null) continue;
            Optional<ExamGrade> existing = examGradeRepository.findByExamIdAndStudentId(examId, item.getStudentId());
            ExamGrade grade = existing.orElse(new ExamGrade());
            grade.setExamId(examId);
            grade.setStudentId(item.getStudentId());
            grade.setScore(item.getScore());
            examGradeRepository.save(grade);
        }
    }

    /**
     * 查询学生自己的所有成绩（返回 GradeItem 列表，examId 字段可通过调用方关联）
     */
    public List<ExamGrade> findByStudentId(Long studentId) {
        return examGradeRepository.findByStudentId(studentId);
    }
}
