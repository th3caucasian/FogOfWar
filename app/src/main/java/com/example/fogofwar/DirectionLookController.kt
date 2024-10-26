package com.example.fogofwar

class DirectionLookController {
    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)
    private val smoothing = 15
    private var azimuths = mutableListOf<Float>()
    private var avgAzimuth = 0F
}