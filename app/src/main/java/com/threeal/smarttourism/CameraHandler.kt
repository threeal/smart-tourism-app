package com.threeal.smarttourism

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_ar.*

class CameraHandler constructor(private val activity: Activity) {

    companion object {
        const val CAMERA_REQUEST_CODE_PERMISSIONS = 10
        val CAMERA_REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

    private fun checkPermission(type: String): Boolean {
        val permission = ActivityCompat.checkSelfPermission(activity, type)
        return permission == PackageManager.PERMISSION_GRANTED
    }

    fun start() {
        if (!CAMERA_REQUIRED_PERMISSIONS.all { checkPermission(it) }) {
            return ActivityCompat.requestPermissions(
                activity, CAMERA_REQUIRED_PERMISSIONS, CAMERA_REQUEST_CODE_PERMISSIONS
            )
        }

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            preview.setSurfaceProvider(activity.cameraPreview.surfaceProvider)

            cameraProvider.bindToLifecycle(activity as LifecycleOwner, cameraSelector, preview)
        }, ContextCompat.getMainExecutor(activity))
    }
}