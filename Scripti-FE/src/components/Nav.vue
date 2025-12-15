<script setup>
import { useAuthStore } from '@/stores';
import { RouterLink, useRoute } from 'vue-router';

const authStore = useAuthStore();
const route = useRoute()

function toggleNavDropdown(elementID){
    //make it so that only 1 dropdown is shown at a time
    let dropdownContentID = elementID + "Content"
    document.getElementById(dropdownContentID).classList.toggle('show')
    
    let dropdownContents = document.getElementsByClassName('my-dropdown-content')
    for(let i=0; i<dropdownContents.length;i++){
        if(dropdownContents[i].classList.contains('show') && dropdownContents[i].id != dropdownContentID){
            dropdownContents[i].classList.remove('show')
        }
    }
}

</script>

<template>
    <nav v-if="authStore.user" class="navbar navbar-expand navbar-dark bg-dark">
        <div class="navbar-nav d-flex justify-content-start gap-2">
            <RouterLink to="/" class="nav-item nav-link">Home</RouterLink>
            <li v-if="authStore.user.role.isAdmin" class="nav-item dropdown">
                <a id="dropdownProposalAdm" class="my-dropdown nav-link dropdown-toggle" :class="{'active':route.fullPath.includes('/proposalpage/adminpage')}" href="#" role="button" @click="toggleNavDropdown('dropdownProposalAdm')">
                    Proposal (admin)
                </a>
                <ul id="dropdownProposalAdmContent" class="my-dropdown-content dropdown-menu dropdown-menu-dark">
                    <li><RouterLink to="/proposalpage/adminpage/proposal-mahasiswa" class="dropdown-item">Daftar Proposal</RouterLink></li>
                    <li><RouterLink to="/deskevaluation/adminpage/proposal-mahasiswa/semua-proposal/" class="dropdown-item">Desk Evaluation</RouterLink></li>
                    <li><RouterLink to="/proposalpage/adminpage/seminar" class="dropdown-item">Seminar</RouterLink></li>
                </ul>
            </li>
            <li v-if="authStore.user.role.isMahasiswa" class="nav-item dropdown">
                <a id="dropdownProposalMhs" class="my-dropdown nav-link dropdown-toggle" :class="{'active':route.fullPath.includes('/proposalpage/mahasiswapage')}" href="#" role="button" @click="toggleNavDropdown('dropdownProposalMhs')">
                    Proposal (mahasiswa)
                </a>
                <ul id="dropdownProposalMhsContent" class="my-dropdown-content dropdown-menu dropdown-menu-dark">
                    <li><RouterLink to="/proposalpage/mahasiswapage/proposal-mahasiswa" class="dropdown-item">Daftar Proposal</RouterLink></li>
                    <li><RouterLink to="/proposalpage/mahasiswapage/desk-evaluation" class="dropdown-item">Desk Evaluation</RouterLink></li>
                    <li><RouterLink to="/proposalpage/mahasiswapage/seminar" class="dropdown-item">Seminar</RouterLink></li>
                </ul>
            </li>
            <li v-if="authStore.user.role.isDosen" class="nav-item dropdown">
                <a id="dropdownProposalDsn" class="my-dropdown nav-link dropdown-toggle" :class="{'active':route.fullPath.includes('/proposalpage/dosenpage')}" href="#" role="button" @click="toggleNavDropdown('dropdownProposalDsn')">
                    Proposal (Dosen)
                </a>
                <ul id="dropdownProposalDsnContent" class="my-dropdown-content dropdown-menu dropdown-menu-dark">
                    <li><RouterLink to="/proposalpage/dosenpage/proposal-mahasiswa" class="dropdown-item">Daftar Proposal</RouterLink></li>
                    <li><RouterLink to="/deskevaluation/dosenpage/jadwal-sidang/current-periode" class="dropdown-item">Desk Evaluation</RouterLink></li>
                    <li><RouterLink to="/proposalpage/dosenpage/seminar" class="dropdown-item">Seminar</RouterLink></li>
                </ul>
            </li>
            <li v-if="authStore.user.role.isAdmin" class="nav-item dropdown">
                <a id="dropdownPeriode" class="my-dropdown nav-link dropdown-toggle" :class="{'active':route.fullPath.includes('/adminpage/periode')}" href="#" role="button" @click="toggleNavDropdown('dropdownPeriode')">
                    Periode
                </a>
                <ul id="dropdownPeriodeContent" class="my-dropdown-content dropdown-menu dropdown-menu-dark">
                    <li><RouterLink to="/adminpage/periode-proposal" class="dropdown-item">Periode Proposal</RouterLink></li>
                    <li><RouterLink to="/adminpage/periode-seminar" class="dropdown-item">Periode Seminar</RouterLink></li>
                    <li><RouterLink to="/adminpage/periode-pendadaran" class="dropdown-item">Periode Pendadaran</RouterLink></li>
                </ul>
            </li>
            <RouterLink v-if="authStore.user.role.isAdmin" to="/users" class="nav-item nav-link">Users</RouterLink>
            <li v-if="authStore.user.role.isAdmin" class="nav-item dropdown">
                <a id="dropdownDsn" class="my-dropdown nav-link dropdown-toggle" :class="{'active':route.fullPath.includes('/adminpage/dosen')}" href="#" role="button" @click="toggleNavDropdown('dropdownDsn')">
                    Dosen
                </a>
                <ul id="dropdownDsnContent" class="my-dropdown-content dropdown-menu dropdown-menu-dark">
                    <li><RouterLink to="/adminpage/dosen" class="dropdown-item">Daftar Dosen</RouterLink></li>
                    <li><RouterLink to="/adminpage/dosen/tambah" class="dropdown-item">Tambah Dosen</RouterLink></li>
                </ul>
            </li>
            <li v-if="authStore.user.role.isAdmin" class="nav-item dropdown">
                <a id="dropdownMhs" class="my-dropdown nav-link dropdown-toggle" :class="{'active':route.fullPath.includes('/adminpage/mahasiswa')}" href="#" role="button" @click="toggleNavDropdown('dropdownMhs')">
                    Mahasiswa
                </a>
                <ul id="dropdownMhsContent" class="my-dropdown-content dropdown-menu dropdown-menu-dark">
                    <li><RouterLink to="/adminpage/mahasiswa" class="dropdown-item">Daftar Mahasiswa</RouterLink></li>
                    <li><RouterLink to="/adminpage/mahasiswa/tambah" class="dropdown-item">Tambah Mahasiswa</RouterLink></li>
                </ul>
            </li>
            <!-- <button @click="authStore.logout()" class="btn btn-link nav-item nav-link">Logout</button> -->
        </div>
    </nav>
</template>