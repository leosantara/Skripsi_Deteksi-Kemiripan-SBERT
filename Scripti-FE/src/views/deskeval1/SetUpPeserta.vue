<template>
    <Loading v-bind:isFetching="data.isFetching"/>
    <Overlay v-bind:isFetching="data.isSubmitting"/>
    <div v-if="!data.isFetching">
        <div v-if="!data.currentJadwal">
            <div class="mt-2 p-4 border border-2 rounded text-center">
                <h4>Gagal memuat data Sidang.</h4>
            </div>
        </div>
        <div v-else>
            <div class="alert alert-secondary mt-3" role="alert">
                <h6>Anda akan mengatur Peserta Sidang Evaluasi untuk jadwal sidang berikut:</h6>
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
                    <div v-if="!timSidangExists" class="text-center text-danger">
                        Maaf, data Tim Sidang tidak dapat ditemukan. Pastikan Anda telah membentuk Tim Sidang Evaluasi terlebih dahulu!
                    </div>
                    <div v-else>
                        Tim Evaluator:<br>
                        &emsp;Ketua: <br>
                        <span class="fw-bold">&emsp;&emsp;{{ data.gelarKetuaSidang }}</span><br>
                        &emsp;Anggota: <br>
                        <span v-for="dosen in data.gelarAnggotaSidang" class="fw-bold">&emsp;&emsp;{{ dosen }}<br></span>
                    </div>
                </div>
            </div>

            <div class="mt-2 p-4 border border-2 rounded" v-if="timSidangExists">
                <h6>Silakan pilih proposal Peserta Sidang:</h6>
                <input type="text" class="form-control mb-2" v-model="data.filter.searchFilter" placeholder="Cari Proposal berdasarkan Judul proposal atau NIM penulis...">
                <div class="mb-2">
                    <span class="badge bg-dark">{{ `found: ${data.proposals.length} proposal valid pada periode ini` }}</span> &emsp; 
                    <input v-model="data.selectAll" class="form-check-input" type="checkbox" value="checked" id="chooseAll">
                    <label class="form-check-label" for="chooseAll">
                        Pilih Semua Proposal
                    </label>
                </div>
                <div style="height: 400px; overflow-y: scroll;" v-if="data.proposals.length>0">
                    <SetupPesertaSidangTable 
                        v-bind:pesertaDatas="filteredPage"
                        v-model:choosenPeserta="data.choosenProposals"
                    />
                    <!-- v-model:selectAll="data.selectAll" -->
                </div>
                <div v-if="data.choosenProposals.length==0 && data.pesertaSidang.length==0" class="text-center mt-3">
                    <h6>Anda belum memilih peserta sidang.</h6>
                </div>
                <div v-else class="mt-3">
                    <table class="table table-striped table-hover">
                        <thead>
                            <tr>
                                <th>NIM</th>
                                <th>NAMA</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="data in data.pesertaSidang">
                                <td>{{ data.nim }}</td>
                                <td>{{ data.nama }}</td>
                            </tr>
                            <tr v-for="data in data.choosenProposals">
                                <td>{{ data.mahasiswa.nim }}</td>
                                <td>{{ data.mahasiswa.nama }}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="text-end mt-2">
                    <button class="btn btn-primary" :disabled="data.choosenProposals.length==0 || data.isSubmitting" @click="handleInsertDE()">SIMPAN</button>
                </div>
            </div>
        </div>
    </div>
    <br>
    <br>
</template>

<script setup>
import { reactive, onBeforeMount, computed, onUpdated, watch } from 'vue';
import { useProposalStore } from '../../stores/proposal.store';
import { useAdminStore } from '../../stores/admin.store';
import { useRoute } from 'vue-router';
import dateFormater from '../../helpers/date-formater';

import Loading from '../../components/animation/Loading.vue';
import SetupPesertaSidangTable from '../../components/searchableDropdown/SetupPesertaSidangTable.vue';
import Overlay from '../../components/animation/Overlay.vue';
import { useAuthStore } from '../../stores/auth.store';

const authStore = useAuthStore()
const adminStore = useAdminStore()
const proposalStore = useProposalStore()
const route = useRoute()

let data = reactive({
    proposals:[],
    choosenProposals:[],
    pesertaSidang:[],
    filter:{
        searchFilter:"",
    },
    currentJadwal:null,
    currentPeriode:null,
    timSidang:{
        ketua:null,
        anggota:[]
    },
    gelarKetuaSidang:"",
    gelarAnggotaSidang:[],
    selectAll:"",
    isFetching:true,
    isSubmitting:false,
})

const filteredPage = computed(
    () => {
        let filteredProposal = data.proposals
        if(data.filter.searchFilter!=""){
            filteredProposal = filteredProposal.filter(proposal => proposal.judul.toLowerCase().includes(data.filter.searchFilter.toLowerCase()) || proposal.mahasiswa.nim.includes(data.filter.searchFilter))
        }
        return filteredProposal
    }
)

function createPostRequest(proposal){
    return {
        tanggalValid: dateFormater.convertToWIB(proposal.modified),
        catatan: "Belum Ada Catatan",
        modified: dateFormater.convertToWIB(new Date()),
        userPenginputDEId: authStore.user.details.id,
        status: "V",
        dosen1Id: proposal.dosenId?.id,
        dosen2Id: null,
        proposalId: proposal.id,
        judulLama: proposal.judul,
        judulBaru: proposal.judul,
        mahasiswaNim: proposal.mahasiswa?.nim,
        uploadRevisi: "T",
        dosenEvaluatorId: null,
        groupEvaluatorId: data.currentJadwal.id
    }
}

async function handleInsertDE(){
    data.isSubmitting = true
    let count = 0
    let deleted = []

    //adding proposal peserta one by one
    for (const proposal of data.choosenProposals){
        let request = createPostRequest(proposal)
        let inserted = await adminStore.createDeskEvaluation(request)
        if(inserted){
            deleted.push(proposal.id)
            data.pesertaSidang.push({nim:proposal.mahasiswa.nim, nama:proposal.mahasiswa.nama})
            count++
        }
    }

    //removing proposal that alredy been choosen
    data.proposals = data.proposals.filter(
        proposal => !deleted.includes(proposal.id)
    )
    
    //restarting choosenProposal as an empty array:
    data.choosenProposals = []
    await new Promise(resolve => setTimeout(resolve, 5000))
    data.isSubmitting = false

    alert(`Berhasil menambahkan ${count} peserta`)
}

function handleGelarDosen(dosen){
    let name = ""
    if(dosen){
        name = dosen.nama
        if (dosen.gelarDepan){
            name = dosen.gelarDepan + " " + name
        }
        if (dosen.gelar){
            name += ", " + dosen.gelar
        }

    }return name
}

async function getGelarDosenByID(dosenID){
    let dosenExists = false
    if(route.fullPath.includes('adminpage')){
        dosenExists = await adminStore.getDosenByDosenID(dosenID)
    }else{
        dosenExists = await dosenStore.getDosenByDosenID(dosenID)
    }

    if(dosenExists && route.fullPath.includes('adminpage')){
        return handleGelarDosen(adminStore.dosen)
    }
    if(dosenExists && route.fullPath.includes('dosenpage')){
        return handleGelarDosen(dosenStore.dosen)
    }
}

//state
const timSidangExists = computed(
    () => {
        return data.timSidang.ketua && data.timSidang.anggota.length>0
    }
)

watch(
    () => data.selectAll,
    (newVal, oldVal) => {
        if(newVal==true){
            data.choosenProposals = data.proposals
        }else{
            data.choosenProposals = []
        }
    }
)

onBeforeMount(
    async() => {
        //REFACTOR SO THAT PERIODE AND SIDANG CAN BE FETCHED AT THE SAME TIME
        await adminStore.getSidangDEByID(route.params.sidangID)
        data.currentJadwal = adminStore.sidangDE
        
        if(data.currentJadwal){
            await adminStore.getPeriodeById(data.currentJadwal.periodeId)
            data.currentPeriode = adminStore.periode

            await adminStore.getDeskEvaluationsBySidangID(data.currentJadwal.id)
            if(adminStore.deskEvaluations.length>0){
                adminStore.deskEvaluations.forEach(de => {
                    data.pesertaSidang.push({nim:de.mahasiswa.nim, nama:de.mahasiswa.nama})
                })
            }
        }

        if(data.currentPeriode){
            await proposalStore.adminGetProposalsForSidangDE(data.currentPeriode.id)
            data.proposals = proposalStore.proposals
        }

        await adminStore.getDosensBySidangID(route.params.sidangID)
        if(adminStore.dosenDE){
            adminStore.dosenDE.anggota.forEach(anggota => {
                data.timSidang.anggota.push(anggota)
            })

            if(adminStore.dosenDE.ketua){
                data.timSidang.ketua = adminStore.dosenDE.ketua
            }
        }

        data.gelarKetuaSidang = await getGelarDosenByID(data.timSidang.ketua.dosenId)
        for(let anggota of data.timSidang.anggota){
            let gelarAnggota = await getGelarDosenByID(anggota.dosenId)
            data.gelarAnggotaSidang.push(gelarAnggota)
        }

        data.isFetching = false
    }
)
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