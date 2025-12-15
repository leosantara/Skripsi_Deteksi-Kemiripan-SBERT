<template>
    <div style="width: 30%;">
        <select class="form-select" v-model="yearFilter" aria-label="Default select example">
            <option value="" selected hidden>Tahun</option>
            <option v-for="year in data.years" :value="year">{{ year }}</option>
        </select>
    </div>
</template>

<script setup>
import { defineModel, onMounted, reactive } from 'vue';

const yearFilter = defineModel('yearFilter', {default:""})
let data = reactive({
    years:[],
    currentYear:""
})

onMounted(
    () => {
        let year = new Date().getFullYear()
        if(year>=2014){
            for(let i = 2014;i<=year;i++){
                data.years.push(i)
            }
        }
        data.currentYear = year
    }
)
</script>