package org.ukdw.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.ukdw.dto.request.similarity.ReindexRequestDTO;
import org.ukdw.dto.request.similarity.SimilarityCheckRequestDTO;
import org.ukdw.dto.response.similarity.ClusterDetailDTO;
import org.ukdw.dto.response.similarity.IndexingStatusDTO;
import org.ukdw.dto.response.similarity.SimilarityResponseDTO;

import java.util.List;
import java.util.Map;

@Service
public class PythonSimilarityClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PythonSimilarityClient(
            RestTemplate restTemplate,
            @Value("${similarity.python.base-url:http://127.0.0.1:8000}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    // Ubah parameter input menjadi SimilarityCheckRequestDTO
    public SimilarityResponseDTO checkSimilarity(SimilarityCheckRequestDTO request, int topK) {
        // Query param di URL
        String url = baseUrl + "/similarity/check?top_k=" + topK;

        // Kirim request
        return restTemplate.postForObject(url, request, SimilarityResponseDTO.class);
    }

    public void triggerReindex(List<ReindexRequestDTO> payload) {
        String url = baseUrl + "/system/reindex";

        // Kita gunakan postForLocation atau postForObject.
        // Karena Python mengembalikan JSON status, kita bisa pakai String.class atau Void.class
        // Payload (List) akan otomatis di-convert jadi JSON Array oleh Jackson
        try {
            restTemplate.postForObject(url, payload, String.class);
            System.out.println("Signal Re-indexing berhasil dikirim ke Python.");
        } catch (Exception e) {
            System.err.println("Gagal mengirim signal Re-indexing: " + e.getMessage());
            throw new RuntimeException("Gagal menghubungi layanan AI untuk update data.", e);
        }
    }

    // Di dalam PythonSimilarityClient.java

    public IndexingStatusDTO getIndexingStatus() {
        String url = baseUrl + "/system/indexing-status";
        // Panggil GET method dan mapping ke DTO baru
        return restTemplate.getForObject(url, IndexingStatusDTO.class);
    }



    public Map<String, ClusterDetailDTO> getAllClusters() {
        String url = baseUrl + "/system/clusters";
        // Butuh ParameterizedTypeReference untuk Map
        return restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<Map<String, ClusterDetailDTO>>() {}
        ).getBody();
    }
}
