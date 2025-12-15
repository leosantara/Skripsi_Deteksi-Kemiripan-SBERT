<template>
    <div>
        <input 
            type="email" 
            class="form-control" 
            v-model="value" 
            v-bind:placeholder="props.placeholderText"
            v-bind:disabled="props.disabled" 
        >
        <span v-if="errorMessage" class="warn">{{ errorMessage }}</span>
    </div>
</template>

<script setup>
import { useField } from 'vee-validate';
import { defineProps } from 'vue';

const props = defineProps({
    name: String,
    placeholderText: String,
    modelValue: String,
    disabled:Boolean
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