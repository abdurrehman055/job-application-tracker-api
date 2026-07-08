package com.abdurrehman.jobtracker.service;

import com.abdurrehman.jobtracker.dto.request.JobApplicationRequest;
import com.abdurrehman.jobtracker.dto.response.JobApplicationResponse;
import com.abdurrehman.jobtracker.entity.JobApplication;
import com.abdurrehman.jobtracker.entity.JobStatus;
import com.abdurrehman.jobtracker.entity.User;
import com.abdurrehman.jobtracker.exception.ResourceNotFoundException;
import com.abdurrehman.jobtracker.repository.JobApplicationRepository;
import com.abdurrehman.jobtracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.abdurrehman.jobtracker.repository.specification.JobApplicationSpecifications.belongsToUser;
import static com.abdurrehman.jobtracker.repository.specification.JobApplicationSpecifications.hasStatus;
import static com.abdurrehman.jobtracker.repository.specification.JobApplicationSpecifications.matchesKeyword;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository, UserRepository userRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public JobApplicationResponse create(Long userId, JobApplicationRequest request) {
        User user = userRepository.getReferenceById(userId);
        JobApplication jobApplication = JobApplication.builder()
                .companyName(request.companyName())
                .jobTitle(request.jobTitle())
                .status(request.status())
                .applicationDate(request.applicationDate())
                .notes(request.notes())
                .jobUrl(request.jobUrl())
                .user(user)
                .build();

        return toResponse(jobApplicationRepository.save(jobApplication));
    }

    public JobApplicationResponse getById(Long userId, Long id) {
        return toResponse(findOwnedOrThrow(userId, id));
    }

    @Transactional
    public JobApplicationResponse update(Long userId, Long id, JobApplicationRequest request) {
        JobApplication jobApplication = findOwnedOrThrow(userId, id);

        jobApplication.setCompanyName(request.companyName());
        jobApplication.setJobTitle(request.jobTitle());
        jobApplication.setStatus(request.status());
        jobApplication.setApplicationDate(request.applicationDate());
        jobApplication.setNotes(request.notes());
        jobApplication.setJobUrl(request.jobUrl());

        return toResponse(jobApplication);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        jobApplicationRepository.delete(findOwnedOrThrow(userId, id));
    }

    public Page<JobApplicationResponse> search(Long userId, String keyword, JobStatus status, Pageable pageable) {
        Specification<JobApplication> spec = belongsToUser(userId)
                .and(hasStatus(status))
                .and(matchesKeyword(keyword));

        return jobApplicationRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private JobApplication findOwnedOrThrow(Long userId, Long id) {
        JobApplication jobApplication = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found: " + id));

        if (!jobApplication.getUser().getId().equals(userId)) {
            // Deliberately the same 404 as "not found" - don't reveal that the
            // resource exists but belongs to someone else.
            throw new ResourceNotFoundException("Job application not found: " + id);
        }

        return jobApplication;
    }

    private JobApplicationResponse toResponse(JobApplication jobApplication) {
        return new JobApplicationResponse(
                jobApplication.getId(),
                jobApplication.getCompanyName(),
                jobApplication.getJobTitle(),
                jobApplication.getStatus(),
                jobApplication.getApplicationDate(),
                jobApplication.getNotes(),
                jobApplication.getJobUrl(),
                jobApplication.getCreatedAt(),
                jobApplication.getUpdatedAt()
        );
    }
}
