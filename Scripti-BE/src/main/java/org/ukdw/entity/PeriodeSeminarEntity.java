package org.ukdw.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Date;

@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "perseminars")
public class PeriodeSeminarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Setter
    @Getter
    private Long id;

    @Column(name = "tanggal")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter
    @Getter
    private Date tanggal;

    @Column(name = "tgl_awal")
    @Temporal(TemporalType.DATE)
    @Setter
    @Getter
    private Date tglAwal;

    @Column(name = "tgl_akhir")
    @Temporal(TemporalType.DATE)
    @Setter
    @Getter
    private Date tglAkhir;

    @Column(name = "aktif", columnDefinition = "enum('Y', 'T')")
    @Setter
    @Getter
    private String aktif;

}
