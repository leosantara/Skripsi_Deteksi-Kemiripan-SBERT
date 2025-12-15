<script setup>
import * as Yup from 'yup';
import { reactive } from 'vue';
import { useAuthStore } from '@/stores';
// import { googleSdkLoaded } from "vue3-google-login"
import { googleAuthCodeLogin } from "vue3-google-login";
import { useAlertStore } from '../../stores/alert.store';
import { router } from '@/router';

let data = reactive({
    submitted: false
})

async function onSubmit(values) {
    const authStore = useAuthStore();
    const { serverAuthCode, clientType } = values;
    try {
        data.submitted = true
        await authStore.login(serverAuthCode, clientType);
    } catch (error) {
        const alertStore = useAlertStore()
        alertStore.error(error)
    }finally{
        data.submitted = false
    }
}

const login = () => {
//   googleSdkLoaded((google) => {
//     google.accounts.oauth2.initCodeClient({
//       client_id: '118435807079-p3c6j2v8buos0emcst2olthij71krin6.apps.googleusercontent.com',
//       scope: 'https://www.googleapis.com/auth/userinfo.profile',
//       //https://stackoverflow.com/questions/74189161/google-identity-services-sdk-authorization-code-model-in-popup-mode-how-to-r
//       redirect_uri: 'postmessage',
//       callback: (response) => {
//         console.log("Handle the response", response)
//       }
//     }).requestCode()
//   })
    if(sessionStorage.getItem('email')){
        router.push("/account/signup")
    }else{
        googleAuthCodeLogin().then((response) => {
        // console.log("Handle the response", response)
        console.log("response code", response["code"])
        console.log("response scope", response["scope"])
        if(response["hd"] != "ti.ukdw.ac.id"){
            const alertStore = useAlertStore()
            alertStore.error("Hanya email yang berasal dari domain: @ti.ukdw.ac.id yang diizinkan masuk!")
            return
        }
        onSubmit({ serverAuthCode:response["code"], clientType :"web_app"} )
    })
    }
}
</script>

<template>
    <div class="card m-3">
        <h4 class="card-header">Login</h4>
        <div class="card-body">
            <button class="btn btn-primary" @click="login" :disabled="data.submitted?true:false">
                {{ !data.submitted?'Login With Google':'Please Wait...'}}
            </button>
        </div>
    </div>
</template>