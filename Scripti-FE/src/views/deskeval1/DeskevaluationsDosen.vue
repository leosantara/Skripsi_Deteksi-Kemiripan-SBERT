<template>
<Loading v-bind:isFetching="data.isFetching"/>
<div v-if="!data.isFetching" class="mt-2 p-4 border border-2 rounded">
    <h4>Proposal</h4>
    <h6>Berikut ini semua data proposal yang sedang atau telah mengikuti sidang Desk Evaluation:</h6>
    <div class="d-flex align-self-center justify-content-start gap-2">
        <SearchBar @search="handleSearch" :placeholderText="'Cari Judul atau NIM penulis...'"/>
        <DeskevalutionsFilter
            v-model:statusFilter="data.filter.statusFilter"
            v-model:periodeFilter="data.filter.periodeFilter"
        />
    </div>
    <span class="badge bg-dark">{{ `found: ${data.filteredDE.length} data`+ (data.filteredDE.length>1?'s':'') }}</span>
    <DeskevaluationsTable
        v-if="paginatedPage.length>0"
        v-bind:deskevaluationDatas="paginatedPage"
        @deleteDeskevaluation="()=>{}"
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

import { reactive, onBeforeMount, onUpdated, computed, watch } from 'vue';
import { useDosenStore } from '../../stores/dosen.store';
import { useRoute } from 'vue-router';

const route = useRoute()
const dosenStore = useDosenStore()
let data = reactive({
    deskevaluations:[],
    filteredDE:[],
    pagination:{
        currentPage:1,
        itemPerPage:10
    },
    filter:{
        searchFilter:"",
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
        filteredPage = filteredPage.filter(de => data.filter.statusFilter.includes(de.proposal?.status))

        data.filteredDE = filteredPage
        return filteredPage
    }
)

function handleSearch(keyword){
    data.filter.searchFilter = keyword.toLowerCase()
}

onBeforeMount(
    async () => {
        let deskevaluationsExists = await dosenStore.getDeskEvaluationsBySidangID(route.params.sidangID)
        if(deskevaluationsExists){
            data.deskevaluations = dosenStore.deskEvaluations
        }

        data.isFetching = false
    }
)

// onUpdated(
//     () => {
//         console.log('FILTER', data.filter)
//     }
// )
</script>