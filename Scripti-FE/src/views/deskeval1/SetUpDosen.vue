<template>
    <Loading v-bind:isFetching="data.isFetching"/>
    <div v-if="!data.isFetching">
        <div v-if="!data.currentJadwal">
            <div class="mt-2 p-4 border border-2 rounded text-center">
                <h4>Gagal memuat data Sidang.</h4>
            </div>
        </div>
        <div v-else>
            <div class="alert alert-secondary mt-3" role="alert">
                <h6>Anda akan mengatur Tim Sidang Evaluasi untuk jadwal sidang berikut:</h6>
                <div class="px-3">
                    <table>
                        <tbody>
                            <tr>
                                <td>Periode Title</td>
                                <td class="fw-bold">: {{ data.currentPeriode?data.currentPeriode.title:"Tidak dapat menampilkan data Title Periode" }}</td>
                            </tr>
                            <tr>
                                <td>Periode Evaluasi</td>
                                <td class="fw-bold">: {{ data.currentPeriode?`${dateFormater.formatDate(data.currentPeriode.tglAwal)} s/d ${dateFormater.formatDate(data.currentPeriode.tglAkhir)}`:"Tidak dapat menampilkan Periode Evaluasi" }}</td>
                            </tr>
                            <tr>
                                <td>Waktu Sidang</td>
                                <td class="fw-bold">: {{dateFormater.formatDateTime(data.currentJadwal.waktu)}}</td>
                            </tr>
                            <tr>
                                <td>Kelompok Tim Sidang</td>
                                <td class="fw-bold">: {{ data.currentJadwal.kelompok }}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="mt-2 p-4 border border-2 rounded">
                <h6>Silakan pilih dosen yang akan menjadi ketua dan anggota tim sidang:</h6>
                <input type="text" class="form-control mb-2" v-model="data.filter.searchFilter" placeholder="Cari Nama atau NIDN Dosen..."/>
                <div class="mb-2">
                    <span class="badge bg-dark">{{ `found: ${data.dosens.length} Dosen` }}</span>
                </div>
                <div style="height: 400px; overflow-y: scroll;">
                    <SetupTimSidangTable
                        v-bind:tableHeaders="['nidn', 'nama']"
                        v-bind:tableDatas="filteredPage"
                        v-model:choosenItem="data.choosenDosens"
                        v-model:ketua="data.namaKetua"
                        @assignKetua="handleDosenKetua"
                    />
                </div>
                
                <div v-if="anggota.length==0 && !data.ketua" class="text-center mt-3">
                    <h6>Anda belum memilih anggota tim sidang.</h6>
                </div>
                <div v-else class="mt-3">
                    Ketua: <br>
                    <span class="fw-bold" v-if="data.ketua">&emsp;{{ handleDosenKetua(data.ketua) }}</span>
                    <span class="text-danger fw-bold" v-else>&emsp;Anda belum memilih Ketua Tim Sidang</span> <br>
                    Anggota: <br>
                    <span v-for="anggotaID in anggota" class="fw-bold">&emsp;{{ getDosenByID(anggotaID).nama }}<br></span>
                </div>

                <div class="text-end mt-2">
                    <button class="btn btn-primary" :disabled="anggota.length==0 || !data.ketua || data.isSubmitting" @click="handleUpsert()">SIMPAN</button>
                </div>
            </div>
        </div>
    </div>
    <br>
    <br>
</template>

<script setup>
import { reactive, onBeforeMount, computed, onUpdated, watch } from 'vue';
import { useAdminStore } from '../../stores/admin.store';
import { useAlertStore } from '../../stores/alert.store';
import { useRoute } from 'vue-router';
import dateFormater from '../../helpers/date-formater';

import Loading from '../../components/animation/Loading.vue';
import SetupTimSidangTable from '../../components/searchableDropdown/SetupTimSidangTable.vue';
import tempMapper from '../../helpers/temp-mapper';

const alertStore = useAlertStore()
const adminStore = useAdminStore()
const route = useRoute()

let data = reactive({
    dosens:[],
    filter:{
        searchFilter:""
    },
    choosenDosens:[],
    ketua:"",
    namaKetua:"",
    anggota:[],
    currentJadwal:null,
    currentPeriode:null,
    isFetching:true,
    isSubmitting:false
})

const filteredPage = computed(
    () => {
        let filteredPage = data.dosens
        if(data.filter.searchFilter!="" && data.dosens.length>0){
            filteredPage = filteredPage.filter(item => item.nama.toLowerCase().includes(data.filter.searchFilter.toLowerCase()) || item.nidn.includes(data.filter.searchFilter))
        }
        return filteredPage
    }
)

const anggota = computed(
    () => {
        data.anggota = []
        let anggotaArr = []
        data.choosenDosens.forEach(dosenID => {
            if(dosenID!=data.ketua){
                anggotaArr.push(dosenID)
            }
        })
        anggotaArr.forEach(anggotaID => {
            data.anggota.push(anggotaID)
        })
        return anggotaArr
    }
)

function handleGelarDosen(dosen){
    let name = dosen.nama
    if (dosen.gelarDepan){
        name = dosen.gelarDepan + " " + name
    }
    if (dosen.gelar){
        name += ", " + dosen.gelar
    }
    return name
}

async function handleUpsert(){
    data.isSubmitting = true
    let success = await adminStore.upsertDosensBySidangID(data.currentJadwal.id, data.ketua, data.anggota)
    if(success){
        alertStore.success("Tim Sidang berhasil ditambahkan!")
    }
    data.isSubmitting = false
}

function getDosenByID(dosenID){
    let dosenMap = tempMapper.getDosenMapByID(data.dosens)
    return dosenMap.get(dosenID)
}

function handleDosenKetua(dosenID){
    let dosenMap = tempMapper.getDosenMapByID(data.dosens)
    data.ketua = dosenID
    data.namaKetua = dosenMap.get(dosenID).nama
    return data.namaKetua
}

onBeforeMount(
    async () => {
        //REFACTOR SO THAT PERIODE AND SIDANG CAN BE FETCHED AT THE SAME TIME
        await adminStore.getSidangDEByID(route.params.sidangID)
        data.currentJadwal = adminStore.sidangDE
        
        if(data.currentJadwal){
            await adminStore.getPeriodeById(data.currentJadwal.periodeId)
            data.currentPeriode = adminStore.periode
        }

        await adminStore.getAllDosen()
        data.dosens = adminStore.dosens
        data.dosens.forEach(dosen => {
            dosen.nama = handleGelarDosen(dosen)
        })

        //REFACTOR SO THAT SIDANG AND DOSENS CAN BE FETCHED AT THE SAME TIME
        //REFACTOR SO THAT DOSENS CAN BE FETHCED WITH GELAR_DEPAN AND GELAR
        await adminStore.getDosensBySidangID(route.params.sidangID)
        if(adminStore.dosenDE){
            adminStore.dosenDE.anggota.forEach(anggota => {
                data.choosenDosens.push(anggota.dosenId)
            });

            if(adminStore.dosenDE.ketua){
                data.ketua = adminStore.dosenDE.ketua.dosenId
                data.choosenDosens.push(data.ketua)
            }
        }
        data.isFetching = false
    }
)

// onUpdated(
//     () => {
//         console.log('ketua: ', data.ketua)
//         console.log('anggota: ', data.anggota)
//     }
// )
</script>

<style scoped>
/* applying no style: */
tr:hover{
    cursor: default;
    background: none !important;
}
tr:hover td{
    background: none !important;
}
</style>