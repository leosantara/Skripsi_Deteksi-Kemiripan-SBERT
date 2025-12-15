<template>
    <Loading v-bind:isFetching="data.isFetching" />
    <div v-if="!data.isFetching">
        <h1>Daftar Proposal</h1>
        <hr style="height:1.5px;border:none;color:#333;background-color:#333;">
        <div class="d-flex justify-content-start gap-2">
            <button class="btn btn-primary rounded-pill" type="button">Semua Proposal</button>
            <button class="btn btn-outline-success rounded-pill" @click="router.push('/proposalpage/mahasiswapage/tambah')">&#128934; Unggah Proposal</button>
        </div>
        <div class="mt-2 p-4 border border-2 rounded">
            <h4>Semua Proposal</h4>
            <h6>Berikut ini data semua proposal yang pernah Anda unggah:</h6>
            <div class="mb-2">
                <span class="badge bg-dark">{{ `found: ${data.filteredProposal.length} data`+ (data.filteredProposal.length>1?'s':'') }}</span>
            </div>
            <div class="d-flex align-self-center justify-content-start gap-2">
                <SearchBar @search="handleSearch" :placeholderText="'Cari Judul Proposal...'"/>
                <ProposalFilter 
                    v-model:statusFilter="data.filter.statusFilter"
                    v-model:submitFilter="data.filter.submitFilter"
                />
            </div>
            <ProposalTable 
                v-if="paginatedPage.length>0"
                v-bind:proposalDatas="paginatedPage"
                @deleteProposal="handleDeleteProposal"
            />
            <PaginationBar 
                v-if="filteredPage.length>0"
                v-bind:totalItems="filteredPage.length"
                v-bind:itemPerPages="data.pagination.itemPerPage"
                v-model:selected="data.pagination.currentPage"
            />
        </div>
    </div>
    <br>
</template>

<script setup>
import Loading from '../../components/animation/Loading.vue';
import ProposalFilter from '../../components/pagination/ProposalFilter.vue';
import SearchBar from '../../components/pagination/SearchBar.vue';
import PaginationBar from '../../components/pagination/PaginationBar.vue';
import ProposalTable from '../../components/pagination/ProposalTable.vue';
import { router } from '../../router';

import { reactive, onBeforeMount, computed, onUpdated} from 'vue';
import { useProposalStore } from '../../stores/proposal.store';
import { useAuthStore } from '../../stores/auth.store';

const proposalStore = useProposalStore()
const authStore = useAuthStore()

let data = reactive({
    proposals:[],
    filteredProposal:[],
    pagination:{
        currentPage:1,
        itemPerPage:50
    },
    filter:{
        searchFilter:"",
        statusFilter:[],
        submitFilter:""
    },
    isFetching:true
})

const filteredPage = computed(
    () => {
        data.pagination.currentPage = 1
        let filteredPage = data.proposals
        if(data.filter.statusFilter.length==0){
            data.filteredProposal = []
            return []
        }

        if(data.filter.searchFilter!=""){
            filteredPage = filteredPage.filter(
                proposal => {
                    return proposal.judul.toLowerCase().includes(data.filter.searchFilter)
                }
            )    
        }
        if(data.filter.submitFilter == "recent"){
            filteredPage = filteredPage.filter(
                proposal => 
                new Date(proposal.modified).getMonth() == new Date().getMonth() && 
                new Date(proposal.modified).getFullYear() == new Date().getFullYear()
            )
        }
        if(data.filter.submitFilter == "this_year"){
            filteredPage = filteredPage.filter(proposal => new Date(proposal.modified).getFullYear() == new Date().getFullYear())
        }
        filteredPage = filteredPage.filter(proposal => data.filter.statusFilter.includes(proposal.status))
        data.filteredProposal = filteredPage
        return filteredPage
    }
)

const paginatedPage = computed(
    () => {
        return data.filteredProposal.slice((data.pagination.currentPage - 1) * data.pagination.itemPerPage, (data.pagination.currentPage) * data.pagination.itemPerPage)
    }
)

function handleSearch(keyword){
    data.filter.searchFilter = keyword.toLowerCase()
}

async function handleDeleteProposal(proposalID){
    await proposalStore.adminDeleteProposal(proposalID)
    data.proposals = data.proposals.filter(proposal => proposal.id != proposalID)
}

onBeforeMount(
    async () => {
        //await proposalStore.mahasiswaGetAllProposal('71200610')
        data.filter.statusFilter=['B','T','K','R','L','V']
        data.filter.submitFilter = "all"
        await proposalStore.mahasiswaGetAllProposal(authStore.user.details.nim)       
        if(proposalStore.proposalsByMahasiswa.length>0){
            data.proposals = proposalStore.proposalsByMahasiswa
        }
        data.isFetching=false
    }
)

//FOR TESTING PURPOSES ONLY!
// onUpdated(
//     () => {
//         console.log('filter : ', data.filter)
//     }
// )
</script>