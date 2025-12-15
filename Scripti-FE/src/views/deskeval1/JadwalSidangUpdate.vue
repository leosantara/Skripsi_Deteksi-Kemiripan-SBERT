<template>
    <Loading :isFetching="data.isFetching"/>
    <div v-if="!data.isFetching" class="mt-2 p-4 border border-2 rounded">
        <div v-if="!data.currentJadwal" class="text-center">
            <h1 class="text-center">Data Tidak Ditemukan</h1>
            <h6 class="text-center">Maaf, jadwal evaluasi dengan ID {{ route.params.sidangID }} tidak dapat kami temukan.</h6>
        </div>
        <div v-else-if="!data.currentPeriode" class="text-center">
            <h1 class="text-center">Data Tidak Ditemukan</h1>
            <h6 class="text-center">Gagal memuat data periode.</h6>
        </div>
        <div v-else>
            <div class="d-flex bd-highlight">
                <form id="oldData" class="p-4 bd-highlight border rounded shadow">
                    <h6>Data Lama:</h6>
                    <label>Periode</label>
                    <select disabled v-model="data.currentPeriode.id" class="form-select">
                        <option :value=data.currentPeriode.id selected>
                            {{ `${dateFormater.formatDate(data.currentPeriode.tglAwal)} s/d ${dateFormater.formatDate(data.currentPeriode.tglAkhir)}` }}
                        </option>
                    </select>
                    <label>Waktu Sidang</label>
                    <InputDatetime :disabled="true" name="Tanggal Sidang Old" v-model="data.currentJadwal.waktu"/>
                    <label>Kelompok</label>
                    <select disabled required v-model="data.currentJadwal.kelompok" class="form-select">
                        <option :value=data.currentJadwal.kelompok selected>{{data.currentJadwal.kelompok}}</option>
                    </select>
                </form>

                <form id="newData" class="px-4 flex-grow-1 bd-highlight">
                    <label>Periode <span class="text-danger">*</span></label>
                    <select required v-model="data.post.periodeId" class="form-select">
                        <option :value=null selected hidden>Pilih Periode Evaluasi</option>
                        <option v-for="(periode, index) in data.allPeriodes" :value="periode.id">
                            {{ `${dateFormater.formatDate(periode.tglAwal)} s/d ${dateFormater.formatDate(periode.tglAkhir)}` }}
                        </option>
                    </select>
                    <label>Waktu Sidang <span class="text-danger">*</span></label>
                    <InputDatetime name="Tanggal Sidang" v-model="data.post.waktu"/>
                    <label>Kelompok <span class="text-danger">*</span></label>
                    <select required v-model="data.post.kelompok" class="form-select">
                        <option :value=null selected hidden>Pilih Kelompok</option>
                        <option v-for="(char) in letters" :value="char">
                            {{ char }}
                        </option>
                    </select>
                    <button type="button" :disable="data.isSubmitting" class="btn btn-success mt-4" @click="handleUpdate()">
                        {{ !data.isSubmitting?'UPDATE':'updating...' }}
                    </button>
                </form>
            </div>
        </div>
    </div>
    <br>
    <br>
</template>

<script setup>
import { reactive, onBeforeMount } from 'vue';
import { useAdminStore } from '../../stores/admin.store';
import { useRoute } from 'vue-router';
import { router } from '../../router';
import dateFormater from '../../helpers/date-formater';
import * as yup from "yup";
import { useForm } from 'vee-validate';

import Loading from '../../components/animation/Loading.vue';
import InputDatetime from '../../components/fields/InputDatetime.vue';

const adminStore = useAdminStore()
const route = useRoute()
let data = reactive({
    post:{
        kelompok:null,
        periodeId:"",
        waktu:""
    },
    dateTimeInput:"",
    currentJadwal:null,
    currentPeriode:null,
    allPeriodes:[],
    isFetching:true,
    isSubmitting:false
})
let letters = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"]

const validation = yup.object({
    "Tanggal Sidang":yup.date().required()
})
const {values, errors} = useForm({
    validationSchema: validation
})
async function handleUpdate(){
    data.post.waktu = dateFormater.convertToWIB(data.post.waktu)
    data.isSubmitted = await adminStore.updateSidangDE(route.params.sidangID, data.post)
    router.push(`/deskevaluation/adminpage/periode/${data.currentPeriode.id}/semua-sidang`)
}

onBeforeMount(
    async () => {
        await adminStore.getSidangDEByID(route.params.sidangID)
        if(adminStore.sidangDE){
            data.currentJadwal = adminStore.sidangDE
            data.currentJadwal.waktu = dateFormater.convertToDateTimeInput(data.currentJadwal.waktu)
            
            Object.assign(data.post, data.currentJadwal)
        }

        if(data.currentJadwal){
            await adminStore.getPeriodeById(data.currentJadwal.periodeId)
            data.currentPeriode = adminStore.periode
        }

        await adminStore.getAllPeriode()
        if(adminStore.periodes.length>0){
            data.allPeriodes = adminStore.periodes
        }

        data.isFetching = false
    }
)
</script>

<style scoped>
#oldData{
    background-color: rgb(255, 226, 226);
}
</style>