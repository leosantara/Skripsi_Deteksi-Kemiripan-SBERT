<template>
<div class="pageNotFound">
    <h1>PAGE NOT FOUND</h1>
    <h6>Maaf, halaman yang Anda cari saat ini sedang tidak dapat diakses.</h6>
    <h6><button class="btn btn-link" @click="router.push('/')">Kembali Ke Home Page</button></h6>
</div>
</template>

<script setup>
import { router } from '../router';

</script>

<style scoped>
.pageNotFound{
    width: 100%;
    height: 200px;
    text-align: center;
    align-items: center;
    margin: auto;
    position: relative;
}
h1{
    position: relative;
    top: 50%;
}
h6{
    position: relative;
    top: 50%;
}
</style>