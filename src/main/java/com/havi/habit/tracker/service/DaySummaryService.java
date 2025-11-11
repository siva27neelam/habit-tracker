package com.havi.habit.tracker.service;

import com.havi.habit.tracker.domain.Task;
import com.havi.habit.tracker.domain.TaskLog;
import com.havi.habit.tracker.domain.User;
import com.havi.habit.tracker.dto.DaySummaryDto;
import com.havi.habit.tracker.dto.TaskLogDto;
import com.havi.habit.tracker.repository.TaskLogRepository;
import com.havi.habit.tracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DaySummaryService {

    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;

    public DaySummaryDto getDaySummary(User user, LocalDate date) {
        List<Task> tasks = taskRepository.findByUserIdAndActiveTrueOrderByIdAsc(user.getId());
        List<TaskLog> logs = taskLogRepository.findByUserIdAndLogDate(user.getId(), date);

        Map<Long, TaskLog> logByTaskId = logs.stream()
                .collect(Collectors.toMap(l -> l.getTask().getId(), Function.identity()));

        List<TaskLogDto> taskDtos = tasks.stream()
                .map(task -> {
                    TaskLog log = logByTaskId.get(task.getId());
                    return TaskLogDto.builder()
                            .taskId(task.getId())
                            .name(task.getName())
                            .category(task.getCategory())
                            .done(log != null && log.isDone())
                            .startTime(log != null && log.getStartTime() != null
                                    ? log.getStartTime().toString()
                                    : null)
                            .endTime(log != null && log.getEndTime() != null
                                    ? log.getEndTime().toString()
                                    : null)
                            .notes(log != null ? log.getNotes() : null)
                            .build();
                })
                .toList();

        boolean submitted = !logs.isEmpty();

        LocalDate firstDate = taskLogRepository.findMinLogDateByUserId(user.getId());
        if (firstDate == null) {
            // no data yet: starting point is "today"
            firstDate = LocalDate.now();
        }

        return DaySummaryDto.builder()
                .date(date)
                .tasks(taskDtos)
                .submitted(submitted)
                .firstDate(firstDate)
                .build();
    }



    @Transactional
    public void saveDaySummary(User user, LocalDate date, List<TaskLogDto> taskDtos) {
        LocalDate today = LocalDate.now();

        // 1) Block future dates completely
        if (date.isAfter(today)) {
            throw new IllegalStateException("Cannot submit future dates");
        }

        // 2) If this date already has any logs → treat as submitted & read-only
        //    We simply NO-OP and return 200 OK. No error to the UI.
        List<TaskLog> existingLogs = taskLogRepository.findByUserIdAndLogDate(user.getId(), date);
        if (!existingLogs.isEmpty()) {
            return; // already submitted, ignore new payload
        }

        // 3) First submission for this date (today or a past date with no data)
        List<Task> tasks = taskRepository.findByUserIdAndActiveTrueOrderByIdAsc(user.getId());
        Map<Long, Task> taskById = tasks.stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));

        for (TaskLogDto dto : taskDtos) {
            if (dto.getTaskId() == null) continue;
            Task task = taskById.get(dto.getTaskId());
            if (task == null) continue;

            TaskLog log = TaskLog.builder()
                    .user(user)
                    .task(task)
                    .logDate(date)
                    .done(Boolean.TRUE.equals(dto.getDone()))
                    .startTime(parseTimeOrNull(dto.getStartTime()))
                    .endTime(parseTimeOrNull(dto.getEndTime()))
                    .notes(dto.getNotes())
                    .build();

            taskLogRepository.save(log);
        }
    }



    private LocalTime parseTimeOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalTime.parse(value);
    }

}
