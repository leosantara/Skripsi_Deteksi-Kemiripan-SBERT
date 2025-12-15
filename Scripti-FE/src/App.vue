<script setup>
import { Nav, Alert } from '@/components';
import { useAuthStore } from '@/stores';
import Header from './components/Header.vue';

const authStore = useAuthStore();
const d = new Date()

//ensure that our custom dropdown will be closed when losing focus (onblur)
window.onclick = function(event){
    //console.log('clicked on', event.target.classList)
    if(!event.target.matches('.my-dropdown')){
        let dropdownContent = document.getElementsByClassName('my-dropdown-content')
        for(let i=0; i<dropdownContent.length;i++){
            if(dropdownContent[i].classList.contains('show')){
                dropdownContent[i].classList.remove('show')
            }
        }
    }
}
</script>

<template>
    <div class="app-container" :class="authStore.user && 'bg-light'">
        <Header />
        <Nav />
        <Alert />
        <div class="container pt-4 pb-4">
            <router-view />
        </div>
        <br>
        <div class="scripTiFooter px-4 py-2">
            &copy;2014 - {{ `${d.getFullYear()}` }} Prodi Informatika Universitas Kristen Duta Wacana
        </div>
    </div>
</template>

<style>
@import '@/assets/styles.css';

.scripTiFooter{
    position: absolute;
    bottom: 0;
    height: 2.5rem;
    width: 100%;
    background-color: black;
    color: white;
    margin-top: 40px;
}
</style>