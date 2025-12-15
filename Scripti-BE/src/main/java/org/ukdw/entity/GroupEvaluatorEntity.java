package org.ukdw.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Date;

@Entity
@Table(name = "sidangs")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GroupEvaluatorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Setter
    @Getter
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "periode_id", referencedColumnName = "id", nullable = true)
    @Setter
    @Getter
    private PeriodeKolokiumEntity periode;

    @Column(name = "waktu")
    @Setter
    @Getter
    private Date waktu;

    @Enumerated(EnumType.STRING)
    @Column(name = "kelompok", length = 1)
    @Setter
    @Getter
    private Kelompok kelompok;

    // Enum untuk kolom 'kelompok'
    public enum Kelompok {
        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z
    }
}
