package org.ukdw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ukdw.entity.GroupEvaluatorEntity;
import org.ukdw.entity.PeriodeKolokiumEntity;
import java.util.List;

public interface GroupEvaluatorRepository extends JpaRepository<GroupEvaluatorEntity, Long> {
    List<GroupEvaluatorEntity> findByPeriode(PeriodeKolokiumEntity periode);
    List<GroupEvaluatorEntity> findByPeriode_IdAndKelompok(Long periodeId, GroupEvaluatorEntity.Kelompok kelompok);
    List<GroupEvaluatorEntity> findAllById(Long sidangId);
}
