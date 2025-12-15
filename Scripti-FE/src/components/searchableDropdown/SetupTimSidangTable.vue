<template>
    <table class="table table-striped table-hover">
        <thead>
            <tr>
                <th>Pilih</th>
                <th v-if="showDropdown"></th>
                <th v-for="header in props.tableHeaders">{{ `${new String(header).toUpperCase()}` }}</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="(tableData, index) in tableDatas" v-bind:key="index">
                <td>
                    <div class="form-check">
                        <input class="form-check-input" v-model="choosenItem" type="checkbox" :value="tableData.id?tableData.id:tableData">
                    </div>
                </td>
                <td v-if="showDropdown">
                    <!-- this part is for custom dropdown -->
                    <div class="dropdown">
                        <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Action
                        </button>
                        <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                            <!-- SETUP TIM SIDANG DE ONLY OPTION: -->
                            <a v-if="route.fullPath.includes('/setup-timSidang')" @click.prevent="assignKetua(tableData.id)" class="dropdown-item" href="#">Set sebagai Ketua Sidang</a>
                        </div>
                    </div>
                </td>
                <td v-for="header in props.tableHeaders">
                    {{ tableData[header] }}

                    <!-- incase you want to modified how the output looks, make sure that it is specified! -->
                    <span v-if="route.fullPath.includes('setup-timSidang') && tableData[header]==ketua" class="badge bg-primary">Ketua</span>
                </td>
            </tr>
        </tbody>
    </table>
</template>

<script setup>
import { reactive, computed, defineProps, defineModel, defineEmits } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute()
const choosenItem = defineModel('choosenItem', {default:[]})
const ketua = defineModel('ketua', {default:""})

const emit = defineEmits(['assignKetua'])
const props = defineProps({
    tableDatas:{
        type: Array,
        required:true
    },
    tableHeaders:{
        type: Array,
        required:true
    }
})

function assignKetua(dosenID){
    emit("assignKetua", dosenID)
}

const showDropdown = computed(
    () => {
        if(
            route.fullPath.includes('/setup-timSidang')
        ){return true}
        return false
    }
)
// const headers = computed(
//     () => {
//         //please refers to: https://stackoverflow.com/questions/5525795/does-javascript-guarantee-object-property-order
//         //in ES2015 and later non-integer keys will be returned in insertion order.
//         //since all the keys in our db entity is non-integer, this should be safe
//         let keys = Object.keys(props.tableDatas[0])
//         return keys
//     }
// )
</script>

<style scoped>
tr:hover{
    cursor: pointer;
    background: mediumseagreen !important;
}
tr:hover td{
    background: transparent;
}
</style>
