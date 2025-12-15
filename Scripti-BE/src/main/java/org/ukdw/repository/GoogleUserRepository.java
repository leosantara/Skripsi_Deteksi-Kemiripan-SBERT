/**
 * Author: dendy
 * Date:26/09/2024
 * Time:8:38
 * Description:
 */

package org.ukdw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ukdw.entity.GoogleUserEntity;

@Repository
public interface GoogleUserRepository extends JpaRepository<GoogleUserEntity, Long> {
    GoogleUserEntity findByGoogleEmail(String googleEmail);
}
