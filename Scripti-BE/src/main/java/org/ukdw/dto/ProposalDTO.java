package org.ukdw.dto;

import lombok.Data;
import java.util.Date;

@Data
public class ProposalDTO {
    private Long id;
    private String nim;
    private Long periodeId;
    private String judul;
    private String latarBelakang;
    private String rumusan;
    private String batasan;
    private String tujuan;
    private String referensi;
    private String status;
    private Long userId;
    private Date modified;
    private String manfaat;
    private Integer revisiDari;
    private String dosenId;
    private String uploadRevisi;

    // Field untuk upload file
    private String filename;
    private String mimeType;
    private String filedir;
}
