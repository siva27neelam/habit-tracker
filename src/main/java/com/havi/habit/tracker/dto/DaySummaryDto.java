package com.havi.habit.tracker.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DaySummaryDto {
    private LocalDate date;
    private List<TaskLogDto> tasks;
    private boolean submitted;

}
