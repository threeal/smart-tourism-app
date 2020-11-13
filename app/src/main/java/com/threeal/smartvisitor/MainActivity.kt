package com.threeal.smartvisitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.*
import android.location.Location
import android.opengl.Matrix
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        private const val TAG = "MainActivity"

        private const val CAMERA_REQUEST_CODE_PERMISSIONS = 10
        private val CAMERA_REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private const val LOCATION_REQUEST_CODE_PERMISSIONS = 0
        private val LOCATION_REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private lateinit var bearingTextView: TextView
    private lateinit var locationTextView: TextView

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var sensorManager: SensorManager

    private lateinit var arOverlayView: ArOverlayView
    private lateinit var overlayView: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bearingTextView = findViewById(R.id.bearingTextView)
        locationTextView = findViewById(R.id.locationTexView)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                for (location in (locationResult ?: return).locations) {
                    updateLocation(location)
                }
            }
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        arOverlayView = ArOverlayView(this)
        overlayView = findViewById(R.id.overlayView)
    }

    override fun onResume() {
        super.onResume()

        if (cameraPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, CAMERA_REQUIRED_PERMISSIONS, CAMERA_REQUEST_CODE_PERMISSIONS
            )
        }

        if (locationPermissionsGranted()) {
            startLocation()
        } else {
            ActivityCompat.requestPermissions(
                this, LOCATION_REQUIRED_PERMISSIONS, LOCATION_REQUEST_CODE_PERMISSIONS
            )
        }

        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SENSOR_DELAY_NORMAL
        )

        if (arOverlayView.parent != null) {
            (arOverlayView.parent as ViewGroup).removeView(arOverlayView)
        }
        overlayView.addView(arOverlayView)
    }

    override fun onPause() {
        super.onPause()

        fusedLocationProvider.removeLocationUpdates(locationCallback)
    }

    private fun cameraPermissionsGranted() = CAMERA_REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED;
    }

    private fun locationPermissionsGranted() = LOCATION_REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE_PERMISSIONS -> {
                if (cameraPermissionsGranted()) {
                    startCamera()
                } else {
                    Toast.makeText(
                        this, "Camera permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            LOCATION_REQUEST_CODE_PERMISSIONS -> {
                if (locationPermissionsGranted()) {
                    startLocation()
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(cameraPreview.createSurfaceProvider())
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("MissingPermission")
    private fun startLocation() {
        fusedLocationProvider.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                updateLocation(location)
            }
        }

        val locationRequest = LocationRequest.create()?.apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationProvider.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateLocation(location: Location) {
        arOverlayView.updateCurrentLocation(location)
//        locationTextView.text = ("lat: ${location.latitude}\nlon: ${location.longitude}\n"
//                + "alt: ${location.altitude}\ntime: ${location.time}")
        locationTextView.text = ""
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        if (sensorEvent?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(16)
            getRotationMatrixFromVector(rotationMatrix, sensorEvent.values)

            val projectionMatrix = FloatArray(16)

            val ratio: Float = when {
                cameraPreview.width < cameraPreview.height -> {
                    cameraPreview.width.toFloat() / cameraPreview.height.toFloat()
                }
                else -> {
                    cameraPreview.height.toFloat() / cameraPreview.width.toFloat()
                }
            }

            Matrix.frustumM(
                projectionMatrix, 0, -ratio, ratio,
                -1f, 1f, 0.5f, 10000f
            )

            val rotatedProjectionMatrix = FloatArray(16)
            Matrix.multiplyMM(
                rotatedProjectionMatrix, 0, projectionMatrix, 0,
                rotationMatrix, 0
            )

            arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix)

//            val orientation = FloatArray(3)
//            getOrientation(rotatedProjectionMatrix, orientation)
//
//            val orientationDegree = DoubleArray(3)
//            orientation.forEachIndexed { index, element ->
//                orientationDegree[index] = Math.toDegrees(element.toDouble())
//            }
//
//            bearingTextView.text =
//                "${orientationDegree[0]}\n${orientationDegree[1]}\n${orientationDegree[2]}"
            bearingTextView.text = ""
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.w(TAG, "Orientation compass unreliable")
        }
    }
}