package org.ukdw.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.ukdw.exception.BadRequestException;

import java.time.LocalDateTime;

@Entity
@Table(name = "jadwalseminars")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class JadwalSeminarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    @Getter
    private Long id;

    @Column(name = "waktu")
    @Setter
    @Getter
    private LocalDateTime waktu;

    @Enumerated(EnumType.STRING)
    @Column(name = "kelompok", length = 1)
    @NotNull
    @Setter
    @Getter
    private Kelompok kelompok;

    @ManyToOne
    @JoinColumn(name = "perseminar_id", referencedColumnName = "id")
    @Setter
    @Getter
    private PeriodeSeminarEntity perseminar;

    public enum Kelompok {
        A, B, C, D, E, F, G, H, I;

        @JsonCreator
        public static Kelompok fromString(String value) {
            try {
                return Kelompok.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Kelompok harus diisi dengan salah satu nilai A, B, C, D, E, F, G, H, I!");
            }
        }

        @JsonValue
        public String toValue() {
            return name();
        }
    }

}

