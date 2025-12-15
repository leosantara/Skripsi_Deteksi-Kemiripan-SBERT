<template>
    <table class="table table-striped table-hover">
        <thead>
            <tr>
                <th></th>
                <th>NIDN</th>
                <th>NAMA</th>
                <th>GELAR</th>
                <th>BOLEH</th>
                <th>EMAIL</th>
                <th>NO TELP</th>
                <th>STATUS</th>
                <th>GELAR DEPAN</th>
                <th>PRODI</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="(data, index) in props.dosenDatas" v-bind:key="index">
                <td>
                    <div class="dropdown">
                        <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Action
                        </button>
                        <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                            <a @click.prevent="updateDosen(data.nidn)" class="dropdown-item" href="#">Update</a>
                            <a @click.prevent="assignDosen(data)" class="dropdown-item" href="#" data-bs-toggle="modal" data-bs-target="#deleteConfirmation">Delete</a>
                        </div>
                    </div>
                </td>
                <td style="width: fit-content;">{{ data.nidn }}</td>
                <td>{{ data.nama }}</td>
                <td>{{ data.gelar }}</td>
                <td>{{ data.boleh }}</td>
                <td>{{ data.email }}</td>
                <td>{{ data.telpNo }}</td>
                <td>{{ data.status }}</td>
                <td>{{ data.gelarDepan }}</td>
                <td>{{ data.prodi }}</td>
                <!-- <td><span :class="{'badge bg-success':data.status==1,'badge bg-danger':data.status==0}">{{ data.status==1?"Mahasiswa Aktif":"Mahasiswa Nonaktif"}}</span></td>
                <td><span :class="{'badge bg-success':data.ta==1,'badge bg-danger':data.ta==0}">{{ data.ta==1?"Sedang Mengikuti Tugas Akhir":"Tidak Sedang Mengikuti Tugas Akhir" }}</span></td> -->
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
                    Anda akan menghapus data dosen: <br> 
                    <b>{{ data.currentDosen?`${data.currentDosen.nidn} - ${data.currentDosen.nama}`:""}}</b> <br> <br>
                    Begitu data dihapus, data tidak dapat di restore, apa Anda yakin ingin melanjutkan?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-bs-dismiss="modal" @click="deleteDosen()">Hapus</button>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { defineProps, defineEmits, reactive } from 'vue';
import {router} from '../../router/'

let data = reactive({
    currentDosen:null
})

const props = defineProps({
    dosenDatas:{
        type: Array,
        required:true
    }
})

const emit = defineEmits(['deleteDosen'])

function deleteDosen(){
    let nidn = data.currentDosen?data.currentDosen.nidn:""
    emit('deleteDosen', nidn)
}

function assignDosen(dosen){
    data.currentDosen = dosen
}

function updateDosen(nidn){
    router.push(`/adminpage/dosen/${nidn}`)
}
</script>

<!-- <style scoped>
tr:hover{
    cursor: pointer;
    background: mediumseagreen !important;
}
tr:hover td{
    background: transparent;
}
</style> -->