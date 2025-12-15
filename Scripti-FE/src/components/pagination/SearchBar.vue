<template>
    <form class="form-inline">
        <div class="form-group">
            <div class="input-group">
                <input 
                    class="form-control" 
                    v-bind:placeholder="props.placeholderText" 
                    type="text" 
                    v-model="data.title" />
                <button 
                    class="btn btn-primary" 
                    @click.prevent = "$emit('search', data.title)">
                    Search
                </button>
            </div>
        </div>
    </form>
</template>

<script setup>
import { reactive, defineProps } from 'vue';

let props = defineProps({
    placeholderText:String
})
let data = reactive({
    title:""
})
</script>

<style scoped>
.form-control{
    width: 250px;
}

input, button{
    height: 42px;
}
</style>