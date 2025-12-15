<template>
<Loading v-bind:isFetching="data.isFetching"/>
<div v-if="!data.isFetching">
    <div v-if="!data.isFetching && !data.currentProposal" class="border rounded p-4 shadow">
        <h1 class="text-center">Data Tidak Ditemukan</h1>
        <h6 class="text-center">Maaf, data proposal dengan ID {{ route.params.proposalID }} tidak dapat kami temukan.</h6>
    </div>
    <div v-if="!data.isFetching && data.currentProposal" class="mt-2 p-4 border border-2 rounded">
        <h4>Detail Proposal:</h4>
        <div class="d-flex align-self-center justify-content-start gap-2">
            <div>
                <input type="text" class="form-control" disabled v-model="data.penulis.nim" placeholder="Nomor Induk Mahasiswa"style="height: 42px;">
                <label class="fw-bold">Periode Proposal: </label>
                <div class="alert alert-primary" role="alert">                  
                    <div v-if="data.currentActivePeriode">  
                        <p class="mb-0">
                            <span class="fw-bold">Title:</span> <br>
                            {{ data.currentActivePeriode.title?data.currentActivePeriode.title:'Belum ada title.'}}
                        </p>  
                        <p class="mb-0">
                            <span class="fw-bold">Batas Waktu Pengumpulan Proposal:</span> <br>
                            <span class="badge bg-dark">{{ dateFormater.formatDateTime(data.currentActivePeriode.tanggal) }}</span>
                        </p>
                        <p class="mb-0">
                            <span class="fw-bold">Tanggal Evaluasi:</span> <br>
                            <span class="badge bg-dark">{{ `${dateFormater.formatDate(data.currentActivePeriode.tglAwal)} sampai ${dateFormater.formatDate(data.currentActivePeriode.tglAkhir)}` }}</span>
                        </p>
                    </div>
                </div>
            </div>

            <div class="flex-grow-1">
                <input v-model="data.penulis.nama" type="text"  disabled style="height: 42px;" class="form-control">
                <label class="fw-bold">Dokumen Proposal: </label> 
                <div>
                    <button v-if="data.fileExists" class="btn btn-link" @click.prevent="handleDownload()">
                        &#x1F5CE; {{ data.currentProposal.filename }}
                    </button>
                    <span v-else class="badge bg-danger">File Tidak Ditemukan</span>
                </div>

                <label class="fw-bold">Usulan Dosen Pembimbing: </label>
                <select disabled class="form-select">
                    <option value="" selected hidden>{{ handleGelarDosen(data.currentProposal.dosenId) }}</option>
                </select>
            </div>
        </div>

        <label class="fw-bold">Judul</label>
        <InputTextArea name="judul" v-model="data.currentProposal.judul" :placeholderText="'Tuliskan judul proposal.'" :disabled="true"/>
        <label class="fw-bold">Latar Belakang</label>
        <MyCKEditor v-model:editorData="data.currentProposal.latarBelakang" name="latarBelakang" v-bind:disabled=true />
        <label class="fw-bold">Rumusan Masalah</label>
        <MyCKEditor v-model:editorData="data.currentProposal.rumusan" name="rumusan" v-bind:disabled=true />
        <label class="fw-bold">Batasan Masalah</label>
        <MyCKEditor v-model:editorData="data.currentProposal.batasan" name="batasan" v-bind:disabled=true />
        <label class="fw-bold">Tujuan Penelitian</label>
        <MyCKEditor v-model:editorData="data.currentProposal.tujuan" name="tujuan" v-bind:disabled=true />
        <label class="fw-bold">Manfaat Penelitian</label>
        <MyCKEditor v-model:editorData="data.currentProposal.manfaat" name="manfaat" v-bind:disabled=true />
        <label class="fw-bold">Daftar Pustaka</label>
        <MyCKEditor v-model:editorData="data.currentProposal.referensi" name="referensi" v-bind:disabled=true /> 
        
        <hr style="height:1.5px;border:none;color:#333;background-color:#333;">
        <div v-if="data.currentTimSidang">
            <h4>Penilaian:</h4>
            <form>
                <label class="fw-bold">Di Evaluasi Oleh: <span class="text-danger">*</span></label>
                <select v-if="data.currentTimSidang":disabled="data.currentDE.status!='V'" class="form-select" v-model="data.post.dosenEvaluatorId" name="dosenEvaluator">
                    <option value="" selected hidden>pilih dosen</option>
                    <option :value="data.currentTimSidang.ketua.dosenId"> {{ data.currentTimSidang.ketua.dosenNama }} (KETUA) </option>
                    <option v-for="anggota in data.currentTimSidang.anggota" :value="anggota.dosenId">{{ anggota.dosenNama }} (ANGGOTA)</option>
                </select>

                <label class="fw-bold">Status <span class="text-danger">*</span></label>
                <select :disabled="data.currentDE.status!='V'" class="form-select" v-model="data.post.status" name="status">
                    <option value="" selected hidden>pilih status</option>
                    <option value="T"> Terima </option>
                    <option value="K"> Tolak </option>
                </select>

                <label class="fw-bold">Catatan <span class="text-danger">*</span></label>
                <MyCKEditor
                    v-model:editorData="data.post.catatan" 
                    name="catatan"
                    :disabled="data.currentDE.status!='V'"
                    :placeholder="'Anda dapat memberikan catatan dan skor kepada proposal mahasiswa yang sedang dinilai.'"
                />
                <div class="text-end mt-3 px-2">
                    <button v-if="data.currentDE.status=='V' && !route.fullPath.includes('mahasiswapage')" class="btn btn-success" @click.prevent="handleUpdate()" :disabled="data.isUpdating || errorExists">{{ data.isUpdating?'saving...':'SIMPAN' }}</button>
                </div>
                <div v-if="!route.fullPath.includes('mahasiswapage')" class="fw-bold text-danger text-center">Cek kembali data sebelum di-submit, perubahan yang Anda lakukan bersifat final</div>
            </form>
        </div>
        <div v-else class="text-center text-danger">
            <h6>Gagal memuat data Tim Dosen Evaluator, Pastikan bahwa mahasiswa ini telah terdaftar dalam sidang Desk Evaluation</h6>
        </div>
    </div>
    <br>
    <br>
</div>
</template>

<script setup>
import { reactive, onBeforeMount, computed } from 'vue';
import dateFormater from '../../helpers/date-formater';
import { useRoute } from 'vue-router';
import { useAdminStore } from '../../stores/admin.store';
import { useDosenStore } from '../../stores/dosen.store';
import { useProposalStore } from '../../stores/proposal.store';

import Loading from '../../components/animation/Loading.vue';
import MyCKEditor from '../../components/texteditor/MyCKEditor.vue';
import InputTextArea from '../../components/fields/InputTextArea.vue';
import { useAuthStore } from '../../stores/auth.store';
import { useAlertStore } from '../../stores/alert.store';
import fileHandler from '../../helpers/file-handler';
import { router } from '../../router';

const adminStore = useAdminStore()
const dosenStore = useDosenStore()
const authStore = useAuthStore()
const alertStore = useAlertStore()
const proposalStore = useProposalStore()
const route = useRoute()
let data = reactive({
    post:{
        catatan:"",
        status:"",
        dosenEvaluatorId:"",
        currentProposalFile:null
    },
    currentDE:null,
    currentProposal:null,
    currentActivePeriode:null,
    currentJadwal:null,
    currentTimSidang:null,
    fileExists:null,
    penulis:null,
    isUpdating:false,
    isFetching:true
})

const errorExists = computed(
    () => {
        if(!data.post.dosenEvaluatorId || !data.post.status || !data.post.catatan){
            return true
        } return false
    }
)

function createUpdateRequest(){
    return {
        tanggalValid: dateFormater.convertToWIB(data.currentDE.tanggalValid),
        catatan: data.post.catatan,
        modified: dateFormater.convertToWIB(new Date()),
        userPenginputDEId: authStore.user.details.id,
        status: data.post.status,
        dosen1Id: data.currentDE.dosen1?.id,
        dosen2Id: null,
        proposalId: data.currentProposal.id,
        judulLama: data.currentProposal.judul,
        judulBaru: data.currentDE.judulBaru,
        mahasiswaNim: data.penulis.nim,
        uploadRevisi: "T",
        dosenEvaluatorId: data.post.dosenEvaluatorId,
        groupEvaluatorId: data.currentJadwal?.id
    }
}

async function handleDownload(){
    let filename = data.currentProposal.filename
    if(route.fullPath.includes('adminpage')){
        await proposalStore.adminGetProposalFile(data.currentProposal.id, filename)
    }else if(route.fullPath.includes('dosenpage')){
        await proposalStore.dosenGetProposalFile(data.currentProposal.id, filename)
    }
}

function handleGelarDosen(dosen){
    let name = dosen.nama
    if (dosen.gelarDepan){
        name = dosen.gelarDepan + " " + name
    }
    if (dosen.gelar){
        name += ", " + dosen.gelar
    }
    return name
}

async function getGelarDosenByID(dosenID){
    let dosenExists = false
    if(route.fullPath.includes('adminpage')){
        dosenExists = await adminStore.getDosenByDosenID(dosenID)
    }else{
        dosenExists = await dosenStore.getDosenByDosenID(dosenID)
    }

    if(dosenExists && route.fullPath.includes('adminpage')){
        return handleGelarDosen(adminStore.dosen)
    }
    if(dosenExists && route.fullPath.includes('dosenpage')){
        return handleGelarDosen(dosenStore.dosen)
    }
}

async function handleUpdate(){
    //REFACTOR
    data.isUpdating=true
    if(route.fullPath.includes('adminpage')){
        let updated = await adminStore.updateDeskEvaluation(data.currentDE.id, createUpdateRequest())
        if(updated){
            await proposalStore.adminUpdateProposalStatus(data.currentProposal.id, data.post.status)
            alertStore.success("Proposal berhasil dievaluasi")
            router.push(`/deskevaluation/adminpage/proposal-mahasiswa/semua-proposal/`)
        }
    }else if(route.fullPath.includes('dosenpage')){
        let updated = await dosenStore.updateDeskEvaluation(data.currentDE.id, createUpdateRequest())
        if(updated){
            await proposalStore.dosenUpdateProposalStatus(data.currentProposal.id, data.post.status)
            router.push(`/deskevaluation/dosenpage/jadwal-sidang/${data.currentJadwal.id}/semua-proposal/`)
        }
    }
    data.isUpdating=false
}

onBeforeMount(
    async () => {
        if(route.fullPath.includes('adminpage')){
            let success = await adminStore.getDeskEvaluationByID(route.params.deskevaluationID)
            if(success){
                data.currentDE = adminStore.deskEvaluation
            }
        }else if(route.fullPath.includes('dosenpage')){
            let success = await dosenStore.getDeskEvaluationByProposalID(route.params.proposalID)
            if(success){
                data.currentDE = dosenStore.deskEvaluation
            }
        }

        if(data.currentDE){
            data.currentProposal = data.currentDE.proposal
            data.currentActivePeriode = data.currentProposal.periodes
            data.penulis = data.currentDE.mahasiswa
            data.currentJadwal = data.currentDE.groupEvaluator

            let timSidangExists = false
            if(route.fullPath.includes('adminpage')){
                data.post.currentProposalFile = await proposalStore.adminGetProposalBlob(data.currentProposal.id)
                data.fileExists = await proposalStore.adminCheckIfProposalExists(data.currentProposal.id)
                timSidangExists = await adminStore.getDosensBySidangID(data.currentJadwal?.id)
                if(timSidangExists){
                    data.currentTimSidang = adminStore.dosenDE
                }

            }
            else if(route.fullPath.includes('dosenpage')){
                data.post.currentProposalFile = await proposalStore.dosenGetProposalBlob(data.currentProposal.id)
                data.fileExists = await proposalStore.dosenCheckIfProposalExists(data.currentProposal.id)
                timSidangExists = await dosenStore.getJadwalSidangDosenByNidnAndPeriodeID(authStore.user.details.nim, data.currentActivePeriode.id)
                if(timSidangExists){
                    for(let i = 0; i<dosenStore.jadwalSidangDosen.length;i++){
                        let jadwal = dosenStore.jadwalSidangDosen[i]
                        if(jadwal.sidangId == data.currentDE.groupEvaluator?.id){
                            data.currentTimSidang = jadwal
                            break
                        }
                    }
                }
            }
        }

        if(data.currentDE.status!='V'){
            data.post.dosenEvaluatorId = data.currentDE.dosenEvaluator?.id
            data.post.status = data.currentDE.status
            data.post.catatan = data.currentDE.catatan?data.currentDE.catatan:"Tidak ada catatan dari Dosen"
        }

        data.currentTimSidang.ketua.dosenNama = await getGelarDosenByID(data.currentTimSidang.ketua.dosenId)
        for(let anggota of data.currentTimSidang.anggota){
            let gelarAnggota = await getGelarDosenByID(anggota.dosenId)
            anggota.dosenNama = gelarAnggota
        }

        data.isFetching = false
    }
)
</script>