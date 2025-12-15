package org.ukdw.entity;

import jakarta.persistence.*;  // Gunakan jakarta.persistence.*
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sidangs_dosens")
public class DosenEvaluatorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // @Id dari jakarta.persistence
    @Column(name = "id", nullable = false)
    @Getter
    @Setter
    private Long id;

    // Relasi ManyToOne dengan GroupEvaluatorEntity (sidang)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sidang_id", referencedColumnName = "id", nullable = true)
    @Getter
    @Setter
    private GroupEvaluatorEntity sidang;

    // Relasi ManyToOne dengan DosenEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosen_id", referencedColumnName = "id", nullable = true)
    @Getter
    @Setter
    private DosenEntity dosen;

    // Kolom status (K = Ketua, A = Anggota)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 1)
    @Getter
    @Setter
    private Status status;

    // Enum untuk kolom status
    public enum Status {
        K, // Ketua
        A  // Anggota
    }
}
