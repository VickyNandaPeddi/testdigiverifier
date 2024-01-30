package com.aashdit.digiverifier.config.superadmin.dto;

import java.util.ArrayList;

import lombok.Data;


@Data
public class CheckAttributeAndValueDTO {
	
	 private String sourceName;
	    private ArrayList<String> attributeAndValue;

}
