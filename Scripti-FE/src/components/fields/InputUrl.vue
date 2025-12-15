<template>
    <div>
        <input type="url" class="form-control" v-model="value" v-bind:placeholder="props.placeholderText">
        <span v-if="errorMessage" class="warn">{{ errorMessage }}</span>
    </div>
</template>

<script setup>
import { useField } from 'vee-validate';
import { defineProps } from 'vue';

const props = defineProps({
    name: String,
    placeholderText: String,
    modelValue: String
})

const {value, errorMessage} = useField(
    () => props.name,
    undefined,
    {syncVModel:true}
)
</script>

<style scoped>
.warn{
    color: crimson;
}
</style>