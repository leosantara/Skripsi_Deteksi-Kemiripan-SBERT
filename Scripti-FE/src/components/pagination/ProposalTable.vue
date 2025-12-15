<template>
    <table class="table table-striped table-hover">
        <thead>
            <tr>
                <th></th>
                <th>NIM</th>
                <th>NAMA</th>
                <th>JUDUL PROPOSAL</th>
                <th>STATUS</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="(data, index) in props.proposalDatas" v-bind:key="index">
                <td>
                    <div class="dropdown">
                        <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Action
                        </button>
                        <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                            <!-- ADMIN ONLY OPTION: -->
                            <a v-if="route.fullPath.includes('/adminpage/') && data.status == 'B'" @click.prevent="validating(data, 'V')" class="dropdown-item" href="#">Set Status Valid</a>
                            <a v-if="route.fullPath.includes('/adminpage/') && data.status != 'B'" @click.prevent="router.push(`/proposalpage/adminpage/update/${data.id}`)" class="dropdown-item" href="#">Sunting Proposal</a>
                            <a v-if="route.fullPath.includes('/adminpage/')" @click.prevent="assignProposal(data)" class="dropdown-item" href="#" data-bs-toggle="modal" data-bs-target="#deleteConfirmation">Hapus Proposal</a>

                            <!-- MAHASISWA ONLY OPTION: -->
                            <a v-if="route.fullPath.includes('/mahasiswapage/')" @click.prevent="router.push(`/proposalpage/mahasiswapage/update/${data.id}`)" class="dropdown-item" href="#">Lihat Proposal</a>
                            
                            <!-- DOSEN ONLY OPTION: -->
                            <a v-if="route.fullPath.includes('/dosenpage/')" @click.prevent="$emit('dosenLihatDetail', data)" class="dropdown-item" href="#">Lihat Proposal</a>
                        </div>
                    </div>
                </td>
                <td>{{ data.mahasiswa?.nim || '' }}</td>
                <td>{{ data.mahasiswa?.nama || '' }}</td>
                <td>
                    <span v-html="handleStatusRevisi(data.uploadRevisi)"></span> <br>
                    <span class="fw-bold">{{ data.judul }}</span> <br>
                    Diunggah: <span class="badge bg-dark">{{ dateFormater.formatDateTime(data.modified) }}</span> || <a class="myDownloadLink" href="#" @click.prevent="handleDownload(data.id, data.filename)" >&#x1F5CE; Download Proposal</a>
                </td>
                <td> 
                    <span class="badge bg-dark">{{ handleStatus(data.status) }}</span> <br> 
                </td>
            </tr>
        </tbody>
    </table>
    
    <!-- Modal -->
    <div class="modal fade" id="deleteConfirmation" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="exampleModalLongTitle">Perhatian</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" />
                </div>
                <div class="modal-body">
                    Anda akan menghapus data proposal dengan judul: <br> 
                    <b>{{ data.currentProposal?`${data.currentProposal.judul}`:""}}</b><br>
                    yang di tulis oleh: 
                    <b>{{ data.currentProposal?.mahasiswa?`${data.currentProposal.mahasiswa.nama}`:"Data penulis tidak dapat ditemukan"}}</b><br>
                    dengan periode evaluasi: <br>
                    <b>{{ data.currentProposal?`${dateFormater.formatDate(data.currentProposal.periodes.tglAwal)} s/d ${dateFormater.formatDate(data.currentProposal.periodes.tglAwal)}`:"Data periode tidak dapat ditemukan"}}</b><br><br>
                    Begitu data dihapus, data tidak dapat di restore, apa Anda yakin ingin melanjutkan?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-bs-dismiss="modal" @click="deleteProposal()">Hapus</button>
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
import { defineProps, defineEmits, reactive, onBeforeMount } from 'vue';
import dateFormater from '../../helpers/date-formater';
import { router } from '../../router';
import { useRoute } from 'vue-router';
import { useProposalStore } from '../../stores/proposal.store';

const route = useRoute()
const proposalStore = useProposalStore()
let data = reactive({
    currentProposal:null
})

const props = defineProps({
    proposalDatas:{
        type:Array,
        required:true
    }
})

const emit = defineEmits(['deleteProposal', 'dosenLihatDetail'])

function assignProposal(proposal){
    data.currentProposal = proposal
}

function deleteProposal(){
    let proposalID = data.currentProposal?data.currentProposal.id:""
    emit('deleteProposal', proposalID)
}

function handleStatus(status){
    if(status == 'B'){
        return 'Baru'
    }else if(status == 'V'){
        return 'Valid'
    }else if(status == 'L'){
        return 'Batal'
    }else if(status == 'T'){
        return 'Diterima'
    }else if(status == 'K'){
        return 'Ditolak'
    }else if(status == 'R'){
        return 'Revisi'
    }
}

async function handleDownload(proposalID, filename){
    let downloaded = null
    if(route.fullPath.includes('/adminpage/')){
        downloaded = await proposalStore.adminGetProposalFile(proposalID, filename)
    }else if(route.fullPath.includes('/mahasiswapage/')){
        downloaded = await proposalStore.mahasiswaGetProposalFile(proposalID, filename)
    }else if(route.fullPath.includes('/dosenpage/')){
        downloaded = await proposalStore.dosenGetProposalFile(proposalID, filename)
    }

    if(!downloaded){
        let myModal = new bootstrap.Modal(document.getElementById('downloadFailed'))
        myModal.show()
    }
}

function handleStatusRevisi(statusRevisi){
    if(statusRevisi==null){
        return "<span class='badge bg-secondary'>Tidak Ada Revisi</span>"
    }else if(statusRevisi=='Y'){
        return "<span class='badge bg-danger'>&#x2612; Belum Upload Revisi</span>"
    }else{
        return "<span class='badge bg-success'>&#x2611; Revisi Ter-upload</span>"
    }
}

async function validating(proposal, newStatus){
    await proposalStore.adminUpdateProposalStatus(proposal.id, newStatus)
    proposal.status = newStatus
}
</script>

<!-- <style>
.myDownloadLink{
    text-decoration: none;
}
.myDownloadLink:hover{
    cursor: pointer;
    color: crimson;
    text-decoration:underline
}
</style> -->