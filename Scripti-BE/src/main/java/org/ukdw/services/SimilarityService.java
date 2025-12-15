package org.ukdw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ukdw.dto.request.similarity.ProposalDraftDTO;
import org.ukdw.dto.request.similarity.ReindexRequestDTO;
import org.ukdw.dto.request.similarity.SimilarityCheckRequestDTO;
import org.ukdw.dto.response.similarity.*;
import org.ukdw.entity.DeskEvaluationEntity;
import org.ukdw.entity.DosenEntity;
import org.ukdw.entity.ProposalEntity;
import org.ukdw.repository.DeskEvaluationRepository;
import org.ukdw.repository.DosenRepository;
import org.ukdw.repository.ProposalRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimilarityService {

    private final PythonSimilarityClient pythonClient;
    private final ProposalRepository proposalRepository;
    private final DosenRepository dosenRepository;
    private final DeskEvaluationRepository deskEvaluationRepository;

    // ================== PUBLIC METHODS FOR MAHASISWA ==================

    public SimilarityResponseDTO checkDraftSimilarity(ProposalDraftDTO request, int topK) {
        // Mapping DTO Draft -> DTO Python
        SimilarityCheckRequestDTO payload = SimilarityCheckRequestDTO.builder()
                .proposalId(0L)
                .judul(nullSafe(request.getJudul()))
                .latarBelakang(nullSafe(request.getLatarBelakang()))
                .rumusan(nullSafe(request.getRumusan()))
                .tujuan(nullSafe(request.getTujuan()))
                .build();

        SimilarityResponseDTO response = pythonClient.checkSimilarity(payload, topK);
        enrichResponse(response);
        return response;
    }

    public SimilarityResponseDTO getSimilarityByProposalId(Long proposalId, String nim, int topK) {
        Optional<ProposalEntity> proposalOpt = proposalRepository.findByIdAndNim(proposalId, nim); // Asumsi ada findByIdAndNim

        if (proposalOpt.isEmpty()) {
            return null; // Nanti controller handle 404
        }

        ProposalEntity p = proposalOpt.get();
        SimilarityCheckRequestDTO payload = SimilarityCheckRequestDTO.builder()
                .proposalId(p.getId())
                .judul(nullSafe(p.getJudul()))
                .latarBelakang(nullSafe(p.getLatarBelakang()))
                .rumusan(nullSafe(p.getRumusan()))
                .tujuan(nullSafe(p.getTujuan()))
                .build();

        SimilarityResponseDTO response = pythonClient.checkSimilarity(payload, topK);
        enrichResponse(response);
        return response;
    }

    // ================== PUBLIC METHODS FOR ADMIN ==================

    public Map<String, ClusterDetailDTO> getClusterOverview() {
        Map<String, ClusterDetailDTO> rawClusters = pythonClient.getAllClusters();
        if (rawClusters == null) return null;

        Map<String, ClusterDetailDTO> sortedClusters = new TreeMap<>(
                (k1, k2) -> Integer.compare(Integer.parseInt(k1), Integer.parseInt(k2))
        );
        sortedClusters.putAll(rawClusters);

        // Enrich Nama Dosen di Cluster
        for (ClusterDetailDTO detail : sortedClusters.values()) {
            detail.setDosen1Names(getDosenNamesByIds(detail.getDosen1()));
            detail.setDosen2Names(getDosenNamesByIds(detail.getDosen2()));
        }

        return sortedClusters;
    }

    public String triggerUpdateAi() {
        List<DeskEvaluationEntity> validEvaluations = deskEvaluationRepository.findAllAcceptedForAI();

        List<ReindexRequestDTO> payload = validEvaluations.stream()
                .map(de -> {
                    ProposalEntity p = de.getProposal();
                    return ReindexRequestDTO.builder()
                            .proposalId(p.getId())
                            .judul(nullSafe(p.getJudul()))
                            .latarBelakang(nullSafe(p.getLatarBelakang()))
                            .rumusan(nullSafe(p.getRumusan()))
                            .tujuan(nullSafe(p.getTujuan()))
                            .idDosen1(de.getDosen1().getId())
                            .idDosen2(de.getDosen2() != null ? de.getDosen2().getId() : null)
                            .build();
                })
                .collect(Collectors.toList());

        if (payload.isEmpty()) return null; // Indikasi data kosong

        pythonClient.triggerReindex(payload);
        return "Proses pembaruan index AI berjalan untuk " + payload.size() + " proposal.";
    }

    public Map<String, Object> getDebugAiData() {
        List<DeskEvaluationEntity> dataSiapAI = deskEvaluationRepository.findAllAcceptedForAI();

        long jumlahBocorDE = dataSiapAI.stream().filter(de -> !"T".equals(de.getStatus())).count();
        long jumlahBocorProposal = dataSiapAI.stream().filter(de -> !"T".equals(de.getProposal().getStatus())).count();

        Map<String, Object> report = new HashMap<>();
        report.put("Total Data Terambil", dataSiapAI.size());
        report.put("Audit Status DE != T", jumlahBocorDE + " (Harus 0)");
        report.put("Audit Status Proposal != T", jumlahBocorProposal + " (Harus 0)");

        List<Map<String, Object>> samples = dataSiapAI.stream().limit(5).map(de -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id_proposal", de.getProposal().getId());
            item.put("judul", de.getProposal().getJudul());
            item.put("dosen1", de.getDosen1().getNama());
            return item;
        }).collect(Collectors.toList());

        report.put("Sampel Data", samples);
        return report;
    }

    public IndexingStatusDTO getIndexingStatus() {
        return pythonClient.getIndexingStatus();
    }

    // ================== PRIVATE HELPER METHODS ==================

    private void enrichResponse(SimilarityResponseDTO simResp) {
        if (simResp == null || simResp.getResults() == null) return;

        // Enrich Judul Asli dari DB
        for (SimilarityProposalDTO prop : simResp.getResults()) {
            proposalRepository.findById(prop.getProposalId())
                    .ifPresent(p -> prop.setJudul(p.getJudul()));
        }

        // Enrich Nama Dosen
        if (simResp.getDosenRecommendations() != null) {
            for (DosenRecommendationDTO rec : simResp.getDosenRecommendations()) {
                rec.setDosen1Names(getDosenNamesByIds(rec.getRecommendedDosen1Ids()));
                rec.setDosen2Names(getDosenNamesByIds(rec.getRecommendedDosen2Ids()));
            }
        }
    }

    private List<String> getDosenNamesByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<DosenEntity> dosens = dosenRepository.findAllById(ids);
        return dosens.stream().map(this::getNamaGelar).collect(Collectors.toList());
    }

    private String getNamaGelar(DosenEntity d) {
        String depan = d.getGelarDepan() != null ? d.getGelarDepan() + " " : "";
        String blkg = d.getGelar() != null ? ", " + d.getGelar() : "";
        return depan + d.getNama() + blkg;
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}