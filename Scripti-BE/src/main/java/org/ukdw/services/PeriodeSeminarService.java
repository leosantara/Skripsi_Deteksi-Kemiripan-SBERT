package org.ukdw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ukdw.entity.PeriodeSeminarEntity;
import org.ukdw.exception.BadRequestException;
import org.ukdw.repository.PeriodeSeminarRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PeriodeSeminarService {

    private final PeriodeSeminarRepository periodeSeminarRepository;

    // Create or Update Seminar
    public PeriodeSeminarEntity savePerseminar(PeriodeSeminarEntity perseminar) {
        if (perseminar.getTanggal() == null) {
            throw new BadRequestException("Tanggal harus diisi!");
        }
        if (perseminar.getTglAwal() == null) {
            throw new BadRequestException("Tanggal Awal harus diisi!");
        }
        if (perseminar.getTglAkhir() == null) {
            throw new BadRequestException("Tanggal Akhir harus diisi!");
        }
        if (perseminar.getAktif() == null || perseminar.getAktif().isEmpty()) {
            throw new BadRequestException("Status aktif harus diisi!");
        }
        return periodeSeminarRepository.save(perseminar);
    }

    // Read Seminar by ID
    public Optional<PeriodeSeminarEntity> getPerseminarById(Long id) {
        return periodeSeminarRepository.findById(id);
    }

    // Read All Seminars
    public List<PeriodeSeminarEntity> getAllPerseminar() {
        return periodeSeminarRepository.findAll();
    }

    // Delete Seminar by ID
    public void deletePerseminarById(Long id) {
        // Cari seminar berdasarkan ID
        Optional<PeriodeSeminarEntity> existingPerseminar = periodeSeminarRepository.findById(id);

        if (existingPerseminar.isPresent()) {
            PeriodeSeminarEntity perseminar = existingPerseminar.get();

            // Mendapatkan tanggal saat ini
            Date currentDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Cek apakah tanggal seminar sudah lewat
            if (perseminar.getTanggal().before(currentDate)) {
                throw new BadRequestException("Seminar sudah lewat dan tidak dapat dihapus.");
            }

            // Hapus data jika tanggal seminar belum lewat
            periodeSeminarRepository.deleteById(id);
        } else {
            throw new BadRequestException("Seminar dengan ID " + id + " tidak ditemukan.");
        }
    }

    public List<PeriodeSeminarEntity> getActiveSeminars() {
        return periodeSeminarRepository.findByAktif("Y");
    }

    public PeriodeSeminarEntity updatePerseminar(Long id, PeriodeSeminarEntity updatedSeminar) {
        // Cari seminar berdasarkan ID
        PeriodeSeminarEntity existingSeminar = periodeSeminarRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Seminar dengan ID " + id + " tidak ditemukan."));

        // Validasi atribut yang diperlukan
        if (updatedSeminar.getTanggal() == null) {
            throw new BadRequestException("Tanggal harus diisi!");
        }
        if (updatedSeminar.getTglAwal() == null) {
            throw new BadRequestException("Tanggal Awal harus diisi!");
        }
        if (updatedSeminar.getTglAkhir() == null) {
            throw new BadRequestException("Tanggal Akhir harus diisi!");
        }
        if (updatedSeminar.getAktif() == null || updatedSeminar.getAktif().isEmpty()) {
            throw new BadRequestException("Status aktif harus diisi!");
        }

        // Cek apakah ada seminar aktif lain
        List<PeriodeSeminarEntity> activeSeminars = periodeSeminarRepository.findByAktif("Y");

        // Jika ada seminar aktif lain, pastikan tglAwal seminar baru setelah tglAkhir seminar aktif terakhir
        if (!activeSeminars.isEmpty()) {
            PeriodeSeminarEntity latestActiveSeminar = activeSeminars.stream()
                    .max(Comparator.comparing(PeriodeSeminarEntity::getTglAkhir))
                    .orElseThrow(() -> new BadRequestException("Tidak ada seminar aktif ditemukan."));

            // Validasi agar tglAwal seminar yang diupdate lebih besar dari tglAkhir seminar aktif terakhir
            if (updatedSeminar.getTglAwal().before(latestActiveSeminar.getTglAkhir()) && !existingSeminar.equals(latestActiveSeminar)) {
                throw new BadRequestException("Seminar yang Anda perbarui harus setelah seminar aktif terakhir ("
                        + latestActiveSeminar.getTglAkhir() + ").");
            }
        }

        // Update data seminar yang ada
        existingSeminar.setTanggal(updatedSeminar.getTanggal());
        existingSeminar.setTglAwal(updatedSeminar.getTglAwal());
        existingSeminar.setTglAkhir(updatedSeminar.getTglAkhir());
        existingSeminar.setAktif(updatedSeminar.getAktif());

        // Simpan perubahan
        return periodeSeminarRepository.save(existingSeminar);
    }
}
