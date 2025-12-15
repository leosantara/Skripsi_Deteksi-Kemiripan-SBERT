<template>
    <div>
        <input type="password" class="form-control" v-model="value" placeholder="password">
        <span v-if="errorMessage" class="warn">{{ errorMessage }}</span>
    </div>
</template>

<script setup>
import { useField } from 'vee-validate';
import { defineProps } from 'vue';

const props = defineProps({
    name: String,
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