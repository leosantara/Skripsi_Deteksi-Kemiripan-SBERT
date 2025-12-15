<template>
    <table class="table table-striped table-hover">
        <thead>
            <tr>
                <th>Pilih</th>
                <th>NIM</th>
                <th>NAMA</th>
                <th>JUDUL PROPOSAL</th>
                <th>DOSEN PEMBIMBING</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="(data,index) in props.pesertaDatas">
                <td>
                    <div class="form-check">
                        <input class="form-check-input" v-model="choosenPeserta" type="checkbox" :value="data">
                    </div>
                </td>
                <td>{{ data.mahasiswa?.nim }}</td>
                <td>{{ data.mahasiswa?.nama }}</td>
                <td>
                    <span class="badge bg-dark">{{ handleStatus(data.status) }}</span> <br>
                    <span class="fw-bold">{{ data.judul }}</span> <br>
                    Diunggah: <span class="badge bg-dark">{{ dateFormater.formatDateTime(data.modified) }}</span> || <a class="myDownloadLink" href="#" @click.prevent="handleDownload(data.id, data.filename)" >&#x1F5CE; Download Proposal</a>
                </td>
                <td>
                    {{ handleGelarDosen(data.dosenId) }}
                </td>
            </tr>
        </tbody>
    </table>
</template>

<script setup>
import { defineModel, defineProps, defineEmits } from 'vue';
import dateFormater from '../../helpers/date-formater';
import { useProposalStore } from '../../stores/proposal.store';

const proposalStore = useProposalStore()
const choosenPeserta = defineModel('choosenPeserta', {default:[]})
const props = defineProps({
    pesertaDatas:{
        type: Array,
        required:true
    }
})

function handleStatus(status){
    if(status == 'B'){
        return 'Baru'
    }else if(status == 'V'){
        return 'Valid'
    }else if(status == 'L'){
        return 'Batal'
    }else if(status == 'T'){
        return 'Diterima'
    }else if(status == 'K'){
        return 'Ditolak'
    }else if(status == 'R'){
        return 'Revisi'
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

async function handleDownload(proposalID, filename){
    await proposalStore.adminGetProposalFile(proposalID, filename)
}
</script>

