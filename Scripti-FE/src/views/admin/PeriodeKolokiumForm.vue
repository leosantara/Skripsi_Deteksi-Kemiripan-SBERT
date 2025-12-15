<template>
    <div class="border rounded p-4 shadow mb-4">
        <h4>
            Tambah Periode Proposal
        </h4>
        <form v-on:submit.prevent="onSubmit()">
            <label>Title <span class="text-danger">*</span></label>
            <InputText name="Title" v-model="data.post.title" :placeholderText="'contoh: Desk Evaluation 1 tahun 2024'"></InputText>
            <label>Deskripsi <span class="text-danger">*</span></label>
            <InputTextArea name="Deskripsi" v-model="data.post.description" :placeholderText="'Berikan deskripsi untuk periode ini.'"></InputTextArea>
            
            <div class="alert alert-warning mt-3" style="width: 70%;" role="alert">
                <h4 class="alert-heading">&#9888; Perhatian!</h4>
                <p>
                    Untuk menjamin bahwa <b>hanya ada 1 periode yang aktif dalam 1 waktu</b>, pastikan
                    bahwa <b>tanggal Desk Evaluation baru yang Anda buat berada diluar rentang waktu Desk Evaluation yang terakhir
                    Anda tambahkan</b>:
                </p>
                <hr>
                <p v-if="data.lastAdded" class="mb-0"> 
                    <span class="badge bg-dark">{{ dateFormater.formatDate(data.lastAdded.tglAkhir) }}</span> 
                </p>
            </div>
            <label>Batas Pengumpulan Proposal <span class="text-danger">*</span></label>
            <InputDatetime name="Batas Pengumpulan Proposal" v-model="data.dateTimeInput"></InputDatetime>
            <label>Tanggal Awal Desk Evaluation <span class="text-danger">*</span></label>
            <InputDate name="Tanggal Awal DE" v-model="data.post.tglAwal"></InputDate>
            <label>Tanggal Akhir Desk Evaluation <span class="text-danger">*</span></label>
            <InputDate name="Tanggal Akhir DE" v-model="data.post.tglAkhir"></InputDate>
            <input type="submit" :value="!isSubmitting?'TAMBAH':'inserting data...'" :disabled="isSubmitting" class="btn btn-success mt-4">
        </form>
    </div>
</template>

<script setup>
import InputDate from '../../components/fields/InputDate.vue';
import InputDatetime from '../../components/fields/InputDatetime.vue';
import InputText from '../../components/fields/InputText.vue';
import InputTextArea from '../../components/fields/InputTextArea.vue';
import { useAdminStore } from '../../stores/admin.store';
import { onBeforeMount, reactive } from 'vue';
import { useForm } from 'vee-validate';
import * as yup from "yup";
import dateFormater from '../../helpers/date-formater';

const adminStore = useAdminStore()
const data = reactive({
    post:{
        title:"",
        description:"",
        tanggal:"",
        tglAwal:"",
        tglAkhir:""
    },
    dateTimeInput:"",
    lastAdded:null
})

const validation = yup.object({
    Title: yup.string().required().max(255),
    Deskripsi: yup.string().required(),
    "Batas Pengumpulan Proposal": yup.date().required(),
    "Tanggal Awal DE": yup.date().required()
    .test(
        'is TanggalAwalDE greater', 
        "Tanggal Awal DE harus lebih besar atau sama dengan tanggal Batas Pengumpulan Proposal",
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
            if(data.lastAdded){
                let tgl_awal_de = value?.toLocaleDateString()
                let tgl_akhir_prev = (new Date(data.lastAdded.tglAkhir)).toLocaleDateString()
                return new Date(tgl_awal_de) > new Date(tgl_akhir_prev)
            }
            return true
        }
    ),
    "Tanggal Akhir DE": yup.date().required().min(yup.ref('Tanggal Awal DE'), "Tanggal Akhir DE harus lebih besar atau sama dengan Tanggal Awal DE")
})
const {values, isSubmitting, handleSubmit} = useForm({
    validationSchema: validation
})
const onSubmit = handleSubmit(
    async values => {
        //manually adding 7 hours to convert UTC Timezone as if it was a WIB Timezone
        //note that this is not the best approach and better solution should be found
        //in the future.
        let tanggalStr = new Date(data.dateTimeInput).toISOString()
        let tanggalUTC = new Date(tanggalStr)
        tanggalUTC.setHours(tanggalUTC.getHours()+7)
        data.post.tanggal = tanggalUTC.toISOString()
        
        await adminStore.addPeriode(data.post)
    }
)

onBeforeMount(
    async () => {
        await adminStore.getAllPeriode()
        if(adminStore.periodes.length>0){
            data.lastAdded = adminStore.periodes[0]
        }
    }
)
</script>