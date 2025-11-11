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

        boolean submitted = !logs.isEmpty(); // == “this day has been saved at least once”

        return DaySummaryDto.builder()
                .date(date)
                .tasks(taskDtos)
                .submitted(submitted)
                .build();
    }


    @Transactional
    public void saveDaySummary(User user, LocalDate date, List<TaskLogDto> taskDtos) {
        LocalDate today = LocalDate.now();

        if (date.isAfter(today)) {
            throw new IllegalStateException("Cannot submit future dates");
        }
        if (date.isBefore(today)) {
            throw new IllegalStateException("Past dates are read-only");
        }

        // At this point: date == today
        List<TaskLog> existingLogs = taskLogRepository.findByUserIdAndLogDate(user.getId(), date);
        boolean alreadySubmitted = !existingLogs.isEmpty();
        if (alreadySubmitted) {
            throw new IllegalStateException("This day's data has already been submitted and is read-only");
        }

        // First submission for today: create logs for each task
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
