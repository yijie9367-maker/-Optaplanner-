package com.wj.aischedule.service;

import com.wj.aischedule.entity.Classroom;
import com.wj.aischedule.repository.ClassroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepository;

    // 查询全部教室
    public List<Classroom> findAll() {
        return classroomRepository.findAll();
    }

    // 根据 ID 查询
    public Classroom findById(Long id) {
        return classroomRepository.findById(id).orElse(null);
    }

    // 新增 / 更新
    public Classroom save(Classroom classroom) {
        return classroomRepository.save(classroom);
    }

    // 删除
    public void deleteById(Long id) {
        classroomRepository.deleteById(id);
    }
}
