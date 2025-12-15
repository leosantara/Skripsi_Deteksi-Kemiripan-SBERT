package org.ukdw.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.ukdw.entity.DeskEvaluationEntity;
import org.ukdw.entity.GroupEvaluatorEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DeskEvaluationRepository extends JpaRepository<DeskEvaluationEntity, Integer> {
    // Cari berdasarkan NIM mahasiswa
    List<DeskEvaluationEntity> findByMahasiswa_Nim(String nim);

    // Cari berdasarkan evaluator ID
    List<DeskEvaluationEntity> findByDosenEvaluator_Id(Integer evaluatorId);

    // Cari berdasarkan dosen 1 ID
    List<DeskEvaluationEntity> findByDosen1_Id(Integer dosen1Id);

    // Cari berdasarkan dosen 2 ID
    List<DeskEvaluationEntity> findByDosen2_Id(Integer dosen2Id);

    // Cari berdasarkan dosen 1 atau dosen 2 ID
    List<DeskEvaluationEntity> findByDosen1_IdOrDosen2_Id(Integer dosen1Id, Integer dosen2Id);

    // Cari berdasarkan NIDN dosen 1 atau dosen 2
    List<DeskEvaluationEntity> findByDosen1_NidnOrDosen2_Nidn(String nidn1, String nidn2);

    // Cari berdasarkan NIDN dosen 1
    List<DeskEvaluationEntity> findByDosen1_Nidn(String nidn);

    // Cari berdasarkan NIDN dosen 2
    List<DeskEvaluationEntity> findByDosen2_Nidn(String nidn);

    // Cari berdasarkan proposal ID
    List<DeskEvaluationEntity> findByProposal_Id(Long proposalId);

    // Cari berdasarkan sidang ID
    List<DeskEvaluationEntity> findByGroupEvaluator_Id(Long sidangId);

    @Query("SELECT de FROM DeskEvaluationEntity de " +
            "WHERE de.status = 'T' " +
            "AND de.proposal.status = 'T' " +
            "AND de.dosen1 IS NOT NULL")
    List<DeskEvaluationEntity> findAllAcceptedForAI();
//    DeskEvaluationEntity findByStatus(String status);

    @Query("SELECT de FROM DeskEvaluationEntity de WHERE de.status = :status ORDER BY de.tanggalValid DESC LIMIT 1")
    DeskEvaluationEntity findFirstByStatusOrderByTanggalValidDesc(@Param("status") String status);

    @Query("SELECT de.proposal.id FROM DeskEvaluationEntity de")
    List<Long> findAllProposalIdsInDeskEvaluation();

    List<DeskEvaluationEntity> findByGroupEvaluator_Periode_Id(Long periodeId);

    List<DeskEvaluationEntity> findByGroupEvaluatorPeriodeIdAndGroupEvaluatorKelompok(
            Long periodeId, GroupEvaluatorEntity.Kelompok kelompok);

    List<DeskEvaluationEntity> findByTanggalValidBetween(LocalDate startDate, LocalDate endDate);

}
