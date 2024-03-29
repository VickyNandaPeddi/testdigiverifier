package com.aashdit.digiverifier.config.candidate.dto;

import lombok.Data;

@Data
public class ApprovalStatusRemarkDto {
	public Long id;

	public Long organizationid;

	private Long qualificationId;

	private String schoolOrCollegeName;

	private String boardOrUniversityName;

	private String yearOfPassing;

	private String percentage;

	private String CompanyName;

	private String DateofJoining;

	private String LastWorkingDay;

	private Long colorId;
	
	private Long remarkId;
	
	private String customRemark;

	private String candidateCode;

	private String candidateEmployerName;

    private String inputDateOfJoining;

    private String inputDateOfExit;

	private Boolean isPresentAddress;

	private Boolean isPermanentAddress;

	private Boolean isAssetDeliveryAddress;

	private Long candidateCafAddressId;
	
	private Boolean undisclosed;
	
	private String outputDateOfExit;
	
	private String outputDateOfExitProofName;
}
