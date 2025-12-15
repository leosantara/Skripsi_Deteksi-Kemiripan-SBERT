package org.ukdw.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "seminars")
public class SeminarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Getter
    @Setter
    private Long id;

    // Relasi ManyToOne dengan DeskevaluationEntity (tabel deskevaluations)
    @ManyToOne
    @JoinColumn(name = "deskevaluation_id", referencedColumnName = "id", nullable = true)
    @Getter
    @Setter
    private DeskEvaluationEntity deskevaluation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "enum('B', 'V', 'T', 'L') default 'B'")
    @Getter
    @Setter
    private Status status = Status.B;

    @Column(name = "catatan", columnDefinition = "TEXT")
    @Getter
    @Setter
    private String catatan;

    @Column(name = "modified")
    @Getter
    @Setter
    private LocalDateTime modified;

    // Relasi ManyToOne dengan UserEntity (tabel users)
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true)
    @Getter
    @Setter
    private UserEntity user;

    // Relasi ManyToOne dengan JadwalSeminarEntity (tabel jadwalseminar)
    @ManyToOne
    @JoinColumn(name = "jadwalseminar_id", referencedColumnName = "id", nullable = true)
    @Getter
    @Setter
    private JadwalSeminarEntity jadwalSeminar;

    // Relasi ManyToOne dengan PerseminarEntity (tabel perseminars)
    @ManyToOne
    @JoinColumn(name = "perseminar_id", referencedColumnName = "id", nullable = true)
    @Getter
    @Setter
    private PeriodeSeminarEntity perseminar;

    @Column(name = "validate_time")
    @Getter
    @Setter
    private LocalDateTime validateTime;

    // Relasi ManyToOne dengan ProposalEntity (tabel proposal)
    @ManyToOne
    @JoinColumn(name = "proposal_id", referencedColumnName = "id", nullable = true)
    @Getter
    @Setter
    private ProposalEntity proposal;

    public enum Status {
        B,  // Baru
        V,  // Valid
        T,  // Selesai
        L   // Batal
    }
}
