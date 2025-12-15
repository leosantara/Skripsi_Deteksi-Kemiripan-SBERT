<template>
    <h1>Jadwal Sidang Evaluasi Proposal</h1>
    <div class="card bg-light" style="max-width: 40%;">
        <h4 class="card-header">
            Periode Aktif
        </h4>
        <Loading v-bind:isFetching="data.isFetchingActivePeriode"/>
        <div v-if="!data.isFetchingActivePeriode" class="card-body text-center">
            <div v-if="!data.currentActivePeriode" class="p-5">
                <h6>Periode evaluasi proposal sudah berakhir. Anda dapat menambah periode proposal baru:</h6>
                <button class="btn btn-success" @click="() => {router.push('/adminpage/periode-proposal/tambah')}">Tambah Periode</button>
            </div>
            <div v-else-if="data.currentActivePeriode=='ERROR'">
                <span class="text-danger fw-bold">Terjadi kesalahan, tidak dapat menampilkan data periode aktif terkini.</span>
            </div>
            <div v-else-if="data.currentActivePeriode" class="text-start">
                <h4>{{ data.currentActivePeriode.title }}</h4>
                <p>{{ data.currentActivePeriode.description }}</p>
                <div class="btn-group">
                    <button type="button" class="btn btn-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
                        Set Up Desk Evaluation
                    </button>
                    <ul class="dropdown-menu">
                        <a @click.prevent="router.push(`/deskevaluation/adminpage/periode/${data.currentActivePeriode.id}/semua-sidang`)" class="dropdown-item" href="#">Atur Jadwal Sidang DE</a>
                        <a @click.prevent="()=>{router.push(`/adminpage/periode-proposal/update/${data.currentActivePeriode.id}`)}" class="dropdown-item" href="#">Sunting Periode Sidang</a>
                    </ul>
                </div>
                <hr style="height:1.5px;border:none;color:#333;background-color:#333;">
                <p>
                    <span class="fw-bold">Batas Waktu Pengumpulan Proposal:</span> <br>
                    <span class="badge bg-dark">{{ dateFormater.formatDateTime(data.currentActivePeriode.tanggal) }}</span>
                </p>
                <p>
                    <span class="fw-bold">Tanggal Evaluasi:</span> <br>
                    <span class="badge bg-dark">{{ `${dateFormater.formatDate(data.currentActivePeriode.tglAwal)} sampai ${dateFormater.formatDate(data.currentActivePeriode.tglAkhir)}` }}</span>
                </p>
                <button class="btn btn-success" @click="() => {router.push('/adminpage/periode-proposal/tambah')}">Tambah Periode</button>
            </div>
        </div>
    </div>
    <div class="mt-4">
        <h4>Periode Evaluasi Terdahulu</h4>
        <span class="badge bg-dark">{{ `found: ${data.periodes.length} data`+ (data.periodes.length>1?'s':'') }}</span>
        <Loading v-bind:isFetching="data.isFetchingAllPeriode"/>
        <PeriodeDETable  
            v-bind:periodeDatas="paginatedPage"
            @deletePeriode="handleDeletePeriode"
        />
        <PaginationBar
            v-if="data.periodes.length>0"
            v-bind:totalItems="data.periodes.length"
            v-bind:itemPerPages="data.pagination.itemPerPage"
            v-model:selected="data.pagination.currentPage">
        </PaginationBar>
    </div>
</template>

<script setup>
import { reactive, onBeforeMount, computed } from 'vue';
import { useAdminStore } from '../../stores/admin.store';
import PeriodeDETable from '../../components/pagination/PeriodeDETable.vue';
import Loading from '../../components/animation/Loading.vue';
import PaginationBar from '../../components/pagination/PaginationBar.vue';
import { router } from '../../router';
import dateFormater from '../../helpers/date-formater';

let data = reactive({
    currentActivePeriode:null,
    periodes:[],
    pagination:{
        currentPage:1,
        itemPerPage:25
    },
    isFetchingActivePeriode:true,
    isFetchingAllPeriode:true
})
const adminStore = useAdminStore()

onBeforeMount(
    async () =>  {
        await adminStore.getAllPeriode()
        data.isFetchingAllPeriode = false

        let activePeriodeExists = await adminStore.getCurrentActivePeriode()
        if(activePeriodeExists && adminStore.periodesActive != 'ERROR'){
            data.currentActivePeriode = adminStore.periodesActive[0]
        }
        if(!activePeriodeExists && adminStore.periodesActive == 'ERROR'){
            data.currentActivePeriode = 'ERROR'
        }
        if(adminStore.periodes.length>0){
            data.periodes = adminStore.periodes
        }
        data.isFetchingActivePeriode = false
    }
)

const paginatedPage = computed(
    () => {
        return data.periodes.slice((data.pagination.currentPage - 1) * data.pagination.itemPerPage, data.pagination.currentPage * data.pagination.itemPerPage)
    }
)

async function handleDeletePeriode(periodeID){
    await adminStore.deletePeriodeById(periodeID)
    data.periodes = data.periodes.filter(periode => periode.id != periodeID)
}
</script>