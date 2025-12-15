package org.ukdw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ukdw.entity.DosenEvaluatorEntity;

import java.util.List;
import java.util.Optional;


@Repository
public interface DosenEvaluatorRepository extends JpaRepository<DosenEvaluatorEntity, Long> {
    List<DosenEvaluatorEntity> findAll();
    // Mencari semua evaluator berdasarkan sidangId
    List<DosenEvaluatorEntity> findAllBySidangId(Long sidangId);
    Optional<DosenEvaluatorEntity> findBySidangIdAndDosenId(Long sidangId, Long dosenId);
    Optional<DosenEvaluatorEntity> findBySidangIdAndStatus(Long sidangId, DosenEvaluatorEntity.Status status);
    List<DosenEvaluatorEntity> findAllByDosenId(Long dosenId);
    List<DosenEvaluatorEntity> findAllByDosen_Nidn(String nidn);
    List<DosenEvaluatorEntity> findAllBySidang_Periode_IdAndDosen_Nidn(Long periodeId, String nidn);
    List<DosenEvaluatorEntity> findAllBySidang_IdIn(List<Long> periodeId);
    List<DosenEvaluatorEntity> findAllBySidang_Id(Long sidangId);

}