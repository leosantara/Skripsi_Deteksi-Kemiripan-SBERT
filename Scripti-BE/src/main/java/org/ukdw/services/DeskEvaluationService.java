package org.ukdw.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ukdw.dto.kolokium.DeskEvaluationDTO;
import org.ukdw.entity.DeskEvaluationEntity;
import org.ukdw.entity.GroupEvaluatorEntity;
import org.ukdw.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeskEvaluationService {

    private final DeskEvaluationRepository deskEvaluationRepository;
    private final UserRepository userRepository;
    private final DosenRepository dosenRepository;
    private final MahasiswaRepository mahasiswaRepository;
    private final ProposalRepository proposalRepository;
    private final GroupEvaluatorRepository groupEvaluatorRepository;
    private final SeminarService seminarService;



    public DeskEvaluationService(DeskEvaluationRepository deskEvaluationRepository,
                                 UserRepository userRepository,
                                 DosenRepository dosenRepository,
                                 MahasiswaRepository mahasiswaRepository,
                                 ProposalRepository proposalRepository,
                                 GroupEvaluatorRepository groupEvaluatorRepository, SeminarService seminarService) {
        this.deskEvaluationRepository = deskEvaluationRepository;
        this.userRepository = userRepository;
        this.dosenRepository = dosenRepository;
        this.mahasiswaRepository = mahasiswaRepository;
        this.proposalRepository = proposalRepository;
        this.groupEvaluatorRepository = groupEvaluatorRepository;
        this.seminarService = seminarService;
    }

    public DeskEvaluationEntity saveDeskEvaluation(DeskEvaluationDTO dto) {
        DeskEvaluationEntity deskEvaluation = new DeskEvaluationEntity();
        deskEvaluation.setTanggalValid(dto.getTanggalValid());
        if (dto.getCatatan() != null) {
            deskEvaluation.setCatatan(dto.getCatatan());
        }
        deskEvaluation.setModified(dto.getModified());
        deskEvaluation.setStatus(dto.getStatus());
        if (dto.getJudulLama() == null) {
            throw new RuntimeException("Judul Lama must not be null!");
        }
        deskEvaluation.setJudulLama(dto.getJudulLama());
        if (dto.getJudulBaru() != null) {
            deskEvaluation.setJudulBaru(dto.getJudulBaru());
        }
        deskEvaluation.setUploadRevisi(dto.getUploadRevisi());
        if (dto.getUserPenginputDEId() != null) {
            deskEvaluation.setUserPenginputDE(userRepository.findById(Long.valueOf(dto.getUserPenginputDEId())).orElse(null));
        }
        deskEvaluation.setDosen1(dosenRepository.findById(Integer.parseInt(String.valueOf(dto.getDosen1Id()))));
        if (dto.getDosen2Id() != null) {
            deskEvaluation.setDosen2(dosenRepository.findById(Integer.parseInt(String.valueOf(dto.getDosen2Id()))));
        }
        deskEvaluation.setProposal(proposalRepository.findById(Long.valueOf(dto.getProposalId())).orElse(null));
        deskEvaluation.setMahasiswa(mahasiswaRepository.findById(dto.getMahasiswaNim()).orElse(null));
        if (dto.getDosenEvaluatorId() != null) {
            deskEvaluation.setDosenEvaluator(dosenRepository.findById(Integer.parseInt(String.valueOf(dto.getDosenEvaluatorId()))));
        }
        if (dto.getGroupEvaluatorId() != null) {
            deskEvaluation.setGroupEvaluator(groupEvaluatorRepository.findById(Long.valueOf(dto.getGroupEvaluatorId())).orElse(null));
        }

        // Simpan DeskEvaluation ke database
        DeskEvaluationEntity savedDeskEvaluation = deskEvaluationRepository.save(deskEvaluation);

        // Jika status DeskEvaluation adalah "K", panggil method createSeminarOtomatis dari SeminarService
        if ("K".equals(savedDeskEvaluation.getStatus())) {
            seminarService.createSeminarOtomatis();
        }

        return savedDeskEvaluation;
    }


    public List<DeskEvaluationEntity> getAllDeskEvaluations() {
        return deskEvaluationRepository.findAll();
    }

    public DeskEvaluationEntity getDeskEvaluationById(Integer id) {
        return deskEvaluationRepository.findById(id).orElse(null);
    }

    public void deleteDeskEvaluation(Integer id) {
        deskEvaluationRepository.deleteById(id);
    }

    public List<DeskEvaluationEntity> findByMahasiswaNim(String nim) {
        return deskEvaluationRepository.findByMahasiswa_Nim(nim);
    }

    public List<DeskEvaluationEntity> findByEvaluatorId(Integer evaluatorId) {
        return deskEvaluationRepository.findByDosenEvaluator_Id(evaluatorId);
    }

    public List<DeskEvaluationEntity> findByDosen1Id(Integer dosen1Id) {
        return deskEvaluationRepository.findByDosen1_Id(dosen1Id);
    }

    public List<DeskEvaluationEntity> findByDosen2Id(Integer dosen2Id) {
        return deskEvaluationRepository.findByDosen2_Id(dosen2Id);
    }

    public List<DeskEvaluationEntity> findByDosen1OrDosen2(Integer dosenId) {
        return deskEvaluationRepository.findByDosen1_IdOrDosen2_Id(dosenId, dosenId);
    }

    public List<DeskEvaluationEntity> findByDosen1Nidn(String nidn) {
        return deskEvaluationRepository.findByDosen1_Nidn(nidn);
    }
    public List<DeskEvaluationEntity> findByDosen2Nidn(String nidn) {
        return deskEvaluationRepository.findByDosen2_Nidn(nidn);
    }

    public List<DeskEvaluationEntity> findByDosen1OrDosen2Nidn(String nidn) {
        return deskEvaluationRepository.findByDosen1_NidnOrDosen2_Nidn(nidn, nidn);
    }

    public DeskEvaluationEntity updateDeskEvaluation(Integer id, DeskEvaluationDTO dto) {
        DeskEvaluationEntity deskEvaluation = deskEvaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Desk Evaluation with ID " + id + " not found!"));
        deskEvaluation.setTanggalValid(dto.getTanggalValid());
        if (dto.getCatatan() != null) {
            deskEvaluation.setCatatan(dto.getCatatan());
        }
        deskEvaluation.setModified(dto.getModified());
        deskEvaluation.setStatus(dto.getStatus());
        deskEvaluation.setJudulLama(dto.getJudulLama());
        if (dto.getJudulBaru() != null) {
            deskEvaluation.setJudulBaru(dto.getJudulBaru());
        }
        deskEvaluation.setUploadRevisi(dto.getUploadRevisi());
        if (dto.getUserPenginputDEId() != null) {
            deskEvaluation.setUserPenginputDE(userRepository.findById(Long.valueOf(dto.getUserPenginputDEId())).orElse(null));
        }
        deskEvaluation.setDosen1(dosenRepository.findById(Integer.parseInt(String.valueOf(dto.getDosen1Id()))));
        if (dto.getDosen2Id() != null) {
            deskEvaluation.setDosen2(dosenRepository.findById(Integer.parseInt(String.valueOf(dto.getDosen2Id()))));
        }
        deskEvaluation.setProposal(proposalRepository.findById(Long.valueOf(dto.getProposalId())).orElse(null));
        deskEvaluation.setMahasiswa(mahasiswaRepository.findById(dto.getMahasiswaNim()).orElse(null));
        if (dto.getDosenEvaluatorId() != null) {
            deskEvaluation.setDosenEvaluator(dosenRepository.findById(Integer.parseInt(String.valueOf(dto.getDosenEvaluatorId()))));
        }
        if (dto.getGroupEvaluatorId() != null) {
            deskEvaluation.setGroupEvaluator(groupEvaluatorRepository.findById(Long.valueOf(dto.getGroupEvaluatorId())).orElse(null));
        }

        return deskEvaluationRepository.save(deskEvaluation);
    }


    public List<DeskEvaluationEntity> getDeskEvaluationsByProposalId(Long proposalId) {
        return deskEvaluationRepository.findByProposal_Id(proposalId);
    }

    public List<DeskEvaluationEntity> getDeskEvaluationsBySidangId(Long sidangId) {
        return deskEvaluationRepository.findByGroupEvaluator_Id(sidangId);
    }

    public List<DeskEvaluationEntity> getDeskEvaluationsByPeriodeId(Long periodeId) {
        return deskEvaluationRepository.findByGroupEvaluator_Periode_Id(periodeId);
    }

    public List<DeskEvaluationEntity> getDeskEvaluationByPeriodeAndKelompok(Long periodeId, GroupEvaluatorEntity.Kelompok kelompok) {
        return deskEvaluationRepository.findByGroupEvaluatorPeriodeIdAndGroupEvaluatorKelompok(periodeId, kelompok);
    }

    public List<DeskEvaluationEntity> getDeskEvaluationByYear(int year) {
        // Menghitung tanggal awal dan akhir tahun
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31).withDayOfMonth(31);

        // Mengambil data berdasarkan rentang tanggal
        return deskEvaluationRepository.findByTanggalValidBetween(startDate, endDate);
    }
}