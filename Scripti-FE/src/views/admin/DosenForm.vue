<template>
    <Loading :isFetching="adminStore.fetching"/>
    <div v-if="!adminStore.fetching" class="border rounded p-4 shadow">
        <h4>Tambah Dosen</h4>
        <div v-if="data.submitted">
            <h6>Data Berhasil Ditambahkan!</h6>
        </div>
        <form v-if="!data.submitted" v-on:submit.prevent="onSubmit()">
            <label>NIDN <span class="text-danger">*</span></label>
            <InputText name="nidn" v-model="data.post.nidn" :placeholderText="'Nomor Induk Dosen'"></InputText>
            
            <label>Nama <span class="text-danger">*</span></label>
            <InputText name="nama" v-model="data.post.nama" :placeholderText="'Nama dosen'"></InputText>
            
            <label>Email <span class="text-danger">*</span></label>
            <InputText name="email" v-model="data.post.email" :placeholderText="'Email'"></InputText>

            <label>Gelar</label>
            <InputText name="gelar" v-model="data.post.gelar" :placeholderText="'Gelar'"></InputText>

            <label>Gelar Depan</label>
            <InputText name="gelarDepan" v-model="data.post.gelarDepan" :placeholderText="'Gelar Depan'"></InputText>
            
            <label>No Telepon <span class="text-danger">*</span></label>
            <InputText name="telpNo" v-model="data.post.telpNo" :placeholderText="'No Telepon'"></InputText>
            
            <label>Prodi <span class="text-danger">*</span></label>
            <InputText name="prodi" v-model="data.post.prodi" :placeholderText="'Prodi'"></InputText>

            <!-- <div class="p-2">
                <h6>Status:</h6>
                <div class="form-check form-check-inline">
                    <input v-model="data.post.status" class="form-check-input" type="radio" id="status1" value="S">
                <label class="form-check-label" for="status1">S</label>
                </div>
                <div class="form-check form-check-inline">
                    <input v-model="data.post.status" class="form-check-input" type="radio" id="status2" value="K">
                <label class="form-check-label" for="status2">K</label>
                </div>
            </div>
            <div class="p-2">
                <h6>Boleh:</h6>
                <div class="form-check form-check-inline">
                    <input v-model ="data.post.boleh" class="form-check-input" type="radio" id="boleh1" value="B">
                    <label class="form-check-label" for="boleh1">B</label>
                </div>
                <div class="form-check form-check-inline">
                    <input v-model ="data.post.boleh" class="form-check-input" type="radio" name="inlineRadioOptions" id="boleh2" value="M">
                    <label class="form-check-label" for="boleh2">M</label>
                </div> 
            </div> -->
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
        nidn:"",
        nama:"",
        gelar:"",
        email:"",
        telpNo:"",
        status:"K",
        gelarDepan:"",
        boleh:"",
        prodi:"71"
    },
    submitted:false
})

const validation = yup.object({
    nidn: yup.string().required().matches(/^[0-9]+$/, "NIDN only accept numbers").max(10),
    nama: yup.string().required().max(255),
    telpNo: yup.string().required().matches(/^[0-9]+$/, "No Telp only accept numbers").max(15),
    prodi: yup.string().required().matches(/^[0-9]+$/, "Prodi only accept numbers").max(2),
    email: yup.string().required().matches(/^[\w.+\-]+@(ti\.)?ukdw\.ac\.id$/, "Only accept email address from @ti.ukdw.ac.id or @ukdw.ac.id").max(255)
})
const {values, isSubmitting, handleSubmit, resetForm, errors} = useForm({
    validationSchema: validation
})
const onSubmit = handleSubmit(
    async values => {
        data.post.nama = toTitleCase(data.post.nama)
        data.submitted = await adminStore.addDosen(data.post)
    }
)

function reset(){
    data.submitted=false
    data.post.status=""
    data.post.boleh=""
    data.post.nama=""
    data.post.nidn=""
    data.post.email=""
    data.post.telpNo=""
    data.post.gelar=""
    data.post.gelarDepan=""
    data.post.prodi="71"
}

function toTitleCase(name){
    let titleCase = ""
    name.split(" ").forEach(word => {
        let capitalized = word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
        titleCase += capitalized + " "
    })
    return titleCase.trim()
}
</script>