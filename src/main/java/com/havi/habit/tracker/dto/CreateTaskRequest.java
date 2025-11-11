package com.havi.habit.tracker.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CreateTaskRequest {
    private String name;
    private String category;
}
