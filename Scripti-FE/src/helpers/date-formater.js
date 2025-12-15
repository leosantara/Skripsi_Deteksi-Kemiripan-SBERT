export default{
    //must refactor the way we handle date in the future, the BACKEND and FRONTEND side 
    //is mostly okay since we live in INDONESIA (WIB TIMEZONE)
    formatDate(unformatedDate){
        //MIGHT NEED TO REFACTOR THIS ONE, SINCE THE DATE RECEIVED FROM BACKEND IS LAGGING 7 HOURS BEHIND
        const myMonth = ["Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"];
    
        let myDate = new Date(unformatedDate)
        let year = myDate.getFullYear()
        let month = myMonth[myDate.getMonth()]
        let date = myDate.getDate()
    
        return `${date} ${month} ${year}`
    },
    
    formatDateTime(unformatedDateTime){
        //THIS WORK BECAUSE THE DATETIME INPUT WE RECEIVE FROM THE BACKEND IS ALREADY IN WIB
        const myMonth = ["Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"];
        const myDay = ["Minggu","Senin","Selasa","Rabu","Kamis","Jumat","Sabtu"];
    
        let myDatetime = new Date(unformatedDateTime).toISOString()
        let myDate = new Date(myDatetime.split('T')[0])

        let year = myDate.getFullYear()
        let month = myMonth[myDate.getMonth()]
        let date = myDate.getDate()
        let day = myDay[myDate.getDay()]
        let hour = unformatedDateTime.split('T')[1].split('.000')[0]
    
        return `${day}, ${date} ${month} ${year} pukul ${hour} WIB`
    },

    convertToWIB(unformatedDateTime){
        //THIS WORK BECAUSE WE MANUALLY CONVERTING TIME FROM UTC TO WIB
        let unformatedDateTimeStr = new Date(unformatedDateTime).toISOString()
        let dateTimeWIB = new Date(unformatedDateTimeStr)
        dateTimeWIB.setHours(dateTimeWIB.getHours()+7)
        return dateTimeWIB
    },

    convertToDateTimeInput(unformatedDateTime){
        //supaya di accept oleh HTML Input
        //THIS WORK BECAUSE THE DATETIME INPUT WE RECEIVE FROM THE BACKEND IS ALREADY IN WIB
        let utcDateStr = new Date(unformatedDateTime).toISOString()
        let datetimeArr = utcDateStr.split('T')
        let dateOnly = datetimeArr[0]
        let timeArr = datetimeArr[1].split(':')

        return `${dateOnly}T${timeArr[0]}:${timeArr[1]}`
    },

    convertToDateInput(unformatedDate){
        //supaya di accept oleh HTML Input
        //MIGHT NEED TO REFACTOR THIS ONE ALONG WITH THE formatDate() function
        //this is because the date sent by the backend is lagging 7 hours behind
        let utcDateStr = new Date(unformatedDate).toISOString()
        let defaultDateFormat = utcDateStr.split('T')[0]
        return defaultDateFormat
    }
}