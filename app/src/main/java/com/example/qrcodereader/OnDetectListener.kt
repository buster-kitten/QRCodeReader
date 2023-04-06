package com.example.qrcodereader

interface OnDetectListener {
    fun onDetect(msg : String) //QRCodeAnalyzer에서 QR코드가 인식되었을 때 호출
}