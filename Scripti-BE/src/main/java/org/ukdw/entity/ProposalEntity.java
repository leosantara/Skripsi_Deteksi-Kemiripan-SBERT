package org.ukdw.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.util.Date;

@Entity(name = "proposals")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProposalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Setter
    @Getter
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nim", referencedColumnName = "nim",
            foreignKey = @ForeignKey(name = "fk_proposals_mahasiswa_nim", value = ConstraintMode.CONSTRAINT))
    @Setter
    @Getter
    private MahasiswaEntity mahasiswa;


    @ManyToOne
    @JoinColumn(name = "periode_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_proposals_periode_id", value = ConstraintMode.CONSTRAINT))
    @Setter
    @Getter
    private PeriodeKolokiumEntity periodes;

    @Column(name = "judul", columnDefinition = "text", nullable = false)
    @Setter
    @Getter
    private String judul;

    @Column(name = "latar_belakang", columnDefinition = "text")
    @Setter
    @Getter
    private String latarBelakang;

    @Column(name = "rumusan", columnDefinition = "text")
    @Setter
    @Getter
    private String rumusan;

    @Column(name = "batasan", columnDefinition = "text")
    @Setter
    @Getter
    private String batasan;

    @Column(name = "tujuan", columnDefinition = "text")
    @Setter
    @Getter
    private String tujuan;

    @Column(name = "referensi", columnDefinition = "text")
    @Setter
    @Getter
    private String referensi;

    @Column(name = "status", columnDefinition = "enum('B', 'T', 'K', 'R', 'L', 'V')", nullable = false)
    @Setter
    @Getter
    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_proposals_user_id", value = ConstraintMode.CONSTRAINT))
    @Setter
    @Getter
    private UserEntity userId;

    @Column(name = "modified", nullable = false)
    @Setter
    @Getter
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "filename", length = 256)
    @Setter
    @Getter
    private String filename;

    @Column(name = "filedir", length = 256)
    @Setter
    @Getter
    private String filedir;

    @Column(name = "mime_type", length = 256)
    @Setter
    @Getter
    private String mimeType;

    @Column(name = "manfaat", columnDefinition = "text")
    @Setter
    @Getter
    private String manfaat;

    @Column(name = "revisi_dari")
    @Setter
    @Getter
    private Integer revisiDari;

    @ManyToOne
    @JoinColumn(name = "dosen_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_proposals_dosen_id", value = ConstraintMode.CONSTRAINT))
    @Setter
    @Getter
    private DosenEntity dosenId;

    @Column(name = "upload_revisi")
    @Setter
    @Getter
    private String uploadRevisi;

}

