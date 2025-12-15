package org.ukdw.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ukdw.entity.JadwalSeminarEntity;
import org.ukdw.exception.BadRequestException;
import org.ukdw.repository.JadwalSeminarRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class JadwalSeminarService {

    private final JadwalSeminarRepository jadwalSeminarRepository;



    // Create
    public JadwalSeminarEntity createJadwalSeminar(JadwalSeminarEntity jadwalSeminar) {
        // Validasi bahwa semua atribut harus diisi
        if (jadwalSeminar.getWaktu() == null || jadwalSeminar.getKelompok() == null || jadwalSeminar.getPerseminar() == null) {
            throw new BadRequestException("Semua atribut harus diisi: waktu, kelompok, dan perseminar.");
        }

        // Validasi bahwa tidak ada jadwal yang memiliki kelompok yang sama pada waktu yang sama
        boolean isKelompokAlreadyExist = jadwalSeminarRepository.existsByWaktuAndKelompok(jadwalSeminar.getWaktu(), jadwalSeminar.getKelompok());
        if (isKelompokAlreadyExist) {
            throw new BadRequestException("Kelompok " + jadwalSeminar.getKelompok() + " sudah terdaftar pada waktu " + jadwalSeminar.getWaktu());
        }

        // Simpan jadwal seminar
        return jadwalSeminarRepository.save(jadwalSeminar);
    }


    // Read
    public Optional<JadwalSeminarEntity> getJadwalSeminarById(Long id) {
        return jadwalSeminarRepository.findById(id);
    }

    public List<JadwalSeminarEntity> getAllJadwalSeminars() {
        return jadwalSeminarRepository.findAll();
    }

    public List<JadwalSeminarEntity> getJadwalSeminarsByPerseminarId(Long perseminarId) {
        return jadwalSeminarRepository.findByPerseminarId(perseminarId);
    }

    // Update
    public JadwalSeminarEntity updateJadwalSeminar(Long id, JadwalSeminarEntity updatedJadwalSeminar) {
        // Cari jadwal seminar berdasarkan ID
        JadwalSeminarEntity existingJadwalSeminar = jadwalSeminarRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Jadwal Seminar dengan ID " + id + " tidak ditemukan."));

        // Validasi atribut yang diperlukan
        if (updatedJadwalSeminar.getWaktu() == null) {
            throw new BadRequestException("Waktu harus diisi!");
        }
        if (updatedJadwalSeminar.getPerseminar() == null) {
            throw new BadRequestException("Perseminar harus diisi!");
        }


        // Validasi agar waktu yang diupdate tidak lebih kecil dari waktu saat ini
        if (updatedJadwalSeminar.getWaktu().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Waktu seminar tidak boleh diubah menjadi waktu yang sudah lewat.");
        }

        // Update data jadwal seminar yang ada
        existingJadwalSeminar.setWaktu(updatedJadwalSeminar.getWaktu());
        existingJadwalSeminar.setKelompok(updatedJadwalSeminar.getKelompok());
        existingJadwalSeminar.setPerseminar(updatedJadwalSeminar.getPerseminar());

        // Simpan perubahan
        return jadwalSeminarRepository.save(existingJadwalSeminar);
    }


    // Delete
    public void deleteJadwalSeminar(Long id) {
        Optional<JadwalSeminarEntity> jadwalSeminarOpt = jadwalSeminarRepository.findById(id);

        if (jadwalSeminarOpt.isPresent()) {
            JadwalSeminarEntity jadwalSeminar = jadwalSeminarOpt.get();

            // Validasi bahwa waktu seminar belum lewat
            if (jadwalSeminar.getWaktu().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Tidak dapat menghapus jadwal seminar. Waktu seminar sudah lewat.");
            }

            jadwalSeminarRepository.deleteById(id);
        } else {
            throw new BadRequestException("Jadwal Seminar dengan ID " + id + " tidak ditemukan.");
        }
    }

    public List<JadwalSeminarEntity> getJadwalSeminarByTanggal(LocalDate tanggal) {
        LocalDateTime startOfDay = tanggal.atStartOfDay();
        LocalDateTime endOfDay = tanggal.atTime(LocalTime.MAX);

        return jadwalSeminarRepository.findByWaktuBetween(startOfDay, endOfDay);
    }
}
