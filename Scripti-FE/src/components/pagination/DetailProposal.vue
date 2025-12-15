<template>
    <label class="fw-bold">Judul</label>
    <InputTextArea name="judul" v-model="props.proposalData.judul" :placeholderText="'Tuliskan judul proposal.'" :disabled="true"/>
    <label class="fw-bold">Latar Belakang</label>
    <MyCKEditor v-model:editorData="props.proposalData.latarBelakang" name="latarBelakang" v-bind:disabled=true />
    <label class="fw-bold">Rumusan Masalah</label>
    <MyCKEditor v-model:editorData="props.proposalData.rumusan" name="rumusan" v-bind:disabled=true />
    <label class="fw-bold">Batasan Masalah</label>
    <MyCKEditor v-model:editorData="props.proposalData.batasan" name="batasan" v-bind:disabled=true />
    <label class="fw-bold">Tujuan Penelitian</label>
    <MyCKEditor v-model:editorData="props.proposalData.tujuan" name="tujuan" v-bind:disabled=true />
    <label class="fw-bold">Manfaat Penelitian</label>
    <MyCKEditor v-model:editorData="props.proposalData.manfaat" name="manfaat" v-bind:disabled=true />
    <label class="fw-bold">Daftar Pustaka</label>
    <MyCKEditor v-model:editorData="props.proposalData.referensi" name="referensi" v-bind:disabled=true /> 
</template>

<script setup>
import { defineProps } from 'vue';
import MyCKEditor from '../texteditor/MyCKEditor.vue';
import InputTextArea from '../fields/InputTextArea.vue';

const props = defineProps({
    proposalData:{
        type:Object,
        required:true
    }
})
</script>