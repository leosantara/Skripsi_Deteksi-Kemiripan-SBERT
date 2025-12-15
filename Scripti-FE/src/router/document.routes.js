import Layout from '../components/generatedDocument/Layout.vue'
import JadwalSidang from '../components/generatedDocument/JadwalSidang.vue'
import BeritaAcara from '../components/generatedDocument/BeritaAcara.vue'

export default {
    path: '/dokumen',
    component: Layout,
    children: [
        {path: 'adminpage/berita-acara/:deskevaluationID', component:BeritaAcara},
        {path: 'dosenpage/berita-acara/:proposalID', component:BeritaAcara},
        // {path: 'mahasiswapage/berita-acara/:proposalID', component:BeritaAcara},
        {path: 'adminpage/jadwalSidang/:sidangID', component: JadwalSidang},
        {path: 'dosenpage/jadwalSidang/:sidangID', component: JadwalSidang}
    ]
}