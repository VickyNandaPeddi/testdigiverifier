package com.aashdit.digiverifier.config.superadmin.service;

import java.util.List;

import com.aashdit.digiverifier.common.model.ServiceOutcome;
import com.aashdit.digiverifier.config.superadmin.Enum.ReportType;
import com.aashdit.digiverifier.config.superadmin.dto.CURReportDto;
import com.aashdit.digiverifier.config.superadmin.dto.ReportSearchDto;
import com.aashdit.digiverifier.config.superadmin.dto.VendorSearchDto;

public interface ReportService {

    ServiceOutcome<ReportSearchDto> getCustomerUtilizationReportData(ReportSearchDto reportSearchDto);

    ServiceOutcome<ReportSearchDto> getCustomerUtilizationReportByAgent(ReportSearchDto reportSearchDto);

    ServiceOutcome<ReportSearchDto> getCanididateDetailsByStatus(ReportSearchDto reportSearchDto);

    ServiceOutcome<ReportSearchDto> eKycReportData(ReportSearchDto reportSearchDto);

    String generateDocument(String candidateCode, String token, ReportType documentType);

    ServiceOutcome<ReportSearchDto> generateConventionalUtilizationReport(ReportSearchDto reportSearchDto);

	ServiceOutcome<List<CURReportDto>> getVendorDetailsByStatus(VendorSearchDto reportSearchDto);

//	ServiceOutcome<VendorSearchDto> getVendorDetailsByStatus(VendorSearchDto reportSearchDto);
}
