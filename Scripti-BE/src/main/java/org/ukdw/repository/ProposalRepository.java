package org.ukdw.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.ukdw.entity.ProposalEntity;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<ProposalEntity, Long> {
    // Metode untuk mencari proposal berdasarkan nama (judul) yang mengandung substring
    @Query("SELECT p FROM proposals p WHERE LOWER(p.judul) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<ProposalEntity> findByJudulContainingIgnoreCase(String name);
    @Query("SELECT p FROM proposals p WHERE LOWER(p.mahasiswa.nim) LIKE LOWER(CONCAT('%', :nim, '%'))")
    List<ProposalEntity> findByNimContainingIgnoreCase(@Param("nim") String nim);

    List<ProposalEntity> findByDosenId_Id(Long dosenId);

    @Query(value = "SELECT * FROM proposals p WHERE p.periode_id = :periodeId", nativeQuery = true)
    List<ProposalEntity> findProposalsByPeriodeId(@Param("periodeId") Long periodeId);

    List<ProposalEntity> findAllByOrderByModifiedDesc();

    @Query("SELECT p FROM proposals p WHERE p.id = :id AND p.mahasiswa.nim = :nim")
    Optional<ProposalEntity> findByIdAndNim(@Param("id") Long id, @Param("nim") String nim);

    @Query("SELECT p FROM proposals p WHERE p.id = :id AND p.dosenId.nidn = :nidn")
    Optional<ProposalEntity> findByIdAndNidn(@Param("id") Long id, @Param("nidn") String nidn);

    @Query("SELECT p FROM proposals p WHERE FUNCTION('YEAR', p.modified) = :year")
    List<ProposalEntity> findProposalsByYear(@Param("year") int year);

    @Query("SELECT p FROM proposals p WHERE p.periodes.id = :periodeId AND p.status = :status")
    List<ProposalEntity> findProposalsByPeriodeIdAndStatus(@Param("periodeId") Long periodeId, @Param("status") String status);


}