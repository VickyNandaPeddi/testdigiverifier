package com.aashdit.digiverifier.itr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aashdit.digiverifier.itr.model.ITRData;

public interface ITRDataRepository extends JpaRepository<ITRData, Long> {

	@Query("FROM ITRData where candidate.candidateCode =:candidateCode order by filedDate DESC")
	List<ITRData> findAllByCandidateCandidateCodeOrderByFiledDateDesc(@Param("candidateCode") String candidateCode);

	@Query("Select COUNT(d.itrId) FROM ITRData d where d.candidate.candidateCode =:candidateCode AND d.date LIKE %:finYear%")
	int getCountByCandidateCandidateCodeFinYear(@Param("candidateCode") String candidateCode,@Param("finYear") String finYear);
}
