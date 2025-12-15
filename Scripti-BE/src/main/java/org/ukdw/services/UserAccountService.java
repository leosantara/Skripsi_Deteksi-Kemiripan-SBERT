package org.ukdw.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.ukdw.dto.request.account.StatusAccountRequest;
import org.ukdw.entity.DosenEntity;
import org.ukdw.entity.GoogleUserEntity;
import org.ukdw.entity.MahasiswaEntity;
import org.ukdw.entity.UserEntity;
import org.ukdw.entity.GroupEntity;
import org.ukdw.exception.BadRequestException;
import org.ukdw.repository.GroupRepository;
import org.ukdw.repository.GoogleUserRepository;
import org.ukdw.repository.UserRepository;

import java.util.*;


//https://www.baeldung.com/spring-transactional-propagation-isolation
@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final DosenService dosenService;
    private final MahasiswaService mahasiswaService;
    private final GoogleUserRepository googleUserRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserAccountService.class);

    @Transactional
    public List<UserEntity> listUserAccount() {
//        List<DosenEntity> dosenEntityList = dosenRepository.findAll();
//        List<MahasiswaEntity> mahasiswaEntityList = mahasiswaRepository.findAll();
        return userRepository.findAll();
    }

    @Transactional
    public UserEntity getUserAccountByEmail(String email) {
        GoogleUserEntity googleUser = googleUserRepository.findByGoogleEmail(email);
        //check if user with registered google email already available
        UserEntity userEntity = userRepository.findByGoogleUser(googleUser);
        return userEntity;
    }

    @Transactional
    public Object findByNimOrNidn(String id) {
        // Coba mencari data di mahasiswa berdasarkan NIM
        MahasiswaEntity mahasiswa = mahasiswaService.findByNim(id);

        if (mahasiswa != null) {
            // Jika ditemukan di mahasiswa, kembalikan data mahasiswa
            return mahasiswa;
        }
        // Jika tidak ditemukan di mahasiswa, coba mencari di dosen berdasarkan NIDN
        DosenEntity dosen = dosenService.findByNidn(id);

        if (dosen != null) {
            // Jika ditemukan di dosen, kembalikan data dosen
            return dosen;
        }
        // Jika tidak ditemukan di mahasiswa maupun dosen
        throw new BadRequestException("Data not found for NIM/NIDN: " + id);
    }

    @Transactional
    public MahasiswaEntity findByNIM(String id) {
        // Coba mencari data di mahasiswa berdasarkan NIM
        MahasiswaEntity mahasiswa = mahasiswaService.findByNim(id);
        if (mahasiswa != null) {
            return mahasiswa;
        }
        throw new BadRequestException("Data not found for NIM: " + id);
    }

    @Transactional
    public DosenEntity findByNIDN(String id) {
        //Mencari di dosen berdasarkan NIDN
        DosenEntity dosen = dosenService.findByNidn(id);
        if (dosen != null) {
            return dosen;
        }
        // Jika tidak ditemukan di mahasiswa maupun dosen
        throw new BadRequestException("Data not found for NIDN: " + id);
    }

    @Transactional
    public UserEntity createUserAccount(UserEntity userAccount) {
        try{
            GoogleUserEntity googleUser = googleUserRepository.findByGoogleEmail(userAccount.getGoogleUser().getGoogleEmail());
            if (googleUser == null) {
                googleUser = userAccount.getGoogleUser();
                googleUser = googleUserRepository.save(googleUser);
                throw new RuntimeException("Google account not found for email: " + userAccount.getGoogleUser().getGoogleEmail());
            }
            userAccount.setGoogleUser(googleUser);
            if (userAccount.getDir() == null || userAccount.getDir().isEmpty()) {
                userAccount.setDir("");
            }
            if (userAccount.getPassword() == null || userAccount.getPassword().isEmpty()) {
                userAccount.setPassword("");
            }
            if (userAccount.getHashValidation() == null || userAccount.getHashValidation().isEmpty()) {
                userAccount.setHashValidation("");
            }
            if (userAccount.getFileSize() == null )  {
                userAccount.setFileSize(0);
            }
            if (userAccount.getHashtag() == null || userAccount.getHashtag().isEmpty()) {
                userAccount.setHashtag("");
            }
            if (userAccount.getMimetype() == null || userAccount.getMimetype().isEmpty()) {
                userAccount.setMimetype("");
            }

            // Simpan grup (jika ada) dan asosiasikan dengan pengguna
            if (userAccount.getGroups() != null && !userAccount.getGroups().isEmpty()) {
                Set<GroupEntity> savedGroups = new HashSet<>();
                for (GroupEntity group : userAccount.getGroups()) {
                    // Simpan setiap grup dan asosiasikan dengan pengguna
                    savedGroups.add(groupRepository.save(group));
                }
                userAccount.setGroups(savedGroups);
            }
            // Simpan pengguna
            UserEntity createdUser = userRepository.save(userAccount);
            return createdUser;
        }
        catch (Exception e){
            e.printStackTrace();
            return null; // Jika
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity findUserAccountById(Long id) {
        Optional<UserEntity> person = userRepository.findById(id);
        return person.orElse(null);
    }

    @Transactional
    public UserEntity updateUserStatus(StatusAccountRequest request) {
        UserEntity userEntity = userRepository.findByGoogleUser(googleUserRepository.findByGoogleEmail(request.getEmail()));
        if (userEntity == null) {
            throw new EntityNotFoundException("User with email " + request.getEmail() + " not found.");
        }
        // Ubah status pengguna
        userEntity.setStatus(request.getStatus());
        return userRepository.save(userEntity);
    }
}
