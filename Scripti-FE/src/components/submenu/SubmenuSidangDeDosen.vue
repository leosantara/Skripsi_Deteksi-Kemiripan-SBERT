<template>
<h1>{{ pageHeader }}</h1>
<h6>{{ pageDescription }}</h6>
<hr style="height:1.5px;border:none;color:#333;background-color:#333;">
<div>
    <div class="d-flex justify-content-start gap-2">
        <button class="btn btn-outline-dark rounded-pill" v-if="backButtonVisible" @click="router.push(data.returnURL)" type="button">&#129092; Kembali</button>
        <!-- <button class="btn btn-primary rounded-pill" v-if="!backButtonVisible" @click.prevent="" type="button">Semua Periode</button> -->
    </div>
</div>
</template>

<script setup>
import { useRoute } from 'vue-router';
import { router } from '../../router';
import { reactive, onUpdated, watch, computed } from 'vue';

let route = useRoute()
let data = reactive({
    returnURL:"/deskevaluation/dosenpage/jadwal-sidang/current-periode"
})

const pageHeader = computed(
    () => {
        if(route.fullPath.includes('/current-periode')){
            return "Jadwal Sidang Evaluasi"
        }else if(route.fullPath.includes('/evaluate')){
            return "Evaluasi Proposal"
        }else if(route.fullPath.includes('/semua-proposal/')){
            return "Peserta Sidang"
        }else{
            return "Sidang Evaluasi"
        }
    }
)

const pageDescription = computed(
    () => {
        if(route.fullPath.includes('/current-periode')){
            return "Berikut ini jadwal sidang yang Anda ikuti pada periode ini:"
        }else if(route.fullPath.includes('/evaluate')){
            return "Berikut data proposal mahasiswa yang harus dievaluasi:"
        }else if(route.fullPath.includes('/semua-proposal/')){
            return "Berikut data mahasiswa yang mengikuti sidang:"
        }else{
            return ""
        }
    }
)

const backButtonVisible = computed(
    () => {
        if(route.fullPath.includes('/current-periode')){
            return false
        }return true
    }
)

watch(
    () => route.fullPath,
    (newRoute, oldRoute) => {
        if(route.fullPath.includes('/evaluate')){
            data.returnURL=oldRoute
        } else{
            data.returnURL='/deskevaluation/dosenpage/jadwal-sidang/current-periode'
        }
    }
)
</script>