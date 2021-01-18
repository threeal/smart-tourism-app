package com.threeal.smarttourism

import android.app.Activity
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

interface RotationListener {
    companion object {
        private val rotationListeners = mutableListOf<RotationListener>()

        fun register(rotationListener: RotationListener) {
            if (rotationListeners.all { it != rotationListener }) {
                rotationListeners.add(rotationListener)
            }
        }

        fun trigger(rotationVector: FloatArray) {
            rotationListeners.forEach {
                it.onRotationVectorChanged(rotationVector)
            }
        }
    }

    fun onRotationVectorChanged(rotationVector: FloatArray)
}

class RotationHandler constructor(activity: Activity) : SensorEventListener {
    private val sensorManager = activity.getSystemService(SENSOR_SERVICE) as SensorManager

    fun start() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent?.let {
            if (it.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                RotationListener.trigger(it.values)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}