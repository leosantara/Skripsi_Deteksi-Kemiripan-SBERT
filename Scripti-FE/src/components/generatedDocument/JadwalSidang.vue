<template>
<h1>Jadwal Sidang</h1>
<hr>
<Loading v-bind:isFetching="data.isFetching"/> 
<div v-if="!data.isFetching && data.deskevaluations.length==0">
    <div class="border p-4 shadow">
        <h6>Gagal memuat jadwal, pastikan bahwa data peserta sidang sudah dibuat</h6>
    </div>
</div>
<div v-if="!data.isFetching && data.deskevaluations.length>0">
    <div class="border p-4 shadow">
        <div ref="pdfContent" id="pdfContent"class="pdf-content" style="padding: 30px; font-size: 10pt;">
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
            <hr>
            <div style="padding: 10px;">
                <table>
                    <tbody>
                        <tr>
                            <td>Periode Evaluasi</td>
                            <td>: {{ data.currentPeriode?`${dateFormater.formatDate(data.currentPeriode.tglAwal)} s/d ${dateFormater.formatDate(data.currentPeriode.tglAkhir)}`:"Tidak dapat menampilkan Periode Evaluasi" }}</td>
                        </tr>
                        <tr>
                            <td>Waktu Sidang</td>
                            <td>: {{dateFormater.formatDateTime(data.currentJadwal.waktu)}}</td>
                        </tr>
                        <tr>
                            <td>Kelompok Tim Sidang</td>
                            <td>: {{ data.currentJadwal.kelompok }}</td>
                        </tr>
                    </tbody>
                </table>
                <div v-if="!data.timSidang">
                    Maaf, data Tim Sidang tidak dapat ditemukan. Pastikan Anda telah membentuk Tim Sidang Evaluasi terlebih dahulu!
                </div>
                <div v-else>
                    Tim Evaluator<br>
                    &emsp;&emsp;Ketua: <br>
                    <span>&emsp;&emsp;&emsp;<b>{{ data.gelarKetuaSidang }}</b></span><br>
                    &emsp;&emsp;Anggota: <br>
                    <span v-for="dosen in data.gelarAnggotaSidang">&emsp;&emsp;&emsp;<b>{{ dosen }}</b><br></span>
                </div>
                <br>
                <p>Dengan data peserta sidang sebagai berikut:</p>
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background-color: #f2f2f2;">
                            <th>NIM</th>
                            <th>NAMA MAHASISWA</th>
                            <th>JUDUL PROPOSAL</th>
                            <th>DOSEN PEMBIMBING</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(data, index) in data.deskevaluations" :style="index%2==0?'background-color: #fff':'background-color: #f2f2f2;'">
                            <td>{{data.mahasiswa?.nim}}</td>
                            <td>{{data.mahasiswa?.nama}}</td>
                            <td>{{data.proposal?.judul}}</td>
                            <td>{{handleGelarDosen(data.dosen1)}}</td>
                        </tr>
                    </tbody>
                </table>
                <!-- <div class="spacer"></div> -->
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
import html2pdf from 'html2pdf.js';
import dateFormater from '../../helpers/date-formater';
import Loading from '../animation/Loading.vue';

const pdfContent = ref(null)
const route = useRoute()
const adminStore = useAdminStore()
const dosenStore = useDosenStore()
const authStore = useAuthStore()
let data = reactive({
    deskevaluations:[],
    currentPeriode:null,
    currentJadwal:null,
    timSidang:null,
    gelarKetuaSidang:"",
    gelarAnggotaSidang:[],
    isFetching:true
})

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

function generatePDF(){
    let fileName = `jadwalSidangGrup${data.currentJadwal.kelompok}-${data.currentJadwal.waktu}.pdf`
    html2pdf(document.getElementById('pdfContent'), {
        filename:fileName,
        margin:[1,1,1,1]
    })

    // const printContent = pdfContent.value
    // const newWindow = window.open('', '', 'width=794,height=1123') // seukuran kertas A4
    // newWindow.document.write('<html><head><title>Print</title>')
    // //newWindow.document.write('<link rel="stylesheet" href="path/to/your/styles.css">'); // incase butuh css
    // newWindow.document.write('</head><body>')
    // newWindow.document.write(printContent.innerHTML)
    // newWindow.document.write('</body></html>')
    // newWindow.document.close()
    // newWindow.print()
    // newWindow.close()
}

onBeforeMount(
    async () => {
        let deskevaluationsExist = false
        let timSidangExists = false
        if(route.fullPath.includes('adminpage')){
            deskevaluationsExist = await adminStore.getDeskEvaluationsBySidangID(route.params.sidangID)
            if(deskevaluationsExist){
                data.deskevaluations = adminStore.deskEvaluations
                data.currentPeriode = data.deskevaluations[0].proposal?.periodes
                data.currentJadwal = data.deskevaluations[0].groupEvaluator
                timSidangExists = await adminStore.getDosensBySidangID(route.params.sidangID)
            }
        }else if(route.fullPath.includes('dosenpage')){
            deskevaluationsExist = await dosenStore.getDeskEvaluationsBySidangID(route.params.sidangID)
            if(deskevaluationsExist){
                data.deskevaluations = dosenStore.deskEvaluations
                data.currentPeriode = data.deskevaluations[0].proposal?.periodes
                data.currentJadwal = data.deskevaluations[0].groupEvaluator
                timSidangExists = await dosenStore.getJadwalSidangDosenByNidnAndPeriodeID(authStore.user.details.nim, data.currentPeriode.id)
            }
        }

        if(timSidangExists && route.fullPath.includes('adminpage')){
            data.timSidang = adminStore.dosenDE
        }

        if(timSidangExists && route.fullPath.includes('dosenpage')){
            for(let i = 0; i<dosenStore.jadwalSidangDosen.length;i++){
                let jadwal = dosenStore.jadwalSidangDosen[i]
                if(jadwal.sidangId == route.params.sidangID){
                    data.timSidang = jadwal
                    break
                }
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
table { 
    page-break-inside: auto;
    page-break-before: avoid;
}
tr { 
    page-break-inside: avoid; 
    page-break-after: auto; 
}
tr:hover{
    cursor: default;
    background: none !important;
}
tr:hover td{
    background: none !important;
}
/* tbody {
    margin-top: 20px;
    display: block
} */
</style>