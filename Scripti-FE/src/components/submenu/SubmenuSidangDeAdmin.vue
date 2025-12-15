<template>
<h1>Proposal Sidang Desk Evaluation</h1>
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
    returnURL:"/deskevaluation/adminpage/proposal-mahasiswa/semua-proposal/"
})

const pageDescription = computed(
    () => {
        if(route.fullPath.includes('/deskevaluation/adminpage/proposal-mahasiswa/semua-proposal/')){
            return ""
        }else if(route.fullPath.includes('/adminpage/proposal-mahasiswa/evaluate/')){
            return "Berikut ini data proposal mahasiswa yang harus dievaluasi:"
        }
    }
)

const backButtonVisible = computed(
    () => {
        if(route.fullPath.includes('/adminpage/proposal-mahasiswa/semua-proposal/')){
            return false
        }return true
    }
)
</script>