<template>
    <div v-if="authStore.user" class="myHeader d-flex justify-content-between">
        <div class="scripti">
            <h1 id="scripti">ScripTI</h1>
            <h4>Sistem Pengelolaan Matakuliah Skripsi TI UKDW</h4>
        </div>
        <div v-if="authStore.user" class="profile d-flex align-self-center">
            <img class="profile_pic" :src="authStore.user.details.googleUser.googlePictureLink" alt="profile picture" referrerpolicy="no-referrer">
            <div>
                <h6>{{ authStore.user.details.fullname }}</h6>
                <h6 id="dropdownHeader" class="my-dropdown dropdown-toggle" @click="toggleHeaderDropdown('dropdownHeader')">{{ authStore.user.details.nim }}</h6>
                <ul class="my-dropdown-content dropdown-menu dropdown-menu-dark" id="dropdownHeaderContent">
                    <li class="dropdown-item" @click="logout()">Logout</li>
                </ul>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useAuthStore } from '@/stores'
import { onUpdated } from 'vue';

const authStore = useAuthStore()
let dropdownHeader = null

function toggleHeaderDropdown(elementID){
    let dropdownContentID = elementID + "Content"
    document.getElementById(dropdownContentID).classList.toggle('show')

    let dropdownContents = document.getElementsByClassName('my-dropdown-content')
    for(let i=0; i<dropdownContents.length;i++){
        if(dropdownContents[i].classList.contains('show') && dropdownContents[i].id != dropdownContentID){
            dropdownContents[i].classList.remove('show')
        }
    }
}

function logout(){
    authStore.logout()
}

onUpdated(
    () => {
        dropdownHeader = document.getElementById('dropdownHeader')
    }
)
function showContent(elementID){
    //make it so that only 1 dropdown is shown at a time
    let dropdownContentID = elementID + "Content"
    document.getElementById(dropdownContentID).classList.toggle('show')
    let dropdownContents = document.getElementsByClassName('nav-dropdown-menu')
    for(let i=0; i<dropdownContents.length;i++){
        if(dropdownContents[i].classList.contains('show') && dropdownContents[i].id != dropdownContentID){
            dropdownContents[i].classList.remove('show')
        }
    }
}
</script>

<style scoped>
.myHeader{
    /* background-color: #47a3da; */
    color: white;
    background: rgb(71,163,218);
    background: linear-gradient(90deg, rgba(71,163,218,1) 35%, rgba(35,78,123,1) 79%, rgba(35,78,123,1) 100%);
}

.scripti{
    padding: 20px;
}

#scripti{
    font-family: 'Times New Roman', Times, serif;
    font-weight: bold;
}

.profile{
    margin: 40px;
    gap: 10px;
}

#dropdownHeader:hover{
    cursor: pointer;
    text-decoration: underline;
    color: orange;
}

.profile_pic{
    width: 50px;
    height: 50px;
    border: 4px solid gray;
    border-radius: 4px;
}
</style>