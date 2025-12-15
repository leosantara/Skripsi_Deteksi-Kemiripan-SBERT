<template>
    <Loading v-bind:isFetching="data.isFetching"/>
    <div v-if="!data.isFetching" class="mt-2 p-4 border border-2 rounded">
        <div v-if="!data.currentPeriode" class="text-center">
            <h4>Maaf, data periode tidak dapat ditemukan.</h4>
        </div>
        <div v-else>
            <div v-if="!data.isSubmitted">
                <h6>Anda dapat menambahkan jadwal sidang untuk periode saat ini atau periode lainnya:</h6>
                <form v-on:submit.prevent="onSubmit()">
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
                    <input type="submit" :value="!isSubmitting?'TAMBAH':'submitting...'" :disable="isSubmitting" class="btn btn-success mt-4">
                </form>
            </div>
            <div class="text-center" v-else>
                <h4>Data Berhasil Disubmit!</h4>
                <button type="button"class="btn btn-primary" @click="resetForm()">Tambah Data Lagi</button>
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
import { useForm } from 'vee-validate';
import dateFormater from '../../helpers/date-formater';
import * as yup from "yup";

import Loading from '../../components/animation/Loading.vue';
import InputDatetime from '../../components/fields/InputDatetime.vue';

const route = useRoute()
const adminStore = useAdminStore()
let data = reactive({
    post:{
        kelompok:null,
        periodeId:"",
        waktu:""
    },
    allPeriodes:[],
    currentPeriode:null,
    isFetching:true,
    isSubmitted:false
})
let letters = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"]

const validation = yup.object({
    "Tanggal Sidang":yup.date().required()
})
const {values, isSubmitting, handleSubmit, errors} = useForm({
    validationSchema: validation
})
const onSubmit = handleSubmit(
    async values => {
        data.post.waktu = dateFormater.convertToWIB(data.post.waktu)
        data.isSubmitted = await adminStore.createSidangDE(data.post)
    },
    errors => {
        alertStore.error('Mohon lengkapi semua data!')
    }
)

function resetForm(){
    data.post = {
        kelompok:null,
        periodeId:data.currentPeriode.id,
        waktu:""
    }
    data.isSubmitted = false
}

onBeforeMount(
    async () => {
        await adminStore.getPeriodeById(route.params.periodeID)
        data.currentPeriode = adminStore.periode
        data.post.periodeId = adminStore.periode.id

        await adminStore.getAllPeriode()
        data.allPeriodes = adminStore.periodes

        data.isFetching = false
    }
)
</script>