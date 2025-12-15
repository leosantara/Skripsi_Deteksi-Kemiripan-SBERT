/**
 * Author: dendy
 * Date:27/09/2024
 * Time:13:23
 * Description:
 */

package org.ukdw.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "dosens", uniqueConstraints = {
        @UniqueConstraint(columnNames = "nidn")
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DosenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Setter
    @Getter
    private Integer id;

    @Column(name = "nidn", length = 10, nullable = false, unique = true)
    @Setter
    @Getter
    private String nidn;

    @Column(name = "nama", length = 255)
    @Setter
    @Getter
    private String nama;

    @Column(name = "gelar", length = 255)
    @Setter
    @Getter
    private String gelar;

    @Column(name = "boleh", length = 1)
    @Setter
    @Getter
    private String boleh;

    @Column(name = "email", length = 255, nullable = false)
    @Setter
    @Getter
    private String email;

    @Column(name = "telpno", length = 20, nullable = false)
    @Setter
    @Getter
    private String telpNo;

    @Column(name = "status", length = 1, nullable = false)
    @Setter
    @Getter
    private String status = "K";

    @Column(name = "gelar_depan", length = 25)
    @Setter
    @Getter
    private String gelarDepan;

    @Column(name = "prodi", length = 2, nullable = false)
    @Setter
    @Getter
    private String prodi = "71";
}
