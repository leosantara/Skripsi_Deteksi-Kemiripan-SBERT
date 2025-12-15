<template>
    <table class="table table-striped table-hover">
        <thead>
            <tr>
                <th></th>
                <th>Title</th>
                <th>Deskripsi</th>
                <th>Periode Evaluasi</th>
                <th>Batas Pengumpulan Proposal</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="(data, index) in props.periodeDatas" v-bind:key="index">
                <td>
                    <div class="dropdown">
                        <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Action
                        </button>
                        <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                            <a @click.prevent="router.push(`/deskevaluation/adminpage/periode/${data.id}/semua-sidang`)" class="dropdown-item" href="#">Atur Jadwal Sidang DE</a>
                            <li><hr class="dropdown-divider"></li>
                            <a @click.prevent="()=>{router.push(`/adminpage/periode-proposal/update/${data.id}`)}" class="dropdown-item" href="#">Sunting Periode Sidang</a>
                            <a 
                                :aria-disabled="data.currentPeriode" 
                                :class="{'disabled':isDeleteDisabled(data)}" 
                                @click.prevent="assignPeriode(data)" 
                                class="dropdown-item" href="#" 
                                data-bs-toggle="modal" data-bs-target="#deleteConfirmation">
                                Delete Periode Sidang
                            </a>
                        </div>
                    </div>
                </td>
                <td>{{ data.title }}</td>
                <td>{{ data.description }}</td>
                <td>{{ `${dateFormater.formatDate(data.tglAwal)} s/d ${dateFormater.formatDate(data.tglAkhir)}` }}</td>
                <td>{{ `${dateFormater.formatDateTime(data.tanggal)}` }}</td>
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
                <div v-if="data.currentPeriode" class="modal-body">
                    Anda akan menghapus Periode Desk Evaluation berikut: <br> 
                    Title: <br> <span class="badge bg-dark">{{ data.currentPeriode.title?data.currentPeriode.title:"Belum ada judul." }}</span> <br>
                    Periode Evaluasi: <br> <span class="badge bg-dark">{{ `${dateFormater.formatDate(data.currentPeriode.tglAwal)} s/d ${dateFormater.formatDate(data.currentPeriode.tglAkhir)}` }}</span> <br>
                    Batas Pengumpulan Proposal: <br> <span class="badge bg-dark">{{ `${dateFormater.formatDateTime(data.currentPeriode.tanggal)}` }}</span> <br>
                    Begitu data dihapus, data tidak dapat di restore, apa Anda yakin ingin melanjutkan?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-bs-dismiss="modal" @click="$emit('deletePeriode', data.currentPeriode.id)">Hapus</button>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { defineProps, defineEmits, reactive } from 'vue';
import { useAdminStore } from '../../stores/admin.store';
import { router } from '../../router';
import dateFormater from '../../helpers/date-formater';

const adminStore = useAdminStore()
let currentDate = new Date()
let data = reactive({
    currentPeriode:null
})

const props = defineProps({
    periodeDatas:{
        type:Array,
        required:true
    }
})

const emits = defineEmits(['deletePeriode'])

function assignPeriode(periode){
    data.currentPeriode = periode
}

function isDeleteDisabled(periode){
    if(currentDate > new Date(periode.tglAkhir)){
        return true
    }else if(adminStore.periodesActive.length>0 && adminStore.periodesActive[0].id == periode.id){
        return true
    }
    return false
}

</script>