<template>
    <div>
        <textarea 
            v-model="value" 
            class="form-control"
            rows="3" 
            v-bind:placeholder="props.placeholderText" 
            v-bind:disabled="props.disabled">
        </textarea>
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
    disabled: Boolean
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