package com.aashdit.digiverifier.config.superadmin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aashdit.digiverifier.common.model.ServiceOutcome;
import com.aashdit.digiverifier.config.superadmin.Enum.ReportType;
import com.aashdit.digiverifier.config.superadmin.dto.CURReportDto;
import com.aashdit.digiverifier.config.superadmin.dto.ReportSearchDto;
import com.aashdit.digiverifier.config.superadmin.dto.VendorSearchDto;
import com.aashdit.digiverifier.config.superadmin.service.ReportService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/api/report")
public class ReportController {
	
	@Autowired
	private ReportService reportService;
	
//	@ApiOperation("Customer Utilization Report")
//	@RequestMapping(value = "/getCustomerUtilizationReport", method = { RequestMethod.GET, RequestMethod.POST })
//	public ResponseEntity<ServiceOutcome<ReportSearchDto>> getCustomerUtilizationReport(@RequestHeader("Authorization") String authorization,@RequestBody(required=false) ReportSearchDto reportSearchDto) {
//		ServiceOutcome<ReportSearchDto> svcSearchResult=  reportService.getCustomerUtilizationReportData(reportSearchDto);
//		return new ResponseEntity<ServiceOutcome<ReportSearchDto>>(svcSearchResult, HttpStatus.OK);
//	}
	
	@ApiOperation("Customer Utilization Report")
	@PostMapping("/getCustomerUtilizationReportByAgent")
	public ResponseEntity<ServiceOutcome<ReportSearchDto>> getCustomerUtilizationReportByAgent(@RequestHeader("Authorization") String authorization,@RequestBody ReportSearchDto reportSearchDto) {
		ServiceOutcome<ReportSearchDto> svcSearchResult=  reportService.getCustomerUtilizationReportByAgent(reportSearchDto);
		return new ResponseEntity<ServiceOutcome<ReportSearchDto>>(svcSearchResult, HttpStatus.OK);
	}
	
	@ApiOperation("Candidate Details By Status")
	@PostMapping("/getCanididateDetailsByStatus")
	public ResponseEntity<ServiceOutcome<ReportSearchDto>> getCanididateDetailsByStatus(@RequestHeader("Authorization") String authorization,@RequestBody ReportSearchDto reportSearchDto) {
		ServiceOutcome<ReportSearchDto> svcSearchResult=  reportService.getCanididateDetailsByStatus(reportSearchDto);
		return new ResponseEntity<ServiceOutcome<ReportSearchDto>>(svcSearchResult, HttpStatus.OK);
	}

//	@ApiOperation("E-KYC Report")
//	@RequestMapping(value = "/eKycReport", method = { RequestMethod.GET, RequestMethod.POST })
//	public ResponseEntity<ServiceOutcome<ReportSearchDto>> eKycReport(@RequestHeader("Authorization") String authorization,@RequestBody(required=false) ReportSearchDto reportSearchDto) {
//		ServiceOutcome<ReportSearchDto> svcSearchResult=  reportService.eKycReportData(reportSearchDto);
//		return new ResponseEntity<ServiceOutcome<ReportSearchDto>>(svcSearchResult, HttpStatus.OK);
//	}
	
	@GetMapping(value = "")
	public ResponseEntity getReport(@RequestParam("Authorization") String authorization,@RequestParam String candidateCode,@RequestParam
		ReportType type) {
		ServiceOutcome svcSearchResult = new ServiceOutcome();
		String url = reportService.generateDocument(candidateCode, authorization, type);
		svcSearchResult.setData(url);
		return new ResponseEntity<ServiceOutcome<ReportSearchDto>>(svcSearchResult, HttpStatus.OK);
	}
	
	
//	@GetMapping(value = "/getVendorUtilizationReport")
//	public ResponseEntity<ServiceOutcome<List<CURReportDto>>> getVendorUtilizationReport() {
//		ServiceOutcome<List<CURReportDto>> response = new ServiceOutcome<List<CURReportDto>>();
//		response = reportService.generateConventionalUtilizationReport(ReportSearchDto reportSearchDto);
//		return new ResponseEntity<ServiceOutcome<List<CURReportDto>>>(response, HttpStatus.OK);
//	}
	
    @ApiOperation("Vendor Utilization Report")
	@RequestMapping(value = "/getVendorUtilizationReport", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<ServiceOutcome<ReportSearchDto>> getVendorUtilizationReport(@RequestHeader("Authorization") String authorization,@RequestBody(required=false) ReportSearchDto reportSearchDto) {
		ServiceOutcome<ReportSearchDto> svcSearchResult=  reportService.generateConventionalUtilizationReport(reportSearchDto);
		return new ResponseEntity<ServiceOutcome<ReportSearchDto>>(svcSearchResult, HttpStatus.OK);
	}
    
//	@ApiOperation("Vendor Details By Status")
//	@PostMapping("/getVendorDetailsByStatus")
//	public ResponseEntity<ServiceOutcome<VendorSearchDto>> getVendorDetailsByStatus(@RequestHeader("Authorization") String authorization,@RequestBody VendorSearchDto reportSearchDto) {
//		ServiceOutcome<VendorSearchDto> svcSearchResult=  reportService.getVendorDetailsByStatus(reportSearchDto);
//		return new ResponseEntity<ServiceOutcome<VendorSearchDto>>(svcSearchResult, HttpStatus.OK);
//	}
	
	@ApiOperation("Vendor Details By Status")
	@PostMapping("/getVendorDetailsByStatus")
	public ResponseEntity<ServiceOutcome<List<CURReportDto>>> getVendorDetailsByStatus(@RequestHeader("Authorization") String authorization,@RequestBody VendorSearchDto vendorStatusmasterId) {
		ServiceOutcome<List<CURReportDto>> svcSearchResult=  reportService.getVendorDetailsByStatus(vendorStatusmasterId);
		return new ResponseEntity<ServiceOutcome<List<CURReportDto>>>(svcSearchResult, HttpStatus.OK);
	}
}
