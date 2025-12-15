<template>
<h1>Jadwal Sidang Desk Evaluation</h1>
<hr style="height:1.5px;border:none;color:#333;background-color:#333;">
<div class="d-flex justify-content-between">
    <div class="d-flex justify-content-start gap-2">
        <button class="btn btn-outline-dark rounded-pill" @click="router.push(data.returnURL)" type="button">&#129092; Kembali</button>
    </div>
    <div v-if="showSubmenu" class="d-flex justify-content-start gap-2">
        <!-- data.currentID?route.params.periodeID:data.currentID -->
        <button 
            class="btn rounded-pill" 
            @click="router.push(`/deskevaluation/adminpage/periode/${route.params.periodeID}/semua-sidang`)" 
            type="button"
            v-bind:class="
                {
                    'btn-primary':route.fullPath.includes('semua-sidang'), 
                    'btn-outline-primary':!route.fullPath.includes('semua-sidang')
                }">
            Semua Jadwal
        </button>
        <button 
            class="btn rounded-pill" 
            @click="router.push(`/deskevaluation/adminpage/periode/${route.params.periodeID}/tambah`)"
            type="button"
            v-bind:class="
                {
                    'btn-success':route.fullPath.includes('tambah'), 
                    'btn-outline-success':!route.fullPath.includes('tambah')
                }">
            &#128934; Tambah Jadwal
        </button>
    </div>
</div>
</template>

<script setup>
import { useRoute } from 'vue-router';
import { router } from '../../router';
import { reactive, onUpdated, watch, computed } from 'vue';

let route = useRoute()
let data = reactive({
    returnURL:"/adminpage/periode-proposal"
})

//TODO: REFACTOR
const showSubmenu = computed(
    () => {
        if(
            route.fullPath.includes('/update') ||
            route.fullPath.includes('/setup')
        ){ return false }
        return true
    }
)

watch(
    () => route.fullPath,
    (newRoute, oldRoute) => {
        if(route.fullPath.includes('/update') || route.fullPath.includes('/setup')){
            data.returnURL=oldRoute
        } else{
            data.returnURL='/adminpage/periode-proposal'
        }
    }
)
</script>