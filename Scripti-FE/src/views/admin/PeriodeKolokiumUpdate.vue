<template>
    <Loading :isFetching="data.isFetching"/>
    <div v-if="!data.isFetching && data.currentPeriode.isEmpty" class="border rounded p-4 shadow">
        <h1 class="text-center">Data Tidak Ditemukan</h1>
        <h6 class="text-center">Maaf, data Periode tidak dapat kami temukan.</h6>
        <div class="d-grid gap-2 col-3 mx-auto">
            <button class="btn btn-warning" type="button" @click="router.push('/adminpage/periode-proposal')">Kembali</button>
        </div>
    </div>
    <div v-else-if="!data.isFetching && !data.currentPeriode.isEmpty" class="border rounded p-4 shadow mb-4">
        <h4>Update Data Periode Proposal</h4>
        <div class="d-flex bd-highlight">
            <form id="oldData" class="p-4 bd-highlight border rounded shadow"  style="width: 30%;">
                <h6>Data Lama:</h6>
                <label>Title</label>
                <input disabled type="text" class="form-control" :value="data.currentPeriode.title?data.currentPeriode.title:'belum ada judul'" >
                <label>Description:</label>
                <p style="text-align: justify;">{{ data.currentPeriode.description?data.currentPeriode.description:'Belum ada deskripsi.' }}</p>
                <label>Batas Pengumpulan Proposal:</label>
                <p> <span class="badge bg-dark">{{ `${dateFormater.formatDateTime(data.currentPeriode.tanggal)}` }}</span> </p>
                <label>Periode Evaluasi</label>
                <p> <span class="badge bg-dark">{{ `${dateFormater.formatDate(data.currentPeriode.tglAwal)} sampai ${dateFormater.formatDate(data.currentPeriode.tglAkhir)}` }}</span> </p>
                <hr style="height:1.5px;border:none;color:#333;background-color:#333;">
                <ul>
                    <li v-if="!data.addedBefore.isEmpty">
                        <span class="fw-bold" style="color: #58151c;">Tanggal Awal Desk Evaluation yang baru harus lebih dari tanggal:</span>
                        <br>
                        <span class="badge bg-danger">{{ `${dateFormater.formatDate(data.addedBefore.tglAkhir)}` }}</span>
                    </li>
                    <li v-if="!data.addedAfter.isEmpty">
                        <span class="fw-bold" style="color: #58151c;">Tanggal Akhir Desk Evaluation yang baru harus kurang dari tanggal:</span>
                        <br>
                        <span class="badge bg-danger">{{ `${dateFormater.formatDate(data.addedAfter.tglAwal)}` }}</span>
                    </li>
                </ul>
            </form>
            <form v-on:submit.prevent="onSubmit()" class="px-4 flex-grow-1 bd-highlight" >
                <h6>Data Baru:</h6>
                <label>Title <span class="text-danger">*</span></label>
                <InputText name="Title" v-model="data.post.title" :placeholderText="'contoh: Desk Evaluation 1 tahun 2024'"></InputText>
                <label>Deskripsi <span class="text-danger">*</span></label>
                <InputTextArea name="Deskripsi" v-model="data.post.description" :placeholderText="'Berikan deskripsi untuk periode ini.'"></InputTextArea>
                <label>Batas Pengumpulan Proposal <span class="text-danger">*</span></label>
                <InputDatetime name="Batas Pengumpulan Proposal" v-model="data.dateTimeInput"></InputDatetime>
                <label>Tanggal Awal Desk Evaluation <span class="text-danger">*</span></label>
                <InputDate name="Tanggal Awal DE" v-model="data.post.tglAwal"></InputDate>
                <label>Tanggal Akhir Desk Evaluation <span class="text-danger">*</span></label>
                <InputDate name="Tanggal Akhir DE" v-model="data.post.tglAkhir"></InputDate>
                <!-- <input type="submit" :value="!isSubmitting?'UPDATE':'updating data...'" :disabled="isSubmitting" class="btn btn-success mt-4"> -->
                <button type="button" class="btn btn-success" @click="openModal()" :disabled="data.isUpdating">{{!data.isUpdating?'UPDATE':'updating...'}}</button>
            </form>
        </div>
    </div>

    <!-- Modal -->
    <div class="modal fade" id="confirmation" tabindex="-1" aria-labelledby="updateConfirmation" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="updateConfirmation">Perhatian</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body row">
                    <span class="fs-6">Cek kembali data yang akan di-update. Begitu data di-update, perubahan bersifat final, apakah Anda yakin untuk melanjutkan?</span>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-success" data-bs-dismiss="modal" @click="handleUpdate()">Update Data</button>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import Loading from '../../components/animation/Loading.vue';
import InputDate from '../../components/fields/InputDate.vue';
import InputDatetime from '../../components/fields/InputDatetime.vue';
import InputText from '../../components/fields/InputText.vue';
import InputTextArea from '../../components/fields/InputTextArea.vue';
import { useAdminStore } from '../../stores/admin.store';
import { onBeforeMount, reactive } from 'vue';
import { useRoute } from 'vue-router';
import { useForm } from 'vee-validate';
import { router } from '../../router';
import dateFormater from '../../helpers/date-formater';
import * as yup from "yup";

const adminStore = useAdminStore()
let myModal = null
const route = useRoute()
const data = reactive({
    post:{
        title:"",
        description:"",
        tanggal:"",
        tglAwal:"",
        tglAkhir:""
    },
    dateTimeInput:"",
    currentPeriode:{isEmpty:true},
    addedAfter:{isEmpty:true},
    addedBefore:{isEmpty:true},
    isFetching:true
})

const validation = yup.object({
    Title: yup.string().required().max(255),
    Deskripsi: yup.string().required(),
    "Batas Pengumpulan Proposal": yup.date().required(),
    "Tanggal Awal DE": yup.date().required()
    .test(
        'is TanggalAwalDE greater', 
        "Tanggal Awal DE harus lebih besar atau sama dengan tanggal Batas Pengumpulan Proposal yang baru",
        (value) => {
            let tgl_awal_de = value?.toLocaleDateString()
            let tgl_kumpul_proposal = (new Date(data.dateTimeInput)).toLocaleDateString()
            return new Date(tgl_awal_de) >= new Date(tgl_kumpul_proposal)
        }
    )
    .test(
        'is prev periode over',
        "Desk Evaluation pada periode sebelumnya masih berlangsung di tanggal ini, silakan ganti tanggal",
        (value) => {
            if(!data.addedBefore.isEmpty){
                let tgl_awal_de = value?.toLocaleDateString()
                let tgl_akhir_prev = (new Date(data.addedBefore.tglAkhir)).toLocaleDateString()
                return new Date(tgl_awal_de) > new Date(tgl_akhir_prev)
            }
            return true
        }
    ),
    "Tanggal Akhir DE": yup.date().required()
    .min(yup.ref('Tanggal Awal DE'), "Tanggal Akhir DE harus lebih besar atau sama dengan Tanggal Awal DE")
    .test(
        "are next DE started yet",
        "Desk Evaluation untuk periode selanjutnya sudah berlangsung pada tanggal ini, silakan majukan tanggal",
        (value) => {
            if(!data.addedAfter.isEmpty){
                let tgl_akhir_de = value?.toLocaleDateString()
                let tgl_awal_next = (new Date(data.addedAfter.tglAwal)).toLocaleDateString()
                return new Date(tgl_akhir_de) < new Date(tgl_awal_next)
            }
            return true
        }
    )
})
const {values, handleSubmit, errors} = useForm({
    validationSchema: validation
})
const onSubmit = handleSubmit(
    async values => {
        let tanggalStr = new Date(data.dateTimeInput).toISOString()
        let tanggalUTC = new Date(tanggalStr)
        tanggalUTC.setHours(tanggalUTC.getHours()+7)
        data.post.tanggal = tanggalUTC.toISOString()
        
        await adminStore.updatePeriode(route.params.periodeID, data.post)
    }
)

async function handleUpdate(){
    data.isUpdating = true
    let tanggalStr = new Date(data.dateTimeInput).toISOString()
    let tanggalUTC = new Date(tanggalStr)
    tanggalUTC.setHours(tanggalUTC.getHours()+7)
    data.post.tanggal = tanggalUTC.toISOString()
    await adminStore.updatePeriode(route.params.periodeID, data.post)
    
    data.isUpdating = false
    data.submitted = true
}

function openModal(){
    if(!errors.value.Title&&!errors.value.Deskripsi&&!errors.value['Batas Pengumpulan Proposal']&&!errors.value['Tanggal Awal DE']&&!errors.value['Tanggal Akhir DE']){
        myModal.show()
    }
}

function getWIBDate(utcDate){
    //date in DB is 7 hours behind compared to the date in WIB
    let utcDateStr = new Date(utcDate).toISOString()
    let tanggalUTC = new Date(utcDateStr)
    tanggalUTC.setHours(tanggalUTC.getHours()+7)

    let defaultDateFormat = tanggalUTC.toISOString().split('T')[0]
    return defaultDateFormat
}

function getWIBDatetime(utcDateTime){
    let utcDateStr = new Date(utcDateTime).toISOString()
    let datetimeArr = utcDateStr.split('T')
    let dateOnly = datetimeArr[0]
    let timeArr = datetimeArr[1].split(':')

    return `${dateOnly}T${timeArr[0]}:${timeArr[1]}`
}

onBeforeMount(
    async () => {
        let adjacentPeriode = []
        await adminStore.getPeriodeById(route.params.periodeID)
        if(adminStore.periode){
            adjacentPeriode = await adminStore.getAdjacentPeriode(route.params.periodeID)
            for(let i=0;i<adjacentPeriode.length;i++){
                if(adjacentPeriode[i].id < route.params.periodeID ){
                    data.addedBefore = adjacentPeriode[i]
                    data.addedBefore['isEmpty'] = false
                }else if(adjacentPeriode[i].id == route.params.periodeID ){
                    Object.assign(data.currentPeriode, adjacentPeriode[i])
                    data.currentPeriode['isEmpty'] = false

                    //for data to be shown, 
                    //the DATE format must be yyyy-MM-dd while the DATETIME format must be yyyy-MM-ddTHH:mm
                    data.post = adjacentPeriode[i]
                    data.post.tglAwal = getWIBDate(data.post.tglAwal)
                    data.post.tglAkhir = getWIBDate(data.post.tglAkhir)
                    data.dateTimeInput = getWIBDatetime(data.post.tanggal)
                }else{
                    data.addedAfter = adjacentPeriode[i]
                    data.addedAfter['isEmpty'] = false
                }
            }
        }
        myModal = new bootstrap.Modal(document.getElementById('confirmation'))
        data.isFetching = false
    }
)
</script>

<style scoped>
#oldData{
    background-color: rgb(255, 226, 226);
}
</style>