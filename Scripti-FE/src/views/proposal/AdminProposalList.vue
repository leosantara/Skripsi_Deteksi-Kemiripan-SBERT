<template>
    <Loading v-bind:isFetching="data.isFetching" />
    <div v-if="!data.isFetching">
        <h1>Daftar Proposal</h1>
        <hr style="height:1.5px;border:none;color:#333;background-color:#333;">
        <div class="d-flex justify-content-start gap-2">
            <button class="btn rounded-pill" :class="showNewPageHeader" @click="data.submenu.showAllProposal=false" type="button">Daftar Proposal Baru</button>
            <button class="btn rounded-pill" :class="showAllPageHeader" @click="data.submenu.showAllProposal=true" type="button">Semua Proposal</button>
            <button class="btn btn-outline-success rounded-pill" @click="router.push('/proposalpage/adminpage/tambah')">&#128934; Unggah Proposal</button>
            <button class="btn btn-warnin btn-outline-success rounded-pill" @click="updateAI"  :disabled="proposalStore.indexingStatus.isRunning"><i class="bi bi-arrow-repeat"></i>{{ proposalStore.indexingStatus.isRunning ? 'Sedang Memproses...' : 'Perbarui Data AI' }}</button>
        </div>
<div v-if="proposalStore.indexingStatus.isRunning" class="alert alert-info mt-3 shadow-sm border border-info bg-light">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="fw-bold text-primary">
                    <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    Memproses Data AI...
                </span>
                <span class="badge bg-primary rounded-pill">{{ Math.round(proposalStore.indexingStatus.progressPercent) }}%</span>
            </div>
            
            <div class="progress" style="height: 20px; border-radius: 10px;">
                <div 
                    class="progress-bar progress-bar-striped progress-bar-animated bg-primary" 
                    role="progressbar" 
                    :style="{ width: proposalStore.indexingStatus.progressPercent + '%' }"
                    :aria-valuenow="proposalStore.indexingStatus.progressPercent" 
                    aria-valuemin="0" 
                    aria-valuemax="100"
                ></div>
            </div>
            
            <div class="mt-2 d-flex justify-content-between small text-muted">
                <span>Status: <i class="fst-italic">{{ proposalStore.indexingStatus.message }}</i></span>
                <span>Mohon jangan tutup browser</span>
            </div>
        </div>
        <div class="mt-2 p-4 border border-2 rounded">
            <h4>{{ `${data.submenu.showAllProposal?'Semua Proposal':'Daftar Proposal Baru'}` }}</h4>
            <h6>{{ `${data.submenu.showAllProposal?data.submenu.descMsg[1]:data.submenu.descMsg[0]}` }}</h6>
            <div class="mb-2">
                <span class="badge bg-dark">{{ `found: ${filteredPage.length} data` + (filteredPage.length>1 ? 's' : '') }}</span>
            </div>
            <div class="d-flex bd-highlight">
                <div class="p-2 flex-grow-1 bd-highlight">
                    <SearchBar @search="handleSearch" :placeholderText="'Cari Judul atau NIM penulis...'"/>
                    <YearFilter v-model:yearFilter="data.filter.yearFilter" />
                </div>
                <div class="p-2 bd-highlight">
                    <ProposalFilter 
                        v-if="data.submenu.showAllProposal" 
                        v-model:statusFilter="data.filter.statusFilter"
                        v-model:submitFilter="data.filter.submitFilter"
                    />
                </div>
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
import YearFilter from '../../components/pagination/YearFilter.vue';
import { router } from '../../router';

import {onUnmounted } from 'vue';
import { reactive, watch, computed, onBeforeMount, ref } from 'vue';
import { useProposalStore } from '../../stores/proposal.store';

const proposalStore = useProposalStore()
const isLoading = ref(false);

let data = reactive({
    proposals: [],
    pagination: {
        currentPage: 1,
        itemPerPage: 25
    },
    filter: {
        searchFilter: "",
        statusFilter: ['B'], // Default untuk "Proposal Baru"
        submitFilter: "all",
        yearFilter: ""
    },
    isFetching: false,
    submenu: {
        showAllProposal: false, // false = Proposal Baru, true = Semua Proposal
        descMsg: ['Berikut ini adalah data proposal baru yang harus divalidasi:', 'Berikut ini adalah data proposal mahasiswa yang telah disubmit dari periode awal sampai periode terbaru:']
    }
})

// --- LOGIKA UPDATE FILTER SAAT SUBMENU BERUBAH ---
// Gunakan watch, BUKAN computed untuk mengubah state
watch(
    () => data.submenu.showAllProposal,
    (newVal) => {
        if (newVal === true) {
            // Mode: Semua Proposal
            data.filter.statusFilter = ['B', 'T', 'K', 'R', 'L', 'V']
        } else {
            // Mode: Proposal Baru
            data.filter.statusFilter = ['B']
            data.filter.submitFilter = 'all'
        }
        // Reset pagination ke halaman 1 setiap ganti tab
        data.pagination.currentPage = 1
    },
    { immediate: true } // Jalankan sekali saat awal mount
)

// --- COMPUTED: CLASS BUTTON ---
const showAllPageHeader = computed(() => {
    return data.submenu.showAllProposal ? 'btn-primary' : 'btn-outline-primary'
})

const showNewPageHeader = computed(() => {
    return !data.submenu.showAllProposal ? 'btn-primary' : 'btn-outline-primary'
})

// --- COMPUTED: FILTERING LOGIC ---
const filteredPage = computed(() => {
    // Pastikan proposals adalah array
    if (!data.proposals || !Array.isArray(data.proposals)) return []
    
    let list = data.proposals.slice()

    // 1. Filter Status
    if (data.filter.statusFilter && data.filter.statusFilter.length > 0) {
        list = list.filter(proposal => data.filter.statusFilter.includes(proposal.status))
    } else {
        // Jika status filter kosong, kembalikan kosong (safety)
        return []
    }

    // 2. Filter Search
    if (data.filter.searchFilter) {
        const search = data.filter.searchFilter
        list = list.filter(proposal => {
            const judulMatch = proposal.judul && proposal.judul.toLowerCase().includes(search)
            const nimMatch = proposal.mahasiswa && proposal.mahasiswa.nim && proposal.mahasiswa.nim.includes(search)
            return judulMatch || nimMatch
        })
    }

    // 3. Filter Submit Date
    if (data.filter.submitFilter === "recent") {
        const now = new Date()
        list = list.filter(proposal => {
            try {
                const d = new Date(proposal.modified)
                return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
            } catch (e) {
                return false
            }
        })
    }

    return list
})

const paginatedPage = computed(() => {
    const list = filteredPage.value
    const start = (data.pagination.currentPage - 1) * data.pagination.itemPerPage
    const end = start + data.pagination.itemPerPage
    return list.slice(start, end)
})

// --- FUNCTIONS ---

function handleSearch(keyword) {
    data.filter.searchFilter = keyword.toLowerCase()
    data.pagination.currentPage = 1 // Reset page saat search
}

async function updateAI() {
    if (!confirm("Proses ini akan memakan waktu beberapa menit untuk memproses ulang semua data. Lanjutkan?")) return;
    await proposalStore.adminTriggerReindex();
    isLoading.value = true;
    try {
        await proposalStore.adminTriggerReindex();
        alert("Permintaan pembaruan dikirim. Sistem akan mengupdate data di background.");
    } catch (e) {
        alert("Gagal mengirim permintaan.");
        console.error(e)
    } finally {
        isLoading.value = false;
    }
}

async function handleDeleteProposal(proposalID) {
    if(!confirm("Yakin ingin menghapus proposal ini?")) return;
    await proposalStore.adminDeleteProposal(proposalID)
    data.proposals = data.proposals.filter(proposal => proposal.id != proposalID)
}

// --- WATCHER TAHUN ---
watch(
    () => data.filter.yearFilter,
    async (newYearValue, oldYearValue) => {
        if (newYearValue && newYearValue != oldYearValue) {
            data.isFetching = true
            const success = await proposalStore.adminGetProposalsByYear(newYearValue)
            if (success) {
                data.proposals = Array.isArray(proposalStore.proposals) ? proposalStore.proposals : []
            } else {
                data.proposals = []
            }
            data.pagination.currentPage = 1
            data.isFetching = false
        }
    }
)

// TAMBAHAN: Lifecycle Hooks
onBeforeMount(async () => {
    // ... (kode load data filter tahun kamu yang lama) ...
    data.filter.yearFilter = new Date().getFullYear()
    
    // --- LOGIKA BARU: CEK STATUS AI ---
    // "Hei backend, apakah kamu sedang sibuk re-index?"
    const status = await proposalStore.adminGetIndexingStatus()
    
    // Jika backend bilang "Ya, isRunning=true", nyalakan polling frontend
    if (status && status.isRunning) {
        proposalStore.startIndexingPolling()
    }
})

onUnmounted(() => {
    proposalStore.stopIndexingPolling()
})

</script>