/**
 * Author: dendy
 * Date:26/09/2024
 * Time:8:38
 * Description:
 */

package org.ukdw.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ukdw.entity.GoogleUserEntity;
import org.ukdw.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @EntityGraph(attributePaths = {"groups"})
    UserEntity findByGoogleUser(GoogleUserEntity googleUser);

    @EntityGraph(attributePaths = {"groups"})
    UserEntity findByUsername(String username);
}
