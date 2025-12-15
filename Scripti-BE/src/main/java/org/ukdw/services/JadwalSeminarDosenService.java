package org.ukdw.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ukdw.dto.kolokium.StatusJadwalSeminarDosenDTO;
import org.ukdw.dto.kolokium.JadwalSeminarDosenDetailDTO;
import org.ukdw.entity.*;
import org.ukdw.exception.BadRequestException;
import org.ukdw.repository.JadwalSeminarDosenRepository;
import org.ukdw.repository.JadwalSeminarRepository;
import org.ukdw.repository.DosenRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JadwalSeminarDosenService {


    private final JadwalSeminarDosenRepository jadwalSeminarDosenRepository;


    // Method untuk membuat JadwalSeminarDosen
    @Transactional
    public List<StatusJadwalSeminarDosenDTO> updateJadwalSeminarDosen(Long jadwalSeminarId, Long ketuaId, List<Long> anggotaIds) {
        try {

            System.out.println("Starting update for jadwalSeminarId: " + jadwalSeminarId);
            // Hapus ketua lama (jika ada)
            jadwalSeminarDosenRepository.findByJadwalSeminarIdAndStatus(jadwalSeminarId,  JadwalSeminarDosenEntity.Status.K).ifPresent(ketuaLama -> {
                System.out.println("Old ketua found with ID: " + ketuaLama.getDosen().getId());
                ketuaLama.setStatus(JadwalSeminarDosenEntity.Status.A); // Ubah status ketua lama jadi anggota
                jadwalSeminarDosenRepository.save(ketuaLama);
            });

            // Update atau tambahkan ketua baru
            jadwalSeminarDosenRepository.findByJadwalSeminarIdAndDosenId(jadwalSeminarId, ketuaId).ifPresentOrElse(ketuaBaru -> {
                // Jika ketua ditemukan di jadwal seminar, ubah statusnya menjadi ketua
                ketuaBaru.setStatus(JadwalSeminarDosenEntity.Status.K);
                jadwalSeminarDosenRepository.save(ketuaBaru);
            }, () -> {
                // Jika ketua baru tidak ditemukan, tambahkan dosen baru sebagai ketua
                JadwalSeminarDosenEntity newKetua = new JadwalSeminarDosenEntity();

                // Set jadwal seminar untuk dosen baru
                JadwalSeminarEntity jadwalSeminar = new JadwalSeminarEntity();
                jadwalSeminar.setId(jadwalSeminarId);
                newKetua.setJadwalSeminar(jadwalSeminar);

                // Set dosen baru sebagai ketua
                DosenEntity dosen = new DosenEntity();
                dosen.setId(Math.toIntExact(ketuaId));
                newKetua.setDosen(dosen);

                newKetua.setStatus(JadwalSeminarDosenEntity.Status.K); // Set status sebagai ketua
                jadwalSeminarDosenRepository.save(newKetua);
            });

            // Dapatkan daftar dosen yang saat ini ada dalam jadwal seminar
            List<JadwalSeminarDosenEntity> currentDosenList = jadwalSeminarDosenRepository.findAllByJadwalSeminarId(jadwalSeminarId);

            // Hapus dosen yang tidak ada di daftar anggota yang baru (kecuali ketua)
            for (JadwalSeminarDosenEntity jadwalSeminarDosen : currentDosenList) {
                Long dosenId = Long.valueOf(jadwalSeminarDosen.getDosen().getId());  // Konversi ID ke tipe Long jika diperlukan
                if (!dosenId.equals(ketuaId) && !anggotaIds.contains(dosenId)) {
                    jadwalSeminarDosenRepository.delete(jadwalSeminarDosen); // Hapus dosen
                }
            }

            // Update atau tambahkan anggota baru
            for (Long anggotaId : anggotaIds) {
                jadwalSeminarDosenRepository.findByJadwalSeminarIdAndDosenId(jadwalSeminarId, anggotaId).ifPresentOrElse(anggota -> {
                    anggota.setStatus(JadwalSeminarDosenEntity.Status.A); // Pastikan status anggota tetap A
                    jadwalSeminarDosenRepository.save(anggota);
                }, () -> {
                    // Tambahkan dosen baru ke jadwal seminar
                    JadwalSeminarDosenEntity newAnggota = new JadwalSeminarDosenEntity();

                    // Set jadwal seminar menggunakan setter
                    JadwalSeminarEntity jadwalSeminar = new JadwalSeminarEntity();
                    jadwalSeminar.setId(jadwalSeminarId);  // Set ID jadwalSeminar
                    newAnggota.setJadwalSeminar(jadwalSeminar);  // Set jadwal seminar ke JadwalSeminarDosenEntity

                    // Set dosen menggunakan setter
                    DosenEntity dosen = new DosenEntity();
                    dosen.setId(Math.toIntExact(anggotaId));  // Konversi Long ke Integer
                    newAnggota.setDosen(dosen);  // Set dosen ke JadwalSeminarDosenEntity

                    newAnggota.setStatus(JadwalSeminarDosenEntity.Status.A); // Set sebagai anggota
                    jadwalSeminarDosenRepository.save(newAnggota);
                });
            }

            // Kembalikan daftar dosen yang ada di jadwal seminar setelah update
            // Ubah hasil dari entitas ke DTO
            List<JadwalSeminarDosenEntity> dosenList = jadwalSeminarDosenRepository.findAllByJadwalSeminarId(jadwalSeminarId);
            return dosenList.stream().map(jadwalSeminarDosen -> {
                StatusJadwalSeminarDosenDTO dto = new StatusJadwalSeminarDosenDTO();
                dto.setDosenId(Long.valueOf(jadwalSeminarDosen.getDosen().getId()));
                dto.setDosenNama(jadwalSeminarDosen.getDosen().getNama()); // Asumsikan ada getter untuk nama dosen
                dto.setStatus(jadwalSeminarDosen.getStatus().name());
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace(); // Log error untuk pemecahan masalah
            throw new BadRequestException("Failed to change data JadwalSeminar Dosen: " + e.getMessage());
        }
    }

    public JadwalSeminarDosenDetailDTO getJadwalSeminarDosenByJadwalSeminarId(Long jadwalSeminarId) {
        List<JadwalSeminarDosenEntity> evaluators = jadwalSeminarDosenRepository.findAllByJadwalSeminarId(jadwalSeminarId);

        // Pisahkan antara Ketua dan Anggota
        Optional<JadwalSeminarDosenEntity> ketuaOpt = evaluators.stream()
                .filter(e -> e.getStatus() == JadwalSeminarDosenEntity.Status.K)
                .findFirst();

        StatusJadwalSeminarDosenDTO ketuaDTO = ketuaOpt.map(ketua -> {
            StatusJadwalSeminarDosenDTO dto = new StatusJadwalSeminarDosenDTO();
            dto.setDosenId(Long.valueOf(ketua.getDosen().getId()));
            dto.setDosenNama(ketua.getDosen().getNama()); // Pastikan `DosenEntity` memiliki `getNama()`
            dto.setStatus(ketua.getStatus().name());
            return dto;
        }).orElse(null);

        List<StatusJadwalSeminarDosenDTO> anggotaDTOs = evaluators.stream()
                .filter(e -> e.getStatus() == JadwalSeminarDosenEntity.Status.A)
                .map(e -> {
                    StatusJadwalSeminarDosenDTO dto = new StatusJadwalSeminarDosenDTO();
                    dto.setDosenId(Long.valueOf(e.getDosen().getId()));
                    dto.setDosenNama(e.getDosen().getNama()); // Pastikan `DosenEntity` memiliki `getNama()`
                    dto.setStatus(e.getStatus().name());
                    return dto;
                })
                .collect(Collectors.toList());


        // Buat DosenEvaluatorDetailDTO
        JadwalSeminarDosenDetailDTO dto = new JadwalSeminarDosenDetailDTO();
        dto.setJadwalSeminarId(jadwalSeminarId);
        dto.setKetua(ketuaDTO);
        dto.setAnggota(anggotaDTOs);

        return dto;
    }

    public List<JadwalSeminarDosenDetailDTO> getAllDosenJadwalSeminar() {
        List<JadwalSeminarDosenEntity> evaluators = jadwalSeminarDosenRepository.findAll();

        // Kelompokkan evaluator berdasarkan sidangId
        Map<Long, List<JadwalSeminarDosenEntity>> groupedByJadwalSeminar = evaluators.stream()
                .collect(Collectors.groupingBy(e -> e.getJadwalSeminar().getId()));

        // Proses setiap sidang untuk membuat DosenEvaluatorDetailDTO
        return groupedByJadwalSeminar.entrySet().stream()
                .map(entry -> {
                    Long jadwalSeminarId = entry.getKey();
                    List<JadwalSeminarDosenEntity> dosenForSeminar = entry.getValue();

                    // Pisahkan antara Ketua dan Anggota
                    Optional<JadwalSeminarDosenEntity> ketuaOpt = dosenForSeminar.stream()
                            .filter(e -> e.getStatus() == JadwalSeminarDosenEntity.Status.K)
                            .findFirst();

                    StatusJadwalSeminarDosenDTO ketuaDTO = ketuaOpt.map(ketua -> {
                        StatusJadwalSeminarDosenDTO dto = new StatusJadwalSeminarDosenDTO();
                        dto.setDosenId(Long.valueOf(ketua.getDosen().getId()));
                        dto.setDosenNama(ketua.getDosen().getNama());
                        dto.setStatus(ketua.getStatus().name());
                        return dto;
                    }).orElse(null);

                    List<StatusJadwalSeminarDosenDTO> anggotaDTOs = dosenForSeminar.stream()
                            .filter(e -> e.getStatus() == JadwalSeminarDosenEntity.Status.A)
                            .map(e -> {
                                StatusJadwalSeminarDosenDTO dto = new StatusJadwalSeminarDosenDTO();
                                dto.setDosenId(Long.valueOf(e.getDosen().getId()));
                                dto.setDosenNama(e.getDosen().getNama());
                                dto.setStatus(e.getStatus().name());
                                return dto;
                            })
                            .collect(Collectors.toList());

                    // Buat DosenEvaluatorDetailDTO untuk sidang ini
                    JadwalSeminarDosenDetailDTO dto = new JadwalSeminarDosenDetailDTO();
                    dto.setJadwalSeminarId(jadwalSeminarId);
                    dto.setKetua(ketuaDTO);
                    dto.setAnggota(anggotaDTOs);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<JadwalSeminarDosenDetailDTO> getGroupJadwalSeminarByDosen(Long dosenId) {
        // Ambil semua evaluators yang berhubungan dengan dosenId
        List<JadwalSeminarDosenEntity> evaluators = jadwalSeminarDosenRepository.findAllByDosenId(dosenId);

        // Dapatkan semua sidangId yang unik dari evaluators
        List<Long> jadwalSeminarIds = evaluators.stream()
                .map(e -> e.getJadwalSeminar().getId())
                .distinct()
                .collect(Collectors.toList());

        // Buat daftar DosenEvaluatorDetailDTO untuk setiap sidang
        List<JadwalSeminarDosenDetailDTO> jadwalSeminarDosenDetails = new ArrayList<>();

        for (Long seminarId : jadwalSeminarIds) {
            // Ambil semua evaluator berdasarkan sidangId saat ini
            List<JadwalSeminarDosenEntity> allEvaluators = jadwalSeminarDosenRepository.findAllByJadwalSeminarId(seminarId);

            // Pisahkan ketua dan anggota untuk sidang ini
            Optional<JadwalSeminarDosenEntity> ketuaOpt = allEvaluators.stream()
                    .filter(e -> e.getStatus() == JadwalSeminarDosenEntity.Status.K)
                    .findFirst();

            StatusJadwalSeminarDosenDTO ketuaDTO = ketuaOpt.map(ketua -> {
                StatusJadwalSeminarDosenDTO dto = new StatusJadwalSeminarDosenDTO();
                dto.setDosenId(Long.valueOf(ketua.getDosen().getId()));
                dto.setDosenNama(ketua.getDosen().getNama());
                dto.setStatus(ketua.getStatus().name());
                return dto;
            }).orElse(null);

            List<StatusJadwalSeminarDosenDTO> anggotaDTOs = allEvaluators.stream()
                    .filter(e -> e.getStatus() == JadwalSeminarDosenEntity.Status.A)
                    .map(e -> {
                        StatusJadwalSeminarDosenDTO dto = new StatusJadwalSeminarDosenDTO();
                        dto.setDosenId(Long.valueOf(e.getDosen().getId()));
                        dto.setDosenNama(e.getDosen().getNama());
                        dto.setStatus(e.getStatus().name());
                        return dto;
                    })
                    .collect(Collectors.toList());

            // Buat DosenEvaluatorDetailDTO untuk sidang ini
            JadwalSeminarDosenDetailDTO jadwalSeminarDosenDetail = new JadwalSeminarDosenDetailDTO();
            jadwalSeminarDosenDetail.setJadwalSeminarId(seminarId);
            jadwalSeminarDosenDetail.setKetua(ketuaDTO);
            jadwalSeminarDosenDetail.setAnggota(anggotaDTOs);

            // Tambahkan ke daftar hasil
            jadwalSeminarDosenDetails.add(jadwalSeminarDosenDetail);
        }

        return jadwalSeminarDosenDetails;
    }
}
