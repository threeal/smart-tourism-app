package com.threeal.smarttourism

import android.hardware.SensorManager
import android.opengl.Matrix

class ProjectionMatrix private constructor(
    private val data: FloatArray,
    val width: Float,
    val height: Float
) {
    companion object {
        fun fromRotationVectorAndLayout(
            rotationVector: FloatArray, width: Float, height: Float
        ): ProjectionMatrix {
            val rotationMatrix = FloatArray(16)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

            val ratio: Float = when {
                width < height -> {
                    width / height
                }
                else -> {
                    height / width
                }
            }

            val viewMatrix = FloatArray(16)
            Matrix.frustumM(
                viewMatrix, 0, -ratio, ratio,
                -1f, 1f, 0.5f, 10000f
            )

            val projectionMatrix = FloatArray(16)
            Matrix.multiplyMM(
                projectionMatrix, 0, viewMatrix, 0,
                rotationMatrix, 0
            )

            return ProjectionMatrix(projectionMatrix, width, height)
        }
    }

    fun toArray(): FloatArray {
        return data
    }
}