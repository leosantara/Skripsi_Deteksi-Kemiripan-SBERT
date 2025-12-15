<template>
    <Loading :isFetching="adminStore.fetching"/>
    <div v-if="!adminStore.fetching && !adminStore.mahasiswa" class="border rounded p-4 shadow">
        <h1 class="text-center">Data Tidak Ditemukan</h1>
        <h6 class="text-center">Maaf, data mahasiswa dengan NIM {{ route.params.nim }} tidak dapat kami temukan.</h6>
        <div class="d-grid gap-2 col-3 mx-auto">
            <button class="btn btn-warning" type="button" @click="router.push('/adminpage/mahasiswa')">Kembali</button>
        </div>
    </div>
    <div v-if="!adminStore.fetching && adminStore.mahasiswa" class="border rounded p-4 shadow">
        <h4>Update Data Mahasiswa</h4>
        <div v-if="data.submitted">
            <h6>Data Berhasil Di-Update!</h6>
        </div>
        <div class="d-flex bd-highlight">
            <form id="oldData" class="p-4 bd-highlight border rounded shadow">
                <h6>Data Lama:</h6>
                <label>NIM:</label>
                <input disabled type="text" class="form-control" v-model="data.currentMahasiswa.nim">

                <label>Nama:</label>
                <input disabled type="text" class="form-control" v-model="data.currentMahasiswa.nama">

                <div class="p-2">
                    <h6>Status:</h6>
                    <span :class="{'badge bg-success':data.currentMahasiswa.status==1,'badge bg-danger':data.currentMahasiswa.status==0}">{{ data.currentMahasiswa.status==1?"Mahasiswa Aktif":"Mahasiswa Nonaktif"}}</span>
                </div>
                <div class="p-2">
                    <h6>Tugas Akhir:</h6>
                    <span :class="{'badge bg-success':data.currentMahasiswa.ta==1,'badge bg-danger':data.currentMahasiswa.ta==0}">{{ data.currentMahasiswa.ta==1?"Sedang Mengikuti Tugas Akhir":"Tidak Sedang Mengikuti Tugas Akhir" }}</span>
                </div>
            </form>

            <form id="newData" class="px-4 flex-grow-1 bd-highlight">
                <h6>Data Baru:</h6>
                <label>NIM <span class="text-danger">*</span></label>
                <InputText :disabled="true" v-model="data.post.nim" name="nim" :placeholderText="'Nomor Induk Mahasiswa'"/>
                
                <label>Nama <span class="text-danger">*</span></label>
                <InputText v-model="data.post.nama" name="nama" :placeholderText="'Nama Mahasiswa'"/>
                
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
        nim:"",
        nama:"",
        status:1,
        ta:1
    },
    currentMahasiswa:{
        nim:"",
        nama:"",
        status:0,
        ta:0
    },
    submitted:false,
    isUpdating:false
})

const validation = yup.object({
    nim: yup.string().required().matches(/^[0-9]+$/, "NIM only accept numbers"),
    nama: yup.string().required()
})
const {values, isSubmitting, errors} = useForm({
    validationSchema: validation
})

function openModal(){
    if(!errors.value.nim&&!errors.value.nama){
        myModal.show()
    }
}

async function handleUpdate(){
    data.isUpdating = true
    data.post.nama = data.post.nama.toUpperCase()
    await adminStore.updateMahasiswa(data.post.nim, data.post)
    
    data.isUpdating = false
    data.submitted = true
}

onBeforeMount(
    async () => {
        if(route.params.nim){
            await adminStore.getMahasiswaByNim(route.params.nim)
            if(adminStore.mahasiswa){
                Object.assign(data.post, adminStore.mahasiswa)
                Object.assign(data.currentMahasiswa, adminStore.mahasiswa)
                
                myModal = new bootstrap.Modal(document.getElementById('confirmation'))
            }
        }
    }
)
</script>

<style scoped>
#oldData{
    background-color: rgb(255, 226, 226);
}
</style>