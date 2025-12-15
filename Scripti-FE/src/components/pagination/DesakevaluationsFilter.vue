<template>
    <div class="dropdown">
        <button type="button" class="btn btn-outline-dark dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false" data-bs-auto-close="outside">
            Filter
        </button>
        <div class="dropdown-menu p-4 overflow-auto" style="height: 280px;">
            Filter berdasarkan:
            <div v-if="route.fullPath.includes('adminpage')">
                <h6 class="dropdown-header">Periode Sidang:</h6>
                <div class="form-check">
                    <input class="form-check-input" type="radio" id="recent" value='recent' v-model="periodeFilter" />
                    <label class="form-check-label" for="recent">Periode Terbaru</label>    
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="radio" id="all" value='all' v-model="periodeFilter" />
                    <label class="form-check-label" for="all">Semua Periode</label>
                </div>
                <hr class="dropdown-divider">
            </div>
            <div>
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
            </div>

        </div>
    </div>
</template>

<script setup>
import { defineModel } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute()
const statusFilter = defineModel('statusFilter', {default:[]})
const periodeFilter = defineModel('periodeFilter', {default:'recent'})
</script>

<style scoped>
button{
    height: 42px;
}
</style>