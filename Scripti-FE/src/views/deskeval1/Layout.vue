<template>
    <SubmenuSidangDE 
        v-if="route.fullPath.includes('/deskevaluation/adminpage/periode/')"
    />
    <SubmenuSidangDeAdmin 
        v-if="route.fullPath.includes('/deskevaluation/adminpage/proposal-mahasiswa/')" 
    />
    <SubmenuSidangDeDosen
        v-if="route.fullPath.includes('/deskevaluation/dosenpage')"
    />
    <RouterView>

    </RouterView>
</template>

<script setup>
import { RouterView } from 'vue-router';
import SubmenuSidangDE from '../../components/submenu/SubmenuSidangDE.vue';
import SubmenuSidangDeAdmin from '../../components/submenu/SubmenuSidangDeAdmin.vue';
import SubmenuSidangDeDosen from '../../components/submenu/SubmenuSidangDeDosen.vue';
import { useRoute } from 'vue-router';

const route = useRoute()
</script>