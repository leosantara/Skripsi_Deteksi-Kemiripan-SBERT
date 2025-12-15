<template>
    <h1>Daftar Mahasiswa</h1>
    <div class="d-flex align-self-center justify-content-start gap-2">
        <SearchBar @search="handleSearch" :placeholderText="'Masukkan nama atau NIM...'"/>
        <MahasiswaFilter v-model:statusFilter="data.filter.statusFilter" v-model:taFilter="data.filter.taFilter"/>
    </div>
    <span class="badge bg-dark">{{ `found: ${data.filteredMahasiswa.length} data`+ (data.filteredMahasiswa.length>1?'s':'') }}</span>
    <Loading v-bind:isFetching="adminStore.fetching"/>
    <MahasiswaTable
        v-if="paginatedPage.length>0"
        v-bind:mahasiswaDatas="paginatedPage"
        @deleteMahasiswa="handleDeleteMahasiswa">
    </MahasiswaTable>
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
import MahasiswaTable from '../../components/pagination/MahasiswaTable.vue';
import Loading from '../../components/animation/Loading.vue';
import MahasiswaFilter from '../../components/pagination/MahasiswaFilter.vue';
import { reactive, computed, onBeforeMount } from 'vue';
import { useAdminStore } from '../../stores/admin.store';

const adminStore = useAdminStore()
let data = reactive({
    mahasiswas:[],
    filteredMahasiswa:[],
    pagination:{
        currentPage:1,
        itemPerPage:25
    },
    filter:{
        searchFilter:"",
        statusFilter:null,
        taFilter:null
    },
    isFetching:false
})

const paginatedPage = computed(
    () => {
        //everytime the currentPage is changed, we will slice the whole filtered array then show them:
        return data.filteredMahasiswa.slice((data.pagination.currentPage - 1) * data.pagination.itemPerPage, data.pagination.currentPage * data.pagination.itemPerPage)
    }
)

const filteredPage = computed(
    () => {
        data.pagination.currentPage = 1
        let filteredPage = data.mahasiswas
        if(data.filter.searchFilter!=""){
            filteredPage = filteredPage.filter(item => item.nama.toLowerCase().includes(data.filter.searchFilter) || item.nim.includes(data.filter.searchFilter))
        }
        if(data.filter.statusFilter!=null){
            filteredPage = filteredPage.filter(item => item.status == data.filter.statusFilter)
        }
        if(data.filter.taFilter!=null){
            filteredPage = filteredPage.filter(item => item.ta == data.filter.taFilter)
        }
        data.filteredMahasiswa = filteredPage
        return filteredPage
    }
)

function handleSearch(keyword){
    data.filter.searchFilter = keyword.toLowerCase()
    // data.pagination.currentPage = 1
}

async function handleDeleteMahasiswa(nim){
    await adminStore.deleteMahasiswaByNim(nim)

    //the actual data also need to be removed from in memory array for
    //the changes to be rendered to the user!
    data.filteredMahasiswa = data.filteredMahasiswa.filter(mahasiswa => mahasiswa.nim != nim)
}

onBeforeMount(
    async () => {
        await adminStore.getAllMahasiswas()
        if(adminStore.mahasiswas.length>0){
            data.mahasiswas = adminStore.mahasiswas
        }
    }
)
</script>