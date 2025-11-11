package com.havi.habit.tracker.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TaskLogDto {
    private Long taskId;
    private String name;
    private String category;
    private Boolean done;
    private String startTime; // "HH:mm" or null
    private String endTime;   // "HH:mm" or null
    private String notes;
}
