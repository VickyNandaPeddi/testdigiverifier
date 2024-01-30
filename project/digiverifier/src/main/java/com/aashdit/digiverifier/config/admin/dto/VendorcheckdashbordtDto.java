package com.aashdit.digiverifier.config.admin.dto;
import com.aashdit.digiverifier.config.superadmin.model.VendorMasterNew;
import com.aashdit.digiverifier.config.admin.model.VendorChecks;
import com.aashdit.digiverifier.config.candidate.model.Candidate;
import com.aashdit.digiverifier.config.superadmin.model.VendorCheckStatusMaster;
import com.aashdit.digiverifier.config.admin.model.VendorUploadChecks;


import lombok.Data;
import java.util.List;

@Data
public class VendorcheckdashbordtDto {


	private String documentname;


    private Long colorid;

	private Long vendorcheckId;

	private Long vendorCheckStatusMasterId;
	
	private String value;
	
	private List<VendorCheckStatusAndCountDTO> vendorCheckStatusAndCount;
	
	private String fromDate;

	private String toDate;
	
	private Long userId;
	
	private Long vendorId;

	private Boolean stopCheck;
	
	

	// private Long VendorCheckStatusId;



	// private byte proofDocumentNew;


	// String candidateName;
	// String userName;
	// String emailId;
	// String sourceName;
	// String proofuploaded;
	// String lastUpdatedBy;
	// Boolean isActive;
	// Long vendorcheckId;
	// Boolean expireson;
	// Double tat;
	// String createdBy;
	
public VendorcheckdashbordtDto() {
	// TODO Auto-generated constructor stub
}	
	
	
	public VendorcheckdashbordtDto(String documentname, Long colorid, Long vendorcheckId,
			Long vendorCheckStatusMasterId, String value, List<VendorCheckStatusAndCountDTO> vendorCheckStatusAndCount,
			String fromDate, String toDate, Long userId) {
		super();
		this.documentname = documentname;
		this.colorid = colorid;
		this.vendorcheckId = vendorcheckId;
		this.vendorCheckStatusMasterId = vendorCheckStatusMasterId;
		this.value = value;
		this.vendorCheckStatusAndCount = vendorCheckStatusAndCount;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.userId = userId;
	}

   
}
