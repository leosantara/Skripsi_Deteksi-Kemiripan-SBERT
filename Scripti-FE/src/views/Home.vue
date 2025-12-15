<script setup>
// import { useAdminStore } from '../stores/admin.store';
// const adminStore = useAdminStore()

// async function getInput(){
//     let sidangID = window.prompt('ID SIDANG:')
//     await adminStore.checkIfDosenExistsOnSidang(sidangID)
// }
</script>

<template>
    <div id="homepageContent">
        <h1>Welcome to home page!</h1>
        <section id="sectionNews">
            <h1>Berkas Panduan dan Template</h1>
            <ol>
                <li><a href="https://drive.google.com/file/d/1gK13LdBK4Zjzpv4b1LrKX593rCCy7ctS/view?usp=sharing" target="_blank">Buku Panduan Tugas Akhir Informatika UKDW</a></li>
                <li><a href="https://docs.google.com/document/d/1PoX1p-tOiQXbfUHgEntVA7D5FpPrCU_W/edit?usp=sharing&ouid=111857110795667484309&rtpof=true&sd=true" target="_blank">Template Format Jurnal</a></li>
                <li><a href="https://docs.google.com/document/d/1lM103xwo9Ca_7kdJIHgjLogromAhsZHE/edit?usp=sharing&ouid=111857110795667484309&rtpof=true&sd=true" target="_blank">Template Format Skripsi</a></li>
            </ol>
        </section>
        <!-- <section id="sectionJadwal">

        </section>
        <section id="sectionOthers">

        </section> -->
        
        <!-- DO YOUR TEST HERE -->
        <!-- <hr>
        <br>
        <button @click="getInput()">Test Get Dosen For Sidang DE</button> -->
    </div>
</template>

<style scoped>
#sectionNews{
    font-family: 'Times New Roman', Times, serif;
    text-align: center;
    ol{
        list-style-type: none;
    }
    a{
        font-size: large;
        text-decoration: none;
    }
}

#sectionNews a:hover{
    text-decoration: underline;
}
</style>