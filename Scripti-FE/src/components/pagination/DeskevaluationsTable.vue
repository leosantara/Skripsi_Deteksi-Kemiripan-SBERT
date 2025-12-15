<template>
<table class="table table-striped table-hover">
    <thead>
        <tr>
            <th></th>
            <th>NIM</th>
            <th>NAMA MAHASISWA</th>
            <th>JUDUL</th>
            <th>USULAN DOSEN I</th>
            <th>USULAN DOSEN II</th>
            <th>EVALUATOR</th>
            <th>STATUS</th>
        </tr>
    </thead>
    <tbody>
        <tr v-for="(data, index) in props.deskevaluationDatas" v-bind:key="index">
            <td>
                <div class="dropdown">
                    <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        Action
                    </button>
                    <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                        <a  @click.prevent="router.push(handleNextPage(data))" class="dropdown-item" href="#">Evaluate</a>

                        <!-- admin only actions: -->
                        <a v-if="data.status!='V' && route.fullPath.includes('adminpage')" @click.prevent="router.push(`/deskevaluation/adminpage/proposal-mahasiswa/rekap/${data.id}`)" class="dropdown-item">Rekap Hasil</a>
                        <a v-if="data.status=='V' && route.fullPath.includes('adminpage')" @click.prevent="assignDE(data)" class="dropdown-item" href="#" data-bs-toggle="modal" data-bs-target="#deleteConfirmation">Hapus</a>

                        <RouterLink v-if="data.status!='V'" target="_blank" :to="handleBeritaAcara(data)" class="dropdown-item">Cetak Berita Acara</RouterLink>
                    </div>
                </div>
            </td>
            <td>{{ data.mahasiswa.nim }}</td>
            <td>{{ data.mahasiswa.nama }}</td>
            <td>
                <b>{{data.proposal.judul}}</b> <br>
                Diunggah: <span class="badge bg-dark">{{ dateFormater.formatDateTime(data.proposal.modified) }}</span> || <a class="myDownloadLink" href="#" @click.prevent="handleDownload(data.proposal.id, data.proposal.filename)" >&#x1F5CE; Download Proposal</a>
            </td>
            <td>{{ handleGelarDosen(data.dosen1) }}</td>
            <td>{{ handleGelarDosen(data.dosen2) }}</td>
            <td>{{ handleGelarDosen(data.dosenEvaluator) }}</td>
            <td><span v-html="handleStatus(data.status)"></span></td>
        </tr>
    </tbody>
</table>

<!-- Saver Modal -->
<div class="modal fade" id="deleteConfirmation" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="exampleModalLongTitle">Perhatian</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" />
            </div>
            <div class="modal-body">
                <div v-if="data.currentDE">
                    Anda akan menghapus data Deskevaluation untuk proposal dengan judul: <br> 
                    <b>{{ data.currentDE.proposal?.judul }}</b> <br> <br>
                    yang akan melaksanakan sidang pada<br>
                    Waktu: <b>{{ data.currentDE.groupEvaluator?dateFormater.formatDateTime(data.currentDE.groupEvaluator.waktu):"Belum Ada Kelompok" }}</b> <br>
                    Kelompok: <b>{{ data.currentDE.groupEvaluator?data.currentDE.groupEvaluator.kelompok:"Belum Ada Kelompok" }}</b> <br>
                    Peserta: <b>{{ `${data.currentDE.mahasiswa?.nim} - ${data.currentDE.mahasiswa?.nama}` }}</b> <br>
                    Begitu data dihapus, data tidak dapat di restore, apa Anda yakin ingin melanjutkan?
                </div>
                <div v-else>
                    Data sidang tidak dapat ditemukan.
                </div>
            </div>
            <div class="modal-footer">
                <button v-if="data.currentDE" type="button" class="btn btn-danger" data-bs-dismiss="modal" @click="handleDelete()">Hapus</button>
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Tutup</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal -->
<div class="modal fade" id="downloadFailed" tabindex="-1" aria-labelledby="downloadModal" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5" id="downloadModal">Download Gagal</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body row">
                <span class="fs-6">Maaf, file yang Anda cari tidak dapat kami temukan.</span>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Tutup</button>
            </div>
        </div>
    </div>
</div>
</template>

<script setup>
import { defineProps, defineEmits, reactive } from 'vue';
import {router} from '../../router/'
import { RouterLink, useRoute } from 'vue-router';
import dateFormater from '../../helpers/date-formater';
import { useProposalStore } from '../../stores/proposal.store';

const route = useRoute()
const proposalStore = useProposalStore()
const props = defineProps({
    deskevaluationDatas:{
        type: Array,
        required:true
    }
})
const emit = defineEmits(['deleteDeskevaluation'])

let data = reactive({
    currentDE:null
})

function assignDE(deskevaluation){
    data.currentDE = deskevaluation
}

function handleDelete(){
    let deskevaluationID = data.currentDE?data.currentDE.id:""
    emit("deleteDeskevaluation", deskevaluationID)
}

function handleNextPage(deskevaluation){
    if(route.fullPath.includes('adminpage')){
        return `/deskevaluation/adminpage/proposal-mahasiswa/evaluate/${deskevaluation.id}`
    }else if(route.fullPath.includes('dosenpage')){
        return `/deskevaluation/dosenpage/proposal-mahasiswa/evaluate/${deskevaluation.proposal?.id}`
    }else{
        return ``
    }
}

function handleBeritaAcara(deskevaluation){
    if(route.fullPath.includes('adminpage')){
        return `/dokumen/adminpage/berita-acara/${deskevaluation.id}`
    }else if(route.fullPath.includes('dosenpage')){
        return `/dokumen/dosenpage/berita-acara/${deskevaluation.proposal?.id}`
    }else{
        return ``
    }
}

function handleGelarDosen(dosen){
    if(dosen){
        let name = dosen.nama
        if (dosen.gelarDepan){
            name = dosen.gelarDepan + " " + name
        }
        if (dosen.gelar){
            name += ", " + dosen.gelar
        }
        return name
    }else{
        return "Belum ada"
    }
}

function handleStatus(status){
    if(status == 'B'){
        return "<span class='badge bg-secondary'>Baru</span>"
    }else if(status == 'V'){
        return "<span class='badge bg-dark'>Valid</span>"
    }else if(status == 'L'){
        return "<span class='badge bg-danger'>Batal</span>"
    }else if(status == 'T'){
        return "<span class='badge bg-success'>Diterima</span>"
    }else if(status == 'K'){
        return "<span class='badge bg-danger'>Ditolak</span>"
    }else if(status == 'R'){
        return "<span class='badge bg-success'>Revisi</span>"
    }
}

async function handleDownload(proposalID, filename){
    let downloaded = await proposalStore.adminGetProposalFile(proposalID, filename)
    if(!downloaded){
        let myModal = new bootstrap.Modal(document.getElementById('downloadFailed'))
        myModal.show()
    }
}
</script>