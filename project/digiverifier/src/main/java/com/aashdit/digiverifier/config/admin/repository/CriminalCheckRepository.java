package com.aashdit.digiverifier.config.admin.repository;

import com.aashdit.digiverifier.config.admin.model.CriminalCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriminalCheckRepository extends JpaRepository<CriminalCheck,Long> {
    public List<CriminalCheck> findByRequestId(String requestId);
    public List<CriminalCheck> findByRequestIdAndProceedingsType(String requestId,String proceedingsType);

    public CriminalCheck findByVendorUploadCheckIdAndProceedingsTypeAndCourt(Long vendorUploadCheckId,String proceedingType,String court);


    List<CriminalCheck> findByCheckUniqueId(String checkUniqueId);
    List<CriminalCheck> findByCheckUniqueIdAndProceedingsType(String checkUniqueId,String proceedingType);
}
