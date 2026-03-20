package com.example.ResumeATS.Controller;

import com.example.ResumeATS.dto.AnalysisResult;
import com.example.ResumeATS.dto.AnalyzeRequest;
import com.example.ResumeATS.model.Resume;
import com.example.ResumeATS.service.RateLimitService;
import com.example.ResumeATS.service.ResumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
public class ResumeController {


    private final ChatClient chatClient;
    private final ResumeService resumeService;
    private final RateLimitService rateLimitService;

    public ResumeController(ChatClient.Builder builder, ResumeService resumeService, RateLimitService rateLimitService) {
        this.chatClient = builder.build();
        this.resumeService = resumeService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/analyze")
    public AnalysisResult analyze(@RequestBody AnalyzeRequest request, Authentication auth) {
        rateLimitService.checkAnalyzeLimit(auth.getName());

        Resume resume = resumeService.getByUserId(auth.getName());
        String prompt = """
                You are an ATS resume analyzer. Compare the resume against the job description.
                Return a JSON object with:
                - score: integer 0-5
                - matchingSkills: array of strings
                - missingSkills: array of strings
                - suggestions: array of improvement suggestion strings

                Resume:
                %s

                Job Description:
                %s
                """.formatted(resume.getExtractedText(), request.jobDescription());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(AnalysisResult.class);
    }

    @PostMapping(value = "/resumes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Resume upload(@RequestParam("file") MultipartFile file, Authentication auth) throws Exception {
        return resumeService.upload(file, auth.getName());
    }

    @GetMapping("/resumes")
    public List<Resume> list(Authentication auth) {
        return resumeService.list(auth.getName());
    }

    @DeleteMapping("/resumes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Authentication auth) {
        resumeService.delete(id, auth.getName());
    }
}