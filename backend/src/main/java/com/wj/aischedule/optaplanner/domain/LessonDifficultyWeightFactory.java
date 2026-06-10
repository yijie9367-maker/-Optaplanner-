package com.wj.aischedule.optaplanner.domain;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import java.util.Comparator;

/**
 * Lesson 难度权重工厂
 * 修复了相同权重导致报错的问题
 */
public class LessonDifficultyWeightFactory implements SelectionSorterWeightFactory<TimeTable, Lesson> {

    @Override
    public Comparable createSorterWeight(TimeTable timetable, Lesson lesson) {
        int weight = 0;

        // 1. 计算业务权重
        if (lesson.getStudentCount() != null) {
            weight += lesson.getStudentCount() / 10;
        }
        if (Boolean.TRUE.equals(lesson.getNeedProjector())) {
            weight += 5;
        }
        if (Boolean.TRUE.equals(lesson.getNeedComputer())) {
            weight += 5;
        }
        if (lesson.getCredit() != null) {
            weight += lesson.getCredit();
        }
        if (lesson.getDuration() != null) {
            weight += lesson.getDuration();
        }

        // 2. 返回一个复合比较对象
        // 即使 weight 相同，只要 id 不同，它们就不会被判定为“同一个选择”
        return new LessonDifficultyWeight(lesson, weight);
    }

    public static class LessonDifficultyWeight implements Comparable<LessonDifficultyWeight> {
        private static final Comparator<LessonDifficultyWeight> COMPARATOR = Comparator
                .comparingInt((LessonDifficultyWeight w) -> w.weight)
                .thenComparingLong(w -> w.lesson.getId()); // 【关键】权重一样时，按ID排序

        private final Lesson lesson;
        private final int weight;

        public LessonDifficultyWeight(Lesson lesson, int weight) {
            this.lesson = lesson;
            this.weight = weight;
        }

        @Override
        public int compareTo(LessonDifficultyWeight other) {
            return COMPARATOR.compare(this, other);
        }
    }
}