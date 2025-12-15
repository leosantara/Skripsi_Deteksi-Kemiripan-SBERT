<template>
    <div v-if="props.isFetching">
        <div
            class="loading">
        </div>
        <h4>Sedang Memuat Data...</h4>
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
.loading {
    width: 2rem;
    height: 2rem;
    border: 5px solid #f3f3f3;
    border-top: 6px solid mediumseagreen;
    border-radius: 100%;
    margin: auto;
    animation: spin 1s infinite linear;
}
@keyframes spin {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}
h4{
    vertical-align: middle;
    text-align: center;
}
</style>