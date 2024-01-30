package com.aashdit.digiverifier.config.candidate.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import com.aashdit.digiverifier.config.admin.model.User;
import com.aashdit.digiverifier.config.superadmin.model.Organization;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name="t_dgv_candidate_basic")
public class Candidate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6486326677138491369L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "candidate_id")
	private Long candidateId;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;
	
	@Column(name = "candidate_name")
	private String candidateName;
	
	@Column(name = "candidate_code")
	private String candidateCode;
	
	@Column(name = "email_id")
	private String emailId;
	
	@Column(name = "contact_number")
	private String contactNumber;
	
	@Column(name = "date_of_birth")
	private String dateOfBirth;
	
	@Column(name = "pan_number")
	private String panNumber;

	@Column(name = "pan_name")
	private String panName;

	@Column(name = "pan_dob")
	private String panDob;
	
	@Column(name = "aadhar_number")
	private String aadharNumber;
	
	@Column(name = "aadhar_name")
	private String aadharName;
	
	@Column(name = "aadhar_father_name")
	private String aadharFatherName;
	
	@Column(name = "aadhar_dob")
	private String aadharDob;

	@Column(name = "aadhar_gender")
	private String aadharGender;
	
	@Column(name = "experience_in_month")
	private Float experienceInMonth;
	
	@Column(name = "cc_email_id")
	private String ccEmailId;
	
	@Column(name = "applicant_id")
	private String applicantId;
	
	@Column(name = "submitted_on")
	private Date submittedOn;
	
	@ManyToOne
	@JoinColumn(name = "created_by")
	private User createdBy;
	
	@Column(name = "created_on")
	private Date createdOn;
	
	@ManyToOne
	@JoinColumn(name = "last_updated_by")
	private User lastUpdatedBy;

	@Column(name = "last_updated_on")
	private Date lastUpdatedOn;
	
	@Column(name = "approval_required")
	private boolean approvalRequired;
	
	@Column(name = "is_fresher")
	private Boolean isFresher;
	
	@Column(name = "is_loa_accepted")
	private Boolean isLoaAccepted=false;
	
	@Column(name = "is_uan_skipped")
	private Boolean isUanSkipped;
	
	@Column(name = "is_active")
	private Boolean isActive;
	
	@Column(name = "uan")
	private String uan;

	@Column(name = "uan_name")
	private String uanName;

	@Column(name = "uan_dob")
	private String uanDob;
	
	@ManyToOne
	@JoinColumn(name = "candidate_sample_id")
	private CandidateSampleCsvXlsMaster candidateSampleId;
	
	@Column(name = "itr_pan_number")
	private String itrPanNumber;
	
	@Column(name = "show_validation")
    private Boolean showvalidation;
	
	@Column(name = "account_name")
	private String accountName;
	
	@Column(name = "candidate_upload_filename")
	private String candidateUploadFileName;
	
	@Column(name = "purged_on")
	private Date purgedOn;
	
	
//	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	private CandidateVerificationState candidateVerificationState;
	
	public Candidate(String candidateName, String emailId, String contactNumber, float experienceInMonth,
			String ccEmailId, String applicantId,String itrPanNumber, String uan) {
		super();
		this.candidateName = candidateName;
		this.emailId = emailId;
		this.contactNumber = contactNumber;
		this.experienceInMonth = experienceInMonth;
		this.ccEmailId = ccEmailId;
		this.applicantId = applicantId;
		this.itrPanNumber = itrPanNumber;
		this.uan = uan;
	}

	
}
