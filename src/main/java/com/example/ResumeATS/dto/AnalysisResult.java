package com.example.ResumeATS.dto;

import java.util.List;

public record AnalysisResult(
        int score,
        List<String> matchingSkills,
        List<String> missingSkills,
        List<String> suggestions
) {}