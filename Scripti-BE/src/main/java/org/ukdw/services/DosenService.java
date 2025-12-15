package org.ukdw.services;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ukdw.entity.DosenEntity; // Pastikan ini sesuai dengan nama entity yang Anda gunakan
import org.ukdw.repository.DosenRepository; // Pastikan ini sesuai dengan nama repository yang Anda gunakan

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DosenService {
    private final DosenRepository dosenRepository;

    // Menyimpan data dosen
    public DosenEntity save(DosenEntity teacherEntity) {
        // Cek apakah NIDN sudah ada di database
        if (findByNidn(teacherEntity.getNidn()) != null) {
            return null;
        }
        // Jika NIDN belum ada, simpan data mahasiswa
        return dosenRepository.save(teacherEntity);
    }

    // Mendapatkan semua data dosen
    public Iterable<DosenEntity> findAllDosen() {
        return dosenRepository.findAll();
    }

    // Mendapatkan dosen berdasarkan NIDN
    public DosenEntity findByNidn(String nidn) {
        return dosenRepository.findByNidn(nidn);
    }

    // Menghapus dosen berdasarkan NIDN
    public void deleteByNidn(String nidn) {
        dosenRepository.deleteByNidn(nidn); // Hapus menggunakan NIDN
    }

    // Update dosen berdasarkan NIDN
    @Transactional
    public DosenEntity updateDosen(String nidn, DosenEntity updatedDosen) {
        try {
            DosenEntity existingDosen = dosenRepository.findByNidn(nidn);
            if (existingDosen != null) {
                // Update semua field yang diperlukan
                existingDosen.setNidn(updatedDosen.getNidn());
                existingDosen.setNama(updatedDosen.getNama());
                existingDosen.setGelar(updatedDosen.getGelar());
                existingDosen.setBoleh(updatedDosen.getBoleh());
                existingDosen.setEmail(updatedDosen.getEmail());
                existingDosen.setTelpNo(updatedDosen.getTelpNo());
                existingDosen.setStatus(updatedDosen.getStatus());
                existingDosen.setGelarDepan(updatedDosen.getGelarDepan());
                existingDosen.setProdi(updatedDosen.getProdi());

                // Simpan perubahan
                return dosenRepository.save(existingDosen);
            } else {
                throw new RuntimeException("Dosen with NIDN " + nidn + " not found");
            }
        }catch (Exception e) {
            throw new RuntimeException("Dosen with NIDN" + nidn + " not found");
        }
    }

    public Optional<DosenEntity> getDosenById(Integer id) {
        return dosenRepository.findById(id);
    }

}