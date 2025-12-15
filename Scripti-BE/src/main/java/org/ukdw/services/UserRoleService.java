package org.ukdw.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ukdw.dto.request.account.AccountProfileRequest;
import org.ukdw.dto.request.account.RoleAccountRequest;
import org.ukdw.dto.user.UserRoleDTO;
import org.ukdw.entity.GroupEntity;
import org.ukdw.entity.UserEntity;
import org.ukdw.repository.GroupRepository;
import org.ukdw.repository.UserRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRoleService {

//    private final UserRoleDao repository;

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserAccountService userAccountService;

    public UserRoleDTO getRole(String role) {
        UserRoleDTO dummy = new UserRoleDTO();
        dummy.setRole(role);
        dummy.setIdRole(role);
        return dummy;
    }

    @Transactional
    public UserEntity updateUserGroups(RoleAccountRequest request, Set<Long> groupIds) {
        // Dapatkan user berdasarkan email dari request
        UserEntity userEntity = userAccountService.getUserAccountByEmail(request.getEmail());
        if (userEntity == null) {
            throw new EntityNotFoundException("User with email " + request.getEmail() + " not found.");
        }
        // Ambil semua GroupEntity berdasarkan groupIds
        Set<GroupEntity> groups = new HashSet<>(groupRepository.findAllById(groupIds));
        // Set groups baru ke user
        userEntity.setGroups(groups);
        // Simpan user yang sudah diperbarui
        return userRepository.save(userEntity);
    }

    /*public UserRoleDTO getRole(String name){
        Optional<UserRole> role = repository.findByRoleName(name);
        if(role.isPresent()){
            UserRoleDTO data = modelMapper.map(role.get(),UserRoleDTO.class);
            return data;
        }else{
            throw new BadRequestException("Role is doesnt exist");
        }

    }

    public UserRoleDTO getRoleByEmail(String email){
        Optional<UserRole> role = repository.findByEmail(email);
        if(role.isPresent()){
            UserRoleDTO data = modelMapper.map(role.get(),UserRoleDTO.class);
            return data;
        }else{
            throw new BadRequestException("Role is doesnt exist");
        }
    }*/
}
