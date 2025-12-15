<template>
    <div class="dropdown">
        <button type="button" class="btn btn-outline-dark dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false" data-bs-auto-close="outside">
            Filter
        </button>
        <div class="dropdown-menu p-4 overflow-auto" style="height: 280px;">
            Filter berdasarkan:
            <h6 class="dropdown-header">Status</h6>
            <div>
                <div class="form-check">
                    <input class="form-check-input" type="radio" id="statusAll" :value=null v-model="statusFilter" />
                    <label class="form-check-label" for="statusAll">Semua</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="radio" id="statusTrue" :value=1 v-model="statusFilter" />
                    <label class="form-check-label" for="statusTrue">Mahasiswa Aktif</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="radio" id="statusFalse" :value=0 v-model="statusFilter" />
                    <label class="form-check-label" for="statusFalse">Mahasiswa Nonaktif</label>
                </div>
            </div>
            <hr class="dropdown-divider">
            <h6 class="dropdown-header">Tugas Akhir</h6>
            <div>
                <div class="form-check">
                    <input class="form-check-input" type="radio" id="taAll" :value=null v-model="taFilter" />
                    <label class="form-check-label" for="taAll">Semua</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="radio" id="taTrue" :value=1 v-model="taFilter" />
                    <label class="form-check-label" for="taTrue">Sedang Mengikuti</label>    
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="radio" id="taFalse" :value=0 v-model="taFilter" />
                    <label class="form-check-label" for="taFalse">Tidak Sedang Mengikuti</label>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { reactive, defineModel } from 'vue';
let data = reactive({
    showFilter: false,
})
const statusFilter = defineModel('statusFilter', {default:null})
const taFilter = defineModel('taFilter', {default:null})
</script>

<style scoped>
button{
    height: 42px;
}
</style>