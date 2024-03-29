package com.aashdit.digiverifier.config.candidate.model;

import java.beans.Transient;
import java.io.Serializable;
import java.sql.Types;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.springframework.web.multipart.MultipartFile;

import com.aashdit.digiverifier.config.admin.model.User;
import com.aashdit.digiverifier.config.superadmin.model.Color;
import com.aashdit.digiverifier.config.superadmin.model.ServiceMaster;
import com.aashdit.digiverifier.config.superadmin.model.ServiceSourceMaster;
import com.aashdit.digiverifier.config.superadmin.model.Source;

import lombok.Data;

@Data
@Entity
@Table(name="t_dgv_candidate_caf_education")
public class CandidateCafEducation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4386254957126811179L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "candidate_caf_education_id")
	private Long candidateCafEducationId;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "candidate_id")
	private Candidate candidate;
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "qualification_id")
	private QualificationMaster qualificationMaster;
	
	@ManyToOne
	@JoinColumn(name = "color_id")
	private Color color;
	
	@Column(name = "school_or_college_name")
	private String schoolOrCollegeName;
	
	@Column(name = "board_or_university_name")
	private String boardOrUniversityName;
	
	@Column(name = "total_marks")
	private Integer totalMarks;
	
	@Column(name = "percentage")
	private String percentage;
	
	@Column(name = "year_of_passing")
	private String yearOfPassing;
	
	@Column(name = "remark_on_candidate")
	private String remarkOnCandidate;
	
	@Column(name = "remark_date")
	private Date remarkDate;
	
	@Column(name = "candidate_education_status")
	private String candidateEducationStatus;
	
	@Column(name = "candidate_roll_number")
	private String candidateRollNumber;
	
	@ManyToOne
	@JoinColumn(name = "source_service_id")
	private ServiceSourceMaster serviceSourceMaster;
	
	@ManyToOne
	@JoinColumn(name = "candidate_status_id")
	private CandidateStatus candidateStatus;
	
	@ManyToOne
	@JoinColumn(name = "remark_id")
	private RemarkMaster remarkMaster;
	
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
	
	//@Type(type="org.hibernate.type.BinaryType")
	@JdbcTypeCode(Types.BINARY)
    @Column(name = "education_certificate", columnDefinition="BLOB")
    private byte[] certificate;
	
	@Column(name = "content_id")
	private Long contentId;
	
	@Column(name = "is_highest_qualification")
	private Boolean isHighestQualification=false;
	
	@ManyToOne
	@JoinColumn(name = "suspect_clg_master_id")
	private SuspectClgMaster suspectClgMaster;
	
	@Column(name = "course_name")
	private String courseName;
	
	@Column(name = "custom_remark")
	private String customRemark;
}
