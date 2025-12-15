package org.ukdw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ukdw.dto.kolokium.DosenEvaluatorDetailDTO;
import org.ukdw.dto.kolokium.StatusDosenEvaluatorDTO;
import org.ukdw.dto.kolokium.GroupEvaluatorDTO;
import org.ukdw.entity.DosenEntity;
import org.ukdw.entity.DosenEvaluatorEntity;
import org.ukdw.entity.GroupEvaluatorEntity;
import org.ukdw.entity.PeriodeKolokiumEntity;
import org.ukdw.exception.BadRequestException;
import org.ukdw.exception.ScNotFoundException;
import org.ukdw.repository.DosenEvaluatorRepository;
import org.ukdw.repository.GroupEvaluatorRepository;
import org.ukdw.repository.PeriodeKolokiumRepository;


import java.util.ArrayList;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class KolokiumService {

    private final PeriodeKolokiumRepository periodeKolokiumRepository;

    private final GroupEvaluatorRepository groupEvaluatorRepository;

    private final DosenEvaluatorRepository dosenEvaluatorRepository;

    // Constructor manual
    public KolokiumService(
            PeriodeKolokiumRepository periodeKolokiumRepository,
            GroupEvaluatorRepository groupEvaluatorRepository,
            DosenEvaluatorRepository dosenEvaluatorRepository
    ) {
        this.periodeKolokiumRepository = periodeKolokiumRepository;
        this.groupEvaluatorRepository = groupEvaluatorRepository;
        this.dosenEvaluatorRepository = dosenEvaluatorRepository;
    }

    // Create
    @Transactional
    public PeriodeKolokiumEntity saveOrUpdate(PeriodeKolokiumEntity periodeKolokiumEntity) {
        // Validasi atribut yang diperlukan
        if (periodeKolokiumEntity.getTanggal() == null) {
            throw new BadRequestException("Tanggal harus diisi!");
        }
        if (periodeKolokiumEntity.getTglAwal() == null) {
            throw new BadRequestException("Tanggal Awal harus diisi!");
        }
        if (periodeKolokiumEntity.getTglAkhir() == null) {
            throw new BadRequestException("Tanggal Akhir harus diisi!");
        }
        if (periodeKolokiumEntity.getTitle() == null || periodeKolokiumEntity.getTitle().isEmpty()) {
            throw new BadRequestException("Title harus diisi!");
        }
        if (periodeKolokiumEntity.getDescription() == null || periodeKolokiumEntity.getDescription().isEmpty()) {
            throw new BadRequestException("Description harus diisi!");
        }

        // Cek apakah sudah ada periode yang aktif
        Date currentDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<PeriodeKolokiumEntity> activePeriode = periodeKolokiumRepository.findActivePeriode(currentDate);

        // Cek apakah ada periode aktif
        if (!activePeriode.isEmpty()) {
            // Ambil periode dengan tglAkhir paling besar (terakhir)
            PeriodeKolokiumEntity latestActivePeriode = activePeriode.stream()
                    .max(Comparator.comparing(PeriodeKolokiumEntity::getTglAkhir))
                    .orElseThrow(() -> new BadRequestException("Tidak ada periode aktif ditemukan."));

            // Validasi agar tglAwal periode baru lebih besar dari tglAkhir periode aktif terakhir
            if (periodeKolokiumEntity.getTglAwal().before(latestActivePeriode.getTglAkhir())) {
                throw new BadRequestException("Periode kolokium yang Anda buat harus setelah periode aktif terakhir ("
                        + latestActivePeriode.getTglAkhir() + ").");
            }
        }

        // Simpan atau perbarui data jika valid
        return periodeKolokiumRepository.save(periodeKolokiumEntity);
    }



    // Read (get by id)
    public Optional<PeriodeKolokiumEntity> findById(Long id) {
        return periodeKolokiumRepository.findById(id);
    }

    // Read (get all)
    public List<PeriodeKolokiumEntity> findAll() {
        return periodeKolokiumRepository.findAll();
    }

    // Delete by ID
    // Delete by ID
    public void deleteById(Long id) {
        // Cari Periode Kolokium berdasarkan ID
        Optional<PeriodeKolokiumEntity> existingPeriode = periodeKolokiumRepository.findById(id);

        if (existingPeriode.isPresent()) {
            PeriodeKolokiumEntity periodeKolokium = existingPeriode.get();

            // Mendapatkan tanggal saat ini
            Date currentDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Cek apakah tanggal kolokium sudah lewat
            if (periodeKolokium.getTanggal().before(currentDate)) {
                throw new BadRequestException("Periode kolokium sudah lewat dan tidak dapat dihapus.");
            }

            // Hapus data jika tanggal kolokium belum lewat
            periodeKolokiumRepository.deleteById(id);
        } else {
            throw new BadRequestException("Periode kolokium tidak ditemukan untuk ID: " + id);
        }
    }


    @Transactional
    public PeriodeKolokiumEntity updatePeriodeKolokium(Long id, PeriodeKolokiumEntity updatedPeriodeKolokiumEntity) {
        Optional<PeriodeKolokiumEntity> existingPeriode = periodeKolokiumRepository.findById(id);

        if (existingPeriode.isPresent()) {
            PeriodeKolokiumEntity periodeKolokium = existingPeriode.get();

            // Validasi agar data yang diubah tidak kosong
            if (updatedPeriodeKolokiumEntity.getTanggal() == null) {
                throw new BadRequestException("Tanggal tidak boleh kosong");
            }
            if (updatedPeriodeKolokiumEntity.getTglAwal() == null) {
                throw new BadRequestException("Tanggal Awal tidak boleh kosong");
            }
            if (updatedPeriodeKolokiumEntity.getTglAkhir() == null) {
                throw new BadRequestException("Tanggal Akhir tidak boleh kosong");
            }
            if (updatedPeriodeKolokiumEntity.getTitle() == null || updatedPeriodeKolokiumEntity.getTitle().isEmpty()) {
                throw new BadRequestException("Title tidak boleh kosong");
            }
            if (updatedPeriodeKolokiumEntity.getDescription() == null || updatedPeriodeKolokiumEntity.getDescription().isEmpty()) {
                throw new BadRequestException("Description tidak boleh kosong");
            }

            // Jika valid, lakukan pembaruan data
            periodeKolokium.setTanggal(updatedPeriodeKolokiumEntity.getTanggal());
            periodeKolokium.setTglAwal(updatedPeriodeKolokiumEntity.getTglAwal());
            periodeKolokium.setTglAkhir(updatedPeriodeKolokiumEntity.getTglAkhir());
            periodeKolokium.setTitle(updatedPeriodeKolokiumEntity.getTitle());
            periodeKolokium.setDescription(updatedPeriodeKolokiumEntity.getDescription());

            // Simpan data yang telah diperbarui
            return periodeKolokiumRepository.save(periodeKolokium);
        } else {
            throw new BadRequestException("Periode Kolokium not found for id: " + id);
        }
    }


    public List<PeriodeKolokiumEntity> getActivePeriode() {
        // Mendapatkan tanggal saat ini
        Date currentDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Mendapatkan periode aktif berdasarkan tanggal sekarang
        List<PeriodeKolokiumEntity> activePeriode = periodeKolokiumRepository.findActivePeriode(currentDate);

        // Mengecek apakah periode aktif ditemukan
        if (activePeriode.isEmpty()) {
            // Lemparkan exception jika tidak ada data periode aktif
            throw new ScNotFoundException("Periode kolokium aktif tidak ditemukan.");
        }

        // Kembalikan periode yang paling dekat dengan hari ini
        return activePeriode;
    }

    public List<PeriodeKolokiumEntity> getAdjacentPeriods(Integer id) {
        return periodeKolokiumRepository.findAdjacentPeriods(id);
    }

    @Transactional
    public PeriodeKolokiumEntity getActivePeriodeLimit(Date currentDate) {
        // Panggil repository untuk mencari periode aktif
        return periodeKolokiumRepository.findActivePeriodeLimit(currentDate);
    }


    public GroupEvaluatorDTO convertToDto(GroupEvaluatorEntity entity) {
        GroupEvaluatorDTO dto = new GroupEvaluatorDTO();
        dto.setId(entity.getId());
        dto.setKelompok(entity.getKelompok().name());
        dto.setWaktu(entity.getWaktu());
        dto.setPeriodeId(entity.getPeriode().getId());
        return dto;
    }

    public GroupEvaluatorEntity convertToEntity(GroupEvaluatorDTO dto) {
        GroupEvaluatorEntity entity = new GroupEvaluatorEntity();
        entity.setId(dto.getId());
        entity.setKelompok(GroupEvaluatorEntity.Kelompok.valueOf(dto.getKelompok()));
        entity.setWaktu(dto.getWaktu());

        PeriodeKolokiumEntity periode = new PeriodeKolokiumEntity();
        periode.setId(dto.getPeriodeId());  // Hanya ambil ID dari DTO
        entity.setPeriode(periode);

        return entity;
    }

    @Transactional()
    public List<GroupEvaluatorDTO> getAllKelompokDE() {
        List<GroupEvaluatorEntity> entities = groupEvaluatorRepository.findAll();
        return entities.stream()
                .map(this::convertToDto) // Mengonversi setiap entity ke DTO
                .collect(Collectors.toList());
    }


    @Transactional()
    public Optional<GroupEvaluatorDTO> getKelompokDEById(Long id) {
        Optional<GroupEvaluatorEntity> entity = groupEvaluatorRepository.findById(id);
        return entity.map(this::convertToDto); // Mengonversi entity ke DTO jika ada
    }


    @Transactional()
    public List<DosenEvaluatorDetailDTO> getKelompokDEByPeriode(PeriodeKolokiumEntity periode) {
        List<GroupEvaluatorEntity> entities = groupEvaluatorRepository.findByPeriode(periode);
        // Proses setiap GroupEvaluatorEntity untuk membuat DosenEvaluatorDetailDTO
        return entities.stream()
                .map(groupEvaluator -> {
                    // Ambil semua evaluator dalam satu kelompok
                    List<DosenEvaluatorEntity> evaluators = dosenEvaluatorRepository.findAllBySidang_Id(groupEvaluator.getId());

                    // Pisahkan antara Ketua dan Anggota
                    Optional<DosenEvaluatorEntity> ketuaOpt = evaluators.stream()
                            .filter(e -> e.getStatus() == DosenEvaluatorEntity.Status.K)
                            .findFirst();

                    StatusDosenEvaluatorDTO ketuaDTO = ketuaOpt.map(ketua -> {
                        StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
                        dto.setDosenId(Long.valueOf(ketua.getDosen().getId()));
                        dto.setDosenNama(ketua.getDosen().getNama());
                        dto.setStatus(ketua.getStatus().name());
                        return dto;
                    }).orElse(null);

                    List<StatusDosenEvaluatorDTO> anggotaDTOs = evaluators.stream()
                            .filter(e -> e.getStatus() == DosenEvaluatorEntity.Status.A)
                            .map(e -> {
                                StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
                                dto.setDosenId(Long.valueOf(e.getDosen().getId()));
                                dto.setDosenNama(e.getDosen().getNama());
                                dto.setStatus(e.getStatus().name());
                                return dto;
                            })
                            .collect(Collectors.toList());

                    // Buat DosenEvaluatorDetailDTO untuk kelompok ini
                    DosenEvaluatorDetailDTO dto = new DosenEvaluatorDetailDTO();
                    dto.setSidangId(groupEvaluator.getId());
                    dto.setPeriodeKolokium(groupEvaluator.getPeriode());
                    dto.setKelompok(List.of(groupEvaluator.getKelompok().name()));
                    dto.setKetua(ketuaDTO);
                    dto.setAnggota(anggotaDTOs);

                    return dto;
                })
                .collect(Collectors.toList());
    }



    @Transactional
    public GroupEvaluatorEntity createKelompokDE(GroupEvaluatorEntity kelompokDEEntity) {
        return groupEvaluatorRepository.save(kelompokDEEntity);
    }

    @Transactional
    public GroupEvaluatorEntity updateKelompokDE(Long id, GroupEvaluatorEntity updatedKelompokDEEntity) {
        Optional<GroupEvaluatorEntity> existingKelompok = groupEvaluatorRepository.findById(id);
        if (existingKelompok.isPresent()) {
            GroupEvaluatorEntity kelompokDE = existingKelompok.get();
            kelompokDE.setPeriode(updatedKelompokDEEntity.getPeriode());
            kelompokDE.setWaktu(updatedKelompokDEEntity.getWaktu());
            kelompokDE.setKelompok(updatedKelompokDEEntity.getKelompok());
            return groupEvaluatorRepository.save(kelompokDE);
        } else {
            throw new RuntimeException("Kelompok DE not found for id: " + id);
        }
    }

    @Transactional
    public long deleteKelompokDE(Long id) {
        groupEvaluatorRepository.deleteById(id);
        return id;
    }

    @Transactional
    public List<StatusDosenEvaluatorDTO> updateDosenEvaluator(Long sidangId, Long ketuaId, List<Long> anggotaIds) {
        try{
            // Hapus ketua lama (jika ada)
            dosenEvaluatorRepository.findBySidangIdAndStatus(sidangId, DosenEvaluatorEntity.Status.K).ifPresent(ketuaLama -> {
                ketuaLama.setStatus(DosenEvaluatorEntity.Status.A); // Ubah status ketua lama jadi anggota
                dosenEvaluatorRepository.save(ketuaLama);
            });

            // Update atau tambahkan ketua baru
            dosenEvaluatorRepository.findBySidangIdAndDosenId(sidangId, ketuaId).ifPresentOrElse(ketuaBaru -> {
                // Jika ketua ditemukan di sidang, ubah statusnya menjadi ketua
                ketuaBaru.setStatus(DosenEvaluatorEntity.Status.K);
                dosenEvaluatorRepository.save(ketuaBaru);
            }, () -> {
                // Jika ketua baru tidak ditemukan, tambahkan dosen baru sebagai ketua
                DosenEvaluatorEntity newKetua = new DosenEvaluatorEntity();

                // Set sidang untuk dosen baru
                GroupEvaluatorEntity sidang = new GroupEvaluatorEntity();
                sidang.setId(sidangId);
                newKetua.setSidang(sidang);

                // Set dosen baru sebagai ketua
                DosenEntity dosen = new DosenEntity();
                dosen.setId(Math.toIntExact(ketuaId));  // Konversi Long ke Integer jika diperlukan
                newKetua.setDosen(dosen);

                newKetua.setStatus(DosenEvaluatorEntity.Status.K); // Set status sebagai ketua
                dosenEvaluatorRepository.save(newKetua);
            });

            // Dapatkan daftar dosen yang saat ini ada dalam sidang
            List<DosenEvaluatorEntity> currentDosenList = dosenEvaluatorRepository.findAllBySidangId(sidangId);
            // Hapus dosen yang tidak ada di daftar anggota yang baru (kecuali ketua)
            for (DosenEvaluatorEntity dosenEvaluator : currentDosenList) {
                Long dosenId = Long.valueOf(dosenEvaluator.getDosen().getId());  // Konversi ID ke tipe Long jika diperlukan
                if (!dosenId.equals(ketuaId) && !anggotaIds.contains(dosenId)) {
                    dosenEvaluatorRepository.delete(dosenEvaluator); // Hapus dosen
                }
            }
            // Update atau tambahkan anggota baru
            for (Long anggotaId : anggotaIds) {
                dosenEvaluatorRepository.findBySidangIdAndDosenId(sidangId, anggotaId).ifPresentOrElse(anggota -> {
                    anggota.setStatus(DosenEvaluatorEntity.Status.A); // Pastikan status anggota tetap A
                    dosenEvaluatorRepository.save(anggota);
                }, () -> {
                    // Tambahkan dosen baru ke sidang
                    DosenEvaluatorEntity newAnggota = new DosenEvaluatorEntity();

                    // Set sidang menggunakan setter
                    GroupEvaluatorEntity sidang = new GroupEvaluatorEntity();
                    sidang.setId(sidangId);  // Set ID sidang
                    newAnggota.setSidang(sidang);  // Set sidang ke DosenEvaluatorEntity

                    // Set dosen menggunakan setter
                    DosenEntity dosen = new DosenEntity();
                    dosen.setId(Math.toIntExact(anggotaId));  // Konversi Long ke Integer
                    newAnggota.setDosen(dosen);  // Set dosen ke DosenEvaluatorEntity

                    newAnggota.setStatus(DosenEvaluatorEntity.Status.A); // Set sebagai anggota
                    dosenEvaluatorRepository.save(newAnggota);
                });
            }

            // Kembalikan daftar dosen yang ada di sidang setelah update
            // Ubah hasil dari entitas ke DTO
            List<DosenEvaluatorEntity> dosenList = dosenEvaluatorRepository.findAllBySidangId(sidangId);
            return dosenList.stream().map(dosenEvaluator -> {
                StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
                dto.setDosenId(Long.valueOf(dosenEvaluator.getDosen().getId()));
                dto.setDosenNama(dosenEvaluator.getDosen().getNama()); // Asumsikan ada getter untuk nama dosen
                dto.setStatus(dosenEvaluator.getStatus().name());
                return dto;
            }).collect(Collectors.toList());
        }catch (Exception e) {
            throw new RuntimeException("Failed to change data dosen Evaluator");
        }
    }

    // Method untuk mendapatkan semua evaluator berdasarkan sidangId
    public DosenEvaluatorDetailDTO getDosenEvaluatorsBySidangId(Long sidangId) {
        // Ambil daftar dosen evaluator berdasarkan sidang ID
        List<DosenEvaluatorEntity> evaluators = dosenEvaluatorRepository.findAllBySidangId(sidangId);

        // Ambil daftar kelompok berdasarkan sidangId
        List<String> kelompokList = groupEvaluatorRepository.findAllById(sidangId).stream()
                .map(group -> group.getKelompok().name())
                .collect(Collectors.toList());

        // Pisahkan Ketua dan Anggota
        Optional<DosenEvaluatorEntity> ketuaOpt = evaluators.stream()
                .filter(e -> e.getStatus() == DosenEvaluatorEntity.Status.K)
                .findFirst();

        // Map ketua ke StatusDosenEvaluatorDTO
        StatusDosenEvaluatorDTO ketuaDTO = ketuaOpt.map(ketua -> {
            StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
            dto.setDosenId(Long.valueOf(ketua.getDosen().getId()));
            dto.setDosenNama(ketua.getDosen().getNama());
            dto.setStatus(ketua.getStatus().name());
            return dto;
        }).orElse(null);

        // Map anggota ke StatusDosenEvaluatorDTO
        List<StatusDosenEvaluatorDTO> anggotaDTOs = evaluators.stream()
                .filter(e -> e.getStatus() == DosenEvaluatorEntity.Status.A)
                .map(e -> {
                    StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
                    dto.setDosenId(Long.valueOf(e.getDosen().getId()));
                    dto.setDosenNama(e.getDosen().getNama());
                    dto.setStatus(e.getStatus().name());
                    return dto;
                })
                .collect(Collectors.toList());

        // Buat DosenEvaluatorDetailDTO
        DosenEvaluatorDetailDTO dto = new DosenEvaluatorDetailDTO();
        dto.setSidangId(sidangId);
        dto.setKelompok(kelompokList); // Tambahkan kelompok
        dto.setKetua(ketuaDTO);
        dto.setAnggota(anggotaDTOs);

        return dto;
    }

    public List<DosenEvaluatorDetailDTO> getAllDosenEvaluators() {
        List<DosenEvaluatorEntity> evaluators = dosenEvaluatorRepository.findAll();

        // Kelompokkan evaluator berdasarkan sidangId
        Map<Long, List<DosenEvaluatorEntity>> groupedBySidang = evaluators.stream()
                .collect(Collectors.groupingBy(e -> e.getSidang().getId()));

        // Proses setiap sidang untuk membuat DosenEvaluatorDetailDTO
        return groupedBySidang.entrySet().stream()
                .map(entry -> {
                    Long sidangId = entry.getKey();
                    List<DosenEvaluatorEntity> evaluatorsForSidang = entry.getValue();

                    // Ambil informasi periode dan kelompok dari salah satu evaluator (karena sama untuk semua dalam satu sidang)
                    GroupEvaluatorEntity sidang = evaluatorsForSidang.get(0).getSidang();
                    PeriodeKolokiumEntity periodeKolokium = sidang.getPeriode();
                    String kelompok = sidang.getKelompok().name();

                    // Pisahkan antara Ketua dan Anggota
                    Optional<DosenEvaluatorEntity> ketuaOpt = evaluatorsForSidang.stream()
                            .filter(e -> e.getStatus() == DosenEvaluatorEntity.Status.K)
                            .findFirst();

                    StatusDosenEvaluatorDTO ketuaDTO = ketuaOpt.map(ketua -> {
                        StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
                        dto.setDosenId(Long.valueOf(ketua.getDosen().getId()));
                        dto.setDosenNama(ketua.getDosen().getNama());
                        dto.setStatus(ketua.getStatus().name());
                        return dto;
                    }).orElse(null);

                    List<StatusDosenEvaluatorDTO> anggotaDTOs = evaluatorsForSidang.stream()
                            .filter(e -> e.getStatus() == DosenEvaluatorEntity.Status.A)
                            .map(e -> {
                                StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
                                dto.setDosenId(Long.valueOf(e.getDosen().getId()));
                                dto.setDosenNama(e.getDosen().getNama());
                                dto.setStatus(e.getStatus().name());
                                return dto;
                            })
                            .collect(Collectors.toList());

                    // Buat DosenEvaluatorDetailDTO untuk sidang ini
                    DosenEvaluatorDetailDTO dto = new DosenEvaluatorDetailDTO();
                    dto.setSidangId(sidangId);
                    dto.setPeriodeKolokium(periodeKolokium);
                    dto.setKelompok(List.of(kelompok));
                    dto.setKetua(ketuaDTO);
                    dto.setAnggota(anggotaDTOs);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<DosenEvaluatorDetailDTO> getGroupEvaluatorByDosenId(Long dosenId) {
        // Ambil semua evaluators yang berhubungan dengan dosenId
        List<DosenEvaluatorEntity> evaluators = dosenEvaluatorRepository.findAllByDosenId(dosenId);

        // Dapatkan semua sidangId yang unik dari evaluators
        List<Long> sidangIds = evaluators.stream()
                .map(e -> e.getSidang().getId())
                .distinct()
                .collect(Collectors.toList());

        // Buat daftar DosenEvaluatorDetailDTO untuk setiap sidang
        List<DosenEvaluatorDetailDTO> groupEvaluatorDetails = new ArrayList<>();

        for (Long sidangId : sidangIds) {
            // Ambil semua evaluator berdasarkan sidangId saat ini
            List<DosenEvaluatorEntity> allEvaluators = dosenEvaluatorRepository.findAllBySidangId(sidangId);

            // Ambil semua kelompok terkait dengan sidangId
            List<String> kelompokList = groupEvaluatorRepository.findAllById(sidangId).stream()
                    .map(group -> group.getKelompok().name())
                    .collect(Collectors.toList());

            // Pisahkan ketua dan anggota untuk sidang ini
            Optional<DosenEvaluatorEntity> ketuaOpt = allEvaluators.stream()
                    .filter(e -> e.getStatus() == DosenEvaluatorEntity.Status.K)
                    .findFirst();

            StatusDosenEvaluatorDTO ketuaDTO = ketuaOpt.map(ketua -> {
                StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
                dto.setDosenId(Long.valueOf(ketua.getDosen().getId()));
                dto.setDosenNama(ketua.getDosen().getNama());
                dto.setStatus(ketua.getStatus().name());
                return dto;
            }).orElse(null);

            List<StatusDosenEvaluatorDTO> anggotaDTOs = allEvaluators.stream()
                    .filter(e -> e.getStatus() == DosenEvaluatorEntity.Status.A)
                    .map(e -> {
                        StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
                        dto.setDosenId(Long.valueOf(e.getDosen().getId()));
                        dto.setDosenNama(e.getDosen().getNama());
                        dto.setStatus(e.getStatus().name());
                        return dto;
                    })
                    .collect(Collectors.toList());

            // Buat DosenEvaluatorDetailDTO untuk sidang ini
            DosenEvaluatorDetailDTO dosenEvaluatorDetail = new DosenEvaluatorDetailDTO();
            dosenEvaluatorDetail.setSidangId(sidangId);
            dosenEvaluatorDetail.setKelompok(kelompokList); // Set daftar kelompok
            dosenEvaluatorDetail.setKetua(ketuaDTO);
            dosenEvaluatorDetail.setAnggota(anggotaDTOs);

            // Tambahkan ke daftar hasil
            groupEvaluatorDetails.add(dosenEvaluatorDetail);
        }

        return groupEvaluatorDetails;
    }

    public List<GroupEvaluatorEntity> getGroupEvaluatorsByPeriodeIdAndKelompok(Long periodeId, GroupEvaluatorEntity.Kelompok kelompok) {
        return groupEvaluatorRepository.findByPeriode_IdAndKelompok(periodeId, kelompok);
    }

    public List<DosenEvaluatorDetailDTO> getGroupEvaluatorByDosenNidn(String dosenNidn) {
        // Ambil semua evaluators yang berhubungan dengan dosenNidn
        List<DosenEvaluatorEntity> evaluators = dosenEvaluatorRepository.findAllByDosen_Nidn(dosenNidn);

        // Dapatkan semua sidangId yang unik dari evaluators
        List<Long> sidangIds = evaluators.stream()
                .map(e -> e.getSidang().getId())
                .distinct()
                .collect(Collectors.toList());

        // Buat daftar DosenEvaluatorDetailDTO untuk setiap sidang
        List<DosenEvaluatorDetailDTO> groupEvaluatorDetails = new ArrayList<>();

        for (Long sidangId : sidangIds) {
            // Ambil semua evaluator berdasarkan sidangId saat ini
            List<DosenEvaluatorEntity> allEvaluators = dosenEvaluatorRepository.findAllBySidangId(sidangId);

            // Ambil semua kelompok terkait dengan sidangId
            List<String> kelompokList = groupEvaluatorRepository.findAllById(sidangId).stream()
                    .map(group -> group.getKelompok().name())
                    .collect(Collectors.toList());

            // Pisahkan ketua dan anggota untuk sidang ini
            Optional<DosenEvaluatorEntity> ketuaOpt = allEvaluators.stream()
                    .filter(e -> e.getStatus() == DosenEvaluatorEntity.Status.K)
                    .findFirst();

            StatusDosenEvaluatorDTO ketuaDTO = ketuaOpt.map(ketua -> {
                StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
                dto.setDosenId(Long.valueOf(ketua.getDosen().getId()));
                dto.setDosenNama(ketua.getDosen().getNama());
                dto.setStatus(ketua.getStatus().name());
                return dto;
            }).orElse(null);

            List<StatusDosenEvaluatorDTO> anggotaDTOs = allEvaluators.stream()
                    .filter(e -> e.getStatus() == DosenEvaluatorEntity.Status.A)
                    .map(e -> {
                        StatusDosenEvaluatorDTO dto = new StatusDosenEvaluatorDTO();
                        dto.setDosenId(Long.valueOf(e.getDosen().getId()));
                        dto.setDosenNama(e.getDosen().getNama());
                        dto.setStatus(e.getStatus().name());
                        return dto;
                    })
                    .collect(Collectors.toList());

            // Buat DosenEvaluatorDetailDTO untuk sidang ini
            DosenEvaluatorDetailDTO dosenEvaluatorDetail = new DosenEvaluatorDetailDTO();
            dosenEvaluatorDetail.setSidangId(sidangId);
            dosenEvaluatorDetail.setKelompok(kelompokList); // Set daftar kelompok
            dosenEvaluatorDetail.setKetua(ketuaDTO);
            dosenEvaluatorDetail.setAnggota(anggotaDTOs);

            // Tambahkan ke daftar hasil
            groupEvaluatorDetails.add(dosenEvaluatorDetail);
        }

        return groupEvaluatorDetails;
    }

    public List<DosenEvaluatorDetailDTO> getGroupEvaluatorByPeriodeIdAndDosenNidn(Long periodeId, String nidn) {
        // Ambil semua evaluator berdasarkan periodeId dan nidn
        List<DosenEvaluatorEntity> evaluators = dosenEvaluatorRepository.findAllBySidang_Periode_IdAndDosen_Nidn(periodeId, nidn);

        // Dapatkan semua sidangId unik dari evaluators
        List<Long> sidangIds = evaluators.stream()
                .map(e -> e.getSidang().getId())
                .distinct()
                .collect(Collectors.toList());

        // Ambil semua evaluator terkait dengan sidang-sidang tersebut
        List<DosenEvaluatorEntity> allEvaluators = dosenEvaluatorRepository.findAllBySidang_IdIn(sidangIds);

        // Transformasi data ke DTO
        List<DosenEvaluatorDetailDTO> result = sidangIds.stream().map(sidangId -> {
            // Ambil semua evaluator untuk sidang tertentu
            List<DosenEvaluatorEntity> sidangEvaluators = allEvaluators.stream()
                    .filter(e -> e.getSidang().getId().equals(sidangId))
                    .collect(Collectors.toList());

            // Ambil periode dan kelompok dari salah satu evaluator (karena semua evaluator di sidang yang sama punya data sidang yang sama)
            GroupEvaluatorEntity sidang = sidangEvaluators.get(0).getSidang();
            PeriodeKolokiumEntity periodeKolokium = sidang.getPeriode();

            // Buat DTO
            DosenEvaluatorDetailDTO dto = new DosenEvaluatorDetailDTO();
            dto.setSidangId(sidangId);
            dto.setKelompok(List.of(sidang.getKelompok().name()));
            dto.setPeriodeKolokium(periodeKolokium);

            // Pisahkan ketua dan anggota
            sidangEvaluators.forEach(e -> {
                StatusDosenEvaluatorDTO statusDosen = new StatusDosenEvaluatorDTO();
                statusDosen.setDosenId(Long.valueOf(e.getDosen().getId()));
                statusDosen.setDosenNama(e.getDosen().getNama());
                statusDosen.setStatus(e.getStatus().name());

                if (e.getStatus() == DosenEvaluatorEntity.Status.K) {
                    dto.setKetua(statusDosen);
                } else {
                    if (dto.getAnggota() == null) {
                        dto.setAnggota(new ArrayList<>());
                    }
                    dto.getAnggota().add(statusDosen);
                }
            });

            return dto;
        }).collect(Collectors.toList());

        return result;
    }

}
