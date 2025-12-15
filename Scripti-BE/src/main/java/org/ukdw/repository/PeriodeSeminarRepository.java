package org.ukdw.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ukdw.entity.PeriodeSeminarEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodeSeminarRepository extends JpaRepository<PeriodeSeminarEntity, Long> {
    List<PeriodeSeminarEntity> findByAktif(String aktif);

    @Query("SELECT p FROM PeriodeSeminarEntity p WHERE p.aktif = :status")
    Optional<PeriodeSeminarEntity> findPerseminarByStatus(@Param("status") String status);

        @Query("SELECT p FROM PeriodeSeminarEntity p WHERE p.aktif = 'Y'")
        PeriodeSeminarEntity findActivePerseminar();


}
