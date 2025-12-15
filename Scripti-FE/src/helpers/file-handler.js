export default{
    convertBlobToFile(blob, fileName){
        return new File([blob], fileName, { type: blob.type });
    },

    downloadFileFromBlob(blob, fileName){
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');

        link.href = url;
        link.setAttribute('download', fileName); // Specify the file name
        // Append to the body and trigger click
        document.body.appendChild(link);
        link.click();

        // Clean up
        link.remove();
        window.URL.revokeObjectURL(url);
    },

    convertBinaryToFile(binaryString, fileName, mimeType){
        //since BE only sent binary string,
        //we will convert it to blob, and then convert it to file
        //that can be accepted by HTML Input
        
        const byteArray = this.binaryStringToByteArray(binaryString);
        const blob = new Blob([byteArray], { type: mimeType });
        const file = new File([blob], fileName, { type: blob.type });

        //console.log('file is ready! : ',file)
        return file
    },

    binaryStringToByteArray(binaryString) {
        // to convert binary string to byte array
        // kita tidak bisa langsung convert bin str ke file!
        const length = binaryString.length;
        const bytes = new Array(length);
        for (let i = 0; i < length; i++) {
            bytes[i] = binaryString.charCodeAt(i) & 0xFF; // Convert each character to byte
        }
        return new Uint8Array(bytes);
    }
}