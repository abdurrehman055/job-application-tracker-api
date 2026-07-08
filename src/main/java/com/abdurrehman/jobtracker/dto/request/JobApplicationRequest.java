package com.abdurrehman.jobtracker.dto.request;

import com.abdurrehman.jobtracker.entity.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record JobApplicationRequest(
        @NotBlank String companyName,
        @NotBlank String jobTitle,
        @NotNull JobStatus status,
        LocalDate applicationDate,
        String notes,
        String jobUrl
) {
}
