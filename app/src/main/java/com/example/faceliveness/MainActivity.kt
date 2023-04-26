package com.example.faceliveness

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.faceliveness.databinding.ActivityMainBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val requestPermissionLauncher by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            ::onPermissionResult
        )
    }
    private var isBlink :Boolean ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermissionLauncher
        setListeners()
    }



    private fun setListeners(){
        binding.uiBtnBlink.setOnClickListener {
            if ( checkPlayServices()){
                Log.e("cameraddddd","error${checkPlayServices()}")
                isBlink = true
                requestCameraPermission()
            }
        }
        binding.uiBtnSmile.setOnClickListener {
            isBlink = false
            requestCameraPermission()
        }
    }

    var errorDialog: Dialog? = null

    private fun checkPlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                if (errorDialog == null) {
                    errorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404)
                    errorDialog?.setCancelable(false)
                }
                if (!errorDialog?.isShowing!!) errorDialog?.show()
            }
        }
        return resultCode == ConnectionResult.SUCCESS
    }

    private fun requestCameraPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED){
            onPermissionResult(true)
        } else requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun onPermissionResult(result : Boolean){
        if (result){
            navigateToCameraView()
        }
    }

    private fun navigateToCameraView(){
        val intent = Intent(this,CameraActivity::class.java)
        intent.putExtra(KEY_LIVE_NESS,isBlink)
        startActivity(intent)
    }

    companion object{
        const val KEY_LIVE_NESS = " key.live.ness"
    }
}