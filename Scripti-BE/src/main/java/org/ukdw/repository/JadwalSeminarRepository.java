package org.ukdw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ukdw.entity.JadwalSeminarEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JadwalSeminarRepository extends JpaRepository<JadwalSeminarEntity, Long> {
    boolean existsByWaktuAndKelompok(LocalDateTime waktu, JadwalSeminarEntity.Kelompok kelompok);
    List<JadwalSeminarEntity> findByPerseminarId(Long perseminarId);

    List<JadwalSeminarEntity> findByWaktuBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query(value = "SELECT * FROM jadwalseminars j JOIN perseminars p ON j.perseminar_id = p.id WHERE p.aktif = 'Y'", nativeQuery = true)
    List<JadwalSeminarEntity> findJadwalSeminarByActivePerseminarNative();


}

