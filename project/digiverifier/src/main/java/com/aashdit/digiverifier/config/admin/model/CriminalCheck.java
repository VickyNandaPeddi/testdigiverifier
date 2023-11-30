package com.aashdit.digiverifier.config.admin.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "t_dgv_convetional_criminal_check")
public class CriminalCheck {

    private static final long serialVersionUID = -763414907911681633L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    private String proceedingsType;

    private String dateOfSearch;

    private String court;

    private String jurisdiction;

    private String nameOfTheCourt;

    private String result;

    private String requestId;

    private String checkUniqueId;

    private Date createdOn;

    private Long vendorUploadCheckId;
}
