<template>
    <h1>Tambah Proposal</h1>
    <h6>tambahkan proposal untuk periode aktif saat ini.</h6>
    <hr style="height:1.5px;border:none;color:#333;background-color:#333;">
    <div>
        <button class="btn btn-outline-dark rounded-pill" @click="router.push('/proposalpage/adminpage/proposal-mahasiswa')" type="button">&#129092; Kembali</button>
    </div>
    <Loading v-bind:isFetching="data.isFetching"/>
    <div v-if="!data.isFetching" class="mt-2 p-4 border border-2 rounded">
        <!-- <div v-if="!data.isFetchingActivePeriode && !data.currentActivePeriode">
            Waktu Unggah Proposal sudah berakhir.
        </div> -->
        <form v-on:submit.prevent="onSubmit()" action="#" method="POST" enctype="multipart/form-data" id="formProposal">
            <div class="d-flex align-self-center justify-content-start gap-2">
                <div>
                    <SearchBar @search="handleSearch" :placeholderText="'Cari NIM mahasiswa...'"/>
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
                    <input v-model="penulis" type="text" required name="penulis" v-bind="penulisAttrs" disabled style="height: 42px;" class="form-control" placeholder="Nama Mahasiswa">
                    <div class="warn">{{ errors.penulis }}</div>
                    <label for="inputfile" class="fw-bold">Dokumen Proposal <span class="text-danger">*</span></label> <!-- &#x2754; -->
                    <input type="file" @change="getUserFile($event)" class="form-control" id="inputfile" accept=".docx, .doc, .pdf, .odt, text/plain" required>
                    
                    <label class="fw-bold">Usulan Dosen Pembimbing <span class="text-danger">*</span></label>
                    <select v-model="data.post.dosenId" class="form-select" required>
                        <option value="" selected hidden>Pilih Dosen Pendamping</option>
                        <option v-for="(dosen, index) in data.dosens" :value="dosen.id">{{ getDosenName(dosen) }}</option>
                    </select>
                </div>
            </div>
            <label class="fw-bold">Judul <span class="text-danger">*</span></label>
            <InputTextArea name="judul" v-model="data.post.judul" :placeholderText="'Tuliskan judul proposal, ikuti aturan penulisan judul sesuai kaidah ejaan Bahasa Indonesia atau tulis dalam huruf kapital.'"/>
            <label class="fw-bold">Latar Belakang <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.latarBelakang" :placeholder="'Tuliskan Latar Belakang Masalah'" name="latarBelakang"/>
            <label class="fw-bold">Rumusan Masalah <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.rumusan" :placeholder="'Tuliskan Rumusan Masalah'" name="rumusan"/>
            <label class="fw-bold">Batasan Masalah <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.batasan" :placeholder="'Tuliskan Batasan Masalah'" name="batasan"/>
            <label class="fw-bold">Tujuan Penelitian <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.tujuan" :placeholder="'Tuliskan Tujuan Penulisan'" name="tujuan"/>
            <label class="fw-bold">Manfaat Penelitian <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.manfaat" :placeholder="'Tuliskan Manfaat Penulisan'" name="manfaat"/>
            <label class="fw-bold">Daftar Pustaka <span class="text-danger">*</span></label>
            <MyCKEditor v-model:editorData="data.post.referensi" :placeholder="'Tuliskan Referensi yang Anda pakai dalam menuliskan proposal'" name="referensi"/> 
            <input type="submit" :value="!isSubmitting?'TAMBAH':'submitting...'" :disable="isSubmitting" class="btn btn-success mt-4">
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
import SearchBar from '../../components/pagination/SearchBar.vue';
import InputTextArea from '../../components/fields/InputTextArea.vue';
import Loading from '../../components/animation/Loading.vue';

const route = useRoute()
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
        status:'B',
        file:null
    },
    penulis:"",
    dosens:[],
    allPeriodes:[],
    currentActivePeriode:null,
    isFetching:true
})

const validation = yup.object({
    penulis: yup.string().required(),
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
const [penulis, penulisAttrs] = defineField('penulis')
const onSubmit = handleSubmit(
    async values => {
        const proposalStore = useProposalStore()
        data.post.userId = authStore.user?.details.id
        data.post.periodeId = data.currentActivePeriode.id

        let formData = new FormData()
        Object.keys(data.post).forEach(
            key => formData.append(key, data.post[`${key}`])
        )
        await proposalStore.adminAddProposal(formData)
        router.push('/proposalpage/adminpage/proposal-mahasiswa')
    },
    errors => {
        alertStore.error('Mohon lengkapi semua data!')
    }
)

const handleSearch = async (keyword) => {
    let name = await authStore.adminFindMahasiswaNameByNim(keyword)
    if(name){
        penulis.value = name
        data.post.nim = keyword
        return
    }
    penulis.value = ""
}

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

const areErrorsExists = computed(
    () => {
        return errors.value.penulis||errors.value.judul||errors.value.latarBelakang||errors.value.rumusan||errors.value.batasan||errors.value.tujuan||errors.value.referensi
    }
)

onBeforeMount(
    async () => {
        await adminStore.getAllDosen()
        if(adminStore.dosens.length>0){
            data.dosens = adminStore.dosens
        }

        await adminStore.getAllPeriode()
        if(adminStore.periodes.length>0){
            data.allPeriodes = adminStore.periodes
        }

        let activePeriodeExists = await adminStore.getCurrentActivePeriode()
        if(activePeriodeExists && adminStore.periodesActive != 'ERROR'){
            data.currentActivePeriode = adminStore.periodesActive[0]
        }
        
        data.isFetching = false
    }
)

//for debugging:
// onUpdated(
//     () => {
//         console.log('selected dosen : ', data.post.dosenId)
//     }
// )
</script>

<style scoped>
.warn{
    color: crimson;
}
</style>