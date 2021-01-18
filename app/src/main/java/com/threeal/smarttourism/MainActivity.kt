package com.threeal.smarttourism

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private var cameraHandler: CameraHandler? = null
    private var locationHandler: LocationHandler? = null
    private var rotationHandler: RotationHandler? = null
    private var arOverlayView: ArOverlayView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arOverlayView = ArOverlayView(this)

        cameraHandler = CameraHandler(this)
        locationHandler = LocationHandler(this)
        rotationHandler = RotationHandler(this)
    }

    override fun onResume() {
        super.onResume()

        arOverlayView?.start()

        cameraHandler?.start()
        locationHandler?.start()
        rotationHandler?.start()
    }

    override fun onPause() {
        super.onPause()

        locationHandler?.stop()
    }


    private fun checkPermission(type: String): Boolean {
        val permission = ActivityCompat.checkSelfPermission(this, type)
        return permission == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            CameraHandler.CAMERA_REQUEST_CODE_PERMISSIONS -> {
                if (CameraHandler.CAMERA_REQUIRED_PERMISSIONS.all { checkPermission(it) }) {
                    cameraHandler?.start()
                } else {
                    Toast.makeText(
                        this, "Camera permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            LocationHandler.LOCATION_REQUEST_CODE_PERMISSIONS -> {
                if (LocationHandler.LOCATION_REQUIRED_PERMISSIONS.all { checkPermission(it) }) {
                    locationHandler?.start()
                } else {
                    Toast.makeText(
                        this, "Location permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }
}