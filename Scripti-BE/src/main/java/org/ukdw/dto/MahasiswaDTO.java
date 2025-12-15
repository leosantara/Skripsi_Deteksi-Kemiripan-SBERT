package org.ukdw.dto;

import lombok.Data;

import java.util.Date;

/**

 * Creator: dendy
 * Date: 8/4/2020
 * Time: 1:59 PM
 * Description : Mahasiswa DTO
 */

@Data
public class MahasiswaDTO {

    String nim;

    String kodeProdi;

    String tahunAngkatan;

    String nama;

    String jenisKelamin;

    Date tanggalLahir;

    String tempatLahir;

    String wargaNegara;

    String agama;

    String alamat;

    String sekolahAsal;

    String jurusan;

    Date tanggalMulaiKuliah;

    String kodeposRumah;

    String provinsiRumah;

    String kabKotaRumah;

    String kecamatanRumah;

    String kelurahanRumah;

    String jenisKabKotaRumah;

    String kodeposSekolah;

    String provinsiSekolah;

    String kabKotaSekolah;

    String kecamatanSekolah;

    String kelurahanSekolah;

    String jenisKabKotaSekolah;

    String email;

    boolean flagPemutihan = false;

    String tanggalInput;
}