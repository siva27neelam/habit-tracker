package com.havi.habit.tracker.repository;

import com.havi.habit.tracker.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdAndActiveTrueOrderByIdAsc(Long userId);
}
