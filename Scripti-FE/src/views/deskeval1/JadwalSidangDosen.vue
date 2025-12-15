<template>
<Loading v-bind:isFetching="data.isFetching"/>
<div v-if="!data.isFetching" class="mt-2 p-4 border border-2 rounded">
    <div v-if="data.jadwalDosen.length==0" class="text-center">
        <h6>Anda tidak termuat di jadwal sidang manapun pada periode ini.</h6>
    </div>
    <div v-else>
        <JadwalDETable 
            v-if="data.jadwalDosen.length>0"
            v-bind:sidangDatas="data.jadwalDosen"
            v-bind:periodeData="data.jadwalDosen.periodeKolokium"
        />
    </div>
</div>
<br>
<br>
</template>

<script setup>
import { reactive, onBeforeMount } from 'vue';
import { useDosenStore } from '../../stores/dosen.store';
import { useAuthStore } from '../../stores/auth.store';

import Loading from '../../components/animation/Loading.vue';
import JadwalDETable from '../../components/pagination/JadwalDETable.vue';

const dosenStore = useDosenStore()
const authStore = useAuthStore()
let data = reactive({
    jadwalDosen:[],
    currentPeriode:null,
    isFetching:true
})

onBeforeMount(
    async () => {
        let periodeExists = await dosenStore.getCurrentActivePeriode()
        if(periodeExists){
            data.currentPeriode = dosenStore.periodesActive[0]
            let jadwalExists = await dosenStore.getJadwalSidangDosenByNidnAndPeriodeID(authStore.user.details.nim, data.currentPeriode.id)
            if(jadwalExists){
                data.jadwalDosen = dosenStore.jadwalSidangDosen
            }
        }
        data.isFetching=false
    }
)
</script>