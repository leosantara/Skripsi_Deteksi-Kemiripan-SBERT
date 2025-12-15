package org.ukdw.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Creator: dendy
 * Date: 8/4/2020
 * Time: 1:59 PM
 * Description : taken from Table Mahasiswa of srm rdb
 */
@Entity(name = "mahasiswas")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MahasiswaEntity {

    @Id
    @Column(name = "nim", length = 8, nullable = false)
    @Setter
    @Getter
    private String nim;

    @Column(name = "nama", length = 255)
    @Setter
    @Getter
    private String nama;

    @Column(name = "status", columnDefinition = "int(1) default '0'")
    @Setter
    @Getter
    private Integer status;

    @Column(name = "ta", columnDefinition = "int(1) default '0'")
    @Setter
    @Getter
    private Integer ta;
}