package org.ukdw.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ukdw.entity.ProposalEntity;
import org.ukdw.entity.SeminarEntity;
import org.ukdw.entity.SeminarEntity.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeminarRepository extends JpaRepository<SeminarEntity, Integer> {

    // Mencari seminar berdasarkan status
    List<SeminarEntity> findAllByStatus(Status status);

    // Mencari seminar berdasarkan jadwal seminar ID
    List<SeminarEntity> findAllByJadwalSeminar_Id(Integer jadwalSeminarId);

    // Mencari seminar berdasarkan user ID
    List<SeminarEntity> findAllByUser_Id(Integer userId);

    // Mencari seminar berdasarkan proposal ID
    List<SeminarEntity> findAllByProposal_Id(Integer proposalId);

    // Mencari seminar berdasarkan waktu validasi setelah tanggal tertentu
    List<SeminarEntity> findAllByValidateTimeAfter(LocalDateTime validateTime);

    // Mencari seminar berdasarkan jadwal seminar ID dan status
    Optional<SeminarEntity> findByJadwalSeminar_IdAndStatus(Integer jadwalSeminarId, Status status);

    Optional<SeminarEntity> findById(Long id); // Ensure `Long` here

    List<SeminarEntity> findAllByDeskevaluationId(Integer deskevaluationId);

    @Query(value = "SELECT s.id AS seminar_id, s.proposal_id, s.perseminar_id, s.status, s.catatan, s.modified, s.user_id, s.jadwalseminar_id, s.validate_time, " +
            "p.id AS perseminar_id, p.aktif " +
            "FROM seminars s " +
            "JOIN perseminars p ON s.perseminar_id = p.id " +
            "WHERE s.proposal_id IS NULL " +
            "AND p.aktif = 'Y'", nativeQuery = true)
    List<SeminarEntity> findSeminarsWithNullProposalIdAndActivePerseminar();

    @Query(value = "SELECT * FROM seminars s " +
            "WHERE s.proposal_id IS NULL " +
            "AND s.perseminar_id = (SELECT MAX(se.perseminar_id) FROM seminars se)",
            nativeQuery = true)
    List<SeminarEntity> findSeminarsWithNullProposalIdAndLatestPerseminar();

    @Query(value = "SELECT * FROM seminars WHERE jadwalseminar_id IS NULL", nativeQuery = true)
    List<SeminarEntity> findSeminarsWithNullJadwalSeminarNative();

    @Query(value = "SELECT * FROM seminars s " +
            "WHERE s.status IN (:statuses) " +
            "AND s.proposal_id IS NOT NULL", nativeQuery = true)
    List<SeminarEntity> findSeminarsWithStatusBOrVAndProposalIdNotNull(
            @Param("statuses") List<String> statuses);

    @Query(value = "SELECT s.* FROM seminars s " +
            "JOIN deskevaluations d ON s.deskevaluation_id = d.id " +
            "WHERE d.nim = :nim",
            nativeQuery = true)
    List<SeminarEntity> findSeminarsByNim(@Param("nim") String nim);

}
