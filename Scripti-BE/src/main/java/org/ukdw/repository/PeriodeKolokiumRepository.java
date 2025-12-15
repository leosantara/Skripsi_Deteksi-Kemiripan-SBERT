package org.ukdw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ukdw.entity.PeriodeKolokiumEntity;

import java.util.Date;
import java.util.List;

@Repository
public interface PeriodeKolokiumRepository extends JpaRepository<PeriodeKolokiumEntity, Long> {

    @Query("SELECT p FROM PeriodeKolokiumEntity p WHERE p.tglAkhir >= :currentDate ORDER BY p.tglAkhir ASC")
    List<PeriodeKolokiumEntity> findActivePeriode(@Param("currentDate") Date currentDate);

    @Query(value = "SELECT * FROM periodes p WHERE p.tgl_akhir_de >= :currentDate ORDER BY p.tgl_akhir_de ASC LIMIT 1", nativeQuery = true)
    PeriodeKolokiumEntity findActivePeriodeLimit(@Param("currentDate") Date currentDate);


    @Query(value = "(" +
            "SELECT * FROM periodes WHERE id > :id ORDER BY id ASC LIMIT 1" +
            ") UNION ALL (" +
            "SELECT * FROM periodes WHERE id = :id" +
            ") UNION ALL (" +
            "SELECT * FROM periodes WHERE id < :id ORDER BY id DESC LIMIT 1" +
            ")", nativeQuery = true)
    List<PeriodeKolokiumEntity> findAdjacentPeriods(@Param("id") Integer id);
}
