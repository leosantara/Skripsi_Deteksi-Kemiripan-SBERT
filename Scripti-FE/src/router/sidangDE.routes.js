import Layout from '../views/deskeval1/Layout.vue'
import JadwalSidangList from '../views/deskeval1/JadwalSidangList.vue'
import JadwalSidangForm from '../views/deskeval1/JadwalSidangForm.vue'
import JadwalSidangUpdate from '../views/deskeval1/JadwalSidangUpdate.vue'
import SetUpDosen from '../views/deskeval1/SetUpDosen.vue'
import SetUpPeserta from '../views/deskeval1/SetUpPeserta.vue'
import DeskevaluationsList from '../views/deskeval1/DeskevaluationsList.vue'
import JadwalSidangDosen from '../views/deskeval1/JadwalSidangDosen.vue'
import DeskevaluationsUpdate from '../views/deskeval1/DeskevaluationsUpdate.vue'
import DeskevaluationsDosen from '../views/deskeval1/DeskevaluationsDosen.vue'
import DeskevaluationsRekap from '../views/deskeval1/DeskevaluationsRekap.vue'

export default {
    path: '/deskevaluation',
    component: Layout,
    children: [
        {path: 'adminpage/periode/:periodeID/semua-sidang', component: JadwalSidangList},
        {path: 'adminpage/periode/:periodeID/tambah', component: JadwalSidangForm},
        {path: 'adminpage/periode/:periodeID/:sidangID/update', component:JadwalSidangUpdate},
        {path: 'adminpage/periode/:periodeID/:sidangID/setup-timSidang', component:SetUpDosen},
        {path: 'adminpage/periode/:periodeID/:sidangID/setup-pesertaSidang', component:SetUpPeserta},
        {path: 'adminpage/proposal-mahasiswa/semua-proposal/', component: DeskevaluationsList},
        {path: 'adminpage/proposal-mahasiswa/evaluate/:deskevaluationID', component: DeskevaluationsUpdate},
        {path: 'adminpage/proposal-mahasiswa/rekap/:deskevaluationID', component: DeskevaluationsRekap},
        {path: 'dosenpage/jadwal-sidang/current-periode', component: JadwalSidangDosen},
        {path: 'dosenpage/jadwal-sidang/:sidangID/semua-proposal/', component: DeskevaluationsDosen},
        {path: 'dosenpage/proposal-mahasiswa/evaluate/:proposalID', component: DeskevaluationsUpdate},
        {path: 'mahasiswapage/proposal/hasil-sidang/:proposalID', component: DeskevaluationsUpdate}
    ]
}