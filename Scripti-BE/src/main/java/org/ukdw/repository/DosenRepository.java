/**
 * Author: dendy
 * Date:26/09/2024
 * Time:8:40
 * Description:
 */

package org.ukdw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ukdw.entity.DosenEntity;

import java.util.List;

public interface DosenRepository extends JpaRepository<DosenEntity,Integer> {
    DosenEntity findByNidn(String nidn);
    void deleteByNidn(String nidn);
    DosenEntity findById(int id);



}
