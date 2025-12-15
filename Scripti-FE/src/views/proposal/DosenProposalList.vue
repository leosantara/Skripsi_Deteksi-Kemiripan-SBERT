<template>
<Loading v-bind:isFetching="data.isFetching" />
<div v-if="!data.isFetching">
    <h1>Daftar Proposal</h1>
    <hr>
    <div class="mt-2 p-4 border border-2 rounded">
        <h6>Berikut ini daftar proposal yang Anda bimbing pada periode ini:</h6>
        <SearchBar @search="handleSearch" :placeholderText="'Cari Nama atau NIM penulis...'"/>
        <div class="mb-2">
            <span class="badge bg-dark">{{ `found: ${data.filteredProposal.length} data`+ (data.filteredProposal.length>1?'s':'') }}</span>
        </div>
        <ProposalTable 
            v-if="paginatedPage.length>0"
            v-bind:proposalDatas="paginatedPage"
            @dosenLihatDetail="handleDetail"
        />
        <PaginationBar 
            v-if="filteredPage.length>0"
            v-bind:totalItems="filteredPage.length"
            v-bind:itemPerPages="data.pagination.itemPerPage"
            v-model:selected="data.pagination.currentPage"
        />
    </div>
</div>

<!-- Modal -->
<div class="modal fade" id="detailProposal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5" id="exampleModalLabel">Detail</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <DetailProposal
                    v-if="data.currentProposal"
                    v-bind:proposalData="data.currentProposal"
                />
                <span v-else>Gagal menampilkan data proposal</span>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<br>
<br>
</template>

<script setup>
import { reactive, onBeforeMount, computed } from 'vue';
import { useProposalStore } from '../../stores/proposal.store';
import { useDosenStore } from '../../stores/dosen.store';
import { useAuthStore } from '../../stores/auth.store';

import Loading from '../../components/animation/Loading.vue';
import SearchBar from '../../components/pagination/SearchBar.vue';
import PaginationBar from '../../components/pagination/PaginationBar.vue';
import ProposalTable from '../../components/pagination/ProposalTable.vue';
import DetailProposal from '../../components/pagination/DetailProposal.vue'

const proposalStore = useProposalStore()
const dosenStore = useDosenStore()
const authStore = useAuthStore()
let myModal = null
let data = reactive({
    currentDosen:null,
    proposals:[],
    currentProposal:null,
    filteredProposal:[],
    pagination:{
        currentPage:1,
        itemPerPage:10
    },
    filter:{
        searchFilter:"",
    },
    activePeriode:null,
    isFetching:true
})

const filteredPage = computed(
    () => {
        data.pagination.currentPage = 1
        let filteredPage = data.proposals
        if(data.filter.searchFilter!=""){
            filteredPage = filteredPage.filter(
                proposal => {
                    return proposal.judul.toLowerCase().includes(data.filter.searchFilter) || proposal.mahasiswa?.nama.toLowerCase().includes(data.filter.searchFilter)
                }
            )    
        }
        
        data.filteredProposal = filteredPage
        return filteredPage
    }
)

const paginatedPage = computed(
    () => {
        return data.filteredProposal.slice((data.pagination.currentPage - 1) * data.pagination.itemPerPage, (data.pagination.currentPage) * data.pagination.itemPerPage)
    }
)

function handleDetail(proposal){
    data.currentProposal = proposal
    myModal.show()
    console.log(proposal)
}

function handleSearch(keyword){
    data.filter.searchFilter = keyword.toLowerCase()
}

onBeforeMount(
    async () => {
        await dosenStore.getAllDosen()
        if(dosenStore.dosens.length>0){
            for(let i=0;i<dosenStore.dosens.length;i++){
                let dosen = dosenStore.dosens[i]
                if(dosen.nidn == authStore.user.details.nim){
                    data.currentDosen = dosen
                    break
                }
            }
        }

        let periodeActiveExists = await dosenStore.getCurrentActivePeriode()
        let proposalsExists = await proposalStore.dosenGetAllProposals(data.currentDosen.id)
        //let proposal2Exists = await dosenStore.getDeskEvaluationByDosen2Nidn(authStore.user.details.nim)
        
        if(periodeActiveExists){
            data.activePeriode = dosenStore.periodesActive[0]
        }

        if(proposalsExists && periodeActiveExists){
            proposalStore.proposalsByDosen.forEach(proposal => {
                if(proposal.periodes?.id==data.activePeriode.id){
                    data.proposals.push(proposal)
                }
            });
        }

        myModal = new bootstrap.Modal(document.getElementById('detailProposal'))
        data.isFetching = false
    }
)
</script>