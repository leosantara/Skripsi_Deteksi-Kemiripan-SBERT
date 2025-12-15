<template>
    <table class="table table-striped table-hover">
        <thead>
            <tr>
                <th></th>
                <th>PERIODE EVALUASI</th>
                <th>WAKTU SIDANG</th>
                <th>KELOMPOK</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="(data, index) in props.sidangDatas" v-bind:key="index">
                <td>
                    <div class="dropdown">
                        <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Action
                        </button>
                        <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                            <a v-if="route.fullPath.includes('adminpage')" @click.prevent="router.push(`/deskevaluation/adminpage/periode/${route.params.periodeID}/${data.sidangId}/setup-timSidang`)" class="dropdown-item" href="#">Pengaturan Tim Sidang</a>
                            <a v-if="route.fullPath.includes('adminpage')" @click.prevent="router.push(`/deskevaluation/adminpage/periode/${route.params.periodeID}/${data.sidangId}/setup-pesertaSidang`)" class="dropdown-item" href="#">Pengaturan Peserta Sidang</a>
                            <li v-if="route.fullPath.includes('adminpage')"><hr class="dropdown-divider"></li>
                            
                            <a v-if="route.fullPath.includes('dosenpage')" @click.prevent="router.push(`/deskevaluation/dosenpage/jadwal-sidang/${data.sidangId}/semua-proposal/`)" class="dropdown-item" href="#">Mulai Evaluasi</a>
                            <RouterLink :to="handleCetakJadwalSidang(data)" target="_blank" class="dropdown-item">Cetak Peserta Sidang</RouterLink>
                            <li v-if="route.fullPath.includes('adminpage')"><hr class="dropdown-divider"></li>

                            <a v-if="route.fullPath.includes('adminpage')" @click.prevent="router.push(`/deskevaluation/adminpage/periode/${route.params.periodeID}/${data.sidangId}/update`)" class="dropdown-item" href="#">Sunting Sidang</a>
                            <a v-if="route.fullPath.includes('adminpage')" @click.prevent="assignSidang(data)" class="dropdown-item" href="#" data-bs-toggle="modal" data-bs-target="#deleteConfirmation">Delete Sidang</a>
                        </div>
                    </div>
                </td>
                <td>
                    <span class="badge bg-secondary">{{ data.periodeKolokium.title?data.periodeKolokium.title:'Tidak ada Title' }}</span> <br>
                    <span>{{ dateFormater.formatDate(data.periodeKolokium.tglAwal) }}</span> s/d <span>{{ dateFormater.formatDate(data.periodeKolokium.tglAkhir) }} </span>
                </td>
                <td>
                    <TanggalUjian v-bind:sidang="data"/>
                </td>
                <td> {{ data.kelompok[0] }} </td>
            </tr>
        </tbody>
    </table>

    <!-- Modal -->
    <div class="modal fade" id="deleteConfirmation" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="exampleModalLongTitle">Perhatian</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" />
                </div>
                <div class="modal-body">
                    Anda akan menghapus data sidang berikut: <br><br>
                    <p v-if="data.currentSidang">
                        Kelompok: <span class="fw-bold">{{ data.currentSidang.kelompok[0] }}</span> <br>
                        <!-- Waktu Sidang: <span class="fw-bold">{{ dateFormater.formatDateTime(data.currentSidang.waktu) }}</span> <br> -->
                        yang berlangsung pada periode evaluasi {{ dateFormater.formatDate(props.periodeData.tglAwal) }} s/d {{ dateFormater.formatDate(props.periodeData.tglAkhir) }}
                    </p>
                    <b>Begitu data dihapus, data tidak dapat di restore, apa Anda yakin ingin melanjutkan?</b>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-bs-dismiss="modal" @click="deleteSidang()">Hapus</button>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Tutup</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { reactive, defineProps, defineEmits, onMounted } from 'vue';
import dateFormater from '../../helpers/date-formater';
import { router } from '../../router';
import { RouterLink, useRoute } from 'vue-router';
import { useAdminStore } from '../../stores/admin.store';
import { useAuthStore } from '../../stores/auth.store';
import TanggalUjian from '../lazyComponents/TanggalUjian.vue';

const authStore = useAuthStore()
const route = useRoute()
let data = reactive({
    currentSidang:null,
    tanggalUjianArr:[]
})
let props = defineProps({
    sidangDatas:{
        type:Array,
        required:true
    }, 
    periodeData:{
        type:Object,
        require:true
    }
})
const emit = defineEmits(['deleteSidang'])

function handleCetakJadwalSidang(sidang){
    if(route.fullPath.includes('adminpage')){
        //router.push(`/dokumen/adminpage/jadwalSidang/${sidang.sidangId}`)
        return `/dokumen/adminpage/jadwalSidang/${sidang.sidangId}`
    }else if(route.fullPath.includes('dosenpage')){
        //router.push(`/dokumen/dosenpage/jadwalSidang/${authStore.user.details.nim}/${sidang.periodeKolokium.id}`)
        return `/dokumen/dosenpage/jadwalSidang/${sidang.sidangId}`
    }
}

function assignSidang(sidang){
    data.currentSidang = sidang
}

function deleteSidang(){
    let sidangID = data.currentSidang?data.currentSidang.sidangId:""
    emit('deleteSidang', sidangID)
}
</script>