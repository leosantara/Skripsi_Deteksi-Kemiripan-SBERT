package org.ukdw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ukdw.entity.JadwalSeminarDosenEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface JadwalSeminarDosenRepository extends JpaRepository<JadwalSeminarDosenEntity, Long> {
    // Mencari semua record dari JadwalSeminarDosen
    List<JadwalSeminarDosenEntity> findAll();

    // Mencari semua dosen berdasarkan jadwalseminar_id
    List<JadwalSeminarDosenEntity> findAllByJadwalSeminarId(Long jadwalSeminarId);

    // Mencari record berdasarkan jadwalseminar_id dan dosen_id
    Optional<JadwalSeminarDosenEntity> findByJadwalSeminarIdAndDosenId(Long jadwalSeminarId, Long dosenId);

    // Mencari record berdasarkan jadwalseminar_id dan status
    Optional<JadwalSeminarDosenEntity> findByJadwalSeminarIdAndStatus(Long jadwalSeminarId, JadwalSeminarDosenEntity.Status status);

    // Mencari semua dosen berdasarkan dosen_id
    List<JadwalSeminarDosenEntity> findAllByDosenId(Long dosenId);
}
