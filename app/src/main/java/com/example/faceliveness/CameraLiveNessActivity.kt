/*
package com.example.faceliveness

import android.annotation.SuppressLint
import android.content.*
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.faceliveness.databinding.ActivityCameraBinding
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraLiveNessActivity:AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val liveNess = arrayListOf<Boolean>()
    private val spoofNess = arrayListOf<Boolean>()
    private val completedSeconds = arrayListOf<Int>()
    private var formattedTime : Int = 0
    private val timer : ArrayList<String> = arrayListOf(
        "246","247","248","257","258","268","357","358","368","468"
    )
    private var randomTime:String? = null
    private var isBlink: Boolean = false

    private val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    private fun getRandomTimerCount(): String{
        val r = Random()
        val index = r.nextInt(9)
        return timer[index]
    }

    private val volumeDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(CameraActivity.KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    binding.uiCameraCapture.simulateClick()
                }
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    binding.uiCameraCapture.simulateClick()
                }
            }
        }
    }
    private val smileTimer by lazy {
        object : CountDownTimer(10*1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                val seconds =(60 % (millisUntilFinished / 1000).toInt() )
                formattedTime = seconds
            }

            override fun onFinish() {
                checkBlinkNessProbability()
                formattedTime = 0
                completedSeconds.clear()
                binding.uiCameraCapture.visibility = View.VISIBLE
                binding.uiTvSmile.visibility = View.GONE
                liveNess.clear()
                spoofNess.clear()
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isBlink = intent.getBooleanExtra(MainActivity.KEY_LIVE_NESS,false)
        setUpView()
        setListeners()
    }
    private fun setListeners() {
        binding.uiIvClose.setOnClickListener { finish() }
    }

    private fun setUpView() {

        val filter = IntentFilter().apply { addAction(CameraActivity.KEY_EVENT_ACTION) }
        this.registerReceiver(volumeDownReceiver, filter)

        displayId = binding.uiCameraPreview.display?.displayId ?: -1

        updateCameraUi()

        setUpCamera()
        if (isBlink) binding.uiTvSmile.text = "BLINK NOW"
        else binding.uiTvSmile.text = "SMILE NOW"
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        this.unregisterReceiver(volumeDownReceiver)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Redraw the camera UI controls
        updateCameraUi()
    }

    */
/** Initialize CameraX, and prepare to bind the camera use cases  *//*

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({

            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Select lensFacing depending on the available cameras
            lensFacing = when {
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    */
/** Declare and bind preview, capture and analysis use cases *//*

    private fun bindCameraUseCases() {

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { binding.uiCameraPreview.display?.getRealMetrics(it) }
        Log.d(ContentValues.TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Log.d(ContentValues.TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = binding.uiCameraPreview.display?.rotation ?: Surface.ROTATION_0

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(
                    cameraExecutor,
                    LuminosityAnalyzer(this) { face ->
                        if (formattedTime != 0) onFaceDetected(face)
                    }
                )
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.uiCameraPreview.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(ContentValues.TAG, "Use case binding failed", exc)
        }
    }


    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - CameraActivity.RATIO_4_3_VALUE) <= abs(previewRatio - CameraActivity.RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun updateCameraUi() {
        // Listener for button used to capture photo
        binding.uiCameraCapture.setOnClickListener {
            startTimer()
        }
    }

    */
/** Returns true if the device has an available back camera. False otherwise *//*

    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    */
/** Returns true if the device has an available front camera. False otherwise *//*

    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    */
/**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     *//*

    private class LuminosityAnalyzer(
        val context: Context,
        val onFaceDetected : (Face) ->Unit,
    ) : ImageAnalysis.Analyzer {

        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val faceDetector = FaceDetection.getClient(highAccuracyOpts)

        */
/**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         *//*

        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
        override fun analyze(image: ImageProxy) {
            image.image?.let {
                faceDetector.process(InputImage.fromMediaImage(it, image.imageInfo.rotationDegrees))
                    .addOnSuccessListener { faces ->
                        faces.forEach { face ->
                            Log.d(
                                "TAG",
                                "analyze: ${face.leftEyeOpenProbability}   ${face.rightEyeOpenProbability}"
                            )
                            onFaceDetected(face)
                        }
                        image.close()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                        image.close()
                    }
            }
        }
    }
    private fun onImageCapture(){
        binding.uiCameraCapture.isEnabled = false

        imageCapture?.let { imageCapture ->
            val photoFile = getFile(this)

            val metadata = ImageCapture.Metadata().apply {
                isReversedHorizontal = lensFacing ==    CameraSelector.LENS_FACING_FRONT
            }
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            imageCapture.takePicture(
                outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                        Log.d(ContentValues.TAG,"photo capture succeeded:${savedUri}")
                        runOnUiThread {
                            binding.uiCameraCapture.isEnabled = true
                        }

                    }

                    override fun onError(exception: ImageCaptureException) {
                        binding.uiCameraCapture.isEnabled = true
                        Log.e(ContentValues.TAG,"photo capture failed :${exception}",exception)
                    }

                }
            )
        }
    }
    private fun startTimer(){
        randomTime = getRandomTimerCount()
        smileTimer.cancel()
        smileTimer.start()
        binding.uiCameraCapture.visibility = View.GONE
    }

    private fun checkBlinkNessProbability(){
        val smileCount = liveNess.count{it}
        val spoofCount = spoofNess.count{it}
        Log.d(ContentValues.TAG,"checksmileNessProbability : $liveNess")
        Log.d(ContentValues.TAG,"checkUnsmileNessProbability : $spoofNess")
        if (smileCount >= 2 && spoofCount >= 2){
            onImageCapture()
        } else showSnackBar("Sorry, Failed",true)
    }
    private fun formatSecondsInAscending(second:Int): Int {
        return when (second){
            9 -> 1
            8 -> 2
            7 -> 3
            6 -> 4
            5 -> 5
            4 -> 6
            3 -> 7
            2 -> 8
            1 -> 9
            else -> 0
        }
    }

    private fun showSnackBar(message : String, isError:Boolean){
        val snackBar = Snackbar.make(binding.uiCameraCapture,message,Snackbar.LENGTH_SHORT)

        if(isError){
            snackBar.setBackgroundTint(ContextCompat.getColor(this,R.color.red_A700))
        } else snackBar.setBackgroundTint(ContextCompat.getColor(this,R.color.green_A700))
        snackBar.show()

    }


    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val KEY_EVENT_ACTION ="key_event_action"
        private const val KEY_EVENT_EXTRA = "key_event_extra"
    }
}*/
