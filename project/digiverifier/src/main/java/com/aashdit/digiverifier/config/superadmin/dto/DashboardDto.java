package com.aashdit.digiverifier.config.superadmin.dto;

import java.util.List;

import com.aashdit.digiverifier.config.candidate.dto.CandidateDetailsDto;
import com.aashdit.digiverifier.config.candidate.dto.CandidateStatusCountDto;

import com.aashdit.digiverifier.vendorcheck.model.ConventionalVendorCandidatesSubmitted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDto {

    private String fromDate;

    private String toDate;

    private List<Long> organizationIds;

    private List<String> activityCode;

    private List<CandidateStatusCountDto> candidateStatusCountDto;

    private Long userId;

    private String status;

    private List<CandidateDetailsDto> candidateDtoList;

    private List<ConventionalVendorCandidatesSubmitted> conventionalVendorCandidatesSubmittedList;

    private Integer pageNumber;

    private String  totalPages;


    public DashboardDto(String fromDate, String toDate, List<Long> organizationIds, List<String> activityCode,
                        List<CandidateStatusCountDto> candidateStatusCountDto, Long userId, String status,
                        List<CandidateDetailsDto> candidateDtoList) {
        super();
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.organizationIds = organizationIds;
        this.activityCode = activityCode;
        this.candidateStatusCountDto = candidateStatusCountDto;
        this.userId = userId;
        this.status = status;
        this.candidateDtoList = candidateDtoList;
    }

    public DashboardDto(String fromDate, String toDate, List<Long> organizationIds,
                        List<String> activityCode, List<CandidateStatusCountDto> candidateStatusCountDto,
                        Long userId, String status, List<ConventionalVendorCandidatesSubmitted> conventionalVendorCandidatesSubmittedList, Integer pageNumber, String totalPages) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.organizationIds = organizationIds;
        this.activityCode = activityCode;
        this.candidateStatusCountDto = candidateStatusCountDto;
        this.userId = userId;
        this.status = status;
        this.conventionalVendorCandidatesSubmittedList = conventionalVendorCandidatesSubmittedList;
        this.pageNumber = pageNumber;
        this.totalPages = totalPages;
    }
}
