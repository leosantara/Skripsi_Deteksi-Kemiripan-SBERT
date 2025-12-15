<template>
    <div class="overlay" v-if="props.isFetching">
        <div class="loading-text">Mohon tunggu...</div>
    </div>
</template>

<script setup>
import { defineProps } from 'vue';
let props = defineProps({
    isFetching:{
        type:Boolean,
        required:true
    }
})
</script>

<style scoped>
.overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(255, 255, 255, 0.5); /* Semi-transparent background */
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 999; /* Ensure it sits above other content */
}
.loading-text {
    color: #282c34; /* Text color */
    font-size: 2em;
    text-shadow: 2px 2px 4px rgba(255, 255, 255, 0.7);
    animation: fade 3s infinite; /* Fade animation */
}

@keyframes fade {
    0%, 100% {
        opacity: 0;
    }
    50% {
        opacity: 1;
    }
}
</style>