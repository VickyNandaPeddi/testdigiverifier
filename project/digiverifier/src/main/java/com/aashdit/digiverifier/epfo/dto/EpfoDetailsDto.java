package com.aashdit.digiverifier.epfo.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EpfoDetailsDto {

	String candidateCode;
	String uanusername;
	String uanpassword;
	String captcha;
	String transactionid;
	String errorMessage;

	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}
}
