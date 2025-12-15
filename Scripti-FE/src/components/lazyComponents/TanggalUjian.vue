<template>
    <span v-if=!data.tanggalSidang><i>Sedang memuat tanggal...</i></span>
    <span v-else>{{ data.tanggalSidang }}</span>
</template>

<script setup>
import { reactive, defineProps, onMounted } from 'vue';
import dateFormater from '../../helpers/date-formater';
import { useAdminStore } from '../../stores/admin.store';

const adminStore = useAdminStore()
let data = reactive({
    tanggalSidang:""
})
let props = defineProps({
    sidang:{
        type:Object,
        required:true
    }
})

onMounted(
    async () => {
        let tanggal = await adminStore.getTanggalSidangDE(props.sidang.sidangId)
        if(tanggal){
            data.tanggalSidang = dateFormater.formatDateTime(tanggal)
        }else{
            data.tanggalSidang = "Gagal memuat tanggal"
        }
    }
)
</script>