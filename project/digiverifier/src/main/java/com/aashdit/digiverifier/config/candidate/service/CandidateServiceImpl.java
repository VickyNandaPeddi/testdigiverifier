package com.aashdit.digiverifier.config.candidate.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.generic.DateTool;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.aashdit.digiverifier.client.securityDetails.EPFOSecurityConfig;
import com.aashdit.digiverifier.common.ContentRepository;
import com.aashdit.digiverifier.common.dto.ContentDTO;
import com.aashdit.digiverifier.common.dto.EpfoItrResponseDTO;
import com.aashdit.digiverifier.common.enums.ContentCategory;
import com.aashdit.digiverifier.common.enums.ContentSubCategory;
import com.aashdit.digiverifier.common.enums.ContentType;
import com.aashdit.digiverifier.common.enums.FileType;
import com.aashdit.digiverifier.common.model.Content;
import com.aashdit.digiverifier.common.model.ServiceOutcome;
import com.aashdit.digiverifier.common.service.ContentService;
import com.aashdit.digiverifier.common.util.RandomString;
import com.aashdit.digiverifier.config.admin.dto.UserDto;
import com.aashdit.digiverifier.config.admin.dto.VendorUploadChecksDto;
import com.aashdit.digiverifier.config.admin.model.User;
import com.aashdit.digiverifier.config.admin.model.VendorChecks;
import com.aashdit.digiverifier.config.admin.model.VendorUploadChecks;
import com.aashdit.digiverifier.config.admin.repository.RoleRepository;
import com.aashdit.digiverifier.config.admin.repository.UserRepository;
import com.aashdit.digiverifier.config.admin.repository.VendorChecksRepository;
import com.aashdit.digiverifier.config.admin.repository.VendorUploadChecksRepository;
import com.aashdit.digiverifier.config.candidate.dto.ApprovalStatusRemarkDto;
import com.aashdit.digiverifier.config.candidate.dto.BulkUanDTO;
import com.aashdit.digiverifier.config.candidate.dto.CandidateCafAddressDto;
import com.aashdit.digiverifier.config.candidate.dto.CandidateCafEducationDto;
import com.aashdit.digiverifier.config.candidate.dto.CandidateCafExperienceDto;
import com.aashdit.digiverifier.config.candidate.dto.CandidateCafRelationshipDto;
import com.aashdit.digiverifier.config.candidate.dto.CandidateCaseDetailsDTO;
import com.aashdit.digiverifier.config.candidate.dto.CandidateDetailsDto;
import com.aashdit.digiverifier.config.candidate.dto.CandidateFileDto;
import com.aashdit.digiverifier.config.candidate.dto.CandidateInvitationSentDto;
import com.aashdit.digiverifier.config.candidate.dto.CandidateStatusCountDto;
import com.aashdit.digiverifier.config.candidate.dto.CandidationApplicationFormDto;
import com.aashdit.digiverifier.config.candidate.dto.ContentFileDto;
import com.aashdit.digiverifier.config.candidate.dto.EmploymentDetailsDto;
import com.aashdit.digiverifier.config.candidate.dto.ExecutiveSummaryDto;
import com.aashdit.digiverifier.config.candidate.dto.IdItemsDto;
import com.aashdit.digiverifier.config.candidate.dto.SearchAllCandidateDTO;
import com.aashdit.digiverifier.config.candidate.dto.SuspectEmpMasterDto;
import com.aashdit.digiverifier.config.candidate.dto.UanSearchDashboardFilterDTO;
import com.aashdit.digiverifier.config.candidate.dto.UanSearchDataDTO;
import com.aashdit.digiverifier.config.candidate.dto.UanSearchEpfoDTO;
import com.aashdit.digiverifier.config.candidate.model.Candidate;
import com.aashdit.digiverifier.config.candidate.model.CandidateAddComments;
import com.aashdit.digiverifier.config.candidate.model.CandidateAdressVerification;
import com.aashdit.digiverifier.config.candidate.model.CandidateCafAddress;
import com.aashdit.digiverifier.config.candidate.model.CandidateCafEducation;
import com.aashdit.digiverifier.config.candidate.model.CandidateCafExperience;
import com.aashdit.digiverifier.config.candidate.model.CandidateCafRelationship;
import com.aashdit.digiverifier.config.candidate.model.CandidateCaseDetails;
import com.aashdit.digiverifier.config.candidate.model.CandidateEmailStatus;
import com.aashdit.digiverifier.config.candidate.model.CandidateIdItems;
import com.aashdit.digiverifier.config.candidate.model.CandidateResumeUpload;
import com.aashdit.digiverifier.config.candidate.model.CandidateSampleCsvXlsMaster;
import com.aashdit.digiverifier.config.candidate.model.CandidateStatus;
import com.aashdit.digiverifier.config.candidate.model.CandidateStatusHistory;
import com.aashdit.digiverifier.config.candidate.model.CandidateVerificationState;
import com.aashdit.digiverifier.config.candidate.model.OrganisationScope;
import com.aashdit.digiverifier.config.candidate.model.QualificationMaster;
import com.aashdit.digiverifier.config.candidate.model.RemarkMaster;
import com.aashdit.digiverifier.config.candidate.model.StatusMaster;
import com.aashdit.digiverifier.config.candidate.model.SuspectClgMaster;
import com.aashdit.digiverifier.config.candidate.model.SuspectEmpMaster;
import com.aashdit.digiverifier.config.candidate.model.UanSearchData;
import com.aashdit.digiverifier.config.candidate.repository.CandidateAddCommentRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateAdressVerificationRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateCafAddressRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateCafEducationRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateCafExperienceRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateCafRelationshipRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateCaseDetailsRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateEmailStatusRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateIdItemsRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateResumeUploadRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateSampleCsvXlsMasterRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateStatusHistoryRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateStatusRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateVerificationStateRepository;
import com.aashdit.digiverifier.config.candidate.repository.OrganisationScopeRepository;
import com.aashdit.digiverifier.config.candidate.repository.QualificationMasterRepository;
import com.aashdit.digiverifier.config.candidate.repository.RemarkMasterRepository;
import com.aashdit.digiverifier.config.candidate.repository.StatusMasterRepository;
import com.aashdit.digiverifier.config.candidate.repository.SuspectClgMasterRepository;
import com.aashdit.digiverifier.config.candidate.repository.SuspectEmpMasterRepository;
import com.aashdit.digiverifier.config.candidate.repository.UanSearchDataRepository;
import com.aashdit.digiverifier.config.candidate.util.CSVUtil;
import com.aashdit.digiverifier.config.candidate.util.ExcelUtil;
import com.aashdit.digiverifier.config.superadmin.Enum.ReportType;
import com.aashdit.digiverifier.config.superadmin.dto.DashboardDto;
import com.aashdit.digiverifier.config.superadmin.model.Color;
import com.aashdit.digiverifier.config.superadmin.model.Organization;
import com.aashdit.digiverifier.config.superadmin.model.OrganizationEmailTemplate;
import com.aashdit.digiverifier.config.superadmin.repository.ColorRepository;
import com.aashdit.digiverifier.config.superadmin.repository.OrganizationEmailTemplateRepository;
import com.aashdit.digiverifier.config.superadmin.repository.OrganizationRepository;
import com.aashdit.digiverifier.config.superadmin.repository.ServiceSourceMasterRepository;
import com.aashdit.digiverifier.config.superadmin.repository.ServiceTypeConfigRepository;
import com.aashdit.digiverifier.config.superadmin.repository.ToleranceConfigRepository;
import com.aashdit.digiverifier.config.superadmin.service.ReportService;
import com.aashdit.digiverifier.config.superadmin.service.SuperAdminDashboardServiceImpl;
import com.aashdit.digiverifier.constants.EPFOConstants;
import com.aashdit.digiverifier.epfo.dto.EpfoDataFromApiDto;
import com.aashdit.digiverifier.epfo.dto.EpfoDataFromDetailsDto;
import com.aashdit.digiverifier.epfo.dto.EpfoDetailsDto;
import com.aashdit.digiverifier.epfo.model.EpfoData;
import com.aashdit.digiverifier.epfo.remittance.dto.RemittanceDataFromApiDto;
import com.aashdit.digiverifier.epfo.remittance.model.RemittanceData;
import com.aashdit.digiverifier.epfo.remittance.repository.RemittanceRepository;
import com.aashdit.digiverifier.epfo.remittance.service.RemittanceServiceImpl;
import com.aashdit.digiverifier.epfo.repository.CandidateEPFOResponseRepository;
import com.aashdit.digiverifier.epfo.repository.EpfoDataRepository;
import com.aashdit.digiverifier.epfo.service.EpfoServiceImpl;
import com.aashdit.digiverifier.itr.dto.ITRDataFromApiDto;
import com.aashdit.digiverifier.itr.model.ITRData;
import com.aashdit.digiverifier.itr.repository.CanditateItrEpfoResponseRepository;
import com.aashdit.digiverifier.itr.repository.ITRDataRepository;
import com.aashdit.digiverifier.utils.ApplicationDateUtils;
import com.aashdit.digiverifier.utils.CommonUtils;
import com.aashdit.digiverifier.utils.CommonValidation;
import com.aashdit.digiverifier.utils.DateDifference;
import com.aashdit.digiverifier.utils.DateUtil;
import com.aashdit.digiverifier.utils.EmailSentTask;
import com.aashdit.digiverifier.utils.FileUtil;
import com.aashdit.digiverifier.utils.SecurityHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CandidateServiceImpl implements CandidateService, MessageSourceAware {

	SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
	
	private MessageSource messageSource;

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Autowired
	@Lazy
	private CandidateEPFOResponseRepository candidateEPFOResponseRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CandidateAddCommentRepository candidateAddCommentRepository;

	@Autowired
	private CandidateRepository candidateRepository;

	@Autowired
	private VendorChecksRepository vendorChecksRepository;

	@Autowired
	private VendorUploadChecksRepository vendorUploadChecksRepository;

	@Autowired
	private ContentRepository contentRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private CSVUtil cSVUtil;

	@Autowired
	private ExcelUtil excelUtil;

	@Autowired
	private CandidateStatusRepository candidateStatusRepository;

	@Autowired
	private CandidateStatusHistoryRepository candidateStatusHistoryRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private EmailSentTask emailSentTask;

	@Autowired
	private CommonValidation commonValidation;

	@Autowired
	private CandidateSampleCsvXlsMasterRepository candidateSampleCsvXlsMasterRepository;

	@Autowired
	private StatusMasterRepository statusMasterRepository;

	@Autowired
	private CandidateEmailStatusRepository candidateEmailStatusRepository;

	@Autowired
	private RemarkMasterRepository remarkMasterRepository;

	@Autowired
	private SuspectEmpMasterRepository suspectEmpMasterRepository;

	@Autowired
	private SuspectClgMasterRepository suspectClgMasterRepository;

	@Autowired
	private CandidateCafEducationRepository candidateCafEducationRepository;

	@Autowired
	private QualificationMasterRepository qualificationMasterRepository;

	@Autowired
	private CandidateCafExperienceRepository candidateCafExperienceRepository;

	@Autowired
	private CandidateCafAddressRepository candidateCafAddressRepository;

	@Autowired
	private CandidateResumeUploadRepository candidateResumeUploadRepository;

	@Autowired
	private ColorRepository colorRepository;

	@Autowired
	private CandidateCaseDetailsRepository candidateCaseDetailsRepository;

	@Autowired
	private ITRDataRepository itrDataRepository;

	@Autowired
	private CandidateCafRelationshipRepository candidateCafRelationshipRepository;

	@Autowired
	private CandidateAdressVerificationRepository candidateAdressVerificationRepository;

	@Autowired
	private CandidateIdItemsRepository candidateIdItemsRepository;

	@Autowired
	private ServiceTypeConfigRepository serviceTypeConfigRepository;

	@Autowired
	private ToleranceConfigRepository toleranceConfigRepository;

	@Autowired
	private CandidateService candidateService;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private EpfoDataRepository epfoDataRepository;

	@Autowired
	private CanditateItrEpfoResponseRepository canditateItrEpfoResponseRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ServiceSourceMasterRepository serviceSourceMasterRepository;

	@Autowired
	private CandidateVerificationStateRepository candidateVerificationStateRepository;

	@Autowired
	@Lazy
	private ReportService reportService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private OrganisationScopeRepository organisationScopeRepository;

	@Autowired
	private UanSearchDataRepository uanSearchDataRepository;

	@Autowired
	private EPFOSecurityConfig epfoSecurityConfig;

	@Autowired
	private EpfoServiceImpl epfoServiceImpl;
	
	@Autowired
	private SuperAdminDashboardServiceImpl superAdminDashboardServiceImpl;
	
	@Autowired
	private RemittanceServiceImpl remittanceServiceImpl;
	
	@Autowired
	private RemittanceRepository remittanceRepository;
	
	@Autowired
	private OrganizationEmailTemplateRepository organizationEmailTemplateRepository;
	

	public ServiceOutcome<Boolean> updateCandidateOrganisationScope(OrganisationScope organisationScope) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		OrganisationScope result = null;
		// CandidateCafExperience candidateCafExperience=null;
		User user = SecurityHelper.getCurrentUser();
		try {
			if (organisationScope.getOrgScopeId() != null) {
				Optional<OrganisationScope> orgScope = organisationScopeRepository
						.findById(organisationScope.getOrgScopeId());
				organisationScope.setOrgScopeId(orgScope.get().getOrgScopeId());
				result = organisationScopeRepository.save(organisationScope);
			} else {
				OrganisationScope exsitingOrgScope = organisationScopeRepository.findByCandidateId(organisationScope.getCandidateId());
				if(exsitingOrgScope != null)
					organisationScope.setOrgScopeId(exsitingOrgScope.getOrgScopeId());
				
				result = organisationScopeRepository.save(organisationScope);
			}

			if (result != null) {
				svcSearchResult.setMessage("Comment Update successfully.");

			} else {
				System.out.println("-------------candidate-----else------");
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
			}
		} catch (Exception e) {
			log.error("Exception occured in updateCandidateVendorProofColor method in CandidateServiceImpl-->", e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Transactional
	@Override
	public ServiceOutcome<List> saveCandidateInformation(MultipartFile file) {
		ServiceOutcome<List> svcSearchResult = new ServiceOutcome<List>();
		String originalFilename = file.getOriginalFilename();
		System.out.println("OrginalFileName::>>>"+originalFilename);
		try {
			User user = SecurityHelper.getCurrentUser();
			Organization organization = organizationRepository.findById(user.getOrganization().getOrganizationId()).get();
			RandomString rd = null;
			List<Candidate> candidates = null;
			List<CandidateStatus> candidateStatusList = new ArrayList<CandidateStatus>();
			CandidateSampleCsvXlsMaster candidateSampleCsvXlsMaster = null;
			if (CSVUtil.hasCSVFormat(file)) {
				candidates = cSVUtil.csvToCandidateList(file.getInputStream(),originalFilename, organization.getNoYearsToBeVerified());
				candidateSampleCsvXlsMaster = new CandidateSampleCsvXlsMaster();
				candidateSampleCsvXlsMaster.setCandidateSampleCsv(file.getBytes());
			}
			if (ExcelUtil.hasExcelFormat(file)) {
				candidates = excelUtil.excelToCandidate(file.getInputStream(),originalFilename, organization.getNoYearsToBeVerified());
				candidateSampleCsvXlsMaster = new CandidateSampleCsvXlsMaster();
				candidateSampleCsvXlsMaster.setCandidateSampleXls(file.getBytes());
			}
			for (Candidate candidate : candidates) {
				candidate.setOrganization(organization);
				rd = new RandomString(12);
				Candidate findByCandidateCode = candidateRepository.findByCandidateCode(rd.nextString());
				if (findByCandidateCode != null) {
					rd = new RandomString(12);
					candidate.setCandidateCode(rd.nextString());
				} else {
					candidate.setCandidateCode(rd.nextString());
				}
				candidate.setIsLoaAccepted(false);
				candidate.setApprovalRequired(false);
				candidate.setIsActive(true);
				candidate.setCreatedOn(new Date());
				candidate.setCreatedBy(user);
			}
			List<Candidate> candidateList = candidateRepository.saveAllAndFlush(candidates);
			if (!candidateList.isEmpty()) {
				candidateSampleCsvXlsMaster.setOrganization(
						organizationRepository.findById(user.getOrganization().getOrganizationId()).get());
				candidateSampleCsvXlsMaster.setUploadedTimestamp(new Date());
				candidateSampleCsvXlsMaster.setCreatedBy(user);
				candidateSampleCsvXlsMaster.setCreatedOn(new Date());
				CandidateSampleCsvXlsMaster result = candidateSampleCsvXlsMasterRepository
						.save(candidateSampleCsvXlsMaster);
				candidateList.forEach(candidateOBJ -> candidateOBJ.setCandidateSampleId(result));
				candidateRepository.saveAllAndFlush(candidateList);
				for (Candidate candidate : candidateList) {
					CandidateStatus candidateStatus = new CandidateStatus();
					candidateStatus.setCandidate(candidate);
					candidateStatus.setCreatedBy(user);
					candidateStatus.setCreatedOn(new Date());
					if (!candidate.getCcEmailId().isEmpty()) {
						if (commonValidation.validationEmail(candidate.getEmailId())) {
							candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("NEWUPLOAD"));
						} else {
							candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("INVALIDUPLOAD"));
							candidateStatus.setLastUpdatedOn(new Date());
							candidateStatus.setLastUpdatedBy(user);
						}

					} else {
						if (commonValidation.validationEmail(candidate.getEmailId())) {
							candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("NEWUPLOAD"));
						} else {
							candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("INVALIDUPLOAD"));
							candidateStatus.setLastUpdatedOn(new Date());
							candidateStatus.setLastUpdatedBy(user);
						}
					}
					candidateStatus = candidateStatusRepository.save(candidateStatus);
					candidateStatusList.add(candidateStatus);
					createCandidateStatusHistory(candidateStatus, "NOTCANDIDATE");

				}
				List<String> referenceList = candidateStatusList.stream()
						.filter(c -> c.getStatusMaster().getStatusCode().equals("NEWUPLOAD"))
						.map(x -> x.getCandidate().getCandidateCode()).collect(Collectors.toList());
				CandidateInvitationSentDto candidateInvitationSentDto = new CandidateInvitationSentDto();
				candidateInvitationSentDto.setCandidateReferenceNo(referenceList);
				System.out.println(referenceList + "referenceList");
				candidateInvitationSentDto.setStatuscode("INVITATIONSENT");
				ServiceOutcome<Boolean> svcOutcome = candidateService.invitationSent(candidateInvitationSentDto);
				if (svcOutcome.getOutcome()) {
					svcSearchResult.setData(referenceList);
					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage("File Uploaded Successfully");
				} else {
					svcSearchResult.setData(referenceList);
					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage(svcOutcome.getMessage());
				}
			}
			else if(candidateList.isEmpty()) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage(originalFilename+" - filename already exists OR the file content is invalid.");
			}
			else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("File Could not be Uploaded.");
			}
		} catch (IOException e) {
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("File Could not be Uploaded.");
			log.error("Exception occured in saveCandidateInformation method in CandidateServiceImpl-->" + e);
			throw new RuntimeException("fail to store csv/xls data: " + e.getMessage());
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<DashboardDto> getAllCandidateList(DashboardDto dashboardDto) {
		User currentUser = SecurityHelper.getCurrentUser();
		log.info("current user-User Id -"+currentUser.getUserId());
		ServiceOutcome<DashboardDto> svcSearchResult = new ServiceOutcome<DashboardDto>();
		List<Candidate> candidateList = new ArrayList<Candidate>();
		List<CandidateDetailsDto> candidateDtoList = new ArrayList<CandidateDetailsDto>();
		List<String> statusCodes = new ArrayList<String>();
		List<Long> agentIds = new ArrayList<Long>();
		String strToDate = "";
		String strFromDate = "";
		try {
			if (currentUser.getUserId() != null && currentUser.getUserId() != 0l
					&& StringUtils.isNotBlank(dashboardDto.getStatus())) {
//				log.info("PAGE NUMBER::{}",dashboardDto.getPageNumber());
				Pageable pageable =null;
				if(dashboardDto.getPageNumber()!=null) {
					pageable = PageRequest.of(dashboardDto.getPageNumber(), 10);
				}
				User user = userRepository.findById(currentUser.getUserId()).get();
				strToDate = dashboardDto.getToDate() != null ? dashboardDto.getToDate()
						: ApplicationDateUtils.getStringTodayAsDDMMYYYY();
				strFromDate = dashboardDto.getFromDate() != null ? dashboardDto.getFromDate()
						: ApplicationDateUtils.subtractNoOfDaysFromDateAsDDMMYYYY(
								new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);
				Date startDate = formatter.parse(strFromDate + " 00:00:00");
				Date endDate = formatter.parse(strToDate + " 23:59:59");
				String status = dashboardDto.getStatus();
				if (status.equals("PENDINGAPPROVAL")) {	
					status = "PENDINGAPPROVAL";
					statusCodes.add(0, status);

//					status = "INTERIMREPORT";
//					statusCodes.add(0, status);
				}
				if (status.equals("INTERIMREPORT")) {
					status = "INTERIMREPORT";
					statusCodes.add(0, status);
				} else if (status.equals("CAFPENDING")) {
					status = "RELATIVEADDRESS";
					statusCodes.add(0, status);
				} else if (status.equals("EPFOSKIPPED")) {
					Collections.addAll(statusCodes, "ITR", "DIGILOCKER");
				} else if (status.equals("NEWUPLOAD")) {
					statusCodes.addAll(statusMasterRepository.findAll().parallelStream().map(x -> x.getStatusCode())
							.collect(Collectors.toList()));
				} else {
					statusCodes.add(0, status);
				}
				List<StatusMaster> statusMasterList = statusMasterRepository.findByStatusCodeIn(statusCodes);
				List<Long> statusIds = statusMasterList.stream().map(x -> x.getStatusMasterId())
						.collect(Collectors.toList());
				if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_ADMIN")
						|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_PARTNERADMIN")) {
					if(status.equals("NEWUPLOAD")){
						//System.out.println("IF NEWUPLOAD");
						//System.out.println("STATUS ID::: "+statusIds);
				    	if(pageable!=null) {
				    		candidateList = candidateRepository.getPageCandidateListByOrganizationIdAndStatusAndCreatedOn(
									user.getOrganization().getOrganizationId(), statusIds, startDate, endDate,pageable);
						
				    	}else {
					     candidateList = candidateRepository.getCandidateListByOrganizationIdAndStatusAndCreatedOn(
							user.getOrganization().getOrganizationId(), statusIds, startDate, endDate);
				        }
						//System.out.println("CANIDATELIST IN NEWUPLOAD::"+candidateList.size());
					}else {
						//System.out.println("Now getting pagewise QC");
						if(pageable!=null) {
							candidateList = candidateRepository.getPageCandidateListByOrganizationIdAndStatusAndLastUpdated(
									user.getOrganization().getOrganizationId(), statusIds, startDate, endDate,pageable);
//							log.info("PAGINATION CANDIDATE LIST SIZE::{}",candidateList.size());
						}else {

						    candidateList = candidateRepository.getCandidateListByOrganizationIdAndStatusAndLastUpdated(
								user.getOrganization().getOrganizationId(), statusIds, startDate, endDate);
						}
					}
					
				}
				if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTSUPERVISOR")
						|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTHR")) {
					List<User> agentList = userRepository.findAllByAgentSupervisorUserId(user.getUserId());
					if (!agentList.isEmpty()) {
						agentIds = agentList.stream().map(x -> x.getUserId()).collect(Collectors.toList());
					}
					agentIds.add(user.getUserId());
					
					if(pageable!=null) {
						candidateList = candidateRepository.getPageCandidateListByUserIdAndStatusAndLastUpdated(agentIds,
								statusIds, startDate, endDate,pageable);
						log.info("PAGINATION CANDIDATE LIST SIZE::{}",candidateList.size());
					}else {
					  candidateList = candidateRepository.getCandidateListByUserIdAndStatusAndLastUpdated(agentIds,
							statusIds, startDate, endDate);
					}
				}
				if (dashboardDto.getStatus().equals("EPFOSKIPPED")) {
					candidateList = candidateList.parallelStream().filter(x -> x.getIsUanSkipped())
							.collect(Collectors.toList());
				}
				if (dashboardDto.getStatus().equals("DIGILOCKER") || dashboardDto.getStatus().equals("ITR")) {
					candidateList = candidateList.parallelStream()
							.filter(x -> x.getIsUanSkipped() != null ? !x.getIsUanSkipped() : true)
							.collect(Collectors.toList());
				}
				for (Candidate candidate : candidateList) {
					CandidateDetailsDto candidateDto = this.modelMapper.map(candidate, CandidateDetailsDto.class);
					candidateDto.setCreatedOn(formatter.format(candidate.getCreatedOn()));
					candidateDto.setSubmittedOn(
							candidate.getSubmittedOn() != null ? formatter.format(candidate.getSubmittedOn()) : null);
					CandidateEmailStatus candidateEmailStatus = candidateEmailStatusRepository
							.findByCandidateCandidateCode(candidate.getCandidateCode());
					if (candidateEmailStatus != null) {
						candidateDto.setDateOfEmailInvite(candidateEmailStatus.getDateOfEmailInvite() != null
								? formatter.format(candidateEmailStatus.getDateOfEmailInvite())
								: null);
						candidateDto.setDateOfEmailFailure(candidateEmailStatus.getDateOfEmailFailure() != null
								? formatter.format(candidateEmailStatus.getDateOfEmailFailure())
								: null);
						candidateDto.setDateOfEmailExpire(candidateEmailStatus.getDateOfEmailExpire() != null
								? formatter.format(candidateEmailStatus.getDateOfEmailExpire())
								: null);
						candidateDto.setDateOfEmailReInvite(candidateEmailStatus.getDateOfEmailReInvite() != null
								? formatter.format(candidateEmailStatus.getDateOfEmailReInvite())
								: null);
					}
					CandidateStatus candidateStatus = candidateStatusRepository
							.findByCandidateCandidateCode(candidate.getCandidateCode());
					Boolean uan = candidate.getIsUanSkipped() != null ? candidate.getIsUanSkipped() : false;
					if (candidateStatus.getStatusMaster().getStatusCode().equals("DIGILOCKER") && uan
							|| candidateStatus.getStatusMaster().getStatusCode().equals("ITR") && uan) {
						candidateDto.setCandidateStatusName("EPFO Skipped");
					} else {
						candidateDto.setCandidateStatusName(candidateStatus.getStatusMaster().getStatusName());
					}
					candidateDto.setLastUploadedOn(candidateStatus.getLastUpdatedOn());
					List<ContentDTO> contentDTOList = contentService
							.getContentListByCandidateId(candidate.getCandidateId());
//					System.out.println(contentDTOList + "--------contentdtolist-------");
					candidateDto.setContentDTOList(contentDTOList);
					
					CandidateVerificationState updateVerificationStatus = candidateVerificationStateRepository.findByCandidateCandidateId(candidate.getCandidateId());
					if (updateVerificationStatus != null) {
						candidateDto.setPreOfferVerificationColorCode(updateVerificationStatus.getPreApprovalColorCodeStatus());
						candidateDto.setInterimVerificationColorCode(updateVerificationStatus.getInterimColorCodeStatus());
						candidateDto.setFinalVerificationColorCode(updateVerificationStatus.getFinalColorCodeStatus());
						
						//adding report delivered dates
						candidateDto.setPreOfferReportDate(
								updateVerificationStatus.getPreApprovalTime() != null ? formatter.format(Date.from(updateVerificationStatus.getPreApprovalTime().toInstant())) : null);
						candidateDto.setInterimReportDate(
								updateVerificationStatus.getInterimReportTime() != null ? formatter.format(Date.from(updateVerificationStatus.getInterimReportTime().toInstant())) : null);
						candidateDto.setFinalReportDate(
								updateVerificationStatus.getFinalReportTime() != null ? formatter.format(Date.from(updateVerificationStatus.getFinalReportTime().toInstant())) : null);
						candidateDto.setCaseInitiationDate(
								updateVerificationStatus.getCaseInitiationTime() != null ? formatter.format(Date.from(updateVerificationStatus.getCaseInitiationTime().toInstant())) : null);
					}
					candidateDtoList.add(candidateDto);

				}
				
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
				Collections.sort(candidateDtoList, (s1, s2) -> {
					try { 
//						LocalDate date1 = LocalDate.parse(s1.getSubmittedOn() != null ? s1.getSubmittedOn() : s1.getCreatedOn(), formatter);
//						LocalDate date2 = LocalDate.parse(s2.getSubmittedOn() != null ? s2.getSubmittedOn() : s2.getCreatedOn(), formatter);
//						return date1.compareTo(date2);
						LocalDateTime dateTime1 = LocalDateTime.parse(
							    s1.getSubmittedOn() != null ? s1.getSubmittedOn() : s1.getCreatedOn(), formatter
							);
							LocalDateTime dateTime2 = LocalDateTime.parse(
							    s2.getSubmittedOn() != null ? s2.getSubmittedOn() : s2.getCreatedOn(), formatter
							);
							return dateTime2.compareTo(dateTime1);
					} catch (DateTimeParseException e) {
						log.error("Exception occured in getAllCandidateList method in CandidateServiceImpl-->", e);
						return 0; // Handle parsing error, e.g., consider them equal
					}
				}); 
				
				DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null, null,
						dashboardDto.getUserId(), dashboardDto.getStatus(), candidateDtoList,dashboardDto.getPageNumber());
				if (!candidateDtoList.isEmpty()) {
					svcSearchResult.setData(dashboardDtoObj);
					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage("Candidate list fetched successfully.");
					svcSearchResult.setStatus(status);
				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult.setMessage("NO Candidate FOUND");
				}
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("please specify user.");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getAllCandidateList method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Transactional
	@Override
	public ServiceOutcome<Boolean> invitationSent(CandidateInvitationSentDto candidateInvitationSentDto) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		try {
			User user = SecurityHelper.getCurrentUser();
			CandidateStatus candidateStatus = null;
			CandidateEmailStatus candidateEmailStatus = null;
			CandidateStatus candidateStatusresult = null;
			if (candidateInvitationSentDto.getCandidateReferenceNo().size() > 0) {
				for (int i = 0; i < candidateInvitationSentDto.getCandidateReferenceNo().size(); i++) {
					candidateStatus = candidateStatusRepository
							.findByCandidateCandidateCode(candidateInvitationSentDto.getCandidateReferenceNo().get(i));
					if (candidateStatus != null) {
						Boolean result = emailSentTask.sendEmail(candidateStatus.getCandidate().getCandidateCode(),
								candidateStatus.getCandidate().getCandidateName(),
								candidateStatus.getCandidate().getEmailId(),
								candidateStatus.getCandidate().getCcEmailId());
						candidateEmailStatus = candidateEmailStatusRepository
								.findByCandidateCandidateCode(candidateStatus.getCandidate().getCandidateCode());
						if (candidateEmailStatus == null) {
							candidateEmailStatus = new CandidateEmailStatus();
							candidateEmailStatus.setCreatedBy(user);
							candidateEmailStatus.setCreatedOn(new Date());
							candidateEmailStatus.setCandidate(candidateStatus.getCandidate());
						} else {
							candidateEmailStatus.setLastUpdatedBy(user);
							candidateEmailStatus.setLastUpdatedOn(new Date());
						}
						if (result && candidateInvitationSentDto.getStatuscode().equalsIgnoreCase("INVITATIONSENT")) {
							candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("INVITATIONSENT"));
							candidateEmailStatus.setDateOfEmailInvite(new Date());
						} else if (result && candidateInvitationSentDto.getStatuscode().equalsIgnoreCase("REINVITE")) {
							
							//Start changes for get back to candidate on its previous state
							List<CandidateStatusHistory> candidateStatusHistories=candidateStatusHistoryRepository.findAllByCandidateCandidateId(candidateStatus.getCandidate().getCandidateId());
							
							if("INVITATIONEXPIRED".equals(candidateStatusHistories.get(candidateStatusHistories.size()-1).getStatusMaster().getStatusCode())){
								candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("REINVITE"));
							}else {
								candidateStatus.setStatusMaster(candidateStatusHistories.get(candidateStatusHistories.size()-1).getStatusMaster());
							}
							//end
							//candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("REINVITE"));
							candidateEmailStatus.setDateOfEmailReInvite(new Date());
						} else {
							candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("INVALIDUPLOAD"));
							candidateEmailStatus.setDateOfEmailFailure(new Date());
						}
						candidateStatus.setLastUpdatedBy(user);
						candidateStatus.setLastUpdatedOn(new Date());
						candidateStatusresult = candidateStatusRepository.save(candidateStatus);
						createCandidateStatusHistory(candidateStatusresult, "NOTCANDIDATE");
						candidateEmailStatus.setCandidateStatus(candidateStatusresult);
						candidateEmailStatusRepository.save(candidateEmailStatus);
					}
				}
				svcSearchResult.setData(true);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("Invitations sent successfully.");
			} else {
				svcSearchResult.setData(false);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("No records found to send mail.");
			}
		} catch (Exception ex) {
			log.error("Exception occured in invitationSent method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<CandidateDetailsDto> updateCandidate(CandidateDetailsDto candidateDetails) {
		ServiceOutcome<CandidateDetailsDto> svcSearchResult = new ServiceOutcome<CandidateDetailsDto>();
		CandidateDetailsDto candidateDetailsDto = new CandidateDetailsDto();
		try {
			User user = SecurityHelper.getCurrentUser();
			Candidate result = null;
			CandidateEmailStatus candidateEmailStatus = candidateEmailStatusRepository.findByCandidateCandidateCode(candidateDetails.getCandidateCode());
			if (StringUtils.isNotBlank(candidateDetails.getCandidateCode())) {
				Candidate candidate = candidateRepository
						.findByCandidateCode(candidateDetails.getCandidateCode().trim());
				if (candidate != null) {
					candidate.setCandidateName(candidateDetails.getCandidateName());
					candidate.setEmailId(candidateDetails.getEmailId());
					candidate.setContactNumber(candidateDetails.getContactNumber());
					candidate.setCcEmailId(candidateDetails.getCcEmailId());
					candidate.setApplicantId(candidateDetails.getApplicantId());
					candidate.setLastUpdatedBy(user);
					candidate.setLastUpdatedOn(new Date());
					result = candidateRepository.save(candidate);
					boolean sendMail = emailSentTask.sendEmail(candidateDetails.getCandidateCode(), candidate.getCandidateName(),
							candidate.getEmailId(), candidate.getCcEmailId());
					CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(candidateDetails.getCandidateCode());
					if(sendMail) {
						candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("INVITATIONSENT"));
						if (candidateEmailStatus == null) {
							candidateEmailStatus = new CandidateEmailStatus();
							candidateEmailStatus.setCreatedBy(user);
							candidateEmailStatus.setCreatedOn(new Date());
							candidateEmailStatus.setCandidate(candidateStatus.getCandidate());
							candidateEmailStatus.setDateOfEmailInvite(new Date());
							candidateEmailStatusRepository.save(candidateEmailStatus);

						}
						candidateStatusRepository.save(candidateStatus);
						createCandidateStatusHistory(candidateStatus, "NOTCANDIDATE");
					}					
					BeanUtils.copyProperties(result, candidateDetailsDto);
					svcSearchResult.setData(candidateDetailsDto);
					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage("Candidate information Updated successfully");
				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult.setMessage("NO CANDIDATE FOUND FOR THIS REFERENCE NO");
				}
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("PLEASE SPECIFY CANDIDATE REFERENCE NUMBER");
			}
		} catch (Exception ex) {
			log.error("Exception occured in updateCandidate method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	public ServiceOutcome<List<CandidateStatus>> getCandidateStatusList(String strToDate, String strFromDate,
			Long userId) {
		ServiceOutcome<List<CandidateStatus>> svcSearchResult = new ServiceOutcome<List<CandidateStatus>>();
		List<CandidateStatus> candidateStatusList = new ArrayList<CandidateStatus>();
		List<Long> agentIds = new ArrayList<Long>();
		try {
			Date startDate = formatter.parse(strFromDate + " 00:00:00");
			Date endDate = formatter.parse(strToDate + " 23:59:59");
			User user = userRepository.findById(userId).get();
			if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_ADMIN")
					|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_PARTNERADMIN")) {
				candidateStatusList = candidateStatusRepository
						.findAllByCandidateOrganizationOrganizationIdAndLastUpdatedOnBetween(
								user.getOrganization().getOrganizationId(), startDate, endDate);
			}
			if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTSUPERVISOR")
					|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTHR")) {
				List<User> agentList = userRepository.findAllByAgentSupervisorUserId(user.getUserId());
				if (!agentList.isEmpty()) {
					agentIds = agentList.stream().map(x -> x.getUserId()).collect(Collectors.toList());
				}
				agentIds.add(user.getUserId());
				candidateStatusList = candidateStatusRepository
						.findAllByCandidateCreatedByUserIdInAndLastUpdatedOnBetween(agentIds, startDate, endDate);
			}
			svcSearchResult.setData(candidateStatusList);
			svcSearchResult.setOutcome(true);
			svcSearchResult.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
		} catch (Exception ex) {
			log.error("Exception occured in getCandidateStatusList method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<DashboardDto> getCandidateStatusAndCount(DashboardDto dashboardDto) {
		ServiceOutcome<DashboardDto> svcSearchResult = new ServiceOutcome<DashboardDto>();
		List<CandidateStatusCountDto> candidateStatusCountDtoList = new ArrayList<CandidateStatusCountDto>();
		List<CandidateStatus> candidateStatusList = null;
		String strToDate = "";
		String strFromDate = "";
		try {
			if (dashboardDto.getUserId() != null && dashboardDto.getUserId() != 0l) {
				strToDate = dashboardDto.getToDate() != null ? dashboardDto.getToDate()
						: ApplicationDateUtils.getStringTodayAsDDMMYYYY();
				strFromDate = dashboardDto.getFromDate() != null ? dashboardDto.getFromDate()
						: ApplicationDateUtils.subtractNoOfDaysFromDateAsDDMMYYYY(
								new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);
				
				//calling the function to get counts for dashboard
				int newUploadCount =0;
				int invitationexpiredCount =0;
				int invaliduploadCount =0;
				int reinviteCount =0;
				List<Object[]> activityList =getCountsForDashboard(strToDate, strFromDate,dashboardDto.getUserId());
				for(Object[] activity : activityList) {
//					log.info("NEWUPLOAD::{}",Integer.parseInt(activity[0].toString()));
//					log.info("INVALIDUPLOAD::{}",Integer.parseInt(activity[1].toString()));
//					log.info("REINVITE::{}",Integer.parseInt(activity[2].toString()));
//					
//					log.info("INVITATIONEXPIRED::{}",Integer.parseInt(activity[6].toString()));
					
					newUploadCount =Integer.parseInt(activity[0].toString());
					invitationexpiredCount =Integer.parseInt(activity[6].toString());
					invaliduploadCount =Integer.parseInt(activity[1].toString());
					reinviteCount=Integer.parseInt(activity[2].toString());
				}
				
//				ServiceOutcome<List<CandidateStatus>> svcOutCome = getCandidateStatusList(strToDate, strFromDate,
//						dashboardDto.getUserId());
//				candidateStatusList = svcOutCome.getData();

				// List<CandidateStatus> invitationSentList = candidateStatusList != null
				// ?candidateStatusList.stream().filter(c ->
				// c.getStatusMaster().getStatusCode().equals("INVITATIONSENT")).collect(Collectors.toList())
				// : null;
//				List<CandidateStatus> invitationexpiredList = candidateStatusList != null ? candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("INVITATIONEXPIRED"))
//						.collect(Collectors.toList()) : null;
//				log.info("invitationexpiredList FROM LIST COUNT::{}",invitationexpiredList.size());
//				int invitationexpiredCount = candidateStatusList != null ? (int)candidateStatusList.stream()
//				        .filter(c -> c.getStatusMaster().getStatusCode().equals("INVITATIONEXPIRED"))
//				        .count() : 0;
//				log.info("invitationexpiredCount FROM ONLY COUNT::{}",invitationexpiredCount);
				
//				List<CandidateStatus> invalidUploadList = candidateStatusList != null ? candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("INVALIDUPLOAD"))
//						.collect(Collectors.toList()) : null;
//				log.info("invalidUploadList FROM LIST COUNT::{}",invalidUploadList.size());
//				int invaliduploadCount = candidateStatusList != null ? (int)candidateStatusList.stream()
//				        .filter(c -> c.getStatusMaster().getStatusCode().equals("INVALIDUPLOAD"))
//				        .count() : 0;
//				log.info("invaliduploadCount FROM ONLY COUNT::{}",invaliduploadCount);
				
//				List<CandidateStatus> reinviteList = candidateStatusList != null ? candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("REINVITE"))
//						.collect(Collectors.toList()) : null;
//				log.info("reinviteList FROM LIST COUNT::{}",reinviteList.size());
//				int reinviteCount = candidateStatusList != null ? (int)candidateStatusList.stream()
//				        .filter(c -> c.getStatusMaster().getStatusCode().equals("REINVITE"))
//				        .count() : 0;
//				log.info("reinviteList FROM ONLY COUNT::{}",reinviteCount);
//				
//				int newUploadCount = getNewUploadCountForDashboard(strFromDate,strToDate, dashboardDto.getUserId(), "NEWUPLOAD");
//				log.info("newUploadCount FROM ONLY COUNT::{}",newUploadCount);
				
//				//adding below code for getting correct candidate counts.
//				DashboardDto dashboardDto1=new DashboardDto();
//				dashboardDto1.setFromDate(strFromDate);
//				dashboardDto1.setToDate(strToDate);
//				dashboardDto1.setUserId(dashboardDto.getUserId());
//				dashboardDto1.setStatus("NEWUPLOAD");
//				
//				ServiceOutcome<DashboardDto> svcSearchResult1 =getAllCandidateList(dashboardDto1);
//				DashboardDto dashboardDtoObj1 =svcSearchResult1.getData();
				//finish
				StatusMaster newUploadStatusMaster=statusMasterRepository.findByStatusCode("NEWUPLOAD");
				candidateStatusCountDtoList.add(0,
						new CandidateStatusCountDto(
								newUploadStatusMaster.getStatusName(),
								newUploadStatusMaster.getStatusCode(),
								newUploadCount));
//								dashboardDtoObj1 != null ? dashboardDtoObj1.getCandidateDtoList().size() : 0));
								//candidateStatusList != null ? candidateStatusList.size() : 0));
				// candidateStatusCountDtoList.add(1, new
				// CandidateStatusCountDto(statusMasterRepository.findByStatusCode("INVITATIONSENT").getStatusName(),statusMasterRepository.findByStatusCode("INVITATIONSENT").getStatusCode(),invitationSentList!=null?invitationSentList.size():0));
				StatusMaster invitExpStatusMaster=statusMasterRepository.findByStatusCode("INVITATIONEXPIRED");
				candidateStatusCountDtoList.add(1,
						new CandidateStatusCountDto(
								invitExpStatusMaster.getStatusName(),
								invitExpStatusMaster.getStatusCode(),
								invitationexpiredCount));
//								invitationexpiredList != null ? invitationexpiredList.size() : 0));
				StatusMaster invalidUploadStatusMaster=statusMasterRepository.findByStatusCode("INVALIDUPLOAD");
				candidateStatusCountDtoList.add(2,
						new CandidateStatusCountDto(
								invalidUploadStatusMaster.getStatusName(),
								invalidUploadStatusMaster.getStatusCode(),
								invaliduploadCount));
//								invalidUploadList != null ? invalidUploadList.size() : 0));
				
				StatusMaster reInnviteStatusMaster=statusMasterRepository.findByStatusCode("REINVITE");
				candidateStatusCountDtoList.add(3,
						new CandidateStatusCountDto(reInnviteStatusMaster.getStatusName(),
								reInnviteStatusMaster.getStatusCode(),
								reinviteCount));

				DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null,
						candidateStatusCountDtoList, dashboardDto.getUserId(), null, null,dashboardDto.getPageNumber());
				svcSearchResult.setData(dashboardDtoObj);
				svcSearchResult.setOutcome(true);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("please specify user.");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getCandidateStatusAndCount method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<CandidateDetailsDto> getCandidateByCandidateCode(String referenceNo) {
		ServiceOutcome<CandidateDetailsDto> svcSearchResult = new ServiceOutcome<>();
		try {
			Candidate candidate = candidateRepository.findByCandidateCode(referenceNo);
			if (candidate != null) {
				CandidateDetailsDto candidateDetailsDto = this.modelMapper.map(candidate, CandidateDetailsDto.class);
				svcSearchResult.setData(candidateDetailsDto);
				svcSearchResult.setOutcome(true);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("No candidate found");
			}
		} catch (Exception ex) {
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(true);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
			log.error("Exception occured in getCandidate method in CandidateServiceImpl-->", ex);
		}
		return svcSearchResult;
	}

	@Override
	public Candidate findCandidateByCandidateCode(String candidateCode) {
		Candidate byCandidateCode = candidateRepository.findByCandidateCode(candidateCode);
		if (Objects.isNull(byCandidateCode)) {
			throw new RuntimeException("invalid candidate code");
		}
		return byCandidateCode;
	}

	@Override
	public ServiceOutcome<Boolean> cancelCandidate(String referenceNo) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		try {
			User user = SecurityHelper.getCurrentUser();
			CandidateStatus result = null;
			if (StringUtils.isNotBlank(referenceNo)) {
				CandidateStatus candidateStatus = candidateStatusRepository
						.findByCandidateCandidateCode(referenceNo.trim());
				if (candidateStatus != null) {
					candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("PROCESSDECLINED"));
					candidateStatus.setLastUpdatedBy(user);
					candidateStatus.setLastUpdatedOn(new Date());
					result = candidateStatusRepository.save(candidateStatus);
					createCandidateStatusHistory(result, "NOTCANDIDATE");
					if (result != null) {
						svcSearchResult.setData(true);
						svcSearchResult.setOutcome(true);
						svcSearchResult.setMessage("Verification process declined successfully.");
					}
				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult.setMessage("NO CANDIDATE FOUND");
				}
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("PLEASE SPECIFY CANDIDATE REFERENCE NUMBER");
			}
		} catch (Exception ex) {
			log.error("Exception occured in cancelCandidate method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Transactional
	@Override
	public List<CandidateStatus> expireInvitationForCandidate() {
		List<CandidateStatus> candidateList = new ArrayList<CandidateStatus>();
		try {
			List<String> statusList = new ArrayList<String>();
			statusList.add("INVITATIONSENT");
			statusList.add("REINVITE");
			List<CandidateStatus> candidateStatusList = candidateStatusRepository
					.findAllByStatusMasterStatusCodeIn(statusList);
			User user = SecurityHelper.getCurrentUser();
//			List<CandidateStatus> candidateStatusList = candidateStatusRepository
//					.findAllByStatusMasterStatusCode("INVITATIONSENT");
			if (candidateStatusList != null && candidateStatusList.size() > 0) {
				for (CandidateStatus candidateStatus : candidateStatusList) {
					Timestamp currentTimeInTimestamp = new Timestamp(System.currentTimeMillis());
					Timestamp savedTimestamp = (Timestamp) candidateStatus.getLastUpdatedOn();
//					Timestamp savedTimestamp = (Timestamp) candidateStatus.getCandidate().getCreatedOn();
					Long timeDifferenceInMillis = currentTimeInTimestamp.getTime() - savedTimestamp.getTime();
					Long timeDifferenceInSecond = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceInMillis);
					if (timeDifferenceInSecond > 259200) {
						candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("INVITATIONEXPIRED"));
						candidateStatus.setLastUpdatedOn(new Date());
						CandidateStatus candidateStatusObj = candidateStatusRepository.save(candidateStatus);
						createCandidateStatusHistory(candidateStatusObj, "CANDIDATE");
						CandidateEmailStatus candidateEmailStatus = candidateEmailStatusRepository
								.findByCandidateCandidateCode(candidateStatus.getCandidate().getCandidateCode());
						candidateEmailStatus.setDateOfEmailExpire(new Date());
						candidateEmailStatus.setLastUpdatedOn(new Date());
						candidateEmailStatus.setLastUpdatedBy(user);
						candidateEmailStatusRepository.save(candidateEmailStatus);
						candidateList.add(candidateStatusObj);
					}
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in expireInvitationForCandidate method in CandidateServiceImpl-->", ex);
		}
		return candidateList;
	}

	@Override
	public ServiceOutcome<CandidateStatus> getCandidateStatusByCandidateCode(String code) {
		ServiceOutcome<CandidateStatus> outcome = new ServiceOutcome<>();
		try {
			CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(code);
			if (candidateStatus != null) {
				outcome.setData(candidateStatus);
				outcome.setMessage("Record fetched successfully.");
				outcome.setOutcome(true);
			} else {
				outcome.setData(null);
				outcome.setMessage("");
				outcome.setOutcome(false);
			}

		} catch (Exception ex) {
			outcome.setData(null);
			outcome.setMessage("");
			outcome.setOutcome(false);
			log.error("Exception occured in getCandidateStatusByCandidateCode method in CandidateServiceImpl-->", ex);
		}
		return outcome;
	}

	@Override
	public ServiceOutcome<DashboardDto> getReportDeliveryDetailsStatusAndCount(DashboardDto dashboardDto) {
		ServiceOutcome<DashboardDto> svcSearchResult = new ServiceOutcome<DashboardDto>();
		List<CandidateStatusCountDto> candidateStatusCountDtoList = new ArrayList<CandidateStatusCountDto>();
		List<CandidateStatus> candidateStatusList = null;
		String strToDate = "";
		String strFromDate = "";
		try {
			if (dashboardDto.getUserId() != null && dashboardDto.getUserId() != 0l) {
				strToDate = dashboardDto.getToDate() != null ? dashboardDto.getToDate()
						: ApplicationDateUtils.getStringTodayAsDDMMYYYY();
				strFromDate = dashboardDto.getFromDate() != null ? dashboardDto.getFromDate()
						: ApplicationDateUtils.subtractNoOfDaysFromDateAsDDMMYYYY(
								new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);
				
				//calling the function to get counts for dashboard
				int pendingCount =0;
				int interimCount =0;
				int processDeclinedCount =0;
				int finalReportCount =0;
				List<Object[]> activityList =getCountsForDashboard(strToDate, strFromDate,dashboardDto.getUserId());
				for(Object[] activity : activityList) {
					
					finalReportCount =Integer.parseInt(activity[4].toString());
					processDeclinedCount =Integer.parseInt(activity[5].toString());
					interimCount =Integer.parseInt(activity[3].toString());
					pendingCount=Integer.parseInt(activity[7].toString());
				}
				
//				ServiceOutcome<List<CandidateStatus>> svcOutCome = getCandidateStatusList(strToDate, strFromDate,
//						dashboardDto.getUserId());
//				candidateStatusList = svcOutCome.getData();
				StatusMaster pending = statusMasterRepository.findByStatusCode("PENDINGAPPROVAL");
				StatusMaster interim = statusMasterRepository.findByStatusCode("INTERIMREPORT");
				StatusMaster processDeclined = statusMasterRepository.findByStatusCode("PROCESSDECLINED");
				StatusMaster finalReport = statusMasterRepository.findByStatusCode("FINALREPORT");
//				List<CandidateStatus> pendingList = candidateStatusList != null ? candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("PENDINGAPPROVAL"))
//						.collect(Collectors.toList()) : null;
				//getting pending count
//				int pendingCount = candidateStatusList != null ? (int)candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("PENDINGAPPROVAL"))
//						.count() : 0;
//				List<CandidateStatus> interimList = candidateStatusList != null ? candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("INTERIMREPORT"))
//						.collect(Collectors.toList()) : null;
				//getting interimm count
//				int interimCount = candidateStatusList != null ? (int)candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("INTERIMREPORT"))
//						.count() : 0;
//				List<CandidateStatus> processsdeclinedList = candidateStatusList != null ? candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("PROCESSDECLINED"))
//						.collect(Collectors.toList()) : null;
				//getting process declined count
//				int processDeclinedCount = candidateStatusList != null ? (int)candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("PROCESSDECLINED"))
//						.count() : 0;
//				List<CandidateStatus> finalReportList = candidateStatusList != null ? candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("FINALREPORT"))
//						.collect(Collectors.toList()) : null;
				//getting final count
//				int finalReportCount = candidateStatusList != null ? (int)candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("FINALREPORT"))
//						.count() : 0;
			//	int pendingCount = pendingList.size() + interimList.size();
//				int pendingCount =pendingList!=null? pendingList.size():0;
				candidateStatusCountDtoList.add(0, new CandidateStatusCountDto("CWF Completed",
						pending.getStatusCode(), pendingCount));
				candidateStatusCountDtoList.add(1, new CandidateStatusCountDto(interim.getStatusName(),
						interim.getStatusCode(), interimCount));
				candidateStatusCountDtoList.add(2,
						new CandidateStatusCountDto(processDeclined.getStatusName(), processDeclined.getStatusCode(),
								processDeclinedCount));
				candidateStatusCountDtoList.add(3, new CandidateStatusCountDto(finalReport.getStatusName(),
						finalReport.getStatusCode(), finalReportCount));
				DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null,
						candidateStatusCountDtoList, dashboardDto.getUserId(), null, null,dashboardDto.getPageNumber());
				svcSearchResult.setData(dashboardDtoObj);
				svcSearchResult.setOutcome(true);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("please specify user.");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getReportDeliveryDetailsStatusAndCount method in CandidateServiceImpl-->",
					ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<RemarkMaster>> getAllRemark(String remarkType) {
		ServiceOutcome<List<RemarkMaster>> svcSearchResult = new ServiceOutcome<List<RemarkMaster>>();
		try {
			List<RemarkMaster> remarkList = remarkMasterRepository.findAllByRemarkType(remarkType.toUpperCase());
			if (!remarkList.isEmpty()) {
				svcSearchResult.setData(remarkList);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NOT FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getAllRemark method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public List<CandidateStatus> processDeclined() {
		List<CandidateStatus> candidateList = new ArrayList<CandidateStatus>();
		try {
			List<String> statusList = new ArrayList<String>();
			statusList.add("DIGILOCKER");
			statusList.add("ITR");
			statusList.add("EPFO");
			statusList.add("RELATIVEADDRESS");
			List<CandidateStatus> candidateStatusList = candidateStatusRepository
					.findAllByStatusMasterStatusCodeIn(statusList);
			if (candidateStatusList != null && candidateStatusList.size() > 0) {
				for (CandidateStatus candidateStatus : candidateStatusList) {
					Timestamp currentTimeInTimestamp = new Timestamp(System.currentTimeMillis());
					Timestamp savedTimestamp = (Timestamp) candidateStatus.getLastUpdatedOn();
					Long timeDifferenceInMillis = currentTimeInTimestamp.getTime() - savedTimestamp.getTime();
					Long timeDifferenceInSecond = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceInMillis);
					if (timeDifferenceInSecond > 604800) {
						candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("PROCESSDECLINED"));
						candidateStatus.setLastUpdatedOn(new Date());
						CandidateStatus candidateStatusObj = candidateStatusRepository.save(candidateStatus);
						createCandidateStatusHistory(candidateStatusObj, "CANDIDATE");
						candidateList.add(candidateStatusObj);
					}
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in processDeclined method in CandidateServiceImpl-->", ex);
		}
		return candidateList;
	}

	@Override
	public ServiceOutcome<Boolean> declineAuthLetter(String candidateCode) {
		ServiceOutcome<Boolean> svcOutcome = new ServiceOutcome<>();
		try {
			CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(candidateCode);
			candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("PROCESSDECLINED"));
			candidateStatus.setLastUpdatedOn(new Date());
			candidateStatus = candidateStatusRepository.save(candidateStatus);
			createCandidateStatusHistory(candidateStatus, "CANDIDATE");
			svcOutcome.setData(true);
			svcOutcome.setOutcome(true);
			svcOutcome.setMessage("Your process was declined successfully.");

		} catch (Exception e) {
			svcOutcome.setData(true);
			svcOutcome.setOutcome(true);
			svcOutcome.setMessage("Unable to decline process.");
			log.error("Exception occured in declineAuthLetter method.");
		}
		return svcOutcome;
	}

	@Override
	public ServiceOutcome<List<QualificationMaster>> getQualificationList() {
		ServiceOutcome<List<QualificationMaster>> svcSearchResult = new ServiceOutcome<List<QualificationMaster>>();
		try {
			List<QualificationMaster> qualifiacationList = qualificationMasterRepository.findAll();
			if (!qualifiacationList.isEmpty()) {

				svcSearchResult.setData(qualifiacationList);
				svcSearchResult.setOutcome(true);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("No Qualification List found");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getQualificationList method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<Boolean> saveNUpdateCandidateEducation(String candidateCafEducationObject,
			MultipartFile certificate) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		CandidateCafEducation candidateCafEducation = null;
		CandidateCafEducation candidateCafEducationObj = null;
		try {
			CandidateCafEducationDto candidateCafEducationDto = new ObjectMapper()
					.readValue(candidateCafEducationObject, CandidateCafEducationDto.class);
			if (candidateCafEducationDto.getCandidateCafEducationId() != null) {
				Optional<CandidateCafEducation> candidateCafEducationObj1 = candidateCafEducationRepository
						.findById(candidateCafEducationDto.getCandidateCafEducationId());
				if (candidateCafEducationObj1.isPresent()) {
					candidateCafEducation = candidateCafEducationObj1.get();
					candidateCafEducation.setLastUpdatedOn(new Date());
					svcSearchResult.setMessage("Education details updated successfully.");
				}
			} else {
				candidateCafEducation = new CandidateCafEducation();
				candidateCafEducation.setCandidate(
						candidateRepository.findByCandidateCode(candidateCafEducationDto.getCandidateCode()));
				candidateCafEducation.setCandidateStatus(candidateStatusRepository
						.findByCandidateCandidateCode(candidateCafEducationDto.getCandidateCode()));
				candidateCafEducation.setCreatedOn(new Date());
				candidateCafEducation.setIsHighestQualification(false);
				svcSearchResult.setMessage("Education details added successfully.");
			}
			BeanUtils.copyProperties(candidateCafEducationDto, candidateCafEducation);
			// todo upload the file to s3
			candidateCafEducation.setCertificate(certificate != null ? certificate.getBytes() : null);
			candidateCafEducation.setQualificationMaster(
					qualificationMasterRepository.findById(candidateCafEducationDto.getQualificationId()).get());
			SuspectClgMaster suspectClgMasterObj = suspectClgMasterRepository
					.findById(candidateCafEducationDto.getSuspectClgMasterId()).get();
			candidateCafEducation.setSuspectClgMaster(suspectClgMasterObj);

			// removed DNH logic
//			if(suspectClgMasterObj.getSuspectClgMasterId()!=0) {
//				candidateCafEducation.setBoardOrUniversityName(suspectClgMasterObj.getSuspectInstitutionName());
//				candidateCafEducation.setCourseName(candidateCafEducationDto.getCourseName());
//				candidateCafEducation.setColor(colorRepository.findByColorCode("RED"));
//				candidateCafEducation.setRemarkMaster(remarkMasterRepository.findByRemarkCode("EDUCATION-1"));
//			}
//			else {
			candidateCafEducation.setBoardOrUniversityName(candidateCafEducationDto.getBoardOrUniversityName());
			candidateCafEducation.setCourseName(candidateCafEducationDto.getCourseName());
			candidateCafEducation.setColor(colorRepository.findByColorCode("AMBER"));
			candidateCafEducation.setRemarkMaster(remarkMasterRepository.findByRemarkCode("ALL"));
//			}
			candidateCafEducationObj = candidateCafEducationRepository.save(candidateCafEducation);
			if (candidateCafEducationObj != null) {
				svcSearchResult.setData(true);
				svcSearchResult.setOutcome(true);

			} else {
				svcSearchResult.setData(false);
				svcSearchResult.setOutcome(false);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
			}
		} catch (Exception ex) {
			log.error("Exception occured in saveNUpdateEducation method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(false);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<CandidateCafEducationDto> getCandidateEducationById(Long candidateCafEducationId) {
		ServiceOutcome<CandidateCafEducationDto> svcSearchResult = new ServiceOutcome<CandidateCafEducationDto>();
		try {
			Optional<CandidateCafEducation> candidateCafEducation = candidateCafEducationRepository
					.findById(candidateCafEducationId);
			if (candidateCafEducation.isPresent()) {
				CandidateCafEducationDto candidateCafEducationDto = this.modelMapper.map(candidateCafEducation.get(),
						CandidateCafEducationDto.class);
				svcSearchResult.setData(candidateCafEducationDto);
				svcSearchResult.setOutcome(true);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("No Candidate Education Details Found");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getCandidateEducationById method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<Boolean> saveNUpdateCandidateExperience(String candidateCafExperienceDtoObj,
			MultipartFile certificate) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		CandidateCafExperience candidateCafExperience = null;
		CandidateCafExperience candidateCafExperienceObj = null;
		try {
			CandidateCafExperienceDto candidateCafExperienceDto = new ObjectMapper()
					.readValue(candidateCafExperienceDtoObj, CandidateCafExperienceDto.class);

			if (candidateCafExperienceDto.getSuspectEmpMasterId() == null) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please select employer.");
				return svcSearchResult;
			} else if (candidateCafExperienceDto.getSuspectEmpMasterId() == 0
					&& candidateCafExperienceDto.getCandidateEmployerName().equals("")) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please enter candidate employer name.");
				return svcSearchResult;
			} else if (candidateCafExperienceDto.getInputDateOfJoining() == null) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please select date of joining.");
				return svcSearchResult;

			} else if (candidateCafExperienceDto.getInputDateOfExit() == null) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please select date of exit.");
				return svcSearchResult;
			} else if (certificate == null) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please upload certificate.");
				return svcSearchResult;
			}
			if (candidateCafExperienceDto.getCandidateCafExperienceId() != null) {
				Optional<CandidateCafExperience> candidateCafExperienceObj1 = candidateCafExperienceRepository
						.findById(candidateCafExperienceDto.getCandidateCafExperienceId());
				if (candidateCafExperienceObj1.isPresent()) {
					candidateCafExperience = candidateCafExperienceObj1.get();
					candidateCafExperience.setLastUpdatedOn(new Date());
					candidateCafExperienceObj = candidateCafExperienceRepository.save(candidateCafExperience);
					svcSearchResult.setMessage("Experience updated successfully.");
				}
			} else {
				candidateCafExperience = new CandidateCafExperience();
				candidateCafExperience.setCandidate(
						candidateRepository.findByCandidateCode(candidateCafExperienceDto.getCandidateCode()));
				candidateCafExperience.setCandidateStatus(candidateStatusRepository
						.findByCandidateCandidateCode(candidateCafExperienceDto.getCandidateCode()));
				candidateCafExperience.setCreatedOn(new Date());
				svcSearchResult.setMessage("Experience saved successfully.");
			}
			SuspectEmpMaster suspectEmpMaster = suspectEmpMasterRepository
					.findById(candidateCafExperienceDto.getSuspectEmpMasterId()).get();
			candidateCafExperience.setSuspectEmpMaster(suspectEmpMaster);
			if (suspectEmpMaster.getSuspectEmpMasterId() != 0) {
				candidateCafExperience.setCandidateEmployerName(suspectEmpMaster.getSuspectCompanyName());
				candidateCafExperience.setColor(colorRepository.findByColorCode("RED"));
				candidateCafExperience.setRemarkMaster(remarkMasterRepository.findByRemarkCode("EMPLOYMENT-1"));
			} else {
				candidateCafExperience.setCandidateEmployerName(candidateCafExperienceDto.getCandidateEmployerName());
				candidateCafExperience.setColor(colorRepository.findByColorCode("AMBER"));
				candidateCafExperience.setRemarkMaster(remarkMasterRepository.findByRemarkCode("ALL"));
			}
			candidateCafExperience.setInputDateOfJoining(candidateCafExperienceDto.getInputDateOfJoining() != null
					? sdf.parse(candidateCafExperienceDto.getInputDateOfJoining())
					: null);
			candidateCafExperience.setInputDateOfExit(candidateCafExperienceDto.getInputDateOfExit() != null
					? sdf.parse(candidateCafExperienceDto.getInputDateOfExit())
					: null);
			candidateCafExperience.setExperienceCertificate(certificate != null ? certificate.getBytes() : null);
			candidateCafExperienceObj = candidateCafExperienceRepository.save(candidateCafExperience);
			if (candidateCafExperienceObj != null) {
				svcSearchResult.setData(true);
				svcSearchResult.setOutcome(true);
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(true);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
			}
		} catch (Exception ex) {
			log.error("Exception occured in saveNUpdateCandidateExperience method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<CandidateCafExperienceDto> getCandidateExperienceById(Long candidateCafExperienceId) {
		ServiceOutcome<CandidateCafExperienceDto> svcSearchResult = new ServiceOutcome<CandidateCafExperienceDto>();
		try {
			Optional<CandidateCafExperience> candidateCafExperienceObj = candidateCafExperienceRepository
					.findById(candidateCafExperienceId);
			if (candidateCafExperienceObj.isPresent()) {
				CandidateCafExperience candidateCafExperience = candidateCafExperienceObj.get();
				CandidateCafExperienceDto candidateCafExperienceDto = this.modelMapper.map(candidateCafExperience,
						CandidateCafExperienceDto.class);
				candidateCafExperienceDto.setInputDateOfJoining(candidateCafExperience.getInputDateOfJoining() != null
						? sdf.format(candidateCafExperience.getInputDateOfJoining())
						: null);
				candidateCafExperienceDto.setOutputDateOfJoining(candidateCafExperience.getOutputDateOfJoining() != null
						? sdf.format(candidateCafExperience.getOutputDateOfJoining())
						: null);
				candidateCafExperienceDto.setInputDateOfExit(candidateCafExperience.getInputDateOfExit() != null
						? sdf.format(candidateCafExperience.getInputDateOfExit())
						: null);
				candidateCafExperienceDto.setOutputDateOfExit(candidateCafExperience.getOutputDateOfExit() != null
						? sdf.format(candidateCafExperience.getOutputDateOfExit())
						: null);
				svcSearchResult.setData(candidateCafExperienceDto);
				svcSearchResult.setOutcome(true);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("No Candidate Experience Details Found");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getCandidateExperienceById method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	// Candidate details
	@Override
	public ServiceOutcome<CandidationApplicationFormDto> candidateApplicationFormDetails(String candidateCode) {
		ServiceOutcome<CandidationApplicationFormDto> svcSearchResult = new ServiceOutcome<CandidationApplicationFormDto>();
		CandidationApplicationFormDto candidationApplicationFormDto = new CandidationApplicationFormDto();
		List<CandidateCafEducationDto> candidateCafEducationDtoList = new ArrayList<CandidateCafEducationDto>();
		List<CandidateCafExperienceDto> candidateCafExperienceDtoList = new ArrayList<CandidateCafExperienceDto>();
		List<CandidateCafAddressDto> candidateCafAddressDtoList = new ArrayList<CandidateCafAddressDto>();
		// List<ITRDataFromApiDto> iTRDataFromApiDtoList=new
		// ArrayList<ITRDataFromApiDto>();
		CandidateFileDto candidateFileDto = null;
		try {
			entityManager.setFlushMode(FlushModeType.COMMIT);
			if (StringUtils.isNotEmpty(candidateCode)) {
				Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);
				IdItemsDto candidateIdItemPan = candidateIdItemsRepository
						.findByCandidateCandidateCodeAndServiceSourceMasterServiceCode(candidateCode, "PAN");
				if (candidateIdItemPan != null && candidateIdItemPan.getIdHolder() != null) {
					candidate.setCandidateName(candidateIdItemPan.getIdHolder()); 
					entityManager.detach(candidate);
				} else {
//					IdItemsDto candidateIdItemAadhar = candidateIdItemsRepository
//							.findByCandidateCandidateCodeAndServiceSourceMasterServiceCode(candidateCode, "AADHARID");
					if (Objects.nonNull(candidate.getAadharName())) {
						candidate.setCandidateName(candidate.getAadharName());
						entityManager.detach(candidate);
					}
				}
				candidationApplicationFormDto.setCandidate(candidate);
				List<CandidateCafEducation> candidateCafEducationList = candidateCafEducationRepository
						.findAllByCandidateCandidateCode(candidateCode);
				if (!candidateCafEducationList.isEmpty()) {
					candidateCafEducationDtoList = candidateCafEducationList.stream()
							.map(candidateCafEducation -> modelMapper.map(candidateCafEducation,
									CandidateCafEducationDto.class))
							.collect(Collectors.toList());
					candidationApplicationFormDto.setCandidateCafEducationDto(candidateCafEducationDtoList);
				}
//				List<CandidateCafExperience> candidateCafExperienceList=candidateCafExperienceRepository.findAllByCandidateCandidateCodeOrderByInputDateOfJoiningDesc(candidateCode);
//				List<CandidateCafExperience> efpolist = candidateCafExperienceList != null ?candidateCafExperienceList.stream().filter(c -> c.getServiceSourceMaster()!=null && c.getServiceSourceMaster().getServiceCode().equals("EPFO")).collect(Collectors.toList()) : new ArrayList<>();
//				List<Long> itrIds = candidateCafExperienceList != null ?candidateCafExperienceList.stream().filter(c -> c.getServiceSourceMaster()!=null && c.getServiceSourceMaster().getServiceCode().equals("ITR")).map(f->f.getCandidateCafExperienceId()).collect(Collectors.toList()) : new ArrayList<>();
//
//				for(CandidateCafExperience efpo: efpolist) {
//					Date inputJoiningDate = efpo.getInputDateOfJoining();
//					Date inputExitDate = efpo.getInputDateOfExit();
//					List<Long> itrRemoveIdsList = candidateCafExperienceRepository.findByCandidateCodeAndServiceSourceMasterCodeAndDates(candidateCode, "ITR",inputJoiningDate,inputExitDate);
//					itrIds.removeAll(itrRemoveIdsList);
//				}
//
//				List<CandidateCafExperience> itrList  = candidateCafExperienceList.stream().filter(c-> itrIds.contains(c.getCandidateCafExperienceId())).collect(Collectors.toList());
//				efpolist.addAll(itrList);
//				List<CandidateCafExperience> newList  = candidateCafExperienceList.stream().filter(c -> c.getServiceSourceMaster()==null).collect(Collectors.toList());
//				efpolist.addAll(newList);
//				efpolist.sort(Comparator.comparing(CandidateCafExperience::getInputDateOfJoining));
//				Collections.reverse(efpolist);
//				if(!efpolist.isEmpty()) {
//					for (CandidateCafExperience candidateCafExperience : efpolist) {
//						CandidateCafExperienceDto candidateCafExperienceDto = this.modelMapper.map(candidateCafExperience, CandidateCafExperienceDto.class);
//						candidateCafExperienceDto.setInputDateOfJoining(candidateCafExperience.getInputDateOfJoining()!=null?sdf.format(candidateCafExperience.getInputDateOfJoining()):null);
//						candidateCafExperienceDto.setInputDateOfExit(candidateCafExperience.getInputDateOfExit()!=null?sdf.format(candidateCafExperience.getInputDateOfExit()):null);
//						candidateCafExperienceDto.setOutputDateOfJoining(candidateCafExperience.getOutputDateOfJoining()!=null?sdf.format(candidateCafExperience.getOutputDateOfJoining()):null);
//						candidateCafExperienceDto.setOutputDateOfExit(candidateCafExperience.getOutputDateOfExit()!=null?sdf.format(candidateCafExperience.getOutputDateOfExit()):null);
//						candidateCafExperienceDto.setServiceName(candidateCafExperience.getServiceSourceMaster()!=null?candidateCafExperience.getServiceSourceMaster().getServiceName():"Candidate");
//						candidateCafExperienceDtoList.add(candidateCafExperienceDto);
//					}
//
//					candidationApplicationFormDto.setCandidateCafExperienceDto(candidateCafExperienceDtoList);
//					List<String> uanNUmberList=candidateCafExperienceRepository.getCandidateUan(candidate.getCandidateId());
//					String uanNumber = uanNUmberList.stream().map(uan -> uan.toString()).collect(Collectors.joining("/"));
//					candidationApplicationFormDto.setCandidateUan(uanNumber);
//				}
				List<CandidateCafExperience> candidateCafExperiences = candidateCafExperienceRepository
						.findAllByCandidateCandidateId(candidate.getCandidateId());
				if (candidateCafExperiences.isEmpty()) {
					candidateCafExperiences = getCandidateExperienceFromItrAndEpfoByCandidateId(
							candidate.getCandidateId(), false);
					System.out.println(candidateCafExperiences.size() + "getting");
					candidateCafExperienceRepository.saveAll(candidateCafExperiences);
				}
				List<CandidateCafExperienceDto> collect = candidateCafExperiences.stream()
						.map(candidateCafExperience -> {
							CandidateCafExperienceDto candidateCafExperienceDto = this.modelMapper
									.map(candidateCafExperience, CandidateCafExperienceDto.class);
							candidateCafExperienceDto
									.setInputDateOfJoining(candidateCafExperience.getInputDateOfJoining() != null
											? sdf.format(candidateCafExperience.getInputDateOfJoining())
											: null);
							candidateCafExperienceDto
									.setInputDateOfExit(candidateCafExperience.getInputDateOfExit() != null
											? sdf.format(candidateCafExperience.getInputDateOfExit())
											: null);
							candidateCafExperienceDto
									.setOutputDateOfJoining(candidateCafExperience.getOutputDateOfJoining() != null
											? sdf.format(candidateCafExperience.getOutputDateOfJoining())
											: null);
							candidateCafExperienceDto
									.setOutputDateOfExit(candidateCafExperience.getOutputDateOfExit() != null
											? sdf.format(candidateCafExperience.getOutputDateOfExit())
											: null);
							candidateCafExperienceDto
									.setServiceName(candidateCafExperience.getServiceSourceMaster() != null
											? candidateCafExperience.getServiceSourceMaster().getServiceName()
											: "Candidate");
							
							DateDifference dateDifference = DateUtil.getPreodDifference(candidateCafExperience.getInputDateOfJoining()!=null ? candidateCafExperience.getInputDateOfJoining() : new Date(),
									candidateCafExperience.getInputDateOfExit()!=null ? candidateCafExperience.getInputDateOfExit() : new Date());
							log.info("Checking the Tenuer duration::{}",dateDifference);
							//adding the experience in list only when tenuer not less then 1 and should not have ITR data
							if(candidateCafExperience.getServiceSourceMaster() != null
									&& candidateCafExperience.getServiceSourceMaster().getServiceName().equalsIgnoreCase("EPFO")) {
									if (dateDifference.getYears()>=1 || dateDifference.getMonths()>1) {
										return candidateCafExperienceDto;
				                    }else {
				                    	return null;
				                    }
								
							}else if(candidateCafExperience.getServiceSourceMaster() != null ?
									!candidateCafExperience.getServiceSourceMaster().getServiceName().equalsIgnoreCase("ITR") : true) {
								
								return candidateCafExperienceDto;
							}else {
								return null;
							}
						
						}).filter(candidateCafExperienceDto -> candidateCafExperienceDto != null)
						.collect(Collectors.toList());
				
				candidateCafExperienceDtoList.addAll(collect);
				candidationApplicationFormDto.setCandidateCafExperienceDto(candidateCafExperienceDtoList);
				List<CandidateCafAddress> candidateCafAddressList = candidateCafAddressRepository
						.findAllByCandidateCandidateCode(candidateCode);
				if (candidateCafAddressList != null) {
					candidateCafAddressDtoList = candidateCafAddressList.stream().map(
							candidateCafAddress -> modelMapper.map(candidateCafAddress, CandidateCafAddressDto.class))
							.collect(Collectors.toList());
					candidationApplicationFormDto.setCandidateCafAddressDto(candidateCafAddressDtoList);
				}

//				List<ITRData> iTRDataList=itrDataRepository.findAllByCandidateCandidateCodeOrderByDateDesc(candidateCode);
//				if(!iTRDataList.isEmpty()) {
//					iTRDataFromApiDtoList= iTRDataList.stream().map(itr -> modelMapper.map(itr,ITRDataFromApiDto.class)).collect(Collectors.toList());
//					candidationApplicationFormDto.setITRDataFromApiDto(iTRDataFromApiDtoList);
//				}
				CandidateResumeUpload candidateResume = candidateResumeUploadRepository
						.findByCandidateCandidateCode(candidateCode);
				if (candidateResume != null && Objects.nonNull(candidateResume.getContentId())) {
					String resumeUrl = contentService.getFileUrlFromContentId(candidateResume.getContentId());
					candidationApplicationFormDto.setCandidateResumeUrl(resumeUrl);
				} else if (candidateResume != null) {
					candidateFileDto = new CandidateFileDto(candidateResume.getCandidateResumeUploadId(),
							candidateResume.getCandidateResume(), null);
					candidationApplicationFormDto.setCandidateResume(candidateFileDto);
				}
//				CandidateCaseDetails candidateCaseDetails=candidateCaseDetailsRepository.findByCandidateCandidateCode(candidateCode);
//				if(candidateCaseDetails!=null) {
//					candidateFileDto=new CandidateFileDto(candidateCaseDetails.getCandidateCaseDetailsId(), candidateCaseDetails.getCriminalVerificationDocument(),candidateCaseDetails.getCriminalVerificationisExist().getColorName());
//					candidationApplicationFormDto.setCaseDetails(candidateFileDto);
//					candidateFileDto=new CandidateFileDto(candidateCaseDetails.getCandidateCaseDetailsId(), candidateCaseDetails.getGlobalDatabaseCaseDetailsDocument(),candidateCaseDetails.getGlobalDatabaseCaseDetailsIsExist().getColorName());
//					candidationApplicationFormDto.setGlobalDatabaseCaseDetails(candidateFileDto);
//				}

				CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(candidateCode);
				candidationApplicationFormDto.setCandidateStatus(candidateStatus);

			}
			svcSearchResult.setData(candidationApplicationFormDto);
			svcSearchResult.setOutcome(true);
			svcSearchResult.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
		} catch (Exception ex) {
			log.error("Exception occured in candidateApplicationFormDetails method in CandidateServiceImpl-->",candidateCode, ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}
	


	@Transactional
	@Override
	public ServiceOutcome<Boolean> saveCandidateApplicationForm(String candidateCafEducationId,
			JSONArray candidateCafAddressDto, MultipartFile resume, String candidateCode) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		try {
			Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);

			if (resume != null && !candidateCode.isEmpty()) {
				CandidateResumeUpload candidateResume = candidateResumeUploadRepository
						.findByCandidateCandidateCode(candidateCode);
				if (candidateResume != null) {
					candidateResume.setLastUpdatedOn(new Date());
				} else {
					candidateResume = new CandidateResumeUpload();
					candidateResume.setCandidate(candidateRepository.findByCandidateCode(candidateCode));
					candidateResume.setCreatedOn(new Date());
				}
				ContentDTO contentDTO = new ContentDTO();
				File resumeFile = FileUtil.createUniqueTempFile(candidateCode, ".pdf");
				resume.transferTo(resumeFile);
				contentDTO.setFile(resumeFile);
				contentDTO.setCandidateCode(candidateCode);
				contentDTO.setCandidateId(candidate.getCandidateId());
				contentDTO.setContentType(ContentType.CANDIDATE_UPLOADED);
				contentDTO.setFileType(FileType.PDF);
				contentDTO.setContentCategory(ContentCategory.OTHERS);
				contentDTO.setContentSubCategory(ContentSubCategory.RESUME);
				ContentDTO contentDTO1 = contentService.uploadFile(contentDTO);
//				candidateResume.setCandidateResume(resume!=null?resume.getBytes():null);
				candidateResume.setContentId(contentDTO1.getContentId());
				candidateResumeUploadRepository.save(candidateResume);
			}
			if (!candidateCafEducationId.isEmpty() && !candidateCafEducationId.equals("")
					&& candidateCafEducationId != null) {
				Optional<CandidateCafEducation> candidateCafEducationObj1 = candidateCafEducationRepository
						.findById(Long.parseLong(candidateCafEducationId));
				if (candidateCafEducationObj1.isPresent()) {
					CandidateCafEducation candidateCafEducation = candidateCafEducationObj1.get();
					candidateCafEducation.setIsHighestQualification(true);
					candidateCafEducationRepository.save(candidateCafEducation);
				}
			}
			if (!candidateCafAddressDto.isEmpty()) {
				for (int i = 0; i < candidateCafAddressDto.length(); i++) {
					JSONObject object = candidateCafAddressDto.getJSONObject(i);
					CandidateCafAddressDto candidateCafAddressDtoObj1 = new ObjectMapper().readValue(object.toString(),
							CandidateCafAddressDto.class);
					if (candidateCafAddressDtoObj1.getCandidateCafAddressId() != null) {
						Optional<CandidateCafAddress> candidateCafAddress = candidateCafAddressRepository
								.findById(candidateCafAddressDtoObj1.getCandidateCafAddressId());
						if (candidateCafAddress.isPresent()) {
							CandidateCafAddress candidateCafAddressobj = candidateCafAddress.get();
							candidateCafAddressobj.setIsAssetDeliveryAddress(
									candidateCafAddressDtoObj1.getIsAssetDeliveryAddress() != null
											? candidateCafAddressDtoObj1.getIsAssetDeliveryAddress()
											: candidateCafAddressobj.getIsAssetDeliveryAddress());
							candidateCafAddressobj
									.setIsPermanentAddress(candidateCafAddressDtoObj1.getIsPermanentAddress() != null
											? candidateCafAddressDtoObj1.getIsPermanentAddress()
											: candidateCafAddressobj.getIsAssetDeliveryAddress());
							candidateCafAddressobj
									.setIsPresentAddress(candidateCafAddressDtoObj1.getIsPresentAddress() != null
											? candidateCafAddressDtoObj1.getIsPresentAddress()
											: candidateCafAddressobj.getIsAssetDeliveryAddress());
							candidateCafAddressRepository.save(candidateCafAddressobj);
						}
					}
				}

			}
			candidate.setSubmittedOn(new Date());
			candidate.setApprovalRequired(true);
			candidateRepository.save(candidate);
			CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(candidateCode);
			candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("PENDINGAPPROVAL"));
			candidateStatus.setLastUpdatedOn(new Date());
			candidateStatus = candidateStatusRepository.save(candidateStatus);
			createCandidateStatusHistory(candidateStatus, "CANDIDATE");
			svcSearchResult.setData(true);
			svcSearchResult.setOutcome(true);
			svcSearchResult.setMessage("Thank you for submitting your response.");

			CompletableFuture.runAsync(() -> {
				reportService.generateDocument(candidateCode, "", ReportType.PRE_OFFER,"");
			});

		} catch (Exception ex) {
			log.error("Exception occured in saveCandidateApplicationForm method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(false);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<Boolean> updateCandidateEducationStatusAndRemark(
			ApprovalStatusRemarkDto approvalStatusRemarkDto) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		CandidateCafEducation candidateCafEducation = null;
		// System.out.println("\n__________approvalStatusRemarkDto________"+approvalStatusRemarkDto);
		try {
			User user = SecurityHelper.getCurrentUser();
			if (approvalStatusRemarkDto.getId() != null) {
				Optional<CandidateCafEducation> candidateCafEducationObj1 = candidateCafEducationRepository
						.findById(approvalStatusRemarkDto.getId());
				if (candidateCafEducationObj1.isPresent()) {
					Optional<QualificationMaster> qualificationMasterobj = qualificationMasterRepository
							.findById(approvalStatusRemarkDto.getQualificationId());
//					System.out.println("qualificationMasterobj====================" + qualificationMasterobj);
					CandidateCafEducation candidateCafEducationobj = candidateCafEducationObj1.get();
					candidateCafEducationobj.setQualificationMaster(
							qualificationMasterRepository.findById(approvalStatusRemarkDto.getQualificationId()).get());
					if (qualificationMasterobj.isPresent()) {
						QualificationMaster qualificationMasterobj1 = qualificationMasterobj.get();
						String qualificationName = qualificationMasterobj1.getQualificationName();
						candidateCafEducationobj.setCourseName(qualificationName);
					}
					candidateCafEducationobj.setSchoolOrCollegeName(approvalStatusRemarkDto.getSchoolOrCollegeName());
					candidateCafEducationobj
							.setBoardOrUniversityName(approvalStatusRemarkDto.getBoardOrUniversityName());
					candidateCafEducationobj.setYearOfPassing(approvalStatusRemarkDto.getYearOfPassing());
					candidateCafEducationobj.setPercentage(approvalStatusRemarkDto.getPercentage());
					candidateCafEducationobj
							.setColor(colorRepository.findById(approvalStatusRemarkDto.getColorId()).get());
					if (approvalStatusRemarkDto.getRemarkId() != null)
						candidateCafEducationobj.setRemarkMaster(
								remarkMasterRepository.findById(approvalStatusRemarkDto.getRemarkId()).get());
					candidateCafEducationobj.setCustomRemark(approvalStatusRemarkDto.getCustomRemark());
					candidateCafEducationobj.setLastUpdatedOn(new Date());
					candidateCafEducationobj.setLastUpdatedBy(user);
					candidateCafEducation = candidateCafEducationRepository.save(candidateCafEducationobj);
					if (candidateCafEducation != null) {
						svcSearchResult.setData(true);
						svcSearchResult.setOutcome(true);
						svcSearchResult.setMessage("Education remarks saved successfully.");
					} else {
						svcSearchResult.setData(true);
						svcSearchResult.setOutcome(false);
						svcSearchResult.setMessage(
								messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
					}
				}
			} else {
				Optional<QualificationMaster> qualificationMasterobj = qualificationMasterRepository
						.findById(approvalStatusRemarkDto.getQualificationId());
//				System.out.println("qualificationMasterobj====================" + qualificationMasterobj);

				candidateCafEducation = new CandidateCafEducation();
				candidateCafEducation.setCandidate(
						candidateRepository.findByCandidateCode(approvalStatusRemarkDto.getCandidateCode()));
				candidateCafEducation.setCandidateStatus(candidateStatusRepository
						.findByCandidateCandidateCode(approvalStatusRemarkDto.getCandidateCode()));
				candidateCafEducation.setCreatedOn(new Date());
				candidateCafEducation.setIsHighestQualification(false);
				if (qualificationMasterobj.isPresent()) {
					QualificationMaster qualificationMasterobj1 = qualificationMasterobj.get();
					String qualificationName = qualificationMasterobj1.getQualificationName();
					candidateCafEducation.setCourseName(qualificationName);
				}

				BeanUtils.copyProperties(approvalStatusRemarkDto, candidateCafEducation);
				candidateCafEducation.setSchoolOrCollegeName(approvalStatusRemarkDto.getSchoolOrCollegeName());
				candidateCafEducation.setBoardOrUniversityName(approvalStatusRemarkDto.getBoardOrUniversityName());
				// candidateCafEducation.setQualificationMaster(approvalStatusRemarkDto.getQualificationMaster());
				candidateCafEducation.setQualificationMaster(
						qualificationMasterRepository.findById(approvalStatusRemarkDto.getQualificationId()).get());
				// candidateCafEducation.setColor(colorRepository.findByColorCode("AMBER"));
				candidateCafEducation.setColor(colorRepository.findById(approvalStatusRemarkDto.getColorId()).get());
				candidateCafEducation.setRemarkMaster(remarkMasterRepository.findByRemarkCode("ALL"));
				candidateCafEducationRepository.save(candidateCafEducation);
				svcSearchResult.setMessage("Education details added successfully.");
				// System.out.println("***********************candidateCafEducation"+candidateCafEducation);

			}
		} catch (Exception ex) {
			log.error("Exception occured in updateCandidateEducationStatusAndRemark method in CandidateServiceImpl-->",
					ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<Boolean> updateCandidateExperienceStatusAndRemark(
			ApprovalStatusRemarkDto approvalStatusRemarkDto) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		CandidateCafExperience candidateCafExperience = null;

		try {
			User user = SecurityHelper.getCurrentUser();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			org.joda.time.format.DateTimeFormatter formatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
			if (approvalStatusRemarkDto.getId() != null) {
				Optional<CandidateCafExperience> candidateCafExperienceObj1 = candidateCafExperienceRepository
						.findById(approvalStatusRemarkDto.getId());
				if (candidateCafExperienceObj1.isPresent()) {
					CandidateCafExperience candidateCafExperienceobj = candidateCafExperienceObj1.get();

					candidateCafExperienceobj
							.setCandidateEmployerName(approvalStatusRemarkDto.getCandidateEmployerName());
					candidateCafExperienceobj.setInputDateOfJoining(
							DateUtil.getDate(approvalStatusRemarkDto.getInputDateOfJoining(), "yyyy-MM-dd"));
					candidateCafExperienceobj.setInputDateOfExit(
							DateUtil.getDate(approvalStatusRemarkDto.getInputDateOfExit()!=null? approvalStatusRemarkDto.getInputDateOfExit(): "", "yyyy-MM-dd"));

					// candidateCafExperienceobj.setColor("GREEN");
					// candidateCafExperienceobj.setRemarkMaster(remarkMasterRepository.findById(approvalStatusRemarkDto.getRemarkId()).get());
					// candidateCafExperienceobj.setColor(colorRepository.findById(approvalStatusRemarkDto.getColorId()).get());
					// candidateCafExperienceobj.setRemarkMaster(remarkMasterRepository.findById(approvalStatusRemarkDto.getRemarkId()).get());
					candidateCafExperienceobj.setCustomRemark(approvalStatusRemarkDto.getCustomRemark());
					
					//setting OUTPUT DOE AND REMARK AS NAME OF PROOF OF OUTPUTDOE
					if(approvalStatusRemarkDto.getOutputDateOfExit()!=null && !approvalStatusRemarkDto.getOutputDateOfExit().isEmpty()){
						log.info("Updating approvalStatusRemarkDto.getOutputDateOfExit()::{}",approvalStatusRemarkDto.getOutputDateOfExit());
						candidateCafExperienceobj.setOutputDateOfExit(
								DateUtil.getDate(approvalStatusRemarkDto.getOutputDateOfExit(), "yyyy-MM-dd"));
						candidateCafExperienceobj.setCustomRemark(approvalStatusRemarkDto.getOutputDateOfExitProofName()!=null ?
								approvalStatusRemarkDto.getOutputDateOfExitProofName(): approvalStatusRemarkDto.getCustomRemark());
					}
					candidateCafExperienceobj.setLastUpdatedOn(new Date());
					candidateCafExperienceobj.setLastUpdatedBy(user);
					if (approvalStatusRemarkDto.getColorId() != null) {
						candidateCafExperienceobj
								.setColor((colorRepository.findById(approvalStatusRemarkDto.getColorId())).get());
					}

					if(approvalStatusRemarkDto.getUndisclosed() != null)
						candidateCafExperienceobj.setUndisclosed(approvalStatusRemarkDto.getUndisclosed());
					
					// added for moonlighting 
					List<CandidateCafExperience> existingCafExperienceList = candidateCafExperienceRepository.findAllByCandidateCandidateId(candidateCafExperienceobj.getCandidate().getCandidateId());
					
					CandidateCafExperience epfoData1 = this.modelMapper.map(candidateCafExperienceobj,
							CandidateCafExperience.class);

					for(int k=0;k < existingCafExperienceList.size();k++) {
						entityManager.detach(existingCafExperienceList.get(k));
						if(!candidateCafExperienceObj1.get().equals(existingCafExperienceList.get(k))) {
							
							CandidateCafExperience epfoData2 = this.modelMapper.map(existingCafExperienceList.get(k),
									CandidateCafExperience.class);
							if(epfoData1.getInputDateOfJoining() != null && epfoData2.getInputDateOfJoining() != null) {
								
								String doj1 = sdf.format(epfoData1.getInputDateOfJoining());								
								String doj2 = sdf.format(epfoData2.getInputDateOfJoining());
								
								org.joda.time.LocalDate startDate1 = org.joda.time.LocalDate.parse(doj1, formatter);
								org.joda.time.LocalDate endDate1 = null;
								org.joda.time.LocalDate startDate2 = org.joda.time.LocalDate.parse(doj2, formatter);
								org.joda.time.LocalDate endDate2 = null;
						        
								if(epfoData1.getInputDateOfExit() == null) {
									endDate1 = org.joda.time.LocalDate.now();
								}else {
									String doe1 = sdf.format(epfoData1.getInputDateOfExit());
									endDate1 = org.joda.time.LocalDate.parse(doe1, formatter);
								}
									
								if(epfoData2.getInputDateOfExit() == null) {
									endDate2 = org.joda.time.LocalDate.now();
								}else {
									String doe2 = sdf.format(epfoData2.getInputDateOfExit());
									endDate2 = org.joda.time.LocalDate.parse(doe2, formatter);
								}

								Interval interval1 = null;
								Interval interval2 = null;
								if(startDate1.isBefore(endDate1)) {
							        interval1 = new Interval(startDate1.toDateMidnight(), endDate1.toDateMidnight());
								} else {
							        interval1 = new Interval(endDate1.toDateMidnight(), startDate1.toDateMidnight());
								}
								if(startDate2.isBefore(endDate2)) {
							        interval2 = new Interval(startDate2.toDateMidnight(), endDate2.toDateMidnight());
								}else {
							        interval2 = new Interval(endDate2.toDateMidnight(), startDate2.toDateMidnight());
								}

								if(interval1.overlaps(interval2) || interval1.abuts(interval2)) { 
									for(CandidateCafExperience candidateCafExp  : existingCafExperienceList) {
										if (!candidateCafExperienceobj.getColor().getColorCode().equals("OUTOFSCOPE"))
											candidateCafExperienceobj.setColor(colorRepository.findByColorCode("MOONLIGHTING")); 
										if(candidateCafExp.getCandidateEmployerName().equalsIgnoreCase(epfoData2.getCandidateEmployerName()) && candidateCafExp.getInputDateOfJoining().equals(epfoData2.getInputDateOfJoining()) && !candidateCafExp.getColor().getColorCode().equals("OUTOFSCOPE")) {  
											candidateCafExp.setColor(colorRepository.findByColorCode("MOONLIGHTING"));
										}
									} 
								}
							}
						}
					}
					//taking color as selected by user in UI
					if (approvalStatusRemarkDto.getColorId() != null) {
						candidateCafExperienceobj
								.setColor((colorRepository.findById(approvalStatusRemarkDto.getColorId())).get());
					}
					//end 
					
					candidateCafExperience = candidateCafExperienceRepository.save(candidateCafExperienceobj);
					if (candidateCafExperience != null) {
						svcSearchResult.setData(true);
						svcSearchResult.setOutcome(true);
						svcSearchResult.setMessage("Experience remarks update successfully.");

					} else {
						svcSearchResult.setData(true);
						svcSearchResult.setOutcome(false);
						svcSearchResult.setMessage(
								messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
					}
				}

			} else {
				
				candidateCafExperience = new CandidateCafExperience();

				Candidate candidate = candidateRepository.findByCandidateCode(approvalStatusRemarkDto.getCandidateCode());
				List<CandidateCafExperience> existingCafExperienceList = candidateCafExperienceRepository.findAllByCandidateCandidateId(candidate.getCandidateId());

				Optional<Organization> organization = organizationRepository
						.findById(approvalStatusRemarkDto.getOrganizationid());
				if (organization.isPresent()) {
					Organization orgObj = organization.get();
					Long orgid = orgObj.getOrganizationId();

					List<SuspectEmpMaster> suspectEmpMaster = suspectEmpMasterRepository
							.findAllByOrganizationOrganizationIdAndIsActiveTrue(orgid);
					if (!suspectEmpMaster.isEmpty()) {
						ServiceOutcome<String> response = suspectEmpMasterCheck(approvalStatusRemarkDto.getCandidateEmployerName(), orgid);
						if(response.getOutcome() == true) {
							if(response.getData().equalsIgnoreCase("RED")) {
								candidateCafExperience.setColor(colorRepository.findByColorCode("RED"));
								candidateCafExperience.setServiceSourceMaster(serviceSourceMasterRepository.findByServiceCode("DNHDB"));
							} else if(response.getData().equalsIgnoreCase("AMBER")) {
								candidateCafExperience.setColor(colorRepository.findByColorCode("AMBER"));
							}
						}
						candidateCafExperience.setCandidate(candidate);
						candidateCafExperience.setCandidateStatus(candidateStatusRepository
								.findByCandidateCandidateCode(approvalStatusRemarkDto.getCandidateCode()));
						candidateCafExperience.setCreatedOn(new Date());
						candidateCafExperience.setCreatedBy(user);
						candidateCafExperience
								.setCandidateEmployerName(approvalStatusRemarkDto.getCandidateEmployerName());
						candidateCafExperience.setCustomRemark(approvalStatusRemarkDto.getCustomRemark());						

						candidateCafExperience.setRemarkMaster(remarkMasterRepository.findByRemarkCode("ALL"));
						candidateCafExperience.setInputDateOfJoining(
								DateUtil.getDate(approvalStatusRemarkDto.getInputDateOfJoining(), "yyyy-MM-dd"));
						candidateCafExperience.setInputDateOfExit(
								DateUtil.getDate(approvalStatusRemarkDto.getInputDateOfExit(), "yyyy-MM-dd"));

						if (approvalStatusRemarkDto.getColorId() != null) {
							candidateCafExperience
									.setColor((colorRepository.findById(approvalStatusRemarkDto.getColorId())).get());
						}

						if(approvalStatusRemarkDto.getUndisclosed() != null)
							candidateCafExperience.setUndisclosed(approvalStatusRemarkDto.getUndisclosed());
						
						existingCafExperienceList.add(candidateCafExperience);
						// added for moonlighting 
						
						CandidateCafExperience epfoData1 = this.modelMapper.map(candidateCafExperience,
								CandidateCafExperience.class);
						for(int k=0;k<existingCafExperienceList.size()-1;k++) {
							entityManager.detach(existingCafExperienceList.get(k));
							
							CandidateCafExperience epfoData2 = this.modelMapper.map(existingCafExperienceList.get(k),
									CandidateCafExperience.class);
							if(epfoData1.getInputDateOfJoining() != null && epfoData2.getInputDateOfJoining() != null) { 
								String doj1 = sdf.format(epfoData1.getInputDateOfJoining());								
								String doj2 = sdf.format(epfoData2.getInputDateOfJoining());
								
								org.joda.time.LocalDate startDate1 = org.joda.time.LocalDate.parse(doj1, formatter);
								org.joda.time.LocalDate endDate1 = null;
								org.joda.time.LocalDate startDate2 = org.joda.time.LocalDate.parse(doj2, formatter);
								org.joda.time.LocalDate endDate2 = null;
						        
								if(epfoData1.getInputDateOfExit() == null) {
									endDate1 = org.joda.time.LocalDate.now();
								}else {
									String doe1 = sdf.format(epfoData1.getInputDateOfExit());
									endDate1 = org.joda.time.LocalDate.parse(doe1, formatter);
								}
									
								if(epfoData2.getInputDateOfExit() == null) {
									endDate2 = org.joda.time.LocalDate.now();
								}else {
									String doe2 = sdf.format(epfoData2.getInputDateOfExit());
									endDate2 = org.joda.time.LocalDate.parse(doe2, formatter);
								}

								Interval interval1 = null;
								Interval interval2 = null;
								if(startDate1.isBefore(endDate1)) {
							        interval1 = new Interval(startDate1.toDateMidnight(), endDate1.toDateMidnight());
								} else {
							        interval1 = new Interval(endDate1.toDateMidnight(), startDate1.toDateMidnight());
								}
								if(startDate2.isBefore(endDate2)) {
							        interval2 = new Interval(startDate2.toDateMidnight(), endDate2.toDateMidnight());
								}else {
							        interval2 = new Interval(endDate2.toDateMidnight(), startDate2.toDateMidnight());
								}

								if(interval1.overlaps(interval2) || interval1.abuts(interval2)) {
									for(CandidateCafExperience candidateCafExp  : existingCafExperienceList) {
										if(candidateCafExp.getCandidateEmployerName().equalsIgnoreCase(epfoData1.getCandidateEmployerName()) && candidateCafExp.getInputDateOfJoining().equals(epfoData1.getInputDateOfJoining()) && !candidateCafExp.getColor().getColorCode().equals("OUTOFSCOPE")) { 
											candidateCafExp.setColor(colorRepository.findByColorCode("MOONLIGHTING")); 
										}
										if(candidateCafExp.getCandidateEmployerName().equalsIgnoreCase(epfoData2.getCandidateEmployerName()) && candidateCafExp.getInputDateOfJoining().equals(epfoData2.getInputDateOfJoining()) && !candidateCafExp.getColor().getColorCode().equals("OUTOFSCOPE")) {  
											candidateCafExp.setColor(colorRepository.findByColorCode("MOONLIGHTING"));
										}
									} 
								}
							}
						}
						//end 
						
						candidateCafExperienceRepository.saveAll(existingCafExperienceList);
						if (candidateCafExperience != null) {
							svcSearchResult.setData(true);
							svcSearchResult.setOutcome(true);
							svcSearchResult.setMessage("Experience saved successfully.");

						} else {
							svcSearchResult.setData(true);
							svcSearchResult.setOutcome(false);
							svcSearchResult.setMessage(
									messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
						}

					} else {
						candidateCafExperience = new CandidateCafExperience();
						candidateCafExperience.setCandidate(
								candidateRepository.findByCandidateCode(approvalStatusRemarkDto.getCandidateCode()));
						candidateCafExperience.setCandidateStatus(candidateStatusRepository
								.findByCandidateCandidateCode(approvalStatusRemarkDto.getCandidateCode()));
						candidateCafExperience.setCreatedOn(new Date());
						candidateCafExperience.setCreatedBy(user);
						candidateCafExperience
								.setCandidateEmployerName(approvalStatusRemarkDto.getCandidateEmployerName());
						candidateCafExperience.setColor(colorRepository.findByColorCode("AMBER"));
						candidateCafExperience.setRemarkMaster(remarkMasterRepository.findByRemarkCode("ALL"));
						candidateCafExperience.setInputDateOfJoining(
								DateUtil.getDate(approvalStatusRemarkDto.getInputDateOfJoining(), "yyyy-MM-dd"));
						candidateCafExperience.setInputDateOfExit(
								DateUtil.getDate(approvalStatusRemarkDto.getInputDateOfExit(), "yyyy-MM-dd"));

						if (approvalStatusRemarkDto.getColorId() != null) {
							candidateCafExperience
									.setColor((colorRepository.findById(approvalStatusRemarkDto.getColorId())).get());
						}

						if(approvalStatusRemarkDto.getUndisclosed() != null)
							candidateCafExperience.setUndisclosed(approvalStatusRemarkDto.getUndisclosed());
							
						existingCafExperienceList.add(candidateCafExperience);
						// added for moonlighting 
						CandidateCafExperience epfoData1 = this.modelMapper.map(candidateCafExperience,
								CandidateCafExperience.class);
						for(int k=0;k<existingCafExperienceList.size()-1;k++) {					
							
							CandidateCafExperience epfoData2 = this.modelMapper.map(existingCafExperienceList.get(k),
									CandidateCafExperience.class);
							if(epfoData1.getInputDateOfJoining() != null && epfoData2.getInputDateOfJoining() != null) { 
								String doj1 = sdf.format(epfoData1.getInputDateOfJoining());								
								String doj2 = sdf.format(epfoData2.getInputDateOfJoining());
								
								org.joda.time.LocalDate startDate1 = org.joda.time.LocalDate.parse(doj1, formatter);
								org.joda.time.LocalDate endDate1 = null;
								org.joda.time.LocalDate startDate2 = org.joda.time.LocalDate.parse(doj2, formatter);
								org.joda.time.LocalDate endDate2 = null;
						        
								if(epfoData1.getInputDateOfExit() == null) {
									endDate1 = org.joda.time.LocalDate.now();
								}else {
									String doe1 = sdf.format(epfoData1.getInputDateOfExit());
									endDate1 = org.joda.time.LocalDate.parse(doe1, formatter);
								}
									
								if(epfoData2.getInputDateOfExit() == null) {
									endDate2 = org.joda.time.LocalDate.now();
								}else {
									String doe2 = sdf.format(epfoData2.getInputDateOfExit());
									endDate2 = org.joda.time.LocalDate.parse(doe2, formatter);
								}

								Interval interval1 = null;
								Interval interval2 = null;
								if(startDate1.isBefore(endDate1)) {
							        interval1 = new Interval(startDate1.toDateMidnight(), endDate1.toDateMidnight());
								} else {
							        interval1 = new Interval(endDate1.toDateMidnight(), startDate1.toDateMidnight());
								}
								if(startDate2.isBefore(endDate2)) {
							        interval2 = new Interval(startDate2.toDateMidnight(), endDate2.toDateMidnight());
								}else {
							        interval2 = new Interval(endDate2.toDateMidnight(), startDate2.toDateMidnight());
								}

								if(interval1.overlaps(interval2) || interval1.abuts(interval2)) { 
									for(CandidateCafExperience candidateCafExp  : existingCafExperienceList) {
										if(candidateCafExp.getCandidateEmployerName().equalsIgnoreCase(epfoData1.getCandidateEmployerName()) && candidateCafExp.getInputDateOfJoining().equals(epfoData1.getInputDateOfJoining()) && !candidateCafExp.getColor().getColorCode().equals("OUTOFSCOPE")) { 
											candidateCafExp.setColor(colorRepository.findByColorCode("MOONLIGHTING")); 
										}
										if(candidateCafExp.getCandidateEmployerName().equalsIgnoreCase(epfoData2.getCandidateEmployerName()) && candidateCafExp.getInputDateOfJoining().equals(epfoData2.getInputDateOfJoining()) && !candidateCafExp.getColor().getColorCode().equals("OUTOFSCOPE")) {  
											candidateCafExp.setColor(colorRepository.findByColorCode("MOONLIGHTING"));
										}
									} 
								}
							}
						}
						log.info("passed moonlighing check "+candidateCafExperience.getCandidate().getCandidateId());
						//end  

						candidateCafExperienceRepository.saveAll(existingCafExperienceList);
						if (candidateCafExperience != null) {
							svcSearchResult.setData(true);
							svcSearchResult.setOutcome(true);
							svcSearchResult.setMessage("Organization Not Found, Experience saved successfully.");

						} else {
							svcSearchResult.setData(true);
							svcSearchResult.setOutcome(false);
							svcSearchResult.setMessage(
									messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
						}

					}
				}

			}
		} catch (Exception ex) {
			log.error("Exception occured in updateCandidateExperienceStatusAndRemark method in CandidateServiceImpl-->",
					ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<Boolean> updateCandidateAddressStatusAndRemark(
			ApprovalStatusRemarkDto approvalStatusRemarkDto) {
		System.out.println("***********************approvalStatusRemarkDto" + approvalStatusRemarkDto);
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		CandidateCafAddress candidateCafAddress = null;
		try {
			User user = SecurityHelper.getCurrentUser();
			if (approvalStatusRemarkDto.getId() != null) {
				System.out.println("*********%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + approvalStatusRemarkDto.getId());
				Optional<CandidateCafAddress> candidateCafAddressObj1 = candidateCafAddressRepository
						.findById(approvalStatusRemarkDto.getId());
				if (candidateCafAddressObj1.isPresent()) {
					System.out.println("*********%%%%%%%%%%%%%%%%");
					CandidateCafAddress candidateCafAddressObj = candidateCafAddressObj1.get();
					// candidateCafAddressObj.setCandidateCafAddressId(candidateCafAddressRepository.findById(approvalStatusRemarkDto.getCandidateCafAddressId()).get());
					candidateCafAddressObj
							.setColor(colorRepository.findById(approvalStatusRemarkDto.getColorId()).get());
					if (approvalStatusRemarkDto.getRemarkId() != null)
						candidateCafAddressObj.setRemarkMaster(
								remarkMasterRepository.findById(approvalStatusRemarkDto.getRemarkId()).get());

					candidateCafAddressObj
							.setIsAssetDeliveryAddress(approvalStatusRemarkDto.getIsAssetDeliveryAddress() != null
									? approvalStatusRemarkDto.getIsAssetDeliveryAddress()
									: candidateCafAddressObj.getIsAssetDeliveryAddress());
					candidateCafAddressObj.setIsPermanentAddress(approvalStatusRemarkDto.getIsPermanentAddress() != null
							? approvalStatusRemarkDto.getIsPermanentAddress()
							: candidateCafAddressObj.getIsAssetDeliveryAddress());
					candidateCafAddressObj.setIsPresentAddress(approvalStatusRemarkDto.getIsPresentAddress() != null
							? approvalStatusRemarkDto.getIsPresentAddress()
							: candidateCafAddressObj.getIsAssetDeliveryAddress());
					candidateCafAddressObj.setCustomRemark(approvalStatusRemarkDto.getCustomRemark());
					candidateCafAddressObj.setLastUpdatedOn(new Date());
					candidateCafAddressObj.setLastUpdatedBy(user);
					System.out.println("***********************candidateCafAddressObj" + candidateCafAddressObj);
					candidateCafAddress = candidateCafAddressRepository.save(candidateCafAddressObj);

					if (candidateCafAddress != null) {
						svcSearchResult.setData(true);
						svcSearchResult.setOutcome(true);
						svcSearchResult.setMessage("Address remarks saved successfully.");
					} else {
						svcSearchResult.setData(true);
						svcSearchResult.setOutcome(false);
						svcSearchResult.setMessage(
								messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
					}
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in updateCandidateAddressStatusAndRemark method in CandidateServiceImpl-->",
					ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Transactional
	@Override
	public ServiceOutcome<Boolean> candidateApplicationFormApproved(String candidateCode,
			MultipartFile criminalVerificationDocument, Long criminalVerificationColorId,
			MultipartFile globalDatabseCaseDetailsDocument, Long globalDatabseCaseDetailsColorId, String reportType) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		try {
//			System.out.println("criminalVerificationDocument" + criminalVerificationDocument);
			Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);
			User user = SecurityHelper.getCurrentUser();
			if (criminalVerificationDocument != null || globalDatabseCaseDetailsDocument != null) {
//				System.out.println("criminalVerificationDocument" + criminalVerificationDocument);
				CandidateCaseDetails existingCaseDetails = candidateCaseDetailsRepository
						.findByCandidateCandidateCode(candidateCode);

				CandidateCaseDetails candidateCaseDetails = new CandidateCaseDetails();
				if (existingCaseDetails != null) {
					candidateCaseDetails = existingCaseDetails;
				}

				candidateCaseDetails.setCandidate(candidate);

				if (criminalVerificationDocument != null) {
					candidateCaseDetails.setCriminalVerificationDocument(
							criminalVerificationDocument != null ? criminalVerificationDocument.getBytes() : null);
					candidateCaseDetails.setCriminalVerificationisExist(criminalVerificationColorId != null
							? colorRepository.findById(criminalVerificationColorId).get()
							: null);
				}

				if (globalDatabseCaseDetailsDocument != null) {
					candidateCaseDetails.setGlobalDatabaseCaseDetailsDocument(
							globalDatabseCaseDetailsDocument != null ? globalDatabseCaseDetailsDocument.getBytes()
									: null);
					candidateCaseDetails.setGlobalDatabaseCaseDetailsIsExist(globalDatabseCaseDetailsColorId != null
							? colorRepository.findById(globalDatabseCaseDetailsColorId).get()
							: null);
				}

				candidateCaseDetails.setCreatedOn(new Date());
				candidateCaseDetails.setCreatedBy(user);
				CandidateCaseDetails result = candidateCaseDetailsRepository.save(candidateCaseDetails);
			}

			if (reportType.equals("INTERIMREPORT")) {
				candidate.setApprovalRequired(true);
			} else {
				candidate.setApprovalRequired(false);
			}
			candidateRepository.save(candidate);
			CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(candidateCode);
			if (reportType.equals("INTERIMREPORT")) {
				if(candidateStatus.getStatusMaster().getStatusCode().equals("INTERIMREPORT")) {
					CandidateStatusHistory candidateStatusHistoryObj=candidateStatusHistoryRepository.findLastStatusHistorytRecord(candidate.getCandidateId());
					log.info("LAST STATUS HISTORY IS ::{}",candidateStatusHistoryObj.getStatusMaster().getStatusCode());
					candidateStatusHistoryObj.setCreatedOn(new Date());
					candidateStatusHistoryObj.setCandidateStatusChangeTimestamp(new Date());
					candidateStatusHistoryRepository.save(candidateStatusHistoryObj);
					
				}else {
				    candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("INTERIMREPORT"));
				    
				    candidateStatus.setLastUpdatedOn(new Date());
					candidateStatus.setLastUpdatedBy(user);
					candidateStatusRepository.save(candidateStatus);
					createCandidateStatusHistory(candidateStatus, "NOTCANDIDATE");
				}
			} else {
				candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("FINALREPORT"));
				
				candidateStatus.setLastUpdatedOn(new Date());
				candidateStatus.setLastUpdatedBy(user);
				candidateStatusRepository.save(candidateStatus);
				createCandidateStatusHistory(candidateStatus, "NOTCANDIDATE");
			}

//			candidateStatus.setLastUpdatedOn(new Date());
//			candidateStatus.setLastUpdatedBy(user);
//			candidateStatusRepository.save(candidateStatus);
//			createCandidateStatusHistory(candidateStatus, "NOTCANDIDATE");
			svcSearchResult.setData(true);
			svcSearchResult.setOutcome(true);
			svcSearchResult.setMessage("QC done successfully.");
			
// 			if (reportType.equals("INTERIMREPORT")) {
// //				CompletableFuture.runAsync(() -> {
// //					reportService.generateDocument(candidateCode, "", ReportType.INTERIM);
// //				});
// 			} else {
// 				CompletableFuture.runAsync(() -> {
// 					reportService.generateDocument(candidateCode, "", ReportType.FINAL);
// 				});			
// 			}


		} catch (Exception ex) {
			log.error("Exception occured in candidateApplicationFormApproved method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Transactional
	@Override
	public ServiceOutcome<Boolean> saveFakeCompanyDetails(MultipartFile file, Long organizationId,String status) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		try {
			List<SuspectEmpMaster> suspectEmpMasterListObj = null;

			if (CSVUtil.hasCSVFormat(file)) {
				suspectEmpMasterListObj = cSVUtil.csvToSuspectEmpMaster(file.getInputStream(), organizationId);

			}

			if (ExcelUtil.hasExcelFormat(file)) {
				suspectEmpMasterListObj = excelUtil.excelToSuspectEmpMaster(file.getInputStream(), organizationId);
			}

			log.info("OrganizationID:::: {}",organizationId);
			List<String> allSuspectEmpMasterByOrgId = suspectEmpMasterRepository.getAllSuspectEmpMasterByOrgId(organizationId);
			log.info("AllSuspectEmpMasterByOrgId::: {}",allSuspectEmpMasterByOrgId.size());
			
			boolean companyExists = false;
			List<String> companyAlreadyExists = new ArrayList<>();
			List<SuspectEmpMaster> companiesNotExists = new ArrayList<>();
			
			Set<String> existingCompanies = new HashSet<>(allSuspectEmpMasterByOrgId);
			//List<SuspectEmpMaster> companiesNotExists = new ArrayList<>();

			for (SuspectEmpMaster newEmpMaster : suspectEmpMasterListObj) {
			    if (existingCompanies.contains(newEmpMaster.getSuspectCompanyName())) {
			        companyExists = true;
			        companyAlreadyExists.add(newEmpMaster.getSuspectCompanyName());
			    } else {
			        companiesNotExists.add(newEmpMaster);
			    }
			}
			int existingCompanyCount = existingCompanies.size();
			log.info("existingCompanyCount::: {}",existingCompanyCount);
			if (!companiesNotExists.isEmpty()) {
			    suspectEmpMasterRepository.saveAllAndFlush(companiesNotExists);
			}

			if (companyExists) {
			    log.info("Companies already exist: {}", companyAlreadyExists);
			    svcSearchResult.setOutcome(false);
			    svcSearchResult.setMessage("Companies Already Exist: " + companyAlreadyExists.toString());
			} else {
			    log.info("Companies do not exist in the database.");
			    svcSearchResult.setData(true);
			    svcSearchResult.setOutcome(true);
			    svcSearchResult.setMessage("Uploaded the file successfully: " + file.getOriginalFilename());
			}	
			
			if (status != null && status.equals("proceed")) {
			    log.info("================== PROCEED STATUS BLOCK ================================");
			     suspectEmpMasterRepository.saveAllAndFlush(companiesNotExists);
			        svcSearchResult.setData(true);
			        svcSearchResult.setOutcome(true);
			        svcSearchResult.setMessage("Uploaded the file successfully: " + file.getOriginalFilename());
			    
			}
			
				//log.info("Companies Not Found in the Database: " + companiesNotExists.toString());
			
//			List<SuspectEmpMaster> suspectEmpMasterList = suspectEmpMasterRepository.saveAllAndFlush(companiesNotExists);
//			if (!suspectEmpMasterList.isEmpty()) {
//				svcSearchResult.setData(true);
//				svcSearchResult.setOutcome(true);
//				svcSearchResult.setMessage("Uploaded the file successfully: " + file.getOriginalFilename());
//			}
		} catch (IOException e) {
			log.error("Exception occured in saveFakeCompanyDetails method in CandidateServiceImpl-->" + e);
			throw new RuntimeException("fail to store csv/xls data: " + e.getMessage());
		}
		return svcSearchResult;
	}

	@Transactional
	@Override
	public ServiceOutcome<Boolean> saveFakeCollegeDetails(MultipartFile file) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		try {
			List<SuspectClgMaster> suspectClgMasterListObj = null;
			if (ExcelUtil.hasExcelFormat(file)) {
				suspectClgMasterListObj = excelUtil.excelToSuspectClgMaster(file.getInputStream());
			}
			List<SuspectClgMaster> suspectClgMasterList = suspectClgMasterRepository
					.saveAllAndFlush(suspectClgMasterListObj);
			if (!suspectClgMasterList.isEmpty()) {
				svcSearchResult.setData(true);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("Uploaded the file successfully: " + file.getOriginalFilename());
			}
		} catch (IOException e) {
			log.error("Exception occured in saveFakeCollegeDetails method in CandidateServiceImpl-->" + e);
			throw new RuntimeException("fail to store csv/xls data: " + e.getMessage());
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<SuspectClgMaster>> getAllSuspectClgList() {
		ServiceOutcome<List<SuspectClgMaster>> svcSearchResult = new ServiceOutcome<List<SuspectClgMaster>>();
		try {
			SuspectClgMaster suspectClgMaster = suspectClgMasterRepository.findById(0L).get();
			List<SuspectClgMaster> suspectClgMasterList = suspectClgMasterRepository
					.findAllByIsActiveTrueOrderBySuspectInstitutionNameAsc();
			if (!suspectClgMasterList.isEmpty() && suspectClgMaster != null) {
				List<SuspectClgMaster> newList = new ArrayList<SuspectClgMaster>();
				newList.add(suspectClgMaster);
				newList.addAll(suspectClgMasterList);
				svcSearchResult.setData(newList);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NOT FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getAllSuspectClgList method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<SuspectEmpMaster>> getAllSuspectEmpList(Long organizationId,int pageNumber, int pageSize) {
		log.info("--------------++++++===---------IMPPPorganizationId {}" + organizationId);
		ServiceOutcome<List<SuspectEmpMaster>> svcSearchResult = new ServiceOutcome<List<SuspectEmpMaster>>();
		try {
			log.info("+++++++++++++++++++++++++TRY {}" + organizationId);
			Optional<Organization> organization = organizationRepository.findById(organizationId);
//			log.info("=====================LIST {}" + organization);
			if (organization.isPresent()) {

				Organization orgObj = organization.get();
				Long orgid = orgObj.getOrganizationId();
				System.out.println(orgObj.getOrganizationId());
				
				
				Pageable pageable = PageRequest.of(pageNumber, pageSize);
				
				Page<SuspectEmpMaster> suspectEmpMaster = suspectEmpMasterRepository.findAllByOrganizationOrganizationId(orgid, pageable);

//				log.info("===================***********==LISTsuspectEmpMaster" + suspectEmpMaster);
				
				// Retrieve the list of items on the current page
				List<SuspectEmpMaster> suspectList = suspectEmpMaster.getContent();
				
				int currentPageNumber = suspectEmpMaster.getNumber();  
				log.info("CurrentPage:: {}",currentPageNumber);
				int totalPages = suspectEmpMaster.getTotalPages();
				String totalPagesString = String.valueOf(totalPages);
				log.info("totalPage:: {}",totalPages);
				long totalElements = suspectEmpMaster.getTotalElements();
				log.info("totalElements:: {}",totalElements);
				boolean hasNextPage = suspectEmpMaster.hasNext(); 
				log.info("hasNextPage:: {}",hasNextPage);
				boolean hasPreviousPage = suspectEmpMaster.hasPrevious();
				log.info("hasPreviousPage:: {}",hasPreviousPage);

				if (!suspectEmpMaster.isEmpty()) {
					List<SuspectEmpMaster> newList = new ArrayList<SuspectEmpMaster>();
					newList.addAll(suspectList);
					svcSearchResult.setData(newList);
					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage("SUCCESS");
					svcSearchResult.setStatus(totalPagesString);
					} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult.setMessage("NOT FOUND");
				}
			}

		} catch (Exception ex) {
			log.error("Exception occured in getAllSuspectEmpList method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Transactional
	@Override
	public ServiceOutcome<Boolean> relationshipAddressVerification(String candidateCafRelation,
			MultipartFile document) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		CandidateCafRelationship candidateCafRelationshipObj = null;

		try {
			CandidateCafRelationshipDto candidateCafRelationDto = new ObjectMapper().readValue(candidateCafRelation,
					CandidateCafRelationshipDto.class);
			if (StringUtils.isNotBlank(candidateCafRelationDto.getCandidateCode())) {
				Candidate candidate = candidateRepository
						.findByCandidateCode(candidateCafRelationDto.getCandidateCode());
				CandidateCafRelationship candidateCafRelationship = new CandidateCafRelationship();
				CandidateStatus candidateStatus = candidateStatusRepository
						.findByCandidateCandidateCode(candidateCafRelationDto.getCandidateCode());
				candidateCafRelationship.setCandidateStatus(candidateStatus);
				candidateCafRelationship.setCandidateRelationship(candidateCafRelationDto.getCandidateRelationship());
				candidateCafRelationship.setCreatedOn(new Date());
				if (candidateCafRelationDto.getCandidateRelationship().equals("LANDLORD")) {
					// TODO upload file to s3
					candidateCafRelationship.setDocumentUploaded(document != null ? document.getBytes() : null);
					candidateCafRelationship.setRentType(candidateCafRelationDto.getRentType());
					candidateCafRelationshipObj = candidateCafRelationshipRepository.save(candidateCafRelationship);
				} else {
					Boolean result = emailSentTask.sendRelationshipEmail(candidateCafRelationDto.getCandidateCode(),
							candidate.getCandidateName(), candidateCafRelationDto.getCandidateRelationshipEmail());
					if (result) {
						candidateCafRelationshipObj = candidateCafRelationshipRepository.save(candidateCafRelationship);
					}
				}
				if (candidateCafRelationshipObj != null) {
					CandidateAdressVerification candidateAdressVerification = new CandidateAdressVerification();
					candidateAdressVerification.setCandidateCafRelationship(candidateCafRelationshipObj);
					candidateAdressVerification
							.setVerificationEmailId(candidateCafRelationDto.getCandidateRelationshipEmail());
					candidateAdressVerification.setDateOfAdressVerification(new Date());
					candidateAdressVerification.setCandidate(candidate);
					candidateAdressVerification.setCandidateStatus(candidateStatus);
					candidateAdressVerification.setCreatedOn(new Date());
					candidateAdressVerification = candidateAdressVerificationRepository
							.save(candidateAdressVerification);

					if (candidateCafRelationDto.getCandidateRelationship().equals("LANDLORD")) {
						CandidateCafAddress address = new CandidateCafAddress();
						address.setCandidate(candidate);
						address.setColor(colorRepository.findByColorCode("AMBER"));
						address.setCreatedOn(new Date());
						address.setName("LANDLORD");
						address.setAddressVerification(candidateAdressVerification);
						candidateCafAddressRepository.save(address);
					}
					candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("RELATIVEADDRESS"));
					candidateStatus.setLastUpdatedOn(new Date());
					CandidateStatus candidatestatus = candidateStatusRepository.save(candidateStatus);
					createCandidateStatusHistory(candidatestatus, "CANDIDATE");
					svcSearchResult.setData(true);
					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage("Relationship address details saved successfully.");
				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult
							.setMessage(messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
				}
			}

		} catch (Exception ex) {
			log.error("Exception occured in relationshipAddressVerification method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<DashboardDto> getPendingDetailsStatusAndCount(DashboardDto dashboardDto) {
		ServiceOutcome<DashboardDto> svcSearchResult = new ServiceOutcome<DashboardDto>();
		List<CandidateStatusCountDto> candidateStatusCountDtoList = new ArrayList<CandidateStatusCountDto>();
		List<CandidateStatus> candidateStatusList = null;
		List<Long> agentIds = new ArrayList<>();
		String strToDate = "";
		String strFromDate = "";
		try {
			if (dashboardDto.getUserId() != null) {
				User user = userRepository.findById(dashboardDto.getUserId()).get();
				strToDate = dashboardDto.getToDate() != null ? dashboardDto.getToDate()
						: ApplicationDateUtils.getStringTodayAsDDMMYYYY();
				strFromDate = dashboardDto.getFromDate() != null ? dashboardDto.getFromDate()
						: ApplicationDateUtils.subtractNoOfDaysFromDateAsDDMMYYYY(
								new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);
				
				//below changes for getting the pending list counts directly
				Date startDate = user.getCreatedOn();
				Date endDate = new Date();
				
				if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_ADMIN")
						|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_PARTNERADMIN")) {
					
					candidateStatusCountDtoList = superAdminDashboardServiceImpl.getPendingDetailsCounts(user.getOrganization().getOrganizationId(),startDate,endDate);
				}
				if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTSUPERVISOR")
						|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTHR")) {
					List<User> agentList = userRepository.findAllByAgentSupervisorUserId(user.getUserId());
					if (!agentList.isEmpty()) {
						agentIds = agentList.stream().map(x -> x.getUserId()).collect(Collectors.toList());
					}
					agentIds.add(user.getUserId());
					candidateStatusCountDtoList = getPendingCountsForAgent(agentIds,startDate,endDate);
				}
				
				//END
				//commenting below old code, that is taking the counts by getting list and by filters.....!!
				
//				ServiceOutcome<List<CandidateStatus>> svcOutCome = getCandidateStatusList(strToDate, strFromDate,
//						dashboardDto.getUserId());
//				candidateStatusList = svcOutCome.getData();
//
//				ServiceOutcome<List<String>> configCodes = candidateService.getServiceConfigCodes(null,
//						user.getOrganization().getOrganizationId());
//
//				StatusMaster invStatusMaster = statusMasterRepository.findByStatusCode("INVITATIONSENT");
//				StatusMaster digiStatusMaster = statusMasterRepository.findByStatusCode("DIGILOCKER");
//				StatusMaster itrStatusMaster = statusMasterRepository.findByStatusCode("ITR");
//				StatusMaster epfoStatusMaster = statusMasterRepository.findByStatusCode("EPFO");
//				StatusMaster relStatusMaster = statusMasterRepository.findByStatusCode("RELATIVEADDRESS");
//				StatusMaster canStatusMaster = statusMasterRepository.findByStatusCode("PROCESSDECLINED");
//
//				List<CandidateStatus> invitationSentList = candidateStatusList != null ? candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("INVITATIONSENT"))
//						.collect(Collectors.toList()) : null;
//				List<CandidateStatus> digiList = candidateStatusList != null ? candidateStatusList.stream()
//						.filter(c -> c.getCandidate().getIsUanSkipped() != null
//								? c.getStatusMaster().getStatusCode().equals("DIGILOCKER")
//										&& !c.getCandidate().getIsUanSkipped()
//								: c.getStatusMaster().getStatusCode().equals("DIGILOCKER"))
//						.collect(Collectors.toList()) : null;
//				candidateStatusCountDtoList.add(new CandidateStatusCountDto(invStatusMaster.getStatusName().equalsIgnoreCase("Invitation Sent")?"LOA Pending":invStatusMaster.getStatusName(),
//						invStatusMaster.getStatusCode(), invitationSentList!=null ? invitationSentList.size():0));
//				candidateStatusCountDtoList.add(new CandidateStatusCountDto(digiStatusMaster.getStatusName().equalsIgnoreCase("Digilocker Completed")?"ITR Pending":digiStatusMaster.getStatusName(),
//						digiStatusMaster.getStatusCode(), digiList.size()));
//				if (configCodes.getOutcome()) {
//					if (configCodes.getData().contains("ITR")) {
//						List<CandidateStatus> itrList = candidateStatusList != null ? candidateStatusList.stream()
//								.filter(c -> c.getCandidate().getIsUanSkipped() != null
//										? c.getStatusMaster().getStatusCode().equals("ITR")
//												&& !c.getCandidate().getIsUanSkipped()
//										: c.getStatusMaster().getStatusCode().equals("ITR"))
//								.collect(Collectors.toList()) : null;
//						candidateStatusCountDtoList.add(new CandidateStatusCountDto(itrStatusMaster.getStatusName().equalsIgnoreCase("ITR Completed")?"EPFO Pending":itrStatusMaster.getStatusName(),
//								itrStatusMaster.getStatusCode(), itrList.size()));
//					}
//					if (configCodes.getData().contains("EPFO")) {
//						List<CandidateStatus> epfoList = candidateStatusList != null ? candidateStatusList.stream()
//								.filter(c -> c.getStatusMaster().getStatusCode().equals("EPFO"))
//								.collect(Collectors.toList()) : null;
//						candidateStatusCountDtoList.add(new CandidateStatusCountDto(epfoStatusMaster.getStatusName().equalsIgnoreCase("EPFO Completed")?"Preoffer Pending":itrStatusMaster.getStatusName(),
//								epfoStatusMaster.getStatusCode(), epfoList.size()));
//						List<CandidateStatus> epfoSkippedList = candidateStatusList != null
//								? candidateStatusList.stream()
//										.filter(c -> c.getCandidate().getIsUanSkipped() != null
//												? c.getStatusMaster().getStatusCode().equals("DIGILOCKER")
//														&& c.getCandidate().getIsUanSkipped()
//														|| c.getStatusMaster().getStatusCode().equals("ITR")
//																&& c.getCandidate().getIsUanSkipped()
//												: false)
//										.collect(Collectors.toList())
//								: null;
//						candidateStatusCountDtoList.add(
//								new CandidateStatusCountDto("EPFO Skipped", "EPFOSKIPPED", epfoSkippedList.size()));
//					}
//					if (configCodes.getData().contains("RELBILLTRUE")) {
//						List<CandidateStatus> relList = candidateStatusList != null
//								? candidateStatusList.stream()
//										.filter(c -> c.getStatusMaster().getStatusCode().equals("RELATIVEADDRESS"))
//										.collect(Collectors.toList())
//								: null;
//						List<CandidateStatus> newRelList = new ArrayList<>();
//						for (CandidateStatus rel : relList) {
//							String code = rel.getCandidate().getCandidateCode();
//							Long count = candidateCafAddressRepository
//									.findCountByCandidateCodeAndRelAddrVerification(code);
//							if (count > 0) {
//								newRelList.add(rel);
//							}
//						}
//						candidateStatusCountDtoList.add(new CandidateStatusCountDto(relStatusMaster.getStatusName(),
//								relStatusMaster.getStatusCode(), newRelList.size()));
//					}
//				}
//				if (configCodes.getData().contains("RELBILLTRUE")) {
//					List<CandidateStatus> cafList = candidateStatusList != null ? candidateStatusList.stream()
//							.filter(c -> c.getStatusMaster().getStatusCode().equals("RELATIVEADDRESS"))
//							.collect(Collectors.toList()) : null;
//					List<CandidateStatus> newRelList = new ArrayList<>();
//					for (CandidateStatus rel : cafList) {
//						String code = rel.getCandidate().getCandidateCode();
//						Long count = candidateCafAddressRepository.findCountByCandidateCodeAndRelAddrVerification(code);
//						if (count > 0) {
//							newRelList.add(rel);
//						}
//					}
//					candidateStatusCountDtoList.add(new CandidateStatusCountDto("CWF Pending", "CAFPENDING",
//							cafList != null ? newRelList.size() : 0));
//				} else if (configCodes.getData().contains("RELBILLFALSE") && configCodes.getData().contains("EPFO")) {
//					List<CandidateStatus> cafList = candidateStatusList != null ? candidateStatusList.stream()
//							.filter(c -> c.getStatusMaster().getStatusCode().equals("EPFO"))
//							.collect(Collectors.toList()) : null;
//					candidateStatusCountDtoList.add(new CandidateStatusCountDto("CWF Pending", "CAFPENDING",
//							cafList != null ? cafList.size() : 0));
//				} else if (configCodes.getData().contains("RELBILLFALSE") && !configCodes.getData().contains("EPFO")
//						&& configCodes.getData().contains("ITR")) {
//					List<CandidateStatus> cafList = candidateStatusList != null ? candidateStatusList.stream()
//							.filter(c -> c.getStatusMaster().getStatusCode().equals("ITR")).collect(Collectors.toList())
//							: null;
//					candidateStatusCountDtoList.add(new CandidateStatusCountDto("CWF Pending", "CAFPENDING",
//							cafList != null ? cafList.size() : 0));
//				} else if (configCodes.getData().contains("RELBILLFALSE") && !configCodes.getData().contains("EPFO")
//						&& !configCodes.getData().contains("ITR")) {
//					List<CandidateStatus> cafList = candidateStatusList != null ? candidateStatusList.stream()
//							.filter(c -> c.getStatusMaster().getStatusCode().equals("DIGILOCKER"))
//							.collect(Collectors.toList()) : null;
//					candidateStatusCountDtoList.add(new CandidateStatusCountDto("CWF Pending", "CAFPENDING",
//							cafList != null ? cafList.size() : 0));
//				}
//
//				List<CandidateStatus> canList = candidateStatusList != null ? candidateStatusList.stream()
//						.filter(c -> c.getStatusMaster().getStatusCode().equals("PROCESSDECLINED"))
//						.collect(Collectors.toList()) : null;
//				candidateStatusCountDtoList.add(new CandidateStatusCountDto(canStatusMaster.getStatusName(),
//						canStatusMaster.getStatusCode(), canList.size()));
				DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null,
						candidateStatusCountDtoList, dashboardDto.getUserId(), null, null,dashboardDto.getPageNumber());
				svcSearchResult.setData(dashboardDtoObj);
				svcSearchResult.setOutcome(true);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("please specify user.");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getPendingDetailsStatusAndCount method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<Candidate> saveIsFresher(String candidateCode, Boolean isFresher) {
		ServiceOutcome<Candidate> svcSearchResult = new ServiceOutcome<Candidate>();
		try {
			Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);
			candidate.setIsFresher(isFresher);
			candidateRepository.save(candidate);

			svcSearchResult.setData(candidate);
			svcSearchResult.setOutcome(true);
			svcSearchResult.setMessage("Data saved successfully.");

		} catch (Exception e) {
			log.error("Exception occured in saveIsFresher method in CandidateServiceImpl-->", e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<CandidationApplicationFormDto> candidateApplicationFormDetailsExceptCandidate(
			String candidateCode) {
		ServiceOutcome<CandidationApplicationFormDto> svcSearchResult = new ServiceOutcome<CandidationApplicationFormDto>();
		CandidationApplicationFormDto candidationApplicationFormDto = new CandidationApplicationFormDto();
		List<CandidateCafEducationDto> candidateCafEducationDtoList = new ArrayList<CandidateCafEducationDto>();
		List<CandidateCafExperienceDto> candidateCafExperienceDtoList = new ArrayList<CandidateCafExperienceDto>();
		List<CandidateCafAddressDto> candidateCafAddressDtoList = new ArrayList<CandidateCafAddressDto>();
		List<ITRDataFromApiDto> iTRDataFromApiDtoList = new ArrayList<ITRDataFromApiDto>();
		List<EpfoDataFromApiDto> ePFODataFromApiDtoList = new ArrayList<>();
		List<ContentFileDto> docDtoList = new ArrayList<ContentFileDto>();
		List<VendorUploadChecksDto> vendordocDtoList = new ArrayList<VendorUploadChecksDto>();
		CandidateFileDto candidateFileDto = null;
		ContentFileDto contentFileDto = null;
		VendorUploadChecksDto vendorUploadChecksDto = null;
		CandidateCaseDetailsDTO candidateCaseDetailsDTO = null;
		boolean isdigilocker=true;
		try {
			if (StringUtils.isNotEmpty(candidateCode)) {
				Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);
				Long candidateId = candidate.getCandidateId();
				// Added for KMPG 
				Organization organization = candidate.getOrganization();
				if(organization.getOrganizationName().equalsIgnoreCase("KPMG")){
					isdigilocker = false;
				}
				IdItemsDto candidateIdItemPan = candidateIdItemsRepository
						.findByCandidateCandidateCodeAndServiceSourceMasterServiceCode(candidateCode, "PAN");
				if (candidateIdItemPan != null) {
					candidate.setCandidateName(candidateIdItemPan.getIdHolder());
				} else {
//					IdItemsDto candidateIdItemAadhar = candidateIdItemsRepository
//							.findByCandidateCandidateCodeAndServiceSourceMasterServiceCode(candidateCode, "AADHARID");

					if (isdigilocker && Objects.nonNull(candidate.getAadharName())) {
						candidate.setCandidateName(candidate.getAadharName());
						entityManager.detach(candidate);
					}
				}
				candidationApplicationFormDto.setCandidate(candidate);
				List<CandidateCafEducation> candidateCafEducationList = candidateCafEducationRepository
						.findAllByCandidateCandidateCode(candidateCode);
				if (!candidateCafEducationList.isEmpty()) {
					candidateCafEducationDtoList = candidateCafEducationList.stream()
							.map(candidateCafEducation -> modelMapper.map(candidateCafEducation,
									CandidateCafEducationDto.class))
							.collect(Collectors.toList());
					candidationApplicationFormDto.setCandidateCafEducationDto(candidateCafEducationDtoList);
				}
				List<CandidateCafExperience> candidateCafExperienceList = candidateCafExperienceRepository
						.findAllByCandidateCandidateCodeOrderByInputDateOfJoiningDesc(candidateCode);

				List<CandidateCafExperience> efpolist = candidateCafExperienceList != null
						? candidateCafExperienceList.stream()
								.filter(c -> c.getServiceSourceMaster() != null
										&& c.getServiceSourceMaster().getServiceCode().equals("EPFO"))
								.collect(Collectors.toList())
						: new ArrayList<>();
				List<Long> itrIds = candidateCafExperienceList != null
						? candidateCafExperienceList.stream()
								.filter(c -> c.getServiceSourceMaster() != null
										&& c.getServiceSourceMaster().getServiceCode().equals("ITR"))
								.map(f -> f.getCandidateCafExperienceId()).collect(Collectors.toList())
						: new ArrayList<>();

				Boolean dojNull = false; // added to avoid null doj value while sorting
				for (CandidateCafExperience efpo : efpolist) {
					if (efpo.getInputDateOfJoining() == null) {
						dojNull = true;
					}
					Date inputJoiningDate = efpo.getInputDateOfJoining();
					Date inputExitDate = efpo.getInputDateOfExit() == null ? new Date() : efpo.getInputDateOfExit();
					List<Long> itrRemoveIdsList = candidateCafExperienceRepository
							.findByCandidateCodeAndServiceSourceMasterCodeAndDates(candidateCode, "ITR",
									inputJoiningDate, inputExitDate);
//					itrIds.removeAll(itrRemoveIdsList);
				}

				List<CandidateCafExperience> itrList = candidateCafExperienceList.stream()
						.filter(c -> itrIds.contains(c.getCandidateCafExperienceId())).collect(Collectors.toList());
				efpolist.addAll(itrList);
				List<CandidateCafExperience> newList = candidateCafExperienceList.stream()
						.filter(c -> c.getServiceSourceMaster() == null || c.getServiceSourceMaster().getServiceCode().equals("DNHDB")).collect(Collectors.toList());
				efpolist.addAll(newList);
				if (!dojNull) {
					efpolist.sort(Comparator.comparing(CandidateCafExperience::getInputDateOfJoining));
				}
				Collections.reverse(efpolist);
				if (!efpolist.isEmpty()) {
					Date dateWith1Days = null;
					Date doee = null;
					for (CandidateCafExperience candidateCafExperience : efpolist) {
						CandidateCafExperienceDto candidateCafExperienceDto = this.modelMapper
								.map(candidateCafExperience, CandidateCafExperienceDto.class);
						if (candidateCafExperience.getInputDateOfJoining() != null) {
							Date doj = candidateCafExperience.getInputDateOfJoining();
							Calendar cal = Calendar.getInstance();
							cal.setTime(doj);
//							cal.add(Calendar.DATE, 1);
							dateWith1Days = cal.getTime();
							candidateCafExperienceDto
									.setInputDateOfJoining(dateWith1Days != null ? sdf.format(dateWith1Days) : null);
							candidateCafExperienceDto
									.setOutputDateOfJoining(dateWith1Days != null ? sdf.format(dateWith1Days) : null);

						}
						if (candidateCafExperience.getInputDateOfExit() != null) {
							Date doe = candidateCafExperience.getInputDateOfExit();
							Calendar cal = Calendar.getInstance();
							cal.setTime(doe);
//							cal.add(Calendar.DATE, 1);
							doee = cal.getTime();
							candidateCafExperienceDto.setInputDateOfExit(doee != null ? sdf.format(doee) : null);

							candidateCafExperienceDto.setOutputDateOfExit(doee != null ? sdf.format(doee) : null);
						}
						if (candidateCafExperience.getOutputDateOfExit() != null) {
							Date odoe = candidateCafExperience.getOutputDateOfExit();
							Calendar cal = Calendar.getInstance();
							cal.setTime(odoe);
//							cal.add(Calendar.DATE, 1);
							Date odoee = cal.getTime();

							candidateCafExperienceDto.setOutputDateOfExit(odoee != null ? sdf.format(odoee) : null);
						}

						candidateCafExperienceDto.setServiceName(candidateCafExperience.getServiceSourceMaster() != null
								? candidateCafExperience.getServiceSourceMaster().getServiceName()
								: "NA");

						candidateCafExperienceDtoList.add(candidateCafExperienceDto);
					}

					candidationApplicationFormDto.setCandidateCafExperienceDto(candidateCafExperienceDtoList);
					List<String> uanNUmberList = candidateCafExperienceRepository
							.getCandidateUan(candidate.getCandidateId());
					String uanNumber = uanNUmberList.stream().map(uan -> uan.toString())
							.collect(Collectors.joining("/"));
					candidationApplicationFormDto.setCandidateUan(uanNumber);
				}
				List<CandidateCafAddress> candidateCafAddressList = candidateCafAddressRepository
						.findAllByCandidateCandidateCode(candidateCode);
				if (candidateCafAddressList != null) {
					candidateCafAddressDtoList = candidateCafAddressList.stream().map(
							candidateCafAddress -> modelMapper.map(candidateCafAddress, CandidateCafAddressDto.class))
							.collect(Collectors.toList());
					candidationApplicationFormDto.setCandidateCafAddressDto(candidateCafAddressDtoList);
				}
				List<ITRData> iTRDataList = itrDataRepository
						.findAllByCandidateCandidateCodeOrderByFiledDateDesc(candidateCode);
				if (!iTRDataList.isEmpty()) {
					iTRDataFromApiDtoList = iTRDataList.stream()
							.map(itr -> modelMapper.map(itr, ITRDataFromApiDto.class)).collect(Collectors.toList());
					candidationApplicationFormDto.setITRDataFromApiDto(iTRDataFromApiDtoList);
				}
				
				//Adding epfo dto for output service history table
				List<EpfoData> epfoDataList = epfoDataRepository.findAllByCandidateCandidateCode(candidateCode);
				if (!epfoDataList.isEmpty()) {
					for (EpfoData epfoData : epfoDataList) {
						EpfoDataFromApiDto epfoDataFromApiDto = this.modelMapper
								.map(epfoData, EpfoDataFromApiDto.class);
						//update remittance present flag to show remittance button in qc 
						if(epfoData.getIsRemittanceChecked()!=null && epfoData.getIsRemittanceChecked()) {
							candidationApplicationFormDto.setIsRemittancePresent(epfoData.getIsRemittanceChecked());
						}
						if (epfoData.getDoj() != null) {
							Date doj = epfoData.getDoj();
							Calendar cal = Calendar.getInstance();
							cal.setTime(doj);
							Date dateWith1Days = cal.getTime();
							log.info("EPFOdoj ::{}",dateWith1Days);
							epfoDataFromApiDto
									.setDoj(dateWith1Days != null ? sdf.format(dateWith1Days) : null);

						}
						if (epfoData.getDoe() != null) {
							Date doe = epfoData.getDoe();
							Calendar cal = Calendar.getInstance();
							cal.setTime(doe);
							Date doee = cal.getTime();
							log.info("EPFOdoee ::{}",doee);
							epfoDataFromApiDto.setDoe(doee != null ? sdf.format(doee) : null);
						}

						ePFODataFromApiDtoList.add(epfoDataFromApiDto);
					}

//					ePFODataFromApiDtoList = epfoDataList.stream()
//							.map(epfo -> modelMapper.map(epfo, EpfoDataFromApiDto.class)).collect(Collectors.toList());
					candidationApplicationFormDto.setEpfoDataFromApiDto(ePFODataFromApiDtoList);
				}
				
				
				//adding below section for Remittance images proof data
				List<RemittanceData> remittanceRecords = remittanceRepository.findAllByCandidateCandidateCode(candidateCode);
				List<RemittanceDataFromApiDto> dataDTOList = new ArrayList<>();
				for(RemittanceData remittanceData : remittanceRecords) {
					RemittanceDataFromApiDto remittanceDataFromApiDto=new RemittanceDataFromApiDto();
		            remittanceDataFromApiDto.setCandidateCode(candidateCode);
		            remittanceDataFromApiDto.setColor(remittanceData.getColor().getColorCode());
		            remittanceDataFromApiDto.setCompany(remittanceData.getCompany());
		            remittanceDataFromApiDto.setCreatedOn(remittanceData.getCreatedOn());
		            remittanceDataFromApiDto.setImage(Base64.getEncoder().encodeToString(remittanceData.getImage()));
		            remittanceDataFromApiDto.setMemberId(remittanceData.getMemberId());
		            remittanceDataFromApiDto.setName(remittanceData.getName());
		            remittanceDataFromApiDto.setYear(remittanceData.getYear());
		            
		            dataDTOList.add(remittanceDataFromApiDto);
				}
				candidationApplicationFormDto.setRemittanceProofImagesData(dataDTOList!=null && !dataDTOList.isEmpty() ? dataDTOList : null);
				//end
				
				CandidateResumeUpload candidateResume = candidateResumeUploadRepository
						.findByCandidateCandidateCode(candidateCode);
				if (candidateResume != null && Objects.nonNull(candidateResume.getContentId())) {
					String resumeUrl = contentService.getFileUrlFromContentId(candidateResume.getContentId());
					candidationApplicationFormDto.setCandidateResumeUrl(resumeUrl);
				} else if (candidateResume != null) {
					candidateFileDto = new CandidateFileDto(candidateResume.getCandidateResumeUploadId(),
							candidateResume.getCandidateResume(), null);
					candidationApplicationFormDto.setCandidateResume(candidateFileDto);
				}
				CandidateCaseDetails candidateCaseDetails = candidateCaseDetailsRepository
						.findByCandidateCandidateCode(candidateCode);
				if (candidateCaseDetails != null) {
					if (candidateCaseDetails.getCriminalVerificationisExist() != null) {
						candidateFileDto = new CandidateFileDto(candidateCaseDetails.getCandidateCaseDetailsId(),
								candidateCaseDetails.getCriminalVerificationDocument(),
								candidateCaseDetails.getCriminalVerificationisExist().getColorName());
						candidationApplicationFormDto.setCaseDetails(candidateFileDto);
					}
					if (candidateCaseDetails.getGlobalDatabaseCaseDetailsIsExist() != null) {
						candidateFileDto = new CandidateFileDto(candidateCaseDetails.getCandidateCaseDetailsId(),
								candidateCaseDetails.getGlobalDatabaseCaseDetailsDocument(),
								candidateCaseDetails.getGlobalDatabaseCaseDetailsIsExist().getColorName());
						candidationApplicationFormDto.setGlobalDatabaseCaseDetails(candidateFileDto);
					}
				}
				List<VendorChecks> vendorList = vendorChecksRepository.findAllByCandidateCandidateId(candidateId);
				for (VendorChecks vendorChecks : vendorList) {
					User user = userRepository.findByUserId(vendorChecks.getVendorId());
					VendorUploadChecks vendorChecksss = vendorUploadChecksRepository
							.findByVendorChecksVendorcheckId(vendorChecks.getVendorcheckId());
					if (vendorChecksss != null) {
						vendorUploadChecksDto = new VendorUploadChecksDto(user.getUserFirstName(),
								vendorChecksss.getVendorChecks().getVendorcheckId(),
								vendorChecksss.getVendorUploadedDocument(), vendorChecksss.getDocumentname(),
								vendorChecksss.getAgentColor().getColorName(),
								vendorChecksss.getAgentColor().getColorHexCode(), null,null);
						vendordocDtoList.add(vendorUploadChecksDto);

					}
					candidationApplicationFormDto.setVendorProofDetails(vendordocDtoList);

				}

				List<Content> contentDetails = contentRepository.findAllByCandidateId(candidateId);
				byte[] document;

				if (contentDetails != null) {
					for (Content content : contentDetails) {
						contentFileDto = new ContentFileDto(content.getContentId(), content.getDocument(),
								content.getContentSubCategory());
						docDtoList.add(contentFileDto);

					}
					candidationApplicationFormDto.setDocument(docDtoList);

				}
				CandidateAddComments candidateAddComments = candidateAddCommentRepository
						.findByCandidateCandidateId(candidateId);
				if (candidateAddComments != null) {
					candidationApplicationFormDto.setCandidateAddComments(candidateAddComments);
				}

				CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(candidateCode);
				candidationApplicationFormDto.setCandidateStatus(candidateStatus);

				List<CandidateIdItems> candidateIdItems = candidateIdItemsRepository
						.findByCandidateCandidateCode(candidateCode);
				candidationApplicationFormDto.setCandidateIdItems(candidateIdItems);

				CandidateEmailStatus candidateEmailStatus = candidateEmailStatusRepository
						.findByCandidateCandidateCode(candidate.getCandidateCode());
				candidationApplicationFormDto.setEmailStatus(candidateEmailStatus);

				StringBuilder query = new StringBuilder();

				query.append(
						"select distinct srcId,srcName,count(distinct case when svcName !='' then  item end) as servicecount,\n");
				query.append("REPLACE(GROUP_CONCAT(DISTINCT(svcName)),',','/'), ");
				query.append(
						"count(distinct smm1.source_service_id) as totalcount, count(distinct case when svcName ='' then  item1 end) as nullcount\n");
				query.append("from\n");
				query.append("(\n");
				query.append(
						"select distinct tds.source_id as srcId,tds.source_name as srcName,service_name as svcName,\n");
				query.append("cii.items_id as item,0 as item1\n");
				query.append("from t_dgv_candidate_id_items cii,\n");
				query.append(
						"t_dgv_source_service_master ssm,t_dgv_service_type_config_master tstcm,t_dgv_source tds,\n");
				query.append("t_dgv_service_master tdsm\n");
				query.append(
						"where cii.service_source_master_id=ssm.source_service_id and cii.candidate_id =:candidateId\n");
				query.append("and ssm.source_id =tds.source_id \n");
				query.append("and tds.source_id =ssm.source_id and ssm.source_service_id =tstcm.source_service_id \n");
				query.append("and tdsm.organization_id =tstcm.organization_id and tdsm.source_id=tds.source_id \n");
				query.append("and tdsm.organization_id=:organizationId\n");
				query.append("and tstcm.organization_id =:organizationId\n");
				query.append("union \n");
				query.append(
						"select distinct tds.source_id as srcId,tds.source_name as srcName,service_name as svcName,\n");
				query.append("caa.candidate_caf_address_id as item,0 as item1\n");
				query.append("from  t_dgv_candidate_caf_address caa,\n");
				query.append(
						"t_dgv_source_service_master ssm,t_dgv_service_type_config_master tstcm,t_dgv_source tds,\n");
				query.append("t_dgv_service_master tdsm\n");
				query.append("where caa.source_service_id =ssm.source_service_id\n");
				query.append("and caa.candidate_id =:candidateId\n");
				query.append("and ssm.source_id =tds.source_id \n");
				query.append("and tds.source_id =ssm.source_id and ssm.source_service_id =tstcm.source_service_id \n");
				query.append("and tdsm.organization_id =tstcm.organization_id and tdsm.source_id=tds.source_id \n");
				query.append("and tdsm.organization_id=:organizationId\n");
				query.append("and tstcm.organization_id =:organizationId\n");
				query.append("and caa.source_service_id is not null\n");
				query.append("union \n");
				query.append("select tds.source_id as srcId,tds.source_name as srcName,'' as svcName,\n");
				query.append("0 as item, ca.candidate_caf_address_id as item1\n");
				query.append("from  t_dgv_candidate_caf_address ca, t_dgv_source tds\n");
				query.append("where (ca.source_service_id is null or ca.source_service_id=null)\n");
				query.append("and ca.candidate_id =:candidateId and source_code='ADDRESS'\n");
				query.append("union \n");
				query.append(
						"select distinct tds.source_id as srcId,tds.source_name as srcName,service_name as svcName,\n");
				query.append("ce.candidate_caf_education_id as item,0 as item1\n");
				query.append("from  t_dgv_candidate_caf_education ce,\n");
				query.append(
						"t_dgv_source_service_master ssm,t_dgv_service_type_config_master tstcm,t_dgv_source tds,\n");
				query.append("t_dgv_service_master tdsm\n");
				query.append("where ce.source_service_id =ssm.source_service_id\n");
				query.append("and ce.candidate_id =:candidateId\n");
				query.append("and ssm.source_id =tds.source_id \n");
				query.append("and ssm.source_service_id =tstcm.source_service_id \n");
				query.append("and tdsm.organization_id =tstcm.organization_id and tdsm.source_id=tds.source_id \n");
				query.append("and tdsm.organization_id=:organizationId\n");
				query.append("and tstcm.organization_id =:organizationId\n");
				query.append("and ce.source_service_id is not null\n");
				query.append("Union\n");
				query.append("select tds.source_id as srcId,tds.source_name as srcName,'' as svcName,\n");
				query.append("0 as item,cce.candidate_caf_education_id as item1  \n");
				query.append("from     t_dgv_candidate_caf_education cce , t_dgv_source tds\n");
				query.append("where (cce.source_service_id is null or cce.source_service_id=null)\n");
				query.append("and cce.candidate_id =:candidateId and source_code='EDUCATION'\n");
				query.append("union \n");
				query.append(
						"select distinct tds.source_id as srcId,tds.source_name as srcName,service_name as svcName,\n");
				query.append("ce.candidate_caf_education_id as item,0 as item1\n");
				query.append("from  t_dgv_candidate_caf_education ce,\n");
				query.append(
						"t_dgv_source_service_master ssm,t_dgv_service_type_config_master tstcm,t_dgv_source tds,\n");
				query.append("t_dgv_service_master tdsm\n");
				query.append("where ce.source_service_id =ssm.source_service_id\n");
				query.append("and ce.candidate_id =:candidateId\n");
				query.append("and ssm.source_id =tds.source_id \n");
				query.append("and ssm.source_service_id =tstcm.source_service_id \n");
				query.append("and tdsm.organization_id =tstcm.organization_id and tdsm.source_id=tds.source_id \n");
				query.append("and tdsm.organization_id=:organizationId\n");
				query.append("and tstcm.organization_id =:organizationId\n");
				query.append("and ce.source_service_id is not null\n");
				query.append("union \n");
				query.append(
						"select distinct tds.source_id as srcId,tds.source_name as srcName,service_name as svcName,\n");
				query.append("cx.candidate_caf_experience_id as item,0 as item1\n");
				query.append("from  t_dgv_candidate_caf_experience cx,\n");
				query.append(
						"t_dgv_source_service_master ssm,t_dgv_service_type_config_master tstcm,t_dgv_source tds,\n");
				query.append("t_dgv_service_master tdsm\n");
				query.append("where cx.service_source_master_id =ssm.source_service_id\n");
				query.append("and cx.candidate_id =:candidateId\n");
				query.append("and ssm.source_id =tds.source_id \n");
				query.append("and ssm.source_service_id =tstcm.source_service_id \n");
				query.append("and tdsm.organization_id =tstcm.organization_id and tdsm.source_id=tds.source_id \n");
				query.append("and tdsm.organization_id=:organizationId\n");
				query.append("and tstcm.organization_id =:organizationId\n");
				query.append("and cx.service_source_master_id is not null\n");
				query.append("union \n");
				query.append("select tds.source_id as srcId,tds.source_name as srcName,'' as svcName,\n");
				query.append("0 as item,cex.candidate_caf_experience_id as item1  \n");
				query.append("from    t_dgv_candidate_caf_experience  cex , t_dgv_source tds\n");
				query.append("where (cex.service_source_master_id is null or cex.service_source_master_id=null)\n");
				query.append("and cex.candidate_id =:candidateId and source_code='EMPLOYMENTS'\n");
				query.append(") t, t_dgv_source_service_master smm1\n");
				query.append("where smm1.source_id =t.srcId\n");
				query.append("group  by srcId,srcName\n");

				Query resultQuery = entityManager.createNativeQuery(query.toString());
				resultQuery.setParameter("candidateId", candidate.getCandidateId());
				resultQuery.setParameter("organizationId", candidate.getOrganization().getOrganizationId());

				List<Object[]> executiveSummaryList = resultQuery.getResultList();
				List<ExecutiveSummaryDto> executiveSummaryDtoList = new ArrayList<ExecutiveSummaryDto>();
				Color colGreen = colorRepository.findByColorCode("GREEN");
				Color colAmber = colorRepository.findByColorCode("AMBER");
				Color colRed = colorRepository.findByColorCode("RED");
				List<String> configCodes = getServiceConfigCodes(candidateCode, null).getData();

				for (Object[] executiveSummary : executiveSummaryList) {
					ExecutiveSummaryDto executiveSummaryDto = new ExecutiveSummaryDto();
					executiveSummaryDto.setVerificationItem(executiveSummary[1].toString());
//					System.out.println("caseDetails");
					CandidateCaseDetails caseDetails = candidateCaseDetailsRepository
							.findByCandidateCandidateCode(candidateCode);
//					System.out.println("caseDetailsssssssssss" + caseDetails);
					switch (executiveSummary[1].toString()) {
					case "Employments":
						if (!executiveSummary[3].toString().equals("")
								&& executiveSummary[3].toString().indexOf("/") == 0) {
							executiveSummaryDto.setSource(executiveSummary[3].toString().substring(1));
						} else if (!executiveSummary[3].toString().equals("")
								&& executiveSummary[3].toString().indexOf("/") != 0) {
							executiveSummaryDto.setSource(executiveSummary[3].toString());
						} else {
							executiveSummaryDto.setSource("Not-Available");
						}

						List<CandidateCafExperience> redColorList = candidateCafExperienceList.stream()
								.filter(c -> c.getColor().getColorCode().equals("RED")).collect(Collectors.toList());
						if (redColorList != null && redColorList.size() > 0) {
							executiveSummaryDto.setResult(colRed.getColorName());
							executiveSummaryDto.setColorCode(colRed.getColorCode());
							executiveSummaryDto.setColorHexCode(colRed.getColorHexCode());
						} else {
							List<CandidateCafExperience> amberColorList = candidateCafExperienceList.stream()
									.filter(c -> c.getColor().getColorCode().equals("AMBER"))
									.collect(Collectors.toList());
							if (amberColorList != null && amberColorList.size() > 0) {
								executiveSummaryDto.setResult(colAmber.getColorName());
								executiveSummaryDto.setColorCode(colAmber.getColorCode());
								executiveSummaryDto.setColorHexCode(colAmber.getColorHexCode());
							} else {
								executiveSummaryDto.setResult(colGreen.getColorName());
								executiveSummaryDto.setColorCode(colGreen.getColorCode());
								executiveSummaryDto.setColorHexCode(colGreen.getColorHexCode());
							}
						}
						break;
					case "Education":
						if (!executiveSummary[3].toString().equals("")
								&& executiveSummary[3].toString().indexOf("/") == 0) {
							executiveSummaryDto.setSource(executiveSummary[3].toString().substring(1));
						} else if (!executiveSummary[3].toString().equals("")
								&& executiveSummary[3].toString().indexOf("/") != 0) {
							executiveSummaryDto.setSource(executiveSummary[3].toString());
						} else {
							executiveSummaryDto.setSource("Not-Available");
						}
						List<CandidateCafEducation> redColorEduList = candidateCafEducationList.stream()
								.filter(c -> c.getColor().getColorCode().equals("RED")).collect(Collectors.toList());
						if (redColorEduList != null && redColorEduList.size() > 0) {
							executiveSummaryDto.setResult(colRed.getColorName());
							executiveSummaryDto.setColorCode(colRed.getColorCode());
							executiveSummaryDto.setColorHexCode(colRed.getColorHexCode());
						} else {
							List<CandidateCafEducation> amberColorEduList = candidateCafEducationList.stream()
									.filter(c -> c.getColor().getColorCode().equals("AMBER"))
									.collect(Collectors.toList());
							if (amberColorEduList != null && amberColorEduList.size() > 0) {
								executiveSummaryDto.setResult(colAmber.getColorName());
								executiveSummaryDto.setColorCode(colAmber.getColorCode());
								executiveSummaryDto.setColorHexCode(colAmber.getColorHexCode());
							} else {
								executiveSummaryDto.setResult(colGreen.getColorName());
								executiveSummaryDto.setColorCode(colGreen.getColorCode());
								executiveSummaryDto.setColorHexCode(colGreen.getColorHexCode());
							}
						}
						break;
					case "Global Database check":
						if (caseDetails != null && caseDetails.getGlobalDatabaseCaseDetailsIsExist() != null) {
							executiveSummaryDto
									.setResult(caseDetails.getGlobalDatabaseCaseDetailsIsExist().getColorName());
							executiveSummaryDto
									.setColorCode(caseDetails.getGlobalDatabaseCaseDetailsIsExist().getColorCode());
							executiveSummaryDto.setColorHexCode(
									caseDetails.getGlobalDatabaseCaseDetailsIsExist().getColorHexCode());
						} else {
							executiveSummaryDto.setResult(colAmber.getColorName());
							executiveSummaryDto.setColorCode(colAmber.getColorCode());
							executiveSummaryDto.setColorHexCode(colAmber.getColorHexCode());
						}
						break;
					case "Criminal":
						if (caseDetails != null && caseDetails.getCriminalVerificationisExist() != null) {
							executiveSummaryDto.setResult(caseDetails.getCriminalVerificationisExist().getColorName());
							executiveSummaryDto
									.setColorCode(caseDetails.getCriminalVerificationisExist().getColorCode());
							executiveSummaryDto
									.setColorHexCode(caseDetails.getCriminalVerificationisExist().getColorHexCode());
						} else {
							executiveSummaryDto.setResult(colAmber.getColorName());
							executiveSummaryDto.setColorCode(colAmber.getColorCode());
							executiveSummaryDto.setColorHexCode(colAmber.getColorHexCode());
						}
						break;
					case "Address":
						if (!executiveSummary[3].toString().equals("")
								&& executiveSummary[3].toString().indexOf("/") == 0) {
							executiveSummaryDto.setSource(executiveSummary[3].toString().substring(1));
						} else if (!executiveSummary[3].toString().equals("")
								&& executiveSummary[3].toString().indexOf("/") != 0) {
							executiveSummaryDto.setSource(executiveSummary[3].toString());
						} else {
							executiveSummaryDto.setSource("Not-Available");
						}
						List<CandidateCafAddress> redColorAddrList = candidateCafAddressList.stream()
								.filter(c -> c.getColor().getColorCode().equals("RED")).collect(Collectors.toList());
						if (redColorAddrList != null && redColorAddrList.size() > 0) {
							executiveSummaryDto.setResult(colRed.getColorName());
							executiveSummaryDto.setColorCode(colRed.getColorCode());
							executiveSummaryDto.setColorHexCode(colRed.getColorHexCode());
						} else {
							List<CandidateCafAddress> amberColorAddrList = candidateCafAddressList.stream()
									.filter(c -> c.getColor().getColorCode().equals("AMBER"))
									.collect(Collectors.toList());
							if (amberColorAddrList != null && amberColorAddrList.size() > 0) {
								executiveSummaryDto.setResult(colAmber.getColorName());
								executiveSummaryDto.setColorCode(colAmber.getColorCode());
								executiveSummaryDto.setColorHexCode(colAmber.getColorHexCode());
							} else {
								executiveSummaryDto.setResult(colGreen.getColorName());
								executiveSummaryDto.setColorCode(colGreen.getColorCode());
								executiveSummaryDto.setColorHexCode(colGreen.getColorHexCode());
							}
						}
						break;
					case "ID Items":

						if (!executiveSummary[3].toString().equals("")
								&& executiveSummary[3].toString().indexOf("/") == 0) {

							executiveSummaryDto.setSource(executiveSummary[3].toString().substring(1));
						} else if (!executiveSummary[3].toString().equals("")
								&& executiveSummary[3].toString().indexOf("/") != 0) {
							executiveSummaryDto.setSource(executiveSummary[3].toString());
						} else {
							executiveSummaryDto.setSource("Not-Available");
						}
						if (Long.parseLong(executiveSummary[5].toString()) == 0) {
							executiveSummaryDto.setResult(colGreen.getColorName());
							executiveSummaryDto.setColorCode(colGreen.getColorCode());
							executiveSummaryDto.setColorHexCode(colGreen.getColorHexCode());
						} else {
							executiveSummaryDto.setResult(colAmber.getColorName());
							executiveSummaryDto.setColorCode(colAmber.getColorCode());
							executiveSummaryDto.setColorHexCode(colAmber.getColorHexCode());
						}
						break;
					}

					executiveSummaryDtoList.add(executiveSummaryDto);

				}
				if (configCodes.contains("CRIMINAL")) {
					if (candidateCaseDetails != null) {
						ExecutiveSummaryDto executiveSummaryDto = new ExecutiveSummaryDto();
						executiveSummaryDto.setVerificationItem("Criminal");

						if (candidateCaseDetails.getCreatedBy() != null)
							if (candidateCaseDetails.getCreatedBy().getRole() != null)
								executiveSummaryDto
										.setSource(candidateCaseDetails.getCreatedBy().getRole().getRoleName());

						if (candidateCaseDetails.getCriminalVerificationisExist() != null)
							executiveSummaryDto
									.setResult(candidateCaseDetails.getCriminalVerificationisExist().getColorName());

						if (candidateCaseDetails.getCriminalVerificationisExist() != null)
							executiveSummaryDto
									.setColorCode(candidateCaseDetails.getCriminalVerificationisExist().getColorCode());

						if (candidateCaseDetails.getCriminalVerificationisExist() != null)
							executiveSummaryDto.setColorHexCode(
									candidateCaseDetails.getCriminalVerificationisExist().getColorHexCode());

						executiveSummaryDtoList.add(executiveSummaryDto);
					} else {
						ExecutiveSummaryDto executiveSummaryDto = new ExecutiveSummaryDto();
						executiveSummaryDto.setVerificationItem("Criminal");
						executiveSummaryDto.setSource("Pending");
						executiveSummaryDto.setResult("");// colAmber.getColorName()
						executiveSummaryDto.setColorCode("");// colAmber.getColorCode()
						executiveSummaryDto.setColorHexCode("");// colAmber.getColorHexCode()
						executiveSummaryDtoList.add(executiveSummaryDto);
					}
				}
				if (configCodes.contains("GLOBAL")) {
					if (candidateCaseDetails != null) {
						ExecutiveSummaryDto executiveSummaryDto = new ExecutiveSummaryDto();
						executiveSummaryDto.setVerificationItem("Global Database check");

						if (candidateCaseDetails.getCreatedBy() != null)
							if (candidateCaseDetails.getCreatedBy().getRole() != null)
								executiveSummaryDto
										.setSource(candidateCaseDetails.getCreatedBy().getRole().getRoleName());

						if (candidateCaseDetails.getGlobalDatabaseCaseDetailsIsExist() != null)
							executiveSummaryDto.setResult(
									candidateCaseDetails.getGlobalDatabaseCaseDetailsIsExist().getColorName());

						if (candidateCaseDetails.getGlobalDatabaseCaseDetailsIsExist() != null)
							executiveSummaryDto.setColorCode(
									candidateCaseDetails.getGlobalDatabaseCaseDetailsIsExist().getColorCode());

						if (candidateCaseDetails.getGlobalDatabaseCaseDetailsIsExist() != null)
							executiveSummaryDto.setColorHexCode(
									candidateCaseDetails.getGlobalDatabaseCaseDetailsIsExist().getColorHexCode());

						executiveSummaryDtoList.add(executiveSummaryDto);
					} else {
						ExecutiveSummaryDto executiveSummaryDto = new ExecutiveSummaryDto();
						executiveSummaryDto.setVerificationItem("Global Database check");
						executiveSummaryDto.setSource("Pending");
						executiveSummaryDto.setResult("");// colAmber.getColorName()
						executiveSummaryDto.setColorCode("");// colAmber.getColorCode()
						executiveSummaryDto.setColorHexCode("");// colAmber.getColorHexCode()
						executiveSummaryDtoList.add(executiveSummaryDto);
					}
				}
				List<EmploymentDetailsDto> employmentDetailsDto = new ArrayList<EmploymentDetailsDto>();
				int i = 0;
				for (CandidateCafExperience experience : efpolist) {
					String inputTenure = "";
					String outputTenure = "";
					Date idoj = experience.getInputDateOfJoining();
					Date idoe = experience.getInputDateOfExit();

					String gap = "0y 0m";

					if (idoj == null) {
						inputTenure = 0 + "y " + 0 + "m";
					} else {
						LocalDate inputdoj = idoj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						LocalDate inputdoe = idoe == null ? LocalDate.now()
								: idoe.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						Period inputdiff = Period.between(inputdoj, inputdoe);

						int years = inputdiff.getYears();
						int months = inputdiff.getMonths();

						if (inputdiff.getDays() > 0) {
							months += 1;
						}
						inputTenure = years + "y " + months + "m";
					}

					Date odoj = experience.getOutputDateOfJoining();
					Date odoe = experience.getOutputDateOfExit();

					if (odoj == null) {
						outputTenure = 0 + "y " + 0 + "m";
					} else {
						LocalDate outputdoj = odoj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						LocalDate outputdoe = odoe == null ? LocalDate.now()
								: odoe.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						Period outputdiff = Period.between(outputdoj, outputdoe);
						int oyears = outputdiff.getYears();
						int omonths = outputdiff.getMonths();
						if (outputdiff.getDays() > 0) {
							omonths += 1;
						}
						outputTenure = oyears + "y " + omonths + "m";

						if (i < efpolist.size()) {
							CandidateCafExperience experience1 = efpolist.get(i);
							if (i + 1 < efpolist.size() && experience1.getInputDateOfJoining() != null) {
								CandidateCafExperience experience2 = efpolist.get(i + 1);
								LocalDate experience1Inputdoj = experience1.getInputDateOfJoining().toInstant()
										.atZone(ZoneId.systemDefault()).toLocalDate();
								LocalDate experience2Outputdoe = experience2.getOutputDateOfExit() == null
										? LocalDate.now()
										: experience2.getOutputDateOfExit().toInstant().atZone(ZoneId.systemDefault())
												.toLocalDate();
								Period gapPeriod = Period.between(experience2Outputdoe, experience1Inputdoj);
								int gapYears = gapPeriod.getYears();
								int gapMonths = gapPeriod.getMonths();
								if (gapPeriod.getDays() > 0) {
									gapMonths += 1;
								}
								gap = gapYears + "y " + gapMonths + "m";
							}
							i++;
						}
					}

					EmploymentDetailsDto employmentDetails = new EmploymentDetailsDto(experience, inputTenure,
							outputTenure, gap);
					employmentDetailsDto.add(employmentDetails);

				}
				candidationApplicationFormDto.setEmploymentDetails(employmentDetailsDto);
				tenureSum(employmentDetailsDto, candidationApplicationFormDto);
				gapSum(employmentDetailsDto, candidationApplicationFormDto);
				candidationApplicationFormDto.setExecutiveSummary(executiveSummaryDtoList);

				candidationApplicationFormDto.setOrganisationScope(
						organisationScopeRepository.findByCandidateId(candidate.getCandidateId()));

				svcSearchResult.setData(candidationApplicationFormDto);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("Data retrieved successfully.");
			}
		} catch (Exception e) {
			log.error(
					"Exception occured in candidateApplicationFormDetailsExceptCandidate method in CandidateServiceImpl-->",
					e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<CandidateCafExperience> updateCandidateExperience(
			CandidateCafExperienceDto candidateCafExperienceDto) {
		ServiceOutcome<CandidateCafExperience> outcome = new ServiceOutcome<CandidateCafExperience>();
		try {
			Candidate candidate = candidateRepository.findByCandidateCode(candidateCafExperienceDto.getCandidateCode());
			if (candidate != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				CandidateCafExperience experience = candidateCafExperienceRepository
						.findById(candidateCafExperienceDto.getCandidateCafExperienceId()).get();
				experience.setInputDateOfJoining(candidateCafExperienceDto.getInputDateOfJoining() != null
						? sdf.parse(candidateCafExperienceDto.getInputDateOfJoining())
						: experience.getInputDateOfJoining());
				experience.setInputDateOfExit(candidateCafExperienceDto.getInputDateOfExit() != null
						? sdf.parse(candidateCafExperienceDto.getInputDateOfExit())
						: experience.getInputDateOfExit());
				experience.setColor(colorRepository.findByColorCode("AMBER"));
				candidateCafExperienceRepository.save(experience);

				outcome.setData(experience);
				outcome.setOutcome(true);
				outcome.setMessage("Experience updated successfully.");
			} else {
				outcome.setData(null);
				outcome.setOutcome(false);
				outcome.setMessage("Candidate not found.");
			}
		} catch (Exception e) {
			log.error("Exception occured in updateCandidateExperience method in CandidateServiceImpl-->", e);
			outcome.setData(null);
			outcome.setOutcome(false);
			outcome.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return outcome;
	}

	@Override
	public ServiceOutcome<CandidateCafAddress> saveCandidateAddress(CandidateCafAddressDto candidateCafAddressDto) {
		ServiceOutcome<CandidateCafAddress> outcome = new ServiceOutcome<CandidateCafAddress>();
		try {
			Candidate candidate = candidateRepository.findByCandidateCode(candidateCafAddressDto.getCandidateCode());
			if (candidate != null) {
				if (candidateCafAddressDto.getCandidateCafAddressId() == null) {
					CandidateCafAddress candidateCafAddress = new CandidateCafAddress();
					BeanUtils.copyProperties(candidateCafAddressDto, candidateCafAddress);
					candidateCafAddress.setCandidate(candidate);
					candidateCafAddress.setName(candidate.getCandidateName());
					candidateCafAddress.setCreatedOn(new Date());
					candidateCafAddress.setColor(colorRepository.findByColorCode("AMBER"));
					candidateCafAddress = candidateCafAddressRepository.save(candidateCafAddress);
					outcome.setData(candidateCafAddress);
					outcome.setOutcome(true);
					outcome.setMessage("Address saved successfully.");

				} else {
					CandidateCafAddress candidateCafAddress = candidateCafAddressRepository
							.findById(candidateCafAddressDto.getCandidateCafAddressId()).get();
					candidateCafAddress.setCandidate(candidate);
					candidateCafAddress.setCandidateAddress(candidateCafAddressDto.getCandidateAddress());
					candidateCafAddress.setPinCode(candidateCafAddressDto.getPinCode());
					candidateCafAddress.setCity(candidateCafAddressDto.getCity());
					candidateCafAddress.setName(candidate.getCandidateName());
					candidateCafAddress.setLastUpdatedOn(new Date());
					candidateCafAddress = candidateCafAddressRepository.save(candidateCafAddress);
					outcome.setData(candidateCafAddress);
					outcome.setOutcome(true);
					outcome.setMessage("Address updated successfully.");
				}

			} else {
				outcome.setData(null);
				outcome.setOutcome(false);
				outcome.setMessage("Candidate not found.");
			}

		} catch (Exception e) {
			log.error("Exception occured in saveCandidateAddress method in CandidateServiceImpl-->", e);
			outcome.setData(null);
			outcome.setOutcome(false);
			outcome.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return outcome;
	}

	@Override
	public ServiceOutcome<List<String>> getServiceConfigCodes(String candidateCode, Long orgId) {
		ServiceOutcome<List<String>> outcome = new ServiceOutcome<List<String>>();
		try {
			if (candidateCode != null && !candidateCode.equals("")) {
				Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);
				orgId = candidate.getOrganization().getOrganizationId();
			}

			List<String> serviceTypeConfig = serviceTypeConfigRepository.getServiceSourceMasterByOrgId(orgId);
			String relutilityString = toleranceConfigRepository.findByOrganizationOrganizationId(orgId)
					.getAccessToRelativesBill() ? "RELBILLTRUE" : "RELBILLFALSE";
			serviceTypeConfig.add(relutilityString);
			outcome.setData(serviceTypeConfig);
			outcome.setOutcome(true);
			outcome.setMessage("List of services.");
		} catch (Exception e) {
			log.error("Exception occured in getServiceConfigCodes method in CandidateServiceImpl-->", e);
			outcome.setData(null);
			outcome.setOutcome(false);
			outcome.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return outcome;
	}

	@Transactional
	@Override
	public CandidateStatusHistory createCandidateStatusHistory(CandidateStatus candidateStatus, String who) {
		CandidateStatusHistory candidateStatusHistoryObj = new CandidateStatusHistory();
		try {
			candidateStatusHistoryObj.setCandidate(candidateStatus.getCandidate());
			candidateStatusHistoryObj.setStatusMaster(candidateStatus.getStatusMaster());
			if (who.equals("NOTCANDIDATE")) {
				candidateStatusHistoryObj.setCreatedBy(SecurityHelper.getCurrentUser());
			} else {
				candidateStatusHistoryObj.setCreatedBy(candidateStatus.getCandidate().getCreatedBy());
			}
			candidateStatusHistoryObj.setCreatedOn(new Date());
			candidateStatusHistoryObj.setCandidateStatusChangeTimestamp(new Date());
			candidateStatusHistoryRepository.save(candidateStatusHistoryObj);
		} catch (Exception ex) {
			log.error("Exception occured in createCandidateStatusHistory method in CandidateServiceImpl-->", ex);
		}
		return candidateStatusHistoryObj;
	}

	@Override
	public ServiceOutcome<Candidate> setIsLoaAccepted(String candidateCode) {
		ServiceOutcome<Candidate> svcSearchResult = new ServiceOutcome<Candidate>();
		try {
			Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);

			if (candidate != null) {
				candidate.setIsLoaAccepted(true);
				candidateRepository.save(candidate);
				svcSearchResult.setData(candidate);
				svcSearchResult.setOutcome(true);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("No candidate found");
			}
		} catch (Exception ex) {
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(true);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
			log.error("Exception occured in findByCandidateCode method in CandidateServiceImpl-->", ex);
		}
		return svcSearchResult;
	}

	public CandidationApplicationFormDto tenureSum(List<EmploymentDetailsDto> employmentDetailsDto,
			CandidationApplicationFormDto candidationApplicationFormDto) {
		Integer opTenureYears = 0;
		Integer opTenureMonths = 0;
		Integer ipTenureYears = 0;
		Integer ipTenureMonths = 0;
		for (EmploymentDetailsDto employmentDetails : employmentDetailsDto) {
			if (!employmentDetails.getOutputTenure().equals("Not-Available")) {
				String[] data = employmentDetails.getOutputTenure().split(" ");
				String yearString = data[0];
				String monthString = data[1];
				Integer years = Integer.parseInt(yearString.replace("y", ""));
				Integer months = Integer.parseInt(monthString.replace("m", ""));
				opTenureYears = opTenureYears + years;
				opTenureMonths = opTenureMonths + months;
			}
		}

		for (EmploymentDetailsDto employmentDetails : employmentDetailsDto) {
			if (!employmentDetails.getInputTenure().equals("Not-Available")) {
				String[] data = employmentDetails.getInputTenure().split(" ");
				String yearString = data[0];
				String monthString = data[1];
				Integer years = Integer.parseInt(yearString.replace("y", ""));
				Integer months = Integer.parseInt(monthString.replace("m", ""));
				ipTenureYears = ipTenureYears + years;
				ipTenureMonths = ipTenureMonths + months;
			}
		}

		if (opTenureMonths >= 12) {
			Period period = Period.ofMonths(opTenureMonths).normalized();
			int years = period.getYears();
			int months = period.getMonths();
			opTenureYears = opTenureYears + years;
			opTenureMonths = months;

		}

		if (ipTenureMonths >= 12) {
			Period period = Period.ofMonths(ipTenureMonths).normalized();
			int years = period.getYears();
			int months = period.getMonths();
			ipTenureYears = ipTenureYears + years;
			ipTenureMonths = months;

		}

		String outputTenureSum = opTenureYears.toString() + "y " + opTenureMonths.toString() + "m";
		String inputTenureSum = ipTenureYears.toString() + "y " + ipTenureMonths.toString() + "m";
		candidationApplicationFormDto.setOutputTenureSum(outputTenureSum);
		candidationApplicationFormDto.setInputTenureSum(inputTenureSum);
		return candidationApplicationFormDto;
	}

	public CandidationApplicationFormDto gapSum(List<EmploymentDetailsDto> employmentDetailsDto,
			CandidationApplicationFormDto candidationApplicationFormDto) {
		Integer gapYears = 0;
		Integer gapMonths = 0;

		for (EmploymentDetailsDto employmentDetails : employmentDetailsDto) {
			if (!employmentDetails.getGap().equals("Not-Available")) {
				String[] data = employmentDetails.getGap().split(" ");
				String yearString = data[0];
				String monthString = data[1];
				Integer years = Integer.parseInt(yearString.replace("y", ""));
				Integer months = Integer.parseInt(monthString.replace("m", ""));
				gapYears = gapYears + years;
				gapMonths = gapMonths + months;
			}
		}

		if (gapMonths >= 12) {
			Period period = Period.ofMonths(gapMonths).normalized();
			int years = period.getYears();
			int months = period.getMonths();
			gapYears = gapYears + years;
			gapMonths = months;

		}

		String gapSum = gapYears.toString() + "y " + gapMonths.toString() + "m";
		candidationApplicationFormDto.setGapSum(gapSum);
		return candidationApplicationFormDto;
	}

	@Override
	public ServiceOutcome<List<StatusMaster>> getAllStatus() {
		ServiceOutcome<List<StatusMaster>> svcSearchResult = new ServiceOutcome<List<StatusMaster>>();
		try {
			List<StatusMaster> statusMasterList = statusMasterRepository.findAll();
			if (!statusMasterList.isEmpty()) {
				svcSearchResult.setData(statusMasterList);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NOT FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getAllStatus method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<String> generateInterimReport(String candidateCode)
			throws FileNotFoundException, IOException {
		ServiceOutcome<String> svcSearchResult = new ServiceOutcome<String>();
		ServiceOutcome<CandidationApplicationFormDto> applicationForm = candidateApplicationFormDetailsExceptCandidate(
				candidateCode);

		VelocityEngine ve = new VelocityEngine();

		/* next, get the Template */
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();
		Template t = ve.getTemplate("templates/interimReport.vm");
		/* create a context and add data */
		VelocityContext context = new VelocityContext();

		List<ExecutiveSummaryDto> executiveSummaryDto = applicationForm.getData().getExecutiveSummary();
		String executiveSummary_stat = "";
		if (!executiveSummaryDto.isEmpty()) {
			List<String> colorArray = new ArrayList<>();
			for (int index = 0; index < executiveSummaryDto.size(); index++) {
				colorArray.add(executiveSummaryDto.get(index).getColorCode());
			}
			if (colorArray.contains("RED")) {
				executiveSummary_stat = "Red";
			} else if (colorArray.contains("AMBER")) {
				executiveSummary_stat = "Amber";
			} else {
				executiveSummary_stat = "Green";
			}

		}

		List<CandidateIdItems> idItems = applicationForm.getData().getCandidateIdItems();
		String candidateIdItems_stat = "";
		if (!idItems.isEmpty()) {
			List<String> colorArray = new ArrayList<>();
			for (int index = 0; index < idItems.size(); index++) {
				colorArray.add(idItems.get(index).getColor().getColorCode());
			}
			if (colorArray.contains("RED")) {
				candidateIdItems_stat = "Red";
			} else if (colorArray.contains("AMBER")) {
				candidateIdItems_stat = "Amber";
			} else {
				candidateIdItems_stat = "Green";
			}

		}

		List<CandidateCafEducationDto> education = applicationForm.getData().getCandidateCafEducationDto();
		String candidateEducation_stat = "";
		if (!education.isEmpty()) {
			List<String> colorArray = new ArrayList<>();
			for (int index = 0; index < education.size(); index++) {
				colorArray.add(education.get(index).getColorColorCode());
			}
			if (colorArray.contains("RED")) {
				candidateEducation_stat = "Red";
			} else if (colorArray.contains("AMBER")) {
				candidateEducation_stat = "Amber";
			} else {
				candidateEducation_stat = "Green";
			}

		}

		List<EmploymentDetailsDto> employmentDetails = applicationForm.getData().getEmploymentDetails();
		String candidateEmployment_stat = "";
		if (!employmentDetails.isEmpty()) {
			List<String> colorArray = new ArrayList<>();
			for (int index = 0; index < employmentDetails.size(); index++) {
				colorArray.add(employmentDetails.get(index).getColorCode());
			}
			if (colorArray.contains("RED")) {
				candidateEmployment_stat = "Red";
			} else if (colorArray.contains("AMBER")) {
				candidateEmployment_stat = "Amber";
			} else {
				candidateEmployment_stat = "Green";
			}

		}

		List<CandidateCafAddressDto> candidateCafAddressDto = applicationForm.getData().getCandidateCafAddressDto();
		String candidateAddress_stat = "";
		if (!candidateCafAddressDto.isEmpty()) {
			List<String> colorArray = new ArrayList<>();
			for (int index = 0; index < candidateCafAddressDto.size(); index++) {
				colorArray.add(candidateCafAddressDto.get(index).getColorColorCode());
			}
			if (colorArray.contains("RED")) {
				candidateAddress_stat = "Red";
			} else if (colorArray.contains("AMBER")) {
				candidateAddress_stat = "Amber";
			} else {
				candidateAddress_stat = "Green";
			}

		}
		CandidateFileDto caseDetails = applicationForm.getData().getCaseDetails();
		String criminal_stat = "";
		if (Optional.ofNullable(caseDetails).isPresent()) {
			criminal_stat = caseDetails.getColorName();
		}
		CandidateFileDto globalDatabaseCaseDetails = applicationForm.getData().getGlobalDatabaseCaseDetails();
		String globalDatabaseCase_stat = "";
		if (Optional.ofNullable(globalDatabaseCaseDetails).isPresent()) {
			globalDatabaseCase_stat = globalDatabaseCaseDetails.getColorName();
		}
		String candidateFinalReport_stat = "";
		if (executiveSummary_stat.equals("Red") || candidateAddress_stat.equals("Red") || candidateEducation_stat.equals("Red")
				|| candidateIdItems_stat.equals("Red") || candidateEmployment_stat.equals("Red")
				|| globalDatabaseCase_stat.equals("Red") || criminal_stat.equals("Red")) {
			candidateFinalReport_stat = "Red";
		} else if (executiveSummary_stat.equals("Amber") || candidateAddress_stat.equals("Amber")
				|| candidateEducation_stat.equals("Amber") || candidateIdItems_stat.equals("Amber")
				|| candidateEmployment_stat.equals("Amber") || globalDatabaseCase_stat.equals("Amber")
				|| criminal_stat.equals("Amber")) {
			candidateFinalReport_stat = "Amber";
		} else if (executiveSummary_stat.equals("Green") || candidateAddress_stat.equals("Green")
				|| candidateEducation_stat.equals("Green") || candidateIdItems_stat.equals("Green")
				|| candidateEmployment_stat.equals("Green") || globalDatabaseCase_stat.equals("Green")
				|| criminal_stat.equals("Green")) {
			candidateFinalReport_stat = "Green";
		}

		context.put("data", applicationForm.getData());
		context.put("report", "Interim Report");
		context.put("date", new DateTool());
		context.put("executiveSummary_stat", executiveSummary_stat);
		context.put("candidateIdItems_stat", candidateIdItems_stat);
		context.put("candidateEducation_stat", candidateEducation_stat);
		context.put("candidateEmployment_stat", candidateEmployment_stat);
		context.put("candidateAddress_stat", candidateAddress_stat);
		context.put("candidateFinalReport_stat", candidateFinalReport_stat);
		context.put("criminal_stat", criminal_stat);
		context.put("globalDatabaseCase_stat", globalDatabaseCase_stat);
		/* now render the template into a StringWriter */
		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		/* show the World */

		HtmlConverter.convertToPdf(writer.toString(), new FileOutputStream("E://interimLatest.pdf"));

		File file = new File("E://interimLatest.pdf");
		FileItem fileItem = new DiskFileItem("mainFile", Files.probeContentType(file.toPath()), false, file.getName(),
				(int) file.length(), file.getParentFile());

		try {
			IOUtils.copy(new FileInputStream(file), fileItem.getOutputStream());
		} catch (IOException ex) {
			// do something.
		}

	//	MultipartFile multipartFile = new CommonsMultipartFile(fileItem);

//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//		baos = generatePdf(writer.toString());
//		try 
//		OutputStream out = new FileOutputStream("E://out.pdf");
//		
//			out.write(baos.toByteArray());
//			out.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally {
//			
//		}
		svcSearchResult.setData("PDF generated");
		svcSearchResult.setOutcome(true);
		svcSearchResult.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));

		return svcSearchResult;
	}

	public ByteArrayOutputStream generatePdf(String html) {

		PdfWriter pdfWriter = null;

		// create a new document
		Document document = new Document();
		try {

			document = new Document();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, baos);

			// open document
			document.open();

			XMLWorkerHelper xmlWorkerHelper = XMLWorkerHelper.getInstance();
			xmlWorkerHelper.getDefaultCssResolver(true);
			xmlWorkerHelper.parseXHtml(pdfWriter, document, new StringReader(html));
			// close the document
			document.close();
			System.out.println("PDF generated successfully");

			return baos;
		} catch (Exception e) {
			log.error("Exception occured in generatePdf method in CandidateServiceImpl-->", e);
			return null;
		}

	}

	@Override
	public ServiceOutcome<Candidate> saveIsUanSkipped(String candidateCode, String isUanSkipped) {
		ServiceOutcome<Candidate> svcSearchResult = new ServiceOutcome<Candidate>();
		try {
			Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);
			if (candidate != null) {
				if (isUanSkipped.equals("yes")) {
					candidate.setIsUanSkipped(false);
				} else {
					candidate.setIsUanSkipped(true);
				}
				candidate.setLastUpdatedOn(new Date());
				candidateRepository.save(candidate);
				svcSearchResult.setData(candidate);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("UAN check saved successfully.");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Candidate not found.");
			}

		} catch (Exception e) {
			log.error("Exception occured in saveIsUanSkipped method in CandidateServiceImpl-->", e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public List<CandidateCafExperience> getCandidateExperienceFromItrAndEpfoByCandidateId_old(Long candidateId,
			Boolean formatEpfoDate) {
		System.out.println("enter to node js server" + formatEpfoDate);
		Candidate candidate = candidateRepository.findById(candidateId)
				.orElseThrow(() -> new RuntimeException("invalid candidate id"));
		String nodeServerUrl = "http://ec2-35-154-251-102.ap-south-1.compute.amazonaws.com:9090/v1.0/candidate/get-itr-epfo/"
				+ candidateId + "?formatEpfoDate=" + (formatEpfoDate ? "1" : "0");
		ResponseEntity<EpfoItrResponseDTO> epfoItrResponseDTOResponseEntity = restTemplate.getForEntity(nodeServerUrl,
				EpfoItrResponseDTO.class);
		List<CandidateCafExperience> candidateCafExperiences = new ArrayList<>();
		if (epfoItrResponseDTOResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			EpfoItrResponseDTO epfoItrResponseDTO = epfoItrResponseDTOResponseEntity.getBody();
			epfoItrResponseDTO.getData().getEmployerList().stream().forEach(employerData -> {
				System.out.println("enter to node js server" + employerData.getDoj());
				CandidateCafExperience candidateCafExperience = new CandidateCafExperience();
				candidateCafExperience.setCandidateEmployerName(employerData.getEmployerName());
				candidateCafExperience.setOutputDateOfJoining(DateUtil.getDate(employerData.getDoj(), "dd-MM-yyyy"));
				candidateCafExperience.setInputDateOfJoining(DateUtil.getDate(employerData.getDoj(), "dd-MM-yyyy"));
				candidateCafExperience.setOutputDateOfExit(DateUtil.getDate(employerData.getDoe(), "dd-MM-yyyy"));
				candidateCafExperience.setInputDateOfExit(DateUtil.getDate(employerData.getDoe(), "dd-MM-yyyy"));
				candidateCafExperience.setServiceSourceMaster(
						serviceSourceMasterRepository.findByServiceCode(employerData.getSource()));
				System.out.println(candidateCafExperience + "candidateCafExperience");
				candidateCafExperience.setCandidate(candidate);
				candidateCafExperience.setCreatedOn(new Date());
				candidateCafExperience.setColor(colorRepository.findByColorCode("GREEN"));
				candidateCafExperience.setUan(epfoItrResponseDTO.getData().getUan());
				candidateCafExperiences.add(candidateCafExperience);
			});
			return candidateCafExperiences;

		}
		return candidateCafExperiences;

	}

	@Override
	public List<CandidateCafExperience> getCandidateExperienceFromItrAndEpfoByCandidateId(Long candidateId,
			Boolean formatEpfoDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		org.joda.time.format.DateTimeFormatter formatter = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd");
		System.out.println("enter new" + formatEpfoDate);
		Candidate candidate = candidateRepository.findById(candidateId)
				.orElseThrow(() -> new RuntimeException("invalid candidate id"));
		log.info("STARTING THE ADJUDICATION LOGIC FOR APPLICANT ID::{}",candidate.getApplicantId());
		List<CandidateCafExperience> candidateCafExperiences = new ArrayList<>();
		List<ITRData> iTRDataList = itrDataRepository
				.findAllByCandidateCandidateCodeOrderByFiledDateDesc(candidate.getCandidateCode());
		List<EpfoData> uan = epfoDataRepository.findAllByCandidateCandidateCode(candidate.getCandidateCode());
		// System.out.println(uan+"uan");
		Date doe = null;
		if (uan.isEmpty()) {
			if (!iTRDataList.isEmpty()) {
//				int j = 0;
//				Collections.reverse(iTRDataList);
//				for (ITRData itrData : iTRDataList) {
//					ITRDataFromApiDto itrDataFromApiDto = this.modelMapper.map(itrData, ITRDataFromApiDto.class);
//
//					System.out.println(itrDataFromApiDto + "iTRDataList" + iTRDataList.size());
//					CandidateCafExperience candidateCafExperience = new CandidateCafExperience();
//					if (j == 0) {
//
//						candidateCafExperience.setCandidateEmployerName(itrDataFromApiDto.getDeductor());
//						candidateCafExperience
//								.setServiceSourceMaster(serviceSourceMasterRepository.findByServiceCode("ITR"));
//						candidateCafExperience.setCandidate(candidate);
//						candidateCafExperience.setCreatedOn(new Date());
//						candidateCafExperience.setColor(colorRepository.findByColorCode("GREEN"));
//						System.out.println(itrDataFromApiDto.getFiledDate() + "date1");
//						candidateCafExperience.setOutputDateOfJoining(itrDataFromApiDto.getFiledDate());
//						candidateCafExperience.setInputDateOfJoining(itrDataFromApiDto.getFiledDate());
//						candidateCafExperiences.add(candidateCafExperience);
//						return candidateCafExperiences;
//
//					}
//					j++;
//
//				}
//				return candidateCafExperiences;

				
				// start
				HashMap<String, List<ITRData>> map = new HashMap<String, List<ITRData>>();
				iTRDataList.forEach(itr -> {
					if (!map.containsKey(itr.getDeductor())) {
						List<ITRData> list = new ArrayList<>();
						iTRDataList.forEach(itrIndivisual -> {
							if (itr.getDeductor().equalsIgnoreCase(itrIndivisual.getDeductor())) {
								list.add(itrIndivisual);
							}
						});
						map.put(itr.getDeductor(), list);
					}
				});

				map.keySet().forEach(itrMapKey -> {
					if (!containsITR(candidateCafExperiences, itrMapKey)) {

						List<ITRData> itrList = map.get(itrMapKey);

								ITRDataFromApiDto itrDataFromApiDto = this.modelMapper.map(itrList.get(0), ITRDataFromApiDto.class);
								CandidateCafExperience candidateCafExperience = new CandidateCafExperience(); 

								candidateCafExperience.setCandidateEmployerName(itrDataFromApiDto.getDeductor());
								candidateCafExperience
										.setServiceSourceMaster(serviceSourceMasterRepository.findByServiceCode("ITR"));
								candidateCafExperience.setCandidate(candidate);
								candidateCafExperience.setCreatedOn(new Date());
								candidateCafExperience.setColor(colorRepository.findByColorCode("GREEN"));

								try {
									if (itrList.size() == 1) { 
										candidateCafExperience.setInputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
										candidateCafExperience.setInputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
										candidateCafExperience.setOutputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
										candidateCafExperience.setOutputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
										
									} else {
										candidateCafExperience
												.setInputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(itrList.size()-1).getDate()));
										candidateCafExperience.setInputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
										candidateCafExperience.setOutputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
										candidateCafExperience.setOutputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(itrList.size()-1).getDate()));
									}
								} catch(Exception e) {
									log.error("Exception occured in getCandidateExperienceFromItrAndEpfoByCandidateId method in CandidateServiceImpl-->", e);
								}

								double similarity = 0.0;
								for(CandidateCafExperience candidateCafExperice: candidateCafExperiences) {
									if(CommonUtils.checkStringSimilarity(candidateCafExperience.getCandidateEmployerName(),
											candidateCafExperice.getCandidateEmployerName()) > similarity) {
										similarity = CommonUtils.checkStringSimilarity(candidateCafExperience.getCandidateEmployerName(),
												candidateCafExperice.getCandidateEmployerName());
									}	
								}
								if(similarity < 0.90)
									candidateCafExperiences.add(candidateCafExperience);   
					}
				});
				log.info("ENDING THE ADJUDICATION LOGIC FOR APPLICANT ID IN block1::{}",candidate.getApplicantId());
				return candidateCafExperiences;
			}
			
			
		} else if(!uan.isEmpty() && !iTRDataList.isEmpty()) {
			
			HashMap<String, List<ITRData>> map = new HashMap<String, List<ITRData>>();
			iTRDataList.forEach(itr -> {
				if (!map.containsKey(itr.getDeductor())) {
					List<ITRData> list = new ArrayList<>();
					iTRDataList.forEach(itrIndivisual -> {
						if (itr.getDeductor().equalsIgnoreCase(itrIndivisual.getDeductor())) {
							list.add(itrIndivisual);
						}
					});
					map.put(itr.getDeductor(), list);
				}
			}); 
			
			for (EpfoData epfoData : uan) {
				EpfoDataFromDetailsDto epfoDataFromApiDto = this.modelMapper.map(epfoData,
						EpfoDataFromDetailsDto.class);
				
				CandidateCafExperience candidateCafExperience = new CandidateCafExperience();
				candidateCafExperience.setCandidateEmployerName(epfoDataFromApiDto.getCompany());
				candidateCafExperience.setServiceSourceMaster(serviceSourceMasterRepository.findByServiceCode("EPFO"));
				candidateCafExperience.setCandidate(candidate);
				candidateCafExperience.setCreatedOn(new Date());
				candidateCafExperience.setColor(colorRepository.findByColorCode("GREEN"));
				candidateCafExperience.setUan(epfoDataFromApiDto.getUan());
				

					if (epfoDataFromApiDto.getDoj() == null && map.keySet().stream().anyMatch(p -> p.equalsIgnoreCase(epfoDataFromApiDto.getCompany()))
							|| map.keySet().stream().anyMatch(p -> p.equalsIgnoreCase(epfoDataFromApiDto.getCompany())) && epfoDataFromApiDto.getDoj().after(epfoDataFromApiDto.getDoe() != null ? epfoDataFromApiDto.getDoe() : new Date())) {
						log.info("getDoj() after doe then dates and taken from itr for {}", epfoDataFromApiDto.getCompany());
//						map.keySet().forEach(itrMapKey -> {
						List<ITRData> itrList = map.get(epfoDataFromApiDto.getCompany());
						
						try {
							if (itrList.size() == 1) { 
								candidateCafExperience.setInputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
								candidateCafExperience.setInputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
								candidateCafExperience.setOutputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
								candidateCafExperience.setOutputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
								
							} else {
								candidateCafExperience
										.setInputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(itrList.size()-1).getDate()));
								candidateCafExperience.setInputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
								candidateCafExperience.setOutputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
								candidateCafExperience.setOutputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(itrList.size()-1).getDate()));
							}
						} catch(Exception e) {
							log.error("Exception 2 occured in getCandidateExperienceFromItrAndEpfoByCandidateId method in CandidateServiceImpl-->", e);
						}

//						});

					} else {
						log.info("Doj before doe dates taken from epfo for {}", epfoDataFromApiDto.getCompany());
//						Date doj = epfoDataFromApiDto.getDoj() != null ? epfoDataFromApiDto.getDoj() : new Date();
						if(epfoDataFromApiDto.getDoj() != null && epfoDataFromApiDto.getDoj().after(epfoDataFromApiDto.getDoe() != null ? epfoDataFromApiDto.getDoe() : new Date())) {
							candidateCafExperience.setOutputDateOfJoining(epfoDataFromApiDto.getDoe());
							candidateCafExperience.setInputDateOfJoining(epfoDataFromApiDto.getDoe());
							if (epfoDataFromApiDto.getDoj() != null) {
								candidateCafExperience.setOutputDateOfExit(epfoDataFromApiDto.getDoj());
								candidateCafExperience.setInputDateOfExit(epfoDataFromApiDto.getDoj());
							}
							
						} else {
							candidateCafExperience.setOutputDateOfJoining(epfoDataFromApiDto.getDoj());
							candidateCafExperience.setInputDateOfJoining(epfoDataFromApiDto.getDoj());
							if (epfoDataFromApiDto.getDoe() != null) {
								candidateCafExperience.setOutputDateOfExit(epfoDataFromApiDto.getDoe());
								candidateCafExperience.setInputDateOfExit(epfoDataFromApiDto.getDoe());
							}
							
							
						}
					}
				
				double similarity = 0.0;
				for(CandidateCafExperience candidateCafExperice: candidateCafExperiences) {
					if(CommonUtils.checkStringSimilarity(candidateCafExperience.getCandidateEmployerName(),
							candidateCafExperice.getCandidateEmployerName()) > similarity) {
						similarity = CommonUtils.checkStringSimilarity(candidateCafExperience.getCandidateEmployerName(),
								candidateCafExperice.getCandidateEmployerName());
					}	
				}
				if(similarity < 0.90)
					candidateCafExperiences.add(candidateCafExperience);
				else if(similarity > 0.90 && (!isOverllappingTenure(candidateCafExperience, candidateCafExperiences) || !isSameOverllappingTenureDuration(candidateCafExperience, candidateCafExperiences)))
					candidateCafExperiences.add(candidateCafExperience);
			}
			
			uan.forEach(uanObj -> {
				for(int i=0;i<uan.size() && !uanObj.equals(uan.get(i));i++) {
					
					EpfoDataFromDetailsDto epfoData1 = this.modelMapper.map(uanObj,
							EpfoDataFromDetailsDto.class);
					
					EpfoDataFromDetailsDto epfoData2 = this.modelMapper.map(uan.get(i),
							EpfoDataFromDetailsDto.class);
					if(epfoData1.getDoj() != null && epfoData2.getDoj() != null) { 
						String doj1 = sdf.format(epfoData1.getDoj());								
						String doj2 = sdf.format(epfoData2.getDoj());
						
						org.joda.time.LocalDate startDate1 = org.joda.time.LocalDate.parse(doj1, formatter);
						org.joda.time.LocalDate endDate1 = null;
						org.joda.time.LocalDate startDate2 = org.joda.time.LocalDate.parse(doj2, formatter);
						org.joda.time.LocalDate endDate2 = null;
				        
						if(epfoData1.getDoe() == null) {
							endDate1 = org.joda.time.LocalDate.now();
						}else {
							String doe1 = sdf.format(epfoData1.getDoe());
							endDate1 = org.joda.time.LocalDate.parse(doe1, formatter);
						}
							
						if(epfoData2.getDoe() == null) {
							endDate2 = org.joda.time.LocalDate.now();
						}else {
							String doe2 = sdf.format(epfoData2.getDoe());
							endDate2 = org.joda.time.LocalDate.parse(doe2, formatter);
						}

						Interval interval1 = null;
						Interval interval2 = null;
						if(startDate1.isBefore(endDate1)) {
					        interval1 = new Interval(startDate1.toDateMidnight(), endDate1.toDateMidnight());
						} else {
					        interval1 = new Interval(endDate1.toDateMidnight(), startDate1.toDateMidnight());
						}
						if(startDate2.isBefore(endDate2)) {
					        interval2 = new Interval(startDate2.toDateMidnight(), endDate2.toDateMidnight());
						}else {
					        interval2 = new Interval(endDate2.toDateMidnight(), startDate2.toDateMidnight());
						}

						if(interval1.overlaps(interval2) || interval1.abuts(interval2)) { 
							for(CandidateCafExperience candidateCafExperience  : candidateCafExperiences) {
								if(candidateCafExperience.getCandidateEmployerName().equalsIgnoreCase(epfoData1.getCompany()) && candidateCafExperience.getOutputDateOfJoining().equals(epfoData1.getDoj())) { 
									candidateCafExperience.setColor(colorRepository.findByColorCode("MOONLIGHTING")); 
								}
								if(candidateCafExperience.getCandidateEmployerName().equalsIgnoreCase(epfoData2.getCompany()) && candidateCafExperience.getOutputDateOfJoining().equals(epfoData2.getDoj())) {  
									candidateCafExperience.setColor(colorRepository.findByColorCode("MOONLIGHTING"));
								}
							} 
						}
					}
				}
			});
			
			// start

			map.keySet().forEach(itrMapKey -> {
				if (!containsITR(candidateCafExperiences, itrMapKey)) {

					List<ITRData> itrList = map.get(itrMapKey);
							ITRDataFromApiDto itrDataFromApiDto = this.modelMapper.map(itrList.get(0), ITRDataFromApiDto.class);
							CandidateCafExperience candidateCafExperience = new CandidateCafExperience(); 

							candidateCafExperience.setCandidateEmployerName(itrDataFromApiDto.getDeductor());
							log.info("taking itr record for caf exp {}", itrDataFromApiDto.getDeductor());
							candidateCafExperience
									.setServiceSourceMaster(serviceSourceMasterRepository.findByServiceCode("ITR"));
							candidateCafExperience.setCandidate(candidate);
							candidateCafExperience.setCreatedOn(new Date());
							candidateCafExperience.setColor(colorRepository.findByColorCode("GREEN"));

							try {
								if (itrList.size() == 1) { 
									candidateCafExperience.setInputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
									candidateCafExperience.setInputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
									candidateCafExperience.setOutputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
									candidateCafExperience.setOutputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
									
								} else {
									candidateCafExperience
											.setInputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(itrList.size()-1).getDate()));
									candidateCafExperience.setInputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
									candidateCafExperience.setOutputDateOfExit(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(0).getDate()));
									candidateCafExperience.setOutputDateOfJoining(new SimpleDateFormat("dd-MM-yyyy").parse(itrList.get(itrList.size()-1).getDate()));
								}
							} catch(Exception e) {
								log.error("Exception occured in getCandidateExperienceFromItrAndEpfoByCandidateId method in CandidateServiceImpl-->", e);
							}
							
							double similarity = 0.0;
							for(CandidateCafExperience candidateCafExperice: candidateCafExperiences) {
								if(CommonUtils.checkStringSimilarity(candidateCafExperience.getCandidateEmployerName(),
										candidateCafExperice.getCandidateEmployerName()) > similarity) {
									similarity = CommonUtils.checkStringSimilarity(candidateCafExperience.getCandidateEmployerName(),
											candidateCafExperice.getCandidateEmployerName());
								}	
							}
							if(similarity < 0.90)
								candidateCafExperiences.add(candidateCafExperience); 
							   
				}
			});
			log.info("ENDING THE ADJUDICATION LOGIC FOR APPLICANT ID IN block2::{}",candidate.getApplicantId());
			return candidateCafExperiences;
		} else {
			for (EpfoData epfoData : uan) {
				EpfoDataFromDetailsDto epfoDataFromApiDto = this.modelMapper.map(epfoData,
						EpfoDataFromDetailsDto.class);
				System.out.println(uan.size() + "iTRDataList");
				System.out.println(epfoDataFromApiDto + "uan");
				System.out.println(epfoDataFromApiDto.getDoj());
				CandidateCafExperience candidateCafExperience = new CandidateCafExperience();
				candidateCafExperience.setCandidateEmployerName(epfoDataFromApiDto.getCompany());
				candidateCafExperience.setOutputDateOfJoining(epfoDataFromApiDto.getDoj());
				candidateCafExperience.setInputDateOfJoining(epfoDataFromApiDto.getDoj());
				if (epfoDataFromApiDto.getDoe() != null) {
					candidateCafExperience.setOutputDateOfExit(epfoDataFromApiDto.getDoe());
					candidateCafExperience.setInputDateOfExit(epfoDataFromApiDto.getDoe());
				}
				candidateCafExperience.setServiceSourceMaster(serviceSourceMasterRepository.findByServiceCode("EPFO"));
				candidateCafExperience.setCandidate(candidate);
				candidateCafExperience.setCreatedOn(new Date());
				candidateCafExperience.setColor(colorRepository.findByColorCode("GREEN"));
				candidateCafExperience.setUan(epfoDataFromApiDto.getUan());

				double similarity = 0.0;
				for(CandidateCafExperience candidateCafExperice: candidateCafExperiences) {
					if(CommonUtils.checkStringSimilarity(candidateCafExperience.getCandidateEmployerName(),
							candidateCafExperice.getCandidateEmployerName()) > similarity) {
						similarity = CommonUtils.checkStringSimilarity(candidateCafExperience.getCandidateEmployerName(),
								candidateCafExperice.getCandidateEmployerName());
					}	
				}
				
				if(similarity < 0.90)
					candidateCafExperiences.add(candidateCafExperience);
				else if(similarity > 0.90 && !isOverllappingTenure(candidateCafExperience, candidateCafExperiences))
					candidateCafExperiences.add(candidateCafExperience);

			}
			
			uan.forEach(uanObj -> {
				for(int i=0;i<uan.size() && !uanObj.equals(uan.get(i));i++) {
					
					EpfoDataFromDetailsDto epfoData1 = this.modelMapper.map(uanObj,
							EpfoDataFromDetailsDto.class);
					
					EpfoDataFromDetailsDto epfoData2 = this.modelMapper.map(uan.get(i),
							EpfoDataFromDetailsDto.class);
					if(epfoData1.getDoj() != null && epfoData2.getDoj() != null) { 
						String doj1 = sdf.format(epfoData1.getDoj());								
						String doj2 = sdf.format(epfoData2.getDoj());
						
						org.joda.time.LocalDate startDate1 = org.joda.time.LocalDate.parse(doj1, formatter);
						org.joda.time.LocalDate endDate1 = null;
						org.joda.time.LocalDate startDate2 = org.joda.time.LocalDate.parse(doj2, formatter);
						org.joda.time.LocalDate endDate2 = null;
				        
						if(epfoData1.getDoe() == null) {
							endDate1 = org.joda.time.LocalDate.now();
						}else {
							String doe1 = sdf.format(epfoData1.getDoe());
							endDate1 = org.joda.time.LocalDate.parse(doe1, formatter);
						}
							
						if(epfoData2.getDoe() == null) {
							endDate2 = org.joda.time.LocalDate.now();
						}else {
							String doe2 = sdf.format(epfoData2.getDoe());
							endDate2 = org.joda.time.LocalDate.parse(doe2, formatter);
						}

						Interval interval1 = null;
						Interval interval2 = null;
						if(startDate1.isBefore(endDate1)) {
					        interval1 = new Interval(startDate1.toDateMidnight(), endDate1.toDateMidnight());
						} else {
					        interval1 = new Interval(endDate1.toDateMidnight(), startDate1.toDateMidnight());
						}
						if(startDate2.isBefore(endDate2)) {
					        interval2 = new Interval(startDate2.toDateMidnight(), endDate2.toDateMidnight());
						}else {
					        interval2 = new Interval(endDate2.toDateMidnight(), startDate2.toDateMidnight());
						}

						if(interval1.overlaps(interval2) || interval1.abuts(interval2)) {
							for(CandidateCafExperience candidateCafExperience  : candidateCafExperiences) {
								if(candidateCafExperience.getCandidateEmployerName().equalsIgnoreCase(epfoData1.getCompany()) && candidateCafExperience.getOutputDateOfJoining().equals(epfoData1.getDoj())) { 
									candidateCafExperience.setColor(colorRepository.findByColorCode("MOONLIGHTING")); 
								}
								if(candidateCafExperience.getCandidateEmployerName().equalsIgnoreCase(epfoData2.getCompany()) && candidateCafExperience.getOutputDateOfJoining().equals(epfoData2.getDoj())) {  
									candidateCafExperience.setColor(colorRepository.findByColorCode("MOONLIGHTING"));
								}
							} 
						}
					}
				}
			});

			log.info("ENDING THE ADJUDICATION LOGIC FOR APPLICANT ID IN block3::{}",candidate.getApplicantId());
			return candidateCafExperiences;
		}

		return candidateCafExperiences;
	}
	
	boolean isOverllappingTenure(CandidateCafExperience candidateCafExperience, List<CandidateCafExperience> candidateCafExperiences){
		boolean result = false;
		CandidateCafExperience epfoData1 = this.modelMapper.map(candidateCafExperience,
				CandidateCafExperience.class);
		
		CandidateCafExperience duplicateExp = new CandidateCafExperience();
		for(CandidateCafExperience candidateCafExp: candidateCafExperiences) {
			if(CommonUtils.checkStringSimilarity(candidateCafExp.getCandidateEmployerName(),
					candidateCafExperience.getCandidateEmployerName()) > 0.90) {
				duplicateExp = candidateCafExp;
			}
		}
//		for(int k=0;k<candidateCafExperiences.size();k++) {
			
			CandidateCafExperience epfoData2 = duplicateExp;
			if(epfoData1.getInputDateOfJoining() != null && epfoData2.getInputDateOfJoining() != null) { 
				
				DateTime start1 = new DateTime(epfoData1.getInputDateOfJoining()); 
				DateTime end1 = null;
				if(epfoData1.getInputDateOfExit() == null) {
					end1 = DateTime.now();
				}else {
					end1 = new DateTime(epfoData1.getInputDateOfExit());
				}
				
				DateTime start2 = new DateTime(epfoData2.getInputDateOfJoining());
				DateTime end2 = null;
				if(epfoData2.getInputDateOfExit() == null) {
					end2 = DateTime.now();
				}else {
					end2 = new DateTime(epfoData2.getInputDateOfExit());
				}
				
				Interval interval = null;
				Interval interval2 = null;
				if(start1.isBefore(end1)) {
					interval = new Interval( start1, end1 );
				} else {
					interval = new Interval( end1, start1 );
				}
				if(start2.isBefore(end2)) {
					interval2 = new Interval( start2, end2 );
				}else {
					interval2 = new Interval( end2, start2 );
				}


				if(interval.overlaps( interval2 )) { 
					result = true;
				}
			} else {
				result = true;
			}
//		}
		return result;
	}
	
	boolean isSameOverllappingTenureDuration(CandidateCafExperience candidateCafExperience, List<CandidateCafExperience> candidateCafExperiences){
		boolean result = false;
		CandidateCafExperience epfoData1 = this.modelMapper.map(candidateCafExperience,
				CandidateCafExperience.class);
		
		CandidateCafExperience duplicateExp = new CandidateCafExperience();
		for(CandidateCafExperience candidateCafExp: candidateCafExperiences) {
			if(CommonUtils.checkStringSimilarity(candidateCafExp.getCandidateEmployerName(),
					candidateCafExperience.getCandidateEmployerName()) > 0.90) {
				duplicateExp = candidateCafExp;
			}
		}
		
		CandidateCafExperience epfoData2 = duplicateExp;
		if(epfoData1.getInputDateOfJoining() != null && epfoData2.getInputDateOfJoining() != null) {
			Date start1 =epfoData1.getInputDateOfJoining(); 
			Date end1 = null;
			if(epfoData1.getInputDateOfExit() == null) {
				end1 = new Date();
			}else {
				end1 = epfoData1.getInputDateOfExit();
			}
			
			Date start2 = epfoData2.getInputDateOfJoining();
			Date end2 = null;
			if(epfoData2.getInputDateOfExit() == null) {
				end2 = new Date();
			}else {
				end2 = epfoData2.getInputDateOfExit();
			}
			
			DateDifference dd1= DateUtil.getPreodDifference(start1, end1);
			DateDifference dd2= DateUtil.getPreodDifference(start2, end2);
			if(dd1.getYears()==dd2.getYears() && dd1.getMonths()==dd2.getMonths() && dd1.getDays()==dd2.getDays()) {
				result = true;
			}
		}
	 return result;
	}
	
	boolean containsITR(List<CandidateCafExperience> list, String name) {
		return list.stream().anyMatch(p -> p.getCandidateEmployerName().equalsIgnoreCase(name));
	}
	
	@Override
	public void updateCandidateExperienceDetails(String candidateCode) {

		try {
			if (StringUtils.isNotEmpty(candidateCode)) {
				Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);
				List<CandidateCafExperience> candidateCafExperiences = new ArrayList<>(); 
				
				List<String> codes = new ArrayList<String>() {{add("ITR"); add("EPFO");}};
				
				List<CandidateCafExperience> existingCandidateCafExperiences = candidateCafExperienceRepository
						.findByServiceSourceMasterCodes(candidate.getCandidateId(), codes);
				
				candidate.setIsUanSkipped(false);
				candidateRepository.save(candidate);
				
				candidateCafExperiences = getCandidateExperienceFromItrAndEpfoByCandidateId(
						candidate.getCandidateId(), false);
				if (!existingCandidateCafExperiences.isEmpty()) {
//					for(CandidateCafExperience  cafExperience: candidateCafExperiences) {
//						List<CandidateCafExperience> filteredExperience = existingCandidateCafExperiences.stream().filter(o -> cafExperience.getCandidateEmployerName().equalsIgnoreCase(o.getCandidateEmployerName()) && sdf.format(cafExperience.getInputDateOfJoining()).equals(sdf.format(o.getInputDateOfJoining()))).collect(Collectors.toList());
//						
//						if(filteredExperience.size() > 0) { 
//							candidateCafExperienceRepository.deleteById(filteredExperience.get(0).getCandidateCafExperienceId());
//						}
						
						for(CandidateCafExperience  cafExperience: existingCandidateCafExperiences) { 
							if(cafExperience.getServiceSourceMaster() != null)
								if(cafExperience.getServiceSourceMaster().getServiceCode().equals("ITR") || cafExperience.getServiceSourceMaster().getServiceCode().equals("EPFO"))
									candidateCafExperienceRepository.deleteById(cafExperience.getCandidateCafExperienceId());
						}
						
//						if(!existingCandidateCafExperiences.stream().anyMatch(o -> cafExperience.getCandidateEmployerName().equalsIgnoreCase(o.getCandidateEmployerName()) && sdf.format(cafExperience.getInputDateOfJoining()).equals(sdf.format(o.getInputDateOfJoining())))){
//							candidateCafExperienceRepository.save(cafExperience);
//						}
//					}
				}
				candidateCafExperienceRepository.saveAll(candidateCafExperiences);
			} 
		} catch (Exception ex) {
			log.error("Exception occured in candidateApplicationFormDetails method in CandidateServiceImpl-->", ex.getMessage()); 
		} 
	}

	@Override
	public List<CandidateCafExperience> getCandidateExperienceByCandidateId(Long candidateId) {
		return candidateCafExperienceRepository.findAllByCandidateCandidateId(candidateId);
	}

	@Override
	public CandidateVerificationState getCandidateVerificationStateByCandidateId(Long candidateId) {
		return candidateVerificationStateRepository.findByCandidateCandidateId(candidateId);
	}

	@Override
	public CandidateVerificationState addOrUpdateCandidateVerificationStateByCandidateId(Long candidateId,
			CandidateVerificationState candidateVerificationState) {
		CandidateVerificationState candidateVerificationState1 = candidateVerificationStateRepository
				.findByCandidateCandidateId(candidateId);
		if (Objects.nonNull(candidateVerificationState1)) {
			candidateVerificationState
					.setCandidateVerificationStateId(candidateVerificationState1.getCandidateVerificationStateId());
		}
		return candidateVerificationStateRepository.save(candidateVerificationState);
	}

	@Override
	public List<CandidateCafEducationDto> getAllCandidateEducationByCandidateId(Long candidateId) {
		return candidateCafEducationRepository.findAllByCandidateCandidateId(candidateId).stream().map(
				candidateCafEducation -> this.modelMapper.map(candidateCafEducation, CandidateCafEducationDto.class))
				.collect(Collectors.toList());
	}

	@Override
	public List<CandidateCafAddressDto> getCandidateAddress(Candidate candidate) {
		return candidateCafAddressRepository.findAllByCandidateCandidateCode(candidate.getCandidateCode()).stream()
				.map(candidateCafAddress -> this.modelMapper.map(candidateCafAddress, CandidateCafAddressDto.class))
				.collect(Collectors.toList());
	}

	@Transactional
	@Override
	public ServiceOutcome<Boolean> qcPendingstatus(String candidateCode) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		System.out.println(candidateCode + "candidtaecoe");
		try {
			System.out.println("inside try");
			entityManager.setFlushMode(FlushModeType.COMMIT);
			
			CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(candidateCode);
			if(!candidateStatus.getStatusMaster().getStatusCode().equals("PENDINGAPPROVAL")
					&& !candidateStatus.getStatusMaster().getStatusCode().equals("INTERIMREPORT")
					&& !candidateStatus.getStatusMaster().getStatusCode().equals("FINALREPORT")) {
				
				Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);
				if(!candidate.getOrganization().getOrganizationName().equalsIgnoreCase("Accolite Digital India Pvt Ltd")) {
					//Calling the Remittance api to collect proof images
					log.info("Remittance Call Started-->");
					remittanceServiceImpl.getremittanceRecords(candidateCode,"CANDIDATE");
					log.info("Remittance Call completed-->");
					//end
				}
				candidate.setSubmittedOn(new Date());
				candidate.setApprovalRequired(true);
				candidateRepository.save(candidate);
	//			CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(candidateCode);
				candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("PENDINGAPPROVAL"));
				candidateStatus.setLastUpdatedOn(new Date());
				// System.out.println(candidateStatus+"candidtaecoestatus");
				candidateStatus = candidateStatusRepository.save(candidateStatus);
	
				createCandidateStatusHistory(candidateStatus, "CANDIDATE");
				svcSearchResult.setData(true);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("Thank you for submitting your response.");
	
				log.info("PRE offer Async call started-->");
				CompletableFuture.runAsync(() -> {
					reportService.generateDocument(candidateCode, "", ReportType.PRE_OFFER,"");
				});
				
				
			}else {
				svcSearchResult.setData(true);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("Thank you for submitting your response.");
			}
		} catch (Exception ex) {
			log.error("Exception occured in qcPendingstatus method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(false);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;

	}

	@Override
	public ServiceOutcome<CandidateCafExperience> deletecandidateExpById(Long id) {
		ServiceOutcome<CandidateCafExperience> svcSearchResult = new ServiceOutcome<>();
		try {
			CandidateCafExperience result = null;
			if (id == null || id.equals(0l)) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please specify User");
			} else {
				Optional<CandidateCafExperience> candidateCafExperienceObj = candidateCafExperienceRepository
						.findById(id);
				if (candidateCafExperienceObj.isPresent()) {
					CandidateCafExperience candidateCafExperienceObj1 = candidateCafExperienceObj.get();
					candidateCafExperienceRepository.deleteById(id);
					
//					if(candidateCafExperienceObj1.getServiceSourceMaster() != null) {
//						if(candidateCafExperienceObj1.getServiceSourceMaster().getServiceCode().equals("ITR")) {
//							 List<ITRData> itrList = itrDataRepository.findAllByCandidateCandidateCodeOrderByFiledDateDesc(candidateCafExperienceObj1.getCandidate().getCandidateCode());
//							 itrList.stream().forEach(itr -> {
//								 if(itr.getDeductor().equalsIgnoreCase(candidateCafExperienceObj1.getCandidateEmployerName()))
//									 itrDataRepository.deleteById(itr.getItrId());
//							 });
//						} else if(candidateCafExperienceObj1.getServiceSourceMaster().getServiceCode().equals("EPFO")) {
//							List<EpfoData> uanList = epfoDataRepository.findAllByCandidateCandidateCode(candidateCafExperienceObj1.getCandidate().getCandidateCode());
//							uanList.stream().forEach(uan -> {
//								 if(uan.getCompany().equalsIgnoreCase(candidateCafExperienceObj1.getCandidateEmployerName()))
//									 epfoDataRepository.deleteById(uan.getEpfoId());
//							 });
//						}
//					}

					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage("Experience Details Deleted Successfully");
				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult.setMessage("No User Found");
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in deletecandidateExp method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<CandidateCafEducation> deletecandidateEducationById(Long id) {
		ServiceOutcome<CandidateCafEducation> svcSearchResult = new ServiceOutcome<>();
		try {
			CandidateCafEducation result = null;
			if (id == null || id.equals(0l)) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please specify User");
			} else {
				Optional<CandidateCafEducation> candidateCafEducationObj = candidateCafEducationRepository.findById(id);
				if (candidateCafEducationObj.isPresent()) {
					CandidateCafEducation candidateCafEducationObj1 = candidateCafEducationObj.get();
					candidateCafEducationRepository.deleteById(id);
					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage("Education Details Deleted Successfully");
				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult.setMessage("No User Found");
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in deletecandidateEducationById method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	// get digilocker details name and dateof birth
	@Override
	public ServiceOutcome<CandidateDetailsDto> candidateDLdata(String candidateCode) {
		ServiceOutcome<CandidateDetailsDto> svcSearchResult = new ServiceOutcome<CandidateDetailsDto>();
		System.out.println(candidateCode + "candidate=====================================");
		try {
			System.out.println("iside========try=============================");
			Candidate candidate = candidateRepository.findByCandidateCode(candidateCode);
			if (candidate != null) {
				CandidateDetailsDto candidateDetailsDto = this.modelMapper.map(candidate, CandidateDetailsDto.class);
				candidateDetailsDto.setAadharName(candidate.getAadharName());
				candidateDetailsDto.setAadharDob(candidate.getAadharDob());
				System.out.println("iside========try=============================" + candidateDetailsDto);
				svcSearchResult.setData(candidateDetailsDto);

				svcSearchResult.setOutcome(true);
				svcSearchResult
						.setMessage(messageSource.getMessage("msg.success", null, LocaleContextHolder.getLocale()));
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("No candidate found");
			}

		} catch (Exception e) {
			log.error("Exception occured in candidateDLdata method in CandidateServiceImpl-->", e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}

		return svcSearchResult;
	}

// update the vendor proof color by agent ///
	@Override
	public ServiceOutcome<Boolean> updateCandidateVendorProofColor(VendorUploadChecksDto vendorUploadChecksDto) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		VendorUploadChecks result = null;
		// CandidateCafExperience candidateCafExperience=null;
		User user = SecurityHelper.getCurrentUser();
		try {
			VendorUploadChecks vendorChecks = vendorUploadChecksRepository
					.findByVendorChecksVendorcheckId(vendorUploadChecksDto.getVendorChecks());
			System.out.println("...............updateCandidateVendorProofColor" + vendorUploadChecksDto);
			if (vendorChecks != null) {
				vendorChecks.setAgentColor(colorRepository.findById(vendorUploadChecksDto.getColorId()).get());
				vendorChecks.setCreatedOn(new Date());
				vendorChecks.setCreatedBy(user);
				result = vendorUploadChecksRepository.save(vendorChecks);
				if (result != null) {
					svcSearchResult.setMessage("vendorProofs Update successfully.");

				} else {
					System.out.println("-------------candidate-----else------");
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult
							.setMessage(messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
				}

			} else {
				System.out.println("-------------candvendorChecksidate-----else------");
			}
		} catch (Exception e) {
			log.error("Exception occured in updateCandidateVendorProofColor method in CandidateServiceImpl-->", e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<Long> getContentById(String CandidateCode) {
		ServiceOutcome<Long> svcSearchResult = new ServiceOutcome<Long>();
		try {
			Candidate candidate = candidateRepository.findByCandidateCode(CandidateCode);
			Optional<Content> contentList = contentRepository
					.findByCandidateIdAndContentTypeAndContentCategoryAndContentSubCategory(candidate.getCandidateId(),
							ContentType.GENERATED, ContentCategory.OTHERS, ContentSubCategory.PRE_APPROVAL);
			if (contentList.isPresent()) {
				Content contentListObj1 = contentList.get();
				System.out.println(contentListObj1 + "content list");
				Long contentid = contentListObj1.getContentId();
				System.out.println(contentid + "content id");
				svcSearchResult.setData(contentid);
				svcSearchResult.setOutcome(true);

			} else {
				//checking for regeneration of the Pre-offer
				CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(CandidateCode);
				String statusCode= candidateStatus.getStatusMaster().getStatusCode();
				if(((statusCode.equalsIgnoreCase("ITR")&& candidate.getIsUanSkipped()) || statusCode.equalsIgnoreCase("EPFO") || statusCode.equalsIgnoreCase("PENDINGAPPROVAL"))
						&& (!statusCode.equalsIgnoreCase("INTERIMREPORT") && !statusCode.equalsIgnoreCase("FINALREPORT"))) {
					log.info("PRE OFFER REGENERATION START ::");
					
					if(!statusCode.equalsIgnoreCase("PENDINGAPPROVAL")) {
						candidateStatus.setStatusMaster(statusMasterRepository.findByStatusCode("PENDINGAPPROVAL"));
						candidateStatus.setLastUpdatedOn(new Date());
						candidateStatus = candidateStatusRepository.save(candidateStatus);
			
						createCandidateStatusHistory(candidateStatus, "CANDIDATE");
					}
					//start generation
					reportService.generateDocument(CandidateCode, "", ReportType.PRE_OFFER,"");
					
					Optional<Content> updatedContentList = contentRepository
							.findByCandidateIdAndContentTypeAndContentCategoryAndContentSubCategory(candidate.getCandidateId(),
									ContentType.GENERATED, ContentCategory.OTHERS, ContentSubCategory.PRE_APPROVAL);
					if (updatedContentList.isPresent()) {
						Content contentListObj1 = updatedContentList.get();
						Long contentid = contentListObj1.getContentId();
						svcSearchResult.setData(contentid);
						svcSearchResult.setOutcome(true);

					} else {						
						svcSearchResult.setMessage("Content Id Not Found");
						svcSearchResult.setOutcome(false);
					}
				}
				
				if(candidate.getOrganization().getOrganizationName().equalsIgnoreCase("Accolite Digital India Pvt Ltd")
						&& (statusCode.equalsIgnoreCase("INTERIMREPORT") || statusCode.equalsIgnoreCase("FINALREPORT"))) {
					log.info("PROVIDING INTERIM CONYTENT ID FOR::{}",CandidateCode);
					List<Content> updatedContentList = contentRepository
							.findAllByCandidateIdAndContentTypeAndContentCategoryAndContentSubCategory(candidate.getCandidateId(),
									ContentType.GENERATED, ContentCategory.OTHERS, ContentSubCategory.INTERIM);
					if (updatedContentList!=null && !updatedContentList.isEmpty()) {
						Content contentListObj1 = updatedContentList.get(0);
						Long contentid = contentListObj1.getContentId();
						svcSearchResult.setData(contentid);
						svcSearchResult.setOutcome(true);

					} else {						
						svcSearchResult.setMessage("Content Id Not Found");
						svcSearchResult.setOutcome(false);
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception occured in getContentById method in CandidateServiceImpl-->", e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	/// comments in qc /////
	@Override
	public ServiceOutcome<Boolean> AddCommentsReports(CandidateCaseDetailsDTO candidateCaseDetailsDTO) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		CandidateAddComments candidateAddCommentsObj = null;
		CandidateAddComments result = null;

		try {
			User user = SecurityHelper.getCurrentUser();
			Candidate candidate = candidateRepository.findByCandidateCode(candidateCaseDetailsDTO.getCandidateCode());
			Long candidateId = candidate.getCandidateId();
			CandidateAddComments candidateAddComments = candidateAddCommentRepository
					.findByCandidateCandidateId(candidateId);
			if (candidateAddComments != null) {
				candidateAddComments.setComments(candidateCaseDetailsDTO.getAddComments());
				candidateAddComments.setCreatedOn(new Date());
				candidateAddComments.setCreatedBy(user);
				result = candidateAddCommentRepository.save(candidateAddComments);
				if (result != null) {
					svcSearchResult.setMessage("Addcomments Update Sucussfully.");

				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult
							.setMessage(messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
				}
			} else {
				candidateAddCommentsObj = new CandidateAddComments();
				candidateAddCommentsObj.setCandidate(candidate);
				candidateAddCommentsObj.setComments(candidateCaseDetailsDTO.getAddComments());
				candidateAddCommentsObj.setCreatedOn(new Date());
				candidateAddCommentsObj.setCreatedBy(user);
				result = candidateAddCommentRepository.save(candidateAddCommentsObj);
				if (result != null) {
					svcSearchResult.setMessage("Addcomments Saved Sucussfully.");

				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult
							.setMessage(messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
				}
			}

		} catch (Exception ex) {
			log.error("Exception occured in AddCommentsReports method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<SuspectEmpMaster> deleteSuspectExpById(Long id) {
		ServiceOutcome<SuspectEmpMaster> svcSearchResult = new ServiceOutcome<>();
		try {
			SuspectEmpMaster result = null;
			if (id == null || id.equals(0l)) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please specify User");
			} else {
				Optional<SuspectEmpMaster> suspectEmpMastereObj = suspectEmpMasterRepository.findById(id);
				if (suspectEmpMastereObj.isPresent()) {
					SuspectEmpMaster suspectEmpMastereObj1 = suspectEmpMastereObj.get();
					suspectEmpMasterRepository.deleteById(id);
					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage("suspectEmploye Details Deleted Successfully");
				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult.setMessage("No User Found");
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in deletesuspectEmp method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<Boolean> updateSpectEMPloyee(SuspectEmpMasterDto suspectEmpMasterDto) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		User user = SecurityHelper.getCurrentUser();
		SuspectEmpMaster result = null;
		try {
			System.out.println("...............updateCandidateVendorProofColor" + suspectEmpMasterDto);
			Optional<SuspectEmpMaster> suspectEmpMastereObj = suspectEmpMasterRepository
					.findById(suspectEmpMasterDto.getId());
			if (suspectEmpMastereObj.isPresent()) {
				SuspectEmpMaster suspectEmpMastereObj1 = suspectEmpMastereObj.get();
				suspectEmpMastereObj1.setSuspectCompanyName(suspectEmpMasterDto.getSuspectCompanyName());
				suspectEmpMastereObj1.setAddress(suspectEmpMasterDto.getAddress());

				suspectEmpMastereObj1.setIsActive(suspectEmpMasterDto.getIsActive());
				svcSearchResult.setOutcome(true);
				result = suspectEmpMasterRepository.save(suspectEmpMastereObj1);
				if (result != null) {
					svcSearchResult.setMessage("suspectEmploye Details Updated Successfully");

				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult
							.setMessage(messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
				}

			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("No User Found");
			}

		} catch (Exception e) {
			log.error("Exception occured in updateSpectEMPloyee method in CandidateServiceImpl-->", e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult
					.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Transactional
	@Override
	public ServiceOutcome<UserDto> agentCreatepasswrd(UserDto user) {
		ServiceOutcome<UserDto> svcSearchResult = new ServiceOutcome<>();
		System.out.println(user + "user");
		UserDto userDto = new UserDto();
		try {
			User result = null;
			// if (user.getUserId() != null && !user.getUserId().equals(0l) ) {
			// System.out.println(user+"inside_user");
			// User findUserEmail = userRepository.findByUserEmailId(user.getUserEmailId());

			User userObj = userRepository.findByEmployeeId(user.getEmployeeId());
			if (userObj != null) {
				System.out.println(user + "inside_if");
				// User userObj1 = userObj.get();
				String passwoString = userObj.getPassword();
				if (!user.getPassword().equals("")) {
					userObj.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
					userObj.setAddlPassword(user.getPassword());

				} else {
					userObj.setPassword(passwoString);
					userObj.setAddlPassword(userObj.getAddlPassword());

				}

				result = userRepository.save(userObj);

				BeanUtils.copyProperties(result, userDto);

				// setSomeUserDataInDTO(userDto,result);

				svcSearchResult.setData(userDto);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("User information Updated successfully");
			}

		} catch (Exception ex) {
			log.error("Exception occured in saveUser method in UserServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<String> suspectEmpMasterCheck(String companyName, Long oganizationId) {
		ServiceOutcome<String> svcSearchResult = new ServiceOutcome<String>();
		Optional<Organization> organization = organizationRepository.findById(oganizationId);
		if (organization.isPresent()) {
			Organization orgObj = organization.get();
			Long orgid = orgObj.getOrganizationId();

			String removenumbers = extractMainEmployerName(companyName).trim();
			String[] enteredEmp = removenumbers.split(" ", 8);
			int wordCount = 2;
			String searchEmployer = getSubString(enteredEmp, wordCount);
			
			List<SuspectEmpMaster> suspectEmpMaster = suspectEmpMasterRepository
					.getByOrganizationIdAndSuspectCompanyName(orgid, searchEmployer + "%");
			
//			if(suspectEmpMaster.size() > 1) {
//				wordCount += 1;
//				searchEmployer = getSubString(enteredEmp, wordCount);
//				suspectEmpMaster = suspectEmpMasterRepository
//						.getByOrganizationIdAndSuspectCompanyName(orgid, searchEmployer + "%");
//			}
//			
//			if(suspectEmpMaster.size() > 1) {
//				wordCount += 1;
//				searchEmployer = getSubString(enteredEmp, wordCount);
//				suspectEmpMaster = suspectEmpMasterRepository
//						.getByOrganizationIdAndSuspectCompanyName(orgid, searchEmployer + "%");
//			}
			log.info("no of iterations done and for emp {}", wordCount);
			
			if (!suspectEmpMaster.isEmpty()) {
				int i = 0;
				int j = 0;
				String[] splitSuspectEmp = null;
				String[] splitSearchEmployer= null;
				String matchedFromDBWith = null;
				List<String> partialMatches = new ArrayList<>();
				
				if(suspectEmpMaster.size() == 1) {
					log.info("SUSPECT ONE MATCHING ONLY :{}");
					String suspectEmployer = extractMainEmployerName(suspectEmpMaster.get(0).getSuspectCompanyName()).trim();
					svcSearchResult.setData("RED");
					matchedFromDBWith = suspectEmployer;
//					double similarity = CommonUtils.checkStringSimilarity(searchEmployer, suspectEmployer);
//					if(similarity > 0.90) {
//						svcSearchResult.setData("RED");
//						matchedFromDBWith = suspectEmployer;
//					} else {
//						log.info("SIMILARITY NOT MATCHING FOR :{}",suspectEmpMaster.get(0).getSuspectCompanyName());
//						svcSearchResult.setData("AMBER");
//					}
				} else {
		outerLoop: for (SuspectEmpMaster SuspectEmp : suspectEmpMaster) {
						String suspectEmployer = extractMainEmployerName(SuspectEmp.getSuspectCompanyName()).trim();
						splitSuspectEmp = suspectEmployer.split(" ", 8);
						splitSearchEmployer = searchEmployer.split(" ", 8); // given employment name

						if (splitSuspectEmp[0].equalsIgnoreCase(splitSearchEmployer[0])) { 
							i++;
						}
						partialMatches.add(SuspectEmp.getSuspectCompanyName());
						
						if(splitSearchEmployer.length != 1 && splitSuspectEmp.length != 1 && splitSearchEmployer.length == splitSuspectEmp.length) {
							List<Boolean> wordMatchCheckList = new ArrayList<>();
							for(int k = 0; k < splitSearchEmployer.length; k++) {
								if(splitSuspectEmp[k].equalsIgnoreCase(splitSearchEmployer[k]))
									wordMatchCheckList.add(true);
								else 
									wordMatchCheckList.add(false);
							}
							if(!wordMatchCheckList.contains(false)) {
								j++;
								matchedFromDBWith = SuspectEmp.getSuspectCompanyName();
								log.info("exact match {}", matchedFromDBWith);
							}
						} else {
							searchEmployer = getSubString(enteredEmp, enteredEmp.length);
							splitSearchEmployer = searchEmployer.split(" ", 8); // given employment name
							
							if(splitSearchEmployer.length != 1 && splitSuspectEmp.length != 1 && splitSearchEmployer.length == splitSuspectEmp.length) {
								List<Boolean> wordMatchCheckList = new ArrayList<>();
								for(int k = 0; k < splitSearchEmployer.length; k++) {
									if(splitSuspectEmp[k].equalsIgnoreCase(splitSearchEmployer[k]))
										wordMatchCheckList.add(true);
									else 
										wordMatchCheckList.add(false);
								}
								if(!wordMatchCheckList.contains(false)) {
									j++;
									matchedFromDBWith = SuspectEmp.getSuspectCompanyName();
									log.info("exact match in else block {}", matchedFromDBWith);
								}
							}
						}
						
						if (splitSearchEmployer.length != 1 && splitSuspectEmp.length != 1) {
							if (i >= 1 && j >= 1) {
								svcSearchResult.setData("RED");
								break outerLoop;
							}
							 else
								svcSearchResult.setData("AMBER");
						} else if (splitSearchEmployer.length == 1 && splitSuspectEmp.length == 1){
							if (i >= 1) {
								svcSearchResult.setData("RED");
								break outerLoop;
							}
							 else 
								svcSearchResult.setData("AMBER");
						} else {
							if (i >= 1) {
								svcSearchResult.setData("AMBER");
								break outerLoop;
							}
							else 
								svcSearchResult.setData("GREEN");
						}
						
					}
				}

				svcSearchResult.setOutcome(true);
				if(matchedFromDBWith != null)
					svcSearchResult.setMessage("Match found " + matchedFromDBWith);
				else
					svcSearchResult.setMessage("Match found " + partialMatches.toString());

			} else {
				svcSearchResult.setData("GREEN");
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("Match NOT found");
			}
		}

		return svcSearchResult;
	}
	
	public String getSubString(String[] splitedWords, int noOfWordsRequired) {
		String subString = "";
		for(int i=0; i < noOfWordsRequired && i < splitedWords.length ; i++) {
			subString += splitedWords[i];
			subString += " ";
		}
		return subString.trim();
	}
	
	public String extractMainEmployerName(String companyName) {

		if (companyName.matches(".*\\s*(?i)private\\s*.*"))
			companyName = companyName.replaceFirst("\\s*(?i)private\\s*", "");
		if (companyName.matches(".*\\s*(?i)limited\\s*.*"))
			companyName = companyName.replaceFirst("\\s*(?i)limited\\s*", "");
		if (companyName.matches(".*\\s*(?i)pvt.\\s*.*"))
			companyName = companyName.replaceFirst("\\s*(?i)pvt.\\s*", "");
		if (companyName.matches(".*\\s*(?i)pvt\\s*.*"))
			companyName = companyName.replaceFirst("\\s*(?i)pvt\\s*", "");
		if (companyName.matches(".*\\s*(?i)ltd.\\s*.*"))
			companyName = companyName.replaceFirst("\\s*(?i)ltd.\\s*", " ");
		if (companyName.matches(".*\\s*(?i)ltd\\s*.*"))
			companyName = companyName.replaceFirst("\\s*(?i)ltd\\s*", " ");
		
		return companyName;
	}

	private HttpHeaders setHeaderDetails(HttpHeaders headers) {
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	@Override
	public ServiceOutcome<UanSearchData> saveUan(UanSearchDataDTO uanDTO) {

		log.info("ApplicantId::1:::{}",uanDTO.getApplicantId());
		log.info("uanSave::::{}",uanDTO);
		
		ServiceOutcome<UanSearchData> svcSearchResult = new ServiceOutcome<>();

		try {

			Date date = new Date();
			String applicantId = uanDTO.getApplicantId();
			String uanNumber = uanDTO.getUanusername();
			String uploadedBy = uanDTO.getUploadedBy();
			String message = uanDTO.getMsg();

			log.info("UAN_NUMBER::1:::{}",uanNumber);
			log.info("UPLOADEDBY::1:::{}",uploadedBy);
			log.info("MSG:::::1:::{}",message);

			UanSearchData uanSave = new UanSearchData();


			String transactionId = null;

			//if (findByApplicantId == null) {
				log.info("===========APPLICANTID DOES NOT EXIST=================");

				
				ResponseEntity<String> epfoTokenResponse = null;
				HttpHeaders headers = new HttpHeaders();
				setHeaderDetails(headers);
				JSONObject request = new JSONObject();

				request.put(epfoSecurityConfig.getClientIdValue(), epfoSecurityConfig.getClientId());
				request.put(epfoSecurityConfig.getClientSecretValue(), epfoSecurityConfig.getClientSecret());
				HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);


				
				log.info("epfoSecurityConfig.getAccessTokenUrl() *************************\" =",epfoSecurityConfig.getAccessTokenUrl());

				epfoTokenResponse = restTemplate.exchange(epfoSecurityConfig.getAccessTokenUrl(), HttpMethod.POST,
						entity, String.class);
				log.info("Response from EPFO TOKEN API " + epfoTokenResponse);
				String message1 = epfoTokenResponse.getBody(); // .get("message").toString().replaceAll("=", ":")
				log.info("epfoTokenResponse  ************************* =",epfoTokenResponse.getBody());
				JSONObject obj1 = new JSONObject(message1);
				log.info("Response from EPFO TOKEN API - message " + obj1);
				log.info("last message " + obj1.getJSONObject("message"));
				JSONObject obj = obj1.getJSONObject("message");
				String access_token = obj.getString("access_token");
				log.info("access_token =",access_token);


		
				ResponseEntity<String> response = null;
				headers.setBearerAuth(access_token);
				headers.add("Bearer", access_token);
				HttpEntity<String> request1 = new HttpEntity<String>(headers);

				EpfoDetailsDto epfoDetails = new EpfoDetailsDto();
				
				response = restTemplate.exchange(epfoSecurityConfig.getTransactionIdUrl(), HttpMethod.GET, request1,
						String.class);
				String message2 = response.getBody();
				log.info("Response from EPFO Transaction API : " + message2);
				JSONObject obj2 = new JSONObject(message2);
				log.info("Response from EPFO Transaction API - obj: " + obj2);
				transactionId = obj2.getString("message").toString();
				epfoDetails.setTransactionid(transactionId);
				log.info("Generated transactionId Id is {}" + transactionId);
				log.info("transaction 123 id--> {}",transactionId);

				int maxRetryCount = 2;
				int retryCount = 0;
				try {
				    while (retryCount < maxRetryCount) {
				    	log.info("____________________epfoDetails.getUanusername()",uanNumber);

						request.put(EPFOConstants.EPFO_USR, uanNumber);

						request.put(EPFOConstants.EPFO_PWD, ' ');
						request.put(EPFOConstants.EPFO_UAN, uanNumber);

						HttpEntity<String> entity1 = new HttpEntity<String>(request.toString(), headers);
						log.info("\\n------epfoSecurityConfig ------ {}",epfoSecurityConfig.getFinalSubmitPostUrl());
						log.info("\\n------epfoDetails ------  {}",epfoDetails.getTransactionid());
						response = restTemplate.exchange(
								epfoSecurityConfig.getFinalSubmitPostUrl() + epfoDetails.getTransactionid(), HttpMethod.POST,
								entity1, String.class);

						String responseBody = response.getBody();
						System.out.println("RESPONSEBODY::::"+responseBody);
						JSONObject obj3 = new JSONObject(responseBody);
						log.info("\\n--------obj ---------  {}",obj3);

						boolean success = obj3.getBoolean("success");
						//handling empty response message array
						JSONArray messagee = obj3.getBoolean("success")?obj3.getJSONArray("message") : new JSONArray();
						if(obj3.getBoolean("success") && (messagee==null || messagee.isEmpty())){
							obj3.remove("message");
							obj3.put("message", "No data found for this UAN");
							success=false;
						}
						
						String resMsg = obj3.toString();
						log.info("\\n--------resMsg --------- {}",resMsg);
						uanSave.setEPFOResponse(resMsg);
						 
					        if (success) {
					        	log.info("SUCCESS IS TRUE {}");
					            break; // Success, exit the retry loop
					        } else {
					            // Retry the operation
					            retryCount++;
					            log.info("Retry #  {}" + retryCount);
					           
					        }
				    	
				    }
				    uanSave.setApplicantId(applicantId);
					uanSave.setUan(uanNumber);
					uanSave.setUploadedBy(uploadedBy);
					uanSave.setUploadedOn(date);
					if (uanDTO.isBulkUanSearch()) {
						log.info("MULTIPLERECODS::: {}",uanDTO.getTotalRecordUploaded());
						uanSave.setTotalRecordUploaded(uanDTO.getTotalRecordUploaded());
						uanSave.setBulkUanId(uanDTO.getBulkUanId());
					}

				    
				    if (retryCount == maxRetryCount) {
				        log.info("Maximum retry count reached. Operation failed. {}");
				        uanSave.setTotalRecordUploaded(1);
						uanSave.setTotalRecordFailed(1);
				        
				    } else {
				        log.info("Operation succeeded. {}");
						uanSave.setTotalRecordUploaded(1);
						uanSave.setTotalRecordFetched(1);
						uanSave.setTotalRecordFailed(0);
//						uanSearchDataRepository.save(uanSave);
//						svcSearchResult.setData(uanSave);
						svcSearchResult.setMessage("SUCCESS");
					}
					uanSearchDataRepository.save(uanSave);
					svcSearchResult.setData(uanSave);

				} catch (Exception e) {
					log.info(e.getMessage());
				}
				
				//
				

				log.info("ApplicantID:: If Not Exist:: {}",applicantId);
				log.info("UAN NUMBER:: If Not Exist:: {}",uanNumber);


				log.info("==============================:FINSIHED:===========================");


			log.info("UANSAVE::: {}", uanSave);

		} catch (Exception ex) {
			log.error("Exception occured in saveUan method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes");

		}

		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<UanSearchEpfoDTO>> getEpfoData(Map<String, String> data) {

		ServiceOutcome<List<UanSearchEpfoDTO>> svcSearchResult = new ServiceOutcome<>();

		String candidateCode = data.get("candidateCode");
		String uan = data.get("uanusername");
		String applicantId = data.get("applicantId");
		String bulkUanId = data.get("bulkUanId");
		String uanSearchId = data.get("uanSearchId");

		log.info("CCODE:: {}", candidateCode);
		log.info("UAN:: {}", uan);
		log.info("APPI::: {}", applicantId);
		log.info("BulkUanId::>> {}", bulkUanId);
		log.info("UANSEARCHID::: {}",uanSearchId);


		try {


				if (bulkUanId != null) {
					List<UanSearchData> uanSearchDataList = uanSearchDataRepository.findByBulkUanId(bulkUanId);
					log.info("NoT exist applicant And Id is not null", uan);
					List<String> applicantIdsAndUans = new ArrayList<>();
					List<UanSearchEpfoDTO> epfoDataList = new ArrayList<>();
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

					for (UanSearchData uanSearchData : uanSearchDataList) {
						uan = uanSearchData.getUan();
						String BulkApplicantId = uanSearchData.getApplicantId();
						log.info("BulkUANNumber::: >>>> {}", uan);
						log.info("BulkApplicantId >>>> {}", BulkApplicantId);


						applicantIdsAndUans.add("BulkApplicantId: " + BulkApplicantId + ", UAN: " + uan);

						List<UanSearchData> findByBulkId = uanSearchDataRepository
								.findByBulkIdAndUan(bulkUanId, uan);
						log.info("BULKDOWNLOAD PROCESS >>>> {}", findByBulkId);

						for (UanSearchData searchData : findByBulkId) {
							String epfoResponse = searchData.getEPFOResponse();
							JsonObject epfoJson = new Gson().fromJson(epfoResponse, JsonObject.class);
							
							//if (epfoJson.has("message") && epfoJson.get("message").isJsonArray()) {
								//JsonArray messageArray = epfoJson.getAsJsonArray("message");
								
								JsonElement messageElement = epfoJson.get("message");

								if (messageElement.isJsonArray()) {
								    JsonArray messageArray = messageElement.getAsJsonArray();

									for (JsonElement messageElement2 : messageArray) {
										JsonObject messageObj = messageElement2.getAsJsonObject();

										// Convert JsonObject to EpfoDataFromDetailsDto
										UanSearchEpfoDTO epfoData = new UanSearchEpfoDTO();
										epfoData.setUan(messageObj.get("uan").getAsString());
										epfoData.setName(messageObj.get("name").getAsString());
										epfoData.setCompany(messageObj.get("company").getAsString());


										if (messageObj.has("doe")) {
											String doeString = messageObj.get("doe").getAsString();
											if (!doeString.equals("NOT_AVAILABLE")) {
												try {
													Date doe = dateFormat.parse(doeString);
													epfoData.setDoe(doe);
												} catch (ParseException e) {
													log.error("Exception1 occured in getEpfoData method for DOE parsing bulk in CandidateServiceImpl-->", e);
													epfoData.setDoe(null); // Set as null if parsing fails
												}
											}
										}

//										String dojString = messageObj.get("doj").getAsString();
//										Date doj = dateFormat.parse(dojString);
//										epfoData.setDoj(doj);
										if (messageObj.has("doj")) {
											String dojString = messageObj.get("doj").getAsString();
											if (!dojString.equals("NOT_AVAILABLE")) {
												try {
													Date doj = dateFormat.parse(dojString);
													epfoData.setDoj(doj);
												} catch (ParseException e) {
													log.error("Exception1A occured in getEpfoData method for DOJ parsing bulk in CandidateServiceImpl-->", e);
													epfoData.setDoj(null); // Set as null if parsing fails
												}
											}
										}
										epfoData.setApplicantId(BulkApplicantId);
										
										    epfoData.setBulkId(bulkUanId);
										
										epfoDataList.add(epfoData);
									}
								}else if(messageElement.isJsonPrimitive()) {
									log.info("EPFO RESPONSE IS TRUE::: {}");
									UanSearchEpfoDTO epfoData = new UanSearchEpfoDTO();
									String messageString = messageElement.getAsString();
									epfoData.setApplicantId(BulkApplicantId);
									epfoData.setUan(uan);
									epfoData.setEpfoResponse(messageString);
									epfoData.setBulkId(bulkUanId);
									epfoData.setCompany(null);
									epfoData.setDoe(null);
									epfoData.setDoj(null);
									epfoData.setName(null);
									epfoDataList.add(epfoData);
									svcSearchResult.setOutcome(false);
								    // Handle the String
								}
																	
						}

					}
					svcSearchResult.setData(epfoDataList);

				}

				else {
					log.info("Candidate_Code is Null:::");
					List<UanSearchData> findByUanAndApplicantID = uanSearchDataRepository
							.findByApplicantIdAndUanAndUanSearchId(applicantId, uan, uanSearchId);
					log.info("FINDBYUAN>>>> DATA::>> {}", findByUanAndApplicantID.toString());
					List<UanSearchEpfoDTO> epfoDataList = new ArrayList<>();
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

					for (UanSearchData searchData : findByUanAndApplicantID) {
						String epfoResponse = searchData.getEPFOResponse();
						String applicantIdInDb = searchData.getApplicantId();
						String uanInDb = searchData.getUan();
						Long uanIDInDb = searchData.getUanSearchId();
						
						JsonObject epfoJson = new Gson().fromJson(epfoResponse, JsonObject.class);						
						
						JsonElement messageElement = epfoJson.get("message");
						if (messageElement.isJsonArray()) {
						    JsonArray messageArray = messageElement.getAsJsonArray();
							for (JsonElement messageElement2 : messageArray) {
								JsonObject messageObj = messageElement2.getAsJsonObject();

								// Convert JsonObject to EpfoDataFromDetailsDto
								UanSearchEpfoDTO epfoData = new UanSearchEpfoDTO();
								epfoData.setUan(messageObj.get("uan").getAsString());
								epfoData.setName(messageObj.get("name").getAsString());
								epfoData.setCompany(messageObj.get("company").getAsString());


								if (messageObj.has("doe")) {
									String doeString = messageObj.get("doe").getAsString();
									if (!doeString.equals("NOT_AVAILABLE")) {
										try {
											Date doe = dateFormat.parse(doeString);
											epfoData.setDoe(doe);
										} catch (ParseException e) {
											log.error("Exception2 occured in getEpfoData method in DOE PARSING CandidateServiceImpl-->", e);
											epfoData.setDoe(null); // Set as null if parsing fails
										}
									}
								}

//								String dojString = messageObj.get("doj").getAsString();
//								Date doj = dateFormat.parse(dojString);
//								epfoData.setDoj(doj);
								if (messageObj.has("doj")) {
									String dojString = messageObj.get("doj").getAsString();
									if (!dojString.equals("NOT_AVAILABLE")) {
										try {
											Date doj = dateFormat.parse(dojString);
											epfoData.setDoj(doj);
										} catch (ParseException e) {
											log.error("Exception2A occured in getEpfoData method for DOJ parsing in CandidateServiceImpl-->", e);
											epfoData.setDoj(null); // Set as null if parsing fails
										}
									}
								}

								epfoDataList.add(epfoData);
								svcSearchResult.setOutcome(true);
							}
						    // Handle the JsonArray
						} else if(messageElement.isJsonPrimitive()) {
							UanSearchEpfoDTO epfoData = new UanSearchEpfoDTO();
							String messageString = messageElement.getAsString();
							epfoData.setApplicantId(applicantId);
							epfoData.setUan(uan);
							epfoData.setEpfoResponse(messageString);
							epfoData.setBulkId(null);
							epfoData.setCompany(null);
							epfoData.setDoe(null);
							epfoData.setDoj(null);
							epfoData.setName(null);
							epfoDataList.add(epfoData);
							svcSearchResult.setOutcome(false);
						    // Handle the String
						}
						
						

						svcSearchResult.setData(epfoDataList);

					}
				}

				// Set the converted data in svcSearchResult
				log.info("===================FINSIHED=================GETEPFODATA======== {}");

					svcSearchResult.setMessage("SUCCESS::");

			

		} catch (Exception ex) {
			log.error("Exception occured in getEpfoData method in UserServiceImpl-->", ex);
		}

		return svcSearchResult;
	}

// OLD BULKUAN SEARCH	
//	@Override
//	public ServiceOutcome<List<UanSearchData>> bulkUan(List<BulkUanDTO> bulkUan) {
//		
//		System.out.println("uanSave::" + bulkUan);
//			log.info("uanSave:::>" + bulkUan);
//
//		
//		ServiceOutcome<List<UanSearchData>> svcSearchResult = new ServiceOutcome<>();
//
//		
//		try {
//			
//			Date date = new Date();
//			String applicantId = null;
//			String uanNumber = null;
//			String uploadedBy = null;
//			String bulkUanId = null;
//			int totalRecordUploaded;
//					
//	        List<BulkUanDTO> dataList = new ArrayList<>(); // Create a list to store the data
//
//			
//			for (BulkUanDTO dto : bulkUan) {
//				BulkUanDTO bulkUanDTO = new BulkUanDTO();
//				bulkUanDTO.setApplicantId(dto.getApplicantId());
//				bulkUanDTO.setBulkUanId(dto.getRandomId());
//				bulkUanDTO.setUan(dto.getUan());
//				bulkUanDTO.setUploadedBy(dto.getUploadedBy());
//				bulkUanDTO.setTotalRecordUploaded(dto.getTotalRecordUploaded());
//				bulkUanDTO.setBulkUanSearch(true);
//				
//	    
//		        dataList.add(bulkUanDTO);	        
//	 			log.info("TotalRecords:::>" + dto.getTotalRecordUploaded());
//		    }
//			
// 			log.info("BULKDATALIST:::>>>" + dataList.toString());
//			String transactionId = null;
//			UanSearchData uanSave;
//			List<UanSearchData> bulkData = new ArrayList<>();
//			
// 			ResponseEntity<String> epfoTokenResponse = null;
// 			HttpHeaders headers = new HttpHeaders();
// 			setHeaderDetails(headers);
// 			JSONObject request = new JSONObject();
//
// 			request.put(epfoSecurityConfig.getClientIdValue(), epfoSecurityConfig.getClientId());
// 			request.put(epfoSecurityConfig.getClientSecretValue(), epfoSecurityConfig.getClientSecret());
// 			HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
//
// 			
// 			log.info("epfoSecurityConfig.getAccessTokenUrl() *************************" + epfoSecurityConfig.getAccessTokenUrl());
//
// 			epfoTokenResponse = restTemplate.exchange(epfoSecurityConfig.getAccessTokenUrl(), HttpMethod.POST,
// 					entity, String.class);
// 			log.info("Response from EPFO TOKEN API " + epfoTokenResponse);
// 			String message1 = epfoTokenResponse.getBody(); // .get("message").toString().replaceAll("=", ":")
// 			log.info("epfoTokenResponse  ************************* {}",epfoTokenResponse.getBody()); 
// 			JSONObject obj1 = new JSONObject(message1);
// 			log.info("Response from EPFO TOKEN API - message " + obj1);
// 			log.info("last message " + obj1.getJSONObject("message"));
// 			JSONObject obj = obj1.getJSONObject("message");
// 			String access_token = obj.getString("access_token");
// 			log.info("access_token-->  {}",access_token); 
//
//
// 			//
// 			ResponseEntity<String> response = null;
// 			headers.setBearerAuth(access_token);
// 			headers.add("Bearer", access_token);
// 			HttpEntity<String> request1 = new HttpEntity<String>(headers);
//
// 			EpfoDetailsDto epfoDetails = new EpfoDetailsDto();
// 			
// 			response = restTemplate.exchange(epfoSecurityConfig.getTransactionIdUrl(), HttpMethod.GET, request1,
// 					String.class);
// 			String message2 = response.getBody();
// 			log.info("Response from EPFO Transaction API : " + message2);
// 			JSONObject obj2 = new JSONObject(message2);
// 			log.info("Response from EPFO Transaction API - obj: " + obj2);
// 			transactionId = obj2.getString("message").toString();
// 			epfoDetails.setTransactionid(transactionId);
// 			log.info("Generated transactionId Id is " + transactionId);
// 			log.info("transaction 123 id-->  {}",transactionId); 
//
//
//			
//
//			for (BulkUanDTO details : dataList) {
//				log.info("{}",details); 
//	             uanNumber = details.getUan();
//	             applicantId = details.getApplicantId();
//	             bulkUanId = details.getBulkUanId();
//	             uploadedBy = details.getUploadedBy();
//	             totalRecordUploaded = details.getTotalRecordUploaded();
//		 		 uanSave = new UanSearchData();
//
//	            
//	             log.info("APPLICANT_ID::>>  {}",applicantId); 
//					log.info("UANNUMBER::>>  {}",uanNumber); 
//					log.info("BulkUaNID::>>  {}",bulkUanId); 
//					log.info("UploadedBy::>>  {}",uploadedBy); 
//					
//				
//				int maxRetryCount = 2;
//				int retryCount = 0;
//				
//				try {
//					
//				    while (retryCount < maxRetryCount) {
//				    	
//				    	log.info("____________________epfoDetails.getUanusername()  {}",uanNumber); 
//			 			request.put(EPFOConstants.EPFO_USR, uanNumber);
//
//			 			request.put(EPFOConstants.EPFO_PWD, ' ');
//			 			request.put(EPFOConstants.EPFO_UAN, uanNumber);
//
//			 			HttpEntity<String> entity1 = new HttpEntity<String>(request.toString(), headers);
//			 			log.info("\\n------epfoSecurityConfig ------  {}",epfoSecurityConfig.getFinalSubmitPostUrl()); 
//						log.info("\\n------epfoDetails ------  {}",epfoDetails.getTransactionid()); 
//
//			 			response = restTemplate.exchange(
//			 					epfoSecurityConfig.getFinalSubmitPostUrl() + epfoDetails.getTransactionid(), HttpMethod.POST,
//			 					entity1, String.class);
//			 			String responseBody = response.getBody();
//			 			JSONObject obj3 = new JSONObject(responseBody);
//			 			log.info("\\n--------obj --------- {}",obj3); 
//			 			String resMsg = obj3.toString();
//			 			log.info("\\n--------resMsg --------- {}",resMsg); 
//
//
//
//			 			uanSave.setApplicantId(applicantId);
//			 			uanSave.setBulkUanId(bulkUanId);
//			 			uanSave.setUan(uanNumber);
//			 			uanSave.setTotalRecordUploaded(totalRecordUploaded);
//			 			uanSave.setTotalRecordFetched(totalRecordUploaded);
//			 			uanSave.setUploadedBy(uploadedBy);
//			 			uanSave.setUploadedOn(date);
//			 			uanSave.setEPFOResponse(resMsg);
//			 			
//			 			
//			 			bulkData.add(uanSave);
//
//				    	
//				    	boolean success = obj3.getBoolean("success");
//				        if (success) {
//				        	log.info("SUCCESS IS TRUE {}");
//				            break; // Success, exit the retry loop
//				        } else {
//				            // Retry the operation
//				            retryCount++;
//				            log.info("Retry # {}" + retryCount);
//				           
//				        }
//				    	
//				    }
//
//		 			if (retryCount == maxRetryCount) {
//		 			    // Maximum retry count reached. Operation failed.
//		 			   log.info("Maximum retry count reached. Operation failed. {}");
//		 
//		 			} else {
//		 			    // Operation succeeded
//		 			    log.info("Operation Succeeded!! {}");
//		 			}
//		 			uanSearchDataRepository.save(uanSave);
//
//					
//				} catch (Exception e) {
//					log.info(e.getMessage());
//				}
//	 			
//	 			
//	 			
//	        }
//			log.info("BULKUANID:: {}",bulkUanId); 
//			 TypedQuery<UanSearchData> query = entityManager.createQuery(
//			            "SELECT e FROM UanSearchData e WHERE e.bulkUanId = :bulkUanId", UanSearchData.class);
//			    query.setParameter("bulkUanId", bulkUanId);
//			    query.setMaxResults(1); // Limit the result to one record
//			    List<UanSearchData> resultList = query.getResultList();
//
//			  
//			    log.info("RESULTLIST::::>>> {}",resultList); 
//				log.info("RESULTLIST::::>>> {}",resultList.get(0)); 
//
//			
//				log.info("BULKDATA::>>> {}",bulkData.toString()); 
//
//		  
//			svcSearchResult.setData(resultList);
//			svcSearchResult.setMessage(bulkData.toString());			
//		} catch (Exception ex) {
//			log.error("Exception occured in BulkUan method in CandidateServiceImpl-->", ex);
//			svcSearchResult.setOutcome(false);
//			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes");
//
//
//		}
//		
//		return svcSearchResult;
//	}

	@Override
	public ServiceOutcome<List<UanSearchData>> retriveBulkUanData(String bulkUanId) {
		
		log.info("BULKUANID {}",bulkUanId); 

		ServiceOutcome<List<UanSearchData>> svcSearchResult = new ServiceOutcome<>();

		
		try {

			TypedQuery<UanSearchData> query = entityManager.createQuery(
		            "SELECT e FROM UanSearchData e WHERE e.bulkUanId = :bulkUanId", UanSearchData.class);
		    query.setParameter("bulkUanId", bulkUanId);
		    query.setMaxResults(1); // Limit the result to one record
		    List<UanSearchData> resultList = query.getResultList();
		    log.info("RESULTLIST::::>>> {}",resultList);
			log.info("RESULTLIST::::>>> {}",resultList.get(0)); 
			svcSearchResult.setData(resultList);

			
		} catch (Exception ex) {
			log.error("Exception occured in BulkUanRetrive method in CandidateServiceImpl-->", ex);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes");

		}
		
		return svcSearchResult;
	}

	public ServiceOutcome<DashboardDto> searchAllCandidate(SearchAllCandidateDTO searchAllcandidate) {
	    ServiceOutcome<DashboardDto> svcSearchResult = new ServiceOutcome<>();
		log.info("AgentName for Search All Candidate {}",searchAllcandidate.getAgentName());
		log.info("userSearchInput for Search All Candidate {}",searchAllcandidate.getUserSearchInput());
		log.info("ORGANISTATION ID:: {}",searchAllcandidate.getOrganisationId());
		List<CandidateDetailsDto> candidateDtoList = new ArrayList<CandidateDetailsDto>();
        List<Candidate> searchResult = null;


		Long userId = searchAllcandidate.getUserId();
		User findByUserId = userRepository.findByUserId(userId); 
	    Long orgId = searchAllcandidate.getOrganisationId();
	  
		      Date createdOnDate = findByUserId.getCreatedOn();
		      
		      log.info("CreatedOnDate {}",createdOnDate);

		      LocalDate currentDate = LocalDate.now();

		   LocalDateTime endOfDay = currentDate.atTime(LocalTime.MAX);
		   DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
		   String currentDateAndTime = endOfDay.format(formatter2);

		   log.info("CURRENT DATE AND TIME: {}" + currentDateAndTime);

		   if(searchAllcandidate.getRoleName().equalsIgnoreCase("agent")) {

		         searchResult = candidateRepository.searchAllCandidateByAgent(userId, searchAllcandidate.getUserSearchInput(),createdOnDate,currentDateAndTime);

		   }
		   else {
		        searchResult = candidateRepository.searchAllCandidateByAdmin(orgId, searchAllcandidate.getUserSearchInput(),createdOnDate,currentDateAndTime);
		   }
	        for (Candidate candidate : searchResult) {
	        	CandidateDetailsDto candidateDto = this.modelMapper.map(candidate, CandidateDetailsDto.class);
				candidateDto.setCreatedOn(formatter.format(candidate.getCreatedOn()));
				candidateDto.setSubmittedOn(
						candidate.getSubmittedOn() != null ? formatter.format(candidate.getSubmittedOn()) : null);
				CandidateEmailStatus candidateEmailStatus = candidateEmailStatusRepository
						.findByCandidateCandidateCode(candidate.getCandidateCode());
				if (candidateEmailStatus != null) {
					candidateDto.setDateOfEmailInvite(candidateEmailStatus.getDateOfEmailInvite() != null
							? formatter.format(candidateEmailStatus.getDateOfEmailInvite())
							: null);
					candidateDto.setDateOfEmailFailure(candidateEmailStatus.getDateOfEmailFailure() != null
							? formatter.format(candidateEmailStatus.getDateOfEmailFailure())
							: null);
					candidateDto.setDateOfEmailExpire(candidateEmailStatus.getDateOfEmailExpire() != null
							? formatter.format(candidateEmailStatus.getDateOfEmailExpire())
							: null);
					candidateDto.setDateOfEmailReInvite(candidateEmailStatus.getDateOfEmailReInvite() != null
							? formatter.format(candidateEmailStatus.getDateOfEmailReInvite())
							: null);
				}
				
	            Long candidateId = candidate.getCandidateId();
	            log.info("Candidate: {}",candidateId);

	            
	            CandidateStatus candidateStatus = candidateStatusRepository
						.findByCandidateCandidateCode(candidate.getCandidateCode());
				candidateDto.setCandidateStatusName(candidateStatus.getStatusMaster().getStatusName());
				
				List<ContentDTO> contentDTOList = contentService
						.getContentListByCandidateId(candidate.getCandidateId());
//				System.out.println(contentDTOList + "--------contentdtolist-------");
				candidateDto.setContentDTOList(contentDTOList);
				
				//adding cadidate status related details for search functionalities
				CandidateVerificationState updateVerificationStatus = candidateVerificationStateRepository.findByCandidateCandidateId(candidate.getCandidateId());
				if (updateVerificationStatus != null) {
					candidateDto.setPreOfferVerificationColorCode(updateVerificationStatus.getPreApprovalColorCodeStatus());
					candidateDto.setInterimVerificationColorCode(updateVerificationStatus.getInterimColorCodeStatus());
					candidateDto.setFinalVerificationColorCode(updateVerificationStatus.getFinalColorCodeStatus());
					
					//adding report delivered dates
					candidateDto.setPreOfferReportDate(
							updateVerificationStatus.getPreApprovalTime() != null ? formatter.format(Date.from(updateVerificationStatus.getPreApprovalTime().toInstant())) : null);
					candidateDto.setInterimReportDate(
							updateVerificationStatus.getInterimReportTime() != null ? formatter.format(Date.from(updateVerificationStatus.getInterimReportTime().toInstant())) : null);
					candidateDto.setFinalReportDate(
							updateVerificationStatus.getFinalReportTime() != null ? formatter.format(Date.from(updateVerificationStatus.getFinalReportTime().toInstant())) : null);
					candidateDto.setCaseInitiationDate(
							updateVerificationStatus.getCaseInitiationTime() != null ? formatter.format(Date.from(updateVerificationStatus.getCaseInitiationTime().toInstant())) : null);
				}

				candidateDtoList.add(candidateDto);    
//				log.info("candidateDateDTOLIST::::============== {}",candidateDtoList.toString());

	        }

	        DashboardDto dashboardDtoObj = new DashboardDto(null, null, null, null, null,
	        		orgId, null, candidateDtoList,0);
			if (!candidateDtoList.isEmpty()) {
				svcSearchResult.setData(dashboardDtoObj);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("Candidate list fetched successfully.");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NO Candidate FOUND");
			}
  
	    return svcSearchResult;
	}

	@Override
	@Transactional
	public ServiceOutcome<SuspectEmpMaster> deleteAllSuspectById(List<Long> ids) {
		ServiceOutcome<SuspectEmpMaster> svcSearchResult = new ServiceOutcome<>();

		try {
			SuspectEmpMaster result = null;
			if (ids == null || ids.contains(0L)) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please specify User");
			}
			else {
				log.info("EMP IDS:: {}",ids);
				String sql = "DELETE FROM t_dgv_suspect_emp_master WHERE suspect_emp_master_id IN :ids";
		        Query query = entityManager.createNativeQuery(sql);
		        query.setParameter("ids", ids);
		        query.executeUpdate();
		        svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("suspectEmploye Details Deleted Successfully");
			}
			
		} catch (Exception e) {
			log.error("Exception occured in deleteAllSuspectById method in candidateServiceImp-->" + e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}

		return svcSearchResult;
	}
	
	@Override
	public ServiceOutcome<Organization> findOrganization(String candidateCode) {

	    ServiceOutcome<Organization> svcSearchResult = new ServiceOutcome<>();

		try {
			
			log.info("CandidateCode:: {}",candidateCode);
			Candidate findByCandidateCode = candidateRepository.findByCandidateCode(candidateCode);
			if (findByCandidateCode != null) {
				Organization organization = findByCandidateCode.getOrganization();
				svcSearchResult.setData(organization);
			    log.info("OrgId:: {}",organization);
			} else {
			    log.info("Candidate Not Found:: {}");

			}
			
		} catch (Exception e) {
			log.info("An error occurred: in candidateServiceImpl {}",e.getMessage());

		}
		
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<SuspectEmpMaster>> searchDnh(String searchData) {
	    ServiceOutcome<List<SuspectEmpMaster>> svcSearchResult = new ServiceOutcome<>();
	    
	    try {
	        JSONObject json = new JSONObject(searchData);
	        String userSearchInput = json.getString("userSearchInput");
	        Long orgId = json.getLong("orgId");
	        
	        log.info("userSearchInput: {}" + userSearchInput);
	        log.info("orgId: " + orgId);
		  
	        String removenumbers = extractMainEmployerName(userSearchInput).trim();
	        List<SuspectEmpMaster> searchDnh = suspectEmpMasterRepository.getByOrganizationIdAndSuspectCompanyName(orgId, removenumbers + "%"); 
	        svcSearchResult.setData(searchDnh);	   
	        svcSearchResult.setOutcome(true);
	        
		} catch (Exception e) {
			log.info("Exception In SearchDnhb {}",e.getMessage());
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("NO DATA FOUND");
		}
	 
		
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<UanSearchData>> uanSearchDashboardFilter(
			UanSearchDashboardFilterDTO uanSearchDashboardFilter) {

		ServiceOutcome<List<UanSearchData>> svcSearchResult = new ServiceOutcome<>();
		
		try {
			
			String fromDate = uanSearchDashboardFilter.getFromDate();
			String toDate = uanSearchDashboardFilter.getToDate();

			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			LocalDate from = LocalDate.parse(fromDate, inputFormatter);
			LocalDate to = LocalDate.parse(toDate, inputFormatter);

			// Set the time portions to 12:00 AM and 11:59 PM, respectively
			LocalTime fromTime = LocalTime.of(0, 0);
			LocalTime toTime = LocalTime.of(23, 59, 59);

			LocalDateTime fromDateTime = from.atTime(fromTime);
			LocalDateTime toDateTime = to.atTime(toTime);

			String formattedFromDate = fromDateTime.format(outputFormatter);
			String formattedToDate = toDateTime.format(outputFormatter);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date fromDateFormat = dateFormat.parse(formattedFromDate);
			Date toDateFormat = dateFormat.parse(formattedToDate);
				
			List<UanSearchData> uanSearchFilterData = uanSearchDataRepository.uanSearchDashboardFilter(fromDateFormat, toDateFormat, uanSearchDashboardFilter.getUser());

			Set<String> uniqueBulkIds = new HashSet<>();
			List<UanSearchData> filteredData = new ArrayList<>();
			List<UanSearchData> nullBulkIdData = new ArrayList<>();

			for (UanSearchData searchData : uanSearchFilterData) {
			    String bulkId = searchData.getBulkUanId();

			    if (bulkId != null && !uniqueBulkIds.contains(bulkId)) {
			        uniqueBulkIds.add(bulkId);
			        filteredData.add(searchData);
			    } else if (bulkId == null) {
			        nullBulkIdData.add(searchData);
			    }
			}

			filteredData.addAll(nullBulkIdData);	
			svcSearchResult.setData(filteredData);
			svcSearchResult.setMessage("DATA FOUND");
			svcSearchResult.setOutcome(true);
			
			}
		catch (Exception e) {
		log.info("CandidateServiceIMP uanSearchDashboardFilter ---> {}",e.getMessage());
		svcSearchResult.setData(null);
		svcSearchResult.setOutcome(false);
		svcSearchResult.setMessage("NO DATA FOUND");
		}
		
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<Color> getCandidateReportStatus(String candidateCode) {
		ServiceOutcome<Color> sv =new ServiceOutcome<>();
		try {
			Color statusColor=null;
			Candidate findByCandidateCode = candidateRepository.findByCandidateCode(candidateCode);
			CandidateVerificationState candidateVerificationState=candidateVerificationStateRepository.findByCandidateCandidateId(findByCandidateCode.getCandidateId());
			if(candidateVerificationState!=null && candidateVerificationState.getInterimColorCodeStatus()!=null){
				statusColor= candidateVerificationState.getInterimColorCodeStatus();
			    log.info("candidateReportStatus FOR INTERIM REPORT::{}",statusColor);
				
			}
            if(candidateVerificationState!=null && candidateVerificationState.getFinalColorCodeStatus()!=null) {
            	statusColor= candidateVerificationState.getFinalColorCodeStatus();
			    log.info("candidateReportStatus FOR FINAL REPORT::{}",statusColor);
			}
            sv.setData(statusColor);
            sv.setOutcome(true);
         }catch(Exception e){
			log.info("EXCEPTION while retriving the candidate report status::{}",e);
		}
		
		return sv;
	}

	@Override
	public ServiceOutcome<String> updateCandidateReportStatus(String candidateCode, Long colorId) {
		ServiceOutcome<String> serviceOutcome=new ServiceOutcome<>();
		try {
			Candidate findByCandidateCode = candidateRepository.findByCandidateCode(candidateCode);
			CandidateVerificationState candidateVerificationState=candidateVerificationStateRepository.findByCandidateCandidateId(findByCandidateCode.getCandidateId());
			if(candidateVerificationState!=null && colorId!=null) {
				Optional<Color> color=colorRepository.findById(colorId);
				if(color.isPresent()) {
				    Color c=color.get();
				    candidateVerificationState.setInterimColorCodeStatus(c);
					candidateVerificationStateRepository.save(candidateVerificationState);
					
					serviceOutcome.setData("Status Updated Successfully.");	
					serviceOutcome.setOutcome(true);
					serviceOutcome.setMessage("Status Updated Successfully.");
					
				}
						   
		    }else {
		    	candidateVerificationState.setInterimColorCodeStatus(null);
				candidateVerificationStateRepository.save(candidateVerificationState);
				
				serviceOutcome.setData("Status Updated Successfully.");	
				serviceOutcome.setOutcome(true);
				serviceOutcome.setMessage("Status Updated Successfully.");
		    	
		    }
			
		}catch(Exception e) {
			log.info("EXCEPTION while Updating the candidate report status::{}",e);
		}
		return serviceOutcome;
	}

	@Override
	public ServiceOutcome<List<Long>> oldCandidatesPurge() {
		ServiceOutcome<List<Long>> serviceOutcome=new ServiceOutcome<>();
		List<Candidate> purgedCandidates=new ArrayList<>();
		List<Long> candidateIds =new ArrayList<>();
//	    candidateIds.add(4572L);        //enable only for testing
		try {
			//from below assign values greater then 0 to only one variable at a time.
//			int monthsOld=0;
//			int yearsOld=1;
//			Calendar calendar = Calendar.getInstance();
//	        calendar.setTime(new Date());
//	        if (monthsOld > 0) {
//	            calendar.add(Calendar.MONTH, -monthsOld);
//	        } else if (yearsOld > 0) {
//	            calendar.add(Calendar.YEAR, -yearsOld);
//	        }
			
			List<Organization> activeORGS = organizationRepository.findAllByIsActive(true);
			log.info("No of active ORGS::{}",activeORGS.size());
			
			for(Organization org :activeORGS) {
				Calendar calendar = Calendar.getInstance();
		        calendar.setTime(new Date());
		        
		        int daysToPurge=Integer.parseInt(org.getDaysToPurge()!=null ? org.getDaysToPurge() : "0");
		        
		        int years = daysToPurge!=0 ? daysToPurge / 365 : 7 ;
		        
		        calendar.add(Calendar.YEAR, -years);
		        Date cutoffDate = calendar.getTime();
		        
		        List<Candidate> candidatesNeedsToPurge = candidateRepository.findByCreatedOnBeforeAndOrganizationOrganizationId(cutoffDate, org.getOrganizationId());
			    List<Long> orgsCandidateIds = candidatesNeedsToPurge
			    	    .stream()
			    	    .map(Candidate::getCandidateId) // Map Candidate objects to their IDs
			    	    .collect(Collectors.toList()); // Collect IDs into a new List
			    log.info("No Of candidatesNeedsToPurge in ORG::{}",org.getOrganizationName()+"::"+candidatesNeedsToPurge.size());
			    
			    purgedCandidates.addAll(candidatesNeedsToPurge);
			    candidateIds.addAll(orgsCandidateIds);
			}
			
			    //delete the records only when records are present.
			    if(candidateIds!=null && !candidateIds.isEmpty()) {
				    //Delete candidate address verification records
				    candidateAdressVerificationRepository.deleteCanAddressVerByCanIds(candidateIds);
				    log.info("candidateAdressVerificationRepository deleted...");
				    
				    //get the candidate status ids
				    List<Long> candidateStatusIds= candidateStatusRepository.getCandidateStatusIdByCandidateId(candidateIds);
				    log.info("No Of candidateStatusIds ::{}",candidateStatusIds);
				    
				    //delete the candidateCafRelation table by the candidate status ids
				    candidateCafRelationshipRepository.deleteCafRelRecordsByCanStatusIds(candidateStatusIds);
				    log.info("deleted the candidateCafRelation");
				    
				    //get vendorChecksIds
				    List<Long> vendorCheckIds=vendorChecksRepository.getvendorCheckIdByCandidateId(candidateIds);
				    log.info("No Of vendorCheckIds ::{}",vendorCheckIds);
				    
				    //delete vendorUploadChecks 
				    vendorUploadChecksRepository.deleteVendorCheckUploadByVendorCheckIds(vendorCheckIds);
				    log.info("Deleted vendor check uploads ::{}");
				    
				    //Delete directly linked tables record based on joins
				    candidateRepository.deleteCandidatesAndRelatedRecords(candidateIds);
				    log.info("deleted ALL the candidates child records");
				    
	//			  //Now finally Delete candidate basic records
	//			    candidateRepository.deleteAllById(candidateIds);
	//			    log.info("deleted ALL the candidates records");
			    	
			    	//dont delete candidate basic just updating candidate basic table personal info records for purge candidates.
			    	log.info("Candidates Basic records Updating Started");
			    	List<Candidate> updatedCandidates = purgedCandidates.stream().map(x -> {
			    			x.setPurgedOn(new Date());
			    			
			    			//general
			    			x.setDateOfBirth(null);
			    			x.setEmailId(null);
			    			x.setContactNumber(null);
			    			
			    			//adhar
			    			x.setAadharDob(null);
			    			x.setAadharFatherName(null);
			    			x.setAadharGender(null);
			    			x.setAadharName(null);
			    			x.setAadharNumber(null);
			    			
			    			//pan
			    			x.setPanDob(null);
			    			x.setPanName(null);
			    			x.setPanNumber(null);
			    			x.setItrPanNumber(null);
			    			
			    			//uan
			    			x.setUan(null);
			    			x.setUanDob(null);
			    			x.setUanName(null);
			    			
			    			return x;
			    	}).collect(Collectors.toList());
			    	
			    	candidateRepository.saveAll(updatedCandidates);
			    	
			    	serviceOutcome.setData(candidateIds);
				    serviceOutcome.setMessage("Candidates Purged Successfully..");
				    serviceOutcome.setOutcome(true);
			    }else {
			    	log.info("NO RECORDS FOUND TO PURGE..");
			    	serviceOutcome.setData(null);
				    serviceOutcome.setMessage("NO RECORDS FOUND TO PURGE..");
				    serviceOutcome.setOutcome(true);
			    }
			    
		    return serviceOutcome;
		} catch (Exception e) {
			log.info("Exception In oldCandidatesPurge {}",e.getMessage());
			serviceOutcome.setData(null);
		    serviceOutcome.setMessage("Something Went Wrong..");
		    serviceOutcome.setOutcome(false);
		}
		return serviceOutcome;
	}

//	public int getNewUploadCountForDashboard(String fromDate,String toDate, Long userId, String status) {
//		
//		List<String> statusCodes = new ArrayList<>();
//		List<Long> agentIds = new ArrayList<>();
//		String strToDate = "";
//		String strFromDate = "";
//		int newUploadCount=0;
//
//		try {
//			if (userId != null && userId != 0l
//								&& StringUtils.isNotBlank(status)) {
//	
//				User user = userRepository.findById(userId).get();
//				strToDate = toDate != null ? toDate
//										: ApplicationDateUtils.getStringTodayAsDDMMYYYY();
//				strFromDate = fromDate != null ? fromDate
//										: ApplicationDateUtils.subtractNoOfDaysFromDateAsDDMMYYYY(
//				new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);
//				Date startDate = formatter.parse(strFromDate + " 00:00:00");
//				Date endDate = formatter.parse(strToDate + " 23:59:59");
//		
//				statusCodes.addAll(statusMasterRepository.findAll().parallelStream().map(x -> x.getStatusCode())
//											.collect(Collectors.toList()));
//				List<StatusMaster> statusMasterList = statusMasterRepository.findByStatusCodeIn(statusCodes);
//								List<Long> statusIds = statusMasterList.stream().map(x -> x.getStatusMasterId())
//										.collect(Collectors.toList());
//				if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_ADMIN")
//										|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_PARTNERADMIN")) {
//					
//					newUploadCount = candidateRepository.getNewUploadCountByOrganizationIdAndStatusAndCreatedOn(
//								user.getOrganization().getOrganizationId(), statusIds, startDate, endDate);
//					log.info("NEW UPLOAD COUNT FROM ADMIN ::{}",newUploadCount);				
//						
//				}
//				if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTSUPERVISOR")
//										|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTHR")) {
//					List<User> agentList = userRepository.findAllByAgentSupervisorUserId(user.getUserId());
//					if (!agentList.isEmpty()) {
//						agentIds = agentList.stream().map(x -> x.getUserId()).collect(Collectors.toList());
//					}
//					agentIds.add(user.getUserId());
//					newUploadCount = candidateRepository.getNewUploadCountByUserIdAndStatusAndLastUpdated(agentIds,
//							statusIds, startDate, endDate);
//					log.info("NEW UPLOAD COUNT FROM AGENTS ::{}",newUploadCount);
//				}
//				return newUploadCount;
//			}
//		}catch(Exception e) {
//			log.error("Exception in getNewUploadCountForDashboard ::{}",e);
//		}
//		return newUploadCount;
//	}
	
	public List<Object[]> getCountsForDashboard(String strToDate, String strFromDate,
			Long userId) {
		List<Object[]> activityList =new ArrayList<>();
		List<Long> agentIds = new ArrayList<>();
		Long orgId=0L;
		try {
			Date startDate = formatter.parse(strFromDate + " 00:00:00");
			Date endDate = formatter.parse(strToDate + " 23:59:59");
			User user = userRepository.findById(userId).get();
			if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_ADMIN")
					|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_PARTNERADMIN")) {
				
				orgId= user.getOrganization().getOrganizationId();
			}
			if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTSUPERVISOR")
					|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTHR")) {
				List<User> agentList = userRepository.findAllByAgentSupervisorUserId(user.getUserId());
				if (!agentList.isEmpty()) {
					agentIds = agentList.stream().map(x -> x.getUserId()).collect(Collectors.toList());
				}
				agentIds.add(user.getUserId());
			}
			
			//Query Start
			StringBuilder query = new StringBuilder();
			query.append("select newupload,invalid,reinvites,interReport,finalReport,cancelled,invExpired,pendingNow\n");
			query.append("from \n");
			query.append("(select count(distinct tdcsh.candidate_id) as newupload from t_dgv_candidate_status_history tdcsh\n");
			query.append("join t_dgv_candidate_basic bas on bas.candidate_id = tdcsh.candidate_id \n");
			query.append("join t_dgv_organization_master org on bas.organization_id = org.organization_id \n");
			query.append("join t_dgv_status_master mas on mas.status_master_id = tdcsh.status_master_id \n");
			query.append("where mas.status_code IN ('NEWUPLOAD','INVALIDUPLOAD')\n");
			if(orgId!=0) {
				query.append("and org.organization_id =:orgId\n");
			}
			if(agentIds!=null && !agentIds.isEmpty()){
				query.append("and bas.created_by IN (:agentIds)\n");
			}
			
			query.append("and tdcsh.candidate_status_change_timestamp between :startDate and :endDate) ne,\n");
			query.append("(select count(*) as invalid from t_dgv_candidate_status tdcsh\n");
			query.append("join t_dgv_candidate_basic bas on bas.candidate_id = tdcsh.candidate_id \n");
			query.append("join t_dgv_organization_master org on bas.organization_id = org.organization_id \n");
			query.append("join t_dgv_status_master mas on mas.status_master_id = tdcsh.status_master_id \n");
			query.append("where mas.status_code IN ('INVALIDUPLOAD')\n");
			if(orgId!=0) {
				query.append("and org.organization_id =:orgId\n");
			}
			if(agentIds!=null && !agentIds.isEmpty()){
				query.append("and bas.created_by IN (:agentIds)\n");
			}
			query.append("and tdcsh.last_updated_on between :startDate and :endDate) ie,\n");
			query.append("(select count(*) as reinvites from t_dgv_candidate_status tdcsh\n");
			query.append("join t_dgv_candidate_basic bas on bas.candidate_id = tdcsh.candidate_id \n");
			query.append("join t_dgv_organization_master org on bas.organization_id = org.organization_id \n");
			query.append("join t_dgv_status_master mas on mas.status_master_id = tdcsh.status_master_id \n");
			query.append("where mas.status_code ='REINVITE'\n");
			if(orgId!=0) {
				query.append("and org.organization_id =:orgId\n");
			}
			if(agentIds!=null && !agentIds.isEmpty()){
				query.append("and bas.created_by IN (:agentIds)\n");
			}
			query.append("and tdcsh.last_updated_on between :startDate and :endDate) re,\n");
			query.append("(select count(*) as interReport from t_dgv_candidate_status tdcsh\n");
			query.append("join t_dgv_candidate_basic bas on bas.candidate_id = tdcsh.candidate_id \n");
			query.append("join t_dgv_organization_master org on bas.organization_id = org.organization_id \n");
			query.append("join t_dgv_status_master mas on mas.status_master_id = tdcsh.status_master_id \n");
			query.append("where mas.status_code ='INTERIMREPORT'\n");
			if(orgId!=0) {
				query.append("and org.organization_id =:orgId\n");
			}
			if(agentIds!=null && !agentIds.isEmpty()){
				query.append("and bas.created_by IN (:agentIds)\n");
			}
			query.append("and tdcsh.last_updated_on between :startDate and :endDate) ir,\n");
			query.append("(select count(*) as finalReport from t_dgv_candidate_status tdcsh \n");
			query.append("join t_dgv_candidate_basic bas on bas.candidate_id = tdcsh.candidate_id \n");
			query.append("join t_dgv_organization_master org on bas.organization_id = org.organization_id \n");
			query.append("join t_dgv_status_master mas on mas.status_master_id = tdcsh.status_master_id \n");
			query.append("where mas.status_code ='FINALREPORT'\n");
			if(orgId!=0) {
				query.append("and org.organization_id =:orgId\n");
			}
			if(agentIds!=null && !agentIds.isEmpty()){
				query.append("and bas.created_by IN (:agentIds)\n");
			}
			query.append("and tdcsh.last_updated_on between :startDate and :endDate) fr,\n");
			query.append("(select count(*) as cancelled from t_dgv_candidate_status tdcsh\n");
			query.append("join t_dgv_candidate_basic bas on bas.candidate_id = tdcsh.candidate_id \n");
			query.append("join t_dgv_organization_master org on bas.organization_id = org.organization_id \n");
			query.append("join t_dgv_status_master mas on mas.status_master_id = tdcsh.status_master_id \n");
			query.append("where mas.status_code ='PROCESSDECLINED'\n");
			if(orgId!=0) {
				query.append("and org.organization_id =:orgId\n");
			}
			if(agentIds!=null && !agentIds.isEmpty()){
				query.append("and bas.created_by IN (:agentIds)\n");
			}
			query.append("and tdcsh.last_updated_on between :startDate and :endDate) can,\n");
			query.append("(select count(*) as invExpired from t_dgv_candidate_status tdcsh\n");
			query.append("join t_dgv_candidate_basic bas on bas.candidate_id = tdcsh.candidate_id \n");
			query.append("join t_dgv_organization_master org on bas.organization_id = org.organization_id \n");
			query.append("join t_dgv_status_master mas on mas.status_master_id = tdcsh.status_master_id \n");
			query.append("where mas.status_code ='INVITATIONEXPIRED'\n");
			if(orgId!=0) {
				query.append("and org.organization_id =:orgId\n");
			}
			if(agentIds!=null && !agentIds.isEmpty()){
				query.append("and bas.created_by IN (:agentIds)\n");
			}
			query.append("and tdcsh.last_updated_on between :startDate and :endDate) inve,\n");
			query.append("(select count(*) as pendingNow from t_dgv_candidate_status tds\n");
			query.append("join t_dgv_candidate_basic bas on bas.candidate_id = tds.candidate_id \n");
			query.append("join t_dgv_organization_master org on bas.organization_id = org.organization_id \n");
			query.append("join t_dgv_status_master mas on mas.status_master_id = tds.status_master_id \n");
			query.append("where mas.status_code ='PENDINGAPPROVAL'\n");
			if(orgId!=0) {
				query.append("and org.organization_id =:orgId\n");
			}
			if(agentIds!=null && !agentIds.isEmpty()){
				query.append("and bas.created_by IN (:agentIds)\n");
			}
			query.append("and tds.last_updated_on between :startDate and :endDate) pd\n");
			
			Query resultQuery = entityManager.createNativeQuery(query.toString());
			resultQuery.setParameter("startDate", startDate);
			resultQuery.setParameter("endDate", endDate);
			if(orgId!=0) {
				resultQuery.setParameter("orgId", orgId);
			}
			if(agentIds!=null && !agentIds.isEmpty()){
				resultQuery.setParameter("agentIds", agentIds);
			}
			
			
			activityList = resultQuery.getResultList();
		} catch (Exception ex) {
			log.error("Exception occured in getCountsForDashboard method in CandidateServiceImpl-->", ex);
		}
		return activityList;
		
	}

	@Override
	public ServiceOutcome<DashboardDto> getAllPendingCandidateList(DashboardDto dashboardDto) {
			ServiceOutcome<DashboardDto> svcSearchResult = new ServiceOutcome<DashboardDto>();
			List<Candidate> candidateList = new ArrayList<>();
			List<CandidateDetailsDto> candidateDtoList = new ArrayList<>();
			List<String> statusCodes = new ArrayList<>();
			List<Long> agentIds = new ArrayList<>();
			String strToDate = "";
			String strFromDate = "";
			try {
				if (dashboardDto.getUserId() != null && dashboardDto.getUserId() != 0l
						&& StringUtils.isNotBlank(dashboardDto.getStatus())) {
					log.info("PAGE NUMBER TO GET PENDING RECORDS::{}",dashboardDto.getPageNumber());
					Pageable pageable =null;
					if(dashboardDto.getPageNumber()!=null) {
						pageable = PageRequest.of(dashboardDto.getPageNumber(), 10);
					}
					User user = userRepository.findById(dashboardDto.getUserId()).get();
					Date startDate = user.getCreatedOn();
					Date endDate = new Date();
					strToDate =sdf.format(endDate);
					strFromDate =sdf.format(startDate);
					
					String status = dashboardDto.getStatus();
					if (status.equals("PENDINGAPPROVAL")) {	
						status = "PENDINGAPPROVAL";
						statusCodes.add(0, status);

//						status = "INTERIMREPORT";
//						statusCodes.add(0, status);
					}
					if (status.equals("INTERIMREPORT")) {
						status = "INTERIMREPORT";
						statusCodes.add(0, status);
					} else if (status.equals("CAFPENDING")) {
						status = "RELATIVEADDRESS";
						statusCodes.add(0, status);
					} else if (status.equals("EPFOSKIPPED")) {
						Collections.addAll(statusCodes, "ITR", "DIGILOCKER");
					} else if (status.equals("NEWUPLOAD")) {
						statusCodes.addAll(statusMasterRepository.findAll().parallelStream().map(x -> x.getStatusCode())
								.collect(Collectors.toList()));
					} else {
						statusCodes.add(0, status);
					}
					List<StatusMaster> statusMasterList = statusMasterRepository.findByStatusCodeIn(statusCodes);
					List<Long> statusIds = statusMasterList.stream().map(x -> x.getStatusMasterId())
							.collect(Collectors.toList());
					if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_ADMIN")
							|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_PARTNERADMIN")) {
						if(status.equals("NEWUPLOAD")){
					    		candidateList = candidateRepository.getPageCandidateListByOrganizationIdAndStatusAndCreatedOn(
										user.getOrganization().getOrganizationId(), statusIds, startDate, endDate,pageable);
							
						}else {
								candidateList = candidateRepository.getPageCandidateListByOrganizationIdAndStatusAndLastUpdated(
										user.getOrganization().getOrganizationId(), statusIds, startDate, endDate,pageable);
								log.info("PAGINATION CANDIDATE LIST SIZE::{}",candidateList.size());
							
						}
//						if (dashboardDto.getStatus().equals("EPFOSKIPPED")) {
//							
//							candidateList =candidateRepository.getPageEPFOSkippedCandidateListForAdmin(
//									    user.getOrganization().getOrganizationId(), statusIds, startDate, endDate,pageable);
//							log.info("EPFOSKIPPED  LIST SIZE::{}",candidateList.size());
//						}
						
						
					}
					if (user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTSUPERVISOR")
							|| user.getRole().getRoleCode().equalsIgnoreCase("ROLE_AGENTHR")) {
						List<User> agentList = userRepository.findAllByAgentSupervisorUserId(user.getUserId());
						if (!agentList.isEmpty()) {
							agentIds = agentList.stream().map(x -> x.getUserId()).collect(Collectors.toList());
						}
						agentIds.add(user.getUserId());
						
							candidateList = candidateRepository.getPageCandidateListByUserIdAndStatusAndLastUpdated(agentIds,
									statusIds, startDate, endDate,pageable);
							log.info("PAGINATION CANDIDATE LIST SIZE FOR AGENT::{}",candidateList.size());
						
//							if (dashboardDto.getStatus().equals("EPFOSKIPPED")) {
//								candidateList =candidateRepository.getPageEPFOSkippedCandidateListForAgent(agentIds,
//										statusIds, startDate, endDate,pageable);
//							}
					}
					if (dashboardDto.getStatus().equals("EPFOSKIPPED")) {
						candidateList = candidateList.parallelStream().filter(x -> x.getIsUanSkipped()!=null)
								.collect(Collectors.toList());
					}
					if (dashboardDto.getStatus().equals("DIGILOCKER") || dashboardDto.getStatus().equals("ITR")) {
						candidateList = candidateList.parallelStream()
								.filter(x -> x.getIsUanSkipped() != null ? !x.getIsUanSkipped() : true)
								.collect(Collectors.toList());
					}
					for (Candidate candidate : candidateList) {
						CandidateDetailsDto candidateDto = this.modelMapper.map(candidate, CandidateDetailsDto.class);
						candidateDto.setCreatedOn(formatter.format(candidate.getCreatedOn()));
						candidateDto.setSubmittedOn(
								candidate.getSubmittedOn() != null ? formatter.format(candidate.getSubmittedOn()) : null);
						CandidateEmailStatus candidateEmailStatus = candidateEmailStatusRepository
								.findByCandidateCandidateCode(candidate.getCandidateCode());
						if (candidateEmailStatus != null) {
							candidateDto.setDateOfEmailInvite(candidateEmailStatus.getDateOfEmailInvite() != null
									? formatter.format(candidateEmailStatus.getDateOfEmailInvite())
									: null);
							candidateDto.setDateOfEmailFailure(candidateEmailStatus.getDateOfEmailFailure() != null
									? formatter.format(candidateEmailStatus.getDateOfEmailFailure())
									: null);
							candidateDto.setDateOfEmailExpire(candidateEmailStatus.getDateOfEmailExpire() != null
									? formatter.format(candidateEmailStatus.getDateOfEmailExpire())
									: null);
							candidateDto.setDateOfEmailReInvite(candidateEmailStatus.getDateOfEmailReInvite() != null
									? formatter.format(candidateEmailStatus.getDateOfEmailReInvite())
									: null);
						}
						CandidateStatus candidateStatus = candidateStatusRepository
								.findByCandidateCandidateCode(candidate.getCandidateCode());
						Boolean uan = candidate.getIsUanSkipped() != null ? candidate.getIsUanSkipped() : false;
						if (candidateStatus.getStatusMaster().getStatusCode().equals("DIGILOCKER") && uan
								|| candidateStatus.getStatusMaster().getStatusCode().equals("ITR") && uan) {
							candidateDto.setCandidateStatusName("EPFO Skipped");
						} else {
							candidateDto.setCandidateStatusName(candidateStatus.getStatusMaster().getStatusName());
						}
						candidateDto.setLastUploadedOn(candidateStatus.getLastUpdatedOn());
						List<ContentDTO> contentDTOList = contentService
								.getContentListByCandidateId(candidate.getCandidateId());
//						System.out.println(contentDTOList + "--------contentdtolist-------");
						candidateDto.setContentDTOList(contentDTOList);
						
						CandidateVerificationState updateVerificationStatus = candidateVerificationStateRepository.findByCandidateCandidateId(candidate.getCandidateId());
						if (updateVerificationStatus != null) {
							candidateDto.setPreOfferVerificationColorCode(updateVerificationStatus.getPreApprovalColorCodeStatus());
							candidateDto.setInterimVerificationColorCode(updateVerificationStatus.getInterimColorCodeStatus());
							candidateDto.setFinalVerificationColorCode(updateVerificationStatus.getFinalColorCodeStatus());
						}
						candidateDtoList.add(candidateDto);

					}
					
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
					Collections.sort(candidateDtoList, (s1, s2) -> {
						try { 
//							LocalDate date1 = LocalDate.parse(s1.getSubmittedOn() != null ? s1.getSubmittedOn() : s1.getCreatedOn(), formatter);
//							LocalDate date2 = LocalDate.parse(s2.getSubmittedOn() != null ? s2.getSubmittedOn() : s2.getCreatedOn(), formatter);
//							return date1.compareTo(date2);
							LocalDateTime dateTime1 = LocalDateTime.parse(
								    s1.getSubmittedOn() != null ? s1.getSubmittedOn() : s1.getCreatedOn(), formatter
								);
								LocalDateTime dateTime2 = LocalDateTime.parse(
								    s2.getSubmittedOn() != null ? s2.getSubmittedOn() : s2.getCreatedOn(), formatter
								);
								return dateTime2.compareTo(dateTime1);
						} catch (DateTimeParseException e) {
							log.error("Exception occured in getAllCandidateList method in CandidateServiceImpl-->", e);
							return 0; // Handle parsing error, e.g., consider them equal
						}
					}); 
					
					DashboardDto dashboardDtoObj = new DashboardDto(strFromDate, strToDate, null, null, null,
							dashboardDto.getUserId(), dashboardDto.getStatus(), candidateDtoList,dashboardDto.getPageNumber());
					if (!candidateDtoList.isEmpty()) {
						svcSearchResult.setData(dashboardDtoObj);
						svcSearchResult.setOutcome(true);
						svcSearchResult.setMessage("Candidate list fetched successfully.");
						svcSearchResult.setStatus(status);
					} else {
						svcSearchResult.setData(null);
						svcSearchResult.setOutcome(false);
						svcSearchResult.setMessage("NO Candidate FOUND");
					}
				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult.setMessage("please specify user.");
				}
			} catch (Exception ex) {
				log.error("Exception occured in getAllCandidateList method in CandidateServiceImpl-->", ex);
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult
						.setMessage(messageSource.getMessage("ERROR.MESSAGE", null, LocaleContextHolder.getLocale()));
			}
			return svcSearchResult;
	}
	
	public List<CandidateStatusCountDto> getPendingCountsForAgent(List<Long> agentIds,
			Date startDate,Date endDate) {
		List<CandidateStatusCountDto> candidateStatusCountDtoList=new ArrayList<>();
		try {
			StringBuffer query = new StringBuffer();
			query.append("SELECT loaPending, itrPending, epfoPending, preOfferPending, relAddress, processDecline, epfoSkipped, qcPending\n");
			query.append("FROM (\n");

			// LOA Pending
			query.append("(SELECT COUNT(*) AS loaPending FROM t_dgv_candidate_status tdcsh\n");
			query.append("JOIN t_dgv_candidate_basic bas ON bas.candidate_id = tdcsh.candidate_id\n");
			query.append("JOIN t_dgv_organization_master org ON bas.organization_id = org.organization_id\n");
			query.append("JOIN t_dgv_status_master mas ON mas.status_master_id = tdcsh.status_master_id\n");
			query.append("WHERE mas.status_code IN ('INVITATIONSENT')\n");
			query.append("AND bas.created_by in (:agentIds)\n");
			query.append("AND tdcsh.last_updated_on BETWEEN :startDate AND :endDate) ne,\n");

			// ITR Pending
			query.append("(SELECT COUNT(*) AS itrPending FROM t_dgv_candidate_status tdcsh\n");
			query.append("JOIN t_dgv_candidate_basic bas ON bas.candidate_id = tdcsh.candidate_id\n");
			query.append("JOIN t_dgv_organization_master org ON bas.organization_id = org.organization_id\n");
			query.append("JOIN t_dgv_status_master mas ON mas.status_master_id = tdcsh.status_master_id\n");
			query.append("WHERE mas.status_code IN ('DIGILOCKER')\n");
			query.append("AND bas.created_by in (:agentIds)\n");
			query.append("AND tdcsh.last_updated_on BETWEEN :startDate AND :endDate) ie,\n");

			// EPFO Pending
			query.append("(SELECT COUNT(*) AS epfoPending FROM t_dgv_candidate_status tdcsh\n");
			query.append("JOIN t_dgv_candidate_basic bas ON bas.candidate_id = tdcsh.candidate_id\n");
			query.append("JOIN t_dgv_organization_master org ON bas.organization_id = org.organization_id\n");
			query.append("JOIN t_dgv_status_master mas ON mas.status_master_id = tdcsh.status_master_id\n");
			query.append("WHERE mas.status_code = 'ITR'\n");
			query.append("AND bas.created_by in (:agentIds)\n");
			query.append("AND tdcsh.last_updated_on BETWEEN :startDate AND :endDate) re,\n");

			// Pre-Offer Pending
			query.append("(SELECT COUNT(*) AS preOfferPending FROM t_dgv_candidate_status tdcsh\n");
			query.append("JOIN t_dgv_candidate_basic bas ON bas.candidate_id = tdcsh.candidate_id\n");
			query.append("JOIN t_dgv_organization_master org ON bas.organization_id = org.organization_id\n");
			query.append("JOIN t_dgv_status_master mas ON mas.status_master_id = tdcsh.status_master_id\n");
			query.append("WHERE mas.status_code = 'EPFO'\n");
			query.append("AND bas.created_by in (:agentIds)\n");
			query.append("AND tdcsh.last_updated_on BETWEEN :startDate AND :endDate) ir,\n");

			// Relative Address Pending
			query.append("(SELECT COUNT(*) AS relAddress FROM t_dgv_candidate_status tdcsh\n");
			query.append("JOIN t_dgv_candidate_basic bas ON bas.candidate_id = tdcsh.candidate_id\n");
			query.append("JOIN t_dgv_organization_master org ON bas.organization_id = org.organization_id\n");
			query.append("JOIN t_dgv_status_master mas ON mas.status_master_id = tdcsh.status_master_id\n");
			query.append("WHERE mas.status_code = 'RELATIVEADDRESS'\n");
			query.append("AND bas.created_by in (:agentIds)\n");
			query.append("AND tdcsh.last_updated_on BETWEEN :startDate AND :endDate) fr,\n");

			// Process Decline
			query.append("(SELECT COUNT(*) AS processDecline FROM t_dgv_candidate_status tdcsh\n");
			query.append("JOIN t_dgv_candidate_basic bas ON bas.candidate_id = tdcsh.candidate_id\n");
			query.append("JOIN t_dgv_organization_master org ON bas.organization_id = org.organization_id\n");
			query.append("JOIN t_dgv_status_master mas ON mas.status_master_id = tdcsh.status_master_id\n");
			query.append("WHERE mas.status_code = 'PROCESSDECLINED'\n");
			query.append("AND bas.created_by in (:agentIds)\n");
			query.append("AND tdcsh.last_updated_on BETWEEN :startDate AND :endDate) can,\n");

			// EPFO Skipped
			query.append("(SELECT COUNT(*) AS epfoSkipped FROM t_dgv_candidate_status tdcsh\n");
			query.append("JOIN t_dgv_candidate_basic bas ON bas.candidate_id = tdcsh.candidate_id\n");
			query.append("JOIN t_dgv_organization_master org ON bas.organization_id = org.organization_id\n");
			query.append("JOIN t_dgv_status_master mas ON mas.status_master_id = tdcsh.status_master_id\n");
			query.append("WHERE mas.status_code IN ('DIGILOCKER', 'ITR')\n");
			query.append("AND bas.is_uan_skipped = true\n");
			query.append("AND bas.created_by in (:agentIds)\n");
			query.append("AND tdcsh.last_updated_on BETWEEN :startDate AND :endDate) ep,\n");

			// QC Pending
			query.append("(SELECT COUNT(*) AS qcPending FROM t_dgv_candidate_status tdcsh\n");
			query.append("JOIN t_dgv_candidate_basic bas ON bas.candidate_id = tdcsh.candidate_id\n");
			query.append("JOIN t_dgv_organization_master org ON bas.organization_id = org.organization_id\n");
			query.append("JOIN t_dgv_status_master mas ON mas.status_master_id = tdcsh.status_master_id\n");
			query.append("WHERE mas.status_code = 'PENDINGAPPROVAL'\n");
			query.append("AND bas.created_by in (:agentIds)\n");
			query.append("AND tdcsh.last_updated_on BETWEEN :startDate AND :endDate) qc\n");

			query.append(");");

			System.out.println(query.toString());



			
			Query resultQuery = entityManager.createNativeQuery(query.toString());
			
			
				resultQuery.setParameter("agentIds", agentIds);
				resultQuery.setParameter("startDate", startDate);
				resultQuery.setParameter("endDate", endDate);
			
					
			List<Object[]> activityList = resultQuery.getResultList();
			for(Object[] activity : activityList) {
//				log.info("INVITATIONSENT ::{}",Integer.parseInt(activity[0].toString()));
//				log.info("DIGILOCKER::{}",Integer.parseInt(activity[1].toString()));
//				log.info("ITR::{}",Integer.parseInt(activity[2].toString()));
//				log.info("EPFO::{}",Integer.parseInt(activity[3].toString()));
//				log.info("RELATIVEADDRESS::{}",Integer.parseInt(activity[4].toString()));
//				log.info("PROCESSDECLINED::{}",Integer.parseInt(activity[5].toString()));
//				log.info("EPFOSKIPPED::{}",Integer.parseInt(activity[6].toString()));
//				log.info("PENDINGAPPROVAL::{}",Integer.parseInt(activity[7].toString()));
				candidateStatusCountDtoList.add(0, new CandidateStatusCountDto("LOA Pending","INVITATIONSENT",Integer.parseInt(activity[0].toString())));
				candidateStatusCountDtoList.add(1, new CandidateStatusCountDto("ITR Pending","DIGILOCKER",Integer.parseInt(activity[1].toString())));
				candidateStatusCountDtoList.add(2, new CandidateStatusCountDto("EPFO Pending","ITR",Integer.parseInt(activity[2].toString())));
				candidateStatusCountDtoList.add(3, new CandidateStatusCountDto("PFR Pending","EPFO",Integer.parseInt(activity[3].toString())));
				//candidateStatusCountDtoList.add(4, new CandidateStatusCountDto("Relative Address Completed","RELATIVEADDRESS",Integer.parseInt(activity[4].toString())));
				candidateStatusCountDtoList.add(4, new CandidateStatusCountDto("Process Decline","PROCESSDECLINED",Integer.parseInt(activity[5].toString())));
				candidateStatusCountDtoList.add(5, new CandidateStatusCountDto("EPFO Skipped","EPFOSKIPPED",Integer.parseInt(activity[6].toString())));
				candidateStatusCountDtoList.add(6, new CandidateStatusCountDto("QC Pending","PENDINGAPPROVAL",Integer.parseInt(activity[7].toString())));
			}
			
		}catch(Exception e) {
			log.error("Error Occured in getPendingDetailsCounts::{}",e);		
		}
		return candidateStatusCountDtoList;
	}
	
	@Transactional
	@Override
	public ServiceOutcome<List> bulkUanNew(MultipartFile file) {
		ServiceOutcome<List> svcSearchResult = new ServiceOutcome<List>();
		try {
                 
			if (ExcelUtil.hasExcelFormat(file)) {
				final List<BulkUanDTO> uanSearchData = excelUtil.excelToBulkUanSearch(file.getInputStream());
				ExecutorService executorService = Executors.newFixedThreadPool(1);
			    // Submit the task to run bulkUanEpfoProcess asynchronously
				executorService.submit(() -> bulkUanEpfoProcess(uanSearchData));

			    svcSearchResult.setOutcome(true);
			    svcSearchResult.setMessage("File Uploaded Successfully.");
			    executorService.shutdown();
			}	
				
		} catch (IOException e) {
			log.error("Exception occured in BulkUanExcel method in CandidateServiceImpl-->" + e);
			throw new RuntimeException("fail to store csv/xls data: " + e.getMessage());
		}
		
		return svcSearchResult;
	}
	
	private void bulkUanEpfoProcess(List<BulkUanDTO> bulkUan) {
		
		try {
			String transactionId = null;
			UanSearchData uanSave = null;
			List<UanSearchData> bulkData = new ArrayList<>();
			
 			ResponseEntity<String> epfoTokenResponse = null;
 			HttpHeaders headers = new HttpHeaders();
 			setHeaderDetails(headers);
 			JSONObject request = new JSONObject();

 			request.put(epfoSecurityConfig.getClientIdValue(), epfoSecurityConfig.getClientId());
 			request.put(epfoSecurityConfig.getClientSecretValue(), epfoSecurityConfig.getClientSecret());
 			HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);

 			
 			log.info("epfoSecurityConfig.getAccessTokenUrl() *************************" + epfoSecurityConfig.getAccessTokenUrl());

 			epfoTokenResponse = restTemplate.exchange(epfoSecurityConfig.getAccessTokenUrl(), HttpMethod.POST,
 					entity, String.class);
 			log.info("Response from EPFO TOKEN API " + epfoTokenResponse);
 			String message1 = epfoTokenResponse.getBody(); // .get("message").toString().replaceAll("=", ":")
 			log.info("epfoTokenResponse  ************************* {}",epfoTokenResponse.getBody()); 
 			JSONObject obj1 = new JSONObject(message1);
 			log.info("Response from EPFO TOKEN API - message " + obj1);
 			log.info("last message " + obj1.getJSONObject("message"));
 			JSONObject obj = obj1.getJSONObject("message");
 			String access_token = obj.getString("access_token");
 			log.info("access_token-->  {}",access_token); 


 			//
 			ResponseEntity<String> response = null;
 			headers.setBearerAuth(access_token);
 			headers.add("Bearer", access_token);
 			HttpEntity<String> request1 = new HttpEntity<String>(headers);

 			EpfoDetailsDto epfoDetails = new EpfoDetailsDto();
 			
 			response = restTemplate.exchange(epfoSecurityConfig.getTransactionIdUrl(), HttpMethod.GET, request1,
 					String.class);
 			String message2 = response.getBody();
 			log.info("Response from EPFO Transaction API : " + message2);
 			JSONObject obj2 = new JSONObject(message2);
 			log.info("Response from EPFO Transaction API - obj: " + obj2);
 			transactionId = obj2.getString("message").toString();
 			epfoDetails.setTransactionid(transactionId);
 			log.info("Generated transactionId Id is " + transactionId);
 			log.info("transaction 123 id-->  {}",transactionId); 


			int totalRecordFetchedIncrementCounter = 0;
			int totalRecordFailedIncrementCounter = 0;

			for (BulkUanDTO details : bulkUan) {
				//log.info("{}",details); 
	            String uanNumber = details.getUan();
	            String applicantId = details.getApplicantId();
//	            Date uploadedOn = currentDate;
//	            String uploadedBy = details.getUploadedBy();
	            int totalRecordUploaded = bulkUan.size();
	            String bulkUanId = details.getBulkUanId();

		 		 UanSearchData findByBulkIdAndUanAndApplicantId = uanSearchDataRepository.findByBulkIdAndUanAndApplicantId(applicantId, uanNumber, bulkUanId);
	            
	             log.info("APPLICANT_ID::>>  {}",applicantId); 
					log.info("UANNUMBER::>>  {}",uanNumber); 
					
				
				int maxRetryCount = 2;
				int retryCount = 0;

				try {
					
				    while (retryCount < maxRetryCount) {
				    	
				    	log.info("____________________epfoDetails.getUanusername()  {}",uanNumber); 
			 			request.put(EPFOConstants.EPFO_USR, uanNumber);

			 			request.put(EPFOConstants.EPFO_PWD, ' ');
			 			request.put(EPFOConstants.EPFO_UAN, uanNumber);

			 			HttpEntity<String> entity1 = new HttpEntity<String>(request.toString(), headers);
			 			log.info("\\n------epfoSecurityConfig ------  {}",epfoSecurityConfig.getFinalSubmitPostUrl()); 
						log.info("\\n------epfoDetails ------  {}",epfoDetails.getTransactionid()); 

			 			response = restTemplate.exchange(
			 					epfoSecurityConfig.getFinalSubmitPostUrl() + epfoDetails.getTransactionid(), HttpMethod.POST,
			 					entity1, String.class);
			 			String responseBody = response.getBody();
			 			JSONObject obj3 = new JSONObject(responseBody);
			 			log.info("\\n--------obj --------- {}",obj3); 
			 			
						boolean success = obj3.getBoolean("success");
						//handling empty response message array
						JSONArray messagee = obj3.getBoolean("success")?obj3.getJSONArray("message") : new JSONArray();
						if(obj3.getBoolean("success") && (messagee==null || messagee.isEmpty())){
							obj3.remove("message");
							obj3.put("message", "No data found for this UAN");
							success=false;
						}
						
						String resMsg = obj3.toString();
						log.info("\\n--------resMsg --------- {}",resMsg);
						findByBulkIdAndUanAndApplicantId.setTotalRecordUploaded(totalRecordUploaded);
			 			findByBulkIdAndUanAndApplicantId.setEPFOResponse(resMsg);
						 
				        if (success) {
				        	//log.info("SUCCESS IS TRUE {}");
				        	totalRecordFetchedIncrementCounter++;
				           log.info("INCREMENTVALUE:: in success {}"+totalRecordFetchedIncrementCounter);
				            break; // Success, exit the retry loop
				        } else {
				            // Retry the operation
				            retryCount++;
				            if(retryCount == 2) {
				            	totalRecordFailedIncrementCounter++;
					            log.info("INCREMENTFAIL COUNT::: in success {}"+totalRecordFailedIncrementCounter);
				            }
				            log.info("Retry # {}" + retryCount);
				           
				        }
				    	
				    }

		 			if (retryCount == maxRetryCount) {
		 			    // Maximum retry count reached. Operation failed.
		 			   log.info("Maximum retry count reached. Operation failed. {}");
		 			   findByBulkIdAndUanAndApplicantId.setTotalRecordFailed(totalRecordFailedIncrementCounter);
		 			  for (UanSearchData uanData : bulkData) {
		 				  uanData.setTotalRecordFailed(totalRecordFailedIncrementCounter);
		 				 uanData.setTotalRecordFetched(totalRecordFetchedIncrementCounter);
		 			  }	
		 			   
		 			} else {
		 			    // Operation succeeded
		 			    log.info("Operation Succeeded!! {}");
		 			    findByBulkIdAndUanAndApplicantId.setTotalRecordFetched(totalRecordFetchedIncrementCounter);
			            for (UanSearchData uanData : bulkData) {
			 			    uanData.setTotalRecordFetched(totalRecordFetchedIncrementCounter);
			 			   uanData.setTotalRecordFailed(totalRecordFailedIncrementCounter);
			 			}		
		 			}
		 			
		 			bulkData.add(findByBulkIdAndUanAndApplicantId);
					
				} catch (Exception e) {
					log.info(e.getMessage());
				}
	 			
	 			
	 			
	        }
 			uanSearchDataRepository.saveAll(bulkData);
			
		} catch (Exception e) {
			log.error("Exception occured in BulkUanExcel method in CandidateServiceImpl-->" + e);
		}
		
	}

	@Override
	public ServiceOutcome<Boolean> updateCandidateExperienceInCForm(String candidateCafExperience,
			MultipartFile certificate) {
		ServiceOutcome<Boolean> svcSearchResult=new ServiceOutcome<>();
		try {
			
			CandidateCafExperienceDto candidateCafExperienceDto = new ObjectMapper()
					.readValue(candidateCafExperience, CandidateCafExperienceDto.class);
			if(candidateCafExperienceDto.getInputDateOfJoining()==null || candidateCafExperienceDto.getInputDateOfExit()==null) {
				svcSearchResult.setData(false);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please Select The Joining And Exit Date..!");
				return svcSearchResult;
			}
			
			log.info("START UPDATE CANDIDATE EXPERIENCE FOR ::{}",candidateCafExperienceDto.getCandidateCode());
			Candidate findByCandidateCode = candidateRepository.findByCandidateCode(candidateCafExperienceDto.getCandidateCode());
			
			//checking suspect employee for candidate input company name
			ServiceOutcome<String> suspectResponse = suspectEmpMasterCheck(candidateCafExperienceDto.getCandidateEmployerName(), findByCandidateCode.getOrganization().getOrganizationId());
			String suspectColor="AMBER";
			String suspectMessage="";
			if(Boolean.TRUE.equals(suspectResponse.getOutcome())) {
				suspectColor = suspectResponse.getData();
				suspectMessage =suspectResponse.getMessage();
			}
			
			log.info("SUSPECT EMPLOYER RESULT ::{}",suspectResponse.getData());
			ApprovalStatusRemarkDto approvalStatusRemarkDto=new ApprovalStatusRemarkDto();
			approvalStatusRemarkDto.setId(candidateCafExperienceDto.getCandidateCafExperienceId());
			approvalStatusRemarkDto.setCandidateCode(candidateCafExperienceDto.getCandidateCode());
			approvalStatusRemarkDto.setColorId(colorRepository.findByColorCode(suspectColor).getColorId());
			approvalStatusRemarkDto.setCustomRemark(suspectMessage);
			approvalStatusRemarkDto.setCandidateEmployerName(candidateCafExperienceDto.getCandidateEmployerName());
			approvalStatusRemarkDto.setInputDateOfExit(candidateCafExperienceDto.getInputDateOfExit());
			approvalStatusRemarkDto.setInputDateOfJoining(candidateCafExperienceDto.getInputDateOfJoining());
			approvalStatusRemarkDto.setOrganizationid(findByCandidateCode.getOrganization().getOrganizationId());
			
			log.info("NEW EXPERIENCE ADDED BY CANDIDATE::{}",candidateCafExperienceDto.getCandidateCode()+"::"+approvalStatusRemarkDto);
			svcSearchResult = updateCandidateExperienceStatusAndRemark(approvalStatusRemarkDto);
		} catch (Exception ex) {
			log.error("Exception occured in updateCandidateExperienceInCForm method in CandidateAPPFormController-->", ex);
			svcSearchResult.setData(false);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("");
		}
		return svcSearchResult;
	}
	
	@Transactional
	@Override
	public ServiceOutcome<SuspectEmpMaster> removeAllSuspectEmployerByOrgId(Long orgId) {

		ServiceOutcome<SuspectEmpMaster> svcSearchResult = new ServiceOutcome<>();
		try {
			if(orgId != null) {
				suspectEmpMasterRepository.removeAllSuspectEmpByOrgId(orgId);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("All suspectEmployer Details Deleted Successfully");
			}

		} catch (Exception e) {
			log.info(e.getMessage());
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}

		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<String> getAuthLetterContent(String candidateCode) {
		ServiceOutcome<String> svcSearchResult = new ServiceOutcome<>();
		try {
			Candidate findByCandidateCode = candidateRepository.findByCandidateCode(candidateCode);
			OrganizationEmailTemplate organizationEmailTemplate=organizationEmailTemplateRepository.getByOrganizationId(findByCandidateCode.getOrganization().getOrganizationId());

			if(organizationEmailTemplate!=null && organizationEmailTemplate.getCandidateLoaEmailTemp()!=null) {
				String loaTemplate = organizationEmailTemplate.getCandidateLoaEmailTemp();
				
				svcSearchResult.setData(loaTemplate);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("Retrieved LOA content.");
			}else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("LOA Content Not Present.");
			}
		} catch (Exception e) {
			log.info(e.getMessage());
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}

		return svcSearchResult;
	}

	@Override
	public void postStatusToOrganization(String candidateCode) {
		try {
			Candidate findByCandidateCode = candidateRepository.findByCandidateCode(candidateCode);
			CandidateStatus candidateStatus = candidateStatusRepository.findByCandidateCandidateCode(candidateCode);
			Organization organization=findByCandidateCode.getOrganization();

			if(organization!=null && organization.getCallBackUrl()!=null && !organization.getCallBackUrl().isEmpty()) {
				String orgCallBackUrl = organization.getCallBackUrl();
				log.info("Organization Callback Url for candidate::{}", candidateCode +"::::"+ orgCallBackUrl);
				
				// Format the date
		        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		        String formattedDate = dateFormat.format(new Date());
		        
				HttpHeaders headers = new HttpHeaders();
				setHeaderDetails(headers);
				//request object
				JSONObject requestJson = new JSONObject();
				requestJson.put("candidateCode",candidateCode);
				requestJson.put("status",candidateStatus.getStatusMaster().getStatusCode());
				requestJson.put("submittedOn",formattedDate);
				
				log.info("Request to post status to organization ::{}",requestJson.toString());
				HttpEntity<String> requestEntity = new HttpEntity<>(requestJson.toString(), headers);
				
				//calling callback URL To post the candidate status
				ResponseEntity<String> response =restTemplate.exchange(orgCallBackUrl , HttpMethod.POST, requestEntity, String.class);
				String message = response.getBody();
				log.info("Response after posting the status to organization ::{}",message);
			
			}
		}catch(HttpClientErrorException c) {
			log.info("Exception in Client call To send status for ::{}",candidateCode);
  			log.error("CLIENT Exception occured while posting the candidate status to client::{}",c);
		} catch (Exception e) {
			log.info("Exception To send status for ::{}",candidateCode);
			log.info("Exception in postStatusToOrganization::{}",e.getMessage());
		}

	}

}
