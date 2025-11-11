package com.havi.habit.tracker.controller;

import com.havi.habit.tracker.domain.User;
import com.havi.habit.tracker.dto.DaySummaryDto;
import com.havi.habit.tracker.service.DaySummaryService;
import com.havi.habit.tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users/{username}")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DaySummaryController {

    private final DaySummaryService daySummaryService;
    private final UserService userService;

    @GetMapping("/day/{date}")
    public DaySummaryDto getDay(
            @PathVariable String username,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User user = userService.getByUsername(username);
        return daySummaryService.getDaySummary(user, date);
    }

    @PutMapping("/day/{date}")
    public void saveDay(
            @PathVariable String username,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody DaySummaryDto body) {

        User user = userService.getByUsername(username);
        try {
            daySummaryService.saveDaySummary(user, date, body.getTasks());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
