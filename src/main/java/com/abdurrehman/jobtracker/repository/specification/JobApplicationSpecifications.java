package com.abdurrehman.jobtracker.repository.specification;

import com.abdurrehman.jobtracker.entity.JobApplication;
import com.abdurrehman.jobtracker.entity.JobStatus;
import org.springframework.data.jpa.domain.Specification;

public final class JobApplicationSpecifications {

    private JobApplicationSpecifications() {
    }

    public static Specification<JobApplication> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<JobApplication> hasStatus(JobStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<JobApplication> matchesKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return (root, query, cb) -> null;
        }
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("companyName")), pattern),
                cb.like(cb.lower(root.get("jobTitle")), pattern)
        );
    }
}
