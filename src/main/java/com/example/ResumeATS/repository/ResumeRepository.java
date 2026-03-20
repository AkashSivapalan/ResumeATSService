package com.example.ResumeATS.repository;

import com.example.ResumeATS.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    List<Resume> findAllByUserId(String userId);
}