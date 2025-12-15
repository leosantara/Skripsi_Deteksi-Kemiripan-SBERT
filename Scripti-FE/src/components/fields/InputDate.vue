<template>
    <div>
        <input 
            type="date" 
            class="form-control" 
            v-model="value" 
            v-bind:disabled="props.disabled" 
        >
        <span v-if="errorMessage" class="warn">{{ errorMessage }}</span>
    </div>
</template>

<script setup>
import { useField } from 'vee-validate';

const props = defineProps({
    name: String,
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