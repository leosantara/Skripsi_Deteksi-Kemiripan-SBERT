<template>
    <h1>Update Proposal</h1>
    <h6>Silakan update file proposal yang telah diupload.</h6>
    <hr style="height:1.5px;border:none;color:#333;background-color:#333;">
    <div>
        <button class="btn btn-outline-dark rounded-pill" @click="router.push('/proposalpage/adminpage/proposal-mahasiswa')" type="button">&#129092; Kembali</button>
    </div>
    <Loading v-bind:isFetching="data.isFetching"/>
    <div v-if="!data.isFetching && !data.currentProposal" class="border rounded p-4 shadow">
        <h1 class="text-center">Data Tidak Ditemukan</h1>
        <h6 class="text-center">Maaf, data proposal dengan ID {{ route.params.propodalID }} tidak dapat kami temukan.</h6>
    </div>
    <div v-if="!data.isFetching && data.currentProposal" class="mt-2 p-4 border border-2 rounded">
        <form v-on:submit.prevent="onSubmit()" action="#" method="POST" enctype="multipart/form-data" id="formProposal">
            <div class="d-flex align-self-center justify-content-start gap-2">
                <div>
                    <input type="text" class="form-control" disabled v-model="data.post.nim" placeholder="Nomor Induk Mahasiswa"style="height: 42px;">
                    <label class="fw-bold">Periode Proposal <span class="text-danger">*</span></label>
                    <div class="alert alert-primary" role="alert">
                        <select v-model="data.currentActivePeriode" class="form-select" required>
                            <option :value=null selected hidden>Pilih Periode Evaluasi</option>
                            <option v-for="(periode, index) in data.allPeriodes" :value="periode">{{ `${dateFormater.formatDate(periode.tglAwal)} s/d ${dateFormater.formatDate(periode.tglAkhir)}` }}</option>
                        </select>                       
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
                    <input v-model="data.penulis" type="text"  disabled style="height: 42px;" class="form-control" placeholder="Nama Mahasiswa">
                    <div class="warn">{{ errors.penulis }}</div>
                    <label for="inputfile" class="fw-bold">Dokumen Proposal <span class="text-danger">*</span></label> <!-- &#x2754; -->
                    <input type="file" @change="getUserFile($event)" class="form-control" id="inputfile" accept=".docx, .doc, .pdf, .odt, text/plain">
                    <div>
                        <span class="fw-bold">File Proposal Terdahulu: </span>
                        <button v-if="data.currentFile" class="btn btn-link" @click.prevent="handleDownload()">
                            &#x1F5CE; {{ data.currentProposal.filename }}
                        </button>
                        <span v-else class="badge bg-danger">File Tidak Ditemukan</span>
                    </div>

                    <label class="fw-bold">Usulan Dosen Pembimbing <span class="text-danger">*</span></label>
                    <select v-model="data.post.dosenId" class="form-select" required>
                        <option value="" selected hidden>Pilih Dosen Pendamping</option>
                        <option v-for="(dosen, index) in data.dosens" :value="dosen.id">{{ getDosenName(dosen) }}</option>
                    </select>
                </div>
            </div>
            <label class="fw-bold">Judul <span class="text-danger">*</span></label>
            <InputTextArea name="judul" v-model="data.post.judul" :placeholderText="'Tuliskan judul proposal, ikuti aturan penulisan judul sesuai kaidah ejaan Bahasa Indonesia atau tulis dalam huruf kapital.'" />
            <label class="fw-bold">Latar Belakang <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.latarBelakang" name="latarBelakang" :placeholder="'Tuliskan Latar Belakang Masalah'" />
            <label class="fw-bold">Rumusan Masalah <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.rumusan" :placeholder="'Tuliskan Rumusan Masalah'" name="rumusan" />
            <label class="fw-bold">Batasan Masalah <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.batasan" :placeholder="'Tuliskan Batasan Masalah'" name="batasan" />
            <label class="fw-bold">Tujuan Penelitian <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.tujuan" :placeholder="'Tuliskan Tujuan Penulisan'" name="tujuan" />
            <label class="fw-bold">Manfaat Penelitian <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.manfaat" :placeholder="'Tuliskan Manfaat Penulisan'" name="manfaat" />
            <label class="fw-bold">Daftar Pustaka <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.referensi" :placeholder="'Tuliskan Referensi yang Anda pakai dalam menuliskan proposal'" name="referensi" /> 
            <input type="submit" :value="!isSubmitting?'UPDATE':'updating...'" :disable="isSubmitting" class="btn btn-success mt-4">
        </form>
        <span v-if="areErrorsExists" class="warn">Mohon lengkapi semua data!</span>
    </div>
    <br>
    <br>
</template>

<script setup>
import { reactive, onBeforeMount, computed, onUpdated} from 'vue';
import { router } from '../../router';
import { useRoute } from 'vue-router';
import * as yup from "yup"
import { useForm } from 'vee-validate';
import { useAuthStore } from '../../stores/auth.store';
import { useAdminStore } from '../../stores/admin.store';
import { useAlertStore } from '../../stores/alert.store';
import { useProposalStore } from '../../stores/proposal.store';
import dateFormater from '../../helpers/date-formater';

import MyCKEditor from '../../components/texteditor/MyCKEditor.vue';
import Loading from '../../components/animation/Loading.vue';
import InputTextArea from '../../components/fields/InputTextArea.vue';
import fileHandler from '../../helpers/file-handler';

const route = useRoute()
const proposalStore = useProposalStore()
const authStore = useAuthStore()
const adminStore = useAdminStore()
const alertStore = useAlertStore()

let data = reactive({
    post:{
        nim:"",
        userId:"",
        dosenId:"",
        periodeId:"",
        judul:"",
        latarBelakang:"",
        rumusan:"",
        batasan:"",
        tujuan:"",
        referensi:"",
        manfaat:"",
        status:"",
        file:null,
        uploadRevisi:""
    },
    penulis:"",
    dosens:[],
    allPeriodes:[],
    currentActivePeriode:null,
    currentProposal:{},
    currentFile:null,
    isFetching:true
})

const validation = yup.object({
    judul: yup.string().required(),
    latarBelakang: yup.string().required(),
    rumusan: yup.string().required(),
    batasan: yup.string().required(),
    tujuan: yup.string().required(),
    manfaat: yup.string().required(),
    referensi: yup.string().required(),
})
const {defineField, values, isSubmitting, handleSubmit, errors} = useForm({
    validationSchema: validation
})
const onSubmit = handleSubmit(
    async values => {
        // data.post.revisiDari = data.currentProposal.id
        data.post.status = data.currentProposal.status
        data.post.uploadRevisi = 'T'
        if(!data.post.file && data.currentFile){
            data.post.file = fileHandler.convertBlobToFile(data.currentFile, data.currentProposal.filename)
        }

        let formData = new FormData()
        Object.keys(data.post).forEach(
            key => {
                formData.append(key, data.post[`${key}`])
                //console.log(`${key} - ${data.post[`${key}`]}`)
            }
        )
        //console.log(...formData)
        let updated = await proposalStore.adminUpdateProposal(route.params.proposalID, formData)
        if(updated){
            router.push('/proposalpage/adminpage/proposal-mahasiswa')
        }
    },
    errors => {
        alertStore.error('Mohon lengkapi semua data!')
    }
)

function getDosenName(dosen){
    let name = dosen.nama
    if (dosen.gelarDepan){
        name = dosen.gelarDepan + " " + name
    }
    if (dosen.gelar){
        name += ", " + dosen.gelar
    }
    return name
}

function getUserFile(event){
    if(event.target.files && event.target.files[0]){
        data.post.file = event.target.files[0]
    }
}

async function handleDownload(){
    let filename = data.currentProposal.filename
    await proposalStore.adminGetProposalFile(route.params.proposalID, filename)
}

const areErrorsExists = computed(
    () => {
        return errors.value.penulis||errors.value.judul||errors.value.latarBelakang||errors.value.rumusan||errors.value.batasan||errors.value.tujuan||errors.value.referensi
    }
)

onBeforeMount(
    async () => {
        await proposalStore.adminGetProposalByID(route.params.proposalID)
        if(proposalStore.proposal){
            Object.assign(data.currentProposal, proposalStore.proposal)
            for (const key in data.post) {
                if (data.post.hasOwnProperty(key)) { 
                    //console.log('current key : ', key)
                    if(key == 'nim'){
                        data.post[`${key}`] = data.currentProposal.mahasiswa.nim
                        data.penulis = data.currentProposal.mahasiswa.nama
                    }else if(key == 'dosenId'){
                        data.post[`${key}`] = data.currentProposal.dosenId.id
                    }else if(key == 'userId'){
                        data.post[`${key}`] = authStore.user?.details.id
                    }else if(key == 'periodeId'){
                        data.post[`${key}`] = data.currentProposal.periodes.id
                        data.currentActivePeriode = data.currentProposal.periodes
                    }else{
                        data.post[`${key}`] = data.currentProposal[`${key}`]
                    }
                }
            }
        }

        await adminStore.getAllDosen()
        if(adminStore.dosens.length>0){
            data.dosens = adminStore.dosens
        }

        await adminStore.getAllPeriode()
        if(adminStore.periodes.length>0){
            data.allPeriodes = adminStore.periodes
        }
        
        data.currentFile = await proposalStore.adminGetProposalBlob(route.params.proposalID, data.currentProposal.mimeType)
        
        data.isFetching = false
    }
)

//for debugging:
// onUpdated(
//     () => {
//         console.log('current proposal : ', data.post)
//     }
// )
</script>

<style scoped>
.warn{
    color: crimson;
}
</style>