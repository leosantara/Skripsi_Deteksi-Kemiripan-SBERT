<template>
    <h1>Aktivasi Akun Pertama</h1>
    <h6>Anda melakukan login pertama kali ke sistem ScripTI. Silahkan lengkapi data Anda pada form berikut ini.</h6>
    <div class="card">
        <div class="card-header">
            Sign Up
        </div>
        <div class="card-body">
            <h6>Anda mendaftar sebagai:</h6>
            <div class="mb-2">
                <label for="mahasiswa" class="btn custom" :class="computedClassMhs">
                    <input 
                        class="form-check-input"
                        v-model="data.role"  
                        type="radio" 
                        id="mahasiswa" 
                        value="mahasiswa" />
                    Mahasiswa
                </label>
            </div>
            <div>
                <label for="dosen" class="btn custom" :class="computedClassDosen">
                    <input 
                        class="form-check-input"
                        v-model="data.role" 
                        type="radio" 
                        id="dosen" 
                        value="dosen" />                
                    Dosen
                </label>
            </div>
            <hr>
            <form action="" v-on:submit.prevent="submit">
                <h6>Mohon isikan semua data!</h6>
                <div class="row">
                    <div class="col">
                        <InputText name="nim" v-model="data.post.nim" :placeholder-text="data.role=='mahasiswa'?'Masukkan NIM':'Masukkan NIPD'"></InputText>
                    </div>
                    <div class="col">
                        <button class="btn btn-secondary" v-bind:disabled="!data.post.nim" v-on:click.prevent="checkNimNipd(data.post.nim)">CARI</button>
                    </div>
                </div>
                <InputText name="nama" v-model="data.post.fullname" :placeholderText="'Nama Lengkap'" :disabled="true"></InputText>
                <InputEmail name="email" v-model="data.post.googleUser.googleEmail" :placeholderText="'Email TI'" :disabled="true"></InputEmail>
                <InputText name="telepon" v-model="data.post.telpNo" :placeholder-text = "'Nomor Telepon'"></InputText>
                <input type="submit" :value="isSubmitting ? 'Submitting...':'DAFTAR'" :disabled="isSubmitting? true:false" class="btn btn-primary"></input>
            </form>
        </div>
    </div>
    <br>
</template>

<script setup>
import * as yup from "yup";
import { useForm } from 'vee-validate';
import { reactive, computed } from "vue";
import { useAuthStore } from "@/stores"

import InputText from "../../components/fields/InputText.vue"
import InputEmail from "../../components/fields/InputEmail.vue"
import { useAlertStore } from "../../stores/alert.store";
import { watch } from "vue";

const authStore = useAuthStore()

let data = reactive({
    post:{
        username:sessionStorage.getItem('email')?sessionStorage.getItem('email').split('@')[0]:"",
        password:"",
        fullname:"",
        googleUser: {googleEmail: sessionStorage.getItem('email')?sessionStorage.getItem('email'):""},
        nim:"",
        modified:"",
        telpNo:"",
        active:true,
        hashValidation:"",
        foto:null,
        dir:null,
        mimetype:null,
        fileSize:null,
        hashtag:null,
        jwtToken:null,
        groups: []
    },
    role:"mahasiswa"
})

const validation = yup.object({
    nim: yup.string().required("NIM/NIPD is a required field").matches(/^[0-9]+$/, "NIM/NIPD only accept numbers"),
    telepon: yup.string().required().matches(/^[0-9]+$/, "Telepon only accept numbers"),
    email: yup.string().required().email(),
    nama: yup.string().required()
})
const {values, handleSubmit, isSubmitting, errors} = useForm({
    validationSchema: validation
})

const submit = handleSubmit(
    //if sukses
    async () => {
        data.post.modified = getCurrentDateTime()
        assignRole()
        await authStore.addUser(data.post)
        await authStore.loginWithEmail(data.post.googleUser.googleEmail)
    },
    errors => {
        const alertStore = useAlertStore()
        alertStore.error('Mohon lengkapi data!')
    }
)

async function checkNimNipd(id){
    if(data.role == "mahasiswa"){
        await authStore.findUserByNim(id)
    }else{
        await authStore.findUserByNidn(id)
    }
    data.post.fullname = authStore.account_info?authStore.account_info.nama:""
}

function getCurrentDateTime(){
    let currentdate = new Date();
    let year = currentdate.getFullYear()
    let month = (currentdate.getMonth()+1).toString().padStart(2,'0')
    let date = currentdate.getDate().toString().padStart(2,'0')
    let hour = currentdate.getHours().toString().padStart(2,'0')
    let minute = currentdate.getMinutes().toString().padStart(2,'0')
    let second = currentdate.getSeconds().toString().padStart(2,'0')
    return `${year}-${month}-${date} ${hour}:${minute}:${second}`
}

function assignRole() {
    data.post.groups = []
    if(data.role == "mahasiswa"){
        data.post.groups.push({id: 2,groupname: "MAHASISWA"})
    }else{
        data.post.groups.push({id: 3,groupname: "DOSEN"})
    }
}

const computedClassMhs = computed(
    () => ({
        'btn-outline-dark':'mahasiswa'!=data.role,
        'btn-dark':'mahasiswa'==data.role
    })
)
const computedClassDosen = computed(
    () => ({
        'btn-outline-dark':'dosen'!=data.role,
        'btn-dark':'dosen'==data.role
    })
)

watch(
    () => data.role,
    (newRole, oldRole) => {
        if(newRole != oldRole)
        data.post.fullname = ""
    }
)
</script>

<style scoped>
.btn.custom{
    text-align: left;
    width: 30%;
}
</style>