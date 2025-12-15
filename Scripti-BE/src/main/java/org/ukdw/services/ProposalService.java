package org.ukdw.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.ukdw.dto.ProposalDTO;
import org.ukdw.entity.*;
import org.ukdw.exception.BadRequestException;
import org.ukdw.repository.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final UserRepository userRepository;
    private final PeriodeKolokiumRepository periodeRepository;
    private final DosenRepository dosenRepository;
    private final MahasiswaRepository mahasiswaRepository;
    private final SeminarService seminarService;
    private final DeskEvaluationRepository deskEvaluationRepository;

    private static final Set<String> VALID_STATUSES = Set.of("T", "R", "B", "K", "L", "V");
    private static final Logger logger = LoggerFactory.getLogger(ProposalService.class);

    // CREATE

    @Transactional
    public ProposalEntity saveProposal(ProposalDTO proposalDTO, MultipartFile file) throws Exception {
        ProposalEntity proposal = new ProposalEntity();
        logger.info("biji 1");
        // Cari MahasiswaEntity berdasarkan nim
        MahasiswaEntity mahasiswa = mahasiswaRepository.findById(proposalDTO.getNim())
                .orElseThrow(() -> new BadRequestException("Mahasiswa dengan NIM " + proposalDTO.getNim() + " tidak ditemukan"));
        proposal.setMahasiswa(mahasiswa); // Set relasi MahasiswaEntity
        logger.info("biji 2");
        proposal.setJudul(proposalDTO.getJudul());
        proposal.setLatarBelakang(proposalDTO.getLatarBelakang());
        proposal.setRumusan(proposalDTO.getRumusan());
        proposal.setBatasan(proposalDTO.getBatasan());
        proposal.setTujuan(proposalDTO.getTujuan());
        proposal.setReferensi(proposalDTO.getReferensi());
        proposal.setStatus(proposalDTO.getStatus());
        proposal.setManfaat(proposalDTO.getManfaat());
        proposal.setUploadRevisi(proposalDTO.getUploadRevisi());

        // Set userId, periodeId, dosenId
        UserEntity user = userRepository.findById(proposalDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        proposal.setUserId(user);
        logger.info("biji 3");
        PeriodeKolokiumEntity periode = periodeRepository.findById(proposalDTO.getPeriodeId())
                .orElseThrow(() -> new RuntimeException("Periode tidak ditemukan"));
        proposal.setPeriodes(periode);
        logger.info("biji 4");
        DosenEntity dosen = dosenRepository.findById(Integer.parseInt(proposalDTO.getDosenId()));
        proposal.setDosenId(dosen);
        logger.info("biji 5");
        // Handle file upload
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            String fileDir = "C:\\xampp\\htdocs\\uploads"; // Path untuk menyimpan file
            proposal.setFilename(fileName);
            proposal.setFiledir(fileDir);
            proposal.setMimeType(file.getContentType());
            Files.copy(file.getInputStream(), Paths.get(fileDir).resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
        logger.info("biji 6");
        // Tambahkan waktu 7 jam ke tanggal sekarang
        LocalDateTime now = LocalDateTime.now().plusHours(7);
        proposal.setModified(java.sql.Timestamp.valueOf(now)); // Konversi ke java.sql.Timestamp

        // Save the proposal
        ProposalEntity savedProposal = proposalRepository.save(proposal);
        logger.info("biji 7");
        return savedProposal;
    }



    // READ ALL
    public List<ProposalEntity> getAllProposals() {
        return proposalRepository.findAll();
    }

    // READ BY ID
    public Optional<ProposalEntity> getProposalById(Long id) {
        return proposalRepository.findById(id);
    }

    // SELECT Proposal berdasarkan judul
    public List<ProposalEntity> selectByNameProposal(String name) {
        return proposalRepository.findByJudulContainingIgnoreCase(name);
    }

    // UPDATE
    public ProposalEntity updateProposal(Long id, ProposalDTO proposalDTO, MultipartFile file) throws Exception {
        Optional<ProposalEntity> proposalOpt = proposalRepository.findById(id);

        if (proposalOpt.isPresent()) {
            ProposalEntity proposalEntity = proposalOpt.get();

            // Cari MahasiswaEntity berdasarkan nim
            MahasiswaEntity mahasiswa = mahasiswaRepository.findById(proposalDTO.getNim())
                    .orElseThrow(() -> new BadRequestException("Mahasiswa dengan NIM " + proposalDTO.getNim() + " tidak ditemukan"));
            proposalEntity.setMahasiswa(mahasiswa); // Set relasi MahasiswaEntity

            proposalEntity.setJudul(proposalDTO.getJudul());
            proposalEntity.setLatarBelakang(proposalDTO.getLatarBelakang());
            proposalEntity.setRumusan(proposalDTO.getRumusan());
            proposalEntity.setBatasan(proposalDTO.getBatasan());
            proposalEntity.setTujuan(proposalDTO.getTujuan());
            proposalEntity.setReferensi(proposalDTO.getReferensi());
            proposalEntity.setStatus(proposalDTO.getStatus());
            proposalEntity.setManfaat(proposalDTO.getManfaat());

            DosenEntity dosen = dosenRepository.findById(Integer.parseInt(proposalDTO.getDosenId()));

            proposalEntity.setDosenId(dosen);

            proposalEntity.setUploadRevisi(proposalDTO.getUploadRevisi());

            // Tambahkan waktu 7 jam ke tanggal sekarang
            LocalDateTime now = LocalDateTime.now().plusHours(7);
            proposalEntity.setModified(java.sql.Timestamp.valueOf(now)); // Konversi ke java.sql.Timestamp

            // Handle file upload
            if (file != null && !file.isEmpty()) {
                String uploadDir = "C:\\xampp\\htdocs\\uploads\\"; // Lokasi folder penyimpanan
                String filename = file.getOriginalFilename(); // Nama file
                String filePath = uploadDir + filename; // Path lengkap untuk file

                // Perbarui atribut sesuai dengan kebutuhan
                proposalEntity.setFilename(filename); // Hanya nama file
                proposalEntity.setFiledir(uploadDir); // Hanya direktori (tanpa nama file)
                proposalEntity.setMimeType(file.getContentType()); // MIME type dari file

                Path path = Paths.get(filePath);
                Files.createDirectories(path.getParent()); // Membuat direktori jika belum ada
                Files.write(path, file.getBytes()); // Menulis file ke path yang benar
            }

            return proposalRepository.save(proposalEntity);
        }

        return null; // Kembalikan null jika proposal tidak ditemukan
    }

    // DELETE
    public void deleteProposal(Long id) throws Exception {
        Optional<ProposalEntity> proposalOpt = proposalRepository.findById(id);

        if (proposalOpt.isPresent()) {
            ProposalEntity proposalEntity = proposalOpt.get();

            // Hapus file dari filesystem jika ada
            String uploadDir = proposalEntity.getFiledir();
            String filename = proposalEntity.getFilename(); // Nama file dari entitas
            if (filename != null) {
                Path filePath = Paths.get(uploadDir, filename); // Gabungkan folder path dengan nama file
                System.out.println("Path file untuk dihapus: " + filePath);

                if (Files.exists(filePath)) {
                    try {
                        Files.delete(filePath); // Hapus file
                        System.out.println("File berhasil dihapus: " + filePath);
                    } catch (Exception e) {
                        throw new Exception("Gagal menghapus file: " + filePath, e);
                    }
                } else {
                    System.out.println("File tidak ditemukan di path: " + filePath);
                }
            }

            // Hapus entitas dari database
            proposalRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Proposal dengan ID " + id + " tidak ditemukan.");
        }
    }

    public List<ProposalEntity> selectByNim(String nim) {
        return proposalRepository.findByNimContainingIgnoreCase(nim);
    }

    public List<ProposalEntity> selectByDosenId(Long dosenId) {
        return proposalRepository.findByDosenId_Id(dosenId);
    }

    public ProposalEntity updateProposalStatus(Long id, String status) throws Exception {
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Status tidak valid: " + status);
        }

        ProposalEntity proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proposal tidak ditemukan dengan ID: " + id));

        // Update status
        proposal.setStatus(status);

        // Tambahkan waktu 7 jam ke tanggal sekarang untuk modified
        LocalDateTime now = LocalDateTime.now().plusHours(7);
        proposal.setModified(java.sql.Timestamp.valueOf(now));

        return proposalRepository.save(proposal);
    }

    public List<ProposalEntity> getAllProposalsSortedByModifiedDesc() {
        return proposalRepository.findAllByOrderByModifiedDesc();
    }
    public Resource downloadFile(Long id) throws Exception {
        ProposalEntity proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proposal tidak ditemukan dengan ID: " + id));

        String fileDir = proposal.getFiledir(); // Direktori file
        String fileName = proposal.getFilename(); // Nama file

        if (fileDir == null || fileName == null) {
            throw new BadRequestException("File tidak ditemukan untuk proposal dengan ID: " + id);
        }

        Path filePath = Paths.get(fileDir, fileName); // Gabungkan path direktori dan nama file
        if (!Files.exists(filePath)) {
            throw new BadRequestException("File tidak ditemukan di path: " + filePath);
        }

        try {
            return new UrlResource(filePath.toUri()); // Kembalikan file sebagai resource
        } catch (Exception e) {
            throw new BadRequestException("Gagal mengakses file: " + filePath, e);
        }
    }

    public String getFilenameById(Long id) throws Exception {
        ProposalEntity proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proposal tidak ditemukan dengan ID: " + id));

        String fileDir = proposal.getFiledir(); // Direktori file
        String fileName = proposal.getFilename(); // Nama file

        if (fileDir == null || fileName == null) {
            return null; // Return null if either fileDir or filename is null
        }

        Path filePath = Paths.get(fileDir, fileName); // Gabungkan path direktori dan nama file
        if (Files.exists(filePath)) {
            return fileName; // Return the filename if the file exists
        } else {
            return null; // Return null if the file doesn't exist
        }
    }

    public Optional<ProposalEntity> getProposalByIdAndNim(Long id, String nim) {
        return proposalRepository.findByIdAndNim(id, nim);
    }

    public Optional<ProposalEntity> getProposalByIdAndNidn(Long id, String nidn) {
        return proposalRepository.findByIdAndNidn(id, nidn);
    }

    public List<ProposalEntity> findProposalsByYear(int year) {
        return proposalRepository.findProposalsByYear(year);
    }


    public List<ProposalEntity> findProposalsByPeriodeIdAndStatusExcludingDeskEvaluation(Long periodeId, String status) {
        // Query proposals by periodeId and status
        List<ProposalEntity> proposals = proposalRepository.findProposalsByPeriodeIdAndStatus(periodeId, status);

        // Query all proposal IDs in DeskEvaluation
        List<Long> excludedProposalIds = deskEvaluationRepository.findAllProposalIdsInDeskEvaluation();

        // Filter proposals to exclude those present in DeskEvaluation
        return proposals.stream()
                .filter(p -> !excludedProposalIds.contains(p.getId()))
                .collect(Collectors.toList());
    }
}
