package com.aashdit.digiverifier.config.admin.dto;

import com.aashdit.digiverifier.config.admin.model.CriminalCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LegalProceedingsDTO {
	
	
    private List<CivilProceedingsDTO> civilProceedingsList;
    private List<CriminalProceedingsDTO> criminalProceedingsList;
    private List<CriminalCheck> civilProceedingList;
    private List<CriminalCheck> criminalProceedingList;
}
