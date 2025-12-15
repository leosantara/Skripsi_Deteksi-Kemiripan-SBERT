package org.ukdw.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "jadwalseminars_dosens")
public class JadwalSeminarDosenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @Getter
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jadwalseminar_id", referencedColumnName = "id", nullable = true)
    @Setter
    @Getter
    private JadwalSeminarEntity jadwalSeminar;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosen_id",  referencedColumnName = "id", nullable = true)
    @Setter
    @Getter
    private DosenEntity dosen;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "enum('K', 'A')")
    @Setter
    @Getter
    private Status status;

    public enum Status {
        K, // Ketua
        A  // Anggota
    }
}

