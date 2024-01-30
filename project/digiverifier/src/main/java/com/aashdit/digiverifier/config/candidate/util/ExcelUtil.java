package com.aashdit.digiverifier.config.candidate.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import com.aashdit.digiverifier.config.admin.model.User;
import com.aashdit.digiverifier.config.admin.repository.UserRepository;
import com.aashdit.digiverifier.config.candidate.dto.BulkUanDTO;
import com.aashdit.digiverifier.config.candidate.model.Candidate;
import com.aashdit.digiverifier.config.candidate.model.SuspectClgMaster;
import com.aashdit.digiverifier.config.candidate.model.SuspectEmpMaster;
import com.aashdit.digiverifier.config.candidate.model.UanSearchData;
import com.aashdit.digiverifier.config.candidate.repository.CandidateRepository;
import com.aashdit.digiverifier.config.candidate.repository.UanSearchDataRepository;
import com.aashdit.digiverifier.config.superadmin.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.Date;
// import java.util.concurrent.ThreadLocalRandom; 
import com.aashdit.digiverifier.common.util.RandomString;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ExcelUtil {
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private CandidateRepository candidateRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UanSearchDataRepository uanSearchDataRepository;
	
	 public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	  public static boolean hasExcelFormat(MultipartFile file) {
	    if (!TYPE.equals(file.getContentType())) {
	      return false;
	    }
	    return true;
	  }
      
	  public  List<Candidate> excelToCandidate(InputStream is,String filename,String yearsToBeVerified) {
	        try {
	        	log.info("FILENAME::>>>"+filename);
	        	  if(yearsToBeVerified == null)
	        		  yearsToBeVerified = "7";
	              ArrayList<Candidate> candidateList = new ArrayList<Candidate>();
	              XSSFWorkbook workbook = new XSSFWorkbook(is);
	              XSSFSheet worksheet = workbook.getSheetAt(0);
	              
	              Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	              Object principal = authentication.getPrincipal();
	              log.info("principal:: {}",principal.toString());
	              String username = "";
	                  username = ((UserDetails) principal).getUsername();
	                  log.info("username 2::?? {}",username);
	                  User findByUserName = userRepository.findByUserName(username);
	                  log.info("ORgID::{}",findByUserName.getOrganization().getOrganizationName());	                  
	                  log.info(principal.toString());
	             
	                  if(findByUserName.getOrganization().getOrganizationName().equalsIgnoreCase("Accolite Digital India Pvt Ltd")){
		            	  log.info("accolite is true::");
		            	  try {
		            		  
			                  for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
			                      Candidate candidate = new Candidate();
			                      XSSFRow row = worksheet.getRow(i);
			                        XSSFRow header = worksheet.getRow(0);
			                        log.info("xls heading row {}", header.getLastCellNum());
			                        if (getCellValue(row, 0) != null && !getCellValue(row, 0).equals("") &&
				                    	    getCellValue(row, 1) != null && !getCellValue(row, 1).equals("")){
			                            candidate.setCandidateName(getCellValue(row, 0));
			                            candidate.setContactNumber(getCellValue(row, 1).trim());
				                        candidate.setEmailId(getCellValue(row, 2).trim());


			                            if(!getCellValue(row, 7).trim().isEmpty()|| getCellValue(row, 7).trim().equals("")) {

			                                candidate.setAccountName(null);
			                                log.info("AccountName is null= {}"+candidate.getAccountName());

			                            }
			                                if(getCellValue(row, 7).isEmpty()) {
			                                candidate.setAccountName(getCellValue(row, 6));
			                                candidate.setShowvalidation(false);

			                                 }

			                                if (getCellValue(row, 7) != null && getCellValue(row, 6) != null) {

			                                         log.info("getCEllValue::{}"+getCellValue(row, 6));
			                                        String cellValue = getCellValue(row, 6);
			                                        log.info("CELLVALUE:: {}"+cellValue);
			                                        if(cellValue.trim().equalsIgnoreCase("true")|| cellValue.trim().equalsIgnoreCase("TRUE()")) {
			                                        candidate.setShowvalidation(true);
			                                        log.info("True:::::: {}");
			                                        }
			                                        
			                                     if(cellValue.trim().equalsIgnoreCase("false") || cellValue.trim().equalsIgnoreCase("")
			                                    		 || cellValue==null || cellValue.trim().equalsIgnoreCase("FALSE()")) {
			                                                candidate.setShowvalidation(false);
			                                                log.info("False::::::");
			                                         }
			                                                candidate.setAccountName(getCellValue(row, 7));

			                                    
			                                }

			                            SecureRandom secureRnd = new SecureRandom();
			                            int n = 100000 + secureRnd.nextInt(900000);

			                            if(header.getLastCellNum() == 7 && getCellValue(header, 3).equals("Applicant Id")) {
			                                if(!getCellValue(row, 3).equals("")) {
			                                    candidate.setApplicantId(getCellValue(row, 3));
			                                }
			                                else {
			                                    candidate.setApplicantId(String.valueOf(n));
			                                }

			                                //candidate.setExperienceInMonth(!getCellValue(row, 4).equals("")?Integer.valueOf(getCellValue(row, 4)):null);
			                                candidate.setExperienceInMonth(!getCellValue(row, 4).equals("") ? Float.parseFloat(getCellValue(row, 4)) : Float.valueOf(yearsToBeVerified));
				                            candidate.setCcEmailId(getCellValue(row, 5));
			                            } else {

			                                candidate.setApplicantId(String.valueOf(n));
			                               // candidate.setExperienceInMonth(!getCellValue(row, 4).equals("")?Integer.valueOf(getCellValue(row, 4)):null);
			                                candidate.setExperienceInMonth(!getCellValue(row, 4).equals("") ? Float.parseFloat(getCellValue(row, 4)) : Float.valueOf(yearsToBeVerified));
				                            candidate.setCcEmailId(getCellValue(row, 5).trim());
			                            }
			                            if(getCellValue(row,8) != null || getCellValue(row, 9)!= null) {		                            
				                        	candidate.setItrPanNumber(getCellValue(row, 8));
				                        	candidate.setUan(getCellValue(row, 9));
				                        }

			                            candidateList.add(candidate);
			                        }
			                    }
							
						} catch (Exception e) {
							log.info("ExcelUtils:::"+e.getMessage());
						}

		              }        
	              
	              else {
	            	  
	            	  List<String> getFilenameFromCandidatebasic = candidateRepository.getFilename();
		              if (getFilenameFromCandidatebasic.contains(filename)) {
		                  log.info("Filename already exists: " + filename);
		              }
	            	  
		              else {
		            	  try {
				              for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
				                  Candidate candidate = new Candidate();
				                  candidate.setCandidateUploadFileName(filename);
				                  XSSFRow row = worksheet.getRow(i);
				                    XSSFRow header = worksheet.getRow(0);
					                  System.out.println("Pan NUMBER ::"+getCellValue(row, 8));
					                  System.out.println("Uan Number ::"+getCellValue(row, 9));
				                    log.info("xls heading row {}", header.getLastCellNum());
				                    if (getCellValue(row, 0) != null && !getCellValue(row, 0).equals("") &&
				                    	    getCellValue(row, 1) != null && !getCellValue(row, 1).equals("")){
				                        candidate.setCandidateName(getCellValue(row, 0));
				                        candidate.setContactNumber(getCellValue(row, 1).trim());
				                        candidate.setEmailId(getCellValue(row, 2).trim());



				                        if(!getCellValue(row, 7).trim().isEmpty()|| getCellValue(row, 7).trim().equals("")) {

				                            candidate.setAccountName(null);
				                            log.info("AccountName is null= "+candidate.getAccountName());

				                        }

				                            if(getCellValue(row, 7).isEmpty()) {
				                            candidate.setAccountName(getCellValue(row, 6));
				                            candidate.setShowvalidation(false);

				                             }

				                            if (getCellValue(row, 7) != null && getCellValue(row, 6) != null) {

				                                     log.info("getCEllValue::"+getCellValue(row, 6));
				                                    String cellValue = getCellValue(row, 6);
				                                    log.info("CELLVALUE::"+cellValue);
				                                    log.info("Formula: {}" , row.getCell(6).getCellType());
				                                    if(cellValue.trim().equalsIgnoreCase("true")|| cellValue.trim().equalsIgnoreCase("TRUE()")) {
				                                    candidate.setShowvalidation(true);
				                                    log.info("True::::::");
				                                    }
				                                    
				                                 if(cellValue.trim().equalsIgnoreCase("false") || cellValue.trim().equalsIgnoreCase("")
				                                		|| cellValue==null || cellValue.trim().equalsIgnoreCase("FALSE()")) {
				                                            candidate.setShowvalidation(false);
				                                            log.info("False::::::");
				                                     }
				                                            candidate.setAccountName(getCellValue(row, 7));  
				                            }

				                        SecureRandom secureRnd = new SecureRandom();
				                        int n = 100000 + secureRnd.nextInt(900000);

				                        if(header.getLastCellNum() == 7 && getCellValue(header, 3).equals("Applicant Id")) {
				                            if(!getCellValue(row, 3).equals("")) {
				                                candidate.setApplicantId(getCellValue(row, 3));
				                            }
				                            else {
				                                candidate.setApplicantId(String.valueOf(n));
				                            }

				                            //candidate.setExperienceInMonth(!getCellValue(row, 4).equals("")?Integer.valueOf(getCellValue(row, 4)):null);
				                            candidate.setExperienceInMonth(!getCellValue(row, 4).equals("") ? Float.parseFloat(getCellValue(row, 4)) : Float.valueOf(yearsToBeVerified));
				                            candidate.setCcEmailId(getCellValue(row, 5));

				                        } else {
				                            candidate.setApplicantId(String.valueOf(getCellValue(row, 3)));
				                            
				                        if (getCellValue(row, 3).equals("")) {
				                        	candidate.setApplicantId(String.valueOf(n));
				                        }
				                        //candidate.setExperienceInMonth(!getCellValue(row, 4).equals("")?Integer.valueOf(getCellValue(row, 4)):null);
				                        candidate.setExperienceInMonth(!getCellValue(row, 4).equals("") ? Float.parseFloat(getCellValue(row, 4)) : Float.valueOf(yearsToBeVerified));
				                        candidate.setCcEmailId(getCellValue(row, 5).trim());


				                        }
				                        
				                        if(getCellValue(row,8) != null || getCellValue(row, 9)!= null) {		                            
				                        	candidate.setItrPanNumber(getCellValue(row, 8));
				                        	candidate.setUan(getCellValue(row, 9));;
				                        }

				                        candidateList.add(candidate);
				                    }
				                }
						} catch (Exception e) {
							log.info("ExcelUtils:::"+e.getMessage());;
						}

		              }
	              }
	              
	              return candidateList;
	            }

	              catch (IOException e) {
	              throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
	            }
	      }
	  
	  private String getCellValue(Row row, int cellNo) {
		  String cellValue=null;
		  try {
	        DataFormatter formatter = new DataFormatter();
	        Cell cell = row.getCell(cellNo);
	        cellValue=formatter.formatCellValue(cell);
		  }
		  catch(Exception ex) {
			  log.error("Exception occured in getCellValue method in ExcelUtil-->"+ex);
		  }
		  return cellValue;
	  }

	public List<User> excelToUserList(InputStream inputStream) {
		 try {
	    	  ArrayList<User> userList = new ArrayList<User>();
	    	  XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
	          XSSFSheet worksheet = workbook.getSheetAt(0);
			  for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
				  	User user = new User();
		            XSSFRow row = worksheet.getRow(i);
		            if(!getCellValue(row, 0).equals("")){
		            	user.setEmployeeId(getCellValue(row, 0));
			            user.setUserFirstName(getCellValue(row, 1));
			            user.setUserLastName(getCellValue(row, 2));
			            user.setUserEmailId(getCellValue(row, 3));
			            user.setUserName(getCellValue(row, 3).trim());
			            user.setLocation(getCellValue(row, 4));
			            user.setUserMobileNum(getCellValue(row, 5));
			            user.setUserLandlineNum(getCellValue(row, 6));
			            user.setReportingEmailId(getCellValue(row, 7));
			            userList.add(user);
		            }
		            
		        }
		      return userList;
		    } catch (IOException e) {
		      throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		    }
	}

	public List<SuspectEmpMaster> excelToSuspectEmpMaster(InputStream inputStream,Long organizationId) {
		try {
	    	  ArrayList<SuspectEmpMaster> suspectEmpMasterList = new ArrayList<SuspectEmpMaster>();
	    	  XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
	          XSSFSheet worksheet = workbook.getSheetAt(0);
			  for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
				  SuspectEmpMaster suspectEmpMaster = new SuspectEmpMaster();
		            XSSFRow row = worksheet.getRow(i);
		            if(!getCellValue(row, 0).equals("")) {
						 
		            	suspectEmpMaster.setSuspectCompanyName(getCellValue(row, 0));
			            suspectEmpMaster.setAddress(getCellValue(row, 1));
						
			            suspectEmpMaster.setIsActive(true);
						suspectEmpMaster.setOrganization(organizationRepository.findById(organizationId).get());
						suspectEmpMaster.setCreatedOn(new Date());
						
			            suspectEmpMasterList.add(suspectEmpMaster);
						
		            }
		            
		        }
		      return suspectEmpMasterList;
		    } catch (IOException e) {
		      throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		   }
	}

	public List<SuspectClgMaster> excelToSuspectClgMaster(InputStream inputStream) {
		try {
	    	  ArrayList<SuspectClgMaster> suspectClgMasterList = new ArrayList<SuspectClgMaster>();
	    	  XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
	          XSSFSheet worksheet = workbook.getSheetAt(1);
			  for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
				  SuspectClgMaster suspectClgMaster = new SuspectClgMaster();
		            XSSFRow row = worksheet.getRow(i);
		            if(!getCellValue(row, 0).equals("")) {
		            	suspectClgMaster.setSuspectInstitutionName(getCellValue(row, 0));
			            suspectClgMaster.setAssociatedInstitution(getCellValue(row, 1));
			            suspectClgMaster.setAddress(getCellValue(row, 2));
			            suspectClgMaster.setSource(getCellValue(row, 3));
			            suspectClgMaster.setClassifiedAs(getCellValue(row, 4));
			            suspectClgMaster.setDateModified(getCellValue(row, 5));
			            suspectClgMaster.setVendor(getCellValue(row, 6));
			            suspectClgMaster.setIsActive(true);
			            suspectClgMasterList.add(suspectClgMaster);
		            }
		        }
		      return suspectClgMasterList;
		    } catch (IOException e) {
		      throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		   }
	}
	
	public List<BulkUanDTO> excelToBulkUanSearch(InputStream inputStream){
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            String username = "";
                username = ((UserDetails) principal).getUsername();
                User findByUserName = userRepository.findByUserName(username);
                String getUserForUploadedBy = findByUserName.getUserFirstName();
                
                int min = 100000; 
     	        int max = 999999;
     	       SecureRandom secureRandom = new SecureRandom();
     	        int randomNum = secureRandom.nextInt(max - min + 1) + min;
     	        String bulkUanId = Integer.toString(randomNum);
             
 	        Date currentDate = new Date();
            Date uploadedOn = currentDate;
			ArrayList<BulkUanDTO> bulkUanSearchList = new ArrayList<BulkUanDTO>();
	    	  XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
	          XSSFSheet worksheet = workbook.getSheetAt(0);
			  for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
				  BulkUanDTO uanSearchData = new BulkUanDTO();
		            XSSFRow row = worksheet.getRow(i);
				  if(!getCellValue(row, 0).equals("")) {
					  uanSearchData.setApplicantId(getCellValue(row, 0));
					  uanSearchData.setUan(getCellValue(row, 1));
					  uanSearchData.setUploadedBy(getUserForUploadedBy);
					  uanSearchData.setBulkUanId(bulkUanId);				  
					  
			          UanSearchData uanSave = new UanSearchData(); 
					  uanSave.setApplicantId(getCellValue(row, 0));
					  uanSave.setUan(getCellValue(row, 1));	
					  uanSave.setBulkUanId(bulkUanId);
					  uanSave.setEPFOResponse("Search In Progress...");
					  uanSave.setUploadedOn(uploadedOn);
					  uanSave.setUploadedBy(getUserForUploadedBy);
					  bulkUanSearchList.add(uanSearchData);
					  
				 	uanSearchDataRepository.save(uanSave);

				  }

			  }

			  return bulkUanSearchList;
		} catch (IOException e) {
		      throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
		}
				
	}
	
}
