package org.ukdw.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "deskevaluations")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DeskEvaluationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Setter
    @Getter
    private Integer id;

    @Column(name = "tanggal_valid")
    @Setter
    @Getter
    private LocalDate tanggalValid;

    @Column(name = "catatan", columnDefinition = "text")
    @Setter
    @Getter
    private String catatan;

    @Column(name = "modified")
    @Setter
    @Getter
    private Date modified;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_deskevaluation_user"))
    @Setter
    @Getter
    private UserEntity userPenginputDE;

    @Column(name = "status", nullable = false, columnDefinition = "enum('V','L','T','K','R') default 'V'")
    @Setter
    @Getter
    private String status;

    @ManyToOne
    @JoinColumn(name = "dosen1", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_deskevaluation_dosen1"))
    @Setter
    @Getter
    private DosenEntity dosen1;

    @ManyToOne
    @JoinColumn(name = "dosen2", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_deskevaluation_dosen2"))
    @Setter
    @Getter
    private DosenEntity dosen2;

    @ManyToOne
    @JoinColumn(name = "proposal_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_deskevaluation_proposal"))
    @Setter
    @Getter
    private ProposalEntity proposal;

    @Column(name = "judul_lama", columnDefinition = "text")
    @Setter
    @Getter
    private String judulLama;

    @Column(name = "judul_baru", columnDefinition = "text")
    @Setter
    @Getter
    private String judulBaru;

    @ManyToOne
    @JoinColumn(name = "nim", referencedColumnName = "nim", foreignKey = @ForeignKey(name = "fk_deskevaluation_mahasiswa"))
    @Setter
    @Getter
    private MahasiswaEntity mahasiswa;

    @Column(name = "upload_revisi", columnDefinition = "enum('Y','T') default 'T'")
    @Setter
    @Getter
    private String uploadRevisi;

    @ManyToOne
    @JoinColumn(name = "evaluator_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_deskevaluation_evaluator"))
    @Setter
    @Getter
    private DosenEntity dosenEvaluator;

    @ManyToOne
    @JoinColumn(name = "sidang_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_deskevaluation_sidang"))
    @Setter
    @Getter
    private GroupEvaluatorEntity groupEvaluator;
}

