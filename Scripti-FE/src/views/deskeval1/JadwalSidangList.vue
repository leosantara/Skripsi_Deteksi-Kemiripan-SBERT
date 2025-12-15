<template>
    <Loading v-bind:isFetching="data.isFetching"/>
    <div v-if="!data.isFetching" class="mt-2 p-4 border border-2 rounded">
        <div v-if="!data.currentPeriode" class="text-center">
            <h4>Maaf, data periode tidak dapat ditemukan.</h4>
        </div>
        <div v-else>
            <h4>Semua Sidang</h4>
            <h6 v-if="data.currentPeriode">
                {{ data.jadwals.length>0?'Berikut ini Jadwal Sidang Desk Evaluation untuk periode evaluasi:':'Silakan tambah jadwal baru untuk periode evaluasi:' }} <br>
                <span class="badge bg-dark mt-1">{{ `${dateFormater.formatDate(data.currentPeriode.tglAwal)} s/d ${dateFormater.formatDate(data.currentPeriode.tglAkhir)}` }}</span>
            </h6>
            <JadwalDETable 
                v-if="paginatedPage.length>0"
                v-bind:sidangDatas="paginatedPage"
                v-bind:periodeData="data.currentPeriode"
                @deleteSidang="handleDeleteSidang"
            />
            <PaginationBar 
                v-if="data.jadwals.length>0"
                v-bind:totalItems="data.jadwals.length"
                v-bind:itemPerPages="data.pagination.itemPerPage"
                v-model:selected="data.pagination.currentPage"
            />
        </div>
    </div>
    <br>
    <br>
</template>

<script setup>
import { reactive, computed, onBeforeMount } from 'vue';
import { useRoute } from 'vue-router';
import Loading from '../../components/animation/Loading.vue';
import PaginationBar from '../../components/pagination/PaginationBar.vue';
import JadwalDETable from '../../components/pagination/JadwalDETable.vue';

import { useAdminStore } from '../../stores/admin.store';
import { useAlertStore } from '../../stores/alert.store';
import dateFormater from '../../helpers/date-formater';

const route = useRoute()
const adminStore = useAdminStore()
const alertStore = useAlertStore()
let data = reactive({
    currentPeriode:null,
    jadwals:[],
    pagination:{
        currentPage:1,
        itemPerPage:10
    },
    isFetching:true
})

const paginatedPage = computed(
    () => {
        return data.jadwals.slice((data.pagination.currentPage - 1) * data.pagination.itemPerPage, (data.pagination.currentPage) * data.pagination.itemPerPage)
    }
)

async function handleDeleteSidang(sidangID){
    let canBeDeleted = await adminStore.checkIfDosenExistsOnSidang(sidangID)
    if(canBeDeleted){
        await adminStore.deleteSidangDE(sidangID)
        data.jadwals = adminStore.sidangDEArr
    }
    else{alertStore.error('Delete gagal, terdapat sidang yang sedang berlangsung pada tanggal tersebut!')}
}

onBeforeMount(
    async () => {
        if(!adminStore.periode || adminStore.periode?.id != route.params.periodeID){
            await adminStore.getPeriodeById(route.params.periodeID)
        }
        data.currentPeriode = adminStore.periode
        
        await adminStore.getSidangDEByPeriodeID(route.params.periodeID) //-> got refactored, tidak bisa ambil waktu ujian
        if(adminStore.sidangDEArr.length>0){
            data.jadwals = adminStore.sidangDEArr
        }

        data.isFetching = false
    }
)

</script>