<template>
<Loading v-bind:isFetching="data.isFetching"/>
<div v-if="!data.isFetching">
    <div v-if="!data.currentDE" class="text-center border rounded p-4 shadow">
        <h1>Tidak dapat menemukan data hasil sidang.</h1>
    </div>
    <div v-else class="mt-2 p-4 border border-2 rounded-4">
        <div class="p-4 border border-2 rounded bg-secondary text-white">
            <h4>Hasil Evaluasi:</h4>
            <table>
                <tbody>
                    <tr>
                        <td>Judul Proposal:&emsp;</td>
                        <td class="fw-bold">{{ data.currentProposal.judul }}</td>
                    </tr>
                    <tr>
                        <td>Penulis:&emsp;</td>
                        <td>{{ `${data.penulis.nim} - ${data.penulis.nama}` }}</td>
                    </tr>
                    <tr>
                        <td>Dosen Pembimbing:&emsp;</td>
                        <td>{{ `${handleGelarDosen(data.currentDE.dosen1)}` }}</td>
                    </tr>
                    <tr>
                        <td>Hasil Sidang:&emsp;</td>
                        <td>{{ handleStatus(data.currentDE.status) }}</td>
                    </tr>
                    <tr>
                        <td>Evaluator:&emsp;</td>
                        <td>{{ handleGelarDosen(data.currentDE.dosenEvaluator) }}</td>
                    </tr>
                </tbody>
            </table>
        </div>
        <hr>
        <h4>Rekap:</h4>
        <form>
            <label class="fw-bold">Saran Dosen Pembimbing 1</label>
            <select class="form-select" v-model="data.post.dosen1Id" name="dosen2">
                <option value="" selected hidden>pilih dosen</option>
                <option v-for="dosen in data.dosens" :value="dosen.id">{{ handleGelarDosen(dosen) }}</option>
            </select>

            <label class="fw-bold">Saran Dosen Pembimbing 2</label>
            <select class="form-select" v-model="data.post.dosen2Id" name="dosen2">
                <option value="" selected hidden>pilih dosen</option>
                <option v-for="dosen in data.dosens" :value="dosen.id">{{ handleGelarDosen(dosen) }}</option>
            </select>

            <label class="fw-bold">Perubahan Judul</label>
            <div class="px-2">
                <label>Judul Lama</label>
                <InputTextArea name="judulLama" v-model="data.currentProposal.judul" :placeholderText="'Judul Lama'" :disabled="true"/>
                <label>Judul Baru</label>
                <InputTextArea name="judulBaru" v-model="data.post.judulBaru" :placeholderText="'Judul Baru'"/>
                <div class="text-end mt-3 px-2">
                    <button :disable="data.currentDE.status!='T' || !errorExists" class="btn btn-success" @click.prevent="handleUpdate()" :disabled="data.isUpdating || errorExists">{{ data.isUpdating?'saving...':'SIMPAN' }}</button>
                </div>
            </div>
        </form>
    </div>
</div>
<br>
<br>
</template>

<script setup>
import Loading from '../../components/animation/Loading.vue';
import InputTextArea from '../../components/fields/InputTextArea.vue';

import { reactive, onBeforeMount, computed} from 'vue';
import { useAdminStore } from '../../stores/admin.store';
import { useAuthStore } from '../../stores/auth.store';
import { useAlertStore } from '../../stores/alert.store';
import { useRoute } from 'vue-router';
import { router } from '../../router';
import dateFormater from '../../helpers/date-formater';

const route = useRoute()
const authStore = useAuthStore()
const adminStore = useAdminStore()
const alertStore = useAlertStore()
let data = reactive({
    currentDE:null,
    isFetching:true,
    post:{
        dosen1Id:"",
        dosen2Id:"",
        judulBaru:""
    },
    currentDE:null,
    currentProposal:null,
    currentActivePeriode:null,
    currentJadwal:null,
    currentTimSidang:null,
    dosens:[],
    penulis:null
})

const errorExists = computed(
    () => {
        return !data.post.dosen2Id || !data.post.judulBaru
    }
)

function createUpdateRequest(){
    return {
        tanggalValid: dateFormater.convertToWIB(data.currentDE.tanggalValid),
        catatan: data.currentDE.catatan,
        modified: dateFormater.convertToWIB(new Date()),
        userPenginputDEId: authStore.user.details.id,
        status: data.currentDE.status,
        dosen1Id: data.post.dosen1Id,
        dosen2Id: data.post.dosen2Id,
        proposalId: data.currentProposal.id,
        judulLama: data.currentProposal.judul,
        judulBaru: data.post.judulBaru,
        mahasiswaNim: data.penulis.nim,
        uploadRevisi: "T",
        dosenEvaluatorId: data.currentDE.dosenEvaluator?.id,
        groupEvaluatorId: data.currentJadwal?.id
    }
}

async function handleUpdate(){
    data.isUpdating=true
    let updated = await adminStore.updateDeskEvaluation(data.currentDE.id, createUpdateRequest())
    if(updated){
        alertStore.success("Proposal berhasil direkap")
        router.push(`/deskevaluation/adminpage/proposal-mahasiswa/semua-proposal/`)
    }
    data.isUpdating=false
}

function handleStatus(status){
    if(status == 'B'){
        return "Proposal Baru"
    }else if(status == 'V'){
        return "Proposal berstatus Valid sehingga belum dievaluasi"
    }else if(status == 'L'){
        return "<span class='badge bg-danger'>Batal</span>"
    }else if(status == 'T'){
        return `Diterima pada sidang hari ${dateFormater.formatDateTime(data.currentJadwal.waktu)} (Grup ${data.currentJadwal.kelompok})`
    }else if(status == 'K'){
        return `Ditolak pada sidang hari ${dateFormater.formatDateTime(data.currentJadwal.waktu)} (Grup ${data.currentJadwal.kelompok})`
    }else if(status == 'R'){
        return `Proposal harus direvisi`
    }else{
        return `${status}`
    }
}

function handleGelarDosen(dosen){
    let name = dosen?.nama
    if (dosen?.gelarDepan){
        name = dosen?.gelarDepan + " " + name
    }
    if (dosen?.gelar){
        name += ", " + dosen?.gelar
    }
    return name
}

onBeforeMount(
    async () => {
        let deskevaluationExists = await adminStore.getDeskEvaluationByID(route.params.deskevaluationID)
        if(deskevaluationExists){
            data.currentDE = adminStore.deskEvaluation
        }

        if(data.currentDE){
            data.currentProposal = data.currentDE.proposal
            data.currentActivePeriode = data.currentProposal.periodes
            data.penulis = data.currentDE.mahasiswa
            data.currentJadwal = data.currentDE.groupEvaluator

            data.post.dosen1Id = data.currentDE.dosen1?data.currentDE.dosen1.id:""
            data.post.dosen2Id = data.currentDE.dosen2?data.currentDE.dosen2.id:""
            data.post.judulBaru = data.currentDE.judulBaru?data.currentDE.judulBaru:""

            let success = await adminStore.getDosensBySidangID(data.currentJadwal?.id)
            if(success){
                data.currentTimSidang = adminStore.dosenDE
            }
        }

        await adminStore.getAllDosen()
        data.dosens = adminStore.dosens
        // if(data.dosens.length>0){
        //     data.dosens = data.dosens.filter(dosen => dosen.id != data.currentDE.dosen1?.id)
        // }

        data.isFetching = false
    }
)
</script>

<style scoped>
tr:hover{
    cursor: default;
    background: none !important;
}
tr:hover td{
    background: none !important;
}
</style>