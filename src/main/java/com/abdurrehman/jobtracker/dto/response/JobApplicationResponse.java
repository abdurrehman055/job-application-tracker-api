package com.abdurrehman.jobtracker.dto.response;

import com.abdurrehman.jobtracker.entity.JobStatus;

import java.time.Instant;
import java.time.LocalDate;

public record JobApplicationResponse(
        Long id,
        String companyName,
        String jobTitle,
        JobStatus status,
        LocalDate applicationDate,
        String notes,
        String jobUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
