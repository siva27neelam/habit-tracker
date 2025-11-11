package com.havi.habit.tracker.repository;

import com.havi.habit.tracker.domain.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {

    List<TaskLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
}
