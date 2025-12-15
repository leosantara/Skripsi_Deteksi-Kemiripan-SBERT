<template>
    <h1>Daftar Dosen</h1>
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
        <SearchBar @search="handleSearch" :placeholderText="'Masukkan nama atau NIDP...'"/>
    </nav>
    <span class="badge bg-dark">{{ `found: ${data.filteredDosen.length} data`+ (data.filteredDosen.length>1?'s':'') }}</span>
    <Loading v-bind:isFetching="adminStore.fetching"/>
    <DosenTable
        v-if="paginatedPage.length>0"
        v-bind:dosenDatas="paginatedPage"
        @deleteDosen="handleDeleteDosen">
    </DosenTable>
    <PaginationBar
        v-if="filteredPage.length>0"
        v-bind:totalItems="filteredPage.length"
        v-bind:itemPerPages="data.pagination.itemPerPage"
        v-model:selected="data.pagination.currentPage">
    </PaginationBar>
</template>

<script setup>
import PaginationBar from '../../components/pagination/PaginationBar.vue';
import SearchBar from '../../components/pagination/SearchBar.vue';
import Loading from '../../components/animation/Loading.vue';
import { reactive, computed, onBeforeMount } from 'vue';
import { useAdminStore } from '../../stores/admin.store';
import DosenTable from '../../components/pagination/DosenTable.vue';

const adminStore = useAdminStore()
let data = reactive({
    dosens:[],
    filteredDosen:[],
    pagination:{
        currentPage:1,
        itemPerPage:25
    },
    filter:{
        searchFilter:""
    },
    isFetching:false
})

const paginatedPage = computed(
    () => {
        //everytime the currentPage is changed, we will slice the whole filtered array then show them:
        return data.filteredDosen.slice((data.pagination.currentPage - 1) * data.pagination.itemPerPage, data.pagination.currentPage * data.pagination.itemPerPage)
    }
)

const filteredPage = computed(
    () => {
        let filteredPage = data.dosens
        if(data.filter.searchFilter!=""){
            filteredPage = filteredPage.filter(item => item.nama.toLowerCase().includes(data.filter.searchFilter) || item.nidn.includes(data.filter.searchFilter))
        }
        data.filteredDosen = filteredPage
        return filteredPage
    }
)

function handleSearch(keyword){
    data.filter.searchFilter = keyword.toLowerCase()
    data.pagination.currentPage = 1
}

async function handleDeleteDosen(nidn){
    await adminStore.deleteDosenByNidn(nidn)
    data.filteredDosen = data.filteredDosen.filter(dosen => dosen.nidn != nidn)
}

onBeforeMount(
    async () => {
        await adminStore.getAllDosen()
        if(adminStore.dosens.length>0){
            data.dosens = adminStore.dosens
        }
    }
)
</script>