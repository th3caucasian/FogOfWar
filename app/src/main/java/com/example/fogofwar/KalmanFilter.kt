package com.example.fogofwar

class KalmanFilter {
    private var x = 0f
    private var p = 1f
    private val q = 0.7f
    private val r = 0.4f
    private var k = 0f

    fun update(measurment: Float): Float {
        val xPrior = x
        val pPrior = p + q

        k = pPrior / (pPrior + r)

        x = xPrior + k * (measurment - xPrior)

        p = (1 - k) * pPrior

        return x
    }
}