let dosenMap = null

//manually mapping things until BE can handle the mapping
export default{
    setDosenMapByID(arrayOfDosenObj){
        let map = new Map()
        arrayOfDosenObj.forEach(dosen => {
            map.set(dosen.id, dosen)
        });
        return map
    },

    getDosenMapByID(arrayOfDosenObj){
        if(!dosenMap){
            dosenMap = this.setDosenMapByID(arrayOfDosenObj)
        }
        return dosenMap
    }
}