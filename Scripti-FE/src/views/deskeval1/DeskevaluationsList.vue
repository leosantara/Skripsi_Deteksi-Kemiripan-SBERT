<template>
<Loading v-bind:isFetching="data.isFetching"/>
<div v-if="!data.isFetching" class="mt-2 p-4 border border-2 rounded">
    <h4>Semua Periode</h4>
    <h6>Berikut ini semua data proposal yang sedang atau telah mengikuti sidang Desk Evaluation:</h6>
    <div class="d-flex bd-highlight">
        <div class="p-2 flex-grow-1 bd-highlight">
            <SearchBar @search="handleSearch" :placeholderText="'Cari Judul atau NIM penulis...'"/>
            <YearFilter v-model:yearFilter="data.filter.yearFilter"/>
        </div>
        <div class="p-2 bd-highlight">
            <DeskevalutionsFilter
                v-model:statusFilter="data.filter.statusFilter"
                v-model:periodeFilter="data.filter.periodeFilter"
            />
        </div>
    </div>
    <span class="badge bg-dark">{{ `found: ${data.filteredDE.length} data`+ (data.filteredDE.length>1?'s':'') }}</span>
    <DeskevaluationsTable
        v-if="paginatedPage.length>0"
        v-bind:deskevaluationDatas="paginatedPage"
        @deleteDeskevaluation="handleDeleteDE"
    />
    <PaginationBar 
        v-if="filteredPage.length>0"
        v-bind:totalItems="filteredPage.length"
        v-bind:itemPerPages="data.pagination.itemPerPage"
        v-model:selected="data.pagination.currentPage"
    />
</div>
<br>
<br>
</template>

<script setup>
import Loading from '../../components/animation/Loading.vue';
import PaginationBar from '../../components/pagination/PaginationBar.vue';
import SearchBar from '../../components/pagination/SearchBar.vue';
import DeskevaluationsTable from'../../components/pagination/DeskevaluationsTable.vue'
import DeskevalutionsFilter from '../../components/pagination/DesakevaluationsFilter.vue'
import YearFilter from '../../components/pagination/YearFilter.vue'

import { reactive, onBeforeMount, onUpdated, computed, watch } from 'vue';
import { useAdminStore } from '../../stores/admin.store';

const adminStore = useAdminStore()
let data = reactive({
    deskevaluations:[],
    filteredDE:[],
    pagination:{
        currentPage:1,
        itemPerPage:25
    },
    filter:{
        searchFilter:"",
        yearFilter:"",
        periodeFilter:"all",
        statusFilter:['V', 'K', 'T']
    },
    currentPeriode:null,
    isFetching:true
})

const paginatedPage = computed(
    () => {
        return data.filteredDE.slice((data.pagination.currentPage - 1) * data.pagination.itemPerPage, (data.pagination.currentPage) * data.pagination.itemPerPage)
    }
)

const filteredPage = computed(
    () => {
        data.pagination.currentPage = 1

        let filteredPage = data.deskevaluations
        if(data.filter.searchFilter!=""){
            filteredPage = filteredPage.filter(
                de => {
                    return de.proposal?.judul.toLowerCase().includes(data.filter.searchFilter) || de.proposal?.mahasiswa?.nim.includes(data.filter.searchFilter)
                }
            )    
        }
        if(data.filter.periodeFilter == "recent"){
            filteredPage = filteredPage.filter(
                de => de.proposal?.periodes?.id == data.currentPeriode.id
            )
        }
        filteredPage = filteredPage.filter(de => data.filter.statusFilter.includes(de.proposal?.status))

        data.filteredDE = filteredPage
        return filteredPage
    }
)

async function handleDeleteDE(deskEvaluationID){
    await adminStore.deleteDeskEvalutionByID(deskEvaluationID)
    data.deskevaluations = data.deskevaluations.filter(deskevaluation => deskevaluation.id != deskEvaluationID) 
}

function handleSearch(keyword){
    data.filter.searchFilter = keyword.toLowerCase()
}

watch(
    () => data.filter.yearFilter,
    async (newYearValue, oldYearValue) => {
        if(newYearValue != oldYearValue && newYearValue!=""){
            data.isFetching = true
            let success = await adminStore.getDeskEvaluationByYear(newYearValue)
            if(success){
                data.deskevaluations = adminStore.deskEvaluations
            }else{
                data.deskevaluations = []
            }
            data.pagination.currentPage = 1
            data.isFetching = false
        }
    }
)

async function fetchCurrentPeriode(){
    data.isFetching = true
    let success = await adminStore.getCurrentActivePeriode()
    if(success){
        data.currentPeriode = adminStore.periodesActive[0]
    }else{
        data.filter.periodeFilter='all'
        data.currentPeriode = null
    }
    data.isFetching = false
}

watch(
    () => data.filter.periodeFilter,
    async (newVal, oldVal) => {
        if(newVal=='recent' && !data.currentPeriode){
            fetchCurrentPeriode()
        }
    }
)

onBeforeMount(
    async () => {
        await fetchCurrentPeriode()
        data.filter.yearFilter = new Date().getFullYear()
    }
)

// onUpdated(
//     () => {
//         console.log('FILTER', data.filter)
//     }
// )
</script>