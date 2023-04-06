package com.example.qrcodereader

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage


class QRCodeAnalyzer(val onDetectListener: OnDetectListener)  : ImageAnalysis.Analyzer{ // ❶
    //바코드 스캐닝 객체 생성
    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            //이미지 회전각도를 고려하여 입력 이미지 생성
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image) //이미지 분석
                .addOnSuccessListener { qrCodes ->
                    //리스너
                    for (qrCode in qrCodes) {
                        onDetectListener.onDetect(qrCode.rawValue ?: "")
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}