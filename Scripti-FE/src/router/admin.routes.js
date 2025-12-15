import Layout from "../views/admin/Layout.vue";
import MahasiswaForm from "../views/admin/MahasiswaForm.vue";
import MahasiswaList from "../views/admin/MahasiswaList.vue";
import MahasiswaUpdate from "../views/admin/MahasiswaUpdate.vue";
import DosenList from "../views/admin/DosenList.vue";
import DosenForm from "../views/admin/DosenForm.vue";
import DosenUpdate from "../views/admin/DosenUpdate.vue";
import PeriodeKolokiumList from "../views/admin/PeriodeKolokiumList.vue";
import PeriodeKolokiumForm from "../views/admin/PeriodeKolokiumForm.vue"
import PeriodeKolokiumUpdate from "../views/admin/PeriodeKolokiumUpdate.vue";

export default {
    path: '/adminpage',
    component: Layout,
    children: [
        {path: 'mahasiswa', component: MahasiswaList},
        {path: 'mahasiswa/tambah', component:MahasiswaForm},
        {path: 'mahasiswa/:nim', component:MahasiswaUpdate},
        {path: 'dosen', component: DosenList},
        {path: 'dosen/tambah', component:DosenForm},
        {path: 'dosen/:nidn', component:DosenUpdate},
        {path: 'periode-proposal', component:PeriodeKolokiumList},
        {path: 'periode-proposal/tambah', component:PeriodeKolokiumForm},
        {path: 'periode-proposal/update/:periodeID', component: PeriodeKolokiumUpdate}
    ]
}