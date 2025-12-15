package org.ukdw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.ukdw.dto.*;
import org.ukdw.dto.kolokium.ProposalRequestDTO;
import org.ukdw.dto.request.proposal.ProposalSearchByNimRequest;
import org.ukdw.dto.request.similarity.ProposalDraftDTO;
import org.ukdw.dto.response.ResponseWrapper;
import org.ukdw.dto.response.similarity.ClusterDetailDTO;
import org.ukdw.dto.response.similarity.SimilarityResponseDTO;
import org.ukdw.entity.*;
import org.ukdw.services.*;
import org.ukdw.entity.ProposalEntity;

import java.util.*;

@RestController
@RequiredArgsConstructor
// Restricted endpoint, accessible only to users with the role "ADMIN"
@PreAuthorize("hasRole(T(org.ukdw.entity.AuthoritiesConstants).ROLE_MAHASISWA.name())")
@RequestMapping("/mahasiswa")
public class MahasiswaController {

    private final ProposalService proposalService;
    private final DeskEvaluationService deskEvaluationService;
    private final SeminarService seminarService;
    private final KolokiumService kolokiumService;
    private final DosenService dosenService;
    private final SimilarityService similarityService;

    @PostMapping("/proposal/check-draft")
    public ResponseEntity<ResponseWrapper<SimilarityResponseDTO>> checkDraftSimilarity(
            @RequestBody ProposalDraftDTO request,
            @RequestParam(name = "topK", defaultValue = "5") int topK
    ) {
        SimilarityResponseDTO result = similarityService.checkDraftSimilarity(request, topK);
        return ResponseEntity.ok(new ResponseWrapper<>(HttpStatus.OK.value(), "Draft similarity calculated", result));
    }

    @PostMapping("/proposal/nim/similarity")
    public ResponseEntity<ResponseWrapper<SimilarityResponseDTO>> getProposalSimilarityByIdAndNim(
            @RequestBody ProposalRequestDTO request,
            @RequestParam(name = "topK", defaultValue = "5") int topK
    ) {
        SimilarityResponseDTO result = similarityService.getSimilarityByProposalId(request.getId(), request.getNim(), topK);

        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Proposal not found", null));
        }
        return ResponseEntity.ok(new ResponseWrapper<>(HttpStatus.OK.value(), "Similarity calculated", result));
    }

    // Opsional: Jika mahasiswa boleh lihat cluster
    @GetMapping("/system/clusters")
    public ResponseEntity<ResponseWrapper<Map<String, ClusterDetailDTO>>> getClusterOverview() {
        Map<String, ClusterDetailDTO> result = similarityService.getClusterOverview();
        return ResponseEntity.ok(new ResponseWrapper<>(HttpStatus.OK.value(), "Cluster data fetched", result));
    }

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

    @PostMapping("/proposal/nim")
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

    @GetMapping("/seminar/by-nim/{nim}")
    public ResponseWrapper<List<SeminarEntity>> getSeminarsByNim(@PathVariable String nim) {
        List<SeminarEntity> seminars = seminarService.getSeminarsByNim(nim);

        if (seminars.isEmpty()) {
            return new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "No seminars found", seminars);
        }

        return new ResponseWrapper<>(HttpStatus.OK.value(), "Seminars retrieved successfully", seminars);
    }

    @GetMapping("/data-dosen")
    public ResponseEntity<?> findAllDosen() {
        Iterable<DosenEntity> dosenList = dosenService.findAllDosen();
        ResponseWrapper<Iterable<DosenEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), dosenList);
        return ResponseEntity.ok(response);
    }
}