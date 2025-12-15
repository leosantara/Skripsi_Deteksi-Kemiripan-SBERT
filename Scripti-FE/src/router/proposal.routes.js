import Layout from "../views/proposal/Layout.vue";
import ProposalList from "../views/proposal/AdminProposalList.vue"
import ProposalForm from "../views/proposal/AdminProposalForm.vue"
import ProposalUpdate from "../views/proposal/AdminProposalUpdate.vue";
import ProposalListMhs from "../views/proposal/MahasiswaProposalList.vue";
import ProposalFormMhs from "../views/proposal/MahasiswaProposalForm.vue";
import ProposalUpdateMhs from "../views/proposal/MahasiswaProposalUpdate.vue"
import DosenProposalList from "../views/proposal/DosenProposalList.vue";

export default {
    path: '/proposalpage',
    component: Layout,
    children: [
        {path: 'adminpage/proposal-mahasiswa', component:ProposalList},
        {path: 'adminpage/tambah', component: ProposalForm},
        {path: 'adminpage/update/:proposalID', component: ProposalUpdate},
        {path: 'mahasiswapage/proposal-mahasiswa', component:ProposalListMhs},
        {path: 'mahasiswapage/tambah', component: ProposalFormMhs},
        {path: 'dosenpage/proposal-mahasiswa', component: DosenProposalList},
        {path: 'mahasiswapage/update/:proposalID', component: ProposalUpdateMhs}
    ]
}