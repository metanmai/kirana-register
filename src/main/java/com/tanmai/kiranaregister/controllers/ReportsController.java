package com.tanmai.kiranaregister.controllers;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/analytics")
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
    
    @GetMapping("/yearly")
    public String getYearlyReport() {
        return "Yearly Report";
    }
    
}
