package com.example.ResumeATS.service;

import com.example.ResumeATS.model.Resume;
import com.example.ResumeATS.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    public Resume upload(MultipartFile file, String userId) throws Exception {
        resumeRepository.findAllByUserId(userId).forEach(resumeRepository::delete);

        Resource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        String extractedText = new TikaDocumentReader(resource).get()
                .stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        return resumeRepository.save(Resume.builder()
                .userId(userId)
                .filename(file.getOriginalFilename())
                .extractedText(extractedText)
                .build());
    }

    public void delete(UUID resumeId, String userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));

        if (!resume.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        resumeRepository.delete(resume);
    }

    public List<Resume> list(String userId) {
        return resumeRepository.findAllByUserId(userId);
    }

    public Resume getByUserId(String userId) {
        return resumeRepository.findAllByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No resume found. Please upload one first."));
    }
}