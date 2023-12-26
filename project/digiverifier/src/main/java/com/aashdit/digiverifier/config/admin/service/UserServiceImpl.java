package com.aashdit.digiverifier.config.admin.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.aashdit.digiverifier.common.model.ServiceOutcome;
import com.aashdit.digiverifier.config.admin.dto.CivilProceedingsDTO;
import com.aashdit.digiverifier.config.admin.dto.CriminalProceedingsDTO;
import com.aashdit.digiverifier.config.admin.dto.UserDto;
import com.aashdit.digiverifier.config.admin.dto.VendorChecksDto;
import com.aashdit.digiverifier.config.admin.dto.VendorInitiatDto;
import com.aashdit.digiverifier.config.admin.dto.VendorcheckdashbordtDto;
import com.aashdit.digiverifier.config.admin.model.AgentSampleCsvXlsMaster;
import com.aashdit.digiverifier.config.admin.model.CriminalCheck;
import com.aashdit.digiverifier.config.admin.model.Role;
import com.aashdit.digiverifier.config.admin.model.User;
import com.aashdit.digiverifier.config.admin.model.VendorChecks;
import com.aashdit.digiverifier.config.admin.model.VendorUploadChecks;
import com.aashdit.digiverifier.config.admin.repository.AgentSampleCsvXlsMasterRepository;
import com.aashdit.digiverifier.config.admin.repository.CriminalCheckRepository;
import com.aashdit.digiverifier.config.admin.repository.RoleRepository;
import com.aashdit.digiverifier.config.admin.repository.UserRepository;
import com.aashdit.digiverifier.config.admin.repository.VendorChecksRepository;
import com.aashdit.digiverifier.config.admin.repository.VendorUploadChecksRepository;
import com.aashdit.digiverifier.config.candidate.model.Candidate;
import com.aashdit.digiverifier.config.candidate.model.CandidateCaseDetails;
import com.aashdit.digiverifier.config.candidate.repository.CandidateCaseDetailsRepository;
import com.aashdit.digiverifier.config.candidate.repository.CandidateRepository;
import com.aashdit.digiverifier.config.candidate.util.CSVUtil;
import com.aashdit.digiverifier.config.candidate.util.ExcelUtil;
import com.aashdit.digiverifier.config.superadmin.dto.DashboardDto;
import com.aashdit.digiverifier.config.superadmin.model.Source;
import com.aashdit.digiverifier.config.superadmin.model.VendorCheckStatusMaster;
import com.aashdit.digiverifier.config.superadmin.repository.ColorRepository;
import com.aashdit.digiverifier.config.superadmin.repository.OrganizationRepository;
import com.aashdit.digiverifier.config.superadmin.repository.SourceRepository;
import com.aashdit.digiverifier.config.superadmin.repository.VendorCheckStatusMasterRepository;
import com.aashdit.digiverifier.config.superadmin.repository.VendorMasterNewRepository;
import com.aashdit.digiverifier.utils.ApplicationDateUtils;
import com.aashdit.digiverifier.utils.AwsUtils;
import com.aashdit.digiverifier.utils.SecurityHelper;
import com.aashdit.digiverifier.vendorcheck.dto.FetchVendorConventionalCandidateDto;
import com.aashdit.digiverifier.vendorcheck.model.ConventionalVendorCandidatesSubmitted;
import com.aashdit.digiverifier.vendorcheck.model.ConventionalVendorliChecksToPerform;
import com.aashdit.digiverifier.vendorcheck.model.ModeOfVerificationStatusMaster;
import com.aashdit.digiverifier.vendorcheck.repository.ConventionalCandidatesSubmittedRepository;
import com.aashdit.digiverifier.vendorcheck.repository.ConventionalVendorCandidatesSubmittedRepository;
import com.aashdit.digiverifier.vendorcheck.repository.LiCheckToPerformRepository;
import com.aashdit.digiverifier.vendorcheck.repository.ModeOfVerificationStatusMasterRepository;
import com.aashdit.digiverifier.vendorcheck.service.liCheckToPerformService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private liCheckToPerformService liCheckToPerformService;

    @Autowired
    RestTemplate restTemplate;
    public static final String DIGIVERIFIER_DOC_BUCKET_NAME = "digiverifier-new";
    @Autowired
    AmazonS3 s3Client;
    @Autowired
    ConventionalVendorCandidatesSubmittedRepository conventionalVendorCandidatesSubmittedRepository;
    @Autowired
    ConventionalCandidatesSubmittedRepository conventionalCandidatesSubmittedRepository;

    @Autowired
    AwsUtils awsUtils;
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

    @Transactional
    @Override
    public ServiceOutcome<UserDto> saveUser(UserDto user) {
        ServiceOutcome<UserDto> svcSearchResult = new ServiceOutcome<>();
        UserDto userDto = new UserDto();
        try {
            User result = null;
            log.debug("User object is-->" + user);
            if (user.getUserId() != null && !user.getUserId().equals(0l) && user.getUserEmailId() != null) {
                User findUserEmail = userRepository.findByUserEmailId(user.getUserEmailId());
                if (findUserEmail != null && findUserEmail.getUserId() != user.getUserId()) {
                    svcSearchResult.setData(null);
                    svcSearchResult.setOutcome(false);
                    svcSearchResult.setMessage("User Email Id already exists.Choose another Email Id");
                } else {
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

                            saveNewUser.setUserName(user.getEmployeeId());
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
            for (User userObj : users) {
                userObj.setUserName(userObj.getEmployeeId());
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
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser()
                    : userRepository.findByUserId(53l);
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
//            System.out.println(candidateId);

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

    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
    @Autowired
    LiCheckToPerformRepository liCheckToPerformRepository;
    @Autowired
    ModeOfVerificationStatusMasterRepository modeOfVerificationStatusMasterRepository;

    @Override
    public ServiceOutcome<List<VendorChecksDto>> getallVendorCheckDetails(DashboardDto dashboardDto) {
        ServiceOutcome<List<VendorChecksDto>> svcSearchResult = new ServiceOutcome<List<VendorChecksDto>>();

        try {
//            List<VendorChecks> vendorList = new ArrayList<>();
            List<VendorChecksDto> vendorChecksDtos = new ArrayList<>();
            String strToDate = "";
            String strFromDate = "";
            strToDate = dashboardDto.getToDate() != null ? dashboardDto.getToDate()
                    : ApplicationDateUtils.getStringTodayAsDDMMYYYY();
            strFromDate = dashboardDto.getFromDate() != null ? dashboardDto.getFromDate()
                    : ApplicationDateUtils
                    .subtractNoOfDaysFromDateAsDDMMYYYY(new SimpleDateFormat("dd/MM/yyyy").parse(strToDate), 7);
            Date startDate = formatter.parse(strFromDate + " 00:00:00");
            Date endDate = formatter.parse(strToDate + " 23:59:59");
            
			Pageable pageable =null;
			if(dashboardDto.getPageNumber()!=null) {
				pageable = PageRequest.of(dashboardDto.getPageNumber(), 10);
			}
			
			Long checkStatusId = Long.valueOf(0);
			if(!dashboardDto.getStatus().isEmpty()) {
				Optional<VendorCheckStatusMaster> vendorCheckStatusMaster = vendorCheckStatusMasterRepository.findByCheckStatusCode(dashboardDto.getStatus());
				if(vendorCheckStatusMaster.isPresent())
					checkStatusId = vendorCheckStatusMaster.get().getVendorCheckStatusMasterId();
			}
			
            Page<VendorChecks> vendorList = vendorChecksRepository.findAllByDateRange(dashboardDto.getUserId(), startDate, endDate, checkStatusId, pageable);
            if(vendorList.hasContent()) {
            	vendorList.getContent().forEach(vc -> {
                    VendorChecksDto vendorChecksDto = new VendorChecksDto();
                    vendorChecksDto.setAddress(vc.getAddress());
                    vendorChecksDto.setAgentUploadedDocument(vc.getAgentUploadedDocument());
                    vendorChecksDto.setAlternateContactNo(vc.getAlternateContactNo());
                    vendorChecksDto.setCandidate(vc.getCandidate());
                    vendorChecksDto.setCandidateName(vc.getCandidateName());
                    ConventionalVendorliChecksToPerform byVendorChecksVendorcheckId = liCheckToPerformRepository
                            .findByVendorChecksVendorcheckId(vc.getVendorcheckId());
                    if (byVendorChecksVendorcheckId != null) {
                        vendorChecksDto.setCheckUniqueId(String.valueOf(byVendorChecksVendorcheckId.getCheckUniqueId()));
                        vendorChecksDto.setVendorCheckStatusMaster(byVendorChecksVendorcheckId.getCheckStatus());
                        ModeOfVerificationStatusMaster modeOfVerificationStatusMaster = modeOfVerificationStatusMasterRepository
                                .findById(Long.valueOf(byVendorChecksVendorcheckId.getModeOfVerificationRequired())).get();
                        vendorChecksDto
                                .setModeOfVerificationPerformed(modeOfVerificationStatusMaster.getModeOfVerification());
                        if (byVendorChecksVendorcheckId.getStopCheck() != null) {
                            vendorChecksDto.setStopCheckStatus(byVendorChecksVendorcheckId.getStopCheck());
                        }
                        if (byVendorChecksVendorcheckId.getDateToComplete() != null) {
                            vendorChecksDto
                                    .setFastTrackDateTime(String.valueOf(byVendorChecksVendorcheckId.getDateToComplete()));
                        }
                        if (byVendorChecksVendorcheckId.getDisabled() != null) {
                            vendorChecksDto.setDisableStatus(byVendorChecksVendorcheckId.getDisabled());
                        }
                    }
                    vendorChecksDto.setContactNo(vc.getContactNo());
                    vendorChecksDto.setCreatedBy(vc.getCreatedBy());
                    vendorChecksDto.setCreatedOn(vc.getCreatedOn());
                    vendorChecksDto.setDateOfBirth(vc.getDateOfBirth());
                    vendorChecksDto.setDocumentname(vc.getDocumentname());
                    vendorChecksDto.setEmailId(vc.getEmailId());
                    vendorChecksDto.setExpireson(vc.getExpireson());
                    vendorChecksDto.setFatherName(vc.getFatherName());
                    vendorChecksDto.setIsproofuploaded(vc.getIsproofuploaded());
                    vendorChecksDto.setPathKey(vc.getPathKey());
                    vendorChecksDto.setSource(vc.getSource());
                    vendorChecksDto.setTat(vc.getTat());
                    vendorChecksDto.setTypeOfPanel(vc.getTypeOfPanel());
                    vendorChecksDto.setVendorcheckId(vc.getVendorcheckId());
                    vendorChecksDto.setVendorId(vc.getVendorId());
                    VendorUploadChecks vendoruploadChecks = vendorUploadChecksRepository
                            .findByVendorChecksVendorcheckId(vc.getVendorcheckId());
                    if (vendoruploadChecks != null) {
                        vendorChecksDto.setVendorUplodedDocument(vendoruploadChecks.getVendorUploadedDocument());
                    }
                    vendorChecksDtos.add(vendorChecksDto);
                });
            }
            List<VendorChecksDto> collect = vendorChecksDtos.stream().filter(vc -> vc.getCheckUniqueId() != null)
                    .collect(Collectors.toList());
            if (!vendorList.isEmpty()) {
                svcSearchResult.setData(collect);
                svcSearchResult.setOutcome(true);
                svcSearchResult.setMessage(String.valueOf(vendorList.getTotalPages()));
            } else {
                svcSearchResult.setData(new ArrayList<>());
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
    public ServiceOutcome<List<VendorChecks>> getallVendorChecsa(Long venorId) {
        ServiceOutcome<List<VendorChecks>> svcSearchResult = new ServiceOutcome<List<VendorChecks>>();

        try {
            List<VendorChecks> vendorList = new ArrayList<>();
            vendorList = vendorChecksRepository.findAllByVendorId(venorId);

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

    @Autowired
    private CriminalCheckRepository criminalCheckRepository;

    @Override
    @Transactional
    public ServiceOutcome<VendorChecks> saveproofuploadVendorChecks(String vendorChecksString,
                                                                    MultipartFile proofDocumentNew, String vendorRemarksReport) {
//        System.out.println(proofDocumentNew + "===========================" + vendorChecksString);
        ServiceOutcome<VendorChecks> svcSearchResult = new ServiceOutcome<VendorChecks>();
        VendorUploadChecks result = null;
        ArrayList<CriminalCheck> criminalChecks = new ArrayList<>();
        try {
            VendorcheckdashbordtDto vendorcheckdashbordtDto = new ObjectMapper().readValue(vendorChecksString, VendorcheckdashbordtDto.class);
            VendorChecks vendorCheckss = vendorChecksRepository
                    .findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
            VendorUploadChecks vendorUploadChecks = vendorUploadChecksRepository
                    .findByVendorChecksVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser()
                    : userRepository.findByUserId(53l);
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayList<String> agentAttributeList = new ArrayList<>();
            String vendorRemarksRep = vendorRemarksReport.replace("\"", "");
            

            if(vendorCheckss.getSource().getSourceName().equalsIgnoreCase("GLOBAL DATABASE CHECK")) {
            	TypeReference<Map<String, List<Map<String, String>>>> typeReference = new TypeReference<Map<String, List<Map<String, String>>>>() {
            	};
            	Map<String, List<Map<String, String>>> data = objectMapper.readValue(vendorRemarksReport, typeReference);
            	// Convert the map entries to a list
            	List<Map<String, List<Map<String, String>>>> resultList = new ArrayList<>();
            	for (Map.Entry<String, List<Map<String, String>>> entry : data.entrySet()) {
            		Map<String, List<Map<String, String>>> groupMap = new HashMap<>();
            		groupMap.put(entry.getKey(), entry.getValue());
            		resultList.add(groupMap);
            		String entryJson = objectMapper.writeValueAsString(groupMap);
            		System.out.println(entryJson);
            		agentAttributeList.add(entryJson);
            	}
            	// Print the result list
            	for (Map<String, List<Map<String, String>>> entry : resultList) {
            		System.out.println(entry);
            	}
            }
            if(vendorCheckss.getSource().getSourceName().equalsIgnoreCase("GLOBAL DATABASE CHECK")==false) {
            	if (vendorcheckdashbordtDto.getValue() != null) {
            		if (vendorRemarksRep.contains("null") == false) {
            			String dssa = vendorRemarksReport.replace("\"", "");
            			Map<String, String> agentAttributeMap = objectMapper.readValue(vendorRemarksReport, new TypeReference<Map<String, String>>() {
            			});
            			for (Map.Entry<String, String> entry : agentAttributeMap.entrySet()) {
            				String concatenated = entry.getKey() + "=" + entry.getValue();
            				String keyWithoutSpaces = entry.getKey().replaceAll("\\s", "").toLowerCase();
            				if (keyWithoutSpaces.equalsIgnoreCase("pancardnumber")) {
            					String panCardNumberValue = entry.getValue();
            					System.out.println("PanCardNumber value: " + panCardNumberValue);
            					VendorChecks byVendorcheckId = vendorChecksRepository
            							.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
            					Candidate byCandidateId = candidateRepository
            							.findByCandidateId(byVendorcheckId.getCandidate().getCandidateId());
            					byCandidateId.setPanNumber(panCardNumberValue);
            					Candidate save = candidateRepository.save(byCandidateId);
            					log.info("updated candidate with pancard number -" + save.getPanNumber()
            					+ " -for candidate -" + save.getCandidateId());
            				}
            				if (keyWithoutSpaces.equalsIgnoreCase("aadharcardno")) {
            					String aadharCardNumberValue = entry.getValue();
            					log.info("AADHARCardNumber value: " + aadharCardNumberValue);
            					VendorChecks byVendorcheckId = vendorChecksRepository
            							.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
            					Candidate byCandidateId = candidateRepository
            							.findByCandidateId(byVendorcheckId.getCandidate().getCandidateId());
            					byCandidateId.setAadharNumber(aadharCardNumberValue);
            					Candidate save = candidateRepository.save(byCandidateId);
            					log.info("updated candidate with aadhar number -" + save.getAadharNumber()
            					+ " -for candidate -" + save.getCandidateId());
            				}
            				if (keyWithoutSpaces.equalsIgnoreCase("drivinglicensenumber")) {
            					String drivinglicneseNumber = entry.getValue();
            					log.info("Driving License number value: " + drivinglicneseNumber);
            					VendorChecks byVendorcheckId = vendorChecksRepository
            							.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
            					Candidate byCandidateId = candidateRepository
            							.findByCandidateId(byVendorcheckId.getCandidate().getCandidateId());
            					byCandidateId.setDrivingLicenseNumber(drivinglicneseNumber);
            					Candidate save = candidateRepository.save(byCandidateId);
            					log.info("updated candidate with aadhar number -" + save.getAadharNumber()
            					+ " -for candidate -" + save.getCandidateId());
            				}
            				if (keyWithoutSpaces.equalsIgnoreCase("passportnumber")) {
            					String passportnumber = entry.getValue();
            					log.info("Driving License number value: " + passportnumber);
            					VendorChecks byVendorcheckId = vendorChecksRepository
            							.findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
            					Candidate byCandidateId = candidateRepository
            							.findByCandidateId(byVendorcheckId.getCandidate().getCandidateId());
            					byCandidateId.setPassportNumber(passportnumber);
            					Candidate save = candidateRepository.save(byCandidateId);
            					log.info("updated candidate with aadhar number -" + save.getAadharNumber()
            					+ " -for candidate -" + save.getCandidateId());
            				}
            				if (keyWithoutSpaces.equalsIgnoreCase("yearofpassing")) {
            					log.info("eve" + entry.getValue());
            				}
            				agentAttributeList.add(concatenated);
            			}
            		}
            	}
            }

            if (vendorUploadChecks == null) {
                VendorUploadChecks vendorUploadCheckNew = new VendorUploadChecks();
                System.out.println("-------------create------");
                if (proofDocumentNew != null) {
                    byte[] vendorProof = proofDocumentNew.getBytes();
                    if (vendorProof != null) {
                        vendorUploadCheckNew.setVendorUploadedDocument(vendorProof);
                        String contentType = proofDocumentNew.getContentType();
                        if (contentType.equalsIgnoreCase("application/pdf")) {
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
                                    vendorUploadCheckNew.setVendorUploadedImage(jsonEncodedImageMapsList);

                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }else {
                            System.out.println(contentType + "Content type");
                            // Directly encode the image to Base64
                            String encodedImage = Base64.getEncoder().encodeToString(vendorProof);
                            // Create a new map for each image
                            Map<String, List<String>> encodedImageMap = new HashMap<>();
                            String key = "image1"; // You can customize the key as needed
                            List<String> encodedImagesForDocument = new ArrayList<>();
                            encodedImagesForDocument.add(encodedImage);
                            encodedImageMap.put(key, encodedImagesForDocument);

                            // Add the map to the list
                            List<Map<String, List<String>>> encodedImageMapsList = new ArrayList<>();
                            encodedImageMapsList.add(encodedImageMap);

                            log.info("encodedImagesForDocument size: {}", encodedImageMapsList.size());

                            // Convert the list to a JSON string
                            try {
                                ObjectMapper objectMapper1 = new ObjectMapper();
                                String jsonEncodedImageMapsList = objectMapper1.writeValueAsString(encodedImageMapsList);
                                // Set the JSON string to the entity field
                                vendorUploadCheckNew.setVendorUploadedImage(jsonEncodedImageMapsList);
                            } catch (JsonProcessingException e) {
                                // Handle the exception (e.g., log or throw)
                                e.printStackTrace();
                            }
                        }
                        // NEW CHANGE FOR CONVERTING PDF TO IMAGE END 

                    }
                }
//                vendorUploadCheckNew.setVendorUploadedDocument(proofDocumentNew != null ? proofDocumentNew.getBytes() : null);
                vendorUploadCheckNew
                        .setAgentColor(colorRepository.findById(vendorcheckdashbordtDto.getColorid()).get());
                vendorUploadCheckNew.setCreatedOn(new Date());
                vendorUploadCheckNew.setCreatedBy(user);
                vendorUploadCheckNew.setVendorChecks(vendorCheckss);
                vendorUploadCheckNew.setDocumentname(vendorcheckdashbordtDto.getDocumentname());
                vendorUploadCheckNew.setVendorAttirbuteValue(agentAttributeList);
                System.out.println("-------------------==========getVendorCheckStatusMasterId");
                result = vendorUploadChecksRepository.save(vendorUploadCheckNew);
//                System.out.println(result + "-------------------==========result");
                if (result != null) {
                    VendorChecks vendorChecksnew = vendorChecksRepository
                            .findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
                    vendorChecksnew.setIsproofuploaded(true);
                    VendorCheckStatusMaster byId = vendorCheckStatusMasterRepository
                            .getById(Long.valueOf(vendorcheckdashbordtDto.getStatus()));
                    if (byId != null) {
                        vendorChecksnew.setVendorCheckStatusMaster(byId);
                    }
                    vendorChecksRepository.save(vendorChecksnew);
                    liCheckToPerformService.UpdateBGVCheckStatusRowwise(vendorChecksString, proofDocumentNew, "ONLINE");
                    svcSearchResult.setMessage("vendorchecks document saved successfully.");

                } else {
                    System.out.println("-------------candidate-----else------");
                    svcSearchResult.setData(null);
                    svcSearchResult.setOutcome(false);
                    // svcSearchResult.setMessage(messageSource.getMessage("msg.error", null,
                    // LocaleContextHolder.getLocale()));
                }

            } else {
                System.out.println("-------------update------");

                // NEW CHANGE FOR CONVERT PDF TO IMAGE THIS FOR UPDATE PROOF START

                if (proofDocumentNew != null) {
                    String contentType = proofDocumentNew.getContentType();
                    if (contentType.equalsIgnoreCase("application/pdf")) {
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
                                    vendorUploadChecks.setVendorUploadedImage(jsonEncodedImageMapsList);
                                } catch (JsonProcessingException e) {
                                    // Handle the exception (e.g., log or throw)
                                    e.printStackTrace();
                                }
                            }

                        }
                    } else {
                        byte[] vendorProof = proofDocumentNew.getBytes();
                        System.out.println(contentType + "Content type");
                        // Directly encode the image to Base64
                        String encodedImage = Base64.getEncoder().encodeToString(vendorProof);
                        // Create a new map for each image
                        Map<String, List<String>> encodedImageMap = new HashMap<>();
                        String key = "image1"; // You can customize the key as needed
                        List<String> encodedImagesForDocument = new ArrayList<>();
                        encodedImagesForDocument.add(encodedImage);
                        encodedImageMap.put(key, encodedImagesForDocument);

                        // Add the map to the list
                        List<Map<String, List<String>>> encodedImageMapsList = new ArrayList<>();
                        encodedImageMapsList.add(encodedImageMap);

                        log.info("encodedImagesForDocument size: {}", encodedImageMapsList.size());

                        // Convert the list to a JSON string
                        try {
                            ObjectMapper objectMapper1 = new ObjectMapper();
                            String jsonEncodedImageMapsList = objectMapper1.writeValueAsString(encodedImageMapsList);
                            // Set the JSON string to the entity field
                            vendorUploadChecks.setVendorUploadedImage(jsonEncodedImageMapsList);
                        } catch (JsonProcessingException e) {
                            // Handle the exception (e.g., log or throw)
                            e.printStackTrace();
                        }
                    }
                }
				 
                // NEW CHANGE FOR CONVERT PDF TO IMAGE THIS FOR UPDATE PROOF END
                
                vendorUploadChecks
                        .setVendorUploadedDocument(proofDocumentNew != null ? proofDocumentNew.getBytes() : null);
                vendorUploadChecks.setAgentColor(colorRepository.findById(vendorcheckdashbordtDto.getColorid()).get());
                vendorUploadChecks.setCreatedOn(new Date());
                vendorUploadChecks.setCreatedBy(user);
                vendorUploadChecks.setDocumentname(vendorcheckdashbordtDto.getDocumentname());
                vendorUploadChecks.setVendorAttirbuteValue(agentAttributeList);
                result = vendorUploadChecksRepository.save(vendorUploadChecks);
                if (result != null) {
                    VendorChecks vendorChecksnew = vendorChecksRepository
                            .findByVendorcheckId(vendorcheckdashbordtDto.getVendorcheckId());
                    vendorChecksnew.setIsproofuploaded(true);
                    VendorCheckStatusMaster byId = vendorCheckStatusMasterRepository
                            .getById(Long.valueOf(vendorcheckdashbordtDto.getStatus()));
                    if (byId != null) {
                        vendorChecksnew.setVendorCheckStatusMaster(byId);
                    }
                    vendorChecksRepository.save(vendorChecksnew);
                    liCheckToPerformService.UpdateBGVCheckStatusRowwise(vendorChecksString, proofDocumentNew, "ONLINE");
                    svcSearchResult.setMessage("vendorchecks document update successfully.");
                } else {
                    System.out.println("-------------candidate-----else------");
                    svcSearchResult.setData(null);
                    svcSearchResult.setOutcome(false);
                }
            }
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
            if (vendorcheckdashbordtDto.getLegalProcedings() != null) {
                log.info("vendorupload check id" + result.getVendoruploadcheckId());
                Optional<ConventionalVendorliChecksToPerform> findById = liCheckToPerformRepository
                        .findById(vendorCheckss.getLicheckId());
                if (vendorcheckdashbordtDto.getLegalProcedings().getCivilProceedingsList() != null) {
                    List<CivilProceedingsDTO> civilProceedings = vendorcheckdashbordtDto.getLegalProcedings()
                            .getCivilProceedingsList();
                    for (CivilProceedingsDTO civilProceeding : civilProceedings) {
                        // Find existing CriminalCheck by vendor upload check ID, proceedings type, and court
                        CriminalCheck criminalproceeding = criminalCheckRepository.findByVendorUploadCheckIdAndProceedingsTypeAndCourt(
                                result.getVendoruploadcheckId(), "CIVILPROCEDING", civilProceeding.getCourt());


                        // If a matching CriminalCheck is found
                        if (criminalproceeding != null) {
                            criminalproceeding.setCheckUniqueId(String.valueOf(findById.get().getCheckUniqueId()));
                            criminalproceeding.setRequestId(findById.get().getRequestId());
                            criminalproceeding.setCreatedOn(new Date());
                            criminalproceeding.setProceedingsType("CIVILPROCEDING");
                            criminalproceeding.setJurisdiction(civilProceeding.getJurisdiction());
                            criminalproceeding.setNameOfTheCourt(civilProceeding.getNameOfTheCourt());
                            criminalproceeding.setResult(civilProceeding.getResult());
                            criminalproceeding.setCourt(civilProceeding.getCourt());
                            
                            
                            Date date = inputFormat.parse(civilProceeding.getDateOfSearch());
							String outputDateString = outputFormat.format(date);

                            criminalproceeding.setDateOfSearch(outputDateString);
                            criminalproceeding.setVendorUploadCheckId(result.getVendoruploadcheckId());

                            
                            criminalChecks.add(criminalproceeding);
                        } else {
                            CriminalCheck criminalCheckForCivilProcedings = new CriminalCheck();
                            criminalCheckForCivilProcedings.setCheckUniqueId(String.valueOf(findById.get().getCheckUniqueId()));
                            criminalCheckForCivilProcedings.setRequestId(findById.get().getRequestId());
                            criminalCheckForCivilProcedings.setCreatedOn(new Date());
                            criminalCheckForCivilProcedings.setProceedingsType("CIVILPROCEDING");
                            criminalCheckForCivilProcedings.setJurisdiction(civilProceeding.getJurisdiction());
                            criminalCheckForCivilProcedings.setNameOfTheCourt(civilProceeding.getNameOfTheCourt());
                            criminalCheckForCivilProcedings.setResult(civilProceeding.getResult());
                            criminalCheckForCivilProcedings.setCourt(civilProceeding.getCourt());
                            
                            Date date = inputFormat.parse(civilProceeding.getDateOfSearch());
							String outputDateString = outputFormat.format(date);

                            criminalCheckForCivilProcedings.setDateOfSearch(outputDateString);
                            criminalCheckForCivilProcedings.setVendorUploadCheckId(result.getVendoruploadcheckId());
                            criminalChecks.add(criminalCheckForCivilProcedings);
                        }
                    }
                }
                if (vendorcheckdashbordtDto.getLegalProcedings().getCriminalProceedingsList() != null) {
                    List<CriminalProceedingsDTO> criminalProceedings = vendorcheckdashbordtDto.getLegalProcedings()
                            .getCriminalProceedingsList();
                    
                    for (CriminalProceedingsDTO criminalProceeding : criminalProceedings) {
                        CriminalCheck criminalproceding = criminalCheckRepository.findByVendorUploadCheckIdAndProceedingsTypeAndCourt(
                                result.getVendoruploadcheckId(), "CRIMINALPROCEDING", criminalProceeding.getCourt());

                        if (criminalproceding != null) {
                            criminalproceding.setCheckUniqueId(String.valueOf(findById.get().getCheckUniqueId()));
                            criminalproceding.setRequestId(findById.get().getRequestId());
                            criminalproceding.setCreatedOn(new Date());
                            criminalproceding.setProceedingsType("CRIMINALPROCEDING");
                            criminalproceding.setJurisdiction(criminalProceeding.getJurisdiction());
                            criminalproceding.setNameOfTheCourt(criminalProceeding.getNameOfTheCourt());
                            criminalproceding.setResult(criminalProceeding.getResult());
                            criminalproceding.setCourt(criminalProceeding.getCourt());
                            
                            Date date = inputFormat.parse(criminalProceeding.getDateOfSearch());
							String outputDateString = outputFormat.format(date);
                            
                            criminalproceding.setDateOfSearch(outputDateString);
                            criminalproceding.setVendorUploadCheckId(result.getVendoruploadcheckId());
                            criminalChecks.add(criminalproceding);
                        } else {
                            CriminalCheck criminalCheckForCriminalProceding = new CriminalCheck();
                            criminalCheckForCriminalProceding.setCheckUniqueId(String.valueOf(findById.get().getCheckUniqueId()));
                            criminalCheckForCriminalProceding.setRequestId(findById.get().getRequestId());
                            criminalCheckForCriminalProceding.setCreatedOn(new Date());
                            criminalCheckForCriminalProceding.setProceedingsType("CRIMINALPROCEDING");
                            criminalCheckForCriminalProceding.setJurisdiction(criminalProceeding.getJurisdiction());
                            criminalCheckForCriminalProceding.setNameOfTheCourt(criminalProceeding.getNameOfTheCourt());
                            criminalCheckForCriminalProceding.setResult(criminalProceeding.getResult());
                            criminalCheckForCriminalProceding.setCourt(criminalProceeding.getCourt());
                            Date date = inputFormat.parse(criminalProceeding.getDateOfSearch());
							String outputDateString = outputFormat.format(date);
                            criminalCheckForCriminalProceding.setDateOfSearch(outputDateString);
                            criminalCheckForCriminalProceding.setVendorUploadCheckId(result.getVendoruploadcheckId());
                            criminalChecks.add(criminalCheckForCriminalProceding);
                        }
                    }
                }
                log.info("Number of criminal checks to save: " + criminalChecks.size());
                List<CriminalCheck> criminalChecks1 = criminalCheckRepository.saveAll(criminalChecks);
                log.info("saved criminal checks" + criminalChecks1);
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
    @Transactional(rollbackFor = Exception.class)
    public ServiceOutcome<VendorChecks> saveInitiateVendorChecks(String vendorChecksString,
                                                                 MultipartFile proofDocumentNew, String documentUrl) throws IOException {
        ServiceOutcome<VendorChecks> svcSearchResult = new ServiceOutcome<VendorChecks>();
        try {
            Candidate Candidatelist = null;
            Long vendorid = null;
            CandidateCaseDetails result = null;
            VendorInitiatDto vendorInitiatDto = new ObjectMapper().readValue(vendorChecksString,
                    VendorInitiatDto.class);
            byte[] candidateDocument = new byte[0];
            VendorCheckStatusMaster byVendorCheckStatusMasterId = vendorCheckStatusMasterRepository.findByVendorCheckStatusMasterId(vendorInitiatDto.getVendorCheckStatusMasterId());
            if (vendorInitiatDto.getDocumentUrl() != null) {
                S3Object object = s3Client.getObject(DIGIVERIFIER_DOC_BUCKET_NAME, vendorInitiatDto.getDocumentUrl());
                InputStream inputStream = object.getObjectContent();
                candidateDocument = StreamUtils.copyToByteArray(inputStream);
                String output = documentUrl.replaceAll("\"", "");
                documentUrl = output;
            }
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser()
                    : userRepository.findByUserId(53l);
//            System.out.println("user = " + user);
            VendorChecks vendorChecksobj = null;
            VendorChecks savedVendorChecks = null;
            Source source = sourceRepository.findById(vendorInitiatDto.getSourceId()).get();
            Candidate byConventionalRequestId = candidateRepository
                    .findByConventionalRequestId(vendorInitiatDto.getCandidateId());
            if (vendorInitiatDto.getDocumentname() != null
                    && vendorInitiatDto.getDocumentname().equalsIgnoreCase("NA") == false) {
                vendorChecksobj = vendorChecksRepository
                        .findByCandidateCandidateIdAndVendorIdAndSourceSourceIdAndDocumentnameAndLicheckId(
                                byConventionalRequestId.getCandidateId(), vendorInitiatDto.getVendorId(),
                                vendorInitiatDto.getSourceId(), vendorInitiatDto.getDocumentname(),
                                Long.valueOf(vendorInitiatDto.getLicheckId()));
                if (vendorChecksobj != null) {
                    if (vendorInitiatDto.getDocumentUrl() != null) {
                        vendorChecksobj.setAgentUploadedDocument(candidateDocument);
                    }
                    if (documentUrl != null) {
                        vendorChecksobj.setPathKey(documentUrl);
                    }
                    if (vendorInitiatDto.getDocumentname() != null) {
                        vendorChecksobj.setDocumentname(vendorInitiatDto.getDocumentname());
                    } // candidate id means request id
                    Candidate byCandidateId = candidateRepository
                            .findByConventionalRequestId(vendorInitiatDto.getCandidateId());
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository
                            .findByRequestId(String.valueOf(byCandidateId.getConventionalRequestId()));
                    vendorChecksobj.setCandidateName(conventionalVendorCandidatesSubmitted.getName());
                    vendorChecksobj.setCreatedBy(user);
                    if (vendorInitiatDto.getLicheckId() != null) {
                        vendorChecksobj.setLicheckId(Long.valueOf(vendorInitiatDto.getLicheckId()));
                    }
                    vendorChecksobj.setDateOfBirth(vendorInitiatDto.getDateOfBirth());
                    vendorChecksobj.setContactNo(vendorInitiatDto.getContactNo());
                    vendorChecksobj.setFatherName(vendorInitiatDto.getFatherName());
                    vendorChecksobj.setAddress(vendorInitiatDto.getAddress());
                    vendorChecksobj.setAlternateContactNo(vendorInitiatDto.getAlternateContactNo());
                    vendorChecksobj.setTypeOfPanel(vendorInitiatDto.getTypeOfPanel());
                    vendorChecksobj.setCreatedOn(new Date());
                    if(byVendorCheckStatusMasterId!=null) {
                    	vendorChecksobj.setVendorCheckStatusMaster(byVendorCheckStatusMasterId);
                    }
                    savedVendorChecks = vendorChecksRepository.save(vendorChecksobj);
                    svcSearchResult.setMessage("vendor Checks document update successfully.");
//                    svcSearchResult.setData(vendorChecksobj);
                    svcSearchResult.setStatus(String.valueOf(savedVendorChecks.getVendorcheckId()));
                } else {
                    VendorChecks vendorChecks = new VendorChecks();
                    // candidate id means request id
                    Candidate byCandidateId = candidateRepository
                            .findByConventionalRequestId(vendorInitiatDto.getCandidateId());
                    vendorChecks.setCandidate(byCandidateId);
                    if (vendorInitiatDto.getDocumentUrl() != null) {
                        vendorChecks.setAgentUploadedDocument(candidateDocument);
                    }
                    if (documentUrl != null) {
                        vendorChecks.setPathKey(documentUrl);
                        vendorChecks.setDocumentname(vendorInitiatDto.getDocumentname());
                    }
                    ConventionalVendorCandidatesSubmitted conventionalVendorCandidatesSubmitted = conventionalCandidatesSubmittedRepository
                            .findByRequestId(String.valueOf(byCandidateId.getConventionalRequestId()));
                    vendorChecks.setCandidateName(conventionalVendorCandidatesSubmitted.getName());
                    vendorChecks.setVendorId(vendorInitiatDto.getVendorId());
                    vendorChecks.setSource(sourceRepository.findById(vendorInitiatDto.getSourceId()).get());
                    vendorChecks.setDateOfBirth(vendorInitiatDto.getDateOfBirth());
                    vendorChecks.setCreatedBy(user);
                    vendorChecks.setContactNo(vendorInitiatDto.getContactNo());
                    vendorChecks.setFatherName(vendorInitiatDto.getFatherName());
                    vendorChecks.setAddress(vendorInitiatDto.getAddress());
                    vendorChecks.setAlternateContactNo(vendorInitiatDto.getAlternateContactNo());
                    vendorChecks.setTypeOfPanel(vendorInitiatDto.getTypeOfPanel());
                    vendorChecks.setCreatedOn(new Date());
                    if (vendorInitiatDto.getLicheckId() != null) {
                        vendorChecks.setLicheckId(Long.valueOf(vendorInitiatDto.getLicheckId()));
                    }
                    if(byVendorCheckStatusMasterId!=null) {
                    	vendorChecks.setVendorCheckStatusMaster(byVendorCheckStatusMasterId);
                    }
                    savedVendorChecks = vendorChecksRepository.save(vendorChecks);
//                    svcSearchResult.setData(save);
                    svcSearchResult.setMessage("vendor Checks document saved successfully.");
                    svcSearchResult.setStatus(String.valueOf(savedVendorChecks.getVendorcheckId()));
                }

            } else {
                vendorChecksobj = vendorChecksRepository
                        .findByCandidateCandidateIdAndVendorIdAndSourceSourceIdAndDocumentnameAndLicheckId(
                                byConventionalRequestId.getCandidateId(), vendorInitiatDto.getVendorId(),
                                vendorInitiatDto.getSourceId(), vendorInitiatDto.getDocumentname(),
                                Long.valueOf(vendorInitiatDto.getLicheckId()));
                if (vendorChecksobj != null) {
                    if (vendorInitiatDto.getDocumentUrl() != null) {
                        vendorChecksobj.setAgentUploadedDocument(candidateDocument);
                    }
//                    else {
//                        vendorChecksobj.setAgentUploadedDocument(proofDocumentNew != null ? proofDocumentNew.getBytes() : null);
//                    }

                    if (documentUrl != null) {
                        vendorChecksobj.setPathKey(documentUrl);
                        vendorChecksobj.setDocumentname(vendorInitiatDto.getDocumentname());
                    }
                    vendorChecksobj.setCreatedBy(user);
                    vendorChecksobj.setDateOfBirth(vendorInitiatDto.getDateOfBirth());
                    vendorChecksobj.setContactNo(vendorInitiatDto.getContactNo());
                    vendorChecksobj.setFatherName(vendorInitiatDto.getFatherName());
                    vendorChecksobj.setAddress(vendorInitiatDto.getAddress());
                    vendorChecksobj.setAlternateContactNo(vendorInitiatDto.getAlternateContactNo());
                    vendorChecksobj.setTypeOfPanel(vendorInitiatDto.getTypeOfPanel());
                    vendorChecksobj.setCreatedOn(new Date());
                    vendorChecksobj.setDocumentname(vendorInitiatDto.getDocumentname());
                    if (vendorInitiatDto.getLicheckId() != null) {
                        vendorChecksobj.setLicheckId(Long.valueOf(vendorInitiatDto.getLicheckId()));
                    }
                    if(byVendorCheckStatusMasterId!=null) {
                    	vendorChecksobj.setVendorCheckStatusMaster(byVendorCheckStatusMasterId);
                    }
                    savedVendorChecks = vendorChecksRepository.save(vendorChecksobj);
//                    svcSearchResult.setData(save);
                    svcSearchResult.setMessage("vendor Checks  update successfully.");
                    svcSearchResult.setStatus(String.valueOf(savedVendorChecks.getVendorcheckId()));
                } else {

                    VendorChecks vendorChecks = new VendorChecks();
                    // candidate id means request id
                    vendorChecks.setCandidate(
                            candidateRepository.findByConventionalRequestId(vendorInitiatDto.getCandidateId()));
                    vendorChecks.setVendorId(vendorInitiatDto.getVendorId());
                    vendorChecks.setSource(sourceRepository.findById(vendorInitiatDto.getSourceId()).get());
                    if (vendorInitiatDto.getDocumentname() != null) {
                        vendorChecks.setDocumentname(vendorInitiatDto.getDocumentname());
                    }
                    if (vendorInitiatDto.getDocumentUrl() != null) {
                        vendorChecks.setAgentUploadedDocument(candidateDocument);
                    }
//                    else {
//                        vendorChecksobj.setAgentUploadedDocument(proofDocumentNew != null ? proofDocumentNew.getBytes() : null);
//                    }
                    vendorChecks.setCreatedBy(user);
                    vendorChecks.setCandidateName(vendorInitiatDto.getCandidateName());
                    vendorChecks.setDateOfBirth(vendorInitiatDto.getDateOfBirth());
                    vendorChecks.setContactNo(vendorInitiatDto.getContactNo());
                    vendorChecks.setFatherName(vendorInitiatDto.getFatherName());
                    vendorChecks.setAddress(vendorInitiatDto.getAddress());
                    vendorChecks.setAlternateContactNo(vendorInitiatDto.getAlternateContactNo());
                    vendorChecks.setTypeOfPanel(vendorInitiatDto.getTypeOfPanel());
                    vendorChecks.setCreatedOn(new Date());
                    if (vendorInitiatDto.getLicheckId() != null) {
                        vendorChecks.setLicheckId(Long.valueOf(vendorInitiatDto.getLicheckId()));
                    }
                    if(byVendorCheckStatusMasterId!=null) {
                    	vendorChecks.setVendorCheckStatusMaster(byVendorCheckStatusMasterId);
                    }
                    savedVendorChecks = vendorChecksRepository.save(vendorChecks);
//                    svcSearchResult.setData(save);
                    svcSearchResult.setMessage("vendor Checks saved successfully.");
                    svcSearchResult.setStatus(String.valueOf(savedVendorChecks.getVendorcheckId()));
                }

            }
            log.info("***************Saved vendorcheck********************" + savedVendorChecks);
            FetchVendorConventionalCandidateDto fetchVendorConventionalCandidateDto = new FetchVendorConventionalCandidateDto();
            fetchVendorConventionalCandidateDto.setRequestId(String.valueOf(vendorInitiatDto.getCandidateId()));
            fetchVendorConventionalCandidateDto.setSourceId(vendorInitiatDto.getSourceId());
            fetchVendorConventionalCandidateDto.setVendorId(String.valueOf(vendorInitiatDto.getVendorId()));
            fetchVendorConventionalCandidateDto.setLicheckId(Long.valueOf(vendorInitiatDto.getLicheckId()));
            fetchVendorConventionalCandidateDto.setSourceName(vendorInitiatDto.getSourceName());
            fetchVendorConventionalCandidateDto.setVendorName(vendorInitiatDto.getVendorName());
            liCheckToPerformService.addUpdateLiCheckToPerformData(fetchVendorConventionalCandidateDto);
            ServiceOutcome<String> updateLicheckWithVendorCheck = liCheckToPerformService
                    .findUpdateLicheckWithVendorCheck(savedVendorChecks.getVendorcheckId(),
                            savedVendorChecks.getLicheckId());
            if (vendorInitiatDto.getInsufficiencyRemarks() != null) {
                VendorcheckdashbordtDto vendorcheckdashbordtDto = new VendorcheckdashbordtDto();
                vendorcheckdashbordtDto.setVendorcheckId(savedVendorChecks.getVendorcheckId());
                vendorcheckdashbordtDto.setStatus("3");
                vendorcheckdashbordtDto.setRemarks(vendorInitiatDto.getInsufficiencyRemarks());
                vendorcheckdashbordtDto.setColorid(2l);
                String vendorUploadData = new ObjectMapper().writeValueAsString(vendorcheckdashbordtDto);
                saveproofuploadVendorChecks(vendorUploadData, null, null);
            }
        } catch (Exception ex) {
            log.error("Exception occured in saveInitiateVendorChecks method in userServiceImpl-->" + ex);
        }
        return svcSearchResult;
    }

//    public String uploadVendorRemarksForChecks(Long vendorCheckId, String vendorRemarksJson) {
//        try {
//
//        } catch (Exception e) {
//
//        }
//
//    }
//
//    private boolean matchesSearch(VendorChecksDto dto, String searchText) {
//        searchText = searchText.toLowerCase(); // Convert to lowercase for case-insensitive search
//
//        return
//                dto.getCheckUniqueId().toString().toLowerCase().contains(searchText) ||
//                        dto.getDocumentname().toLowerCase().contains(searchText) ||
//                        dto.getCandidateName().toLowerCase().contains(searchText) ||
//                        dto.getSource().getSourceName().toLowerCase().contains(searchText) ||
//                        dto.getCreatedBy().getUserName().toLowerCase().contains(searchText) ||
//                        dto.getModeOfVerificationPerformed().toLowerCase().contains(searchText) ||
//                        dto.getVendorCheckStatusMaster().getCheckStatusCode().toLowerCase().contains(searchText) ||
//                        dto.getCheckUniqueId().toLowerCase().contains(searchText);
//    }

    public ServiceOutcome<List<VendorChecksDto>> searchAllVendorData(String searchString) {
        ServiceOutcome<List<VendorChecksDto>> svcSearchResult = new ServiceOutcome<>();
        List<VendorChecks> vendorList = new ArrayList<>();
        List<VendorChecksDto> vendorChecksDtos = new ArrayList<>();
        Optional<VendorCheckStatusMaster> matchedCheckStatusStatus = null;
        Optional<Source> matchedSource = null;
        Optional<ModeOfVerificationStatusMaster> matchedMode = null;
        Optional<ConventionalVendorCandidatesSubmitted> matchedCandidate = null;
        try {
            User user = (SecurityHelper.getCurrentUser() != null) ? SecurityHelper.getCurrentUser()
                    : userRepository.findByUserId(53l);
            Long userId = user.getUserId();
            Date createdOnDate = user.getCreatedOn();
            log.info("CreatedOnDate {}", createdOnDate);
            Date date = new Date();
            log.info("Current Date: {}", date);
            List<VendorCheckStatusMaster> all = vendorCheckStatusMasterRepository.findAll();
            List<Source> allSources = sourceRepository.findAll();
            List<ModeOfVerificationStatusMaster> modeAll = modeOfVerificationStatusMasterRepository.findAll();
            List<ConventionalVendorCandidatesSubmitted> candidateAll = conventionalCandidatesSubmittedRepository
                    .findAll();
            String updateSearchString = searchString.replaceAll("[^a-zA-Z0-9]", "");
            matchedCheckStatusStatus = all.stream().filter(statusMaster -> statusMaster.getCheckStatusCode()
                    .contains(searchString.trim().replaceAll("\\s+", "").toUpperCase())).findFirst();
            matchedSource = allSources.stream().filter(source -> {
                return source.getSourceName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
                        .contains(updateSearchString.trim().replaceAll("\\s+", "").toLowerCase());
            }).findFirst();
            matchedMode = modeAll.stream().filter(mode -> mode.getModeOfVerification()
                    .contains(searchString.trim().replaceAll("\\s+", "").toUpperCase())).findFirst();
            if (matchedCandidate == null || matchedCandidate.isEmpty() == true) {
                matchedCandidate = candidateAll.stream().filter(canddiate -> {
                    return canddiate.getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
                            .contains(searchString.trim().replaceAll("\\s+", "").toLowerCase());
                }).findFirst();
            }
            if (matchedCandidate == null || matchedCandidate.isEmpty() == true) {
                matchedCandidate = candidateAll.stream().filter(canddiate -> {
                    return canddiate.getRequestId().replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
                            .contains(searchString.trim().replaceAll("\\s+", "").toLowerCase());
                }).findFirst();
            }
            if (matchedCandidate == null || matchedCandidate.isEmpty() == true) {
                matchedCandidate = candidateAll.stream().filter(canddiate -> {
                    return canddiate.getCandidateId().toString().replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
                            .contains(searchString.trim().replaceAll("\\s+", "").toLowerCase());
                }).findFirst();
            }
            if (matchedSource.isPresent()) {
                vendorList = vendorChecksRepository.searchAllVendorCheckFilterForSource(user.getUserId(),
                        String.valueOf(matchedSource.get().getSourceId()), createdOnDate, date);
            } else if (matchedCheckStatusStatus.isPresent()) {
                vendorList = vendorChecksRepository.searchAllVendorCheckFilterForCheckStatus(user.getUserId(),
                        String.valueOf(matchedCheckStatusStatus.get().getVendorCheckStatusMasterId()), createdOnDate,
                        date);
            } else if (matchedMode.isPresent()) {
                vendorList = vendorChecksRepository.searchAllVendorCheckFilterForModeOfVerification(user.getUserId(),
                        String.valueOf(matchedMode.get().getModeTypeCode()), createdOnDate, date);
            } else if (matchedCandidate.isPresent()) {
                Candidate byConventionalRequestId = candidateRepository
                        .findByConventionalRequestId(Long.valueOf(matchedCandidate.get().getRequestId()));
                vendorList = vendorChecksRepository.searchAllVendorCheckFilterByCandidateId(user.getUserId(),
                        String.valueOf(byConventionalRequestId.getCandidateId()), createdOnDate, date);
            } else {
                log.info("else");
                vendorList = vendorChecksRepository.searchAllVendorCheckFilter(user.getUserId(), searchString,
                        createdOnDate, date);
            }
            vendorList.forEach(vc -> {
                VendorChecksDto vendorChecksDto = new VendorChecksDto();
                vendorChecksDto.setAddress(vc.getAddress());
                vendorChecksDto.setAgentUploadedDocument(vc.getAgentUploadedDocument());
                vendorChecksDto.setAlternateContactNo(vc.getAlternateContactNo());
                vendorChecksDto.setCandidate(vc.getCandidate());
                vendorChecksDto.setCandidateName(vc.getCandidateName());
                ConventionalVendorliChecksToPerform byVendorChecksVendorcheckId = liCheckToPerformRepository
                        .findByVendorChecksVendorcheckId(vc.getVendorcheckId());
                if (byVendorChecksVendorcheckId != null) {
                    vendorChecksDto.setCheckUniqueId(String.valueOf(byVendorChecksVendorcheckId.getCheckUniqueId()));
                    vendorChecksDto.setVendorCheckStatusMaster(byVendorChecksVendorcheckId.getCheckStatus());
                    ModeOfVerificationStatusMaster modeOfVerificationStatusMaster = modeOfVerificationStatusMasterRepository
                            .findById(Long.valueOf(byVendorChecksVendorcheckId.getModeOfVerificationRequired())).get();
                    vendorChecksDto
                            .setModeOfVerificationPerformed(modeOfVerificationStatusMaster.getModeOfVerification());
                    if (byVendorChecksVendorcheckId.getStopCheck() != null) {
                        vendorChecksDto.setStopCheckStatus(byVendorChecksVendorcheckId.getStopCheck());
                    }
                    if (byVendorChecksVendorcheckId.getDisabled() != null) {
                        vendorChecksDto.setDisableStatus(byVendorChecksVendorcheckId.getDisabled());
                    }
                }
                vendorChecksDto.setContactNo(vc.getContactNo());
                vendorChecksDto.setCreatedBy(vc.getCreatedBy());
                vendorChecksDto.setCreatedOn(vc.getCreatedOn());
                vendorChecksDto.setDateOfBirth(vc.getDateOfBirth());
                vendorChecksDto.setDocumentname(vc.getDocumentname());
                vendorChecksDto.setEmailId(vc.getEmailId());
                vendorChecksDto.setExpireson(vc.getExpireson());
                vendorChecksDto.setFatherName(vc.getFatherName());
                vendorChecksDto.setIsproofuploaded(vc.getIsproofuploaded());
                vendorChecksDto.setPathKey(vc.getPathKey());
                vendorChecksDto.setSource(vc.getSource());
                vendorChecksDto.setTat(vc.getTat());
                vendorChecksDto.setTypeOfPanel(vc.getTypeOfPanel());
                vendorChecksDto.setVendorcheckId(vc.getVendorcheckId());
                vendorChecksDto.setVendorId(vc.getVendorId());
                VendorUploadChecks vendoruploadChecks = vendorUploadChecksRepository
                        .findByVendorChecksVendorcheckId(vc.getVendorcheckId());
                if (vendoruploadChecks != null) {
                    vendorChecksDto.setVendorUplodedDocument(vendoruploadChecks.getVendorUploadedDocument());
                }
                vendorChecksDtos.add(vendorChecksDto);
            });
            List<VendorChecksDto> collect = vendorChecksDtos.stream().filter(vc -> vc.getCheckUniqueId() != null)
                    .collect(Collectors.toList());
            if (!vendorList.isEmpty()) {
                svcSearchResult.setData(collect);
                svcSearchResult.setOutcome(true);
                svcSearchResult.setMessage("SUCCESS");
            } else {
                svcSearchResult.setData(new ArrayList<>());
                svcSearchResult.setOutcome(false);
                svcSearchResult.setMessage("NO VENDORCHECKS FOUND");
            }
//            log.info(vendorList.toString());
        } catch (Exception e) {
            log.error("inside search data" + e.getMessage());
        }
        return svcSearchResult;
    }

}