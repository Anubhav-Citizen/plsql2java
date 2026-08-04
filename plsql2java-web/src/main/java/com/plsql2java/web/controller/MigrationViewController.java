package com.plsql2java.web.controller;

import com.plsql2java.web.service.MigrationJobRegistry;
import com.plsql2java.web.model.MigrationJobState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MigrationViewController {

    private final MigrationJobRegistry jobRegistry;

    public MigrationViewController(MigrationJobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/progress/{jobId}")
    public String progress(@PathVariable String jobId, Model model) {
        MigrationJobState state = jobRegistry.getOrThrow(jobId);
        model.addAttribute("jobId", jobId);
        model.addAttribute("status", state.getStatus().name());
        return "progress";
    }

    @GetMapping("/report/{jobId}")
    public String report(@PathVariable String jobId, Model model) {
        MigrationJobState state = jobRegistry.getOrThrow(jobId);
        model.addAttribute("jobId", jobId);
        if (state.getMigrationResult() != null && state.getMigrationResult().getMigrationReport() != null) {
            model.addAttribute("reportHtml", state.getMigrationResult().getMigrationReport().getHtmlContent());
        }
        return "report";
    }
}
