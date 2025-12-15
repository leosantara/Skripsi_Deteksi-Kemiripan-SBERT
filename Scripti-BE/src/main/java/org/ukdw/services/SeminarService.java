package org.ukdw.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ukdw.dto.kolokium.SeminarUpdateDTO;
import org.ukdw.entity.*;
import org.ukdw.exception.BadRequestException;
import org.ukdw.exception.ScNotFoundException;
import org.ukdw.repository.*;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeminarService {

    private final SeminarRepository seminarRepository;
    private final ProposalRepository proposalRepository;
    private final DeskEvaluationRepository deskEvaluationRepository;
    private final PeriodeKolokiumRepository periodeKolokiumRepository;
    private final PeriodeSeminarRepository periodeSeminarRepository;
    private final KolokiumService kolokiumService;
    private final JadwalSeminarRepository jadwalSeminarRepository;
    private PeriodeSeminarRepository perseverminarsRepository;


    @Transactional
    public SeminarEntity createSeminarOtomatis() {
        // Mencari data DeskEvaluation yang statusnya "K" dan yang terbaru berdasarkan tanggal valid
        DeskEvaluationEntity deskEvaluation = deskEvaluationRepository.findFirstByStatusOrderByTanggalValidDesc("K");

        if (deskEvaluation == null) {
            throw new ScNotFoundException("Tidak ada DeskEvaluation dengan status 'K'");
        }

        System.out.println("DeskEvaluation ditemukan: " + deskEvaluation);

        // Mencari data Perseminar berdasarkan status periode yang ada pada proposal
        PeriodeSeminarEntity perseminar = periodeSeminarRepository.findActivePerseminar();

        if (perseminar == null) {
            throw new ScNotFoundException("Tidak ada Perseminar yang sesuai dengan status periode");
        }

        System.out.println("Perseminar ditemukan: " + perseminar);

        // Menentukan proposal, jika nim dari mahasiswa di DeskEvaluation tidak ada proposal terkait, maka tetap simpan seminar dengan proposal null
        ProposalEntity proposal = null;

        // Membuat entitas Seminar baru
        SeminarEntity seminar = new SeminarEntity();

        // Menyusun nilai untuk atribut Seminar
        seminar.setDeskevaluation(deskEvaluation);
        seminar.setStatus(SeminarEntity.Status.B); // Status "B" Baru
        seminar.setCatatan(null); // Catatan null
        seminar.setModified(null); // Modified null
        seminar.setUser(null); // User null
        seminar.setJadwalSeminar(null); // JadwalSeminar null
        seminar.setPerseminar(perseminar); // Perseminar dari data Perseminar
        seminar.setValidateTime(LocalDateTime.now()); // Tanggal dan jam saat dibuat
        seminar.setProposal(proposal); // Proposal bisa null jika tidak ada

        try {
            // Menyimpan SeminarEntity ke database
            SeminarEntity savedSeminar = seminarRepository.save(seminar);
            System.out.println("Seminar berhasil disimpan: " + savedSeminar);
            return savedSeminar;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("Gagal menyimpan SeminarEntity", e);
        }
    }

    @Transactional
    public boolean updateProposal(Long seminarId, Long proposalId) {
        // Mendapatkan SeminarEntity berdasarkan seminarId
        Optional<SeminarEntity> seminarOptional = seminarRepository.findById(seminarId);
        // Mendapatkan ProposalEntity berdasarkan proposalId
        Optional<ProposalEntity> proposalOptional = proposalRepository.findById(proposalId);

        if (seminarOptional.isPresent() && proposalOptional.isPresent()) {
            SeminarEntity seminar = seminarOptional.get();
            ProposalEntity proposal = proposalOptional.get();

            // Menetapkan proposal ke SeminarEntity
            seminar.setProposal(proposal);
            seminar.setModified(LocalDateTime.now());  // Update waktu modifikasi

            seminarRepository.save(seminar);  // Menyimpan perubahan di database
            return true;
        } else {
            return false;  // Mengembalikan false jika seminar atau proposal tidak ditemukan
        }
    }


    // Mendapatkan Perseminar yang aktif (status "Y")
    private Optional<PeriodeSeminarEntity> findActivePerseminar() {
        // Pass "Y" as the status to find active Perseminars
        return perseverminarsRepository.findPerseminarByStatus("Y");
    }

    // Validasi data seminar agar tidak ada yang kosong
    private void validateSeminarData(SeminarEntity seminar) {
        // Memastikan Deskevaluation tidak null
        if (seminar.getDeskevaluation() == null) {
            throw new RuntimeException("Deskevaluation tidak boleh kosong");
        }

        // Memastikan nim yang ada di DeskEvaluationEntity tidak kosong
        String nimFromDeskEvaluation = seminar.getDeskevaluation().getMahasiswa().getNim();
        if (nimFromDeskEvaluation == null || nimFromDeskEvaluation.isEmpty()) {
            throw new RuntimeException("NIM pada DeskEvaluation tidak boleh kosong");
        }
    }

    public Optional<SeminarEntity> getSeminarById(Integer id) {
        return seminarRepository.findById(id);
    }

    // Mendapatkan semua seminar
    public List<SeminarEntity> getAllSeminars() {
        return seminarRepository.findAll();
    }

    public SeminarEntity updateSeminar(Integer id, SeminarEntity updatedSeminar) {
        validateSeminarData(updatedSeminar);  // Validasi data seminar

        return seminarRepository.findById(id)
                .map(seminar -> {
                    // Pastikan status yang diterima hanya valid
                    if (updatedSeminar.getStatus() != null && !isValidStatus(updatedSeminar.getStatus())) {
                        throw new RuntimeException("Status yang diberikan tidak valid");
                    }

                    // Jika status null, jangan ubah status
                    if (updatedSeminar.getStatus() != null) {
                        seminar.setStatus(updatedSeminar.getStatus());
                    }

                    seminar.setDeskevaluation(updatedSeminar.getDeskevaluation());
                    seminar.setCatatan(updatedSeminar.getCatatan());

                    // Set modified time when data is updated
                    seminar.setModified(LocalDateTime.now());

                    seminar.setUser(updatedSeminar.getUser());
                    seminar.setJadwalSeminar(updatedSeminar.getJadwalSeminar());
                    seminar.setPerseminar(updatedSeminar.getPerseminar());
                    seminar.setValidateTime(updatedSeminar.getValidateTime());
                    seminar.setProposal(updatedSeminar.getProposal());

                    return seminarRepository.save(seminar);
                })
                .orElseThrow(() -> new RuntimeException("Seminar dengan ID " + id + " tidak ditemukan"));
    }

    // Menghapus seminar berdasarkan ID
    public void deleteSeminar(Integer id) {
        SeminarEntity seminar = seminarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seminar dengan ID " + id + " tidak ditemukan"));

        // Cek apakah seminar sudah memiliki nilai di kolom modified
        if (seminar.getModified() != null) {
            throw new RuntimeException("Seminar dengan ID " + id + " tidak dapat dihapus karena sudah memiliki data modified.");
        }

        seminarRepository.deleteById(id);
    }

    public List<SeminarEntity> getSeminarsWithNullProposalAndActivePerseminar() {
        // Langsung menggunakan status "Y" tanpa parameter input dari pengguna
        return seminarRepository.findSeminarsWithNullProposalIdAndLatestPerseminar();
    }


    private boolean isValidStatus(SeminarEntity.Status status) {
        return status == SeminarEntity.Status.B || status == SeminarEntity.Status.V ||
                status == SeminarEntity.Status.T || status == SeminarEntity.Status.L;
    }

    public SeminarUpdateDTO updateSeminarStatus(Integer id, String status) {
        Optional<SeminarEntity> seminarOpt = seminarRepository.findById(id);
        if (seminarOpt.isPresent()) {
            SeminarEntity seminar = seminarOpt.get();
            seminar.setStatus(SeminarEntity.Status.valueOf(status));
            seminarRepository.save(seminar);

            // Pemetaan SeminarEntity ke SeminarDTO menggunakan ModelMapper
            ModelMapper modelMapper = new ModelMapper();
            return modelMapper.map(seminar, SeminarUpdateDTO.class);  // Mengembalikan DTO yang telah dipetakan
        }
        return null;  // Mengembalikan null jika seminar tidak ditemukan
    }

    public List<ProposalEntity> getProposalsForActivePeriode() {
        // Mendapatkan periode aktif
        PeriodeKolokiumEntity activePeriode = kolokiumService.getActivePeriodeLimit(new Date());

        if (activePeriode != null) {
            // Menemukan proposal yang memiliki periode_id yang sesuai
            return proposalRepository.findProposalsByPeriodeId(activePeriode.getId());
        }
        return null;
    }

    @Transactional
    public boolean updateJadwalSeminarId(Long seminarId, Long jadwalseminarId) {
        // Mencari SeminarEntity berdasarkan seminarId
        Optional<SeminarEntity> seminarOptional = seminarRepository.findById(seminarId);

        if (seminarOptional.isPresent()) {
            // Mendapatkan SeminarEntity
            SeminarEntity seminar = seminarOptional.get();

            // Mencari JadwalSeminarEntity berdasarkan jadwalseminarId
            Optional<JadwalSeminarEntity> jadwalSeminarOptional = jadwalSeminarRepository.findById(jadwalseminarId);

            if (jadwalSeminarOptional.isPresent()) {
                // Mendapatkan JadwalSeminarEntity
                JadwalSeminarEntity jadwalSeminar = jadwalSeminarOptional.get();

                // Mengupdate jadwalseminar_id pada seminar
                seminar.setJadwalSeminar(jadwalSeminar);
                seminar.setModified(LocalDateTime.now()); // Mengupdate waktu modifikasi

                // Menyimpan perubahan pada SeminarEntity
                seminarRepository.save(seminar);

                return true;  // Berhasil mengupdate
            } else {
                // Jika jadwal seminar tidak ditemukan
                return false;
            }
        } else {
            // Jika seminar tidak ditemukan
            return false;
        }
    }

    public List<JadwalSeminarEntity> getJadwalSeminarsByActivePerseminarNative() {
        return jadwalSeminarRepository.findJadwalSeminarByActivePerseminarNative();
    }

    @Transactional()
    public List<SeminarEntity> getSeminarsWithNullJadwalSeminarNative() {
        return seminarRepository.findSeminarsWithNullJadwalSeminarNative();
    }

    @Transactional
    public boolean updateSeminarStatusAndNotes(Long seminarId, SeminarEntity.Status status, String catatan) {
        Optional<SeminarEntity> seminarOptional = seminarRepository.findById(seminarId);

        if (seminarOptional.isPresent()) {
            SeminarEntity seminar = seminarOptional.get();

            // Update the status and optional catatan attributes
            seminar.setStatus(status);
            if (catatan != null && !catatan.isEmpty()) {
                seminar.setCatatan(catatan);
            }

            // Set modified to current date and time
            seminar.setModified(LocalDateTime.now());

            // Save the updated seminar entity
            seminarRepository.save(seminar);

            return true;
        } else {
            return false;  // Return false if seminar is not found
        }
    }

    public List<SeminarEntity> getSeminarsWithStatusBOrVAndProposalIdNotNull(List<String> status) {
        return seminarRepository.findSeminarsWithStatusBOrVAndProposalIdNotNull(status);
    }

    public List<SeminarEntity> getSeminarsByNim(String nim) {
        return seminarRepository.findSeminarsByNim(nim);
    }
}
