<template>
    <h1>Berita Acara</h1>
    <hr>
    <Loading v-bind:isFetching="data.isFetching"/> 
    <div v-if="!data.isFetching && !data.currentDE">
        <div class="border p-4 shadow">
            <h6>Gagal memuat data Berita Acara</h6>
        </div>
    </div>
    <div v-if="!data.isFetching && data.currentDE">
        <div class="border p-4 shadow" style="height: 1123px; width: 794px; overflow-y: scroll">
            <div id="pdfContent" class="pdf-content" style="padding: 30px;">
                <div style="display: flex; flex-direction: row; gap: 10px;">
                    <div>
                        <img src="../../assets/logo-ukdw.jpg" style="height: 70px; align-items: center; margin-top: 5px;">
                    </div>
                    <div>
                        <p style="font-size: 10pt;"> 
                            <b>
                            Program Studi Informatika<br/>
                            Fakultas Teknologi Informasi<br/>
                            </b>
                            Universitas Kristen Duta Wacana Yogyakarta<br/>
                            Dr. Wahidin Sudirahusada 5-25 Yogyakarta, 55224. Telp. (0274)563929<br/>
                        </p>
                    </div>
                </div>
                <div style="text-align: center;">
                    <h4 style="margin-bottom: 0;">Berita Acara Evaluasi Proposal Tugas Akhir</h4>
                    <h6>Strata-1 Program Studi Informatika</h6>
                </div>
                <br>
                <p style="font-size: 10pt;">Dengan ini dinyatakan bahwa PROPOSAL TUGAS AKHIR (SKRIPSI) dengan judul:<br/></p>
                <div style="border: 1px solid black; padding: 5px; text-align: center; font-size: 12pt;"><b> {{ data.currentProposal.judul }} </b></div>
                <br>
                <div style="font-size: 10pt;">
                    <p>yang diajukan oleh mahasiswa:<br/></p>
                    <table style="border-spacing: 0 10px; border-collapse: separate;">
                        <tbody>
                            <tr>
                                <td>
                                    <div style="display: flex; flex-direction: row; gap: 40px; align-items: center;">
                                        <span>NIM/NAMA</span>
                                        <span>:</span>
                                    </div>
                                </td>
                                <td style="width: 100%; border-bottom: 0.8px solid black;"><b>{{ `${data.penulis.nim}/${data.penulis.nama}` }}</b></td>
                            </tr>
                            <tr>
                                <td>
                                    <div style="display: flex; flex-direction: row; gap: 40px; align-items: center;">
                                        <span>Dinyatakan</span>
                                        <span>&nbsp;:</span>
                                    </div>
                                </td>
                                <td style="width: 70%;">
                                    <div style="display: flex; flex-direction: row; gap: 40px; align-items: center;">
                                        <span v-html="handleStatus('terima')"></span>
                                        <span v-html="handleStatus('tolak')"></span>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p>Adapun judul tugas akhir yang disetujui:</p>
                </div>
                <div style="border: 1px solid black; padding: 5px; text-align: center; font-size: 12pt;"><b> {{ data.currentDE.judulBaru }} </b></div>
                <p style="font-size: 10pt;">Adapun beberapa catatan terkait dengan proposal tugas akhir (skripsi) yang disepakati dalam tahapan evaluasi proposal ini:</p>
                <div style="background-color: #efefef;border: 1px solid black; padding: 5px; font-size: 10pt;">
                    <p v-html="data.currentDE.catatan"></p>
                </div>
                <div style="font-size: 10pt;">
                    <table style="border-spacing: 0 10px; border-collapse: separate;">
                        <tbody>
                            <tr>
                                <td>
                                    <div style="display: flex; flex-direction: row; gap: 40px; align-items: center;">
                                        <span>Usulan Dosen Pembimbing I</span>
                                        <span>&NonBreakingSpace;:</span>
                                    </div>
                                </td>
                                <td style="border-bottom: 0.8px solid black; ;width: 70%"><b>{{ `${handleGelarDosen(data.currentDE.dosen1)}` }}</b></td>
                            </tr>
                            <tr>
                                <td>
                                    <div style="display: flex; flex-direction: row; gap: 40px; align-items: center;">
                                        <span>Usulan Dosen Pembimbing II</span>
                                        <span>:</span>
                                    </div>
                                </td>
                                <td style="border-bottom: 0.8px solid black;width: 70%;"><b>{{ `${handleGelarDosen(data.currentDE.dosen2)}` }}</b></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <p style="font-size: 10pt;">Berita acara ini disepakati dalam sidang evaluasi proposal pada tanggal: {{ dateFormater.formatDate(data.currentJadwal.waktu) }}.</p>
                <div style="text-align: center">
                    <p style="font-size: 10pt; text-align: center">Mengetahui, </p>
                    <table style="width: 100%; border-spacing: 0 0px; border-collapse: separate;">
                        <tbody>
                            <tr>
                                <td style="height: 100px; vertical-align: bottom;" >(......................................)</td>
                                <td style="height: 100px; vertical-align: bottom;" >(......................................)</td>
                            </tr>
                            <tr>
                                <td>Ketua</td>
                                <td>Anggota</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <br>
                <p style="font-size: 8pt;"><i>dicetak pada tanggal: {{ handleTanggalPrint() }}</i></p>
            </div>
        </div>
        <div class="text-start px-2 mt-3">
            <button class="btn btn-primary" @click="generatePDF()">Download file PDF</button>
        </div>
    </div>
    <br>
    <br>
</template>

<script setup>
import { ref, reactive, onBeforeMount } from 'vue';
import { useAdminStore } from '../../stores/admin.store';
import { useDosenStore } from '../../stores/dosen.store';
import { useAuthStore } from '../../stores/auth.store';
import { useRoute } from 'vue-router';
import dateFormater from '../../helpers/date-formater';
import html2pdf from 'html2pdf.js'
import Loading from '../animation/Loading.vue';

const pdfContent = ref(null)
const route = useRoute()
const adminStore = useAdminStore()
const dosenStore = useDosenStore()
const authStore = useAuthStore()

let data = reactive({
    currentDE:null,
    currentProposal:null,
    currentActivePeriode:null,
    currentJadwal:null,
    currentTimSidang:null,
    penulis:null,
    isFetching:true
})

function generatePDF() {
    let fileName = `${data.penulis.nim}-berita-acara.pdf`
    html2pdf(document.getElementById('pdfContent'), {
        filename:fileName,
        margin:[1,1,1,1]
    })

    // const printContent = pdfContent.value
    // const newWindow = window.open('', '', 'width=794,height=1123') // seukuran kertas A4
    // newWindow.document.write('<html><head><title>Print</title>')
    // newWindow.document.write('</head><body>')
    // newWindow.document.write(printContent.innerHTML)
    // newWindow.document.write('</body></html>')
    // newWindow.document.close()
    // newWindow.print()
    // newWindow.close()
}

function handleStatus(checkbox){
    if(checkbox=='terima'){
        if(data.currentDE.status=='T'){
            return "<span>&#9746; Terima</span>"
        }else{
            return "<span>&#9744; Terima</span>"
        }
    }else if(checkbox='tolak'){
        if(data.currentDE.status=='K'){
            return "<span>&#9746; Tolak</span>"
        }else{
            return "<span>&#9744; Tolak</span>"
        }
    }
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

function handleTanggalPrint(){
    let tanggalUTC = new Date().toISOString()
    let tanggalWIB = new Date(tanggalUTC)
    tanggalWIB.setHours(tanggalWIB.getHours()+7)

    let formattedDatetime = dateFormater.formatDateTime(tanggalWIB.toISOString())
    let formattedDatetimeArr = formattedDatetime.split('.')
    return `${formattedDatetimeArr[0]} WIB`
}

onBeforeMount(
    async () => {
        let deskevaluationExist = false
        if(route.fullPath.includes('adminpage')){
            deskevaluationExist = await adminStore.getDeskEvaluationByID(route.params.deskevaluationID)
            if(deskevaluationExist){
                data.currentDE = adminStore.deskEvaluation
            }
        }else if(route.fullPath.includes('dosenpage')){
            deskevaluationExist = await dosenStore.getDeskEvaluationByProposalID(route.params.proposalID)
            if(deskevaluationExist){
                data.currentDE = dosenStore.deskEvaluation
            }
        }

        if(data.currentDE){
            data.currentProposal = data.currentDE.proposal
            data.currentActivePeriode = data.currentProposal.periodes
            data.penulis = data.currentDE.mahasiswa
            data.currentJadwal = data.currentDE.groupEvaluator

            let timSidangExists = false
            if(route.fullPath.includes('adminpage')){
                timSidangExists = await adminStore.getDosensBySidangID(data.currentJadwal?.id)
                if(timSidangExists){
                    data.currentTimSidang = adminStore.dosenDE
                }
            }
            else if(route.fullPath.includes('dosenpage')){
                timSidangExists = await dosenStore.getJadwalSidangDosenByNidnAndPeriodeID(authStore.user.details.nim, data.currentActivePeriode.id)
                if(timSidangExists){
                    for(let i = 0; i<dosenStore.jadwalSidangDosen.length;i++){
                        let jadwal = dosenStore.jadwalSidangDosen[i]
                        if(jadwal.sidangId == data.currentDE.groupEvaluator?.id){
                            data.currentTimSidang = jadwal
                            break
                        }
                    }
                }
            }
        }

        data.isFetching = false
    }
)
</script>

<style scoped>
/* in case butuh css untuk pdf: */
/* p { 
    page-break-inside: avoid; 
} */
tr:hover{
    cursor: default;
    background: none !important;
}
tr:hover td{
    background: none !important;
}
</style>