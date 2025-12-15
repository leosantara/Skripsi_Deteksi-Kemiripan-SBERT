package org.ukdw.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.ukdw.dto.response.similarity.ClusterDetailDTO;
import org.ukdw.dto.response.similarity.IndexingStatusDTO;
import org.ukdw.dto.ProposalDTO;
import org.ukdw.dto.request.similarity.ReindexRequestDTO;
import org.ukdw.dto.kolokium.*;
import org.ukdw.dto.request.proposal.ProposalEditStatusRequest;
import org.ukdw.dto.request.proposal.ProposalSearchByDosenIdRequest;
import org.ukdw.dto.request.proposal.ProposalSearchByNimRequest;
import org.ukdw.dto.request.proposal.ProposalSearchRequest;
import org.ukdw.dto.request.account.RoleAccountRequest;
import org.ukdw.dto.request.account.StatusAccountRequest;
import org.ukdw.dto.kolokium.DosenEvaluatorDetailDTO;
import org.ukdw.dto.kolokium.StatusDosenEvaluatorDTO;
import org.ukdw.dto.kolokium.GroupEvaluatorDTO;
import org.ukdw.dto.kolokium.DosenEvaluatorDTO;
import org.ukdw.dto.request.seminar.JadwalSeminarByTanggalRequestDTO;
import org.ukdw.dto.response.ResponseWrapper;
import org.ukdw.entity.*;
import org.ukdw.repository.DeskEvaluationRepository;
import org.ukdw.services.*;
import org.springframework.http.HttpHeaders;


import java.util.*;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
// Restricted endpoint, accessible only to users with the role "ADMIN"
@PreAuthorize("hasRole(T(org.ukdw.entity.AuthoritiesConstants).ROLE_ADMINISTRATOR.name())")
@RequestMapping("/admin")
@SecurityRequirement(name = "bearer-key")
public class AdminController
{
    private final DosenService dosenService;
    private final MahasiswaService mahasiswaService;
    private final ProposalService proposalService;
    private final KolokiumService kolokiumService;
    private final UserRoleService userRoleService;
    private final UserAccountService userAccountService;
    private final DeskEvaluationService deskEvaluationService;
    private final KolokiumService periodeKolokiumService;
    private final ModelMapper modelMapper;
    private final PeriodeSeminarService periodeSeminarService;
    private final JadwalSeminarService jadwalSeminarService;
    private final JadwalSeminarDosenService jadwalSeminarDosenService;
    private final SeminarService seminarService;
    private final SimilarityService similarityService;


    // Endpoint untuk POST request (menambahkan dosen)
    @PostMapping("/data-dosen")
    public ResponseEntity<?> createDosen(@RequestBody DosenEntity dosen) {
        DosenEntity savedDosen = dosenService.save(dosen);
        ResponseWrapper<DosenEntity> response;
        if (savedDosen != null) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(),"Dosen succesfull added", savedDosen);
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Dosen dengan NIDN " + dosen.getNidn() + " is already in the database", null);
        }
        return ResponseEntity.ok(response);
    }

    // GET request untuk mendapatkan semua dosen
    @GetMapping("/data-dosen")
    public ResponseEntity<?> findAllDosen() {
        Iterable<DosenEntity> dosenList = dosenService.findAllDosen();
        ResponseWrapper<Iterable<DosenEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), dosenList);
        return ResponseEntity.ok(response);
    }

    // GET request untuk mendapatkan dosen berdasarkan NIDN
    @GetMapping("/data-dosen/{nidn}")
    public ResponseEntity<?> findByNidn(@PathVariable String nidn) {
        DosenEntity dosen = dosenService.findByNidn(nidn);
        ResponseWrapper<DosenEntity> response;
        if (dosen != null) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), dosen);
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Dosen dengan NIDN " + nidn + " tidak ditemukan", null);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/data-dosen/id/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        Optional<DosenEntity> dosen = dosenService.getDosenById(id);
        ResponseWrapper<DosenEntity> response;
        if (dosen.isPresent()) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), dosen.get());
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Dosen dengan ID " + id + " tidak ditemukan", null);
        }
        return ResponseEntity.ok(response);
    }


    // DELETE request untuk menghapus dosen berdasarkan NIDN
    @DeleteMapping("/data-dosen/{nidn}")
    public ResponseEntity<?> deleteByNidn(@PathVariable String nidn) {
        DosenEntity dosen = dosenService.findByNidn(nidn);
        ResponseWrapper<String> response;
        if (dosen != null) {
            dosenService.deleteByNidn(nidn);
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Dosen dengan NIDN " + nidn + " telah dihapus", null);
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Dosen dengan NIDN " + nidn + " tidak ditemukan", null);
        }
        return ResponseEntity.ok(response);
    }

    // PUT request untuk mengupdate data dosen
    @PutMapping("/data-dosen/{nidn}")
    public ResponseEntity<?> updateDosen(@PathVariable String nidn, @RequestBody DosenEntity updatedDosen) {
        DosenEntity updatedDosenEntity = dosenService.updateDosen(nidn, updatedDosen);
        ResponseWrapper<DosenEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Dosen berhasil diupdate", updatedDosenEntity);
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk POST request (menambahkan mahasiswa)
    @PostMapping("/data-mahasiswa")
    public ResponseEntity<?> createMahasiswa(@RequestBody MahasiswaEntity mahasiswa) {
        MahasiswaEntity savedMahasiswa = mahasiswaService.save(mahasiswa);
        ResponseWrapper<MahasiswaEntity> response;
        if (savedMahasiswa != null) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), savedMahasiswa);
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Mahasiswa dengan NIM " + mahasiswa.getNim() + " is already in the database", null);
        }
        return ResponseEntity.ok(response);
    }

    // GET request untuk mendapatkan semua mahasiswa
    @GetMapping("/data-mahasiswa")
    public ResponseEntity<?> findAllMahasiswa() {
        Iterable<MahasiswaEntity> mahasiswaList = mahasiswaService.findAllMahasiswa();
        ResponseWrapper<Iterable<MahasiswaEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), mahasiswaList);
        return ResponseEntity.ok(response);
    }

    // GET request untuk mendapatkan mahasiswa berdasarkan NIM
    @GetMapping("/data-mahasiswa/{nim}")
    public ResponseEntity<?> findByNim(@PathVariable String nim) {
        MahasiswaEntity mahasiswa = mahasiswaService.findByNim(nim);
        ResponseWrapper<MahasiswaEntity> response;
        if (mahasiswa != null) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), mahasiswa);
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Mahasiswa dengan NIM " + nim + " tidak ditemukan", null);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/data-mahasiswa/{nim}")
    public ResponseEntity<?> deleteByNim(@PathVariable String nim) {
        MahasiswaEntity mahasiswa = mahasiswaService.findByNim(nim);
        ResponseWrapper<String> response;
        if (mahasiswa != null) {
            mahasiswaService.deleteByNim(nim);
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Mahasiswa dengan NIM " + nim + " telah dihapus", null);
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Mahasiswa dengan NIM " + nim + " tidak ditemukan", null);
        }
        return ResponseEntity.ok(response);
    }

    // CREATE
    @PostMapping("/proposal/submit")
    public ResponseEntity<ResponseWrapper<String>> uploadProposal(@ModelAttribute ProposalDTO proposalDTO, @RequestParam("file") MultipartFile file) {
        try {
            proposalService.saveProposal(proposalDTO, file);
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Proposal berhasil disimpan", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Gagal menyimpan proposal", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/system/update-ai")
    public ResponseEntity<ResponseWrapper<String>> updateAiData() {
        String result = similarityService.triggerUpdateAi();

        if (result == null) {
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), "Tidak ada data proposal valid", null));
        }
        return ResponseEntity.ok(new ResponseWrapper<>(HttpStatus.OK.value(), "Re-indexing triggered", result));
    }

    @GetMapping("/system/indexing-status")
    public ResponseEntity<ResponseWrapper<IndexingStatusDTO>> getIndexingStatus() {
        IndexingStatusDTO status = similarityService.getIndexingStatus();
        return ResponseEntity.ok(new ResponseWrapper<>(HttpStatus.OK.value(), "Status fetched", status));
    }

    @GetMapping("/system/check-ai-data")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> debugAiData() {
        Map<String, Object> report = similarityService.getDebugAiData();
        return ResponseEntity.ok(new ResponseWrapper<>(HttpStatus.OK.value(), "Debug data fetched", report));
    }

    @GetMapping("/system/clusters")
    public ResponseEntity<ResponseWrapper<Map<String, ClusterDetailDTO>>> getClusterOverview() {
        Map<String, ClusterDetailDTO> result = similarityService.getClusterOverview();
        return ResponseEntity.ok(new ResponseWrapper<>(HttpStatus.OK.value(), "Cluster data fetched", result));
    }

    // READ ALL
    @GetMapping("/proposal/getall")
    public ResponseEntity<ResponseWrapper<List<ProposalEntity>>> getAllProposals() {
        List<ProposalEntity> proposals = proposalService.getAllProposals();
        ResponseWrapper<List<ProposalEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Data proposal berhasil diambil", proposals);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/proposal/getalldesc")
    public ResponseEntity<ResponseWrapper<List<ProposalEntity>>> getAllProposalsDesc() {
        List<ProposalEntity> proposals = proposalService.getAllProposalsSortedByModifiedDesc();
        ResponseWrapper<List<ProposalEntity>> response = new ResponseWrapper<>(
                HttpStatus.OK.value(),
                "Data fetched successfully",
                proposals
        );
        return ResponseEntity.ok(response);
    }

    // READ BY ID
    @GetMapping("/proposal/{id}")
    public ResponseEntity<ResponseWrapper<ProposalEntity>> getProposalById(@PathVariable Long id) {
        Optional<ProposalEntity> proposal = proposalService.getProposalById(id);
        ResponseWrapper<ProposalEntity> response;
        if (proposal.isPresent()) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Proposal ditemukan", proposal.get());
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Proposal tidak ditemukan", null);
        }
        return ResponseEntity.ok(response);
    }
    // PUT request untuk update mahasiswa berdasarkan NIM
    @PutMapping("/data-mahasiswa/{nim}")
    public ResponseEntity<?> updateMahasiswa(@PathVariable String nim, @RequestBody MahasiswaEntity mahasiswa) {
        MahasiswaEntity updatedMahasiswa = mahasiswaService.updateMahasiswa(nim, mahasiswa);
        ResponseWrapper<MahasiswaEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Mahasiswa berhasil diupdate", updatedMahasiswa);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list-group-evaluator")
    public ResponseEntity<?> getAllKelompokDE() {
        List<GroupEvaluatorDTO> kelompokList = kolokiumService.getAllKelompokDE();
        ResponseWrapper<List<GroupEvaluatorDTO>> response = new ResponseWrapper<>(HttpStatus.OK.value(), kelompokList);
        return ResponseEntity.ok(response);
    }

    // Get Kelompok DE by ID
    @GetMapping("/group-evaluator/{id}")
    public ResponseEntity<?> getKelompokDEById(@PathVariable Long id) {
        Optional<GroupEvaluatorDTO> kelompokDE = kolokiumService.getKelompokDEById(id);
        ResponseWrapper<GroupEvaluatorDTO> response;
        if (kelompokDE.isPresent()) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), kelompokDE.get());
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Kelompok DE dengan ID " + id + " tidak ditemukan", null);
        }
        return ResponseEntity.ok(response);
    }

    // SELECT BY NAME PROPOSAL (menggunakan JSON)
    @PostMapping("/proposal/search")
    public ResponseEntity<ResponseWrapper<List<ProposalEntity>>> searchByNameProposal(@RequestBody ProposalSearchRequest searchDTO) {
        List<ProposalEntity> proposals = proposalService.selectByNameProposal(searchDTO.getJudul());
        ResponseWrapper<List<ProposalEntity>> response;

        if (proposals.isEmpty()) {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Tidak ada proposal ditemukan", null);
        } else {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Proposal ditemukan", proposals);
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/proposal/search/nim")
    public ResponseEntity<ResponseWrapper<List<ProposalEntity>>> searchByNim(@RequestBody ProposalSearchByNimRequest searchRequest) {
        List<ProposalEntity> proposals = proposalService.selectByNim(searchRequest.getNim());

        ResponseWrapper<List<ProposalEntity>> response;
        if (proposals.isEmpty()) {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Tidak ada proposal ditemukan untuk NIM tersebut", null);
        } else {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Proposal ditemukan", proposals);
        }

        return ResponseEntity.ok(response);
    }


    // UPDATE
    @PutMapping("/proposal/{id}")
    public ResponseEntity<ResponseWrapper<String>> updateProposal(@PathVariable Long id,
                                                                  @ModelAttribute ProposalDTO proposalDTO,
                                                                  @RequestParam("file") MultipartFile file) {
        ResponseWrapper<String> response;
        try {
            ProposalEntity updatedProposal = proposalService.updateProposal(id, proposalDTO, file);
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Proposal berhasil diperbarui", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Proposal tidak ditemukan", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    // DELETE
    @DeleteMapping("/proposal/{id}")
    public ResponseEntity<ResponseWrapper<String>> deleteProposal(@PathVariable Long id) {
        try {
            proposalService.deleteProposal(id);
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Proposal berhasil dihapus", null);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Gagal menghapus file: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/proposal/search-by-dosenid")
    public ResponseEntity<ResponseWrapper<List<ProposalEntity>>> searchByDosenId(@RequestBody ProposalSearchByDosenIdRequest searchRequest) {
        List<ProposalEntity> proposals = proposalService.selectByDosenId(searchRequest.getDosenId());

        ResponseWrapper<List<ProposalEntity>> response;
        if (proposals.isEmpty()) {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Tidak ada proposal ditemukan untuk dosen tersebut", null);
        } else {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Proposal ditemukan", proposals);
        }

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/proposal/update-status/{id}")
    public ResponseEntity<ResponseWrapper<ProposalEntity>> updateProposalStatus(
            @PathVariable Long id, @RequestBody ProposalEditStatusRequest request) {

        String status = request.getStatus();
        try {
            ProposalEntity updatedProposal = proposalService.updateProposalStatus(id, status);
            return ResponseEntity.ok(new ResponseWrapper<>(HttpStatus.OK.value(), "Status proposal berhasil diperbarui", updatedProposal));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Gagal memperbarui status proposal", null));
        }
    }

    @GetMapping("/proposal/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws Exception {
        Resource file = proposalService.downloadFile(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(file);
    }

    @GetMapping("/proposal/{id}/filename")
    public ResponseEntity<ResponseWrapper<String>> getFilenameById(@PathVariable Long id) throws Exception {
        String filename = proposalService.getFilenameById(id);

        if (filename != null) {
            // File exists, return filename wrapped in ResponseWrapper
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.OK.value(), "File found", filename);
            return ResponseEntity.ok(response);
        } else {
            // File does not exist, return 404 with appropriate message
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "File not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/proposal/mahasiswa")
    public ResponseEntity<?> getProposalByIdAndNim(@RequestBody ProposalRequestDTO request) {
        var proposalOptional = proposalService.getProposalByIdAndNim(request.getId(), request.getNim());
        if (proposalOptional.isPresent()) {
            ProposalEntity proposal = proposalOptional.get();
            ResponseWrapper<ProposalEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), proposal);
            return ResponseEntity.ok(response);
        } else {
            ResponseWrapper<?> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Proposal not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/proposal/dosen")
    public ResponseEntity<?> getProposalByIdAndNidn(@RequestBody ProposalRequestDTO request) {
        var proposalOptional = proposalService.getProposalByIdAndNidn(request.getId(), request.getNidn());
        if (proposalOptional.isPresent()) {
            ProposalEntity proposal = proposalOptional.get();
            ResponseWrapper<ProposalEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), proposal);
            return ResponseEntity.ok(response);
        } else {
            ResponseWrapper<?> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Proposal not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/proposal/by-year")
    public ResponseEntity<?> getProposalsByYear(@RequestBody ProposalByYearRequestDTO requestDTO) {
        List<ProposalEntity> proposals = proposalService.findProposalsByYear(requestDTO.getYear());
        if (!proposals.isEmpty()) {
            ResponseWrapper<List<ProposalEntity>> response = new ResponseWrapper<>(
                    HttpStatus.OK.value(),
                    proposals
            );
            return ResponseEntity.ok(response);
        } else {
            ResponseWrapper<?> response = new ResponseWrapper<>(
                    HttpStatus.NOT_FOUND.value(),
                    "No proposals found for the specified year"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/proposal/by-periode-and-status")
    public ResponseEntity<ResponseWrapper<?>> getFilteredProposals(
            @RequestBody ProposalByPeriodeAndStatusRequestDTO requestDTO) {
        // Ambil parameter dari DTO
        Long periodeId = requestDTO.getPeriodeId();
        String status = requestDTO.getStatus();

        // Panggil service untuk mendapatkan hasil filter
        List<ProposalEntity> proposals = proposalService.findProposalsByPeriodeIdAndStatusExcludingDeskEvaluation(periodeId, status);

        // Periksa apakah hasil kosong
        if (proposals.isEmpty()) {
            ResponseWrapper<?> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "No proposals found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Jika ada hasil, bungkus dalam ResponseWrapper
        ResponseWrapper<List<ProposalEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), proposals);
        return ResponseEntity.ok(response);
    }




    // Get Kelompok DE by Periode ID
    @GetMapping("group-evaluator/periode/{periodeId}")
    public ResponseEntity<?> getKelompokDEByPeriode(@PathVariable Long periodeId) {
        List<DosenEvaluatorDetailDTO> kelompokList = kolokiumService.getKelompokDEByPeriode(new PeriodeKolokiumEntity(periodeId));
        ResponseWrapper<List<DosenEvaluatorDetailDTO>> response = new ResponseWrapper<>(HttpStatus.OK.value(), kelompokList);
        return ResponseEntity.ok(response);
    }

    // Create new Kelompok DE
    @PostMapping("group-evaluator/")
    public ResponseEntity<?> createKelompokDE(@RequestBody GroupEvaluatorDTO kelompokDEDTO) {
        GroupEvaluatorEntity kelompokDEEntity = kolokiumService.convertToEntity(kelompokDEDTO);
        GroupEvaluatorEntity createdKelompok = kolokiumService.createKelompokDE(kelompokDEEntity);
        GroupEvaluatorDTO createdDto = kolokiumService.convertToDto(createdKelompok);
        ResponseWrapper<GroupEvaluatorDTO> response = new ResponseWrapper<>(HttpStatus.CREATED.value(), createdDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("group-evaluator/dosen-evaluator/nidn/{nidn}")
    public ResponseEntity<?> getGroupEvaluatorByDosenNidn(@PathVariable String nidn) {
        List<DosenEvaluatorDetailDTO> dtos = kolokiumService.getGroupEvaluatorByDosenNidn(nidn);
        ResponseWrapper<List<DosenEvaluatorDetailDTO>> response = new ResponseWrapper<>(HttpStatus.OK.value(), dtos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("group-evaluator/dosen-evaluator/periode-and-nidn")
    public ResponseEntity<?> getDosenEvaluatorsByPeriodeIdAndNidn(
            @RequestParam Long periodeId,
            @RequestParam String nidn) {
        List<DosenEvaluatorDetailDTO> dtos = kolokiumService.getGroupEvaluatorByPeriodeIdAndDosenNidn(periodeId, nidn);
        ResponseWrapper<List<DosenEvaluatorDetailDTO>> response = new ResponseWrapper<>(HttpStatus.OK.value(), dtos);
        return ResponseEntity.ok(response);
    }

    // Update Kelompok DE
    @PutMapping("group-evaluator/{id}")
    public ResponseEntity<?> updateKelompokDE(@PathVariable Long id, @RequestBody GroupEvaluatorDTO updatedKelompokDEDTO) {
        GroupEvaluatorEntity updatedKelompokDEEntity = kolokiumService.convertToEntity(updatedKelompokDEDTO);
        GroupEvaluatorEntity updatedKelompok = kolokiumService.updateKelompokDE(id, updatedKelompokDEEntity);
        GroupEvaluatorDTO updatedDto = kolokiumService.convertToDto(updatedKelompok);
        ResponseWrapper<GroupEvaluatorDTO> response = new ResponseWrapper<>(HttpStatus.OK.value(), updatedDto);
        return ResponseEntity.ok(response);
    }

    // Delete Kelompok DE by ID
    @DeleteMapping("group-evaluator/{id}")
    public ResponseEntity<?> deleteKelompokDE(@PathVariable Long id) {
        kolokiumService.deleteKelompokDE(id);
        ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NO_CONTENT.value(), "Kelompok DE "+id+" berhasil dihapus");
        return ResponseEntity.ok(response);
    }

    //Update dosen pada kelompok DE
    @PostMapping("group-evaluator/dosen-evaluator")
    public ResponseEntity<?> updateDosenSidang(@RequestBody DosenEvaluatorDTO request) {
        List<StatusDosenEvaluatorDTO> updatedDosenList = kolokiumService.updateDosenEvaluator( request.getSidangId(), request.getKetuaId(), request.getAnggotaIds());
        ResponseWrapper<List<StatusDosenEvaluatorDTO>> response = new ResponseWrapper<>(HttpStatus.OK.value(), updatedDosenList);
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk mendapatkan seluruh dosen evaluator berdasarkan sidangId
    @GetMapping("group-evaluator/dosen-evaluator/sidang/{sidangId}")
    public ResponseEntity<?> getDosenEvaluatorsBySidangId(@PathVariable Long sidangId) {
        DosenEvaluatorDetailDTO dto = kolokiumService.getDosenEvaluatorsBySidangId(sidangId);
        ResponseWrapper<DosenEvaluatorDetailDTO> response = new ResponseWrapper<>(HttpStatus.OK.value(), dto);
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk mendapatkan seluruh dosen evaluator dari semua sidang
    @GetMapping("group-evaluator/dosen-evaluator")
    public ResponseEntity<?> getAllDosenEvaluators() {
        List<DosenEvaluatorDetailDTO> allEvaluators = kolokiumService.getAllDosenEvaluators();
        ResponseWrapper<List<DosenEvaluatorDetailDTO>> response = new ResponseWrapper<>(HttpStatus.OK.value(), allEvaluators);
        return ResponseEntity.ok(response);
    }

    @GetMapping("group-evaluator/dosen-evaluator/id/{dosenId}")
    public ResponseEntity<?> getDosenEvaluatorByDosen(@PathVariable long dosenId) {
        List<DosenEvaluatorDetailDTO> groupDosenEvaluator = kolokiumService.getGroupEvaluatorByDosenId(dosenId);
        ResponseWrapper<List<DosenEvaluatorDetailDTO>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Group Desk Evaluations retrieved successfully", groupDosenEvaluator);
        return ResponseEntity.ok(response);
    }

    @GetMapping("group-evaluator/periode-and-kelompok")
    public ResponseEntity<?> getGroupEvaluatorsByPeriodeIdAndKelompok(
            @RequestParam Long periodeId,
            @RequestParam GroupEvaluatorEntity.Kelompok kelompok) {
        // Memanggil metode service melalui instance
        List<GroupEvaluatorEntity> groupEvaluators = kolokiumService.getGroupEvaluatorsByPeriodeIdAndKelompok(periodeId, kelompok);
        ResponseWrapper<List<GroupEvaluatorEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Group Desk Evaluations retrieved successfully", groupEvaluators);
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk memperbarui groups
    @PutMapping("/data-user/role")
    public ResponseEntity<?> updateUserGroups(@RequestBody @Valid RoleAccountRequest request) {
        UserEntity updatedUser = userRoleService.updateUserGroups(request, new HashSet<>(request.getGroupIds()));
        ResponseWrapper<UserEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), updatedUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/data-user/status")
    public ResponseEntity<?> updateUserStatus(@Valid @RequestBody StatusAccountRequest request) {
        UserEntity updatedUser = userAccountService.updateUserStatus(request);
        ResponseWrapper<UserEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), updatedUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("desk-evaluation")
    public ResponseEntity<?> createDeskEvaluation(@RequestBody DeskEvaluationDTO dto) {
        DeskEvaluationEntity savedDeskEvaluation = deskEvaluationService.saveDeskEvaluation(dto);
        ResponseWrapper<DeskEvaluationEntity> response = new ResponseWrapper<>(HttpStatus.CREATED.value(), "Desk Evaluation created successfully", savedDeskEvaluation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("desk-evaluation/periode/{periodeId}/kelompok/{kelompok}")
    public ResponseEntity<?> getDeskEvaluationByPeriodeAndKelompok(
            @PathVariable Long periodeId, @PathVariable String kelompok) {
        // Validasi nilai kelompok sebagai enum
        GroupEvaluatorEntity.Kelompok enumKelompok;
        enumKelompok = GroupEvaluatorEntity.Kelompok.valueOf(kelompok.toUpperCase());
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.getDeskEvaluationByPeriodeAndKelompok(periodeId, enumKelompok);
        ResponseWrapper< List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.CREATED.value(), "Desk Evaluation Data Retrived successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation/year/{year}")
    public ResponseEntity<?> getDeskEvaluationByYear(@PathVariable int year) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.getDeskEvaluationByYear(year);
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.CREATED.value(), "Desk Evaluation Data Retrived successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation")
    public ResponseEntity<?> getAllDeskEvaluations() {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.getAllDeskEvaluations();
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation/{id}")
    public ResponseEntity<?> getDeskEvaluationById(@PathVariable Integer id) {
        DeskEvaluationEntity deskEvaluation = deskEvaluationService.getDeskEvaluationById(id);
        if (deskEvaluation != null) {
            ResponseWrapper<DeskEvaluationEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluation retrieved successfully", deskEvaluation);
            return ResponseEntity.ok(response);
        } else {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Desk Evaluation not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    @GetMapping("desk-evaluation/periode/id/{periodeId}")
    public ResponseEntity<?> getDeskEvaluationsByPeriodeId(@PathVariable Long periodeId) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.getDeskEvaluationsByPeriodeId(periodeId);
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }
    //Periodes
    @PutMapping("/periodes/{id}")
    public ResponseEntity<?> updatePeriodeKolokium(@PathVariable Long id, @RequestBody PeriodeKolokiumEntity updatedPeriodeKolokium) {
        PeriodeKolokiumEntity updatedEntity = kolokiumService.updatePeriodeKolokium(id, updatedPeriodeKolokium);

        ResponseWrapper<PeriodeKolokiumEntity> response;
        if (updatedEntity != null) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Periode Kolokium berhasil diperbarui", updatedEntity);
            return ResponseEntity.ok(response);
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Periode Kolokium tidak ditemukan", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/periodes/create")
    public ResponseEntity<?> create(@RequestBody PeriodeKolokiumDTO periodeKolokiumDTO) {
        // Konversi DTO ke Entity menggunakan ModelMapper
        PeriodeKolokiumEntity periodeKolokiumEntity = modelMapper.map(periodeKolokiumDTO, PeriodeKolokiumEntity.class);

        // Simpan data ke database
        PeriodeKolokiumEntity savedPeriode = periodeKolokiumService.saveOrUpdate(periodeKolokiumEntity);

        // Kembalikan response dengan entity yang disimpan
        ResponseWrapper<PeriodeKolokiumEntity> response = new ResponseWrapper<>(HttpStatus.CREATED.value(), "Periode Kolokium berhasil dibuat", savedPeriode);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/periodes/{id}")
    public ResponseEntity<?> getPeriodeById(@PathVariable Long id) {
        PeriodeKolokiumEntity periodeKolokiumEntity = periodeKolokiumService.findById(id)
                .orElse(null);

        ResponseWrapper<?> response;
        if (periodeKolokiumEntity != null) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Periode ditemukan", periodeKolokiumEntity);
            return ResponseEntity.ok(response);
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Periode tidak ditemukan", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("desk-evaluation/{id}")
    public ResponseEntity<?> deleteDeskEvaluation(@PathVariable Integer id) {
        deskEvaluationService.deleteDeskEvaluation(id);
        ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NO_CONTENT.value(), "Desk Evaluation " + id + " deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation/mahasiswa/{nim}")
    public ResponseEntity<?> getDeskEvaluationByNim(@PathVariable("nim") String nim) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.findByMahasiswaNim(nim);
        if (deskEvaluations.isEmpty()) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Desk Evaluation not found for NIM: " + nim);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation/dosen-evaluator/{evaluatorId}")
    public ResponseEntity<?> getDeskEvaluationByEvaluatorId(@PathVariable("evaluatorId") Integer evaluatorId) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.findByEvaluatorId(evaluatorId);
        if (deskEvaluations.isEmpty()) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Desk Evaluation not found for evaluator ID: " + evaluatorId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation/dosen-pembimbing/1/id/{dosen1Id}")
    public ResponseEntity<?> getDeskEvaluationByDosen1Id(@PathVariable("dosen1Id") Integer dosen1Id) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.findByDosen1Id(dosen1Id);
        if (deskEvaluations.isEmpty()) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Desk Evaluation not found for dosen1 ID: " + dosen1Id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation/dosen-pembimbing/1/nidn/{dosen1Nidn}")
    public ResponseEntity<?> getDeskEvaluationByDosen1Nidn(@PathVariable("dosen1Nidn") String dosen1Ndin) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.findByDosen1Nidn(dosen1Ndin);
        if (deskEvaluations.isEmpty()) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Desk Evaluation not found for dosen1 NIDN: " + dosen1Ndin);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation/dosen-pembimbing/2/id/{dosen2Id}")
    public ResponseEntity<?> getDeskEvaluationByDosen2Id(@PathVariable("dosen2Id") Integer dosen2Id) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.findByDosen2Id(dosen2Id);
        if (deskEvaluations.isEmpty()) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Desk Evaluation not found for dosen2 ID: " + dosen2Id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation/dosen-pembimbing/2/nidn/{dosen2Nidn}")
    public ResponseEntity<?> getDeskEvaluationByDosen2Nidn(@PathVariable("dosen2Nidn") String dosen2Nidn) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.findByDosen2Nidn(dosen2Nidn);
        if (deskEvaluations.isEmpty()) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Desk Evaluation not found for dosen2 NIDN: " + dosen2Nidn);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation/dosen-pembimbing/id/{dosenId}")
    public ResponseEntity<?> getDeskEvaluationsByDosen1OrDosen2(@PathVariable Integer dosenId) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.findByDosen1OrDosen2(dosenId);
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("desk-evaluation/dosen-pembimbing/nidn/{dosenNidn}")
    public ResponseEntity<?> getDeskEvaluationsByDosen1OrDosen2Nidn(@PathVariable String dosenNidn) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.findByDosen1OrDosen2Nidn(dosenNidn);
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @PutMapping("desk-evaluation/{id}")
    public ResponseEntity<?> updateDeskEvaluation(
            @PathVariable Integer id, @RequestBody DeskEvaluationDTO dto) {
        DeskEvaluationEntity updatedDeskEvaluation = deskEvaluationService.updateDeskEvaluation(id, dto);
        ResponseWrapper<DeskEvaluationEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(),"Desk Evaluation successfully added", updatedDeskEvaluation);
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk mencari berdasarkan proposal ID
    @GetMapping("desk-evaluation/proposal/id/{proposalId}")
    public ResponseEntity<?> getByProposalId(@PathVariable Long proposalId) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.getDeskEvaluationsByProposalId(proposalId);
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk mencari berdasarkan sidang ID
    @GetMapping("desk-evaluation/sidang/id/{sidangId}")
    public ResponseEntity<?> getBySidangId(@PathVariable Long sidangId) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.getDeskEvaluationsBySidangId(sidangId);
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/periodes/findAll")
    public ResponseEntity<?> getAllPeriodes() {
        List<PeriodeKolokiumEntity> periodes = periodeKolokiumService.findAll();
        ResponseWrapper<List<PeriodeKolokiumEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Daftar periode kolokium", periodes);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/periodes/{id}")
    public ResponseEntity<?> deletePeriodeById(@PathVariable Long id) {
        ResponseWrapper<String> response;
        if (periodeKolokiumService.findById(id).isPresent()) {
            periodeKolokiumService.deleteById(id);
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Periode berhasil dihapus", null);
            return ResponseEntity.ok(response);
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Periode tidak ditemukan", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    //perseminar
    @PostMapping("/perseminar/create")
    public ResponseEntity<?> savePerseminar(@RequestBody PeriodeSeminarEntity perseminar) {
        PeriodeSeminarEntity savedPerseminar = periodeSeminarService.savePerseminar(perseminar);
        ResponseWrapper<PeriodeSeminarEntity> response = new ResponseWrapper<>(HttpStatus.CREATED.value(), "Seminar created successfully.", savedPerseminar);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/perseminar/{id}")
    public ResponseEntity<?> getPerseminarById(@PathVariable Long id) {
        Optional<PeriodeSeminarEntity> seminar = periodeSeminarService.getPerseminarById(id);
        if (seminar.isPresent()) {
            ResponseWrapper<PeriodeSeminarEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Seminar found.", seminar.get());
            return ResponseEntity.ok(response);
        } else {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Seminar with ID " + id + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/periodes/active")
    public ResponseEntity<?> getActivePeriode() {
        List<PeriodeKolokiumEntity> activePeriode = kolokiumService.getActivePeriode();
        ResponseWrapper<List<PeriodeKolokiumEntity>> response;

        if (activePeriode.isEmpty()) {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Tidak ada periode aktif", null);
        } else {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Periode aktif ditemukan", activePeriode);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/periodes/{id}/adjacent")
    public ResponseEntity<ResponseWrapper<List<PeriodeKolokiumEntity>>> getAdjacentPeriods(@PathVariable Integer id) {
        List<PeriodeKolokiumEntity> periods = periodeKolokiumService.getAdjacentPeriods(id);

        // Membuat ResponseWrapper untuk membungkus respons
        ResponseWrapper<List<PeriodeKolokiumEntity>> response = new ResponseWrapper<>(
                HttpStatus.OK.value(),
                "Data periode berhasil diambil",
                periods
                );

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/perseminar/getall")
    public ResponseEntity<?> getAllPerseminars() {
        List<PeriodeSeminarEntity> seminars = periodeSeminarService.getAllPerseminar();
        ResponseWrapper<List<PeriodeSeminarEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Seminars retrieved successfully.", seminars);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/perseminar/{id}")
    public ResponseEntity<?> deletePerseminarById(@PathVariable Long id) {
        ResponseWrapper<String> response;

        // Check if the seminar exists
        if (periodeSeminarService.getPerseminarById(id).isPresent()) {
            // If exists, proceed to delete
            periodeSeminarService.deletePerseminarById(id);
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Perseminar with ID " + id + " deleted successfully", null);
            return ResponseEntity.ok(response);
        } else {
            // If not found, return not found response
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Perseminar with ID " + id + " not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/perseminar/active")
    public ResponseEntity<?> getActiveSeminars() {
        List<PeriodeSeminarEntity> activeSeminars = periodeSeminarService.getActiveSeminars();

        if (activeSeminars.isEmpty()) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "No active seminars found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        ResponseWrapper<List<PeriodeSeminarEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Active seminars retrieved successfully", activeSeminars);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/perseminar/{id}")
    public ResponseEntity<ResponseWrapper<PeriodeSeminarEntity>> updatePerseminar(
            @PathVariable Long id,
            @Valid @RequestBody PeriodeSeminarEntity updatedSeminar) {

        PeriodeSeminarEntity updatedPerseminar = periodeSeminarService.updatePerseminar(id, updatedSeminar);

        ResponseWrapper<PeriodeSeminarEntity> response = new ResponseWrapper<>(
                HttpStatus.OK.value(),
                "Seminar berhasil diperbarui",
                updatedPerseminar
        );

        return ResponseEntity.ok(response);
    }

    //JadwalSeminar
    @PostMapping("/jadwalseminars/create")
    public ResponseEntity<?> createJadwalSeminar(@RequestBody JadwalSeminarEntity jadwalSeminar) {
        if (jadwalSeminar.getWaktu() == null || jadwalSeminar.getKelompok() == null || jadwalSeminar.getPerseminar() == null) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), "Semua atribut harus diisi: waktu, kelompok, dan perseminar.");
            return ResponseEntity.badRequest().body(response);
        }


        JadwalSeminarEntity createdJadwalSeminar = jadwalSeminarService.createJadwalSeminar(jadwalSeminar);
        ResponseWrapper<JadwalSeminarEntity> response = new ResponseWrapper<>(HttpStatus.CREATED.value(), "Jadwal Seminar berhasil dibuat", createdJadwalSeminar);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Read by ID
    @GetMapping("/jadwalseminars/{id}")
    public ResponseEntity<?> getJadwalSeminarById(@PathVariable Long id) {
        Optional<JadwalSeminarEntity> jadwalSeminar = jadwalSeminarService.getJadwalSeminarById(id);
        if (jadwalSeminar.isPresent()) {
            ResponseWrapper<JadwalSeminarEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Jadwal Seminar ditemukan", jadwalSeminar.get());
            return ResponseEntity.ok(response);
        } else {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Jadwal Seminar tidak ditemukan");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // Read all
    @GetMapping("/jadwalseminars/getall")
    public ResponseEntity<ResponseWrapper<List<JadwalSeminarEntity>>> getAllJadwalSeminars() {
        List<JadwalSeminarEntity> jadwalSeminars = jadwalSeminarService.getAllJadwalSeminars();
        ResponseWrapper<List<JadwalSeminarEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Semua Jadwal Seminar ditemukan", jadwalSeminars);
        return ResponseEntity.ok(response);
    }

    // Update
    @PutMapping("/jadwalseminars/{id}")
    public ResponseEntity<?> updateJadwalSeminar(@PathVariable Long id, @RequestBody JadwalSeminarEntity updatedJadwalSeminar) {
        // Lanjutkan ke service untuk update
        JadwalSeminarEntity result = jadwalSeminarService.updateJadwalSeminar(id, updatedJadwalSeminar);

        // Kembalikan response
        ResponseWrapper<JadwalSeminarEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Jadwal Seminar updated successfully", result);
        return ResponseEntity.ok(response);
    }


    // Delete
    @DeleteMapping("/jadwalseminars/{id}")
    public ResponseEntity<ResponseWrapper<Void>> deleteJadwalSeminar(@PathVariable Long id) {
        jadwalSeminarService.deleteJadwalSeminar(id);
        ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NO_CONTENT.value(), "Jadwal Seminar berhasil dihapus");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/jadwalseminars/perseminar/{perseminarId}")
    public ResponseEntity<?> getJadwalSeminarsByPerseminarId(@PathVariable Long perseminarId) {
        List<JadwalSeminarEntity> jadwalSeminars = jadwalSeminarService.getJadwalSeminarsByPerseminarId(perseminarId);
        if (jadwalSeminars != null && !jadwalSeminars.isEmpty()) {
            ResponseWrapper<List<JadwalSeminarEntity>> response =
                    new ResponseWrapper<>(HttpStatus.OK.value(), "Data jadwal seminar ditemukan", jadwalSeminars);
            return ResponseEntity.ok(response);
        } else {
            ResponseWrapper<String> response =
                    new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Tidak ada jadwal seminar ditemukan untuk perseminar id: " + perseminarId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/jadwalseminars/search-by-tanggal")
    public ResponseEntity<ResponseWrapper<List<JadwalSeminarEntity>>> getJadwalSeminarByTanggal(
            @RequestBody JadwalSeminarByTanggalRequestDTO requestDTO) {

        List<JadwalSeminarEntity> jadwalSeminarList = jadwalSeminarService.getJadwalSeminarByTanggal(requestDTO.getWaktu());

        ResponseWrapper<List<JadwalSeminarEntity>> response;
        if (!jadwalSeminarList.isEmpty()) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), "Jadwal seminar ditemukan", jadwalSeminarList);
            return ResponseEntity.ok(response);
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Jadwal seminar tidak ditemukan pada tanggal tersebut", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    //Jadwal Seminar Dosen
    @PostMapping("/jadwalseminardosen/update-dosen-seminar")
    public ResponseEntity<?> updateDosenSeminar(@RequestBody JadwalSeminarDosenDTO request) {
        List<StatusJadwalSeminarDosenDTO> updatedDosenList = jadwalSeminarDosenService.updateJadwalSeminarDosen(request.getSeminarId(), request.getKetuaId(), request.getAnggotaIds());
        ResponseWrapper<List<StatusJadwalSeminarDosenDTO>> response = new ResponseWrapper<>(HttpStatus.OK.value(), updatedDosenList);
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk mendapatkan seluruh dosen seminar berdasarkan seminarId
    @GetMapping("/jadwalseminardosen/seminar/{seminarId}")
    public ResponseEntity<?> getDosenSeminarsBySeminarId(@PathVariable Long seminarId) {
        JadwalSeminarDosenDetailDTO dto = jadwalSeminarDosenService.getJadwalSeminarDosenByJadwalSeminarId(seminarId);
        ResponseWrapper<JadwalSeminarDosenDetailDTO> response = new ResponseWrapper<>(HttpStatus.OK.value(), dto);
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk mendapatkan seluruh dosen seminar dari semua seminar
    @GetMapping("/jadwalseminardosen/all-dosen-seminar")
    public ResponseEntity<?> getAllDosenSeminars() {
        List<JadwalSeminarDosenDetailDTO> allDosenSeminars = jadwalSeminarDosenService.getAllDosenJadwalSeminar();
        ResponseWrapper<List<JadwalSeminarDosenDetailDTO>> response = new ResponseWrapper<>(HttpStatus.OK.value(), allDosenSeminars);
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk mendapatkan seluruh dosen seminar berdasarkan dosenId
    @GetMapping("/jadwalseminardosen/dosen/{dosenId}")
    public ResponseEntity<?> getDosenSeminarsByDosen(@PathVariable long dosenId) {
        List<JadwalSeminarDosenDetailDTO> groupDosenSeminar = jadwalSeminarDosenService.getGroupJadwalSeminarByDosen(dosenId);
        ResponseWrapper<List<JadwalSeminarDosenDetailDTO>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Seminars retrieved successfully", groupDosenSeminar);
        return ResponseEntity.ok(response);
    }

    // Endpoint to get a seminar by its ID
    @GetMapping("/seminar/{id}")
    public ResponseEntity<ResponseWrapper<SeminarEntity>> getSeminarById(@PathVariable("id") Integer id) {
        Optional<SeminarEntity> seminarOpt = seminarService.getSeminarById(id);
        if (seminarOpt.isPresent()) {
            ResponseWrapper<SeminarEntity> response = new ResponseWrapper<>(
                    HttpStatus.OK.value(), "Seminar found", seminarOpt.get());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            ResponseWrapper<SeminarEntity> response = new ResponseWrapper<>(
                    HttpStatus.NOT_FOUND.value(), "Seminar not found", null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint to get all seminars
    @GetMapping("/seminar/all")
    public ResponseEntity<ResponseWrapper<List<SeminarEntity>>> getAllSeminars() {
        List<SeminarEntity> seminars = seminarService.getAllSeminars();
        ResponseWrapper<List<SeminarEntity>> response = new ResponseWrapper<>(
                HttpStatus.OK.value(), "Seminars retrieved", seminars);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Endpoint to update seminar status (example for PATCH)
    @PatchMapping("/seminar/{id}/status")
    public ResponseEntity<ResponseWrapper<SeminarUpdateDTO>> updateSeminarStatus(@PathVariable("id") Integer id,
                                                                           @RequestParam("status") String status) {
        SeminarUpdateDTO updatedSeminarDTO = seminarService.updateSeminarStatus(id, status);
        if (updatedSeminarDTO != null) {
            ResponseWrapper<SeminarUpdateDTO> response = new ResponseWrapper<>(
                    HttpStatus.OK.value(), "Seminar status updated", updatedSeminarDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            ResponseWrapper<SeminarUpdateDTO> response = new ResponseWrapper<>(
                    HttpStatus.NOT_FOUND.value(), "Seminar not found", null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/seminar/null-proposal-active-perseminar")
    public ResponseEntity<ResponseWrapper<List<SeminarEntity>>> getSeminarsWithNullProposalAndActivePerseminar() {

        // Memanggil service tanpa memerlukan parameter status
        List<SeminarEntity> seminars = seminarService.getSeminarsWithNullProposalAndActivePerseminar();

        // Membungkus respons dalam ResponseWrapper
        ResponseWrapper<List<SeminarEntity>> response = new ResponseWrapper<>(
                HttpStatus.OK.value(),
                "Seminars fetched successfully.",
                seminars);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/seminar/{seminarId}/proposal/{proposalId}")
    public ResponseEntity<ResponseWrapper<String>> updateProposal(
            @PathVariable Long seminarId,
            @PathVariable Long proposalId) {

        boolean isUpdated = seminarService.updateProposal(seminarId, proposalId);

        if (isUpdated) {
            ResponseWrapper<String> response = new ResponseWrapper<>(
                    HttpStatus.OK.value(),
                    "Proposal updated successfully for seminar with ID: " + seminarId,
                    null);
            return ResponseEntity.ok(response);
        } else {
            ResponseWrapper<String> response = new ResponseWrapper<>(
                    HttpStatus.NOT_FOUND.value(),
                    "Seminar or Proposal not found for provided IDs.",
                    null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/seminar/active-periode")
    public ResponseWrapper<List<ProposalEntity>> getProposalsForActivePeriode() {
        // Memanggil service instance yang sudah di-autowired
        List<ProposalEntity> proposals = seminarService.getProposalsForActivePeriode();

        // Menggunakan ResponseWrapper untuk membungkus response
        return new ResponseWrapper<>(HttpStatus.OK.value(), "Proposals for active periode fetched successfully", proposals);
    }

    @PutMapping("/seminar/{seminarId}/update-jadwal-seminar/{jadwalseminarId}")
    public ResponseWrapper<Boolean> updateJadwalSeminarId(
            @PathVariable Long seminarId,
            @PathVariable Long jadwalseminarId) {

        boolean isUpdated = seminarService.updateJadwalSeminarId(seminarId, jadwalseminarId);

        if (isUpdated) {
            return new ResponseWrapper<>(HttpStatus.OK.value(), "Jadwal seminar updated successfully.", true);
        } else {
            return new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Seminar or Jadwal seminar not found.", false);
        }
    }

    // Endpoint untuk mendapatkan seminar dengan jadwalseminar_id yang null
    @GetMapping("/seminar/without-jadwalseminar")
    public ResponseWrapper<List<SeminarEntity>> getSeminarsWithNullJadwalSeminar() {
        List<SeminarEntity> seminars = seminarService.getSeminarsWithNullJadwalSeminarNative();

        return new ResponseWrapper<>(
                HttpStatus.OK.value(),
                "Seminars with null jadwalseminar_id fetched successfully.",
                seminars
        );
    }

    @PatchMapping("/seminar/{seminarId}/updatestatus")
    public ResponseWrapper<Boolean> updateSeminarStatusAndNotes(
            @PathVariable Long seminarId,
            @RequestBody SeminarStatusUpdateDTO updateDTO) {

        // Use ModelMapper to convert status
        ModelMapper modelMapper = new ModelMapper();
        SeminarEntity.Status status = modelMapper.map(updateDTO.getStatus(), SeminarEntity.Status.class);

        boolean isUpdated = seminarService.updateSeminarStatusAndNotes(
                seminarId, status, updateDTO.getCatatan());

        if (isUpdated) {
            return new ResponseWrapper<>(HttpStatus.OK.value(), "Seminar status and notes updated successfully", true);
        } else {
            return new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Seminar not found", false);
        }
    }


    @GetMapping("/seminar/status-b-or-v/active-perseminar/{statuses}")
    public ResponseWrapper<List<SeminarEntity>> getSeminarsWithStatusBOrVAndProposalIdNotNull(
            @PathVariable List<String> statuses) {

        List<SeminarEntity> seminars = seminarService.getSeminarsWithStatusBOrVAndProposalIdNotNull(statuses);

        if (seminars.isEmpty()) {
            return new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "No seminars found", seminars);
        } else {
            return new ResponseWrapper<>(HttpStatus.OK.value(), "Seminars retrieved successfully", seminars);
        }
    }



}
