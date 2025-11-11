package com.havi.habit.tracker.controller;

import com.havi.habit.tracker.domain.Task;
import com.havi.habit.tracker.domain.User;
import com.havi.habit.tracker.dto.CreateTaskRequest;
import com.havi.habit.tracker.dto.TaskLogDto;
import com.havi.habit.tracker.repository.TaskRepository;
import com.havi.habit.tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{username}/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserService userService;

    @GetMapping
    public List<TaskLogDto> listTasks(@PathVariable String username) {
        User user = userService.getByUsername(username);
        return taskRepository.findByUserIdAndActiveTrueOrderByIdAsc(user.getId())
                .stream()
                .map(task -> TaskLogDto.builder()
                        .taskId(task.getId())
                        .name(task.getName())
                        .category(task.getCategory())
                        .build())
                .toList();
    }

    @PostMapping
    public TaskLogDto createTask(
            @PathVariable String username,
            @RequestBody CreateTaskRequest request) {

        User user = userService.getByUsername(username);

        Task task = Task.builder()
                .user(user)
                .name(request.getName())
                .category(request.getCategory())
                .isDefault(false)
                .active(true)
                .build();

        Task saved = taskRepository.save(task);

        return TaskLogDto.builder()
                .taskId(saved.getId())
                .name(saved.getName())
                .category(saved.getCategory())
                .build();
    }
}
