package com.aashdit.digiverifier.config.admin.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.aashdit.digiverifier.config.admin.dto.VendorInitiatDto;
import com.aashdit.digiverifier.config.admin.dto.VendorcheckdashbordtDto;
import com.aashdit.digiverifier.config.candidate.repository.CandidateRepository;
import com.aashdit.digiverifier.config.admin.model.VendorChecks;
import com.aashdit.digiverifier.config.admin.repository.VendorChecksRepository;
import com.aashdit.digiverifier.config.admin.repository.VendorUploadChecksRepository;
import com.aashdit.digiverifier.config.candidate.dto.CandidateStatusCountDto;
import com.aashdit.digiverifier.config.candidate.model.Candidate;
import com.aashdit.digiverifier.config.superadmin.repository.SourceRepository;
import com.aashdit.digiverifier.config.superadmin.model.Source;
import com.aashdit.digiverifier.config.superadmin.repository.VendorMasterNewRepository;
import com.aashdit.digiverifier.config.superadmin.model.VendorMasterNew;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aashdit.digiverifier.config.superadmin.dto.DashboardDto;
import com.aashdit.digiverifier.config.superadmin.model.Color;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.aashdit.digiverifier.config.admin.service.UserService;

import com.aashdit.digiverifier.common.model.ServiceOutcome;
import com.aashdit.digiverifier.config.admin.dto.UserDto;
import com.aashdit.digiverifier.config.admin.dto.VendorCheckStatusAndCountDTO;
import com.aashdit.digiverifier.config.admin.dto.AgentInvitationSentDto;
import com.aashdit.digiverifier.config.admin.dto.SearchAllVendorCheckDTO;
import com.aashdit.digiverifier.config.admin.model.AgentSampleCsvXlsMaster;
import com.aashdit.digiverifier.config.admin.model.ConventionalAttributesMaster;
import com.aashdit.digiverifier.config.admin.model.Role;
import com.aashdit.digiverifier.config.admin.model.VendorUploadChecks;
import com.aashdit.digiverifier.config.admin.model.User;
import com.aashdit.digiverifier.config.admin.repository.AgentSampleCsvXlsMasterRepository;
import com.aashdit.digiverifier.config.admin.repository.ConventionalAttributesMasterRepository;
import com.aashdit.digiverifier.config.admin.repository.RoleRepository;
import com.aashdit.digiverifier.config.admin.repository.UserRepository;
import com.aashdit.digiverifier.config.candidate.util.CSVUtil;
import com.aashdit.digiverifier.config.candidate.repository.CandidateCaseDetailsRepository;
import com.aashdit.digiverifier.config.candidate.model.CandidateCaseDetails;
import com.aashdit.digiverifier.config.candidate.model.CandidateStatus;
import com.aashdit.digiverifier.config.candidate.model.SuspectEmpMaster;
import com.aashdit.digiverifier.config.candidate.model.UanSearchData;
import com.aashdit.digiverifier.config.candidate.util.ExcelUtil;
import com.aashdit.digiverifier.config.superadmin.repository.OrganizationRepository;
import com.aashdit.digiverifier.config.superadmin.repository.ColorRepository;
import com.aashdit.digiverifier.utils.SecurityHelper;
import com.aashdit.digiverifier.utils.ApplicationDateUtils;
import com.aashdit.digiverifier.utils.EmailSentTask;
import com.aashdit.digiverifier.config.superadmin.repository.VendorCheckStatusMasterRepository;
import com.aashdit.digiverifier.config.admin.model.VendorUploadChecks;
import com.aashdit.digiverifier.config.superadmin.model.VendorCheckStatusMaster;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private ColorRepository colorRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private CSVUtil cSVUtil;

	@Autowired
	private ExcelUtil excelUtil;

	@Autowired
	private AgentSampleCsvXlsMasterRepository agentSampleCsvXlsMasterRepository;

	@Autowired
	private CandidateRepository candidateRepository;

	@Autowired
	private VendorMasterNewRepository vendorMasterNewRepository;

	@Autowired
	private VendorChecksRepository vendorChecksRepository;

	@Autowired
	private VendorUploadChecksRepository vendorUploadChecksRepository;

	@Autowired
	private SourceRepository sourceRepository;

	@Autowired
	private CandidateCaseDetailsRepository candidateCaseDetailsRepository;

	@Autowired
	private VendorCheckStatusMasterRepository vendorCheckStatusMasterRepository;

	@Autowired
	private EmailSentTask emailSentTask;

	@Autowired
	private ConventionalAttributesMasterRepository conventionalAttributesMasterRepository;
	
	@Override
    public ServiceOutcome<ConventionalAttributesMaster> saveConventionalAttributesMaster(
            ConventionalAttributesMaster conventionalAttributesMaster) {
        ServiceOutcome<ConventionalAttributesMaster> svcSearchResult = new ServiceOutcome<ConventionalAttributesMaster>();
        try {

 

        	if(!conventionalAttributesMaster.getAgentAttributeList().isEmpty() || !conventionalAttributesMaster.getVendorAttributeList().isEmpty()){

        		System.out.println("Conventional==="+conventionalAttributesMaster);
        		ConventionalAttributesMaster attributesMaster = new ConventionalAttributesMaster();
        		attributesMaster.setCheckId(conventionalAttributesMaster.getCheckId());
        		//                attributesMaster.setCheckName(conventionalAttributesMaster.getCheckName());
        		attributesMaster.setAgentAttributeList(conventionalAttributesMaster.getAgentAttributeList());
        		attributesMaster.setVendorAttributeList(conventionalAttributesMaster.getVendorAttributeList()); 

        		attributesMaster.setSource(conventionalAttributesMaster.getSource());


        		ConventionalAttributesMaster save = conventionalAttributesMasterRepository.save(attributesMaster);
        		svcSearchResult.setMessage("Conventional Attribute Master successfully.");
        		svcSearchResult.setData( attributesMaster);

        	}
        }
        catch (Exception ex) {
        	log.error("Exception occured in saveConventionalAttributesMaster method in userServiceImpl-->"+ex);
        }
        return svcSearchResult;




    }


	@SuppressWarnings("unused")
	@Transactional
	@Override
	public ServiceOutcome<UserDto> saveUser(UserDto user) {
		ServiceOutcome<UserDto> svcSearchResult = new ServiceOutcome<>();
		UserDto userDto = new UserDto();
		try {
			User result = null;
			log.debug("User object is-->" + user);
			if (user.getUserId() != null && !user.getUserId().equals(0l) && user.getUserEmailId() != null) {
//					User findUserEmail = userRepository.findByUserEmailId(user.getUserEmailId());
				User findUserById = userRepository.findByUserId(user.getUserId());
				System.out.println("findUserEmail:::" + findUserById);

				if (findUserById != null) {

					Optional<User> getExistingPassByUserID = userRepository.findById(user.getUserId());
					System.out.println("GETEXISTINGPASSBYUSERID::" + getExistingPassByUserID);

					String passwoString = getExistingPassByUserID.get().getPassword();

					System.out.println("PASSWOSTRING::" + passwoString);

					String userEmailId = getExistingPassByUserID.get().getUserEmailId();
					System.out.println("USEREMAILID::" + userEmailId);

					findUserById.setUserFirstName(user.getUserFirstName());
//						findUserEmail.setUserLastName(user.getUserLastName());
//						findUserEmail.setUserEmailId(user.getUserEmailId());
					findUserById.setUserMobileNum(user.getUserMobileNum());
					findUserById.setLocation(user.getLocation());
					findUserById.setRole(roleRepository.findById(user.getRoleId()).get());

					String password = user.getPassword();
					System.out.println("PASSWORD::" + password);
					if (!password.equals("")) {

						findUserById.setPassword(bCryptPasswordEncoder.encode(password));
						findUserById.setAddlPassword(password);
					} else {
						findUserById.setPassword(passwoString);
					}
					result = userRepository.save(findUserById);

					System.out.println("RESULT INN::" + result);
					
					BeanUtils.copyProperties(result, userDto);

					setSomeUserDataInDTO(userDto, result);

					svcSearchResult.setData(userDto);
					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage("User information Updated successfully");

				}

				else if (findUserById != null && findUserById.getUserId() != user.getUserId()) {

					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult.setMessage("User Email Id already exists.Choose another Email Id");
				}

				else {
					Optional<User> userObj = userRepository.findById(user.getUserId());
					if (userObj.isPresent()) {
						User userObj1 = userObj.get();
						String passwoString = userObj.get().getPassword();
						if (!user.getPassword().equals("")) {
							userObj1.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
							userObj1.setAddlPassword(user.getPassword());
						} else {
							userObj1.setPassword(passwoString);
							userObj1.setAddlPassword(userObj1.getAddlPassword());
						}
						userObj1.setRole(roleRepository.findById(user.getRoleId()).get());
						userObj1.setEmployeeId(user.getEmployeeId());
						userObj1.setUserFirstName(user.getUserFirstName());
						userObj1.setUserLastName(user.getUserLastName());
						userObj1.setUserLandlineNum(user.getUserLandlineNum());
						userObj1.setLocation(user.getLocation());
						userObj1.setUserMobileNum(user.getUserMobileNum());
						userObj1.setUserEmailId(user.getUserEmailId());
						userObj1.setLastUpdatedOn(new Date());
						userObj1.setLastUpdatedBy(SecurityHelper.getCurrentUser());
						userObj1.setIsActive(user.getIsActive() != null ? user.getIsActive() : userObj1.getIsActive());
						if (SecurityHelper.getCurrentUser().getRole().getRoleCode().equals("ROLE_AGENTSUPERVISOR")) {
							userObj1.setAgentSupervisor(SecurityHelper.getCurrentUser());
						} else {
							userObj1.setAgentSupervisor(user.getAgentSupervisorId() != null
									? userRepository.findById(user.getAgentSupervisorId()).get()
									: null);
						}
						result = userRepository.save(userObj1);

						BeanUtils.copyProperties(result, userDto);

						setSomeUserDataInDTO(userDto, result);

						svcSearchResult.setData(userDto);
						svcSearchResult.setOutcome(true);
						svcSearchResult.setMessage("User information Updated successfully");
					}
				}
			} else {
				if (user.getUserEmailId() != null && user.getEmployeeId() != null) {
					User findUserEmail = userRepository.findByUserEmailId(user.getUserEmailId());
					if (findUserEmail != null) {

						svcSearchResult.setData(null);
						svcSearchResult.setOutcome(false);
						svcSearchResult.setMessage("User Email Id exists present.Choose another Email Id");
					} else {
						User userObj = userRepository.findByEmployeeId(user.getEmployeeId());
						if (userObj != null) {
							svcSearchResult.setData(null);
							svcSearchResult.setOutcome(false);
							svcSearchResult.setMessage("EmployeeId already exists.Choose another EmployeeId");
						} else {
							User saveNewUser = new User();

							BeanUtils.copyProperties(user, saveNewUser);

							//saveNewUser.setUserName(user.getEmployeeId());
							saveNewUser.setUserName(user.getUserEmailId().toLowerCase());
							saveNewUser.setAddlPassword(user.getPassword());
							saveNewUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
							saveNewUser.setIsUserBlocked(false);
							saveNewUser
									.setOrganization(organizationRepository.findById(user.getOrganizationId()).get());
							saveNewUser.setRole(roleRepository.findById(user.getRoleId()).get());
							saveNewUser.setIsActive(true);
							saveNewUser.setIsLocked(false);
							saveNewUser.setWrongLoginCount(0);
							saveNewUser.setIsLoggedIn(false);
							saveNewUser.setCreatedOn(new Date());
							if (SecurityHelper.getCurrentUser().getRole().getRoleCode()
									.equals("ROLE_AGENTSUPERVISOR")) {
								saveNewUser.setAgentSupervisor(SecurityHelper.getCurrentUser());
							} else {
								saveNewUser.setAgentSupervisor(user.getAgentSupervisorId() != null
										? userRepository.findById(user.getAgentSupervisorId()).get()
										: null);
							}
							saveNewUser.setCreatedBy(SecurityHelper.getCurrentUser());
							log.debug("User username is-->" + saveNewUser.getUserName());
							result = userRepository.save(saveNewUser);

							BeanUtils.copyProperties(result, userDto);

							setSomeUserDataInDTO(userDto, result);
							svcSearchResult.setData(userDto);
							svcSearchResult.setOutcome(true);
							svcSearchResult.setMessage("User information saved successfully");
						}
					}
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in saveUser method in UserServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes");
		}
		return svcSearchResult;
	}

	private UserDto setSomeUserDataInDTO(UserDto userDto, User result) {
		userDto.setOrganizationId(
				result.getOrganization() != null ? result.getOrganization().getOrganizationId() : null);
		userDto.setRoleId(result.getRole().getRoleId());
		userDto.setRoleName(result.getRole().getRoleName());
		userDto.setCreatedBy(result.getCreatedBy() != null ? result.getCreatedBy().getUserFirstName() : null);
		userDto.setCreatedOn(result.getCreatedOn());
		userDto.setLastUpdatedBy(result.getLastUpdatedBy() != null ? result.getLastUpdatedBy().getUserFirstName() : "");
		userDto.setLastUpdatedOn(result.getLastUpdatedOn() != null ? result.getLastUpdatedOn() : null);
		userDto.setAgentSupervisorId(
				result.getAgentSupervisor() != null ? result.getAgentSupervisor().getUserId() : null);
		return userDto;
	}

	@Override
	public ServiceOutcome<List<UserDto>> getUserByOrganizationIdAndUser(Long organizationId, User user) {
		ServiceOutcome<List<UserDto>> svcSearchResult = new ServiceOutcome<List<UserDto>>();
		List<UserDto> userDtoList = new ArrayList<UserDto>();
		List<User> userList = new ArrayList<User>();
		try {
			if (user.getRole().getRoleCode().equals("ROLE_ADMIN")) {
				userList = userRepository.findAllByOrganizationOrganizationId(organizationId);
				userList = userList.stream().filter(u -> !u.getRole().getRoleCode().equals("ROLE_ADMIN"))
						.collect(Collectors.toList());
			} else if (user.getRole().getRoleCode().equals("ROLE_PARTNERADMIN")) {
				userList = userRepository.findAllByOrganizationOrganizationId(organizationId);
				userList = userList.stream().filter(
						u -> !u.getRole().getRoleCode().equals("ROLE_ADMIN") && u.getUserId() != user.getUserId())
						.collect(Collectors.toList());
			} else {
				userList = userRepository.findAllByOrganizationOrganizationIdAndCreatedByUserId(organizationId,
						user.getUserId());
			}
			for (User userobj : userList) {
				if (userList != null) {
					UserDto userDto = new UserDto();
					BeanUtils.copyProperties(userobj, userDto);
					setSomeUserDataInDTO(userDto, userobj);
					userDtoList.add(userDto);
				}
			}
			if (!userDtoList.isEmpty()) {
				svcSearchResult.setData(userDtoList);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NO USER FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getUserByOrganizationId method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<UserDto> getUserById(Long userId) {
		ServiceOutcome<UserDto> svcSearchResult = new ServiceOutcome<UserDto>();
		try {
			Optional<User> user = userRepository.findById(userId);
			if (user.isPresent()) {

				UserDto userDto = new UserDto();

				BeanUtils.copyProperties(user.get(), userDto);
				setSomeUserDataInDTO(userDto, user.get());
				svcSearchResult.setData(userDto);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NO USER FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getUserById method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<User> activeAndInactiveUserById(Long userId, Boolean isActive) {
		ServiceOutcome<User> svcSearchResult = new ServiceOutcome<>();
		try {
			User result = null;
			if (userId == null || userId.equals(0l)) {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("Please specify User");
			} else {
				Optional<User> userObj = userRepository.findById(userId);
				if (userObj.isPresent()) {
					User user = userObj.get();
					user.setIsActive(isActive);
					user.setIsUserBlocked(!isActive);
					result = userRepository.save(user);
					svcSearchResult.setData(result);
					svcSearchResult.setOutcome(true);
					if (isActive) {
						svcSearchResult.setMessage("User activated successfully.");
					}
					if (!isActive) {
						svcSearchResult.setMessage("User deactivated successfully.");
					}
				} else {
					svcSearchResult.setData(null);
					svcSearchResult.setOutcome(false);
					svcSearchResult.setMessage("No User Found");
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in activeAndInactiveUserById method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<User> findByUsername(String userName) {

		ServiceOutcome<User> svcOutcome = new ServiceOutcome<User>();
		try {
			User user = userRepository.findByUserName(userName);
			svcOutcome.setData(user);
		} catch (Exception ex) {
			log.error("Exception occured in findByUsername method in UserServiceImpl-->" + ex);

			svcOutcome.setData(null);
			svcOutcome.setOutcome(false);
			svcOutcome.setMessage("Error");

		}
		return svcOutcome;
	}

	@Override
	public ServiceOutcome<User> saveUserLoginData(User user) {
		ServiceOutcome<User> svcOutcome = new ServiceOutcome<User>();
		try {
			user = userRepository.saveAndFlush(user);
			svcOutcome.setData(user);
		} catch (Exception ex) {
			log.error("Exception occured in save method in UserServiceImpl-->" + ex);

			svcOutcome.setData(null);
			svcOutcome.setOutcome(false);
			svcOutcome.setMessage("Error");
		}

		return svcOutcome;
	}

	@Override
	public ServiceOutcome<User> getAdminDetailsForOrganization(Long organizationId) {
		ServiceOutcome<User> svcOutcome = new ServiceOutcome<User>();
		try {
			Role role = roleRepository.findRoleByRoleCode("ROLE_ADMIN");
			User user = userRepository.findByOrganizationOrganizationIdAndRoleRoleIdAndIsActiveTrue(organizationId,
					role.getRoleId());
			if (user != null) {
				svcOutcome.setData(user);
				svcOutcome.setOutcome(true);
				svcOutcome.setMessage("SUCCESS");
			} else {
				svcOutcome.setData(null);
				svcOutcome.setOutcome(false);
				svcOutcome.setMessage("ADMIN NOT FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getAdminDetailsForOrganization Method-->" + ex);
			svcOutcome.setData(null);
			svcOutcome.setOutcome(false);
			svcOutcome.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcOutcome;
	}

	@Override
	public ServiceOutcome<List<User>> getAgentSupervisorList(Long organizationId) {
		ServiceOutcome<List<User>> svcOutcome = new ServiceOutcome<List<User>>();
		try {
			Role role = roleRepository.findRoleByRoleCode("ROLE_AGENTSUPERVISOR");
			if (role != null) {
				List<User> userList = userRepository.findAllByOrganizationOrganizationIdAndRoleRoleIdAndIsActiveTrue(
						organizationId, role.getRoleId());
				if (!userList.isEmpty()) {
					svcOutcome.setData(userList);
					svcOutcome.setOutcome(true);
					svcOutcome.setMessage("SUCCESS");
				} else {
					svcOutcome.setData(null);
					svcOutcome.setOutcome(false);
					svcOutcome.setMessage("No Agent Supervisor found for this Organization");
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in getAgentSupervisorList Method-->" + ex);
			svcOutcome.setData(null);
			svcOutcome.setOutcome(false);
			svcOutcome.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcOutcome;
	}

	@Transactional
	@Override
	public ServiceOutcome<Boolean> saveAgentInformation(MultipartFile file) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		AgentSampleCsvXlsMaster agentSampleCsvXlsMaster = null;
		try {
			System.out.println("inside service");
			User user = SecurityHelper.getCurrentUser();
			List<User> users = null;
			if (CSVUtil.hasCSVFormat(file)) {
				users = cSVUtil.csvToUserList(file.getInputStream());
				agentSampleCsvXlsMaster = new AgentSampleCsvXlsMaster();
				agentSampleCsvXlsMaster.setAgentSampleCsv(file.getBytes());
			}
			if (ExcelUtil.hasExcelFormat(file)) {
				users = excelUtil.excelToUserList(file.getInputStream());
				agentSampleCsvXlsMaster = new AgentSampleCsvXlsMaster();
				agentSampleCsvXlsMaster.setAgentSampleXls(file.getBytes());
			}
			List<String> employeeIdList = new ArrayList<String>();

			for (User userObj : users) {
				System.out.println("inside for");

				userObj.setUserName(userObj.getUserName());
				employeeIdList.add(userObj.getEmployeeId());
				userObj.setOrganization(
						organizationRepository.findById(user.getOrganization().getOrganizationId()).get());
				userObj.setCreatedOn(new Date());
				userObj.setCreatedBy(user);
				userObj.setAddlPassword("123456");
				userObj.setPassword(bCryptPasswordEncoder.encode("123456"));
				userObj.setIsUserBlocked(false);
				userObj.setIsActive(true);
				userObj.setIsLocked(false);
				userObj.setWrongLoginCount(0);
				userObj.setIsLoggedIn(false);
				userObj.setRole(roleRepository.findRoleByRoleCode("ROLE_AGENTHR"));

			}
			List<User> userList = userRepository.saveAllAndFlush(users);

			if (!userList.isEmpty()) {
				AgentInvitationSentDto agentInvitationSentDto = new AgentInvitationSentDto();
				agentInvitationSentDto.setEmployeeId(employeeIdList);
				System.out.println(employeeIdList + "referenceList");
				ServiceOutcome<Boolean> svcOutcome = userService.invitationSent(agentInvitationSentDto);

				agentSampleCsvXlsMaster.setOrganization(
						organizationRepository.findById(user.getOrganization().getOrganizationId()).get());
				agentSampleCsvXlsMaster.setUploadedTimestamp(new Date());
				agentSampleCsvXlsMaster.setCreatedBy(user);
				agentSampleCsvXlsMaster.setCreatedOn(new Date());
				agentSampleCsvXlsMasterRepository.save(agentSampleCsvXlsMaster);

				svcSearchResult.setData(true);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("Agents uploaded successfully.");
			} else {
				svcSearchResult.setData(false);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage(file.getOriginalFilename() + " could not be uploaded.");
			}
		} catch (IOException e) {
			svcSearchResult.setData(false);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Unable to upload agent details.");
			log.error("Exception occured in saveAgentInformation method in UserServiceImpl-->" + e);
			throw new RuntimeException("fail to store csv/xls data: " + e.getMessage());
		}
		return svcSearchResult;
	}

	@Transactional
	@Override
	public ServiceOutcome<Boolean> invitationSent(AgentInvitationSentDto agentInvitationSentDto) {
		ServiceOutcome<Boolean> svcSearchResult = new ServiceOutcome<Boolean>();
		try {
			System.out.println(agentInvitationSentDto + "agentInvitationSentDto");
			for (int i = 0; i < agentInvitationSentDto.getEmployeeId().size(); i++) {
				User users = userRepository.findByEmployeeId(agentInvitationSentDto.getEmployeeId().get(i));
				if (users != null) {
					Boolean result = emailSentTask.sendAgentEmail(users.getEmployeeId(), users.getUserFirstName(),
							users.getUserEmailId(), users.getReportingEmailId());
					System.out.println(agentInvitationSentDto + "agentInvi");
				}
			}

		} catch (Exception ex) {
			log.error("Exception occured in invitationSent method in CandidateServiceImpl-->", ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			// svcSearchResult.setMessage(messageSource.getMessage("ERROR.MESSAGE", null,
			// LocaleContextHolder.getLocale()));
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<UserDto> getUserProfile() {
		ServiceOutcome<UserDto> svcSearchResult = new ServiceOutcome<UserDto>();
		try {
			User user = SecurityHelper.getCurrentUser();
			Optional<User> userObj = userRepository.findById(user.getUserId());
			if (userObj.isPresent()) {
				UserDto userDto = new UserDto();
				BeanUtils.copyProperties(userObj.get(), userDto);
				setSomeUserDataInDTO(userDto, userObj.get());
				svcSearchResult.setData(userDto);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NO USER FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getUserProfile method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<User>> getAdminList() {
		ServiceOutcome<List<User>> svcSearchResult = new ServiceOutcome<List<User>>();
		try {
			List<User> adminUserList = userRepository.findByRoleRoleCode("ROLE_ADMIN");
			svcSearchResult.setData(adminUserList);
			svcSearchResult.setOutcome(true);
			svcSearchResult.setMessage("SUCCESS");
		} catch (Exception ex) {
			log.error("Exception occured in getAdminList method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<User> activeNInAtiveAdmin(Long userId, Boolean isActive) {
		ServiceOutcome<User> svcSearchResult = new ServiceOutcome<User>();
		try {
			Optional<User> userObj = userRepository.findById(userId);
			if (userObj.isPresent()) {
				User user = userObj.get();
				Role role = roleRepository.findRoleByRoleCode("ROLE_ADMIN");
				User userActive = userRepository.findByOrganizationOrganizationIdAndRoleRoleIdAndIsActiveTrue(
						user.getOrganization().getOrganizationId(), role.getRoleId());
				if (userActive != null) {
					svcSearchResult.setData(user);
					svcSearchResult.setOutcome(true);
					svcSearchResult.setMessage(
							"Only one admin can be active in one time. Please deactivate one before continuing.");
				} else {
					user.setIsActive(isActive);
					user.setIsUserBlocked(!isActive);
					user = userRepository.save(user);
					svcSearchResult.setData(user);
					svcSearchResult.setOutcome(true);
					if (isActive) {
						svcSearchResult.setMessage("Admin activated successfully.");
					}
					if (!isActive) {
						svcSearchResult.setMessage("Admin deactivated successfully.");
					}
				}

			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("No User Found");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getUserProfile method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<User>> getAgentList(Long organizationId) {
		ServiceOutcome<List<User>> svcOutcome = new ServiceOutcome<List<User>>();
		try {
			User user = SecurityHelper.getCurrentUser();
			List<User> userList = new ArrayList<User>();
			Role role = roleRepository.findRoleByRoleCode("ROLE_AGENTHR");
			if (role != null) {
				if (organizationId != 0) {
					if (user.getRole().getRoleCode().equals("ROLE_AGENTSUPERVISOR")) {
						userList = userRepository.findAllByAgentSupervisorUserIdAndRoleRoleIdAndIsActiveTrue(
								user.getUserId(), role.getRoleId());
					} else {
						userList = userRepository.findAllByOrganizationOrganizationIdAndRoleRoleIdAndIsActiveTrue(
								organizationId, role.getRoleId());
					}
				} else {
					userList = userRepository.findAllByRoleRoleIdAndIsActiveTrue(role.getRoleId());
				}
				if (!userList.isEmpty()) {
					svcOutcome.setData(userList);
					svcOutcome.setOutcome(true);
					svcOutcome.setMessage("SUCCESS");
				} else {
					svcOutcome.setData(null);
					svcOutcome.setOutcome(false);
					svcOutcome.setMessage("No Agent found for this Organization");
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in getAgentList Method-->", ex);
			svcOutcome.setData(null);
			svcOutcome.setOutcome(false);
			svcOutcome.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcOutcome;
	}

	@Override
	public ServiceOutcome<User> getUserByUserId(Long userId) {
		ServiceOutcome<User> svcSearchResult = new ServiceOutcome<User>();
		try {
			Optional<User> user = userRepository.findById(userId);
			if (user.isPresent()) {

				svcSearchResult.setData(user.get());
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NO USER FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getUserById method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<User>> getUsersByRoleCode(String roleCode) {
		roleCode = roleCode.replaceAll("\"", "");
		ServiceOutcome<List<User>> svcSearchResult = new ServiceOutcome<List<User>>();
		List<User> userList = new ArrayList<User>();
		try {
			if (roleCode.equals("ROLE_ADMIN")) {
				User user = SecurityHelper.getCurrentUser();
				userList = userRepository
						.findAllByOrganizationOrganizationId(user.getOrganization().getOrganizationId());
				userList = userList.stream().filter(u -> !u.getRole().getRoleCode().equals("ROLE_ADMIN"))
						.collect(Collectors.toList());
			} else if (roleCode.equals("ROLE_PARTNERADMIN")) {
				User user = SecurityHelper.getCurrentUser();
				userList = userRepository
						.findAllByOrganizationOrganizationId(user.getOrganization().getOrganizationId());
				userList = userList.stream().filter(
						u -> !u.getRole().getRoleCode().equals("ROLE_ADMIN") && u.getUserId() != user.getUserId())
						.collect(Collectors.toList());
			} else if (roleCode.equals("ROLE_AGENTSUPERVISOR")) {
				User user = SecurityHelper.getCurrentUser();
				userList = userRepository.findAllByOrganizationOrganizationIdAndCreatedByUserId(
						user.getOrganization().getOrganizationId(), user.getUserId());
			} else {
				userList = userRepository.findByIsActiveTrue();
			}
			if (!userList.isEmpty()) {
				svcSearchResult.setData(userList);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NO USERS FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getUsersByRoleCode method in UserServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Transactional
	@Override
	public void logoutUserAfter5Mins() {
		try {
			userRepository.logoutUserAfter5Mins();
		} catch (Exception ex) {
			log.error("Exception occured in logoutUserAfter5Mins method in UserServiceImpl-->", ex);
		}

	}

	@Override
	public ServiceOutcome<List<User>> getVendorList(Long organizationId) {
		ServiceOutcome<List<User>> svcOutcome = new ServiceOutcome<List<User>>();
		try {
			User user = SecurityHelper.getCurrentUser();
			List<User> userList = new ArrayList<User>();
			Role role = roleRepository.findRoleByRoleCode("ROLE_VENDOR");
			if (role != null) {
				if (organizationId != 0) {
					if (user.getRole().getRoleCode().equals("ROLE_AGENTSUPERVISOR")) {
						userList = userRepository.findAllByAgentSupervisorUserIdAndRoleRoleIdAndIsActiveTrue(
								user.getUserId(), role.getRoleId());
					} else {
						userList = userRepository.findAllByOrganizationOrganizationIdAndRoleRoleIdAndIsActiveTrue(
								organizationId, role.getRoleId());
					}
				} else {
					userList = userRepository.findAllByRoleRoleIdAndIsActiveTrue(role.getRoleId());
				}
				if (!userList.isEmpty()) {
					svcOutcome.setData(userList);
					svcOutcome.setOutcome(true);
					svcOutcome.setMessage("SUCCESS");
				} else {
					svcOutcome.setData(null);
					svcOutcome.setOutcome(false);
					svcOutcome.setMessage("No Vendor found for this Organization");
				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in getAgentList Method-->", ex);
			svcOutcome.setData(null);
			svcOutcome.setOutcome(false);
			svcOutcome.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcOutcome;
	}

	@Override
	public ServiceOutcome<List<VendorChecks>> getVendorCheckDetails(Long candidateId) {
		ServiceOutcome<List<VendorChecks>> svcSearchResult = new ServiceOutcome<List<VendorChecks>>();
		try {
			System.out.println(candidateId);
			// List<VendorChecks> vendorList= vendorChecksRepository.findAll();

			List<VendorChecks> vendorList = vendorChecksRepository.findAllByCandidateCandidateId(candidateId);
			if (!vendorList.isEmpty()) {
				svcSearchResult.setData(vendorList);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NO VENDORCHECKS FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getVendorCheckDetails method in userServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<VendorChecks>> getallVendorCheckDetails(Long vendorId,Map<String, String> dateSearchFilter,int pageNumber, int pageSize, String vendorCheckDashboardStatusCode) {
		ServiceOutcome<List<VendorChecks>> svcSearchResult = new ServiceOutcome<List<VendorChecks>>();
		try {
			System.out.println(vendorId + "5666666666666666666");
			String fromDate = dateSearchFilter.get("fromDate");
			String toDate = dateSearchFilter.get("toDate");
            
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
			
			Pageable pageable = PageRequest.of(pageNumber, pageSize);
			
			VendorCheckStatusMaster findByCheckStatusCode = vendorCheckStatusMasterRepository.findByCheckStatusCode(vendorCheckDashboardStatusCode);
			Long vendorCheckStatusMasterId = findByCheckStatusCode.getVendorCheckStatusMasterId();
			
//			System.out.println("VENDORCHECKSTATUSMASTERID =================== :::::::::"+vendorCheckStatusMasterId);
			
			Page<VendorChecks> vendorList = vendorChecksRepository.findAllByVendorId(vendorId,fromDateFormat,toDateFormat,vendorCheckStatusMasterId,pageable);
			
			// Retrieve the list of items on the current page
			List<VendorChecks> vendorCheckList = vendorList.getContent();
			
			int currentPageNumber = vendorList.getNumber();  
			int totalPages = vendorList.getTotalPages();
			String totalPagesString = String.valueOf(totalPages);
			long totalElements = vendorList.getTotalElements();
			boolean hasNextPage = vendorList.hasNext(); 
			boolean hasPreviousPage = vendorList.hasPrevious();

			if (!vendorList.isEmpty()) {
				List<VendorChecks> newList = new ArrayList<VendorChecks>();
				newList.addAll(vendorCheckList);
				svcSearchResult.setData(newList);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
				svcSearchResult.setStatus(totalPagesString);
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NO VENDORCHECKS FOUND");
			}
		} catch (Exception ex) {
			log.error("Exception occured in getVendorCheckDetails method in userServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

//	@Override
//	public ServiceOutcome<VendorChecks> saveproofuploadVendorChecks(String vendorChecksString,MultipartFile proofDocumentNew) {
//		System.out.println(proofDocumentNew+"==========================="+vendorChecksString);
//		ServiceOutcome<VendorChecks> svcSearchResult = new ServiceOutcome<VendorChecks>();
//		VendorUploadChecks result=null;
//		// VendorCheckStatusMaster vendorCheckStatusMaster =null;
//		
//		try {
//			
//			VendorcheckdashbordtDto vendorcheckdashbordtDto  = new ObjectMapper().readValue(vendorChecksString, VendorcheckdashbordtDto.class);
//			System.out.println(vendorcheckdashbordtDto+"------------------------");
//			VendorChecks vendorCheckss= vendorChecksRepository.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
//			System.out.println(vendorCheckss.getVendorcheckId()+"------------------ert------");
//			VendorUploadChecks vendorChecks= vendorUploadChecksRepository.findByVendorChecksVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
//			User user = SecurityHelper.getCurrentUser();
//			
//			if(vendorChecks == null ){
//				VendorUploadChecks vendorUploadChecks=new VendorUploadChecks();
//				System.out.println("-------------create------");
//				vendorUploadChecks.setVendorUploadedDocument(proofDocumentNew!=null?proofDocumentNew.getBytes():null);
//				vendorUploadChecks.setAgentColor(colorRepository.findById(vendorcheckdashbordtDto.getColorid()).get());
//				vendorUploadChecks.setCreatedOn(new Date());
//				vendorUploadChecks.setCreatedBy(user);
//				vendorUploadChecks.setVendorChecks(vendorCheckss);
//				vendorUploadChecks.setDocumentname(vendorcheckdashbordtDto.getDocumentname());
//				
//				
//				
//				// vendorCheckStatusMaster=vendorCheckStatusMasterRepository.findById(vendorcheckdashbordtDto.getVendorCheckStatusMasterId());
//				// vendorUploadChecks.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findById(vendorcheckdashbordtDto.getVendorCheckStatusMasterId()).get());
//				// vendorUploadChecks.setVendorCheckStatusId(vendorcheckdashbordtDto.getVendorCheckStatusMasterId());
//				System.out.println("-------------------==========getVendorCheckStatusMasterId");
//				result=vendorUploadChecksRepository.save(vendorUploadChecks);
//				if(result!=null) {
//					System.out.println("-------------------==========getVendorCheckStatusMasterId");
//					System.out.println("candidate");
//					VendorChecks vendorChecksnew= vendorChecksRepository.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
//					vendorChecksnew.setIsproofuploaded(true);
//					vendorChecksnew.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findById(vendorcheckdashbordtDto.getVendorCheckStatusMasterId()).get());
//					vendorChecksRepository.save(vendorChecksnew);
//					svcSearchResult.setMessage("vendorchecks document saved successfully.");
//			
//				}else {
//					System.out.println("-------------candidate-----else------");
//					svcSearchResult.setData(null);
//					svcSearchResult.setOutcome(false);
//					// svcSearchResult.setMessage(messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
//				}
//				
//				}
//			else{
//				System.out.println("-------------update------");
//				vendorChecks.setVendorUploadedDocument(proofDocumentNew!=null?proofDocumentNew.getBytes():null);
//				vendorChecks.setAgentColor(colorRepository.findById(vendorcheckdashbordtDto.getColorid()).get());
//				vendorChecks.setCreatedOn(new Date());
//				vendorChecks.setCreatedBy(user);
//				vendorChecks.setDocumentname(vendorcheckdashbordtDto.getDocumentname());
//				// vendorChecks.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findById(vendorcheckdashbordtDto.getVendorCheckStatusMasterId()).get());
//				result=vendorUploadChecksRepository.save(vendorChecks);
//			
//				if(result!=null) {
//				
//					System.out.println("candidate");
//					VendorChecks vendorChecksnew= vendorChecksRepository.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
//					vendorChecksnew.setIsproofuploaded(true);
//					vendorChecksnew.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findById(vendorcheckdashbordtDto.getVendorCheckStatusMasterId()).get());
//					vendorChecksRepository.save(vendorChecksnew);
//					svcSearchResult.setMessage("vendorchecks document update successfully.");
//			
//				}else {
//					System.out.println("-------------candidate-----else------");
//					svcSearchResult.setData(null);
//					svcSearchResult.setOutcome(false);
//					// svcSearchResult.setMessage(messageSource.getMessage("msg.error", null, LocaleContextHolder.getLocale()));
//				}
//
//			}
//			
//			
//		}
//		catch(Exception ex)
//		{
//			log.error("Exception occured in saveproofuploadVendorChecks method in userServiceImpl-->"+ex);
//			
//		}
//		return svcSearchResult;
//	}

	@Override
	public ServiceOutcome<VendorChecks> saveproofuploadVendorChecks(String vendorChecksString,
			String vendorAttributesValue, MultipartFile proofDocumentNew) {
		System.out.println(proofDocumentNew + "===========================" + vendorChecksString);
		ServiceOutcome<VendorChecks> svcSearchResult = new ServiceOutcome<VendorChecks>();
		VendorUploadChecks result = null;
		// VendorCheckStatusMaster vendorCheckStatusMaster =null;

		try {
			System.out.println("VENDOR_ATTRIBUTE_VALUE++++++" + vendorAttributesValue);

			VendorcheckdashbordtDto vendorcheckdashbordtDto = new ObjectMapper().readValue(vendorChecksString,
					VendorcheckdashbordtDto.class);
			System.out.println(vendorcheckdashbordtDto + "------------------------");
			if (vendorcheckdashbordtDto.getVendorcheckId() != null) {
				VendorChecks vendorCheckss = vendorChecksRepository
						.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
				System.out.println(vendorCheckss.getVendorcheckId() + "------------------ert------");
				VendorCheckStatusMaster vendorCheckStatusMaster = vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(vendorcheckdashbordtDto.getVendorCheckStatusMasterId());
				vendorCheckss.setVendorCheckStatusMaster(vendorCheckStatusMaster);
				VendorChecks saveObj = vendorChecksRepository.save(vendorCheckss);
				
				VendorUploadChecks vendorChecks = vendorUploadChecksRepository
						.findByVendorChecksVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());

				System.out.println("vendorChecks=====>" + vendorChecks);
				User user = SecurityHelper.getCurrentUser();

				if (vendorChecks == null) {
//                 
					VendorUploadChecks vendorUploadChecks = new VendorUploadChecks();
					System.out.println("-------------create------");
					if (proofDocumentNew != null) {
						byte[] vendorProof = proofDocumentNew.getBytes();
						if (vendorProof != null) {
							vendorUploadChecks.setVendorUploadedDocument(vendorProof);

							// NEW CHANGE FOR CONVERTING PDF TO IMAGE THIS IS NEW PROOF START
							List<byte[]> imageBytes = convertPDFToImage(vendorProof);
							List<Map<String, List<String>>> encodedImageMapsList = new ArrayList<>();

							if (imageBytes != null && !imageBytes.isEmpty()) {
								for (int j = 0; j < imageBytes.size(); j++) {
									byte[] imageBytess = imageBytes.get(j);
									String encodedImage = Base64.getEncoder().encodeToString(imageBytess);
									String key = "image" + (j + 1);
									log.info("Encoded image {} added to list.", key);

									// Create a new list for each image
									List<String> encodedImagesForDocument = new ArrayList<>();
									encodedImagesForDocument.add(encodedImage);

									// Create a new map for each image
									Map<String, List<String>> encodedImageMap = new HashMap<>();
									encodedImageMap.put(key, encodedImagesForDocument);

									// Add the map to the list
									encodedImageMapsList.add(encodedImageMap);
								}

								log.info("encodedImagesForDocument size: {}", encodedImageMapsList.size());

								// Convert the list to a JSON string
								try {
									ObjectMapper objectMapper1 = new ObjectMapper();
									String jsonEncodedImageMapsList = objectMapper1.writeValueAsString(encodedImageMapsList);
									// Set the JSON string to the entity field
									vendorUploadChecks.setVendorUploadedImage(jsonEncodedImageMapsList);

								} catch (JsonProcessingException e) {
									log.error("Exception occured in saveproofuploadVendorChecks method in userServiceImpl-->" + e);
								}
							}

							// NEW CHANGE FOR CONVERTING PDF TO IMAGE END 

						}
					}
					vendorUploadChecks
							.setVendorUploadedDocument(proofDocumentNew != null ? proofDocumentNew.getBytes() : null);
					vendorUploadChecks
							.setAgentColor(colorRepository.findById(vendorcheckdashbordtDto.getColorid()).get());
					vendorUploadChecks.setCreatedOn(new Date());
					vendorUploadChecks.setCreatedBy(user);
					vendorUploadChecks.setVendorChecks(saveObj);
					vendorUploadChecks.setDocumentname(vendorcheckdashbordtDto.getDocumentname());
//                 
//					System.out.println("VENDORUPLOADCHECKS:::" + vendorUploadChecks);
					ObjectMapper objectMapper = new ObjectMapper();
					Map<String, String> venderAttributeMap = objectMapper.readValue(vendorAttributesValue,
							new TypeReference<Map<String, String>>() {
							});

					System.out.println("VendorAttributeMap:::>>>>>>>>" + venderAttributeMap);
					// Convert the map to an ArrayList of concatenated key-value strings
					ArrayList<String> venderAttributeList = new ArrayList<>();
					for (Map.Entry<String, String> entry : venderAttributeMap.entrySet()) {
						String concatenated = entry.getKey() + "=" + entry.getValue();

						venderAttributeList.add(concatenated);
					}
					System.out.println("agentAttributeList@@@@@@@@@@@@@@@@@>" + venderAttributeList);
//                 Set the agentAttirbuteValue field in vendorChecks
					vendorUploadChecks.setVendorAttirbuteValue(venderAttributeList);

					svcSearchResult.setMessage("Vendor checks document updated successfully.");
					// vendorCheckStatusMaster=vendorCheckStatusMasterRepository.findById(vendorcheckdashbordtDto.getVendorCheckStatusMasterId());
					// vendorUploadChecks.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findById(vendorcheckdashbordtDto.getVendorCheckStatusMasterId()).get());
					// vendorUploadChecks.setVendorCheckStatusId(vendorcheckdashbordtDto.getVendorCheckStatusMasterId());
					System.out.println("-------------------==========getVendorCheckStatusMasterId");
					result = vendorUploadChecksRepository.save(vendorUploadChecks);
					if (result != null) {
						System.out.println("-------------------==========getVendorCheckStatusMasterId");
						System.out.println("candidate");
						VendorChecks vendorChecksnew = vendorChecksRepository
								.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
						vendorChecksnew.setIsproofuploaded(true);
//                    vendorChecksnew.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findById(vendorcheckdashbordtDto.getVendorCheckStatusMasterId()).orElse(null));
						vendorChecksRepository.save(vendorChecksnew);
						svcSearchResult.setMessage("vendorchecks document saved successfully.");

					} else {
						System.out.println("-------------candidate-----else------");
						svcSearchResult.setData(null);
						svcSearchResult.setOutcome(false);
						// svcSearchResult.setMessage(messageSource.getMessage("msg.error", null,
						// LocaleContextHolder.getLocale()));
					}

				} else {
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode vendorChecksNode = objectMapper.readTree(vendorChecksString);

					System.out.println("-------------update------");
					
	                // NEW CHANGE FOR CONVERT PDF TO IMAGE THIS FOR UPDATE PROOF START
	                
	                if (proofDocumentNew != null) {
	                    byte[] vendorProof = proofDocumentNew.getBytes();
	                    if (vendorProof != null) {
	                    	List<byte[]> imageBytes = convertPDFToImage(vendorProof);
	                    	List<Map<String, List<String>>> encodedImageMapsList = new ArrayList<>();

	                    	if (imageBytes != null && !imageBytes.isEmpty()) {
	                    	    for (int j = 0; j < imageBytes.size(); j++) {
	                    	        byte[] imageBytess = imageBytes.get(j);
	                    	        String encodedImage = Base64.getEncoder().encodeToString(imageBytess);
	                    	        String key = "image" + (j + 1);
	                    	        log.info("Encoded image {} added to list.", key);

	                    	        // Create a new list for each image
	                    	        List<String> encodedImagesForDocument = new ArrayList<>();
	                    	        encodedImagesForDocument.add(encodedImage);

	                    	        // Create a new map for each image
	                    	        Map<String, List<String>> encodedImageMap = new HashMap<>();
	                    	        encodedImageMap.put(key, encodedImagesForDocument);

	                    	        // Add the map to the list
	                    	        encodedImageMapsList.add(encodedImageMap);
	                    	    }

	                    	    log.info("encodedImagesForDocument size: {}", encodedImageMapsList.size());

	                    	    // Convert the list to a JSON string
	                    	    try {
	                    	        ObjectMapper objectMapper1 = new ObjectMapper();
	                    	        String jsonEncodedImageMapsList = objectMapper1.writeValueAsString(encodedImageMapsList);
	                    	        // Set the JSON string to the entity field
	                    	        vendorChecks.setVendorUploadedImage(jsonEncodedImageMapsList);  	            
	                    	    } catch (JsonProcessingException e) {
	                    	        // Handle the exception (e.g., log or throw)
	                    			log.error("Exception occured in saveproofuploadVendorChecks method in userServiceImpl-->" + e);
	                    	    }
	                    	}

	                    }
	                
	                }
					 
	                // NEW CHANGE FOR CONVERT PDF TO IMAGE THIS FOR UPDATE PROOF END
	              
					
					vendorChecks
							.setVendorUploadedDocument(proofDocumentNew != null ? proofDocumentNew.getBytes() : null);
					vendorChecks.setAgentColor(colorRepository.findById(vendorcheckdashbordtDto.getColorid()).get());
					vendorChecks.setCreatedOn(new Date());
					vendorChecks.setCreatedBy(user);
					vendorChecks.setDocumentname(vendorcheckdashbordtDto.getDocumentname());
					vendorChecks.setVendorChecks(saveObj);
//                 
					ObjectMapper objectMapper = new ObjectMapper();
					Map<String, String> venderAttributeMap = objectMapper.readValue(vendorAttributesValue,
							new TypeReference<Map<String, String>>() {
							});

					// Convert the map to an ArrayList of concatenated key-value strings
					ArrayList<String> venderAttributeList = new ArrayList<>();
					for (Map.Entry<String, String> entry : venderAttributeMap.entrySet()) {
						String concatenated = entry.getKey() + "=" + entry.getValue();

						venderAttributeList.add(concatenated);
					}
					System.out.println("agentAttributeList@@@@@@@@@@@@@@@@@>" + venderAttributeList);
//                 Set the vendorAttirbuteValue field in vendorChecks
					vendorChecks.setVendorAttirbuteValue(venderAttributeList);
					svcSearchResult.setMessage("Vendor checks document updated successfully.");
					// vendorChecks.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findById(vendorcheckdashbordtDto.getVendorCheckStatusMasterId()).get());
					result = vendorUploadChecksRepository.save(vendorChecks);

					if (result != null) {

						System.out.println("candidate");
						VendorChecks vendorChecksnew = vendorChecksRepository
								.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
						vendorChecksnew.setIsproofuploaded(true);
//                    vendorChecksnew.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findById(vendorcheckdashbordtDto.getVendorCheckStatusMasterId()).get());
						vendorChecksRepository.save(vendorChecksnew);
						svcSearchResult.setMessage("vendorchecks document update successfully.");

					} else {
						System.out.println("-------------candidate-----else------");
						svcSearchResult.setData(null);
						svcSearchResult.setOutcome(false);
						// svcSearchResult.setMessage(messageSource.getMessage("msg.error", null,
						// LocaleContextHolder.getLocale()));
					}

				}

			}
		} catch (Exception ex) {
			log.error("Exception occured in saveproofuploadVendorChecks method in userServiceImpl-->" + ex);

		}
		return svcSearchResult;
	}
	
	 public List<byte[]> convertPDFToImage(byte[] pdfBytes) throws IOException {
		    try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
		        PDFRenderer pdfRenderer = new PDFRenderer(document);
		        int numberOfPages = document.getNumberOfPages();

		        List<byte[]> imageBytesList = new ArrayList<>();

		        for (int pageIndex = 0; pageIndex < numberOfPages; pageIndex++) {
		            BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);
		            ByteArrayOutputStream baos = new ByteArrayOutputStream();
		            javax.imageio.ImageIO.write(image, "png", baos);
		            imageBytesList.add(baos.toByteArray());
		        }

		        log.info("Number of Images: {}" + imageBytesList.size());
		        // If needed, you can return the list of image bytes
		        return imageBytesList;
		    }
		}

	@Override
	public ServiceOutcome<VendorChecks> saveInitiateVendorChecks(String vendorChecksString,
			MultipartFile proofDocumentNew) {
		System.out.println(proofDocumentNew + "===========================" + vendorChecksString);
		ServiceOutcome<VendorChecks> svcSearchResult = new ServiceOutcome<VendorChecks>();
		Candidate Candidatelist = null;
//		VendorChecks vendorcheckObj=null;
		// Long sourceid=2;
		Long vendorid = null;
		CandidateCaseDetails result = null;

		try {
			VendorInitiatDto vendorInitiatDto = new ObjectMapper().readValue(vendorChecksString,
					VendorInitiatDto.class);
			System.out.println(vendorInitiatDto + "------------------------+++++++++++++++");
			User user = SecurityHelper.getCurrentUser();
			Source source = sourceRepository.findById(vendorInitiatDto.getSourceId()).get();
			System.out.println(source + "sourceee000000" + vendorInitiatDto.getDocumentname() + "-------");
			if (vendorInitiatDto.getDocumentname() != null) {
				VendorChecks vendorChecksobj = vendorChecksRepository
						.findByCandidateCandidateIdAndVendorIdAndSourceSourceIdAndDocumentname(
								vendorInitiatDto.getCandidateId(), vendorInitiatDto.getVendorId(),
								vendorInitiatDto.getSourceId(), vendorInitiatDto.getDocumentname());
				System.out.println("--------------docis----------" + vendorInitiatDto.getVendorId() + "---"
						+ vendorInitiatDto.getSourceId());
				if (vendorChecksobj != null) {
					// vendorChecksobj.setDocumentname(vendorInitiatDto.getDocumentname());
					vendorChecksobj
							.setAgentUploadedDocument(proofDocumentNew != null ? proofDocumentNew.getBytes() : null);
					vendorChecksobj.setCreatedBy(user);
					vendorChecksobj.setCandidateName(vendorInitiatDto.getCandidateName());
					vendorChecksobj.setDateOfBirth(vendorInitiatDto.getDateOfBirth());
					vendorChecksobj.setContactNo(vendorInitiatDto.getContactNo());
					vendorChecksobj.setFatherName(vendorInitiatDto.getFatherName());
					vendorChecksobj.setAddress(vendorInitiatDto.getAddress());
					vendorChecksobj.setAlternateContactNo(vendorInitiatDto.getAlternateContactNo());
					vendorChecksobj.setTypeOfPanel(vendorInitiatDto.getTypeOfPanel());
					vendorChecksobj.setCreatedOn(new Date());
					vendorChecksobj.setStopCheck(false);
					vendorChecksobj.setVendorCheckStatusMaster(
						    vendorInitiatDto.getVendorCheckStatusMasterId() != null ?
						    vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(vendorInitiatDto.getVendorCheckStatusMasterId()) :
						    vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(2L)
						);

//					vendorChecksobj.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(vendorInitiatDto.getVendorCheckStatusMasterId()));
//					vendorChecksobj.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findByCheckStatusCode("INPROGRESS"));
					vendorChecksRepository.save(vendorChecksobj);
					svcSearchResult.setMessage("vendor Checks document update successfully.");

				} else {

					VendorChecks vendorChecks = new VendorChecks();
					vendorChecks.setCandidate(candidateRepository.findByCandidateId(vendorInitiatDto.getCandidateId()));
					vendorChecks.setVendorId(vendorInitiatDto.getVendorId());
					vendorChecks.setSource(sourceRepository.findById(vendorInitiatDto.getSourceId()).get());
					vendorChecks.setDocumentname(vendorInitiatDto.getDocumentname());
					vendorChecks
							.setAgentUploadedDocument(proofDocumentNew != null ? proofDocumentNew.getBytes() : null);
					vendorChecks.setCreatedBy(user);
					vendorChecks.setCandidateName(vendorInitiatDto.getCandidateName());
					vendorChecks.setDateOfBirth(vendorInitiatDto.getDateOfBirth());
					vendorChecks.setContactNo(vendorInitiatDto.getContactNo());
					vendorChecks.setFatherName(vendorInitiatDto.getFatherName());
					vendorChecks.setAddress(vendorInitiatDto.getAddress());
					vendorChecks.setAlternateContactNo(vendorInitiatDto.getAlternateContactNo());
					vendorChecks.setTypeOfPanel(vendorInitiatDto.getTypeOfPanel());
					vendorChecks.setCreatedOn(new Date());
					vendorChecks.setStopCheck(false);
					vendorChecks.setVendorCheckStatusMaster(
						    vendorInitiatDto.getVendorCheckStatusMasterId() != null ?
						    vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(vendorInitiatDto.getVendorCheckStatusMasterId()) :
						    vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(2L)
						);

					//vendorChecks.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findByCheckStatusCode("INPROGRESS"));
					ObjectMapper objectMapper = new ObjectMapper();
					Map<String, String> agentAttributeMap = objectMapper.readValue(vendorChecksString,
							new TypeReference<Map<String, String>>() {
							});
					// Convert the map to an ArrayList of concatenated key-value strings
					ArrayList<String> agentAttributeList = new ArrayList<>();
					for (Map.Entry<String, String> entry : agentAttributeMap.entrySet()) {
						if (entry.getKey() != "candidateName" && entry.getKey() != "dateOfBirth"
								&& entry.getKey() != "contactNo" && entry.getKey() != "fatherName"
								&& entry.getKey() != "address" && entry.getKey() != "vendorId"
								&& entry.getKey() != "sourceId" && entry.getKey() != "candidateId"
								&& entry.getKey() != "value" && entry.getKey() != "documentname"
								&& entry.getKey() != "vendorCheckStatusMasterId") {
							
							String concatenated = entry.getKey() + "=" + entry.getValue();
							agentAttributeList.add(concatenated);
						}
					}
					System.out.println("agentAttributeList@@@@@@@@@@@@@@@@@>" + agentAttributeList);
//                     Set the agentAttirbuteValue field in vendorChecks
					vendorChecks.setAgentAttirbuteValue(agentAttributeList);

					vendorChecksRepository.save(vendorChecks);
					svcSearchResult.setMessage("vendor Checks document saved successfully.");

				}

			} else {
				VendorChecks vendorChecksobj = vendorChecksRepository
						.findByCandidateCandidateIdAndVendorIdAndSourceSourceId(vendorInitiatDto.getCandidateId(),
								vendorInitiatDto.getVendorId(), vendorInitiatDto.getSourceId());
				System.out.println("--------docelse----------------" + vendorInitiatDto.getVendorId() + "---"
						+ vendorInitiatDto.getSourceId());
				if (vendorChecksobj != null) {
					System.out.println("inside ifff");
					vendorChecksobj.setDocumentname(vendorInitiatDto.getDocumentname());
					vendorChecksobj
							.setAgentUploadedDocument(proofDocumentNew != null ? proofDocumentNew.getBytes() : null);
					vendorChecksobj.setCreatedBy(user);
					vendorChecksobj.setCandidateName(vendorInitiatDto.getCandidateName());
					vendorChecksobj.setDateOfBirth(vendorInitiatDto.getDateOfBirth());
					vendorChecksobj.setContactNo(vendorInitiatDto.getContactNo());
					vendorChecksobj.setFatherName(vendorInitiatDto.getFatherName());
					vendorChecksobj.setAddress(vendorInitiatDto.getAddress());
					vendorChecksobj.setAlternateContactNo(vendorInitiatDto.getAlternateContactNo());
					vendorChecksobj.setTypeOfPanel(vendorInitiatDto.getTypeOfPanel());
					vendorChecksobj.setCreatedOn(new Date());
					vendorChecksobj.setDocumentname(vendorInitiatDto.getDocumentname());
					vendorChecksobj.setStopCheck(false);
					vendorChecksobj.setVendorCheckStatusMaster(
						    vendorInitiatDto.getVendorCheckStatusMasterId() != null ?
						    vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(vendorInitiatDto.getVendorCheckStatusMasterId()) :
						    vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(2L)
						);
					//vendorChecksobj.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findByCheckStatusCode("INPROGRESS"));
					vendorChecksRepository.save(vendorChecksobj);
					svcSearchResult.setMessage("vendor Checks  update successfully.");

				} else {

					VendorChecks vendorChecks = new VendorChecks();
					vendorChecks.setCandidate(candidateRepository.findByCandidateId(vendorInitiatDto.getCandidateId()));
					vendorChecks.setVendorId(vendorInitiatDto.getVendorId());
					vendorChecks.setSource(sourceRepository.findById(vendorInitiatDto.getSourceId()).get());
					vendorChecks.setDocumentname(vendorInitiatDto.getDocumentname());
					vendorChecks
							.setAgentUploadedDocument(proofDocumentNew != null ? proofDocumentNew.getBytes() : null);
					vendorChecks.setCreatedBy(user);
					vendorChecks.setCandidateName(vendorInitiatDto.getCandidateName());
					vendorChecks.setDateOfBirth(vendorInitiatDto.getDateOfBirth());
					vendorChecks.setContactNo(vendorInitiatDto.getContactNo());
					vendorChecks.setFatherName(vendorInitiatDto.getFatherName());
					vendorChecks.setAddress(vendorInitiatDto.getAddress());
					vendorChecks.setAlternateContactNo(vendorInitiatDto.getAlternateContactNo());
					vendorChecks.setTypeOfPanel(vendorInitiatDto.getTypeOfPanel());
					vendorChecks.setCreatedOn(new Date());
					vendorChecks.setStopCheck(false);
					vendorChecks.setVendorCheckStatusMaster(
						    vendorInitiatDto.getVendorCheckStatusMasterId() != null ?
						    vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(vendorInitiatDto.getVendorCheckStatusMasterId()) :
						    vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(2L)
						);//					vendorChecks.setVendorCheckStatusMaster(vendorCheckStatusMasterRepository.findByCheckStatusCode("INPROGRESS"));
					ObjectMapper objectMapper = new ObjectMapper();
					Map<String, String> agentAttributeMap = objectMapper.readValue(vendorChecksString,
							new TypeReference<Map<String, String>>() {
							});
					// Convert the map to an ArrayList of concatenated key-value strings
					ArrayList<String> agentAttributeList = new ArrayList<>();
					for (Map.Entry<String, String> entry : agentAttributeMap.entrySet()) {
						if (entry.getKey() != "candidateName" && entry.getKey() != "dateOfBirth"
								&& entry.getKey() != "contactNo" && entry.getKey() != "fatherName"
								&& entry.getKey() != "address" && entry.getKey() != "vendorId"
								&& entry.getKey() != "sourceId" && entry.getKey() != "candidateId"
								&& entry.getKey() != "value" && entry.getKey() != "documentname"
								&& entry.getKey() != "vendorCheckStatusMasterId") {
							
							String concatenated = entry.getKey() + "=" + entry.getValue();

							agentAttributeList.add(concatenated);
						}
					}
					System.out.println("agentAttributeList@@@@@@@@@@@@@@@@@>" + agentAttributeList);
//                     Set the agentAttirbuteValue field in vendorChecks
					vendorChecks.setAgentAttirbuteValue(agentAttributeList);

					vendorChecksRepository.save(vendorChecks);
					svcSearchResult.setMessage("vendor Checks saved successfully.");

				}
			}
		} catch (Exception ex) {
			log.error("Exception occured in saveInitiateVendorChecks method in userServiceImpl-->" + ex);

		}
		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<ConventionalAttributesMaster> getConventionalAttributesMasterById(Long Id) {
		ServiceOutcome<ConventionalAttributesMaster> svcSearchResult = new ServiceOutcome<ConventionalAttributesMaster>();

		List<Long> ids = Arrays.asList(1L, 2L,3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);

		List<Source> findByparticularSourceIds = sourceRepository.findByParticularSourceIdsIn(ids);

		List<String> sourceNames = findByparticularSourceIds.stream()
				.map(Source::getSourceName)
				.collect(Collectors.toList());

		Optional<Source> findById2 = sourceRepository.findById(Id);

		for (String sourceName : sourceNames) {
			String findById2SourceName = findById2.get().getSourceName().toLowerCase().trim();
			String sourceNameLower = sourceName.toLowerCase().trim();

			// Splitting the source names into words
			String[] findById2Words = findById2SourceName.split("\\s+");
			String firstWordOfFindById2 = findById2Words.length > 0 ? findById2Words[0] : "";

			String[] sourceNameWords = sourceNameLower.split("\\s+");
			String firstWordOfSourceName = sourceNameWords.length > 0 ? sourceNameWords[0] : "";

			if (firstWordOfFindById2.contains(firstWordOfSourceName)) {   
				Source findBySourceName = sourceRepository.findBySourceName(sourceName);
				Id = findBySourceName.getSourceId();
				break;
			}
		}

		List<ConventionalAttributesMaster> findById = conventionalAttributesMasterRepository.findBySourceId(Id);

		if (findById != null && !findById.isEmpty()) {
			svcSearchResult.setMessage("Fetched Data");
			svcSearchResult.setData(findById.get(0));
		} else {
			svcSearchResult.setMessage("No Data Found");
		}

		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<ConventionalAttributesMaster> findBySourceName(String sourceName) {
		ServiceOutcome<ConventionalAttributesMaster> svcSearchResult = new ServiceOutcome<ConventionalAttributesMaster>();

		try {
//            Source sourceList= sourceRepository.findBySourceName(sourceName);
			Source findByName = sourceRepository.findBySourceName(sourceName);
//            if(!source.isEmpty()) {
//                svcSearchResult.setData(sourceList);
//                svcSearchResult.setOutcome(true);
//                svcSearchResult.setMessage("SUCCESS");
//            }else {
//                svcSearchResult.setData(null);
//                svcSearchResult.setOutcome(false);
//                svcSearchResult.setMessage("NO SOURCE FOUND");
//            }
			Long sourceId = findByName.getSourceId();
			System.out.println("sourceId======>" + sourceId);

			List<ConventionalAttributesMaster> findById = conventionalAttributesMasterRepository
					.findBySourceId(sourceId);
			System.out.println("findById=====" + findById.toString());
			svcSearchResult.setData(findById.get(0));

			svcSearchResult.setOutcome(true);
			svcSearchResult.setMessage("SUCCESS");
		} catch (Exception ex) {
			log.error("Exception occured in getSource method in OrganizationServiceImpl-->" + ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}


	@Override
	public ServiceOutcome<List<VendorChecks>> getAllSearchDataForVendor(SearchAllVendorCheckDTO searchAllVendorCheck) {

		ServiceOutcome<List<VendorChecks>> svcSearchResult = new ServiceOutcome<>();

		try {
			User currentUser = SecurityHelper.getCurrentUser();
			Long vendorId =currentUser.getUserId();
			String searchInput = searchAllVendorCheck.getUserSearchInput().trim();
			List<VendorChecks> searchAllVendorChecksByVendorIdAndUserSearchInput = null;

			List<Long> byCandidateName = candidateRepository.getByApplicantIdAndCandidateName(searchInput);
			List<Long> applicantIdAndCandidateName = byCandidateName;

			searchAllVendorChecksByVendorIdAndUserSearchInput =	vendorChecksRepository.searchAllVendorChecksByVendorIdAndUserSearchInputByCandidateName(vendorId, applicantIdAndCandidateName, null,null,null);				

			if(applicantIdAndCandidateName.isEmpty()) {
				User findByEmployeeId = userRepository.findByEmployeeId(searchInput);
				Source findBySourceName = sourceRepository.findBySourceName(searchInput);
				if (findByEmployeeId != null) {
					Long agentId = findByEmployeeId.getUserId();
					searchAllVendorChecksByVendorIdAndUserSearchInput =	vendorChecksRepository.searchAllVendorChecksByVendorIdAndUserSearchInputByCandidateName(vendorId, applicantIdAndCandidateName, agentId,null,null);				
				}
				else if(findBySourceName != null) {
					Long sourceId = findBySourceName.getSourceId();
					searchAllVendorChecksByVendorIdAndUserSearchInput =	vendorChecksRepository.searchAllVendorChecksByVendorIdAndUserSearchInputByCandidateName(vendorId, applicantIdAndCandidateName, null,sourceId,null);				
				}
				else {
					VendorCheckStatusMaster findByCheckStatusName = vendorCheckStatusMasterRepository.findByCheckStatusName(searchInput);
					Long vendorCheckStatusId = findByCheckStatusName.getVendorCheckStatusMasterId();
					searchAllVendorChecksByVendorIdAndUserSearchInput =	vendorChecksRepository.searchAllVendorChecksByVendorIdAndUserSearchInputByCandidateName(vendorId, applicantIdAndCandidateName, null,null,vendorCheckStatusId);
				}
			}

			if (!searchAllVendorChecksByVendorIdAndUserSearchInput.isEmpty()) {
				svcSearchResult.setData(searchAllVendorChecksByVendorIdAndUserSearchInput);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			} else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NO VENDORCHECKS FOUND");
			}


		} catch (Exception e) {
			log.error("Exception occured in getVendorCheckDetails method in userServiceImpl-->" + e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}

		return svcSearchResult;
	}


	@Override
	public ServiceOutcome<VendorcheckdashbordtDto> getVendorCheckStatusAndCount(VendorcheckdashbordtDto dashboardDto) {

		ServiceOutcome<VendorcheckdashbordtDto> svcSearchResult = new ServiceOutcome<VendorcheckdashbordtDto>();
		List<VendorCheckStatusAndCountDTO> vendorCheckStatusAndCount = new ArrayList<VendorCheckStatusAndCountDTO>();

		try {
			if (dashboardDto.getUserId() != null && dashboardDto.getUserId() != 0l) {
//				System.out.println("VendorcheckdashbordtDto SERVICE ================ "+dashboardDto.getFromDate());
//				System.out.println("VendorcheckdashbordtDto SERVICE ================ "+dashboardDto.getToDate());
//				System.out.println("VendorcheckdashbordtDto SERVICE ================ "+dashboardDto.getUserId());

				String fromDate = dashboardDto.getFromDate();
				String toDate = dashboardDto.getToDate();
				Long vendorId = dashboardDto.getUserId();

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

				AtomicInteger clear = new AtomicInteger(0);
				AtomicInteger inProgress = new AtomicInteger(0);
				AtomicInteger inSufficiency = new AtomicInteger(0);
				AtomicInteger majorDiscrepancy = new AtomicInteger(0);
				AtomicInteger minorDiscrepancy = new AtomicInteger(0);
				AtomicInteger unableToVerify = new AtomicInteger(0);

				List<VendorChecks> vendorList = vendorChecksRepository.vendorDashboardStatusAndCount(vendorId,fromDateFormat,toDateFormat);

				vendorList.stream()
				.map(VendorChecks::getVendorCheckStatusMaster)
				.forEach(vendorCheckStatusId -> {
//					System.out.println("VendorCheckStatusId: " + vendorCheckStatusId);

					if (vendorCheckStatusId != null) {
						if (vendorCheckStatusId.getVendorCheckStatusMasterId() == 1) {
							clear.incrementAndGet();
						} else if (vendorCheckStatusId.getVendorCheckStatusMasterId() == 2) {
							inProgress.incrementAndGet();
						} else if (vendorCheckStatusId.getVendorCheckStatusMasterId() == 3) {
							inSufficiency.incrementAndGet();
						} else if (vendorCheckStatusId.getVendorCheckStatusMasterId() == 4) {
							majorDiscrepancy.incrementAndGet();
						} else if (vendorCheckStatusId.getVendorCheckStatusMasterId() == 5) {
							minorDiscrepancy.incrementAndGet();
						} else if (vendorCheckStatusId.getVendorCheckStatusMasterId() == 6) {
							unableToVerify.incrementAndGet();
						}
					}

				});

				int clearCount = clear.get();
				int inProgressCount = inProgress.get();
				int inSufficientCount = inSufficiency.get();
				int majorDiscrepancyCount = majorDiscrepancy.get();
				int minorDiscrepancyCount = minorDiscrepancy.get();
				int unableToVerifyCountCount = unableToVerify.get();

//				log.info("Clear: {}" + clearCount);
//				log.info("In Progress: {}" + inProgressCount);
//				log.info("In Sufficiency: {}" + inSufficientCount);
//				log.info("Major Discrepancy: {}" + majorDiscrepancyCount);
//				log.info("Minor Discrepancy: {}" + minorDiscrepancyCount);
//				log.info("Unable To Verify: {}" + unableToVerifyCountCount);

				vendorCheckStatusAndCount.add(0,new VendorCheckStatusAndCountDTO("Clear","CLEAR",clearCount));		
				vendorCheckStatusAndCount.add(1,new VendorCheckStatusAndCountDTO("In Progress","INPROGRESS",inProgressCount));		
				vendorCheckStatusAndCount.add(2,new VendorCheckStatusAndCountDTO("In Sufficiency","INSUFFICIENCY",inSufficientCount));
				vendorCheckStatusAndCount.add(3,new VendorCheckStatusAndCountDTO("Major Discrepancy","MAJORDISCREPANCY",majorDiscrepancyCount));
				vendorCheckStatusAndCount.add(4,new VendorCheckStatusAndCountDTO("Minor Discrepancy","MINORDISCREPANCY",minorDiscrepancyCount));
				vendorCheckStatusAndCount.add(5,new VendorCheckStatusAndCountDTO("Unable to Verify","UNABLETOVERIFY",unableToVerifyCountCount));

				VendorcheckdashbordtDto vendorDashboardDTO = new VendorcheckdashbordtDto(null,null,null,null,null,vendorCheckStatusAndCount,fromDate,toDate,vendorId);
				svcSearchResult.setData(vendorDashboardDTO);
				svcSearchResult.setOutcome(true);

				//				vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId()			

			}
			else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("please specify user.");
			}

		} catch (Exception e) {
			log.error("Exception occured in getVendorCheckStatusAndCount method in UserServiceIMPL-->",e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);			
		}

		return svcSearchResult;
	}


	@Override
	public ServiceOutcome<VendorcheckdashbordtDto> updateVendor(VendorcheckdashbordtDto updateVendor) {

		ServiceOutcome<VendorcheckdashbordtDto> svcSearchResult = new ServiceOutcome<VendorcheckdashbordtDto>();

		try {

			Long vendorCheckId = updateVendor.getVendorcheckId();
			Long vendorId = updateVendor.getVendorId();

			if(vendorCheckId != null) {
				VendorChecks findByVendorcheckId = vendorChecksRepository.findByVendorcheckId(vendorCheckId);

				if(findByVendorcheckId != null) {
					findByVendorcheckId.setVendorId(vendorId);
					VendorChecks save = vendorChecksRepository.save(findByVendorcheckId);

					if(save != null) {
						svcSearchResult.setMessage("Vendor Updated Successfully");
						svcSearchResult.setOutcome(true);
					}					
				}		

			}

		} catch (Exception e) {
			log.error("Exception occured in updateVendor method in UserServiceIMPL-->",e);

		}

		return svcSearchResult;
	}


	@Override
	public ServiceOutcome<VendorcheckdashbordtDto> stopCheck(VendorcheckdashbordtDto stopCheck) {

		ServiceOutcome<VendorcheckdashbordtDto> svcSearchResult = new ServiceOutcome<VendorcheckdashbordtDto>();

		try {
			Long vendorCheckId = stopCheck.getVendorcheckId();
			Boolean stopCheckStatus = stopCheck.getStopCheck();

			if (stopCheckStatus == null) {
				stopCheckStatus = false;
			}		
			stopCheckStatus = !stopCheckStatus;			
			if(vendorCheckId != null) {
				VendorChecks findByVendorcheckId = vendorChecksRepository.findByVendorcheckId(vendorCheckId);

				if(findByVendorcheckId != null) {
					findByVendorcheckId.setStopCheck(stopCheckStatus);
					VendorChecks save = vendorChecksRepository.save(findByVendorcheckId);

					if(save != null) {
						svcSearchResult.setMessage("StopCheck Status Updated Successfully");
						svcSearchResult.setOutcome(true);
					}					
				}		

			}

		} catch (Exception e) {
			log.error("Exception occured in stopCheck method in UserServiceIMPL-->",e);
		}

		return svcSearchResult;
	}


	@Override
	public ServiceOutcome<?> addChecks(String addCheckData) {

		ServiceOutcome<?> svcSearchResult = new ServiceOutcome<>();

		try {			
			String jsonString = addCheckData;
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(jsonString);

			String sourceIdAsString = jsonNode.get("sourceId").asText();	
			Long sourceId = null;

			if (!sourceIdAsString.isEmpty()) {
				sourceId = Long.parseLong(sourceIdAsString);
			}

			String sourceName = jsonNode.get("sourceName").asText();
			String sourceCode = jsonNode.get("sourceCode").asText();
			String Status = jsonNode.get("changeActiveStatus").asText();

			boolean changeActiveStatus = Boolean.parseBoolean(Status);

			if(sourceId != null && !changeActiveStatus) {
				Optional<Source> findById = sourceRepository.findById(sourceId);
				Source existingSource = findById.get();
				existingSource.setSourceName(sourceName);
				existingSource.setSourceCode(sourceCode.toUpperCase());

				Source updateCheck = sourceRepository.save(existingSource);

				if(updateCheck != null) {
					svcSearchResult.setMessage("Check Updated Successfully");
					svcSearchResult.setOutcome(true);
				}
			}
			else if(changeActiveStatus) {
				String isActive = jsonNode.get("isActive").asText();
				boolean parseIsActive = Boolean.parseBoolean(isActive);

				Optional<Source> findById = sourceRepository.findById(sourceId);
				Source existingSource = findById.get();
				existingSource.setIsActive(!parseIsActive);			    
				Source updateIsActiveStatus = sourceRepository.save(existingSource);

				if(updateIsActiveStatus != null) {
					svcSearchResult.setMessage("Check is Updated to " + (!parseIsActive ? "Active" : "Inactive"));
					svcSearchResult.setOutcome(true);
				}
			}
			else {
				if(sourceName != null) {
					  String sanitizedString = sourceName.replaceAll("[^a-zA-Z0-9]", "");

				        // Remove whitespace
					   sanitizedString = sanitizedString.replaceAll("\\s", "");
  
				        List<Source> findAllSource = sourceRepository.findAll();
//				        List<String> sourceNames = findAllSource.stream()
//				                .map(Source::getSourceName)
//				                .collect(Collectors.toList());
				        
				        List<String> sanitizedSourceNames = findAllSource.stream()
				                .map(Source::getSourceName)
				                .map(sourceNames -> sourceNames.replaceAll("\\s", "").replaceAll("[^a-zA-Z0-9]", ""))
				                .collect(Collectors.toList());
				        	        
				        
				        if(sanitizedSourceNames.contains(sanitizedString)){
							svcSearchResult.setMessage("Check Already Exists");
							svcSearchResult.setOutcome(false);
				        }

				        else {
							Date currentDate = new Date();
							Source source = new Source();
							source.setSourceName(sourceName);
							source.setSourceCode(sourceCode.toUpperCase());
							source.setCreatedOn(currentDate);
							source.setIsActive(true);				
							Source newCheck = sourceRepository.save(source);

							if(newCheck != null) {
								svcSearchResult.setMessage("Check Added Successfully");
								svcSearchResult.setOutcome(true);
							}

						}
				        }
				        
				}



		} catch (Exception e) {
			log.error("Exception occured in addCheck method in UserServiceIMPL-->",e);
		}

		return svcSearchResult;
	}

	@Override
	public ServiceOutcome<List<Source>> getAllSource() {
		ServiceOutcome<List<Source>> svcSearchResult = new ServiceOutcome<List<Source>>();
		try {
			List<Source> sourceList= sourceRepository.findAll();
			if(!sourceList.isEmpty()) {
				svcSearchResult.setData(sourceList);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("SUCCESS");
			}else {
				svcSearchResult.setData(null);
				svcSearchResult.setOutcome(false);
				svcSearchResult.setMessage("NO SOURCE FOUND");
			}
		}
		catch(Exception ex)
		{
			log.error("Exception occured in getAllSource method in USERSERVICEIMPL-->"+ex);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("Something Went Wrong, Please Try After Sometimes.");
		}
		return svcSearchResult;
	}

	
	@Override
	public ServiceOutcome<Source> deleteCheckById(Long sourceId) {

		ServiceOutcome<Source> svcSearchResult = new ServiceOutcome<>();

		try {
			if(sourceId != null) {
				sourceRepository.deleteById(sourceId);
				svcSearchResult.setOutcome(true);
				svcSearchResult.setMessage("Check Deleted Successfully");				
			}
		}
		catch (Exception e) {
			log.error("Exception occured in deleteCheckById method in USERSERVICEIMPL-->"+e);
			svcSearchResult.setData(null);
			svcSearchResult.setOutcome(false);
			svcSearchResult.setMessage("This Check Cannot be Deleted");
		}

		return svcSearchResult;
	}

}