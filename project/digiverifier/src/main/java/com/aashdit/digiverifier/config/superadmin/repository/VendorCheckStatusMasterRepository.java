package com.aashdit.digiverifier.config.superadmin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aashdit.digiverifier.config.superadmin.model.VendorCheckStatusMaster;

public interface VendorCheckStatusMasterRepository extends JpaRepository<VendorCheckStatusMaster, Long> {

//    VendorCheckStatusMaster findByCheckStatusCode();

    VendorCheckStatusMaster findByVendorCheckStatusMasterId(Long vendorCheckStatusMasterId);

	Optional<VendorCheckStatusMaster> findByCheckStatusCode(String status);


}
