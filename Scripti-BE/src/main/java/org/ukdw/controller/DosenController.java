package org.ukdw.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.ukdw.dto.ProposalDTO;
import org.ukdw.dto.kolokium.DeskEvaluationDTO;
import org.ukdw.dto.kolokium.DosenEvaluatorDetailDTO;
import org.ukdw.dto.kolokium.ProposalRequestDTO;
import org.ukdw.dto.request.proposal.ProposalEditStatusRequest;
import org.ukdw.dto.request.proposal.ProposalSearchByDosenIdRequest;
import org.ukdw.dto.response.ResponseWrapper;
import org.ukdw.entity.*;
import org.ukdw.services.DeskEvaluationService;
import org.ukdw.services.DosenService;
import org.ukdw.services.KolokiumService;
import org.ukdw.services.ProposalService;

import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
// Restricted endpoint, accessible only to users with the role "ADMIN"
@PreAuthorize("hasRole(T(org.ukdw.entity.AuthoritiesConstants).ROLE_DOSEN.name())")
@RequestMapping("/dosen")
public class DosenController {

    private final ProposalService proposalService;
    private final DeskEvaluationService deskEvaluationService;
    private final KolokiumService kolokiumService;
    private final DosenService dosenService;

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

    @PostMapping("/proposal/nidn")
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

    @GetMapping("/desk-evaluation/dosen-evaluator/{evaluatorId}")
    public ResponseEntity<?> getDeskEvaluationByEvaluatorId(@PathVariable("evaluatorId") Integer evaluatorId) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.findByEvaluatorId(evaluatorId);
        if (deskEvaluations.isEmpty()) {
            ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Desk Evaluation not found for evaluator ID: " + evaluatorId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/desk-evaluation")
    public ResponseEntity<?> createDeskEvaluation(@RequestBody DeskEvaluationDTO dto) {
        DeskEvaluationEntity savedDeskEvaluation = deskEvaluationService.saveDeskEvaluation(dto);
        ResponseWrapper<DeskEvaluationEntity> response = new ResponseWrapper<>(HttpStatus.CREATED.value(), "Desk Evaluation created successfully", savedDeskEvaluation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/desk-evaluation/{id}")
    public ResponseEntity<DeskEvaluationEntity> updateDeskEvaluation(
            @PathVariable Integer id, @RequestBody DeskEvaluationDTO dto) {
        DeskEvaluationEntity updatedDeskEvaluation = deskEvaluationService.updateDeskEvaluation(id, dto);
        return ResponseEntity.ok(updatedDeskEvaluation);
    }

    // Endpoint untuk mencari berdasarkan sidang ID
    @GetMapping("desk-evaluation/sidang/id/{sidangId}")
    public ResponseEntity<?> getBySidangId(@PathVariable Long sidangId) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.getDeskEvaluationsBySidangId(sidangId);
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk mencari berdasarkan proposal ID
    @GetMapping("desk-evaluation/proposal/id/{proposalId}")
    public ResponseEntity<?> getByProposalId(@PathVariable Long proposalId) {
        List<DeskEvaluationEntity> deskEvaluations = deskEvaluationService.getDeskEvaluationsByProposalId(proposalId);
        ResponseWrapper<List<DeskEvaluationEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Desk Evaluations retrieved successfully", deskEvaluations);
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

    @GetMapping("group-evaluator/periode-and-kelompok")
    public ResponseEntity<?> getGroupEvaluatorsByPeriodeIdAndKelompok(
            @RequestParam Long periodeId,
            @RequestParam GroupEvaluatorEntity.Kelompok kelompok) {
        // Memanggil metode service melalui instance
        List<GroupEvaluatorEntity> groupEvaluators = kolokiumService.getGroupEvaluatorsByPeriodeIdAndKelompok(periodeId, kelompok);
        ResponseWrapper<List<GroupEvaluatorEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Group Desk Evaluations retrieved successfully", groupEvaluators);
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

    @GetMapping("/periodes/{id}")
    public ResponseEntity<?> getPeriodeById(@PathVariable Long id) {
        PeriodeKolokiumEntity periodeKolokiumEntity = kolokiumService.findById(id)
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

    @GetMapping("/periodes/findAll")
    public ResponseEntity<?> getAllPeriodes() {
        List<PeriodeKolokiumEntity> periodes = kolokiumService.findAll();
        ResponseWrapper<List<PeriodeKolokiumEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Daftar periode kolokium", periodes);
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

    @GetMapping("/data-dosen/id/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        Optional<DosenEntity> dosen = dosenService.getDosenById(id);
        ResponseWrapper<DosenEntity> response;
        if (dosen != null) {
            response = new ResponseWrapper<>(HttpStatus.OK.value(), dosen.get());
        } else {
            response = new ResponseWrapper<>(HttpStatus.NOT_FOUND.value(), "Dosen dengan ID " + id + " tidak ditemukan", null);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/data-dosen")
    public ResponseEntity<?> findAllDosen() {
        Iterable<DosenEntity> dosenList = dosenService.findAllDosen();
        ResponseWrapper<Iterable<DosenEntity>> response = new ResponseWrapper<>(HttpStatus.OK.value(), dosenList);
        return ResponseEntity.ok(response);
    }

}
