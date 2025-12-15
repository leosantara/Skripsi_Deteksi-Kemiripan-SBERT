<template>
    <Loading :isFetching="adminStore.fetching"/>
    <div v-if="!adminStore.fetching" class="border rounded p-4 shadow">
        <h4>Tambah Mahasiswa</h4>
        <div v-if="data.submitted">
            <h6>Data Berhasil Ditambahkan!</h6>
        </div>
        <form v-if="!data.submitted" v-on:submit.prevent="onSubmit()">
            <label>NIM <span class="text-danger">*</span></label>
            <InputText name="nim" v-model="data.post.nim" :placeholderText="'Nomor Induk Mahasiswa'"></InputText>
            
            <label>Nama <span class="text-danger">*</span></label>
            <InputText name="nama" v-model="data.post.nama" :placeholderText="'Nama Mahasiswa'"></InputText>
            
            <div class="p-2">
                <h6>Status:</h6>
                <div class="form-check form-check-inline">
                    <input v-model="data.post.status" class="form-check-input" type="radio" id="status2" :value=1>
                    <label class="form-check-label" for="status2">Aktif</label>
                </div>
                <div class="form-check form-check-inline">
                    <input v-model="data.post.status" class="form-check-input" type="radio" id="status1" :value=0>
                    <label class="form-check-label" for="status1">Tidak Aktif</label>
                </div>
            </div>
            <div class="p-2">
                <h6>Tugas Akhir:</h6>
                <div class="form-check form-check-inline">
                    <input v-model ="data.post.ta" class="form-check-input" type="radio" id="ta2" :value=1>
                    <label class="form-check-label" for="ta2">Sedang Mengikuti</label>
                </div>
                <div class="form-check form-check-inline">
                    <input v-model ="data.post.ta" class="form-check-input" type="radio" id="ta1" :value=0>
                    <label class="form-check-label" for="ta1">Tidak Sedang Mengikuti</label>
                </div> 
            </div>
            <input v-if="!data.submitted" type="submit" :value="!isSubmitting?'TAMBAH':'submitting...'" :disable="isSubmitting" class="btn btn-success">
        </form>

        <button 
            v-if="data.submitted" 
            type="button" @click="reset()"
            class="btn btn-success">
        TAMBAH DATA LAGI
        </button>
    </div>
</template>

<script setup>
import InputText from '../../components/fields/InputText.vue';
import Loading from '../../components/animation/Loading.vue';
import { reactive} from 'vue';
import { useForm } from 'vee-validate';
import { useAdminStore } from '../../stores/admin.store';
import * as yup from "yup"

const adminStore = useAdminStore()
let data = reactive({
    post:{
        nim:"",
        nama:"",
        status:1,
        ta:1
    },
    submitted:false
})

const validation = yup.object({
    nim: yup.string().required().matches(/^[0-9]+$/, "NIM only accept numbers").max(8),
    nama: yup.string().required().max(255)
})
const {values, isSubmitting, handleSubmit, resetForm, errors} = useForm({
    validationSchema: validation
})
const onSubmit = handleSubmit(
    async values => {
        data.post.nama = data.post.nama.toUpperCase()
        data.submitted = await adminStore.addMahasiswa(data.post)
    }
)

function reset(){
    data.submitted=false
    data.post.status=1
    data.post.ta=1
    data.post.nama=""
    data.post.nim=""
}
</script>