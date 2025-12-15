package org.ukdw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ukdw.entity.MahasiswaEntity;
import org.ukdw.repository.MahasiswaRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class MahasiswaService {
    private final MahasiswaRepository mahasiswaRepository;

    // Menyimpan data mahasiswa
    public MahasiswaEntity save(MahasiswaEntity studentEntity) {
        // Cek apakah NIM sudah ada di database
        if (findByNim(studentEntity.getNim()) != null) {
            return null;
        }
        // Jika NIM belum ada, simpan data mahasiswa
        return mahasiswaRepository.save(studentEntity);
    }
    // Mendapatkan semua data mahasiswa
    public Iterable<MahasiswaEntity> findAllMahasiswa() {
        return mahasiswaRepository.findAll();
    }

    // Mendapatkan mahasiswa berdasarkan NIM
    public MahasiswaEntity findByNim(String nim) {
        return mahasiswaRepository.findByNim(nim);
    }

    public void deleteByNim(String nim) {
        mahasiswaRepository.deleteByNim(nim); // Hapus menggunakan NIM
    }

    // Update data mahasiswa
    @Transactional
    public MahasiswaEntity updateMahasiswa(String nim, MahasiswaEntity updatedMahasiswaEntity) {
        try {
            MahasiswaEntity existingMahasiswa = mahasiswaRepository.findByNim(nim);
            if (existingMahasiswa != null) {
                existingMahasiswa.setNama(updatedMahasiswaEntity.getNama());
                existingMahasiswa.setStatus(updatedMahasiswaEntity.getStatus());
                existingMahasiswa.setTa(updatedMahasiswaEntity.getTa());
                return mahasiswaRepository.save(existingMahasiswa);
            } else {
                throw new RuntimeException("Mahasiswa with NIM " + nim + " not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Mahasiswa with NIM" + nim + " not found");
        }
    }



}
