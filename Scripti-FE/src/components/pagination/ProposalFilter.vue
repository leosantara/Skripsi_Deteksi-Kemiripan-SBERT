<template>
    <div class="dropdown">
        <button type="button" class="btn btn-outline-dark dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false" data-bs-auto-close="outside">
            Filter
        </button>
        <div class="dropdown-menu p-4 overflow-auto" style="height: 280px;">
            Filter berdasarkan:
            <h6 class="dropdown-header">Status</h6>
            <div class="form-check">
                <input class="form-check-input" v-model="statusFilter" type="checkbox" value="B" id="B">
                <label class="form-check-label" for="B">
                    Baru
                </label>
            </div>
            <div class="form-check">
                <input class="form-check-input" v-model="statusFilter" type="checkbox" value="V" id="V">
                <label class="form-check-label" for="V">
                    Valid
                </label>
            </div>
            <div class="form-check">
                <input class="form-check-input" v-model="statusFilter" type="checkbox" value="T" id="T">
                <label class="form-check-label" for="T">
                    Diterima
                </label>
            </div>
            <div class="form-check">
                <input class="form-check-input" v-model="statusFilter" type="checkbox" value="R" id="R">
                <label class="form-check-label" for="R">
                    Revisi
                </label>
            </div>
            <div class="form-check">
                <input class="form-check-input" v-model="statusFilter" type="checkbox" value="L" id="L">
                <label class="form-check-label" for="L">
                    Batal
                </label>
            </div>
            <div class="form-check">
                <input class="form-check-input" v-model="statusFilter" type="checkbox" value="K" id="K">
                <label class="form-check-label" for="K">
                    Ditolak
                </label>
            </div>
            <hr class="dropdown-divider">
            <h6 class="dropdown-header">Waktu Submit:</h6>
            <div class="form-check">
                <input class="form-check-input" type="radio" id="recent" value='recent' v-model="submitFilter" />
                <label class="form-check-label" for="recent">Bulan Ini</label>    
            </div>
            <div class="form-check">
                <input class="form-check-input" type="radio" id="all" value='all' v-model="submitFilter" />
                <label class="form-check-label" for="all">Semua</label>
            </div>
        </div>
    </div>
</template>

<script setup> 
import { defineModel } from 'vue';

const statusFilter = defineModel('statusFilter', {default:['B']})
const submitFilter = defineModel('submitFilter', {default:'all'})
</script>

<style scoped>
button{
    height: 42px;
}
</style>