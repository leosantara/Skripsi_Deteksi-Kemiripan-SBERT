<template>
    <Loading :isFetching="adminStore.fetching"/>
    <div v-if="!adminStore.fetching && !adminStore.dosen" class="border rounded p-4 shadow">
        <h1 class="text-center">Data Tidak Ditemukan</h1>
        <h6 class="text-center">Maaf, data dosen dengan NIDN {{ route.params.nidn }} tidak dapat kami temukan.</h6>
        <div class="d-grid gap-2 col-3 mx-auto">
            <button class="btn btn-warning" type="button" @click="router.push('/adminpage/dosen')">Kembali</button>
        </div>
    </div>
    <div v-if="!adminStore.fetching && adminStore.dosen" class="border rounded p-4 shadow">
        <h4>Update Data Dosen</h4>
        <div v-if="data.submitted">
            <h6>Data Berhasil Di-Update!</h6>
        </div>
        <div class="d-flex bd-highlight">
            <form id="oldData" class="p-4 bd-highlight border rounded shadow">
                <h6>Data Lama:</h6>
                <label>NIDN:</label>
                <input disabled type="text" class="form-control" v-model="data.currentDosen.nidn">

                <label>Nama:</label>
                <input disabled type="text" class="form-control" v-model="data.currentDosen.nama">

                <label>Email:</label>
                <input disabled type="text" class="form-control" v-model="data.currentDosen.email">

                <label>No Telp:</label>
                <input disabled type="text" class="form-control" v-model="data.currentDosen.telpNo">

                <label>Prodi:</label>
                <input disabled type="text" class="form-control" v-model="data.currentDosen.prodi">

                <div class="p-2">
                    <h6>Status:</h6>
                    {{ data.currentDosen.status }}
                </div>
                <div class="p-2">
                    <h6>Boleh:</h6>
                    {{ data.currentDosen.boleh }}
                </div>
            </form>

            <form id="newData" class="px-4 flex-grow-1 bd-highlight">
                <h6>Data Baru:</h6>
                <label>NIDN <span class="text-danger">*</span></label>
                <InputText :disabled="true" v-model="data.post.nidn" name="nidn" :placeholderText="'Nomor Induk Dosen'"/>
                
                <label>Nama <span class="text-danger">*</span></label>
                <InputText v-model="data.post.nama" name="nama" :placeholderText="'Nama Dosen'"/>
                
                <label>Email <span class="text-danger">*</span></label>
                <InputText v-model="data.post.email" name="email" :placeholderText="'Email Dosen'"/>

                <label>Gelar</label>
                <InputText v-model="data.post.gelar" name="gelar" :placeholderText="'Gelar Dosen'"/>
                
                <label>Gelar Depan</label>
                <InputText v-model="data.post.gelarDepan" name="gelarDepan" :placeholderText="'Gelar Depan Dosen'"/>
                
                <label>No Telp <span class="text-danger">*</span></label>
                <InputText v-model="data.post.telpNo" name="telpNo" :placeholderText="'No Telp Dosen'"/>

                <label>prodi <span class="text-danger">*</span></label>
                <InputText v-model="data.post.prodi" name="prodi" :placeholderText="'Kode Prodi'"/>

                <div class="p-2">
                    <h6>Status:</h6>
                    <div class="form-check form-check-inline">
                        <input v-model="data.post.status" class="form-check-input" type="radio" id="status1" value='S'>
                        <label class="form-check-label" for="status1">S</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input v-model="data.post.status" class="form-check-input" type="radio" id="status2" value='K'>
                        <label class="form-check-label" for="status2">K</label>
                    </div>
                </div>
                <div class="p-2">
                    <h6>Boleh:</h6>
                        <div class="form-check form-check-inline">
                        <input v-model ="data.post.boleh" class="form-check-input" type="radio" id="boleh1" value='B'>
                        <label class="form-check-label" for="boleh1">B</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input v-model ="data.post.boleh" class="form-check-input" type="radio" id="boleh2" value='M'>
                        <label class="form-check-label" for="boleh2">M</label>
                    </div> 
                </div>
                <button type="button" class="btn btn-success" @click="openModal()" :disabled="data.isUpdating">{{!data.isUpdating?'UPDATE':'updating...'}}</button>
            </form>
        </div>
    </div>

    <!-- Modal -->
    <div class="modal fade" id="confirmation" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="exampleModalLabel">Perhatian</h1>
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
import InputText from '../../components/fields/InputText.vue';
import Loading from '../../components/animation/Loading.vue';
import { reactive, onBeforeMount} from 'vue';
import { useForm } from 'vee-validate';
import { useAdminStore } from '../../stores/admin.store';
import { useRoute } from 'vue-router';
import * as yup from "yup"
import { router } from '../../router';

const adminStore = useAdminStore()
const route = useRoute()
let myModal = null

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
    currentDosen:{
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
    submitted:false,
    isUpdating:false
})

const validation = yup.object({
    nim: yup.string().required().matches(/^[0-9]+$/, "NIDN only accept numbers").max(10),
    nama: yup.string().required().max(255),
    telpNo: yup.string().required().matches(/^[0-9]+$/, "No Telp only accept numbers").max(15),
    prodi: yup.string().required().matches(/^[0-9]+$/, "Prodi only accept numbers").max(2),
    email: yup.string().required().matches(/^[\w.+\-]+@(ti\.)?ukdw\.ac\.id$/, "Only accept email address from @ti.ukdw.ac.id or @ukdw.ac.id").max(255)
})
const {values, isSubmitting, errors} = useForm({
    validationSchema: validation
})

function openModal(){
    if(!errors.value.nidn&&!errors.value.nama){
        myModal.show()
    }
}

async function handleUpdate(){
    data.isUpdating = true
    data.post.nama = toTitleCase(data.post.nama)
    await adminStore.updateDosen(data.post.nidn, data.post)
    
    data.isUpdating = false
    data.submitted = true
}

onBeforeMount(
    async () => {
        if(route.params.nidn){
            await adminStore.getDosenByNidn(route.params.nidn)
            if(adminStore.dosen){
                Object.assign(data.post, adminStore.dosen)
                Object.assign(data.currentDosen, adminStore.dosen)
                
                let dosenName = new String(adminStore.dosen.gelarDepan + " " + adminStore.dosen.nama + ", " + adminStore.dosen.gelar).trim()
                data.currentDosen.nama = dosenName
                myModal = new bootstrap.Modal(document.getElementById('confirmation'))
            }
        }
    }
)

function toTitleCase(name){
    let titleCase = ""
    name.split(" ").forEach(word => {
        let capitalized = word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
        titleCase += capitalized + " "
    })
    return titleCase.trim()
}
</script>

<style scoped>
#oldData{
    background-color: rgb(255, 226, 226);
}
</style>
