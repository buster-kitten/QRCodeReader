package com.example.qrcodereader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.qrcodereader.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    //카메라 권한 확인
    private val PERMISSIONS_REQUEST_CODE = 1
    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

    //뷰 바인딩
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (!hasPermissions(this)) {
            //카메라 권한 요청
            requestPermissions(PERMISSIONS_REQUIRED,PERMISSIONS_REQUEST_CODE)
        }else {
            startCamera() //권한 있을 시 카메라 시작
        }

    }

    //권한 유무 확인
    fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    //권한 요청 콜백 함수
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSIONS_REQUEST_CODE) {
            if(PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                Toast.makeText(this@MainActivity, "권한 요청이 승인되었습니다.", Toast.LENGTH_LONG).show()
                startCamera()
            }else {
                Toast.makeText(this@MainActivity, "권한 요청이 거부되었습니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private var isDetected = false

    override fun onResume() {
        //다시 사용자의 포커스가 MainActivity로 돌아와 QR코드 분석 수행
        super.onResume()
        isDetected = false
    }

    fun getImageAnalysis() : ImageAnalysis {

        val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
        val imageAnalysis = ImageAnalysis.Builder().build()

        //Analyzer 설정
        imageAnalysis.setAnalyzer(cameraExecutor, QRCodeAnalyzer(object : OnDetectListener {
                override fun onDetect(msg: String) {
                    //실시간 이미지분석을 잠시 막음
                    if(!isDetected) { //중복실행 방지
                        isDetected = true //데이터가 감지되었으므로 true로 바꿈
                        val intent = Intent(this@MainActivity, ResultActivity::class.java)
                        intent.putExtra("msg",msg)
                        startActivity(intent)
                    }
                }
            }))

        return imageAnalysis
    }

    //미리보기와 이미지 분석
    fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = getPreview()  //미리보기 객체 가져오기
            val imageAnalysis = getImageAnalysis()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            //후면카메라 선택
            cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageAnalysis)
            //미리보기 기능 선택

        }, ContextCompat.getMainExecutor(this))
    }

    //미리보기 객체 반환
    //activity_main.xml에서 id가 barcode_preview인 뷰의 SurfaceProvider
    fun getPreview() : Preview {
        val preview : Preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.barcodePreview.getSurfaceProvider())

        return preview
    }



}