package com.havi.habit.tracker.service;

import com.havi.habit.tracker.domain.Task;
import com.havi.habit.tracker.domain.User;
import com.havi.habit.tracker.repository.TaskRepository;
import com.havi.habit.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            // Ensure users exist
            User siva = userRepository.findByUsername("Siva").orElseGet(() ->
                    userRepository.save(User.builder()
                            .username("Siva")
                            .displayName("Siva")
                            .build())
            );

            User kalpana = userRepository.findByUsername("Kalpana").orElseGet(() ->
                    userRepository.save(User.builder()
                            .username("Kalpana")
                            .displayName("Kalpana")
                            .build())
            );

            seedDefaultTasksForUser(siva);
            seedDefaultTasksForUser(kalpana);
        };
    }

    private void seedDefaultTasksForUser(User user) {
        List<String> existingNames = taskRepository.findByUserIdAndActiveTrueOrderByIdAsc(user.getId())
                .stream()
                .map(Task::getName)
                .toList();

        addIfMissing(user, existingNames, "Reading", "Mind");
        addIfMissing(user, existingNames, "Exercise", "Body");
        addIfMissing(user, existingNames, "Skincare", "Self-care");
        addIfMissing(user, existingNames, "Cooking", "Home");
        addIfMissing(user, existingNames, "Diet", "Health");
    }

    private void addIfMissing(User user, List<String> existingNames, String name, String category) {
        if (existingNames.contains(name)) return;

        Task task = Task.builder()
                .user(user)
                .name(name)
                .category(category)
                .isDefault(true)
                .active(true)
                .build();

        taskRepository.save(task);
    }
}
