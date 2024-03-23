package com.tanmai.kiranaregister.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/reports")
public class ReportsController {

    public ReportsController() {}

    @GetMapping("/daily")
    public String getDailyReport() {
        return "Daily Report";
    }

    @GetMapping("/weekly")
    public String getWeeklyReport() {
        return "Weekly Report";
    }

    @GetMapping("/monthly")
    public String getMonthlyReport() {
        return "Monthly Report";
    }
}
