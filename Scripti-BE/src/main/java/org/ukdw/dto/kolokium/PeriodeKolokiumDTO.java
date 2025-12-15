package org.ukdw.dto.kolokium;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Date;

@Data
public class PeriodeKolokiumDTO {

    @NotEmpty(message = "Tanggal harus diisi!")
    private Date tanggal;

    @NotEmpty(message = "Tanggal Awal harus diisi!")
    private Date tglAwal;

    @NotEmpty(message = "Tanggal Akhir harus diisi!")
    private Date tglAkhir;

    @NotEmpty(message = "Title harus diisi!")
    private String title;

    @NotEmpty(message = "Description harus diisi!")
    private String description;

}
