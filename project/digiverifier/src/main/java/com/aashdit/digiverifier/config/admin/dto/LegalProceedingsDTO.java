package com.aashdit.digiverifier.config.admin.dto;

import com.aashdit.digiverifier.config.admin.model.CriminalCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LegalProceedingsDTO {
    private CivilProceedingsDTO civilProceedings;
    private CriminalProceedingsDTO criminalProceedings;
    private List<CriminalCheck> civilProceedingList;
    private List<CriminalCheck> criminalProceedingList;
}
