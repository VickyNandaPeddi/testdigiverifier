package com.aashdit.digiverifier.vendorcheck.repository;


import com.aashdit.digiverifier.vendorcheck.model.ConventionalVendorCandidatesSubmitted;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ConventionalVendorCandidatesSubmittedRepository extends JpaRepository<ConventionalVendorCandidatesSubmitted, Long> {

    @Query("select cs from ConventionalVendorCandidatesSubmitted cs WHERE cs.createdOn between :startDate and :endDate ORDER BY cs.createdOn DESC")
    Page<ConventionalVendorCandidatesSubmitted> findAllByUserIdAndDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

    @Query("select cs from ConventionalVendorCandidatesSubmitted cs WHERE  cs.stopCheckRecivedDate is  not  null and cs.createdOn between :startDate and :endDate ORDER BY cs.createdOn DESC")
    Page<ConventionalVendorCandidatesSubmitted> findAllByUserIdAndDateRangeForStopBgv(@Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

    @Query("select cs from ConventionalVendorCandidatesSubmitted cs WHERE cs.fastTrack is  not  null and cs.fastTrack like 'Yes' and cs.createdOn between :startDate and :endDate ORDER BY cs.createdOn DESC")
    Page<ConventionalVendorCandidatesSubmitted> findAllByUserIdAndDateRangeForFastTrack(@Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

    @Query("select cs from ConventionalVendorCandidatesSubmitted cs WHERE cs.status.statusCode=:status  and cs.createdOn between :startDate and :endDate ORDER BY cs.createdOn DESC")
    Page<ConventionalVendorCandidatesSubmitted> findAllByUserIdAndDateRangeForStaus(@Param("status") String status, @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

    @Query(value = "select count(cnv) from ConventionalVendorCandidatesSubmitted cnv WHERE cnv.status.statusCode=:status and cnv.createdOn between :startDate and :endDate ")
    int findAllByUserIdAndDateRangeCount(@Param("status") String status, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "select count(cnv) from ConventionalVendorCandidatesSubmitted cnv WHERE cnv.createdOn between :startDate and :endDate")
    int findAllByUserIdAndDateRange( @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "select count(cnv) from ConventionalVendorCandidatesSubmitted cnv WHERE cnv.stopCheckRecivedDate is  not  null and cnv.createdOn between :startDate and :endDate")
    int findAllByUserIdAndDateRangeForStopBgv(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "select count(cnv) from ConventionalVendorCandidatesSubmitted cnv WHERE cnv.fastTrack is  not  null and cnv.fastTrack like 'Yes' and cnv.createdOn between :startDate and :endDate")
    int findAllByUserIdAndDateRangeForFastTrack(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


}